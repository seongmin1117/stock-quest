package com.stockquest.application.service;

import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import com.stockquest.domain.execution.Trade.ExecutionGrade;
import com.stockquest.domain.execution.Trade.ExecutionQualityMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 실행 분석 및 베스트 실행 모니터링 서비스
 * Phase 8.4: Real-time Execution Engine - Execution Analytics & Best Execution Monitoring
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionAnalyticsService {

    private final Map<String, ExecutionMetrics> dailyMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<Trade>> executionHistory = new ConcurrentHashMap<>();
    private final Map<String, BestExecutionReport> dailyReports = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    
    // Monitoring thresholds
    private static final BigDecimal EXECUTION_DELAY_THRESHOLD = BigDecimal.valueOf(500); // 500ms
    private static final BigDecimal MARKET_IMPACT_THRESHOLD = BigDecimal.valueOf(0.005); // 0.5%
    private static final BigDecimal QUALITY_DEGRADATION_THRESHOLD = BigDecimal.valueOf(0.8); // 80%
    private static final BigDecimal COMPLIANCE_THRESHOLD = BigDecimal.valueOf(0.95); // 95%
    
    /**
     * 거래 실행 품질 분석
     */
    public CompletableFuture<ExecutionAnalysis> analyzeTradeExecution(Trade trade) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("거래 실행 품질 분석: {}", trade.getTradeId());
            
            ExecutionAnalysis analysis = ExecutionAnalysis.builder()
                .tradeId(trade.getTradeId())
                .symbol(trade.getSymbol())
                .analysisTime(LocalDateTime.now())
                .build();
            
            // 1. 슬리피지 분석
            BigDecimal slippage = calculateSlippage(trade);
            analysis.setSlippage(slippage);
            
            // 2. 시장 영향 분석
            BigDecimal marketImpact = calculateMarketImpact(trade);
            analysis.setMarketImpact(marketImpact);
            
            // 3. 타이밍 비용 분석
            BigDecimal timingCost = calculateTimingCost(trade);
            analysis.setTimingCost(timingCost);
            
            // 4. 총 실행 비용 계산
            BigDecimal totalCost = trade.getTotalTradingCost();
            analysis.setTotalExecutionCost(totalCost);
            
            // 5. 벤치마크 대비 성과
            BigDecimal benchmarkPerformance = calculateBenchmarkPerformance(trade);
            analysis.setBenchmarkPerformance(benchmarkPerformance);
            
            // 6. 실행 품질 등급
            ExecutionGrade grade = trade.calculateExecutionGrade();
            analysis.setExecutionGrade(grade);
            
            // 7. 실행 히스토리에 추가
            addToExecutionHistory(trade);
            
            // 8. 일일 메트릭 업데이트
            updateDailyMetrics(trade, analysis);
            
            return analysis;
        });
    }
    
    /**
     * 주문 실행 품질 종합 분석
     */
    public CompletableFuture<OrderExecutionAnalysis> analyzeOrderExecution(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("주문 실행 품질 종합 분석: {}", order.getOrderId());
            
            OrderExecutionAnalysis analysis = OrderExecutionAnalysis.builder()
                .orderId(order.getOrderId())
                .symbol(order.getSymbol())
                .orderType(order.getOrderType())
                .executionAlgorithm(order.getExecutionAlgorithm())
                .analysisTime(LocalDateTime.now())
                .build();
            
            if (order.getTrades() == null || order.getTrades().isEmpty()) {
                analysis.setStatus("No trades executed");
                return analysis;
            }
            
            List<Trade> trades = order.getTrades();
            
            // 1. 실행 통계
            analysis.setTotalTrades(trades.size());
            analysis.setTotalQuantity(order.getExecutedQuantity());
            analysis.setAverageExecutionPrice(order.getAvgExecutionPrice());
            analysis.setFillRate(order.getFillRate());
            
            // 2. 실행 품질 메트릭
            BigDecimal avgSlippage = calculateAverageSlippage(trades);
            BigDecimal avgMarketImpact = calculateAverageMarketImpact(trades);
            BigDecimal totalTimingCost = calculateTotalTimingCost(trades);
            
            analysis.setAverageSlippage(avgSlippage);
            analysis.setAverageMarketImpact(avgMarketImpact);
            analysis.setTotalTimingCost(totalTimingCost);
            
            // 3. 실행 속도 분석
            Long executionTime = calculateTotalExecutionTime(order);
            analysis.setTotalExecutionTimeMs(executionTime);
            
            // 4. 알고리즘 성과 평가
            BigDecimal algorithmPerformance = evaluateAlgorithmPerformance(order);
            analysis.setAlgorithmPerformance(algorithmPerformance);
            
            // 5. 전체 실행 등급
            ExecutionGrade overallGrade = calculateOverallExecutionGrade(trades);
            analysis.setOverallGrade(overallGrade);
            
            return analysis;
        });
    }
    
    /**
     * 일일 베스트 실행 리포트 생성
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI") // 평일 오후 6시
    public void generateDailyBestExecutionReport() {
        String date = LocalDate.now().toString();
        
        log.info("일일 베스트 실행 리포트 생성 시작: {}", date);
        
        CompletableFuture.supplyAsync(() -> {
            BestExecutionReport report = BestExecutionReport.builder()
                .reportDate(LocalDate.now())
                .generationTime(LocalDateTime.now())
                .build();
            
            ExecutionMetrics todayMetrics = dailyMetrics.get(date);
            if (todayMetrics == null) {
                report.setStatus("No execution data available");
                dailyReports.put(date, report);
                return report;
            }
            
            // 1. 실행 통계 요약
            report.setTotalTrades(todayMetrics.getTotalTrades());
            report.setTotalVolume(todayMetrics.getTotalVolume());
            report.setTotalNotionalValue(todayMetrics.getTotalNotionalValue());
            
            // 2. 실행 품질 지표
            report.setAverageSlippage(todayMetrics.getAverageSlippage());
            report.setAverageMarketImpact(todayMetrics.getAverageMarketImpact());
            report.setAverageExecutionSpeed(todayMetrics.getAverageExecutionSpeed());
            
            // 3. 베스트 실행 준수율
            BigDecimal bestExecutionRate = calculateBestExecutionComplianceRate(todayMetrics);
            report.setBestExecutionComplianceRate(bestExecutionRate);
            
            // 4. 알고리즘 성과 순위
            Map<String, BigDecimal> algorithmRanking = rankAlgorithmPerformance(date);
            report.setAlgorithmPerformanceRanking(algorithmRanking);
            
            // 5. 실행 장소 분석
            Map<String, BigDecimal> venueAnalysis = analyzeExecutionVenues(date);
            report.setVenueAnalysis(venueAnalysis);
            
            // 6. 리스크 지표
            report.setRiskAdjustedReturn(todayMetrics.getRiskAdjustedReturn());
            report.setMaximumDrawdown(todayMetrics.getMaxDrawdown());
            
            // 7. 권고사항
            List<String> recommendations = generateRecommendations(todayMetrics);
            report.setRecommendations(recommendations);
            
            dailyReports.put(date, report);
            
            log.info("일일 베스트 실행 리포트 생성 완료: {}", date);
            
            // 베스트 실행 리포트 알림 발송
            sendBestExecutionReportNotification(report);
            
            return report;
        });
    }
    
    /**
     * 베스트 실행 모니터링 (실시간)
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    public void monitorBestExecution() {
        String currentTime = LocalDateTime.now().toString().substring(0, 16);
        
        CompletableFuture.runAsync(() -> {
            log.debug("베스트 실행 모니터링 수행: {}", currentTime);
            
            // 1. 최근 거래 품질 검사
            checkRecentTradeQuality();
            
            // 2. 실행 지연 모니터링
            monitorExecutionDelays();
            
            // 3. 시장 영향 임계값 검사
            checkMarketImpactThresholds();
            
            // 4. 알고리즘 성과 모니터링
            monitorAlgorithmPerformance();
            
            // 5. 규제 준수 모니터링
            monitorRegulatoryCompliance();
        });
    }
    
    /**
     * 실행 품질 벤치마크 분석
     */
    public CompletableFuture<BenchmarkAnalysis> analyzeBenchmarkPerformance(
        String symbol, LocalDate startDate, LocalDate endDate) {
        
        return CompletableFuture.supplyAsync(() -> {
            log.info("실행 품질 벤치마크 분석: {} ({} ~ {})", symbol, startDate, endDate);
            
            BenchmarkAnalysis analysis = BenchmarkAnalysis.builder()
                .symbol(symbol)
                .startDate(startDate)
                .endDate(endDate)
                .analysisTime(LocalDateTime.now())
                .build();
            
            List<Trade> periodTrades = getTradesForPeriod(symbol, startDate, endDate);
            
            if (periodTrades.isEmpty()) {
                analysis.setStatus("No trades found for the period");
                return analysis;
            }
            
            // 1. VWAP 대비 성과
            BigDecimal vwapPerformance = calculateVWAPPerformance(periodTrades);
            analysis.setVwapPerformance(vwapPerformance);
            
            // 2. Arrival Price 대비 성과
            BigDecimal arrivalPricePerformance = calculateArrivalPricePerformance(periodTrades);
            analysis.setArrivalPricePerformance(arrivalPricePerformance);
            
            // 3. Implementation Shortfall 성과
            BigDecimal implementationShortfall = calculateImplementationShortfall(periodTrades);
            analysis.setImplementationShortfall(implementationShortfall);
            
            // 4. 시장 상대 성과
            BigDecimal marketRelativePerformance = calculateMarketRelativePerformance(periodTrades);
            analysis.setMarketRelativePerformance(marketRelativePerformance);
            
            // 5. 실행 효율성 점수
            BigDecimal efficiencyScore = calculateExecutionEfficiency(periodTrades);
            analysis.setExecutionEfficiencyScore(efficiencyScore);
            
            return analysis;
        });
    }
    
    /**
     * 거래 실행 히스토리 조회
     */
    public List<ExecutionAnalysis> getExecutionHistory(String symbol, int days) {
        return executionHistory.getOrDefault(symbol, List.of()).stream()
            .map(this::createExecutionAnalysisFromTrade)
            .filter(analysis -> analysis.getAnalysisTime().isAfter(
                LocalDateTime.now().minusDays(days)))
            .sorted((a, b) -> b.getAnalysisTime().compareTo(a.getAnalysisTime()))
            .collect(Collectors.toList());
    }
    
    /**
     * 베스트 실행 준수 상태 조회
     */
    public CompletableFuture<BestExecutionStatus> getBestExecutionStatus() {
        return CompletableFuture.supplyAsync(() -> {
            String today = LocalDate.now().toString();
            ExecutionMetrics todayMetrics = dailyMetrics.get(today);
            
            BestExecutionStatus status = BestExecutionStatus.builder()
                .reportDate(LocalDate.now())
                .statusTime(LocalDateTime.now())
                .build();
            
            if (todayMetrics == null) {
                status.setOverallStatus("NO_DATA");
                return status;
            }
            
            // 1. 전반적 준수 상태
            BigDecimal complianceRate = calculateBestExecutionComplianceRate(todayMetrics);
            status.setComplianceRate(complianceRate);
            
            if (complianceRate.compareTo(BigDecimal.valueOf(95)) >= 0) {
                status.setOverallStatus("EXCELLENT");
            } else if (complianceRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
                status.setOverallStatus("GOOD");
            } else if (complianceRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
                status.setOverallStatus("FAIR");
            } else {
                status.setOverallStatus("POOR");
            }
            
            // 2. 주요 지표 상태
            status.setSlippageStatus(getSlippageStatus(todayMetrics.getAverageSlippage()));
            status.setMarketImpactStatus(getMarketImpactStatus(todayMetrics.getAverageMarketImpact()));
            status.setExecutionSpeedStatus(getExecutionSpeedStatus(todayMetrics.getAverageExecutionSpeed()));
            
            return status;
        });
    }
    
    // Private Helper Methods
    
    private BigDecimal calculateSlippage(Trade trade) {
        // TODO: 실제 벤치마크 가격과 비교한 슬리피지 계산
        return BigDecimal.valueOf(0.001); // 0.1% 임시 값
    }
    
    private BigDecimal calculateMarketImpact(Trade trade) {
        if (trade.getExecutionMetrics() != null) {
            return trade.getExecutionMetrics().getMarketImpact();
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateTimingCost(Trade trade) {
        if (trade.getExecutionMetrics() != null) {
            return trade.getExecutionMetrics().getTimingCost();
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateBenchmarkPerformance(Trade trade) {
        if (trade.getExecutionMetrics() != null) {
            return trade.getExecutionMetrics().getArrivalPricePerformance();
        }
        return BigDecimal.ZERO;
    }
    
    private void addToExecutionHistory(Trade trade) {
        executionHistory.computeIfAbsent(trade.getSymbol(), k -> new ArrayList<>())
            .add(trade);
        
        // 히스토리 크기 제한 (심볼당 최대 1000건)
        List<Trade> history = executionHistory.get(trade.getSymbol());
        if (history.size() > 1000) {
            history.remove(0);
        }
    }
    
    private void updateDailyMetrics(Trade trade, ExecutionAnalysis analysis) {
        String date = LocalDate.now().toString();
        ExecutionMetrics metrics = dailyMetrics.computeIfAbsent(date, 
            k -> new ExecutionMetrics());
        
        metrics.incrementTotalTrades();
        metrics.addVolume(trade.getQuantity());
        metrics.addNotionalValue(trade.getAmount());
        metrics.addSlippage(analysis.getSlippage());
        metrics.addMarketImpact(analysis.getMarketImpact());
        
        if (trade.getExecutionMetrics() != null && 
            trade.getExecutionMetrics().getExecutionSpeed() != null) {
            metrics.addExecutionSpeed(trade.getExecutionMetrics().getExecutionSpeed());
        }
    }
    
    // Calculation methods
    
    private BigDecimal calculateAverageSlippage(List<Trade> trades) {
        return trades.stream()
            .map(this::calculateSlippage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(trades.size()), 6, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateAverageMarketImpact(List<Trade> trades) {
        return trades.stream()
            .map(this::calculateMarketImpact)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(trades.size()), 6, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateTotalTimingCost(List<Trade> trades) {
        return trades.stream()
            .map(this::calculateTimingCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Long calculateTotalExecutionTime(Order order) {
        if (order.getExecutionStats() != null) {
            return order.getExecutionStats().getTotalExecutionTime();
        }
        return 0L;
    }
    
    private BigDecimal evaluateAlgorithmPerformance(Order order) {
        // TODO: 알고리즘 성과 평가 로직
        return BigDecimal.valueOf(0.85); // 85% 임시 성과
    }
    
    private ExecutionGrade calculateOverallExecutionGrade(List<Trade> trades) {
        BigDecimal avgQuality = trades.stream()
            .map(trade -> {
                if (trade.getExecutionMetrics() != null && 
                    trade.getExecutionMetrics().getExecutionEfficiency() != null) {
                    return trade.getExecutionMetrics().getExecutionEfficiency();
                }
                return BigDecimal.valueOf(70); // 기본값
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(trades.size()), 2, RoundingMode.HALF_UP);
        
        return ExecutionGrade.fromScore(avgQuality);
    }
    
    // Monitoring methods
    
    private void checkRecentTradeQuality() {
        log.debug("최근 거래 품질 검사 수행");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            
            // 최근 1시간 거래 조회
            List<Trade> recentTrades = executionHistory.values().stream()
                .flatMap(List::stream)
                .filter(trade -> trade.getTradeTime().isAfter(cutoffTime))
                .collect(Collectors.toList());
            
            if (recentTrades.isEmpty()) {
                return;
            }
            
            // 거래 품질 점수 계산 (실행 효율성 기반)
            double averageQuality = recentTrades.stream()
                .mapToDouble(trade -> {
                    ExecutionQualityMetrics metrics = trade.getExecutionMetrics();
                    if (metrics != null && metrics.getExecutionEfficiency() != null) {
                        return metrics.getExecutionEfficiency().doubleValue() / 100.0; // 0-1 스케일로 변환
                    }
                    return 0.8; // 기본값
                })
                .average()
                .orElse(0.8);
            
            // 품질 저하 감지
            if (averageQuality < QUALITY_DEGRADATION_THRESHOLD.doubleValue()) {
                ExecutionAlert alert = ExecutionAlert.builder()
                    .alertType(ExecutionAlertType.QUALITY_DEGRADATION)
                    .severity("HIGH")
                    .message(String.format("거래 품질 저하 감지: 평균 품질 %.2f%% (임계값: %.1f%%)", 
                        averageQuality * 100, QUALITY_DEGRADATION_THRESHOLD.doubleValue() * 100))
                    .affectedTrades(recentTrades.size())
                    .timestamp(LocalDateTime.now())
                    .build();
                
                sendExecutionAlert(alert);
            }
            
        } catch (Exception e) {
            log.error("거래 품질 검사 중 오류 발생", e);
        }
    }
    
    private void monitorExecutionDelays() {
        log.debug("실행 지연 모니터링 수행");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
            
            // 최근 30분 거래 중 지연이 발생한 거래 조회 (실행 속도 기반)
            List<Trade> delayedTrades = executionHistory.values().stream()
                .flatMap(List::stream)
                .filter(trade -> trade.getTradeTime().isAfter(cutoffTime))
                .filter(trade -> {
                    ExecutionQualityMetrics metrics = trade.getExecutionMetrics();
                    if (metrics != null && metrics.getExecutionSpeed() != null) {
                        return metrics.getExecutionSpeed() > EXECUTION_DELAY_THRESHOLD.longValue();
                    }
                    return false;
                })
                .collect(Collectors.toList());
            
            if (!delayedTrades.isEmpty()) {
                double averageDelay = delayedTrades.stream()
                    .mapToLong(trade -> {
                        ExecutionQualityMetrics metrics = trade.getExecutionMetrics();
                        return metrics != null && metrics.getExecutionSpeed() != null ? 
                            metrics.getExecutionSpeed() : 0L;
                    })
                    .average()
                    .orElse(0.0);
                
                ExecutionAlert alert = ExecutionAlert.builder()
                    .alertType(ExecutionAlertType.EXECUTION_DELAY)
                    .severity("MEDIUM")
                    .message(String.format("실행 지연 감지: %d건 거래, 평균 지연 %.0fms (임계값: %dms)", 
                        delayedTrades.size(), averageDelay, EXECUTION_DELAY_THRESHOLD.longValue()))
                    .affectedTrades(delayedTrades.size())
                    .timestamp(LocalDateTime.now())
                    .build();
                
                sendExecutionAlert(alert);
            }
            
        } catch (Exception e) {
            log.error("실행 지연 모니터링 중 오류 발생", e);
        }
    }
    
    private void checkMarketImpactThresholds() {
        log.debug("시장 영향 임계값 검사 수행");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
            
            // 최근 2시간 거래 중 시장 영향이 높은 거래 조회
            List<Trade> highImpactTrades = executionHistory.values().stream()
                .flatMap(List::stream)
                .filter(trade -> trade.getTradeTime().isAfter(cutoffTime))
                .filter(trade -> {
                    ExecutionQualityMetrics metrics = trade.getExecutionMetrics();
                    if (metrics != null && metrics.getMarketImpact() != null) {
                        return metrics.getMarketImpact().abs().compareTo(MARKET_IMPACT_THRESHOLD) > 0;
                    }
                    return false;
                })
                .collect(Collectors.toList());
            
            if (!highImpactTrades.isEmpty()) {
                BigDecimal totalImpact = highImpactTrades.stream()
                    .map(trade -> trade.getExecutionMetrics().getMarketImpact().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal averageImpact = totalImpact.divide(BigDecimal.valueOf(highImpactTrades.size()), 4, RoundingMode.HALF_UP);
                
                ExecutionAlert alert = ExecutionAlert.builder()
                    .alertType(ExecutionAlertType.HIGH_MARKET_IMPACT)
                    .severity("HIGH")
                    .message(String.format("높은 시장 영향 감지: %d건 거래, 평균 시장영향 %.2f%% (임계값: %.1f%%)", 
                        highImpactTrades.size(), 
                        averageImpact.multiply(BigDecimal.valueOf(100)).doubleValue(),
                        MARKET_IMPACT_THRESHOLD.multiply(BigDecimal.valueOf(100)).doubleValue()))
                    .affectedTrades(highImpactTrades.size())
                    .timestamp(LocalDateTime.now())
                    .build();
                
                sendExecutionAlert(alert);
            }
            
        } catch (Exception e) {
            log.error("시장 영향 임계값 검사 중 오류 발생", e);
        }
    }
    
    private void monitorAlgorithmPerformance() {
        log.debug("알고리즘 성과 모니터링 수행");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(4);
            String today = LocalDate.now().toString();
            
            // 오늘의 알고리즘별 성과 분석
            Map<String, BigDecimal> algorithmPerformance = rankAlgorithmPerformance(today);
            
            // 성과 저하 알고리즘 감지
            List<String> underperformingAlgos = algorithmPerformance.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.valueOf(70)) < 0) // 70% 미만
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            if (!underperformingAlgos.isEmpty()) {
                ExecutionAlert alert = ExecutionAlert.builder()
                    .alertType(ExecutionAlertType.ALGORITHM_UNDERPERFORMANCE)
                    .severity("MEDIUM")
                    .message(String.format("알고리즘 성과 저하 감지: %s (성과: %s)", 
                        String.join(", ", underperformingAlgos),
                        underperformingAlgos.stream()
                            .map(algo -> algo + "=" + algorithmPerformance.get(algo) + "%")
                            .collect(Collectors.joining(", "))))
                    .affectedTrades(0) // 알고리즘 전체 영향
                    .timestamp(LocalDateTime.now())
                    .build();
                
                sendExecutionAlert(alert);
            }
            
            // 최고 성과 알고리즘 정보 로깅
            algorithmPerformance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> log.info("최고 성과 알고리즘: {} ({}%)", 
                    entry.getKey(), entry.getValue()));
            
        } catch (Exception e) {
            log.error("알고리즘 성과 모니터링 중 오류 발생", e);
        }
    }
    
    private void monitorRegulatoryCompliance() {
        log.debug("규제 준수 모니터링 수행");
        
        try {
            String today = LocalDate.now().toString();
            ExecutionMetrics todayMetrics = dailyMetrics.get(today);
            
            if (todayMetrics == null) {
                return;
            }
            
            // 베스트 실행 준수율 확인
            BigDecimal complianceRate = calculateBestExecutionComplianceRate(todayMetrics);
            
            if (complianceRate.compareTo(COMPLIANCE_THRESHOLD) < 0) {
                ExecutionAlert alert = ExecutionAlert.builder()
                    .alertType(ExecutionAlertType.COMPLIANCE_BREACH)
                    .severity("HIGH")
                    .message(String.format("베스트 실행 준수율 미달: %.1f%% (임계값: %.1f%%)", 
                        complianceRate.doubleValue(), 
                        COMPLIANCE_THRESHOLD.multiply(BigDecimal.valueOf(100)).doubleValue()))
                    .affectedTrades(todayMetrics.getTotalTrades())
                    .timestamp(LocalDateTime.now())
                    .build();
                
                sendExecutionAlert(alert);
            }
            
            // 추가 규제 준수 체크
            checkTradeReporting(todayMetrics);
            checkPositionLimits(todayMetrics);
            
        } catch (Exception e) {
            log.error("규제 준수 모니터링 중 오류 발생", e);
        }
    }
    
    private void checkTradeReporting(ExecutionMetrics metrics) {
        // 거래 보고 준수 확인 (단순화된 구현)
        if (metrics.getTotalTrades() > 0 && metrics.getReportingCompliance().compareTo(COMPLIANCE_THRESHOLD) < 0) {
            log.warn("거래 보고 준수율 저하: {}%", metrics.getReportingCompliance().multiply(BigDecimal.valueOf(100)));
        }
    }
    
    private void checkPositionLimits(ExecutionMetrics metrics) {
        // 포지션 한도 준수 확인 (단순화된 구현)
        if (metrics.getPositionLimitCompliance().compareTo(COMPLIANCE_THRESHOLD) < 0) {
            log.warn("포지션 한도 준수율 저하: {}%", metrics.getPositionLimitCompliance().multiply(BigDecimal.valueOf(100)));
        }
    }
    
    // Report generation methods
    
    private BigDecimal calculateBestExecutionComplianceRate(ExecutionMetrics metrics) {
        return BigDecimal.valueOf(92); // 92% 임시 준수율
    }
    
    private Map<String, BigDecimal> rankAlgorithmPerformance(String date) {
        Map<String, BigDecimal> ranking = new HashMap<>();
        ranking.put("VWAP", BigDecimal.valueOf(95));
        ranking.put("TWAP", BigDecimal.valueOf(88));
        ranking.put("IS", BigDecimal.valueOf(82));
        return ranking;
    }
    
    private Map<String, BigDecimal> analyzeExecutionVenues(String date) {
        Map<String, BigDecimal> analysis = new HashMap<>();
        analysis.put("KRX", BigDecimal.valueOf(85));
        analysis.put("KOSDAQ", BigDecimal.valueOf(78));
        return analysis;
    }
    
    private List<String> generateRecommendations(ExecutionMetrics metrics) {
        List<String> recommendations = new ArrayList<>();
        
        if (metrics.getAverageSlippage().compareTo(BigDecimal.valueOf(0.002)) > 0) {
            recommendations.add("슬리피지 개선을 위해 알고리즘 파라미터 조정 고려");
        }
        
        if (metrics.getAverageMarketImpact().compareTo(BigDecimal.valueOf(0.01)) > 0) {
            recommendations.add("시장 영향 감소를 위해 실행 속도 조절 필요");
        }
        
        return recommendations;
    }
    
    // Utility methods
    
    private List<Trade> getTradesForPeriod(String symbol, LocalDate start, LocalDate end) {
        return executionHistory.getOrDefault(symbol, List.of()).stream()
            .filter(trade -> !trade.getTradeTime().toLocalDate().isBefore(start) &&
                           !trade.getTradeTime().toLocalDate().isAfter(end))
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateVWAPPerformance(List<Trade> trades) {
        return BigDecimal.valueOf(0.95); // 95% VWAP 성과
    }
    
    private BigDecimal calculateArrivalPricePerformance(List<Trade> trades) {
        return BigDecimal.valueOf(0.88); // 88% Arrival Price 성과
    }
    
    private BigDecimal calculateImplementationShortfall(List<Trade> trades) {
        return BigDecimal.valueOf(0.003); // 0.3% Implementation Shortfall
    }
    
    private BigDecimal calculateMarketRelativePerformance(List<Trade> trades) {
        return BigDecimal.valueOf(1.05); // 105% 시장 상대 성과
    }
    
    private BigDecimal calculateExecutionEfficiency(List<Trade> trades) {
        return BigDecimal.valueOf(87); // 87% 실행 효율성
    }
    
    private ExecutionAnalysis createExecutionAnalysisFromTrade(Trade trade) {
        return ExecutionAnalysis.builder()
            .tradeId(trade.getTradeId())
            .symbol(trade.getSymbol())
            .analysisTime(trade.getTradeTime())
            .slippage(calculateSlippage(trade))
            .marketImpact(calculateMarketImpact(trade))
            .executionGrade(trade.calculateExecutionGrade())
            .build();
    }
    
    private String getSlippageStatus(BigDecimal avgSlippage) {
        if (avgSlippage.compareTo(BigDecimal.valueOf(0.001)) <= 0) {
            return "EXCELLENT";
        } else if (avgSlippage.compareTo(BigDecimal.valueOf(0.002)) <= 0) {
            return "GOOD";
        } else {
            return "NEEDS_IMPROVEMENT";
        }
    }
    
    private String getMarketImpactStatus(BigDecimal avgMarketImpact) {
        if (avgMarketImpact.compareTo(BigDecimal.valueOf(0.005)) <= 0) {
            return "LOW";
        } else if (avgMarketImpact.compareTo(BigDecimal.valueOf(0.01)) <= 0) {
            return "MODERATE";
        } else {
            return "HIGH";
        }
    }
    
    private String getExecutionSpeedStatus(BigDecimal avgSpeed) {
        if (avgSpeed.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return "FAST";
        } else if (avgSpeed.compareTo(BigDecimal.valueOf(5000)) <= 0) {
            return "NORMAL";
        } else {
            return "SLOW";
        }
    }
    
    // Data classes
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutionAnalysis {
        private String tradeId;
        private String symbol;
        private LocalDateTime analysisTime;
        private BigDecimal slippage;
        private BigDecimal marketImpact;
        private BigDecimal timingCost;
        private BigDecimal totalExecutionCost;
        private BigDecimal benchmarkPerformance;
        private ExecutionGrade executionGrade;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderExecutionAnalysis {
        private String orderId;
        private String symbol;
        private Order.OrderType orderType;
        private Order.ExecutionAlgorithm executionAlgorithm;
        private LocalDateTime analysisTime;
        private String status;
        
        private Integer totalTrades;
        private BigDecimal totalQuantity;
        private BigDecimal averageExecutionPrice;
        private BigDecimal fillRate;
        
        private BigDecimal averageSlippage;
        private BigDecimal averageMarketImpact;
        private BigDecimal totalTimingCost;
        private Long totalExecutionTimeMs;
        private BigDecimal algorithmPerformance;
        private ExecutionGrade overallGrade;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BestExecutionReport {
        private LocalDate reportDate;
        private LocalDateTime generationTime;
        private String status;
        
        private Integer totalTrades;
        private BigDecimal totalVolume;
        private BigDecimal totalNotionalValue;
        
        private BigDecimal averageSlippage;
        private BigDecimal averageMarketImpact;
        private BigDecimal averageExecutionSpeed;
        
        private BigDecimal bestExecutionComplianceRate;
        private Map<String, BigDecimal> algorithmPerformanceRanking;
        private Map<String, BigDecimal> venueAnalysis;
        
        private BigDecimal riskAdjustedReturn;
        private BigDecimal maximumDrawdown;
        
        private List<String> recommendations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BenchmarkAnalysis {
        private String symbol;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime analysisTime;
        private String status;
        
        private BigDecimal vwapPerformance;
        private BigDecimal arrivalPricePerformance;
        private BigDecimal implementationShortfall;
        private BigDecimal marketRelativePerformance;
        private BigDecimal executionEfficiencyScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BestExecutionStatus {
        private LocalDate reportDate;
        private LocalDateTime statusTime;
        private String overallStatus;
        private BigDecimal complianceRate;
        private String slippageStatus;
        private String marketImpactStatus;
        private String executionSpeedStatus;
    }
    
    @lombok.Data
    public static class ExecutionMetrics {
        private Integer totalTrades = 0;
        private BigDecimal totalVolume = BigDecimal.ZERO;
        private BigDecimal totalNotionalValue = BigDecimal.ZERO;
        private BigDecimal totalSlippage = BigDecimal.ZERO;
        private BigDecimal totalMarketImpact = BigDecimal.ZERO;
        private Long totalExecutionSpeed = 0L;
        private Integer executionSpeedCount = 0;
        
        public void incrementTotalTrades() { this.totalTrades++; }
        public void addVolume(BigDecimal volume) { this.totalVolume = this.totalVolume.add(volume); }
        public void addNotionalValue(BigDecimal value) { this.totalNotionalValue = this.totalNotionalValue.add(value); }
        public void addSlippage(BigDecimal slippage) { this.totalSlippage = this.totalSlippage.add(slippage); }
        public void addMarketImpact(BigDecimal impact) { this.totalMarketImpact = this.totalMarketImpact.add(impact); }
        public void addExecutionSpeed(Long speed) { 
            this.totalExecutionSpeed += speed; 
            this.executionSpeedCount++;
        }
        
        public BigDecimal getAverageSlippage() {
            return totalTrades > 0 ? totalSlippage.divide(BigDecimal.valueOf(totalTrades), 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }
        
        public BigDecimal getAverageMarketImpact() {
            return totalTrades > 0 ? totalMarketImpact.divide(BigDecimal.valueOf(totalTrades), 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }
        
        public BigDecimal getAverageExecutionSpeed() {
            return executionSpeedCount > 0 ? BigDecimal.valueOf(totalExecutionSpeed / executionSpeedCount) : BigDecimal.ZERO;
        }
        
        public BigDecimal getRiskAdjustedReturn() {
            return BigDecimal.valueOf(0.08); // 8% 임시 값
        }
        
        public BigDecimal getMaxDrawdown() {
            return BigDecimal.valueOf(0.03); // 3% 임시 값
        }
        
        public BigDecimal getReportingCompliance() {
            return BigDecimal.valueOf(0.98); // 98% 임시 값
        }
        
        public BigDecimal getPositionLimitCompliance() {
            return BigDecimal.valueOf(0.97); // 97% 임시 값
        }
    }
    
    // ========================= Notification Helper Methods =========================
    
    /**
     * 베스트 실행 리포트 알림 발송
     */
    private void sendBestExecutionReportNotification(BestExecutionReport report) {
        try {
            log.info("베스트 실행 리포트 알림 발송: {}", report.getReportDate());
            
            // 리포트를 RiskAlert 형태로 변환하여 기존 NotificationService 활용
            com.stockquest.domain.risk.RiskAlert reportAlert = convertToRiskAlert(report);
            
            // 이메일과 슬랙으로 알림 발송
            notificationService.sendNotification(reportAlert, 
                com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.EMAIL);
            notificationService.sendNotification(reportAlert, 
                com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.SLACK);
                
        } catch (Exception e) {
            log.error("베스트 실행 리포트 알림 발송 실패", e);
        }
    }
    
    /**
     * 실행 알림 발송
     */
    private void sendExecutionAlert(ExecutionAlert alert) {
        try {
            log.warn("실행 알림 발송: {} - {}", alert.getAlertType(), alert.getMessage());
            
            // ExecutionAlert를 RiskAlert로 변환
            com.stockquest.domain.risk.RiskAlert riskAlert = convertExecutionAlertToRiskAlert(alert);
            
            // 심각도에 따라 다른 채널로 알림 발송
            switch (alert.getSeverity()) {
                case "HIGH":
                    // 높은 심각도: 이메일, 슬랙, SMS
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.EMAIL);
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.SLACK);
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.SMS);
                    break;
                case "MEDIUM":
                    // 중간 심각도: 이메일, 슬랙
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.EMAIL);
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.SLACK);
                    break;
                default:
                    // 낮은 심각도: 인앱 알림만
                    notificationService.sendNotification(riskAlert, 
                        com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel.IN_APP);
            }
            
        } catch (Exception e) {
            log.error("실행 알림 발송 실패: {}", alert.getAlertType(), e);
        }
    }
    
    /**
     * BestExecutionReport를 RiskAlert로 변환
     */
    private com.stockquest.domain.risk.RiskAlert convertToRiskAlert(BestExecutionReport report) {
        return com.stockquest.domain.risk.RiskAlert.builder()
            .alertId("EXECUTION_REPORT_" + report.getReportDate())
            .alertType(com.stockquest.domain.risk.RiskAlert.AlertType.MODEL_DEGRADATION)
            .severity(com.stockquest.domain.risk.RiskAlert.AlertSeverity.INFO)
            .title("일일 베스트 실행 리포트")
            .message(String.format("날짜: %s, 총 거래: %d건, 평균 슬리피지: %.4f", 
                report.getReportDate(), report.getTotalTrades(), report.getAverageSlippage()))
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * ExecutionAlert를 RiskAlert로 변환
     */
    private com.stockquest.domain.risk.RiskAlert convertExecutionAlertToRiskAlert(ExecutionAlert alert) {
        com.stockquest.domain.risk.RiskAlert.AlertSeverity severity = switch (alert.getSeverity()) {
            case "HIGH" -> com.stockquest.domain.risk.RiskAlert.AlertSeverity.CRITICAL;
            case "MEDIUM" -> com.stockquest.domain.risk.RiskAlert.AlertSeverity.HIGH;
            default -> com.stockquest.domain.risk.RiskAlert.AlertSeverity.MEDIUM;
        };
        
        com.stockquest.domain.risk.RiskAlert.AlertType alertType = mapExecutionAlertToRiskAlertType(alert.getAlertType());
        
        return com.stockquest.domain.risk.RiskAlert.builder()
            .alertId("EXECUTION_" + alert.getAlertType() + "_" + System.currentTimeMillis())
            .alertType(alertType)
            .severity(severity)
            .title("실행 품질 알림")
            .message(alert.getMessage())
            .createdAt(alert.getTimestamp())
            .build();
    }
    
    /**
     * ExecutionAlertType을 RiskAlert.AlertType으로 매핑
     */
    private com.stockquest.domain.risk.RiskAlert.AlertType mapExecutionAlertToRiskAlertType(ExecutionAlertType executionAlertType) {
        return switch (executionAlertType) {
            case QUALITY_DEGRADATION -> com.stockquest.domain.risk.RiskAlert.AlertType.MODEL_DEGRADATION;
            case EXECUTION_DELAY -> com.stockquest.domain.risk.RiskAlert.AlertType.LIQUIDITY_SHORTAGE;
            case HIGH_MARKET_IMPACT -> com.stockquest.domain.risk.RiskAlert.AlertType.VOLATILITY_SURGE;
            case ALGORITHM_UNDERPERFORMANCE -> com.stockquest.domain.risk.RiskAlert.AlertType.MODEL_DEGRADATION;
            case COMPLIANCE_BREACH -> com.stockquest.domain.risk.RiskAlert.AlertType.COMPLIANCE_VIOLATION;
        };
    }
    
    // ========================= Execution Alert DTOs =========================
    
    /**
     * 실행 알림 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutionAlert {
        private ExecutionAlertType alertType;
        private String severity; // HIGH, MEDIUM, LOW
        private String message;
        private Integer affectedTrades;
        private LocalDateTime timestamp;
    }
    
    /**
     * 실행 알림 유형
     */
    public enum ExecutionAlertType {
        QUALITY_DEGRADATION("거래 품질 저하"),
        EXECUTION_DELAY("실행 지연"),
        HIGH_MARKET_IMPACT("높은 시장 영향"),
        ALGORITHM_UNDERPERFORMANCE("알고리즘 성과 저하"),
        COMPLIANCE_BREACH("규제 준수 위반");
        
        private final String description;
        
        ExecutionAlertType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}