package app.backend.infra.file.local;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.backend.infra.file.StorageHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 파일 시스템 Storage 설정
 *
 * <p>⚠️ 주의사항: - 로컬 파일 시스템을 사용하지 않으면 이 파일과 local 패키지 전체를 삭제하세요. - storage.type=local 설정이 있을 때만
 * 활성화됩니다.
 *
 * <p>application.yml 설정 예시: storage: type: local local: base-path: /var/app/uploads # 파일 저장 기본 경로
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalStorageConfig {

    @Bean
    public StorageHelper storageHelper() {
        log.info(">>> LocalStorageHelper 빈 등록");
        return new LocalStorageHelper();
    }
}
