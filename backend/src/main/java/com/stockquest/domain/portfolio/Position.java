package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포지션 도메인 엔터티 (임시 구현)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    
    private Long id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private String positionType; // LONG, SHORT
    private LocalDateTime entryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 포지션의 현재 시장 가치 계산
     */
    public BigDecimal getValue() {
        if (quantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice);
    }
    
    /**
     * 포지션의 현재 시장 가치 계산 (alias method)
     */
    public BigDecimal getCurrentValue() {
        return getValue();
    }
    
    /**
     * 포지션의 손익 계산
     */
    public BigDecimal getProfitLoss() {
        if (quantity == null || currentPrice == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice.subtract(averagePrice));
    }
    
    /**
     * 포지션의 손익률 계산 (%)
     */
    public BigDecimal getProfitLossPercentage() {
        if (averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profitLoss = getProfitLoss();
        BigDecimal totalCost = quantity.multiply(averagePrice);
        
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return profitLoss.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 포지션 비중 계산 (전체 포트폴리오 대비)
     */
    public BigDecimal getWeight(BigDecimal totalPortfolioValue) {
        if (totalPortfolioValue == null || totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getValue().divide(totalPortfolioValue, 4, java.math.RoundingMode.HALF_UP);
    }
}