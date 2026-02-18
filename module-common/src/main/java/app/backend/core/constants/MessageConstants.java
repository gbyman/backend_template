package app.backend.core.constants;

public final class MessageConstants {
    public static final String INTERNAL_SERVER_ERROR = "ERR0001";
    public static final String BAD_REQUEST = "ERR0002";
    public static final String FORBIDDEN = "ERR0003";
    public static final String UNAUTHORIZED = "ERR0004";
    public static final String TOKEN_EXPIRED = "ERR0005";
    public static final String DUPLICATE_LOGIN = "ERR0006";
    public static final String IS_DISABLED_ACCOUNT = "ERR0007";
    public static final String ACCOUNT_WITHDRAWN = "ERR0008";

    private MessageConstants() {
        throw new IllegalStateException();
    }
}
