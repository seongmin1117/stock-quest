package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.application.session.dto.GetPortfolioResult;
import com.stockquest.domain.portfolio.PortfolioPosition;

import java.math.BigDecimal;
import java.util.List;

/**
 * 포트폴리오 조회 응답 DTO
 */
public record PortfolioResponse(
    Long sessionId,
    BigDecimal totalValue,
    BigDecimal cashBalance,
    List<PortfolioPosition> positions
) {
    public static PortfolioResponse from(GetPortfolioResult result) {
        return new PortfolioResponse(
                result.sessionId(),
                result.totalValue(),
                result.cashBalance(),
                result.positions()
        );
    }
}