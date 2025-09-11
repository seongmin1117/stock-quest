package com.stockquest.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 도메인 엔티티
 * 챌린지 세션 내에서 발생하는 모든 거래 주문
 */
@Getter
@Builder
@AllArgsConstructor
public class Order {
    private Long id;
    private Long sessionId;
    private String instrumentKey;    // 챌린지 내 상품 키 (A, B, C, ...)
    private OrderSide side;          // 매수/매도
    private BigDecimal quantity;     // 수량
    private OrderType orderType;     // 시장가/지정가
    private BigDecimal limitPrice;   // 지정가 (시장가의 경우 null)
    private BigDecimal executedPrice; // 체결가
    private BigDecimal slippageRate; // 슬리피지 비율 (1-2%)
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private LocalDateTime executedAt;
    
    protected Order() {}
    
    public Order(Long sessionId, String instrumentKey, OrderSide side, 
                BigDecimal quantity, OrderType orderType, BigDecimal limitPrice) {
        validateSessionId(sessionId);
        validateInstrumentKey(instrumentKey);
        validateQuantity(quantity);
        validateOrderType(orderType, limitPrice);
        
        this.sessionId = sessionId;
        this.instrumentKey = instrumentKey;
        this.side = side;
        this.quantity = quantity;
        this.orderType = orderType;
        this.limitPrice = limitPrice;
        this.status = OrderStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
        this.slippageRate = BigDecimal.ZERO; // 기본값
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
    
    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }
    }
    
    private void validateOrderType(OrderType orderType, BigDecimal limitPrice) {
        if (orderType == null) {
            throw new IllegalArgumentException("주문 유형은 필수입니다");
        }
        if (orderType == OrderType.LIMIT && 
            (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("지정가 주문은 유효한 가격이 필요합니다");
        }
        if (orderType == OrderType.MARKET && limitPrice != null) {
            throw new IllegalArgumentException("시장가 주문에는 지정가를 설정할 수 없습니다");
        }
    }
    
    public void execute(BigDecimal marketPrice, BigDecimal slippageRate) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 체결할 수 있습니다");
        }
        
        validateMarketPrice(marketPrice);
        
        // 슬리피지 적용하여 체결가 계산
        BigDecimal finalPrice = calculateExecutionPrice(marketPrice, slippageRate);
        
        this.executedPrice = finalPrice;
        this.slippageRate = slippageRate;
        this.status = OrderStatus.EXECUTED;
        this.executedAt = LocalDateTime.now();
    }
    
    private void validateMarketPrice(BigDecimal marketPrice) {
        if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("유효한 시장가가 필요합니다");
        }
    }
    
    private BigDecimal calculateExecutionPrice(BigDecimal marketPrice, BigDecimal slippageRate) {
        if (slippageRate == null) {
            slippageRate = BigDecimal.ZERO;
        }
        
        BigDecimal slippageMultiplier = BigDecimal.ONE;
        if (side == OrderSide.BUY) {
            // 매수 시 슬리피지로 인해 더 높은 가격에 체결
            slippageMultiplier = BigDecimal.ONE.add(slippageRate.divide(new BigDecimal("100")));
        } else {
            // 매도 시 슬리피지로 인해 더 낮은 가격에 체결
            slippageMultiplier = BigDecimal.ONE.subtract(slippageRate.divide(new BigDecimal("100")));
        }
        
        return marketPrice.multiply(slippageMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 취소할 수 있습니다");
        }
        this.status = OrderStatus.CANCELLED;
    }
    
    public BigDecimal getTotalValue() {
        if (executedPrice == null) {
            return BigDecimal.ZERO;
        }
        return executedPrice.multiply(quantity);
    }
    
}