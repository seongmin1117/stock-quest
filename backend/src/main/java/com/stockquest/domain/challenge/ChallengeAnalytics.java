package com.stockquest.domain.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 분석 데이터 도메인 엔티티
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeAnalytics {
    private Long id;
    private Long challengeId;
    private Integer totalParticipants;
    private Integer completedParticipants;
    private BigDecimal averageReturnRate;
    private BigDecimal medianReturnRate;
    private BigDecimal bestReturnRate;
    private BigDecimal worstReturnRate;
    private Integer averageCompletionTimeMinutes;
    private BigDecimal successRatePercentage;
    private BigDecimal engagementScore;     // Based on actions per minute, etc.
    private BigDecimal difficultyRating;   // User-reported difficulty (1-5)
    private BigDecimal ratingAverage;      // User ratings (1-5)
    private Integer ratingCount;
    private LocalDateTime lastCalculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 성공률 계산
     */
    public BigDecimal calculateSuccessRate() {
        if (totalParticipants == null || totalParticipants == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(completedParticipants)
                .divide(BigDecimal.valueOf(totalParticipants), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 난이도 등급 반환
     */
    public String getDifficultyGrade() {
        if (difficultyRating == null) return "N/A";
        
        double rating = difficultyRating.doubleValue();
        if (rating <= 1.5) return "매우 쉬움";
        else if (rating <= 2.5) return "쉬움";
        else if (rating <= 3.5) return "보통";
        else if (rating <= 4.5) return "어려움";
        else return "매우 어려움";
    }
    
    /**
     * 추천도 등급 반환
     */
    public String getRecommendationGrade() {
        if (ratingAverage == null) return "N/A";
        
        double rating = ratingAverage.doubleValue();
        if (rating <= 1.5) return "비추천";
        else if (rating <= 2.5) return "보통";
        else if (rating <= 3.5) return "추천";
        else if (rating <= 4.5) return "강력 추천";
        else return "최고 추천";
    }
}