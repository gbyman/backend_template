# Backend Template

Spring Boot 기반의 백엔드 템플릿 프로젝트입니다. JPA와 MyBatis를 동시에 사용할 수 있으며, 로컬 Docker 환경과 개발/운영 서버 환경을 분리하여 관리합니다.

## 📑 목차

- [주요 특징](#-주요-특징)
- [기술 스택](#-기술-스택)
- [빠른 시작](#-빠른-시작)
- [프로젝트 구조](#-프로젝트-구조)
- [환경별 실행 방법](#-환경별-실행-방법)
- [Makefile 명령어](#️-makefile-명령어)
- [코드 품질 검사](#-코드-품질-검사)
- [데이터베이스 마이그레이션](#️-데이터베이스-마이그레이션-flyway)
- [API 사용 예제](#-api-사용-예제)
- [JPA vs MyBatis 사용 가이드](#-jpa-vs-mybatis-사용-가이드)
- [컨트롤러 응답 패턴](#-컨트롤러-응답-패턴-abstractcontroller)
- [환경별 설정 파일](#-환경별-설정-파일)
- [도메인 추가 방법](#-도메인-추가-방법)
- [검증 기능 (Bean Validation)](#-검증-기능-bean-validation)
- [문서 및 가이드](#-문서-및-가이드)
- [현재 진행 중인 작업](#-현재-진행-중인-작업)
- [주의사항](#️-주의사항)
- [트러블슈팅](#-트러블슈팅)

## 🎯 주요 특징

- **멀티 환경 지원**: 로컬(Docker) / 개발 / 운영 환경 완벽 분리
- **JPA + MyBatis 통합**: 상황에 맞는 최적의 데이터 접근 방식 선택
- **Flyway 마이그레이션**: 데이터베이스 스키마 버전 관리
- **Profile 기반 설정**: 환경별 독립적인 설정 관리
- **Docker 기반 로컬 개발**: 개발자별 독립적인 데이터베이스

## 📦 기술 스택

- **Java**: 21
- **Spring Boot**: 3.4.4
- **Database**: PostgreSQL
- **ORM**: JPA (Hibernate) + MyBatis 3.0.4
- **QueryDSL**: 타입 안전한 쿼리 작성
- **Mapper**: MapStruct 1.6.3
- **Migration**: Flyway
- **Security**: Spring Security + JWT + Jasypt (설정 암호화)
- **Cache**: Redis (세션 관리 포함)
- **Monitoring**: Actuator + Prometheus (메트릭 노출)
- **Documentation**: Swagger/OpenAPI
- **Logging**: Logstash Encoder (ELK Stack 연동)
- **Code Quality**: Checkstyle, PMD, SpotBugs, Spotless, Jacoco
- **Build Tool**: Gradle 8.x


## 🚀 빠른 시작

### 사전 요구사항

- JDK 21
- Docker & Docker Compose
- Make (선택, 없어도 무방)

### 1. 환경 체크

```bash
# 개발 환경 확인
./scripts/check-env.sh
# 또는
make help
```

### 2. 로컬 환경 설정

```bash
# 환경 변수 파일 생성 (선택)
cp .env.example .env

# Docker 컨테이너 시작 (PostgreSQL + Redis + pgAdmin)
make start
# 또는
docker-compose up -d
```

**실행 후 서비스 URL:**
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- pgAdmin: `http://localhost:5050` (admin@example.com / admin)

### 3. 애플리케이션 실행

```bash
# 로컬 프로파일로 실행
make run
# 또는
./gradlew :module-api:bootRun
```

### 4. 동작 확인 및 테스트

#### 접속 확인
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/api/health
- **환경 정보**: http://localhost:8080/api/env
- **pgAdmin**: http://localhost:5050 (admin@example.com / admin)

#### API 테스트 (Swagger UI)
1. 브라우저에서 http://localhost:8080/swagger-ui.html 접속
2. `sample-controller` 섹션 펼치기
3. `POST /api/v1/samples` - 샘플 데이터 생성 테스트
   - Try it out 클릭
   - Request body 입력:
     ```json
     {
       "title": "로컬 테스트",
       "content": "Docker 환경 테스트"
     }
     ```
   - Execute 클릭
4. `GET /api/v1/samples` - JPA로 목록 조회 테스트
5. `GET /api/v1/samples/mybatis` - MyBatis로 목록 조회 테스트

#### 데이터베이스 확인 (pgAdmin)
1. 브라우저에서 http://localhost:5050 접속
2. 로그인: `admin@example.com` / `admin`
3. 서버 추가:
   - Name: `Local Template DB`
   - Host: `postgres` (Docker 네트워크 내부 호스트명)
   - Port: `5432`
   - Database: `template_db`
   - Username: `postgres`
   - Password: `postgres`
4. 테이블 확인: `template_db` → `Schemas` → `public` → `Tables` → `TB_SAMPLE`
5. 데이터 조회: 테이블 우클릭 → `View/Edit Data` → `All Rows`

#### Health Check 테스트
```bash
# 전체 헬스 체크
curl http://localhost:8080/api/health

# 응답 예시 (로컬 환경에서는 상세 정보 포함)
# {
#   "status": "UP",
#   "profile": "local",
#   "timestamp": "2024-02-13T21:30:00",
#   "details": {
#     "database": {"status": "UP", "database": "PostgreSQL"},
#     "redis": {"status": "UP", "response": "PONG"}
#   }
# }

# 환경 정보 확인
curl http://localhost:8080/api/env
```

#### Flyway 마이그레이션 확인
```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 예상 출력:
# +-----------+---------+---------------------------+------+---------------------+
# | Category  | Version | Description               | Type | Installed On        |
# +-----------+---------+---------------------------+------+---------------------+
# | Versioned | 1       | init schema               | SQL  | 2024-02-13 21:00:00 |
# | Versioned | 2       | insert sample data        | SQL  | 2024-02-13 21:00:00 |
# | Repeatable|         | sample stored procedure   | SQL  | 2024-02-13 21:00:00 |
# +-----------+---------+---------------------------+------+---------------------+
```

## 📋 프로젝트 구조

```
backend_template/
├── docker-compose.yml              # Docker 설정
├── Makefile                        # 편의 명령어
├── .env.example                    # 환경 변수 템플릿
│
├── config/                         # 코드 품질 도구 설정
│   ├── checkstyle/                # Checkstyle 규칙
│   ├── pmd/                       # PMD 규칙
│   └── spotbugs/                  # SpotBugs 제외 규칙
│
├── docs/                           # 📚 프로젝트 문서
│   ├── README.md                  # 문서 목차
│   ├── BUILD_GRADLE_REVIEW.md     # 빌드 설정 검토
│   ├── CODE_STYLE.md              # 코드 스타일 가이드
│   ├── TESTING.md                 # 테스트 가이드
│   ├── JASYPT_USAGE.md            # 암호화 가이드
│   ├── LOGGING_MASKING.md         # 로그 마스킹
│   ├── SESSION_CONTROL.md         # 세션 관리
│   ├── CICD_SECRETS.md            # CI/CD 시크릿
│   ├── NEXUS_SETUP.md             # Nexus 설정
│   ├── ELK_SETUP.md               # ELK Stack 설정
│   ├── COMMON_TABLE_MIGRATION_PLAN.md  # 공통 테이블 전환 계획
│   ├── IMPROVEMENT_ROADMAP.md     # 개선 로드맵
│   └── db/                        # DB 문서
│       ├── TABLE_NAMING_CONVENTION.md  # 테이블 명명 규칙
│       └── REFERENCE_TABLES.md         # 참고 테이블 목차
│
├── docker/
│   └── init/                       # DB 초기화 스크립트
│       ├── 01-init.sql
│       └── 02-create-extensions.sql
│
├── scripts/                        # 유틸리티 스크립트
│   ├── check-env.sh               # 환경 체크
│   └── sync-dev-data.sh           # 개발 서버 데이터 동기화
│
├── module-common/                  # 공통 모듈
│   └── src/main/java/app/backend/
│       ├── common/
│       │   └── http/              # HTTP 클라이언트
│       └── core/
│           ├── annotation/        # 커스텀 검증 어노테이션 (8개)
│           ├── validator/         # Validator 구현체
│           ├── property/          # Configuration Properties
│           ├── base/
│           │   ├── component/     # AbstractController
│           │   ├── exception/     # BizException
│           │   └── vo/            # 공통 VO
│           ├── constants/         # 상수 (MessageConstants 등)
│           ├── interceptor/       # Interceptor
│           ├── utils/             # 유틸리티 (통합)
│           └── swagger/           # Swagger 설정
│
└── module-api/                     # API 모듈
    ├── build.gradle
    └── src/main/
        ├── java/app/backend/
        │   ├── TemplateApplication.java
        │   ├── app/
        │   │   └── sample/        # Sample 도메인
        │   │       ├── controller/
        │   │       ├── service/
        │   │       ├── repository/    # JPA
        │   │       ├── mapper/        # MyBatis
        │   │       ├── mapstruct/     # MapStruct
        │   │       ├── entity/
        │   │       └── dto/
        │   ├── core/
        │   │   ├── base/
        │   │   ├── config/
        │   │   │   ├── SecurityConfig.java    # CORS/CSRF 설정
        │   │   │   ├── RedisConfig.java       # Redis 설정
        │   │   │   └── actuator/              # 헬스체크
        │   │   ├── exception/                 # GlobalExceptionHandler
        │   │   ├── security/                  # Spring Security
        │   │   ├── jwt/                       # JWT 인증/인가
        │   │   │   ├── JwtAuthenticationFilter.java
        │   │   │   ├── JwtExceptionHandlerFilter.java
        │   │   │   └── JwtService.java
        │   │   └── log/                       # 로깅 설정
        │   └── infra/                         # 인프라 계층
        │       ├── file/                      # 파일 저장소
        │       │   ├── local/                 # 로컬 저장소
        │       │   ├── s3/                    # AWS S3
        │       │   └── azure/                 # Azure Blob
        │       └── mail/                      # 이메일
        └── resources/
            ├── application.yml                # 공통 설정
            ├── application-local.yml          # 로컬 환경
            ├── application-dev.yml            # 개발 서버
            ├── application-prod.yml           # 운영 서버
            ├── db/migration/                  # Flyway 마이그레이션
            │   ├── V1__init_schema.sql
            │   ├── V2__insert_sample_data.sql
            │   └── R__sample_stored_procedure.sql
            └── mybatis/mapper/                # MyBatis Mapper XML
                └── SampleMapper.xml

└── module-batch/                   # Batch 모듈
    ├── build.gradle
    └── src/main/
        ├── java/app/backend/batch/
        │   ├── BatchApplication.java
        │   ├── core/
        │   │   ├── config/
        │   │   │   ├── QuartzConfig.java      # Quartz 스케줄러 설정
        │   │   │   └── ShedLockConfig.java    # 분산 락 설정
        │   │   ├── base/                      # AbstractJobConfig
        │   │   ├── batch/                     # BatchJobRunner
        │   │   ├── listener/                  # QuartzJobListener
        │   │   ├── property/                  # QuartzJobProperties
        │   │   └── utils/                     # BatchJobProvider
        │   ├── jobs/                          # Batch Job 구현
        │   │   └── cleanup/                   # 정리 작업
        │   │       └── MailHistoryCleanupJobConfig.java
        │   └── quartz/                        # Quartz 통합
        │       ├── executor/                  # QuartzBatchJobExecutor
        │       └── registrar/                 # QuartzBatchJobRegistrar
        └── resources/
            ├── application.yml                # Batch 설정
            ├── schedule.yml                   # 스케줄 정의 (YAML)
            └── db/migration/                  # Flyway 마이그레이션
                ├── V1__create_quartz_tables.sql
                └── V2__create_shedlock_table.sql
```

## 🌍 환경별 실행 방법

### 로컬 개발 (Docker)

```bash
# 1. Docker 시작
make start

# 2. 애플리케이션 실행 (로컬 프로파일)
make run
# 또는
./gradlew :module-api:bootRun

# 통합 실행 (Docker 시작 + 앱 실행)
make dev
```

**로컬 환경 특징:**
- ✅ 자동 스키마 업데이트 (`ddl-auto: update`)
- ✅ SQL 쿼리 로깅 활성화
- ✅ Swagger UI 활성화
- ✅ 스택트레이스 포함
- ✅ 모든 Actuator 엔드포인트 노출

### 개발 서버

```bash
# 빌드
./gradlew clean build -Pprofile=dev

# 실행 (환경변수 포함)
java -jar \
  -Dspring.profiles.active=dev \
  -DDB_URL=jdbc:postgresql://dev-db:5432/template_dev_db \
  -DDB_USERNAME=dev_user \
  -DDB_PASSWORD=dev_password \
  module-api/build/libs/module-api-0.0.1-SNAPSHOT.jar
```

**개발 환경 특징:**
- ⚠️ 스키마 검증만 (`ddl-auto: validate`)
- ✅ Flyway 마이그레이션 활성화
- ✅ Swagger UI 활성화
- ⚠️ SQL 로깅 최소화
- ⚠️ 선택적 Actuator 노출

### 운영 서버

```bash
# 빌드
./gradlew clean build -Pprofile=prod

# 실행 (환경변수 필수)
java -jar \
  -Dspring.profiles.active=prod \
  -DDB_URL=${DB_URL} \
  -DDB_USERNAME=${DB_USERNAME} \
  -DDB_PASSWORD=${DB_PASSWORD} \
  module-api/build/libs/module-api-0.0.1-SNAPSHOT.jar
```

**운영 환경 특징:**
- ❌ 스키마 변경 금지 (`ddl-auto: none`)
- ❌ Swagger UI 비활성화
- ❌ SQL 로깅 비활성화
- ❌ 최소한의 Actuator만 노출 (health만)
- ✅ 로그 파일 저장 (90일 보관)

## 🛠️ Makefile 명령어

```bash
# Docker 관리
make start          # Docker 컨테이너 시작
make stop           # Docker 컨테이너 중지
make restart        # Docker 컨테이너 재시작
make logs           # Docker 로그 확인

# 데이터베이스 관리
make db-reset       # DB 초기화 (주의: 데이터 삭제)
make db-backup      # DB 백업
make db-restore     # DB 복원
make db-connect     # DB 접속 (psql)

# 빌드 & 실행
make clean          # 빌드 파일 정리
make build          # 프로젝트 빌드
make run            # 애플리케이션 실행 (로컬)
make run-dev        # 애플리케이션 실행 (개발)
make test           # 테스트 실행

# 통합 명령
make dev            # Docker 시작 + 앱 실행
make all            # 전체 빌드 및 실행
```

## 🔍 코드 품질 검사

```bash
# 코드 포맷팅
./gradlew spotlessApply          # 코드 자동 포맷팅

# 개별 검사
./gradlew checkstyleMain         # Checkstyle 검사 (메인 코드)
./gradlew pmdMain                # PMD 검사 (코드 품질)
./gradlew spotbugsMain           # SpotBugs 검사 (버그 탐지)
./gradlew spotlessCheck          # 포맷팅 검증

# 통합 검사
./gradlew codeQuality            # 모든 코드 품질 검사 실행
./gradlew formatAndCheck         # 포맷팅 + 검증

# 테스트 커버리지
./gradlew test jacocoTestReport  # 테스트 실행 + 커버리지 리포트
./gradlew jacocoTestCoverageVerification  # 커버리지 검증 (최소 70%)

# 빌드 시 자동 실행
./gradlew clean build            # build 실행 시 codeQuality 자동 실행
```

## 🗄️ 데이터베이스 마이그레이션 (Flyway)

### 마이그레이션 파일 작성

```bash
# 새 마이그레이션 파일 생성
# resources/db/migration/V{버전}__{설명}.sql

# 예시:
# V1__init_schema.sql           # 초기 스키마
# V2__insert_sample_data.sql    # 샘플 데이터
# V3__add_user_table.sql        # 사용자 테이블 추가
```

### 규칙

- **V (Versioned)**: 버전 관리되는 마이그레이션 (한 번만 실행)
- **R (Repeatable)**: 반복 가능한 마이그레이션 (변경 시마다 실행)
- 파일명 형식: `V{버전}__{설명}.sql` (언더스코어 2개!)

### 마이그레이션 확인

```bash
# Flyway 정보 확인 (Gradle 플러그인 사용)
./gradlew flywayInfo

# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 검증
./gradlew flywayValidate
```

## 📊 API 사용 예제

### 샘플 생성 (JPA)

```bash
curl -X POST http://localhost:8080/api/v1/samples \
  -H "Content-Type: application/json" \
  -d '{
    "title": "테스트 제목",
    "content": "테스트 내용"
  }'
```

### 샘플 목록 조회 (JPA)

```bash
curl -X GET http://localhost:8080/api/v1/samples
```

### 샘플 목록 조회 (MyBatis)

```bash
curl -X GET http://localhost:8080/api/v1/samples/mybatis
```

### 환경 정보 확인

```bash
curl -X GET http://localhost:8080/api/env
```

## 🎨 JPA vs MyBatis 사용 가이드

### JPA 사용 (권장)

- ✅ 단순 CRUD
- ✅ 엔티티 관계가 명확한 경우
- ✅ 객체 지향적 접근
- ✅ 빠른 개발 속도

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public User createUser(UserDto dto) {
        User user = User.builder()
            .name(dto.getName())
            .build();
        return userRepository.save(user);
    }
}
```

### MyBatis 사용 (권장)

- ✅ 복잡한 조인 쿼리
- ✅ 통계 및 집계
- ✅ 동적 쿼리
- ✅ 성능 최적화가 중요한 조회

```java
@Mapper
public interface StatisticsMapper {
    List<DashboardStats> getDashboardStats(
        @Param("startDate") LocalDate startDate
    );
}
```

```xml
<!-- SampleMapper.xml -->
<select id="getDashboardStats" resultType="DashboardStats">
    SELECT
        DATE_TRUNC('day', created_date) as period,
        COUNT(*) as count
    FROM TB_SAMPLE
    WHERE created_date BETWEEN #{startDate} AND #{endDate}
    GROUP BY DATE_TRUNC('day', created_date)
</select>
```

## 🎯 컨트롤러 응답 패턴 (AbstractController)

모든 컨트롤러는 `AbstractController`를 상속받아 일관된 응답 포맷을 사용합니다.

### AbstractController 메서드

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends AbstractController {

    private final UserService userService;

    // 1. 기본 성공 응답 (200 OK)
    @GetMapping("/{id}")
    public BizRespVo<UserDto> getUser(@PathVariable Long id) {
        return super.makeResponse(userService.getUser(id));
    }

    // 2. 메시지 포함 응답 (200 OK)
    @PutMapping("/{id}")
    public BizRespVo<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
        return super.makeResponse("수정되었습니다.", userService.updateUser(id, dto));
    }

    // 3. 리스트 응답 (200 OK)
    @GetMapping
    public BizRespVo<GenericListVo<UserDto>> getAllUsers() {
        return super.makeGenericListResponse(userService.getAllUsers());
    }

    // 4. 생성 성공 응답 (201 CREATED)
    @PostMapping
    public BizRespVo<UserDto> createUser(@RequestBody UserDto dto) {
        return super.makeCreatedResponse("생성되었습니다.", userService.createUser(dto));
    }

    // 5. 삭제 성공 응답 (200 OK, data=null)
    @DeleteMapping("/{id}")
    public BizRespVo<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return super.makeDeleteResponse("삭제되었습니다.");
    }

    // 6. 상태 코드 지정 응답
    @PostMapping("/custom")
    public BizRespVo<UserDto> customResponse(@RequestBody UserDto dto) {
        return super.makeResponse(HttpStatus.ACCEPTED, "처리 중입니다.", dto);
    }
}
```

### 제공되는 메서드

| 메서드 | 상태 코드 | 설명 |
|--------|----------|------|
| `makeResponse(T data)` | 200 OK | 기본 성공 응답 |
| `makeResponse(String message, T data)` | 200 OK | 메시지 포함 응답 |
| `makeGenericListResponse(List<T> list)` | 200 OK | 리스트 응답 |
| `makeCreatedResponse(T data)` | 201 CREATED | 생성 성공 |
| `makeCreatedResponse(String message, T data)` | 201 CREATED | 생성 성공 (메시지 포함) |
| `makeDeleteResponse(String message)` | 200 OK | 삭제 성공 (data=null) |
| `makeResponse(HttpStatus status, T data)` | 사용자 지정 | 상태 코드 지정 |
| `makeResponse(HttpStatus status, String message, T data)` | 사용자 지정 | 상태/메시지 모두 지정 |

### 장점

- ✅ **코드 간결성**: `BizRespVo.withStatus()` → `super.makeResponse()`
- ✅ **일관성**: 모든 컨트롤러가 동일한 응답 포맷 보장
- ✅ **유지보수성**: 응답 생성 로직이 한 곳에 집중
- ✅ **가독성**: 메서드 이름만으로 의도 파악 가능

## 🔧 환경별 설정 파일

### 공통 설정 (application.yml)

```yaml
spring:
  application:
    name: backend_template
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

### 로컬 환경 (application-local.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/template_db
  jpa:
    hibernate:
      ddl-auto: update  # 로컬은 자동 업데이트
```

### 개발 환경 (application-dev.yml)

```yaml
spring:
  datasource:
    url: ${DB_URL}  # 환경변수 사용
  jpa:
    hibernate:
      ddl-auto: validate  # 검증만
```

### 운영 환경 (application-prod.yml)

```yaml
spring:
  datasource:
    url: ${DB_URL}  # 환경변수 필수
  jpa:
    hibernate:
      ddl-auto: none  # 변경 금지
```

## 🆕 도메인 추가 방법

1. **패키지 구조 생성**
```
app/{domain}/
├── controller/
├── service/impl/
├── repository/      # JPA
├── mapper/          # MyBatis
├── mapstruct/       # MapStruct
├── entity/
└── dto/
```

2. **Entity 작성** (JPA)
```java
@Entity
@Table(name = "TB_USER")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

3. **Repository 작성** (JPA)
```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
```

4. **Controller 작성**
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends AbstractController {

    private final UserService userService;

    @GetMapping
    public BizRespVo<GenericListVo<UserDto>> getAllUsers() {
        return super.makeGenericListResponse(userService.getAllUsers());
    }

    @PostMapping
    public BizRespVo<UserDto> createUser(@RequestBody UserDto dto) {
        return super.makeCreatedResponse("생성되었습니다.", userService.createUser(dto));
    }
}
```

5. **Mapper 작성** (MyBatis, 선택)
```java
@Mapper
public interface UserMapper {
    List<UserDto> findAllUsers();
}
```

6. **Flyway 마이그레이션 작성**
```sql
-- V{next_version}__add_user_table.sql
CREATE TABLE TB_USER (
    USER_ID BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL
);
```

## ✅ 검증 기능 (Bean Validation)

프로젝트에는 보안 및 파일 검증을 위한 8가지 커스텀 어노테이션이 포함되어 있습니다:

### 파일 검증
- `@ValidFileExtension` - 파일 확장자 및 크기 검증 (yml 설정 기반)
- `@ValidImageFile` - 실제 이미지 파일 검증 (magic number 검사)
- `@ValidMimeType` - MIME 타입 검증
- `@NoMaliciousFile` - 악성 파일 확장자 차단

### 보안 검증
- `@NoXSS` - XSS 공격 패턴 차단
- `@NoSQLInjection` - SQL Injection 패턴 차단
- `@SafePath` - 경로 탐색 공격(Path Traversal) 방지
- `@ValidPassword` - 비밀번호 강도 검증

상세 사용법: [Validation Annotations 가이드](module-common/src/main/java/app/backend/core/annotation/README.md)

## 🔒 로그 마스킹 (Logback Masking)

로그 출력 시 민감 정보를 자동으로 마스킹 처리합니다.

### 자동 마스킹 항목

- **전화번호**: `010-1234-5678` → `010-****-5678`
- **이메일**: `user@example.com` → `u***@example.com`
- **비밀번호 (JSON)**: `"password":"secret123"` → `"password":"*****"`
- **비밀번호 (텍스트)**: `password=secret123` → `password=*****`

### 사용 예시

```java
// 개발자가 로그에 민감 정보 출력
log.info("사용자 정보: name={}, phone={}, email={}",
         user.getName(), user.getPhone(), user.getEmail());

// 실제 출력 (자동 마스킹)
// 사용자 정보: name=홍길동, phone=010-****-5678, email=h***@example.com
```

**개인정보보호법** 준수를 위해 필수적인 기능이며, 로그 파일에 민감 정보가 평문으로 저장되는 것을 방지합니다.

상세 가이드: [로그 마스킹 가이드](docs/LOGGING_MASKING.md)

## 🔐 세션 동시 접속 제한 (중복 로그인 방지)

JWT + Redis 기반으로 **하나의 계정으로 동시에 여러 곳에서 로그인하는 것을 방지**합니다.

### 기본 동작
- ✅ 마지막 로그인만 유효 (이전 세션 자동 만료)
- ✅ 특정 ID는 중복 로그인 허용 가능
- ✅ Redis 기반으로 분산 환경 지원

### 설정 예시

```yaml
# application.yml
jwt:
  use-redis: true  # Redis 기반 중복 로그인 방지 활성화

# 중복 로그인 허용 ID 목록 (관리자, 서비스 계정 등)
allow-duplicate:
  ids:
    - admin
    - service-account
```

### 작동 방식

```
1. 사용자 A가 PC에서 로그인
   → RefreshToken을 Redis에 저장 (userId: "userA" → token1)

2. 사용자 A가 모바일에서 로그인
   → RefreshToken을 Redis에 업데이트 (userId: "userA" → token2)

3. PC에서 API 요청
   → PC의 token1 ≠ Redis의 token2
   → 401 Unauthorized (세션 만료)
```

**계정 도용 및 계정 공유 방지**에 효과적이며, 감사 추적을 보장합니다.

상세 가이드: [세션 제어 가이드](docs/SESSION_CONTROL.md)

## 📚 문서 및 가이드

### 📖 전체 문서 목차
**모든 프로젝트 문서는 [docs/README.md](docs/README.md)에서 확인할 수 있습니다.**

### 주요 문서

#### 🏗️ 프로젝트 구조 및 설정
- [BUILD_GRADLE_REVIEW.md](docs/BUILD_GRADLE_REVIEW.md) - Gradle 빌드 설정 검토 및 개선 사항
- [CODE_STYLE.md](docs/CODE_STYLE.md) - Google Java Style Guide, 코드 품질 도구 설정
- [TESTING.md](docs/TESTING.md) - 테스트 전략 및 TestContainers 활용

#### 🔐 보안 및 설정
- [JASYPT_USAGE.md](docs/JASYPT_USAGE.md) - 설정 파일 암호화 (DB 비밀번호, API 키)
- [LOGGING_MASKING.md](docs/LOGGING_MASKING.md) - 로그 마스킹 (개인정보 보호)
- [SESSION_CONTROL.md](docs/SESSION_CONTROL.md) - Redis 기반 세션 관리 및 중복 로그인 제어
- [CICD_SECRETS.md](docs/CICD_SECRETS.md) - CI/CD 파이프라인 시크릿 관리

#### 📊 인프라 및 배포
- [NEXUS_SETUP.md](docs/NEXUS_SETUP.md) - Private Maven Repository 구성
- [ELK_SETUP.md](docs/ELK_SETUP.md) - Elasticsearch, Logstash, Kibana 설정

#### 📋 작업 계획 및 로드맵
- ⭐ [COMMON_TABLE_MIGRATION_PLAN.md](docs/COMMON_TABLE_MIGRATION_PLAN.md) - **공통 테이블 전환 작업 계획** (진행 중)
- [IMPROVEMENT_ROADMAP.md](docs/IMPROVEMENT_ROADMAP.md) - 단기/중기/장기 개선 계획

#### 🗄️ 데이터베이스
- [TABLE_NAMING_CONVENTION.md](docs/db/TABLE_NAMING_CONVENTION.md) - 테이블 명명 규칙 (Korean SI 표준)
- [REFERENCE_TABLES.md](docs/db/REFERENCE_TABLES.md) - 참고 시스템 테이블 목차

#### 💡 기타
- [Validation Annotations 가이드](module-common/src/main/java/app/backend/core/annotation/README.md) - 커스텀 검증 어노테이션 (8개)

### 🌐 외부 문서
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [MapStruct Documentation](https://mapstruct.org/)
- [QueryDSL Documentation](http://querydsl.com/)

## 🚧 현재 진행 중인 작업

현재 진행 중인 작업 및 TODO 목록은 [docs/TODO.md](docs/TODO.md)를 참고하세요.

---

## ⚠️ 주의사항

### 로컬 개발 시

- ✅ Docker 컨테이너는 `make start`로 시작
- ✅ 데이터 초기화가 필요하면 `make db-reset`
- ✅ 백업은 `make db-backup`으로 수시로 생성
- ⚠️ docker-compose down -v 하면 데이터 삭제됨

### 개발/운영 서버

- ❌ 절대 `ddl-auto`를 `update`나 `create`로 설정하지 마세요
- ✅ 모든 스키마 변경은 Flyway 마이그레이션으로 관리
- ✅ 환경변수로 민감 정보 관리 (.env 파일 사용)
- ✅ 배포 전 Flyway 마이그레이션 검증 필수


## 🐛 트러블슈팅

### Docker 컨테이너가 시작되지 않는 경우

```bash
# 기존 컨테이너 및 볼륨 삭제
docker-compose down -v

# 재시작
make start
```

### 포트 충돌

```bash
# 실행 중인 포트 확인
lsof -i :5432
lsof -i :6379

# 프로세스 종료 후 재시작
```

### Flyway 마이그레이션 실패

```bash
# Flyway 정보 확인
./gradlew flywayInfo

# Repair (주의해서 사용)
./gradlew flywayRepair
```


---
