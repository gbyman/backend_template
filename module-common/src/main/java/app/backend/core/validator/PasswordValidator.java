package app.backend.core.validator;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import app.backend.core.annotation.ValidPassword;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Password strength validator Validates password based on configurable rules. */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireLetters;
    private boolean requireNumbers;
    private boolean requireSpecialChars;
    private String specialChars;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireLetters = constraintAnnotation.requireLetters();
        this.requireNumbers = constraintAnnotation.requireNumbers();
        this.requireSpecialChars = constraintAnnotation.requireSpecialChars();
        this.specialChars = constraintAnnotation.specialChars();
    }

    @Override
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // null or empty is valid (use @NotNull, @NotBlank for required check)
        if (password == null || password.isEmpty()) {
            return true;
        }

        // 1. Check length
        if (password.length() < minLength || password.length() > maxLength) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("비밀번호는 %d~%d자리여야 합니다.", minLength, maxLength));
        }

        // 2. Check letters requirement
        if (requireLetters && !Pattern.compile("[A-Za-z]").matcher(password).find()) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST, MessageConstants.BAD_REQUEST, "비밀번호는 영문자를 포함해야 합니다.");
        }

        // 3. Check numbers requirement
        if (requireNumbers && !Pattern.compile("\\d").matcher(password).find()) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST, MessageConstants.BAD_REQUEST, "비밀번호는 숫자를 포함해야 합니다.");
        }

        // 4. Check special characters requirement
        if (requireSpecialChars) {
            String specialCharsPattern = "[" + Pattern.quote(specialChars) + "]";
            if (!Pattern.compile(specialCharsPattern).matcher(password).find()) {
                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format("비밀번호는 특수문자(%s)를 포함해야 합니다.", specialChars));
            }
        }

        // 5. Check for invalid characters
        String allowedPattern = buildAllowedPattern();
        if (!Pattern.compile("^" + allowedPattern + "+$").matcher(password).matches()) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    "비밀번호에 허용되지 않은 문자가 포함되어 있습니다.");
        }

        return true;
    }

    /** Build regex pattern for allowed characters */
    private String buildAllowedPattern() {
        StringBuilder pattern = new StringBuilder("[");

        if (requireLetters) {
            pattern.append("A-Za-z");
        }

        if (requireNumbers) {
            pattern.append("\\d");
        }

        if (requireSpecialChars) {
            pattern.append(Pattern.quote(specialChars));
        }

        pattern.append("]");
        return pattern.toString();
    }
}
