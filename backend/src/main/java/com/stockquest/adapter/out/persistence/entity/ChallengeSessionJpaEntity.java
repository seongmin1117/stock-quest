package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 세션 JPA 엔티티
 */
@Entity
@Table(name = "challenge_session")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSessionJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "challenge_id")
    private Long challengeId;
    
    @Column(nullable = false, name = "user_id") 
    private Long userId;
    
    @Column(nullable = false, precision = 15, scale = 2, name = "initial_balance")
    private BigDecimal initialBalance;
    
    @Column(nullable = false, precision = 15, scale = 2, name = "current_balance")
    private BigDecimal currentBalance;
    
    @Column(precision = 10, scale = 6, name = "return_rate")
    private BigDecimal returnRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public ChallengeSession toDomain() {
        return ChallengeSession.builder()
                .id(id)
                .challengeId(challengeId)
                .userId(userId)
                .initialBalance(initialBalance)
                .currentBalance(currentBalance)
                .returnRate(returnRate)
                .status(status)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }
    
    public static ChallengeSessionJpaEntity fromDomain(ChallengeSession session) {
        return ChallengeSessionJpaEntity.builder()
                .id(session.getId())
                .challengeId(session.getChallengeId())
                .userId(session.getUserId())
                .initialBalance(session.getInitialBalance())
                .currentBalance(session.getCurrentBalance())
                .returnRate(session.getReturnRate())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .build();
    }
}