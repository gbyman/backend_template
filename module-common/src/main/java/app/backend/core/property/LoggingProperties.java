package app.backend.core.property;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 로깅 설정 Properties
 *
 * <p>API 접근 로그 저장 설정을 관리합니다.
 *
 * <p><strong>설정 예시 (application.yml):</strong>
 *
 * <pre>
 * logging:
 *   aspect:
 *     enabled: true
 *     exclude-urls:
 *       - /actuator/**
 *       - /health
 *       - /swagger-ui/**
 *       - /v3/api-docs/**
 * </pre>
 *
 * <p><strong>제외 URL 패턴:</strong>
 *
 * <ul>
 *   <li>AntPathMatcher 패턴 사용
 *   <li>예시: /api/**, /*.html, /health
 * </ul>
 *
 * @see app.backend.core.aspect.DatabaseLoggingAspect
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "logging.aspect")
public class LoggingProperties {

    /** 로깅 활성화 여부 (기본값: true) */
    private boolean enabled = true;

    /** 로깅 제외 URL 패턴 리스트 */
    private List<String> excludeUrls = new ArrayList<>();
}
