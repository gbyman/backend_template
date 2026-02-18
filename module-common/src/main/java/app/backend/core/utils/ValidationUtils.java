package app.backend.core.utils;

import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.util.StringUtils;

import lombok.experimental.UtilityClass;

/** Validation Utility Uses Apache Commons Validator for safe and reliable validation. */
@UtilityClass
public class ValidationUtils {

    // Apache Commons Validators (thread-safe singletons)
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();
    private static final UrlValidator URL_VALIDATOR =
            new UrlValidator(new String[] {"http", "https"});

    // Numeric pattern (only digits)
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");

    // Alphanumeric pattern (letters and digits only)
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    /**
     * Validate email format using Apache Commons EmailValidator
     *
     * <p>Valid examples: - user@example.com - first.last@example.co.kr - user+tag@example.com
     *
     * <p>Invalid examples: - @example.com - user@ - user@.com
     *
     * @param email Email address to validate
     * @return true if valid email format
     */
    public boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_VALIDATOR.isValid(email.trim());
    }

    /**
     * Validate URL format using Apache Commons UrlValidator (http/https only)
     *
     * <p>Valid examples: - http://example.com - https://www.example.com -
     * https://example.com:8080/path?query=value - http://localhost:3000 - http://192.168.1.1
     *
     * <p>Invalid examples: - ftp://example.com (only http/https supported) - www.example.com
     * (protocol required) - example.com (protocol required)
     *
     * @param url URL to validate
     * @return true if valid URL format
     */
    public boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        return URL_VALIDATOR.isValid(url.trim());
    }

    /**
     * Check if string contains only numeric characters (0-9)
     *
     * <p>Valid examples: - "123" - "0" - "999999"
     *
     * <p>Invalid examples: - "12.34" (decimal point not allowed) - "12a" (letters not allowed) -
     * "-123" (negative sign not allowed) - "" (empty string)
     *
     * @param str String to check
     * @return true if string contains only digits
     */
    public boolean isNumeric(String str) {
        if (!StringUtils.hasText(str)) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string contains only alphanumeric characters (a-z, A-Z, 0-9)
     *
     * <p>Valid examples: - "abc123" - "ABC" - "123" - "User123"
     *
     * <p>Invalid examples: - "user-123" (hyphen not allowed) - "user_123" (underscore not allowed)
     * - "user 123" (space not allowed) - "user@123" (special characters not allowed)
     *
     * @param str String to check
     * @return true if string contains only letters and digits
     */
    public boolean isAlphanumeric(String str) {
        if (!StringUtils.hasText(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Validate email with exception Throws IllegalArgumentException if email is invalid
     *
     * @param email Email to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if email is invalid
     */
    public void requireValidEmail(String email, String fieldName) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    String.format("%s: 유효하지 않은 이메일 형식입니다. (%s)", fieldName, email));
        }
    }

    /**
     * Validate URL with exception Throws IllegalArgumentException if URL is invalid
     *
     * @param url URL to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if URL is invalid
     */
    public void requireValidUrl(String url, String fieldName) {
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException(
                    String.format("%s: 유효하지 않은 URL 형식입니다. (%s)", fieldName, url));
        }
    }

    /**
     * Require numeric string with exception Throws IllegalArgumentException if string is not
     * numeric
     *
     * @param str String to check
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if string is not numeric
     */
    public void requireNumeric(String str, String fieldName) {
        if (!isNumeric(str)) {
            throw new IllegalArgumentException(
                    String.format("%s: 숫자만 입력 가능합니다. (%s)", fieldName, str));
        }
    }

    /**
     * Require alphanumeric string with exception Throws IllegalArgumentException if string is not
     * alphanumeric
     *
     * @param str String to check
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if string is not alphanumeric
     */
    public void requireAlphanumeric(String str, String fieldName) {
        if (!isAlphanumeric(str)) {
            throw new IllegalArgumentException(
                    String.format("%s: 영문자와 숫자만 입력 가능합니다. (%s)", fieldName, str));
        }
    }
}
