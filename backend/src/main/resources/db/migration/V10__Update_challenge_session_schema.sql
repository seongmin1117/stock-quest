-- 챌린지 세션 테이블 스키마 업데이트

-- 컬럼명 변경 및 추가
ALTER TABLE challenge_session 
    CHANGE COLUMN seed_balance initial_balance DECIMAL(15,2) NOT NULL,
    ADD COLUMN return_rate DECIMAL(10,6) NULL,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHANGE COLUMN started_at started_at TIMESTAMP NULL,
    CHANGE COLUMN ended_at completed_at TIMESTAMP NULL;

-- 상태 ENUM 업데이트
ALTER TABLE challenge_session 
    MODIFY COLUMN status ENUM('READY', 'ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'READY';