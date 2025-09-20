package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.challenge.ChallengeSchedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 챌린지 스케줄 JPA 엔티티
 */
@Entity
@Table(name = "challenge_schedule")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ChallengeScheduleJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ChallengeSchedule.ScheduleType scheduleType = ChallengeSchedule.ScheduleType.ONE_TIME;
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern;
    
    @Column(name = "activation_date")
    private LocalDateTime activationDate;
    
    @Column(name = "deactivation_date")
    private LocalDateTime deactivationDate;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 팩토리 메서드
    public static ChallengeScheduleJpaEntity create(Long challengeId, 
            ChallengeSchedule.ScheduleType scheduleType) {
        ChallengeScheduleJpaEntity entity = new ChallengeScheduleJpaEntity();
        entity.challengeId = challengeId;
        entity.scheduleType = scheduleType != null ? scheduleType : ChallengeSchedule.ScheduleType.ONE_TIME;
        entity.isActive = true;
        return entity;
    }
}