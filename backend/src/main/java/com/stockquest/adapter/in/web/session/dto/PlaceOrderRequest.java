package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.adapter.in.web.common.validation.ValidInstrumentKey;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 주문 실행 요청 DTO
 */
@Schema(description = "주문 실행 요청")
public record PlaceOrderRequest(
    
    @NotNull(message = "상품 키는 필수입니다")
    @ValidInstrumentKey
    @Schema(description = "상품 키 (AAPL, MSFT 등)", example = "AAPL")
    String instrumentKey,
    
    @NotNull(message = "주문 구분은 필수입니다")
    @Schema(description = "주문 구분 (매수/매도)", example = "BUY")
    OrderSide side,
    
    @NotNull(message = "주문 수량은 필수입니다")
    @DecimalMin(value = "0.000001", message = "주문 수량은 0보다 커야 합니다")
    @DecimalMax(value = "999999999", message = "주문 수량이 너무 큩니다")
    @Digits(integer = 9, fraction = 6, message = "주문 수량 형식이 올바르지 않습니다")
    @Schema(description = "주문 수량", example = "10")
    BigDecimal quantity,
    
    @NotNull(message = "주문 유형은 필수입니다")
    @Schema(description = "주문 유형 (시장가/지정가)", example = "MARKET")
    OrderType orderType,
    
    @DecimalMin(value = "0.01", message = "지정가는 0.01 이상이어야 합니다")
    @DecimalMax(value = "999999999", message = "지정가가 너무 큩니다")
    @Digits(integer = 9, fraction = 2, message = "지정가 형식이 올바르지 않습니다")
    @Schema(description = "지정가 (지정가 주문 시에만 필요)", example = "150.00")
    BigDecimal limitPrice
    
) {
}