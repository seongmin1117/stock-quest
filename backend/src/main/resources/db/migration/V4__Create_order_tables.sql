-- 주문 테이블 생성
CREATE TABLE order_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    instrument_key CHAR(1) NOT NULL,
    side ENUM('BUY', 'SELL') NOT NULL,
    quantity DECIMAL(15,4) NOT NULL,
    order_type ENUM('MARKET', 'LIMIT') NOT NULL,
    limit_price DECIMAL(10,2) NULL,
    executed_price DECIMAL(10,2) NULL,
    slippage_rate DECIMAL(5,2) DEFAULT 0.00,
    status ENUM('PENDING', 'EXECUTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP NULL,
    
    FOREIGN KEY (session_id) REFERENCES challenge_session(id) ON DELETE CASCADE,
    INDEX idx_order_session (session_id),
    INDEX idx_order_status (status),
    INDEX idx_order_instrument (instrument_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 포트폴리오 포지션 테이블 생성
CREATE TABLE portfolio_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    instrument_key CHAR(1) NOT NULL,
    quantity DECIMAL(15,4) NOT NULL DEFAULT 0,
    average_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_cost DECIMAL(15,2) NOT NULL DEFAULT 0,
    
    FOREIGN KEY (session_id) REFERENCES challenge_session(id) ON DELETE CASCADE,
    UNIQUE KEY uk_position_session_instrument (session_id, instrument_key),
    INDEX idx_position_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;