package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.leaderboard.LeaderboardEntry;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 리더보드 JPA 엔티티
 */
@Entity
@Table(name = "leaderboard")
@EntityListeners(AuditingEntityListener.class)
public class LeaderboardJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "pnl", nullable = false, precision = 15, scale = 2)
    private BigDecimal pnl;
    
    @Column(name = "return_pct", nullable = false, precision = 8, scale = 4)
    private BigDecimal returnPercentage;
    
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition = 0;
    
    @CreatedDate
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    protected LeaderboardJpaEntity() {}
    
    public LeaderboardJpaEntity(Long challengeId, Long sessionId, Long userId, 
                               BigDecimal pnl, BigDecimal returnPercentage) {
        this.challengeId = challengeId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.pnl = pnl;
        this.returnPercentage = returnPercentage;
    }
    
    public static LeaderboardJpaEntity from(LeaderboardEntry domainEntry) {
        var entity = new LeaderboardJpaEntity(
                domainEntry.getChallengeId(),
                domainEntry.getSessionId(),
                domainEntry.getUserId(),
                domainEntry.getPnl(),
                domainEntry.getReturnPercentage()
        );
        entity.id = domainEntry.getId();
        entity.rankPosition = domainEntry.getRankPosition();
        entity.calculatedAt = domainEntry.getCalculatedAt();
        return entity;
    }
    
    public LeaderboardEntry toDomain() {
        return LeaderboardEntry.of(id, challengeId, sessionId, userId, pnl, returnPercentage, 
                                  rankPosition, calculatedAt);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public Long getChallengeId() { return challengeId; }
    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public BigDecimal getPnl() { return pnl; }
    public BigDecimal getReturnPercentage() { return returnPercentage; }
    public Integer getRankPosition() { return rankPosition; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    
    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }
}