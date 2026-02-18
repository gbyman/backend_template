package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.ImageFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 이미지 파일 검증 어노테이션 파일 확장자뿐만 아니라 실제 파일 헤더(매직 넘버)를 확인하여 진짜 이미지 파일인지 검증합니다.
 *
 * <p>검증 내용: - 파일 확장자가 이미지 형식인지 확인 - 파일 헤더(매직 넘버)가 실제 이미지 형식과 일치하는지 확인 - 확장자를 속여서 업로드하는 공격 차단
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class ProfileDto {
 *     @ValidImageFile(maxSizeInBytes = 5242880) // 5MB
 *     private MultipartFile profileImage;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
public @interface ValidImageFile {

    String message() default "유효한 이미지 파일이 아닙니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 최대 파일 크기 (바이트) -1은 크기 제한 없음 */
    long maxSizeInBytes() default -1L;

    /** 허용할 이미지 형식 기본값: jpg, jpeg, png, gif, webp */
    String[] allowedFormats() default {"jpg", "jpeg", "png", "gif", "webp"};
}
