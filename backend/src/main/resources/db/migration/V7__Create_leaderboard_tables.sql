-- 리더보드 테이블 생성
CREATE TABLE leaderboard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    pnl DECIMAL(15,2) NOT NULL DEFAULT 0,  -- 손익
    return_pct DECIMAL(8,4) NOT NULL DEFAULT 0,  -- 수익률 (%)
    rank_position INT NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES challenge_session(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_leaderboard_challenge_session (challenge_id, session_id),
    INDEX idx_leaderboard_challenge_rank (challenge_id, rank_position),
    INDEX idx_leaderboard_return_pct (return_pct DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;