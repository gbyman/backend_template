# 공통 테이블 전환 작업 계획

> **작성일:** 2026-02-18
> **대상:** 레거시 시스템 → Backend Template 공통 테이블
> **목표:** 16개 시스템 관리 테이블을 프로젝트 범용 공통 테이블로 전환

---

## 📋 작업 개요

### 대상 테이블
- **전체:** 25개
- **공통화:** 16개 (권한, 사용자, 메뉴, 코드, 다국어, 시스템, 게시판)
- **제외:** 9개 (분류체계 관리 6개 - LCI 특화, 특수 시스템 3개 - ETSDB, PKI)

### 작업 범위
1. ✅ 테이블 명명 규칙 변경 (`tb_system_*`)
2. ✅ DDL 파일 생성 (PostgreSQL)
3. ✅ Entity 클래스 생성 (module-common)
4. ✅ Repository 인터페이스 생성
5. ✅ 기본 Service 구현
6. ✅ 확장성 전략 적용

---

## 🎯 Phase 1: 테이블 설계 및 DDL 생성

### 1.1 테이블 명명 규칙 변경

**명명 규칙:** `tb_system_{기능명}`
- **접두사:** `tb_` (Table)
- **모듈:** `system_` (시스템 관리 공통 기능)
- **기능명:** 테이블의 실제 기능 (auth, menu, code 등)

**상세 규칙:** [TABLE_NAMING_CONVENTION.md](db/TABLE_NAMING_CONVENTION.md) 참고

---

### 1.2 대상 테이블 목록 (16개)

#### 권한 관리 (3개)
- [ ] `tb_system_auth` - 시스템 권한
- [ ] `tb_system_group_auth` - 그룹 권한
- [ ] `tb_system_request_assignee` - 권한 요청 담당자

#### 사용자 관리 (2개)
- [ ] `tb_system_userinfo` - 사용자 정보
- [ ] `tb_system_userinfo_test` - 테스트 사용자 (개발 환경용)

#### 메뉴 관리 (1개)
- [ ] `tb_system_menu` - 시스템 메뉴

#### 코드 관리 (2개) ✅
- [x] `tb_system_code_group` - 공통코드 그룹
- [x] `tb_system_code_detail` - 공통코드 상세

#### 다국어 관리 (2개) ✅
- [x] `tb_system_mlg_group` - 다국어 그룹 (is_system 플래그)
- [x] `tb_system_mlg_detail` - 다국어 상세

#### 시스템 관리 (2개)
- [ ] `tb_system_log` - 시스템 로그 (접근 로그, 작업 로그)
- [ ] `tb_system_contact` - CONTACT 정보

#### 파일/템플릿 관리 (2개)
- [ ] `tb_system_excel_template` - 엑셀 템플릿
- [ ] `tb_system_sample_file` - 샘플 파일

#### 게시판 관리 (2개)
- [ ] `tb_system_notice` - 공지사항
- [ ] `tb_system_userguide` - 사용자 가이드

---

### 1.3 DDL 파일 구조

```
docs/db/ddl/
├── 01_system_auth.sql           # 권한 관리 (3개 테이블)
├── 02_system_user.sql           # 사용자 관리 (2개 테이블)
├── 03_system_menu.sql           # 메뉴 관리 (1개 테이블)
├── 04_system_code.sql           # 코드 관리 (2개 테이블)
├── 05_system_mlg.sql            # 다국어 관리 (2개 테이블)
├── 06_system_log.sql            # 시스템 관리 (2개 테이블)
├── 07_system_file.sql           # 파일/템플릿 관리 (2개 테이블)
├── 08_system_board.sql          # 게시판 관리 (2개 테이블)
└── 99_init_data.sql             # 초기 데이터 (권한, 코드 등)
```

**작업 순서:**
1. MySQL DDL 파일 분석
2. PostgreSQL로 변환
3. 공통 컬럼 표준화 (created_at, updated_at, created_by 등)
4. 인덱스 및 제약조건 추가

---

## 🏗️ Phase 2: Entity 클래스 생성

### 2.1 패키지 구조

