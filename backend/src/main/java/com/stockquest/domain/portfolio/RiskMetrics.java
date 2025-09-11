package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 리스크 메트릭스 도메인 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetrics {
    
    /**
     * 변동성 (표준편차)
     */
    private Double volatility;
    
    /**
     * VaR (Value at Risk) - 일간
     */
    private BigDecimal dailyVaR;
    
    /**
     * VaR (Value at Risk) - 주간  
     */
    private BigDecimal weeklyVaR;
    
    /**
     * CVaR (Conditional Value at Risk)
     */
    private BigDecimal conditionalVaR;
    
    /**
     * 최대 낙폭 (Maximum Drawdown)
     */
    private Double maxDrawdown;
    
    /**
     * 최대 낙폭 기간 (일수)
     */
    private Integer maxDrawdownDuration;
    
    /**
     * 베타 (시장 민감도)
     */
    private Double beta;
    
    /**
     * 알파 (초과 수익률)
     */
    private Double alpha;
    
    /**
     * 샤프 비율 (Sharpe Ratio)
     */
    private Double sharpeRatio;
    
    /**
     * 소르티노 비율 (Sortino Ratio)
     */
    private Double sortinoRatio;
    
    /**
     * 칼마 비율 (Calmar Ratio)
     */
    private Double calmarRatio;
    
    /**
     * 정보 비율 (Information Ratio)
     */
    private Double informationRatio;
    
    /**
     * 추적 오차 (Tracking Error)
     */
    private Double trackingError;
    
    /**
     * 하방 변동성 (Downside Deviation)
     */
    private Double downsideDeviation;
    
    /**
     * 상방 잠재력 비율 (Upside Potential Ratio)
     */
    private Double upsidePotentialRatio;
    
    /**
     * 집중도 위험 (Concentration Risk)
     */
    private Double concentrationRisk;
    
    /**
     * 유동성 위험 점수
     */
    private Integer liquidityRiskScore;
    
    /**
     * 신용 위험 점수
     */
    private Integer creditRiskScore;
    
    /**
     * 시장 위험 점수
     */
    private Integer marketRiskScore;
    
    /**
     * 종합 위험 점수 (0-100)
     */
    private Integer overallRiskScore;
    
    /**
     * 위험 등급 (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String riskLevel;
    
    /**
     * 신뢰 구간
     */
    private Double confidenceLevel;
    
    /**
     * 계산 기준 시간
     */
    private LocalDateTime calculatedAt;
    
    /**
     * 분석 기간 (일수)
     */
    private Integer analysisPeriod;
    
    /**
     * 종합 위험 점수 계산
     */
    public Integer calculateOverallRiskScore() {
        // 가중평균을 통한 종합 위험 점수 계산
        double score = 0.0;
        
        // 변동성 (30% 가중치)
        if (volatility != null) {
            score += Math.min(volatility * 100, 100) * 0.30;
        }
        
        // 최대 낙폭 (25% 가중치)
        if (maxDrawdown != null) {
            score += Math.min(Math.abs(maxDrawdown), 100) * 0.25;
        }
        
        // 집중도 위험 (20% 가중치)
        if (concentrationRisk != null) {
            score += Math.min(concentrationRisk, 100) * 0.20;
        }
        
        // 기타 위험 점수들 (25% 가중치)
        double otherRisks = 0.0;
        int riskCount = 0;
        
        if (liquidityRiskScore != null) {
            otherRisks += liquidityRiskScore;
            riskCount++;
        }
        if (creditRiskScore != null) {
            otherRisks += creditRiskScore;
            riskCount++;
        }
        if (marketRiskScore != null) {
            otherRisks += marketRiskScore;
            riskCount++;
        }
        
        if (riskCount > 0) {
            score += (otherRisks / riskCount) * 0.25;
        }
        
        return Math.min((int) Math.round(score), 100);
    }
    
    /**
     * 위험 등급 계산
     */
    public String calculateRiskLevel() {
        Integer score = this.overallRiskScore != null ? this.overallRiskScore : calculateOverallRiskScore();
        
        if (score >= 80) return "VERY_HIGH";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        if (score >= 20) return "LOW";
        return "VERY_LOW";
    }
    
    /**
     * 수익-위험 효율성 계산
     */
    public Double calculateRiskAdjustedReturn(Double portfolioReturn) {
        if (volatility == null || volatility == 0.0 || portfolioReturn == null) {
            return 0.0;
        }
        return portfolioReturn / volatility;
    }
}