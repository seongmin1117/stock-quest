package com.stockquest.domain.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 몬테카를로 시뮬레이션 도메인 엔티티
 * 포트폴리오 가치 경로 시뮬레이션 및 리스크 분석
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonteCarloSimulation {

    private String simulationId;
    private Long portfolioId;
    private LocalDateTime createdAt;
    private Integer numberOfRuns;
    private Integer timeHorizon;            // 시뮬레이션 기간 (일)
    private Double confidenceLevel;

    // 시뮬레이션 파라미터
    private Map<String, Double> expectedReturns;    // 자산별 기대 수익률
    private Map<String, Double> volatilities;       // 자산별 변동성
    private Map<String, Map<String, Double>> correlationMatrix; // 상관관계 행렬

    // 시뮬레이션 결과
    private List<BigDecimal> portfolioValuePaths;   // 포트폴리오 가치 경로들
    private Map<String, List<BigDecimal>> assetPaths; // 자산별 가격 경로들

    // 통계적 결과
    private BigDecimal meanFinalValue;              // 최종 가치 평균
    private BigDecimal medianFinalValue;            // 최종 가치 중간값
    private BigDecimal standardDeviation;           // 표준편차
    private BigDecimal percentile5;                 // 5% 분위수
    private BigDecimal percentile95;                // 95% 분위수
    private BigDecimal percentile99;                // 99% 분위수

    // 리스크 메트릭
    private BigDecimal valueAtRisk95;               // 95% VaR
    private BigDecimal valueAtRisk99;               // 99% VaR
    private BigDecimal expectedShortfall;           // Expected Shortfall
    private Double probabilityOfLoss;               // 손실 확률
    private BigDecimal worstCaseScenario;          // 최악의 시나리오


    /**
     * 시뮬레이션 설정 유효성 검증
     */
    public void validate() {
        if (simulationId == null || simulationId.trim().isEmpty()) {
            throw new IllegalArgumentException("시뮬레이션 ID는 필수입니다");
        }
        if (portfolioId == null || portfolioId <= 0) {
            throw new IllegalArgumentException("포트폴리오 ID는 유효해야 합니다");
        }
        if (numberOfRuns == null || numberOfRuns < 1000) {
            throw new IllegalArgumentException("시뮬레이션 횟수는 최소 1000회 이상이어야 합니다");
        }
        if (timeHorizon == null || timeHorizon <= 0) {
            throw new IllegalArgumentException("시뮬레이션 기간은 양수여야 합니다");
        }
        if (confidenceLevel != null && (confidenceLevel <= 0.0 || confidenceLevel >= 1.0)) {
            throw new IllegalArgumentException("신뢰 수준은 0과 1 사이여야 합니다");
        }
    }

    /**
     * VaR 계산 (Value at Risk)
     */
    public BigDecimal calculateVaR(Double confidenceLevel, BigDecimal initialValue) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> returns = portfolioValuePaths.stream()
                .map(finalValue -> finalValue.subtract(initialValue)
                        .divide(initialValue, 6, RoundingMode.HALF_UP))
                .sorted()
                .toList();

        int index = (int) Math.floor((1.0 - confidenceLevel) * returns.size());
        index = Math.max(0, Math.min(index, returns.size() - 1));

        return returns.get(index).multiply(initialValue);
    }

    /**
     * Expected Shortfall (CVaR) 계산
     */
    public BigDecimal calculateExpectedShortfall(Double confidenceLevel, BigDecimal initialValue) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> returns = portfolioValuePaths.stream()
                .map(finalValue -> finalValue.subtract(initialValue)
                        .divide(initialValue, 6, RoundingMode.HALF_UP))
                .sorted()
                .toList();

        int cutoffIndex = (int) Math.floor((1.0 - confidenceLevel) * returns.size());
        cutoffIndex = Math.max(0, Math.min(cutoffIndex, returns.size() - 1));

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i <= cutoffIndex; i++) {
            sum = sum.add(returns.get(i));
        }

        BigDecimal averageReturn = sum.divide(BigDecimal.valueOf(cutoffIndex + 1), 6, RoundingMode.HALF_UP);
        return averageReturn.multiply(initialValue);
    }

    /**
     * 손실 확률 계산
     */
    public Double calculateProbabilityOfLoss(BigDecimal initialValue) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return 0.0;
        }

        long lossCount = portfolioValuePaths.stream()
                .mapToLong(finalValue -> finalValue.compareTo(initialValue) < 0 ? 1L : 0L)
                .sum();

        return (double) lossCount / portfolioValuePaths.size();
    }

    /**
     * 특정 손실 임계값 초과 확률 계산
     */
    public Double calculateProbabilityOfExceedingLoss(BigDecimal lossThreshold, BigDecimal initialValue) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return 0.0;
        }

        BigDecimal thresholdValue = initialValue.subtract(lossThreshold);

        long exceedCount = portfolioValuePaths.stream()
                .mapToLong(finalValue -> finalValue.compareTo(thresholdValue) < 0 ? 1L : 0L)
                .sum();

        return (double) exceedCount / portfolioValuePaths.size();
    }

    /**
     * 분위수 계산
     */
    public BigDecimal calculatePercentile(Double percentile) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> sortedValues = portfolioValuePaths.stream()
                .sorted()
                .toList();

        int index = (int) Math.floor(percentile * sortedValues.size());
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index);
    }

    /**
     * 포트폴리오 수익률 분포 통계
     */
    public Map<String, BigDecimal> calculateReturnStatistics(BigDecimal initialValue) {
        if (portfolioValuePaths == null || portfolioValuePaths.isEmpty()) {
            return Map.of();
        }

        List<BigDecimal> returns = portfolioValuePaths.stream()
                .map(finalValue -> finalValue.subtract(initialValue)
                        .divide(initialValue, 6, RoundingMode.HALF_UP))
                .toList();

        BigDecimal meanReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
                .map(ret -> ret.subtract(meanReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size() - 1), 6, RoundingMode.HALF_UP);

        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        return Map.of(
                "mean", meanReturn,
                "standardDeviation", stdDev,
                "variance", variance,
                "skewness", calculateSkewness(returns, meanReturn, stdDev),
                "kurtosis", calculateKurtosis(returns, meanReturn, stdDev)
        );
    }

    /**
     * 왜도(Skewness) 계산
     */
    private BigDecimal calculateSkewness(List<BigDecimal> returns, BigDecimal mean, BigDecimal stdDev) {
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = returns.stream()
                .map(ret -> ret.subtract(mean).divide(stdDev, 6, RoundingMode.HALF_UP).pow(3))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
    }

    /**
     * 첨도(Kurtosis) 계산
     */
    private BigDecimal calculateKurtosis(List<BigDecimal> returns, BigDecimal mean, BigDecimal stdDev) {
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = returns.stream()
                .map(ret -> ret.subtract(mean).divide(stdDev, 6, RoundingMode.HALF_UP).pow(4))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal kurtosis = sum.divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        return kurtosis.subtract(BigDecimal.valueOf(3)); // 초과 첨도
    }

    /**
     * 시뮬레이션 수렴성 검증
     */
    public boolean isConverged(Double tolerance) {
        if (portfolioValuePaths == null || portfolioValuePaths.size() < 2) {
            return false;
        }

        // 최근 시뮬레이션 결과들의 변동성이 허용 범위 내인지 확인
        int checkSize = Math.min(1000, portfolioValuePaths.size() / 10);
        List<BigDecimal> recentPaths = portfolioValuePaths.subList(
                portfolioValuePaths.size() - checkSize, portfolioValuePaths.size());

        BigDecimal mean = recentPaths.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(recentPaths.size()), 6, RoundingMode.HALF_UP);

        BigDecimal variance = recentPaths.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(recentPaths.size() - 1), 6, RoundingMode.HALF_UP);

        double coefficientOfVariation = Math.sqrt(variance.doubleValue()) / mean.doubleValue();
        return coefficientOfVariation < tolerance;
    }

    /**
     * 시뮬레이션 품질 점수 계산 (0-100)
     */
    public Integer calculateQualityScore() {
        int score = 0;

        // 시뮬레이션 횟수 점수 (최대 30점)
        if (numberOfRuns >= 100000) score += 30;
        else if (numberOfRuns >= 50000) score += 25;
        else if (numberOfRuns >= 10000) score += 20;
        else if (numberOfRuns >= 5000) score += 15;
        else if (numberOfRuns >= 1000) score += 10;

        // 수렴성 점수 (최대 25점)
        if (isConverged(0.01)) score += 25;
        else if (isConverged(0.05)) score += 20;
        else if (isConverged(0.10)) score += 15;

        // 시간 범위 점수 (최대 20점)
        if (timeHorizon >= 252) score += 20;      // 1년 이상
        else if (timeHorizon >= 126) score += 15; // 6개월 이상
        else if (timeHorizon >= 63) score += 10;  // 3개월 이상
        else if (timeHorizon >= 21) score += 5;   // 1개월 이상

        // 데이터 완성도 점수 (최대 25점)
        int completeness = 0;
        if (portfolioValuePaths != null && !portfolioValuePaths.isEmpty()) completeness += 10;
        if (expectedReturns != null && !expectedReturns.isEmpty()) completeness += 5;
        if (volatilities != null && !volatilities.isEmpty()) completeness += 5;
        if (correlationMatrix != null && !correlationMatrix.isEmpty()) completeness += 5;
        score += completeness;

        return Math.min(score, 100);
    }
}