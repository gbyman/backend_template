package app.backend.core.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import app.backend.core.annotation.NoMaliciousFile;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** 악성 파일 패턴 차단 Validator 위험한 파일 확장자 및 실행 가능한 파일 업로드를 차단합니다. */
public class MaliciousFileValidator implements ConstraintValidator<NoMaliciousFile, MultipartFile> {

    private Set<String> blacklistedExtensions;

    // 기본 차단 확장자 목록
    private static final Set<String> DEFAULT_BLACKLIST =
            new HashSet<>(
                    Arrays.asList(
                            // 실행 파일
                            "exe",
                            "bat",
                            "cmd",
                            "sh",
                            "dll",
                            "so",
                            "dylib",
                            "app",
                            // 스크립트 파일
                            "js",
                            "vbs",
                            "vbe",
                            "ps1",
                            "jar",
                            "wsf",
                            "scr",
                            // 문서 매크로
                            "docm",
                            "xlsm",
                            "pptm",
                            "dotm",
                            "xltm",
                            "potm",
                            // 기타 위험 파일
                            "apk",
                            "ipa",
                            "msi",
                            "deb",
                            "rpm",
                            // 리눅스 실행 파일
                            "bin",
                            "run",
                            // 웹 스크립트 (업로드 후 실행 위험)
                            "php",
                            "jsp",
                            "asp",
                            "aspx",
                            // 압축 내 실행 파일 (선택적 - 필요시 주석 해제)
                            // "zip", "rar", "7z", "tar", "gz"
                            // 데이터베이스 파일
                            "sql",
                            "db",
                            "sqlite"));

    @Override
    public void initialize(NoMaliciousFile constraintAnnotation) {
        this.blacklistedExtensions = new HashSet<>(DEFAULT_BLACKLIST);

        // 추가 차단 확장자 추가
        String[] additional = constraintAnnotation.additionalBlacklist();
        if (additional != null && additional.length > 0) {
            Arrays.stream(additional).map(String::toLowerCase).forEach(blacklistedExtensions::add);
        }
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

        // 1. 확장자 추출
        if (!originalFilename.contains(".")) {
            // 확장자가 없는 파일은 의심스러움
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("파일 '%s': 확장자가 없는 파일은 업로드할 수 없습니다.", originalFilename));
        }

        String extension =
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 2. 차단 확장자 확인
        if (blacklistedExtensions.contains(extension)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format(
                            "파일 '%s': 위험한 파일 형식입니다. '%s' 확장자는 보안상의 이유로 업로드할 수 없습니다.",
                            originalFilename, extension));
        }

        // 3. 이중 확장자 확인 (예: file.pdf.exe)
        String nameWithoutExtension =
                originalFilename.substring(0, originalFilename.lastIndexOf("."));
        if (nameWithoutExtension.contains(".")) {
            String secondExtension =
                    nameWithoutExtension
                            .substring(nameWithoutExtension.lastIndexOf(".") + 1)
                            .toLowerCase();
            if (blacklistedExtensions.contains(secondExtension)) {
                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format(
                                "파일 '%s': 이중 확장자가 감지되었습니다. 악성 파일일 수 있습니다.", originalFilename));
            }
        }

        // 4. 파일명에 위험한 패턴 확인
        if (containsSuspiciousPattern(originalFilename)) {
            throw new BizException(
                    HttpStatus.BAD_REQUEST,
                    MessageConstants.BAD_REQUEST,
                    String.format("파일 '%s': 파일명에 위험한 패턴이 포함되어 있습니다.", originalFilename));
        }

        return true;
    }

    /** 파일명에 위험한 패턴이 있는지 확인 */
    private boolean containsSuspiciousPattern(String filename) {
        String lowerFilename = filename.toLowerCase();

        // NULL 바이트 공격
        if (lowerFilename.contains("\0") || lowerFilename.contains("%00")) {
            return true;
        }

        // 경로 탐색 공격
        if (lowerFilename.contains("..")
                || lowerFilename.contains("./")
                || lowerFilename.contains(".\\")) {
            return true;
        }

        return false;
    }
}
