# 코드 스타일 & 품질 가이드

> 이 문서는 프로젝트의 코드 작성 규칙, 컨벤션, 품질 도구 설정을 통합 정리한 가이드입니다.
> Checkstyle, Spotless, PMD, SpotBugs가 자동으로 검사하며, `./gradlew spotlessApply`로 자동 포맷팅할 수 있습니다.

---

## 1. 기본 설정

| 항목 | 규칙 |
|------|------|
| Java 버전 | 21 |
| 인코딩 | UTF-8 |
| 들여쓰기 | **4 spaces** (탭 금지) |
| 줄 길이 | **120자** (`package`, `import`, URL은 예외) |
| 파일 길이 | **500줄** 이하 |
| 줄 끝 공백 | 금지 |
| 줄 바꿈 | LF (`\n`) |
| 파일 끝 | 빈 줄 1개 |
| 포맷터 | Google Java Format (AOSP 4-space 스타일) |

---

## 2. 네이밍 컨벤션

### 2.1 패키지

- **소문자만** 사용 (`app.backend.core.utils`)
- 대문자, 언더스코어 금지

### 2.2 클래스 / 인터페이스

- **PascalCase**: `MlgGroupController`, `BizRespVo`

| 유형 | 접미사 | 예시 |
|------|--------|------|
| 컨트롤러 | `Controller` | `SampleController` |
| 서비스 인터페이스 | `Service` | `SampleService` |
| 서비스 구현체 | `ServiceImpl` | `SampleServiceImpl` |
| JPA Repository | `Repository` | `SampleRepository` |
| QueryDSL 인터페이스 | `QueryRepository` | `MlgQueryRepository` |
| QueryDSL 구현체 | `QueryRepositoryImpl` | `MlgQueryRepositoryImpl` |
| Entity | `Entity` | `SampleEntity` |
| 요청 DTO | `ReqDto` | `SampleReqDto` |
| 응답 DTO | `RespDto` | `SampleRespDto` |
| 검색 조건 | `SearchCond` | `MlgGroupSearchCond` |
| 페이징 응답 | `PagingRespDto` | `MlgPagingRespDto` |
| MapStruct | `MapStruct` | `SampleMapStruct` |
| 상수 | `Constants` | `MessageConstants` |
| Value Object | `Vo` | `BizRespVo` |

### 2.3 메서드 / 변수

- **camelCase**: `findByUserId`, `requestId`

### 2.4 상수

- **UPPER_SNAKE_CASE**: `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`

---

## 3. 프로젝트 구조

### 3.1 모듈

```
backend_template/
├── module-common/    # 공통 (Base, Utils, Config, Security, Exception)
├── module-api/       # REST API 서비스
└── module-batch/     # 배치 처리
```

### 3.2 패키지 구조 (module-api)

```
app.backend/
├── app/                         # 비즈니스 도메인
│   └── {domain}/                # 도메인 단위 패키지
│       ├── controller/
│       ├── service/
│       │   └── impl/
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       ├── mapstruct/
│       └── constants/
├── core/                        # 프레임워크 인프라
│   ├── base/                    # Base 클래스
│   ├── config/                  # 설정
│   ├── exception/               # 예외 처리
│   ├── jwt/                     # JWT 인증
│   ├── security/                # Spring Security
│   └── ...
└── infra/                       # 외부 인프라 (S3, Mail 등)
```

---

## 4. 레이어별 작성 규칙

### 4.1 Entity

```java
@Entity
@Table(name = "TB_SAMPLE")
@Comment("샘플")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("샘플 ID")
    private Long sampleId;

    @Comment("제목")
    private String title;

    @Comment("사용여부")
    @Builder.Default
    private boolean useYn = true;
}
```

- `BaseEntity`를 상속하여 공통 감사(audit) 필드 포함
- `@Comment`로 테이블/컬럼 설명 작성
- `@Builder.Default`로 기본값 설정
- 연관관계: `@ManyToOne(fetch = FetchType.LAZY)` 기본 사용

### 4.2 Request DTO

