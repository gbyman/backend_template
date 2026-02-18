# 템플릿 개선 로드맵

> Backend Template 프로젝트의 부족한 부분과 추가 권장 기능 목록입니다.
>
> **작성일**: 2025-02-15
> **버전**: 1.0.0

---

## 📊 프로젝트 현황

### ✅ 현재 구현된 기능

#### 아키텍처
- [x] 멀티 모듈 구조 (module-common, module-api, module-batch)
- [x] 최신 기술 스택 (Spring Boot 3.4.4 + Java 21)
- [x] 하이브리드 ORM (JPA + MyBatis)
- [x] 마이그레이션 관리 (Flyway)

#### 보안
- [x] JWT 인증/인가 (stateless/stateful 모드)
- [x] 8가지 커스텀 검증 (XSS, SQL Injection, Path Traversal 등)
- [x] Jasypt 암호화
- [x] Spring Security 통합

#### 인프라
- [x] 멀티 클라우드 파일 저장소 (Local/S3/Azure)
- [x] 배치 처리 (Spring Batch + Quartz + ShedLock)
- [x] 캐싱 (Redis)
- [x] ELK 스택 로깅 준비

#### 개발 환경
- [x] 환경별 설정 분리 (local/dev/prod)
- [x] Docker Compose 로컬 환경
- [x] Makefile 개발 편의성
- [x] CI/CD 예제 (GitLab CI, GitHub Actions)

#### Docker 배포 (2026-02-16 추가)
- [x] module-api/module-batch Multi-stage Dockerfile
- [x] 개발용 Dockerfile.dev, .dockerignore
- [x] docker-compose.prod.yml (운영 배포용)
- [x] CI/CD 파이프라인 Docker 빌드/배포 통합

#### 코드 품질 (2025-02-15 추가)
- [x] Checkstyle - 코딩 스타일 검사
- [x] PMD - 코드 품질 검사
- [x] SpotBugs - 정적 분석
- [x] Spotless - 자동 포맷터
- [x] Jacoco - 테스트 커버리지
- [x] EditorConfig - 에디터 통일

#### 로깅 & 보안 (2025-02-15 추가)
- [x] Logback 마스킹 - 민감 정보 자동 마스킹 (4가지 패턴: 전화번호, 이메일, 비밀번호)
- [x] Git Hooks - 커밋 메시지 검증, 보호 브랜치 push 방지
- [x] API Rate Limiting - Resilience4j 기반, IP별 전역 제한, API별 개별 제한 (@RateLimit)

#### 다국어 지원 (2026-02-16 추가)
- [x] Accept-Language 헤더 자동 파싱 (ISO 639-1 소문자: ko, en)
- [x] DB 기반 다국어 관리 (TB_MLG_GROUP + TB_MLG_DETAIL)
- [x] 다국어 관리 Admin CRUD API (`/api/v1/system/mlg`)
- [x] 번들 API (`/api/v1/i18n/messages?lang=en`)
- [x] 에러 메시지 MLG 코드 통합 (ErrorCode → mlgCode)

#### 테스트 인프라 (2026-02-16 추가)
- [x] 테스트 프로파일 (`application-test.yml`) - 누락 프로퍼티 보강
- [x] Base Test 클래스 (BaseIntegrationTest, BaseRepositoryTest, BaseControllerTest)
- [x] 테스트 보안 설정 (TestSecurityConfig)
- [x] 테스트 데이터 팩토리 (TestFixtures)
- [x] 다국어 도메인 테스트 37건 (Service 10, Repository 7, Controller 9, 기존 11)

---

## ⚠️ 부족한 부분

### 🔴 Critical (즉시 해결 필요)

#### 1. 테스트 인프라 구성 ⭐⭐
**상태**: ✅ 완료 (2026-02-16)
**영향도**: 중간
**설명**:
- 템플릿 프로젝트 특성상 Sample 코드는 실제 프로젝트에서 삭제/교체됨
- 테스트 코드 자체보다 **테스트 설정/구조**를 미리 잡아두는 것이 실용적
- 실제 테스트는 프로젝트 진행 시 비즈니스 로직에 맞춰 작성

