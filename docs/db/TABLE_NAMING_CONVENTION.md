# 테이블 명명 규칙 (Table Naming Convention)

## 📋 목차

1. [기본 원칙](#기본-원칙)
2. [명명 패턴](#명명-패턴)
3. [상세 규칙](#상세-규칙)
4. [예시](#예시)
5. [안티 패턴](#안티-패턴)
6. [체크리스트](#체크리스트)

---

## 기본 원칙

### 1. 가독성 우선
- **명확성 > 간결성**
- 축약형보다는 전체 단어 사용 권장
- 팀원 누구나 이해할 수 있는 이름

### 2. 일관성 유지
- 프로젝트 전체에서 동일한 규칙 적용
- 예외는 최소화

### 3. 확장성 고려
- 멀티 모듈 환경에서도 충돌 없이 확장 가능
- 명확한 소속 표시

---

## 명명 패턴

### 패턴 1: 기본 형식 (단일 모듈/단순 구조)

```
tb_메뉴_기능
```

**사용 시기:**
- 단일 모듈 프로젝트
- 명확한 도메인 구분이 있는 경우
- 테이블 수가 적은 경우 (<50개)

**예시:**
```sql
tb_user_info          -- 사용자 정보
tb_user_profile       -- 사용자 프로필
tb_code_group         -- 코드 그룹
tb_code_detail        -- 코드 상세
tb_board_article      -- 게시판 게시글
tb_board_comment      -- 게시판 댓글
tb_file_attach        -- 파일 첨부
tb_menu_info          -- 메뉴 정보
tb_auth_role          -- 권한 역할
tb_auth_permission    -- 권한 권한
```

**구조:**
- `tb_`: 테이블 접두사 (한국 SI 표준)
- `메뉴`: 기능의 주요 도메인/메뉴 (user, board, code 등)
- `기능`: 구체적인 기능 (info, profile, article, comment 등)

---

### 패턴 2: 멀티 모듈 형식

```
tb_모듈_메뉴_기능
```

**사용 시기:**
- 멀티 모듈 프로젝트 (module-api, module-admin, module-batch 등)
- 모듈 간 테이블 구분이 필요한 경우
- 동일한 도메인이 여러 모듈에 존재하는 경우

**예시:**
```sql
-- 관리자 모듈 (module-admin)
tb_admin_user_info        -- 관리자 모듈 사용자 정보
tb_admin_auth_token       -- 관리자 모듈 인증 토큰
tb_admin_login_history    -- 관리자 모듈 로그인 이력

-- API 모듈 (module-api)
tb_api_user_info          -- API 모듈 사용자 정보
tb_api_auth_token         -- API 모듈 인증 토큰
tb_api_request_log        -- API 모듈 요청 로그

-- 배치 모듈 (module-batch)
tb_batch_job_history      -- 배치 작업 이력
tb_batch_job_param        -- 배치 작업 파라미터
tb_batch_job_result       -- 배치 작업 결과
```

**구조:**
- `tb_`: 테이블 접두사
- `모듈`: 모듈명 (admin, api, batch, report 등)
- `메뉴`: 기능의 주요 도메인
- `기능`: 구체적인 기능

---

### 패턴 3: 시스템/공통 테이블

```
tb_system_*     -- 시스템 전반의 공통 기능
tb_common_*     -- 여러 모듈에서 공유하는 데이터
```

**사용 시기:**
- 모든 모듈에서 사용하는 공통 테이블
- 시스템 레벨의 설정/로그 테이블
- 마스터 데이터

**예시:**
```sql
-- 시스템 테이블 (tb_system_*)
tb_system_log            -- 시스템 접근 로그
tb_system_config         -- 시스템 설정
tb_system_error_log      -- 시스템 에러 로그
tb_system_scheduler      -- 시스템 스케줄러

-- 공통 테이블 (tb_common_*)
tb_common_code           -- 공통 코드
tb_common_code_group     -- 공통 코드 그룹
tb_common_message        -- 공통 메시지
tb_common_file           -- 공통 파일 정보
tb_common_attach         -- 공통 첨부파일
tb_common_region         -- 공통 지역 정보
```

**구분 기준:**
- `tb_system_*`: 시스템 운영/관리 목적 (로그, 설정, 모니터링)
- `tb_common_*`: 비즈니스 데이터 공유 목적 (코드, 메시지, 파일)

---

## 상세 규칙

### 1. 접두사 (Prefix)

| 접두사 | 용도 | 예시 |
|--------|------|------|
| `tb_` | 일반 테이블 (필수) | `tb_user_info` |
| `tv_` | 뷰 (View) | `tv_user_summary` |
| `tmp_` | 임시 테이블 | `tmp_data_migration` |

### 2. 대소문자 규칙

```sql
-- ✅ 올바른 예시 (모두 소문자, snake_case)
tb_user_info
tb_board_article
tb_system_log

-- ❌ 잘못된 예시
TB_USER_INFO          -- 대문자 사용
tb_UserInfo           -- camelCase 사용
tb_user-info          -- 하이픈 사용
```

### 3. 축약형 사용 원칙

**✅ 권장: 전체 단어 사용**
```sql
tb_user_information      -- information (O)
tb_system_log           -- system (O)
tb_admin_user_info      -- admin, user, info (O)
```

**△ 허용: 업계 표준 축약형**
```sql
tb_user_info            -- info (일반적으로 허용)
tb_auth_token           -- auth (일반적으로 허용)
tb_temp_data            -- temp (일반적으로 허용)
```

**❌ 지양: 과도한 축약형**
```sql
tb_usr_inf              -- 이해하기 어려움
tb_sys_log              -- system → sys (지양, 하지만 짧은 경우 허용 가능)
tb_adm_usr_inf          -- 너무 많은 축약
```

### 4. 복수형 vs 단수형

**✅ 권장: 단수형 사용**
```sql
tb_user_info            -- user (단수)
tb_board_article        -- article (단수)
tb_file_attach          -- file (단수)
```

**이유:**
- 일관성 유지 용이
- 한국어 명사는 복수형 구분이 없음
- ORM Entity 클래스명과 일치

### 5. 연결 테이블 (Many-to-Many)

**패턴:**
```
tb_{테이블1}_{테이블2}_mapping
또는
tb_{테이블1}_{테이블2}_rel
```

**예시:**
```sql
-- 사용자-역할 매핑
tb_user_role_mapping
tb_user_role_rel

-- 게시글-태그 매핑
tb_article_tag_mapping
tb_article_tag_rel

-- 메뉴-권한 매핑
tb_menu_permission_mapping
```

---

## 예시

### 예시 1: 사용자 관리 시스템

```sql
-- 기본 형식 (단일 모듈)
tb_user_info              -- 사용자 기본 정보
tb_user_profile           -- 사용자 프로필
tb_user_preference        -- 사용자 환경설정
tb_user_login_history     -- 로그인 이력
tb_user_role_mapping      -- 사용자-역할 매핑

-- 멀티 모듈 형식
tb_admin_user_info        -- 관리자 모듈 사용자
tb_api_user_info          -- API 모듈 사용자
```

### 예시 2: 게시판 시스템

```sql
-- 기본 형식
tb_board_category         -- 게시판 카테고리
tb_board_article          -- 게시글
tb_board_comment          -- 댓글
tb_board_like             -- 좋아요
tb_board_attach           -- 첨부파일

-- 계층 구조가 있는 경우
tb_board_category         -- 1단계: 카테고리
tb_board_article          -- 2단계: 게시글
tb_board_comment          -- 3단계: 댓글 (게시글의 하위)
tb_board_comment_reply    -- 4단계: 대댓글 (댓글의 하위)
```

### 예시 3: 권한 관리 시스템

```sql
-- 역할 기반 접근 제어 (RBAC)
tb_auth_role              -- 역할 (Role)
tb_auth_permission        -- 권한 (Permission)
tb_auth_role_permission_mapping   -- 역할-권한 매핑
tb_auth_user_role_mapping         -- 사용자-역할 매핑

-- 모듈별 권한 관리
tb_admin_auth_role        -- 관리자 모듈 역할
tb_api_auth_role          -- API 모듈 역할
```

### 예시 4: 배치 시스템

```sql
-- 배치 작업 관리
tb_batch_job_info         -- 배치 작업 정보
tb_batch_job_param        -- 배치 작업 파라미터
tb_batch_job_history      -- 배치 실행 이력
tb_batch_job_result       -- 배치 실행 결과
tb_batch_job_error_log    -- 배치 에러 로그
```

---

## 안티 패턴

### ❌ 피해야 할 패턴

#### 1. 너무 긴 이름
```sql
-- ❌ 나쁜 예
tb_user_information_management_system_table

-- ✅ 좋은 예
tb_user_info
```

#### 2. 불명확한 축약
```sql
-- ❌ 나쁜 예
tb_usr_mgmt
tb_sys_cfg
tb_dt_prc

-- ✅ 좋은 예
tb_user_management
tb_system_config
tb_data_process
```

#### 3. 일관성 없는 명명
```sql
-- ❌ 나쁜 예 (혼재)
tb_user_info          -- snake_case
tb_UserProfile        -- PascalCase
tb_user-preference    -- kebab-case

-- ✅ 좋은 예 (일관된 snake_case)
tb_user_info
tb_user_profile
tb_user_preference
```

#### 4. 모호한 이름
```sql
-- ❌ 나쁜 예
tb_data
tb_info
tb_temp
tb_table1

-- ✅ 좋은 예
tb_user_data
tb_product_info
tb_temp_migration
tb_user_backup
```

#### 5. 예약어 사용
```sql
-- ❌ 피해야 할 예약어
tb_user_order         -- ORDER는 SQL 예약어
tb_user_select        -- SELECT는 SQL 예약어
tb_user_index         -- INDEX는 SQL 예약어

-- ✅ 좋은 예
tb_user_order_info
tb_user_selection
tb_user_index_info
```

---

## 체크리스트

테이블 생성 전 다음 항목을 확인하세요:

### 명명 규칙 확인

- [ ] `tb_` 접두사를 사용했는가?
- [ ] 모두 소문자 + snake_case를 사용했는가?
- [ ] 프로젝트 패턴(기본/멀티모듈/시스템)을 따랐는가?
- [ ] 의미 있는 이름을 사용했는가?

### 가독성 확인

- [ ] 팀원이 테이블명만 보고 용도를 이해할 수 있는가?
- [ ] 과도한 축약형을 사용하지 않았는가?
- [ ] 테이블명 길이가 적절한가? (3~5 단어)

### 일관성 확인

- [ ] 기존 테이블 명명 규칙과 일관성이 있는가?
- [ ] 유사한 기능의 테이블과 패턴이 일치하는가?

### 확장성 확인

- [ ] 향후 기능 추가 시 명명 충돌이 없는가?
- [ ] 멀티 모듈 환경에서 명확한 구분이 가능한가?

---

## 참고 자료

### 관련 문서
- [tb_system_log.sql](tb_system_log.sql) - 시스템 로그 테이블 DDL (명명 컨벤션 예시)

### 외부 참고
- [MySQL 테이블 명명 규칙 가이드](https://dev.mysql.com/doc/)
- [PostgreSQL 스타일 가이드](https://www.postgresql.org/docs/)
- Google SQL Style Guide
- 한국 SI 업계 표준 명명 규칙

---

## 버전 이력

| 버전 | 날짜 | 변경 내역 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2026-02-17 | 초기 작성 | Claude Code |

---

## 문의 및 제안

테이블 명명 규칙에 대한 문의사항이나 개선 제안은 팀 리드에게 문의하세요.

**이 문서는 프로젝트 전체의 일관성을 위해 작성되었습니다. 새로운 테이블 생성 시 반드시 이 규칙을 따라주세요.**
