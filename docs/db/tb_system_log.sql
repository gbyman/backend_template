-- ============================================================================
-- 시스템 접근 로그 테이블
-- ============================================================================
--
-- 테이블명: tb_system_log
--
-- ============================================================================
-- 프로젝트 전체 테이블 명명 규칙 (Table Naming Convention)
-- ============================================================================
--
-- 1. 기본 형식: tb_메뉴_기능
--    예시:
--      tb_user_info         (사용자 정보)
--      tb_code_group        (코드 그룹)
--      tb_board_article     (게시판 게시글)
--      tb_file_attach       (파일 첨부)
--
-- 2. 멀티 모듈 형식: tb_모듈_메뉴_기능
--    예시:
--      tb_admin_user_info   (관리자 모듈 사용자 정보)
--      tb_api_auth_token    (API 모듈 인증 토큰)
--      tb_batch_job_history (배치 모듈 작업 이력)
--      tb_api_request_log   (API 모듈 요청 로그)
--
-- 3. 시스템/공통 테이블: tb_system_* 또는 tb_common_*
--    예시:
--      tb_system_log        (시스템 로그) ← 이 테이블
--      tb_system_config     (시스템 설정)
--      tb_common_code       (공통 코드)
--      tb_common_message    (공통 메시지)
--
-- 4. 명명 규칙:
--    - tb_ prefix: 한국 SI 업계 표준 (테이블 구분자)
--    - snake_case: 소문자 + 언더스코어
--    - 축약형 지양: system (O), sys (△)
--    - 명확성 우선: 가독성 > 간결성
--
-- 5. 이 테이블 명명 이유:
--    - tb_system_log: 시스템 전반의 공통 로그 테이블
--    - system: 전체 시스템 범위임을 명시
--    - log: 로그 데이터임을 명확하게 표현
--
-- 용도: API/페이지 접근 이력 감사, 에러 추적, 통계 분석
--
-- 파티션 전략: REQUEST_TIME 기준 월별 파티셔닝 (Range Partitioning)
--
-- 인덱스 전략:
--   - PRIMARY KEY (LOG_ID, REQUEST_TIME) 만 사용
--   - INSERT 성능 최우선 (일 1만 건 이상)
--   - 에러 추적: MDC + 로그 파일 사용
--   - DB 조회: 시간 범위 조건 필수 (파티션 프루닝)
--   - 필요 시 나중에 인덱스 추가 (하단 가이드 참고)
--
-- 로그 카테고리:
--   - PAGE_VIEW: 페이지 접속 (GET 메서드, /api/가 아닌 경로)
--   - API_CALL: API 호출 (모든 /api/** 경로)
--   - FILE_DOWNLOAD: 파일 다운로드
--   - AUTH_LOGIN: 로그인
--   - AUTH_LOGOUT: 로그아웃
--   - ERROR: 에러 발생 (STATUS_CODE >= 400)
--
-- 주의사항:
--   1. 파티션은 자동 생성되지 않으므로 주기적으로 추가 필요
--   2. 오래된 파티션은 DROP PARTITION으로 삭제 (데이터 아카이빙 후)
--   3. REQUEST_PARAMS는 JSON 형태로 저장
--
-- ============================================================================

CREATE TABLE IF NOT EXISTS tb_system_log
(
    LOG_ID              BIGINT AUTO_INCREMENT COMMENT '로그_ID',
    REQUEST_ID          VARCHAR(36)  NOT NULL COMMENT '요청_ID (UUID)',
    REQUEST_URI         VARCHAR(500) NOT NULL COMMENT '요청_URI',
    HTTP_METHOD         VARCHAR(10)  NOT NULL COMMENT 'HTTP_메서드',

    -- 로그 분류
    LOG_CATEGORY        ENUM('PAGE_VIEW', 'API_CALL', 'FILE_DOWNLOAD', 'AUTH_LOGIN', 'AUTH_LOGOUT', 'ERROR')
                        DEFAULT 'API_CALL' COMMENT '로그_카테고리',

    CLIENT_IP           VARCHAR(50)           COMMENT '클라이언트_IP',
    USER_ID             VARCHAR(100)          COMMENT '사용자_ID',

    -- 요청 파라미터 (JSON 형태)
    REQUEST_PARAMS      TEXT                  COMMENT '요청_파라미터 (JSON)',

    REQUEST_TIME        DATETIME     NOT NULL COMMENT '요청_시간',
    RESPONSE_TIME       DATETIME              COMMENT '응답_시간',
    EXECUTION_TIME_MS   INT                   COMMENT '실행_시간_밀리초',
    STATUS_CODE         INT                   COMMENT '응답_상태코드',
    ERROR_MESSAGE       TEXT                  COMMENT '에러_메시지',

    -- 공통 감사 필드
    REG_USER_ID         VARCHAR(50)  NOT NULL COMMENT '등록자_ID',
    REG_DATETIME        DATETIME     NOT NULL COMMENT '등록_일시',
    UPDATER_ID          VARCHAR(50)           COMMENT '수정자_ID',
    UPDATE_DATETIME     DATETIME              COMMENT '수정_일시',

    -- ========================================================================
    -- 인덱스 (INSERT 성능 최우선)
    -- ========================================================================
    -- 파티션 키를 포함한 Primary Key (인덱스 1개만)
    PRIMARY KEY (LOG_ID, REQUEST_TIME)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '시스템 접근 로그'
  -- ============================================================================
  -- 월별 파티셔닝 설정 (Range Partitioning by Days)
  -- ============================================================================
  -- REQUEST_TIME을 일(day) 단위로 변환하여 범위 파티셔닝
  --
  -- 파티션 전략:
  --   - 2026년: 월별 파티션 (12개)
  --   - 2027년: 월별 파티션 (12개)
  --   - p_max: 미래 데이터를 위한 최대값 파티션
  --
  -- 파티션 크기 예상:
  --   - 일 1만 건 기준: 월 30만 건 * 약 1KB = 약 300MB/월
  --   - 연 3.6GB, 3년 약 10GB
  --
  -- 성능 고려사항:
  --   - WHERE REQUEST_TIME BETWEEN ... 조건 시 파티션 프루닝 발생
  --   - 인덱스 크기도 파티션별로 분산되어 관리 용이
  --   - 월 단위 파티션으로 데이터 아카이빙 간편
  --
  -- ============================================================================
  PARTITION BY RANGE (TO_DAYS(REQUEST_TIME))
(
    -- 2026년 파티션 (월별)
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')) COMMENT '2026년 1월',
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')) COMMENT '2026년 2월',
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')) COMMENT '2026년 3월',
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')) COMMENT '2026년 4월',
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')) COMMENT '2026년 5월',
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')) COMMENT '2026년 6월',
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')) COMMENT '2026년 7월',
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')) COMMENT '2026년 8월',
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')) COMMENT '2026년 9월',
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')) COMMENT '2026년 10월',
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')) COMMENT '2026년 11월',
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')) COMMENT '2026년 12월',

    -- 2027년 파티션 (월별)
    PARTITION p202701 VALUES LESS THAN (TO_DAYS('2027-02-01')) COMMENT '2027년 1월',
    PARTITION p202702 VALUES LESS THAN (TO_DAYS('2027-03-01')) COMMENT '2027년 2월',
    PARTITION p202703 VALUES LESS THAN (TO_DAYS('2027-04-01')) COMMENT '2027년 3월',
    PARTITION p202704 VALUES LESS THAN (TO_DAYS('2027-05-01')) COMMENT '2027년 4월',
    PARTITION p202705 VALUES LESS THAN (TO_DAYS('2027-06-01')) COMMENT '2027년 5월',
    PARTITION p202706 VALUES LESS THAN (TO_DAYS('2027-07-01')) COMMENT '2027년 6월',
    PARTITION p202707 VALUES LESS THAN (TO_DAYS('2027-08-01')) COMMENT '2027년 7월',
    PARTITION p202708 VALUES LESS THAN (TO_DAYS('2027-09-01')) COMMENT '2027년 8월',
    PARTITION p202709 VALUES LESS THAN (TO_DAYS('2027-10-01')) COMMENT '2027년 9월',
    PARTITION p202710 VALUES LESS THAN (TO_DAYS('2027-11-01')) COMMENT '2027년 10월',
    PARTITION p202711 VALUES LESS THAN (TO_DAYS('2027-12-01')) COMMENT '2027년 11월',
    PARTITION p202712 VALUES LESS THAN (TO_DAYS('2028-01-01')) COMMENT '2027년 12월',

    -- 미래 데이터를 위한 최대값 파티션
    PARTITION p_max VALUES LESS THAN MAXVALUE COMMENT '미래 데이터'
);

-- ============================================================================
-- 자주 사용하는 쿼리 예시
-- ============================================================================

-- 1. 시간 범위로 조회 (파티션 프루닝 - 빠름)
-- SELECT * FROM tb_system_log
-- WHERE REQUEST_TIME BETWEEN '2026-02-01' AND '2026-02-28'
-- ORDER BY REQUEST_TIME DESC
-- LIMIT 100;

-- 2. 메뉴 접속 에러만 조회 (시간 범위 필수)
-- SELECT * FROM tb_system_log
-- WHERE STATUS_CODE >= 400
--   AND LOG_CATEGORY = 'PAGE_VIEW'
--   AND REQUEST_TIME >= '2026-02-01'
--   AND REQUEST_TIME < '2026-03-01'
-- ORDER BY REQUEST_TIME DESC;

-- 3. API 호출 에러만 조회
-- SELECT * FROM tb_system_log
-- WHERE STATUS_CODE >= 400
--   AND LOG_CATEGORY = 'API_CALL'
--   AND REQUEST_TIME >= '2026-02-01'
--   AND REQUEST_TIME < '2026-03-01'
-- ORDER BY REQUEST_TIME DESC;

-- 4. 카테고리별 에러 통계
-- SELECT
--     LOG_CATEGORY,
--     COUNT(*) as error_count,
--     AVG(EXECUTION_TIME_MS) as avg_time,
--     MAX(EXECUTION_TIME_MS) as max_time
-- FROM tb_sys_log
-- WHERE STATUS_CODE >= 400
--   AND REQUEST_TIME >= '2026-02-01'
--   AND REQUEST_TIME < '2026-03-01'
-- GROUP BY LOG_CATEGORY;

-- 5. 느린 API 조회 (실행 시간 2초 이상)
-- SELECT
--     REQUEST_URI,
--     AVG(EXECUTION_TIME_MS) as avg_time,
--     COUNT(*) as call_count
-- FROM tb_sys_log
-- WHERE EXECUTION_TIME_MS >= 2000
--   AND REQUEST_TIME >= '2026-02-01'
--   AND REQUEST_TIME < '2026-03-01'
-- GROUP BY REQUEST_URI
-- ORDER BY avg_time DESC
-- LIMIT 10;

-- ============================================================================
-- 파티션 관리 스크립트
-- ============================================================================

-- 새로운 파티션 추가 (매월 실행)
-- ALTER TABLE tb_system_log ADD PARTITION (
--   PARTITION p202801 VALUES LESS THAN (TO_DAYS('2028-02-01')) COMMENT '2028년 1월'
-- );

-- 오래된 파티션 삭제 (3년 이상 데이터, 아카이빙 후)
-- ALTER TABLE tb_system_log DROP PARTITION p202401;

-- 파티션 정보 조회
-- SELECT
--     PARTITION_NAME,
--     PARTITION_DESCRIPTION,
--     PARTITION_COMMENT,
--     TABLE_ROWS,
--     ROUND(DATA_LENGTH / 1024 / 1024, 2) as DATA_MB,
--     ROUND(INDEX_LENGTH / 1024 / 1024, 2) as INDEX_MB
-- FROM INFORMATION_SCHEMA.PARTITIONS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME = 'tb_system_log'
-- ORDER BY PARTITION_ORDINAL_POSITION;

-- ============================================================================
-- 필요 시 인덱스 추가 (ALTER TABLE)
-- ============================================================================
--
-- 현재 전략: PK만 사용 (INSERT 성능 최우선)
-- 에러 추적: MDC + 로그 파일 사용
-- DB 조회: 시간 범위 조건 필수 (파티션 프루닝)
--
-- 사용 패턴 변경 시 아래 인덱스를 선택적으로 추가하세요.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. REQUEST_ID 인덱스 (요청 추적용)
-- ----------------------------------------------------------------------------
-- ALTER TABLE tb_system_log ADD INDEX idx_request_id (REQUEST_ID);
--
-- [용도]
--   - requestId로 특정 요청의 전체 로그 추적
--   - 에러 발생 시 해당 요청의 모든 로그 조회
--   - MDC + 로그 파일 방식보다 DB 조회 선호 시
--
-- [성능 개선]
--   - 조회 속도: Full Scan 5~25초 → < 1ms (약 25,000배 빠름)
--   - 파티션 프루닝 없이도 빠른 조회 가능
--
-- [비용]
--   - INSERT 성능: 약 10~15% 저하
--   - 디스크 공간: 약 +20% (인덱스 크기)
--   - 메모리: 인덱스 캐싱에 추가 메모리 필요
--
-- [사용 예시]
--   SELECT * FROM tb_sys_log
--   WHERE REQUEST_ID = 'a1b2c3d4-uuid-here'
--   ORDER BY REQUEST_TIME;
--
-- [추가 시기]
--   - 에러 발생 시 requestId로 추적이 빈번한 경우
--   - MDC + 로그 파일 방식이 불편한 경우
--   - 로그 파일 로테이션으로 과거 로그 접근 어려운 경우
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 2. 에러 검색 복합 인덱스 (에러 모니터링용)
-- ----------------------------------------------------------------------------
-- ALTER TABLE tb_system_log ADD INDEX idx_error_search (STATUS_CODE, LOG_CATEGORY, REQUEST_TIME);
--
-- [용도]
--   - 카테고리별 에러 통계 (PAGE_VIEW vs API_CALL)
--   - 실시간 에러 모니터링 대시보드
--   - 에러율 계산 및 알림
--
-- [성능 개선]
--   - 조회 속도: Full Scan 5~60초 → 0.1~1초 (약 50~600배 빠름)
--   - WHERE STATUS_CODE >= 400 조건 최적화
--
-- [비용]
--   - INSERT 성능: 약 15~20% 저하
--   - 디스크 공간: 약 +30% (복합 인덱스 크기)
--
-- [사용 예시]
--   -- 메뉴 접속 에러만 조회
--   SELECT * FROM tb_sys_log
--   WHERE STATUS_CODE >= 400
--     AND LOG_CATEGORY = 'PAGE_VIEW'
--     AND REQUEST_TIME >= '2026-02-01'
--   ORDER BY REQUEST_TIME DESC;
--
--   -- 카테고리별 에러 통계
--   SELECT
--       LOG_CATEGORY,
--       COUNT(*) as error_count,
--       AVG(EXECUTION_TIME_MS) as avg_time
--   FROM tb_sys_log
--   WHERE STATUS_CODE >= 400
--     AND REQUEST_TIME >= '2026-02-01'
--   GROUP BY LOG_CATEGORY;
--
-- [추가 시기]
--   - 에러 모니터링 대시보드 구축 시
--   - 실시간 알림 시스템 필요 시
--   - 카테고리별 에러율 통계 필요 시
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 3. 사용자별 접근 이력 인덱스
-- ----------------------------------------------------------------------------
-- ALTER TABLE tb_system_log ADD INDEX idx_user_id (USER_ID);
--
-- [용도]
--   - 특정 사용자의 접근 이력 조회
--   - 사용자 행위 분석
--   - 보안 감사 (특정 사용자 추적)
--
-- [성능 개선]
--   - 조회 속도: Full Scan → 인덱스 스캔
--
-- [비용]
--   - INSERT 성능: 약 10~15% 저하
--   - 디스크 공간: 약 +20%
--
-- [사용 예시]
--   SELECT * FROM tb_sys_log
--   WHERE USER_ID = 'user123'
--     AND REQUEST_TIME >= '2026-02-01'
--   ORDER BY REQUEST_TIME DESC;
--
-- [추가 시기]
--   - 사용자별 접근 이력 조회가 빈번한 경우
--   - 보안 감사 요구사항이 있는 경우
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 인덱스 추가 가이드
-- ----------------------------------------------------------------------------
--
-- [단계별 추가 전략]
-- 1단계: PK만 사용 (현재)
--   → INSERT 성능 최우선
--   → 조회: 시간 범위 조건 필수
--
-- 2단계: REQUEST_ID 인덱스 추가
--   → 요청 추적 필요 시
--   → ALTER TABLE tb_sys_log ADD INDEX idx_request_id (REQUEST_ID);
--
-- 3단계: 에러 검색 복합 인덱스 추가
--   → 에러 모니터링 필요 시
--   → ALTER TABLE tb_sys_log ADD INDEX idx_error_search (STATUS_CODE, LOG_CATEGORY, REQUEST_TIME);
--
-- [주의사항]
-- - 인덱스는 한 번에 하나씩 추가하고 성능 모니터링
-- - 사용하지 않는 인덱스는 과감히 삭제 (DROP INDEX)
-- - 파티션 테이블은 인덱스도 파티션별로 관리됨
--
-- [인덱스 삭제]
-- ALTER TABLE tb_sys_log DROP INDEX idx_request_id;
-- ALTER TABLE tb_sys_log DROP INDEX idx_error_search;
-- ALTER TABLE tb_sys_log DROP INDEX idx_user_id;
--
-- ----------------------------------------------------------------------------
