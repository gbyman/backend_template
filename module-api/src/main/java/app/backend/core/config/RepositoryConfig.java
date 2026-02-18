package app.backend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/** Repository 설정 JPA와 Redis Repository를 분리하여 활성화합니다. */
@Configuration
@EnableJpaRepositories(
        basePackages = {
            "app.backend.app", // 애플리케이션 도메인 Repository (JPA)
            "app.backend.infra" // 인프라 Repository (JPA)
        })
@EnableRedisRepositories(
        basePackages = "app.backend.core.jwt.domain.refreshtoken.repository" // Redis Repository만 스캔
        )
public class RepositoryConfig {}
