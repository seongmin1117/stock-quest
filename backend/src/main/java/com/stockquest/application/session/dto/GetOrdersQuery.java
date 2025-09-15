package com.stockquest.application.session.dto;

/**
 * 주문 내역 조회 쿼리
 */
public record GetOrdersQuery(
    Long sessionId,
    Long userId
) {
}