package com.stockquest.application.service;

import com.stockquest.application.port.out.RiskAssessmentPort;
import com.stockquest.domain.analytics.risk.MonteCarloSimulation;
import com.stockquest.domain.analytics.risk.RiskEngine;
import com.stockquest.domain.analytics.risk.RiskScenario;
import com.stockquest.domain.portfolio.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * 실시간 리스크 평가 서비스
 * Monte Carlo 시뮬레이션을 통한 실시간 포트폴리오 리스크 분석
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeRiskAssessmentService {
    
    private final RiskAssessmentPort riskAssessmentPort;
    private final PortfolioService portfolioService;
    
    private static final int DEFAULT_ITERATIONS = 10000;
    private static final int DEFAULT_SIMULATION_DAYS = 252; // 1년 (거래일 기준)
    private static final BigDecimal DEFAULT_CONFIDENCE_LEVEL = new BigDecimal("0.95");
    private static final int PRECISION_SCALE = 6;
    
    /**
     * 포트폴리오에 대한 실시간 리스크 평가 수행
     */
    @Transactional
    public CompletableFuture<MonteCarloSimulation> assessPortfolioRisk(Long portfolioId, String scenarioId) {
        log.info("포트폴리오 리스크 평가 시작: portfolioId={}, scenarioId={}", portfolioId, scenarioId);
        
        try {
            // 캐시 확인
            String cacheKey = generateCacheKey(portfolioId, scenarioId);
            Optional<MonteCarloSimulation> cachedResult = riskAssessmentPort.getCachedSimulationResult(cacheKey);
            
            if (cachedResult.isPresent() && isCacheValid(cachedResult.get())) {
                log.info("캐시된 리스크 평가 결과 반환: portfolioId={}", portfolioId);
                return CompletableFuture.completedFuture(cachedResult.get());
            }
            
            // 비동기 시뮬레이션 실행
            return executeSimulationAsync(portfolioId, scenarioId, cacheKey);
            
        } catch (Exception e) {
            log.error("포트폴리오 리스크 평가 중 오류 발생: portfolioId={}", portfolioId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 비동기 Monte Carlo 시뮬레이션 실행
     */
    @Async("riskAssessmentTaskExecutor")
    public CompletableFuture<MonteCarloSimulation> executeSimulationAsync(Long portfolioId, String scenarioId, String cacheKey) {
        String simulationId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 시뮬레이션 초기화
            MonteCarloSimulation simulation = initializeSimulation(simulationId, portfolioId, scenarioId, startTime);
            simulation = riskAssessmentPort.saveMonteCarloSimulation(simulation);
            
            // 포트폴리오 및 시나리오 데이터 조회
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            RiskScenario scenario = riskAssessmentPort.findRiskScenario(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("리스크 시나리오를 찾을 수 없습니다: " + scenarioId));
            
            // 시뮬레이션 상태 업데이트
            simulation = riskAssessmentPort.updateSimulationStatus(simulationId, MonteCarloSimulation.SimulationStatus.RUNNING);
            
            // Monte Carlo 시뮬레이션 실행
            MonteCarloSimulation result = performMonteCarloSimulation(simulation, portfolio, scenario);
            
            // 결과 저장 및 캐시
            result.setEndTime(LocalDateTime.now());
            result.setExecutionTimeMs(java.time.Duration.between(startTime, result.getEndTime()).toMillis());
            result.setStatus(MonteCarloSimulation.SimulationStatus.COMPLETED);
            
            result = riskAssessmentPort.saveMonteCarloSimulation(result);
            riskAssessmentPort.cacheSimulationResult(cacheKey, result, 3600); // 1시간 캐시
            
            // 리스크 임계값 확인 및 알림
            checkRiskThresholds(result, scenario);
            
            log.info("Monte Carlo 시뮬레이션 완료: simulationId={}, 실행시간={}ms", 
                simulationId, result.getExecutionTimeMs());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("Monte Carlo 시뮬레이션 실행 중 오류: simulationId={}", simulationId, e);
            
            // 실패 상태로 업데이트
            MonteCarloSimulation failedSimulation = MonteCarloSimulation.builder()
                .simulationId(simulationId)
                .portfolioId(portfolioId)
                .scenarioId(scenarioId)
                .startTime(startTime)
                .endTime(LocalDateTime.now())
                .status(MonteCarloSimulation.SimulationStatus.FAILED)
                .errorMessage(e.getMessage())
                .build();
                
            riskAssessmentPort.saveMonteCarloSimulation(failedSimulation);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Monte Carlo 시뮬레이션 실행
     */
    private MonteCarloSimulation performMonteCarloSimulation(MonteCarloSimulation simulation, 
                                                           Portfolio portfolio, RiskScenario scenario) {
        
        int iterations = scenario.getIterations() != null ? scenario.getIterations() : DEFAULT_ITERATIONS;
        int days = scenario.getSimulationDays() != null ? scenario.getSimulationDays() : DEFAULT_SIMULATION_DAYS;
        
        // 난수 생성기 초기화
        MersenneTwister random = new MersenneTwister();
        NormalDistribution normalDist = new NormalDistribution(random, 0.0, 1.0);
        
        // 시뮬레이션 파라미터 설정
        MonteCarloSimulation.SimulationParameters parameters = MonteCarloSimulation.SimulationParameters.builder()
            .iterations(iterations)
            .days(days)
            .initialValue(portfolio.getTotalValue())
            .riskFreeRate(new BigDecimal("0.02")) // 2% 무위험 수익률
            .rebalancingFrequency(21) // 월별 리밸런싱
            .randomSeed(random.getSeed())
            .parallelThreads(Runtime.getRuntime().availableProcessors())
            .samplePathCount(100) // 샘플 경로 저장 개수
            .build();
        
        // 시뮬레이션 실행
        List<Double> finalReturns = new ArrayList<>();
        List<MonteCarloSimulation.SimulationPath> samplePaths = new ArrayList<>();
        
        // 병렬 처리로 시뮬레이션 성능 향상
        IntStream.range(0, iterations).parallel().forEach(i -> {
            List<Double> pathReturns = new ArrayList<>();
            BigDecimal currentValue = portfolio.getTotalValue();
            
            for (int day = 1; day <= days; day++) {
                // 일일 수익률 생성 (정규분포 기반)
                double dailyReturn = scenario.getExpectedReturn().doubleValue() / 252.0 +
                    scenario.getMarketVolatility().doubleValue() / Math.sqrt(252.0) * normalDist.sample();
                
                pathReturns.add(dailyReturn);
                currentValue = currentValue.multiply(BigDecimal.valueOf(1 + dailyReturn));
            }
            
            // 최종 수익률 계산
            double totalReturn = currentValue.subtract(portfolio.getTotalValue())
                .divide(portfolio.getTotalValue(), PRECISION_SCALE, RoundingMode.HALF_UP).doubleValue();
            
            synchronized (finalReturns) {
                finalReturns.add(totalReturn);
                
                // 샘플 경로 저장 (처음 100개 경로만)
                if (samplePaths.size() < parameters.getSamplePathCount()) {
                    samplePaths.add(createSimulationPath(i, pathReturns, portfolio.getTotalValue()));
                }
            }
        });
        
        // 통계 분석 수행
        MonteCarloSimulation.SimulationStatistics statistics = calculateStatistics(finalReturns);
        
        // 리스크 메트릭 계산
        Map<String, BigDecimal> riskMetrics = calculateRiskMetrics(finalReturns, statistics);
        
        // 결과 업데이트
        simulation.setParameters(parameters);
        simulation.setStatistics(statistics);
        simulation.setSamplePaths(samplePaths);
        simulation.setRiskMetrics(riskMetrics);
        
        return simulation;
    }
    
    /**
     * 시뮬레이션 경로 생성
     */
    private MonteCarloSimulation.SimulationPath createSimulationPath(int pathId, List<Double> dailyReturns, BigDecimal initialValue) {
        List<MonteCarloSimulation.PathPoint> points = new ArrayList<>();
        BigDecimal currentValue = initialValue;
        double cumulativeReturn = 0.0;
        double maxDrawdown = 0.0;
        double peakValue = initialValue.doubleValue();
        
        for (int day = 0; day < dailyReturns.size(); day++) {
            double dailyReturn = dailyReturns.get(day);
            currentValue = currentValue.multiply(BigDecimal.valueOf(1 + dailyReturn));
            cumulativeReturn = (currentValue.doubleValue() - initialValue.doubleValue()) / initialValue.doubleValue();
            
            // 최대 낙폭 계산
            if (currentValue.doubleValue() > peakValue) {
                peakValue = currentValue.doubleValue();
            }
            double drawdown = (peakValue - currentValue.doubleValue()) / peakValue;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
            
            points.add(MonteCarloSimulation.PathPoint.builder()
                .day(day + 1)
                .value(currentValue)
                .cumulativeReturn(BigDecimal.valueOf(cumulativeReturn).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
                .dailyReturn(BigDecimal.valueOf(dailyReturn).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
                .build());
        }
        
        // 변동성 계산
        double volatility = calculatePathVolatility(dailyReturns);
        
        return MonteCarloSimulation.SimulationPath.builder()
            .pathId(String.valueOf(pathId))
            .points(points)
            .finalReturn(BigDecimal.valueOf(cumulativeReturn).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .maxDrawdown(BigDecimal.valueOf(maxDrawdown).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .volatility(BigDecimal.valueOf(volatility).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .build();
    }
    
    /**
     * 통계 분석 수행
     */
    private MonteCarloSimulation.SimulationStatistics calculateStatistics(List<Double> returns) {
        Collections.sort(returns);
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        double min = returns.get(0);
        double max = returns.get(returns.size() - 1);
        double median = returns.get(returns.size() / 2);
        
        // VaR 계산 (95%, 99%)
        double var95 = returns.get((int) (returns.size() * 0.05));
        double var99 = returns.get((int) (returns.size() * 0.01));
        
        // CVaR 계산
        double cvar95 = returns.subList(0, (int) (returns.size() * 0.05))
            .stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double cvar99 = returns.subList(0, (int) (returns.size() * 0.01))
            .stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // 샤프 비율 계산 (무위험 수익률 2% 가정)
        double riskFreeRate = 0.02;
        double sharpeRatio = stdDev > 0 ? (mean - riskFreeRate) / stdDev : 0.0;
        
        // 손실 확률
        double probabilityOfLoss = (double) returns.stream().mapToInt(r -> r < 0 ? 1 : 0).sum() / returns.size();
        
        // 백분위수 분포
        Map<Integer, BigDecimal> percentiles = new HashMap<>();
        percentiles.put(5, BigDecimal.valueOf(returns.get((int) (returns.size() * 0.05))).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        percentiles.put(25, BigDecimal.valueOf(returns.get((int) (returns.size() * 0.25))).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        percentiles.put(75, BigDecimal.valueOf(returns.get((int) (returns.size() * 0.75))).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        percentiles.put(95, BigDecimal.valueOf(returns.get((int) (returns.size() * 0.95))).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        
        return MonteCarloSimulation.SimulationStatistics.builder()
            .meanReturn(BigDecimal.valueOf(mean).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .standardDeviation(BigDecimal.valueOf(stdDev).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .maxReturn(BigDecimal.valueOf(max).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .minReturn(BigDecimal.valueOf(min).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .median(BigDecimal.valueOf(median).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .var95(BigDecimal.valueOf(var95).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .var99(BigDecimal.valueOf(var99).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .cvar95(BigDecimal.valueOf(cvar95).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .cvar99(BigDecimal.valueOf(cvar99).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .sharpeRatio(BigDecimal.valueOf(sharpeRatio).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .probabilityOfLoss(BigDecimal.valueOf(probabilityOfLoss).setScale(PRECISION_SCALE, RoundingMode.HALF_UP))
            .percentiles(percentiles)
            .build();
    }
    
    /**
     * 리스크 메트릭 계산
     */
    private Map<String, BigDecimal> calculateRiskMetrics(List<Double> returns, MonteCarloSimulation.SimulationStatistics stats) {
        Map<String, BigDecimal> metrics = new HashMap<>();
        
        metrics.put("EXPECTED_RETURN", stats.getMeanReturn());
        metrics.put("VOLATILITY", stats.getStandardDeviation());
        metrics.put("VAR_95", stats.getVar95());
        metrics.put("VAR_99", stats.getVar99());
        metrics.put("CVAR_95", stats.getCvar95());
        metrics.put("CVAR_99", stats.getCvar99());
        metrics.put("SHARPE_RATIO", stats.getSharpeRatio());
        metrics.put("PROBABILITY_OF_LOSS", stats.getProbabilityOfLoss());
        
        // 추가 리스크 메트릭
        double skewness = calculateSkewness(returns, stats.getMeanReturn().doubleValue());
        double kurtosis = calculateKurtosis(returns, stats.getMeanReturn().doubleValue(), stats.getStandardDeviation().doubleValue());
        
        metrics.put("SKEWNESS", BigDecimal.valueOf(skewness).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        metrics.put("KURTOSIS", BigDecimal.valueOf(kurtosis).setScale(PRECISION_SCALE, RoundingMode.HALF_UP));
        
        return metrics;
    }
    
    /**
     * 경로별 변동성 계산
     */
    private double calculatePathVolatility(List<Double> dailyReturns) {
        double mean = dailyReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = dailyReturns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance * 252); // 연간화
    }
    
    /**
     * 왜도(Skewness) 계산
     */
    private double calculateSkewness(List<Double> returns, double mean) {
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        if (stdDev == 0) return 0.0;
        
        double skewness = returns.stream()
            .mapToDouble(r -> Math.pow((r - mean) / stdDev, 3))
            .average().orElse(0.0);
            
        return skewness;
    }
    
    /**
     * 첨도(Kurtosis) 계산
     */
    private double calculateKurtosis(List<Double> returns, double mean, double stdDev) {
        if (stdDev == 0) return 0.0;
        
        double kurtosis = returns.stream()
            .mapToDouble(r -> Math.pow((r - mean) / stdDev, 4))
            .average().orElse(0.0);
            
        return kurtosis - 3.0; // Excess kurtosis
    }
    
    /**
     * 시뮬레이션 초기화
     */
    private MonteCarloSimulation initializeSimulation(String simulationId, Long portfolioId, 
                                                    String scenarioId, LocalDateTime startTime) {
        return MonteCarloSimulation.builder()
            .simulationId(simulationId)
            .portfolioId(portfolioId)
            .scenarioId(scenarioId)
            .startTime(startTime)
            .status(MonteCarloSimulation.SimulationStatus.PENDING)
            .build();
    }
    
    /**
     * 캐시 키 생성
     */
    private String generateCacheKey(Long portfolioId, String scenarioId) {
        return String.format("risk_assessment:%d:%s", portfolioId, scenarioId);
    }
    
    /**
     * 캐시 유효성 검사 (1시간 이내)
     */
    private boolean isCacheValid(MonteCarloSimulation simulation) {
        return simulation.getEndTime() != null && 
               simulation.getEndTime().isAfter(LocalDateTime.now().minusHours(1));
    }
    
    /**
     * 리스크 임계값 확인 및 알림
     */
    private void checkRiskThresholds(MonteCarloSimulation simulation, RiskScenario scenario) {
        // VaR 99% 임계값 확인 (20% 손실 이상)
        if (simulation.getStatistics().getVar99().compareTo(new BigDecimal("-0.20")) < 0) {
            riskAssessmentPort.sendRiskAlert(
                "MAIN_ENGINE", 
                String.format("극한 VaR 위험: 포트폴리오 %d, VaR99: %s", 
                    simulation.getPortfolioId(), 
                    simulation.getStatistics().getVar99()), 
                "HIGH"
            );
        }
        
        // 손실 확률 임계값 확인 (30% 이상)
        if (simulation.getStatistics().getProbabilityOfLoss().compareTo(new BigDecimal("0.30")) > 0) {
            riskAssessmentPort.sendRiskAlert(
                "MAIN_ENGINE", 
                String.format("높은 손실 확률: 포트폴리오 %d, 손실확률: %s", 
                    simulation.getPortfolioId(), 
                    simulation.getStatistics().getProbabilityOfLoss()), 
                "MEDIUM"
            );
        }
    }
    
    /**
     * 실행 중인 시뮬레이션 상태 조회
     */
    @Cacheable(value = "runningSimulations", unless = "#result.empty")
    public List<MonteCarloSimulation> getRunningSimulations() {
        return riskAssessmentPort.findRunningSimulations();
    }
    
    /**
     * 포트폴리오별 최근 리스크 평가 결과 조회
     */
    @Cacheable(value = "recentRiskAssessments", key = "#portfolioId")
    public List<MonteCarloSimulation> getRecentRiskAssessments(Long portfolioId) {
        return riskAssessmentPort.findRecentSimulationsByPortfolio(portfolioId, 10);
    }
}