package com.stockquest.application.session.dto;

/**
 * 포트폴리오 조회 쿼리
 */
public record GetPortfolioQuery(
    Long sessionId,
    Long userId
) {
}