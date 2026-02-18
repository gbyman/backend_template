package app.backend.infra.file.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.backend.infra.file.StorageHelper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Storage 설정
 *
 * <p>⚠️ 주의사항: - AWS S3를 사용하지 않으면 이 파일과 s3 패키지 전체를 삭제하세요. - storage.type=s3 설정이 있을 때만 활성화됩니다.
 *
 * <p>application.yml 설정 예시: storage: type: s3 s3: bucket-name: your-bucket-name region:
 * ap-northeast-2 access-key: ${AWS_ACCESS_KEY} secret-key: ${AWS_SECRET_KEY}
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageConfig {

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Value("${storage.s3.region}")
    private String region;

    @Value("${storage.s3.access-key}")
    private String accessKey;

    @Value("${storage.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        log.info(">>> AWS S3 클라이언트 설정 - Bucket: {}, Region: {}", bucketName, region);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public StorageHelper storageHelper(S3Client s3Client) {
        log.info(">>> S3StorageHelper 빈 등록");
        return new S3StorageHelper(s3Client, bucketName);
    }
}
