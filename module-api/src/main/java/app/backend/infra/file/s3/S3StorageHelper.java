package app.backend.infra.file.s3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import app.backend.infra.file.StorageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * AWS S3 저장소 구현체
 *
 * <p>⚠️ 주의사항: - 이 파일은 AWS S3를 사용할 때만 필요합니다. - S3를 사용하지 않는 경우 이 파일과 s3 패키지 전체를 삭제해도 됩니다.
 *
 * <p>1. 필요한 의존성 (module-api/build.gradle에 추가): implementation 'software.amazon.awssdk:s3:2.20.+'
 *
 * <p>2. 필요한 Configuration 클래스 생성 예시: @Configuration @ConditionalOnProperty(name = "storage.type",
 * havingValue = "s3") public class S3StorageConfig { @Value("${storage.s3.bucket-name}") private
 * String bucketName; @Value("${storage.s3.region}") private String
 * region; @Value("${storage.s3.access-key}") private String
 * accessKey; @Value("${storage.s3.secret-key}") private String secretKey; @Bean public S3Client
 * s3Client() { AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
 * return S3Client.builder() .region(Region.of(region))
 * .credentialsProvider(StaticCredentialsProvider.create(credentials)) .build(); } @Bean public
 * StorageHelper storageHelper(S3Client s3Client) { return new S3StorageHelper(s3Client,
 * bucketName); } }
 *
 * <p>3. application.yml 설정 예시: storage: type: s3 s3: bucket-name: your-bucket-name region:
 * ap-northeast-2 access-key: ${AWS_ACCESS_KEY} secret-key: ${AWS_SECRET_KEY}
 */
@Slf4j
@RequiredArgsConstructor
public class S3StorageHelper implements StorageHelper {

    private static final int BUFFER_SIZE = 1024;

    private final S3Client s3Client;
    private final String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String filePath, String fileName) {
        String key = buildKey(filePath, fileName);

        try {
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info(">>> S3 파일 업로드 성공: s3://{}/{}", bucketName, key);
            return String.format("s3://%s/%s", bucketName, key);

        } catch (S3Exception | IOException e) {
            log.error(">>> S3 파일 업로드 에러: {}", e.getMessage());
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath, String fileName) {
        String key = buildKey(filePath, fileName);

        try {
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder().bucket(bucketName).key(key).build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            log.info(">>> S3 파일 스트림 가져오기 성공: s3://{}/{}", bucketName, key);
            return response;

        } catch (S3Exception e) {
            log.error(">>> S3 파일 스트림 가져오기 에러: {}", e.getMessage());
            throw new RuntimeException("S3 파일 스트림 가져오기 실패", e);
        }
    }

    @Override
    public byte[] getFileBytes(String filePath, String fileName) {
        try (InputStream inputStream = getFileStream(filePath, fileName);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error(">>> S3 파일 바이트 배열 변환 에러: {}", e.getMessage());
            throw new RuntimeException("S3 파일 바이트 배열 변환 실패", e);
        }
    }

    @Override
    public void deleteFile(String filePath, String fileName) {
        String key = buildKey(filePath, fileName);

        try {
            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info(">>> S3 파일 삭제 성공: s3://{}/{}", bucketName, key);

        } catch (S3Exception e) {
            log.error(">>> S3 파일 삭제 에러: {}", e.getMessage());
            throw new RuntimeException("S3 파일 삭제 실패", e);
        }
    }

    /** S3 키 생성 (filePath + fileName) */
    private String buildKey(String filePath, String fileName) {
        return StorageHelper.normalizePath(filePath, fileName);
    }
}
