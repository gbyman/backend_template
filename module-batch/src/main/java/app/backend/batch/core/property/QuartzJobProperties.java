package app.backend.batch.core.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** schedule.yml의 spring.quartz.jobs 설정을 매핑하는 Properties 클래스 */
@Component
@ConfigurationProperties(prefix = "spring.quartz")
@Getter
@Setter
public class QuartzJobProperties {

    private List<JobConfig> jobs = new ArrayList<>();

    /** 개별 Job 설정 */
    @Getter
    @Setter
    public static class JobConfig {
        /** Job Bean 이름 (Spring Batch Job 이름과 일치) */
        private String name;

        /** Job 설명 */
        private String description;

        /** Cron 표현식 */
        private String cron;

        /** Quartz 등록 여부 (false면 비활성화) */
        private boolean registered;

        /** Job 파라미터 */
        private Map<String, Object> params = new HashMap<>();
    }
}
