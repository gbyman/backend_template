package app.backend.core.utils;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * String Utility Provides additional string manipulation methods not available in Apache Commons
 * Lang3.
 *
 * <p>For basic string operations, use org.apache.commons.lang3.StringUtils: - capitalize(),
 * uncapitalize() - reverse() - repeat() - leftPad(), rightPad() - truncate() - abbreviate()
 * (ellipsis) - deleteWhitespace()
 */
@UtilityClass
public class StringUtils {

    private static final String ALPHANUMERIC_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHABETIC_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC_CHARS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate random alphanumeric string using SecureRandom
     *
     * @param length Length of string to generate
     * @return Random alphanumeric string (e.g., "aB3xK9mP2q")
     */
    public String generateRandomAlphanumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        return RANDOM.ints(length, 0, ALPHANUMERIC_CHARS.length())
                .mapToObj(ALPHANUMERIC_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Generate random alphabetic string (letters only) using SecureRandom
     *
     * @param length Length of string to generate
     * @return Random alphabetic string (e.g., "aBxKmPqR")
     */
    public String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        return RANDOM.ints(length, 0, ALPHABETIC_CHARS.length())
                .mapToObj(ALPHABETIC_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Generate random numeric string using SecureRandom
     *
     * @param length Length of string to generate
     * @return Random numeric string (e.g., "483726")
     */
    public String generateRandomNumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        return RANDOM.ints(length, 0, NUMERIC_CHARS.length())
                .mapToObj(NUMERIC_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Convert snake_case to camelCase
     *
     * <p>Examples: - "user_name" → "userName" - "user_id" → "userId" - "created_at" → "createdAt"
     *
     * @param snakeCase Snake case string
     * @return Camel case string
     */
    public String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        String[] parts = snakeCase.split("_");
        if (parts.length == 0) {
            return snakeCase;
        }

        StringBuilder result = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    result.append(parts[i].substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    /**
     * Convert camelCase to snake_case
     *
     * <p>Examples: - "userName" → "user_name" - "userId" → "user_id" - "createdAt" → "created_at"
     *
     * @param camelCase Camel case string
     * @return Snake case string
     */
    public String toSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Convert text to kebab-case
     *
     * <p>Examples: - "userName" → "user-name" - "user_name" → "user-name" - "User Name" →
     * "user-name"
     *
     * @param text Text to convert
     * @return Kebab case string
     */
    public String toKebabCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.replaceAll("([a-z])([A-Z])", "$1-$2") // camelCase to kebab-case
                .replace("_", "-") // snake_case to kebab-case
                .replaceAll("\\s+", "-") // spaces to dashes
                .toLowerCase();
    }

    /**
     * Capitalize first letter and lowercase rest
     *
     * <p>Examples: - "hello" → "Hello" - "HELLO" → "Hello" - "hello world" → "Hello world"
     *
     * <p>Note: Different from Commons Lang3 StringUtils.capitalize() which preserves the rest.
     *
     * @param text Text to capitalize
     * @return Capitalized string
     */
    public String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }

    /**
     * Capitalize each word
     *
     * <p>Examples: - "hello world" → "Hello World" - "foo bar baz" → "Foo Bar Baz"
     *
     * @param text Text to process
     * @return String with each word capitalized
     */
    public String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1));
                }
            }
        }

        return result.toString();
    }

    /**
     * Mask string (show first and last N characters)
     *
     * <p>Examples: - mask("1234567890", 2, 2) → "12******90" - mask("hello", 1, 1) → "h***o"
     *
     * @param text Text to mask
     * @param showFirst Number of characters to show at start
     * @param showLast Number of characters to show at end
     * @return Masked string
     */
    public String mask(String text, int showFirst, int showLast) {
        if (text == null || text.length() <= (showFirst + showLast)) {
            return text;
        }

        String first = text.substring(0, showFirst);
        String last = text.substring(text.length() - showLast);
        int maskLength = text.length() - showFirst - showLast;

        return first + "*".repeat(maskLength) + last;
    }
}