```java
@Getter
@Setter
public class SampleReqDto {

    @Getter
    @Setter
    public static class Create {
        @Schema(description = "제목")
        @NotBlank
        private String title;
    }

    @Getter
    @Setter
    public static class Update {
        @Schema(description = "제목")
        @NotBlank
        private String title;
    }
}
```

- **외부 클래스** 안에 `Create`, `Update` 등 **static inner class**로 구분
- `@Schema`로 Swagger 문서화
- `@NotBlank`, `@Size` 등 Bean Validation 적용

### 4.3 Response DTO

```java
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "샘플 응답")
public class SampleRespDto extends BaseRespDto {

    @Schema(description = "샘플 ID")
    private Long sampleId;

    @Schema(description = "제목")
    private String title;
}
```

- `BaseRespDto`를 상속하여 공통 감사 필드 포함
- `@SuperBuilder`로 빌더 패턴 지원

### 4.4 검색 조건 DTO

```java
@Getter
@Setter
@Schema(description = "샘플 검색 조건")
public class SampleSearchCond extends BizPageableDto {

    @Schema(description = "제목")
    private String title;

    @Schema(description = "사용여부")
    private Boolean useYn;
}
```

- `BizPageableDto`를 상속하여 페이징 파라미터 자동 포함

### 4.5 Service

```java
// 인터페이스
public interface SampleService {
    Page<SampleRespDto> paging(SampleSearchCond cond, Pageable pageable);
    SampleRespDto getSample(Long id);
    SampleRespDto createSample(SampleReqDto.Create dto);
}

// 구현체
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;
    private final SampleMapStruct sampleMapStruct;

    @Override
    public SampleRespDto getSample(Long id) {
        SampleEntity entity = findSampleOrThrow(id);
        return sampleMapStruct.toDto(entity);
    }

    @Override
    @Transactional
    public SampleRespDto createSample(SampleReqDto.Create dto) {
        SampleEntity entity = sampleMapStruct.toEntity(dto);
        return sampleMapStruct.toDto(sampleRepository.save(entity));
    }

    private SampleEntity findSampleOrThrow(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new BizException(
                        HttpStatus.NOT_FOUND, "NOT_FOUND", "샘플을 찾을 수 없습니다: " + id));
    }
}
```

- 클래스 레벨: `@Transactional(readOnly = true)` 기본 적용
- 쓰기 메서드: `@Transactional` 개별 적용
- 조회 실패 시: `find{Entity}OrThrow()` 패턴으로 `BizException` 발생

### 4.6 Controller

```java
@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "샘플", description = "샘플 API")
public class SampleController extends AbstractController {

    private final SampleService sampleService;

    @GetMapping
    @Operation(summary = "샘플 목록 (페이징)")
    public BizRespVo<Page<SampleRespDto>> paging(SampleSearchCond cond, Pageable pageable) {
        return makeResponse(sampleService.paging(cond, pageable));
    }

    @PostMapping
    @Operation(summary = "샘플 등록")
    public BizRespVo<SampleRespDto> createSample(@Valid @RequestBody SampleReqDto.Create dto) {
        return makeCreatedResponse(sampleService.createSample(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "샘플 삭제")
    public BizRespVo<Void> deleteSample(@PathVariable Long id) {
        sampleService.deleteSample(id);
        return makeDeleteResponse("삭제되었습니다.");
    }
}
```

- `AbstractController` 상속하여 응답 헬퍼 사용
- 응답 메서드: `makeResponse()`, `makeCreatedResponse()`, `makeDeleteResponse()`

### 4.7 MapStruct

```java
@Mapper(config = MapStructConfig.class)
public interface SampleMapStruct {
    SampleRespDto toDto(SampleEntity entity);
    SampleEntity toEntity(SampleReqDto.Create dto);
}
```

- `@Mapper(config = MapStructConfig.class)` 공통 설정 사용
- 연관 매퍼 사용 시: `@Mapper(config = MapStructConfig.class, uses = DetailMapStruct.class)`

---

## 5. API 규칙

### 5.1 URL 패턴

```
/api/{version}/{resource}
/api/{version}/{category}/{resource}
```

