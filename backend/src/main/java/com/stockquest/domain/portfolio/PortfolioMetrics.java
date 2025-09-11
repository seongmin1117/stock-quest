package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 기본 메트릭스 도메인 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioMetrics {
    
    /**
     * 총 포트폴리오 가치
     */
    private BigDecimal totalValue;
    
    /**
     * 투자 원금
     */
    private BigDecimal initialInvestment;
    
    /**
     * 총 수익/손실
     */
    private BigDecimal totalGainLoss;
    
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
    private Double dayReturn;
    
    /**
     * 포지션 수
     */
    private Integer positionCount;
    
    /**
     * 현금 보유량
     */
    private BigDecimal cashBalance;
    
    /**
     * 투자된 금액
     */
    private BigDecimal investedAmount;
    
    /**
     * 투자 비율 (%)
     */
    private Double investmentRatio;
    
    /**
     * 최고 수익률 달성 일자
     */
    private LocalDateTime highWaterMark;
    
    /**
     * 최고 수익률
     */
    private Double peakReturn;
    
    /**
     * 현재 낙폭 (%)
     */
    private Double currentDrawdown;
    
    /**
     * 거래 횟수
     */
    private Integer tradeCount;
    
    /**
     * 평균 포지션 크기
     */
    private BigDecimal averagePositionSize;
    
    /**
     * 계산 기준 시간
     */
    private LocalDateTime calculatedAt;
    
    /**
     * 수익률 계산
     */
    public Double calculateReturn() {
        if (initialInvestment.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return totalGainLoss.divide(initialInvestment, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100.0))
                .doubleValue();
    }
    
    /**
     * 투자 비율 계산
     */
    public Double calculateInvestmentRatio() {
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return investedAmount.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100.0))
                .doubleValue();
    }
    
    /**
     * 현재 낙폭 계산
     */
    public Double calculateCurrentDrawdown() {
        if (peakReturn == null || peakReturn == 0.0) {
            return 0.0;
        }
        return Math.min(0.0, (totalReturn - peakReturn) / peakReturn * 100.0);
    }
}