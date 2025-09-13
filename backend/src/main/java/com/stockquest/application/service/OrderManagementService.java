package com.stockquest.application.service;

import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import com.stockquest.domain.execution.Order.OrderStatus;
import com.stockquest.domain.execution.Order.OrderEvent;
import com.stockquest.domain.execution.Order.OrderEventType;
import com.stockquest.domain.execution.Order.RiskValidationResult;
import com.stockquest.domain.order.port.OrderRepository;
import com.stockquest.application.event.TradeExecutedEvent;
import com.stockquest.application.event.OrderScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 주문 관리 시스템 (Order Management System)
 * Phase 8.4: Real-time Execution Engine - Core OMS Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final Map<String, Order> activeOrders = new ConcurrentHashMap<>();
    private final Map<String, List<Order>> userOrders = new ConcurrentHashMap<>();
    private final Map<String, List<Order>> portfolioOrders = new ConcurrentHashMap<>();
    
    // Dependencies would be injected
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    // private final RiskManagementService riskService;
    
    /**
     * 새로운 주문 제출
     */
    @Async
    public CompletableFuture<Order> submitOrder(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("주문 제출 시작: {} - {} {} shares of {}", 
                    order.getOrderId(), order.getSide().getKoreanName(), 
                    order.getQuantity(), order.getSymbol());
                
                // 1. 주문 유효성 검증
                if (!order.isValid()) {
                    log.error("주문 유효성 검증 실패: {}", order.getOrderId());
                    order.updateStatus(OrderStatus.REJECTED, "Invalid order parameters");
                    return order;
                }
                
                // 2. 리스크 검증
                RiskValidationResult riskResult = performRiskValidation(order);
                order.setRiskValidation(riskResult);
                
                if (!riskResult.isPassed()) {
                    log.warn("주문 리스크 검증 실패: {} - {}", order.getOrderId(), 
                        String.join(", ", riskResult.getViolations()));
                    order.updateStatus(OrderStatus.REJECTED, "Risk validation failed");
                    return order;
                }
                
                // 3. 주문 접수
                order.setCreatedAt(LocalDateTime.now());
                order.setLastUpdatedAt(LocalDateTime.now());
                order.updateStatus(OrderStatus.NEW, "Order accepted and active");
                
                // 4. 주문 저장 및 추적
                activeOrders.put(order.getOrderId(), order);
                addToUserOrders(order.getUserId(), order);
                addToPortfolioOrders(order.getPortfolioId(), order);
                
                // 5. 실행 큐에 추가
                scheduleForExecution(order);
                
                log.info("주문 접수 완료: {} - Status: {}", order.getOrderId(), 
                    order.getStatus().getKoreanName());
                
                return order;
                
            } catch (Exception e) {
                log.error("주문 제출 실패: {} - {}", order.getOrderId(), e.getMessage(), e);
                order.updateStatus(OrderStatus.REJECTED, "System error: " + e.getMessage());
                return order;
            }
        });
    }
    
    /**
     * 주문 취소
     */
    @Async
    public CompletableFuture<Order> cancelOrder(String orderId, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = activeOrders.get(orderId);
            
            if (order == null) {
                log.warn("취소 요청된 주문을 찾을 수 없음: {}", orderId);
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            
            if (order.isTerminal()) {
                log.warn("이미 완료된 주문 취소 시도: {} - Status: {}", 
                    orderId, order.getStatus().getKoreanName());
                return order;
            }
            
            log.info("주문 취소 처리: {} - Reason: {}", orderId, reason);
            
            // 부분 체결된 경우 남은 수량만 취소
            order.updateRemainingQuantity();
            order.updateStatus(OrderStatus.CANCELED, "Canceled: " + reason);
            
            // 활성 주문 목록에서 제거
            activeOrders.remove(orderId);
            
            return order;
        });
    }
    
    /**
     * 주문 수정 (Replace)
     */
    @Async
    public CompletableFuture<Order> replaceOrder(String orderId, Order newOrder) {
        return CompletableFuture.supplyAsync(() -> {
            // 기존 주문 취소 후 새 주문 제출
            Order canceledOrder = cancelOrder(orderId, "Replaced with new order").join();
            
            log.info("주문 수정: {} → {}", orderId, newOrder.getOrderId());
            
            // 새 주문에 원래 주문 정보 연결
            newOrder.getMetadata().put("replacedOrderId", orderId);
            
            return submitOrder(newOrder).join();
        });
    }
    
    /**
     * 주문 상태 조회
     */
    public Optional<Order> getOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order != null) {
            return Optional.of(order);
        }
        
        // 데이터베이스에서 완료된 주문 조회
        try {
            Long orderIdLong = Long.valueOf(orderId);
            Optional<com.stockquest.domain.order.Order> dbOrder = orderRepository.findById(orderIdLong);
            
            if (dbOrder.isPresent()) {
                Order executionOrder = mapToExecutionOrder(dbOrder.get());
                log.debug("주문 조회 성공: {} - Found in database", orderId);
                return Optional.of(executionOrder);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid order ID format: {} - Expected numeric ID", orderId);
        } catch (Exception e) {
            log.error("Failed to query order from database: {}", orderId, e);
        }
        
        log.debug("주문 조회: {} - Not found in active orders or database", orderId);
        return Optional.empty();
    }
    
    /**
     * 사용자별 주문 목록 조회
     */
    public List<Order> getUserOrders(String userId) {
        return userOrders.getOrDefault(userId, List.of());
    }
    
    /**
     * 포트폴리오별 주문 목록 조회
     */
    public List<Order> getPortfolioOrders(String portfolioId) {
        return portfolioOrders.getOrDefault(portfolioId, List.of());
    }
    
    /**
     * 활성 주문 목록 조회
     */
    public List<Order> getActiveOrders() {
        return new ArrayList<>(activeOrders.values());
    }
    
    /**
     * 주문 체결 처리 (이벤트 리스너)
     */
    @EventListener
    @Async
    public void handleTradeExecuted(TradeExecutedEvent event) {
        processTradeFill(event.getOrderId(), event.getTrade());
    }

    /**
     * 주문 체결 처리
     */
    private void processTradeFill(String orderId, Trade trade) {
        Order order = activeOrders.get(orderId);
        
        if (order == null) {
            log.error("체결 처리할 주문을 찾을 수 없음: {}", orderId);
            return;
        }
        
        synchronized (order) {
            log.info("주문 체결 처리: {} - Trade: {} shares at {}", 
                orderId, trade.getQuantity(), trade.getPrice());
            
            // 체결 수량 업데이트
            BigDecimal previousExecuted = order.getExecutedQuantity() != null ? 
                order.getExecutedQuantity() : BigDecimal.ZERO;
            BigDecimal newExecuted = previousExecuted.add(trade.getQuantity());
            
            order.setExecutedQuantity(newExecuted);
            order.updateRemainingQuantity();
            
            // 평균 체결 가격 업데이트
            updateAverageExecutionPrice(order, trade);
            
            // 거래 목록에 추가
            if (order.getTrades() == null) {
                order.setTrades(new ArrayList<>());
            }
            order.getTrades().add(trade);
            
            // 주문 상태 업데이트
            if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                order.updateStatus(OrderStatus.FILLED, "Order fully executed");
                activeOrders.remove(orderId);
            } else {
                order.updateStatus(OrderStatus.PARTIALLY_FILLED, 
                    "Partially executed: " + newExecuted + "/" + order.getQuantity());
            }
            
            // 실행 통계 업데이트
            updateExecutionStatistics(order);
        }
    }
    
    /**
     * 리스크 검증 수행
     */
    private RiskValidationResult performRiskValidation(Order order) {
        log.debug("리스크 검증 수행: {}", order.getOrderId());
        
        List<Order.RiskCheckResult> riskChecks = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        boolean overallPassed = true;
        String overallRiskLevel = "LOW";
        
        // 1. 포지션 한도 검증
        Order.RiskCheckResult positionCheck = validatePositionLimits(order);
        riskChecks.add(positionCheck);
        if (!positionCheck.isPassed()) {
            overallPassed = false;
            violations.add("Position limit exceeded: " + positionCheck.getDescription());
        } else if ("MEDIUM".equals(positionCheck.getSeverity())) {
            warnings.add("Position size approaching limit: " + positionCheck.getDescription());
            if ("LOW".equals(overallRiskLevel)) overallRiskLevel = "MEDIUM";
        }
        
        // 2. 집중도 한도 검증
        Order.RiskCheckResult concentrationCheck = validateConcentrationLimits(order);
        riskChecks.add(concentrationCheck);
        if (!concentrationCheck.isPassed()) {
            overallPassed = false;
            violations.add("Concentration limit exceeded: " + concentrationCheck.getDescription());
        } else if ("MEDIUM".equals(concentrationCheck.getSeverity())) {
            warnings.add("Concentration risk detected: " + concentrationCheck.getDescription());
            if ("LOW".equals(overallRiskLevel)) overallRiskLevel = "MEDIUM";
        }
        
        // 3. 유동성 검증
        Order.RiskCheckResult liquidityCheck = validateLiquidity(order);
        riskChecks.add(liquidityCheck);
        if (!liquidityCheck.isPassed()) {
            overallPassed = false;
            violations.add("Liquidity validation failed: " + liquidityCheck.getDescription());
        } else if ("HIGH".equals(liquidityCheck.getSeverity())) {
            overallRiskLevel = "HIGH";
            warnings.add("High liquidity risk: " + liquidityCheck.getDescription());
        }
        
        // 4. 신용 한도 검증
        Order.RiskCheckResult creditCheck = validateCreditLimits(order);
        riskChecks.add(creditCheck);
        if (!creditCheck.isPassed()) {
            overallPassed = false;
            violations.add("Credit limit exceeded: " + creditCheck.getDescription());
        } else if ("MEDIUM".equals(creditCheck.getSeverity())) {
            warnings.add("Credit utilization high: " + creditCheck.getDescription());
            if ("LOW".equals(overallRiskLevel)) overallRiskLevel = "MEDIUM";
        }
        
        // 5. 주문 크기 검증
        Order.RiskCheckResult orderSizeCheck = validateOrderSize(order);
        riskChecks.add(orderSizeCheck);
        if (!orderSizeCheck.isPassed()) {
            overallPassed = false;
            violations.add("Order size validation failed: " + orderSizeCheck.getDescription());
        }
        
        if (overallPassed) {
            log.debug("리스크 검증 통과: {} - Risk Level: {}", order.getOrderId(), overallRiskLevel);
        } else {
            log.warn("리스크 검증 실패: {} - Violations: {}", order.getOrderId(), 
                String.join(", ", violations));
        }
        
        return RiskValidationResult.builder()
            .passed(overallPassed)
            .validationTime(LocalDateTime.now())
            .riskChecks(riskChecks)
            .overallRiskLevel(overallRiskLevel)
            .warnings(warnings)
            .violations(violations)
            .build();
    }
    
    /**
     * 실행 스케줄링
     */
    private void scheduleForExecution(Order order) {
        log.debug("주문 실행 스케줄링: {} - Algorithm: {}", 
            order.getOrderId(), order.getExecutionAlgorithm().getShortName());
        
        try {
            // 기본 실행 알고리즘이 없는 경우 NONE으로 설정
            if (order.getExecutionAlgorithm() == null) {
                order.setExecutionAlgorithm(Order.ExecutionAlgorithm.NONE);
            }
            
            // 기본 실행 파라미터가 없는 경우 기본값 설정
            if (order.getExecutionParameters() == null) {
                order.setExecutionParameters(createDefaultExecutionParameters(order));
            }
            
            // 주문 실행 스케줄링 이벤트 발행
            eventPublisher.publishEvent(new OrderScheduledEvent(this, order));
            
            log.info("주문 실행 스케줄링 완료: {} - Algorithm: {}", 
                order.getOrderId(), order.getExecutionAlgorithm().getShortName());
                
        } catch (Exception e) {
            log.error("주문 실행 스케줄링 실패: {} - {}", order.getOrderId(), e.getMessage(), e);
            
            // 스케줄링 실패 시 주문 상태를 거부로 변경
            order.updateStatus(Order.OrderStatus.REJECTED, 
                "Execution scheduling failed: " + e.getMessage());
        }
    }
    
    /**
     * 기본 실행 파라미터 생성
     */
    private Order.ExecutionParameters createDefaultExecutionParameters(Order order) {
        return Order.ExecutionParameters.builder()
            .participationRate(BigDecimal.valueOf(20)) // 20% 참여율
            .executionPeriod(60) // 60분 실행 기간
            .maxMarketImpact(BigDecimal.valueOf(0.005)) // 0.5% 최대 시장 영향
            .urgencyLevel(BigDecimal.valueOf(0.5)) // 중간 긴급도
            .priceTolerance(BigDecimal.valueOf(0.002)) // 0.2% 가격 허용범위
            .minOrderSize(BigDecimal.valueOf(1)) // 최소 주문 크기
            .maxOrderSize(BigDecimal.valueOf(1000)) // 최대 주문 크기
            .executionInterval(30) // 30초 실행 간격
            .riskLimit(BigDecimal.valueOf(0.1)) // 10% 리스크 한도
            .algorithmSettings(new HashMap<>()) // 빈 설정 맵
            .build();
    }
    
    /**
     * 사용자 주문 목록에 추가
     */
    private void addToUserOrders(String userId, Order order) {
        userOrders.computeIfAbsent(userId, k -> new ArrayList<>()).add(order);
    }
    
    /**
     * 포트폴리오 주문 목록에 추가
     */
    private void addToPortfolioOrders(String portfolioId, Order order) {
        portfolioOrders.computeIfAbsent(portfolioId, k -> new ArrayList<>()).add(order);
    }
    
    /**
     * 평균 체결 가격 업데이트
     */
    private void updateAverageExecutionPrice(Order order, Trade trade) {
        if (order.getAvgExecutionPrice() == null) {
            order.setAvgExecutionPrice(trade.getPrice());
        } else {
            BigDecimal totalValue = order.getAvgExecutionPrice()
                .multiply(order.getExecutedQuantity().subtract(trade.getQuantity()));
            BigDecimal newTradeValue = trade.getPrice().multiply(trade.getQuantity());
            
            BigDecimal newAvgPrice = totalValue.add(newTradeValue)
                .divide(order.getExecutedQuantity(), 6, java.math.RoundingMode.HALF_UP);
            
            order.setAvgExecutionPrice(newAvgPrice);
        }
    }
    
    /**
     * 실행 통계 업데이트
     */
    private void updateExecutionStatistics(Order order) {
        log.debug("실행 통계 업데이트: {} - Fill Rate: {}%", 
            order.getOrderId(), order.getFillRate());
        
        if (order.getTrades() == null || order.getTrades().isEmpty()) {
            log.debug("주문에 거래 내역이 없어 통계 계산을 건너뜁니다: {}", order.getOrderId());
            return;
        }
        
        // 실행 통계 계산
        Order.ExecutionStatistics stats = calculateExecutionStatistics(order);
        order.setExecutionStats(stats);
        
        log.info("실행 통계 완료: {} - Quality Score: {}, Slippage: {}%, Execution Time: {}ms", 
            order.getOrderId(), 
            stats.getExecutionQualityScore(),
            stats.getSlippage() != null ? stats.getSlippage().multiply(BigDecimal.valueOf(100)) : "N/A",
            stats.getTotalExecutionTime());
    }
    
    /**
     * 실행 통계 계산
     */
    private Order.ExecutionStatistics calculateExecutionStatistics(Order order) {
        LocalDateTime startTime = order.getCreatedAt();
        LocalDateTime endTime = order.getLastUpdatedAt();
        
        // 1. 실행 시간 계산
        Long executionTimeMs = null;
        if (startTime != null && endTime != null) {
            executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        
        // 2. 거래 통계 계산
        List<Trade> trades = order.getTrades();
        int numberOfTrades = trades.size();
        
        // 3. 평균 체결가격 (이미 order.getAvgExecutionPrice()에 계산됨)
        BigDecimal avgExecutionPrice = order.getAvgExecutionPrice();
        
        // 4. 평균 거래 크기
        BigDecimal averageTradeSize = trades.stream()
            .map(Trade::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(numberOfTrades), 4, BigDecimal.ROUND_HALF_UP);
        
        // 5. 슬리피지 계산 (실제 체결가격 vs 주문가격)
        BigDecimal slippage = calculateSlippage(order);
        
        // 6. 시장 영향 비용 계산 (주문 크기에 비례)
        BigDecimal marketImpactCost = calculateMarketImpactCost(order);
        
        // 7. 타이밍 비용 계산 (실행 지연에 따른 비용)
        BigDecimal timingCost = calculateTimingCost(order, executionTimeMs);
        
        // 8. 총 거래 비용
        BigDecimal totalTradingCost = BigDecimal.ZERO;
        if (marketImpactCost != null) totalTradingCost = totalTradingCost.add(marketImpactCost);
        if (timingCost != null) totalTradingCost = totalTradingCost.add(timingCost);
        
        // 9. 체결률 (이미 order.getFillRate()에 계산됨)
        BigDecimal fillRate = order.getFillRate();
        
        // 10. 실행 품질 점수 계산 (0-100)
        BigDecimal executionQualityScore = calculateExecutionQualityScore(
            slippage, marketImpactCost, timingCost, fillRate, executionTimeMs);
        
        // 11. 벤치마크 대비 성과 (TWAP 대비)
        BigDecimal benchmarkPerformance = calculateBenchmarkPerformance(order);
        
        return Order.ExecutionStatistics.builder()
            .executionStartTime(startTime)
            .executionEndTime(endTime)
            .totalExecutionTime(executionTimeMs)
            .averageExecutionPrice(avgExecutionPrice)
            .slippage(slippage)
            .marketImpactCost(marketImpactCost)
            .timingCost(timingCost)
            .totalTradingCost(totalTradingCost)
            .executionQualityScore(executionQualityScore)
            .benchmarkPerformance(benchmarkPerformance)
            .fillRate(fillRate)
            .numberOfTrades(numberOfTrades)
            .averageTradeSize(averageTradeSize)
            .build();
    }
    
    /**
     * 슬리피지 계산
     */
    private BigDecimal calculateSlippage(Order order) {
        if (order.getPrice() == null || order.getAvgExecutionPrice() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal orderPrice = order.getPrice();
        BigDecimal executionPrice = order.getAvgExecutionPrice();
        
        // 슬리피지 = (실행가격 - 주문가격) / 주문가격
        BigDecimal slippage = executionPrice.subtract(orderPrice)
            .divide(orderPrice, 4, BigDecimal.ROUND_HALF_UP);
        
        // 매도 주문의 경우 부호 반전
        if (order.getSide() == Order.OrderSide.SELL) {
            slippage = slippage.negate();
        }
        
        return slippage;
    }
    
    /**
     * 시장 영향 비용 계산
     */
    private BigDecimal calculateMarketImpactCost(Order order) {
        // 주문 크기에 비례한 시장 영향 비용 (간단한 모델)
        BigDecimal orderValue = order.getQuantity().multiply(
            order.getAvgExecutionPrice() != null ? order.getAvgExecutionPrice() : BigDecimal.valueOf(100));
        
        // 대형 주문일수록 높은 시장 영향 비용
        BigDecimal impactRate = BigDecimal.valueOf(0.001); // 0.1% 기본
        if (orderValue.compareTo(BigDecimal.valueOf(100000)) > 0) {
            impactRate = BigDecimal.valueOf(0.002); // 0.2%
        }
        if (orderValue.compareTo(BigDecimal.valueOf(500000)) > 0) {
            impactRate = BigDecimal.valueOf(0.005); // 0.5%
        }
        
        return orderValue.multiply(impactRate);
    }
    
    /**
     * 타이밍 비용 계산
     */
    private BigDecimal calculateTimingCost(Order order, Long executionTimeMs) {
        if (executionTimeMs == null) {
            return BigDecimal.ZERO;
        }
        
        // 실행 지연에 따른 비용 (긴급도에 따라)
        BigDecimal orderValue = order.getQuantity().multiply(
            order.getAvgExecutionPrice() != null ? order.getAvgExecutionPrice() : BigDecimal.valueOf(100));
        
        // 5분 이상 지연 시 타이밍 비용 발생
        long delayMinutes = executionTimeMs / (1000 * 60);
        if (delayMinutes <= 5) {
            return BigDecimal.ZERO;
        }
        
        // 지연 시간에 비례한 비용 (분당 0.01%)
        BigDecimal timingCostRate = BigDecimal.valueOf(delayMinutes - 5)
            .multiply(BigDecimal.valueOf(0.0001));
        
        return orderValue.multiply(timingCostRate);
    }
    
    /**
     * 실행 품질 점수 계산 (0-100)
     */
    private BigDecimal calculateExecutionQualityScore(BigDecimal slippage, BigDecimal marketImpact, 
            BigDecimal timingCost, BigDecimal fillRate, Long executionTimeMs) {
        
        BigDecimal score = BigDecimal.valueOf(100); // 시작 점수
        
        // 1. 슬리피지 패널티 (절대값 1%당 -10점)
        if (slippage != null) {
            BigDecimal slippagePenalty = slippage.abs().multiply(BigDecimal.valueOf(1000)); // 1% = 10점
            score = score.subtract(slippagePenalty);
        }
        
        // 2. 체결률 보너스/패널티
        if (fillRate != null) {
            if (fillRate.compareTo(BigDecimal.valueOf(100)) == 0) {
                score = score.add(BigDecimal.valueOf(10)); // 완전 체결 보너스
            } else {
                BigDecimal fillPenalty = BigDecimal.valueOf(100).subtract(fillRate)
                    .multiply(BigDecimal.valueOf(0.5)); // 미체결 1%당 -0.5점
                score = score.subtract(fillPenalty);
            }
        }
        
        // 3. 실행 시간 패널티 (30초 이상시)
        if (executionTimeMs != null && executionTimeMs > 30000) {
            BigDecimal timePenalty = BigDecimal.valueOf((executionTimeMs - 30000) / 1000)
                .multiply(BigDecimal.valueOf(0.1)); // 초당 -0.1점
            score = score.subtract(timePenalty);
        }
        
        // 4. 최소/최대 점수 제한
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        }
        if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
            score = BigDecimal.valueOf(100);
        }
        
        return score.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 벤치마크 대비 성과 계산
     */
    private BigDecimal calculateBenchmarkPerformance(Order order) {
        // 간단한 TWAP 벤치마크 대비 성과 (실제로는 시장 데이터 필요)
        if (order.getAvgExecutionPrice() == null || order.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        
        // 벤치마크를 주문가격으로 가정
        BigDecimal benchmark = order.getPrice();
        BigDecimal execution = order.getAvgExecutionPrice();
        
        // (실행가격 - 벤치마크) / 벤치마크
        BigDecimal performance = execution.subtract(benchmark)
            .divide(benchmark, 4, BigDecimal.ROUND_HALF_UP);
        
        // 매도 주문의 경우 부호 반전 (높은 가격에 파는 것이 좋음)
        if (order.getSide() == Order.OrderSide.SELL) {
            performance = performance.negate();
        }
        
        return performance;
    }
    
    /**
     * 주문 성과 리포트 생성
     */
    public CompletableFuture<Map<String, Object>> generateOrderReport(String orderId) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = getOrder(orderId).orElse(null);
            
            if (order == null) {
                return Map.of("error", "Order not found");
            }
            
            Map<String, Object> report = new HashMap<>();
            report.put("orderId", order.getOrderId());
            report.put("status", order.getStatus().getKoreanName());
            report.put("fillRate", order.getFillRate());
            report.put("executionProgress", order.getExecutionProgress());
            
            if (order.getExecutionStats() != null) {
                report.put("executionQuality", order.getExecutionStats().getExecutionQualityScore());
                report.put("slippage", order.getExecutionStats().getSlippage());
                report.put("executionTime", order.getExecutionStats().getTotalExecutionTime());
            }
            
            return report;
        });
    }
    
    /**
     * 데이터베이스 도메인 주문을 실행 도메인 주문으로 매핑
     */
    private Order mapToExecutionOrder(com.stockquest.domain.order.Order dbOrder) {
        return Order.builder()
            .orderId(String.valueOf(dbOrder.getId()))
            .symbol(dbOrder.getInstrumentKey()) // instrumentKey를 symbol로 매핑
            .side(mapOrderSide(dbOrder.getSide()))
            .quantity(dbOrder.getQuantity())
            .orderType(mapOrderType(dbOrder.getOrderType()))
            .price(dbOrder.getLimitPrice())
            .status(mapOrderStatus(dbOrder.getStatus()))
            .createdAt(dbOrder.getOrderedAt())
            .lastUpdatedAt(dbOrder.getExecutedAt()) // 완료된 주문의 실행 시간을 마지막 업데이트로 매핑
            .avgExecutionPrice(dbOrder.getExecutedPrice())
            .executedQuantity(dbOrder.getQuantity()) // 완료된 주문이므로 전량 체결
            .build();
    }
    
    /**
     * 도메인 모델 간 OrderSide 매핑
     */
    private Order.OrderSide mapOrderSide(com.stockquest.domain.order.OrderSide dbSide) {
        return switch (dbSide) {
            case BUY -> Order.OrderSide.BUY;
            case SELL -> Order.OrderSide.SELL;
        };
    }
    
    /**
     * 도메인 모델 간 OrderType 매핑
     */
    private Order.OrderType mapOrderType(com.stockquest.domain.order.OrderType dbType) {
        return switch (dbType) {
            case MARKET -> Order.OrderType.MARKET;
            case LIMIT -> Order.OrderType.LIMIT;
        };
    }
    
    /**
     * 도메인 모델 간 OrderStatus 매핑
     */
    private Order.OrderStatus mapOrderStatus(com.stockquest.domain.order.OrderStatus dbStatus) {
        return switch (dbStatus) {
            case PENDING -> Order.OrderStatus.PENDING_NEW;
            case EXECUTED -> Order.OrderStatus.FILLED;
            case CANCELLED -> Order.OrderStatus.CANCELED;
        };
    }
    
    /**
     * 포지션 한도 검증
     */
    private Order.RiskCheckResult validatePositionLimits(Order order) {
        // 현재 포트폴리오에서 해당 심볼의 포지션 확인
        BigDecimal orderValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
        BigDecimal positionLimit = BigDecimal.valueOf(1000000); // 1M 한도
        
        // 실제로는 포트폴리오 서비스에서 현재 포지션을 조회해야 함
        BigDecimal currentPosition = BigDecimal.valueOf(500000); // 현재 포지션 (예시)
        BigDecimal newPosition = currentPosition.add(orderValue);
        
        boolean passed = newPosition.compareTo(positionLimit) <= 0;
        String severity = "LOW";
        
        if (newPosition.compareTo(positionLimit.multiply(BigDecimal.valueOf(0.8))) > 0) {
            severity = "MEDIUM";
        }
        if (newPosition.compareTo(positionLimit.multiply(BigDecimal.valueOf(0.95))) > 0) {
            severity = "HIGH";
        }
        
        return Order.RiskCheckResult.builder()
            .checkType("POSITION_LIMIT")
            .passed(passed)
            .description(String.format("Symbol %s position: %s / %s", order.getSymbol(), newPosition, positionLimit))
            .currentValue(newPosition)
            .limitValue(positionLimit)
            .severity(severity)
            .build();
    }
    
    /**
     * 집중도 한도 검증
     */
    private Order.RiskCheckResult validateConcentrationLimits(Order order) {
        // 포트폴리오 대비 단일 종목 집중도 확인
        BigDecimal orderValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
        BigDecimal totalPortfolioValue = BigDecimal.valueOf(5000000); // 총 포트폴리오 가치 (예시)
        
        BigDecimal concentrationRatio = orderValue.divide(totalPortfolioValue, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal concentrationLimit = BigDecimal.valueOf(0.20); // 20% 한도
        
        boolean passed = concentrationRatio.compareTo(concentrationLimit) <= 0;
        String severity = "LOW";
        
        if (concentrationRatio.compareTo(BigDecimal.valueOf(0.15)) > 0) {
            severity = "MEDIUM";
        }
        if (concentrationRatio.compareTo(BigDecimal.valueOf(0.18)) > 0) {
            severity = "HIGH";
        }
        
        return Order.RiskCheckResult.builder()
            .checkType("CONCENTRATION_LIMIT")
            .passed(passed)
            .description(String.format("Concentration ratio: %.2f%% (limit: %.2f%%)", 
                concentrationRatio.multiply(BigDecimal.valueOf(100)), 
                concentrationLimit.multiply(BigDecimal.valueOf(100))))
            .currentValue(concentrationRatio.multiply(BigDecimal.valueOf(100)))
            .limitValue(concentrationLimit.multiply(BigDecimal.valueOf(100)))
            .severity(severity)
            .build();
    }
    
    /**
     * 유동성 검증
     */
    private Order.RiskCheckResult validateLiquidity(Order order) {
        // 시장 유동성 대비 주문 크기 확인
        BigDecimal orderQuantity = order.getQuantity();
        BigDecimal dailyVolume = BigDecimal.valueOf(1000000); // 일일 거래량 (예시)
        
        BigDecimal volumeRatio = orderQuantity.divide(dailyVolume, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal liquidityLimit = BigDecimal.valueOf(0.05); // 일일 거래량의 5% 한도
        
        boolean passed = volumeRatio.compareTo(liquidityLimit) <= 0;
        String severity = "LOW";
        
        if (volumeRatio.compareTo(BigDecimal.valueOf(0.03)) > 0) {
            severity = "MEDIUM";
        }
        if (volumeRatio.compareTo(BigDecimal.valueOf(0.04)) > 0) {
            severity = "HIGH";
        }
        
        return Order.RiskCheckResult.builder()
            .checkType("LIQUIDITY_CHECK")
            .passed(passed)
            .description(String.format("Order size vs daily volume: %.2f%% (limit: %.2f%%)", 
                volumeRatio.multiply(BigDecimal.valueOf(100)), 
                liquidityLimit.multiply(BigDecimal.valueOf(100))))
            .currentValue(volumeRatio.multiply(BigDecimal.valueOf(100)))
            .limitValue(liquidityLimit.multiply(BigDecimal.valueOf(100)))
            .severity(severity)
            .build();
    }
    
    /**
     * 신용 한도 검증
     */
    private Order.RiskCheckResult validateCreditLimits(Order order) {
        // 신용 한도 및 현금 잔고 확인
        BigDecimal orderValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
        BigDecimal creditLimit = BigDecimal.valueOf(2000000); // 신용 한도
        BigDecimal currentCredit = BigDecimal.valueOf(800000); // 현재 신용 사용액
        
        BigDecimal availableCredit = creditLimit.subtract(currentCredit);
        boolean passed = orderValue.compareTo(availableCredit) <= 0;
        
        BigDecimal utilizationRatio = currentCredit.add(orderValue).divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP);
        String severity = "LOW";
        
        if (utilizationRatio.compareTo(BigDecimal.valueOf(0.70)) > 0) {
            severity = "MEDIUM";
        }
        if (utilizationRatio.compareTo(BigDecimal.valueOf(0.90)) > 0) {
            severity = "HIGH";
        }
        
        return Order.RiskCheckResult.builder()
            .checkType("CREDIT_LIMIT")
            .passed(passed)
            .description(String.format("Credit utilization: %.2f%% (%s / %s)", 
                utilizationRatio.multiply(BigDecimal.valueOf(100)), 
                currentCredit.add(orderValue), creditLimit))
            .currentValue(currentCredit.add(orderValue))
            .limitValue(creditLimit)
            .severity(severity)
            .build();
    }
    
    /**
     * 주문 크기 검증
     */
    private Order.RiskCheckResult validateOrderSize(Order order) {
        // 최소/최대 주문 크기 확인
        BigDecimal orderQuantity = order.getQuantity();
        BigDecimal minOrderSize = BigDecimal.valueOf(1);
        BigDecimal maxOrderSize = BigDecimal.valueOf(100000);
        
        boolean passed = orderQuantity.compareTo(minOrderSize) >= 0 && 
                        orderQuantity.compareTo(maxOrderSize) <= 0;
        
        String severity = "LOW";
        if (orderQuantity.compareTo(maxOrderSize.multiply(BigDecimal.valueOf(0.8))) > 0) {
            severity = "MEDIUM";
        }
        
        return Order.RiskCheckResult.builder()
            .checkType("ORDER_SIZE")
            .passed(passed)
            .description(String.format("Order quantity: %s (range: %s - %s)", 
                orderQuantity, minOrderSize, maxOrderSize))
            .currentValue(orderQuantity)
            .limitValue(maxOrderSize)
            .severity(severity)
            .build();
    }
}