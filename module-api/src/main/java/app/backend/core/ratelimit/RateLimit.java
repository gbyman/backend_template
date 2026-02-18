package app.backend.core.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * API별 Rate Limit 설정 어노테이션
 *
 * <p>컨트롤러 메서드에 적용하여 해당 API의 호출 제한을 설정합니다.
 *
 * <pre>{@code
 * @RateLimit(limit = 10, duration = 1, unit = TimeUnit.MINUTES)
 * @PostMapping("/api/login")
 * public ResponseEntity<?> login() {
 *     // 로그인 API는 IP당 1분에 10회만 허용
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 허용 요청 수 */
    long limit();

    /** 기간 */
    long duration();

    /** 기간 단위 */
    TimeUnit unit() default TimeUnit.MINUTES;
}
