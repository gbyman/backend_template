-- ============================================
-- 코드 관리 (Code Management)
-- ============================================
-- 작성일: 2026-02-18
-- 설명: 공통코드 그룹 및 상세 관리
-- 테이블: tb_system_code_group, tb_system_code_detail
-- ============================================

-- ============================================
-- 1. tb_system_code_group (공통코드 그룹)
-- ============================================

CREATE TABLE tb_system_code_group (
    -- 기본 키
    code_group_id       SERIAL PRIMARY KEY,

    -- 코드 정보
    code_value          VARCHAR(50) NOT NULL,
    code_name           VARCHAR(100) NOT NULL,
    description         TEXT,

    -- 공통 컬럼 (BaseEntity)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    use_yn              VARCHAR(1) NOT NULL DEFAULT 'Y',

    -- 제약 조건
    CONSTRAINT uk_code_group_code_value UNIQUE (code_value),
    CONSTRAINT ck_code_group_use_yn CHECK (use_yn IN ('Y', 'N'))
);

-- 코멘트
COMMENT ON TABLE tb_system_code_group IS '시스템관리 공통코드 그룹';
COMMENT ON COLUMN tb_system_code_group.code_group_id IS '공통코드 그룹 ID (PK)';
COMMENT ON COLUMN tb_system_code_group.code_value IS '코드 값 (예: USER_STATUS, BOARD_TYPE, AUTH_TYPE)';
COMMENT ON COLUMN tb_system_code_group.code_name IS '코드 명 (예: 사용자상태, 게시판유형, 권한유형)';
COMMENT ON COLUMN tb_system_code_group.description IS '코드 그룹 설명';
COMMENT ON COLUMN tb_system_code_group.created_at IS '생성 일시';
COMMENT ON COLUMN tb_system_code_group.updated_at IS '수정 일시';
COMMENT ON COLUMN tb_system_code_group.created_by IS '생성자 ID';
COMMENT ON COLUMN tb_system_code_group.updated_by IS '수정자 ID';
COMMENT ON COLUMN tb_system_code_group.use_yn IS '사용 여부 (Y: 사용, N: 미사용)';


-- ============================================
-- 2. tb_system_code_detail (공통코드 상세)
-- ============================================

CREATE TABLE tb_system_code_detail (
    -- 기본 키
    code_detail_id      SERIAL PRIMARY KEY,

    -- 외래 키
    code_group_id       INTEGER NOT NULL,
    upper_code_id       INTEGER,

    -- 코드 정보
    code_value          VARCHAR(50) NOT NULL,
    code_name           VARCHAR(100) NOT NULL,
    mlg_code            VARCHAR(50),
    description         TEXT,

    -- 정렬 및 옵션
    sort_order          INTEGER NOT NULL DEFAULT 0,
    option1             VARCHAR(100),
    option2             VARCHAR(100),
    option3             VARCHAR(100),
    option4             VARCHAR(200),

    -- 공통 컬럼 (BaseEntity)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    use_yn              VARCHAR(1) NOT NULL DEFAULT 'Y',

    -- 제약 조건
    CONSTRAINT fk_code_detail_group FOREIGN KEY (code_group_id)
        REFERENCES tb_system_code_group(code_group_id) ON DELETE CASCADE,
    CONSTRAINT fk_code_detail_upper FOREIGN KEY (upper_code_id)
        REFERENCES tb_system_code_detail(code_detail_id) ON DELETE SET NULL,
    CONSTRAINT uk_code_detail_group_value UNIQUE (code_group_id, code_value),
    CONSTRAINT ck_code_detail_use_yn CHECK (use_yn IN ('Y', 'N'))
);

