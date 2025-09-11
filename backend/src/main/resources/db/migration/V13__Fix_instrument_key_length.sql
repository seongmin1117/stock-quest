-- Fix instrument_key column length to support ticker symbols like AAPL, MSFT, etc.
-- Current issue: instrument_key is CHAR(1) but application tries to store ticker symbols (4+ chars)
-- Also fix decimal precision mismatches between JPA entities and database schema

-- Update order_history table
ALTER TABLE order_history 
    MODIFY COLUMN instrument_key VARCHAR(10) NOT NULL,
    MODIFY COLUMN limit_price DECIMAL(12,4) NULL,
    MODIFY COLUMN executed_price DECIMAL(12,4) NULL;

-- Update portfolio_position table  
ALTER TABLE portfolio_position 
    MODIFY COLUMN instrument_key VARCHAR(10) NOT NULL,
    MODIFY COLUMN average_price DECIMAL(12,4) NOT NULL DEFAULT 0,
    MODIFY COLUMN total_cost DECIMAL(18,2) NOT NULL DEFAULT 0;

-- Add indexes for better performance on longer keys
ALTER TABLE order_history 
    DROP INDEX idx_order_instrument,
    ADD INDEX idx_order_instrument (instrument_key, session_id);

-- Update unique constraint on portfolio_position to ensure proper indexing
ALTER TABLE portfolio_position 
    DROP KEY uk_position_session_instrument,
    ADD UNIQUE KEY uk_position_session_instrument (session_id, instrument_key);