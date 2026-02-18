package app.backend.core.validator;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import app.backend.core.annotation.ValidFileExtension;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import app.backend.core.property.FileUploadProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * File extension and size validator Used with @ValidFileExtension annotation.
 *
 * <p>Configuration priority: 1. Annotation values (if specified) - takes precedence 2.
 * application.yml defaults (file-upload.allowed-extensions) - fallback
 */
public class FileExtensionValidator
        implements ConstraintValidator<ValidFileExtension, List<MultipartFile>> {

    private final FileUploadProperties fileUploadProperties;

    private static final double BYTES_PER_MB = 1024.0 * 1024.0;

    private List<String> allowedExtensions;
    private long maxSizeInBytes;

    public FileExtensionValidator(FileUploadProperties fileUploadProperties) {
        this.fileUploadProperties = fileUploadProperties;
    }

    @Override
    public void initialize(ValidFileExtension constraintAnnotation) {
        // Priority 1: Use annotation value if specified
        if (constraintAnnotation.allowed().length > 0) {
            this.allowedExtensions =
                    Arrays.stream(constraintAnnotation.allowed()).map(String::toLowerCase).toList();
        } else {
            // Priority 2: Fallback to yml configuration
            this.allowedExtensions =
                    fileUploadProperties.getAllowedExtensions().stream()
                            .map(String::toLowerCase)
                            .toList();
        }

        // Priority 1: Use annotation value if specified (not -1)
        if (constraintAnnotation.maxSizeInBytes() != -1L) {
            this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
        } else {
            // Priority 2: Fallback to yml configuration
            this.maxSizeInBytes = fileUploadProperties.getMaxSizeInBytes();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        // null이거나 비어있으면 통과 (required 체크는 @NotNull, @NotEmpty로)
        if (files == null || files.isEmpty()) {
            return true;
        }

        for (MultipartFile file : files) {
            // 파일이 비어있거나 파일명이 없으면 건너뛰기
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                continue;
            }

            if (!originalFilename.contains(".")) {
                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format("파일 '%s': 확장자가 없습니다.", originalFilename));
            }

            String extension =
                    originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            if (!allowedExtensions.contains(extension)) {
                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format(
                                "파일 '%s': 허용되지 않은 확장자입니다. 허용된 확장자: %s",
                                originalFilename, String.join(", ", allowedExtensions)));
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
        }

        return true;
    }
}
