-- ShedLock 테이블 (분산 락)
-- 서버 이중화 환경에서 스케줄 작업 중복 실행 방지

CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,        -- 락 이름 (작업 식별자)
    lock_until TIMESTAMP NOT NULL,    -- 락 만료 시각
    locked_at TIMESTAMP NOT NULL,     -- 락 획득 시각
    locked_by VARCHAR(255) NOT NULL,  -- 락을 획득한 서버 (hostname)
    PRIMARY KEY (name)
);

-- 인덱스 (성능 향상)
CREATE INDEX idx_shedlock_lock_until ON shedlock(lock_until);

-- 테이블 코멘트
COMMENT ON TABLE shedlock IS 'ShedLock 분산 락 테이블 - 스케줄 작업 중복 실행 방지';
COMMENT ON COLUMN shedlock.name IS '락 이름 (작업 고유 식별자)';
COMMENT ON COLUMN shedlock.lock_until IS '락 만료 시각 (이 시각 이후 다른 서버가 락 획득 가능)';
COMMENT ON COLUMN shedlock.locked_at IS '락을 획득한 시각';
COMMENT ON COLUMN shedlock.locked_by IS '락을 획득한 서버 식별자 (hostname 등)';
