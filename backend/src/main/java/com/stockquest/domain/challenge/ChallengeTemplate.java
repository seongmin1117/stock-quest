package com.stockquest.domain.challenge;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 챌린지 템플릿 도메인 엔티티
 * 재사용 가능한 챌린지 설정을 정의하는 템플릿
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChallengeTemplate {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Long marketPeriodId;
    
    // 기본 설정
    private ChallengeDifficulty difficulty;
    private BigDecimal initialBalance;
    private int durationDays;
    
    // 거래 설정
    private List<String> availableInstruments;    // 거래 가능한 종목 목록
    private Map<String, Object> tradingRestrictions; // 거래 제한 규칙
    private Map<String, Object> successCriteria;     // 성공 기준
    
    // 교육 정보
    private String learningObjectives;         // 학습 목표
    private String marketScenarioDescription; // 시장 시나리오 설명
    private int riskLevel;                     // 리스크 레벨 (1-10)
    private int estimatedCompletionTime;       // 예상 완료 시간 (분)
    private Integer estimatedDurationMinutes;  // 추가 duration 필드
    private String prerequisites;              // 사전 요구사항
    private List<String> tags;                 // 태그 목록
    private Integer speedFactor;               // 속도 배율
    
    // 메타데이터
    private boolean isActive;
    private ChallengeType templateType;  // 템플릿 유형
    private int usageCount;                    // 사용 횟수
    private BigDecimal averageRating;         // 평균 평점
    private Long createdBy;                   // 생성자 ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 템플릿 설정 (JSON으로 저장되는 설정 정보)
    private Map<String, Object> templateConfig;
    private Map<String, Object> config;  // 추가 config 필드
    private Map<String, Object> marketScenario;  // 시장 시나리오

    // 도메인 생성자
    public ChallengeTemplate(String name, String description, Long categoryId,
                           ChallengeDifficulty difficulty, BigDecimal initialBalance, int durationDays) {
        validateBasicInfo(name, initialBalance, durationDays);
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.difficulty = difficulty != null ? difficulty : ChallengeDifficulty.BEGINNER;
        this.initialBalance = initialBalance != null ? initialBalance : new BigDecimal("1000000.00");
        this.durationDays = durationDays;
        this.isActive = true;
        this.usageCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.riskLevel = 5;
        this.estimatedCompletionTime = 120;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void updateBasicInfo(String name, String description, Long categoryId,
                              ChallengeDifficulty difficulty, BigDecimal initialBalance, int durationDays) {
        validateBasicInfo(name, initialBalance, durationDays);
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.initialBalance = initialBalance;
        this.durationDays = durationDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTradingSettings(List<String> availableInstruments,
                                    Map<String, Object> tradingRestrictions,
                                    Map<String, Object> successCriteria) {
        this.availableInstruments = availableInstruments;
        this.tradingRestrictions = tradingRestrictions;
        this.successCriteria = successCriteria;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEducationalInfo(String learningObjectives, String marketScenarioDescription,
                                    int riskLevel, int estimatedCompletionTime,
                                    String prerequisites, List<String> tags) {
        validateRiskLevel(riskLevel);
        validateEstimatedTime(estimatedCompletionTime);
        this.learningObjectives = learningObjectives;
        this.marketScenarioDescription = marketScenarioDescription;
        this.riskLevel = riskLevel;
        this.estimatedCompletionTime = estimatedCompletionTime;
        this.prerequisites = prerequisites;
        this.tags = tags;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTemplateConfig(Map<String, Object> templateConfig) {
        this.templateConfig = templateConfig;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementUsageCount() {
        this.usageCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(BigDecimal newAverageRating) {
        if (newAverageRating != null && newAverageRating.compareTo(BigDecimal.ZERO) >= 0 
            && newAverageRating.compareTo(new BigDecimal("5.0")) <= 0) {
            this.averageRating = newAverageRating;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean isPopular() {
        return usageCount > 10 && averageRating.compareTo(new BigDecimal("4.0")) > 0;
    }

    public boolean isHighRisk() {
        return riskLevel >= 8;
    }

    public boolean isLongTerm() {
        return durationDays > 60;
    }

    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.trim().isEmpty();
    }
    
    public Challenge createChallenge() {
        return Challenge.builder()
            .title(this.name)
            .description(this.description)
            .categoryId(this.categoryId)
            .templateId(this.id)
            .marketPeriodId(this.marketPeriodId)
            .difficulty(this.difficulty)
            .challengeType(this.templateType)
            .status(ChallengeStatus.DRAFT)
            .initialBalance(this.initialBalance)
            .durationDays(this.durationDays)
            .tags(this.tags)
            .successCriteria(this.successCriteria)
            .learningObjectives(this.learningObjectives)
            .availableInstruments(this.availableInstruments)
            .tradingRestrictions(this.tradingRestrictions)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // 검증 메서드
    private void validateBasicInfo(String name, BigDecimal initialBalance, int durationDays) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("템플릿 이름은 필수입니다.");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("템플릿 이름은 200자를 초과할 수 없습니다.");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("초기 잔액은 0보다 커야 합니다.");
        }
        if (initialBalance.compareTo(new BigDecimal("100000000")) > 0) {
            throw new IllegalArgumentException("초기 잔액이 너무 큽니다.");
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("챌린지 기간은 0보다 커야 합니다.");
        }
        if (durationDays > 365) {
            throw new IllegalArgumentException("챌린지 기간은 365일을 초과할 수 없습니다.");
        }
    }

    private void validateRiskLevel(int riskLevel) {
        if (riskLevel < 1 || riskLevel > 10) {
            throw new IllegalArgumentException("리스크 레벨은 1-10 사이의 값이어야 합니다.");
        }
    }

    private void validateEstimatedTime(int estimatedTime) {
        if (estimatedTime < 10) {
            throw new IllegalArgumentException("예상 완료 시간은 10분 이상이어야 합니다.");
        }
        if (estimatedTime > 1440) { // 24시간
            throw new IllegalArgumentException("예상 완료 시간은 24시간을 초과할 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeTemplate that = (ChallengeTemplate) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "ChallengeTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", difficulty=" + difficulty +
                ", initialBalance=" + initialBalance +
                ", durationDays=" + durationDays +
                ", riskLevel=" + riskLevel +
                ", usageCount=" + usageCount +
                ", averageRating=" + averageRating +
                ", isActive=" + isActive +
                '}';
    }
}