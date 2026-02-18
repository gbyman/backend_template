package app.backend.batch.core.utils;

import javax.sql.DataSource;

import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.experimental.UtilityClass;

/**
 * 다중 DB 환경에서 Spring Batch 컴포넌트를 생성하는 유틸리티
 *
 * <p>여러 개의 DataSource를 사용하는 프로젝트에서 각 DB별로 JobRepository, JobExplorer, JobLauncher를 생성합니다.
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>다중 DB 환경 (예: DB1, DB2, DB3)
 *   <li>DB별로 독립적인 Spring Batch 메타데이터 관리
 *   <li>Batch Job 메타데이터를 별도 DB에 저장
 * </ul>
 *
 * <p><strong>단일 DB 환경에서는 사용 불필요</strong> - Spring Boot Auto-configuration이 자동으로 생성
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 다중 DB 설정 예시
 * &#64;Configuration
 * public class MultiDataSourceBatchConfig {
 *
 *     &#64;Bean
 *     &#64;ConfigurationProperties(prefix = "spring.datasource.db1")
 *     public DataSource db1DataSource() {
 *         return DataSourceBuilder.create().build();
 *     }
 *
 *     &#64;Bean
 *     &#64;ConfigurationProperties(prefix = "spring.datasource.db2")
 *     public DataSource db2DataSource() {
 *         return DataSourceBuilder.create().build();
 *     }
 *
 *     &#64;Bean
 *     public JobRepository db1JobRepository(
 *             &#64;Qualifier("db1DataSource") DataSource dataSource,
 *             PlatformTransactionManager transactionManager) throws Exception {
 *         return BatchJobProvider.createJobRepository(dataSource, transactionManager);
 *     }
 *
 *     &#64;Bean
 *     public JobExplorer db1JobExplorer(
 *             &#64;Qualifier("db1DataSource") DataSource dataSource,
 *             PlatformTransactionManager transactionManager) throws Exception {
 *         return BatchJobProvider.createJobExplorer(dataSource, transactionManager);
 *     }
 *
 *     &#64;Bean
 *     public JobLauncher db1JobLauncher(
 *             &#64;Qualifier("db1JobRepository") JobRepository jobRepository) throws Exception {
 *         return BatchJobProvider.createJobLauncher(jobRepository);
 *     }
 * }
 * </pre>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>각 DataSource에 Spring Batch 메타데이터 테이블 필요 (BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION 등)
 *   <li>테이블 prefix는 "BATCH_" (기본값)
 *   <li>DB 타입 자동 감지 (MySQL, PostgreSQL, Oracle 등)
 * </ul>
 *
 * @see JobRepository
 * @see JobExplorer
 * @see JobLauncher
 */
@UtilityClass
public class BatchJobProvider {

    /** Spring Batch 메타데이터 테이블 prefix */
    private static final String BATCH_TABLE_PREFIX = "BATCH_";

    /** Transaction Isolation Level for Job Repository */
    private static final String ISOLATION_LEVEL = "ISOLATION_DEFAULT";

    /**
     * 지정된 DataSource로 JobRepository를 생성합니다.
     *
     * <p>JobRepository는 Spring Batch Job의 실행 메타데이터를 저장하는 저장소입니다.
     *
     * <p><strong>생성되는 설정:</strong>
     *
     * <ul>
     *   <li>테이블 prefix: BATCH_
     *   <li>DB 타입: 자동 감지
     *   <li>Isolation Level: ISOLATION_DEFAULT
     *   <li>ID 자동 증가: DataFieldMaxValueIncrementer 사용
     * </ul>
     *
     * @param dataSource Spring Batch 메타데이터를 저장할 DataSource
     * @param transactionManager Transaction Manager
     * @return JobRepository 인스턴스
     * @throws Exception JobRepository 생성 실패 시
     */
    public static JobRepository createJobRepository(
            @NonNull DataSource dataSource, @NonNull PlatformTransactionManager transactionManager)
            throws Exception {

        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);

        // DB 타입 자동 감지
        String databaseType = DatabaseType.fromMetaData(dataSource).name();
        factory.setDatabaseType(databaseType);

        // ID 자동 증가 설정
        factory.setIncrementerFactory(new DefaultDataFieldMaxValueIncrementerFactory(dataSource));

        // Transaction Isolation Level
        factory.setIsolationLevelForCreate(ISOLATION_LEVEL);

        // 테이블 prefix
        factory.setTablePrefix(BATCH_TABLE_PREFIX);

        // JdbcOperations 설정
        factory.setJdbcOperations(new JdbcTemplate(dataSource));

        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 지정된 DataSource로 JobExplorer를 생성합니다.
     *
     * <p>JobExplorer는 Job 실행 메타데이터를 조회하는 읽기 전용 인터페이스입니다.
     *
     * <p><strong>주요 기능:</strong>
     *
     * <ul>
     *   <li>Job 실행 이력 조회
     *   <li>Job Instance 조회
     *   <li>Step Execution 조회
     * </ul>
     *
     * @param dataSource Spring Batch 메타데이터가 저장된 DataSource
     * @param transactionManager Transaction Manager
     * @return JobExplorer 인스턴스
     * @throws Exception JobExplorer 생성 실패 시
     */
    public static JobExplorer createJobExplorer(
            @NonNull DataSource dataSource, @NonNull PlatformTransactionManager transactionManager)
            throws Exception {

        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTransactionManager(transactionManager);

        // JdbcOperations 설정
        factoryBean.setJdbcOperations(new JdbcTemplate(dataSource));

        // 테이블 prefix
        factoryBean.setTablePrefix(BATCH_TABLE_PREFIX);

        // Job Key Generator
        factoryBean.setJobKeyGenerator(new DefaultJobKeyGenerator());

        // Conversion Service
        factoryBean.setConversionService(new DefaultConversionService());

        // ExecutionContext Serializer
        factoryBean.setSerializer(new DefaultExecutionContextSerializer());

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * 지정된 JobRepository로 JobLauncher를 생성합니다.
     *
     * <p>JobLauncher는 Spring Batch Job을 실행하는 인터페이스입니다.
     *
     * <p><strong>생성되는 설정:</strong>
     *
     * <ul>
     *   <li>동기 실행 (기본 TaskExecutor 사용)
     *   <li>Job 실행 완료까지 대기
     * </ul>
     *
     * <p><strong>비동기 실행이 필요한 경우:</strong>
     *
     * <pre>
     * TaskExecutorJobLauncher launcher = (TaskExecutorJobLauncher)
     *     BatchJobProvider.createJobLauncher(jobRepository);
     * launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());
     * </pre>
     *
     * @param jobRepository Job 메타데이터를 저장할 JobRepository
     * @return JobLauncher 인스턴스
     * @throws Exception JobLauncher 생성 실패 시
     */
    public static JobLauncher createJobLauncher(@NonNull JobRepository jobRepository)
            throws Exception {

        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        taskExecutorJobLauncher.afterPropertiesSet();

        return taskExecutorJobLauncher;
    }
}
