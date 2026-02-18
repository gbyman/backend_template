package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.FileExtensionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * File extension and size validation annotation Used with MultipartFile list to validate allowed
 * extensions and file size.
 *
 * <p>Configuration priority: 1. Annotation values (if specified) - takes precedence 2.
 * application.yml defaults (file-upload.allowed-extensions) - fallback
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Example 1: Use specific extensions (overrides yml defaults)
 * public class ProfileImageDto {
 *     &#64;ValidFileExtension(allowed = {"jpg", "png"}, maxSizeInBytes = 5242880) // 5MB
 *     private List<MultipartFile> images;
 * }
 *
 * // Example 2: Use yml defaults (file-upload.allowed-extensions)
 * public class DocumentDto {
 *     &#64;ValidFileExtension // Uses yml defaults
 *     private List<MultipartFile> documents;
 * }
 *
 * // Example 3: Override only specific properties
 * public class AttachmentDto {
 *     &#64;ValidFileExtension(maxSizeInBytes = 20971520) // 20MB, uses yml extensions
 *     private List<MultipartFile> attachments;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileExtensionValidator.class)
public @interface ValidFileExtension {

    String message() default "허용되지 않은 파일 확장자입니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Allowed file extensions (lowercase) If empty, uses file-upload.allowed-extensions from
     * application.yml
     */
    String[] allowed() default {};

    /**
     * Maximum file size in bytes -1 means use file-upload.max-size-in-bytes from application.yml
     */
    long maxSizeInBytes() default -1L;
}