| 예시 | 설명 |
|------|------|
| `GET /api/v1/samples` | 목록 (페이징) |
| `GET /api/v1/samples/{id}` | 단건 조회 |
| `POST /api/v1/samples` | 등록 |
| `PUT /api/v1/samples/{id}` | 수정 |
| `DELETE /api/v1/samples/{id}` | 삭제 |
| `GET /api/v1/system/mlg` | 시스템 카테고리 |
| `GET /api/v1/i18n/messages` | 번들 API |

### 5.2 응답 형식

모든 API 응답은 `BizRespVo<T>`로 통일:

```json
{
  "statusCode": "OK",
  "resultCode": 200,
  "message": "OK",
  "body": { ... },
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-01 12:34:56"
}
```

### 5.3 에러 응답

```json
{
  "statusCode": "Bad Request",
  "resultCode": 400,
  "message": "잘못된 요청입니다.",
  "body": {
    "errorCode": "ERROR_02",
    "mlgCode": "MLG_ERR02",
    "path": "/api/v1/samples",
    "trace": ""
  },
  "requestId": "...",
  "timestamp": "..."
}
```

---

## 6. DB 컨벤션

### 6.1 테이블

- 접두사: `TB_` (예: `TB_SAMPLE`, `TB_USER`)
- 네이밍: **UPPER_SNAKE_CASE**

### 6.2 컬럼

- 네이밍: **UPPER_SNAKE_CASE** (예: `USER_ID`, `REG_DATETIME`)
- 공통 감사 컬럼 (BaseEntity):

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `REG_USER_ID` | VARCHAR | 등록자 |
| `REG_DATETIME` | TIMESTAMP | 등록일시 |
| `UPDATER_ID` | VARCHAR | 수정자 |
| `UPDATE_DATETIME` | TIMESTAMP | 수정일시 |

### 6.3 Flyway 마이그레이션

- 파일 위치: `module-api/src/main/resources/db/migration/`
- 네이밍: `V{번호}__{설명}.sql` (예: `V5__create_mlg_tables.sql`)

---

## 7. Import 규칙

Checkstyle + Spotless가 자동 관리하며, 순서는 다음과 같습니다:

```
java.*
                    ← 빈 줄
javax.*
                    ← 빈 줄
org.*
                    ← 빈 줄
com.*
                    ← 빈 줄
기타 (app.* 등)
                    ← 빈 줄
static imports       ← 맨 아래
```

- **와일드카드(`*`) import 금지** — 명시적으로 개별 import
- 사용하지 않는 import 금지
- 중복 import 금지

---

## 8. 중괄호 / 공백 규칙

### 8.1 중괄호

- `if`, `else`, `for`, `while` 등 **항상 중괄호 사용** (한 줄이어도)
- 여는 중괄호 `{`: **같은 줄** (K&R 스타일)
- 닫는 중괄호 `}`: `else`, `catch`, `finally`와 **같은 줄**

```java
// O
if (condition) {
    doSomething();
}

// X
if (condition)
    doSomething();

// X
if (condition)
{
    doSomething();
}
```

### 8.2 공백

- 연산자, 키워드 주변에 공백: `if (`, `a + b`, `a = b`
- 쉼표/세미콜론 뒤에 공백: `method(a, b, c)`
- 쉼표/세미콜론 앞에는 공백 금지

---

## 9. 메서드 / 복잡도 제한

| 항목 | 제한 |
|------|------|
| 메서드 길이 | **150줄** 이하 (빈 줄 제외) |
| 파라미터 개수 | **7개** 이하 (`@Override` 메서드 예외) |
| 순환 복잡도 | **10** 이하 |
| 한 줄에 하나의 statement만 | 필수 |

초과 시 `@SuppressWarnings("checkstyle:CyclomaticComplexity")` 등으로 예외 처리 가능하나, 가급적 리팩토링을 권장합니다.

---

## 10. 매직 넘버 금지

숫자 리터럴은 의미 있는 상수로 추출합니다. `-1`, `0`, `1`, `2`는 허용됩니다.

