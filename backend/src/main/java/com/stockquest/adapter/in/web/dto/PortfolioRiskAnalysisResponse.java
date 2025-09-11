package com.stockquest.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 포트폴리오 리스크 분석 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRiskAnalysisResponse {
    
    /**
     * 분석 기준 시간
     */
    private LocalDateTime analysisTime;
    
    /**
     * 신뢰수준
     */
    private Double confidenceLevel;
    
    /**
     * VaR (Value at Risk)
     */
    private BigDecimal valueAtRisk;
    
    /**
     * CVaR (Conditional Value at Risk)
     */
    private BigDecimal conditionalVaR;
    
    /**
     * 최대 낙폭 (Maximum Drawdown)
     */
    private Double maxDrawdown;
    
    /**
     * 샤프 비율 (Sharpe Ratio)
     */
    private Double sharpeRatio;
    
    /**
     * 베타 (Beta)
     */
    private Double beta;
    
    /**
     * 변동성 (Volatility)
     */
    private Double volatility;
    
    /**
     * 상관관계 (Correlation with benchmark)
     */
    private Double correlation;
    
    /**
     * 위험 분산 분석
     */
    private Map<String, RiskBreakdown> riskBreakdown;
    
    /**
     * 리스크 등급 (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String riskLevel;
    
    /**
     * 리스크 점수 (0-100)
     */
    private Integer riskScore;
    
    /**
     * 주요 위험 요소
     */
    private String primaryRiskFactors;
    
    /**
     * 리스크 관리 권고사항
     */
    private String recommendations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskBreakdown {
        
        /**
         * 자산 클래스별 리스크 기여도
         */
        private Double contribution;
        
        /**
         * 개별 자산 변동성
         */
        private Double individualVolatility;
        
        /**
         * 포트폴리오 내 비중
         */
        private Double weight;
        
        /**
         * 리스크 조정 수익률
         */
        private Double riskAdjustedReturn;
    }
}