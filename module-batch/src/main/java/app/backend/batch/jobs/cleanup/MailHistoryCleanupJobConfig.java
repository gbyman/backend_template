package app.backend.batch.jobs.cleanup;

import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import app.backend.batch.core.base.AbstractJobConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 메일 발송 이력 정리 배치 Job
 *
 * <p>보관 기간(retentionDays)이 지난 메일 이력을 삭제한다. 기본 보관 기간: 90일
 *
 * <p>스케줄: schedule.yml에서 관리 (기본 매일 새벽 2시)
 *
 * <h3>왜 JPA가 아닌 JDBC를 사용하는가?</h3>
 *
 * <p>module-batch는 module-common만 의존하고, module-api에는 의존하지 않는다.
 *
 * <pre>
 * module-api   ──depends on──→ module-common  (MailHistoryEntity는 여기에 있음)
 * module-batch ──depends on──→ module-common  (Entity 클래스 접근 불가)
 * </pre>
 *
 * <p>MailHistoryEntity가 module-api에 있으므로 module-batch에서 JPA Repository를 사용할 수 없다. JdbcTemplate은 테이블
 * 이름과 SQL만 알면 Entity 클래스 없이 동작하므로 모듈 간 의존 없이 독립 실행이 가능하다.
 *
 * <p>또한 배치에서는 대량 데이터 처리 시 JPA 영속성 컨텍스트 오버헤드가 없는 JDBC가 더 효율적이다.
 */
@Slf4j
@Configuration
public class MailHistoryCleanupJobConfig extends AbstractJobConfig {

    public static final String JOB_NAME = "mailHistoryCleanupJob";
    private static final String STEP_NAME = "mailHistoryCleanupStep";
    private static final int DEFAULT_RETENTION_DAYS = 90;

    private final JdbcTemplate jdbcTemplate;

    public MailHistoryCleanupJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        super(jobRepository, transactionManager);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Bean
    public Job mailHistoryCleanupJob() {
        return new JobBuilder(JOB_NAME, jobRepository).start(mailHistoryCleanupStep()).build();
    }

    @Bean
    public Step mailHistoryCleanupStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            int retentionDays = getRetentionDays(chunkContext);
                            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

                            int deletedCount =
                                    jdbcTemplate.update(
                                            "DELETE FROM TB_MAIL_HISTORY WHERE SENT_AT < ?",
                                            cutoffDate);

                            log.info(
                                    "[{}] 보관 기간 {}일 초과 메일 이력 {}건 삭제 완료 (기준일: {})",
                                    JOB_NAME,
                                    retentionDays,
                                    deletedCount,
                                    cutoffDate);

                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }

    private int getRetentionDays(
            org.springframework.batch.core.scope.context.ChunkContext chunkContext) {
        try {
            Object param = chunkContext.getStepContext().getJobParameters().get("retentionDays");
            if (param instanceof Long) {
                return ((Long) param).intValue();
            }
            if (param instanceof String) {
                return Integer.parseInt((String) param);
            }
        } catch (Exception e) {
            log.debug("retentionDays 파라미터 파싱 실패, 기본값 사용: {}", DEFAULT_RETENTION_DAYS);
        }
        return DEFAULT_RETENTION_DAYS;
    }
}