**완료된 항목**:
- [x] Base Test 클래스 (`BaseIntegrationTest`, `BaseRepositoryTest`, `BaseControllerTest`)
- [x] 테스트 프로파일 (`application-test.yml`) - Security, CORS, JWT, Rate Limit 등 설정
- [x] 테스트 보안 설정 (`TestSecurityConfig`) - 슬라이스 테스트용 Security 비활성화
- [x] 테스트 데이터 팩토리 (`TestFixtures`) - MLG 도메인 테스트 데이터 생성
- [x] 테스트 로깅 설정 (`logback-test.xml`) - Elasticsearch 의존성 제거
- [x] 다국어 도메인 테스트 코드 (Service 10건 + Repository 7건 + Controller 9건)

**미완료 항목** (필요시 추가):
- [ ] TestContainers 설정 (PostgreSQL, Redis) - 현재 H2 인메모리 사용
- [ ] JWT 토큰 생성 헬퍼 (인증 필요 API 테스트 시)

**테스트 구조**:
```
module-api/src/test/java/app/backend/
├── support/
│   ├── BaseIntegrationTest.java    # @SpringBootTest 통합 테스트
│   ├── BaseRepositoryTest.java     # @DataJpaTest Repository 테스트
│   ├── BaseControllerTest.java     # @WebMvcTest Controller 테스트
│   ├── TestSecurityConfig.java     # Security 비활성화
│   └── TestFixtures.java           # 테스트 데이터 팩토리
├── app/mlg/
│   ├── group/
│   │   ├── service/MlgGroupServiceTest.java      # 10 tests
│   │   ├── repository/MlgGroupRepositoryTest.java # 4 tests
│   │   └── controller/MlgGroupControllerTest.java # 6 tests
│   ├── detail/
│   │   └── repository/MlgDetailRepositoryTest.java # 3 tests
│   └── bundle/
│       └── controller/MlgBundleControllerTest.java # 3 tests
└── core/log/
    └── MaskingPatternLayoutTest.java              # 11 tests (기존)
```

**참고**: 실제 비즈니스 테스트는 프로젝트별로 작성

---

#### 2. Docker 배포 환경 ⭐⭐⭐
**상태**: ✅ 완료 (2026-02-16)
**영향도**: 높음
**설명**:
- module-api, module-batch 각각 Multi-stage Dockerfile 작성
- 의존성 캐싱 레이어, non-root 사용자, 헬스체크 포함
- CI/CD 파이프라인 예제 업데이트 (GitLab CI, GitHub Actions)

**완료된 항목**:
- [x] `module-api/Dockerfile` - API 프로덕션 이미지 (Multi-stage, ~405MB)
- [x] `module-batch/Dockerfile` - Batch 프로덕션 이미지 (Multi-stage, ~371MB)
- [x] `Dockerfile.dev` - 개발용 이미지 (호스트 빌드 JAR 실행)
- [x] `.dockerignore` - 빌드 컨텍스트 최적화
- [x] `docker-compose.prod.yml` - 운영 배포용 (애플리케이션만, DB/Redis는 외부)
- [x] CI/CD 파이프라인 예제 (GitLab CI, GitHub Actions) - Docker 빌드/배포 통합

---

#### 3. Kubernetes 배포 설정 없음 ⭐
**상태**: ⬜ 보류
**영향도**: 낮음
**설명**:
- 현재 Docker Compose 기반 배포로 충분 (서버 1~2대 운영 기준)
- 서버 3대 이상 분산 배포, 오토스케일링, 무중단 배포가 필요해지는 시점에 도입 검토
- Kubernetes manifest 파일 없음
- Helm Chart 없음

**해결 방안**:
디렉토리 구조:
```
k8s/
├── base/
│   ├── deployment.yml
│   ├── service.yml
│   ├── configmap.yml
│   ├── secret.yml
│   └── ingress.yml
├── overlays/
│   ├── dev/
│   │   └── kustomization.yml
│   └── prod/
│       └── kustomization.yml
└── helm/
    └── backend-template/
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
```

