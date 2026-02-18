package app.backend.infra.file.azure;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;

import app.backend.infra.file.StorageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Azure Blob Storage 저장소 구현체
 *
 * <p>⚠️ 주의사항: - 이 파일은 Azure Blob Storage를 사용할 때만 필요합니다. - Azure를 사용하지 않는 경우 이 파일과 azure 패키지 전체를
 * 삭제해도 됩니다.
 *
 * <p>1. 필요한 의존성 (module-api/build.gradle에 추가): implementation
 * 'com.azure:azure-storage-blob:12.23.+'
 *
 * <p>2. 필요한 Configuration 클래스 생성 예시: @Configuration @ConditionalOnProperty(name = "storage.type",
 * havingValue = "azure") public class AzureStorageConfig
 * { @Value("${storage.azure.connection-string}") private String
 * connectionString; @Value("${storage.azure.container-name}") private String containerName; @Bean
 * public BlobContainerClient blobContainerClient() { BlobServiceClient blobServiceClient = new
 * BlobServiceClientBuilder() .connectionString(connectionString) .buildClient(); return
 * blobServiceClient.getBlobContainerClient(containerName); } @Bean public StorageHelper
 * storageHelper(BlobContainerClient blobContainerClient) { return new
 * AzureBlobStorageHelper(blobContainerClient); } }
 *
 * <p>3. application.yml 설정 예시: storage: type: azure azure: container-name: your-container-name
 * connection-string: ${AZURE_STORAGE_CONNECTION_STRING}
 */
@Slf4j
@RequiredArgsConstructor
public class AzureBlobStorageHelper implements StorageHelper {

    private final BlobContainerClient blobContainerClient;

    @Override
    public String uploadFile(MultipartFile file, String filePath, String fileName) {
        String blobName = buildBlobName(filePath, fileName);

        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            blobClient.upload(BinaryData.fromStream(file.getInputStream(), file.getSize()), true);

            log.info(
                    ">>> Azure Blob 파일 업로드 성공: {}/{}",
                    blobContainerClient.getBlobContainerName(),
                    blobName);
            return blobClient.getBlobUrl();

        } catch (BlobStorageException | IOException e) {
            log.error(">>> Azure Blob 파일 업로드 에러: {}", e.getMessage());
            throw new RuntimeException("Azure Blob 파일 업로드 실패", e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath, String fileName) {
        String blobName = buildBlobName(filePath, fileName);

        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new RuntimeException("파일이 존재하지 않습니다: " + blobName);
            }

            log.info(
                    ">>> Azure Blob 파일 스트림 가져오기 성공: {}/{}",
                    blobContainerClient.getBlobContainerName(),
                    blobName);
            return blobClient.openInputStream();

        } catch (BlobStorageException e) {
            log.error(">>> Azure Blob 파일 스트림 가져오기 에러: {}", e.getMessage());
            throw new RuntimeException("Azure Blob 파일 스트림 가져오기 실패", e);
        }
    }

    @Override
    public byte[] getFileBytes(String filePath, String fileName) {
        String blobName = buildBlobName(filePath, fileName);

        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new RuntimeException("파일이 존재하지 않습니다: " + blobName);
            }

            BinaryData binaryData = blobClient.downloadContent();
            log.info(
                    ">>> Azure Blob 파일 바이트 배열 가져오기 성공: {}/{}",
                    blobContainerClient.getBlobContainerName(),
                    blobName);
            return binaryData.toBytes();

        } catch (BlobStorageException e) {
            log.error(">>> Azure Blob 파일 바이트 배열 가져오기 에러: {}", e.getMessage());
            throw new RuntimeException("Azure Blob 파일 바이트 배열 가져오기 실패", e);
        }
    }

    @Override
    public void deleteFile(String filePath, String fileName) {
        String blobName = buildBlobName(filePath, fileName);

        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info(
                        ">>> Azure Blob 파일 삭제 성공: {}/{}",
                        blobContainerClient.getBlobContainerName(),
                        blobName);
            } else {
                log.warn(">>> Azure Blob 파일 삭제 실패: 파일이 존재하지 않습니다 - {}", blobName);
            }

        } catch (BlobStorageException e) {
            log.error(">>> Azure Blob 파일 삭제 에러: {}", e.getMessage());
            throw new RuntimeException("Azure Blob 파일 삭제 실패", e);
        }
    }

    /** Blob 이름 생성 (filePath + fileName) */
    private String buildBlobName(String filePath, String fileName) {
        return StorageHelper.normalizePath(filePath, fileName);
    }
}
