package com.stockquest.application.service;

import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.risk.*;
import com.stockquest.domain.risk.port.RiskScenarioRepository;
import com.stockquest.domain.risk.port.StressTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 고급 리스크 관리 애플리케이션 서비스
 * 스트레스 테스트, 시나리오 분석, 실시간 모니터링 등 통합 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvancedRiskManagementService {

    private final RiskScenarioRepository riskScenarioRepository;
    private final StressTestRepository stressTestRepository;
    private final StressTestEngine stressTestEngine;
    private final AdvancedRiskCalculator riskCalculator;
    private final RealTimeRiskMonitor riskMonitor;

    /**
     * 종합 리스크 분석 수행
     * 다양한 시나리오에 대한 스트레스 테스트 및 리스크 메트릭 계산
     */
    @Transactional
    public ComprehensiveRiskAnalysisResult performComprehensiveRiskAnalysis(
            Long portfolioId,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix) {

        log.info("포트폴리오 {} 종합 리스크 분석 시작", portfolioId);

        try {
            // 1. 활성 시나리오 조회
            List<RiskScenario> activeScenarios = riskScenarioRepository.findActiveScenarios(LocalDateTime.now());
            log.debug("활성 시나리오 {}개 조회됨", activeScenarios.size());

            // 2. 각 시나리오별 스트레스 테스트 병렬 실행
            List<CompletableFuture<StressTestResult>> stressTestFutures = activeScenarios.stream()
                    .map(scenario -> CompletableFuture.supplyAsync(() -> {
                        try {
                            BigDecimal portfolioValue = calculatePortfolioValue(positions, currentPrices);
                            return stressTestEngine.runSingleScenarioTest(
                                    scenario, positions, currentPrices, portfolioValue);
                        } catch (Exception e) {
                            log.error("시나리오 {} 스트레스 테스트 실행 중 오류", scenario.getScenarioId(), e);
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());

            // 3. 스트레스 테스트 결과 수집
            List<StressTestResult> stressTestResults = stressTestFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 4. 결과 저장
            stressTestResults.forEach(stressTestRepository::save);

            // 5. 고급 리스크 메트릭 계산
            AdvancedRiskMetrics advancedMetrics = calculateAdvancedRiskMetrics(
                    positions, currentPrices, expectedReturns, volatilities, correlationMatrix);

            // 6. 실시간 리스크 모니터링 업데이트
            StressTestResult latestStressTest = stressTestResults.isEmpty() ? null :
                    stressTestResults.get(0);
            RealTimeRiskMonitor.RiskMonitoringResult monitoringResult =
                    riskMonitor.updateRiskLevels(portfolioId, positions, currentPrices, latestStressTest);

            // 7. 종합 분석 결과 생성
            ComprehensiveRiskAnalysisResult result = ComprehensiveRiskAnalysisResult.builder()
                    .portfolioId(portfolioId)
                    .analysisTime(LocalDateTime.now())
                    .portfolioValue(calculatePortfolioValue(positions, currentPrices))
                    .stressTestResults(stressTestResults)
                    .advancedMetrics(advancedMetrics)
                    .monitoringResult(monitoringResult)
                    .riskRecommendations(generateRiskRecommendations(stressTestResults, advancedMetrics))
                    .overallRiskAssessment(generateOverallRiskAssessment(stressTestResults, monitoringResult))
                    .build();

            log.info("포트폴리오 {} 종합 리스크 분석 완료 - 전체 리스크 점수: {}",
                    portfolioId, result.getOverallRiskAssessment().getOverallScore());

            return result;

        } catch (Exception e) {
            log.error("포트폴리오 {} 종합 리스크 분석 중 오류 발생", portfolioId, e);
            throw new RuntimeException("종합 리스크 분석 실행 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 몬테카를로 시뮬레이션 기반 리스크 분석
     */
    @Transactional
    public MonteCarloRiskAnalysisResult performMonteCarloRiskAnalysis(
            Long portfolioId,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix,
            Integer simulations,
            Integer timeHorizon) {

        log.info("포트폴리오 {} 몬테카를로 리스크 분석 시작 - 시뮬레이션: {}회, 기간: {}일",
                portfolioId, simulations, timeHorizon);

        try {
            BigDecimal portfolioValue = calculatePortfolioValue(positions, currentPrices);

            // 1. 몬테카를로 시뮬레이션 실행
            MonteCarloSimulation simulation = MonteCarloSimulation.builder()
                    .simulationId("MC_" + System.currentTimeMillis())
                    .portfolioId(portfolioId)
                    .createdAt(LocalDateTime.now())
                    .numberOfRuns(simulations)
                    .timeHorizon(timeHorizon)
                    .confidenceLevel(0.95)
                    .expectedReturns(expectedReturns)
                    .volatilities(volatilities)
                    .correlationMatrix(correlationMatrix)
                    .build();

            // 2. VaR 계산 (다양한 방법론)
            BigDecimal parametricVaR95 = riskCalculator.calculateParametricVaR(
                    positions, currentPrices, expectedReturns, volatilities, correlationMatrix, 0.95, timeHorizon);
            BigDecimal parametricVaR99 = riskCalculator.calculateParametricVaR(
                    positions, currentPrices, expectedReturns, volatilities, correlationMatrix, 0.99, timeHorizon);

            BigDecimal monteCarloVaR95 = riskCalculator.calculateMonteCarloVaR(
                    positions, currentPrices, expectedReturns, volatilities, correlationMatrix,
                    0.95, simulations, timeHorizon);
            BigDecimal monteCarloVaR99 = riskCalculator.calculateMonteCarloVaR(
                    positions, currentPrices, expectedReturns, volatilities, correlationMatrix,
                    0.99, simulations, timeHorizon);

            // 3. 민감도 분석
            Map<String, BigDecimal> deltaSensitivity = riskCalculator.calculateDeltaSensitivity(
                    positions, currentPrices, BigDecimal.valueOf(0.01));
            Map<String, BigDecimal> gammaSensitivity = riskCalculator.calculateGammaSensitivity(
                    positions, currentPrices, BigDecimal.valueOf(0.01));

            // 4. 결과 생성
            MonteCarloRiskAnalysisResult result = MonteCarloRiskAnalysisResult.builder()
                    .portfolioId(portfolioId)
                    .analysisTime(LocalDateTime.now())
                    .portfolioValue(portfolioValue)
                    .simulation(simulation)
                    .parametricVaR95(parametricVaR95)
                    .parametricVaR99(parametricVaR99)
                    .monteCarloVaR95(monteCarloVaR95)
                    .monteCarloVaR99(monteCarloVaR99)
                    .deltaSensitivity(deltaSensitivity)
                    .gammaSensitivity(gammaSensitivity)
                    .qualityScore(simulation.calculateQualityScore())
                    .build();

            log.info("포트폴리오 {} 몬테카를로 분석 완료 - VaR95: {}, VaR99: {}",
                    portfolioId, monteCarloVaR95, monteCarloVaR99);

            return result;

        } catch (Exception e) {
            log.error("포트폴리오 {} 몬테카를로 리스크 분석 중 오류 발생", portfolioId, e);
            throw new RuntimeException("몬테카를로 리스크 분석 실행 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 블랙스완 이벤트 영향 분석
     */
    @Transactional
    public BlackSwanAnalysisResult analyzeBlackSwanEvents(
            Long portfolioId,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices) {

        log.info("포트폴리오 {} 블랙스완 이벤트 분석 시작", portfolioId);

        try {
            // 1. 블랙스완 시나리오 조회
            List<RiskScenario> blackSwanScenarios = riskScenarioRepository.findByType(ScenarioType.BLACK_SWAN);

            // 2. 극단적 시나리오 추가 생성
            List<RiskScenario> extremeScenarios = createExtremeScenarios();
            blackSwanScenarios.addAll(extremeScenarios);

            BigDecimal portfolioValue = calculatePortfolioValue(positions, currentPrices);

            // 3. 각 블랙스완 시나리오 테스트
            List<StressTestResult> blackSwanResults = blackSwanScenarios.stream()
                    .map(scenario -> stressTestEngine.runSingleScenarioTest(
                            scenario, positions, currentPrices, portfolioValue))
                    .collect(Collectors.toList());

            // 4. 최악의 시나리오 식별
            StressTestResult worstCaseScenario = blackSwanResults.stream()
                    .max(Comparator.comparing(result ->
                            result.getWorstCaseLoss() != null ? result.getWorstCaseLoss() : BigDecimal.ZERO))
                    .orElse(null);

            // 5. 생존 가능성 분석
            SurvivalAnalysis survivalAnalysis = analyzeSurvivalProbability(blackSwanResults, portfolioValue);

            // 6. 대응 전략 생성
            List<String> mitigationStrategies = generateBlackSwanMitigationStrategies(
                    blackSwanResults, positions, currentPrices);

            BlackSwanAnalysisResult result = BlackSwanAnalysisResult.builder()
                    .portfolioId(portfolioId)
                    .analysisTime(LocalDateTime.now())
                    .portfolioValue(portfolioValue)
                    .blackSwanScenarios(blackSwanScenarios)
                    .stressTestResults(blackSwanResults)
                    .worstCaseScenario(worstCaseScenario)
                    .survivalAnalysis(survivalAnalysis)
                    .mitigationStrategies(mitigationStrategies)
                    .riskAssessment(generateBlackSwanRiskAssessment(blackSwanResults))
                    .build();

            log.info("포트폴리오 {} 블랙스완 분석 완료 - 최대 손실: {}",
                    portfolioId, worstCaseScenario != null ? worstCaseScenario.getWorstCaseLoss() : "N/A");

            return result;

        } catch (Exception e) {
            log.error("포트폴리오 {} 블랙스완 이벤트 분석 중 오류 발생", portfolioId, e);
            throw new RuntimeException("블랙스완 이벤트 분석 실행 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 리스크 한도 설정 및 모니터링 활성화
     */
    @Transactional
    public void setupRiskLimitsAndMonitoring(
            Long portfolioId,
            Map<String, BigDecimal> riskLimits,
            Map<String, Double> alertThresholds) {

        log.info("포트폴리오 {} 리스크 한도 설정 및 모니터링 활성화", portfolioId);

        try {
            // 리스크 한도 설정
            for (Map.Entry<String, BigDecimal> entry : riskLimits.entrySet()) {
                String riskType = entry.getKey();
                BigDecimal limit = entry.getValue();
                Double threshold = alertThresholds.getOrDefault(riskType, 0.8);

                riskMonitor.setRiskLimit(riskType, limit, threshold);
            }

            log.info("포트폴리오 {} 리스크 한도 설정 완료 - {}개 한도 설정", portfolioId, riskLimits.size());

        } catch (Exception e) {
            log.error("포트폴리오 {} 리스크 한도 설정 중 오류 발생", portfolioId, e);
            throw new RuntimeException("리스크 한도 설정 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 시나리오 기반 포트폴리오 최적화 권장사항
     */
    public PortfolioOptimizationRecommendation generateScenarioBasedOptimization(
            Long portfolioId,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            List<RiskScenario> targetScenarios) {

        log.info("포트폴리오 {} 시나리오 기반 최적화 권장사항 생성", portfolioId);

        try {
            BigDecimal portfolioValue = calculatePortfolioValue(positions, currentPrices);

            // 1. 각 시나리오별 현재 포트폴리오 성능 분석
            Map<String, StressTestResult> scenarioResults = targetScenarios.stream()
                    .collect(Collectors.toMap(
                            RiskScenario::getScenarioId,
                            scenario -> stressTestEngine.runSingleScenarioTest(
                                    scenario, positions, currentPrices, portfolioValue)
                    ));

            // 2. 리스크 기여도 분석
            Map<String, BigDecimal> riskContributions = calculateRiskContributions(
                    positions, currentPrices, scenarioResults);

            // 3. 포지션 조정 권장사항
            Map<String, BigDecimal> positionAdjustments = riskMonitor.recommendPositionAdjustments(
                    positions, currentPrices, riskContributions);

            // 4. 헤지 전략 권장사항
            List<String> hedgingStrategies = generateHedgingStrategies(scenarioResults, positions);

            // 5. 다각화 개선 권장사항
            List<String> diversificationRecommendations = generateDiversificationRecommendations(
                    positions, currentPrices, riskContributions);

            return PortfolioOptimizationRecommendation.builder()
                    .portfolioId(portfolioId)
                    .analysisTime(LocalDateTime.now())
                    .currentPortfolioValue(portfolioValue)
                    .scenarioResults(scenarioResults)
                    .riskContributions(riskContributions)
                    .positionAdjustments(positionAdjustments)
                    .hedgingStrategies(hedgingStrategies)
                    .diversificationRecommendations(diversificationRecommendations)
                    .expectedRiskReduction(calculateExpectedRiskReduction(positionAdjustments, riskContributions))
                    .build();

        } catch (Exception e) {
            log.error("포트폴리오 {} 시나리오 기반 최적화 권장사항 생성 중 오류 발생", portfolioId, e);
            throw new RuntimeException("시나리오 기반 최적화 권장사항 생성 중 오류가 발생했습니다.", e);
        }
    }

    // === Private Helper Methods ===

    private BigDecimal calculatePortfolioValue(List<PortfolioPosition> positions, Map<String, BigDecimal> currentPrices) {
        return positions.stream()
                .filter(PortfolioPosition::hasPosition)
                .map(position -> {
                    BigDecimal price = currentPrices.get(position.getInstrumentKey());
                    return price != null ? position.calculateCurrentValue(price) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AdvancedRiskMetrics calculateAdvancedRiskMetrics(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, Double> expectedReturns,
            Map<String, Double> volatilities,
            Map<String, Map<String, Double>> correlationMatrix) {

        // 기본 메트릭 계산
        BigDecimal parametricVaR95 = riskCalculator.calculateParametricVaR(
                positions, currentPrices, expectedReturns, volatilities, correlationMatrix, 0.95, 1);
        BigDecimal parametricVaR99 = riskCalculator.calculateParametricVaR(
                positions, currentPrices, expectedReturns, volatilities, correlationMatrix, 0.99, 1);

        Map<String, BigDecimal> deltaSensitivity = riskCalculator.calculateDeltaSensitivity(
                positions, currentPrices, BigDecimal.valueOf(0.01));
        Map<String, BigDecimal> gammaSensitivity = riskCalculator.calculateGammaSensitivity(
                positions, currentPrices, BigDecimal.valueOf(0.01));

        return AdvancedRiskMetrics.builder()
                .parametricVaR95(parametricVaR95)
                .parametricVaR99(parametricVaR99)
                .deltaSensitivity(deltaSensitivity)
                .gammaSensitivity(gammaSensitivity)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private List<String> generateRiskRecommendations(
            List<StressTestResult> stressTestResults,
            AdvancedRiskMetrics advancedMetrics) {

        List<String> recommendations = new ArrayList<>();

        // 스트레스 테스트 기반 권장사항
        if (!stressTestResults.isEmpty()) {
            StressTestResult worstResult = stressTestResults.stream()
                    .max(Comparator.comparing(result ->
                            result.calculateOverallRiskScore() != null ? result.calculateOverallRiskScore() : 0))
                    .orElse(null);

            if (worstResult != null) {
                recommendations.addAll(worstResult.getRecommendedActions());
            }
        }

        // 고급 메트릭 기반 권장사항
        if (advancedMetrics.getParametricVaR99() != null) {
            BigDecimal var99 = advancedMetrics.getParametricVaR99();
            if (var99.compareTo(BigDecimal.valueOf(100000)) > 0) { // 10만원 초과
                recommendations.add("99% VaR이 높습니다. 포지션 축소를 고려하세요.");
            }
        }

        return recommendations;
    }

    private OverallRiskAssessment generateOverallRiskAssessment(
            List<StressTestResult> stressTestResults,
            RealTimeRiskMonitor.RiskMonitoringResult monitoringResult) {

        int overallScore = monitoringResult.getOverallRiskScore() != null ?
                monitoringResult.getOverallRiskScore() : 0;

        String riskLevel = "LOW";
        if (overallScore >= 80) riskLevel = "VERY_HIGH";
        else if (overallScore >= 60) riskLevel = "HIGH";
        else if (overallScore >= 40) riskLevel = "MEDIUM";

        return OverallRiskAssessment.builder()
                .overallScore(overallScore)
                .riskLevel(riskLevel)
                .assessmentTime(LocalDateTime.now())
                .keyRiskFactors(identifyKeyRiskFactors(stressTestResults))
                .recommendation(monitoringResult.getRecommendation())
                .build();
    }

    private List<String> identifyKeyRiskFactors(List<StressTestResult> stressTestResults) {
        List<String> riskFactors = new ArrayList<>();

        for (StressTestResult result : stressTestResults) {
            Integer riskScore = result.calculateOverallRiskScore();
            if (riskScore != null && riskScore > 60) {
                riskFactors.add("높은 리스크 점수: " + riskScore);
            }
            if (result.getConcentrationRisk() != null && result.getConcentrationRisk() > 0.3) {
                riskFactors.add("집중도 위험 높음");
            }
        }

        return riskFactors;
    }

    private List<RiskScenario> createExtremeScenarios() {
        return List.of(
                RiskScenarioFactory.createCustomScenario(
                        "극단적 시장 붕괴",
                        "90% 하락하는 극단적 시장 충격",
                        ScenarioType.BLACK_SWAN,
                        ScenarioSeverity.CATASTROPHIC,
                        Map.of("GENERAL", BigDecimal.valueOf(-0.90)),
                        BigDecimal.valueOf(10.0),
                        1
                )
        );
    }

    private SurvivalAnalysis analyzeSurvivalProbability(
            List<StressTestResult> blackSwanResults, BigDecimal portfolioValue) {

        long survivableScenarios = blackSwanResults.stream()
                .mapToLong(result -> {
                    BigDecimal loss = result.getWorstCaseLoss() != null ?
                            result.getWorstCaseLoss() : BigDecimal.ZERO;
                    BigDecimal remainingValue = portfolioValue.subtract(loss);
                    return remainingValue.compareTo(portfolioValue.multiply(BigDecimal.valueOf(0.1))) > 0 ? 1L : 0L;
                })
                .sum();

        double survivalRate = blackSwanResults.isEmpty() ? 1.0 :
                (double) survivableScenarios / blackSwanResults.size();

        return SurvivalAnalysis.builder()
                .survivalRate(survivalRate)
                .survivableScenarios((int) survivableScenarios)
                .totalScenarios(blackSwanResults.size())
                .analysisTime(LocalDateTime.now())
                .build();
    }

    private List<String> generateBlackSwanMitigationStrategies(
            List<StressTestResult> blackSwanResults,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices) {

        List<String> strategies = new ArrayList<>();

        strategies.add("포트폴리오 다각화 극대화");
        strategies.add("현금 및 현금성 자산 비중 확대 (20% 이상)");
        strategies.add("방어적 자산 (금, 국채) 비중 증가");
        strategies.add("테일 헤지 전략 도입");
        strategies.add("포지션 크기 제한 (단일 자산 5% 이하)");
        strategies.add("스톱로스 주문 자동화");

        return strategies;
    }

    private String generateBlackSwanRiskAssessment(List<StressTestResult> blackSwanResults) {
        if (blackSwanResults.isEmpty()) {
            return "블랙스완 시나리오 데이터가 부족합니다.";
        }

        OptionalDouble avgRiskScore = blackSwanResults.stream()
                .map(StressTestResult::calculateOverallRiskScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();

        if (avgRiskScore.isPresent()) {
            double score = avgRiskScore.getAsDouble();
            if (score >= 80) return "극도로 높은 블랙스완 리스크";
            if (score >= 60) return "높은 블랙스완 리스크";
            if (score >= 40) return "보통 블랙스완 리스크";
            return "낮은 블랙스완 리스크";
        }

        return "블랙스완 리스크 평가 불가";
    }

    private Map<String, BigDecimal> calculateRiskContributions(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, StressTestResult> scenarioResults) {

        Map<String, BigDecimal> contributions = new HashMap<>();

        BigDecimal totalValue = calculatePortfolioValue(positions, currentPrices);

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal positionValue = position.calculateCurrentValue(
                    currentPrices.get(instrument));

            if (positionValue != null && totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal weight = positionValue.divide(totalValue, 6, java.math.RoundingMode.HALF_UP);
                contributions.put(instrument, weight);
            }
        }

        return contributions;
    }

    private List<String> generateHedgingStrategies(
            Map<String, StressTestResult> scenarioResults,
            List<PortfolioPosition> positions) {

        return List.of(
                "변동성 헤지를 위한 VIX 옵션 활용",
                "금리 리스크 헤지를 위한 금리 스왑",
                "통화 헤지를 위한 FX 포워드",
                "섹터 중립 전략 도입",
                "베타 헤지를 위한 지수 선물"
        );
    }

    private List<String> generateDiversificationRecommendations(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, BigDecimal> riskContributions) {

        return List.of(
                "자산군별 분산 투자 확대",
                "지역별 다각화 (미국, 유럽, 아시아)",
                "시가총액별 분산 (대형, 중형, 소형)",
                "투자 스타일 다각화 (성장, 가치, 품질)",
                "대안 투자 고려 (REIT, 원자재, 인프라)"
        );
    }

    private BigDecimal calculateExpectedRiskReduction(
            Map<String, BigDecimal> positionAdjustments,
            Map<String, BigDecimal> riskContributions) {

        // 단순화된 리스크 감소 추정
        return BigDecimal.valueOf(0.15); // 15% 리스크 감소 예상
    }

    // === Inner Classes ===

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ComprehensiveRiskAnalysisResult {
        private Long portfolioId;
        private LocalDateTime analysisTime;
        private BigDecimal portfolioValue;
        private List<StressTestResult> stressTestResults;
        private AdvancedRiskMetrics advancedMetrics;
        private RealTimeRiskMonitor.RiskMonitoringResult monitoringResult;
        private List<String> riskRecommendations;
        private OverallRiskAssessment overallRiskAssessment;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MonteCarloRiskAnalysisResult {
        private Long portfolioId;
        private LocalDateTime analysisTime;
        private BigDecimal portfolioValue;
        private MonteCarloSimulation simulation;
        private BigDecimal parametricVaR95;
        private BigDecimal parametricVaR99;
        private BigDecimal monteCarloVaR95;
        private BigDecimal monteCarloVaR99;
        private Map<String, BigDecimal> deltaSensitivity;
        private Map<String, BigDecimal> gammaSensitivity;
        private Integer qualityScore;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BlackSwanAnalysisResult {
        private Long portfolioId;
        private LocalDateTime analysisTime;
        private BigDecimal portfolioValue;
        private List<RiskScenario> blackSwanScenarios;
        private List<StressTestResult> stressTestResults;
        private StressTestResult worstCaseScenario;
        private SurvivalAnalysis survivalAnalysis;
        private List<String> mitigationStrategies;
        private String riskAssessment;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioOptimizationRecommendation {
        private Long portfolioId;
        private LocalDateTime analysisTime;
        private BigDecimal currentPortfolioValue;
        private Map<String, StressTestResult> scenarioResults;
        private Map<String, BigDecimal> riskContributions;
        private Map<String, BigDecimal> positionAdjustments;
        private List<String> hedgingStrategies;
        private List<String> diversificationRecommendations;
        private BigDecimal expectedRiskReduction;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AdvancedRiskMetrics {
        private BigDecimal parametricVaR95;
        private BigDecimal parametricVaR99;
        private Map<String, BigDecimal> deltaSensitivity;
        private Map<String, BigDecimal> gammaSensitivity;
        private LocalDateTime calculatedAt;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OverallRiskAssessment {
        private Integer overallScore;
        private String riskLevel;
        private LocalDateTime assessmentTime;
        private List<String> keyRiskFactors;
        private String recommendation;
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SurvivalAnalysis {
        private Double survivalRate;
        private Integer survivableScenarios;
        private Integer totalScenarios;
        private LocalDateTime analysisTime;
    }
}