# Module Batch

Spring Batch + Quartz 기반 배치 모듈 (서버 이중화 환경 지원)

## 🎯 주요 기능

- **Spring Batch** - 대용량 데이터 처리
- **Quartz 스케줄러** - Cron 기반 작업 스케줄링
- **클러스터링 지원** - 서버 이중화 환경에서 중복 실행 방지
- **ShedLock** - 분산 락을 통한 이중 보호
- **동적 Job 관리** - YAML 파일로 Job 추가/활성화/비활성화

---

## 🔥 서버 이중화 환경 대응 (핵심!)

### 문제: 서버가 2대 이상일 때 배치 Job이 중복 실행됨

### 해결 방법 1: **Quartz 클러스터링** (기본 방식)

**동작 원리:**
```
Server A, Server B → 같은 DB의 Quartz 테이블 공유
→ Quartz가 DB 락을 통해 한 서버에서만 실행
→ 서버 A 장애 시 → Server B가 자동으로 인계받음
```

**설정 (application.yml):**
```yaml
spring:
  quartz:
    job-store-type: jdbc  # 메모리 대신 DB 사용!
    properties:
      org.quartz.jobStore.isClustered: true  # 클러스터 모드 ON
      org.quartz.scheduler.instanceId: AUTO  # 각 서버 고유 ID
      org.quartz.jobStore.clusterCheckinInterval: 10000  # 10초마다 헬스체크
```

**필요한 테이블:**
- `QRTZ_*` 테이블 (Flyway로 자동 생성됨)

---

### 해결 방법 2: **ShedLock** (추가 보험)

Quartz 외의 스케줄러(`@Scheduled` 등)도 보호

**동작 원리:**
```
@SchedulerLock(name = "myJob")
public void myJob() {
    // 1. DB shedlock 테이블에 락 획득 시도
    // 2. 락 획득 성공 시만 실행
    // 3. 다른 서버는 락이 해제될 때까지 스킵
}
```

**설정:**
```java
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")  // 최대 10분 락
```

**필요한 테이블:**
- `shedlock` 테이블 (Flyway로 자동 생성됨)

---

## 📦 구조

```
module-batch/
├── core/
│   ├── base/
│   │   └── AbstractJobConfig.java         # 모든 Job의 기본 클래스
│   ├── batch/
│   │   └── BatchJobRunner.java            # Job 실행기
│   ├── config/
│   │   ├── QuartzConfig.java              # Quartz 클러스터링 설정
│   │   └── ShedLockConfig.java            # ShedLock 분산 락 설정
│   └── property/
│       └── QuartzJobProperties.java       # schedule.yml 매핑
├── jobs/
│   └── cleanup/
│       └── MailHistoryCleanupJobConfig.java  # 샘플: 메일 이력 정리 (Tasklet + JDBC)
├── quartz/
│   ├── registrar/
│   │   └── QuartzBatchJobRegistrar.java   # Job 동적 등록
│   └── executor/
│       └── QuartzBatchJobExecutor.java    # Job 실행
└── resources/
    ├── application.yml                     # Quartz 클러스터링 설정
    ├── resources-local/
    │   ├── application-local.yml
    │   └── schedule.yml                    # Job 스케줄 정의
    └── db/migration/
        ├── V1__create_quartz_tables.sql    # Quartz 테이블
        └── V2__create_shedlock_table.sql   # ShedLock 테이블
```

---

## 🚀 빠른 시작

### 1. 데이터베이스 준비

```bash
# Flyway가 자동으로 테이블 생성
./gradlew :module-batch:flywayMigrate
```

**생성되는 테이블:**
- Quartz: `QRTZ_*` (11개 테이블)
- ShedLock: `shedlock` (1개 테이블)
- Spring Batch: `BATCH_*` (기본 제공)

### 2. Job 작성

> **참고**: 실제 동작하는 샘플 Job은 `jobs/cleanup/MailHistoryCleanupJobConfig.java`를 참고하세요.
> Tasklet + JDBC 패턴으로 90일 초과 메일 이력을 삭제하며, module-api 의존 없이 독립 실행됩니다.

