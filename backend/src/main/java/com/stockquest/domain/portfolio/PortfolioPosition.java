package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 포트폴리오 포지션 도메인 엔티티
 * 세션 내에서 특정 상품의 보유 현황
 */
@Getter
@Builder
@AllArgsConstructor
public class PortfolioPosition {
    private Long id;
    private Long sessionId;
    private String instrumentKey;
    private BigDecimal quantity;        // 보유 수량
    private BigDecimal averagePrice;    // 평균 매입가
    private BigDecimal totalCost;       // 총 매입 비용
    
    protected PortfolioPosition() {}
    
    public PortfolioPosition(Long sessionId, String instrumentKey) {
        validateSessionId(sessionId);
        validateInstrumentKey(instrumentKey);
        
        this.sessionId = sessionId;
        this.instrumentKey = instrumentKey;
        this.quantity = BigDecimal.ZERO;
        this.averagePrice = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }
    
    private void validateSessionId(Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("유효한 세션 ID가 필요합니다");
        }
    }
    
    private void validateInstrumentKey(String instrumentKey) {
        if (instrumentKey == null || instrumentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 키는 필수입니다");
        }
    }
    
    /**
     * 매수 주문 처리 - 보유량 증가 및 평균가 재계산
     */
    public void addPosition(BigDecimal buyQuantity, BigDecimal buyPrice) {
        if (buyQuantity == null || buyQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매수 수량은 0보다 커야 합니다");
        }
        if (buyPrice == null || buyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매수 가격은 0보다 커야 합니다");
        }
        
        BigDecimal newTotalCost = this.totalCost.add(buyQuantity.multiply(buyPrice));
        BigDecimal newQuantity = this.quantity.add(buyQuantity);
        
        this.quantity = newQuantity;
        this.totalCost = newTotalCost;
        this.averagePrice = newTotalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * 매도 주문 처리 - 보유량 감소 (FIFO 방식)
     */
    public BigDecimal reducePosition(BigDecimal sellQuantity, BigDecimal sellPrice) {
        if (sellQuantity == null || sellQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매도 수량은 0보다 커야 합니다");
        }
        if (sellPrice == null || sellPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매도 가격은 0보다 커야 합니다");
        }
        if (sellQuantity.compareTo(this.quantity) > 0) {
            throw new IllegalArgumentException("보유 수량보다 많이 매도할 수 없습니다");
        }
        
        // 매도한 부분의 평균 매입가로 손익 계산
        BigDecimal soldCost = sellQuantity.multiply(this.averagePrice);
        BigDecimal soldValue = sellQuantity.multiply(sellPrice);
        BigDecimal realizedPnL = soldValue.subtract(soldCost);
        
        // 보유량 및 총 비용 업데이트
        this.quantity = this.quantity.subtract(sellQuantity);
        this.totalCost = this.totalCost.subtract(soldCost);
        
        // 보유량이 0이 되면 평균가도 0으로 리셋
        if (this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.averagePrice = BigDecimal.ZERO;
            this.totalCost = BigDecimal.ZERO;
        }
        
        return realizedPnL;
    }
    
    /**
     * 현재 시장가 기준 평가손익 계산
     */
    public BigDecimal calculateUnrealizedPnL(BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal currentValue = this.quantity.multiply(currentPrice);
        return currentValue.subtract(this.totalCost);
    }
    
    /**
     * 현재 시장가 기준 총 평가금액 계산
     */
    public BigDecimal calculateCurrentValue(BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return this.quantity.multiply(currentPrice);
    }
    
    public boolean hasPosition() {
        return this.quantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
}