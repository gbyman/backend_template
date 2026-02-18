package app.backend.core.ratelimit;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import app.backend.core.utils.IpUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API별 Rate Limit Interceptor
 *
 * <p>@RateLimit 어노테이션이 있는 API에 대해 개별 Rate Limit을 적용합니다.
 *
 * <p>Resilience4j RateLimiter 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Rate Limit이 비활성화된 경우 스킵
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        // Handler가 메서드인지 확인
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // @RateLimit 어노테이션 확인
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true; // 어노테이션 없으면 스킵
        }

        // 클라이언트 IP 추출
        String clientIp = IpUtils.getClientIp(request);

        // API 경로 (메서드 + URI)
        String apiPath = request.getMethod() + ":" + request.getRequestURI();

        // RateLimiter 이름 (API + IP)
        String rateLimiterName = "api:" + apiPath + ":" + clientIp;

        // 커스텀 설정으로 RateLimiter 가져오기/생성
        RateLimiter rateLimiter =
                rateLimiterRegistry.rateLimiter(
                        rateLimiterName, () -> createRateLimiterConfig(rateLimit));

        try {
            // Rate Limit 체크
            rateLimiter.acquirePermission();
            return true;
        } catch (RequestNotPermitted e) {
            // Rate Limit 초과
            long waitForRefill =
                    TimeUnit.of(rateLimit.unit().toChronoUnit()).toSeconds(rateLimit.duration());
            log.warn(
                    "Rate limit exceeded for API: {}, IP: {}, retry after: {}s",
                    apiPath,
                    clientIp,
                    waitForRefill);

            throw new TooManyRequestsException(waitForRefill);
        }
    }

    /** RateLimiter 설정 생성 (어노테이션 값 사용) */
    private RateLimiterConfig createRateLimiterConfig(RateLimit rateLimit) {
        int capacity = (int) rateLimit.limit();
        Duration duration = Duration.of(rateLimit.duration(), rateLimit.unit().toChronoUnit());

        return RateLimiterConfig.custom()
                .limitForPeriod(capacity)
                .limitRefreshPeriod(duration)
                .timeoutDuration(Duration.ZERO)
                .build();
    }
}