**작성 필요**:
- [ ] Deployment - 애플리케이션 배포
- [ ] Service - 로드밸런싱
- [ ] ConfigMap - 설정 관리
- [ ] Secret - 민감 정보 관리
- [ ] Ingress - 외부 접근
- [ ] HPA - 오토스케일링
- [ ] PVC - 파일 저장소 볼륨
- [ ] Helm Chart - 패키지 관리

---

### 🟡 Important (빠른 시일 내 해결)

#### 4. APM (Application Performance Monitoring) 도구 미설정 ⭐
**상태**: 🟡 기본 모니터링은 준비됨
**영향도**: 낮음
**설명**:
- ✅ Spring Boot Actuator 설정 완료
- ✅ Prometheus 메트릭 노출 완료 (`/actuator/prometheus`)
- ✅ 기본 메트릭 자동 수집 (JVM, HTTP, DB 등)
- ⚠️ APM 도구 미설정 (필요시 추가)

**현재 상태로 충분한 이유**:
- Spring Boot Actuator가 이미 모든 기본 메트릭 제공
- Grafana 공식 대시보드 Import로 30초 완성 (Dashboard ID: 4701, 11378)
- 커스텀 메트릭은 특수한 비즈니스 요구사항 있을 때만 추가

**추가 고려사항 (필요시)**:

APM 도구는 **인프라 팀이 별도 설치/운영**:
- Scouter (국내 많이 사용)
- Pinpoint (네이버 오픈소스)
- Jaeger (분산 추적)
- OpenTelemetry (표준)

**애플리케이션에서 할 일**:
```java
// APM 에이전트 추가 (예: Scouter)
// 1. build.gradle
implementation 'io.github.scouter-project:scouter-agent-java:2.15.0'

// 2. JVM 옵션
-javaagent:/path/to/scouter.agent.jar
-Dscouter.config=/path/to/scouter.conf
```

**권장사항**:
- 초기에는 기본 메트릭만으로 충분
- 성능 이슈 발생 시 APM 도구 추가
- 템플릿에 미리 포함시킬 필요 없음

---

#### 5. API Rate Limiting ⭐⭐
**상태**: ✅ 완료 (2025-02-15)
**영향도**: 중간
**설명**:
- ✅ Resilience4j RateLimiter 기반 Rate Limiting 구현
- ✅ IP 기반 전역 Rate Limit (모든 API)
- ✅ @RateLimit 어노테이션으로 API별 개별 제한
- ✅ HTTP 429 응답 및 GlobalExceptionHandler 통합

**구현 내용**:
```yaml
# application.yml
app:
  rate-limit:
    enabled: true
    global:
      capacity: 100  # IP당 100회
      duration-seconds: 60  # 60초 = 1분
```

```java
// API별 개별 제한
@RateLimit(limit = 10, duration = 1, unit = TimeUnit.MINUTES)
@PostMapping("/api/login")
public ResponseEntity<?> login() {
    // 로그인 API는 IP당 1분에 10회만 허용
}
```

**완료된 기능**:
- [x] IP 기반 전역 Rate Limiting (RateLimitFilter)
- [x] API 엔드포인트별 개별 제한 (@RateLimit 어노테이션)
- [x] Rate Limit 초과 시 429 응답
- [x] Resilience4j RateLimiter (간단하고 Spring Boot 통합 우수)
- [x] GlobalExceptionHandler 통합

---

#### 6. API 문서화 부족 ⭐
**상태**: 🟡 Swagger만 있음
**영향도**: 낮음
**설명**:
- Swagger UI는 있지만 상세 예제 부족
- Postman Collection 없음
- API 가이드 문서 없음

**해결 방안**:
```json
// postman/Backend_Template.postman_collection.json
{
  "info": {
    "name": "Backend Template API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Sample API",
      "item": [...]
    }
  ]
}
```

**추가 필요**:
- [ ] Postman Collection
- [ ] Postman Environment (local, dev, prod)
- [ ] Spring REST Docs 통합 (테스트 기반 문서화)
- [ ] AsyncAPI (비동기 API 문서화)
- [ ] API 사용 가이드 (`docs/API_GUIDE.md`)

