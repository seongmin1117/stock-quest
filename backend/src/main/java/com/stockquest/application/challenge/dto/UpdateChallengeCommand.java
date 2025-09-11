package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.ChallengeDifficulty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 수정 명령 객체
 */
public class UpdateChallengeCommand {
    private Long challengeId;
    private String title;
    private String description;
    private Long categoryId;
    private ChallengeDifficulty difficulty;
    private BigDecimal initialBalance;
    private Integer durationDays;
    private Integer maxParticipants;
    private List<String> availableInstruments;
    private Map<String, Object> tradingRestrictions;
    private Map<String, Object> successCriteria;
    private String learningObjectives;
    private String marketScenarioDescription;
    private Integer riskLevel;
    private Integer estimatedTimeMinutes;
    private List<String> tags;
    private Long modifiedBy;

    // 기본 생성자
    public UpdateChallengeCommand() {}

    // 빌더 패턴을 위한 정적 메서드
    public static UpdateChallengeCommandBuilder builder() {
        return new UpdateChallengeCommandBuilder();
    }

    // Getter methods
    public Long getChallengeId() {
        return challengeId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public List<String> getAvailableInstruments() {
        return availableInstruments;
    }

    public Map<String, Object> getTradingRestrictions() {
        return tradingRestrictions;
    }

    public Map<String, Object> getSuccessCriteria() {
        return successCriteria;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public String getMarketScenarioDescription() {
        return marketScenarioDescription;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public List<String> getTags() {
        return tags;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    // Setter methods
    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setDifficulty(ChallengeDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setAvailableInstruments(List<String> availableInstruments) {
        this.availableInstruments = availableInstruments;
    }

    public void setTradingRestrictions(Map<String, Object> tradingRestrictions) {
        this.tradingRestrictions = tradingRestrictions;
    }

    public void setSuccessCriteria(Map<String, Object> successCriteria) {
        this.successCriteria = successCriteria;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public void setMarketScenarioDescription(String marketScenarioDescription) {
        this.marketScenarioDescription = marketScenarioDescription;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // Builder class
    public static class UpdateChallengeCommandBuilder {
        private UpdateChallengeCommand command = new UpdateChallengeCommand();

        public UpdateChallengeCommandBuilder challengeId(Long challengeId) {
            command.setChallengeId(challengeId);
            return this;
        }

        public UpdateChallengeCommandBuilder title(String title) {
            command.setTitle(title);
            return this;
        }

        public UpdateChallengeCommandBuilder description(String description) {
            command.setDescription(description);
            return this;
        }

        public UpdateChallengeCommandBuilder categoryId(Long categoryId) {
            command.setCategoryId(categoryId);
            return this;
        }

        public UpdateChallengeCommandBuilder difficulty(ChallengeDifficulty difficulty) {
            command.setDifficulty(difficulty);
            return this;
        }

        public UpdateChallengeCommandBuilder initialBalance(BigDecimal initialBalance) {
            command.setInitialBalance(initialBalance);
            return this;
        }

        public UpdateChallengeCommandBuilder durationDays(Integer durationDays) {
            command.setDurationDays(durationDays);
            return this;
        }

        public UpdateChallengeCommandBuilder maxParticipants(Integer maxParticipants) {
            command.setMaxParticipants(maxParticipants);
            return this;
        }

        public UpdateChallengeCommandBuilder availableInstruments(List<String> availableInstruments) {
            command.setAvailableInstruments(availableInstruments);
            return this;
        }

        public UpdateChallengeCommandBuilder tradingRestrictions(Map<String, Object> tradingRestrictions) {
            command.setTradingRestrictions(tradingRestrictions);
            return this;
        }

        public UpdateChallengeCommandBuilder successCriteria(Map<String, Object> successCriteria) {
            command.setSuccessCriteria(successCriteria);
            return this;
        }

        public UpdateChallengeCommandBuilder learningObjectives(String learningObjectives) {
            command.setLearningObjectives(learningObjectives);
            return this;
        }

        public UpdateChallengeCommandBuilder marketScenarioDescription(String marketScenarioDescription) {
            command.setMarketScenarioDescription(marketScenarioDescription);
            return this;
        }

        public UpdateChallengeCommandBuilder riskLevel(Integer riskLevel) {
            command.setRiskLevel(riskLevel);
            return this;
        }

        public UpdateChallengeCommandBuilder estimatedTimeMinutes(Integer estimatedTimeMinutes) {
            command.setEstimatedTimeMinutes(estimatedTimeMinutes);
            return this;
        }

        public UpdateChallengeCommandBuilder tags(List<String> tags) {
            command.setTags(tags);
            return this;
        }

        public UpdateChallengeCommandBuilder modifiedBy(Long modifiedBy) {
            command.setModifiedBy(modifiedBy);
            return this;
        }

        public UpdateChallengeCommand build() {
            return command;
        }
    }
}