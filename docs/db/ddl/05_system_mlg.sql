-- ============================================
-- 다국어 관리 (Multilingual Management)
-- ============================================
-- 작성일: 2026-02-18
-- 설명: 다국어 그룹 및 상세 관리
-- 테이블: tb_system_mlg_group, tb_system_mlg_detail
--
-- 설계 전략:
-- 1. 시스템 기본 코드 (is_system = 'Y')
--    - mlg_code: 의미있는 코드 (예: MLG_USE_Y, MLG_NOTICE)
--    - 초기 데이터로 제공, 삭제/수정 제한
--    - Java 코드에서 하드코딩 가능
--
-- 2. 사용자 추가 코드 (is_system = 'N')
--    - mlg_code: 자동 채번 (예: mlg00001, mlg00002)
--    - 프론트에서 mlg_name만 입력, mlg_code는 백엔드 자동 생성
--    - 사용자 입력 편의성 우선
-- ============================================

-- ============================================
-- 1. tb_system_mlg_group (다국어 그룹)
-- ============================================

CREATE TABLE tb_system_mlg_group (
    -- 기본 키
    mlg_group_id        SERIAL PRIMARY KEY,

    -- 다국어 정보
    mlg_code            VARCHAR(50) NOT NULL,
    mlg_name            VARCHAR(100) NOT NULL,
    description         TEXT,
    is_system           VARCHAR(1) NOT NULL DEFAULT 'N',

    -- 공통 컬럼 (BaseEntity)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    use_yn              VARCHAR(1) NOT NULL DEFAULT 'Y',

    -- 제약 조건
    CONSTRAINT uk_mlg_group_code UNIQUE (mlg_code),
    CONSTRAINT ck_mlg_group_use_yn CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_mlg_group_is_system CHECK (is_system IN ('Y', 'N'))
);

-- 코멘트
COMMENT ON TABLE tb_system_mlg_group IS '시스템관리 다국어 그룹';
COMMENT ON COLUMN tb_system_mlg_group.mlg_group_id IS '다국어 그룹 ID (PK)';
COMMENT ON COLUMN tb_system_mlg_group.mlg_code IS '다국어 코드 (시스템: MLG_USE_Y, 사용자: mlg00001)';
COMMENT ON COLUMN tb_system_mlg_group.mlg_name IS '다국어 명칭 (예: 사용, 공지사항, 관리자)';
COMMENT ON COLUMN tb_system_mlg_group.description IS '다국어 그룹 설명';
COMMENT ON COLUMN tb_system_mlg_group.is_system IS '시스템 코드 여부 (Y: 시스템 기본 코드, N: 사용자 추가 코드)';
COMMENT ON COLUMN tb_system_mlg_group.created_at IS '생성 일시';
COMMENT ON COLUMN tb_system_mlg_group.updated_at IS '수정 일시';
COMMENT ON COLUMN tb_system_mlg_group.created_by IS '생성자 ID';
COMMENT ON COLUMN tb_system_mlg_group.updated_by IS '수정자 ID';
COMMENT ON COLUMN tb_system_mlg_group.use_yn IS '사용 여부 (Y: 사용, N: 미사용)';


-- ============================================
-- 2. tb_system_mlg_detail (다국어 상세)
-- ============================================

CREATE TABLE tb_system_mlg_detail (
    -- 기본 키
    mlg_detail_id       SERIAL PRIMARY KEY,

    -- 외래 키
    mlg_group_id        INTEGER NOT NULL,

    -- 다국어 정보
    lang_code           VARCHAR(10) NOT NULL,
    mlg_text            TEXT NOT NULL,
    description         TEXT,

    -- 정렬
    sort_order          INTEGER NOT NULL DEFAULT 0,

    -- 공통 컬럼 (BaseEntity)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    use_yn              VARCHAR(1) NOT NULL DEFAULT 'Y',

    -- 제약 조건
    CONSTRAINT fk_mlg_detail_group FOREIGN KEY (mlg_group_id)
        REFERENCES tb_system_mlg_group(mlg_group_id) ON DELETE CASCADE,
    CONSTRAINT uk_mlg_detail_group_lang UNIQUE (mlg_group_id, lang_code),
    CONSTRAINT ck_mlg_detail_use_yn CHECK (use_yn IN ('Y', 'N'))
);

-- 코멘트
COMMENT ON TABLE tb_system_mlg_detail IS '시스템관리 다국어 상세';
COMMENT ON COLUMN tb_system_mlg_detail.mlg_detail_id IS '다국어 상세 ID (PK)';
COMMENT ON COLUMN tb_system_mlg_detail.mlg_group_id IS '다국어 그룹 ID (FK → tb_system_mlg_group)';
COMMENT ON COLUMN tb_system_mlg_detail.lang_code IS '언어 코드 (ko: 한국어, en: 영어, ja: 일본어, zh: 중국어 등)';
COMMENT ON COLUMN tb_system_mlg_detail.mlg_text IS '다국어 텍스트';
COMMENT ON COLUMN tb_system_mlg_detail.description IS '다국어 상세 설명';
COMMENT ON COLUMN tb_system_mlg_detail.sort_order IS '정렬 순서 (낮은 값이 먼저 표시)';
COMMENT ON COLUMN tb_system_mlg_detail.created_at IS '생성 일시';
COMMENT ON COLUMN tb_system_mlg_detail.updated_at IS '수정 일시';
COMMENT ON COLUMN tb_system_mlg_detail.created_by IS '생성자 ID';
COMMENT ON COLUMN tb_system_mlg_detail.updated_by IS '수정자 ID';
COMMENT ON COLUMN tb_system_mlg_detail.use_yn IS '사용 여부 (Y: 사용, N: 미사용)';


