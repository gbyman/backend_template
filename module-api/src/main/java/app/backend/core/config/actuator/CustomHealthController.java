package app.backend.core.config.actuator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.backend.core.base.component.AbstractController;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@Hidden // Swagger에서 숨김
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomHealthController extends AbstractController {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Value("${spring.application.name:backend_template}")
    private String applicationName;

    private final HealthEndpoint healthEndpoint;

    /** 환경 정보 조회 현재 실행 중인 환경(profile) 정보를 반환합니다. */
    @Operation(summary = "환경 정보 조회", description = "현재 실행 중인 환경 정보를 반환합니다")
    @GetMapping("/env")
    public Map<String, Object> getEnvironment() {
        Map<String, Object> env = new HashMap<>();
        env.put("applicationName", applicationName);
        env.put("profile", activeProfile);
        env.put("timestamp", LocalDateTime.now());
        env.put("javaVersion", System.getProperty("java.version"));
        env.put("osName", System.getProperty("os.name"));

        return env;
    }

    /** 헬스 체크 애플리케이션의 상태를 반환합니다. */
    @Operation(summary = "헬스 체크", description = "애플리케이션의 상태를 반환합니다")
    @GetMapping("/health")
    public Map<String, Object> health() {
        HealthComponent healthComponent = healthEndpoint.health();

        Map<String, Object> response = new HashMap<>();
        response.put("status", healthComponent.getStatus().getCode());
        response.put("profile", activeProfile);
        response.put("timestamp", LocalDateTime.now());

        // 로컬 환경에서만 상세 정보 표시
        if ("local".equals(activeProfile) && healthComponent instanceof Health health) {
            response.put("details", health.getDetails());
        }

        return response;
    }
}