```java
// X
if (retryCount > 3) { ... }
Thread.sleep(5000);

// O
private static final int MAX_RETRY_COUNT = 3;
private static final long SLEEP_MILLIS = 5000L;

if (retryCount > MAX_RETRY_COUNT) { ... }
Thread.sleep(SLEEP_MILLIS);
```

어노테이션 내 숫자, `hashCode()` 내 숫자는 예외입니다.

---

## 11. Javadoc 규칙

- **public 클래스/인터페이스**: `@JavadocType` 필수
- **public 메서드**: Javadoc 권장 (`@param`, `@return` 태그는 생략 가능)
- Javadoc 안에서 `@` 어노테이션 표기 시: `&#64;` 사용

---

## 12. Lombok 사용 가이드

| 위치 | 권장 어노테이션 |
|------|----------------|
| Entity | `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| Request DTO | `@Getter`, `@Setter` |
| Response DTO | `@Getter`, `@Setter`, `@SuperBuilder`, `@NoArgsConstructor` |
| Service / Controller | `@Slf4j`, `@RequiredArgsConstructor` |
| Utility 클래스 | `@UtilityClass` 또는 `private 생성자 + final class` |
| 상수 enum 필드 | `@Getter` |

---

## 13. 보안 Validation 어노테이션

프로젝트 커스텀 보안 어노테이션을 활용합니다:

| 어노테이션 | 용도 |
|-----------|------|
| `@NoXSS` | XSS 공격 방지 |
| `@NoSQLInjection` | SQL Injection 방지 |
| `@ValidPassword` | 비밀번호 강도 검증 |
| `@NoMaliciousFile` | 악성 파일 검증 |
| `@ValidImageFile` | 이미지 파일 검증 |
| `@ValidMimeType` | MIME 타입 검증 |
| `@SafePath` | Path Traversal 방지 |
| `@Masking` | 민감 데이터 마스킹 |

---

## 14. 기타 코딩 규칙

- **빈 `catch` 블록 금지** — 예외 변수명이 `expected` 또는 `ignore`인 경우만 허용
- **유틸리티 클래스**: `private` 생성자 필수 (`HideUtilityClassConstructor`)
- **`final class`**: 상속 불필요한 클래스는 `final`로 선언 (`FinalClass`)
- **어노테이션 위치**: 파라미터 없는 단일 어노테이션은 같은 줄 허용 (`@Getter private String name`)
- **`@SuppressWarnings`**: 불가피한 경우 `@SuppressWarnings("checkstyle:RuleName")`으로 예외 처리

---

## 15. 코드 품질 도구

### 15.1 도구 소개

| 도구 | 목적 | 설정 파일 |
|------|------|-----------|
| **Checkstyle** | 코드 형식과 스타일 일관성 검사 (들여쓰기, 네이밍, 줄 길이, 괄호) | `config/checkstyle/checkstyle.xml` |
| **PMD** | 잠재적 버그와 안티패턴 탐지 (복잡도, 중복, 성능, 보안) | `config/pmd/ruleset.xml` |
| **SpotBugs** | 바이트코드 수준 버그 탐지 (Null pointer, 리소스 누수, 동시성) | `config/spotbugs/exclude.xml` |
| **Spotless** | 코드 자동 정리 및 포맷팅 (Google Java Format, import 정리) | `build.gradle` (spotless 블록) |
| **Jacoco** | 테스트 코드 커버리지 측정 (목표: 70% 이상) | `build.gradle` (jacoco 블록) |
| **EditorConfig** | IDE/에디터 간 일관된 설정 | `.editorconfig` |

### 15.2 Gradle 태스크

| 태스크 | 설명 |
|--------|------|
| `spotlessApply` | 코드 자동 포맷팅 적용 |
| `spotlessCheck` | 포맷팅 확인 (적용 안함) |
| `checkstyleMain` | 메인 코드 스타일 검사 |
| `checkstyleTest` | 테스트 코드 스타일 검사 |
| `pmdMain` | 메인 코드 품질 검사 |
| `pmdTest` | 테스트 코드 품질 검사 |
| `spotbugsMain` | 메인 코드 버그 탐지 |
| `spotbugsTest` | 테스트 코드 버그 탐지 |
| `test` | 테스트 실행 + 커버리지 |
| `jacocoTestReport` | 커버리지 리포트 생성 |
| `codeQuality` | 모든 품질 검사 (통합) |
| `formatAndCheck` | 포맷팅 + 검증 (통합) |

### 15.3 빠른 시작

```bash
# 자동 포맷팅
./gradlew spotlessApply

