package com.stockquest.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 시장 데이터 Port
 * Phase 2.3: 비즈니스 로직 고도화 - 실시간 시장 데이터 액세스
 */
public interface MarketDataPort {

    /**
     * 현재 가격 조회 (단일 심볼)
     */
    Optional<BigDecimal> getCurrentPrice(String symbol);

    /**
     * 현재 가격 조회 (다중 심볼)
     */
    Map<String, BigDecimal> getCurrentPrices(List<String> symbols);

    /**
     * 시장 오픈 여부 확인
     */
    boolean isMarketOpen();

    /**
     * 거래량 정보 조회
     */
    Optional<Long> getTradingVolume(String symbol);

    /**
     * 최근 변동률 조회
     */
    Optional<BigDecimal> getPriceChangePercent(String symbol);
}