package app.backend.core.ratelimit;

/**
 * Rate Limit 초과 예외
 *
 * <p>HTTP 429 Too Many Requests 응답을 반환합니다.
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException() {
        super("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }

    public TooManyRequestsException(String message) {
        super(message);
    }

    public TooManyRequestsException(long retryAfterSeconds) {
        super(String.format("요청이 너무 많습니다. %d초 후 다시 시도해주세요.", retryAfterSeconds));
    }
}
