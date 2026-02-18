package app.backend.core.factory;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

/**
 * YAML 파일을 PropertySource로 로드하는 팩토리
 *
 * <p>Spring의 {@code @PropertySource}는 기본적으로 .properties 파일만 지원합니다. 이 팩토리를 사용하면 YAML 파일도
 * PropertySource로 사용할 수 있습니다.
 *
 * <p><strong>사용 사례:</strong>
 *
 * <ul>
 *   <li>모듈별 설정 파일 분리 (application.yml 외 추가 YAML 파일)
 *   <li>환경별 설정 파일 관리 (dev.yml, prod.yml 등)
 *   <li>기능별 설정 파일 분리 (batch.yml, security.yml 등)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 1. 단일 YAML 파일 로드
 * &#64;Configuration
 * &#64;PropertySource(
 *     value = "classpath:custom-config.yml",
 *     factory = YamlPropertySourceFactory.class
 * )
 * public class CustomConfig {
 *     &#64;Value("${custom.property}")
 *     private String customProperty;
 * }
 *
 * // 2. 여러 YAML 파일 로드
 * &#64;Configuration
 * &#64;PropertySource(
 *     value = {
 *         "classpath:batch-config.yml",
 *         "classpath:mail-config.yml"
 *     },
 *     factory = YamlPropertySourceFactory.class
 * )
 * public class AppConfig { }
 *
 * // 3. 환경별 YAML 파일 (ignoreResourceNotFound 사용)
 * &#64;Configuration
 * &#64;PropertySource(
 *     value = "classpath:config-${spring.profiles.active}.yml",
 *     factory = YamlPropertySourceFactory.class,
 *     ignoreResourceNotFound = true
 * )
 * public class ProfileConfig { }
 * </pre>
 *
 * <p><strong>⚠️ 주의사항:</strong>
 *
 * <ul>
 *   <li>application.yml은 자동으로 로드되므로 이 팩토리 불필요
 *   <li>YAML 파일 형식이 올바르지 않으면 애플리케이션 시작 실패
 *   <li>{@code ignoreResourceNotFound=true} 사용 시 파일 없어도 에러 없음
 * </ul>
 *
 * <p><strong>YAML 파일 예시:</strong>
 *
 * <pre>
 * # custom-config.yml
 * custom:
 *   property: value
 *   list:
 *     - item1
 *     - item2
 *   map:
 *     key1: value1
 *     key2: value2
 * </pre>
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * YAML 파일에서 PropertySource를 생성합니다.
     *
     * @param name 프로퍼티 소스 이름 (nullable, null이면 파일명 사용)
     * @param resource YAML 파일 리소스
     * @return PropertySource 객체
     * @throws IOException YAML 파일 읽기 실패 시
     * @throws IllegalArgumentException YAML 파싱 실패 시
     */
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource)
            throws IOException {

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        Properties properties = factory.getObject();

        // YAML 파싱 실패 시 null 반환 (잘못된 형식)
        if (properties == null) {
            throw new IllegalArgumentException(
                    "Failed to load YAML properties from resource: "
                            + resource.getResource().getFilename());
        }

        // name이 null이면 파일명 사용
        String propertySourceName = name;
        if (propertySourceName == null) {
            String filename = resource.getResource().getFilename();
            if (filename == null) {
                throw new IllegalArgumentException("Resource filename cannot be null");
            }
            propertySourceName = filename;
        }

        return new PropertiesPropertySource(propertySourceName, properties);
    }
}
