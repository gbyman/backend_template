package app.backend.core.exception.constants;

import lombok.Getter;

public enum ErrorCode {
    INTERNAL_SERVER_ERROR("ERROR_01", "MLG_ERR01"),
    BAD_REQUEST("ERROR_02", "MLG_ERR02"),
    FORBIDDEN("ERROR_03", "MLG_ERR03"),
    UNAUTHORIZED("ERROR_04", "MLG_ERR04");

    @Getter private final String errorCode;

    @Getter private final String mlgCode;

    ErrorCode(String errorCode, String mlgCode) {
        this.errorCode = errorCode;
        this.mlgCode = mlgCode;
    }
}
