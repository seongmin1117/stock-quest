package com.stockquest.adapter.out.market;

import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.port.ExternalMarketDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Yahoo Finance API 연동 어댑터
 * ExternalMarketDataClient 포트의 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Primary
public class YahooFinanceAdapter implements ExternalMarketDataClient {
    
    private final WebClient webClient;
    
    @Value("${yahoo-finance.base-url:https://query1.finance.yahoo.com}")
    private String baseUrl;
    
    @Value("${yahoo-finance.timeout:5000}")
    private int timeoutMs;
    
    /**
     * 특정 티커의 기간별 일봉 데이터 조회
     */
    @Override
    @Cacheable(value = "dailyCandles", key = "#ticker + '_' + #startDate + '_' + #endDate", 
               unless = "#result.isEmpty()")
    public List<PriceCandle> fetchDailyCandles(String ticker, LocalDate startDate, LocalDate endDate) {
        log.info("Yahoo Finance에서 일봉 데이터 조회: {} ({} ~ {})", ticker, startDate, endDate);
        
        try {
            // Unix timestamp 변환
            long startTimestamp = startDate.toEpochDay() * 86400;
            long endTimestamp = endDate.plusDays(1).toEpochDay() * 86400; // 종료일 포함
            
            // Yahoo Finance API 호출
            String url = String.format("/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d", 
                                     ticker, startTimestamp, endTimestamp);
            
            YahooFinanceResponse response = webClient.get()
                .uri(baseUrl + url)
                .retrieve()
                .bodyToMono(YahooFinanceResponse.class)
                .timeout(java.time.Duration.ofMillis(timeoutMs))
                .doOnError(error -> log.error("Yahoo Finance API 호출 실패: {}, ticker: {}", error.getMessage(), ticker))
                .block();
            
            if (response == null || response.getChart() == null || response.getChart().getResult() == null) {
                log.warn("Yahoo Finance 응답 데이터 없음: {}", ticker);
                return new ArrayList<>();
            }
            
            return convertToPriceCandles(response, ticker);
            
        } catch (WebClientResponseException e) {
            log.error("Yahoo Finance API HTTP 오류: status={}, ticker={}, message={}", 
                     e.getStatusCode(), ticker, e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Yahoo Finance 데이터 조회 중 오류 발생: ticker={}, error={}", ticker, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 특정 티커의 최신 가격 조회
     */
    @Override
    @Cacheable(value = "latestPrice", key = "#ticker", unless = "#result == null")
    public PriceCandle fetchLatestPrice(String ticker) {
        log.info("Yahoo Finance에서 최신 가격 조회: {}", ticker);
        
        // 최근 5일 데이터를 조회해서 가장 최신 데이터 반환
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(5);
        
        List<PriceCandle> candles = fetchDailyCandles(ticker, startDate, endDate);
        
        if (candles.isEmpty()) {
            log.warn("최신 가격 데이터 없음: {}", ticker);
            return null;
        }
        
        // 가장 최신 데이터 반환
        return candles.stream()
                     .max((c1, c2) -> c1.getDate().compareTo(c2.getDate()))
                     .orElse(null);
    }
    
    /**
     * 여러 티커의 최신 가격 일괄 조회
     */
    @Override
    public List<PriceCandle> fetchLatestPrices(List<String> tickers) {
        log.info("여러 티커 최신 가격 일괄 조회: {}", tickers);
        
        List<PriceCandle> result = new ArrayList<>();
        
        // 병렬 처리로 성능 향상
        tickers.parallelStream()
               .forEach(ticker -> {
                   PriceCandle candle = fetchLatestPrice(ticker);
                   if (candle != null) {
                       synchronized (result) {
                           result.add(candle);
                       }
                   }
               });
        
        log.info("일괄 조회 완료: 요청={}, 성공={}", tickers.size(), result.size());
        return result;
    }
    
    /**
     * Yahoo Finance API 응답을 PriceCandle 객체로 변환
     */
    private List<PriceCandle> convertToPriceCandles(YahooFinanceResponse response, String ticker) {
        List<PriceCandle> candles = new ArrayList<>();
        
        try {
            var result = response.getChart().getResult().get(0);
            var timestamps = result.getTimestamp();
            var indicators = result.getIndicators();
            
            if (indicators == null || indicators.getQuote() == null || indicators.getQuote().isEmpty()) {
                log.warn("Yahoo Finance 응답에 quote 데이터 없음: {}", ticker);
                return candles;
            }
            
            var quote = indicators.getQuote().get(0);
            var opens = quote.getOpen();
            var highs = quote.getHigh();
            var lows = quote.getLow();
            var closes = quote.getClose();
            var volumes = quote.getVolume();
            
            // 데이터 검증
            if (timestamps == null || opens == null || highs == null || 
                lows == null || closes == null || volumes == null) {
                log.warn("Yahoo Finance 응답 데이터 불완전: {}", ticker);
                return candles;
            }
            
            int dataSize = timestamps.size();
            if (opens.size() != dataSize || highs.size() != dataSize || 
                lows.size() != dataSize || closes.size() != dataSize) {
                log.warn("Yahoo Finance 데이터 크기 불일치: {} (timestamp={}, ohlc 크기 다름)", ticker, dataSize);
                return candles;
            }
            
            // PriceCandle 객체 생성
            for (int i = 0; i < dataSize; i++) {
                Long timestamp = timestamps.get(i);
                Double open = opens.get(i);
                Double high = highs.get(i);
                Double low = lows.get(i);
                Double close = closes.get(i);
                Long volume = volumes.get(i);
                
                // null 값 검증
                if (timestamp == null || open == null || high == null || 
                    low == null || close == null) {
                    log.debug("Yahoo Finance 데이터에 null 값 포함 (index={}): {}", i, ticker);
                    continue;
                }
                
                try {
                    LocalDate date = LocalDate.ofEpochDay(timestamp / 86400);
                    
                    PriceCandle candle = new PriceCandle(
                        ticker,
                        date,
                        BigDecimal.valueOf(open).setScale(2, BigDecimal.ROUND_HALF_UP),
                        BigDecimal.valueOf(high).setScale(2, BigDecimal.ROUND_HALF_UP),
                        BigDecimal.valueOf(low).setScale(2, BigDecimal.ROUND_HALF_UP),
                        BigDecimal.valueOf(close).setScale(2, BigDecimal.ROUND_HALF_UP),
                        volume != null ? volume : 0L,
                        CandleTimeframe.DAILY
                    );
                    
                    candles.add(candle);
                    
                } catch (Exception e) {
                    log.debug("PriceCandle 변환 실패 (index={}): ticker={}, error={}", i, ticker, e.getMessage());
                }
            }
            
            log.info("Yahoo Finance 데이터 변환 완료: ticker={}, 원본={}, 변환={}", ticker, dataSize, candles.size());
            
        } catch (Exception e) {
            log.error("Yahoo Finance 응답 파싱 중 오류: ticker={}, error={}", ticker, e.getMessage(), e);
        }
        
        return candles;
    }
    
    /**
     * Yahoo Finance API 응답 DTO 클래스들
     */
    public static class YahooFinanceResponse {
        private Chart chart;
        
        public Chart getChart() { return chart; }
        public void setChart(Chart chart) { this.chart = chart; }
    }
    
    public static class Chart {
        private List<Result> result;
        private Object error;
        
        public List<Result> getResult() { return result; }
        public void setResult(List<Result> result) { this.result = result; }
        public Object getError() { return error; }
        public void setError(Object error) { this.error = error; }
    }
    
    public static class Result {
        private Meta meta;
        private List<Long> timestamp;
        private Indicators indicators;
        
        public Meta getMeta() { return meta; }
        public void setMeta(Meta meta) { this.meta = meta; }
        public List<Long> getTimestamp() { return timestamp; }
        public void setTimestamp(List<Long> timestamp) { this.timestamp = timestamp; }
        public Indicators getIndicators() { return indicators; }
        public void setIndicators(Indicators indicators) { this.indicators = indicators; }
    }
    
    public static class Meta {
        private String currency;
        private String symbol;
        private String exchangeName;
        private String instrumentType;
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getExchangeName() { return exchangeName; }
        public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }
        public String getInstrumentType() { return instrumentType; }
        public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
    }
    
    public static class Indicators {
        private List<Quote> quote;
        
        public List<Quote> getQuote() { return quote; }
        public void setQuote(List<Quote> quote) { this.quote = quote; }
    }
    
    public static class Quote {
        private List<Double> open;
        private List<Double> high;
        private List<Double> low;
        private List<Double> close;
        private List<Long> volume;
        
        public List<Double> getOpen() { return open; }
        public void setOpen(List<Double> open) { this.open = open; }
        public List<Double> getHigh() { return high; }
        public void setHigh(List<Double> high) { this.high = high; }
        public List<Double> getLow() { return low; }
        public void setLow(List<Double> low) { this.low = low; }
        public List<Double> getClose() { return close; }
        public void setClose(List<Double> close) { this.close = close; }
        public List<Long> getVolume() { return volume; }
        public void setVolume(List<Long> volume) { this.volume = volume; }
    }
}