package com.stockquest.application.leaderboard.port.in;

import com.stockquest.domain.leaderboard.LeaderboardEntry;

import java.util.List;

/**
 * 리더보드 계산 유스케이스
 * 챌린지 종료 시 호출되어 전체 참가자의 순위를 계산
 */
public interface CalculateLeaderboardUseCase {
    
    List<LeaderboardEntry> calculateLeaderboard(CalculateLeaderboardCommand command);
    
    record CalculateLeaderboardCommand(
            Long challengeId
    ) {}
}