package com.stockquest.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 상관관계 분석 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationResponse {
    
    /**
     * 분석 기준 시간
     */
    private LocalDateTime analysisTime;
    
    /**
     * 주식 심볼 1
     */
    private String symbol1;
    
    /**
     * 주식 심볼 2
     */
    private String symbol2;
    
    /**
     * 상관계수 (-1.0 ~ 1.0)
     */
    private Double correlationCoefficient;
    
    /**
     * 분석 기간
     */
    private String timeframe;
    
    /**
     * 상관관계 강도 분류 (VERY_STRONG, STRONG, MODERATE, WEAK, VERY_WEAK)
     */
    private String correlationStrength;
    
    /**
     * 상관관계 방향 (POSITIVE, NEGATIVE, NEUTRAL)
     */
    private String correlationDirection;
    
    /**
     * 통계적 유의성 (p-value)
     */
    private Double pValue;
    
    /**
     * 신뢰도 (%)
     */
    private Double confidenceLevel;
    
    /**
     * 기간별 상관계수 변화
     */
    private Map<String, Double> periodCorrelations;
    
    /**
     * 분산 설명력 (R-squared)
     */
    private Double rSquared;
    
    /**
     * 상관관계 안정성 점수 (0-100)
     */
    private Integer stabilityScore;
    
    /**
     * 포트폴리오 분산 기여도
     */
    private Double diversificationBenefit;
    
    /**
     * 분석 해석
     */
    private String interpretation;
    
    /**
     * 투자 시사점
     */
    private String investmentImplications;
}