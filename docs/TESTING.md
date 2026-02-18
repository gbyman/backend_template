# 테스트 작성 가이드

이 프로젝트의 테스트 인프라 구성과 테스트 코드 작성 방법을 설명합니다.

## 목차
- [테스트 구조](#테스트-구조)
- [빠른 시작](#빠른-시작)
- [테스트 프로파일](#테스트-프로파일)
- [Base Test 클래스](#base-test-클래스)
- [Controller 슬라이스 테스트](#controller-슬라이스-테스트)
- [Repository 슬라이스 테스트](#repository-슬라이스-테스트)
- [Service 단위 테스트](#service-단위-테스트)
- [통합 테스트](#통합-테스트)
- [테스트 데이터 팩토리](#테스트-데이터-팩토리)
- [실행 방법](#실행-방법)

---

## 테스트 구조

```
module-api/src/test/
├── java/app/backend/
│   ├── support/                          # 테스트 인프라
│   │   ├── BaseIntegrationTest.java      # @SpringBootTest 통합 테스트
│   │   ├── BaseRepositoryTest.java       # @DataJpaTest Repository 테스트
│   │   ├── BaseControllerTest.java       # @WebMvcTest Controller 테스트
│   │   ├── TestSecurityConfig.java       # Security 비활성화 설정
│   │   └── TestFixtures.java             # 테스트 데이터 팩토리
│   └── app/{도메인}/
│       ├── service/{Service}Test.java    # Mockito 단위 테스트
│       ├── repository/{Repo}Test.java    # H2 슬라이스 테스트
│       └── controller/{Ctrl}Test.java    # MockMvc 슬라이스 테스트
└── resources/
    ├── application-test.yml              # 테스트 전용 설정
    └── logback-test.xml                  # 테스트 전용 로깅
```

### 테스트 유형별 비교

| 유형 | 어노테이션 | Base 클래스 | 컨텍스트 | 속도 | 용도 |
|------|-----------|------------|---------|------|------|
| 단위 | `@ExtendWith(MockitoExtension.class)` | 없음 | 없음 | 빠름 | Service 비즈니스 로직 |
| Controller | `@WebMvcTest` | `BaseControllerTest` | MVC만 | 보통 | 요청/응답 검증 |
| Repository | `@DataJpaTest` | `BaseRepositoryTest` | JPA만 | 보통 | 쿼리/영속성 검증 |
| 통합 | `@SpringBootTest` | `BaseIntegrationTest` | 전체 | 느림 | 전체 흐름 검증 |

---

## 빠른 시작

### 1. Service 단위 테스트 (가장 기본)

```java
@DisplayName("MyService 테스트")
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @InjectMocks private MyServiceImpl myService;
    @Mock private MyRepository myRepository;

    @Test
    @DisplayName("정상 조회")
    void getById() {
        given(myRepository.findById(1L)).willReturn(Optional.of(new MyEntity()));

        MyRespDto result = myService.getById(1L);

        assertThat(result).isNotNull();
    }
}
```

### 2. Controller 슬라이스 테스트

```java
@DisplayName("MyController 테스트")
@WebMvcTest(
        value = MyController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        },
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.backend\\.core\\.(config|jwt|ratelimit|security).*"))
class MyControllerTest extends BaseControllerTest {

    @MockitoBean private MyService myService;

    @Test
    void getById() throws Exception {
        given(myService.getById(1L)).willReturn(new MyRespDto());

        mockMvc.perform(get("/api/v1/my/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200));
    }
}
```

### 3. Repository 슬라이스 테스트

```java
@DisplayName("MyRepository 테스트")
class MyRepositoryTest extends BaseRepositoryTest {

    @Autowired private MyRepository myRepository;

    @Test
    void save() {
        MyEntity entity = MyEntity.builder().name("test").build();
        entityManager.persist(entity);
        flushAndClear();

        assertThat(myRepository.findById(entity.getId())).isPresent();
    }
}
```

---

## 테스트 프로파일

### application-test.yml

테스트 전용 설정 파일입니다. `@ActiveProfiles("test")`로 활성화됩니다.

**주요 설정:**

| 항목 | 값 | 이유 |
|------|---|------|
| `datasource` | H2 (MODE=PostgreSQL) | PostgreSQL 호환 인메모리 DB |
| `jpa.hibernate.ddl-auto` | `create-drop` | 테스트마다 스키마 재생성 |
| `flyway.enabled` | `false` | H2에서 Flyway 마이그레이션 비호환 방지 |
| `jwt.use-redis` | `false` | Redis 의존성 제거 |
| `decorator.datasource.p6spy.enable-logging` | `false` | P6Spy 비활성화로 로그 간소화 |
| `app.rate-limit.enabled` | `false` | Rate Limit 비활성화 |
| `security.public-uris` | Swagger, health 경로 | SecurityConfig `@Value` 주입에 필요 |

### logback-test.xml

프로덕션 `logback-spring.xml`은 Elasticsearch Appender를 포함하여
테스트 환경에서 파싱 에러가 발생합니다. `logback-test.xml`은 classpath에서
`logback-spring.xml`보다 우선 로드되어 콘솔 전용 로깅만 사용합니다.

---

## Base Test 클래스

### BaseControllerTest

```java
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)   // Security 필터 체인 비활성화
public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    /** 객체 → JSON 문자열 변환 헬퍼 */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
```

- `@AutoConfigureMockMvc(addFilters = false)` — 서블릿 필터 체인을 건너뛰어 JWT 인증 없이 테스트
- 서브클래스에서 `@WebMvcTest`와 제외 설정을 직접 지정

### BaseRepositoryTest

```java
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)  // H2 자동 교체 대신 application-test.yml 사용
public abstract class BaseRepositoryTest {

    @Autowired protected TestEntityManager entityManager;

    protected void flushAndClear() { ... }
}
```

- `replace = NONE` — `application-test.yml`의 H2(PostgreSQL 모드) 설정을 그대로 사용
- `TestEntityManager`로 테스트 데이터 직접 persist/flush 가능

### BaseIntegrationTest

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional                              // 테스트 후 자동 롤백
@AutoConfigureTestEntityManager
public abstract class BaseIntegrationTest {

    @Autowired protected TestEntityManager entityManager;

    protected void flushAndClear() { ... }
}
```

- 전체 Spring 컨텍스트를 로드하여 실제 동작과 유사한 환경에서 테스트
- `@Transactional`로 테스트 간 데이터 격리 보장

---

## Controller 슬라이스 테스트

### @WebMvcTest 제외 설정이 필요한 이유

`@WebMvcTest`는 지정한 Controller 외에도 `@Component`로 등록된
`Filter`, `HandlerInterceptor`, `WebMvcConfigurer` 등을 **컴포넌트 스캔**으로 로드합니다.

이 프로젝트에서는 아래 빈들이 로드되면 의존성 충돌이 발생합니다:

| 패키지 | 문제 |
|--------|------|
| `core.config` | `SecurityConfig` → `JwtAuthenticationFilter` 등 JWT 빈 의존 |
| `core.jwt` | `TokenProvider`, `JwtService` → Redis, `UserDetailsService` 의존 |
| `core.ratelimit` | `RateLimitConfig` ↔ `RateLimitInterceptor` 순환 참조 |
| `core.security` | `CustomUserDetailsService` → `UserRepository` 의존 |

### 필수 설정 (복사하여 사용)

```java
@WebMvcTest(
        value = MyController.class,
        // 1. Spring Security 자동 설정 제외
        //    포함되면 SecurityFilterChain이 활성화되어 모든 요청이 401 반환
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        },
        // 2. core 패키지의 문제 빈 컴포넌트 스캔 제외
        //    core.exception.GlobalExceptionHandler는 제외되지 않아
        //    BizRespVo 응답 포맷(resultCode, body)이 유지됨
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.backend\\.core\\.(config|jwt|ratelimit|security).*"))
class MyControllerTest extends BaseControllerTest {

    // Service를 Mock으로 주입 (Spring Boot 3.4+ 에서는 @MockitoBean 사용)
    @MockitoBean private MyService myService;
}
```

> **참고**: Spring Boot 3.4부터 `@MockBean`이 deprecated되었습니다.
> `org.springframework.test.context.bean.override.mockito.MockitoBean`을 사용하세요.

### 응답 검증 패턴

이 프로젝트의 Controller는 `AbstractController.makeResponse()`로
`BizRespVo` 포맷을 반환합니다:

```java
// 200 OK 응답
mockMvc.perform(get("/api/v1/my/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultCode").value(200))
        .andExpect(jsonPath("$.body.name").value("test"));

// 201 Created 응답 (POST)
mockMvc.perform(post("/api/v1/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(reqDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultCode").value(201));

// 유효성 실패 (400 Bad Request)
mockMvc.perform(post("/api/v1/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidDto)))
        .andExpect(status().isBadRequest());
```

---

## Repository 슬라이스 테스트

### 작성 방법

```java
@DisplayName("MyRepository 테스트")
class MyRepositoryTest extends BaseRepositoryTest {

    @Autowired private MyRepository myRepository;

    @Test
    @DisplayName("Cascade 저장 검증")
    void saveWithChildren() {
        // given - TestEntityManager로 직접 persist
        MyEntity entity = MyEntity.builder().name("test").build();
        entity.addChild(ChildEntity.builder().value("child1").build());

        // when
        myRepository.save(entity);
        flushAndClear();   // 영속성 컨텍스트 비우고 DB에서 다시 조회

        // then
        MyEntity found = entityManager.find(MyEntity.class, entity.getId());
        assertThat(found.getChildren()).hasSize(1);
    }
}
```

### 주의사항

- `flushAndClear()`를 호출해야 영속성 컨텍스트 캐시가 아닌 실제 DB에서 조회
- H2는 PostgreSQL 호환 모드(`MODE=PostgreSQL`)이지만 일부 함수/문법은 다를 수 있음
- `@DataJpaTest`는 JPA 관련 빈만 로드하므로 Service, Controller는 포함되지 않음

---

## Service 단위 테스트

### 작성 방법

```java
@DisplayName("MyService 테스트")
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @InjectMocks private MyServiceImpl myService;
    @Mock private MyRepository myRepository;
    @Mock private MyMapStruct myMapStruct;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(myRepository.findById(1L)).willReturn(Optional.of(new MyEntity()));
            given(myMapStruct.toDto(any())).willReturn(new MyRespDto());

            // when
            MyRespDto result = myService.getById(1L);

            // then
            assertThat(result).isNotNull();
            then(myRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않으면 BizException")
        void notFound() {
            given(myRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> myService.getById(1L))
                    .isInstanceOf(BizException.class);
        }
    }
}
```

### 권장 패턴

- **BDD 스타일**: `given()` / `when` / `then()` (Mockito BDDMockito)
- **@Nested 클래스**: 메서드별 테스트 그룹화
- **@DisplayName**: 한국어로 시나리오 설명
- **assertThatThrownBy**: 예외 검증에 AssertJ 사용
- **then().should()**: 메서드 호출 횟수 검증

---

## 통합 테스트

전체 Spring 컨텍스트가 필요한 경우에만 사용합니다.

```java
@DisplayName("다국어 통합 테스트")
class MlgIntegrationTest extends BaseIntegrationTest {

    @Autowired private MlgGroupService mlgGroupService;

    @Test
    @DisplayName("그룹 생성 → 조회 → 번들 조회 전체 흐름")
    void fullFlow() {
        // given
        MlgGroupReqDto.Create reqDto = TestFixtures.createMlgGroupReqCreate();

        // when
        MlgGroupRespDto created = mlgGroupService.createGroup(reqDto);
        Map<String, String> bundle = mlgGroupService.getBundle("ko");

        // then
        assertThat(created.getMlgCodeVal()).startsWith("MLG");
        assertThat(bundle).containsKey(created.getMlgCodeVal());
    }
}
```

---

## 테스트 데이터 팩토리

`TestFixtures`는 도메인별 테스트 데이터를 생성하는 정적 팩토리 클래스입니다.

### 사용 예시

```java
// Entity 생성 (상세 포함)
MlgGroupEntity group = TestFixtures.createMlgGroup("MLG0000001");

// Entity 생성 (상세 없이)
MlgGroupEntity groupOnly = TestFixtures.createMlgGroupOnly("MLG0000001");

// 요청 DTO 생성
MlgGroupReqDto.Create createDto = TestFixtures.createMlgGroupReqCreate();
MlgGroupReqDto.Update updateDto = TestFixtures.createMlgGroupReqUpdate();

// 응답 DTO 생성 (Mock 반환값으로 사용)
MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000001");
```

### 새 도메인 추가 시

```java
public final class TestFixtures {

    // ... 기존 MLG 메서드 ...

    /** 새 도메인 Entity 생성 */
    public static NewEntity createNewEntity(Long id) {
        return NewEntity.builder()
                .id(id)
                .name("테스트")
                .build();
    }
}
```

---

## 실행 방법

```bash
# 전체 테스트
./gradlew :module-api:test

# 특정 도메인 테스트만
./gradlew :module-api:test --tests "app.backend.app.mlg.**"

# 특정 클래스만
./gradlew :module-api:test --tests "app.backend.app.mlg.group.service.MlgGroupServiceTest"

# 테스트 + 코드 품질 검사 (checkstyle, spotbugs 포함)
./gradlew clean build

# 테스트 리포트 확인
# module-api/build/reports/tests/test/index.html
```

---

## 관련 문서

- [코드 스타일 & 품질 가이드](./CODE_STYLE.md)
- [프로젝트 README](../README.md)
