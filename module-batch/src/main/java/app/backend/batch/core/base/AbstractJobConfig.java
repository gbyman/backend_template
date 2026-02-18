package app.backend.batch.core.base;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

/**
 * Spring Batch Job 설정을 위한 기본 추상 클래스
 *
 * <p>모든 배치 Job은 이 클래스를 상속받아 구현합니다. Job, Step 생성을 위한 헬퍼 메서드를 제공하여 보일러플레이트 코드를 줄입니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * &#64;Configuration
 * public class UserBatchJobConfig extends AbstractJobConfig {
 *
 *     public UserBatchJobConfig(JobRepository jobRepository,
 *                               PlatformTransactionManager transactionManager) {
 *         super(jobRepository, transactionManager);
 *     }
 *
 *     &#64;Bean
 *     public Job userJob(Step step1, Step step2) {
 *         return createJobBuilder("userJob")
 *                 .start(step1)
 *                 .next(step2)
 *                 .build();
 *     }
 *
 *     &#64;Bean
 *     public Step step1() {
 *         return createMethodExecutionStep("step1", () -&gt; {
 *             System.out.println("Step 1 executed");
 *         });
 *     }
 *
 *     &#64;Bean
 *     public Step step2(ItemReader&lt;User&gt; reader, ItemWriter&lt;User&gt; writer) {
 *         return createReaderWriterStep("step2", reader, writer);
 *     }
 * }
 * </pre>
 *
 * <p><strong>제공 헬퍼 메서드:</strong>
 *
 * <ul>
 *   <li>{@link #createJobBuilder(String)} - JobBuilder 생성
 *   <li>{@link #createStepBuilder(String)} - StepBuilder 생성
 *   <li>{@link #createReaderWriterStep(String, ItemReader, ItemWriter)} - Reader-Writer Step
 *   <li>{@link #createTaskletStep(String, Tasklet)} - Tasklet Step
 *   <li>{@link #createMethodExecutionStep(String, Runnable)} - 람다 실행 Step
 * </ul>
 */
@RequiredArgsConstructor
public abstract class AbstractJobConfig {
    private static final int DEFAULT_CHUNK_SIZE = 100;

    protected final JobRepository jobRepository;

    protected final PlatformTransactionManager transactionManager;

    protected final int defaultChunk;

    public AbstractJobConfig(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.defaultChunk = DEFAULT_CHUNK_SIZE;
    }

    /**
     * JobBuilder를 생성합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Job myJob(Step step1, Step step2) {
     *     return createJobBuilder("myJob")
     *             .start(step1)
     *             .next(step2)
     *             .build();
     * }
     * </pre>
     *
     * @param jobName Job 이름
     * @return JobBuilder
     */
    protected final JobBuilder createJobBuilder(@NonNull String jobName) {
        return new JobBuilder(jobName, jobRepository);
    }

    /**
     * StepBuilder를 생성합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Step customStep() {
     *     return createStepBuilder("customStep")
     *             .&lt;User, User&gt;chunk(100, transactionManager)
     *             .reader(reader)
     *             .processor(processor)
     *             .writer(writer)
     *             .build();
     * }
     * </pre>
     *
     * @param stepName Step 이름
     * @return StepBuilder
     */
    protected final StepBuilder createStepBuilder(@NonNull String stepName) {
        return new StepBuilder(stepName, jobRepository);
    }

    /**
     * Reader-Writer 기반 StepBuilder를 생성합니다.
     *
     * <p>chunk 크기는 {@link #defaultChunk} (기본값: 100)을 사용합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Step processStep() {
     *     return createReaderWriterStepBuilder("processStep", reader, writer)
     *             .processor(processor)  // processor 추가 가능
     *             .listener(listener)    // listener 추가 가능
     *             .build();
     * }
     * </pre>
     *
     * @param stepName Step 이름
     * @param reader ItemReader
     * @param writer ItemWriter
     * @param <I> ItemReader 타입
     * @param <O> ItemWriter 타입
     * @return SimpleStepBuilder
     */
    protected final <I, O> SimpleStepBuilder<I, O> createReaderWriterStepBuilder(
            @NonNull String stepName,
            @NonNull ItemReader<I> reader,
            @NonNull ItemWriter<O> writer) {

        return createStepBuilder(stepName)
                .<I, O>chunk(defaultChunk, transactionManager)
                .reader(reader)
                .writer(writer);
    }

    /**
     * Reader-Writer 기반 Step을 생성합니다.
     *
     * <p>ItemReader에서 데이터를 읽어 ItemWriter로 쓰는 단순 ETL Step을 생성합니다. chunk 크기는 {@link #defaultChunk}
     * (기본값: 100)을 사용합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Step etlStep(ItemReader&lt;User&gt; reader, ItemWriter&lt;User&gt; writer) {
     *     return createReaderWriterStep("etlStep", reader, writer);
     * }
     * </pre>
     *
     * @param stepName Step 이름
     * @param reader ItemReader
     * @param writer ItemWriter
     * @param <I> ItemReader 타입
     * @param <O> ItemWriter 타입
     * @return Step
     */
    protected final <I, O> Step createReaderWriterStep(
            @NonNull String stepName,
            @NonNull ItemReader<I> reader,
            @NonNull ItemWriter<O> writer) {

        return createReaderWriterStepBuilder(stepName, reader, writer).build();
    }

    /**
     * Tasklet 기반 Step을 생성합니다.
     *
     * <p>단일 작업(Tasklet)을 수행하는 Step을 생성합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Step cleanupStep() {
     *     return createTaskletStep("cleanupStep", (contribution, chunkContext) -&gt; {
     *         // 정리 작업 수행
     *         fileService.deleteTemporaryFiles();
     *         return RepeatStatus.FINISHED;
     *     });
     * }
     * </pre>
     *
     * @param stepName Step 이름
     * @param tasklet Tasklet 구현체
     * @return Step
     */
    protected final Step createTaskletStep(@NonNull String stepName, @NonNull Tasklet tasklet) {
        return createStepBuilder(stepName).tasklet(tasklet, transactionManager).build();
    }

    /**
     * 메서드 실행 Step을 생성합니다.
     *
     * <p>람다 또는 메서드 참조를 사용하여 간단한 작업을 수행하는 Step을 생성합니다. Tasklet으로 감싸져 실행됩니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;Bean
     * public Step notificationStep() {
     *     return createMethodExecutionStep("notificationStep", () -&gt; {
     *         emailService.sendBatchCompletionEmail();
     *     });
     * }
     *
     * // 또는 메서드 참조 사용
     * &#64;Bean
     * public Step cleanupStep() {
     *     return createMethodExecutionStep("cleanupStep", cacheService::clearAll);
     * }
     * </pre>
     *
     * @param stepName Step 이름
     * @param logic 실행할 로직
     * @return Step
     */
    protected final Step createMethodExecutionStep(
            @NonNull String stepName, @NonNull Runnable logic) {
        return createTaskletStep(
                stepName,
                (contribution, chunkContext) -> {
                    logic.run();
                    return RepeatStatus.FINISHED;
                });
    }
}
