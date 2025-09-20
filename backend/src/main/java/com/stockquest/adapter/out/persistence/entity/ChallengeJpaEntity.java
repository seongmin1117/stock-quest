package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 챌린지 JPA 엔티티
 * Hibernate Second-level Cache 적용 (READ_WRITE 전략)
 */
@Entity
@Table(name = "challenge")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChallengeJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeDifficulty difficulty;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;
    
    @Column(nullable = false, precision = 15, scale = 2, name = "initial_balance")
    private BigDecimal initialBalance;
    
    @Column(nullable = false, name = "duration_days")
    private Integer durationDays;
    
    @Column(nullable = false, name = "start_date")
    private LocalDateTime startDate;
    
    @Column(nullable = false, name = "end_date")
    private LocalDateTime endDate;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Missing simulation fields
    @Column(name = "period_start")
    private LocalDate periodStart;
    
    @Column(name = "period_end") 
    private LocalDate periodEnd;
    
    @Column(name = "speed_factor")
    private Integer speedFactor;
    
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
    
    public Challenge toDomain() {
        return Challenge.builder()
                .id(id)
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .status(status)
                .initialBalance(initialBalance)
                .durationDays(durationDays)
                .startDate(startDate)
                .endDate(endDate)
                .periodStart(periodStart != null ? periodStart : LocalDate.now().minusMonths(3))
                .periodEnd(periodEnd != null ? periodEnd : LocalDate.now().minusMonths(1))
                .speedFactor(speedFactor != null ? speedFactor : 1)
                .build();
    }
    
    public static ChallengeJpaEntity fromDomain(Challenge challenge) {
        return ChallengeJpaEntity.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .difficulty(challenge.getDifficulty())
                .status(challenge.getStatus())
                .initialBalance(challenge.getInitialBalance())
                .durationDays(challenge.getDurationDays())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .periodStart(challenge.getPeriodStart())
                .periodEnd(challenge.getPeriodEnd())
                .speedFactor(challenge.getSpeedFactor())
                .build();
    }
}