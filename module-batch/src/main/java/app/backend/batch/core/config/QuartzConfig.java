package app.backend.batch.core.config;

import javax.sql.DataSource;

import org.quartz.JobListener;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.backend.batch.core.listener.QuartzJobListener;

/**
 * Quartz 클러스터링 및 모니터링 설정
 *
 * <p>서버 이중화 환경에서 배치 작업 중복 실행 방지:
 *
 * <ul>
 *   <li>JDBC JobStore 사용 (메모리 대신 DB)
 *   <li>여러 서버가 동일한 Quartz 테이블 공유
 *   <li>DB 락을 통해 한 서버에서만 실행
 *   <li>서버 장애 시 다른 서버가 자동으로 작업 인계
 * </ul>
 *
 * <p>Job 모니터링:
 *
 * <ul>
 *   <li>{@link QuartzJobListener}를 통한 Job 실행 시간 측정
 *   <li>Job 성공/실패 로깅
 *   <li>메트릭 수집 (확장 가능)
 * </ul>
 *
 * <p>application.yml 설정:
 *
 * <pre>
 * spring:
 *   quartz:
 *     job-store-type: jdbc
 *     properties:
 *       org.quartz.jobStore.isClustered: true
 *       org.quartz.scheduler.instanceId: AUTO
 * </pre>
 */
@Configuration
public class QuartzConfig {

    /**
     * Quartz Job 실행 모니터링 리스너
     *
     * <p>모든 Quartz Job의 실행 전/후 이벤트를 감지하여:
     *
     * <ul>
     *   <li>Job 실행 시간 자동 측정
     *   <li>Job 성공/실패 로깅
     *   <li>메트릭 수집 (확장 가능)
     * </ul>
     *
     * <p>이 리스너를 Scheduler에 등록하려면:
     *
     * <pre>
     * &#64;Bean
     * public SchedulerFactoryBean schedulerFactoryBean(JobListener jobListener) {
     *     SchedulerFactoryBean factory = new SchedulerFactoryBean();
     *     factory.setGlobalJobListeners(jobListener);
     *     return factory;
     * }
     * </pre>
     *
     * @return QuartzJobListener 인스턴스
     * @see QuartzJobListener
     */
    @Bean
    public JobListener quartzJobListener() {
        return new QuartzJobListener();
    }

    /**
     * Quartz 전용 DataSource (선택사항) 배치 작업과 Quartz 스케줄링을 별도 DB로 분리하려면 사용
     *
     * <p>주의: application.yml에 spring.quartz.datasource 설정 필요
     */
    @Bean
    @QuartzDataSource
    @ConfigurationProperties(prefix = "spring.quartz.datasource")
    public DataSource quartzDataSource() {
        return DataSourceBuilder.create().build();
    }
}
