-- Create company sync log table for individual company synchronization tracking
CREATE TABLE company_sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    previous_market_cap BIGINT NULL,
    updated_market_cap BIGINT NULL,
    change_percentage DECIMAL(5,2) NULL,
    error_message TEXT NULL,
    synced_at TIMESTAMP NOT NULL,
    sync_type VARCHAR(20) NOT NULL, -- MANUAL, SCHEDULED, API_TRIGGERED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_sync_log_symbol (symbol),
    INDEX idx_sync_log_synced_at (synced_at DESC),
    INDEX idx_sync_log_sync_type (sync_type),
    INDEX idx_sync_log_success (success),
    FOREIGN KEY (symbol) REFERENCES company(symbol) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create company sync batch log table for batch operation tracking
CREATE TABLE company_sync_batch_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_companies INT NOT NULL,
    success_count INT NOT NULL DEFAULT 0,
    failure_count INT NOT NULL DEFAULT 0,
    success_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    elapsed_time_millis BIGINT NOT NULL,
    sync_type VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MANUAL
    trigger_source VARCHAR(20) NOT NULL, -- SCHEDULER, API, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_batch_log_started_at (started_at DESC),
    INDEX idx_batch_log_sync_type (sync_type),
    INDEX idx_batch_log_trigger_source (trigger_source),
    INDEX idx_batch_log_success_rate (success_rate DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;