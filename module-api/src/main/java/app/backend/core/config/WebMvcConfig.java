package app.backend.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import app.backend.core.interceptor.RequestContextInterceptor;
import app.backend.core.property.I18nProperties;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(I18nProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final I18nProperties i18nProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 요청 컨텍스트 인터셉터
        registry.addInterceptor(new RequestContextInterceptor(i18nProperties))
                .addPathPatterns("/api/**");
    }
}
