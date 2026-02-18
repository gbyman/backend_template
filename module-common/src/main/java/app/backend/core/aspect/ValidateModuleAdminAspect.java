package app.backend.core.aspect;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import app.backend.core.annotation.PrimaryKey;
import app.backend.core.annotation.ValidateModuleAdmin;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.ModuleDiv;
import app.backend.core.constants.TableAndPkInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모듈별 관리자 권한 검증 AOP
 *
 * <p>{@link ValidateModuleAdmin} 어노테이션이 붙은 메서드의 실행 전에 권한을 자동으로 검증합니다.
 *
 * <h2>동작 방식</h2>
 *
 * <ol>
 *   <li><strong>moduleDivVal 추출</strong>
 *       <ul>
 *         <li>파라미터 모드: 요청 파라미터에서 moduleDivVal 필드/값 추출
 *         <li>DB 조회 모드: @PrimaryKey로 표시된 값을 사용하여 DB 조회
 *       </ul>
 *   <li><strong>권한 검증</strong>
 *       <ul>
 *         <li>ModuleDiv.COMMON: 어떤 모듈 관리자든 1개 이상 권한 있으면 허용
 *         <li>그 외: 해당 모듈의 관리자 권한이 있어야 허용
 *       </ul>
 *   <li><strong>권한 없으면</strong> → BizException(403 FORBIDDEN) 발생
 * </ol>
 *
 * <h2>사용 예시</h2>
 *
 * <pre>
 * // 파라미터 모드
 * &#64;ValidateModuleAdmin
 * &#64;PostMapping("/codes")
 * public ResponseEntity&lt;?&gt; createCode(&#64;RequestBody CodeCreateReqDto reqDto) {
 *     // reqDto.moduleDivVal을 사용하여 권한 검증
 *     return ResponseEntity.ok().build();
 * }
 *
 * // DB 조회 모드
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;DeleteMapping("/codes/{codeSeq}")
 * public ResponseEntity&lt;?&gt; deleteCode(&#64;PathVariable &#64;PrimaryKey Long codeSeq) {
 *     // DB에서 codeSeq로 moduleDivVal 조회 후 권한 검증
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <h2>권한 체계 요구사항</h2>
 *
 * <p>이 AOP를 사용하려면 권한이 다음 형식으로 구성되어야 합니다:
 *
 * <pre>
 * ROLE_[MODULE]_ADMIN
 *
 * 예시:
 * - ROLE_MODULE_A_ADMIN
 * - ROLE_MODULE_B_ADMIN
 * - ROLE_MODULE_C_ADMIN
 * </pre>
 *
 * <h2>성능 최적화</h2>
 *
 * <p>DB 조회 모드를 사용할 때는 캐싱을 적용하여 성능을 최적화할 수 있습니다:
 *
 * <pre>
 * &#64;Cacheable(value = "moduleDivCache",
 *            key = "#tableAndPkInfo.name() + ':' + #pkValue")
 * private ModuleDiv fetchModuleDivValFromDatabase(...) {
 *     // DB 조회 로직
 * }
 * </pre>
 *
 * <h2>주의사항</h2>
 *
 * <ul>
 *   <li>모든 테이블은 {@code module_div_val} 컬럼을 가져야 함
 *   <li>DB 조회 시 추가 쿼리 발생 → 성능 영향 고려
 *   <li>Reflection 사용으로 약간의 성능 오버헤드 존재
 *   <li>단일 모듈 시스템에서는 과도한 설계
 * </ul>
 *
 * @see ValidateModuleAdmin
 * @see PrimaryKey
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ValidateModuleAdminAspect {

    /** moduleDivVal 필드명 */
    private static final String MODULE_FIELD_NAME = "moduleDivVal";

    /** DB 조회 SQL 템플릿 */
    private static final String MODULE_SQL_FORMAT = "SELECT module_div_val FROM %s WHERE %s = ?";

    private final JdbcTemplate jdbcTemplate;

    /**
     * @ValidateModuleAdmin 어노테이션이 붙은 메서드 실행 전 권한 검증
     *
     * @param joinPoint AOP JoinPoint
     * @param validateModuleAdmin ValidateModuleAdmin 어노테이션
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("@annotation(validateModuleAdmin)")
    public Object validatePermission(
            ProceedingJoinPoint joinPoint, ValidateModuleAdmin validateModuleAdmin)
            throws Throwable {

        // 파라미터가 없는 경우 검증 생략
        if (joinPoint.getArgs() == null || joinPoint.getArgs().length == 0) {
            log.debug("No arguments found, skipping module admin validation");
            return joinPoint.proceed();
        }

        TableAndPkInfo tableAndPkInfo = validateModuleAdmin.tableAndPkInfo();

        // moduleDivVal 추출 (파라미터 모드 or DB 조회 모드)
        ModuleDiv moduleDivVal =
                TableAndPkInfo.DUMMY.equals(tableAndPkInfo)
                        ? extractModuleDivValFromArgs(joinPoint.getArgs())
                        : fetchModuleDivValFromDatabase(joinPoint, tableAndPkInfo);

        // 권한 검증
        validatePermissions(moduleDivVal);

        log.debug(
                "Module admin validation passed - module: {}, method: {}",
                moduleDivVal,
                joinPoint.getSignature().getName());

        return joinPoint.proceed();
    }

    /**
     * 전달된 파라미터에서 moduleDivVal 추출 (파라미터 모드)
     *
     * <p>다음 순서로 moduleDivVal을 찾습니다:
     *
     * <ol>
     *   <li>파라미터가 ModuleDiv 타입인 경우
     *   <li>파라미터 객체의 moduleDivVal 필드
     * </ol>
     *
     * @param args 메서드 파라미터 배열
     * @return 추출된 ModuleDiv
     * @throws BizException moduleDivVal을 찾을 수 없는 경우
     */
    private ModuleDiv extractModuleDivValFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            // 파라미터가 직접 ModuleDiv 타입인 경우
            if (arg instanceof ModuleDiv) {
                return (ModuleDiv) arg;
            }

            // 파라미터 객체의 moduleDivVal 필드에서 추출
            ModuleDiv moduleDiv = extractModuleDivFromObject(arg);
            if (moduleDiv != null) {
                return moduleDiv;
            }
        }

        throw new BizException(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "moduleDivVal 파라미터를 찾을 수 없습니다. " + "요청 파라미터에 moduleDivVal 필드가 포함되어야 합니다.");
    }

    /**
     * 객체의 moduleDivVal 필드에서 값 추출
     *
     * @param arg 파라미터 객체
     * @return 추출된 ModuleDiv (없으면 null)
     */
    private ModuleDiv extractModuleDivFromObject(Object arg) {
        try {
            Field field = arg.getClass().getDeclaredField(MODULE_FIELD_NAME);
            field.setAccessible(true);

            Object moduleDivVal = field.get(arg);
            if (moduleDivVal == null) {
                return null;
            }

            // ModuleDiv 타입 또는 String 타입 처리
            if (moduleDivVal instanceof ModuleDiv) {
                return (ModuleDiv) moduleDivVal;
            } else if (moduleDivVal instanceof String) {
                return ModuleDiv.valueOf((String) moduleDivVal);
            }

            return null;

        } catch (NoSuchFieldException e) {
            // moduleDivVal 필드가 없는 경우
            return null;
        } catch (IllegalAccessException e) {
            throw new BizException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FIELD_ACCESS_ERROR",
                    "moduleDivVal 필드에 접근할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * DB에서 moduleDivVal 조회 (DB 조회 모드)
     *
     * <p>@PrimaryKey로 표시된 값을 사용하여 테이블에서 module_div_val을 조회합니다.
     *
     * @param joinPoint AOP JoinPoint
     * @param tableAndPkInfo 테이블 및 PK 정보
     * @return 조회된 ModuleDiv
     * @throws BizException PK 값이 없거나 DB 조회 실패 시
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    private ModuleDiv fetchModuleDivValFromDatabase(
            ProceedingJoinPoint joinPoint, TableAndPkInfo tableAndPkInfo) {

        // @PrimaryKey로 표시된 값 추출
        Object pkValue = extractPrimaryKeyFromArgs(joinPoint);

        if (pkValue == null) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    "MISSING_PRIMARY_KEY",
                    "@PrimaryKey가 지정된 파라미터를 찾을 수 없습니다.");
        }

        // SQL 생성 및 실행
        String sql =
                String.format(
                        MODULE_SQL_FORMAT,
                        tableAndPkInfo.getTableName(),
                        tableAndPkInfo.getPkColumnName());

        log.debug("Fetching moduleDivVal from DB - SQL: {}, PK: {}", sql, pkValue);

        try {
            String moduleDivValStr = jdbcTemplate.queryForObject(sql, String.class, pkValue);

            if (StringUtils.isBlank(moduleDivValStr)) {
                throw new BizException(
                        HttpStatus.NOT_FOUND,
                        "MODULE_DIV_NOT_FOUND",
                        "해당 데이터의 moduleDivVal을 찾을 수 없습니다.");
            }

            return ModuleDiv.valueOf(moduleDivValStr);

        } catch (Exception e) {
            log.error("Failed to fetch moduleDivVal from database", e);
            throw new BizException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_QUERY_ERROR",
                    "moduleDivVal 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * @PrimaryKey로 표시된 값 추출
     *
     * <p>다음 순서로 PK 값을 찾습니다:
     *
     * <ol>
     *   <li>메서드 파라미터에 @PrimaryKey 어노테이션이 붙은 경우
     *   <li>파라미터 객체의 필드에 @PrimaryKey 어노테이션이 붙은 경우
     * </ol>
     *
     * @param joinPoint AOP JoinPoint
     * @return 추출된 PK 값
     * @throws BizException @PrimaryKey를 찾을 수 없는 경우
     */
    private Object extractPrimaryKeyFromArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        // 1. 메서드 파라미터에서 @PrimaryKey 찾기
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(PrimaryKey.class)) {
                return args[i];
            }
        }

        // 2. 파라미터 객체의 필드에서 @PrimaryKey 찾기
        for (Object arg : args) {
            if (arg != null) {
                Object pkValue = extractPrimaryKeyFromFields(arg);
                if (pkValue != null) {
                    return pkValue;
                }
            }
        }

        throw new BizException(
                HttpStatus.BAD_REQUEST,
                "MISSING_PRIMARY_KEY",
                "@PrimaryKey 어노테이션이 지정된 파라미터 또는 필드를 찾을 수 없습니다.");
    }

    /**
     * 객체의 필드에서 @PrimaryKey로 표시된 값 추출
     *
     * @param dto 파라미터 객체
     * @return 추출된 PK 값 (없으면 null)
     */
    private Object extractPrimaryKeyFromFields(Object dto) {
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                field.setAccessible(true);
                try {
                    return field.get(dto);
                } catch (IllegalAccessException e) {
                    throw new BizException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "FIELD_ACCESS_ERROR",
                            "@PrimaryKey 필드에 접근할 수 없습니다: " + field.getName());
                }
            }
        }
        return null;
    }

    /**
     * 로그인 사용자가 해당 모듈의 관리자 권한을 가지고 있는지 검증
     *
     * <p>권한 검증 로직:
     *
     * <ul>
     *   <li><strong>공통 모듈 (COMMON)</strong>: 어떤 모듈 관리자든 1개 이상 권한 있으면 허용
     *   <li><strong>특정 모듈</strong>: 해당 모듈의 관리자 권한 필요
     * </ul>
     *
     * <p><strong>예시:</strong>
     *
     * <pre>
     * // MODULE_A 관리자가 MODULE_A 데이터 수정: ✅ 허용
     * // MODULE_A 관리자가 MODULE_B 데이터 수정: ❌ 거부
     * // MODULE_A 관리자가 COMMON 데이터 수정: ✅ 허용
     * </pre>
     *
     * @param moduleDiv 검증할 모듈 구분
     * @throws BizException 권한이 없는 경우
     */
    private void validatePermissions(ModuleDiv moduleDiv) {
        boolean hasPermission;

        if (ModuleDiv.COMMON.equals(moduleDiv)) {
            // 공통 모듈: 어떤 모듈 관리자든 하나라도 권한 있으면 허용
            hasPermission = hasAnyAdminAuth();
        } else {
            // 특정 모듈: 해당 모듈의 관리자 권한 필요
            hasPermission = hasAdminAuthByModule(moduleDiv);
        }

        if (!hasPermission) {
            log.warn("Access denied - module: {}, user: {}", moduleDiv, getCurrentUserId());
            throw new BizException(
                    HttpStatus.FORBIDDEN, "ACCESS_DENIED", "해당 모듈에 대한 관리자 권한이 없습니다.");
        }
    }

    /**
     * 특정 모듈의 관리자 권한 확인
     *
     * <p><strong>TODO: 프로젝트에 맞게 구현 필요</strong>
     *
     * <p>Spring Security 예시:
     *
     * <pre>
     * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     * if (auth == null) return false;
     *
     * String requiredRole = "ROLE_" + moduleDiv.name() + "_ADMIN";
     * return auth.getAuthorities().stream()
     *     .anyMatch(authority -> authority.getAuthority().equals(requiredRole));
     * </pre>
     *
     * @param moduleDiv 모듈 구분
     * @return 권한 있으면 true
     */
    private boolean hasAdminAuthByModule(ModuleDiv moduleDiv) {
        // TODO: 실제 권한 체크 로직 구현
        // 예시: SecurityContextHolder에서 권한 확인
        log.warn(
                "hasAdminAuthByModule not implemented - module: {}. "
                        + "Returning true for demo purposes. "
                        + "Implement real authorization logic!",
                moduleDiv);
        return true; // 임시 구현
    }

    /**
     * 어떤 모듈이든 관리자 권한이 하나라도 있는지 확인
     *
     * <p><strong>TODO: 프로젝트에 맞게 구현 필요</strong>
     *
     * <p>Spring Security 예시:
     *
     * <pre>
     * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     * if (auth == null) return false;
     *
     * return Arrays.stream(ModuleDiv.values())
     *     .anyMatch(module -> {
     *         String role = "ROLE_" + module.name() + "_ADMIN";
     *         return auth.getAuthorities().stream()
     *             .anyMatch(authority -> authority.getAuthority().equals(role));
     *     });
     * </pre>
     *
     * @return 권한 있으면 true
     */
    private boolean hasAnyAdminAuth() {
        // TODO: 실제 권한 체크 로직 구현
        log.warn(
                "hasAnyAdminAuth not implemented. "
                        + "Returning true for demo purposes. "
                        + "Implement real authorization logic!");
        return true; // 임시 구현
    }

    /**
     * 현재 로그인 사용자 ID 조회
     *
     * <p><strong>TODO: 프로젝트에 맞게 구현 필요</strong>
     *
     * @return 사용자 ID
     */
    private String getCurrentUserId() {
        // TODO: 실제 사용자 ID 조회 로직 구현
        return "UNKNOWN";
    }
}
