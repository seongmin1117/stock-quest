package com.stockquest.domain.risk;

import com.stockquest.domain.portfolio.PortfolioPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 스트레스 테스트 엔진 도메인 서비스
 * 다양한 리스크 시나리오를 포트폴리오에 적용하여 스트레스 테스트 수행
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StressTestEngine {

    private String engineId;
    private LocalDateTime createdAt;
    private Integer defaultSimulationRuns;
    private Double defaultConfidenceLevel;


    /**
     * 단일 시나리오 스트레스 테스트 실행
     */
    public StressTestResult runSingleScenarioTest(
            RiskScenario scenario,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal initialPortfolioValue) {

        scenario.validate();
        validateInputs(positions, currentPrices, initialPortfolioValue);

        String testId = generateTestId();

        // 시나리오에 따른 가격 충격 적용
        Map<String, BigDecimal> stressedPrices = applyScenarioShocks(scenario, currentPrices);

        // 스트레스된 포트폴리오 가치 계산
        BigDecimal stressedValue = calculatePortfolioValue(positions, stressedPrices);
        BigDecimal portfolioLoss = initialPortfolioValue.subtract(stressedValue);

        // 자산별 손실 기여도 계산
        Map<String, BigDecimal> assetContributions = calculateAssetContributions(
                positions, currentPrices, stressedPrices);

        // 민감도 분석
        Map<String, BigDecimal> deltaSensitivity = calculateDeltaSensitivity(
                positions, currentPrices, scenario);

        return StressTestResult.builder()
                .testId(testId)
                .scenarioId(scenario.getScenarioId())
                .portfolioId(positions.isEmpty() ? null : positions.get(0).getSessionId())
                .executedAt(LocalDateTime.now())
                .simulationRuns(1)
                .confidenceLevel(scenario.getSeverity().getRecommendedConfidenceLevel())
                .worstCaseLoss(portfolioLoss)
                .expectedLoss(portfolioLoss)
                .valueAtRisk95(portfolioLoss)
                .valueAtRisk99(portfolioLoss)
                .conditionalVaR(portfolioLoss)
                .portfolioValue(initialPortfolioValue)
                .assetContributions(assetContributions)
                .deltaSensitivity(deltaSensitivity)
                .liquidationTimeframe(estimateLiquidationTime(positions, scenario))
                .build();
    }

    /**
     * 몬테카를로 시뮬레이션 기반 스트레스 테스트
     */
    public StressTestResult runMonteCarloStressTest(
            RiskScenario scenario,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal initialPortfolioValue,
            Integer simulationRuns) {

        scenario.validate();
        validateInputs(positions, currentPrices, initialPortfolioValue);

        String testId = generateTestId();
        List<BigDecimal> simulationResults = new ArrayList<>();

        // 몬테카를로 시뮬레이션 실행
        Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < simulationRuns; i++) {
            // 랜덤 시장 충격 생성
            Map<String, BigDecimal> randomShocks = generateRandomShocks(scenario, random);
            Map<String, BigDecimal> shockedPrices = applyShocks(currentPrices, randomShocks);

            // 포트폴리오 가치 계산
            BigDecimal portfolioValue = calculatePortfolioValue(positions, shockedPrices);
            simulationResults.add(portfolioValue);
        }

        // 통계 분석
        Collections.sort(simulationResults);

        BigDecimal var95 = calculateVaR(simulationResults, 0.95, initialPortfolioValue);
        BigDecimal var99 = calculateVaR(simulationResults, 0.99, initialPortfolioValue);
        BigDecimal expectedShortfall = calculateExpectedShortfall(simulationResults, 0.95, initialPortfolioValue);
        BigDecimal worstCase = initialPortfolioValue.subtract(simulationResults.get(0));
        BigDecimal expectedLoss = calculateExpectedLoss(simulationResults, initialPortfolioValue);

        // 최대 낙폭 계산
        BigDecimal maxDrawdown = calculateMaximumDrawdown(simulationResults, initialPortfolioValue);

        return StressTestResult.builder()
                .testId(testId)
                .scenarioId(scenario.getScenarioId())
                .portfolioId(positions.isEmpty() ? null : positions.get(0).getSessionId())
                .executedAt(LocalDateTime.now())
                .simulationRuns(simulationRuns)
                .confidenceLevel(0.95)
                .worstCaseLoss(worstCase)
                .expectedLoss(expectedLoss)
                .valueAtRisk95(var95)
                .valueAtRisk99(var99)
                .conditionalVaR(expectedShortfall)
                .maximumDrawdown(maxDrawdown)
                .portfolioValue(initialPortfolioValue)
                .build();
    }

    /**
     * 다중 시나리오 종합 스트레스 테스트
     */
    public List<StressTestResult> runMultiScenarioTest(
            List<RiskScenario> scenarios,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal initialPortfolioValue) {

        return scenarios.stream()
                .map(scenario -> runSingleScenarioTest(scenario, positions, currentPrices, initialPortfolioValue))
                .collect(Collectors.toList());
    }

    /**
     * 히스토리컬 시나리오 재현 테스트
     */
    public StressTestResult runHistoricalScenarioTest(
            RiskScenario scenario,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal initialPortfolioValue,
            Map<String, List<BigDecimal>> historicalPriceData) {

        // 역사적 가격 데이터를 사용한 스트레스 테스트
        List<BigDecimal> portfolioValues = new ArrayList<>();

        for (int i = 0; i < getMinHistoricalDataLength(historicalPriceData); i++) {
            Map<String, BigDecimal> historicalPrices = extractPricesAtIndex(historicalPriceData, i);
            BigDecimal portfolioValue = calculatePortfolioValue(positions, historicalPrices);
            portfolioValues.add(portfolioValue);
        }

        // 최악의 기간 식별
        BigDecimal worstValue = Collections.min(portfolioValues);
        BigDecimal worstLoss = initialPortfolioValue.subtract(worstValue);

        // 역사적 VaR 계산
        Collections.sort(portfolioValues);
        BigDecimal historicalVar95 = calculateVaR(portfolioValues, 0.95, initialPortfolioValue);
        BigDecimal historicalVar99 = calculateVaR(portfolioValues, 0.99, initialPortfolioValue);

        return StressTestResult.builder()
                .testId(generateTestId())
                .scenarioId(scenario.getScenarioId())
                .portfolioId(positions.isEmpty() ? null : positions.get(0).getSessionId())
                .executedAt(LocalDateTime.now())
                .simulationRuns(portfolioValues.size())
                .worstCaseLoss(worstLoss)
                .valueAtRisk95(historicalVar95)
                .valueAtRisk99(historicalVar99)
                .portfolioValue(initialPortfolioValue)
                .build();
    }

    // === Private Helper Methods ===

    private void validateInputs(List<PortfolioPosition> positions,
                               Map<String, BigDecimal> currentPrices,
                               BigDecimal initialPortfolioValue) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("포트폴리오 포지션은 필수입니다");
        }
        if (currentPrices == null || currentPrices.isEmpty()) {
            throw new IllegalArgumentException("현재 가격 정보는 필수입니다");
        }
        if (initialPortfolioValue == null || initialPortfolioValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("초기 포트폴리오 가치는 양수여야 합니다");
        }
    }

    private Map<String, BigDecimal> applyScenarioShocks(RiskScenario scenario, Map<String, BigDecimal> currentPrices) {
        Map<String, BigDecimal> stressedPrices = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : currentPrices.entrySet()) {
            String instrument = entry.getKey();
            BigDecimal originalPrice = entry.getValue();

            // 시나리오별 시장 충격 적용
            BigDecimal stressedPrice = scenario.applyMarketShock("GENERAL", originalPrice);

            // 변동성 충격 추가 적용 (단순화된 모델)
            if (scenario.getVolatilityMultiplier() != null) {
                BigDecimal volatilityImpact = originalPrice
                        .multiply(BigDecimal.valueOf(0.1)) // 기본 10% 변동성 가정
                        .multiply(scenario.getVolatilityMultiplier())
                        .multiply(BigDecimal.valueOf(-1)); // 부정적 시나리오 가정

                stressedPrice = stressedPrice.add(volatilityImpact);
            }

            stressedPrices.put(instrument, stressedPrice.max(BigDecimal.ZERO));
        }

        return stressedPrices;
    }

    private BigDecimal calculatePortfolioValue(List<PortfolioPosition> positions, Map<String, BigDecimal> prices) {
        return positions.stream()
                .filter(PortfolioPosition::hasPosition)
                .map(position -> {
                    BigDecimal price = prices.get(position.getInstrumentKey());
                    return price != null ? position.calculateCurrentValue(price) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> calculateAssetContributions(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> originalPrices,
            Map<String, BigDecimal> stressedPrices) {

        Map<String, BigDecimal> contributions = new HashMap<>();

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal originalPrice = originalPrices.get(instrument);
            BigDecimal stressedPrice = stressedPrices.get(instrument);

            if (originalPrice != null && stressedPrice != null) {
                BigDecimal originalValue = position.calculateCurrentValue(originalPrice);
                BigDecimal stressedValue = position.calculateCurrentValue(stressedPrice);
                BigDecimal contribution = originalValue.subtract(stressedValue);
                contributions.put(instrument, contribution);
            }
        }

        return contributions;
    }

    private Map<String, BigDecimal> calculateDeltaSensitivity(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            RiskScenario scenario) {

        Map<String, BigDecimal> sensitivity = new HashMap<>();
        BigDecimal shockSize = BigDecimal.valueOf(0.01); // 1% 충격

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal currentPrice = currentPrices.get(instrument);

            if (currentPrice != null) {
                BigDecimal shockedPrice = currentPrice.multiply(BigDecimal.ONE.add(shockSize));
                BigDecimal currentValue = position.calculateCurrentValue(currentPrice);
                BigDecimal shockedValue = position.calculateCurrentValue(shockedPrice);
                BigDecimal delta = shockedValue.subtract(currentValue)
                        .divide(currentPrice.multiply(shockSize), 4, RoundingMode.HALF_UP);

                sensitivity.put(instrument, delta);
            }
        }

        return sensitivity;
    }

    private Integer estimateLiquidationTime(List<PortfolioPosition> positions, RiskScenario scenario) {
        // 시나리오 타입과 포트폴리오 크기에 따른 청산 시간 추정
        int baseTime = scenario.getType().getDefaultStressDuration() / 4; // 기본 청산 시간

        // 포지션 수에 따른 조정
        int positionCount = (int) positions.stream().filter(PortfolioPosition::hasPosition).count();
        int adjustedTime = baseTime + (positionCount / 10); // 포지션 10개마다 1일 추가

        // 시나리오 심각도에 따른 조정
        double severityMultiplier = switch (scenario.getSeverity()) {
            case MILD -> 0.5;
            case MODERATE -> 1.0;
            case SEVERE -> 1.5;
            case EXTREME -> 2.0;
            case CATASTROPHIC -> 3.0;
        };

        return (int) (adjustedTime * severityMultiplier);
    }

    private Map<String, BigDecimal> generateRandomShocks(RiskScenario scenario, Random random) {
        Map<String, BigDecimal> shocks = new HashMap<>();

        double volatilityMultiplier = scenario.getVolatilityMultiplier() != null ?
                scenario.getVolatilityMultiplier().doubleValue() : 1.0;

        // 정규분포를 따르는 랜덤 충격 생성
        double shock = random.nextGaussian() * 0.1 * volatilityMultiplier; // 기본 10% 변동성
        shocks.put("GENERAL", BigDecimal.valueOf(shock));

        return shocks;
    }

    private Map<String, BigDecimal> applyShocks(Map<String, BigDecimal> originalPrices, Map<String, BigDecimal> shocks) {
        Map<String, BigDecimal> shockedPrices = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : originalPrices.entrySet()) {
            String instrument = entry.getKey();
            BigDecimal originalPrice = entry.getValue();

            BigDecimal shock = shocks.getOrDefault("GENERAL", BigDecimal.ZERO);
            BigDecimal shockedPrice = originalPrice.multiply(BigDecimal.ONE.add(shock));

            shockedPrices.put(instrument, shockedPrice.max(BigDecimal.ZERO));
        }

        return shockedPrices;
    }

    private BigDecimal calculateVaR(List<BigDecimal> sortedValues, double confidenceLevel, BigDecimal initialValue) {
        int index = (int) Math.floor((1.0 - confidenceLevel) * sortedValues.size());
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return initialValue.subtract(sortedValues.get(index));
    }

    private BigDecimal calculateExpectedShortfall(List<BigDecimal> sortedValues, double confidenceLevel, BigDecimal initialValue) {
        int cutoffIndex = (int) Math.floor((1.0 - confidenceLevel) * sortedValues.size());
        cutoffIndex = Math.max(0, Math.min(cutoffIndex, sortedValues.size() - 1));

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i <= cutoffIndex; i++) {
            sum = sum.add(initialValue.subtract(sortedValues.get(i)));
        }

        return sum.divide(BigDecimal.valueOf(cutoffIndex + 1), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateExpectedLoss(List<BigDecimal> values, BigDecimal initialValue) {
        BigDecimal totalLoss = values.stream()
                .map(value -> initialValue.subtract(value))
                .filter(loss -> loss.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lossCount = values.stream()
                .mapToLong(value -> value.compareTo(initialValue) < 0 ? 1L : 0L)
                .sum();

        return lossCount > 0 ? totalLoss.divide(BigDecimal.valueOf(lossCount), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateMaximumDrawdown(List<BigDecimal> values, BigDecimal initialValue) {
        BigDecimal peak = initialValue;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (BigDecimal value : values) {
            if (value.compareTo(peak) > 0) {
                peak = value;
            }
            BigDecimal drawdown = peak.subtract(value).divide(peak, 4, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown;
    }

    private int getMinHistoricalDataLength(Map<String, List<BigDecimal>> historicalData) {
        return historicalData.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }

    private Map<String, BigDecimal> extractPricesAtIndex(Map<String, List<BigDecimal>> historicalData, int index) {
        Map<String, BigDecimal> prices = new HashMap<>();
        for (Map.Entry<String, List<BigDecimal>> entry : historicalData.entrySet()) {
            if (index < entry.getValue().size()) {
                prices.put(entry.getKey(), entry.getValue().get(index));
            }
        }
        return prices;
    }

    private String generateTestId() {
        return "ST_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}