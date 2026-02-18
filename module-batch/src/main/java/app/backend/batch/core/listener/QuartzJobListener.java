package app.backend.batch.core.listener;

import java.util.Optional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Quartz Job 실행 이벤트를 처리하는 리스너
 *
 * <p>모든 Quartz Job의 실행 전/후 이벤트를 감지하여 로깅, 모니터링, 메트릭 수집 등을 수행합니다.
 *
 * <p><strong>주요 기능:</strong>
 *
 * <ul>
 *   <li>Job 실행 시간 자동 측정
 *   <li>Job 성공/실패 로깅
 *   <li>Job 실행 거부(Veto) 감지
 *   <li>메트릭 저장 (확장 가능)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // Quartz Scheduler에 리스너 등록
 * &#64;Configuration
 * public class QuartzSchedulerConfig {
 *
 *     &#64;Bean
 *     public SchedulerFactoryBean schedulerFactoryBean() {
 *         SchedulerFactoryBean factory = new SchedulerFactoryBean();
 *
 *         // 글로벌 Job 리스너 등록
 *         factory.setGlobalJobListeners(new QuartzJobListener());
 *
 *         return factory;
 *     }
 * }
 * </pre>
 *
 * <p><strong>JobExecutionContext에 저장되는 데이터:</strong>
 *
 * <ul>
 *   <li>{@code startTime} - Job 시작 시간 (밀리초)
 * </ul>
 *
 * <p><strong>로그 출력 예시:</strong>
 *
 * <pre>
 * [INFO] Job : userBatchJob 실행 예정
 * [INFO] Job : userBatchJob 실행 성공
 * [INFO] Job : userBatchJob 실행시간 : 1234 ms
 * </pre>
 */
@Slf4j
public class QuartzJobListener implements JobListener {

    private static final String START_TIME_KEY = "startTime";

    /**
     * 리스너 이름을 반환합니다.
     *
     * @return 리스너 이름 (클래스명)
     */
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * Job 실행 직전에 호출됩니다.
     *
     * <p>Job 시작 시간을 JobExecutionContext에 저장하여 실행 시간을 측정합니다.
     *
     * @param context JobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        try {
            String jobName = context.getJobDetail().getKey().getName();
            log.info("Job : {} 실행 예정", jobName);

            // 시작 시간 기록
            context.put(START_TIME_KEY, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("jobToBeExecuted Exception: ", e);
        }
    }

    /**
     * Job 실행이 거부(Veto)되었을 때 호출됩니다.
     *
     * <p>TriggerListener에서 Job 실행을 거부한 경우 발생합니다.
     *
     * @param context JobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        try {
            String jobName = context.getJobDetail().getKey().getName();
            log.warn("Job : {} 실행 거부됨 (Vetoed)", jobName);
        } catch (Exception e) {
            log.error("jobExecutionVetoed Exception: ", e);
        }
    }

    /**
     * Job 실행 완료 후 호출됩니다.
     *
     * <p>Job 성공/실패 여부와 실행 시간을 로깅하고 메트릭을 저장합니다.
     *
     * @param context JobExecutionContext
     * @param jobException Job 실행 중 발생한 예외 (null이면 성공)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobName = context.getJobDetail().getKey().getName();

        // 시작 시간 조회 (없으면 현재 시간 사용)
        Long startTime =
                Optional.ofNullable(context.get(START_TIME_KEY))
                        .map(Long.class::cast)
                        .orElseGet(
                                () -> {
                                    log.warn("Job : {}, startTime 없음. 현재시간 사용", jobName);
                                    return System.currentTimeMillis();
                                });

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 성공/실패 로깅
        if (jobException != null) {
            log.error(
                    "Job : {} 실행 실패. Exception : {}",
                    jobName,
                    jobException.getMessage(),
                    jobException);
        } else {
            log.info("Job : {} 실행 성공", jobName);
        }

        // 실행 시간 로깅
        log.info("Job : {} 실행시간 : {} ms", jobName, executionTime);

        // 메트릭 저장
        try {
            saveJobMetrics(jobName, executionTime, jobException == null);
        } catch (Exception e) {
            log.error("Job 메트릭 저장 실패 : ", e);
        }
    }

    /**
     * Job 실행 메트릭을 저장합니다.
     *
     * <p>현재는 로깅만 수행하며, 필요 시 다음과 같이 확장 가능합니다:
     *
     * <ul>
     *   <li>Micrometer로 메트릭 전송
     *   <li>DB에 실행 이력 저장
     *   <li>Slack/Email 알림 발송
     *   <li>Prometheus/Grafana 연동
     * </ul>
     *
     * <p><strong>확장 예시 (Micrometer):</strong>
     *
     * <pre>
     * private void saveJobMetrics(String jobName, long executionTime, boolean success) {
     *     meterRegistry.timer("quartz.job.execution",
     *             "job.name", jobName,
     *             "success", String.valueOf(success))
     *         .record(executionTime, TimeUnit.MILLISECONDS);
     * }
     * </pre>
     *
     * @param jobName Job 이름
     * @param executionTime 실행 시간 (밀리초)
     * @param success 성공 여부
     */
    private void saveJobMetrics(String jobName, long executionTime, boolean success) {
        // 현재는 로깅만 수행
        log.debug(
                "Job 메트릭 - name: {}, executionTime: {} ms, success: {}",
                jobName,
                executionTime,
                success);

        // TODO: 필요 시 Micrometer, DB, 알림 등으로 확장
        // Example:
        // meterRegistry.timer("quartz.job.execution", "job.name", jobName, "success",
        // String.valueOf(success))
        //     .record(executionTime, TimeUnit.MILLISECONDS);
    }
}
