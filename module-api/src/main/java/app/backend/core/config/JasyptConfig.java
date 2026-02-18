package app.backend.core.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * Jasypt 암호화 설정
 *
 * <p>사용 방법: 1. 암호화할 값 생성: ./gradlew :module-api:bootRun
 * --args='--jasypt.encryptor.password=your-secret-key' 또는 암호화 유틸리티 사용
 *
 * <p>2. application.yml에 암호화된 값 설정: datasource: password: ENC(암호화된값)
 *
 * <p>3. 실행 시 암호화 키 전달: java -jar app.jar --jasypt.encryptor.password=your-secret-key 또는 환경변수:
 * export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
 */
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    @Value("${jasypt.encryptor.password:default-secret-key}")
    private String encryptorPassword;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 암호화 키 (환경변수 또는 시스템 속성으로 주입)
        config.setPassword(encryptorPassword);

        // 암호화 알고리즘
        config.setAlgorithm("PBEWithMD5AndDES");

        // 키 획득 반복 횟수 (보안 강화)
        config.setKeyObtentionIterations("1000");

        // 풀 크기 (멀티스레드 환경)
        config.setPoolSize("1");

        // Salt 생성기
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");

        // IV 생성기 (초기화 벡터)
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");

        // String 출력 타입
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor;
    }
}
