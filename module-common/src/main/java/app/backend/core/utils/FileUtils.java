package app.backend.core.utils;

import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.experimental.UtilityClass;

/** 파일 처리 유틸리티 */
@UtilityClass
public class FileUtils {

    /** 파일 확장자 추출 */
    public static Optional<String> getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return Optional.of(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
        }
        return Optional.empty();
    }

    /** File 객체에서 확장자만 추출하는 메서드 */
    public static Optional<String> getFileExtension(MultipartFile file) {
        return file != null ? getFileExtension(file.getOriginalFilename()) : Optional.empty();
    }

    /**
     * 주어진 확장자를 가진 파일이 리스트 안에 존재하는지 여부 확인
     *
     * @param files MultipartFile 리스트
     * @param targetExt 찾을 확장자 (예: "jpg", "png")
     * @return 해당 확장자를 가진 파일이 하나라도 있으면 true, 아니면 false
     */
    public static boolean containsFileWithExtension(List<MultipartFile> files, String targetExt) {

        if (!StringUtils.hasText(targetExt)) {
            throw new IllegalArgumentException("Target extension must not be blank");
        }

        if (CollectionUtils.isEmpty(files)) {
            return false;
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(FileUtils::getFileExtension)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(ext -> ext.equalsIgnoreCase(targetExt));
    }

    /**
     * 주어진 리스트에서 특정 확장자의 파일만 필터링
     *
     * @param files MultipartFile 리스트
     * @param targetExt 찾을 확장자 (예: "mp4")
     * @return 해당 확장자의 파일 리스트
     */
    public static List<MultipartFile> filterFilesByExtension(
            List<MultipartFile> files, String targetExt) {
        if (CollectionUtils.isEmpty(files)) {
            return List.of();
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .filter(
                        file ->
                                getFileExtension(file)
                                        .map(ext -> ext.equalsIgnoreCase(targetExt))
                                        .orElse(false))
                .toList();
    }

    /** 랜덤 파일명 생성 (확장자 포함) */
    public String createRandomFileName(String ext) {
        return UUID.randomUUID() + "." + ext;
    }

    /** 랜덤 파일명 생성 (MultipartFile에서 확장자 추출) */
    public static String createRandomFileName(MultipartFile file) {
        String extn = getFileExtension(file).map(ext -> "." + ext).orElse(""); // 확장자가 없으면 빈 문자열 처리
        return UUID.randomUUID() + extn;
    }

    /** 파일명에서 MIME 타입 추측 */
    public String getMimeTypeFromExtension(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);

        if (!StringUtils.hasText(mimeType)) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return mimeType;
    }
}
