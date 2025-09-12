package com.stockquest.application.marketdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 시장 데이터 조회 서비스
 * 캐싱을 활용한 가격 정보 및 차트 데이터 제공 (Cache Warmup 전용)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class GetMarketDataService {
    
    /**
     * 최신 가격 조회 (캐시 워밍업용 - 실제 구현은 별도 필요)
     */
    @Cacheable(value = "latestPrice", key = "#ticker", cacheManager = "cacheManager")
    public BigDecimal getLatestPrice(String ticker) {
        log.debug("Cache warming up latest price for ticker: {}", ticker);
        return BigDecimal.valueOf(100.0); // 임시 값
    }
    
    /**
     * 일간 캔들 데이터 조회 (캐시 워밍업용 - 실제 구현은 별도 필요)
     */
    @Cacheable(value = "dailyCandles", key = "#ticker + '_' + #startDate + '_' + #endDate", cacheManager = "cacheManager")
    public List<Object> getDailyCandles(String ticker, LocalDate startDate, LocalDate endDate) {
        log.debug("Cache warming up daily candles for ticker: {} from {} to {}", ticker, startDate, endDate);
        return List.of(); // 임시 값
    }
    
    /**
     * 거래량 상위 종목 조회 (캐시 워밍업용 - 실제 구현은 별도 필요)
     */
    @Cacheable(value = "topVolumeStocks", key = "#hours", cacheManager = "cacheManager")
    public List<String> getTopVolumeStocks(int hours) {
        log.debug("Cache warming up top volume stocks for hours: {}", hours);
        return List.of("AAPL", "GOOGL", "MSFT"); // 임시 값
    }
}