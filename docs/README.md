# 📚 프로젝트 문서 목차

> **Backend Template** 프로젝트의 모든 문서를 한눈에 확인할 수 있습니다.

---

## 🏗️ 프로젝트 구조 및 설정

### [BUILD_GRADLE_REVIEW.md](BUILD_GRADLE_REVIEW.md)
- **목적:** Gradle 빌드 설정 검토 및 개선 사항
- **주요 내용:**
  - HIGH/MEDIUM 우선순위 개선 완료 (의존성 버전 통합, 중복 코드 제거, Windows 설정 통합)
  - QueryDSL, MapStruct 설정 최적화
  - 코드 품질 도구 통합 (Checkstyle, PMD, SpotBugs, Jacoco)
- **최종 수정:** 2026-02-17

---

## 🔐 보안 및 설정

### [JASYPT_USAGE.md](JASYPT_USAGE.md)
- **목적:** Jasypt를 이용한 설정 파일 암호화
- **주요 내용:**
  - 민감 정보 암호화 (DB 비밀번호, API 키 등)
  - 환경별 암호화 키 관리
  - 암호화/복호화 방법

### [LOGGING_MASKING.md](LOGGING_MASKING.md)
- **목적:** 로그 마스킹 설정 및 민감 정보 보호
- **주요 내용:**
  - 개인정보 마스킹 패턴 (주민번호, 전화번호, 카드번호 등)
  - Logback 설정
  - ELK Stack 연동 시 마스킹

### [CICD_SECRETS.md](CICD_SECRETS.md)
- **목적:** CI/CD 파이프라인에서 시크릿 관리
- **주요 내용:**
  - GitLab CI/CD 변수 설정
  - SonarQube, Black Duck 연동
  - 환경별 시크릿 분리

### [SESSION_CONTROL.md](SESSION_CONTROL.md)
- **목적:** 세션 관리 및 중복 로그인 제어
- **주요 내용:**
  - Redis 기반 세션 관리
  - 동시 세션 제어
  - 세션 타임아웃 설정

---

## 🧪 테스트 및 품질

### [TESTING.md](TESTING.md)
- **목적:** 테스트 전략 및 가이드
- **주요 내용:**
  - 단위 테스트, 통합 테스트 작성법
  - TestContainers 활용
  - Jacoco 커버리지 측정

### [CODE_STYLE.md](CODE_STYLE.md)
- **목적:** 코드 스타일 가이드
- **주요 내용:**
  - Google Java Style Guide 적용
  - Checkstyle, PMD 규칙
  - Spotless 자동 포맷팅

---

## 📊 인프라 및 배포

### [NEXUS_SETUP.md](NEXUS_SETUP.md)
- **목적:** Nexus Repository Manager 설정
- **주요 내용:**
  - Private Maven Repository 구성
  - 의존성 프록시 설정
  - 아티팩트 배포

### [ELK_SETUP.md](ELK_SETUP.md)
- **목적:** ELK Stack (Elasticsearch, Logstash, Kibana) 설정
- **주요 내용:**
  - 로그 수집 및 중앙화
  - Logstash Encoder 설정
  - Kibana 대시보드 구성

---

## 📋 작업 계획

### [TODO.md](TODO.md) ⭐ **NEW**
- **목적:** 현재 진행 중인 작업 및 TODO 목록 관리
- **주요 내용:**
  - 공통 테이블 마이그레이션 작업 현황
  - 16개 전환 대상 테이블 목록 및 우선순위
  - Phase별 작업 체크리스트
  - 완료된 작업 기록
- **작성일:** 2026-02-18

### [COMMON_TABLE_MIGRATION_PLAN.md](COMMON_TABLE_MIGRATION_PLAN.md)
- **목적:** 레거시 시스템 테이블을 공통 테이블로 전환 (상세 작업 계획)
- **주요 내용:**
  - 16개 시스템 관리 테이블 공통화 계획
  - 테이블 명명 규칙 변경 (`tb_system_*`)
  - Phase별 작업 계획 (DDL → Entity → Repository → Service)
  - 확장성 전략 (확장 테이블, JSON 컬럼)
- **작업 순서:**
  1. Phase 1: 테이블 설계 및 DDL 생성
  2. Phase 2: Entity 클래스 생성
  3. Phase 3: Repository 및 Service 구현
  4. Phase 4: module-api 통합
  5. Phase 5: 테스트 작성
- **작성일:** 2026-02-18

---

## 🗺️ 로드맵 및 계획

### [IMPROVEMENT_ROADMAP.md](IMPROVEMENT_ROADMAP.md)
- **목적:** 프로젝트 개선 로드맵
- **주요 내용:**
  - 단기/중기/장기 개선 계획
  - 기술 부채 관리
  - 성능 최적화 계획

---

## 🗄️ 데이터베이스