```java
@Configuration
public class MyJob extends AbstractJobConfig {
    public static final String JOB_NAME = "myJob";

    public MyJob(JobRepository jobRepository,
                 PlatformTransactionManager transactionManager) {
        super(jobRepository, transactionManager);
    }

    @Bean
    public Job myJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    @JobScope
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello Batch!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
```

### 3. 스케줄 등록 (schedule.yml)

```yaml
spring:
  quartz:
    jobs:
      - name: myJob          # Job Bean 이름과 일치
        description: "내 작업"
        cron: "0 */10 * * * ?"  # 10분마다
        registered: true      # true로 설정하여 활성화
        params:
          key: value
```

### 4. 실행

```bash
./gradlew :module-batch:bootRun
```

---

## 🔧 설정

### Quartz 클러스터링 설정

**application.yml:**
```yaml
spring:
  quartz:
    job-store-type: jdbc
    properties:
      org.quartz.jobStore.isClustered: true
      org.quartz.scheduler.instanceId: AUTO
```

**동작 확인:**
```sql
-- Quartz 스케줄러 상태 확인
SELECT * FROM QRTZ_SCHEDULER_STATE;

-- instanceName: 각 서버의 고유 식별자
-- LAST_CHECKIN_TIME: 마지막 헬스체크 시각
```

### ShedLock 사용 예시

```java
@Scheduled(cron = "0 0 * * * ?")
@SchedulerLock(
    name = "hourlyJob",
    lockAtMostFor = "PT59M",    // 최대 59분 락
    lockAtLeastFor = "PT1M"     // 최소 1분 락
)
public void hourlyJob() {
    // 여러 서버 중 한 서버에서만 실행됨
}
```

---

## ⚙️ Job 파라미터

### Map으로 전달
```yaml
params:
  key1: value1
  key2: 123
```

### DTO로 전달
```java
public class JobParamDto {
    private String key1;
    private Integer key2;
}

batchJobRunner.run("myJob", new JobParamDto("value1", 123));
```

---

## 📊 모니터링

### 1. Quartz 실행 이력

```sql
-- 실행 중인 Job
SELECT * FROM QRTZ_FIRED_TRIGGERS;

-- 등록된 Job 목록
SELECT JOB_NAME, JOB_GROUP FROM QRTZ_JOB_DETAILS;

-- Cron 스케줄
SELECT TRIGGER_NAME, CRON_EXPRESSION FROM QRTZ_CRON_TRIGGERS;
```

### 2. ShedLock 상태

```sql
-- 현재 락 상태
SELECT name, locked_by, lock_until
FROM shedlock
WHERE lock_until > NOW();
```

### 3. Spring Batch 실행 이력

```sql
-- 실행 이력
SELECT * FROM BATCH_JOB_EXECUTION ORDER BY START_TIME DESC LIMIT 10;

-- 실패한 Job
SELECT * FROM BATCH_JOB_EXECUTION WHERE STATUS = 'FAILED';
```

---

## 🛠️ 트러블슈팅

### Q1. Job이 중복 실행돼요 (서버 2대)

**확인:**
```yaml
spring:
  quartz:
    job-store-type: jdbc  # memory가 아닌 jdbc인지 확인!
    properties:
      org.quartz.jobStore.isClustered: true  # true인지 확인!
```

### Q2. Quartz 테이블이 없어요

```bash
# Flyway 마이그레이션 실행
./gradlew :module-batch:flywayMigrate
```

### Q3. 서버 A 장애 시 Job이 실행 안 돼요

**자동 인계 대기 시간:**
```yaml
org.quartz.jobStore.clusterCheckinInterval: 10000  # 10초
```
→ 서버 A 장애 후 최대 10초 대기하면 서버 B가 인계받음

### Q4. Job이 늦게 실행돼요 (Misfire)

```yaml
org.quartz.jobStore.misfireThreshold: 60000  # 1분
```
→ 1분 이상 늦으면 Misfire 처리 (다음 스케줄까지 스킵)

---

## 📚 참고

- [Spring Batch 공식 문서](https://spring.io/projects/spring-batch)
- [Quartz Scheduler](http://www.quartz-scheduler.org/)
- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock)

---

**Happy Batching! 🚀**
