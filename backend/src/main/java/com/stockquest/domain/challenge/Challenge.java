package com.stockquest.domain.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 도메인 엔티티
 * 특정 기간의 과거 시장 데이터를 사용한 투자 학습 챌린지
 * V2: Enhanced with category, template, analytics and management features
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private Long templateId;
    private Long marketPeriodId;
    private ChallengeDifficulty difficulty;
    private ChallengeType challengeType;
    private ChallengeStatus status;
    private BigDecimal initialBalance;
    private Integer durationDays;
    private Integer estimatedDurationMinutes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate periodStart;          // 시뮬레이션할 시장 데이터 시작일
    private LocalDate periodEnd;            // 시뮬레이션할 시장 데이터 종료일
    private int speedFactor;                // 시간 압축 배율 (1일 = N초)
    private List<String> tags;              // Tags for filtering and search
    private Map<String, Object> successCriteria;   // Success criteria configuration
    private Map<String, Object> marketScenario;    // Market scenario details
    private String learningObjectives;      // Learning objectives text
    private Integer maxParticipants;        // Maximum number of participants
    private Integer currentParticipants;    // Current number of participants
    private Map<String, Object> entryRequirements;  // Entry requirements
    private List<String> availableInstruments;       // Available trading instruments
    private Map<String, Object> tradingRestrictions; // Trading restrictions
    private Map<String, Object> rewardStructure;     // Reward structure
    private String marketScenarioDescription;        // Market scenario description
    private Integer riskLevel;                       // Risk level (1-10)
    private Integer estimatedTimeMinutes;            // Estimated completion time in minutes
    private Boolean featured;                        // Whether this challenge is featured  
    private Boolean isFeatured;                      // Alternative featured field name
    private Integer sortOrder;                      // Sort order for display
    private BigDecimal averageRating;               // Average user rating
    private Integer totalReviews;                    // Total number of reviews
    private Long createdBy;                         // User ID who created this challenge
    private Long lastModifiedBy;                    // User ID who last modified this challenge
    private Integer version;                        // Version for optimistic locking
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<ChallengeInstrument> instruments = new ArrayList<>();
    
    /**
     * 챌린지 상태 전환 가능 여부 확인
     */
    public boolean canTransitionTo(ChallengeStatus newStatus) {
        return switch (this.status) {
            case DRAFT -> newStatus == ChallengeStatus.SCHEDULED || newStatus == ChallengeStatus.ACTIVE || newStatus == ChallengeStatus.CANCELLED;
            case SCHEDULED -> newStatus == ChallengeStatus.ACTIVE || newStatus == ChallengeStatus.CANCELLED || newStatus == ChallengeStatus.DRAFT;
            case ACTIVE -> newStatus == ChallengeStatus.COMPLETED || newStatus == ChallengeStatus.CANCELLED;
            case COMPLETED -> newStatus == ChallengeStatus.ARCHIVED;
            case ARCHIVED -> false; // Cannot transition from archived
            case CANCELLED -> newStatus == ChallengeStatus.DRAFT; // Can restart as draft
        };
    }
    
    /**
     * 챌린지 참여 가능 여부 확인
     */
    public boolean isJoinable() {
        return status == ChallengeStatus.ACTIVE && 
               (maxParticipants == null || currentParticipants < maxParticipants);
    }
    
    /**
     * 참여자 수 증가
     */
    public void incrementParticipants() {
        if (currentParticipants == null) {
            currentParticipants = 1;
        } else {
            currentParticipants++;
        }
    }
    
    /**
     * 참여자 수 감소
     */
    public void decrementParticipants() {
        if (currentParticipants != null && currentParticipants > 0) {
            currentParticipants--;
        }
    }
    
    /**
     * 챌린지 난이도 레벨 확인
     */
    public boolean isHighDifficulty() {
        return difficulty == ChallengeDifficulty.ADVANCED || difficulty == ChallengeDifficulty.EXPERT;
    }
    
    /**
     * 리스크 레벨 확인
     */
    public boolean isHighRisk() {
        return riskLevel != null && riskLevel >= 8;
    }
    
    /**
     * 장기 챌린지 여부 확인
     */
    public boolean isLongTerm() {
        return durationDays != null && durationDays > 60;
    }
    
    /**
     * 평점 업데이트
     */
    public void updateRating(BigDecimal newAverageRating, Integer newTotalReviews) {
        this.averageRating = newAverageRating;
        this.totalReviews = newTotalReviews;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 인기 챌린지 여부 확인
     */
    public boolean isPopular() {
        return currentParticipants != null && currentParticipants > 20 &&
               averageRating != null && averageRating.compareTo(new BigDecimal("4.0")) > 0;
    }
    
    /**
     * 챌린지 타입별 특성 확인
     */
    public boolean isTournament() {
        return challengeType == ChallengeType.TOURNAMENT;
    }
    
    public boolean isEducational() {
        return challengeType == ChallengeType.EDUCATIONAL;
    }
    
    public boolean isCommunity() {
        return challengeType == ChallengeType.COMMUNITY;
    }
    
    /**
     * 사전 요구사항 존재 여부
     */
    public boolean hasEntryRequirements() {
        return entryRequirements != null && !entryRequirements.isEmpty();
    }
    
    /**
     * 특정 도구 사용 가능 여부
     */
    public boolean isInstrumentAvailable(String ticker) {
        return availableInstruments != null && availableInstruments.contains(ticker);
    }
    
    /**
     * 버전 증가 (낙관적 락킹)
     */
    public void incrementVersion() {
        this.version = (this.version == null) ? 1 : this.version + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 피처드 챌린지로 설정/해제
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 챌린지 수정자 업데이트
     */
    public void updateModifiedBy(Long userId) {
        this.lastModifiedBy = userId;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
    }
}