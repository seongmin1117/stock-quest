package com.stockquest.domain.market.port;

import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.CandleTimeframe;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 시장 데이터 저장소 포트 (출력 포트)
 */
public interface MarketDataRepository {
    
    /**
     * 가격 캔들 저장
     */
    PriceCandle save(PriceCandle candle);
    
    /**
     * 가격 캔들 일괄 저장
     */
    List<PriceCandle> saveAll(List<PriceCandle> candles);
    
    /**
     * 특정 티커의 특정 날짜 캔들 조회
     */
    Optional<PriceCandle> findByTickerAndDate(String ticker, LocalDate date, CandleTimeframe timeframe);
    
    /**
     * 특정 티커의 기간별 캔들 조회
     */
    List<PriceCandle> findByTickerAndDateBetween(String ticker, LocalDate startDate, 
                                                LocalDate endDate, CandleTimeframe timeframe);
    
    /**
     * 특정 티커의 최신 캔들 조회
     */
    Optional<PriceCandle> findLatestByTicker(String ticker, CandleTimeframe timeframe);
    
    /**
     * 특정 날짜에 데이터가 있는 모든 티커 조회
     */
    List<String> findAvailableTickersForDate(LocalDate date, CandleTimeframe timeframe);
}