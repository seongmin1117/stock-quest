package com.stockquest.domain.events.trading;

import com.stockquest.domain.events.DomainEvent;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;

import java.math.BigDecimal;

/**
 * 주문 체결 이벤트
 * 주문이 성공적으로 체결되었을 때 발생
 */
public class OrderExecutedEvent extends DomainEvent {
    
    private final Long orderId;
    private final Long sessionId;
    private final String ticker;
    private final OrderType orderType;
    private final OrderSide side;
    private final Integer quantity;
    private final BigDecimal price;
    private final BigDecimal totalAmount;
    private final BigDecimal commission;
    
    public OrderExecutedEvent(Long orderId, Long sessionId, String userId, String ticker,
                            OrderType orderType, OrderSide side, Integer quantity, 
                            BigDecimal price, BigDecimal totalAmount, BigDecimal commission) {
        super(orderId, userId);
        this.orderId = orderId;
        this.sessionId = sessionId;
        this.ticker = ticker;
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.commission = commission;
    }
    
    @Override
    public int getPriority() {
        return 10; // 높은 우선순위 (즉시 처리 필요)
    }
    
    @Override
    public EventCategory getCategory() {
        return EventCategory.TRADING;
    }
    
    @Override
    public int getExpirationMinutes() {
        return 30; // 30분 내 처리 필요
    }
    
    /**
     * 매수 주문인지 확인
     */
    public boolean isBuyOrder() {
        return OrderSide.BUY.equals(side);
    }
    
    /**
     * 매도 주문인지 확인
     */
    public boolean isSellOrder() {
        return OrderSide.SELL.equals(side);
    }
    
    /**
     * 대량 거래인지 확인 (100주 이상)
     */
    public boolean isLargeOrder() {
        return quantity >= 100;
    }
    
    // Getters
    public Long getOrderId() {
        return orderId;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public String getTicker() {
        return ticker;
    }
    
    public OrderType getOrderType() {
        return orderType;
    }
    
    public OrderSide getSide() {
        return side;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public BigDecimal getCommission() {
        return commission;
    }
    
    @Override
    public String toString() {
        return String.format("OrderExecutedEvent{orderId=%d, sessionId=%d, ticker='%s', side=%s, quantity=%d, price=%s}",
                orderId, sessionId, ticker, side, quantity, price);
    }
}