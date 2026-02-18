-- V2: 초기 샘플 데이터 삽입
-- 실행 날짜: 2025-02-13

-- 샘플 데이터 삽입
INSERT INTO TB_SAMPLE (TITLE, CONTENT, REG_USER_ID, REG_DATETIME) VALUES
('Flyway 마이그레이션 샘플 1', 'Flyway를 통해 관리되는 데이터입니다.', 'system', NOW()),
('Flyway 마이그레이션 샘플 2', '버전 관리되는 데이터베이스 스키마입니다.', 'system', NOW()),
('Flyway 마이그레이션 샘플 3', '모든 환경에서 동일한 데이터로 시작합니다.', 'system', NOW());
