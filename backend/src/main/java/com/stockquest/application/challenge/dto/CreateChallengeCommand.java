package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 생성 명령 객체
 */
public class CreateChallengeCommand {
    private String title;
    private String description;
    private Long categoryId;
    private ChallengeDifficulty difficulty;
    private ChallengeType challengeType;
    private BigDecimal initialBalance;
    private Integer durationDays;
    private Integer maxParticipants;
    private List<String> availableInstruments;
    private Map<String, Object> tradingRestrictions;
    private Map<String, Object> successCriteria;
    private Map<String, Object> entryRequirements;
    private String learningObjectives;
    private String marketScenarioDescription;
    private Integer riskLevel;
    private Integer estimatedTimeMinutes;
    private List<String> tags;
    private Long createdBy;

    // 기본 생성자
    public CreateChallengeCommand() {}

    // 빌더 패턴을 위한 정적 메서드
    public static CreateChallengeCommandBuilder builder() {
        return new CreateChallengeCommandBuilder();
    }

    // Getter methods
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

    public ChallengeType getChallengeType() {
        return challengeType;
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

    public Map<String, Object> getEntryRequirements() {
        return entryRequirements;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    // Setter methods
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

    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
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

    public void setEntryRequirements(Map<String, Object> entryRequirements) {
        this.entryRequirements = entryRequirements;
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

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    // Builder class
    public static class CreateChallengeCommandBuilder {
        private CreateChallengeCommand command = new CreateChallengeCommand();

        public CreateChallengeCommandBuilder title(String title) {
            command.setTitle(title);
            return this;
        }

        public CreateChallengeCommandBuilder description(String description) {
            command.setDescription(description);
            return this;
        }

        public CreateChallengeCommandBuilder categoryId(Long categoryId) {
            command.setCategoryId(categoryId);
            return this;
        }

        public CreateChallengeCommandBuilder difficulty(ChallengeDifficulty difficulty) {
            command.setDifficulty(difficulty);
            return this;
        }

        public CreateChallengeCommandBuilder challengeType(ChallengeType challengeType) {
            command.setChallengeType(challengeType);
            return this;
        }

        public CreateChallengeCommandBuilder initialBalance(BigDecimal initialBalance) {
            command.setInitialBalance(initialBalance);
            return this;
        }

        public CreateChallengeCommandBuilder durationDays(Integer durationDays) {
            command.setDurationDays(durationDays);
            return this;
        }

        public CreateChallengeCommandBuilder maxParticipants(Integer maxParticipants) {
            command.setMaxParticipants(maxParticipants);
            return this;
        }

        public CreateChallengeCommandBuilder availableInstruments(List<String> availableInstruments) {
            command.setAvailableInstruments(availableInstruments);
            return this;
        }

        public CreateChallengeCommandBuilder tradingRestrictions(Map<String, Object> tradingRestrictions) {
            command.setTradingRestrictions(tradingRestrictions);
            return this;
        }

        public CreateChallengeCommandBuilder successCriteria(Map<String, Object> successCriteria) {
            command.setSuccessCriteria(successCriteria);
            return this;
        }

        public CreateChallengeCommandBuilder entryRequirements(Map<String, Object> entryRequirements) {
            command.setEntryRequirements(entryRequirements);
            return this;
        }

        public CreateChallengeCommandBuilder learningObjectives(String learningObjectives) {
            command.setLearningObjectives(learningObjectives);
            return this;
        }

        public CreateChallengeCommandBuilder marketScenarioDescription(String marketScenarioDescription) {
            command.setMarketScenarioDescription(marketScenarioDescription);
            return this;
        }

        public CreateChallengeCommandBuilder riskLevel(Integer riskLevel) {
            command.setRiskLevel(riskLevel);
            return this;
        }

        public CreateChallengeCommandBuilder estimatedTimeMinutes(Integer estimatedTimeMinutes) {
            command.setEstimatedTimeMinutes(estimatedTimeMinutes);
            return this;
        }

        public CreateChallengeCommandBuilder tags(List<String> tags) {
            command.setTags(tags);
            return this;
        }

        public CreateChallengeCommandBuilder createdBy(Long createdBy) {
            command.setCreatedBy(createdBy);
            return this;
        }

        public CreateChallengeCommand build() {
            return command;
        }
    }
}