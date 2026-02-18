-- Repeatable Migration: 샘플 저장 프로시저
-- 이 파일은 내용이 변경될 때마다 재실행됩니다.

-- 샘플 통계 조회 함수
CREATE OR REPLACE FUNCTION get_sample_statistics()
RETURNS TABLE (
    total_count BIGINT,
    active_count BIGINT,
    inactive_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(*)::BIGINT as total_count,
        COUNT(CASE WHEN USE_YN = 'Y' THEN 1 END)::BIGINT as active_count,
        COUNT(CASE WHEN USE_YN = 'N' THEN 1 END)::BIGINT as inactive_count
    FROM TB_SAMPLE;
END;
$$ LANGUAGE plpgsql;

-- 사용 예시:
-- SELECT * FROM get_sample_statistics();
