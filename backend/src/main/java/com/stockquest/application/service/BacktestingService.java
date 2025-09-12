package com.stockquest.application.service;

import com.stockquest.domain.backtesting.BacktestParameters;
import com.stockquest.domain.backtesting.BacktestResult;
import com.stockquest.domain.ml.*;
import com.stockquest.domain.ml.TradingSignal.SignalType;
import com.stockquest.domain.portfolio.Position;
import com.stockquest.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 백테스팅 서비스 - ML 신호 기반 거래 전략 백테스팅
 * Phase 8.2: Enhanced Trading Intelligence - 백테스팅 프레임워크
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestingService {
    
    private final MLTradingSignalService mlTradingSignalService;
    private final RealTimeMarketDataService marketDataService;
    
    /**
     * 백테스트 실행
     */
    public CompletableFuture<BacktestResult> runBacktest(BacktestParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("백테스트 시작: {} ({}~{})", 
                    parameters.getBacktestName(),
                    parameters.getStartDate(),
                    parameters.getEndDate());
                
                LocalDateTime startTime = LocalDateTime.now();
                BacktestEngine engine = new BacktestEngine(parameters);
                
                // 백테스트 실행
                BacktestResult result = engine.execute();
                
                // 실행 시간 기록
                long duration = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                
                return BacktestResult.builder()
                    .backtestId(UUID.randomUUID().toString())
                    .symbol(String.join(",", parameters.getSymbols()))
                    .startDate(parameters.getStartDate())
                    .endDate(parameters.getEndDate())
                    .initialCapital(parameters.getInitialCapital())
                    .finalValue(result.getFinalValue())
                    .totalReturn(result.getTotalReturn())
                    .annualizedReturn(result.getAnnualizedReturn())
                    .volatility(result.getVolatility())
                    .sharpeRatio(result.getSharpeRatio())
                    .maxDrawdown(result.getMaxDrawdown())
                    .winRate(result.getWinRate())
                    .averageWin(result.getAverageWin())
                    .averageLoss(result.getAverageLoss())
                    .profitLossRatio(result.getProfitLossRatio())
                    .totalTrades(result.getTotalTrades())
                    .winningTrades(result.getWinningTrades())
                    .losingTrades(result.getLosingTrades())
                    .calmarRatio(result.getCalmarRatio())
                    .sortinoRatio(result.getSortinoRatio())
                    .beta(result.getBeta())
                    .alpha(result.getAlpha())
                    .informationRatio(result.getInformationRatio())
                    .treynorRatio(result.getTreynorRatio())
                    .trades(result.getTrades())
                    .dailyReturns(result.getDailyReturns())
                    .detailedMetrics(result.getDetailedMetrics())
                    .parameters(parameters)
                    .riskMetrics(result.getRiskMetrics())
                    .benchmarkComparison(result.getBenchmarkComparison())
                    .executionTime(startTime)
                    .executionDuration(duration)
                    .mlPerformance(result.getMlPerformance())
                    .build();
                    
            } catch (Exception e) {
                log.error("백테스트 실행 실패: {}", e.getMessage(), e);
                throw new RuntimeException("백테스트 실행 중 오류 발생", e);
            }
        });
    }
    
    /**
     * 백테스트 엔진 - 실제 백테스팅 로직 구현
     */
    private class BacktestEngine {
        private final BacktestParameters parameters;
        private final TradingPortfolio portfolio;
        private final List<BacktestResult.TradeRecord> trades = new ArrayList<>();
        private final List<BacktestResult.DailyReturn> dailyReturns = new ArrayList<>();
        private final Map<String, Object> detailedMetrics = new HashMap<>();
        
        // ML 모델 성능 추적
        private int totalSignals = 0;
        private int correctSignals = 0;
        private int truePositives = 0;
        private int trueNegatives = 0;
        private int falsePositives = 0;
        private int falseNegatives = 0;
        private double totalConfidence = 0.0;
        private double totalPnL = 0.0;
        
        public BacktestEngine(BacktestParameters parameters) {
            this.parameters = parameters;
            this.portfolio = new TradingPortfolio(parameters.getInitialCapital());
        }
        
        public BacktestResult execute() {
            // 1. 데이터 준비
            Map<String, List<Stock>> marketData = prepareMarketData();
            
            // 2. 백테스트 실행
            simulateTrading(marketData);
            
            // 3. 성과 계산
            return calculatePerformance();
        }
        
        private Map<String, List<Stock>> prepareMarketData() {
            Map<String, List<Stock>> data = new HashMap<>();
            
            for (String symbol : parameters.getSymbols()) {
                // 실제 구현에서는 데이터베이스나 외부 API에서 데이터 가져옴
                List<Stock> stockData = generateSampleData(symbol);
                data.put(symbol, stockData);
            }
            
            return data;
        }
        
        private void simulateTrading(Map<String, List<Stock>> marketData) {
            LocalDateTime currentDate = parameters.getStartDate();
            
            while (currentDate.isBefore(parameters.getEndDate())) {
                // 각 심볼에 대해 거래 신호 평가
                for (String symbol : parameters.getSymbols()) {
                    List<Stock> stockHistory = marketData.get(symbol);
                    Stock currentStock = getCurrentStock(stockHistory, currentDate);
                    
                    if (currentStock != null) {
                        // ML 신호 생성
                        TradingSignal signal = generateTradingSignal(currentStock, stockHistory);
                        processSignal(symbol, signal, currentStock, currentDate);
                    }
                }
                
                // 포트폴리오 가치 업데이트
                updatePortfolioValue(currentDate, marketData);
                
                // 다음 날로 이동
                currentDate = currentDate.plusDays(1);
            }
        }
        
        private TradingSignal generateTradingSignal(Stock stock, List<Stock> history) {
            // ML 모델을 사용하여 신호 생성
            try {
                TechnicalIndicators indicators = calculateTechnicalIndicators(history);
                VolatilityAnalysis volatility = calculateVolatilityAnalysis(history);
                
                return mlTradingSignalService.generateSignal(stock, indicators, volatility);
                
            } catch (Exception e) {
                log.warn("ML 신호 생성 실패: {}", e.getMessage());
                return TradingSignal.builder()
                    .symbol(stock.getSymbol())
                    .signalType(SignalType.NEUTRAL)
                    .confidence(BigDecimal.ZERO)
                    .generatedAt(LocalDateTime.now())
                    .build();
            }
        }
        
        private void processSignal(String symbol, TradingSignal signal, Stock stock, LocalDateTime date) {
            totalSignals++;
            totalConfidence += signal.getConfidence().doubleValue();
            
            // 거래 전략에 따라 신호 처리
            BacktestParameters.TradingStrategy strategy = parameters.getStrategy();
            
            if (shouldExecuteTrade(signal, strategy)) {
                executeTrade(symbol, signal, stock, date, strategy);
            }
            
            // 기존 포지션 관리 (손절/익절)
            manageExistingPositions(symbol, stock, date, strategy);
        }
        
        private boolean shouldExecuteTrade(TradingSignal signal, BacktestParameters.TradingStrategy strategy) {
            // 신호 강도가 임계값을 넘는지 확인
            boolean signalStrong = signal.getConfidence().compareTo(strategy.getSignalThreshold()) >= 0;
            
            // 포지션 제한 확인
            boolean positionLimitOk = portfolio.getCurrentPositions().size() < strategy.getMaxConcurrentPositions();
            
            return signalStrong && positionLimitOk && !signal.getSignalType().equals(SignalType.NEUTRAL);
        }
        
        private void executeTrade(String symbol, TradingSignal signal, Stock stock, LocalDateTime date, BacktestParameters.TradingStrategy strategy) {
            BigDecimal price = stock.getClosePrice();
            BigDecimal positionSize = calculatePositionSize(strategy, portfolio.getCash());
            
            if (signal.getSignalType().equals(SignalType.BUY)) {
                executeBuyOrder(symbol, price, positionSize, date, signal);
            } else if (signal.getSignalType().equals(SignalType.SELL) && strategy.isAllowShortSelling()) {
                executeSellOrder(symbol, price, positionSize, date, signal);
            }
        }
        
        private void executeBuyOrder(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime date, TradingSignal signal) {
            BigDecimal orderValue = price.multiply(quantity);
            BigDecimal commission = calculateCommission(orderValue);
            
            if (portfolio.getCash().compareTo(orderValue.add(commission)) >= 0) {
                portfolio.addPosition(Position.builder()
                    .symbol(symbol)
                    .quantity(quantity)
                    .averagePrice(price)
                    .entryDate(date)
                    .build());
                    
                portfolio.setCash(portfolio.getCash().subtract(orderValue).subtract(commission));
                
                trades.add(BacktestResult.TradeRecord.builder()
                    .entryTime(date)
                    .action("BUY")
                    .entryPrice(price)
                    .quantity(quantity)
                    .signal(signal.getSignalType().toString())
                    .confidence(signal.getConfidence())
                    .build());
                    
                log.debug("매수 주문 실행: {} {}주 @ {}", symbol, quantity, price);
            }
        }
        
        private void executeSellOrder(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime date, TradingSignal signal) {
            // 공매도 또는 기존 포지션 매도
            Position position = portfolio.getPosition(symbol);
            
            if (position != null) {
                // 기존 포지션 매도
                closePosition(position, price, date, signal);
            } else if (parameters.getStrategy().isAllowShortSelling()) {
                // 공매도 실행
                executeShortSale(symbol, price, quantity, date, signal);
            }
        }
        
        private void closePosition(Position position, BigDecimal exitPrice, LocalDateTime date, TradingSignal signal) {
            BigDecimal orderValue = exitPrice.multiply(position.getQuantity());
            BigDecimal commission = calculateCommission(orderValue);
            BigDecimal pnl = orderValue.subtract(position.getAveragePrice().multiply(position.getQuantity())).subtract(commission);
            
            portfolio.removePosition(position.getSymbol());
            portfolio.setCash(portfolio.getCash().add(orderValue).subtract(commission));
            
            // 거래 기록 업데이트
            for (BacktestResult.TradeRecord trade : trades) {
                if (trade.getExitTime() == null && "BUY".equals(trade.getAction())) {
                    trade = BacktestResult.TradeRecord.builder()
                        .entryTime(trade.getEntryTime())
                        .exitTime(date)
                        .action(trade.getAction())
                        .entryPrice(trade.getEntryPrice())
                        .exitPrice(exitPrice)
                        .quantity(trade.getQuantity())
                        .pnl(pnl)
                        .pnlPercent(pnl.divide(position.getAveragePrice().multiply(position.getQuantity()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                        .signal(trade.getSignal())
                        .confidence(trade.getConfidence())
                        .build();
                    break;
                }
            }
            
            // ML 성능 평가
            evaluateMLPrediction(signal, pnl);
            
            log.debug("포지션 매도: {} {}주 @ {}, P&L: {}", position.getSymbol(), position.getQuantity(), exitPrice, pnl);
        }
        
        private void executeShortSale(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime date, TradingSignal signal) {
            // 공매도 구현 (단순화)
            Position shortPosition = Position.builder()
                .symbol(symbol)
                .quantity(quantity.negate()) // 음수로 표시
                .averagePrice(price)
                .entryDate(date)
                .build();
                
            portfolio.addPosition(shortPosition);
            
            BigDecimal orderValue = price.multiply(quantity);
            portfolio.setCash(portfolio.getCash().add(orderValue)); // 공매도 수익 추가
            
            trades.add(BacktestResult.TradeRecord.builder()
                .entryTime(date)
                .action("SELL_SHORT")
                .entryPrice(price)
                .quantity(quantity)
                .signal(signal.getSignalType().toString())
                .confidence(signal.getConfidence())
                .build());
        }
        
        private void manageExistingPositions(String symbol, Stock stock, LocalDateTime date, BacktestParameters.TradingStrategy strategy) {
            Position position = portfolio.getPosition(symbol);
            if (position == null) return;
            
            BigDecimal currentPrice = stock.getClosePrice();
            BigDecimal entryPrice = position.getAveragePrice();
            
            // 손절 체크
            if (strategy.getStopLossPercent() != null) {
                BigDecimal stopLossPrice = entryPrice.multiply(BigDecimal.ONE.subtract(strategy.getStopLossPercent().divide(BigDecimal.valueOf(100))));
                if (currentPrice.compareTo(stopLossPrice) <= 0) {
                    closePosition(position, currentPrice, date, 
                        TradingSignal.builder()
                            .symbol(symbol)
                            .signalType(SignalType.SELL)
                            .confidence(BigDecimal.ONE)
                            .generatedAt(date)
                            .build());
                    return;
                }
            }
            
            // 익절 체크
            if (strategy.getTakeProfitPercent() != null) {
                BigDecimal takeProfitPrice = entryPrice.multiply(BigDecimal.ONE.add(strategy.getTakeProfitPercent().divide(BigDecimal.valueOf(100))));
                if (currentPrice.compareTo(takeProfitPrice) >= 0) {
                    closePosition(position, currentPrice, date,
                        TradingSignal.builder()
                            .symbol(symbol)
                            .signalType(SignalType.SELL)
                            .confidence(BigDecimal.ONE)
                            .generatedAt(date)
                            .build());
                    return;
                }
            }
            
            // 최대 보유 기간 체크
            if (strategy.getMaxHoldingPeriod() != null) {
                long daysHeld = ChronoUnit.DAYS.between(position.getEntryDate(), date);
                if (daysHeld >= strategy.getMaxHoldingPeriod()) {
                    closePosition(position, currentPrice, date,
                        TradingSignal.builder()
                            .symbol(symbol)
                            .signalType(SignalType.SELL)
                            .confidence(BigDecimal.valueOf(0.5))
                            .generatedAt(date)
                            .build());
                }
            }
        }
        
        private BigDecimal calculatePositionSize(BacktestParameters.TradingStrategy strategy, BigDecimal availableCash) {
            // 포지션 크기 계산 (위험 관리 기반)
            BigDecimal maxPositionValue = availableCash.multiply(strategy.getMaxPositionSize().divide(BigDecimal.valueOf(100)));
            
            // 단순화: 고정 수량으로 계산
            return maxPositionValue.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN); // 100달러당 1주
        }
        
        private BigDecimal calculateCommission(BigDecimal orderValue) {
            BacktestParameters.TradingCosts costs = parameters.getCosts();
            if (costs == null) return BigDecimal.ZERO;
            
            BigDecimal percentCommission = orderValue.multiply(costs.getCommissionPercent().divide(BigDecimal.valueOf(100)));
            return percentCommission.add(costs.getCommissionPerTrade());
        }
        
        private void updatePortfolioValue(LocalDateTime date, Map<String, List<Stock>> marketData) {
            BigDecimal portfolioValue = portfolio.getCash();
            
            // 포지션 가치 계산
            for (Position position : portfolio.getCurrentPositions()) {
                Stock currentStock = getCurrentStock(marketData.get(position.getSymbol()), date);
                if (currentStock != null) {
                    BigDecimal positionValue = currentStock.getClosePrice().multiply(position.getQuantity().abs());
                    portfolioValue = portfolioValue.add(positionValue);
                }
            }
            
            // 일별 수익률 계산
            BigDecimal dailyReturn = BigDecimal.ZERO;
            if (!dailyReturns.isEmpty()) {
                BacktestResult.DailyReturn lastReturn = dailyReturns.get(dailyReturns.size() - 1);
                dailyReturn = portfolioValue.subtract(lastReturn.getPortfolioValue())
                    .divide(lastReturn.getPortfolioValue(), 6, RoundingMode.HALF_UP);
            }
            
            BigDecimal cumulativeReturn = portfolioValue.subtract(parameters.getInitialCapital())
                .divide(parameters.getInitialCapital(), 6, RoundingMode.HALF_UP);
            
            // 최대 낙폭 계산
            BigDecimal maxValue = dailyReturns.stream()
                .map(BacktestResult.DailyReturn::getPortfolioValue)
                .max(BigDecimal::compareTo)
                .orElse(parameters.getInitialCapital());
            maxValue = maxValue.max(portfolioValue);
            
            BigDecimal drawdown = portfolioValue.subtract(maxValue).divide(maxValue, 6, RoundingMode.HALF_UP);
            
            dailyReturns.add(BacktestResult.DailyReturn.builder()
                .date(date)
                .portfolioValue(portfolioValue)
                .dailyReturn(dailyReturn)
                .cumulativeReturn(cumulativeReturn)
                .drawdown(drawdown)
                .build());
        }
        
        private void evaluateMLPrediction(TradingSignal signal, BigDecimal pnl) {
            boolean actualPositive = pnl.compareTo(BigDecimal.ZERO) > 0;
            boolean predictedPositive = signal.getSignalType().equals(SignalType.BUY);
            
            if (actualPositive && predictedPositive) {
                truePositives++;
                correctSignals++;
            } else if (!actualPositive && !predictedPositive) {
                trueNegatives++;
                correctSignals++;
            } else if (!actualPositive && predictedPositive) {
                falsePositives++;
            } else {
                falseNegatives++;
            }
            
            totalPnL += pnl.doubleValue();
        }
        
        private BacktestResult calculatePerformance() {
            if (dailyReturns.isEmpty()) {
                return createEmptyResult();
            }
            
            // 기본 성과 지표
            BacktestResult.DailyReturn finalReturn = dailyReturns.get(dailyReturns.size() - 1);
            BigDecimal finalValue = finalReturn.getPortfolioValue();
            BigDecimal totalReturn = finalReturn.getCumulativeReturn().multiply(BigDecimal.valueOf(100));
            
            // 연간 수익률
            long totalDays = ChronoUnit.DAYS.between(parameters.getStartDate(), parameters.getEndDate());
            double annualizedReturnDouble = Math.pow(finalValue.divide(parameters.getInitialCapital(), 10, RoundingMode.HALF_UP).doubleValue(), 365.0 / totalDays) - 1.0;
            BigDecimal annualizedReturn = BigDecimal.valueOf(annualizedReturnDouble * 100).setScale(4, RoundingMode.HALF_UP);
            
            // 변동성 (일별 수익률의 표준편차)
            BigDecimal volatility = calculateVolatility();
            
            // 샤프 비율
            BigDecimal riskFreeRate = parameters.getRiskConfig() != null ? 
                parameters.getRiskConfig().getRiskFreeRate() : BigDecimal.valueOf(0.02);
            BigDecimal excessReturn = annualizedReturn.divide(BigDecimal.valueOf(100)).subtract(riskFreeRate);
            BigDecimal sharpeRatio = volatility.compareTo(BigDecimal.ZERO) > 0 ? 
                excessReturn.divide(volatility, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // 최대 낙폭
            BigDecimal maxDrawdown = dailyReturns.stream()
                .map(BacktestResult.DailyReturn::getDrawdown)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            
            // 거래 통계
            List<BigDecimal> winningTrades = new ArrayList<>();
            List<BigDecimal> losingTrades = new ArrayList<>();
            
            for (BacktestResult.TradeRecord trade : trades) {
                if (trade.getPnl() != null) {
                    if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                        winningTrades.add(trade.getPnl());
                    } else {
                        losingTrades.add(trade.getPnl());
                    }
                }
            }
            
            BigDecimal winRate = trades.size() > 0 ? 
                BigDecimal.valueOf(winningTrades.size()).divide(BigDecimal.valueOf(trades.size()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;
            
            BigDecimal averageWin = winningTrades.isEmpty() ? BigDecimal.ZERO :
                winningTrades.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(winningTrades.size()), 4, RoundingMode.HALF_UP);
                
            BigDecimal averageLoss = losingTrades.isEmpty() ? BigDecimal.ZERO :
                losingTrades.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(losingTrades.size()), 4, RoundingMode.HALF_UP);
            
            BigDecimal profitLossRatio = averageLoss.compareTo(BigDecimal.ZERO) != 0 ? 
                averageWin.divide(averageLoss.abs(), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // 칼마 비율
            BigDecimal calmarRatio = maxDrawdown.compareTo(BigDecimal.ZERO) != 0 ? 
                annualizedReturn.divide(maxDrawdown.abs(), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // ML 모델 성과
            BacktestResult.MLModelPerformance mlPerformance = calculateMLPerformance();
            
            // 위험 지표
            BacktestResult.RiskMetrics riskMetrics = calculateRiskMetrics();
            
            return BacktestResult.builder()
                .finalValue(finalValue)
                .totalReturn(totalReturn)
                .annualizedReturn(annualizedReturn)
                .volatility(volatility)
                .sharpeRatio(sharpeRatio)
                .maxDrawdown(maxDrawdown.multiply(BigDecimal.valueOf(100)))
                .winRate(winRate)
                .averageWin(averageWin)
                .averageLoss(averageLoss)
                .profitLossRatio(profitLossRatio)
                .totalTrades(trades.size())
                .winningTrades(winningTrades.size())
                .losingTrades(losingTrades.size())
                .calmarRatio(calmarRatio)
                .trades(trades)
                .dailyReturns(dailyReturns)
                .mlPerformance(mlPerformance)
                .riskMetrics(riskMetrics)
                .detailedMetrics(detailedMetrics)
                .build();
        }
        
        private BacktestResult.MLModelPerformance calculateMLPerformance() {
            if (totalSignals == 0) {
                return BacktestResult.MLModelPerformance.builder()
                    .signalAccuracy(BigDecimal.ZERO)
                    .precisionScore(BigDecimal.ZERO)
                    .recallScore(BigDecimal.ZERO)
                    .f1Score(BigDecimal.ZERO)
                    .truePositives(0)
                    .trueNegatives(0)
                    .falsePositives(0)
                    .falseNegatives(0)
                    .confidenceCorrelation(BigDecimal.ZERO)
                    .build();
            }
            
            BigDecimal accuracy = BigDecimal.valueOf(correctSignals).divide(BigDecimal.valueOf(totalSignals), 4, RoundingMode.HALF_UP);
            
            // Precision = TP / (TP + FP)
            BigDecimal precision = (truePositives + falsePositives) > 0 ? 
                BigDecimal.valueOf(truePositives).divide(BigDecimal.valueOf(truePositives + falsePositives), 4, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Recall = TP / (TP + FN)
            BigDecimal recall = (truePositives + falseNegatives) > 0 ? 
                BigDecimal.valueOf(truePositives).divide(BigDecimal.valueOf(truePositives + falseNegatives), 4, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // F1 Score = 2 * (Precision * Recall) / (Precision + Recall)
            BigDecimal f1Score = precision.add(recall).compareTo(BigDecimal.ZERO) > 0 ? 
                precision.multiply(recall).multiply(BigDecimal.valueOf(2)).divide(precision.add(recall), 4, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // 신뢰도와 성과의 상관관계 (단순화)
            BigDecimal avgConfidence = BigDecimal.valueOf(totalConfidence / totalSignals);
            BigDecimal avgPnL = BigDecimal.valueOf(totalPnL / totalSignals);
            BigDecimal confidenceCorrelation = avgPnL.compareTo(BigDecimal.ZERO) > 0 ? avgConfidence : avgConfidence.negate();
            
            return BacktestResult.MLModelPerformance.builder()
                .signalAccuracy(accuracy)
                .precisionScore(precision)
                .recallScore(recall)
                .f1Score(f1Score)
                .truePositives(truePositives)
                .trueNegatives(trueNegatives)
                .falsePositives(falsePositives)
                .falseNegatives(falseNegatives)
                .confidenceCorrelation(confidenceCorrelation)
                .build();
        }
        
        private BacktestResult.RiskMetrics calculateRiskMetrics() {
            List<BigDecimal> returns = dailyReturns.stream()
                .map(BacktestResult.DailyReturn::getDailyReturn)
                .toList();
            
            if (returns.isEmpty()) {
                return BacktestResult.RiskMetrics.builder()
                    .var95(BigDecimal.ZERO)
                    .var99(BigDecimal.ZERO)
                    .cvar95(BigDecimal.ZERO)
                    .skewness(BigDecimal.ZERO)
                    .kurtosis(BigDecimal.ZERO)
                    .downsideDeviation(BigDecimal.ZERO)
                    .maxConsecutiveLosses(0)
                    .maxLossStreak(BigDecimal.ZERO)
                    .build();
            }
            
            // VaR 95% (5% 최악의 경우)
            List<BigDecimal> sortedReturns = returns.stream().sorted().toList();
            int var95Index = (int) (sortedReturns.size() * 0.05);
            BigDecimal var95 = var95Index < sortedReturns.size() ? sortedReturns.get(var95Index) : BigDecimal.ZERO;
            
            // VaR 99% (1% 최악의 경우)
            int var99Index = (int) (sortedReturns.size() * 0.01);
            BigDecimal var99 = var99Index < sortedReturns.size() ? sortedReturns.get(var99Index) : BigDecimal.ZERO;
            
            // CVaR 95% (VaR 95% 이하의 평균)
            List<BigDecimal> worstReturns = sortedReturns.subList(0, Math.min(var95Index + 1, sortedReturns.size()));
            BigDecimal cvar95 = worstReturns.isEmpty() ? BigDecimal.ZERO :
                worstReturns.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(worstReturns.size()), 4, RoundingMode.HALF_UP);
            
            // 하락편차 (0 이하 수익률의 표준편차)
            List<BigDecimal> negativeReturns = returns.stream()
                .filter(r -> r.compareTo(BigDecimal.ZERO) < 0)
                .toList();
            
            BigDecimal downsideDeviation = calculateStandardDeviation(negativeReturns);
            
            // 연속 손실 계산
            int maxConsecutiveLosses = 0;
            int currentConsecutiveLosses = 0;
            BigDecimal maxLossStreak = BigDecimal.ZERO;
            BigDecimal currentLossStreak = BigDecimal.ZERO;
            
            for (BigDecimal dailyReturn : returns) {
                if (dailyReturn.compareTo(BigDecimal.ZERO) < 0) {
                    currentConsecutiveLosses++;
                    currentLossStreak = currentLossStreak.add(dailyReturn);
                    maxConsecutiveLosses = Math.max(maxConsecutiveLosses, currentConsecutiveLosses);
                    maxLossStreak = maxLossStreak.min(currentLossStreak);
                } else {
                    currentConsecutiveLosses = 0;
                    currentLossStreak = BigDecimal.ZERO;
                }
            }
            
            return BacktestResult.RiskMetrics.builder()
                .var95(var95.multiply(BigDecimal.valueOf(100)))
                .var99(var99.multiply(BigDecimal.valueOf(100)))
                .cvar95(cvar95.multiply(BigDecimal.valueOf(100)))
                .downsideDeviation(downsideDeviation)
                .maxConsecutiveLosses(maxConsecutiveLosses)
                .maxLossStreak(maxLossStreak.multiply(BigDecimal.valueOf(100)))
                .build();
        }
        
        private BigDecimal calculateVolatility() {
            List<BigDecimal> returns = dailyReturns.stream()
                .map(BacktestResult.DailyReturn::getDailyReturn)
                .toList();
            
            return calculateStandardDeviation(returns).multiply(BigDecimal.valueOf(Math.sqrt(252))); // 연간 변동성
        }
        
        private BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
            if (values.isEmpty()) return BigDecimal.ZERO;
            
            // 평균 계산
            BigDecimal mean = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 10, RoundingMode.HALF_UP);
            
            // 분산 계산
            BigDecimal variance = values.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 10, RoundingMode.HALF_UP);
            
            // 표준편차 = √분산
            return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        }
        
        private BacktestResult createEmptyResult() {
            return BacktestResult.builder()
                .finalValue(parameters.getInitialCapital())
                .totalReturn(BigDecimal.ZERO)
                .annualizedReturn(BigDecimal.ZERO)
                .volatility(BigDecimal.ZERO)
                .sharpeRatio(BigDecimal.ZERO)
                .maxDrawdown(BigDecimal.ZERO)
                .winRate(BigDecimal.ZERO)
                .averageWin(BigDecimal.ZERO)
                .averageLoss(BigDecimal.ZERO)
                .profitLossRatio(BigDecimal.ZERO)
                .totalTrades(0)
                .winningTrades(0)
                .losingTrades(0)
                .trades(new ArrayList<>())
                .dailyReturns(new ArrayList<>())
                .detailedMetrics(new HashMap<>())
                .build();
        }
    }
    
    // 헬퍼 메서드들
    private Stock getCurrentStock(List<Stock> stockHistory, LocalDateTime date) {
        return stockHistory.stream()
            .filter(stock -> stock.getTimestamp().toLocalDate().equals(date.toLocalDate()))
            .findFirst()
            .orElse(null);
    }
    
    private List<Stock> generateSampleData(String symbol) {
        // 임시 샘플 데이터 생성 (실제로는 데이터베이스에서 가져옴)
        List<Stock> sampleData = new ArrayList<>();
        LocalDateTime date = LocalDateTime.now().minusYears(1);
        BigDecimal basePrice = BigDecimal.valueOf(100.0);
        
        LocalDateTime endDate = LocalDateTime.now();
        while (date.isBefore(endDate)) {
            // 랜덤 가격 변동
            double change = (Math.random() - 0.5) * 0.05; // ±2.5% 변동
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.0 + change));
            
            Stock stock = Stock.builder()
                .symbol(symbol)
                .openPrice(basePrice)
                .closePrice(basePrice)
                .highPrice(basePrice.multiply(BigDecimal.valueOf(1.01)))
                .lowPrice(basePrice.multiply(BigDecimal.valueOf(0.99)))
                .volume(BigDecimal.valueOf(1000000 + Math.random() * 500000).longValue())
                .timestamp(date)
                .build();
                
            sampleData.add(stock);
            date = date.plusDays(1);
        }
        
        return sampleData;
    }
    
    private TechnicalIndicators calculateTechnicalIndicators(List<Stock> history) {
        // 기술적 지표 계산 (단순화)
        if (history.size() < 20) {
            return TechnicalIndicators.builder().build();
        }
        
        List<BigDecimal> closes = history.stream()
            .map(Stock::getClosePrice)
            .toList();
        
        // RSI 계산 (14일)
        BigDecimal rsi = calculateRSI(closes, 14);
        
        // 이동평균 계산
        BigDecimal sma20 = calculateSMA(closes, 20);
        BigDecimal ema12 = calculateEMA(closes, 12);
        BigDecimal ema26 = calculateEMA(closes, 26);
        
        // MACD 계산
        BigDecimal macd = ema12.subtract(ema26);
        
        return TechnicalIndicators.builder()
            .rsi(rsi)
            .macd(macd)
            .ema12(ema12)
            .ema26(ema26)
            .sma20(sma20)
            .build();
    }
    
    private VolatilityAnalysis calculateVolatilityAnalysis(List<Stock> history) {
        if (history.size() < 20) {
            return VolatilityAnalysis.builder().build();
        }
        
        List<BigDecimal> returns = calculateReturns(history);
        BigDecimal volatility = calculateStandardDeviation(returns);
        
        return VolatilityAnalysis.builder()
            .historicalVolatility(volatility.doubleValue())
            .realizedVolatility(volatility)
            .volatilityRegime(volatility.compareTo(BigDecimal.valueOf(0.02)) > 0 ? 
                VolatilityAnalysis.VolatilityRegime.HIGH : VolatilityAnalysis.VolatilityRegime.LOW)
            .build();
    }
    
    private List<BigDecimal> calculateReturns(List<Stock> history) {
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            BigDecimal prevPrice = history.get(i-1).getClosePrice();
            BigDecimal currentPrice = history.get(i).getClosePrice();
            BigDecimal dailyReturn = currentPrice.subtract(prevPrice).divide(prevPrice, 6, RoundingMode.HALF_UP);
            returns.add(dailyReturn);
        }
        return returns;
    }
    
    private BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) return BigDecimal.valueOf(50);
        
        BigDecimal totalGain = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i-1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                totalGain = totalGain.add(change);
            } else {
                totalLoss = totalLoss.add(change.abs());
            }
        }
        
        if (totalLoss.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100);
        
        BigDecimal rs = totalGain.divide(totalLoss, 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
    }
    
    private BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        return prices.subList(prices.size() - period, prices.size()).stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = prices.get(prices.size() - period);
        
        for (int i = prices.size() - period + 1; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema;
    }
    
    /**
     * 백테스팅용 포트폴리오 클래스
     */
    private static class TradingPortfolio {
        private BigDecimal cash;
        private final Map<String, Position> positions = new HashMap<>();
        
        public TradingPortfolio(BigDecimal initialCash) {
            this.cash = initialCash;
        }
        
        public BigDecimal getCash() { return cash; }
        public void setCash(BigDecimal cash) { this.cash = cash; }
        
        public void addPosition(Position position) {
            positions.put(position.getSymbol(), position);
        }
        
        public void removePosition(String symbol) {
            positions.remove(symbol);
        }
        
        public Position getPosition(String symbol) {
            return positions.get(symbol);
        }
        
        public Collection<Position> getCurrentPositions() {
            return positions.values();
        }
    }
    
    /**
     * 표준편차 계산 (변동성 측정)
     */
    private BigDecimal calculateStandardDeviation(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 평균 계산
        BigDecimal sum = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        // 분산 계산
        BigDecimal variance = returns.stream()
            .map(r -> r.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        // 표준편차 계산 (제곱근)
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
}