package com.stockquest.application.leaderboard.port.in;

import com.stockquest.domain.leaderboard.LeaderboardEntry;

import java.util.List;

/**
 * 리더보드 조회 유스케이스
 */
public interface GetLeaderboardUseCase {
    
    List<LeaderboardEntry> getLeaderboard(GetLeaderboardQuery query);
    
    record GetLeaderboardQuery(
            Long challengeId,
            Integer limit
    ) {
        public GetLeaderboardQuery(Long challengeId) {
            this(challengeId, 10); // 기본 10명
        }
    }
}