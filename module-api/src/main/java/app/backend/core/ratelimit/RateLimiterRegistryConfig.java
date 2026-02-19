package app.backend.core.ratelimit;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;

/**
 * RateLimiterRegistry 빈 설정
 *
 * <p>RateLimitConfig의 순환 의존성 방지를 위해 별도 클래스로 분리
 */
@Configuration
@RequiredArgsConstructor
public class RateLimiterRegistryConfig {

    private final RateLimitProperties rateLimitProperties;

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config =
                RateLimiterConfig.custom()
                        .limitForPeriod(
                                (int) rateLimitProperties.getGlobal().getCapacity())
                        .limitRefreshPeriod(
                                Duration.ofSeconds(
                                        rateLimitProperties.getGlobal().getDurationSeconds()))
                        .timeoutDuration(Duration.ZERO)
                        .build();

        return RateLimiterRegistry.of(config);
    }
}
