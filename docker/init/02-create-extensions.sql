-- PostgreSQL Extensions 설치

-- UUID 생성 함수
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 전문 검색 (Full Text Search) - 필요시 사용
-- CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 통계 함수 확장
-- CREATE EXTENSION IF NOT EXISTS "tablefunc";

-- 확인 메시지
DO $$
BEGIN
    RAISE NOTICE '✅ PostgreSQL extensions installed successfully!';
END $$;
