package app.backend.infra.file;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/** 파일 저장소 추상화 인터페이스 구현체: LocalStorageHelper, S3StorageHelper, AzureBlobStorageHelper */
public interface StorageHelper {
    /**
     * 파일 업로드
     *
     * @param file 업로드할 파일
     * @param filePath 파일 경로
     * @param fileName 파일명
     * @return 업로드된 파일의 전체 경로 또는 URL
     */
    String uploadFile(MultipartFile file, String filePath, String fileName);

    /**
     * 파일 스트림 가져오기
     *
     * @param filePath 파일 경로
     * @param fileName 파일명
     * @return 파일 InputStream
     */
    InputStream getFileStream(String filePath, String fileName);

    /**
     * 파일 바이트 배열로 가져오기
     *
     * @param filePath 파일 경로
     * @param fileName 파일명
     * @return 파일 byte[]
     */
    byte[] getFileBytes(String filePath, String fileName);

    /**
     * 파일 삭제
     *
     * @param filePath 파일 경로
     * @param fileName 파일명
     */
    void deleteFile(String filePath, String fileName);

    /**
     * 파일 복사
     *
     * @param srcPath 원본 파일 경로
     * @param srcFileName 원본 파일명
     * @param destPath 대상 파일 경로
     * @param destFileName 대상 파일명
     */
    default void copyFile(
            String srcPath, String srcFileName, String destPath, String destFileName) {
        throw new UnsupportedOperationException("copyFile is not supported");
    }

    /**
     * 파일 이동
     *
     * @param srcPath 원본 파일 경로
     * @param srcFileName 원본 파일명
     * @param destPath 대상 파일 경로
     * @param destFileName 대상 파일명
     */
    default void moveFile(
            String srcPath, String srcFileName, String destPath, String destFileName) {
        throw new UnsupportedOperationException("moveFile is not supported");
    }

    /**
     * 파일 경로 정규화 (filePath + fileName 결합)
     *
     * <p>선행 "/" 제거, 후행 "/" 보장
     */
    static String normalizePath(String filePath, String fileName) {
        String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        if (!normalizedPath.isEmpty() && !normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }
        return normalizedPath + fileName;
    }
}
