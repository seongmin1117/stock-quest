-- StockQuest Database Performance Optimization
-- Migration to add essential indexes for portfolio and trading operations

-- Portfolio position queries optimization
CREATE INDEX idx_portfolio_user_id_status ON portfolio_position(user_id, status);
CREATE INDEX idx_portfolio_instrument_key ON portfolio_position(instrument_key);
CREATE INDEX idx_portfolio_created_at ON portfolio_position(created_at);

-- Order history optimization for leaderboard calculations
CREATE INDEX idx_orders_user_session_status ON order_history(user_id, session_id, status);
CREATE INDEX idx_orders_executed_at ON order_history(executed_at) WHERE status = 'EXECUTED';
CREATE INDEX idx_orders_instrument_side ON order_history(instrument_key, order_side);

-- Challenge session performance improvements
CREATE INDEX idx_session_challenge_status ON challenge_session(challenge_id, status);
CREATE INDEX idx_session_user_active ON challenge_session(user_id, status) WHERE status = 'ACTIVE';
CREATE INDEX idx_session_end_time ON challenge_session(end_time);

-- Leaderboard calculation optimization
CREATE INDEX idx_leaderboard_challenge_rank ON leaderboard(challenge_id, rank);
CREATE INDEX idx_leaderboard_user_score ON leaderboard(user_id, total_return_percentage);

-- Market data queries improvement
CREATE INDEX idx_market_data_instrument_time ON price_candle(ticker, date DESC);
CREATE INDEX idx_market_data_timeframe ON price_candle(timeframe, date DESC);

-- User activity tracking for analytics
CREATE INDEX idx_user_last_active ON users(last_active_at);
CREATE INDEX idx_user_created_portfolio ON users(created_at, portfolio_value);

-- Composite indexes for complex queries
CREATE INDEX idx_complex_portfolio_performance ON portfolio_position(user_id, status, updated_at, current_value);
CREATE INDEX idx_complex_session_orders ON order_history(session_id, status, executed_at, total_amount);

-- Challenge analytics optimization
CREATE INDEX idx_challenge_duration_difficulty ON challenge(period_start, period_end, status);
CREATE INDEX idx_challenge_participation ON challenge_session(challenge_id, status, total_return_percentage);

COMMENT ON INDEX idx_portfolio_user_id_status IS 'Optimizes user portfolio queries';
COMMENT ON INDEX idx_orders_user_session_status IS 'Speeds up trading history queries';  
COMMENT ON INDEX idx_session_challenge_status IS 'Improves challenge leaderboard calculations';
COMMENT ON INDEX idx_leaderboard_challenge_rank IS 'Accelerates leaderboard retrieval';