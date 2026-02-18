package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Password validation annotation Validates password strength and format.
 *
 * <p>Default rules: - Length: 8-16 characters - Must contain: letters (A-Z, a-z) - Must contain:
 * numbers (0-9) - Must contain: special characters (@$!%*?&)
 *
 * <p>Usage example:
 *
 * <pre>
 * public class SignUpDto {
 *     @ValidPassword
 *     private String password;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String message() default "비밀번호는 8~16자리, 영문, 숫자, 특수문자를 포함해야 합니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Minimum password length (default: 8) */
    int minLength() default 8;

    /** Maximum password length (default: 16) */
    int maxLength() default 16;

    /** Require letters (default: true) */
    boolean requireLetters() default true;

    /** Require numbers (default: true) */
    boolean requireNumbers() default true;

    /** Require special characters (default: true) */
    boolean requireSpecialChars() default true;

    /** Allowed special characters (default: @$!%*?&) */
    String specialChars() default "@$!%*?&";
}
