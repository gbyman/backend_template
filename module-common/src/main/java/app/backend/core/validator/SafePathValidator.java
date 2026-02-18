package app.backend.core.validator;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import app.backend.core.annotation.SafePath;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Path Traversal Attack Prevention Validator Validates and blocks directory traversal patterns in
 * file paths.
 */
public class SafePathValidator implements ConstraintValidator<SafePath, String> {

    private static final int MIN_WINDOWS_PATH_LENGTH = 3;

    private boolean allowAbsolute;

    // Dangerous path patterns
    private static final Pattern[] DANGEROUS_PATTERNS = {
        // Directory traversal
        Pattern.compile("\\.\\./"), // ../
        Pattern.compile("\\.\\\\"), // ..\
        Pattern.compile("/\\.\\./"), // /../
        Pattern.compile("\\\\\\.\\\\"), // \..\

        // Null byte injection
        Pattern.compile("\\x00"),
        Pattern.compile("%00"),

        // Special characters
        Pattern.compile("[<>:\"|?*]"),

        // Unix special paths
        Pattern.compile("^/dev/"),
        Pattern.compile("^/proc/"),
        Pattern.compile("^/sys/")
    };

    @Override
    public void initialize(SafePath constraintAnnotation) {
        this.allowAbsolute = constraintAnnotation.allowAbsolute();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null or empty is valid (use @NotNull, @NotBlank for required check)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String path = value.trim();

        // 1. Check for dangerous patterns
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(path).find()) {
                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format("경로 '%s': 위험한 경로 패턴이 감지되었습니다.", path));
            }
        }

        // 2. Check for absolute paths (if not allowed)
        if (!allowAbsolute && isAbsolutePath(path)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("경로 '%s': 절대 경로는 허용되지 않습니다.", path));
        }

        // 3. Validate path syntax
        try {
            Paths.get(path);
        } catch (InvalidPathException e) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("경로 '%s': 유효하지 않은 경로입니다.", path));
        }

        // 4. Check for hidden traversal attempts
        if (containsHiddenTraversal(path)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("경로 '%s': 숨겨진 경로 탐색 패턴이 감지되었습니다.", path));
        }

        return true;
    }

    /** Check if the path is absolute */
    private boolean isAbsolutePath(String path) {
        // Unix absolute path
        if (path.startsWith("/")) {
            return true;
        }

        // Windows absolute path
        // C:\, D:\, etc.
        if (path.length() >= MIN_WINDOWS_PATH_LENGTH
                && path.charAt(1) == ':'
                && (path.charAt(2) == '\\' || path.charAt(2) == '/')) {
            return true;
        }

        // UNC path (\\server\share)
        if (path.startsWith("\\\\")) {
            return true;
        }

        return false;
    }

    /** Check for hidden traversal attempts Examples: %2e%2e/, ....// */
    private boolean containsHiddenTraversal(String path) {
        String lowerPath = path.toLowerCase();

        // URL encoded traversal
        if (lowerPath.contains("%2e%2e") || lowerPath.contains("%2f")) {
            return true;
        }

        // Multiple dots
        if (lowerPath.contains("....") || lowerPath.contains("...//")) {
            return true;
        }

        // Mixed slashes
        if (lowerPath.contains("/\\") || lowerPath.contains("\\/")) {
            return true;
        }

        return false;
    }
}