### [db/TABLE_NAMING_CONVENTION.md](db/TABLE_NAMING_CONVENTION.md)
- **목적:** 테이블 명명 규칙
- **주요 내용:**
  - 기본 형식: `tb_메뉴_기능`
  - 멀티 모듈: `tb_모듈_메뉴_기능`
  - 시스템/공통: `tb_system_*`, `tb_common_*`
  - 28개 실제 예시 및 안티패턴

---

## 📁 문서 구조

```
docs/
├── README.md                           # 📚 이 문서 (목차)
│
├── BUILD_GRADLE_REVIEW.md              # 🏗️ 빌드 설정 검토
├── CODE_STYLE.md                       # 📝 코드 스타일
├── TESTING.md                          # 🧪 테스트 가이드
│
├── JASYPT_USAGE.md                     # 🔐 암호화
├── LOGGING_MASKING.md                  # 🔐 로그 마스킹
├── SESSION_CONTROL.md                  # 🔐 세션 관리
├── CICD_SECRETS.md                     # 🔐 CI/CD 시크릿
│
├── NEXUS_SETUP.md                      # 📊 Nexus
├── ELK_SETUP.md                        # 📊 ELK Stack
│
├── TODO.md                             # 📋 현재 진행 중인 작업 ⭐ NEW
├── COMMON_TABLE_MIGRATION_PLAN.md      # 📋 공통 테이블 전환 계획
│
├── IMPROVEMENT_ROADMAP.md              # 🗺️ 개선 로드맵
│
└── db/                                 # 🗄️ 데이터베이스
    └── TABLE_NAMING_CONVENTION.md      # 테이블 명명 규칙
```

---

## 🔍 문서 검색 가이드

### 주제별 찾기

**빌드 및 설정**
- Gradle 설정: [BUILD_GRADLE_REVIEW.md](BUILD_GRADLE_REVIEW.md)
- 코드 품질: [CODE_STYLE.md](CODE_STYLE.md)

**보안**
- 암호화: [JASYPT_USAGE.md](JASYPT_USAGE.md)
- 로그 보안: [LOGGING_MASKING.md](LOGGING_MASKING.md)
- 세션: [SESSION_CONTROL.md](SESSION_CONTROL.md)

**작업 관리**
- 현재 작업: [TODO.md](TODO.md) ⭐ 진행 중인 작업 및 TODO 목록
- 작업 계획: [COMMON_TABLE_MIGRATION_PLAN.md](COMMON_TABLE_MIGRATION_PLAN.md)

**데이터베이스**
- 테이블 명명: [db/TABLE_NAMING_CONVENTION.md](db/TABLE_NAMING_CONVENTION.md)

**인프라**
- Nexus: [NEXUS_SETUP.md](NEXUS_SETUP.md)
- ELK: [ELK_SETUP.md](ELK_SETUP.md)
- CI/CD: [CICD_SECRETS.md](CICD_SECRETS.md)

---

## 📅 최근 업데이트

| 날짜 | 문서 | 변경 내용 |
|------|------|----------|
| 2026-02-18 | **TODO.md** | ⭐ **NEW** - 현재 진행 중인 작업 및 TODO 목록 관리 |
| 2026-02-18 | **루트 README.md** | 코드 품질 검사 섹션 추가, 문서 구조 업데이트, 간결화 |
| 2026-02-18 | **docs/README.md** | TODO.md 추가, 문서 목차 업데이트 |
| 2026-02-18 | **COMMON_TABLE_MIGRATION_PLAN.md** | 공통 테이블 전환 작업 계획 작성 |
| 2026-02-17 | BUILD_GRADLE_REVIEW.md | HIGH + MEDIUM 우선순위 개선 완료 |
| 2026-02-17 | db/TABLE_NAMING_CONVENTION.md | 테이블 명명 규칙 문서 추가 |

---

## 💡 문서 작성 가이드

새로운 문서를 추가할 때는 다음 형식을 따라주세요:

### 문서 템플릿
```markdown
# 문서 제목

> **작성일:** YYYY-MM-DD
> **목적:** 문서의 목적을 한 줄로
> **대상:** 누구를 위한 문서인지

---

## 📋 개요
(문서의 전반적인 내용 소개)

## 🎯 주요 내용
(핵심 내용을 섹션별로)

## 📝 참고 사항
(추가 정보, 링크 등)

---

**버전:** 1.0
**최종 수정:** YYYY-MM-DD
**작성자:** 작성자명
```

---

## 📧 문서 관련 문의

문서 내용에 대한 질문이나 개선 제안이 있으시면:
1. GitHub Issues에 등록
2. 프로젝트 담당자에게 문의
3. Pull Request로 직접 개선

---

**프로젝트:** Backend Template
**문서 버전:** 2.0
**최종 업데이트:** 2026-02-18