# 특정 모듈만
./gradlew :module-api:spotlessApply

# 모든 품질 검사 (Checkstyle + PMD + SpotBugs + Spotless)
./gradlew codeQuality

# 자동 포맷팅 후 모든 품질 검사
./gradlew formatAndCheck

# 전체 빌드 (품질 검사 포함)
./gradlew clean build
```

### 15.4 리포트 위치

```
module-{name}/build/reports/
├── checkstyle/
│   ├── main.html              # Checkstyle 리포트
│   └── test.html
├── pmd/
│   ├── main.html              # PMD 리포트
│   └── test.html
├── spotbugs/
│   └── main.html              # SpotBugs 리포트
├── jacoco/
│   └── test/html/index.html  # 커버리지 리포트
└── tests/
    └── test/index.html        # 테스트 결과
```

---

## 16. 개발 워크플로우

### 코드 작성 전
```bash
# EditorConfig 플러그인 설치 (IntelliJ/VSCode)
# 자동으로 들여쓰기/인코딩이 적용됩니다
```

### 코드 작성 중
```bash
# IDE에 Checkstyle/PMD 플러그인 설치하면
# 실시간으로 경고가 표시됩니다
```

### 커밋 전
```bash
# 자동 포맷팅
./gradlew spotlessApply

# 품질 검사
./gradlew codeQuality

# 테스트 + 커버리지
./gradlew test
```

### PR 전
```bash
# 통합 검증
./gradlew clean build

# 모든 검사 통과 확인
```

---

## 17. 컨벤션 커스터마이징

### 방법 1: 기존 설정 수정

#### Checkstyle 수정
```xml
<!-- config/checkstyle/checkstyle.xml -->

<!-- 줄 길이를 100자로 변경 -->
<module name="LineLength">
    <property name="max" value="100"/>
</module>

<!-- 들여쓰기를 2 spaces로 변경 -->
<module name="Indentation">
    <property name="basicOffset" value="2"/>
</module>
```

#### Spotless 포맷터 변경
```gradle
// build.gradle

spotless {
    java {
        // Google 스타일 → Eclipse 스타일로 변경
        eclipse()

        // 또는 IntelliJ 스타일
        // importOrder('java', 'javax', '', '\\#')

        // 또는 커스텀 Eclipse 설정 파일 사용
        // eclipse().configFile('config/spotless/eclipse-formatter.xml')
    }
}
```

### 방법 2: 예외 규칙 추가

#### Checkstyle 예외
```xml
<!-- config/checkstyle/suppressions.xml -->

<!-- Controller는 파라미터 제한 완화 -->
<suppress checks="ParameterNumber" files=".*Controller\.java"/>

<!-- Batch 클래스는 메서드 길이 제한 완화 -->
<suppress checks="MethodLength" files=".*Batch\.java"/>
```

#### SpotBugs 예외
```xml
<!-- config/spotbugs/exclude.xml -->

<!-- 특정 버그 패턴 제외 -->
<Match>
    <Class name="~.*Service"/>
    <Bug pattern="NP_NULL_ON_SOME_PATH"/>
</Match>
```

### 방법 3: 외부 컨벤션 사용

#### Naver Hackday Convention 사용
```gradle
// build.gradle

checkstyle {
    configFile = file("${rootDir}/config/checkstyle/naver-checkstyle-rules.xml")
}