```
module-common/src/main/java/app/backend/
└── system/                      # 시스템 관리 공통 모듈
    ├── auth/                    # 권한 관리
    │   └── entity/
    │       ├── SystemAuthEntity.java
    │       ├── SystemGroupAuthEntity.java
    │       └── SystemRequestAssigneeEntity.java
    ├── user/                    # 사용자 관리
    │   └── entity/
    │       ├── SystemUserinfoEntity.java
    │       └── SystemUserinfoTestEntity.java
    ├── menu/                    # 메뉴 관리
    │   └── entity/
    │       └── SystemMenuEntity.java
    ├── code/                    # 코드 관리
    │   └── entity/
    │       ├── SystemCodeGroupEntity.java
    │       └── SystemCodeDetailEntity.java
    ├── mlg/                     # 다국어 관리
    │   └── entity/
    │       ├── SystemMlgGroupEntity.java
    │       └── SystemMlgDetailEntity.java
    ├── log/                     # 로그 관리
    │   └── entity/
    │       └── SystemLogEntity.java
    ├── contact/                 # CONTACT 관리
    │   └── entity/
    │       └── SystemContactEntity.java
    ├── file/                    # 파일 관리
    │   └── entity/
    │       ├── SystemExcelTemplateEntity.java
    │       └── SystemSampleFileEntity.java
    └── board/                   # 게시판 관리
        └── entity/
            ├── SystemNoticeEntity.java
            └── SystemUserguideEntity.java
```

---

### 2.2 Entity 설계 원칙

#### BaseEntity 상속
```java
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn = "Y";
}
```

#### Entity 예시 (SystemAuthEntity)
```java
@Entity
@Table(name = "tb_system_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemAuthEntity extends BaseEntity {

    @Id
    @Column(name = "auth_code", length = 50)
    private String authCode;

    @Column(name = "auth_name", length = 100, nullable = false)
    private String authName;

    @Column(name = "auth_desc", length = 500)
    private String authDesc;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public SystemAuthEntity(String authCode, String authName,
                           String authDesc, Integer sortOrder) {
        this.authCode = authCode;
        this.authName = authName;
        this.authDesc = authDesc;
        this.sortOrder = sortOrder;
    }
}
```

---

### 2.3 확장성 전략

#### 옵션 1: 확장 테이블 (추천) ✅
```sql
-- 공통 테이블 (변경 없음)
CREATE TABLE tb_system_userinfo (
    user_id VARCHAR(50) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 프로젝트별 확장 테이블
CREATE TABLE tb_project_userinfo_ext (
    user_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(100),
    position VARCHAR(50),
    employee_number VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES tb_system_userinfo(user_id)
);
```

#### 옵션 2: JSON 컬럼 활용
```sql
CREATE TABLE tb_system_userinfo (
    user_id VARCHAR(50) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    extended_data JSONB,  -- 프로젝트별 확장 데이터
    created_at TIMESTAMP
);
```

**추천 전략:**
- **기본 정보:** 확장 테이블 방식 (타입 안정성)
- **선택적 메타데이터:** JSON 컬럼 방식 (유연성)

---

### 2.4 다국어 관리 특수 전략 ⭐

#### 설계 목표
**사용자 입력 편의성 우선** - 관리자가 다국어를 등록할 때 최소한의 정보만 입력

#### is_system 플래그 활용

```sql
CREATE TABLE tb_system_mlg_group (
    mlg_group_id        SERIAL PRIMARY KEY,
    mlg_code            VARCHAR(50) NOT NULL,
    mlg_name            VARCHAR(100) NOT NULL,
    is_system           VARCHAR(1) NOT NULL DEFAULT 'N',  -- ⭐ 핵심
    -- ... 공통 컬럼
);
```

#### 코드 관리 전략

**1. 시스템 기본 코드 (is_system = 'Y')**
```sql
-- 개발자가 코드에서 하드코딩할 필수 코드
INSERT INTO tb_system_mlg_group (mlg_code, mlg_name, is_system) VALUES
('MLG_USE_Y', '사용', 'Y'),
('MLG_NOTICE', '공지사항', 'Y'),
('MLG_ACTIVE', '활성', 'Y');
```

