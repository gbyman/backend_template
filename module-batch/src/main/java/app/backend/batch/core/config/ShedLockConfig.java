package app.backend.batch.core.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

/**
 * ShedLock 설정 - 서버 이중화 환경에서 스케줄 작업 중복 실행 방지
 *
 * <p>동작 원리:
 *
 * <ul>
 *   <li>DB 테이블(shedlock)을 통해 분산 락 구현
 *   <li>특정 작업 실행 시 DB에 락 획득
 *   <li>다른 서버는 락이 해제될 때까지 대기 또는 스킵
 *   <li>락 최대 시간(lockAtMostFor) 설정으로 데드락 방지
 * </ul>
 *
 * <p>Quartz 클러스터링과 함께 사용하여 이중 보호
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M") // ISO-8601 기간 형식: 10분
public class ShedLockConfig {

    /** JDBC 기반 Lock Provider 생성 PostgreSQL의 shedlock 테이블 사용 */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // DB 시간 사용 (서버 시간 차이 무시)
                        .build());
    }
}
