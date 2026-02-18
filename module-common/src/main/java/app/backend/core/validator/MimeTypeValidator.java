package app.backend.core.validator;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import app.backend.core.annotation.ValidMimeType;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** MIME 타입 검증 Validator 파일의 Content-Type을 확인하여 허용된 MIME 타입인지 검증합니다. */
public class MimeTypeValidator implements ConstraintValidator<ValidMimeType, MultipartFile> {

    private static final double BYTES_PER_MB = 1024.0 * 1024.0;

    private List<String> allowedMimeTypes;
    private long maxSizeInBytes;

    @Override
    public void initialize(ValidMimeType constraintAnnotation) {
        this.allowedMimeTypes = Arrays.asList(constraintAnnotation.allowed());
        this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // null이거나 비어있으면 통과 (required 체크는 @NotNull로)
        if (file == null || file.isEmpty()) {
            return true;
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // MIME 타입 확인
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("파일 '%s': MIME 타입을 확인할 수 없습니다.", originalFilename));
        }

        // 허용된 MIME 타입인지 확인
        boolean isAllowed =
                allowedMimeTypes.stream()
                        .anyMatch(
                                allowed ->
                                        contentType
                                                .toLowerCase()
                                                .startsWith(allowed.toLowerCase()));

        if (!isAllowed) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format(
                            "파일 '%s': 허용되지 않은 파일 형식입니다. (현재: %s, 허용: %s)",
                            originalFilename, contentType, String.join(", ", allowedMimeTypes)));
        }

        // 파일 크기 검증
        if (maxSizeInBytes > 0 && file.getSize() > maxSizeInBytes) {
            double fileSizeMB = file.getSize() / BYTES_PER_MB;
            double maxSizeMB = maxSizeInBytes / BYTES_PER_MB;
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format(
                            "파일 '%s': 파일 크기가 제한을 초과했습니다. (현재: %.2fMB, 최대: %.2fMB)",
                            originalFilename, fileSizeMB, maxSizeMB));
        }

        return true;
    }
}
