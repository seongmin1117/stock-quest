package com.stockquest.domain.leaderboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 리더보드 엔트리 도메인 엔티티
 * 챌린지별 사용자 성과 순위
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class LeaderboardEntry {
    private Long id;
    private Long challengeId;
    private Long sessionId;
    private Long userId;
    private BigDecimal pnl;              // 손익 (실현 + 미실현)
    private BigDecimal returnPercentage; // 수익률 (%)
    private Integer rankPosition;        // 순위
    private LocalDateTime calculatedAt;
    
    
    public LeaderboardEntry(Long challengeId, Long sessionId, Long userId, 
                           BigDecimal pnl, BigDecimal returnPercentage) {
        validateChallengeId(challengeId);
        validateSessionId(sessionId);
        validateUserId(userId);
        validatePnL(pnl);
        validateReturnPercentage(returnPercentage);
        
        this.challengeId = challengeId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.pnl = pnl;
        this.returnPercentage = returnPercentage;
        this.rankPosition = 0; // 계산 후 설정
        this.calculatedAt = LocalDateTime.now();
    }
    
    private void validateChallengeId(Long challengeId) {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("유효한 챌린지 ID가 필요합니다");
        }
    }
    
    private void validateSessionId(Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("유효한 세션 ID가 필요합니다");
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효한 사용자 ID가 필요합니다");
        }
    }
    
    private void validatePnL(BigDecimal pnl) {
        if (pnl == null) {
            throw new IllegalArgumentException("손익은 필수입니다");
        }
    }
    
    private void validateReturnPercentage(BigDecimal returnPercentage) {
        if (returnPercentage == null) {
            throw new IllegalArgumentException("수익률은 필수입니다");
        }
    }
    
    public void updateRank(Integer rank) {
        if (rank == null || rank <= 0) {
            throw new IllegalArgumentException("순위는 1 이상이어야 합니다");
        }
        this.rankPosition = rank;
    }
    
    public void updatePerformance(BigDecimal newPnL, BigDecimal newReturnPercentage) {
        validatePnL(newPnL);
        validateReturnPercentage(newReturnPercentage);
        
        this.pnl = newPnL;
        this.returnPercentage = newReturnPercentage;
        this.calculatedAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메소드 (JPA Entity에서 도메인 객체 생성용)
    public static LeaderboardEntry of(Long id, Long challengeId, Long sessionId, Long userId,
                                      BigDecimal pnl, BigDecimal returnPercentage, Integer rankPosition,
                                      LocalDateTime calculatedAt) {
        var entry = new LeaderboardEntry(challengeId, sessionId, userId, pnl, returnPercentage);
        entry.id = id;
        entry.rankPosition = rankPosition;
        entry.calculatedAt = calculatedAt;
        return entry;
    }
    
}