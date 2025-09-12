package com.stockquest.application.leaderboard;

import com.stockquest.application.leaderboard.port.in.GetLeaderboardUseCase;
import com.stockquest.application.leaderboard.port.in.GetLeaderboardUseCase.GetLeaderboardQuery;
import com.stockquest.domain.leaderboard.LeaderboardEntry;
import com.stockquest.domain.leaderboard.port.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 리더보드 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetLeaderboardService implements GetLeaderboardUseCase {
    
    private final LeaderboardRepository leaderboardRepository;
    
    @Override
    @Cacheable(value = "leaderboard", key = "#query.challengeId() + '_' + #query.limit()")
    public List<LeaderboardEntry> getLeaderboard(GetLeaderboardQuery query) {
        if (query.limit() != null && query.limit() > 0) {
            return leaderboardRepository.findTopNByChallengeId(query.challengeId(), query.limit());
        } else {
            return leaderboardRepository.findByChallengeIdOrderByReturnPercentageDesc(query.challengeId());
        }
    }
    
    /**
     * 캐시 워밍업용 리더보드 조회 (offset, limit 지원)
     */
    @Cacheable(value = "leaderboard", key = "#challengeId + '_' + #offset + '_' + #limit")
    public List<LeaderboardEntry> getLeaderboard(Long challengeId, int offset, int limit) {
        return leaderboardRepository.findTopNByChallengeId(challengeId, limit);
    }
}