package app.backend.core.ratelimit;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Rate Limit 설정 (Resilience4j)
 *
 * <p>Resilience4j RateLimiter를 사용한 API Rate Limiting
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final RateLimitProperties rateLimitProperties;

    /**
     * RateLimiterRegistry 생성
     *
     * <p>전역 기본 설정으로 RateLimiter 인스턴스를 생성합니다.
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config =
                RateLimiterConfig.custom()
                        .limitForPeriod(
                                (int) rateLimitProperties.getGlobal().getCapacity()) // IP당 허용 요청 수
                        .limitRefreshPeriod(
                                Duration.ofSeconds(
                                        rateLimitProperties
                                                .getGlobal()
                                                .getDurationSeconds())) // 갱신 주기
                        .timeoutDuration(Duration.ZERO) // 대기하지 않고 즉시 실패
                        .build();

        return RateLimiterRegistry.of(config);
    }

    /** Rate Limit 인터셉터 등록 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
