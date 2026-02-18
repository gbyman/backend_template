package app.backend.batch.quartz.executor;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import app.backend.batch.core.batch.BatchJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz가 실행하는 Job 클래스
 *
 * <p>Quartz 스케줄에 의해 호출되면, JobDataMap에서 Spring Batch Job 이름을 꺼내 BatchJobRunner로 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzBatchJobExecutor extends QuartzJobBean {

    private final BatchJobRunner batchJobRunner;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String jobName = jobDataMap.getString("jobName");

        log.info("[Quartz] Batch Job 실행 시작: {}", jobName);

        try {
            batchJobRunner.run(jobName, jobDataMap.getWrappedMap());
            log.info("[Quartz] Batch Job 실행 완료: {}", jobName);
        } catch (Exception e) {
            log.error("[Quartz] Batch Job 실행 실패: {}", jobName, e);
        }
    }
}
