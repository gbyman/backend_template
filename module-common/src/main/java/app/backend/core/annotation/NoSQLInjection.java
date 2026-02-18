package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.SQLInjectionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * SQL Injection attack pattern blocking annotation Detects and blocks SQL injection patterns in
 * user input.
 *
 * <p>Blocked patterns: - SQL comments: --, slash-star, star-slash - SQL keywords: SELECT, INSERT,
 * UPDATE, DELETE, DROP, UNION, etc - Special character combinations: quotes with OR, quotes with
 * equals - Function calls: EXEC, EXECUTE, sp_ prefix
 *
 * <p>Usage example: @NoSQLInjection private String keyword;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SQLInjectionValidator.class)
public @interface NoSQLInjection {

    String message() default "입력값에 SQL 인젝션 패턴이 포함되어 있습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
