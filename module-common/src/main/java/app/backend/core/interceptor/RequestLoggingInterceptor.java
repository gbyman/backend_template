package app.backend.core.interceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** 요청/응답 로깅 인터셉터 모든 API 요청과 응답 시간을 로깅합니다. */
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final double MILLIS_PER_SECOND = 1000.0;
    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        long startTime = System.currentTimeMillis();
        LocalDateTime requestTime = LocalDateTime.now();

        // 시작 시간을 request attribute에 저장 (응답 시간 계산용)
        request.setAttribute(REQUEST_START_TIME_ATTR, startTime);

        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? requestURI + "?" + queryString : requestURI;

        log.info(
                ">>>>> API 요청 - 시간: {} | {} {}",
                requestTime.format(DATE_TIME_FORMATTER),
                method,
                fullUrl);

        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME_ATTR);
        if (startTime != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String requestURI = request.getRequestURI();
            int status = response.getStatus();

            log.debug(
                    "<<<<< API 응답 - {} {} | 상태: {} | 소요시간: {}ms ({}초)",
                    method,
                    requestURI,
                    status,
                    executionTime,
                    executionTime / MILLIS_PER_SECOND);
        }
    }
}
