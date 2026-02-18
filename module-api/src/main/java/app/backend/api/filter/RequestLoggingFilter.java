package app.backend.api.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import app.backend.core.utils.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청/응답 로깅 및 MDC 설정 필터 - 요청마다 고유한 requestId 생성 - 인증된 사용자의 userId MDC에 설정 - 요청 처리 시간 측정 - 응답 상태 코드
 * 로깅 - 에러 발생 시 에러 정보 MDC에 설정
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int HTTP_STATUS_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final int HTTP_STATUS_CLIENT_ERROR = HttpStatus.BAD_REQUEST.value();
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String TRACE_ID = "traceId";
    private static final String REQUEST_URI = "requestUri";
    private static final String REQUEST_METHOD = "requestMethod";
    private static final String REQUEST_STATUS = "requestStatus";
    private static final String RESPONSE_TIME_MS = "responseTimeMs";
    private static final String CLIENT_IP = "clientIp";
    private static final String SERVER_IP = "serverIp";
    private static final String HOST_NAME = "hostName";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_CLASS = "errorClass";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 요청 시작 시간
        long startTime = System.currentTimeMillis();

        // MDC 설정
        setupMDC(request);

        // Response Wrapper (응답 내용 읽기 위해)
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // 요청 로깅
            logRequest(request);

            // 다음 필터 체인 실행
            filterChain.doFilter(request, responseWrapper);

            // 응답 상태 MDC에 설정
            int statusCode = responseWrapper.getStatus();
            MDC.put(REQUEST_STATUS, String.valueOf(statusCode));

            // 응답 시간 계산
            long responseTime = System.currentTimeMillis() - startTime;
            MDC.put(RESPONSE_TIME_MS, String.valueOf(responseTime));

            // 응답 로깅
            logResponse(request, statusCode, responseTime);

        } catch (Exception e) {
            // 에러 정보 MDC에 설정
            MDC.put(REQUEST_STATUS, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            MDC.put(ERROR_MESSAGE, e.getMessage() != null ? e.getMessage() : "Unknown error");
            MDC.put(ERROR_CLASS, e.getClass().getName());

            // 에러 로깅
            long responseTime = System.currentTimeMillis() - startTime;
            MDC.put(RESPONSE_TIME_MS, String.valueOf(responseTime));
            log.error(
                    "Request failed: {} {} - Error: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage(),
                    e);

            throw e;
        } finally {
            // Response body를 실제로 전송
            responseWrapper.copyBodyToResponse();

            // MDC 정리
            clearMDC();
        }
    }

    /** MDC 초기 설정 */
    private void setupMDC(HttpServletRequest request) {
        // Request ID 생성 (UUID)
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);

        // Trace ID (분산 추적용, 헤더에서 가져오거나 새로 생성)
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = requestId; // Request ID와 동일하게 설정
        }
        MDC.put(TRACE_ID, traceId);

        // User ID (인증된 사용자)
        String userId = extractUserId();
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }

        // 요청 정보
        MDC.put(REQUEST_URI, request.getRequestURI());
        MDC.put(REQUEST_METHOD, request.getMethod());

        // Client IP
        String clientIp = IpUtils.getClientIp(request);
        MDC.put(CLIENT_IP, clientIp);

        // Server IP / Host Name (ELK 로그 분석용)
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            MDC.put(SERVER_IP, localHost.getHostAddress());
            MDC.put(HOST_NAME, localHost.getHostName());
        } catch (Exception e) {
            log.debug("Failed to resolve server IP/hostname", e);
        }
    }

    /** 인증된 사용자 ID 추출 */
    private String extractUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Failed to extract userId from SecurityContext", e);
        }
        return null;
    }

    /** 요청 로깅 */
    private void logRequest(HttpServletRequest request) {
        String clientIp = IpUtils.getClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        log.info(
                ">>> Request: {} {} | IP: {} | User-Agent: {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp,
                userAgent);
    }

    /** 응답 로깅 */
    private void logResponse(HttpServletRequest request, int statusCode, long responseTime) {
        if (statusCode >= HTTP_STATUS_SERVER_ERROR) {
            log.error(
                    "<<< Response: {} {} | Status: {} | Time: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    statusCode,
                    responseTime);
        } else if (statusCode >= HTTP_STATUS_CLIENT_ERROR) {
            log.warn(
                    "<<< Response: {} {} | Status: {} | Time: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    statusCode,
                    responseTime);
        } else {
            log.info(
                    "<<< Response: {} {} | Status: {} | Time: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    statusCode,
                    responseTime);
        }
    }

    /** MDC 정리 */
    private void clearMDC() {
        MDC.remove(REQUEST_ID);
        MDC.remove(USER_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(REQUEST_URI);
        MDC.remove(REQUEST_METHOD);
        MDC.remove(REQUEST_STATUS);
        MDC.remove(RESPONSE_TIME_MS);
        MDC.remove(CLIENT_IP);
        MDC.remove(SERVER_IP);
        MDC.remove(HOST_NAME);
        MDC.remove(ERROR_MESSAGE);
        MDC.remove(ERROR_CLASS);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Actuator 엔드포인트는 로깅 제외
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
