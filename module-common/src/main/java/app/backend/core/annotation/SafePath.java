package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.SafePathValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Path Traversal Attack Prevention Annotation Validates and blocks directory traversal patterns in
 * file paths.
 *
 * <p>Blocked patterns: - ../ and ..\\ (directory traversal) - Absolute paths (/root, C:\\) -
 * Special characters (null byte, etc.)
 *
 * <p>Usage example: @SafePath private String filePath;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafePathValidator.class)
public @interface SafePath {

    String message() default "위험한 경로 패턴이 포함되어 있습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Allow absolute paths (default: false) */
    boolean allowAbsolute() default false;
}
