package com.stockquest.adapter.in.web.admin.dto;

import com.stockquest.application.admin.challenge.dto.ChallengeDetailResult;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 챌린지 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChallengeResponse {
    
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long templateId;
    private String templateName;
    private Long marketPeriodId;
    private String marketPeriodName;
    private ChallengeDifficulty difficulty;
    private ChallengeType challengeType;
    private ChallengeStatus status;
    private BigDecimal initialBalance;
    private Integer estimatedDurationMinutes;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer speedFactor;
    private List<String> tags;
    private Map<String, Object> successCriteria;
    private Map<String, Object> marketScenario;
    private String learningObjectives;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long createdBy;
    private String createdByName;
    private Boolean isFeatured;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InstrumentInfo> instruments;
    private AnalyticsInfo analytics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstrumentInfo {
        private Long id;
        private String instrumentKey;
        private String actualTicker;
        private String hiddenName;
        private String actualName;
        private String type;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsInfo {
        private Integer totalParticipants;
        private Integer completedParticipants;
        private BigDecimal averageReturnRate;
        private BigDecimal successRatePercentage;
        private BigDecimal ratingAverage;
        private Integer ratingCount;
        private LocalDateTime lastCalculatedAt;
    }
    
    /**
     * 도메인 결과를 DTO로 변환
     */
    public static AdminChallengeResponse from(ChallengeDetailResult result) {
        return AdminChallengeResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .categoryId(result.getCategoryId())
                .categoryName(result.getCategoryName())
                .templateId(result.getTemplateId())
                .templateName(result.getTemplateName())
                .marketPeriodId(result.getMarketPeriodId())
                .marketPeriodName(result.getMarketPeriodName())
                .difficulty(result.getDifficulty())
                .challengeType(result.getChallengeType())
                .status(result.getStatus())
                .initialBalance(result.getInitialBalance())
                .estimatedDurationMinutes(result.getEstimatedDurationMinutes())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .speedFactor(result.getSpeedFactor())
                .tags(result.getTags())
                .successCriteria(result.getSuccessCriteria())
                .marketScenario(result.getMarketScenario())
                .learningObjectives(result.getLearningObjectives())
                .maxParticipants(result.getMaxParticipants())
                .currentParticipants(result.getCurrentParticipants())
                .startDate(result.getStartDate())
                .endDate(result.getEndDate())
                .createdBy(result.getCreatedBy())
                .createdByName(result.getCreatedByName())
                .isFeatured(result.getIsFeatured())
                .sortOrder(result.getSortOrder())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .instruments(result.getInstruments().stream()
                        .map(instrument -> InstrumentInfo.builder()
                                .id(instrument.getId())
                                .instrumentKey(instrument.getInstrumentKey())
                                .actualTicker(instrument.getActualTicker())
                                .hiddenName(instrument.getHiddenName())
                                .actualName(instrument.getActualName())
                                .type(instrument.getType().name())
                                .build())
                        .toList())
                .analytics(result.getAnalytics() != null ? AnalyticsInfo.builder()
                        .totalParticipants(result.getAnalytics().getTotalParticipants())
                        .completedParticipants(result.getAnalytics().getCompletedParticipants())
                        .averageReturnRate(result.getAnalytics().getAverageReturnRate())
                        .successRatePercentage(result.getAnalytics().getSuccessRatePercentage())
                        .ratingAverage(result.getAnalytics().getRatingAverage())
                        .ratingCount(result.getAnalytics().getRatingCount())
                        .lastCalculatedAt(result.getAnalytics().getLastCalculatedAt())
                        .build() : null)
                .build();
    }
}