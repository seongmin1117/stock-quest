package com.stockquest.domain.market.port;

import com.stockquest.domain.market.PriceCandle;
import java.time.LocalDate;
import java.util.List;

/**
 * 외부 시장 데이터 클라이언트 포트 (출력 포트)
 * Yahoo Finance 등 외부 데이터 소스에서 시장 데이터를 가져오는 인터페이스
 */
public interface ExternalMarketDataClient {
    
    /**
     * 특정 티커의 기간별 일봉 데이터 조회
     */
    List<PriceCandle> fetchDailyCandles(String ticker, LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 티커의 최신 가격 조회
     */
    PriceCandle fetchLatestPrice(String ticker);
    
    /**
     * 여러 티커의 최신 가격 일괄 조회
     */
    List<PriceCandle> fetchLatestPrices(List<String> tickers);
}