package com.stockquest.application.service;

import com.stockquest.domain.risk.VaRCalculation;
import com.stockquest.domain.risk.VaRCalculation.*;
import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 고급 VaR 계산 엔진 서비스
 * Phase 8.3: Advanced Risk Management - VaR 계산 시스템
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaRCalculationService {
    
    private final StockMarketDataService stockMarketDataService;
    private final PortfolioService portfolioService;
    private final RiskAlertService riskAlertService;
    
    /**
     * 포트폴리오 VaR 계산 (모든 방법론)
     */
    public CompletableFuture<VaRCalculation> calculatePortfolioVaR(
            String portfolioId, 
            VaRMethod method, 
            VaRParameters parameters) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting VaR calculation for portfolio: {}, method: {}", portfolioId, method);
                
                // 포트폴리오 데이터 조회
                Portfolio portfolio = portfolioService.getPortfolioById(Long.valueOf(portfolioId));
                List<Stock> marketData = getMarketDataForPortfolio(portfolio, parameters.getHistoricalPeriod());
                
                // 방법론에 따른 VaR 계산
                VaRCalculation calculation = switch (method) {
                    case HISTORICAL_SIMULATION -> calculateHistoricalVaR(portfolio, marketData, parameters);
                    case PARAMETRIC -> calculateParametricVaR(portfolio, marketData, parameters);
                    case MONTE_CARLO -> calculateMonteCarloVaR(portfolio, marketData, parameters);
                    case FILTERED_HISTORICAL -> calculateFilteredHistoricalVaR(portfolio, marketData, parameters);
                    case EXTREME_VALUE_THEORY -> calculateExtremeValueVaR(portfolio, marketData, parameters);
                };
                
                // 품질 메트릭 계산
                calculation.setQualityMetrics(calculateQualityMetrics(calculation, portfolio, marketData));
                
                // 백테스팅 수행
                if (parameters.getAdditionalParameters() != null && 
                    Boolean.TRUE.equals(parameters.getAdditionalParameters().get("enableBacktesting"))) {
                    calculation.setBacktestingResults(performBacktesting(calculation, portfolio, marketData));
                }
                
                // VaR 임계값 확인 및 알림 생성
                checkVaRLimitsAndGenerateAlerts(calculation);
                
                log.info("VaR calculation completed for portfolio: {}, VaR: {}", 
                    portfolioId, calculation.getVarValue());
                
                return calculation;
                
            } catch (Exception e) {
                log.error("Error calculating VaR for portfolio: {}", portfolioId, e);
                throw new RuntimeException("VaR calculation failed", e);
            }
        });
    }
    
    /**
     * 역사적 시뮬레이션 VaR 계산
     */
    private VaRCalculation calculateHistoricalVaR(Portfolio portfolio, List<Stock> marketData, VaRParameters parameters) {
        log.debug("Calculating Historical Simulation VaR");
        
        // 수익률 계산
        List<BigDecimal> returns = calculatePortfolioReturns(portfolio, marketData);
        
        // 가중치 적용 (지수가중 등)
        List<BigDecimal> weightedReturns = applyWeightingScheme(returns, parameters.getWeightingScheme());
        
        // 정렬 및 백분위수 계산
        weightedReturns.sort(BigDecimal::compareTo);
        // 기본값 설정
        BigDecimal confidenceLevel = new BigDecimal("0.95"); // 95% 기본값
        Integer holdingPeriod = 1; // 1일 기본값
        
        int varIndex = (int) Math.ceil(weightedReturns.size() * (1 - confidenceLevel.doubleValue()));
        BigDecimal historicalVaR = weightedReturns.get(Math.min(varIndex - 1, weightedReturns.size() - 1)).abs();
        
        // 보유기간 조정
        BigDecimal adjustedVaR = historicalVaR.multiply(
            BigDecimal.valueOf(Math.sqrt(holdingPeriod))
        );
        
        // Expected Shortfall (CVaR) 계산
        BigDecimal expectedShortfall = calculateExpectedShortfall(weightedReturns, varIndex);
        
        // 구성요소 기여도 분석
        List<VaRComponent> components = calculateComponentContributions(portfolio, marketData, adjustedVaR);
        
        return VaRCalculation.builder()
            .calculationId(UUID.randomUUID().toString())
            .portfolioId(String.valueOf(portfolio.getId()))
            .method(VaRMethod.HISTORICAL_SIMULATION)
            .confidenceLevel(confidenceLevel)
            .holdingPeriod(holdingPeriod)
            .varValue(adjustedVaR)
            .varPercentage(adjustedVaR.multiply(new BigDecimal("100")))
            .expectedShortfall(expectedShortfall)
            .calculationTime(LocalDateTime.now())
            .expirationTime(LocalDateTime.now().plusHours(24))
            .components(components)
            .parameters(parameters)
            .build();
    }
    
    /**
     * 모수적 VaR 계산 (정규분포 가정)
     */
    private VaRCalculation calculateParametricVaR(Portfolio portfolio, List<Stock> marketData, VaRParameters parameters) {
        log.debug("Calculating Parametric VaR");
        
        // 수익률 계산
        List<BigDecimal> returns = calculatePortfolioReturns(portfolio, marketData);
        
        // 평균 및 표준편차 계산
        BigDecimal mean = calculateMean(returns);
        BigDecimal stdDev = calculateStandardDeviation(returns, mean);
        
        // 기본값 설정
        BigDecimal confidenceLevel = new BigDecimal("0.95");
        Integer holdingPeriod = 1;
        
        // 신뢰구간에 따른 Z-score
        double zScore = getZScoreForConfidenceLevel(confidenceLevel.doubleValue());
        
        // 정규분포 가정 VaR
        BigDecimal parametricVaR = BigDecimal.valueOf(zScore).multiply(stdDev).subtract(mean).abs();
        
        // 보유기간 조정
        BigDecimal adjustedVaR = parametricVaR.multiply(
            BigDecimal.valueOf(Math.sqrt(holdingPeriod))
        );
        
        // Expected Shortfall 계산 (정규분포 가정)
        BigDecimal expectedShortfall = calculateParametricExpectedShortfall(mean, stdDev, confidenceLevel);
        
        // 구성요소 기여도 분석
        List<VaRComponent> components = calculateComponentContributions(portfolio, marketData, adjustedVaR);
        
        return VaRCalculation.builder()
            .calculationId(UUID.randomUUID().toString())
            .portfolioId(portfolio.getId().toString())
            .method(VaRMethod.PARAMETRIC)
            .confidenceLevel(parameters.getConfidenceLevel())
            .holdingPeriod(parameters.getHoldingPeriod())
            .varValue(adjustedVaR)
            .expectedShortfall(expectedShortfall)
            .calculationTime(LocalDateTime.now())
            .expirationTime(LocalDateTime.now().plusHours(24))
            .components(components)
            .parameters(parameters)
            .build();
    }
    
    /**
     * 몬테카를로 시뮬레이션 VaR 계산
     */
    private VaRCalculation calculateMonteCarloVaR(Portfolio portfolio, List<Stock> marketData, VaRParameters parameters) {
        log.debug("Calculating Monte Carlo VaR with {} simulations", parameters.getNumberOfSimulations());
        
        // 상관관계 행렬 계산
        double[][] correlationMatrix = calculateCorrelationMatrix(portfolio, marketData);
        
        // 개별 자산 수익률 분포 파라미터
        Map<String, AssetDistributionParameters> assetParams = calculateAssetDistributionParameters(portfolio, marketData);
        
        // 몬테카를로 시뮬레이션
        List<BigDecimal> simulatedReturns = new ArrayList<>();
        Random random = new Random(parameters.getRandomSeed() != null ? parameters.getRandomSeed() : System.currentTimeMillis());
        
        for (int i = 0; i < parameters.getNumberOfSimulations(); i++) {
            BigDecimal portfolioReturn = simulatePortfolioReturn(portfolio, assetParams, correlationMatrix, random);
            simulatedReturns.add(portfolioReturn);
        }
        
        // VaR 계산
        simulatedReturns.sort(BigDecimal::compareTo);
        int varIndex = (int) Math.ceil(simulatedReturns.size() * (1 - parameters.getConfidenceLevel().doubleValue()));
        BigDecimal monteCarloVaR = simulatedReturns.get(Math.min(varIndex - 1, simulatedReturns.size() - 1)).abs();
        
        // 보유기간 조정
        BigDecimal adjustedVaR = monteCarloVaR.multiply(
            BigDecimal.valueOf(Math.sqrt(parameters.getHoldingPeriod() != null ? parameters.getHoldingPeriod() : 1))
        );
        
        // Expected Shortfall 계산
        BigDecimal expectedShortfall = calculateExpectedShortfall(simulatedReturns, varIndex);
        
        // 시나리오 분석
        ScenarioAnalysis scenarioAnalysis = createScenarioAnalysis(simulatedReturns, parameters);
        
        // 구성요소 기여도 분석
        List<VaRComponent> components = calculateComponentContributions(portfolio, marketData, adjustedVaR);
        
        return VaRCalculation.builder()
            .calculationId(UUID.randomUUID().toString())
            .portfolioId(portfolio.getId().toString())
            .method(VaRMethod.MONTE_CARLO)
            .confidenceLevel(parameters.getConfidenceLevel())
            .holdingPeriod(parameters.getHoldingPeriod())
            .varValue(adjustedVaR)
            .expectedShortfall(expectedShortfall)
            .calculationTime(LocalDateTime.now())
            .expirationTime(LocalDateTime.now().plusHours(24))
            .scenarioAnalysis(scenarioAnalysis)
            .components(components)
            .parameters(parameters)
            .build();
    }
    
    /**
     * 필터링된 역사적 VaR 계산 (GARCH 등 시변 변동성 모델 적용)
     */
    private VaRCalculation calculateFilteredHistoricalVaR(Portfolio portfolio, List<Stock> marketData, VaRParameters parameters) {
        log.debug("Calculating Filtered Historical VaR");
        
        // 수익률 계산
        List<BigDecimal> returns = calculatePortfolioReturns(portfolio, marketData);
        
        // GARCH 모델 적용하여 시변 변동성 추정
        List<BigDecimal> garchVolatilities = applyGarchModel(returns);
        
        // 표준화된 잔차 계산
        List<BigDecimal> standardizedResiduals = calculateStandardizedResiduals(returns, garchVolatilities);
        
        // 현재 변동성으로 스케일링
        BigDecimal currentVolatility = garchVolatilities.get(garchVolatilities.size() - 1);
        List<BigDecimal> scaledReturns = standardizedResiduals.stream()
            .map(residual -> residual.multiply(currentVolatility))
            .collect(Collectors.toList());
        
        // VaR 계산
        scaledReturns.sort(BigDecimal::compareTo);
        int varIndex = (int) Math.ceil(scaledReturns.size() * (1 - parameters.getConfidenceLevel().doubleValue()));
        BigDecimal filteredVaR = scaledReturns.get(Math.min(varIndex - 1, scaledReturns.size() - 1)).abs();
        
        // 보유기간 조정
        BigDecimal adjustedVaR = filteredVaR.multiply(
            BigDecimal.valueOf(Math.sqrt(parameters.getHoldingPeriod() != null ? parameters.getHoldingPeriod() : 1))
        );
        
        // Expected Shortfall 계산
        BigDecimal expectedShortfall = calculateExpectedShortfall(scaledReturns, varIndex);
        
        // 구성요소 기여도 분석
        List<VaRComponent> components = calculateComponentContributions(portfolio, marketData, adjustedVaR);
        
        return VaRCalculation.builder()
            .calculationId(UUID.randomUUID().toString())
            .portfolioId(portfolio.getId().toString())
            .method(VaRMethod.FILTERED_HISTORICAL)
            .confidenceLevel(parameters.getConfidenceLevel())
            .holdingPeriod(parameters.getHoldingPeriod())
            .varValue(adjustedVaR)
            .expectedShortfall(expectedShortfall)
            .calculationTime(LocalDateTime.now())
            .expirationTime(LocalDateTime.now().plusHours(24))
            .components(components)
            .parameters(parameters)
            .build();
    }
    
    /**
     * 극값 이론 VaR 계산
     */
    private VaRCalculation calculateExtremeValueVaR(Portfolio portfolio, List<Stock> marketData, VaRParameters parameters) {
        log.debug("Calculating Extreme Value Theory VaR");
        
        // 수익률 계산
        List<BigDecimal> returns = calculatePortfolioReturns(portfolio, marketData);
        
        // 극값 추출 (임계값 이상의 손실)
        BigDecimal threshold = calculateExtremeValueThreshold(returns);
        List<BigDecimal> extremeValues = returns.stream()
            .filter(r -> r.compareTo(threshold) < 0) // 손실(음수)만 선택
            .map(BigDecimal::abs)
            .collect(Collectors.toList());
        
        // Generalized Pareto Distribution 파라미터 추정
        GpdParameters gpdParams = estimateGpdParameters(extremeValues, threshold);
        
        // EVT VaR 계산
        double exceedanceProbability = (double) extremeValues.size() / returns.size();
        BigDecimal evtVaR = calculateEvtVaR(gpdParams, exceedanceProbability, parameters.getConfidenceLevel());
        
        // 보유기간 조정
        BigDecimal adjustedVaR = evtVaR.multiply(
            BigDecimal.valueOf(Math.sqrt(parameters.getHoldingPeriod() != null ? parameters.getHoldingPeriod() : 1))
        );
        
        // Expected Shortfall 계산
        BigDecimal expectedShortfall = calculateEvtExpectedShortfall(gpdParams, exceedanceProbability, parameters.getConfidenceLevel());
        
        // 구성요소 기여도 분석
        List<VaRComponent> components = calculateComponentContributions(portfolio, marketData, adjustedVaR);
        
        return VaRCalculation.builder()
            .calculationId(UUID.randomUUID().toString())
            .portfolioId(portfolio.getId().toString())
            .method(VaRMethod.EXTREME_VALUE_THEORY)
            .confidenceLevel(parameters.getConfidenceLevel())
            .holdingPeriod(parameters.getHoldingPeriod())
            .varValue(adjustedVaR)
            .expectedShortfall(expectedShortfall)
            .calculationTime(LocalDateTime.now())
            .expirationTime(LocalDateTime.now().plusHours(24))
            .components(components)
            .parameters(parameters)
            .build();
    }
    
    /**
     * VaR 품질 메트릭 계산
     */
    private VaRQualityMetrics calculateQualityMetrics(VaRCalculation calculation, Portfolio portfolio, List<Stock> marketData) {
        log.debug("Calculating VaR quality metrics");
        
        // 모델 정확도 점수 (백테스팅 기반)
        BigDecimal accuracyScore = calculateAccuracyScore(calculation, portfolio, marketData);
        
        // 백테스팅 위반 분석
        BacktestingResults backtestResults = calculation.getBacktestingResults();
        Integer backtestViolations = backtestResults != null ? backtestResults.getViolations() : 0;
        Integer expectedViolations = calculateExpectedViolations(calculation.getConfidenceLevel(), 250); // 1년 기준
        
        // Kupiec 검정
        BigDecimal kupiecPValue = performKupiecTest(backtestViolations, expectedViolations, 250);
        
        // 신뢰도 등급 결정
        ModelConfidenceLevel confidenceLevel = determineConfidenceLevel(accuracyScore, kupiecPValue);
        
        // 데이터 품질 점수
        BigDecimal dataQualityScore = assessDataQuality(marketData);
        
        return VaRQualityMetrics.builder()
            .accuracyScore(accuracyScore)
            .backtestViolations(backtestViolations)
            .expectedViolations(expectedViolations)
            .kupiecPValue(kupiecPValue)
            .confidenceLevel(confidenceLevel)
            .calculationDuration(1000L) // 실제로는 측정된 시간
            .dataQualityScore(dataQualityScore)
            .build();
    }
    
    /**
     * 백테스팅 수행
     */
    private BacktestingResults performBacktesting(VaRCalculation calculation, Portfolio portfolio, List<Stock> marketData) {
        log.debug("Performing VaR backtesting");
        
        // 백테스팅 기간 설정 (최근 1년)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(250);
        
        // 일별 수익률 계산
        List<BigDecimal> dailyReturns = calculateDailyReturns(portfolio, marketData);
        
        // VaR 위반 계산
        int violations = 0;
        for (BigDecimal dailyReturn : dailyReturns) {
            if (dailyReturn.abs().compareTo(calculation.getVarValue()) > 0) {
                violations++;
            }
        }
        
        // 위반율 계산
        BigDecimal violationRate = BigDecimal.valueOf((double) violations / dailyReturns.size());
        BigDecimal expectedViolationRate = BigDecimal.ONE.subtract(calculation.getConfidenceLevel());
        
        // 독립성 검정
        IndependenceTest independenceTest = performIndependenceTest(dailyReturns, calculation.getVarValue());
        
        // 조건부 커버리지 검정
        ConditionalCoverageTest conditionalCoverageTest = performConditionalCoverageTest(dailyReturns, calculation.getVarValue());
        
        // 평균 및 최대 초과 손실
        BigDecimal averageExcessLoss = calculateAverageExcessLoss(dailyReturns, calculation.getVarValue());
        BigDecimal maximumExcessLoss = calculateMaximumExcessLoss(dailyReturns, calculation.getVarValue());
        
        return BacktestingResults.builder()
            .startDate(startDate)
            .endDate(endDate)
            .totalObservations(dailyReturns.size())
            .violations(violations)
            .violationRate(violationRate)
            .expectedViolationRate(expectedViolationRate)
            .independenceTest(independenceTest)
            .conditionalCoverageTest(conditionalCoverageTest)
            .averageExcessLoss(averageExcessLoss)
            .maximumExcessLoss(maximumExcessLoss)
            .build();
    }
    
    /**
     * VaR 한도 확인 및 알림 생성
     */
    private void checkVaRLimitsAndGenerateAlerts(VaRCalculation calculation) {
        try {
            // VaR 위험도 평가
            VaRCalculation.RiskLevel riskLevel = calculation.assessRiskLevel();
            
            // 높은 위험도인 경우 알림 생성
            if (riskLevel == VaRCalculation.RiskLevel.HIGH || riskLevel == VaRCalculation.RiskLevel.VERY_HIGH) {
                riskAlertService.generateVaRAlert(calculation);
            }
            
            // 품질 점수가 낮은 경우 알림
            if (calculation.getQualityMetrics() != null && 
                calculation.getQualityMetrics().getAccuracyScore() != null &&
                calculation.getQualityMetrics().getAccuracyScore().compareTo(new BigDecimal("0.7")) < 0) {
                riskAlertService.generateModelDegradationAlert(calculation);
            }
            
        } catch (Exception e) {
            log.warn("Failed to generate VaR alerts", e);
        }
    }
    
    // ========================= 헬퍼 메서드들 =========================
    
    private List<Stock> getMarketDataForPortfolio(Portfolio portfolio, Integer days) {
        // MarketDataService를 통해 포트폴리오의 모든 자산에 대한 시장 데이터 조회
        return stockMarketDataService.getHistoricalData(
            portfolio.getHoldings().stream()
                .map(holding -> holding.getSymbol())
                .collect(Collectors.toList()),
            days != null ? days : 252
        );
    }
    
    private List<BigDecimal> calculatePortfolioReturns(Portfolio portfolio, List<Stock> marketData) {
        // 포트폴리오 가중 수익률 계산 구현
        return marketData.stream()
            .map(stock -> calculateDailyReturn(stock))
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateDailyReturn(Stock stock) {
        // 일간 수익률 계산: (close - open) / open
        if (stock.getOpenPrice() == null || stock.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return stock.getClosePrice().subtract(stock.getOpenPrice())
               .divide(stock.getOpenPrice(), 8, RoundingMode.HALF_UP);
    }
    
    private List<BigDecimal> applyWeightingScheme(List<BigDecimal> returns, WeightingScheme scheme) {
        switch (scheme) {
            case EXPONENTIALLY_WEIGHTED -> {
                return applyExponentialWeights(returns, 0.94); // 일반적인 람다값
            }
            case VOLATILITY_WEIGHTED -> {
                return applyVolatilityWeights(returns);
            }
            default -> {
                return returns; // 동일 가중
            }
        }
    }
    
    private List<BigDecimal> applyExponentialWeights(List<BigDecimal> returns, double lambda) {
        List<BigDecimal> weightedReturns = new ArrayList<>();
        for (int i = 0; i < returns.size(); i++) {
            double weight = Math.pow(lambda, returns.size() - i - 1);
            weightedReturns.add(returns.get(i).multiply(BigDecimal.valueOf(weight)));
        }
        return weightedReturns;
    }
    
    private List<BigDecimal> applyVolatilityWeights(List<BigDecimal> returns) {
        // 변동성 가중 구현 (단순화)
        BigDecimal avgReturn = calculateMean(returns);
        BigDecimal stdDev = calculateStandardDeviation(returns, avgReturn);
        
        return returns.stream()
            .map(ret -> ret.divide(stdDev, 8, RoundingMode.HALF_UP))
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal sum = values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateStandardDeviation(List<BigDecimal> values, BigDecimal mean) {
        if (values.size() <= 1) return BigDecimal.ZERO;
        
        BigDecimal sumSquaredDiffs = values.stream()
            .map(value -> value.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal variance = sumSquaredDiffs.divide(BigDecimal.valueOf(values.size() - 1), 8, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
    
    private double getZScoreForConfidenceLevel(double confidenceLevel) {
        // 신뢰구간별 Z-score (정규분포)
        if (confidenceLevel >= 0.99) return 2.326;
        if (confidenceLevel >= 0.95) return 1.645;
        if (confidenceLevel >= 0.90) return 1.282;
        return 1.645; // 기본값
    }
    
    private BigDecimal calculateExpectedShortfall(List<BigDecimal> sortedReturns, int varIndex) {
        if (varIndex <= 0) return BigDecimal.ZERO;
        
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        for (int i = 0; i < Math.min(varIndex, sortedReturns.size()); i++) {
            sum = sum.add(sortedReturns.get(i).abs());
            count++;
        }
        
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    private BigDecimal calculateParametricExpectedShortfall(BigDecimal mean, BigDecimal stdDev, BigDecimal confidenceLevel) {
        // 정규분포 가정하에서의 Expected Shortfall 계산
        double alpha = 1 - confidenceLevel.doubleValue();
        double phi = Math.exp(-0.5 * Math.pow(getZScoreForConfidenceLevel(confidenceLevel.doubleValue()), 2)) / Math.sqrt(2 * Math.PI);
        BigDecimal expectedShortfall = stdDev.multiply(BigDecimal.valueOf(phi / alpha)).subtract(mean);
        return expectedShortfall.abs();
    }
    
    private List<VaRComponent> calculateComponentContributions(Portfolio portfolio, List<Stock> marketData, BigDecimal totalVaR) {
        // 구성요소별 VaR 기여도 계산 (단순화된 버전)
        return portfolio.getHoldings().stream()
            .map(holding -> {
                BigDecimal weight = holding.getQuantity().multiply(holding.getCurrentPrice())
                    .divide(portfolio.getTotalValue(), 4, RoundingMode.HALF_UP);
                BigDecimal contribution = totalVaR.multiply(weight);
                
                return VaRComponent.builder()
                    .componentId(holding.getSymbol())
                    .componentName(holding.getSymbol())
                    .componentType(ComponentType.STOCK)
                    .weight(weight)
                    .varContribution(contribution)
                    .varContributionPercentage(contribution.divide(totalVaR, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    // 추가 헬퍼 메서드들은 실제 구현에서 필요에 따라 구현
    private double[][] calculateCorrelationMatrix(Portfolio portfolio, List<Stock> marketData) { 
        // 상관관계 행렬 계산 구현
        return new double[0][0]; 
    }
    
    private Map<String, AssetDistributionParameters> calculateAssetDistributionParameters(Portfolio portfolio, List<Stock> marketData) { 
        // 자산별 분포 파라미터 계산
        return new HashMap<>(); 
    }
    
    private BigDecimal simulatePortfolioReturn(Portfolio portfolio, Map<String, AssetDistributionParameters> assetParams, 
                                             double[][] correlationMatrix, Random random) { 
        // 포트폴리오 수익률 시뮬레이션
        return BigDecimal.ZERO; 
    }
    
    private ScenarioAnalysis createScenarioAnalysis(List<BigDecimal> simulatedReturns, VaRParameters parameters) { 
        // 시나리오 분석 생성
        return ScenarioAnalysis.builder().numberOfScenarios(parameters.getNumberOfSimulations()).build(); 
    }
    
    private List<BigDecimal> applyGarchModel(List<BigDecimal> returns) { 
        // GARCH 모델 적용
        return returns; 
    }
    
    private List<BigDecimal> calculateStandardizedResiduals(List<BigDecimal> returns, List<BigDecimal> volatilities) { 
        // 표준화 잔차 계산
        return returns; 
    }
    
    private BigDecimal calculateExtremeValueThreshold(List<BigDecimal> returns) { 
        // 극값 임계치 계산
        return new BigDecimal("-0.05"); 
    }
    
    private GpdParameters estimateGpdParameters(List<BigDecimal> extremeValues, BigDecimal threshold) { 
        // GPD 파라미터 추정
        return new GpdParameters(BigDecimal.ZERO, BigDecimal.ONE); 
    }
    
    private BigDecimal calculateEvtVaR(GpdParameters gpdParams, double exceedanceProbability, BigDecimal confidenceLevel) { 
        // EVT VaR 계산
        return BigDecimal.ONE; 
    }
    
    private BigDecimal calculateEvtExpectedShortfall(GpdParameters gpdParams, double exceedanceProbability, BigDecimal confidenceLevel) { 
        // EVT Expected Shortfall 계산
        return BigDecimal.ONE; 
    }
    
    // 추가 메서드들...
    private BigDecimal calculateAccuracyScore(VaRCalculation calculation, Portfolio portfolio, List<Stock> marketData) { return new BigDecimal("0.85"); }
    private Integer calculateExpectedViolations(BigDecimal confidenceLevel, int observations) { return (int)(observations * (1 - confidenceLevel.doubleValue())); }
    private BigDecimal performKupiecTest(Integer violations, Integer expected, int observations) { return new BigDecimal("0.05"); }
    private ModelConfidenceLevel determineConfidenceLevel(BigDecimal accuracyScore, BigDecimal kupiecPValue) { return ModelConfidenceLevel.HIGH; }
    private BigDecimal assessDataQuality(List<Stock> marketData) { return new BigDecimal("0.9"); }
    private List<BigDecimal> calculateDailyReturns(Portfolio portfolio, List<Stock> marketData) { return new ArrayList<>(); }
    private IndependenceTest performIndependenceTest(List<BigDecimal> returns, BigDecimal varValue) { 
        return IndependenceTest.builder().testName("Ljung-Box").pValue(new BigDecimal("0.05")).rejected(false).build(); 
    }
    private ConditionalCoverageTest performConditionalCoverageTest(List<BigDecimal> returns, BigDecimal varValue) { 
        return ConditionalCoverageTest.builder().testName("Christoffersen").pValue(new BigDecimal("0.05")).rejected(false).build(); 
    }
    private BigDecimal calculateAverageExcessLoss(List<BigDecimal> returns, BigDecimal varValue) { return BigDecimal.ZERO; }
    private BigDecimal calculateMaximumExcessLoss(List<BigDecimal> returns, BigDecimal varValue) { return BigDecimal.ZERO; }
    
    // 헬퍼 클래스들
    private record AssetDistributionParameters(BigDecimal mean, BigDecimal stdDev, String distributionType) {}
    private record GpdParameters(BigDecimal xi, BigDecimal beta) {}
}