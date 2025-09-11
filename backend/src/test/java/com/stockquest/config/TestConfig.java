package com.stockquest.config;

import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.port.MarketDataRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 테스트 환경 설정
 * 모킹된 의존성 제공
 */
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public MarketDataRepository mockMarketDataRepository() {
        return new MarketDataRepository() {
            @Override
            public PriceCandle save(PriceCandle candle) {
                return candle;
            }
            
            @Override
            public List<PriceCandle> saveAll(List<PriceCandle> candles) {
                return candles;
            }
            
            @Override
            public Optional<PriceCandle> findByTickerAndDate(String ticker, LocalDate date, CandleTimeframe timeframe) {
                return Optional.of(new PriceCandle(ticker, date, 
                    BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0),
                    BigDecimal.valueOf(95.0), BigDecimal.valueOf(102.0),
                    1000L, timeframe));
            }
            
            @Override
            public List<PriceCandle> findByTickerAndDateBetween(String ticker, LocalDate startDate, 
                                                               LocalDate endDate, CandleTimeframe timeframe) {
                return List.of(new PriceCandle(ticker, startDate, 
                    BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0),
                    BigDecimal.valueOf(95.0), BigDecimal.valueOf(102.0),
                    1000L, timeframe));
            }
            
            @Override
            public Optional<PriceCandle> findLatestByTicker(String ticker, CandleTimeframe timeframe) {
                return Optional.of(new PriceCandle(ticker, LocalDate.now(), 
                    BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0),
                    BigDecimal.valueOf(95.0), BigDecimal.valueOf(102.0),
                    1000L, timeframe));
            }
            
            @Override
            public List<String> findAvailableTickersForDate(LocalDate date, CandleTimeframe timeframe) {
                return List.of("AAPL", "GOOGL", "MSFT");
            }
        };
    }
}