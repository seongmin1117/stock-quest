package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 성과 메트릭스 도메인 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    
    /**
     * 총 수익률 (%)
     */
    private Double totalReturn;
    
    /**
     * 연환산 수익률 (%)
     */
    private Double annualizedReturn;
    
    /**
     * 일간 수익률 (%)
     */
    private Double dailyReturn;
    
    /**
     * 주간 수익률 (%)
     */
    private Double weeklyReturn;
    
    /**
     * 월간 수익률 (%)
     */
    private Double monthlyReturn;
    
    /**
     * 분기 수익률 (%)
     */
    private Double quarterlyReturn;
    
    /**
     * 연간 수익률 (%)
     */
    private Double yearlyReturn;
    
    /**
     * 최고 수익률 (%)
     */
    private Double maxReturn;
    
    /**
     * 최저 수익률 (%)
     */
    private Double minReturn;
    
    /**
     * 평균 수익률 (%)
     */
    private Double averageReturn;
    
    /**
     * 수익률 중간값 (%)
     */
    private Double medianReturn;
    
    /**
     * 양수 수익률 비율 (승률)
     */
    private Double winRate;
    
    /**
     * 평균 양수 수익률 (%)
     */
    private Double averageWin;
    
    /**
     * 평균 음수 수익률 (%)
     */
    private Double averageLoss;
    
    /**
     * 수익/손실 비율
     */
    private Double profitLossRatio;
    
    /**
     * 연속 상승 최대 기간
     */
    private Integer maxWinningStreak;
    
    /**
     * 연속 하락 최대 기간
     */
    private Integer maxLosingStreak;
    
    /**
     * 현재 연속 상승/하락 기간
     */
    private Integer currentStreak;
    
    /**
     * 복합 연간 성장률 (CAGR)
     */
    private Double cagr;
    
    /**
     * 기하평균 수익률
     */
    private Double geometricMeanReturn;
    
    /**
     * 수익률 왜도 (Skewness)
     */
    private Double returnSkewness;
    
    /**
     * 수익률 첨도 (Kurtosis)
     */
    private Double returnKurtosis;
    
    /**
     * 베스트 퍼포밍 에셋
     */
    private String bestPerformingAsset;
    
    /**
     * 최고 에셋 수익률 (%)
     */
    private Double bestAssetReturn;
    
    /**
     * 워스트 퍼포밍 에셋
     */
    private String worstPerformingAsset;
    
    /**
     * 최저 에셋 수익률 (%)
     */
    private Double worstAssetReturn;
    
    /**
     * 총 거래 횟수
     */
    private Integer totalTrades;
    
    /**
     * 수익 거래 횟수
     */
    private Integer profitableTrades;
    
    /**
     * 손실 거래 횟수
     */
    private Integer unprofitableTrades;
    
    /**
     * 평균 보유 기간 (일)
     */
    private Double averageHoldingPeriod;
    
    /**
     * 회전율 (Turnover Rate)
     */
    private Double turnoverRate;
    
    /**
     * 성과 등급 (A+, A, B+, B, C+, C, D)
     */
    private String performanceGrade;
    
    /**
     * 벤치마크 대비 수익률 (%)
     */
    private Double benchmarkOutperformance;
    
    /**
     * 계산 기준 시간
     */
    private LocalDateTime calculatedAt;
    
    /**
     * 분석 기간 (일수)
     */
    private Integer analysisPeriod;
    
    /**
     * CAGR 계산
     */
    public Double calculateCAGR(BigDecimal beginValue, BigDecimal endValue, Integer years) {
        if (beginValue.compareTo(BigDecimal.ZERO) <= 0 || years == 0) {
            return 0.0;
        }
        
        double ratio = endValue.divide(beginValue, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        return (Math.pow(ratio, 1.0 / years) - 1.0) * 100.0;
    }
    
    /**
     * 성과 등급 계산
     */
    public String calculatePerformanceGrade() {
        if (annualizedReturn == null) return "C";
        
        if (annualizedReturn >= 20.0) return "A+";
        if (annualizedReturn >= 15.0) return "A";
        if (annualizedReturn >= 10.0) return "B+";
        if (annualizedReturn >= 5.0) return "B";
        if (annualizedReturn >= 0.0) return "C+";
        if (annualizedReturn >= -5.0) return "C";
        return "D";
    }
    
    /**
     * 수익/손실 비율 계산
     */
    public Double calculateProfitLossRatio() {
        if (averageLoss == null || averageLoss == 0.0 || averageWin == null) {
            return 0.0;
        }
        return Math.abs(averageWin / averageLoss);
    }
    
    /**
     * 승률 계산
     */
    public Double calculateWinRate() {
        if (totalTrades == null || totalTrades == 0 || profitableTrades == null) {
            return 0.0;
        }
        return (profitableTrades.doubleValue() / totalTrades.doubleValue()) * 100.0;
    }
    
    /**
     * 기대 수익률 계산
     */
    public Double calculateExpectedReturn() {
        if (winRate == null || averageWin == null || averageLoss == null) {
            return 0.0;
        }
        
        double winProb = winRate / 100.0;
        double lossProb = 1.0 - winProb;
        
        return (winProb * averageWin) + (lossProb * averageLoss);
    }
}