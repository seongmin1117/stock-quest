package com.stockquest.adapter.out.market;

import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.port.ExternalMarketDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Yahoo Finance API를 사용한 시장 데이터 클라이언트 구현체
 *
 * 주의: 실제 프로덕션에서는 Yahoo Finance의 공식 API를 사용하거나
 * 라이선스가 있는 데이터 제공업체를 사용해야 합니다.
 * 이 구현체는 교육 목적의 시뮬레이션용입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class YahooFinanceMarketDataClient implements ExternalMarketDataClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // Yahoo Finance API endpoints (이것은 예시이며 실제 API는 다를 수 있음)
    private static final String BASE_URL = "https://query1.finance.yahoo.com";
    private static final String QUOTE_URL = BASE_URL + "/v8/finance/chart/{symbol}";

    // 한국 주식을 Yahoo Finance 심볼로 변환 (예: 005930 -> 005930.KS)
    private static final Map<String, String> SYMBOL_MAPPING = Map.of(
        "005930", "005930.KS",  // 삼성전자
        "000660", "000660.KS",  // SK하이닉스
        "373220", "373220.KS",  // LG에너지솔루션
        "035720", "035720.KS",  // 카카오
        "035420", "035420.KS",  // 네이버
        "005380", "005380.KS",  // 현대차
        "000270", "000270.KS"   // 기아
    );

    // 시뮬레이션용 더미 데이터 (실제로는 Yahoo Finance API 호출)
    private static final Map<String, BigDecimal> SIMULATED_PRICES = Map.of(
        "005930", BigDecimal.valueOf(75000),   // 삼성전자
        "000660", BigDecimal.valueOf(120000),  // SK하이닉스
        "373220", BigDecimal.valueOf(450000),  // LG에너지솔루션
        "035720", BigDecimal.valueOf(56000),   // 카카오
        "035420", BigDecimal.valueOf(210000),  // 네이버
        "005380", BigDecimal.valueOf(190000),  // 현대차
        "000270", BigDecimal.valueOf(85000)    // 기아
    );

    @Override
    public List<PriceCandle> fetchDailyCandles(String ticker, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching daily candles for {} from {} to {}", ticker, startDate, endDate);

        try {
            // 실제 구현에서는 Yahoo Finance API를 호출
            // 여기서는 시뮬레이션 데이터 반환
            return generateSimulatedCandles(ticker, startDate, endDate);
        } catch (Exception e) {
            log.error("Error fetching daily candles for {}: {}", ticker, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public PriceCandle fetchLatestPrice(String ticker) {
        log.info("Fetching latest price for {}", ticker);

        try {
            // 실제 구현에서는 Yahoo Finance API를 호출
            // 여기서는 시뮬레이션 데이터 반환
            BigDecimal basePrice = SIMULATED_PRICES.getOrDefault(ticker, BigDecimal.valueOf(100000));

            // 랜덤한 변동 추가 (-2% ~ +2%)
            double randomChange = (Math.random() - 0.5) * 0.04;
            BigDecimal currentPrice = basePrice.multiply(BigDecimal.valueOf(1 + randomChange));

            BigDecimal highPrice = currentPrice.multiply(BigDecimal.valueOf(1.01));
            BigDecimal lowPrice = currentPrice.multiply(BigDecimal.valueOf(0.99));
            BigDecimal openPrice = currentPrice.multiply(BigDecimal.valueOf(1.005));

            return new PriceCandle(
                ticker,
                LocalDate.now(),
                openPrice,
                highPrice,
                lowPrice,
                currentPrice,
                generateRandomVolume(),
                CandleTimeframe.DAILY
            );
        } catch (Exception e) {
            log.error("Error fetching latest price for {}: {}", ticker, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<PriceCandle> fetchLatestPrices(List<String> tickers) {
        log.info("Fetching latest prices for {} tickers", tickers.size());

        return tickers.stream()
                .map(this::fetchLatestPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 시뮬레이션용 캔들 데이터 생성
     */
    private List<PriceCandle> generateSimulatedCandles(String ticker, LocalDate startDate, LocalDate endDate) {
        List<PriceCandle> candles = new ArrayList<>();
        BigDecimal basePrice = SIMULATED_PRICES.getOrDefault(ticker, BigDecimal.valueOf(100000));

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 주말 제외
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                // 랜덤한 변동 추가 (-3% ~ +3%)
                double randomChange = (Math.random() - 0.5) * 0.06;
                BigDecimal closePrice = basePrice.multiply(BigDecimal.valueOf(1 + randomChange));

                BigDecimal highPrice = closePrice.multiply(BigDecimal.valueOf(1.02));
                BigDecimal lowPrice = closePrice.multiply(BigDecimal.valueOf(0.98));
                BigDecimal openPrice = closePrice.multiply(BigDecimal.valueOf(1.01));

                candles.add(new PriceCandle(
                    ticker,
                    currentDate,
                    openPrice,
                    highPrice,
                    lowPrice,
                    closePrice,
                    generateRandomVolume(),
                    CandleTimeframe.DAILY
                ));

                // 다음 날의 기준 가격으로 사용
                basePrice = closePrice;
            }
            currentDate = currentDate.plusDays(1);
        }

        return candles;
    }

    /**
     * 랜덤한 거래량 생성 (1백만 ~ 5천만 사이)
     */
    private Long generateRandomVolume() {
        return (long) (Math.random() * 49_000_000 + 1_000_000);
    }

    /**
     * 실제 Yahoo Finance API 호출 예시 (현재는 사용하지 않음)
     */
    private Optional<Map<String, Object>> fetchFromYahooFinance(String symbol) {
        try {
            String yahooSymbol = SYMBOL_MAPPING.getOrDefault(symbol, symbol + ".KS");
            String url = QUOTE_URL.replace("{symbol}", yahooSymbol);

            log.debug("Fetching from Yahoo Finance: {}", url);

            // 실제 API 호출 (현재는 비활성화)
            // ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            // return Optional.ofNullable(response.getBody());

            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Error calling Yahoo Finance API: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}