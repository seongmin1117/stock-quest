package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.application.session.dto.GetOrdersResult;
import com.stockquest.domain.order.Order;

import java.util.List;

/**
 * 주문 내역 조회 응답 DTO
 */
public record OrdersResponse(
    Long sessionId,
    List<Order> orders
) {
    public static OrdersResponse from(GetOrdersResult result) {
        return new OrdersResponse(
                result.sessionId(),
                result.orders()
        );
    }
}