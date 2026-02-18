package app.backend.core.jwt.constants;

import lombok.Getter;

/** JWT Claim Key 상수 */
@Getter
public enum ClaimKeys {
    TOKEN_TYPE("tokenType"),
    AUTH_CLAIM_KEY("authorities");

    private final String key;

    ClaimKeys(String key) {
        this.key = key;
    }
}
