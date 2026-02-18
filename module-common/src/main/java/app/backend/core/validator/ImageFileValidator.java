package app.backend.core.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import app.backend.core.annotation.ValidImageFile;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** 이미지 파일 검증 Validator 파일 헤더(매직 넘버)를 확인하여 실제 이미지 파일인지 검증합니다. */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    private long maxSizeInBytes;
    private List<String> allowedFormats;

    // JPEG magic number bytes
    private static final byte JPEG_FF = (byte) 0xFF;
    private static final byte JPEG_D8 = (byte) 0xD8;

    // PNG magic number bytes
    private static final byte PNG_89 = (byte) 0x89;
    private static final byte PNG_50 = 0x50;
    private static final byte PNG_4E = 0x4E;
    private static final byte PNG_47 = 0x47;
    private static final byte PNG_0D = 0x0D;
    private static final byte PNG_0A = 0x0A;
    private static final byte PNG_1A = 0x1A;

    // GIF magic number bytes
    private static final byte GIF_47 = 0x47;
    private static final byte GIF_49 = 0x49;
    private static final byte GIF_46 = 0x46;
    private static final byte GIF_38 = 0x38;
    private static final byte GIF_37 = 0x37;
    private static final byte GIF_61 = 0x61;
    private static final byte GIF_39 = 0x39;

    // WebP magic number bytes
    private static final byte WEBP_52 = 0x52;
    private static final byte WEBP_49 = 0x49;
    private static final byte WEBP_46 = 0x46;
    private static final byte WEBP_57 = 0x57;
    private static final byte WEBP_45 = 0x45;
    private static final byte WEBP_42 = 0x42;
    private static final byte WEBP_50 = 0x50;

    // File size conversion constants
    private static final double BYTES_PER_MB = 1024.0 * 1024.0;

    // File header read size
    private static final int FILE_HEADER_SIZE = 12;
    private static final int MIN_HEADER_BYTES = 3;

    // 파일 형식별 매직 넘버 (파일 헤더 바이트 패턴)
    private static final Map<String, byte[][]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // JPEG
        MAGIC_NUMBERS.put("jpg", new byte[][] {{JPEG_FF, JPEG_D8, JPEG_FF}});
        MAGIC_NUMBERS.put("jpeg", new byte[][] {{JPEG_FF, JPEG_D8, JPEG_FF}});

        // PNG
        MAGIC_NUMBERS.put(
                "png",
                new byte[][] {{PNG_89, PNG_50, PNG_4E, PNG_47, PNG_0D, PNG_0A, PNG_1A, PNG_0A}});

        // GIF
        MAGIC_NUMBERS.put(
                "gif",
                new byte[][] {
                    {GIF_47, GIF_49, GIF_46, GIF_38, GIF_37, GIF_61}, // GIF87a
                    {GIF_47, GIF_49, GIF_46, GIF_38, GIF_39, GIF_61} // GIF89a
                });

        // WebP
        MAGIC_NUMBERS.put(
                "webp",
                new byte[][] {
                    {
                        WEBP_52, WEBP_49, WEBP_46, WEBP_46, 0x00, 0x00, 0x00, 0x00, WEBP_57,
                        WEBP_45, WEBP_42, WEBP_50
                    }
                });
    }

    @Override
    public void initialize(ValidImageFile constraintAnnotation) {
        this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
        this.allowedFormats =
                Arrays.stream(constraintAnnotation.allowedFormats())
                        .map(String::toLowerCase)
                        .toList();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // null이거나 비어있으면 통과 (required 체크는 @NotNull로)
        if (file == null || file.isEmpty()) {
            return true;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return true;
        }

        // 1. 확장자 확인
        if (!originalFilename.contains(".")) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("파일 '%s': 확장자가 없습니다.", originalFilename));
        }

        String extension =
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedFormats.contains(extension)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format(
                            "파일 '%s': 허용되지 않은 이미지 형식입니다. 허용된 형식: %s",
                            originalFilename, String.join(", ", allowedFormats)));
        }

        // 2. 파일 크기 검증
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

        // 3. 매직 넘버 검증 (실제 파일 형식 확인)
        if (!isValidMagicNumber(file, extension)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format(
                            "파일 '%s': 실제 파일 형식이 확장자와 일치하지 않습니다. 악성 파일일 수 있습니다.", originalFilename));
        }

        return true;
    }

    /** 파일 헤더(매직 넘버)를 확인하여 실제 이미지 파일인지 검증 */
    private boolean isValidMagicNumber(MultipartFile file, String extension) {
        byte[][] expectedMagicNumbers = MAGIC_NUMBERS.get(extension);

        if (expectedMagicNumbers == null) {
            // 지원하지 않는 형식은 통과 (확장자 검증으로 충분)
            return true;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileHeader = new byte[FILE_HEADER_SIZE]; // 대부분의 매직 넘버는 12바이트 이내
            int bytesRead = inputStream.read(fileHeader);

            if (bytesRead < MIN_HEADER_BYTES) {
                // 파일이 너무 작으면 유효하지 않음
                return false;
            }

            // 여러 매직 넘버 패턴 중 하나라도 일치하면 OK
            for (byte[] magicNumber : expectedMagicNumbers) {
                if (matchesMagicNumber(fileHeader, magicNumber)) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            throw new BizException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR,
                    "파일 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /** 파일 헤더가 매직 넘버 패턴과 일치하는지 확인 */
    private boolean matchesMagicNumber(byte[] fileHeader, byte[] magicNumber) {
        for (int i = 0; i < magicNumber.length; i++) {
            // 0x00은 와일드카드 (무시)
            if (magicNumber[i] != 0x00 && fileHeader[i] != magicNumber[i]) {
                return false;
            }
        }
        return true;
    }
}
