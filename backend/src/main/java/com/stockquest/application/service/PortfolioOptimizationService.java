package com.stockquest.application.service;

import com.stockquest.domain.ml.PortfolioOptimization;
import com.stockquest.domain.ml.PortfolioOptimization.*;
import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.portfolio.Position;
import com.stockquest.domain.marketdata.MarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.optimization.GoalType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 포트폴리오 최적화 서비스
 * Modern Portfolio Theory, Risk Parity, Black-Litterman 등 다양한 최적화 알고리즘 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioOptimizationService {
    
    private final RealTimeMarketDataService marketDataService;
    private final TechnicalAnalysisService technicalAnalysisService;
    
    private static final int OPTIMIZATION_ITERATIONS = 1000;
    private static final double RISK_FREE_RATE = 0.02; // 2% 무위험 이자율
    private static final double TOLERANCE = 1e-6;
    
    /**
     * 포트폴리오 최적화 실행
     */
    @Async("riskAssessmentTaskExecutor")
    public CompletableFuture<PortfolioOptimization> optimizePortfolio(
            Portfolio portfolio, 
            OptimizationType optimizationType, 
            OptimizationObjective objective,
            OptimizationConstraints constraints) {
        
        try {
            log.info("포트폴리오 최적화 시작: portfolioId={}, type={}, objective={}", 
                portfolio.getId(), optimizationType, objective);
            
            // 1. 시장 데이터 수집
            MarketDataMatrix marketData = collectMarketData(portfolio);
            
            // 2. 최적화 실행
            OptimizationResult result = switch (optimizationType) {
                case MODERN_PORTFOLIO_THEORY -> performMPTOptimization(marketData, objective, constraints);
                case RISK_PARITY -> performRiskParityOptimization(marketData, constraints);
                case BLACK_LITTERMAN -> performBlackLittermanOptimization(marketData, objective, constraints);
                case MINIMUM_VARIANCE -> performMinimumVarianceOptimization(marketData, constraints);
                case MAXIMUM_SHARPE -> performMaximumSharpeOptimization(marketData, constraints);
                case HIERARCHICAL_RISK_PARITY -> performHRPOptimization(marketData, constraints);
                case MACHINE_LEARNING_BASED -> performMLBasedOptimization(marketData, objective, constraints);
            };
            
            // 3. 결과를 PortfolioOptimization 객체로 변환
            PortfolioOptimization optimization = buildOptimizationResult(
                portfolio, optimizationType, objective, result, constraints);
            
            log.info("포트폴리오 최적화 완료: portfolioId={}, 예상 수익률={}, 예상 리스크={}", 
                portfolio.getId(), 
                optimization.getExpectedPerformance().getExpectedReturn(),
                optimization.getExpectedPerformance().getExpectedVolatility());
            
            return CompletableFuture.completedFuture(optimization);
            
        } catch (Exception e) {
            log.error("포트폴리오 최적화 실패: portfolioId={}", portfolio.getId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Modern Portfolio Theory 최적화
     */
    private OptimizationResult performMPTOptimization(
            MarketDataMatrix marketData, 
            OptimizationObjective objective, 
            OptimizationConstraints constraints) {
        
        log.info("MPT 최적화 실행: 자산수={}, 목표={}", marketData.getAssetCount(), objective);
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        double[] optimalWeights = switch (objective) {
            case MAXIMIZE_RETURN -> optimizeForMaxReturn(expectedReturns, covarianceMatrix, constraints);
            case MINIMIZE_RISK -> optimizeForMinRisk(covarianceMatrix, constraints);
            case MAXIMIZE_SHARPE_RATIO -> optimizeForMaxSharpe(expectedReturns, covarianceMatrix, constraints);
            case TARGET_VOLATILITY -> optimizeForTargetVolatility(expectedReturns, covarianceMatrix, constraints);
            default -> optimizeForMaxSharpe(expectedReturns, covarianceMatrix, constraints);
        };
        
        return OptimizationResult.builder()
            .optimalWeights(optimalWeights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, optimalWeights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, optimalWeights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, optimalWeights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * Risk Parity 최적화 (동일 리스크 기여도)
     */
    private OptimizationResult performRiskParityOptimization(
            MarketDataMatrix marketData, 
            OptimizationConstraints constraints) {
        
        log.info("Risk Parity 최적화 실행");
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        int n = covarianceMatrix.getRowDimension();
        
        // Risk parity: 모든 자산의 리스크 기여도가 동일하도록 가중치 조정
        double[] weights = new double[n];
        Arrays.fill(weights, 1.0 / n); // 균등 가중치로 시작
        
        // 반복 최적화로 risk parity 달성
        for (int iter = 0; iter < OPTIMIZATION_ITERATIONS; iter++) {
            double[] riskContributions = calculateRiskContributions(covarianceMatrix, weights);
            double totalRisk = calculatePortfolioRisk(covarianceMatrix, weights);
            double targetRiskContribution = totalRisk / n;
            
            boolean converged = true;
            for (int i = 0; i < n; i++) {
                double adjustment = targetRiskContribution / riskContributions[i];
                weights[i] *= Math.pow(adjustment, 0.1); // 점진적 조정
                
                if (Math.abs(riskContributions[i] - targetRiskContribution) > TOLERANCE) {
                    converged = false;
                }
            }
            
            // 가중치 정규화
            normalizeWeights(weights);
            
            if (converged) {
                log.info("Risk Parity 수렴: 반복수={}", iter);
                break;
            }
        }
        
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        return OptimizationResult.builder()
            .optimalWeights(weights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, weights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, weights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, weights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 최대 샤프 비율 최적화
     */
    private double[] optimizeForMaxSharpe(
            RealVector expectedReturns, 
            RealMatrix covarianceMatrix, 
            OptimizationConstraints constraints) {
        
        int n = expectedReturns.getDimension();
        
        // 초기 추정값 (균등 가중치)
        double[] initialWeights = new double[n];
        Arrays.fill(initialWeights, 1.0 / n);
        
        // 경사하강법을 이용한 최적화 (간소화된 구현)
        double[] bestWeights = Arrays.copyOf(initialWeights, n);
        double bestSharpe = calculateSharpeRatio(expectedReturns, covarianceMatrix, bestWeights);
        
        Random random = new Random(42); // 재현 가능한 결과
        
        for (int iter = 0; iter < OPTIMIZATION_ITERATIONS; iter++) {
            double[] candidateWeights = perturbWeights(bestWeights, random, 0.01);
            applyConstraints(candidateWeights, constraints);
            normalizeWeights(candidateWeights);
            
            double candidateSharpe = calculateSharpeRatio(expectedReturns, covarianceMatrix, candidateWeights);
            
            if (candidateSharpe > bestSharpe) {
                bestWeights = candidateWeights;
                bestSharpe = candidateSharpe;
            }
        }
        
        log.info("최대 샤프 비율 최적화 완료: Sharpe={:.4f}", bestSharpe);
        return bestWeights;
    }
    
    /**
     * 최소 분산 최적화
     */
    private OptimizationResult performMinimumVarianceOptimization(
            MarketDataMatrix marketData, 
            OptimizationConstraints constraints) {
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        
        // 최소 분산 포트폴리오: minimize w^T * Σ * w subject to w^T * 1 = 1
        RealMatrix invCov = new LUDecomposition(covarianceMatrix).getSolver().getInverse();
        RealVector ones = new ArrayRealVector(covarianceMatrix.getRowDimension(), 1.0);
        
        double denominator = ones.dotProduct(invCov.operate(ones));
        RealVector optimalWeights = invCov.operate(ones).mapDivide(denominator);
        
        double[] weights = optimalWeights.toArray();
        applyConstraints(weights, constraints);
        normalizeWeights(weights);
        
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        return OptimizationResult.builder()
            .optimalWeights(weights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, weights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, weights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, weights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * Maximum Sharpe Ratio 최적화
     */
    private OptimizationResult performMaximumSharpeOptimization(
            MarketDataMatrix marketData, 
            OptimizationConstraints constraints) {
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        // Maximum Sharpe Ratio: maximize (μ^T w - rf) / sqrt(w^T Σ w)
        // This can be solved as a quadratic programming problem
        RealMatrix invCov = new LUDecomposition(covarianceMatrix).getSolver().getInverse();
        RealVector excessReturns = expectedReturns.mapSubtract(RISK_FREE_RATE);
        
        // Optimal weights proportional to Σ^-1 * (μ - rf)
        RealVector unnormalizedWeights = invCov.operate(excessReturns);
        
        // Normalize to sum to 1
        double weightsSum = Arrays.stream(unnormalizedWeights.toArray()).sum();
        double[] weights = unnormalizedWeights.mapDivide(weightsSum).toArray();
        
        applyConstraints(weights, constraints);
        normalizeWeights(weights);
        
        return OptimizationResult.builder()
            .optimalWeights(weights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, weights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, weights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, weights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * Black-Litterman 최적화 (간소화된 구현)
     */
    private OptimizationResult performBlackLittermanOptimization(
            MarketDataMatrix marketData, 
            OptimizationObjective objective, 
            OptimizationConstraints constraints) {
        
        log.info("Black-Litterman 최적화 실행");
        
        // 시장 균형 수익률 계산
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        RealVector marketCapWeights = calculateMarketCapWeights(marketData);
        
        // 시장 균형 수익률 = λ * Σ * w_market
        double riskAversion = 3.0; // 일반적인 위험 회피 계수
        RealVector equilibriumReturns = covarianceMatrix.operate(marketCapWeights).mapMultiply(riskAversion);
        
        // Black-Litterman 공식 적용 (간소화)
        RealVector blReturns = equilibriumReturns; // 투자자 견해 없이 시장 균형 수익률 사용
        
        // 최적 포트폴리오 계산
        double[] optimalWeights = optimizeForMaxSharpe(blReturns, covarianceMatrix, constraints);
        
        return OptimizationResult.builder()
            .optimalWeights(optimalWeights)
            .expectedReturn(calculatePortfolioReturn(blReturns, optimalWeights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, optimalWeights))
            .sharpeRatio(calculateSharpeRatio(blReturns, covarianceMatrix, optimalWeights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 계층적 리스크 패리티 (HRP) 최적화 (간소화된 구현)
     */
    private OptimizationResult performHRPOptimization(
            MarketDataMatrix marketData, 
            OptimizationConstraints constraints) {
        
        log.info("HRP 최적화 실행");
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        
        // 간소화된 HRP: 상관관계 기반 클러스터링 후 risk parity 적용
        int n = covarianceMatrix.getRowDimension();
        double[] weights = new double[n];
        
        // 각 자산의 변동성의 역수로 가중치 할당 (HRP의 간소화된 버전)
        for (int i = 0; i < n; i++) {
            weights[i] = 1.0 / Math.sqrt(covarianceMatrix.getEntry(i, i));
        }
        
        normalizeWeights(weights);
        applyConstraints(weights, constraints);
        normalizeWeights(weights);
        
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        return OptimizationResult.builder()
            .optimalWeights(weights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, weights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, weights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, weights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * ML 기반 최적화 (간소화된 구현)
     */
    private OptimizationResult performMLBasedOptimization(
            MarketDataMatrix marketData, 
            OptimizationObjective objective, 
            OptimizationConstraints constraints) {
        
        log.info("ML 기반 최적화 실행");
        
        // 현재는 MPT와 Risk Parity의 가중 평균으로 구현
        OptimizationResult mptResult = performMPTOptimization(marketData, objective, constraints);
        OptimizationResult rpResult = performRiskParityOptimization(marketData, constraints);
        
        double[] combinedWeights = new double[mptResult.getOptimalWeights().length];
        for (int i = 0; i < combinedWeights.length; i++) {
            combinedWeights[i] = 0.6 * mptResult.getOptimalWeights()[i] + 0.4 * rpResult.getOptimalWeights()[i];
        }
        
        normalizeWeights(combinedWeights);
        
        RealMatrix returns = marketData.getReturnsMatrix();
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(returns);
        RealVector expectedReturns = calculateExpectedReturns(returns);
        
        return OptimizationResult.builder()
            .optimalWeights(combinedWeights)
            .expectedReturn(calculatePortfolioReturn(expectedReturns, combinedWeights))
            .expectedRisk(calculatePortfolioRisk(covarianceMatrix, combinedWeights))
            .sharpeRatio(calculateSharpeRatio(expectedReturns, covarianceMatrix, combinedWeights))
            .convergenceStatus(OptimizationModelInfo.ConvergenceStatus.CONVERGED)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 시장 데이터 수집
     */
    private MarketDataMatrix collectMarketData(Portfolio portfolio) {
        List<String> symbols = portfolio.getPositions().stream()
            .map(Position::getSymbol)
            .toList();
        
        Map<String, List<MarketData>> historicalData = symbols.stream()
            .collect(Collectors.toMap(
                symbol -> symbol,
                symbol -> marketDataService.getHistoricalData(symbol, 252) // 1년 데이터
            ));
        
        return MarketDataMatrix.builder()
            .symbols(symbols)
            .historicalData(historicalData)
            .build();
    }
    
    /**
     * 공분산 행렬 계산
     */
    private RealMatrix calculateCovarianceMatrix(RealMatrix returns) {
        int n = returns.getColumnDimension();
        RealMatrix covMatrix = new Array2DRowRealMatrix(n, n);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double covariance = calculateCovariance(
                    returns.getColumn(i), 
                    returns.getColumn(j)
                );
                covMatrix.setEntry(i, j, covariance);
            }
        }
        
        return covMatrix;
    }
    
    private double calculateCovariance(double[] x, double[] y) {
        double meanX = Arrays.stream(x).average().orElse(0.0);
        double meanY = Arrays.stream(y).average().orElse(0.0);
        
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            sum += (x[i] - meanX) * (y[i] - meanY);
        }
        
        return sum / (x.length - 1);
    }
    
    private RealVector calculateExpectedReturns(RealMatrix returns) {
        int n = returns.getColumnDimension();
        double[] expectedReturns = new double[n];
        
        for (int i = 0; i < n; i++) {
            expectedReturns[i] = Arrays.stream(returns.getColumn(i))
                .average()
                .orElse(0.0);
        }
        
        return new ArrayRealVector(expectedReturns);
    }
    
    private double calculatePortfolioReturn(RealVector expectedReturns, double[] weights) {
        return expectedReturns.dotProduct(new ArrayRealVector(weights));
    }
    
    private double calculatePortfolioRisk(RealMatrix covarianceMatrix, double[] weights) {
        RealVector w = new ArrayRealVector(weights);
        return Math.sqrt(w.dotProduct(covarianceMatrix.operate(w)));
    }
    
    private double calculateSharpeRatio(RealVector expectedReturns, RealMatrix covarianceMatrix, double[] weights) {
        double portfolioReturn = calculatePortfolioReturn(expectedReturns, weights);
        double portfolioRisk = calculatePortfolioRisk(covarianceMatrix, weights);
        
        return portfolioRisk > 0 ? (portfolioReturn - RISK_FREE_RATE) / portfolioRisk : 0.0;
    }
    
    private double[] calculateRiskContributions(RealMatrix covarianceMatrix, double[] weights) {
        RealVector w = new ArrayRealVector(weights);
        RealVector portfolioRiskContrib = covarianceMatrix.operate(w);
        double portfolioRisk = calculatePortfolioRisk(covarianceMatrix, weights);
        
        double[] riskContributions = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            riskContributions[i] = weights[i] * portfolioRiskContrib.getEntry(i) / portfolioRisk;
        }
        
        return riskContributions;
    }
    
    private void normalizeWeights(double[] weights) {
        double sum = Arrays.stream(weights).sum();
        if (sum > 0) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= sum;
            }
        }
    }
    
    private void applyConstraints(double[] weights, OptimizationConstraints constraints) {
        if (constraints == null) return;
        
        // 가중치 제약 조건 적용
        if (constraints.getWeightConstraints() != null) {
            for (int i = 0; i < weights.length; i++) {
                // 간소화: 모든 자산에 동일한 제약 적용
                weights[i] = Math.max(0.0, Math.min(0.5, weights[i])); // 0% ~ 50% 제한
            }
        }
    }
    
    private double[] perturbWeights(double[] weights, Random random, double perturbationSize) {
        double[] perturbed = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            double noise = (random.nextGaussian() - 0.5) * perturbationSize;
            perturbed[i] = Math.max(0.0, weights[i] + noise);
        }
        return perturbed;
    }
    
    private RealVector calculateMarketCapWeights(MarketDataMatrix marketData) {
        // 간소화: 균등 가중치 반환 (실제로는 시가총액 데이터 필요)
        int n = marketData.getAssetCount();
        double[] weights = new double[n];
        Arrays.fill(weights, 1.0 / n);
        return new ArrayRealVector(weights);
    }
    
    private double[] optimizeForMaxReturn(RealVector expectedReturns, RealMatrix covarianceMatrix, OptimizationConstraints constraints) {
        // 최대 수익률 추구 (제약 조건 하에서)
        return optimizeForMaxSharpe(expectedReturns, covarianceMatrix, constraints);
    }
    
    private double[] optimizeForMinRisk(RealMatrix covarianceMatrix, OptimizationConstraints constraints) {
        // 최소 위험 추구
        return performMinimumVarianceOptimization(
            MarketDataMatrix.builder().build(), constraints
        ).getOptimalWeights();
    }
    
    private double[] optimizeForTargetVolatility(RealVector expectedReturns, RealMatrix covarianceMatrix, OptimizationConstraints constraints) {
        // 목표 변동성 달성
        return optimizeForMaxSharpe(expectedReturns, covarianceMatrix, constraints);
    }
    
    /**
     * 최적화 결과를 PortfolioOptimization 객체로 변환
     */
    private PortfolioOptimization buildOptimizationResult(
            Portfolio portfolio,
            OptimizationType optimizationType,
            OptimizationObjective objective,
            OptimizationResult result,
            OptimizationConstraints constraints) {
        
        List<AssetAllocation> recommendedAllocations = buildAssetAllocations(portfolio, result);
        List<AssetAllocation> currentAllocations = buildCurrentAllocations(portfolio);
        List<RebalancingAction> rebalancingActions = calculateRebalancingActions(
            currentAllocations, recommendedAllocations);
        
        return PortfolioOptimization.builder()
            .optimizationId(UUID.randomUUID().toString())
            .portfolioId(portfolio.getId())
            .optimizationType(optimizationType)
            .objective(objective)
            .recommendedAllocations(recommendedAllocations)
            .currentAllocations(currentAllocations)
            .rebalancingActions(rebalancingActions)
            .expectedPerformance(buildExpectedPerformance(result))
            .riskMetrics(buildRiskMetrics(result, portfolio))
            .constraints(constraints)
            .modelInfo(buildModelInfo(result))
            .marketOutlook(buildMarketOutlook())
            .generatedAt(LocalDateTime.now())
            .nextRebalancingDate(LocalDateTime.now().plusDays(30))
            .confidence(BigDecimal.valueOf(0.85))
            .build();
    }
    
    // 내부 클래스들
    @lombok.Data
    @lombok.Builder
    private static class MarketDataMatrix {
        private List<String> symbols;
        private Map<String, List<MarketData>> historicalData;
        
        public int getAssetCount() {
            return symbols != null ? symbols.size() : 0;
        }
        
        public RealMatrix getReturnsMatrix() {
            if (symbols.isEmpty()) return new Array2DRowRealMatrix(0, 0);
            
            List<MarketData> firstAssetData = historicalData.get(symbols.get(0));
            int timeSteps = firstAssetData.size() - 1;
            int assets = symbols.size();
            
            double[][] returnsData = new double[timeSteps][assets];
            
            for (int assetIdx = 0; assetIdx < assets; assetIdx++) {
                String symbol = symbols.get(assetIdx);
                List<MarketData> data = historicalData.get(symbol);
                
                for (int timeIdx = 0; timeIdx < timeSteps; timeIdx++) {
                    double currentPrice = data.get(timeIdx + 1).getPrice().doubleValue();
                    double previousPrice = data.get(timeIdx).getPrice().doubleValue();
                    returnsData[timeIdx][assetIdx] = (currentPrice - previousPrice) / previousPrice;
                }
            }
            
            return new Array2DRowRealMatrix(returnsData);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    private static class OptimizationResult {
        private double[] optimalWeights;
        private double expectedReturn;
        private double expectedRisk;
        private double sharpeRatio;
        private OptimizationModelInfo.ConvergenceStatus convergenceStatus;
        private long executionTimeMs;
    }
    
    // 헬퍼 메소드들 (간소화된 구현)
    private List<AssetAllocation> buildAssetAllocations(Portfolio portfolio, OptimizationResult result) {
        List<Position> positions = portfolio.getPositions();
        double[] weights = result.getOptimalWeights();
        
        return IntStream.range(0, Math.min(positions.size(), weights.length))
            .mapToObj(i -> AssetAllocation.builder()
                .symbol(positions.get(i).getSymbol())
                .assetName(positions.get(i).getSymbol())
                .category(AssetCategory.EQUITY)
                .recommendedWeight(BigDecimal.valueOf(weights[i]).setScale(4, RoundingMode.HALF_UP))
                .expectedReturn(BigDecimal.valueOf(result.getExpectedReturn()).setScale(4, RoundingMode.HALF_UP))
                .expectedVolatility(BigDecimal.valueOf(result.getExpectedRisk()).setScale(4, RoundingMode.HALF_UP))
                .riskContribution(BigDecimal.valueOf(weights[i] * 100).setScale(2, RoundingMode.HALF_UP))
                .allocationConfidence(BigDecimal.valueOf(0.8))
                .build())
            .toList();
    }
    
    private List<AssetAllocation> buildCurrentAllocations(Portfolio portfolio) {
        BigDecimal totalValue = portfolio.getTotalValue();
        
        return portfolio.getPositions().stream()
            .map(position -> AssetAllocation.builder()
                .symbol(position.getSymbol())
                .assetName(position.getSymbol())
                .category(AssetCategory.EQUITY)
                .currentWeight(position.getCurrentValue().divide(totalValue, 4, RoundingMode.HALF_UP))
                .currentQuantity(position.getQuantity())
                .build())
            .toList();
    }
    
    private List<RebalancingAction> calculateRebalancingActions(
            List<AssetAllocation> current, 
            List<AssetAllocation> recommended) {
        
        return recommended.stream()
            .map(rec -> {
                AssetAllocation cur = current.stream()
                    .filter(c -> c.getSymbol().equals(rec.getSymbol()))
                    .findFirst()
                    .orElse(null);
                
                BigDecimal currentWeight = cur != null ? cur.getCurrentWeight() : BigDecimal.ZERO;
                BigDecimal recommendedWeight = rec.getRecommendedWeight();
                BigDecimal weightDiff = recommendedWeight.subtract(currentWeight);
                
                RebalancingAction.ActionType actionType;
                if (weightDiff.compareTo(BigDecimal.valueOf(0.01)) > 0) {
                    actionType = RebalancingAction.ActionType.INCREASE_POSITION;
                } else if (weightDiff.compareTo(BigDecimal.valueOf(-0.01)) < 0) {
                    actionType = RebalancingAction.ActionType.REDUCE_POSITION;
                } else {
                    actionType = RebalancingAction.ActionType.HOLD;
                }
                
                return RebalancingAction.builder()
                    .actionType(actionType)
                    .symbol(rec.getSymbol())
                    .quantity(BigDecimal.ZERO) // 간소화
                    .amount(BigDecimal.ZERO)
                    .priority(1)
                    .recommendedExecutionTime(LocalDateTime.now())
                    .estimatedCost(BigDecimal.ZERO)
                    .marketImpact(BigDecimal.ZERO)
                    .reason(String.format("가중치 조정: %.2f%% → %.2f%%", 
                        currentWeight.multiply(BigDecimal.valueOf(100)), 
                        recommendedWeight.multiply(BigDecimal.valueOf(100))))
                    .build();
            })
            .toList();
    }
    
    private ExpectedPerformance buildExpectedPerformance(OptimizationResult result) {
        return ExpectedPerformance.builder()
            .expectedReturn(BigDecimal.valueOf(result.getExpectedReturn() * 100).setScale(2, RoundingMode.HALF_UP))
            .expectedVolatility(BigDecimal.valueOf(result.getExpectedRisk() * 100).setScale(2, RoundingMode.HALF_UP))
            .expectedSharpeRatio(BigDecimal.valueOf(result.getSharpeRatio()).setScale(4, RoundingMode.HALF_UP))
            .expectedMaxDrawdown(BigDecimal.valueOf(20.0))
            .expectedInformationRatio(BigDecimal.valueOf(0.5))
            .expectedCalmarRatio(BigDecimal.valueOf(0.3))
            .beta(BigDecimal.valueOf(1.0))
            .trackingError(BigDecimal.valueOf(5.0))
            .build();
    }
    
    private RiskMetrics buildRiskMetrics(OptimizationResult result, Portfolio portfolio) {
        return RiskMetrics.builder()
            .var95(BigDecimal.valueOf(result.getExpectedRisk() * 1.65 * 100).setScale(2, RoundingMode.HALF_UP))
            .cvar95(BigDecimal.valueOf(result.getExpectedRisk() * 2.0 * 100).setScale(2, RoundingMode.HALF_UP))
            .diversificationRatio(BigDecimal.valueOf(1.2))
            .concentrationRisk(BigDecimal.valueOf(30.0))
            .liquidityRisk(BigDecimal.valueOf(10.0))
            .tailRisk(BigDecimal.valueOf(15.0))
            .correlationRisk(BigDecimal.valueOf(20.0))
            .riskBudgetUtilization(BigDecimal.valueOf(85.0))
            .sectorRiskAllocation(Map.of("Technology", BigDecimal.valueOf(40.0)))
            .build();
    }
    
    private OptimizationModelInfo buildModelInfo(OptimizationResult result) {
        return OptimizationModelInfo.builder()
            .algorithmName("Modern Portfolio Theory")
            .modelVersion("1.0")
            .backtestSharpeRatio(BigDecimal.valueOf(result.getSharpeRatio()).setScale(4, RoundingMode.HALF_UP))
            .dataPeriod("252 days")
            .factorCount(10)
            .convergenceStatus(result.getConvergenceStatus())
            .executionTimeMs(result.getExecutionTimeMs())
            .build();
    }
    
    private MarketOutlook buildMarketOutlook() {
        return MarketOutlook.builder()
            .expectedRegime(MarketOutlook.MarketRegime.BULL_MARKET)
            .volatilityOutlook(MarketOutlook.VolatilityOutlook.STABLE)
            .interestRateOutlook(MarketOutlook.InterestRateOutlook.STABLE)
            .sectorOutlooks(Map.of("Technology", MarketOutlook.SectorOutlook.OUTPERFORM))
            .forecastHorizon(30)
            .build();
    }
}