**특징:**
- 의미있는 코드명 (`MLG_USE_Y`)
- 초기 데이터로 제공
- 삭제/수정 제한 (프론트에서 버튼 비활성화)
- Java 코드에서 하드코딩 가능
  ```java
  if (mlgCode.equals("MLG_USE_Y")) { ... }
  ```

**2. 사용자 추가 코드 (is_system = 'N')**
```sql
-- 관리자가 프론트에서 추가한 코드 (자동 채번)
INSERT INTO tb_system_mlg_group (mlg_code, mlg_name, is_system) VALUES
('mlg00001', '주문상태', 'N'),
('mlg00002', '배송상태', 'N');
```

**특징:**
- 순차 번호 자동 생성 (`mlg00001`, `mlg00002`, ...)
- 프론트에서 `mlg_name`만 입력, `mlg_code`는 백엔드 자동 생성
- 삭제/수정 가능
- DB 조회 후 사용
  ```java
  MlgGroup mlg = mlgRepository.findByMlgCode("mlg00001");
  ```

#### 자동 채번 로직 (Service Layer)

```java
@Service
@RequiredArgsConstructor
public class MlgGroupService {

    private final MlgGroupRepository repository;

    @Transactional
    public MlgGroup createUserMlg(MlgGroupRequest request) {
        // 사용자는 mlg_name만 입력
        String autoMlgCode = generateNextMlgCode();

        MlgGroup group = MlgGroup.builder()
            .mlgCode(autoMlgCode)          // mlg00001 (자동)
            .mlgName(request.getMlgName()) // 주문상태 (사용자 입력)
            .isSystem("N")
            .build();

        return repository.save(group);
    }

    private String generateNextMlgCode() {
        // "mlg"로 시작하는 코드 중 최대값 찾기
        String maxCode = repository.findMaxMlgCodeStartingWith("mlg");

        if (maxCode == null) {
            return "mlg00001";
        }

        // "mlg00123" → 123 추출 → 124 → "mlg00124"
        int currentNum = Integer.parseInt(maxCode.substring(3));
        int nextNum = currentNum + 1;

        return String.format("mlg%05d", nextNum);
    }
}
```

#### Repository 메서드

```java
public interface MlgGroupRepository extends JpaRepository<MlgGroup, Long> {

    @Query("SELECT m.mlgCode FROM MlgGroup m " +
           "WHERE m.mlgCode LIKE :prefix% " +
           "ORDER BY m.mlgCode DESC LIMIT 1")
    String findMaxMlgCodeStartingWith(@Param("prefix") String prefix);

    // 시스템 코드 조회
    @Query("SELECT m FROM MlgGroup m WHERE m.isSystem = 'Y'")
    List<MlgGroup> findSystemCodes();

    // 사용자 코드 조회
    @Query("SELECT m FROM MlgGroup m WHERE m.isSystem = 'N'")
    List<MlgGroup> findUserCodes();
}
```

#### 프론트엔드 UI 예시

```typescript
// 사용자 추가 다국어 등록 폼
<form onSubmit={handleSubmit}>
  <input
    name="mlgName"
    placeholder="다국어 명칭 입력 (예: 주문상태)"
    required
  />
  {/* mlgCode 입력 필드 없음 - 자동 생성 */}
  <button type="submit">등록</button>
</form>

// 시스템 코드는 수정/삭제 불가
{mlgGroup.isSystem === 'Y' && (
  <div className="badge-system">시스템 코드</div>
)}
<button
  disabled={mlgGroup.isSystem === 'Y'}
  onClick={handleDelete}
>
  삭제
</button>
```

#### 장단점 트레이드오프

**장점 (사용자 편의성)**
- ✅ 관리자가 코드값 고민 불필요
- ✅ 중복 방지 자동화
- ✅ 입력 항목 최소화

**단점 (개발 편의성)**
- ⚠️ 사용자 추가 코드는 하드코딩 불가
- ⚠️ 조인 시 의미 파악 어려움 (`mlg00001` vs `MLG_USE_Y`)

