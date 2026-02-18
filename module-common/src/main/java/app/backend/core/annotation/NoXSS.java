package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.XSSValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * XSS(Cross-Site Scripting) 공격 패턴 차단 어노테이션 사용자 입력에서 위험한 스크립트 패턴을 감지하고 차단합니다.
 *
 * <p>차단 대상: - <script> 태그 - javascript: URL - on* 이벤트 핸들러 (onclick, onerror 등) - <iframe>, <embed>,
 * <object> 태그 - eval(), expression() 등
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class CommentDto {
 *     @NoXSS
 *     private String content;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = XSSValidator.class)
public @interface NoXSS {

    String message() default "입력값에 위험한 스크립트 패턴이 포함되어 있습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
