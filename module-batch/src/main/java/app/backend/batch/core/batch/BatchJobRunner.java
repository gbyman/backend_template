package app.backend.batch.core.batch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Spring Batch Job 실행기 Map 또는 DTO 형태의 파라미터를 JobParameters로 변환하여 Job 실행 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRunner {

    private final ApplicationContext applicationContext;
    private final JobLauncher jobLauncher;

    @SuppressWarnings("unchecked")
    public void run(String jobName, Object params) {

        try {
            Job job = (Job) applicationContext.getBean(jobName);

            JobParameters jobParameters;
            if (params instanceof Map<?, ?> map) {
                jobParameters = buildJobParametersFromMap((Map<String, Object>) map);
            } else {
                jobParameters = buildJobParametersFromDto(params);
            }

            jobLauncher.run(job, jobParameters);

        } catch (Exception e) {
            log.error("Batch job execution failed for job '{}'", jobName, e);
            throw new RuntimeException("Batch job execution failed", e);
        }
    }

    private JobParameters buildJobParametersFromMap(Map<String, Object> jobDataMap) {
        return buildJobParameters(
                consumer -> {
                    for (Map.Entry<String, Object> entry : jobDataMap.entrySet()) {
                        consumer.accept(entry.getKey(), entry.getValue());
                    }
                });
    }

    private JobParameters buildJobParametersFromDto(Object dto) {
        return buildJobParameters(
                consumer -> {
                    Field[] fields = dto.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        try {
                            consumer.accept(field.getName(), field.get(dto));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to access field in DTO", e);
                        }
                    }
                });
    }

    private JobParameters buildJobParameters(Consumer<BiConsumer<String, Object>> fieldProcessor) {
        JobParametersBuilder builder = new JobParametersBuilder();

        fieldProcessor.accept((key, value) -> applyJobParameter(builder, key, value));

        // 고유 식별자 추가 (중복 실행 방지)
        builder.addString("uuid", UUID.randomUUID().toString());
        return builder.toJobParameters();
    }

    private void applyJobParameter(JobParametersBuilder builder, String key, Object value) {
        if (value instanceof String) {
            builder.addString(key, (String) value);
        } else if (value instanceof Float || value instanceof Double) {
            builder.addDouble(key, ((Number) value).doubleValue());
        } else if (value instanceof Integer || value instanceof Long) {
            builder.addLong(key, ((Number) value).longValue());
        } else if (value instanceof Date) {
            builder.addDate(key, (Date) value);
        } else if (value instanceof LinkedHashMap<?, ?> map) {
            builder.addJobParameter(key, new ArrayList<>(map.values()), List.class);
        } else if (value instanceof ArrayList<?> list) {
            builder.addJobParameter(key, list, List.class);
        } else {
            builder.addJobParameter(key, value, Object.class);
        }
    }
}