-- 코멘트
COMMENT ON TABLE tb_system_code_detail IS '시스템관리 공통코드 상세';
COMMENT ON COLUMN tb_system_code_detail.code_detail_id IS '공통코드 상세 ID (PK)';
COMMENT ON COLUMN tb_system_code_detail.code_group_id IS '공통코드 그룹 ID (FK → tb_system_code_group)';
COMMENT ON COLUMN tb_system_code_detail.upper_code_id IS '상위 코드 상세 ID (FK, 계층 구조 지원)';
COMMENT ON COLUMN tb_system_code_detail.code_value IS '상세 코드 값 (예: ACTIVE, INACTIVE, PENDING)';
COMMENT ON COLUMN tb_system_code_detail.code_name IS '상세 코드 명 (예: 활성, 비활성, 대기)';
COMMENT ON COLUMN tb_system_code_detail.mlg_code IS '다국어 코드 값 (tb_system_mlg_group 연동)';
COMMENT ON COLUMN tb_system_code_detail.description IS '코드 상세 설명';
COMMENT ON COLUMN tb_system_code_detail.sort_order IS '정렬 순서 (낮은 값이 먼저 표시)';
COMMENT ON COLUMN tb_system_code_detail.option1 IS '확장 옵션 필드 1 (프로젝트별 커스터마이징)';
COMMENT ON COLUMN tb_system_code_detail.option2 IS '확장 옵션 필드 2 (프로젝트별 커스터마이징)';
COMMENT ON COLUMN tb_system_code_detail.option3 IS '확장 옵션 필드 3 (프로젝트별 커스터마이징)';
COMMENT ON COLUMN tb_system_code_detail.option4 IS '확장 옵션 필드 4 (프로젝트별 커스터마이징)';
COMMENT ON COLUMN tb_system_code_detail.created_at IS '생성 일시';
COMMENT ON COLUMN tb_system_code_detail.updated_at IS '수정 일시';
COMMENT ON COLUMN tb_system_code_detail.created_by IS '생성자 ID';
COMMENT ON COLUMN tb_system_code_detail.updated_by IS '수정자 ID';
COMMENT ON COLUMN tb_system_code_detail.use_yn IS '사용 여부 (Y: 사용, N: 미사용)';


-- ============================================
-- 초기 데이터 (샘플)
-- ============================================

-- 공통코드 그룹 샘플
INSERT INTO tb_system_code_group (code_value, code_name, description, created_by) VALUES
('USE_YN', '사용여부', 'Y/N 사용 여부', 'SYSTEM'),
('BOARD_TYPE', '게시판유형', '게시판 유형 구분', 'SYSTEM'),
('USER_STATUS', '사용자상태', '사용자 상태 구분', 'SYSTEM'),
('AUTH_TYPE', '권한유형', '권한 유형 구분', 'SYSTEM');

-- 공통코드 상세 샘플 (USE_YN)
INSERT INTO tb_system_code_detail (code_group_id, code_value, code_name, mlg_code, sort_order, created_by) VALUES
(1, 'Y', '사용', 'MLG_USE_Y', 1, 'SYSTEM'),
(1, 'N', '미사용', 'MLG_USE_N', 2, 'SYSTEM');

-- 공통코드 상세 샘플 (BOARD_TYPE)
INSERT INTO tb_system_code_detail (code_group_id, code_value, code_name, mlg_code, sort_order, created_by) VALUES
(2, 'NOTICE', '공지사항', 'MLG_NOTICE', 1, 'SYSTEM'),
(2, 'FAQ', 'FAQ', 'MLG_FAQ', 2, 'SYSTEM'),
(2, 'QNA', 'Q&A', 'MLG_QNA', 3, 'SYSTEM');

-- 공통코드 상세 샘플 (USER_STATUS)
INSERT INTO tb_system_code_detail (code_group_id, code_value, code_name, mlg_code, sort_order, created_by) VALUES
(3, 'ACTIVE', '활성', 'MLG_ACTIVE', 1, 'SYSTEM'),
(3, 'INACTIVE', '비활성', 'MLG_INACTIVE', 2, 'SYSTEM'),
(3, 'PENDING', '승인대기', 'MLG_PENDING', 3, 'SYSTEM'),
(3, 'LOCKED', '잠김', 'MLG_LOCKED', 4, 'SYSTEM');

-- 공통코드 상세 샘플 (AUTH_TYPE)
INSERT INTO tb_system_code_detail (code_group_id, code_value, code_name, mlg_code, sort_order, created_by) VALUES
(4, 'ADMIN', '관리자', 'MLG_ADMIN', 1, 'SYSTEM'),
(4, 'USER', '사용자', 'MLG_USER', 2, 'SYSTEM'),
(4, 'GUEST', '게스트', 'MLG_GUEST', 3, 'SYSTEM');


-- ============================================
-- 권한 설정 (선택 사항)
-- ============================================

-- GRANT SELECT, INSERT, UPDATE, DELETE ON tb_system_code_group TO backend_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON tb_system_code_detail TO backend_user;
-- GRANT USAGE, SELECT ON SEQUENCE tb_system_code_group_code_group_id_seq TO backend_user;
-- GRANT USAGE, SELECT ON SEQUENCE tb_system_code_detail_code_detail_id_seq TO backend_user;
