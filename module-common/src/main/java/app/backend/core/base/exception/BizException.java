package app.backend.core.base.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public BizException(HttpStatus status, String errorCode) {
        super("");
        this.status = status;
        this.errorCode = errorCode;
    }

    public BizException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
