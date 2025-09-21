package com.stockquest.adapter.out.persistence.projection;

import com.stockquest.domain.portfolio.port.PortfolioRepository.PortfolioPositionSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 포트폴리오 포지션 요약 구현체
 * JPA 프로젝션 결과를 도메인 인터페이스로 변환
 */
@Getter
@Builder
@AllArgsConstructor
public class PortfolioPositionSummaryImpl implements PortfolioPositionSummary {

    private final Long sessionId;
    private final Long userId;
    private final String instrumentKey;
    private final BigDecimal currentQuantity;
    private final BigDecimal averagePrice;
    private final BigDecimal currentValue;
    private final Instant lastUpdated;

    /**
     * JPA 프로젝션 결과로부터 도메인 요약 객체 생성
     */
    public static PortfolioPositionSummaryImpl fromProjection(PortfolioPositionSummaryProjection projection) {
        return PortfolioPositionSummaryImpl.builder()
                .sessionId(projection.getSessionId())
                .userId(projection.getUserId())
                .instrumentKey(projection.getInstrumentKey())
                .currentQuantity(projection.getCurrentQuantity())
                .averagePrice(projection.getAveragePrice())
                .currentValue(projection.getCurrentValue())
                .lastUpdated(projection.getLastUpdated().toInstant(ZoneOffset.UTC))
                .build();
    }
}