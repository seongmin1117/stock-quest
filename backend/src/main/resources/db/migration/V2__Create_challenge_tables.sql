-- 챌린지 테이블 생성
CREATE TABLE challenge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    speed_factor INT NOT NULL DEFAULT 10,
    status ENUM('DRAFT', 'ACTIVE', 'COMPLETED') NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_challenge_status (status),
    INDEX idx_challenge_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 챌린지 상품 테이블 생성
CREATE TABLE challenge_instrument (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id BIGINT NOT NULL,
    instrument_key CHAR(1) NOT NULL,
    actual_ticker VARCHAR(10) NOT NULL,
    hidden_name VARCHAR(100) NOT NULL,
    actual_name VARCHAR(100) NOT NULL,
    type ENUM('STOCK', 'DEPOSIT', 'BOND') NOT NULL DEFAULT 'STOCK',
    
    FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    UNIQUE KEY uk_challenge_instrument_key (challenge_id, instrument_key),
    INDEX idx_challenge_instrument_ticker (actual_ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;