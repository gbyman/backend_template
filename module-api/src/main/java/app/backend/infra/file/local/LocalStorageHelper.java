package app.backend.infra.file.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import app.backend.infra.file.StorageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 파일 시스템 저장소 구현체
 *
 * <p>⚠️ 주의사항: - 이 파일은 로컬 파일 시스템을 저장소로 사용할 때 필요합니다. - 로컬 저장소를 사용하지 않는 경우 이 파일과 local 패키지 전체를 삭제해도
 * 됩니다.
 *
 * <p>1. 필요한 의존성: - commons-io는 이미 module-api/build.gradle에 추가되어 있습니다. - 별도 의존성 추가 불필요
 *
 * <p>2. 필요한 Configuration 클래스 생성 예시: @Configuration @ConditionalOnProperty(name = "storage.type",
 * havingValue = "local") public class LocalStorageConfig { @Bean public StorageHelper
 * storageHelper() { return new LocalStorageHelper(); } }
 *
 * <p>3. application.yml 설정 예시: storage: type: local local: base-path: /var/app/uploads # 파일 저장 기본
 * 경로
 */
@Slf4j
@RequiredArgsConstructor
public class LocalStorageHelper implements StorageHelper {

    @Override
    public String uploadFile(MultipartFile file, String filePath, String fileName) {
        Path uploadDir = Paths.get(filePath).toAbsolutePath().normalize();

        try (InputStream inputStream = file.getInputStream()) {

            Files.createDirectories(uploadDir);

            Path targetLocation = uploadDir.resolve(fileName);

            Files.copy(inputStream, targetLocation);

            return uploadDir.toString();

        } catch (IOException e) {
            log.error(">>> 파일 업로드 에러: {}", e.getMessage());
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath, String fileName) {
        try {
            return Files.newInputStream(Paths.get(filePath, fileName));
        } catch (IOException e) {
            log.error(">>> 파일 스트림 열기 에러: {}", e.getMessage());
            throw new RuntimeException("파일 스트림 열기 실패", e);
        }
    }

    @Override
    public byte[] getFileBytes(String filePath, String fileName) {
        try {
            return FileUtils.readFileToByteArray(new File(filePath, fileName));
        } catch (IOException e) {
            log.error(">>> 파일 다운로드 에러: {}", e.getMessage());
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }

    @Override
    public void deleteFile(String filePath, String fileName) {
        Path fileLocation = Paths.get(filePath, fileName).toAbsolutePath();

        try {
            Files.deleteIfExists(fileLocation);
        } catch (IOException e) {
            log.error(">>> 파일 삭제 에러: {}", e.getMessage());
            throw new RuntimeException("파일 삭제 실패: " + fileLocation, e);
        }
    }

    @Override
    public void copyFile(String srcPath, String srcFileName, String destPath, String destFileName) {
        Path source = Paths.get(srcPath, srcFileName).toAbsolutePath();
        Path target = Paths.get(destPath, destFileName).toAbsolutePath();

        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(">>> 파일 복사 에러: {}", e.getMessage());
            throw new RuntimeException("파일 복사 실패: " + source + " → " + target, e);
        }
    }

    @Override
    public void moveFile(String srcPath, String srcFileName, String destPath, String destFileName) {
        Path source = Paths.get(srcPath, srcFileName).toAbsolutePath();
        Path target = Paths.get(destPath, destFileName).toAbsolutePath();

        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(">>> 파일 이동 에러: {}", e.getMessage());
            throw new RuntimeException("파일 이동 실패: " + source + " → " + target, e);
        }
    }
}
