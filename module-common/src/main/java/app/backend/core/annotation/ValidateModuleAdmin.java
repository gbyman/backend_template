package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.constants.TableAndPkInfo;

/**
 * 모듈별 관리자 권한 검증 어노테이션
 *
 * <p>멀티 모듈 시스템에서 로그인 사용자가 해당 모듈의 관리자 권한을 가지고 있는지 자동으로 검증합니다.
 *
 * <p><strong>2가지 검증 모드:</strong>
 *
 * <ol>
 *   <li><strong>파라미터 모드 (tableAndPkInfo = DUMMY)</strong> - 요청 파라미터에서 moduleDivVal 추출
 *   <li><strong>DB 조회 모드 (tableAndPkInfo 지정)</strong> - DB에서 moduleDivVal 조회 후 검증
 * </ol>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>Module A/B/C처럼 여러 모듈이 통합된 시스템
 *   <li>멀티 테넌트 시스템 (테넌트별 관리자 권한)
 *   <li>조직별 권한 분리 시스템 (본사/지사별 관리자)
 * </ul>
 *
 * <h2>모드 1: 파라미터 모드 (기본)</h2>
 *
 * <p>요청 파라미터(DTO 또는 직접 전달)에서 moduleDivVal을 추출하여 권한을 검증합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // DTO에 moduleDivVal 필드가 있는 경우
 * public class CodeCreateReqDto {
 *     private ModuleDiv moduleDivVal;  // 모듈 구분 값
 *     private String codeName;
 *     // ...
 * }
 *
 * &#64;ValidateModuleAdmin  // tableAndPkInfo 생략 시 DUMMY (파라미터 모드)
 * &#64;PostMapping("/codes")
 * public ResponseEntity&lt;?&gt; createCode(&#64;RequestBody CodeCreateReqDto reqDto) {
 *     // ValidateModuleAdminAspect가 자동으로:
 *     // 1. reqDto.moduleDivVal 추출 (예: MODULE_A)
 *     // 2. 로그인 사용자가 MODULE_A 관리자인지 확인
 *     // 3. 권한 없으면 BizException 발생 (403 FORBIDDEN)
 *     codeService.createCode(reqDto);
 *     return ResponseEntity.ok().build();
 * }
 *
 * // moduleDivVal을 직접 파라미터로 전달하는 경우
 * &#64;ValidateModuleAdmin
 * &#64;GetMapping("/codes")
 * public ResponseEntity&lt;?&gt; getCodes(&#64;RequestParam ModuleDiv moduleDivVal) {
 *     // moduleDivVal 파라미터를 사용하여 권한 검증
 *     List&lt;Code&gt; codes = codeService.getCodesByModule(moduleDivVal);
 *     return ResponseEntity.ok(codes);
 * }
 * </pre>
 *
 * <h2>모드 2: DB 조회 모드</h2>
 *
 * <p>DB에서 해당 데이터의 moduleDivVal을 조회하여 권한을 검증합니다. 수정/삭제 시 유용합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 파라미터에 &#64;PrimaryKey 사용
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;DeleteMapping("/codes/{codeSeq}")
 * public ResponseEntity&lt;?&gt; deleteCode(&#64;PathVariable &#64;PrimaryKey Long codeSeq) {
 *     // ValidateModuleAdminAspect가 자동으로:
 *     // 1. TableAndPkInfo.CODE에서 테이블명(tn_system_code_group), PK 컬럼명(code_group_seq) 조회
 *     // 2. DB 쿼리 실행: SELECT module_div_val FROM tn_system_code_group WHERE code_group_seq = ?
 *     // 3. 조회된 moduleDivVal로 권한 검증
 *     codeService.deleteCode(codeSeq);
 *     return ResponseEntity.ok().build();
 * }
 *
 * // DTO 필드에 &#64;PrimaryKey 사용
 * public class CodeUpdateReqDto {
 *     &#64;PrimaryKey
 *     private Long codeSeq;  // PK 값
 *     private String codeName;
 *     // moduleDivVal은 없음 (DB에서 조회)
 * }
 *
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;PutMapping("/codes")
 * public ResponseEntity&lt;?&gt; updateCode(&#64;RequestBody CodeUpdateReqDto reqDto) {
 *     // reqDto.codeSeq를 사용하여 DB에서 moduleDivVal 조회 후 권한 검증
 *     codeService.updateCode(reqDto);
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <h2>공통 모듈 처리</h2>
 *
 * <p>moduleDivVal이 {@code ModuleDiv.COMMON}인 경우, 어떤 모듈 관리자든 하나라도 권한이 있으면 접근 가능합니다.
 *
 * <pre>
 * // moduleDivVal = COMMON인 데이터
 * &#64;ValidateModuleAdmin
 * &#64;PostMapping("/settings")
 * public ResponseEntity&lt;?&gt; createSetting(&#64;RequestBody SettingReqDto reqDto) {
 *     // reqDto.moduleDivVal = COMMON
 *     // → MODULE_A, MODULE_B, MODULE_C 관리자 모두 접근 가능
 *     settingService.createSetting(reqDto);
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <h2>동작 원리</h2>
 *
 * <ol>
 *   <li><strong>moduleDivVal 추출</strong>
 *       <ul>
 *         <li>파라미터 모드: 요청 파라미터에서 moduleDivVal 필드/값 추출
 *         <li>DB 조회 모드: &#64;PrimaryKey로 표시된 값을 사용하여 DB 조회
 *       </ul>
 *   <li><strong>권한 검증</strong>
 *       <ul>
 *         <li>ModuleDiv.COMMON: 어떤 모듈 관리자든 1개 이상 권한 있으면 허용
 *         <li>그 외: 해당 모듈의 관리자 권한이 있어야 허용
 *       </ul>
 *   <li><strong>권한 없으면</strong> → BizException(403 FORBIDDEN, "ACCESS_DENIED") 발생
 * </ol>
 *
 * <h2>주의사항</h2>
 *
 * <ul>
 *   <li><strong>단일 모듈 시스템에서는 사용 불필요</strong> - 일반 Spring Security로 충분
 *   <li>DB 조회 모드는 추가 쿼리 발생 → 성능을 위해 캐싱 권장
 *   <li>모든 테이블은 {@code module_div_val} 컬럼을 가져야 함
 *   <li>권한 체계는 {@code ROLE_[MODULE]_ADMIN} 형식으로 구성 필요
 * </ul>
 *
 * <h2>필요한 설정</h2>
 *
 * <pre>
 * // 1. ValidateModuleAdminAspect Bean 등록 (자동 등록됨)
 * // 2. SecurityUtils 확장 (hasAdminAuthByModule 메서드 추가)
 * // 3. 권한 체계 설정
 * //    - ROLE_MODULE_A_ADMIN
 * //    - ROLE_MODULE_B_ADMIN
 * //    - ROLE_MODULE_C_ADMIN
 * </pre>
 *
 * @see PrimaryKey
 * @see TableAndPkInfo
 * @see app.backend.core.constants.ModuleDiv
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateModuleAdmin {

    /**
     * 테이블 및 PK 정보
     *
     * <p><strong>기본값: TableAndPkInfo.DUMMY</strong> (파라미터 모드)
     *
     * <p>DUMMY가 아닌 값을 지정하면 DB 조회 모드로 동작합니다.
     *
     * @return 테이블 및 PK 정보
     */
    TableAndPkInfo tableAndPkInfo() default TableAndPkInfo.DUMMY;
}
