-- 적금 상품 테이블 생성
CREATE TABLE savings_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('DEPOSIT', 'BOND') NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate_apy DECIMAL(5,2) NOT NULL,  -- 연간 수익률 (%)
    tenor_days INT NOT NULL,         -- 만기일수
    min_amount DECIMAL(15,2) NOT NULL DEFAULT 10000,
    max_amount DECIMAL(15,2) NOT NULL DEFAULT 100000000,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_savings_product_type (type),
    INDEX idx_savings_product_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 적금 포지션 테이블 생성
CREATE TABLE savings_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    principal DECIMAL(15,2) NOT NULL,      -- 원금
    start_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    accrued_interest DECIMAL(15,2) NOT NULL DEFAULT 0,  -- 누적 이자
    status ENUM('ACTIVE', 'MATURED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES savings_product(id) ON DELETE CASCADE,
    INDEX idx_savings_position_user (user_id),
    INDEX idx_savings_position_status (status),
    INDEX idx_savings_position_maturity (maturity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;