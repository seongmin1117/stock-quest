package com.stockquest.domain.order;

/**
 * 주문 상태
 */
public enum OrderStatus {
    PENDING("대기중"),     // 주문 접수됨, 아직 체결되지 않음
    EXECUTED("체결"),      // 주문 체결 완료
    CANCELLED("취소");     // 주문 취소됨
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}