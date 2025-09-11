package com.stockquest.adapter.in.web.admin.dto;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChallengeRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;
    
    @Size(max = 2000, message = "설명은 2000자 이하여야 합니다")
    private String description;
    
    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;
    
    private Long templateId;
    
    private Long marketPeriodId;
    
    @NotNull(message = "난이도는 필수입니다")
    private ChallengeDifficulty difficulty;
    
    @NotNull(message = "챌린지 유형은 필수입니다")
    private ChallengeType challengeType;
    
    @NotNull(message = "초기 자본금은 필수입니다")
    @DecimalMin(value = "1000.0", message = "초기 자본금은 1,000원 이상이어야 합니다")
    @DecimalMax(value = "10000000.0", message = "초기 자본금은 10,000,000원 이하여야 합니다")
    private BigDecimal initialBalance;
    
    @NotNull(message = "예상 소요 시간은 필수입니다")
    @Min(value = 5, message = "예상 소요 시간은 5분 이상이어야 합니다")
    @Max(value = 480, message = "예상 소요 시간은 480분 이하여야 합니다")
    private Integer estimatedDurationMinutes;
    
    @NotNull(message = "시뮬레이션 시작일은 필수입니다")
    private LocalDate periodStart;
    
    @NotNull(message = "시뮬레이션 종료일은 필수입니다")
    private LocalDate periodEnd;
    
    @NotNull(message = "시간 압축 배율은 필수입니다")
    @Min(value = 1, message = "시간 압축 배율은 1 이상이어야 합니다")
    @Max(value = 100, message = "시간 압축 배율은 100 이하여야 합니다")
    private Integer speedFactor;
    
    private List<String> tags;
    
    private Map<String, Object> successCriteria;
    
    private Map<String, Object> marketScenario;
    
    @Size(max = 1000, message = "학습 목표는 1000자 이하여야 합니다")
    private String learningObjectives;
    
    @Min(value = 1, message = "최대 참여자 수는 1명 이상이어야 합니다")
    @Max(value = 10000, message = "최대 참여자 수는 10,000명 이하여야 합니다")
    private Integer maxParticipants;
    
    @NotEmpty(message = "투자 상품은 최소 1개 이상이어야 합니다")
    @Size(max = 10, message = "투자 상품은 최대 10개까지 가능합니다")
    private List<ChallengeInstrumentRequest> instruments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChallengeInstrumentRequest {
        
        @NotBlank(message = "투자 상품 키는 필수입니다")
        @Size(min = 1, max = 1, message = "투자 상품 키는 1자여야 합니다")
        private String instrumentKey;
        
        @NotBlank(message = "실제 티커는 필수입니다")
        @Size(max = 10, message = "실제 티커는 10자 이하여야 합니다")
        private String actualTicker;
        
        @NotBlank(message = "숨김 이름은 필수입니다")
        @Size(max = 100, message = "숨김 이름은 100자 이하여야 합니다")
        private String hiddenName;
        
        @NotBlank(message = "실제 이름은 필수입니다")
        @Size(max = 100, message = "실제 이름은 100자 이하여야 합니다")
        private String actualName;
        
        @NotBlank(message = "투자 상품 유형은 필수입니다")
        private String type; // STOCK, DEPOSIT, BOND
    }
}