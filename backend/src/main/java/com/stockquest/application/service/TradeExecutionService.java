package com.stockquest.application.service;

import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import com.stockquest.domain.execution.Order.ExecutionAlgorithm;
import com.stockquest.domain.execution.Order.ExecutionParameters;
import com.stockquest.domain.execution.Order.ExecutionStatistics;
import com.stockquest.domain.execution.Trade.TradeSide;
import com.stockquest.domain.execution.Trade.TradeType;
import com.stockquest.domain.execution.Trade.TradeStatus;
import com.stockquest.domain.execution.Trade.ExecutionVenue;
import com.stockquest.domain.execution.Trade.ExecutionQualityMetrics;
import com.stockquest.domain.execution.Trade.MarketConditions;
import com.stockquest.application.event.TradeExecutedEvent;
import com.stockquest.application.event.OrderScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 거래 실행 엔진 (Trade Execution Engine)
 * Phase 8.4: Real-time Execution Engine - Advanced Trade Execution
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeExecutionService {

    private final Map<String, Order> executionQueue = new ConcurrentHashMap<>();
    private final Map<String, ExecutionContext> executionContexts = new ConcurrentHashMap<>();
    private final RealTimeMarketDataService realTimeMarketDataService;
    private final ApplicationEventPublisher eventPublisher;
    
    // Additional dependencies (commented for now)
    // private final MarketDataService marketDataService;
    
    /**
     * 주문 실행 스케줄링 이벤트 리스너
     */
    @EventListener
    @Async
    public void handleOrderScheduled(OrderScheduledEvent event) {
        scheduleExecution(event.getOrder());
    }

    /**
     * 주문 실행 스케줄링
     */
    private void scheduleExecution(Order order) {
        log.info("주문 실행 스케줄링: {} - Algorithm: {}", 
            order.getOrderId(), order.getExecutionAlgorithm().getShortName());
        
        ExecutionContext context = createExecutionContext(order);
        executionContexts.put(order.getOrderId(), context);
        executionQueue.put(order.getOrderId(), order);
        
        // 알고리즘 별 초기화
        initializeAlgorithm(order, context);
    }
    
    /**
     * 실행 컨텍스트 생성
     */
    private ExecutionContext createExecutionContext(Order order) {
        return ExecutionContext.builder()
            .orderId(order.getOrderId())
            .symbol(order.getSymbol())
            .algorithm(order.getExecutionAlgorithm())
            .parameters(order.getExecutionParameters())
            .startTime(LocalDateTime.now())
            .remainingQuantity(order.getQuantity())
            .executedQuantity(BigDecimal.ZERO)
            .totalExecutionValue(BigDecimal.ZERO)
            .tradeCount(0)
            .build();
    }
    
    /**
     * 알고리즘 초기화
     */
    private void initializeAlgorithm(Order order, ExecutionContext context) {
        switch (order.getExecutionAlgorithm()) {
            case TWAP -> initializeTWAP(order, context);
            case VWAP -> initializeVWAP(order, context);
            case IMPLEMENTATION_SHORTFALL -> initializeIS(order, context);
            case PARTICIPATION_RATE -> initializePOV(order, context);
            case ICEBERG -> initializeIceberg(order, context);
            case SMART_ORDER_ROUTING -> initializeSOR(order, context);
            default -> initializeSimpleExecution(order, context);
        }
    }
    
    /**
     * 주기적 실행 엔진 (매 초마다 실행)
     */
    @Scheduled(fixedRate = 1000)
    public void executeScheduledOrders() {
        executionQueue.values().parallelStream()
            .filter(order -> order.isActive())
            .forEach(this::processOrderExecution);
    }
    
    /**
     * 주문 실행 처리
     */
    private void processOrderExecution(Order order) {
        ExecutionContext context = executionContexts.get(order.getOrderId());
        if (context == null) {
            log.error("실행 컨텍스트를 찾을 수 없음: {}", order.getOrderId());
            return;
        }
        
        try {
            // 먼저 기본 주문 타입에 따라 처리
            if (order.getOrderType() == Order.OrderType.MARKET) {
                executeMarketOrder(order, context);
            } else if (order.getOrderType() == Order.OrderType.LIMIT) {
                executeLimitOrder(order, context);
            } else {
                // 알고리즘 주문의 경우 ExecutionAlgorithm에 따라 처리
                switch (order.getExecutionAlgorithm()) {
                    case TWAP -> executeTWAP(order, context);
                    case VWAP -> executeVWAP(order, context);
                    case IMPLEMENTATION_SHORTFALL -> executeIS(order, context);
                    case PARTICIPATION_RATE -> executePOV(order, context);
                    case ICEBERG -> executeIceberg(order, context);
                    case SMART_ORDER_ROUTING -> executeSOR(order, context);
                    default -> executeSimpleOrder(order, context);
                }
            }
        } catch (Exception e) {
            log.error("주문 실행 중 오류 발생: {} - {}", order.getOrderId(), e.getMessage(), e);
            handleExecutionError(order, context, e);
        }
    }
    
    /**
     * TWAP (Time-Weighted Average Price) 알고리즘 실행
     */
    private void executeTWAP(Order order, ExecutionContext context) {
        if (!shouldExecuteTWAPSlice(order, context)) {
            return;
        }
        
        BigDecimal sliceSize = calculateTWAPSliceSize(order, context);
        BigDecimal executionPrice = getCurrentMarketPrice(order.getSymbol());
        
        Trade trade = createTrade(order, sliceSize, executionPrice, TradeType.ALGORITHM, context);
        executeTrade(trade, context);
        
        log.debug("TWAP 슬라이스 실행: {} - Size: {}, Price: {}", 
            order.getOrderId(), sliceSize, executionPrice);
    }
    
    /**
     * VWAP (Volume-Weighted Average Price) 알고리즘 실행
     */
    private void executeVWAP(Order order, ExecutionContext context) {
        BigDecimal marketVolume = getCurrentMarketVolume(order.getSymbol());
        BigDecimal participationRate = order.getExecutionParameters().getParticipationRate();
        
        BigDecimal sliceSize = marketVolume.multiply(participationRate)
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        
        // 남은 수량보다 크면 조정
        if (sliceSize.compareTo(context.getRemainingQuantity()) > 0) {
            sliceSize = context.getRemainingQuantity();
        }
        
        BigDecimal executionPrice = getCurrentMarketPrice(order.getSymbol());
        Trade trade = createTrade(order, sliceSize, executionPrice, TradeType.ALGORITHM, context);
        executeTrade(trade, context);
        
        log.debug("VWAP 슬라이스 실행: {} - Size: {}, Market Volume: {}", 
            order.getOrderId(), sliceSize, marketVolume);
    }
    
    /**
     * Implementation Shortfall 알고리즘 실행
     */
    private void executeIS(Order order, ExecutionContext context) {
        BigDecimal arrivalPrice = context.getArrivalPrice();
        BigDecimal currentPrice = getCurrentMarketPrice(order.getSymbol());
        BigDecimal urgency = order.getExecutionParameters().getUrgencyLevel();
        
        // 시장 영향과 타이밍 비용을 고려한 실행 결정
        BigDecimal sliceSize = calculateISSliceSize(order, context, urgency);
        
        Trade trade = createTrade(order, sliceSize, currentPrice, TradeType.ALGORITHM, context);
        executeTrade(trade, context);
        
        log.debug("IS 슬라이스 실행: {} - Size: {}, Urgency: {}", 
            order.getOrderId(), sliceSize, urgency);
    }
    
    /**
     * Participation of Volume (POV) 알고리즘 실행
     */
    private void executePOV(Order order, ExecutionContext context) {
        BigDecimal marketVolume = getCurrentMarketVolume(order.getSymbol());
        BigDecimal participationRate = order.getExecutionParameters().getParticipationRate();
        
        BigDecimal targetVolume = marketVolume.multiply(participationRate)
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        
        BigDecimal sliceSize = targetVolume.min(context.getRemainingQuantity());
        
        if (sliceSize.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal executionPrice = getCurrentMarketPrice(order.getSymbol());
            Trade trade = createTrade(order, sliceSize, executionPrice, TradeType.ALGORITHM, context);
            executeTrade(trade, context);
        }
        
        log.debug("POV 슬라이스 실행: {} - Size: {}, Target Rate: {}%", 
            order.getOrderId(), sliceSize, participationRate);
    }
    
    /**
     * Iceberg 알고리즘 실행
     */
    private void executeIceberg(Order order, ExecutionContext context) {
        BigDecimal icebergSize = order.getExecutionParameters().getMaxOrderSize();
        
        if (context.getCurrentIcebergSlice() == null || 
            context.getCurrentIcebergSlice().compareTo(BigDecimal.ZERO) <= 0) {
            // 새로운 아이스버그 슬라이스 생성
            BigDecimal newSliceSize = icebergSize.min(context.getRemainingQuantity());
            context.setCurrentIcebergSlice(newSliceSize);
        }
        
        // 현재 슬라이스 실행
        BigDecimal executionPrice = getCurrentMarketPrice(order.getSymbol());
        BigDecimal executeSize = context.getCurrentIcebergSlice().min(
            order.getExecutionParameters().getMinOrderSize());
        
        Trade trade = createTrade(order, executeSize, executionPrice, TradeType.ICEBERG, context);
        executeTrade(trade, context);
        
        context.setCurrentIcebergSlice(context.getCurrentIcebergSlice().subtract(executeSize));
    }
    
    /**
     * Smart Order Routing (SOR) 알고리즘 실행
     */
    private void executeSOR(Order order, ExecutionContext context) {
        // 최적 실행 장소 결정
        ExecutionVenue bestVenue = selectBestVenue(order);
        
        BigDecimal sliceSize = calculateOptimalSliceSize(order, context, bestVenue);
        BigDecimal bestPrice = getBestPrice(order.getSymbol(), bestVenue);
        
        Trade trade = createTrade(order, sliceSize, bestPrice, TradeType.REGULAR, context);
        trade.setExecutionVenue(bestVenue);
        executeTrade(trade, context);
        
        log.debug("SOR 실행: {} - Venue: {}, Size: {}", 
            order.getOrderId(), bestVenue.getKoreanName(), sliceSize);
    }
    
    /**
     * 시장가 주문 실행
     */
    private void executeMarketOrder(Order order, ExecutionContext context) {
        BigDecimal marketPrice = getCurrentMarketPrice(order.getSymbol());
        BigDecimal executeSize = context.getRemainingQuantity();
        
        Trade trade = createTrade(order, executeSize, marketPrice, TradeType.REGULAR, context);
        executeTrade(trade, context);
        
        // 시장가 주문은 즉시 완전 실행
        completeOrderExecution(order, context);
    }
    
    /**
     * 지정가 주문 실행
     */
    private void executeLimitOrder(Order order, ExecutionContext context) {
        BigDecimal currentPrice = getCurrentMarketPrice(order.getSymbol());
        BigDecimal limitPrice = order.getPrice();
        
        boolean canExecute = (order.getSide() == Order.OrderSide.BUY && 
                             currentPrice.compareTo(limitPrice) <= 0) ||
                            (order.getSide() == Order.OrderSide.SELL && 
                             currentPrice.compareTo(limitPrice) >= 0);
        
        if (canExecute) {
            BigDecimal executeSize = context.getRemainingQuantity();
            Trade trade = createTrade(order, executeSize, limitPrice, TradeType.REGULAR, context);
            executeTrade(trade, context);
            
            completeOrderExecution(order, context);
        }
    }
    
    /**
     * 거래 생성
     */
    private Trade createTrade(Order order, BigDecimal quantity, BigDecimal price, 
                             TradeType tradeType, ExecutionContext context) {
        
        return Trade.builder()
            .tradeId(generateTradeId())
            .orderId(order.getOrderId())
            .portfolioId(order.getPortfolioId())
            .userId(order.getUserId())
            .symbol(order.getSymbol())
            .side(mapOrderSideToTradeSide(order.getSide()))
            .quantity(quantity)
            .price(price)
            .amount(quantity.multiply(price))
            .tradeTime(LocalDateTime.now())
            .tradeType(tradeType)
            .executionVenue(ExecutionVenue.SIMULATION)
            .status(TradeStatus.PENDING)
            .marketConditions(getCurrentMarketConditions(order.getSymbol()))
            .executionMetrics(ExecutionQualityMetrics.builder()
                .realizedSlippage(BigDecimal.ZERO)
                .marketImpact(calculateMarketImpact(quantity, order.getSymbol()))
                .executionSpeed(1000L) // 1초
                .build())
            .build();
    }
    
    /**
     * 거래 실행
     */
    private void executeTrade(Trade trade, ExecutionContext context) {
        log.info("거래 실행: {} - {} {} shares at {}", 
            trade.getTradeId(), trade.getSide().getKoreanName(), 
            trade.getQuantity(), trade.getPrice());
        
        // 실제 시장 연결 시뮬레이션
        simulateMarketExecution(trade);
        
        // 실행 컨텍스트 업데이트
        updateExecutionContext(context, trade);

        // 체결 이벤트 발행
        eventPublisher.publishEvent(new TradeExecutedEvent(this, trade.getOrderId(), trade));
    }
    
    /**
     * 시장 실행 시뮬레이션
     */
    private void simulateMarketExecution(Trade trade) {
        // 실제 환경에서는 시장 연결 및 실행
        trade.setStatus(TradeStatus.EXECUTED);
        trade.setSettlementDate(LocalDateTime.now().toLocalDate().plusDays(2));
        
        // 수수료 및 세금 계산
        calculateTradeCommissionAndTax(trade);
    }
    
    /**
     * 실행 컨텍스트 업데이트
     */
    private void updateExecutionContext(ExecutionContext context, Trade trade) {
        context.setExecutedQuantity(context.getExecutedQuantity().add(trade.getQuantity()));
        context.setRemainingQuantity(context.getRemainingQuantity().subtract(trade.getQuantity()));
        context.setTotalExecutionValue(context.getTotalExecutionValue().add(trade.getAmount()));
        context.setTradeCount(context.getTradeCount() + 1);
        context.setLastTradeTime(LocalDateTime.now());
        
        // VWAP 업데이트
        updateVWAP(context, trade);
    }
    
    // Helper Methods
    
    private void initializeTWAP(Order order, ExecutionContext context) {
        Integer period = order.getExecutionParameters().getExecutionPeriod();
        context.setTwapPeriodMinutes(period != null ? period : 60);
        context.setTwapSliceInterval(context.getTwapPeriodMinutes() * 60 / 20); // 20 slices
    }
    
    private void initializeVWAP(Order order, ExecutionContext context) {
        context.setVwap(BigDecimal.ZERO);
        context.setVwapVolume(BigDecimal.ZERO);
    }
    
    private void initializeIS(Order order, ExecutionContext context) {
        context.setArrivalPrice(getCurrentMarketPrice(order.getSymbol()));
    }
    
    private void initializePOV(Order order, ExecutionContext context) {
        // POV 특별 초기화 없음
    }
    
    private void initializeIceberg(Order order, ExecutionContext context) {
        context.setCurrentIcebergSlice(BigDecimal.ZERO);
    }
    
    private void initializeSOR(Order order, ExecutionContext context) {
        // SOR 특별 초기화 없음
    }
    
    private void initializeSimpleExecution(Order order, ExecutionContext context) {
        // Simple execution 특별 초기화 없음
    }
    
    private void executeSimpleOrder(Order order, ExecutionContext context) {
        executeMarketOrder(order, context);
    }
    
    private boolean shouldExecuteTWAPSlice(Order order, ExecutionContext context) {
        if (context.getLastSliceTime() == null) {
            context.setLastSliceTime(LocalDateTime.now());
            return true;
        }
        
        long secondsSinceLastSlice = java.time.Duration.between(
            context.getLastSliceTime(), LocalDateTime.now()).getSeconds();
        
        return secondsSinceLastSlice >= context.getTwapSliceInterval();
    }
    
    private BigDecimal calculateTWAPSliceSize(Order order, ExecutionContext context) {
        Integer totalSlices = 20; // Fixed number of slices
        return context.getRemainingQuantity().divide(BigDecimal.valueOf(totalSlices), 
            0, RoundingMode.UP);
    }
    
    private BigDecimal calculateISSliceSize(Order order, ExecutionContext context, 
                                          BigDecimal urgency) {
        // Urgency 레벨에 따른 실행 크기 결정
        BigDecimal baseSize = context.getRemainingQuantity().divide(BigDecimal.valueOf(10), 
            0, RoundingMode.UP);
        
        return baseSize.multiply(urgency).max(
            order.getExecutionParameters().getMinOrderSize());
    }
    
    private ExecutionVenue selectBestVenue(Order order) {
        // 최적 실행 장소 선택 로직
        return ExecutionVenue.KRX; // 임시로 KRX 반환
    }
    
    private BigDecimal calculateOptimalSliceSize(Order order, ExecutionContext context, 
                                               ExecutionVenue venue) {
        return context.getRemainingQuantity().divide(BigDecimal.valueOf(5), 
            0, RoundingMode.UP);
    }
    
    private BigDecimal getBestPrice(String symbol, ExecutionVenue venue) {
        return getCurrentMarketPrice(symbol);
    }
    
    private void completeOrderExecution(Order order, ExecutionContext context) {
        executionQueue.remove(order.getOrderId());
        executionContexts.remove(order.getOrderId());
        
        log.info("주문 실행 완료: {} - Total trades: {}", 
            order.getOrderId(), context.getTradeCount());
    }
    
    private void handleExecutionError(Order order, ExecutionContext context, Exception e) {
        log.error("실행 오류 처리: {} - {}", order.getOrderId(), e.getMessage());
        
        try {
            // 부분 실행된 거래가 있다면 기록
            if (context.getTradeCount() > 0) {
                log.warn("부분 실행됨: {} - {} 거래 완료, 남은 수량: {}", 
                    order.getOrderId(), context.getTradeCount(), context.getRemainingQuantity());
            }
            
            // 실행 대기열에서 제거
            executionQueue.remove(order.getOrderId());
            
            // 간단한 재시도 로직 (재시도 가능한 오류의 경우)
            if (isRetryableError(e)) {
                log.info("재시도 가능한 오류 감지: {} - 5초 후 재시도 스케줄링", order.getOrderId());
                
                // 5초 후 재시도 스케줄링
                CompletableFuture.delayedExecutor(5, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(() -> {
                        log.info("주문 재시도 실행: {}", order.getOrderId());
                        executionQueue.put(order.getOrderId(), order);
                    });
            } else {
                log.error("복구 불가능한 오류: {} - 주문 처리 중단", order.getOrderId());
            }
            
        } catch (Exception innerException) {
            log.error("오류 처리 중 추가 오류 발생: {}", innerException.getMessage());
        }
    }
    
    private boolean isRetryableError(Exception e) {
        // 재시도 가능한 오류 타입 확인
        String message = e.getMessage();
        return e instanceof java.util.concurrent.TimeoutException ||
               e instanceof java.net.ConnectException ||
               (message != null && (message.contains("timeout") || 
                                   message.contains("connection") || 
                                   message.contains("temporary")));
    }
    
    private BigDecimal getCurrentMarketPrice(String symbol) {
        try {
            return realTimeMarketDataService.getCurrentMarketData(symbol).getPrice();
        } catch (Exception e) {
            log.warn("실시간 시장 가격 조회 실패: {} - 기본값 사용", symbol, e);
            return BigDecimal.valueOf(100.0); // Fallback price
        }
    }
    
    private BigDecimal getCurrentMarketVolume(String symbol) {
        try {
            Long volume = realTimeMarketDataService.getCurrentMarketData(symbol).getVolume();
            return volume != null ? BigDecimal.valueOf(volume) : BigDecimal.valueOf(10000);
        } catch (Exception e) {
            log.warn("실시간 시장 볼륨 조회 실패: {} - 기본값 사용", symbol, e);
            return BigDecimal.valueOf(10000); // Fallback volume
        }
    }
    
    private MarketConditions getCurrentMarketConditions(String symbol) {
        return MarketConditions.builder()
            .marketPrice(getCurrentMarketPrice(symbol))
            .volumeInfo(Trade.VolumeInfo.builder()
                .currentVolume(getCurrentMarketVolume(symbol))
                .averageDailyVolume(BigDecimal.valueOf(50000))
                .build())
            .volatility(BigDecimal.valueOf(0.25))
            .liquidityLevel(BigDecimal.valueOf(0.8))
            .bidAskSpread(BigDecimal.valueOf(0.01))
            .build();
    }
    
    private BigDecimal calculateMarketImpact(BigDecimal quantity, String symbol) {
        BigDecimal dailyVolume = BigDecimal.valueOf(50000);
        BigDecimal participationRate = quantity.divide(dailyVolume, 6, RoundingMode.HALF_UP);
        
        // 간단한 시장 영향 모델: sqrt(participation_rate) * 0.5%
        return BigDecimal.valueOf(Math.sqrt(participationRate.doubleValue()) * 0.005);
    }
    
    private void calculateTradeCommissionAndTax(Trade trade) {
        try {
            BigDecimal tradeValue = trade.getPrice().multiply(trade.getQuantity());
            
            // 거래 수수료 계산 (0.1% 기본, 최소 $1)
            BigDecimal commissionRate = BigDecimal.valueOf(0.001); // 0.1%
            BigDecimal calculatedCommission = tradeValue.multiply(commissionRate);
            BigDecimal minCommission = BigDecimal.valueOf(1.00);
            BigDecimal commission = calculatedCommission.max(minCommission);
            
            // 거래 세금 계산 (매도시에만 적용, 0.05%)
            BigDecimal tax = BigDecimal.ZERO;
            if (trade.getSide() == TradeSide.SELL) {
                BigDecimal taxRate = BigDecimal.valueOf(0.0005); // 0.05%
                tax = tradeValue.multiply(taxRate);
            }
            
            // SEC fee 계산 (매도시, 매우 작은 비율)
            BigDecimal secFee = BigDecimal.ZERO;
            if (trade.getSide() == TradeSide.SELL) {
                BigDecimal secRate = BigDecimal.valueOf(0.0000231); // SEC rate
                secFee = tradeValue.multiply(secRate);
            }
            
            // 총 비용 계산
            BigDecimal totalCosts = commission.add(tax).add(secFee);
            
            // 거래 비용 정보 로깅 (실제 구현에서는 Trade 엔티티에 비용 필드 추가 필요)
            log.debug("거래 비용 계산 완료: {} - Commission: {}, Tax: {}, SEC Fee: {}, Total: {}", 
                trade.getTradeId(), commission, tax, secFee, totalCosts);
            
            log.info("거래 {} - 가격: {}, 수량: {}, 수수료: {}, 세금: {}, 총 비용: {}", 
                trade.getTradeId(), trade.getPrice(), trade.getQuantity(), 
                commission, tax, totalCosts);
                
        } catch (Exception e) {
            log.error("수수료/세금 계산 실패: {} - {}", trade.getTradeId(), e.getMessage());
        }
    }
    
    private void updateVWAP(ExecutionContext context, Trade trade) {
        BigDecimal newTotalValue = context.getVwap().multiply(context.getVwapVolume())
            .add(trade.getPrice().multiply(trade.getQuantity()));
        BigDecimal newTotalVolume = context.getVwapVolume().add(trade.getQuantity());
        
        context.setVwap(newTotalValue.divide(newTotalVolume, 6, RoundingMode.HALF_UP));
        context.setVwapVolume(newTotalVolume);
    }
    
    private TradeSide mapOrderSideToTradeSide(Order.OrderSide orderSide) {
        return switch (orderSide) {
            case BUY -> TradeSide.BUY;
            case SELL -> TradeSide.SELL;
            case SELL_SHORT -> TradeSide.SELL_SHORT;
            case BUY_TO_COVER -> TradeSide.BUY_TO_COVER;
        };
    }
    
    private String generateTradeId() {
        return "TRD-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 1000);
    }
    
    /**
     * 실행 컨텍스트 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutionContext {
        private String orderId;
        private String symbol;
        private ExecutionAlgorithm algorithm;
        private ExecutionParameters parameters;
        private LocalDateTime startTime;
        private LocalDateTime lastTradeTime;
        private LocalDateTime lastSliceTime;
        
        private BigDecimal remainingQuantity;
        private BigDecimal executedQuantity;
        private BigDecimal totalExecutionValue;
        private Integer tradeCount;
        
        // Algorithm-specific fields
        private Integer twapPeriodMinutes;
        private Integer twapSliceInterval;
        private BigDecimal vwap;
        private BigDecimal vwapVolume;
        private BigDecimal arrivalPrice;
        private BigDecimal currentIcebergSlice;
    }
}