**결론:** 사용자 입력 편의성을 우선하되, 핵심 시스템 코드는 의미있는 코드로 유지

---

## 🔧 Phase 3: Repository 및 Service 구현

### 3.1 Repository 인터페이스

```java
// module-common/src/main/java/app/backend/system/auth/repository/
public interface SystemAuthRepository extends JpaRepository<SystemAuthEntity, String> {

    // 기본 CRUD는 JpaRepository 제공

    // 공통 쿼리 메서드
    List<SystemAuthEntity> findByUseYn(String useYn);

    Optional<SystemAuthEntity> findByAuthCode(String authCode);

    // QueryDSL 지원
    @Query("SELECT a FROM SystemAuthEntity a WHERE a.authName LIKE %:keyword%")
    List<SystemAuthEntity> searchByKeyword(@Param("keyword") String keyword);
}
```

---

### 3.2 Service 계층

#### Interface
```java
// module-common/src/main/java/app/backend/system/auth/service/
public interface SystemAuthService {
    SystemAuthEntity create(SystemAuthEntity entity);
    SystemAuthEntity update(String authCode, SystemAuthEntity entity);
    void delete(String authCode);
    Optional<SystemAuthEntity> findById(String authCode);
    List<SystemAuthEntity> findAll();
    List<SystemAuthEntity> findActiveList();
}
```

#### Implementation
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemAuthServiceImpl implements SystemAuthService {

    private final SystemAuthRepository repository;

    @Transactional
    @Override
    public SystemAuthEntity create(SystemAuthEntity entity) {
        return repository.save(entity);
    }

    @Transactional
    @Override
    public SystemAuthEntity update(String authCode, SystemAuthEntity entity) {
        SystemAuthEntity existing = repository.findById(authCode)
            .orElseThrow(() -> new EntityNotFoundException("권한을 찾을 수 없습니다."));

        // 업데이트 로직
        return repository.save(existing);
    }

    @Override
    public List<SystemAuthEntity> findActiveList() {
        return repository.findByUseYn("Y");
    }
}
```

---

## 📦 Phase 4: module-api 통합

### 4.1 Controller 구현

```java
@RestController
@RequestMapping("/api/system/auth")
@RequiredArgsConstructor
public class SystemAuthController {

    private final SystemAuthService authService;
    private final SystemAuthMapper authMapper;

    @GetMapping
    public ResponseEntity<List<SystemAuthRespDto>> getAuthList() {
        List<SystemAuthEntity> entities = authService.findActiveList();
        List<SystemAuthRespDto> response = authMapper.toDtoList(entities);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SystemAuthRespDto> createAuth(
            @RequestBody @Valid SystemAuthReqDto request) {
        SystemAuthEntity entity = authMapper.toEntity(request);
        SystemAuthEntity created = authService.create(entity);
        return ResponseEntity.ok(authMapper.toDto(created));
    }
}
```

---

### 4.2 DTO 및 Mapper

```java
// Request DTO
@Getter
@NoArgsConstructor
public class SystemAuthReqDto {
    @NotBlank
    private String authCode;

    @NotBlank
    private String authName;

