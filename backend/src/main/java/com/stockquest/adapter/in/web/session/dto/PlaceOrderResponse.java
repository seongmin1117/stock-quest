package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.application.order.port.in.PlaceOrderUseCase.PlaceOrderResult;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 실행 응답 DTO
 */
@Builder
@Schema(description = "주문 실행 응답")
public record PlaceOrderResponse(
    
    @Schema(description = "주문 ID", example = "123")
    Long orderId,
    
    @Schema(description = "상품 키", example = "AAPL")
    String instrumentKey,
    
    @Schema(description = "매수/매도 구분", example = "BUY")
    OrderSide side,
    
    @Schema(description = "체결 수량", example = "10.000000")
    BigDecimal quantity,
    
    @Schema(description = "체결가", example = "152.50")
    BigDecimal executedPrice,
    
    @Schema(description = "슬리피지 비율 (%)", example = "1.25")
    BigDecimal slippageRate,
    
    @Schema(description = "주문 상태", example = "EXECUTED")
    OrderStatus status,
    
    @Schema(description = "체결 시각")
    LocalDateTime executedAt,
    
    @Schema(description = "주문 후 잔액", example = "998475.00")
    BigDecimal newBalance,
    
    @Schema(description = "처리 메시지", example = "주문이 성공적으로 체결되었습니다.")
    String message
) {
    
    public static PlaceOrderResponse from(PlaceOrderResult result) {
        return PlaceOrderResponse.builder()
                .orderId(result.orderId())
                .instrumentKey(result.instrumentKey())
                .side(result.side())
                .quantity(result.quantity())
                .executedPrice(result.executedPrice())
                .slippageRate(result.slippageRate())
                .status(OrderStatus.EXECUTED)
                .executedAt(result.executedAt())
                .newBalance(result.newBalance())
                .message("주문이 성공적으로 체결되었습니다.")
                .build();
    }
}