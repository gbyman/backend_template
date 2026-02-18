package app.backend.core.swagger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConditionalOnProperty(prefix = "springdoc.swagger-ui.info", name = "enabled", havingValue = "true")
@Configuration
@ConfigurationProperties(prefix = "springdoc.swagger-ui.info")
public class SwaggerInfoProperties {
    private boolean enabled;
    private String title;
    private String description;
    private String version;
    private Contact contact;
    private License license;
    private Boolean jwtHeaderEnabled;

    @Getter
    @Setter
    public static class Contact {
        private String name;
        private String email;
        private String url;
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }
}
