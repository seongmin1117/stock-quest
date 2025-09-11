package com.stockquest.domain.order;

/**
 * 주문 유형
 */
public enum OrderType {
    MARKET("시장가"),     // 현재 시장가로 즉시 체결
    LIMIT("지정가");      // 지정된 가격에서 체결
    
    private final String description;
    
    OrderType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}