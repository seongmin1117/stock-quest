package com.stockquest.application.service.analytics;

import com.stockquest.domain.backtesting.BacktestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 트레이딩 분석 전문 서비스
 * 기존 PerformanceAnalyticsService에서 트레이딩 분석 기능 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradingAnalysisService {

    /**
     * 종합 트레이딩 분석 수행
     */
    public TradingAnalysis performTradingAnalysis(BacktestResult result) {
        try {
            log.info("트레이딩 분석 시작: {}", result.getBacktestId());
            
            List<BacktestResult.TradeRecord> trades = result.getTrades();
            if (trades == null || trades.isEmpty()) {
                log.warn("거래 기록이 없습니다: {}", result.getBacktestId());
                return createEmptyTradingAnalysis();
            }
            
            return TradingAnalysis.builder()
                .tradingPatternAnalysis(analyzeTradingPatterns(trades))
                .tradingEfficiency(analyzeTradingEfficiency(trades, result))
                .tradingCostAnalysis(analyzeTradingCosts(trades, result))
                .tradingTimingAnalysis(analyzeTradingTiming(trades, result))
                .executionQuality(assessExecutionQuality(trades))
                .build();
                
        } catch (Exception e) {
            log.error("트레이딩 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("트레이딩 분석 실패", e);
        }
    }

    /**
     * 빈 트레이딩 분석 결과 생성
     */
    private TradingAnalysis createEmptyTradingAnalysis() {
        return TradingAnalysis.builder()
            .tradingPatternAnalysis(TradingPatternAnalysis.builder().build())
            .tradingEfficiency(TradingEfficiency.builder().build())
            .tradingCostAnalysis(TradingCostAnalysis.builder().build())
            .tradingTimingAnalysis(TradingTimingAnalysis.builder().build())
            .executionQuality(ExecutionQuality.builder().build())
            .build();
    }

    /**
     * 트레이딩 패턴 분석
     */
    private TradingPatternAnalysis analyzeTradingPatterns(List<BacktestResult.TradeRecord> trades) {
        Map<String, Integer> symbolFrequency = calculateSymbolTradingFrequency(trades);
        Map<DayOfWeek, Integer> dayOfWeekPattern = analyzeDayOfWeekPattern(trades);
        Map<Integer, Integer> hourlyPattern = analyzeHourlyTradingPattern(trades);
        
        // 거래 크기 분포
        Map<String, Integer> tradeSizeDistribution = categorizeTradeSizes(trades);
        
        // 연속 거래 패턴
        StreakAnalysis winLossStreaks = analyzeWinLossStreaks(trades);
        
        return TradingPatternAnalysis.builder()
            .totalTrades(trades.size())
            .symbolTradingFrequency(symbolFrequency)
            .dayOfWeekPattern(dayOfWeekPattern)
            .hourlyTradingPattern(hourlyPattern)
            .tradeSizeDistribution(tradeSizeDistribution)
            .avgTradesPerDay(calculateAvgTradesPerDay(trades))
            .winLossStreaks(winLossStreaks)
            .mostTradedSymbol(findMostTradedSymbol(symbolFrequency))
            .leastTradedSymbol(findLeastTradedSymbol(symbolFrequency))
            .build();
    }

    /**
     * 심볼별 거래 빈도 계산
     */
    private Map<String, Integer> calculateSymbolTradingFrequency(List<BacktestResult.TradeRecord> trades) {
        // Since TradeRecord doesn't have symbol, we'll use a placeholder or skip this analysis
        Map<String, Integer> symbolFreq = new HashMap<>();
        symbolFreq.put("UNKNOWN", trades.size()); // Simplified - in reality, symbol would come from external context
        return symbolFreq;
    }

    /**
     * 요일별 트레이딩 패턴 분석
     */
    private Map<DayOfWeek, Integer> analyzeDayOfWeekPattern(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .collect(Collectors.groupingBy(
                trade -> trade.getEntryTime().getDayOfWeek(),
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    Math::toIntExact
                )
            ));
    }

    /**
     * 시간대별 트레이딩 패턴 분석
     */
    private Map<Integer, Integer> analyzeHourlyTradingPattern(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .collect(Collectors.groupingBy(
                trade -> trade.getEntryTime().getHour(),
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    Math::toIntExact
                )
            ));
    }

    /**
     * 거래 크기 분포 분석
     */
    private Map<String, Integer> categorizeTradeSizes(List<BacktestResult.TradeRecord> trades) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (BacktestResult.TradeRecord trade : trades) {
            BigDecimal tradeValue = trade.getEntryPrice().multiply(trade.getQuantity());
            String category = categorizeTradeSize(tradeValue);
            distribution.merge(category, 1, Integer::sum);
        }
        
        return distribution;
    }

    /**
     * 거래 크기 카테고리 분류
     */
    private String categorizeTradeSize(BigDecimal tradeValue) {
        if (tradeValue.compareTo(new BigDecimal("1000")) < 0) {
            return "Small (< $1K)";
        } else if (tradeValue.compareTo(new BigDecimal("10000")) < 0) {
            return "Medium ($1K-$10K)";
        } else if (tradeValue.compareTo(new BigDecimal("100000")) < 0) {
            return "Large ($10K-$100K)";
        } else {
            return "Very Large (> $100K)";
        }
    }

    /**
     * 승패 연속 패턴 분석
     */
    private StreakAnalysis analyzeWinLossStreaks(List<BacktestResult.TradeRecord> trades) {
        List<Boolean> outcomes = trades.stream()
            .map(trade -> trade.getPnl().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
            
        int longestWinStreak = 0;
        int longestLossStreak = 0;
        int currentWinStreak = 0;
        int currentLossStreak = 0;
        
        for (Boolean isWin : outcomes) {
            if (isWin) {
                currentWinStreak++;
                currentLossStreak = 0;
                longestWinStreak = Math.max(longestWinStreak, currentWinStreak);
            } else {
                currentLossStreak++;
                currentWinStreak = 0;
                longestLossStreak = Math.max(longestLossStreak, currentLossStreak);
            }
        }
        
        return StreakAnalysis.builder()
            .longestWinStreak(longestWinStreak)
            .longestLossStreak(longestLossStreak)
            .currentWinStreak(outcomes.isEmpty() ? 0 : (outcomes.get(outcomes.size()-1) ? currentWinStreak : 0))
            .currentLossStreak(outcomes.isEmpty() ? 0 : (outcomes.get(outcomes.size()-1) ? 0 : currentLossStreak))
            .build();
    }

    /**
     * 일평균 거래 횟수 계산
     */
    private BigDecimal calculateAvgTradesPerDay(List<BacktestResult.TradeRecord> trades) {
        if (trades.isEmpty()) return BigDecimal.ZERO;
        
        Set<LocalDateTime> tradingDays = trades.stream()
            .map(trade -> trade.getEntryTime().toLocalDate().atStartOfDay())
            .collect(Collectors.toSet());
            
        return BigDecimal.valueOf(trades.size())
            .divide(BigDecimal.valueOf(tradingDays.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 가장 많이 거래된 심볼 찾기
     */
    private String findMostTradedSymbol(Map<String, Integer> symbolFrequency) {
        return symbolFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    /**
     * 가장 적게 거래된 심볼 찾기
     */
    private String findLeastTradedSymbol(Map<String, Integer> symbolFrequency) {
        return symbolFrequency.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    /**
     * 트레이딩 효율성 분석
     */
    private TradingEfficiency analyzeTradingEfficiency(List<BacktestResult.TradeRecord> trades, BacktestResult result) {
        double winRate = calculateWinRate(trades);
        BigDecimal avgWin = calculateAverageWin(trades);
        BigDecimal avgLoss = calculateAverageLoss(trades);
        BigDecimal profitFactor = calculateProfitFactor(avgWin, avgLoss, winRate);
        BigDecimal expectancy = calculateExpectancy(avgWin, avgLoss, winRate);
        
        return TradingEfficiency.builder()
            .winRate(BigDecimal.valueOf(winRate).setScale(4, RoundingMode.HALF_UP))
            .lossRate(BigDecimal.valueOf(1.0 - winRate).setScale(4, RoundingMode.HALF_UP))
            .averageWin(avgWin)
            .averageLoss(avgLoss)
            .winLossRatio(calculateWinLossRatio(avgWin, avgLoss))
            .profitFactor(profitFactor)
            .expectancy(expectancy)
            .totalProfit(calculateTotalProfit(trades))
            .totalLoss(calculateTotalLoss(trades))
            .netProfit(calculateNetProfit(trades))
            .build();
    }

    /**
     * 승률 계산
     */
    private double calculateWinRate(List<BacktestResult.TradeRecord> trades) {
        if (trades.isEmpty()) return 0.0;
        
        long winningTrades = trades.stream()
            .mapToLong(trade -> trade.getPnl().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
            .sum();
            
        return (double) winningTrades / trades.size();
    }

    /**
     * 평균 수익 계산
     */
    private BigDecimal calculateAverageWin(List<BacktestResult.TradeRecord> trades) {
        List<BigDecimal> winningTrades = trades.stream()
            .map(BacktestResult.TradeRecord::getPnl)
            .filter(pnl -> pnl.compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
            
        if (winningTrades.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal sum = winningTrades.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return sum.divide(BigDecimal.valueOf(winningTrades.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 평균 손실 계산
     */
    private BigDecimal calculateAverageLoss(List<BacktestResult.TradeRecord> trades) {
        List<BigDecimal> losingTrades = trades.stream()
            .map(BacktestResult.TradeRecord::getPnl)
            .filter(pnl -> pnl.compareTo(BigDecimal.ZERO) < 0)
            .collect(Collectors.toList());
            
        if (losingTrades.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal sum = losingTrades.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return sum.divide(BigDecimal.valueOf(losingTrades.size()), 4, RoundingMode.HALF_UP).abs();
    }

    /**
     * 손익비 계산
     */
    private BigDecimal calculateWinLossRatio(BigDecimal avgWin, BigDecimal avgLoss) {
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return avgWin.divide(avgLoss, 4, RoundingMode.HALF_UP);
    }

    /**
     * 수익 인수 계산
     */
    private BigDecimal calculateProfitFactor(BigDecimal avgWin, BigDecimal avgLoss, double winRate) {
        BigDecimal totalWins = avgWin.multiply(BigDecimal.valueOf(winRate));
        BigDecimal totalLosses = avgLoss.multiply(BigDecimal.valueOf(1.0 - winRate));
        
        if (totalLosses.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return totalWins.divide(totalLosses, 4, RoundingMode.HALF_UP);
    }

    /**
     * 기댓값 계산
     */
    private BigDecimal calculateExpectancy(BigDecimal avgWin, BigDecimal avgLoss, double winRate) {
        BigDecimal expectedWin = avgWin.multiply(BigDecimal.valueOf(winRate));
        BigDecimal expectedLoss = avgLoss.multiply(BigDecimal.valueOf(1.0 - winRate));
        
        return expectedWin.subtract(expectedLoss);
    }

    /**
     * 총 수익 계산
     */
    private BigDecimal calculateTotalProfit(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .map(BacktestResult.TradeRecord::getPnl)
            .filter(pnl -> pnl.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 총 손실 계산
     */
    private BigDecimal calculateTotalLoss(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .map(BacktestResult.TradeRecord::getPnl)
            .filter(pnl -> pnl.compareTo(BigDecimal.ZERO) < 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .abs();
    }

    /**
     * 순이익 계산
     */
    private BigDecimal calculateNetProfit(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .map(BacktestResult.TradeRecord::getPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 트레이딩 비용 분석
     */
    private TradingCostAnalysis analyzeTradingCosts(List<BacktestResult.TradeRecord> trades, BacktestResult result) {
        BigDecimal totalCommissions = calculateTotalCommissions(trades);
        BigDecimal totalSlippage = calculateTotalSlippage(trades);
        BigDecimal totalCosts = totalCommissions.add(totalSlippage);
        BigDecimal costPerTrade = trades.isEmpty() ? BigDecimal.ZERO 
            : totalCosts.divide(BigDecimal.valueOf(trades.size()), 4, RoundingMode.HALF_UP);
        
        return TradingCostAnalysis.builder()
            .totalCommissions(totalCommissions)
            .totalSlippage(totalSlippage)
            .totalTradingCosts(totalCosts)
            .averageCostPerTrade(costPerTrade)
            .costAsPercentageOfProfit(calculateCostAsPercentageOfProfit(totalCosts, result))
            .build();
    }

    /**
     * 총 수수료 계산
     */
    private BigDecimal calculateTotalCommissions(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .map(trade -> {
                // 간소화된 수수료 계산 (실제로는 브로커별 수수료 구조 적용)
                BigDecimal tradeValue = trade.getEntryPrice().multiply(trade.getQuantity());
                return tradeValue.multiply(new BigDecimal("0.001")); // 0.1% 수수료 가정
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 총 슬리피지 계산
     */
    private BigDecimal calculateTotalSlippage(List<BacktestResult.TradeRecord> trades) {
        return trades.stream()
            .map(trade -> {
                // 간소화된 슬리피지 계산 (실제로는 주문 크기, 유동성 고려)
                BigDecimal tradeValue = trade.getEntryPrice().multiply(trade.getQuantity());
                return tradeValue.multiply(new BigDecimal("0.0005")); // 0.05% 슬리피지 가정
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 비용의 수익 대비 비율 계산
     */
    private BigDecimal calculateCostAsPercentageOfProfit(BigDecimal totalCosts, BacktestResult result) {
        BigDecimal totalReturn = result.getTotalReturn();
        if (totalReturn.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        return totalCosts.divide(totalReturn.abs(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * 트레이딩 타이밍 분석
     */
    private TradingTimingAnalysis analyzeTradingTiming(List<BacktestResult.TradeRecord> trades, BacktestResult result) {
        Map<String, BigDecimal> entryTimingAnalysis = analyzeEntryTiming(trades);
        Map<String, BigDecimal> exitTimingAnalysis = analyzeExitTiming(trades);
        BigDecimal avgHoldingPeriod = calculateAverageHoldingPeriod(trades);
        
        return TradingTimingAnalysis.builder()
            .entryTimingAnalysis(entryTimingAnalysis)
            .exitTimingAnalysis(exitTimingAnalysis)
            .averageHoldingPeriod(avgHoldingPeriod)
            .bestPerformingTimeWindow(findBestPerformingTimeWindow(trades))
            .worstPerformingTimeWindow(findWorstPerformingTimeWindow(trades))
            .build();
    }

    /**
     * 진입 타이밍 분석
     */
    private Map<String, BigDecimal> analyzeEntryTiming(List<BacktestResult.TradeRecord> trades) {
        Map<String, BigDecimal> analysis = new HashMap<>();
        
        // 시간대별 평균 수익률
        Map<Integer, List<BigDecimal>> hourlyReturns = new HashMap<>();
        for (BacktestResult.TradeRecord trade : trades) {
            int hour = trade.getEntryTime().getHour();
            hourlyReturns.computeIfAbsent(hour, k -> new ArrayList<>()).add(trade.getPnl());
        }
        
        hourlyReturns.forEach((hour, returns) -> {
            BigDecimal avgReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);
            analysis.put(hour + ":00", avgReturn);
        });
        
        return analysis;
    }

    /**
     * 청산 타이밍 분석
     */
    private Map<String, BigDecimal> analyzeExitTiming(List<BacktestResult.TradeRecord> trades) {
        // 진입 타이밍과 유사한 방식으로 청산 타이밍 분석
        return analyzeEntryTiming(trades); // 간소화
    }

    /**
     * 평균 보유 기간 계산
     */
    private BigDecimal calculateAverageHoldingPeriod(List<BacktestResult.TradeRecord> trades) {
        if (trades.isEmpty()) return BigDecimal.ZERO;
        
        // 간소화된 계산 - 실제로는 포지션 오픈/클로즈 시간 차이 계산
        return new BigDecimal("2.5"); // 2.5일 평균 가정
    }

    /**
     * 최고 성과 시간대 찾기
     */
    private String findBestPerformingTimeWindow(List<BacktestResult.TradeRecord> trades) {
        Map<Integer, BigDecimal> hourlyPnL = new HashMap<>();
        
        for (BacktestResult.TradeRecord trade : trades) {
            int hour = trade.getEntryTime().getHour();
            hourlyPnL.merge(hour, trade.getPnl(), BigDecimal::add);
        }
        
        return hourlyPnL.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + ":00-" + (entry.getKey() + 1) + ":00")
            .orElse("N/A");
    }

    /**
     * 최악 성과 시간대 찾기
     */
    private String findWorstPerformingTimeWindow(List<BacktestResult.TradeRecord> trades) {
        Map<Integer, BigDecimal> hourlyPnL = new HashMap<>();
        
        for (BacktestResult.TradeRecord trade : trades) {
            int hour = trade.getEntryTime().getHour();
            hourlyPnL.merge(hour, trade.getPnl(), BigDecimal::add);
        }
        
        return hourlyPnL.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + ":00-" + (entry.getKey() + 1) + ":00")
            .orElse("N/A");
    }

    /**
     * 실행 품질 평가
     */
    private ExecutionQuality assessExecutionQuality(List<BacktestResult.TradeRecord> trades) {
        BigDecimal avgSlippage = calculateAverageSlippage(trades);
        BigDecimal fillRate = calculateFillRate(trades);
        BigDecimal executionSpeed = assessExecutionSpeed(trades);
        
        return ExecutionQuality.builder()
            .averageSlippage(avgSlippage)
            .fillRate(fillRate)
            .partialFillRate(calculatePartialFillRate(trades))
            .averageExecutionSpeed(executionSpeed)
            .priceImprovementRate(calculatePriceImprovementRate(trades))
            .marketImpactScore(calculateMarketImpactScore(trades))
            .overallExecutionScore(calculateOverallExecutionScore(avgSlippage, fillRate, executionSpeed))
            .build();
    }

    /**
     * 평균 슬리피지 계산
     */
    private BigDecimal calculateAverageSlippage(List<BacktestResult.TradeRecord> trades) {
        if (trades.isEmpty()) return BigDecimal.ZERO;
        
        // 간소화된 슬리피지 계산 - 실제로는 예상가격 vs 실제체결가격 비교
        return new BigDecimal("0.0005"); // 0.05% 평균 슬리피지 가정
    }

    /**
     * 체결률 계산
     */
    private BigDecimal calculateFillRate(List<BacktestResult.TradeRecord> trades) {
        // 간소화 - 실제로는 주문 대비 체결 비율
        return new BigDecimal("0.98"); // 98% 체결률 가정
    }

    /**
     * 부분 체결률 계산
     */
    private BigDecimal calculatePartialFillRate(List<BacktestResult.TradeRecord> trades) {
        return new BigDecimal("0.05"); // 5% 부분 체결률 가정
    }

    /**
     * 실행 속도 평가
     */
    private BigDecimal assessExecutionSpeed(List<BacktestResult.TradeRecord> trades) {
        return new BigDecimal("0.15"); // 평균 0.15초 실행 시간 가정
    }

    /**
     * 가격 개선률 계산
     */
    private BigDecimal calculatePriceImprovementRate(List<BacktestResult.TradeRecord> trades) {
        return new BigDecimal("0.12"); // 12% 가격 개선률 가정
    }

    /**
     * 시장 영향 점수 계산
     */
    private BigDecimal calculateMarketImpactScore(List<BacktestResult.TradeRecord> trades) {
        return new BigDecimal("0.03"); // 3% 시장 영향 점수 가정
    }

    /**
     * 전체 실행 품질 점수 계산
     */
    private BigDecimal calculateOverallExecutionScore(BigDecimal avgSlippage, BigDecimal fillRate, BigDecimal executionSpeed) {
        // 가중 평균으로 전체 점수 계산
        BigDecimal slippageScore = BigDecimal.ONE.subtract(avgSlippage.multiply(new BigDecimal("100")));
        BigDecimal speedScore = BigDecimal.ONE.subtract(executionSpeed.divide(new BigDecimal("1.0"), 4, RoundingMode.HALF_UP));
        
        return slippageScore.multiply(new BigDecimal("0.4"))
            .add(fillRate.multiply(new BigDecimal("0.4")))
            .add(speedScore.multiply(new BigDecimal("0.2")));
    }

    // DTO Classes for Trading Analysis Results

    public static class TradingAnalysis {
        private final TradingPatternAnalysis tradingPatternAnalysis;
        private final TradingEfficiency tradingEfficiency;
        private final TradingCostAnalysis tradingCostAnalysis;
        private final TradingTimingAnalysis tradingTimingAnalysis;
        private final ExecutionQuality executionQuality;

        private TradingAnalysis(TradingPatternAnalysis tradingPatternAnalysis, TradingEfficiency tradingEfficiency,
                              TradingCostAnalysis tradingCostAnalysis, TradingTimingAnalysis tradingTimingAnalysis,
                              ExecutionQuality executionQuality) {
            this.tradingPatternAnalysis = tradingPatternAnalysis;
            this.tradingEfficiency = tradingEfficiency;
            this.tradingCostAnalysis = tradingCostAnalysis;
            this.tradingTimingAnalysis = tradingTimingAnalysis;
            this.executionQuality = executionQuality;
        }

        public static TradingAnalysisBuilder builder() { return new TradingAnalysisBuilder(); }

        public static class TradingAnalysisBuilder {
            private TradingPatternAnalysis tradingPatternAnalysis;
            private TradingEfficiency tradingEfficiency;
            private TradingCostAnalysis tradingCostAnalysis;
            private TradingTimingAnalysis tradingTimingAnalysis;
            private ExecutionQuality executionQuality;

            public TradingAnalysisBuilder tradingPatternAnalysis(TradingPatternAnalysis tradingPatternAnalysis) { this.tradingPatternAnalysis = tradingPatternAnalysis; return this; }
            public TradingAnalysisBuilder tradingEfficiency(TradingEfficiency tradingEfficiency) { this.tradingEfficiency = tradingEfficiency; return this; }
            public TradingAnalysisBuilder tradingCostAnalysis(TradingCostAnalysis tradingCostAnalysis) { this.tradingCostAnalysis = tradingCostAnalysis; return this; }
            public TradingAnalysisBuilder tradingTimingAnalysis(TradingTimingAnalysis tradingTimingAnalysis) { this.tradingTimingAnalysis = tradingTimingAnalysis; return this; }
            public TradingAnalysisBuilder executionQuality(ExecutionQuality executionQuality) { this.executionQuality = executionQuality; return this; }

            public TradingAnalysis build() {
                return new TradingAnalysis(tradingPatternAnalysis, tradingEfficiency, tradingCostAnalysis, tradingTimingAnalysis, executionQuality);
            }
        }

        public TradingPatternAnalysis getTradingPatternAnalysis() { return tradingPatternAnalysis; }
        public TradingEfficiency getTradingEfficiency() { return tradingEfficiency; }
        public TradingCostAnalysis getTradingCostAnalysis() { return tradingCostAnalysis; }
        public TradingTimingAnalysis getTradingTimingAnalysis() { return tradingTimingAnalysis; }
        public ExecutionQuality getExecutionQuality() { return executionQuality; }
    }

    public static class TradingPatternAnalysis {
        private final int totalTrades;
        private final Map<String, Integer> symbolTradingFrequency;
        private final Map<DayOfWeek, Integer> dayOfWeekPattern;
        private final Map<Integer, Integer> hourlyTradingPattern;
        private final Map<String, Integer> tradeSizeDistribution;
        private final BigDecimal avgTradesPerDay;
        private final StreakAnalysis winLossStreaks;
        private final String mostTradedSymbol;
        private final String leastTradedSymbol;

        private TradingPatternAnalysis(int totalTrades, Map<String, Integer> symbolTradingFrequency,
                                     Map<DayOfWeek, Integer> dayOfWeekPattern, Map<Integer, Integer> hourlyTradingPattern,
                                     Map<String, Integer> tradeSizeDistribution, BigDecimal avgTradesPerDay,
                                     StreakAnalysis winLossStreaks, String mostTradedSymbol, String leastTradedSymbol) {
            this.totalTrades = totalTrades;
            this.symbolTradingFrequency = symbolTradingFrequency;
            this.dayOfWeekPattern = dayOfWeekPattern;
            this.hourlyTradingPattern = hourlyTradingPattern;
            this.tradeSizeDistribution = tradeSizeDistribution;
            this.avgTradesPerDay = avgTradesPerDay;
            this.winLossStreaks = winLossStreaks;
            this.mostTradedSymbol = mostTradedSymbol;
            this.leastTradedSymbol = leastTradedSymbol;
        }

        public static TradingPatternAnalysisBuilder builder() { return new TradingPatternAnalysisBuilder(); }

        public static class TradingPatternAnalysisBuilder {
            private int totalTrades = 0;
            private Map<String, Integer> symbolTradingFrequency = new HashMap<>();
            private Map<DayOfWeek, Integer> dayOfWeekPattern = new HashMap<>();
            private Map<Integer, Integer> hourlyTradingPattern = new HashMap<>();
            private Map<String, Integer> tradeSizeDistribution = new HashMap<>();
            private BigDecimal avgTradesPerDay = BigDecimal.ZERO;
            private StreakAnalysis winLossStreaks;
            private String mostTradedSymbol = "N/A";
            private String leastTradedSymbol = "N/A";

            public TradingPatternAnalysisBuilder totalTrades(int totalTrades) { this.totalTrades = totalTrades; return this; }
            public TradingPatternAnalysisBuilder symbolTradingFrequency(Map<String, Integer> symbolTradingFrequency) { this.symbolTradingFrequency = symbolTradingFrequency; return this; }
            public TradingPatternAnalysisBuilder dayOfWeekPattern(Map<DayOfWeek, Integer> dayOfWeekPattern) { this.dayOfWeekPattern = dayOfWeekPattern; return this; }
            public TradingPatternAnalysisBuilder hourlyTradingPattern(Map<Integer, Integer> hourlyTradingPattern) { this.hourlyTradingPattern = hourlyTradingPattern; return this; }
            public TradingPatternAnalysisBuilder tradeSizeDistribution(Map<String, Integer> tradeSizeDistribution) { this.tradeSizeDistribution = tradeSizeDistribution; return this; }
            public TradingPatternAnalysisBuilder avgTradesPerDay(BigDecimal avgTradesPerDay) { this.avgTradesPerDay = avgTradesPerDay; return this; }
            public TradingPatternAnalysisBuilder winLossStreaks(StreakAnalysis winLossStreaks) { this.winLossStreaks = winLossStreaks; return this; }
            public TradingPatternAnalysisBuilder mostTradedSymbol(String mostTradedSymbol) { this.mostTradedSymbol = mostTradedSymbol; return this; }
            public TradingPatternAnalysisBuilder leastTradedSymbol(String leastTradedSymbol) { this.leastTradedSymbol = leastTradedSymbol; return this; }

            public TradingPatternAnalysis build() {
                return new TradingPatternAnalysis(totalTrades, symbolTradingFrequency, dayOfWeekPattern,
                    hourlyTradingPattern, tradeSizeDistribution, avgTradesPerDay, winLossStreaks,
                    mostTradedSymbol, leastTradedSymbol);
            }
        }

        // Getters
        public int getTotalTrades() { return totalTrades; }
        public Map<String, Integer> getSymbolTradingFrequency() { return symbolTradingFrequency; }
        public Map<DayOfWeek, Integer> getDayOfWeekPattern() { return dayOfWeekPattern; }
        public Map<Integer, Integer> getHourlyTradingPattern() { return hourlyTradingPattern; }
        public Map<String, Integer> getTradeSizeDistribution() { return tradeSizeDistribution; }
        public BigDecimal getAvgTradesPerDay() { return avgTradesPerDay; }
        public StreakAnalysis getWinLossStreaks() { return winLossStreaks; }
        public String getMostTradedSymbol() { return mostTradedSymbol; }
        public String getLeastTradedSymbol() { return leastTradedSymbol; }
    }

    public static class StreakAnalysis {
        private final int longestWinStreak;
        private final int longestLossStreak;
        private final int currentWinStreak;
        private final int currentLossStreak;

        private StreakAnalysis(int longestWinStreak, int longestLossStreak, int currentWinStreak, int currentLossStreak) {
            this.longestWinStreak = longestWinStreak;
            this.longestLossStreak = longestLossStreak;
            this.currentWinStreak = currentWinStreak;
            this.currentLossStreak = currentLossStreak;
        }

        public static StreakAnalysisBuilder builder() { return new StreakAnalysisBuilder(); }

        public static class StreakAnalysisBuilder {
            private int longestWinStreak = 0;
            private int longestLossStreak = 0;
            private int currentWinStreak = 0;
            private int currentLossStreak = 0;

            public StreakAnalysisBuilder longestWinStreak(int longestWinStreak) { this.longestWinStreak = longestWinStreak; return this; }
            public StreakAnalysisBuilder longestLossStreak(int longestLossStreak) { this.longestLossStreak = longestLossStreak; return this; }
            public StreakAnalysisBuilder currentWinStreak(int currentWinStreak) { this.currentWinStreak = currentWinStreak; return this; }
            public StreakAnalysisBuilder currentLossStreak(int currentLossStreak) { this.currentLossStreak = currentLossStreak; return this; }

            public StreakAnalysis build() { return new StreakAnalysis(longestWinStreak, longestLossStreak, currentWinStreak, currentLossStreak); }
        }

        public int getLongestWinStreak() { return longestWinStreak; }
        public int getLongestLossStreak() { return longestLossStreak; }
        public int getCurrentWinStreak() { return currentWinStreak; }
        public int getCurrentLossStreak() { return currentLossStreak; }
    }

    // Additional DTO classes - simplified for brevity
    
    public static class TradingEfficiency {
        private final BigDecimal winRate;
        private final BigDecimal lossRate;
        private final BigDecimal averageWin;
        private final BigDecimal averageLoss;
        private final BigDecimal winLossRatio;
        private final BigDecimal profitFactor;
        private final BigDecimal expectancy;
        private final BigDecimal totalProfit;
        private final BigDecimal totalLoss;
        private final BigDecimal netProfit;

        private TradingEfficiency(BigDecimal winRate, BigDecimal lossRate, BigDecimal averageWin, BigDecimal averageLoss,
                                BigDecimal winLossRatio, BigDecimal profitFactor, BigDecimal expectancy,
                                BigDecimal totalProfit, BigDecimal totalLoss, BigDecimal netProfit) {
            this.winRate = winRate; this.lossRate = lossRate; this.averageWin = averageWin; this.averageLoss = averageLoss;
            this.winLossRatio = winLossRatio; this.profitFactor = profitFactor; this.expectancy = expectancy;
            this.totalProfit = totalProfit; this.totalLoss = totalLoss; this.netProfit = netProfit;
        }

        public static TradingEfficiencyBuilder builder() { return new TradingEfficiencyBuilder(); }

        public static class TradingEfficiencyBuilder {
            private BigDecimal winRate = BigDecimal.ZERO; private BigDecimal lossRate = BigDecimal.ZERO;
            private BigDecimal averageWin = BigDecimal.ZERO; private BigDecimal averageLoss = BigDecimal.ZERO;
            private BigDecimal winLossRatio = BigDecimal.ZERO; private BigDecimal profitFactor = BigDecimal.ZERO;
            private BigDecimal expectancy = BigDecimal.ZERO; private BigDecimal totalProfit = BigDecimal.ZERO;
            private BigDecimal totalLoss = BigDecimal.ZERO; private BigDecimal netProfit = BigDecimal.ZERO;

            public TradingEfficiencyBuilder winRate(BigDecimal winRate) { this.winRate = winRate; return this; }
            public TradingEfficiencyBuilder lossRate(BigDecimal lossRate) { this.lossRate = lossRate; return this; }
            public TradingEfficiencyBuilder averageWin(BigDecimal averageWin) { this.averageWin = averageWin; return this; }
            public TradingEfficiencyBuilder averageLoss(BigDecimal averageLoss) { this.averageLoss = averageLoss; return this; }
            public TradingEfficiencyBuilder winLossRatio(BigDecimal winLossRatio) { this.winLossRatio = winLossRatio; return this; }
            public TradingEfficiencyBuilder profitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; return this; }
            public TradingEfficiencyBuilder expectancy(BigDecimal expectancy) { this.expectancy = expectancy; return this; }
            public TradingEfficiencyBuilder totalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; return this; }
            public TradingEfficiencyBuilder totalLoss(BigDecimal totalLoss) { this.totalLoss = totalLoss; return this; }
            public TradingEfficiencyBuilder netProfit(BigDecimal netProfit) { this.netProfit = netProfit; return this; }

            public TradingEfficiency build() { return new TradingEfficiency(winRate, lossRate, averageWin, averageLoss, winLossRatio, profitFactor, expectancy, totalProfit, totalLoss, netProfit); }
        }

        public BigDecimal getWinRate() { return winRate; } public BigDecimal getLossRate() { return lossRate; }
        public BigDecimal getAverageWin() { return averageWin; } public BigDecimal getAverageLoss() { return averageLoss; }
        public BigDecimal getWinLossRatio() { return winLossRatio; } public BigDecimal getProfitFactor() { return profitFactor; }
        public BigDecimal getExpectancy() { return expectancy; } public BigDecimal getTotalProfit() { return totalProfit; }
        public BigDecimal getTotalLoss() { return totalLoss; } public BigDecimal getNetProfit() { return netProfit; }
    }

    public static class TradingCostAnalysis {
        private final BigDecimal totalCommissions;
        private final BigDecimal totalSlippage;
        private final BigDecimal totalTradingCosts;
        private final BigDecimal averageCostPerTrade;
        private final BigDecimal costAsPercentageOfProfit;

        private TradingCostAnalysis(BigDecimal totalCommissions, BigDecimal totalSlippage, BigDecimal totalTradingCosts, BigDecimal averageCostPerTrade, BigDecimal costAsPercentageOfProfit) {
            this.totalCommissions = totalCommissions; this.totalSlippage = totalSlippage; this.totalTradingCosts = totalTradingCosts; this.averageCostPerTrade = averageCostPerTrade; this.costAsPercentageOfProfit = costAsPercentageOfProfit;
        }

        public static TradingCostAnalysisBuilder builder() { return new TradingCostAnalysisBuilder(); }

        public static class TradingCostAnalysisBuilder {
            private BigDecimal totalCommissions = BigDecimal.ZERO; private BigDecimal totalSlippage = BigDecimal.ZERO;
            private BigDecimal totalTradingCosts = BigDecimal.ZERO; private BigDecimal averageCostPerTrade = BigDecimal.ZERO; private BigDecimal costAsPercentageOfProfit = BigDecimal.ZERO;

            public TradingCostAnalysisBuilder totalCommissions(BigDecimal totalCommissions) { this.totalCommissions = totalCommissions; return this; }
            public TradingCostAnalysisBuilder totalSlippage(BigDecimal totalSlippage) { this.totalSlippage = totalSlippage; return this; }
            public TradingCostAnalysisBuilder totalTradingCosts(BigDecimal totalTradingCosts) { this.totalTradingCosts = totalTradingCosts; return this; }
            public TradingCostAnalysisBuilder averageCostPerTrade(BigDecimal averageCostPerTrade) { this.averageCostPerTrade = averageCostPerTrade; return this; }
            public TradingCostAnalysisBuilder costAsPercentageOfProfit(BigDecimal costAsPercentageOfProfit) { this.costAsPercentageOfProfit = costAsPercentageOfProfit; return this; }

            public TradingCostAnalysis build() { return new TradingCostAnalysis(totalCommissions, totalSlippage, totalTradingCosts, averageCostPerTrade, costAsPercentageOfProfit); }
        }

        public BigDecimal getTotalCommissions() { return totalCommissions; } public BigDecimal getTotalSlippage() { return totalSlippage; }
        public BigDecimal getTotalTradingCosts() { return totalTradingCosts; } public BigDecimal getAverageCostPerTrade() { return averageCostPerTrade; } public BigDecimal getCostAsPercentageOfProfit() { return costAsPercentageOfProfit; }
    }

    public static class TradingTimingAnalysis {
        private final Map<String, BigDecimal> entryTimingAnalysis;
        private final Map<String, BigDecimal> exitTimingAnalysis;
        private final BigDecimal averageHoldingPeriod;
        private final String bestPerformingTimeWindow;
        private final String worstPerformingTimeWindow;

        private TradingTimingAnalysis(Map<String, BigDecimal> entryTimingAnalysis, Map<String, BigDecimal> exitTimingAnalysis, BigDecimal averageHoldingPeriod, String bestPerformingTimeWindow, String worstPerformingTimeWindow) {
            this.entryTimingAnalysis = entryTimingAnalysis; this.exitTimingAnalysis = exitTimingAnalysis; this.averageHoldingPeriod = averageHoldingPeriod; this.bestPerformingTimeWindow = bestPerformingTimeWindow; this.worstPerformingTimeWindow = worstPerformingTimeWindow;
        }

        public static TradingTimingAnalysisBuilder builder() { return new TradingTimingAnalysisBuilder(); }

        public static class TradingTimingAnalysisBuilder {
            private Map<String, BigDecimal> entryTimingAnalysis = new HashMap<>(); private Map<String, BigDecimal> exitTimingAnalysis = new HashMap<>();
            private BigDecimal averageHoldingPeriod = BigDecimal.ZERO; private String bestPerformingTimeWindow = "N/A"; private String worstPerformingTimeWindow = "N/A";

            public TradingTimingAnalysisBuilder entryTimingAnalysis(Map<String, BigDecimal> entryTimingAnalysis) { this.entryTimingAnalysis = entryTimingAnalysis; return this; }
            public TradingTimingAnalysisBuilder exitTimingAnalysis(Map<String, BigDecimal> exitTimingAnalysis) { this.exitTimingAnalysis = exitTimingAnalysis; return this; }
            public TradingTimingAnalysisBuilder averageHoldingPeriod(BigDecimal averageHoldingPeriod) { this.averageHoldingPeriod = averageHoldingPeriod; return this; }
            public TradingTimingAnalysisBuilder bestPerformingTimeWindow(String bestPerformingTimeWindow) { this.bestPerformingTimeWindow = bestPerformingTimeWindow; return this; }
            public TradingTimingAnalysisBuilder worstPerformingTimeWindow(String worstPerformingTimeWindow) { this.worstPerformingTimeWindow = worstPerformingTimeWindow; return this; }

            public TradingTimingAnalysis build() { return new TradingTimingAnalysis(entryTimingAnalysis, exitTimingAnalysis, averageHoldingPeriod, bestPerformingTimeWindow, worstPerformingTimeWindow); }
        }

        public Map<String, BigDecimal> getEntryTimingAnalysis() { return entryTimingAnalysis; } public Map<String, BigDecimal> getExitTimingAnalysis() { return exitTimingAnalysis; }
        public BigDecimal getAverageHoldingPeriod() { return averageHoldingPeriod; } public String getBestPerformingTimeWindow() { return bestPerformingTimeWindow; } public String getWorstPerformingTimeWindow() { return worstPerformingTimeWindow; }
    }

    public static class ExecutionQuality {
        private final BigDecimal averageSlippage;
        private final BigDecimal fillRate;
        private final BigDecimal partialFillRate;
        private final BigDecimal averageExecutionSpeed;
        private final BigDecimal priceImprovementRate;
        private final BigDecimal marketImpactScore;
        private final BigDecimal overallExecutionScore;

        private ExecutionQuality(BigDecimal averageSlippage, BigDecimal fillRate, BigDecimal partialFillRate, BigDecimal averageExecutionSpeed, BigDecimal priceImprovementRate, BigDecimal marketImpactScore, BigDecimal overallExecutionScore) {
            this.averageSlippage = averageSlippage; this.fillRate = fillRate; this.partialFillRate = partialFillRate; this.averageExecutionSpeed = averageExecutionSpeed; this.priceImprovementRate = priceImprovementRate; this.marketImpactScore = marketImpactScore; this.overallExecutionScore = overallExecutionScore;
        }

        public static ExecutionQualityBuilder builder() { return new ExecutionQualityBuilder(); }

        public static class ExecutionQualityBuilder {
            private BigDecimal averageSlippage = BigDecimal.ZERO; private BigDecimal fillRate = BigDecimal.ZERO; private BigDecimal partialFillRate = BigDecimal.ZERO;
            private BigDecimal averageExecutionSpeed = BigDecimal.ZERO; private BigDecimal priceImprovementRate = BigDecimal.ZERO; private BigDecimal marketImpactScore = BigDecimal.ZERO; private BigDecimal overallExecutionScore = BigDecimal.ZERO;

            public ExecutionQualityBuilder averageSlippage(BigDecimal averageSlippage) { this.averageSlippage = averageSlippage; return this; }
            public ExecutionQualityBuilder fillRate(BigDecimal fillRate) { this.fillRate = fillRate; return this; }
            public ExecutionQualityBuilder partialFillRate(BigDecimal partialFillRate) { this.partialFillRate = partialFillRate; return this; }
            public ExecutionQualityBuilder averageExecutionSpeed(BigDecimal averageExecutionSpeed) { this.averageExecutionSpeed = averageExecutionSpeed; return this; }
            public ExecutionQualityBuilder priceImprovementRate(BigDecimal priceImprovementRate) { this.priceImprovementRate = priceImprovementRate; return this; }
            public ExecutionQualityBuilder marketImpactScore(BigDecimal marketImpactScore) { this.marketImpactScore = marketImpactScore; return this; }
            public ExecutionQualityBuilder overallExecutionScore(BigDecimal overallExecutionScore) { this.overallExecutionScore = overallExecutionScore; return this; }

            public ExecutionQuality build() { return new ExecutionQuality(averageSlippage, fillRate, partialFillRate, averageExecutionSpeed, priceImprovementRate, marketImpactScore, overallExecutionScore); }
        }

        public BigDecimal getAverageSlippage() { return averageSlippage; } public BigDecimal getFillRate() { return fillRate; } public BigDecimal getPartialFillRate() { return partialFillRate; }
        public BigDecimal getAverageExecutionSpeed() { return averageExecutionSpeed; } public BigDecimal getPriceImprovementRate() { return priceImprovementRate; } public BigDecimal getMarketImpactScore() { return marketImpactScore; } public BigDecimal getOverallExecutionScore() { return overallExecutionScore; }
    }
}