    private String authDesc;
    private Integer sortOrder;
}

// Response DTO
@Getter
@NoArgsConstructor
public class SystemAuthRespDto {
    private String authCode;
    private String authName;
    private String authDesc;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}

// MapStruct Mapper
@Mapper(componentModel = "spring")
public interface SystemAuthMapper {
    SystemAuthEntity toEntity(SystemAuthReqDto dto);
    SystemAuthRespDto toDto(SystemAuthEntity entity);
    List<SystemAuthRespDto> toDtoList(List<SystemAuthEntity> entities);
}
```

---

## 🧪 Phase 5: 테스트 작성

### 5.1 Repository 테스트

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SystemAuthRepositoryTest {

    @Autowired
    private SystemAuthRepository repository;

    @Test
    @DisplayName("권한 생성 테스트")
    void createAuth() {
        // given
        SystemAuthEntity auth = SystemAuthEntity.builder()
            .authCode("ADMIN")
            .authName("관리자")
            .build();

        // when
        SystemAuthEntity saved = repository.save(auth);

        // then
        assertThat(saved.getAuthCode()).isEqualTo("ADMIN");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
```

---

### 5.2 Service 테스트

```java
@ExtendWith(MockitoExtension.class)
class SystemAuthServiceTest {

    @Mock
    private SystemAuthRepository repository;

    @InjectMocks
    private SystemAuthServiceImpl service;

    @Test
    @DisplayName("활성 권한 목록 조회")
    void findActiveList() {
        // given
        List<SystemAuthEntity> mockList = List.of(
            SystemAuthEntity.builder().authCode("ADMIN").build(),
            SystemAuthEntity.builder().authCode("USER").build()
        );
        when(repository.findByUseYn("Y")).thenReturn(mockList);

        // when
        List<SystemAuthEntity> result = service.findActiveList();

        // then
        assertThat(result).hasSize(2);
        verify(repository).findByUseYn("Y");
    }
}
```

---

## 📊 작업 우선순위 및 일정

### 우선순위 1: 핵심 기능 (1주차)
- [x] **코드 관리** (code_group, code_detail) ✅ DDL 완료
  - 공통코드는 모든 기능의 기반
  - ~~DDL~~ → Entity → Repository → Service → Controller

- [ ] **권한 관리** (auth, group_auth)
  - 보안의 핵심
  - 권한 체크 로직 구현

- [ ] **사용자 관리** (userinfo)
  - 기본 사용자 정보
  - 인증/인가 연동

### 우선순위 2: 시스템 기능 (2주차)
- [ ] **메뉴 관리** (menu)
  - 동적 메뉴 구성
  - 권한별 메뉴 필터링

- [ ] **로그 관리** (log)
  - 접근 로그 기록
  - 감사 추적

- [x] **다국어 관리** (mlg_group, mlg_detail) ✅ DDL 완료
  - 다국어 지원 기반
  - is_system 플래그 활용 (시스템 코드 vs 사용자 코드)
  - mlg_code 자동 채번 로직 구현
  - ~~DDL~~ → Entity → Repository → Service

### 우선순위 3: 부가 기능 (3주차)
- [ ] **게시판 관리** (notice, userguide)
- [ ] **파일 관리** (excel_template, sample_file)
- [ ] **기타** (contact)

---

## ✅ 완료 기준 (DoD: Definition of Done)

각 테이블별 완료 체크리스트:

### DDL
- [ ] PostgreSQL DDL 작성 완료
- [ ] 인덱스 및 제약조건 정의
- [ ] 초기 데이터 스크립트 작성

### Backend
- [ ] Entity 클래스 생성 (BaseEntity 상속)
- [ ] Repository 인터페이스 생성
- [ ] Service 구현 (CRUD)
- [ ] Controller 구현 (REST API)
- [ ] DTO 및 Mapper 작성

### 테스트
- [ ] Repository 테스트 작성
- [ ] Service 테스트 작성
- [ ] Controller 통합 테스트 작성
- [ ] 테스트 커버리지 70% 이상

### 문서
- [ ] API 문서 작성 (Swagger)
- [ ] 테이블 ERD 업데이트
- [ ] README 업데이트

---

## 📝 참고 문서

- [TABLE_NAMING_CONVENTION.md](db/TABLE_NAMING_CONVENTION.md) - 테이블 명명 규칙
- [BUILD_GRADLE_REVIEW.md](BUILD_GRADLE_REVIEW.md) - 프로젝트 구조

---

## 🔄 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-02-18 | 1.1 | 다국어 관리 특수 전략 추가 (is_system, 자동 채번) | Claude Code |
| 2026-02-18 | 1.0 | 초안 작성 | Claude Code |

---

**완료된 작업:**
- ✅ 코드 관리 테이블 DDL 작성 완료 ([04_system_code.sql](db/ddl/04_system_code.sql))
- ✅ 다국어 관리 테이블 DDL 작성 완료 ([05_system_mlg.sql](db/ddl/05_system_mlg.sql))

**다음 단계:** Phase 2 - Entity 클래스 생성 또는 추가 테이블 DDL 작성
