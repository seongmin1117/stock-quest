package com.stockquest.application.admin.challenge.dto;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeAnalytics;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.ChallengeType;
import com.stockquest.domain.challenge.ChallengeInstrument;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 챌린지 상세 조회 결과
 */
@Getter
@Builder
public class ChallengeDetailResult {
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
    private int speedFactor;
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
    private List<ChallengeInstrument> instruments;
    private ChallengeAnalytics analytics;
}