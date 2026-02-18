package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.MimeTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * MIME 타입 검증 어노테이션 파일의 Content-Type을 확인하여 허용된 MIME 타입인지 검증합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class DocumentDto {
 *     @ValidMimeType(allowed = {"application/pdf", "application/msword"})
 *     private MultipartFile document;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MimeTypeValidator.class)
public @interface ValidMimeType {

    String message() default "허용되지 않은 파일 형식입니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 허용할 MIME 타입 목록 예: "image/jpeg", "application/pdf", "text/plain" */
    String[] allowed();

    /** 최대 파일 크기 (바이트) -1은 크기 제한 없음 */
    long maxSizeInBytes() default -1L;
}
