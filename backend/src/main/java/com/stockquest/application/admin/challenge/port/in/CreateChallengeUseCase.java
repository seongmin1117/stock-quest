package com.stockquest.application.admin.challenge.port.in;

import com.stockquest.application.admin.challenge.dto.ChallengeDetailResult;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 생성 유스케이스
 */
public interface CreateChallengeUseCase {
    
    /**
     * 챌린지 생성
     */
    ChallengeDetailResult create(CreateChallengeCommand command);
    
    /**
     * 템플릿으로부터 챌린지 생성
     */
    ChallengeDetailResult createFromTemplate(CreateFromTemplateCommand command);
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateChallengeCommand {
        private Long adminId;
        private String title;
        private String description;
        private Long categoryId;
        private Long templateId;
        private Long marketPeriodId;
        private ChallengeDifficulty difficulty;
        private ChallengeType challengeType;
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
        private List<ChallengeInstrumentCommand> instruments;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChallengeInstrumentCommand {
            private String instrumentKey;
            private String actualTicker;
            private String hiddenName;
            private String actualName;
            private String type;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateFromTemplateCommand {
        private Long adminId;
        private Long templateId;
        private String title;
        private String description;
        private Map<String, Object> customizations;
    }
}