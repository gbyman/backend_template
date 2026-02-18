package app.backend.core.ratelimit;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.backend.core.utils.IpUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 Rate Limit Filter (IP 기반)
 *
 * <p>모든 API 요청에 대해 IP별 Rate Limit을 적용합니다.
 *
 * <p>Resilience4j RateLimiter 사용
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimitProperties rateLimitProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Rate Limit이 비활성화된 경우 스킵
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 클라이언트 IP 추출
        String clientIp = IpUtils.getClientIp(request);

        // IP별 RateLimiter 가져오기 (없으면 자동 생성)
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("global:" + clientIp);

        try {
            // Rate Limit 체크 및 요청 실행
            rateLimiter.executeRunnable(
                    () -> {
                        try {
                            filterChain.doFilter(request, response);
                        } catch (IOException | ServletException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RequestNotPermitted e) {
            // Rate Limit 초과
            long waitForRefill = rateLimitProperties.getGlobal().getDurationSeconds();
            log.warn("Rate limit exceeded for IP: {}, retry after: {}s", clientIp, waitForRefill);

            throw new TooManyRequestsException(waitForRefill);
        }
    }
}
