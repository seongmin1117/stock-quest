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
 * 고급 리스크 메트릭 계산기 도메인 서비스
 * VaR, CVaR, 그리스 민감도, 성능 지표 등 다양한 리스크 메트릭 계산
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedRiskCalculator {

    private String calculatorId;
    private LocalDateTime createdAt;
    private Integer defaultLookbackPeriod;     // 기본 과거 데이터 기간 (일)
    private Double defaultConfidenceLevel;     // 기본 신뢰 수준


    /**
     * 파라메트릭 VaR 계산 (분산-공분산 방법)
     */
    public BigDecimal calculateParametricVaR(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix,
            Double confidenceLevel,
            Integer timeHorizon) {

        validateInputs(positions, currentPrices, expectedReturns, volatilities);

        // 포트폴리오 가중치 계산
        Map<String, BigDecimal> weights = calculatePortfolioWeights(positions, currentPrices);
        BigDecimal portfolioValue = calculateTotalPortfolioValue(positions, currentPrices);

        // 포트폴리오 기대 수익률 계산
        double portfolioExpectedReturn = calculatePortfolioExpectedReturn(weights, expectedReturns);

        // 포트폴리오 변동성 계산
        double portfolioVolatility = calculatePortfolioVolatility(weights, volatilities, correlationMatrix);

        // 신뢰 구간에 따른 Z-score
        double zScore = calculateZScore(confidenceLevel);

        // 시간 조정
        double timeAdjustment = Math.sqrt(timeHorizon / 252.0); // 연간 252 거래일 기준

        // VaR 계산: -(μ - σ*z) * √t * P
        double varRatio = -(portfolioExpectedReturn - portfolioVolatility * zScore) * timeAdjustment;
        return portfolioValue.multiply(BigDecimal.valueOf(varRatio));
    }

    /**
     * 히스토리컬 VaR 계산 (과거 데이터 기반)
     */
    public BigDecimal calculateHistoricalVaR(
            List<BigDecimal> historicalReturns,
            BigDecimal portfolioValue,
            Double confidenceLevel) {

        if (historicalReturns == null || historicalReturns.isEmpty()) {
            throw new IllegalArgumentException("과거 수익률 데이터가 필요합니다");
        }

        List<BigDecimal> sortedReturns = historicalReturns.stream()
                .sorted()
                .collect(Collectors.toList());

        int index = (int) Math.floor((1.0 - confidenceLevel) * sortedReturns.size());
        index = Math.max(0, Math.min(index, sortedReturns.size() - 1));

        BigDecimal varReturn = sortedReturns.get(index);
        return portfolioValue.multiply(varReturn.abs());
    }

    /**
     * 몬테카를로 VaR 계산
     */
    public BigDecimal calculateMonteCarloVaR(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix,
            Double confidenceLevel,
            Integer simulations,
            Integer timeHorizon) {

        BigDecimal portfolioValue = calculateTotalPortfolioValue(positions, currentPrices);
        List<BigDecimal> simulatedReturns = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < simulations; i++) {
            Map<String, Double> randomReturns = generateCorrelatedRandomReturns(
                    expectedReturns, volatilities, correlationMatrix, random, timeHorizon);

            BigDecimal portfolioReturn = calculatePortfolioReturnFromRandoms(
                    positions, currentPrices, randomReturns);

            simulatedReturns.add(portfolioReturn);
        }

        return calculateHistoricalVaR(simulatedReturns, portfolioValue, confidenceLevel);
    }

    /**
     * CVaR (Conditional VaR / Expected Shortfall) 계산
     */
    public BigDecimal calculateConditionalVaR(
            List<BigDecimal> historicalReturns,
            BigDecimal portfolioValue,
            Double confidenceLevel) {

        if (historicalReturns == null || historicalReturns.isEmpty()) {
            throw new IllegalArgumentException("과거 수익률 데이터가 필요합니다");
        }

        List<BigDecimal> sortedReturns = historicalReturns.stream()
                .sorted()
                .collect(Collectors.toList());

        int cutoffIndex = (int) Math.floor((1.0 - confidenceLevel) * sortedReturns.size());
        cutoffIndex = Math.max(0, Math.min(cutoffIndex, sortedReturns.size() - 1));

        // VaR를 초과하는 손실들의 평균
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i <= cutoffIndex; i++) {
            sum = sum.add(sortedReturns.get(i).abs());
        }

        BigDecimal averageLoss = sum.divide(BigDecimal.valueOf(cutoffIndex + 1), 6, RoundingMode.HALF_UP);
        return portfolioValue.multiply(averageLoss);
    }

    /**
     * 최대 낙폭 (Maximum Drawdown) 계산
     */
    public Map<String, Object> calculateMaximumDrawdown(List<BigDecimal> portfolioValues) {
        if (portfolioValues == null || portfolioValues.size() < 2) {
            return Map.of("maxDrawdown", BigDecimal.ZERO, "duration", 0, "recovery", false);
        }

        BigDecimal peak = portfolioValues.get(0);
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        int maxDrawdownStart = 0;
        int maxDrawdownEnd = 0;
        int currentDrawdownStart = 0;

        for (int i = 1; i < portfolioValues.size(); i++) {
            BigDecimal currentValue = portfolioValues.get(i);

            if (currentValue.compareTo(peak) > 0) {
                peak = currentValue;
                currentDrawdownStart = i;
            } else {
                BigDecimal drawdown = peak.subtract(currentValue).divide(peak, 6, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                    maxDrawdownStart = currentDrawdownStart;
                    maxDrawdownEnd = i;
                }
            }
        }

        int duration = maxDrawdownEnd - maxDrawdownStart;
        boolean recovered = portfolioValues.get(portfolioValues.size() - 1).compareTo(
                portfolioValues.get(maxDrawdownStart)) >= 0;

        return Map.of(
                "maxDrawdown", maxDrawdown,
                "duration", duration,
                "recovery", recovered,
                "startIndex", maxDrawdownStart,
                "endIndex", maxDrawdownEnd
        );
    }

    /**
     * 샤프 비율 계산
     */
    public Double calculateSharpeRatio(
            List<BigDecimal> portfolioReturns,
            BigDecimal riskFreeRate) {

        if (portfolioReturns == null || portfolioReturns.isEmpty()) {
            return 0.0;
        }

        BigDecimal avgReturn = portfolioReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioReturns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal excessReturn = avgReturn.subtract(riskFreeRate);

        double variance = portfolioReturns.stream()
                .mapToDouble(ret -> ret.subtract(avgReturn).pow(2).doubleValue())
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);

        return standardDeviation == 0.0 ? 0.0 : excessReturn.doubleValue() / standardDeviation;
    }

    /**
     * 소르티노 비율 계산 (하방 위험만 고려)
     */
    public Double calculateSortinoRatio(
            List<BigDecimal> portfolioReturns,
            BigDecimal targetReturn) {

        if (portfolioReturns == null || portfolioReturns.isEmpty()) {
            return 0.0;
        }

        BigDecimal avgReturn = portfolioReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioReturns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal excessReturn = avgReturn.subtract(targetReturn);

        // 하방 편차 계산 (목표 수익률 이하의 수익률만 고려)
        double downsideVariance = portfolioReturns.stream()
                .filter(ret -> ret.compareTo(targetReturn) < 0)
                .mapToDouble(ret -> Math.pow(ret.subtract(targetReturn).doubleValue(), 2))
                .average()
                .orElse(0.0);

        double downsideDeviation = Math.sqrt(downsideVariance);

        return downsideDeviation == 0.0 ? 0.0 : excessReturn.doubleValue() / downsideDeviation;
    }

    /**
     * 칼마 비율 계산 (연간 수익률 / 최대 낙폭)
     */
    public Double calculateCalmarRatio(
            List<BigDecimal> portfolioReturns,
            List<BigDecimal> portfolioValues) {

        if (portfolioReturns == null || portfolioReturns.isEmpty() ||
                portfolioValues == null || portfolioValues.isEmpty()) {
            return 0.0;
        }

        // 연간 수익률 계산
        BigDecimal totalReturn = portfolioReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double annualizedReturn = totalReturn.doubleValue() * (252.0 / portfolioReturns.size());

        // 최대 낙폭 계산
        Map<String, Object> drawdownInfo = calculateMaximumDrawdown(portfolioValues);
        BigDecimal maxDrawdown = (BigDecimal) drawdownInfo.get("maxDrawdown");

        return maxDrawdown.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                annualizedReturn / maxDrawdown.doubleValue();
    }

    /**
     * 정보 비율 계산 (초과 수익률 / 추적 오차)
     */
    public Double calculateInformationRatio(
            List<BigDecimal> portfolioReturns,
            List<BigDecimal> benchmarkReturns) {

        if (portfolioReturns == null || benchmarkReturns == null ||
                portfolioReturns.size() != benchmarkReturns.size()) {
            return 0.0;
        }

        List<BigDecimal> excessReturns = new ArrayList<>();
        for (int i = 0; i < portfolioReturns.size(); i++) {
            excessReturns.add(portfolioReturns.get(i).subtract(benchmarkReturns.get(i)));
        }

        BigDecimal avgExcessReturn = excessReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(excessReturns.size()), 6, RoundingMode.HALF_UP);

        double trackingError = Math.sqrt(excessReturns.stream()
                .mapToDouble(ret -> Math.pow(ret.subtract(avgExcessReturn).doubleValue(), 2))
                .average()
                .orElse(0.0));

        return trackingError == 0.0 ? 0.0 : avgExcessReturn.doubleValue() / trackingError;
    }

    /**
     * 베타 계산 (시장 민감도)
     */
    public Double calculateBeta(
            List<BigDecimal> portfolioReturns,
            List<BigDecimal> marketReturns) {

        if (portfolioReturns == null || marketReturns == null ||
                portfolioReturns.size() != marketReturns.size() || portfolioReturns.size() < 2) {
            return 1.0;
        }

        BigDecimal avgPortfolioReturn = portfolioReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioReturns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal avgMarketReturn = marketReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(marketReturns.size()), 6, RoundingMode.HALF_UP);

        double covariance = 0.0;
        double marketVariance = 0.0;

        for (int i = 0; i < portfolioReturns.size(); i++) {
            double portfolioDev = portfolioReturns.get(i).subtract(avgPortfolioReturn).doubleValue();
            double marketDev = marketReturns.get(i).subtract(avgMarketReturn).doubleValue();

            covariance += portfolioDev * marketDev;
            marketVariance += marketDev * marketDev;
        }

        covariance /= (portfolioReturns.size() - 1);
        marketVariance /= (marketReturns.size() - 1);

        return marketVariance == 0.0 ? 1.0 : covariance / marketVariance;
    }

    /**
     * 알파 계산 (초과 수익률)
     */
    public Double calculateAlpha(
            List<BigDecimal> portfolioReturns,
            List<BigDecimal> marketReturns,
            BigDecimal riskFreeRate) {

        if (portfolioReturns == null || marketReturns == null ||
                portfolioReturns.size() != marketReturns.size()) {
            return 0.0;
        }

        BigDecimal avgPortfolioReturn = portfolioReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioReturns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal avgMarketReturn = marketReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(marketReturns.size()), 6, RoundingMode.HALF_UP);

        Double beta = calculateBeta(portfolioReturns, marketReturns);

        // Alpha = 포트폴리오 수익률 - (무위험 수익률 + 베타 * (시장 수익률 - 무위험 수익률))
        BigDecimal expectedReturn = riskFreeRate.add(
                avgMarketReturn.subtract(riskFreeRate).multiply(BigDecimal.valueOf(beta))
        );

        return avgPortfolioReturn.subtract(expectedReturn).doubleValue();
    }

    /**
     * 델타 민감도 계산 (가격 민감도)
     */
    public Map<String, BigDecimal> calculateDeltaSensitivity(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal shockSize) {

        Map<String, BigDecimal> deltaSensitivity = new HashMap<>();

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal currentPrice = currentPrices.get(instrument);

            if (currentPrice != null) {
                BigDecimal shockedPrice = currentPrice.multiply(BigDecimal.ONE.add(shockSize));
                BigDecimal currentValue = position.calculateCurrentValue(currentPrice);
                BigDecimal shockedValue = position.calculateCurrentValue(shockedPrice);

                BigDecimal priceDelta = shockedPrice.subtract(currentPrice);
                BigDecimal valueDelta = shockedValue.subtract(currentValue);

                BigDecimal delta = priceDelta.compareTo(BigDecimal.ZERO) != 0 ?
                        valueDelta.divide(priceDelta, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                deltaSensitivity.put(instrument, delta);
            }
        }

        return deltaSensitivity;
    }

    /**
     * 감마 민감도 계산 (2차 가격 민감도)
     */
    public Map<String, BigDecimal> calculateGammaSensitivity(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            BigDecimal shockSize) {

        Map<String, BigDecimal> gammaSensitivity = new HashMap<>();

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal currentPrice = currentPrices.get(instrument);

            if (currentPrice != null) {
                // 상승, 현재, 하락 가격에서의 포지션 가치
                BigDecimal upPrice = currentPrice.multiply(BigDecimal.ONE.add(shockSize));
                BigDecimal downPrice = currentPrice.multiply(BigDecimal.ONE.subtract(shockSize));

                BigDecimal upValue = position.calculateCurrentValue(upPrice);
                BigDecimal currentValue = position.calculateCurrentValue(currentPrice);
                BigDecimal downValue = position.calculateCurrentValue(downPrice);

                // 2차 차분을 이용한 감마 계산
                BigDecimal gamma = upValue.add(downValue).subtract(currentValue.multiply(BigDecimal.valueOf(2)))
                        .divide(currentPrice.multiply(shockSize).pow(2), 4, RoundingMode.HALF_UP);

                gammaSensitivity.put(instrument, gamma);
            }
        }

        return gammaSensitivity;
    }

    // === Private Helper Methods ===

    private void validateInputs(List<PortfolioPosition> positions,
                               Map<String, BigDecimal> currentPrices,
                               Map<String, Double> expectedReturns,
                               Map<String, Double> volatilities) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("포트폴리오 포지션이 필요합니다");
        }
        if (currentPrices == null || currentPrices.isEmpty()) {
            throw new IllegalArgumentException("현재 가격 정보가 필요합니다");
        }
        if (expectedReturns == null || expectedReturns.isEmpty()) {
            throw new IllegalArgumentException("기대 수익률 정보가 필요합니다");
        }
        if (volatilities == null || volatilities.isEmpty()) {
            throw new IllegalArgumentException("변동성 정보가 필요합니다");
        }
    }

    private Map<String, BigDecimal> calculatePortfolioWeights(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices) {

        BigDecimal totalValue = calculateTotalPortfolioValue(positions, currentPrices);
        Map<String, BigDecimal> weights = new HashMap<>();

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal price = currentPrices.get(instrument);

            if (price != null) {
                BigDecimal positionValue = position.calculateCurrentValue(price);
                BigDecimal weight = positionValue.divide(totalValue, 6, RoundingMode.HALF_UP);
                weights.put(instrument, weight);
            }
        }

        return weights;
    }

    private BigDecimal calculateTotalPortfolioValue(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices) {

        return positions.stream()
                .filter(PortfolioPosition::hasPosition)
                .map(position -> {
                    BigDecimal price = currentPrices.get(position.getInstrumentKey());
                    return price != null ? position.calculateCurrentValue(price) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculatePortfolioExpectedReturn(
            Map<String, BigDecimal> weights,
            Map<String, Double> expectedReturns) {

        return weights.entrySet().stream()
                .mapToDouble(entry -> {
                    String instrument = entry.getKey();
                    BigDecimal weight = entry.getValue();
                    Double expectedReturn = expectedReturns.get(instrument);
                    return expectedReturn != null ? weight.doubleValue() * expectedReturn : 0.0;
                })
                .sum();
    }

    private double calculatePortfolioVolatility(
            Map<String, BigDecimal> weights,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix) {

        double variance = 0.0;

        for (Map.Entry<String, BigDecimal> entry1 : weights.entrySet()) {
            String asset1 = entry1.getKey();
            double weight1 = entry1.getValue().doubleValue();
            Double vol1 = volatilities.get(asset1);

            if (vol1 == null) continue;

            for (Map.Entry<String, BigDecimal> entry2 : weights.entrySet()) {
                String asset2 = entry2.getKey();
                double weight2 = entry2.getValue().doubleValue();
                Double vol2 = volatilities.get(asset2);

                if (vol2 == null) continue;

                double correlation = 1.0; // 기본값
                if (correlationMatrix != null && correlationMatrix.containsKey(asset1) &&
                        correlationMatrix.get(asset1).containsKey(asset2)) {
                    correlation = correlationMatrix.get(asset1).get(asset2);
                }

                variance += weight1 * weight2 * vol1 * vol2 * correlation;
            }
        }

        return Math.sqrt(variance);
    }

    private double calculateZScore(double confidenceLevel) {
        // 근사값 사용 (정확한 값은 통계 라이브러리 필요)
        if (confidenceLevel >= 0.99) return 2.33;
        if (confidenceLevel >= 0.95) return 1.65;
        if (confidenceLevel >= 0.90) return 1.28;
        return 1.65; // 기본값
    }

    private Map<String, Double> generateCorrelatedRandomReturns(
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix,
            Random random,
            Integer timeHorizon) {

        Map<String, Double> randomReturns = new HashMap<>();
        double timeAdjustment = Math.sqrt(timeHorizon / 252.0);

        for (String asset : expectedReturns.keySet()) {
            double expected = expectedReturns.get(asset) * timeAdjustment;
            double vol = volatilities.get(asset) * timeAdjustment;
            double randomReturn = expected + vol * random.nextGaussian();
            randomReturns.put(asset, randomReturn);
        }

        return randomReturns;
    }

    private BigDecimal calculatePortfolioReturnFromRandoms(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> randomReturns) {

        BigDecimal totalValue = calculateTotalPortfolioValue(positions, currentPrices);
        BigDecimal weightedReturn = BigDecimal.ZERO;

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal price = currentPrices.get(instrument);
            Double randomReturn = randomReturns.get(instrument);

            if (price != null && randomReturn != null) {
                BigDecimal positionValue = position.calculateCurrentValue(price);
                BigDecimal weight = positionValue.divide(totalValue, 6, RoundingMode.HALF_UP);
                weightedReturn = weightedReturn.add(weight.multiply(BigDecimal.valueOf(randomReturn)));
            }
        }

        return weightedReturn;
    }
}