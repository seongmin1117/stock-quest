package com.stockquest.adapter.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 포지션 요약 프로젝션 인터페이스
 * 성능 최적화를 위해 필요한 필드만 선택적으로 조회
 */
public interface PortfolioPositionSummaryProjection {

    /**
     * 세션 ID
     */
    Long getSessionId();

    /**
     * 사용자 ID
     */
    Long getUserId();

    /**
     * 상품 키 (종목 코드)
     */
    String getInstrumentKey();

    /**
     * 현재 보유 수량
     */
    BigDecimal getCurrentQuantity();

    /**
     * 평균 매수 가격
     */
    BigDecimal getAveragePrice();

    /**
     * 현재 포지션 가치 (수량 × 평균가격)
     */
    BigDecimal getCurrentValue();

    /**
     * 마지막 업데이트 시간
     */
    LocalDateTime getLastUpdated();
}