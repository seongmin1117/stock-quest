package com.stockquest.application.order.port.in;

import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 접수 유스케이스 (입력 포트)
 */
public interface PlaceOrderUseCase {
    
    /**
     * 주문 접수 및 체결
     */
    PlaceOrderResult placeOrder(PlaceOrderCommand command);
    
    record PlaceOrderCommand(
        Long sessionId,
        String instrumentKey,
        OrderSide side,
        BigDecimal quantity,
        OrderType orderType,
        BigDecimal limitPrice  // 지정가 주문 시만 필요
    ) {}
    
    record PlaceOrderResult(
        Long orderId,
        String instrumentKey,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal executedPrice,
        BigDecimal slippageRate,
        LocalDateTime executedAt,
        BigDecimal newBalance
    ) {}
}