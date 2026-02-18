package app.backend.core.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

/**
 * {@link YamlPropertySourceFactory} 단위 테스트
 *
 * <p>YAML 파일을 PropertySource로 변환하는 기능을 검증합니다.
 */
class YamlPropertySourceFactoryTest {

    private YamlPropertySourceFactory factory;

    @BeforeEach
    void setUp() {
        factory = new YamlPropertySourceFactory();
    }

    @Test
    @DisplayName("name이 제공된 경우 해당 name으로 PropertySource 생성")
    void testCreatePropertySource_WithName() throws Exception {
        // Given
        String propertySourceName = "customName";
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/test-config.yml"));

        // When
        PropertySource<?> propertySource =
                factory.createPropertySource(propertySourceName, resource);

        // Then
        assertThat(propertySource).isNotNull();
        assertThat(propertySource.getName()).isEqualTo(propertySourceName);
        assertThat(propertySource.getProperty("test.string-value")).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("name이 null인 경우 파일명으로 PropertySource 생성")
    void testCreatePropertySource_WithNullName() throws Exception {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/test-config.yml"));

        // When
        PropertySource<?> propertySource = factory.createPropertySource(null, resource);

        // Then
        assertThat(propertySource).isNotNull();
        assertThat(propertySource.getName()).isEqualTo("test-config.yml");
        assertThat(propertySource.getProperty("test.string-value")).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("YAML 파일의 모든 프로퍼티가 정상적으로 로드됨")
    void testCreatePropertySource_VerifyAllProperties() throws Exception {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/test-config.yml"));

        // When
        PropertySource<?> propertySource = factory.createPropertySource("test", resource);

        // Then
        assertThat(propertySource.getProperty("test.string-value")).isEqualTo("Hello World");
        assertThat(propertySource.getProperty("test.number-value")).isEqualTo(12_345);
        assertThat(propertySource.getProperty("test.boolean-value")).isEqualTo(true);
        assertThat(propertySource.getProperty("test.list-value[0]")).isEqualTo("item1");
        assertThat(propertySource.getProperty("test.list-value[1]")).isEqualTo("item2");
        assertThat(propertySource.getProperty("test.list-value[2]")).isEqualTo("item3");
        assertThat(propertySource.getProperty("test.map-value.key1")).isEqualTo("value1");
        assertThat(propertySource.getProperty("test.map-value.key2")).isEqualTo("value2");
        assertThat(propertySource.getProperty("test.map-value.nested.deep-key"))
                .isEqualTo("deep-value");
    }

    @Test
    @DisplayName("잘못된 YAML 형식인 경우 예외 발생")
    void testCreatePropertySource_WithInvalidYaml() {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/invalid-config.yml"));

        // When & Then
        // YAML 파서가 직접 예외를 던지므로 Exception으로 검증
        assertThatThrownBy(() -> factory.createPropertySource("invalid", resource))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("cannot start any token");
    }

    @Test
    @DisplayName("빈 YAML 파일도 정상적으로 처리됨")
    void testCreatePropertySource_WithEmptyYaml() throws Exception {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/empty-config.yml"));

        // When
        PropertySource<?> propertySource = factory.createPropertySource("empty", resource);

        // Then
        assertThat(propertySource).isNotNull();
        assertThat(propertySource.getName()).isEqualTo("empty");
        // 빈 YAML 파일은 프로퍼티가 없음
        assertThat(propertySource.getProperty("any.key")).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 파일인 경우 IOException 발생")
    void testCreatePropertySource_WithNonExistentFile() {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/non-existent.yml"));

        // When & Then
        assertThatThrownBy(() -> factory.createPropertySource("nonExistent", resource))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("숫자, 불린, 리스트 등 다양한 타입이 보존됨")
    void testCreatePropertySource_TypeConversion() throws Exception {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/test-config.yml"));

        // When
        PropertySource<?> propertySource = factory.createPropertySource("test", resource);

        // Then
        // YamlPropertiesFactoryBean은 타입을 보존함
        Object stringValue = propertySource.getProperty("test.string-value");
        Object numberValue = propertySource.getProperty("test.number-value");
        Object booleanValue = propertySource.getProperty("test.boolean-value");

        assertThat(stringValue).isInstanceOf(String.class);
        assertThat(numberValue).isInstanceOf(Integer.class);
        assertThat(booleanValue).isInstanceOf(Boolean.class);

        assertThat(stringValue).isEqualTo("Hello World");
        assertThat(numberValue).isEqualTo(12_345);
        assertThat(booleanValue).isEqualTo(true);
    }

    @Test
    @DisplayName("중첩된 맵 구조가 dot notation으로 정상 변환됨")
    void testCreatePropertySource_NestedMapConversion() throws Exception {
        // Given
        EncodedResource resource =
                new EncodedResource(new ClassPathResource("yaml-test/test-config.yml"));

        // When
        PropertySource<?> propertySource = factory.createPropertySource("test", resource);

        // Then
        assertThat(propertySource.getProperty("test.map-value.nested.deep-key"))
                .isEqualTo("deep-value");
    }
}