---

### 🟢 Nice to Have (추후 개선)

#### 7. Git Hooks 자동화 ⭐
**상태**: ✅ 완료 (2025-02-15)
**영향도**: 낮음
**설명**:
- ✅ commit-msg: Conventional Commits 형식 강제
- ✅ pre-push: main/prod 브랜치 직접 push 방지
- ✅ Gradle task로 자동 설치

**구현 내용**:
```bash
# .githooks/commit-msg
# Conventional Commits 형식 검증: feat:, fix:, docs: 등

# .githooks/pre-push
# main, master, prod, production 브랜치 push 차단

# 설치 방법
./gradlew installGitHooks
# 또는
bash .githooks/install-hooks.sh
```

**완료된 항목**:
- [x] Git Hooks 스크립트 작성
- [x] commit-msg: 커밋 메시지 검증
- [x] pre-push: 보호 브랜치 push 방지
- [x] 자동 설치 스크립트
- [x] Gradle task 통합

---

#### 8. 보안 강화 기능
**상태**: 🟡 일부 완료
**영향도**: 낮음

**✅ 이미 구현된 기능**:
- [x] **세션 동시 접속 제한** (JWT + Redis)
  - 기본: 중복 로그인 차단 (마지막 로그인만 유효)
  - 예외: `allow-duplicate.ids`에 등록된 ID는 중복 로그인 허용
  - 설정: `jwt.use-redis: true` 필요
  - 구현 위치: `JwtService.isRefreshTokenMatched()` (JwtService.java:217-239)

**❌ 미구현 기능**:
- [ ] ~~IP 화이트리스트 설정~~ (인프라 레벨에서 처리 - 방화벽/WAF/Security Group)
- [ ] ~~2FA (Two-Factor Authentication)~~ (SSO/SAML 연동 시 불필요 - IdP MFA 사용)
- [ ] ~~계정 잠금 정책 (5회 실패 시)~~ (SSO/SAML 연동 시 불필요 - IdP에서 관리)
- [ ] ~~비밀번호 변경 주기 강제~~ (SSO/SAML 연동 시 불필요 - IdP에서 관리)

---

## 🎯 추천 기능 (SI 프로젝트 특화)

### Phase 1: 필수 기능

#### 1. Excel Import/Export ⭐⭐⭐
**우선순위**: High
**비즈니스 가치**: 매우 높음 (SI 프로젝트 필수)

```gradle
implementation 'org.apache.poi:poi:5.2.5'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
```

**구현 내용**:
- [ ] Excel 업로드 템플릿 검증
- [ ] 대용량 Excel 스트리밍 처리
- [ ] Excel 다운로드 (템플릿 제공)
- [ ] 에러 검증 및 리포트
- [ ] 샘플 코드 제공

---

#### 2. PDF 생성 ⭐⭐
**우선순위**: High
**비즈니스 가치**: 높음 (보고서, 계약서 등)

```gradle
// 선택 1: iText (상용 라이센스 주의)
implementation 'com.itextpdf:itext7-core:8.0.2'

// 선택 2: OpenPDF (오픈소스)
implementation 'com.github.librepdf:openpdf:1.3.30'
```

**구현 내용**:
- [ ] HTML to PDF 변환
- [ ] PDF 템플릿 엔진
- [ ] 워터마크/전자서명
- [ ] 한글 폰트 지원

---

#### 3. 다국어 지원 (i18n) ⭐⭐
**상태**: ✅ 완료 (2026-02-16)
**비즈니스 가치**: 중간
**설명**:
- ✅ Accept-Language 헤더 자동 파싱 (ReqContextVo + I18nProperties)
- ✅ DB 기반 다국어 관리 시스템 (TB_MLG_GROUP + TB_MLG_DETAIL)
- ✅ 다국어 관리 Admin CRUD API
- ✅ 번들 API (F/E 언어 전환용)
- ✅ 에러 메시지 MLG 코드 통합

