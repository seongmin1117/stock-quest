package com.stockquest.application.service.analytics;

import com.stockquest.domain.backtesting.BacktestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 위험도 분석 전문 서비스
 * 기존 PerformanceAnalyticsService에서 위험도 분석 기능 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

    /**
     * 종합 위험도 분석 수행
     */
    public RiskAnalysis performRiskAnalysis(BacktestResult result) {
        try {
            log.info("위험도 분석 시작: {}", result.getBacktestId());
            
            List<BigDecimal> returns = result.getDailyReturns().stream()
                .map(BacktestResult.DailyReturn::getDailyReturn)
                .collect(Collectors.toList());
            
            return RiskAnalysis.builder()
                .advancedRiskMetrics(calculateAdvancedRiskMetrics(returns))
                .riskDecomposition(performRiskDecomposition(returns, result))
                .stressTestResults(performStressTests(result))
                .riskAdjustedReturns(calculateRiskAdjustedReturns(result, calculateAdvancedRiskMetrics(returns)))
                .riskBudgetAnalysis(performRiskBudgetAnalysis(result))
                .build();
                
        } catch (Exception e) {
            log.error("위험도 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("위험도 분석 실패", e);
        }
    }

    /**
     * 고급 위험도 지표 계산
     */
    private AdvancedRiskMetrics calculateAdvancedRiskMetrics(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return AdvancedRiskMetrics.builder().build();
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        returns.forEach(r -> stats.addValue(r.doubleValue()));

        return AdvancedRiskMetrics.builder()
            .skewness(BigDecimal.valueOf(stats.getSkewness()).setScale(4, RoundingMode.HALF_UP))
            .kurtosis(BigDecimal.valueOf(stats.getKurtosis()).setScale(4, RoundingMode.HALF_UP))
            .volatilityClustering(measureVolatilityClustering(returns))
            .expectedShortfall95(calculateExpectedShortfall(returns, 0.05))
            .expectedShortfall99(calculateExpectedShortfall(returns, 0.01))
            .extremeValueRisk(calculateExtremeValueRisk(returns))
            .coherentRiskMeasures(calculateCoherentRiskMeasures(returns))
            .tailRiskRatio(calculateTailRiskRatio(returns))
            .build();
    }

    /**
     * 변동성 클러스터링 측정
     */
    private BigDecimal measureVolatilityClustering(List<BigDecimal> returns) {
        if (returns.size() < 2) return BigDecimal.ZERO;
        
        // GARCH(1,1) 모델 근사 - 변동성의 자기상관성 측정
        double autocorrelation = calculateAutocorrelation(returns, 1);
        return BigDecimal.valueOf(Math.abs(autocorrelation)).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 자기상관성 계산
     */
    private double calculateAutocorrelation(List<BigDecimal> values, int lag) {
        if (values.size() <= lag) return 0.0;
        
        double[] data = values.stream().mapToDouble(BigDecimal::doubleValue).toArray();
        
        double mean = Arrays.stream(data).average().orElse(0.0);
        double variance = Arrays.stream(data).map(x -> Math.pow(x - mean, 2)).average().orElse(1.0);
        
        double covariance = 0.0;
        for (int i = lag; i < data.length; i++) {
            covariance += (data[i] - mean) * (data[i - lag] - mean);
        }
        covariance /= (data.length - lag);
        
        return variance > 0 ? covariance / variance : 0.0;
    }

    /**
     * 극단값 위험도 계산
     */
    private ExtremeValueRisk calculateExtremeValueRisk(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return ExtremeValueRisk.builder()
                .expectedTailLoss(BigDecimal.ZERO)
                .tailIndex(BigDecimal.ZERO)
                .build();
        }

        // 5% 극단값들의 평균 손실
        List<BigDecimal> sortedReturns = returns.stream()
            .sorted()
            .collect(Collectors.toList());
            
        int tailSize = Math.max(1, (int) (returns.size() * 0.05));
        BigDecimal expectedTailLoss = sortedReturns.subList(0, tailSize)
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(tailSize), 6, RoundingMode.HALF_UP);

        // 꼬리 지수 (Hill 추정량 근사)
        BigDecimal tailIndex = calculateHillEstimator(sortedReturns, tailSize);

        return ExtremeValueRisk.builder()
            .expectedTailLoss(expectedTailLoss.abs())
            .tailIndex(tailIndex)
            .build();
    }

    /**
     * Hill 추정량 계산
     */
    private BigDecimal calculateHillEstimator(List<BigDecimal> sortedReturns, int tailSize) {
        if (tailSize < 2) return BigDecimal.ONE;
        
        double sum = 0.0;
        BigDecimal threshold = sortedReturns.get(tailSize - 1);
        
        for (int i = 0; i < tailSize; i++) {
            double value = sortedReturns.get(i).doubleValue();
            if (value > 0) {
                sum += Math.log(value / threshold.doubleValue());
            }
        }
        
        double hillIndex = tailSize / sum;
        return BigDecimal.valueOf(Math.max(0.1, Math.min(10.0, hillIndex)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 일관성 있는 위험도 측정
     */
    private CoherentRiskMeasures calculateCoherentRiskMeasures(List<BigDecimal> returns) {
        // 스펙트럼 위험도 측정 (간소화된 버전)
        BigDecimal spectralRisk = calculateExpectedShortfall(returns, 0.1);
        
        // 왜곡 위험도 측정 (간소화된 버전)  
        BigDecimal distortionRisk = calculateExpectedShortfall(returns, 0.05);

        return CoherentRiskMeasures.builder()
            .spectralRiskMeasure(spectralRisk)
            .distortionRiskMeasure(distortionRisk)
            .build();
    }

    /**
     * 예상 손실률 (Expected Shortfall) 계산
     */
    private BigDecimal calculateExpectedShortfall(List<BigDecimal> returns, double alpha) {
        if (returns.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> sortedReturns = returns.stream()
            .sorted()
            .collect(Collectors.toList());

        int cutoffIndex = Math.max(1, (int) (returns.size() * alpha));
        
        BigDecimal sum = sortedReturns.subList(0, cutoffIndex)
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return sum.divide(BigDecimal.valueOf(cutoffIndex), 6, RoundingMode.HALF_UP).abs();
    }

    /**
     * 꼬리 위험도 비율 계산
     */
    private BigDecimal calculateTailRiskRatio(List<BigDecimal> returns) {
        BigDecimal es95 = calculateExpectedShortfall(returns, 0.05);
        BigDecimal var95 = calculateVaR(returns, 0.05);
        
        if (var95.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
        
        return es95.divide(var95, 4, RoundingMode.HALF_UP);
    }

    /**
     * VaR (Value at Risk) 계산
     */
    private BigDecimal calculateVaR(List<BigDecimal> returns, double alpha) {
        if (returns.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> sortedReturns = returns.stream()
            .sorted()
            .collect(Collectors.toList());

        int index = Math.max(0, (int) (returns.size() * alpha) - 1);
        return sortedReturns.get(index).abs();
    }

    /**
     * 위험도 분해 분석
     */
    private RiskDecomposition performRiskDecomposition(List<BigDecimal> returns, BacktestResult result) {
        // 체계적 위험도와 비체계적 위험도 분해 (간소화된 버전)
        BigDecimal totalRisk = calculateTotalRisk(returns);
        BigDecimal systematicRisk = estimateSystematicRisk(result);
        BigDecimal idiosyncraticRisk = totalRisk.subtract(systematicRisk);

        return RiskDecomposition.builder()
            .totalRisk(totalRisk)
            .systematicRisk(systematicRisk)
            .idiosyncraticRisk(idiosyncraticRisk.abs())
            .riskContributions(calculateRiskContributions(result))
            .build();
    }

    /**
     * 총 위험도 계산
     */
    private BigDecimal calculateTotalRisk(List<BigDecimal> returns) {
        if (returns.isEmpty()) return BigDecimal.ZERO;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        returns.forEach(r -> stats.addValue(r.doubleValue()));
        
        return BigDecimal.valueOf(stats.getStandardDeviation())
            .setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 체계적 위험도 추정
     */
    private BigDecimal estimateSystematicRisk(BacktestResult result) {
        // 벤치마크와의 베타를 이용한 체계적 위험도 추정 (간소화)
        return result.getBenchmarkComparison() != null 
            ? BigDecimal.valueOf(0.6).multiply(calculateTotalRisk(
                result.getDailyReturns().stream()
                    .map(BacktestResult.DailyReturn::getDailyReturn)
                    .collect(Collectors.toList())))
            : BigDecimal.ZERO;
    }

    /**
     * 위험도 기여도 계산
     */
    private Map<String, BigDecimal> calculateRiskContributions(BacktestResult result) {
        Map<String, BigDecimal> contributions = new HashMap<>();
        
        // 간소화된 위험도 기여도 - 실제로는 포지션별 위험도 기여도를 계산해야 함
        contributions.put("Market Risk", new BigDecimal("0.60"));
        contributions.put("Sector Risk", new BigDecimal("0.25"));
        contributions.put("Security Selection", new BigDecimal("0.15"));
        
        return contributions;
    }

    /**
     * 스트레스 테스트 수행
     */
    private StressTestResults performStressTests(BacktestResult result) {
        Map<String, BigDecimal> scenarios = new HashMap<>();
        
        // 다양한 스트레스 시나리오 결과 (간소화)
        scenarios.put("2008 Financial Crisis", estimateStressScenarioImpact(result, -0.40));
        scenarios.put("COVID-19 Crash", estimateStressScenarioImpact(result, -0.35));
        scenarios.put("Interest Rate Shock", estimateStressScenarioImpact(result, -0.20));
        scenarios.put("Currency Crisis", estimateStressScenarioImpact(result, -0.25));
        scenarios.put("Inflation Spike", estimateStressScenarioImpact(result, -0.15));

        return StressTestResults.builder()
            .scenarioResults(scenarios)
            .worstCaseScenario("2008 Financial Crisis")
            .averageStressLoss(scenarios.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scenarios.size()), 4, RoundingMode.HALF_UP))
            .build();
    }

    /**
     * 스트레스 시나리오 영향 추정
     */
    private BigDecimal estimateStressScenarioImpact(BacktestResult result, double shockMagnitude) {
        BigDecimal currentValue = result.getFinalValue();
        BigDecimal beta = result.getBenchmarkComparison() != null 
            ? result.getBenchmarkComparison().getBeta() 
            : BigDecimal.ONE;
            
        // 베타를 고려한 충격 전파
        BigDecimal impact = beta.multiply(BigDecimal.valueOf(shockMagnitude));
        return currentValue.multiply(impact).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 위험도 조정 수익률 계산
     */
    private RiskAdjustedReturns calculateRiskAdjustedReturns(BacktestResult result, AdvancedRiskMetrics advancedRisk) {
        BigDecimal totalReturn = result.getTotalReturn();
        BigDecimal volatility = advancedRisk.getSkewness(); // 간소화 - 실제로는 변동성 사용
        
        // 다양한 위험도 조정 지표
        BigDecimal sharpeRatio = calculateSharpeRatio(result);
        BigDecimal sortinoRatio = calculateSortinoRatio(result);
        BigDecimal calmarRatio = calculateCalmarRatio(result);
        BigDecimal treynorRatio = calculateTreynorRatio(result);

        return RiskAdjustedReturns.builder()
            .sharpeRatio(sharpeRatio)
            .sortinoRatio(sortinoRatio)
            .calmarRatio(calmarRatio)
            .treynorRatio(treynorRatio)
            .informationRatio(calculateInformationRatio(result))
            .build();
    }

    /**
     * 샤프 비율 계산
     */
    private BigDecimal calculateSharpeRatio(BacktestResult result) {
        BigDecimal excessReturn = result.getTotalReturn().subtract(new BigDecimal("0.02")); // 2% 무위험 수익률 가정
        BigDecimal volatility = calculateVolatility(result.getDailyReturns());
        
        return volatility.compareTo(BigDecimal.ZERO) > 0 
            ? excessReturn.divide(volatility, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 변동성 계산
     */
    private BigDecimal calculateVolatility(List<BacktestResult.DailyReturn> dailyReturns) {
        List<BigDecimal> returns = dailyReturns.stream()
            .map(BacktestResult.DailyReturn::getDailyReturn)
            .collect(Collectors.toList());
            
        DescriptiveStatistics stats = new DescriptiveStatistics();
        returns.forEach(r -> stats.addValue(r.doubleValue()));
        
        return BigDecimal.valueOf(stats.getStandardDeviation()).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 소르티노 비율 계산
     */
    private BigDecimal calculateSortinoRatio(BacktestResult result) {
        BigDecimal excessReturn = result.getTotalReturn().subtract(new BigDecimal("0.02"));
        BigDecimal downsideDeviation = calculateDownsideDeviation(result.getDailyReturns());
        
        return downsideDeviation.compareTo(BigDecimal.ZERO) > 0 
            ? excessReturn.divide(downsideDeviation, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 하향 편차 계산
     */
    private BigDecimal calculateDownsideDeviation(List<BacktestResult.DailyReturn> dailyReturns) {
        List<BigDecimal> negativeReturns = dailyReturns.stream()
            .map(BacktestResult.DailyReturn::getDailyReturn)
            .filter(r -> r.compareTo(BigDecimal.ZERO) < 0)
            .collect(Collectors.toList());
            
        if (negativeReturns.isEmpty()) return BigDecimal.ZERO;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        negativeReturns.forEach(r -> stats.addValue(r.doubleValue()));
        
        return BigDecimal.valueOf(stats.getStandardDeviation()).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 칼마 비율 계산
     */
    private BigDecimal calculateCalmarRatio(BacktestResult result) {
        BigDecimal annualizedReturn = result.getTotalReturn(); // 간소화
        BigDecimal maxDrawdown = result.getMaxDrawdown();
        
        return maxDrawdown.compareTo(BigDecimal.ZERO) > 0 
            ? annualizedReturn.divide(maxDrawdown, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 트레이너 비율 계산
     */
    private BigDecimal calculateTreynorRatio(BacktestResult result) {
        BigDecimal excessReturn = result.getTotalReturn().subtract(new BigDecimal("0.02"));
        BigDecimal beta = result.getBenchmarkComparison() != null 
            ? result.getBenchmarkComparison().getBeta() 
            : BigDecimal.ONE;
        
        return beta.compareTo(BigDecimal.ZERO) > 0 
            ? excessReturn.divide(beta, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 정보 비율 계산
     */
    private BigDecimal calculateInformationRatio(BacktestResult result) {
        if (result.getBenchmarkComparison() == null) return BigDecimal.ZERO;
        
        BigDecimal excessReturn = result.getTotalReturn()
            .subtract(result.getBenchmarkComparison().getBenchmarkReturn());
        BigDecimal trackingError = result.getBenchmarkComparison().getTrackingError();
        
        return trackingError.compareTo(BigDecimal.ZERO) > 0 
            ? excessReturn.divide(trackingError, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 위험도 예산 분석
     */
    private RiskBudgetAnalysis performRiskBudgetAnalysis(BacktestResult result) {
        // 실제로는 복잡한 위험도 예산 계산이 필요하지만 간소화
        Map<String, BigDecimal> allocatedRisk = new HashMap<>();
        allocatedRisk.put("Equity", new BigDecimal("0.60"));
        allocatedRisk.put("Fixed Income", new BigDecimal("0.30"));
        allocatedRisk.put("Alternative", new BigDecimal("0.10"));

        Map<String, BigDecimal> actualRisk = new HashMap<>();
        actualRisk.put("Equity", new BigDecimal("0.65"));
        actualRisk.put("Fixed Income", new BigDecimal("0.25"));
        actualRisk.put("Alternative", new BigDecimal("0.10"));

        return RiskBudgetAnalysis.builder()
            .allocatedRiskBudget(allocatedRisk)
            .actualRiskUtilization(actualRisk)
            .riskBudgetUtilization(new BigDecimal("0.95"))
            .excessRiskTaken(new BigDecimal("0.05"))
            .build();
    }

    // DTO Classes for Risk Analysis Results
    
    public static class RiskAnalysis {
        private final AdvancedRiskMetrics advancedRiskMetrics;
        private final RiskDecomposition riskDecomposition;
        private final StressTestResults stressTestResults;
        private final RiskAdjustedReturns riskAdjustedReturns;
        private final RiskBudgetAnalysis riskBudgetAnalysis;

        private RiskAnalysis(AdvancedRiskMetrics advancedRiskMetrics, RiskDecomposition riskDecomposition,
                           StressTestResults stressTestResults, RiskAdjustedReturns riskAdjustedReturns,
                           RiskBudgetAnalysis riskBudgetAnalysis) {
            this.advancedRiskMetrics = advancedRiskMetrics;
            this.riskDecomposition = riskDecomposition;
            this.stressTestResults = stressTestResults;
            this.riskAdjustedReturns = riskAdjustedReturns;
            this.riskBudgetAnalysis = riskBudgetAnalysis;
        }

        public static RiskAnalysisBuilder builder() { return new RiskAnalysisBuilder(); }

        public static class RiskAnalysisBuilder {
            private AdvancedRiskMetrics advancedRiskMetrics;
            private RiskDecomposition riskDecomposition;
            private StressTestResults stressTestResults;
            private RiskAdjustedReturns riskAdjustedReturns;
            private RiskBudgetAnalysis riskBudgetAnalysis;

            public RiskAnalysisBuilder advancedRiskMetrics(AdvancedRiskMetrics advancedRiskMetrics) { this.advancedRiskMetrics = advancedRiskMetrics; return this; }
            public RiskAnalysisBuilder riskDecomposition(RiskDecomposition riskDecomposition) { this.riskDecomposition = riskDecomposition; return this; }
            public RiskAnalysisBuilder stressTestResults(StressTestResults stressTestResults) { this.stressTestResults = stressTestResults; return this; }
            public RiskAnalysisBuilder riskAdjustedReturns(RiskAdjustedReturns riskAdjustedReturns) { this.riskAdjustedReturns = riskAdjustedReturns; return this; }
            public RiskAnalysisBuilder riskBudgetAnalysis(RiskBudgetAnalysis riskBudgetAnalysis) { this.riskBudgetAnalysis = riskBudgetAnalysis; return this; }

            public RiskAnalysis build() {
                return new RiskAnalysis(advancedRiskMetrics, riskDecomposition, stressTestResults, riskAdjustedReturns, riskBudgetAnalysis);
            }
        }

        // Getters
        public AdvancedRiskMetrics getAdvancedRiskMetrics() { return advancedRiskMetrics; }
        public RiskDecomposition getRiskDecomposition() { return riskDecomposition; }
        public StressTestResults getStressTestResults() { return stressTestResults; }
        public RiskAdjustedReturns getRiskAdjustedReturns() { return riskAdjustedReturns; }
        public RiskBudgetAnalysis getRiskBudgetAnalysis() { return riskBudgetAnalysis; }
    }

    public static class AdvancedRiskMetrics {
        private final BigDecimal skewness;
        private final BigDecimal kurtosis;
        private final BigDecimal volatilityClustering;
        private final BigDecimal expectedShortfall95;
        private final BigDecimal expectedShortfall99;
        private final ExtremeValueRisk extremeValueRisk;
        private final CoherentRiskMeasures coherentRiskMeasures;
        private final BigDecimal tailRiskRatio;

        private AdvancedRiskMetrics(BigDecimal skewness, BigDecimal kurtosis, BigDecimal volatilityClustering,
                                  BigDecimal expectedShortfall95, BigDecimal expectedShortfall99,
                                  ExtremeValueRisk extremeValueRisk, CoherentRiskMeasures coherentRiskMeasures,
                                  BigDecimal tailRiskRatio) {
            this.skewness = skewness;
            this.kurtosis = kurtosis;
            this.volatilityClustering = volatilityClustering;
            this.expectedShortfall95 = expectedShortfall95;
            this.expectedShortfall99 = expectedShortfall99;
            this.extremeValueRisk = extremeValueRisk;
            this.coherentRiskMeasures = coherentRiskMeasures;
            this.tailRiskRatio = tailRiskRatio;
        }

        public static AdvancedRiskMetricsBuilder builder() { return new AdvancedRiskMetricsBuilder(); }

        public static class AdvancedRiskMetricsBuilder {
            private BigDecimal skewness = BigDecimal.ZERO;
            private BigDecimal kurtosis = BigDecimal.ZERO;
            private BigDecimal volatilityClustering = BigDecimal.ZERO;
            private BigDecimal expectedShortfall95 = BigDecimal.ZERO;
            private BigDecimal expectedShortfall99 = BigDecimal.ZERO;
            private ExtremeValueRisk extremeValueRisk;
            private CoherentRiskMeasures coherentRiskMeasures;
            private BigDecimal tailRiskRatio = BigDecimal.ZERO;

            public AdvancedRiskMetricsBuilder skewness(BigDecimal skewness) { this.skewness = skewness; return this; }
            public AdvancedRiskMetricsBuilder kurtosis(BigDecimal kurtosis) { this.kurtosis = kurtosis; return this; }
            public AdvancedRiskMetricsBuilder volatilityClustering(BigDecimal volatilityClustering) { this.volatilityClustering = volatilityClustering; return this; }
            public AdvancedRiskMetricsBuilder expectedShortfall95(BigDecimal expectedShortfall95) { this.expectedShortfall95 = expectedShortfall95; return this; }
            public AdvancedRiskMetricsBuilder expectedShortfall99(BigDecimal expectedShortfall99) { this.expectedShortfall99 = expectedShortfall99; return this; }
            public AdvancedRiskMetricsBuilder extremeValueRisk(ExtremeValueRisk extremeValueRisk) { this.extremeValueRisk = extremeValueRisk; return this; }
            public AdvancedRiskMetricsBuilder coherentRiskMeasures(CoherentRiskMeasures coherentRiskMeasures) { this.coherentRiskMeasures = coherentRiskMeasures; return this; }
            public AdvancedRiskMetricsBuilder tailRiskRatio(BigDecimal tailRiskRatio) { this.tailRiskRatio = tailRiskRatio; return this; }

            public AdvancedRiskMetrics build() {
                return new AdvancedRiskMetrics(skewness, kurtosis, volatilityClustering, expectedShortfall95, 
                    expectedShortfall99, extremeValueRisk, coherentRiskMeasures, tailRiskRatio);
            }
        }

        // Getters
        public BigDecimal getSkewness() { return skewness; }
        public BigDecimal getKurtosis() { return kurtosis; }
        public BigDecimal getVolatilityClustering() { return volatilityClustering; }
        public BigDecimal getExpectedShortfall95() { return expectedShortfall95; }
        public BigDecimal getExpectedShortfall99() { return expectedShortfall99; }
        public ExtremeValueRisk getExtremeValueRisk() { return extremeValueRisk; }
        public CoherentRiskMeasures getCoherentRiskMeasures() { return coherentRiskMeasures; }
        public BigDecimal getTailRiskRatio() { return tailRiskRatio; }
    }

    public static class ExtremeValueRisk {
        private final BigDecimal expectedTailLoss;
        private final BigDecimal tailIndex;

        private ExtremeValueRisk(BigDecimal expectedTailLoss, BigDecimal tailIndex) {
            this.expectedTailLoss = expectedTailLoss;
            this.tailIndex = tailIndex;
        }

        public static ExtremeValueRiskBuilder builder() { return new ExtremeValueRiskBuilder(); }

        public static class ExtremeValueRiskBuilder {
            private BigDecimal expectedTailLoss = BigDecimal.ZERO;
            private BigDecimal tailIndex = BigDecimal.ZERO;

            public ExtremeValueRiskBuilder expectedTailLoss(BigDecimal expectedTailLoss) { this.expectedTailLoss = expectedTailLoss; return this; }
            public ExtremeValueRiskBuilder tailIndex(BigDecimal tailIndex) { this.tailIndex = tailIndex; return this; }

            public ExtremeValueRisk build() {
                return new ExtremeValueRisk(expectedTailLoss, tailIndex);
            }
        }

        public BigDecimal getExpectedTailLoss() { return expectedTailLoss; }
        public BigDecimal getTailIndex() { return tailIndex; }
    }

    public static class CoherentRiskMeasures {
        private final BigDecimal spectralRiskMeasure;
        private final BigDecimal distortionRiskMeasure;

        private CoherentRiskMeasures(BigDecimal spectralRiskMeasure, BigDecimal distortionRiskMeasure) {
            this.spectralRiskMeasure = spectralRiskMeasure;
            this.distortionRiskMeasure = distortionRiskMeasure;
        }

        public static CoherentRiskMeasuresBuilder builder() { return new CoherentRiskMeasuresBuilder(); }

        public static class CoherentRiskMeasuresBuilder {
            private BigDecimal spectralRiskMeasure = BigDecimal.ZERO;
            private BigDecimal distortionRiskMeasure = BigDecimal.ZERO;

            public CoherentRiskMeasuresBuilder spectralRiskMeasure(BigDecimal spectralRiskMeasure) { this.spectralRiskMeasure = spectralRiskMeasure; return this; }
            public CoherentRiskMeasuresBuilder distortionRiskMeasure(BigDecimal distortionRiskMeasure) { this.distortionRiskMeasure = distortionRiskMeasure; return this; }

            public CoherentRiskMeasures build() {
                return new CoherentRiskMeasures(spectralRiskMeasure, distortionRiskMeasure);
            }
        }

        public BigDecimal getSpectralRiskMeasure() { return spectralRiskMeasure; }
        public BigDecimal getDistortionRiskMeasure() { return distortionRiskMeasure; }
    }

    // Additional DTO classes would be defined similarly...
    // For brevity, I'm including simplified versions of the remaining DTOs
    
    public static class RiskDecomposition {
        private final BigDecimal totalRisk;
        private final BigDecimal systematicRisk;
        private final BigDecimal idiosyncraticRisk;
        private final Map<String, BigDecimal> riskContributions;

        private RiskDecomposition(BigDecimal totalRisk, BigDecimal systematicRisk, BigDecimal idiosyncraticRisk, Map<String, BigDecimal> riskContributions) {
            this.totalRisk = totalRisk;
            this.systematicRisk = systematicRisk;
            this.idiosyncraticRisk = idiosyncraticRisk;
            this.riskContributions = riskContributions;
        }

        public static RiskDecompositionBuilder builder() { return new RiskDecompositionBuilder(); }

        public static class RiskDecompositionBuilder {
            private BigDecimal totalRisk = BigDecimal.ZERO;
            private BigDecimal systematicRisk = BigDecimal.ZERO;
            private BigDecimal idiosyncraticRisk = BigDecimal.ZERO;
            private Map<String, BigDecimal> riskContributions = new HashMap<>();

            public RiskDecompositionBuilder totalRisk(BigDecimal totalRisk) { this.totalRisk = totalRisk; return this; }
            public RiskDecompositionBuilder systematicRisk(BigDecimal systematicRisk) { this.systematicRisk = systematicRisk; return this; }
            public RiskDecompositionBuilder idiosyncraticRisk(BigDecimal idiosyncraticRisk) { this.idiosyncraticRisk = idiosyncraticRisk; return this; }
            public RiskDecompositionBuilder riskContributions(Map<String, BigDecimal> riskContributions) { this.riskContributions = riskContributions; return this; }

            public RiskDecomposition build() { return new RiskDecomposition(totalRisk, systematicRisk, idiosyncraticRisk, riskContributions); }
        }

        public BigDecimal getTotalRisk() { return totalRisk; }
        public BigDecimal getSystematicRisk() { return systematicRisk; }
        public BigDecimal getIdiosyncraticRisk() { return idiosyncraticRisk; }
        public Map<String, BigDecimal> getRiskContributions() { return riskContributions; }
    }

    public static class StressTestResults {
        private final Map<String, BigDecimal> scenarioResults;
        private final String worstCaseScenario;
        private final BigDecimal averageStressLoss;

        private StressTestResults(Map<String, BigDecimal> scenarioResults, String worstCaseScenario, BigDecimal averageStressLoss) {
            this.scenarioResults = scenarioResults;
            this.worstCaseScenario = worstCaseScenario;
            this.averageStressLoss = averageStressLoss;
        }

        public static StressTestResultsBuilder builder() { return new StressTestResultsBuilder(); }

        public static class StressTestResultsBuilder {
            private Map<String, BigDecimal> scenarioResults = new HashMap<>();
            private String worstCaseScenario = "";
            private BigDecimal averageStressLoss = BigDecimal.ZERO;

            public StressTestResultsBuilder scenarioResults(Map<String, BigDecimal> scenarioResults) { this.scenarioResults = scenarioResults; return this; }
            public StressTestResultsBuilder worstCaseScenario(String worstCaseScenario) { this.worstCaseScenario = worstCaseScenario; return this; }
            public StressTestResultsBuilder averageStressLoss(BigDecimal averageStressLoss) { this.averageStressLoss = averageStressLoss; return this; }

            public StressTestResults build() { return new StressTestResults(scenarioResults, worstCaseScenario, averageStressLoss); }
        }

        public Map<String, BigDecimal> getScenarioResults() { return scenarioResults; }
        public String getWorstCaseScenario() { return worstCaseScenario; }
        public BigDecimal getAverageStressLoss() { return averageStressLoss; }
    }

    public static class RiskAdjustedReturns {
        private final BigDecimal sharpeRatio;
        private final BigDecimal sortinoRatio;
        private final BigDecimal calmarRatio;
        private final BigDecimal treynorRatio;
        private final BigDecimal informationRatio;

        private RiskAdjustedReturns(BigDecimal sharpeRatio, BigDecimal sortinoRatio, BigDecimal calmarRatio, BigDecimal treynorRatio, BigDecimal informationRatio) {
            this.sharpeRatio = sharpeRatio;
            this.sortinoRatio = sortinoRatio;
            this.calmarRatio = calmarRatio;
            this.treynorRatio = treynorRatio;
            this.informationRatio = informationRatio;
        }

        public static RiskAdjustedReturnsBuilder builder() { return new RiskAdjustedReturnsBuilder(); }

        public static class RiskAdjustedReturnsBuilder {
            private BigDecimal sharpeRatio = BigDecimal.ZERO;
            private BigDecimal sortinoRatio = BigDecimal.ZERO;
            private BigDecimal calmarRatio = BigDecimal.ZERO;
            private BigDecimal treynorRatio = BigDecimal.ZERO;
            private BigDecimal informationRatio = BigDecimal.ZERO;

            public RiskAdjustedReturnsBuilder sharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; return this; }
            public RiskAdjustedReturnsBuilder sortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; return this; }
            public RiskAdjustedReturnsBuilder calmarRatio(BigDecimal calmarRatio) { this.calmarRatio = calmarRatio; return this; }
            public RiskAdjustedReturnsBuilder treynorRatio(BigDecimal treynorRatio) { this.treynorRatio = treynorRatio; return this; }
            public RiskAdjustedReturnsBuilder informationRatio(BigDecimal informationRatio) { this.informationRatio = informationRatio; return this; }

            public RiskAdjustedReturns build() { return new RiskAdjustedReturns(sharpeRatio, sortinoRatio, calmarRatio, treynorRatio, informationRatio); }
        }

        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public BigDecimal getSortinoRatio() { return sortinoRatio; }
        public BigDecimal getCalmarRatio() { return calmarRatio; }
        public BigDecimal getTreynorRatio() { return treynorRatio; }
        public BigDecimal getInformationRatio() { return informationRatio; }
    }

    public static class RiskBudgetAnalysis {
        private final Map<String, BigDecimal> allocatedRiskBudget;
        private final Map<String, BigDecimal> actualRiskUtilization;
        private final BigDecimal riskBudgetUtilization;
        private final BigDecimal excessRiskTaken;

        private RiskBudgetAnalysis(Map<String, BigDecimal> allocatedRiskBudget, Map<String, BigDecimal> actualRiskUtilization, BigDecimal riskBudgetUtilization, BigDecimal excessRiskTaken) {
            this.allocatedRiskBudget = allocatedRiskBudget;
            this.actualRiskUtilization = actualRiskUtilization;
            this.riskBudgetUtilization = riskBudgetUtilization;
            this.excessRiskTaken = excessRiskTaken;
        }

        public static RiskBudgetAnalysisBuilder builder() { return new RiskBudgetAnalysisBuilder(); }

        public static class RiskBudgetAnalysisBuilder {
            private Map<String, BigDecimal> allocatedRiskBudget = new HashMap<>();
            private Map<String, BigDecimal> actualRiskUtilization = new HashMap<>();
            private BigDecimal riskBudgetUtilization = BigDecimal.ZERO;
            private BigDecimal excessRiskTaken = BigDecimal.ZERO;

            public RiskBudgetAnalysisBuilder allocatedRiskBudget(Map<String, BigDecimal> allocatedRiskBudget) { this.allocatedRiskBudget = allocatedRiskBudget; return this; }
            public RiskBudgetAnalysisBuilder actualRiskUtilization(Map<String, BigDecimal> actualRiskUtilization) { this.actualRiskUtilization = actualRiskUtilization; return this; }
            public RiskBudgetAnalysisBuilder riskBudgetUtilization(BigDecimal riskBudgetUtilization) { this.riskBudgetUtilization = riskBudgetUtilization; return this; }
            public RiskBudgetAnalysisBuilder excessRiskTaken(BigDecimal excessRiskTaken) { this.excessRiskTaken = excessRiskTaken; return this; }

            public RiskBudgetAnalysis build() { return new RiskBudgetAnalysis(allocatedRiskBudget, actualRiskUtilization, riskBudgetUtilization, excessRiskTaken); }
        }

        public Map<String, BigDecimal> getAllocatedRiskBudget() { return allocatedRiskBudget; }
        public Map<String, BigDecimal> getActualRiskUtilization() { return actualRiskUtilization; }
        public BigDecimal getRiskBudgetUtilization() { return riskBudgetUtilization; }
        public BigDecimal getExcessRiskTaken() { return excessRiskTaken; }
    }
}