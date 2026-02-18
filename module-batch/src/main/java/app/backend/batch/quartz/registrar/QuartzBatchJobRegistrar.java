package app.backend.batch.quartz.registrar;

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import app.backend.batch.core.property.QuartzJobProperties;
import app.backend.batch.quartz.executor.QuartzBatchJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * schedule.yml에 정의된 Job을 Quartz 스케줄러에 동적으로 등록한다.
 *
 * <p>registered: true인 Job만 등록되며, false인 Job은 스킵한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzBatchJobRegistrar {

    private final Scheduler scheduler;
    private final QuartzJobProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void registerJobs() {
        for (QuartzJobProperties.JobConfig jobConfig : properties.getJobs()) {
            if (!jobConfig.isRegistered()) {
                log.debug("[Quartz] Job 스킵 (registered=false): {}", jobConfig.getName());
                continue;
            }

            try {
                registerJob(jobConfig);
                log.info(
                        "[Quartz] Job 등록 완료: {} (cron: {})",
                        jobConfig.getName(),
                        jobConfig.getCron());
            } catch (Exception e) {
                log.error("[Quartz] Job 등록 실패: {}", jobConfig.getName(), e);
            }
        }
    }

    private void registerJob(QuartzJobProperties.JobConfig jobConfig) throws Exception {
        String jobName = jobConfig.getName();
        String groupName = "BATCH_GROUP";

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", jobName);
        if (jobConfig.getParams() != null) {
            for (Map.Entry<String, Object> entry : jobConfig.getParams().entrySet()) {
                jobDataMap.put(entry.getKey(), entry.getValue());
            }
        }

        JobDetail jobDetail =
                JobBuilder.newJob(QuartzBatchJobExecutor.class)
                        .withIdentity(jobName, groupName)
                        .withDescription(jobConfig.getDescription())
                        .usingJobData(jobDataMap)
                        .storeDurably()
                        .requestRecovery()
                        .build();

        Trigger trigger =
                TriggerBuilder.newTrigger()
                        .withIdentity(jobName + "Trigger", groupName)
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(jobConfig.getCron())
                                        .withMisfireHandlingInstructionDoNothing())
                        .forJob(jobDetail)
                        .build();

        // 이미 등록된 Job이면 교체, 없으면 새로 등록
        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