**구현 방식**: 하이브리드 i18n
- F/E: 번들 API(`/api/v1/i18n/messages?lang=en`)로 정적 UI 텍스트 다국어 처리
- B/E: Accept-Language 헤더 자동 파싱으로 동적 비즈니스 데이터 다국어 처리
- 에러: MLG 코드 반환 → F/E에서 번들을 통해 다국어 표시

```yaml
# application.yml
app:
  i18n:
    default-lang: ko
    supported-langs: ko, en
```

**완료된 기능**:
- [x] Accept-Language 헤더 자동 파싱 (ISO 639-1 소문자)
- [x] 언어 설정 외부화 (application.yml → I18nProperties)
- [x] DB 다국어 테이블 (Flyway V5 마이그레이션)
- [x] 다국어 관리 Admin CRUD API (`/api/v1/system/mlg`)
- [x] 번들 API (`/api/v1/i18n/messages?lang=en`)
- [x] 에러 메시지 MLG 코드 통합 (ErrorCode → mlgCode)

---

### Phase 2: 부가 기능

#### 4. 변경 이력 추적 (Hibernate Envers) - 선택적 ⭐
**우선순위**: Low (대부분 불필요)
**비즈니스 가치**: 특수 프로젝트만 (금융권, 관공서)

**현재 상태**:
```java
// BaseEntity.java (이미 구현됨)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedBy
    private String regUserId;          // 등록자 ID
    @CreatedDate
    private LocalDateTime regDatetime; // 등록일시
    @LastModifiedBy
    private String updaterId;          // 수정자 ID
    @LastModifiedDate
    private LocalDateTime updateDatetime; // 수정일시
}
```

**✅ 이것만으로 충분한 경우** (99% 프로젝트):
- 일반 SI 프로젝트
- 웹 서비스
- 사내 시스템
- "누가, 언제 수정했는지"만 알면 됨

**⚠️ Envers가 필요한 경우** (극히 일부):
- 금융권 (법적 요구사항)
- 관공서 (감사 대응)
- 의료 시스템 (환자 데이터)
- "무엇이 어떻게 변경되었는지" 전체 이력 필요

**구현 방법 (필요시)**:
```gradle
// build.gradle
implementation 'org.hibernate:hibernate-envers'
```

```java
@Audited  // 엔티티에 추가
@Entity
public class SampleEntity extends BaseEntity {
    private String title;
}

// 변경 이력 조회
AuditReader reader = AuditReaderFactory.get(entityManager);
List<Number> revisions = reader.getRevisions(SampleEntity.class, id);
```

**단점**:
- 저장 공간 2배 이상 (별도 이력 테이블)
- 쿼리 성능 저하
- 복잡도 증가

**권장사항**:
- 템플릿에는 BaseEntity만으로 충분
- 특수 요구사항 있을 때만 Envers 추가
- 대부분의 프로젝트는 불필요

---

#### 5. 대용량 파일 업로드 ⭐
**우선순위**: Low
**비즈니스 가치**: 중간

```java
// Resumable Upload (청크 업로드)
@PostMapping("/upload/chunk")
public ResponseEntity<?> uploadChunk(
    @RequestParam("file") MultipartFile chunk,
    @RequestParam("chunkNumber") int chunkNumber,
    @RequestParam("totalChunks") int totalChunks
) {
    // 청크 저장 및 병합
}
```

**구현 내용**:
- [ ] 청크 업로드 지원
- [ ] 업로드 재개 기능
- [ ] 진행률 추적
- [ ] 바이러스 스캔 통합 (선택)

---

#### 6. WebSocket 실시간 알림 ⭐
**우선순위**: 보류
**비즈니스 가치**: 중간
**상태**: ⬜ 보류 - 실시간 알림(결재 승인, 시스템 공지 등) 요구사항이 발생할 때 도입. 단방향 알림만 필요하면 SSE(Server-Sent Events)가 더 간단하다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-websocket'
```

**구현 내용**:
- [ ] STOMP over WebSocket
- [ ] 사용자별 알림 구독
- [ ] 브로드캐스트 메시지
- [ ] SockJS 폴백

---

#### 7. 스케줄러 관리 UI ⭐
**우선순위**: Low
**비즈니스 가치**: 낮음

**구현 내용**:
- [ ] Quartz Job 목록 조회 API
- [ ] Job 실행/중지/재시작 API
- [ ] 실행 이력 조회
- [ ] 크론 표현식 검증기

---

#### 8. 데이터 마스킹 ⭐⭐
**우선순위**: Medium
**비즈니스 가치**: 높음 (개인정보 보호)

```java
@JsonSerialize(using = MaskingSerializer.class)
@Masking(type = MaskingType.PHONE)
private String phoneNumber;

