package app.backend.infra.file.azure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import app.backend.infra.file.StorageHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * Azure Blob Storage 설정
 *
 * <p>⚠️ 주의사항: - Azure Blob Storage를 사용하지 않으면 이 파일과 azure 패키지 전체를 삭제하세요. - storage.type=azure 설정이 있을
 * 때만 활성화됩니다.
 *
 * <p>application.yml 설정 예시: storage: type: azure azure: container-name: your-container-name
 * connection-string: ${AZURE_STORAGE_CONNECTION_STRING}
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "azure")
public class AzureStorageConfig {

    @Value("${storage.azure.connection-string}")
    private String connectionString;

    @Value("${storage.azure.container-name}")
    private String containerName;

    @Bean
    public BlobContainerClient blobContainerClient() {
        log.info(">>> Azure Blob Storage 설정 - Container: {}", containerName);

        BlobServiceClient blobServiceClient =
                new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        return blobServiceClient.getBlobContainerClient(containerName);
    }

    @Bean
    public StorageHelper storageHelper(BlobContainerClient blobContainerClient) {
        log.info(">>> AzureBlobStorageHelper 빈 등록");
        return new AzureBlobStorageHelper(blobContainerClient);
    }
}
