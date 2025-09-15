package com.stockquest.application.session.dto;

import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.session.ChallengeSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * 포트폴리오 조회 결과
 */
public record GetPortfolioResult(
    Long sessionId,
    BigDecimal totalValue,
    BigDecimal cashBalance,
    List<PortfolioPosition> positions
) {
}