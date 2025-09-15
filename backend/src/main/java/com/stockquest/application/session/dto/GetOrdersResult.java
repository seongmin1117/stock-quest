package com.stockquest.application.session.dto;

import com.stockquest.domain.order.Order;

import java.util.List;

/**
 * 주문 내역 조회 결과
 */
public record GetOrdersResult(
    Long sessionId,
    List<Order> orders
) {
}