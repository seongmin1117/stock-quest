package com.stockquest.domain.leaderboard.port;

import com.stockquest.domain.leaderboard.LeaderboardEntry;

import java.util.List;
import java.util.Optional;

/**
 * 리더보드 저장소 포트
 */
public interface LeaderboardRepository {
    
    /**
     * 리더보드 엔트리 저장
     */
    LeaderboardEntry save(LeaderboardEntry entry);
    
    /**
     * ID로 엔트리 조회
     */
    Optional<LeaderboardEntry> findById(Long id);
    
    /**
     * 챌린지별 리더보드 조회 (수익률 높은 순)
     */
    List<LeaderboardEntry> findByChallengeIdOrderByReturnPercentageDesc(Long challengeId);
    
    /**
     * 특정 챌린지의 특정 사용자 엔트리 조회
     */
    Optional<LeaderboardEntry> findByChallengeIdAndUserId(Long challengeId, Long userId);
    
    /**
     * 챌린지별 상위 N명 조회
     */
    List<LeaderboardEntry> findTopNByChallengeId(Long challengeId, int limit);
    
    /**
     * 챌린지 완료된 참가자 수 조회
     */
    long countByChallengeId(Long challengeId);
    
    /**
     * 챌린지 종료 후 리더보드 일괄 저장
     */
    List<LeaderboardEntry> saveAll(List<LeaderboardEntry> entries);
    
    /**
     * 기존 엔트리 삭제 (재계산용)
     */
    void deleteByChallengeId(Long challengeId);
}