spotless {
    java {
        importOrder('java', 'javax', 'jakarta', 'org', 'com', '')
        removeUnusedImports()
        // Naver에서 권장하는 포맷 적용
    }
}
```

다운로드: https://github.com/naver/hackday-conventions-java

---

## 18. IDE 통합

### IntelliJ IDEA

#### 1. EditorConfig 플러그인 (기본 내장)
- 설정 → Editor → Code Style
- "Enable EditorConfig support" 체크

#### 2. Checkstyle 플러그인
```
Settings → Plugins → Marketplace → "Checkstyle-IDEA" 설치
Settings → Tools → Checkstyle
  - Configuration File: config/checkstyle/checkstyle.xml 추가
  - "Scan Scope": All sources 선택
```

#### 3. PMD 플러그인
```
Settings → Plugins → Marketplace → "PMDPlugin" 설치
Settings → Other Settings → PMD
  - RuleSet: config/pmd/ruleset.xml 추가
```

#### 4. SonarLint (선택)
```
Settings → Plugins → Marketplace → "SonarLint" 설치
실시간 코드 품질 분석 제공
```

#### 5. Save Actions (자동 포맷팅)
```
Settings → Plugins → Marketplace → "Save Actions" 설치
Settings → Other Settings → Save Actions
  - "Optimize imports" 체크
  - "Reformat file" 체크
  - "Run on save" 체크
```

### Visual Studio Code

#### 1. Extensions 설치
```
- EditorConfig for VS Code
- Checkstyle for Java
- SonarLint
- Language Support for Java
```

#### 2. settings.json 설정
```json
{
    "java.checkstyle.configuration": "${workspaceFolder}/config/checkstyle/checkstyle.xml",
    "java.format.settings.url": "config/spotless/eclipse-formatter.xml",
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.organizeImports": true
    }
}
```

---

## 19. CI/CD 통합

### GitLab CI 예시
```yaml
# .gitlab-ci.yml

code-quality:
  stage: test
  script:
    - ./gradlew codeQuality
  artifacts:
    reports:
      codequality: build/reports/codequality.json
```

### GitHub Actions 예시
```yaml
# .github/workflows/code-quality.yml

name: Code Quality

on: [pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Run code quality checks
        run: ./gradlew codeQuality

      - name: Upload reports
        uses: actions/upload-artifact@v3
        with:
          name: quality-reports
          path: |
            **/build/reports/checkstyle/
            **/build/reports/pmd/
            **/build/reports/spotbugs/
```

---

## 20. 인라인 예외 처리

특정 파일/줄에서 검사를 제외해야 할 때:

```java
// Checkstyle 제외
// CHECKSTYLE:OFF
public void legacyCode() {
    // ...
}
// CHECKSTYLE:ON

// PMD 제외
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public void method() { }

// SpotBugs 제외
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
public void method() { }
```

빌드 시 검사를 건너뛰려면:

```bash
# 모든 검사 스킵
./gradlew build -x checkstyleMain -x pmdMain -x spotbugsMain

# 또는 임시로 ignoreFailures = true 설정
./gradlew build -PignoreCodeQuality=true
```

---

## 21. FAQ

### Q1. 어떤 컨벤션을 선택해야 하나요?
**A**: 팀 상황에 따라 다릅니다:
- **Google Style**: 모던하고 널리 사용됨 (2 spaces)
- **Sun/Oracle**: 전통적 Java 스타일 (4 spaces)
- **Naver Hackday**: 한국 기업 환경에 최적화

현재 프로젝트는 **Google AOSP (4 spaces)** 사용 중입니다.

### Q2. 기존 코드에 한 번에 적용하려면?
```bash
# 1. 자동 포맷팅 적용
./gradlew spotlessApply

# 2. 검사 실행 (에러 확인)
./gradlew codeQuality

# 3. 에러 수정 후 커밋
git add .
git commit -m "refactor: apply code quality rules"
```

---

## 22. 추가 리소스

- [Checkstyle 공식 문서](https://checkstyle.org/)
- [PMD 규칙 목록](https://pmd.github.io/pmd/pmd_rules_java.html)
- [SpotBugs 버그 패턴](https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Naver Hackday Convention](https://github.com/naver/hackday-conventions-java)

---

**최종 수정**: 2026-02-16
**버전**: 2.0.0
