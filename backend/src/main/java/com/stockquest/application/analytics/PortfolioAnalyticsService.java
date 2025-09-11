package com.stockquest.application.analytics;

import com.stockquest.adapter.in.web.dto.*;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Portfolio Analytics Service
 * 포트폴리오 분석 서비스
 * 
 * 고급 금융 분석 및 위험 관리 기능 제공:
 * - 포트폴리오 종합 분석
 * - 위험 지표 계산 (VaR, CVaR, 베타, 샤프 비율 등)
 * - 성과 측정 및 벤치마크 비교
 * - AI 기반 포트폴리오 추천
 * - 자산 배분 최적화 제안
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioAnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final SessionRepository sessionRepository;
    private final MarketDataService marketDataService;
    private final RiskCalculationService riskCalculationService;

    /**
     * 포트폴리오 종합 분석 계산
     * 
     * @param sessionId 세션 ID
     * @param timeframe 분석 기간
     * @return 종합 분석 결과
     */
    @Cacheable(value = "portfolio-analytics", key = "#sessionId + '_' + #timeframe")
    public PortfolioAnalyticsResponse calculateComprehensiveAnalytics(Long sessionId, String timeframe) {
        log.info("포트폴리오 종합 분석 시작 - sessionId: {}, timeframe: {}", sessionId, timeframe);
        
        // 세션 및 포지션 정보 조회
        ChallengeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다: " + sessionId));
                
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            log.warn("포지션이 없는 세션 - sessionId: {}", sessionId);
            return createEmptyAnalytics(sessionId);
        }

        // 기본 메트릭 계산
        PortfolioMetrics basicMetrics = calculateBasicMetrics(positions);
        
        // 위험 지표 계산
        RiskMetrics riskMetrics = calculateRiskMetrics(positions, timeframe);
        
        // 성과 지표 계산
        PerformanceMetrics performanceMetrics = calculatePerformanceMetrics(positions, timeframe);
        
        // 자산 배분 분석
        AllocationAnalysis allocationAnalysis = calculateAllocationAnalysis(positions);
        
        // 추천사항 생성
        List<PortfolioRecommendationResponse> recommendations = generateIntelligentRecommendations(positions, basicMetrics, riskMetrics);

        PortfolioAnalyticsResponse response = PortfolioAnalyticsResponse.builder()
                .sessionId(sessionId)
                .analysisDate(LocalDateTime.now())
                .timeframe(timeframe)
                
                // 기본 메트릭
                .totalValue(basicMetrics.getTotalValue())
                .totalCost(basicMetrics.getTotalCost())
                .totalReturn(basicMetrics.getTotalReturn())
                .totalReturnPercent(basicMetrics.getTotalReturnPercent())
                .dailyChange(basicMetrics.getDailyChange())
                .dailyChangePercent(basicMetrics.getDailyChangePercent())
                
                // 위험 지표
                .volatility(riskMetrics.getVolatility())
                .portfolioBeta(riskMetrics.getBeta())
                .sharpeRatio(riskMetrics.getSharpeRatio())
                .sortinoRatio(riskMetrics.getSortinoRatio())
                .maxDrawdown(riskMetrics.getMaxDrawdown())
                .valueAtRisk(riskMetrics.getValueAtRisk())
                .expectedShortfall(riskMetrics.getExpectedShortfall())
                
                // 성과 지표
                .annualizedReturn(performanceMetrics.getAnnualizedReturn())
                .winRate(performanceMetrics.getWinRate())
                .profitFactor(performanceMetrics.getProfitFactor())
                .calmarRatio(performanceMetrics.getCalmarRatio())
                .informationRatio(performanceMetrics.getInformationRatio())
                
                // 배분 분석
                .sectorAllocation(allocationAnalysis.getSectorAllocation())
                .assetAllocation(allocationAnalysis.getAssetAllocation())
                .concentrationRisk(allocationAnalysis.getConcentrationRisk())
                .diversificationScore(allocationAnalysis.getDiversificationScore())
                
                // 추천사항
                .recommendations(recommendations)
                
                .build();

        log.info("포트폴리오 종합 분석 완료 - sessionId: {}, totalValue: {}, returnRate: {}%", 
                sessionId, response.getTotalValue(), response.getTotalReturnPercent());
                
        return response;
    }

    /**
     * 위험 분석 계산
     * 
     * @param sessionId 세션 ID
     * @param confidenceLevel VaR 신뢰구간
     * @return 위험 분석 결과
     */
    public PortfolioRiskAnalysisResponse calculateRiskAnalysis(Long sessionId, Double confidenceLevel) {
        log.info("포트폴리오 위험 분석 시작 - sessionId: {}, confidenceLevel: {}%", sessionId, confidenceLevel);
        
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            return PortfolioRiskAnalysisResponse.builder()
                    .sessionId(sessionId)
                    .confidenceLevel(confidenceLevel)
                    .analysisDate(LocalDateTime.now())
                    .build();
        }

        RiskMetrics riskMetrics = riskCalculationService.calculateAdvancedRiskMetrics(positions, confidenceLevel);
        
        return PortfolioRiskAnalysisResponse.builder()
                .sessionId(sessionId)
                .confidenceLevel(confidenceLevel)
                .analysisDate(LocalDateTime.now())
                .volatility(riskMetrics.getVolatility())
                .portfolioBeta(riskMetrics.getBeta())
                .valueAtRisk(riskMetrics.getValueAtRisk())
                .expectedShortfall(riskMetrics.getExpectedShortfall())
                .maxDrawdown(riskMetrics.getMaxDrawdown())
                .downside_deviation(riskMetrics.getDownsideDeviation())
                .trackingError(riskMetrics.getTrackingError())
                .informationRatio(riskMetrics.getInformationRatio())
                .riskContributions(calculateRiskContributions(positions, riskMetrics))
                .stressTestResults(performStressTests(positions))
                .build();
    }

    /**
     * 포트폴리오 추천사항 생성
     * 
     * @param sessionId 세션 ID
     * @param priority 최소 우선순위
     * @return 추천사항 목록
     */
    public List<PortfolioRecommendationResponse> generateRecommendations(Long sessionId, String priority) {
        log.info("포트폴리오 추천사항 생성 시작 - sessionId: {}, priority: {}", sessionId, priority);
        
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            return Collections.emptyList();
        }

        PortfolioMetrics basicMetrics = calculateBasicMetrics(positions);
        RiskMetrics riskMetrics = calculateRiskMetrics(positions, "1Y");
        
        List<PortfolioRecommendationResponse> recommendations = generateIntelligentRecommendations(positions, basicMetrics, riskMetrics);
        
        // 우선순위 필터링
        return recommendations.stream()
                .filter(rec -> isPriorityHigherOrEqual(rec.getPriority(), priority))
                .sorted((a, b) -> comparePriority(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * 섹터별 자산배분 계산
     * 
     * @param sessionId 세션 ID
     * @return 섹터별 배분 분석
     */
    public List<SectorAllocationResponse> calculateSectorAllocation(Long sessionId) {
        log.info("섹터별 분석 시작 - sessionId: {}", sessionId);
        
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            return Collections.emptyList();
        }

        return calculateSectorAllocationInternal(positions);
    }

    /**
     * 벤치마크와 성과 비교
     * 
     * @param sessionId 세션 ID
     * @param benchmarkSymbol 벤치마크 심볼
     * @param timeframe 분석 기간
     * @return 벤치마크 비교 결과
     */
    public BenchmarkComparisonResponse compareToBenchmark(Long sessionId, String benchmarkSymbol, String timeframe) {
        log.info("벤치마크 비교 시작 - sessionId: {}, benchmark: {}, timeframe: {}", sessionId, benchmarkSymbol, timeframe);
        
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            return BenchmarkComparisonResponse.builder()
                    .sessionId(sessionId)
                    .benchmarkSymbol(benchmarkSymbol)
                    .timeframe(timeframe)
                    .analysisDate(LocalDateTime.now())
                    .build();
        }

        // 포트폴리오 수익률 계산
        PerformanceMetrics portfolioPerformance = calculatePerformanceMetrics(positions, timeframe);
        
        // 벤치마크 수익률 조회
        BenchmarkData benchmarkData = marketDataService.getBenchmarkData(benchmarkSymbol, timeframe);
        
        // 알파, 베타, 정보비율 계산
        double alpha = portfolioPerformance.getAnnualizedReturn() - benchmarkData.getAnnualizedReturn();
        double beta = calculateBenchmarkBeta(positions, benchmarkData);
        double trackingError = calculateTrackingError(positions, benchmarkData);
        double informationRatio = trackingError != 0 ? alpha / trackingError : 0.0;
        
        return BenchmarkComparisonResponse.builder()
                .sessionId(sessionId)
                .benchmarkSymbol(benchmarkSymbol)
                .timeframe(timeframe)
                .analysisDate(LocalDateTime.now())
                .portfolioReturn(portfolioPerformance.getAnnualizedReturn())
                .benchmarkReturn(benchmarkData.getAnnualizedReturn())
                .alpha(alpha)
                .beta(beta)
                .trackingError(trackingError)
                .informationRatio(informationRatio)
                .correlationCoefficient(calculateCorrelation(positions, benchmarkData))
                .outperformanceDays(calculateOutperformanceDays(positions, benchmarkData))
                .build();
    }

    /**
     * 상관관계 매트릭스 계산
     * 
     * @param sessionId 세션 ID
     * @return 상관관계 매트릭스
     */
    public List<CorrelationResponse> calculateCorrelationMatrix(Long sessionId) {
        log.info("상관관계 매트릭스 계산 시작 - sessionId: {}", sessionId);
        
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.size() < 2) {
            return Collections.emptyList();
        }

        List<CorrelationResponse> correlations = new ArrayList<>();
        
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 1; j < positions.size(); j++) {
                PortfolioPosition pos1 = positions.get(i);
                PortfolioPosition pos2 = positions.get(j);
                
                double correlation = marketDataService.calculateCorrelation(
                        pos1.getInstrumentKey(), pos2.getInstrumentKey(), "1Y");
                
                correlations.add(CorrelationResponse.builder()
                        .symbol1(pos1.getInstrumentKey())
                        .symbol2(pos2.getInstrumentKey())
                        .correlation(correlation)
                        .significance(0.95) // 고정값, 실제로는 통계적 유의성 계산 필요
                        .build());
            }
        }
        
        return correlations;
    }

    /**
     * 포트폴리오 성과 이력 조회
     * 
     * @param sessionId 세션 ID
     * @param timeframe 조회 기간
     * @param interval 데이터 간격
     * @return 성과 이력 데이터
     */
    public List<PerformanceHistoryResponse> getPerformanceHistory(Long sessionId, String timeframe, String interval) {
        log.info("성과 이력 조회 시작 - sessionId: {}, timeframe: {}, interval: {}", sessionId, timeframe, interval);
        
        // 실제 구현에서는 이력 데이터를 조회하고 계산
        // 현재는 모의 데이터 반환
        return generateMockPerformanceHistory(sessionId, timeframe, interval);
    }

    // === Private Helper Methods ===

    private PortfolioAnalyticsResponse createEmptyAnalytics(Long sessionId) {
        return PortfolioAnalyticsResponse.builder()
                .sessionId(sessionId)
                .analysisDate(LocalDateTime.now())
                .totalValue(BigDecimal.ZERO)
                .totalCost(BigDecimal.ZERO)
                .totalReturn(BigDecimal.ZERO)
                .totalReturnPercent(0.0)
                .recommendations(Collections.emptyList())
                .build();
    }

    private PortfolioMetrics calculateBasicMetrics(List<PortfolioPosition> positions) {
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCost = positions.stream()
                .map(pos -> pos.getAveragePrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalReturn = totalValue.subtract(totalCost);
        double totalReturnPercent = totalCost.compareTo(BigDecimal.ZERO) != 0 
                ? totalReturn.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return PortfolioMetrics.builder()
                .totalValue(totalValue)
                .totalCost(totalCost)
                .totalReturn(totalReturn)
                .totalReturnPercent(totalReturnPercent)
                .dailyChange(BigDecimal.ZERO) // 실제 구현에서는 전일 대비 계산
                .dailyChangePercent(0.0)
                .build();
    }

    private RiskMetrics calculateRiskMetrics(List<PortfolioPosition> positions, String timeframe) {
        // 실제 구현에서는 이력 데이터를 사용하여 복잡한 위험 지표 계산
        return RiskMetrics.builder()
                .volatility(0.15) // 15% 연환산 변동성
                .beta(1.2) // 시장 대비 1.2배 민감도
                .sharpeRatio(1.5) // 샤프 비율
                .sortinoRatio(2.1) // 소르티노 비율
                .maxDrawdown(0.08) // 8% 최대 낙폭
                .valueAtRisk(0.05) // 5% VaR
                .expectedShortfall(0.07) // 7% CVaR
                .build();
    }

    private PerformanceMetrics calculatePerformanceMetrics(List<PortfolioPosition> positions, String timeframe) {
        // 실제 구현에서는 이력 데이터를 사용하여 성과 지표 계산
        return PerformanceMetrics.builder()
                .annualizedReturn(0.12) // 12% 연환산 수익률
                .winRate(0.65) // 65% 승률
                .profitFactor(1.8) // 1.8 수익 인수
                .calmarRatio(1.5) // 칼마 비율
                .informationRatio(0.8) // 정보 비율
                .build();
    }

    private AllocationAnalysis calculateAllocationAnalysis(List<PortfolioPosition> positions) {
        List<SectorAllocationResponse> sectorAllocation = calculateSectorAllocationInternal(positions);
        
        return AllocationAnalysis.builder()
                .sectorAllocation(sectorAllocation)
                .assetAllocation(Collections.emptyList()) // 실제 구현 필요
                .concentrationRisk(0.25) // 25% 집중 위험
                .diversificationScore(0.8) // 80% 분산 점수
                .build();
    }

    private List<SectorAllocationResponse> calculateSectorAllocationInternal(List<PortfolioPosition> positions) {
        // 섹터별 그룹화 및 계산 (실제로는 종목 마스터 데이터에서 섹터 정보 조회)
        Map<String, List<PortfolioPosition>> sectorGroups = positions.stream()
                .collect(Collectors.groupingBy(pos -> 
                        // 실제로는 종목 마스터에서 섹터 정보 조회
                        inferSectorFromSymbol(pos.getInstrumentKey())
                ));
        
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return sectorGroups.entrySet().stream()
                .map(entry -> {
                    String sector = entry.getKey();
                    List<PortfolioPosition> sectorPositions = entry.getValue();
                    
                    BigDecimal sectorValue = sectorPositions.stream()
                            .map(pos -> pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    double percentage = totalValue.compareTo(BigDecimal.ZERO) != 0
                            ? sectorValue.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;
                    
                    return SectorAllocationResponse.builder()
                            .sector(sector)
                            .value(sectorValue)
                            .percentage(percentage)
                            .positionCount(sectorPositions.size())
                            .targetPercentage(getTargetSectorAllocation(sector))
                            .build();
                })
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
    }

    private List<PortfolioRecommendationResponse> generateIntelligentRecommendations(
            List<PortfolioPosition> positions, PortfolioMetrics metrics, RiskMetrics riskMetrics) {
        
        List<PortfolioRecommendationResponse> recommendations = new ArrayList<>();
        
        // 집중도 위험 체크
        if (checkConcentrationRisk(positions)) {
            recommendations.add(PortfolioRecommendationResponse.builder()
                    .type("DIVERSIFY")
                    .priority("HIGH")
                    .title("포지션 집중 위험 완화")
                    .description("일부 종목이 포트폴리오의 과도한 비중을 차지하고 있습니다.")
                    .action("집중된 포지션의 크기를 줄이거나 다른 섹터로 분산하세요.")
                    .impact("위험 감소 및 안정적인 수익 창출")
                    .reasoning("과도한 집중은 개별 종목 위험에 노출을 증가시킵니다.")
                    .affectedSymbols(getConcentratedSymbols(positions))
                    .build());
        }
        
        // 수익 실현 기회 체크
        List<String> profitableSymbols = getProfitableSymbols(positions, 0.3); // 30% 이상 수익
        if (!profitableSymbols.isEmpty()) {
            recommendations.add(PortfolioRecommendationResponse.builder()
                    .type("TAKE_PROFIT")
                    .priority("MEDIUM")
                    .title("수익 실현 기회")
                    .description(String.format("%d개 종목이 높은 수익률을 기록하고 있습니다.", profitableSymbols.size()))
                    .action("일부 수익을 실현하여 위험을 줄이고 재투자 기회를 모색하세요.")
                    .impact("수익 확정 및 위험 관리")
                    .reasoning("높은 수익률은 과평가 위험을 내포할 수 있습니다.")
                    .affectedSymbols(profitableSymbols)
                    .build());
        }
        
        return recommendations;
    }

    // 추가 헬퍼 메서드들...
    private String inferSectorFromSymbol(String symbol) {
        // 실제로는 종목 마스터 데이터에서 조회
        if (symbol.startsWith("A")) return "Technology";
        if (symbol.startsWith("B")) return "Financial";
        return "Industrial";
    }
    
    private double getTargetSectorAllocation(String sector) {
        Map<String, Double> targets = Map.of(
                "Technology", 25.0,
                "Financial", 20.0,
                "Healthcare", 15.0,
                "Industrial", 15.0,
                "Consumer", 10.0
        );
        return targets.getOrDefault(sector, 10.0);
    }
    
    private boolean checkConcentrationRisk(List<PortfolioPosition> positions) {
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return positions.stream()
                .anyMatch(pos -> {
                    BigDecimal positionValue = pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity()));
                    double percentage = positionValue.multiply(BigDecimal.valueOf(100))
                            .divide(totalValue, 2, RoundingMode.HALF_UP).doubleValue();
                    return percentage > 20.0; // 20% 이상이면 집중 위험
                });
    }
    
    private List<String> getConcentratedSymbols(List<PortfolioPosition> positions) {
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return positions.stream()
                .filter(pos -> {
                    BigDecimal positionValue = pos.getCurrentPrice().multiply(BigDecimal.valueOf(pos.getQuantity()));
                    double percentage = positionValue.multiply(BigDecimal.valueOf(100))
                            .divide(totalValue, 2, RoundingMode.HALF_UP).doubleValue();
                    return percentage > 20.0;
                })
                .map(PortfolioPosition::getInstrumentKey)
                .collect(Collectors.toList());
    }
    
    private List<String> getProfitableSymbols(List<PortfolioPosition> positions, double threshold) {
        return positions.stream()
                .filter(pos -> {
                    double returnPercent = pos.getCurrentPrice().subtract(pos.getAveragePrice())
                            .divide(pos.getAveragePrice(), 4, RoundingMode.HALF_UP).doubleValue();
                    return returnPercent > threshold;
                })
                .map(PortfolioPosition::getInstrumentKey)
                .collect(Collectors.toList());
    }

    private boolean isPriorityHigherOrEqual(String priority1, String priority2) {
        int p1 = getPriorityValue(priority1);
        int p2 = getPriorityValue(priority2);
        return p1 >= p2;
    }

    private int comparePriority(String priority1, String priority2) {
        return Integer.compare(getPriorityValue(priority1), getPriorityValue(priority2));
    }

    private int getPriorityValue(String priority) {
        return switch (priority) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    // 나머지 메서드들은 실제 구현에서 완성 필요...
    private List<RiskContributionResponse> calculateRiskContributions(List<PortfolioPosition> positions, RiskMetrics riskMetrics) {
        return Collections.emptyList();
    }

    private List<StressTestResponse> performStressTests(List<PortfolioPosition> positions) {
        return Collections.emptyList();
    }

    private double calculateBenchmarkBeta(List<PortfolioPosition> positions, BenchmarkData benchmarkData) {
        return 1.0;
    }

    private double calculateTrackingError(List<PortfolioPosition> positions, BenchmarkData benchmarkData) {
        return 0.05;
    }

    private double calculateCorrelation(List<PortfolioPosition> positions, BenchmarkData benchmarkData) {
        return 0.8;
    }

    private int calculateOutperformanceDays(List<PortfolioPosition> positions, BenchmarkData benchmarkData) {
        return 180;
    }

    private List<PerformanceHistoryResponse> generateMockPerformanceHistory(Long sessionId, String timeframe, String interval) {
        return Collections.emptyList();
    }
}