-- 시장 데이터 테이블 생성
CREATE TABLE price_candle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    open_price DECIMAL(10,2) NOT NULL,
    high_price DECIMAL(10,2) NOT NULL,
    low_price DECIMAL(10,2) NOT NULL,
    close_price DECIMAL(10,2) NOT NULL,
    volume BIGINT NOT NULL DEFAULT 0,
    timeframe ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'DAILY',
    
    UNIQUE KEY uk_price_candle_ticker_date (ticker, date, timeframe),
    INDEX idx_price_candle_ticker (ticker),
    INDEX idx_price_candle_date (date),
    INDEX idx_price_candle_ticker_date_range (ticker, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;