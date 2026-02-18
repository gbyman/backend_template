package app.backend.core.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.backend.core.base.vo.ReqContextVo;
import app.backend.core.entity.SysLog;
import app.backend.core.enums.LogCategory;
import app.backend.core.property.LoggingProperties;
import app.backend.core.repository.SysLogRepository;
import app.backend.core.utils.ReqContextUtils;
import app.backend.core.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DB 기반 시스템 접근 로그 AOP
 *
 * <p>모든 컨트롤러 메서드 실행 시 DB에 로그를 저장합니다.
 *
 * <p><strong>저장 정보:</strong>
 *
 * <ul>
 *   <li>요청 ID (UUID)
 *   <li>요청 URI 및 HTTP 메서드
 *   <li>로그 카테고리 (PAGE_VIEW, API_CALL, FILE_DOWNLOAD, AUTH_LOGIN, AUTH_LOGOUT, ERROR)
 *   <li>클라이언트 IP 및 사용자 ID
 *   <li>요청 파라미터 (Query String, JSON)
 *   <li>요청/응답 시간 및 실행 시간
 *   <li>응답 상태 코드 및 에러 메시지
 * </ul>
 *
 * <p><strong>로그 카테고리 자동 분류:</strong>
 *
 * <ul>
 *   <li>ERROR: STATUS_CODE >= 400
 *   <li>AUTH_LOGIN: URI contains "/login"
 *   <li>AUTH_LOGOUT: URI contains "/logout"
 *   <li>FILE_DOWNLOAD: URI contains "/download" or "/files/"
 *   <li>API_CALL: URI starts with "/api/"
 *   <li>PAGE_VIEW: GET method and not API
 * </ul>
 *
 * <p><strong>제외 URL 설정:</strong>
 *
 * <pre>
 * logging:
 *   aspect:
 *     enabled: true
 *     exclude-urls:
 *       - /actuator/**
 *       - /health
 *       - /swagger-ui/**
 *       - /v3/api-docs/**
 * </pre>
 *
 * <p><strong>비동기 처리:</strong>
 *
 * <ul>
 *   <li>@Async로 비동기 저장 (API 응답 시간에 영향 없음)
 *   <li>DB 저장 실패가 비즈니스 로직에 영향 주지 않음
 * </ul>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>로그 테이블 크기 증가 → 월별 파티셔닝 사용 중 (tb_sys_log.sql 참고)
 *   <li>개인정보 포함 URL은 제외 설정 필수
 *   <li>비동기 처리로 ThreadLocal 값 사용 시 주의
 *   <li>에러 추적은 MDC + 로그 파일 사용 권장 (RequestContextInterceptor)
 * </ul>
 *
 * @see app.backend.core.entity.SysLog
 * @see app.backend.core.enums.LogCategory
 * @see app.backend.core.property.LoggingProperties
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(
        prefix = "logging.aspect",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DatabaseLoggingAspect {

    private final SysLogRepository sysLogRepository;
    private final LoggingProperties loggingProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 컨트롤러 메서드 실행 시 DB에 로그 저장
     *
     * <p>포인트컷: app.backend.app 패키지의 모든 Controller 클래스의 모든 메서드
     *
     * @param joinPoint 조인 포인트
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("execution(* app.backend.app..*Controller.*(..))")
    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        Integer statusCode = 200;
        String errorMessage = null;

        // 요청 컨텍스트 조회 (Request Attribute)
        ReqContextVo context = ReqContextUtils.getReqContext();

        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (Exception e) {
            statusCode = 500;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // 제외 URL 체크
            if (context != null && !isExcludedUrl(context.getRequestUri())) {
                saveLogAsync(context, executionTime, statusCode, errorMessage);
            }

            if (context != null) {
                log.debug(
                        "requestId: {}, uri: {}, ip: {}, ({}ms)",
                        context.getRequestId(),
                        context.getRequestUri(),
                        context.getClientIp(),
                        executionTime);
            }
        }
    }

    /**
     * DB에 로그 비동기 저장
     *
     * <p>@Async로 비동기 처리하여 API 응답 시간에 영향을 주지 않습니다.
     *
     * <p>DB 저장 실패 시에도 예외를 발생시키지 않아 비즈니스 로직에 영향을 주지 않습니다.
     *
     * @param context 요청 컨텍스트
     * @param executionTime 실행 시간 (밀리초)
     * @param statusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지 (있는 경우)
     */
    @Async
    protected void saveLogAsync(
            ReqContextVo context, long executionTime, Integer statusCode, String errorMessage) {
        try {
            // 사용자 ID 조회 (인증된 경우)
            String userId = null;
            try {
                userId = SecurityUtils.getUserId();
            } catch (Exception e) {
                // 비로그인 요청은 userId null
                log.trace("getUserId failed (anonymous request): {}", e.getMessage());
            }

            // 로그 카테고리 자동 분류
            LogCategory logCategory =
                    LogCategory.classify(
                            context.getRequestUri(), context.getHttpMethod().name(), statusCode);

            // 요청 파라미터 추출 (Query String)
            String requestParams = extractRequestParams();

            SysLog sysLog =
                    SysLog.builder()
                            .requestId(context.getRequestId())
                            .requestUri(context.getRequestUri())
                            .httpMethod(context.getHttpMethod().name())
                            .logCategory(logCategory)
                            .clientIp(context.getClientIp())
                            .userId(userId)
                            .requestParams(requestParams)
                            .requestTime(context.getRequestTimestamp())
                            .responseTime(LocalDateTime.now())
                            .executionTimeMs((int) executionTime)
                            .statusCode(statusCode)
                            .errorMessage(errorMessage)
                            .build();

            sysLogRepository.save(sysLog);

            log.trace(
                    "System log saved - requestId: {}, uri: {}, category: {}",
                    context.getRequestId(),
                    context.getRequestUri(),
                    logCategory);
        } catch (Exception e) {
            // DB 저장 실패가 비즈니스 로직에 영향 주지 않도록 예외 무시
            log.warn("Failed to save system log: {}", e.getMessage());
        }
    }

    /**
     * 요청 파라미터 추출 (Query String)
     *
     * <p>Query String을 JSON 형식으로 변환하여 반환합니다.
     *
     * <p>예시: ?page=1&size=10 → {"page":["1"],"size":["10"]}
     *
     * @return JSON 형식의 요청 파라미터 (없으면 null)
     */
    private String extractRequestParams() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            var parameterMap = request.getParameterMap();
            if (parameterMap.isEmpty()) {
                return null;
            }

            // Query String을 JSON으로 변환
            return objectMapper.writeValueAsString(parameterMap);
        } catch (Exception e) {
            log.trace("Failed to extract request params: {}", e.getMessage());
            return null;
        }
    }

    /**
     * URL이 제외 패턴에 포함되는지 확인
     *
     * <p>AntPathMatcher를 사용하여 패턴 매칭합니다.
     *
     * <p>예시: /actuator/**, /health, /swagger-ui/**
     *
     * @param requestUrl 요청 URL
     * @return 제외 대상이면 true, 아니면 false
     */
    private boolean isExcludedUrl(String requestUrl) {
        return loggingProperties.getExcludeUrls().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestUrl));
    }
}
