package com.stockquest.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 거래 시뮬레이션 설정
 * 개발/테스트 환경에서 외부 API 없이도 완전 동작하는 시뮬레이션 모드 지원
 */
@Slf4j
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "stockquest.trading")
public class TradingSimulationConfig {

    /**
     * 시뮬레이션 모드 활성화 여부
     * true: 외부 API 호출 없이 설정된 기본값 사용
     * false: 실제 시장 데이터 우선 사용, 실패 시 fallback
     */
    private boolean simulationMode = false;

    /**
     * 슬리피지 설정
     */
    private Slippage slippage = new Slippage();

    /**
     * 티커별 기본 가격 설정
     * 시뮬레이션 모드 또는 실시간 데이터 조회 실패 시 사용
     */
    private Map<String, BigDecimal> defaultPrices = new HashMap<>();

    /**
     * 변동성 설정
     */
    private Volatility volatility = new Volatility();

    @Data
    public static class Slippage {
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("10.0")
        private BigDecimal min = new BigDecimal("0.5");

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("10.0")
        private BigDecimal max = new BigDecimal("2.0");
    }

    @Data
    public static class Volatility {
        /**
         * 기본 가격 변동률 (±%)
         */
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("50.0")
        private BigDecimal priceVariation = new BigDecimal("5.0");

        /**
         * 시간에 따른 가격 변동 활성화 여부
         */
        private boolean timeBasedVariation = true;
    }

    /**
     * 기본 가격 초기화
     */
    public void initializeDefaultPrices() {
        if (defaultPrices.isEmpty()) {
            log.info("Initializing default prices for trading simulation");

            // 미국 주식 기본 가격 (2024년 기준)
            defaultPrices.put("AAPL", new BigDecimal("180.00"));
            defaultPrices.put("MSFT", new BigDecimal("420.00"));
            defaultPrices.put("GOOGL", new BigDecimal("140.00"));
            defaultPrices.put("GOOG", new BigDecimal("140.00"));
            defaultPrices.put("AMZN", new BigDecimal("150.00"));
            defaultPrices.put("TSLA", new BigDecimal("250.00"));
            defaultPrices.put("NVDA", new BigDecimal("450.00"));
            defaultPrices.put("META", new BigDecimal("350.00"));
            defaultPrices.put("NFLX", new BigDecimal("400.00"));
            defaultPrices.put("AMD", new BigDecimal("120.00"));
            defaultPrices.put("INTC", new BigDecimal("35.00"));

            // 밈스톡 (2021년 기준 조정)
            defaultPrices.put("GME", new BigDecimal("20.00"));
            defaultPrices.put("AMC", new BigDecimal("10.00"));
            defaultPrices.put("BB", new BigDecimal("8.00"));
            defaultPrices.put("NOK", new BigDecimal("5.00"));
            defaultPrices.put("KOSS", new BigDecimal("15.00"));

            // 방어적 자산
            defaultPrices.put("JNJ", new BigDecimal("165.00"));
            defaultPrices.put("PG", new BigDecimal("150.00"));
            defaultPrices.put("KO", new BigDecimal("62.00"));

            // 한국 주식 (원화)
            defaultPrices.put("005930", new BigDecimal("70000.00")); // 삼성전자
            defaultPrices.put("000660", new BigDecimal("120000.00")); // SK하이닉스
            defaultPrices.put("035720", new BigDecimal("95000.00")); // 카카오

            // 기본 fallback 가격
            defaultPrices.put("DEFAULT", new BigDecimal("100.00"));

            log.info("Initialized {} default prices for trading simulation", defaultPrices.size());
        }
    }

    /**
     * 티커에 대한 기본 가격 조회
     */
    public BigDecimal getDefaultPrice(String ticker) {
        initializeDefaultPrices();

        if (ticker == null || ticker.trim().isEmpty()) {
            return defaultPrices.get("DEFAULT");
        }

        return defaultPrices.getOrDefault(ticker.toUpperCase().trim(), defaultPrices.get("DEFAULT"));
    }

    /**
     * 변동성을 적용한 시뮬레이션 가격 계산
     */
    public BigDecimal calculateSimulationPrice(String ticker, BigDecimal basePrice) {
        if (!volatility.timeBasedVariation) {
            return basePrice;
        }

        // 시간 기반 변동성 적용
        double variationPercent = volatility.priceVariation.doubleValue() / 100.0;
        double randomFactor = 0.95 + (Math.random() * 0.1); // ±5% 기본 변동

        // 시간별 추가 변동성 (현재 시간 기반)
        long currentHour = System.currentTimeMillis() / (1000 * 60 * 60);
        double timeFactor = 1.0 + (Math.sin(currentHour * 0.1) * variationPercent);

        BigDecimal finalPrice = basePrice
            .multiply(BigDecimal.valueOf(randomFactor))
            .multiply(BigDecimal.valueOf(timeFactor))
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        log.debug("Calculated simulation price for {}: base={}, final={}, variation={}%",
                 ticker, basePrice, finalPrice,
                 finalPrice.subtract(basePrice).divide(basePrice, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));

        return finalPrice;
    }
}