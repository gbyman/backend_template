package app.backend.core.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Rate Limit 설정
 *
 * <p>application.yml에서 설정값을 읽어옵니다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Rate Limit 기능 활성화 여부 */
    private boolean enabled = true;

    /** 전역 Rate Limit 설정 (IP 기반) */
    private Global global = new Global();

    private static final long DEFAULT_CAPACITY = 100;
    private static final long DEFAULT_DURATION_SECONDS = 60;

    @Getter
    @Setter
    public static class Global {
        /** IP당 허용 요청 수 */
        private long capacity = DEFAULT_CAPACITY;

        /** 기간 (초) */
        private long durationSeconds = DEFAULT_DURATION_SECONDS;

        public Duration getDuration() {
            return Duration.ofSeconds(durationSeconds);
        }
    }
}