// 로그 출력 시 자동 마스킹
// 010-1234-5678 → 010-****-5678
```

**구현 내용**:
- [x] Jackson Serializer 마스킹 (이미 구현됨)
- [x] Logback 마스킹 패턴 (2025-02-15 완료)
  - [x] 전화번호, 이메일, 비밀번호 4가지 필수 패턴
  - [x] MaskingPatternLayout 구현
  - [x] logback-spring.xml 적용
  - [x] 테스트 코드 작성
**참고**: 주민번호, 카드번호, 계좌번호, 토큰, API Key는 로그 출력 자체를 금지하는 것이 바람직하므로 마스킹 패턴에서 제외됨
- DB 컬럼 암호화는 DB 솔루션(D'Amo, TDE 등)에서 처리
- 리스트/상세 마스킹 분리는 API별 RespDto를 나누어 처리

---

## 📋 우선순위별 로드맵

### Sprint 1 (즉시 시작)
- [x] **테스트 인프라 구성** - Base 클래스, 테스트 프로파일, MLG 도메인 테스트 ✅ 완료
- [x] **Docker 배포 환경** - Dockerfile, CI/CD 파이프라인 ✅ 완료

### Sprint 2 (2주 내)
- [ ] **Excel Import/Export** - SI 필수 기능
- [ ] **PDF 생성** - 보고서 기능
- [ ] **API 문서화 개선** - Postman Collection

### Sprint 4 (2개월 내)
- [x] **다국어 지원** - 글로벌 대응 ✅ 완료
- [ ] **감사 로깅** - 이력 관리

### Sprint 5 (장기)
- [ ] **WebSocket 실시간** - 실시간 알림 요구사항 발생 시 도입 (단방향이면 SSE 검토)
- [ ] **Kubernetes 매니페스트** - 서버 3대 이상 운영 또는 오토스케일링 필요 시 도입

---

## 📊 진행 상황 추적

| 항목 | 상태 | 완료일 | 담당자 | 비고 |
|------|------|--------|--------|------|
| 코드 품질 도구 | ✅ 완료 | 2025-02-15 | - | Checkstyle, PMD 등 |
| 로그 마스킹 | ✅ 완료 | 2025-02-15 | - | 전화번호, 이메일, 비밀번호 |
| Git Hooks | ✅ 완료 | 2025-02-15 | - | commit-msg, pre-push |
| Rate Limiting | ✅ 완료 | 2025-02-15 | - | Resilience4j RateLimiter |
| 기본 모니터링 | ✅ 완료 | - | - | Actuator + Prometheus |
| 다국어 지원 | ✅ 완료 | 2026-02-16 | - | DB 기반 MLG + 번들 API |
| 테스트 인프라 | ✅ 완료 | 2026-02-16 | - | Base 클래스, 테스트 프로파일, MLG 37건 |
| Docker 배포 환경 | ✅ 완료 | 2026-02-16 | - | Dockerfile, CI/CD 파이프라인 |
| Kubernetes | ⬜ 보류 | - | - | 필요 시점에 도입 (서버 3대+, 오토스케일링) |

---

## 🔗 관련 문서

- [코드 스타일 & 품질 가이드](./CODE_STYLE.md)
- [테스트 작성 가이드](./TESTING.md)
- [로그 마스킹 가이드](./LOGGING_MASKING.md)
- [세션 제어 가이드](./SESSION_CONTROL.md)
- [프로젝트 README](../README.md)
- [API 가이드](./API_GUIDE.md) (작성 예정)
- [배포 가이드](./DEPLOYMENT.md) (작성 예정)

---

**최종 수정**: 2026-02-16
