# 현재 진행 중인 작업 (TODO)

> **작성일:** 2026-02-18
> **최종 수정:** 2026-02-18

---

## 📋 진행 중인 작업

### 🚧 공통 테이블 마이그레이션 (레거시 시스템 → Common Tables)

레거시 시스템의 16개 관리 테이블을 재사용 가능한 공통 테이블로 전환하는 작업이 진행 중입니다.

**상세 작업 계획:** [COMMON_TABLE_MIGRATION_PLAN.md](COMMON_TABLE_MIGRATION_PLAN.md)

---

## 🎯 전환 대상 테이블 (16개)

### 1. 권한 관리 (3개)
- `tb_system_auth` - 시스템 권한 관리
- `tb_system_group_auth` - 그룹 권한 관리
- `tb_system_request_assignee` - 권한 요청 담당자 관리

### 2. 사용자 관리 (2개)
- `tb_system_userinfo` - 사용자 정보 관리
- `tb_system_userinfo_test` - 테스트 사용자 정보 관리

### 3. 메뉴 관리 (1개)
- `tb_system_menu` - 시스템 메뉴 구조 관리

### 4. 코드 관리 (2개)
- `tb_system_code_group` - 공통코드 그룹 관리
- `tb_system_code_detail` - 공통코드 상세 관리

### 5. 다국어 관리 (2개)
- `tb_system_mlg_group` - 다국어 그룹 관리
- `tb_system_mlg_detail` - 다국어 상세 관리

### 6. 시스템 관리 (2개)
- `tb_system_log` - 시스템 로그 관리
- `tb_system_contact` - 담당자 연락처 관리

### 7. 파일/템플릿 관리 (2개)
- `tb_system_excel_template` - 엑셀 템플릿 관리
- `tb_system_sample_file` - 샘플 파일 관리

### 8. 게시판 관리 (2개)
- `tb_system_notice` - 공지사항 관리
- `tb_system_userguide` - 사용자 가이드 관리

---

## 📅 작업 단계 (5 Phase)

### Phase 1: DDL 설계 및 생성
- [ ] 16개 테이블 DDL 작성 (`tb_system_*`)
- [ ] 컬럼 설계 및 인덱스 최적화
- [ ] 외래키 관계 정의
- [ ] Flyway 마이그레이션 파일 생성

### Phase 2: Entity 클래스 생성
- [ ] `module-common/system/entity/` 패키지 생성
- [ ] BaseEntity 상속받는 16개 Entity 작성
- [ ] JPA 매핑 어노테이션 적용
- [ ] QueryDSL Q-class 생성 확인

### Phase 3: Repository 및 Service 구현
- [ ] `module-common/system/repository/` 패키지 생성
- [ ] JpaRepository 인터페이스 작성
- [ ] Custom Repository 구현 (필요 시)
- [ ] Service 레이어 구현 (기본 CRUD)

### Phase 4: module-api 통합
- [ ] Controller 작성 (REST API 엔드포인트)
- [ ] DTO 클래스 작성 (Request/Response)
- [ ] MapStruct Mapper 작성 (Entity ↔ DTO)
- [ ] Swagger 문서화

### Phase 5: 테스트 작성
- [ ] 단위 테스트 (Repository, Service)
- [ ] 통합 테스트 (Controller)
- [ ] TestContainers 활용
- [ ] 테스트 커버리지 70% 이상 달성

---

## 🗓️ 작업 우선순위

### Week 1 (우선순위 HIGH)
1. **코드 관리** (`code_group`, `code_detail`) - 가장 많이 사용되는 기능
2. **권한 관리** (`auth`, `group_auth`, `request_assignee`) - 핵심 보안 기능
3. **사용자 관리** (`userinfo`, `userinfo_test`) - 사용자 정보 관리

### Week 2 (우선순위 MEDIUM)
4. **메뉴 관리** (`menu`) - 시스템 메뉴 구조
5. **시스템 로그** (`log`) - 대용량 데이터, 성능 고려 필요
6. **다국어 관리** (`mlg_group`, `mlg_detail`) - 국제화 기능

### Week 3 (우선순위 LOW)
7. **게시판 관리** (`notice`, `userguide`) - 일반 게시판 기능
8. **파일 관리** (`excel_template`, `sample_file`) - 파일 관리 기능
9. **기타 시스템** (`contact`) - 담당자 연락처 관리

---

## 📚 참고 문서

- [COMMON_TABLE_MIGRATION_PLAN.md](COMMON_TABLE_MIGRATION_PLAN.md) - 상세 작업 계획
- [TABLE_NAMING_CONVENTION.md](db/TABLE_NAMING_CONVENTION.md) - 테이블 명명 규칙

---

## ✅ 완료된 작업

### 문서화
- ✅ 레거시 시스템 테이블 분석 완료
- ✅ 공통 테이블 전환 계획 문서 작성
- ✅ 테이블 명명 규칙 정리

### 빌드 설정
- ✅ Gradle 빌드 설정 최적화 (HIGH + MEDIUM 우선순위 완료)
- ✅ QueryDSL 설정 최적화
- ✅ MapStruct 설정 최적화
- ✅ Windows 환경 설정 통합
- ✅ 코드 품질 도구 통합 (Checkstyle, PMD, SpotBugs, Spotless, Jacoco)

---

## 🔜 다음 작업

1. **Phase 1 시작**: 코드 관리 테이블 DDL 작성
   - `tb_system_code_group`
   - `tb_system_code_detail`
2. **Flyway 마이그레이션 파일 생성**
3. **Entity 클래스 작성 (module-common)**

---

**문서 버전:** 1.0
**최종 업데이트:** 2026-02-18
**작성자:** Claude Code
