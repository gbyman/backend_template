package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.backend.core.validator.MaliciousFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 악성 파일 패턴 차단 어노테이션 위험한 파일 확장자 및 실행 가능한 파일 업로드를 차단합니다.
 *
 * <p>차단 대상: - 실행 파일: exe, bat, cmd, sh, dll, so - 스크립트 파일: js, vbs, ps1, jar - 문서 매크로: docm, xlsm,
 * pptm - 압축 파일 내 실행 파일 (선택적)
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class FileUploadDto {
 *     @NoMaliciousFile
 *     private MultipartFile file;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaliciousFileValidator.class)
public @interface NoMaliciousFile {

    String message() default "위험한 파일 형식입니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 추가로 차단할 확장자 목록 */
    String[] additionalBlacklist() default {};
}
