package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;

import java.math.BigDecimal;

/**
 * 템플릿 기반 챌린지 생성 명령 객체
 */
public class CreateFromTemplateCommand {
    private Long templateId;
    private String title;
    private String description;
    private ChallengeDifficulty difficulty;
    private ChallengeType challengeType;
    private BigDecimal initialBalance;
    private Integer durationDays;
    private Integer maxParticipants;
    private Long createdBy;

    // 기본 생성자
    public CreateFromTemplateCommand() {}

    // 빌더 패턴을 위한 정적 메서드
    public static CreateFromTemplateCommandBuilder builder() {
        return new CreateFromTemplateCommandBuilder();
    }

    // Getter methods
    public Long getTemplateId() {
        return templateId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    // Setter methods
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    // Builder class
    public static class CreateFromTemplateCommandBuilder {
        private CreateFromTemplateCommand command = new CreateFromTemplateCommand();

        public CreateFromTemplateCommandBuilder templateId(Long templateId) {
            command.setTemplateId(templateId);
            return this;
        }

        public CreateFromTemplateCommandBuilder title(String title) {
            command.setTitle(title);
            return this;
        }

        public CreateFromTemplateCommandBuilder description(String description) {
            command.setDescription(description);
            return this;
        }

        public CreateFromTemplateCommandBuilder difficulty(ChallengeDifficulty difficulty) {
            command.setDifficulty(difficulty);
            return this;
        }

        public CreateFromTemplateCommandBuilder challengeType(ChallengeType challengeType) {
            command.setChallengeType(challengeType);
            return this;
        }

        public CreateFromTemplateCommandBuilder initialBalance(BigDecimal initialBalance) {
            command.setInitialBalance(initialBalance);
            return this;
        }

        public CreateFromTemplateCommandBuilder durationDays(Integer durationDays) {
            command.setDurationDays(durationDays);
            return this;
        }

        public CreateFromTemplateCommandBuilder maxParticipants(Integer maxParticipants) {
            command.setMaxParticipants(maxParticipants);
            return this;
        }

        public CreateFromTemplateCommandBuilder createdBy(Long createdBy) {
            command.setCreatedBy(createdBy);
            return this;
        }

        public CreateFromTemplateCommand build() {
            return command;
        }
    }
}