package com.stockquest.application.service.validation;

import com.stockquest.domain.ml.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Market Context Validation Service
 * Phase 4.1: Code Quality Enhancement - 시장 상황 적합성 검증 전문 서비스
 * 
 * 현재 시장 상황과 신호의 적합성을 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketContextValidationService {

    // 시장 상황 임계값
    private static final BigDecimal HIGH_VOLATILITY_THRESHOLD = BigDecimal.valueOf(0.3);
    private static final BigDecimal LOW_VOLATILITY_THRESHOLD = BigDecimal.valueOf(0.1);
    private static final BigDecimal BULL_MARKET_THRESHOLD = BigDecimal.valueOf(0.05);
    private static final BigDecimal BEAR_MARKET_THRESHOLD = BigDecimal.valueOf(-0.05);

    /**
     * 시장 상황 적합성 검증
     * 
     * @param signal 검증할 거래 신호
     * @return MarketContextValidation 시장 상황 검증 결과
     */
    public MarketContextValidation validateMarketContext(TradingSignal signal) {
        try {
            log.debug("시장 상황 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());

            // 현재 시장 체제 분석
            MarketRegime currentRegime = analyzeCurrentMarketRegime(signal.getSymbol());
            
            // 변동성 환경 분석
            VolatilityEnvironment volEnvironment = analyzeVolatilityEnvironment(signal.getSymbol());
            
            // 모델의 시장 상황별 성능 이력
            Map<MarketRegime, BigDecimal> regimePerformance = getRegimePerformanceHistory(signal);
            
            // 현재 상황에서의 예상 성능
            BigDecimal expectedPerformance = regimePerformance.getOrDefault(currentRegime, 
                BigDecimal.valueOf(0.5));
            
            // 시장 스트레스 수준
            MarketStressLevel stressLevel = assessMarketStressLevel(signal.getSymbol(), 
                currentRegime, volEnvironment);
            
            // 상황 적합성 점수 계산
            BigDecimal contextScore = calculateContextScore(currentRegime, expectedPerformance, 
                stressLevel, volEnvironment);

            MarketContextValidation result = MarketContextValidation.builder()
                .currentMarketRegime(currentRegime)
                .volatilityEnvironment(volEnvironment)
                .marketStressLevel(stressLevel)
                .expectedPerformance(expectedPerformance)
                .regimePerformanceHistory(regimePerformance)
                .contextScore(contextScore)
                .build();

            log.debug("시장 상황 검증 완료: {} (상황 점수: {})", signal.getSymbol(), contextScore);
            return result;
            
        } catch (Exception e) {
            log.error("시장 상황 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("시장 상황 검증 중 오류 발생", e);
        }
    }

    /**
     * 현재 시장 체제 분석
     */
    private MarketRegime analyzeCurrentMarketRegime(String symbol) {
        // 실제 구현에서는 시장 데이터를 분석해야 함
        // 여기서는 시뮬레이션으로 구현
        
        // 가상의 시장 지표들 (실제로는 외부 데이터 소스에서 가져와야 함)
        BigDecimal marketTrend = getMarketTrend(symbol);
        BigDecimal marketVolatility = getMarketVolatility(symbol);
        BigDecimal marketMomentum = getMarketMomentum(symbol);
        
        log.debug("시장 분석 - 추세: {}, 변동성: {}, 모멘텀: {}", 
            marketTrend, marketVolatility, marketMomentum);

        // 시장 체제 결정 로직
        if (marketTrend.compareTo(BULL_MARKET_THRESHOLD) > 0 && 
            marketMomentum.compareTo(BigDecimal.ZERO) > 0) {
            return MarketRegime.BULL_MARKET;
        } else if (marketTrend.compareTo(BEAR_MARKET_THRESHOLD) < 0 && 
                   marketMomentum.compareTo(BigDecimal.ZERO) < 0) {
            return MarketRegime.BEAR_MARKET;
        } else {
            return MarketRegime.NORMAL_MARKET;
        }
    }

    /**
     * 변동성 환경 분석
     */
    private VolatilityEnvironment analyzeVolatilityEnvironment(String symbol) {
        // 가상의 변동성 지표 계산
        BigDecimal impliedVolatility = getImpliedVolatility(symbol);
        BigDecimal historicalVolatility = getHistoricalVolatility(symbol);
        BigDecimal volatilityTrend = impliedVolatility.subtract(historicalVolatility);
        
        String volatilityLevel;
        String volatilityTrendStr;
        
        // 변동성 수준 결정
        if (impliedVolatility.compareTo(HIGH_VOLATILITY_THRESHOLD) > 0) {
            volatilityLevel = "HIGH";
        } else if (impliedVolatility.compareTo(LOW_VOLATILITY_THRESHOLD) < 0) {
            volatilityLevel = "LOW";
        } else {
            volatilityLevel = "MODERATE";
        }
        
        // 변동성 추세 결정
        if (volatilityTrend.compareTo(BigDecimal.valueOf(0.02)) > 0) {
            volatilityTrendStr = "INCREASING";
        } else if (volatilityTrend.compareTo(BigDecimal.valueOf(-0.02)) < 0) {
            volatilityTrendStr = "DECREASING";
        } else {
            volatilityTrendStr = "STABLE";
        }

        return VolatilityEnvironment.builder()
            .volatilityLevel(volatilityLevel)
            .volatilityTrend(volatilityTrendStr)
            .impliedVolatility(impliedVolatility)
            .historicalVolatility(historicalVolatility)
            .build();
    }

    /**
     * 시장 체제별 성능 이력 가져오기
     */
    private Map<MarketRegime, BigDecimal> getRegimePerformanceHistory(TradingSignal signal) {
        // 실제 구현에서는 데이터베이스에서 과거 성과를 조회해야 함
        // 여기서는 시뮬레이션 데이터 사용
        
        Map<MarketRegime, BigDecimal> history = new HashMap<>();
        
        // 신호 타입에 따른 시장별 예상 성과
        switch (signal.getSignalType()) {
            case BUY:
                history.put(MarketRegime.BULL_MARKET, BigDecimal.valueOf(0.75));
                history.put(MarketRegime.NORMAL_MARKET, BigDecimal.valueOf(0.60));
                history.put(MarketRegime.BEAR_MARKET, BigDecimal.valueOf(0.40));
                break;
            case SELL:
                history.put(MarketRegime.BULL_MARKET, BigDecimal.valueOf(0.40));
                history.put(MarketRegime.NORMAL_MARKET, BigDecimal.valueOf(0.55));
                history.put(MarketRegime.BEAR_MARKET, BigDecimal.valueOf(0.70));
                break;
            case HOLD:
                history.put(MarketRegime.BULL_MARKET, BigDecimal.valueOf(0.50));
                history.put(MarketRegime.NORMAL_MARKET, BigDecimal.valueOf(0.65));
                history.put(MarketRegime.BEAR_MARKET, BigDecimal.valueOf(0.55));
                break;
        }
        
        return history;
    }

    /**
     * 시장 스트레스 수준 평가
     */
    private MarketStressLevel assessMarketStressLevel(String symbol, MarketRegime regime, 
                                                     VolatilityEnvironment volEnv) {
        // 스트레스 지수 계산 (0.0 ~ 1.0)
        double stressIndex = 0.0;
        
        // 변동성 기여도 (40%)
        if ("HIGH".equals(volEnv.getVolatilityLevel())) {
            stressIndex += 0.4;
        } else if ("MODERATE".equals(volEnv.getVolatilityLevel())) {
            stressIndex += 0.2;
        }
        
        // 시장 체제 기여도 (30%)
        if (regime == MarketRegime.BEAR_MARKET) {
            stressIndex += 0.3;
        } else if (regime == MarketRegime.NORMAL_MARKET) {
            stressIndex += 0.1;
        }
        
        // 변동성 추세 기여도 (20%)
        if ("INCREASING".equals(volEnv.getVolatilityTrend())) {
            stressIndex += 0.2;
        }
        
        // 기타 시장 지표 기여도 (10%)
        BigDecimal marketFear = getMarketFearIndex(symbol);
        stressIndex += marketFear.doubleValue() * 0.1;
        
        // 스트레스 수준 결정
        if (stressIndex >= 0.7) {
            return MarketStressLevel.EXTREME;
        } else if (stressIndex >= 0.5) {
            return MarketStressLevel.HIGH;
        } else if (stressIndex >= 0.3) {
            return MarketStressLevel.NORMAL;
        } else {
            return MarketStressLevel.LOW;
        }
    }

    /**
     * 상황 적합성 점수 계산
     */
    private BigDecimal calculateContextScore(MarketRegime regime, BigDecimal expectedPerformance, 
                                           MarketStressLevel stress, VolatilityEnvironment volEnv) {
        // 기본 점수는 예상 성과 (60%)
        BigDecimal baseScore = expectedPerformance.multiply(BigDecimal.valueOf(0.6));
        
        // 스트레스 수준에 따른 조정 (25%)
        BigDecimal stressAdjustment = switch (stress) {
            case LOW -> BigDecimal.valueOf(0.25);
            case NORMAL -> BigDecimal.valueOf(0.15);
            case HIGH -> BigDecimal.valueOf(0.05);
            case EXTREME -> BigDecimal.valueOf(-0.05);
        };
        
        // 변동성 환경에 따른 조정 (15%)
        BigDecimal volatilityAdjustment = BigDecimal.ZERO;
        if ("STABLE".equals(volEnv.getVolatilityTrend())) {
            volatilityAdjustment = BigDecimal.valueOf(0.15);
        } else if ("INCREASING".equals(volEnv.getVolatilityTrend())) {
            volatilityAdjustment = BigDecimal.valueOf(0.05);
        } else {
            volatilityAdjustment = BigDecimal.valueOf(0.10);
        }
        
        return baseScore.add(stressAdjustment).add(volatilityAdjustment)
            .max(BigDecimal.ZERO).min(BigDecimal.ONE)
            .setScale(4, RoundingMode.HALF_UP);
    }

    // 헬퍼 메서드들 (실제 구현에서는 실제 시장 데이터를 사용해야 함)

    private BigDecimal getMarketTrend(String symbol) {
        // 시뮬레이션: -0.1 ~ 0.1 범위의 랜덤 값
        return BigDecimal.valueOf((Math.random() - 0.5) * 0.2);
    }

    private BigDecimal getMarketVolatility(String symbol) {
        // 시뮬레이션: 0.1 ~ 0.4 범위의 랜덤 값
        return BigDecimal.valueOf(0.1 + Math.random() * 0.3);
    }

    private BigDecimal getMarketMomentum(String symbol) {
        // 시뮬레이션: -0.05 ~ 0.05 범위의 랜덤 값
        return BigDecimal.valueOf((Math.random() - 0.5) * 0.1);
    }

    private BigDecimal getImpliedVolatility(String symbol) {
        // 시뮬레이션: 0.15 ~ 0.35 범위의 랜덤 값
        return BigDecimal.valueOf(0.15 + Math.random() * 0.2);
    }

    private BigDecimal getHistoricalVolatility(String symbol) {
        // 시뮬레이션: 0.12 ~ 0.32 범위의 랜덤 값
        return BigDecimal.valueOf(0.12 + Math.random() * 0.2);
    }

    private BigDecimal getMarketFearIndex(String symbol) {
        // 시뮬레이션: 0.0 ~ 1.0 범위의 공포 지수
        return BigDecimal.valueOf(Math.random());
    }

    // DTO Classes

    public enum MarketRegime {
        BULL_MARKET, NORMAL_MARKET, BEAR_MARKET
    }

    public enum MarketStressLevel {
        LOW, NORMAL, HIGH, EXTREME
    }

    public static class MarketContextValidation {
        private final MarketRegime currentMarketRegime;
        private final VolatilityEnvironment volatilityEnvironment;
        private final MarketStressLevel marketStressLevel;
        private final BigDecimal expectedPerformance;
        private final Map<MarketRegime, BigDecimal> regimePerformanceHistory;
        private final BigDecimal contextScore;

        public static MarketContextValidationBuilder builder() {
            return new MarketContextValidationBuilder();
        }

        private MarketContextValidation(MarketContextValidationBuilder builder) {
            this.currentMarketRegime = builder.currentMarketRegime;
            this.volatilityEnvironment = builder.volatilityEnvironment;
            this.marketStressLevel = builder.marketStressLevel;
            this.expectedPerformance = builder.expectedPerformance;
            this.regimePerformanceHistory = builder.regimePerformanceHistory;
            this.contextScore = builder.contextScore;
        }

        // Getters
        public MarketRegime getCurrentMarketRegime() { return currentMarketRegime; }
        public VolatilityEnvironment getVolatilityEnvironment() { return volatilityEnvironment; }
        public MarketStressLevel getMarketStressLevel() { return marketStressLevel; }
        public BigDecimal getExpectedPerformance() { return expectedPerformance; }
        public Map<MarketRegime, BigDecimal> getRegimePerformanceHistory() { return regimePerformanceHistory; }
        public BigDecimal getContextScore() { return contextScore; }

        public static class MarketContextValidationBuilder {
            private MarketRegime currentMarketRegime;
            private VolatilityEnvironment volatilityEnvironment;
            private MarketStressLevel marketStressLevel;
            private BigDecimal expectedPerformance;
            private Map<MarketRegime, BigDecimal> regimePerformanceHistory;
            private BigDecimal contextScore;

            public MarketContextValidationBuilder currentMarketRegime(MarketRegime currentMarketRegime) {
                this.currentMarketRegime = currentMarketRegime;
                return this;
            }

            public MarketContextValidationBuilder volatilityEnvironment(VolatilityEnvironment volatilityEnvironment) {
                this.volatilityEnvironment = volatilityEnvironment;
                return this;
            }

            public MarketContextValidationBuilder marketStressLevel(MarketStressLevel marketStressLevel) {
                this.marketStressLevel = marketStressLevel;
                return this;
            }

            public MarketContextValidationBuilder expectedPerformance(BigDecimal expectedPerformance) {
                this.expectedPerformance = expectedPerformance;
                return this;
            }

            public MarketContextValidationBuilder regimePerformanceHistory(Map<MarketRegime, BigDecimal> regimePerformanceHistory) {
                this.regimePerformanceHistory = regimePerformanceHistory;
                return this;
            }

            public MarketContextValidationBuilder contextScore(BigDecimal contextScore) {
                this.contextScore = contextScore;
                return this;
            }

            public MarketContextValidation build() {
                return new MarketContextValidation(this);
            }
        }
    }

    public static class VolatilityEnvironment {
        private final String volatilityLevel;
        private final String volatilityTrend;
        private final BigDecimal impliedVolatility;
        private final BigDecimal historicalVolatility;

        public static VolatilityEnvironmentBuilder builder() {
            return new VolatilityEnvironmentBuilder();
        }

        private VolatilityEnvironment(VolatilityEnvironmentBuilder builder) {
            this.volatilityLevel = builder.volatilityLevel;
            this.volatilityTrend = builder.volatilityTrend;
            this.impliedVolatility = builder.impliedVolatility;
            this.historicalVolatility = builder.historicalVolatility;
        }

        // Getters
        public String getVolatilityLevel() { return volatilityLevel; }
        public String getVolatilityTrend() { return volatilityTrend; }
        public BigDecimal getImpliedVolatility() { return impliedVolatility; }
        public BigDecimal getHistoricalVolatility() { return historicalVolatility; }

        public static class VolatilityEnvironmentBuilder {
            private String volatilityLevel;
            private String volatilityTrend;
            private BigDecimal impliedVolatility;
            private BigDecimal historicalVolatility;

            public VolatilityEnvironmentBuilder volatilityLevel(String volatilityLevel) {
                this.volatilityLevel = volatilityLevel;
                return this;
            }

            public VolatilityEnvironmentBuilder volatilityTrend(String volatilityTrend) {
                this.volatilityTrend = volatilityTrend;
                return this;
            }

            public VolatilityEnvironmentBuilder impliedVolatility(BigDecimal impliedVolatility) {
                this.impliedVolatility = impliedVolatility;
                return this;
            }

            public VolatilityEnvironmentBuilder historicalVolatility(BigDecimal historicalVolatility) {
                this.historicalVolatility = historicalVolatility;
                return this;
            }

            public VolatilityEnvironment build() {
                return new VolatilityEnvironment(this);
            }
        }
    }
}