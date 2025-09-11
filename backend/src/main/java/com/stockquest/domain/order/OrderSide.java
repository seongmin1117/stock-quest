package com.stockquest.domain.order;

/**
 * 주문 방향 (매수/매도)
 */
public enum OrderSide {
    BUY("매수"),
    SELL("매도");
    
    private final String description;
    
    OrderSide(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}