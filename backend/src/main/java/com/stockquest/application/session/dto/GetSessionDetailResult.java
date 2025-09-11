package com.stockquest.application.session.dto;

import com.stockquest.domain.order.Order;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.session.ChallengeSession;

import java.util.List;

/**
 * 세션 상세 조회 결과
 */
public record GetSessionDetailResult(
    ChallengeSession session,
    String challengeTitle,
    List<PortfolioPosition> portfolio,
    List<Order> orders
) {
}