-- ============================================
-- 초기 데이터 (샘플)
-- ============================================

-- 다국어 그룹 샘플 (시스템 기본 코드 - 의미있는 코드 사용)
INSERT INTO tb_system_mlg_group (mlg_code, mlg_name, description, is_system, created_by) VALUES
('MLG_USE_Y', '사용', '사용 여부 - 사용', 'Y', 'SYSTEM'),
('MLG_USE_N', '미사용', '사용 여부 - 미사용', 'Y', 'SYSTEM'),
('MLG_NOTICE', '공지사항', '게시판 유형 - 공지사항', 'Y', 'SYSTEM'),
('MLG_FAQ', 'FAQ', '게시판 유형 - FAQ', 'Y', 'SYSTEM'),
('MLG_QNA', 'Q&A', '게시판 유형 - Q&A', 'Y', 'SYSTEM'),
('MLG_ACTIVE', '활성', '사용자 상태 - 활성', 'Y', 'SYSTEM'),
('MLG_INACTIVE', '비활성', '사용자 상태 - 비활성', 'Y', 'SYSTEM'),
('MLG_PENDING', '승인대기', '사용자 상태 - 승인대기', 'Y', 'SYSTEM'),
('MLG_LOCKED', '잠김', '사용자 상태 - 잠김', 'Y', 'SYSTEM'),
('MLG_ADMIN', '관리자', '권한 유형 - 관리자', 'Y', 'SYSTEM'),
('MLG_USER', '사용자', '권한 유형 - 사용자', 'Y', 'SYSTEM'),
('MLG_GUEST', '게스트', '권한 유형 - 게스트', 'Y', 'SYSTEM');

-- 다국어 상세 샘플 (MLG_USE_Y)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(1, 'ko', '사용', 1, 'SYSTEM'),
(1, 'en', 'Use', 2, 'SYSTEM'),
(1, 'ja', '使用', 3, 'SYSTEM'),
(1, 'zh', '使用', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_USE_N)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(2, 'ko', '미사용', 1, 'SYSTEM'),
(2, 'en', 'Not Use', 2, 'SYSTEM'),
(2, 'ja', '未使用', 3, 'SYSTEM'),
(2, 'zh', '未使用', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_NOTICE)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(3, 'ko', '공지사항', 1, 'SYSTEM'),
(3, 'en', 'Notice', 2, 'SYSTEM'),
(3, 'ja', 'お知らせ', 3, 'SYSTEM'),
(3, 'zh', '公告', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_FAQ)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(4, 'ko', 'FAQ', 1, 'SYSTEM'),
(4, 'en', 'FAQ', 2, 'SYSTEM'),
(4, 'ja', 'よくある質問', 3, 'SYSTEM'),
(4, 'zh', '常见问题', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_QNA)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(5, 'ko', 'Q&A', 1, 'SYSTEM'),
(5, 'en', 'Q&A', 2, 'SYSTEM'),
(5, 'ja', 'Q&A', 3, 'SYSTEM'),
(5, 'zh', '问答', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_ACTIVE)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(6, 'ko', '활성', 1, 'SYSTEM'),
(6, 'en', 'Active', 2, 'SYSTEM'),
(6, 'ja', '有効', 3, 'SYSTEM'),
(6, 'zh', '激活', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_INACTIVE)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(7, 'ko', '비활성', 1, 'SYSTEM'),
(7, 'en', 'Inactive', 2, 'SYSTEM'),
(7, 'ja', '無効', 3, 'SYSTEM'),
(7, 'zh', '停用', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_PENDING)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(8, 'ko', '승인대기', 1, 'SYSTEM'),
(8, 'en', 'Pending', 2, 'SYSTEM'),
(8, 'ja', '承認待ち', 3, 'SYSTEM'),
(8, 'zh', '等待批准', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_LOCKED)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(9, 'ko', '잠김', 1, 'SYSTEM'),
(9, 'en', 'Locked', 2, 'SYSTEM'),
(9, 'ja', 'ロック', 3, 'SYSTEM'),
(9, 'zh', '锁定', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_ADMIN)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(10, 'ko', '관리자', 1, 'SYSTEM'),
(10, 'en', 'Administrator', 2, 'SYSTEM'),
(10, 'ja', '管理者', 3, 'SYSTEM'),
(10, 'zh', '管理员', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_USER)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(11, 'ko', '사용자', 1, 'SYSTEM'),
(11, 'en', 'User', 2, 'SYSTEM'),
(11, 'ja', 'ユーザー', 3, 'SYSTEM'),
(11, 'zh', '用户', 4, 'SYSTEM');

-- 다국어 상세 샘플 (MLG_GUEST)
INSERT INTO tb_system_mlg_detail (mlg_group_id, lang_code, mlg_text, sort_order, created_by) VALUES
(12, 'ko', '게스트', 1, 'SYSTEM'),
(12, 'en', 'Guest', 2, 'SYSTEM'),
(12, 'ja', 'ゲスト', 3, 'SYSTEM'),
(12, 'zh', '访客', 4, 'SYSTEM');


-- ============================================
-- 권한 설정 (선택 사항)
-- ============================================

-- GRANT SELECT, INSERT, UPDATE, DELETE ON tb_system_mlg_group TO backend_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON tb_system_mlg_detail TO backend_user;
-- GRANT USAGE, SELECT ON SEQUENCE tb_system_mlg_group_mlg_group_id_seq TO backend_user;
-- GRANT USAGE, SELECT ON SEQUENCE tb_system_mlg_detail_mlg_detail_id_seq TO backend_user;
