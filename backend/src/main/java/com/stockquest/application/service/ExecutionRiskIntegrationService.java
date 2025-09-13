package com.stockquest.application.service;

import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import com.stockquest.domain.execution.Order.RiskValidationResult;
import com.stockquest.domain.execution.Order.RiskCheckResult;
import com.stockquest.domain.risk.RiskAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 실행 엔진과 리스크 관리 & 포트폴리오 최적화 통합 서비스
 * Phase 8.4: Real-time Execution Engine - Risk Management & Portfolio Optimization Integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionRiskIntegrationService {

    // Dependencies would be injected
    private final RealTimeRiskAssessmentService riskAssessmentService;
    private final PortfolioOptimizationService portfolioOptimizationService;
    private final VaRCalculationService varCalculationService;
    private final RiskAlertService riskAlertService;
    
    /**
     * 주문 제출 전 종합 리스크 검증
     */
    @Async
    public CompletableFuture<RiskValidationResult> validateOrderRisk(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("주문 종합 리스크 검증 시작: {} - {} {} shares", 
                order.getOrderId(), order.getSide().getKoreanName(), order.getQuantity());
            
            List<RiskCheckResult> riskChecks = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> violations = new ArrayList<>();
            boolean passed = true;
            String riskLevel = "LOW";

            try {
                // 1. 포지션 집중도 검증
                RiskCheckResult concentrationCheck = validatePositionConcentration(order);
                riskChecks.add(concentrationCheck);
                if (!concentrationCheck.isPassed()) {
                    passed = false;
                    violations.add("포지션 집중도 한도 초과");
                    riskLevel = "HIGH";
                }

                // 2. 포트폴리오 VaR 검증
                RiskCheckResult varCheck = validatePortfolioVaR(order);
                riskChecks.add(varCheck);
                if (!varCheck.isPassed()) {
                    passed = false;
                    violations.add("포트폴리오 VaR 한도 초과");
                    riskLevel = "CRITICAL";
                }

                // 3. 유동성 리스크 검증
                RiskCheckResult liquidityCheck = validateLiquidityRisk(order);
                riskChecks.add(liquidityCheck);
                if (!liquidityCheck.isPassed()) {
                    warnings.add("유동성 리스크 주의");
                    if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM";
                }

                // 4. 시장 영향 검증
                RiskCheckResult marketImpactCheck = validateMarketImpact(order);
                riskChecks.add(marketImpactCheck);
                if (!marketImpactCheck.isPassed()) {
                    warnings.add("시장 영향 주의");
                }

                log.info("주문 리스크 검증 완료: {} - Passed: {}, Risk Level: {}", 
                    order.getOrderId(), passed, riskLevel);

                return RiskValidationResult.builder()
                    .passed(passed)
                    .validationTime(LocalDateTime.now())
                    .riskChecks(riskChecks)
                    .overallRiskLevel(riskLevel)
                    .warnings(warnings)
                    .violations(violations)
                    .build();

            } catch (Exception e) {
                log.error("주문 리스크 검증 중 오류 발생: {} - {}", order.getOrderId(), e.getMessage(), e);
                return RiskValidationResult.builder()
                    .passed(false)
                    .validationTime(LocalDateTime.now())
                    .overallRiskLevel("CRITICAL")
                    .violations(List.of("리스크 검증 시스템 오류: " + e.getMessage()))
                    .build();
            }
        });
    }

    /**
     * 거래 체결 후 포트폴리오 리스크 지표 실시간 업데이트
     */
    @Async
    public CompletableFuture<Void> updatePortfolioRiskAfterTrade(Trade trade) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("거래 체결 후 포트폴리오 리스크 업데이트: {}", trade.getTradeId());

                // 1. 포지션 업데이트 후 리스크 지표 재계산
                updateRealTimeRiskMetrics(trade.getPortfolioId(), trade);

                // 2. 포트폴리오 최적화 필요성 평가
                if (shouldTriggerRebalancing(trade)) {
                    log.info("포트폴리오 리밸런싱 필요성 감지: {}", trade.getPortfolioId());
                    // 리밸런싱 제안 생성은 별도로 처리
                }

                log.debug("포트폴리오 리스크 업데이트 완료: {}", trade.getPortfolioId());

            } catch (Exception e) {
                log.error("포트폴리오 리스크 업데이트 중 오류: {} - {}", 
                    trade.getPortfolioId(), e.getMessage(), e);
                // 리스크 업데이트 실패는 중요하므로 알림 생성
                createRiskAlert(trade.getPortfolioId(), "RISK_UPDATE_FAILURE", 
                    "포트폴리오 리스크 업데이트 실패: " + e.getMessage(), "HIGH");
            }
        });
    }

    // Private helper methods for risk validation

    private RiskCheckResult validatePositionConcentration(Order order) {
        try {
            String sector = getStockSectorSimulated(order.getSymbol());
            BigDecimal existingPositionValue = getExistingPositionValue(order.getPortfolioId(), order.getSymbol());
            BigDecimal newPositionValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
            BigDecimal totalPositionValue = existingPositionValue.add(newPositionValue);
            
            // 포트폴리오 총 가치 대비 단일 종목 한도: 15%
            BigDecimal portfolioValue = BigDecimal.valueOf(1000000); // 시뮬레이션
            BigDecimal concentrationRatio = totalPositionValue.divide(portfolioValue, 4, RoundingMode.HALF_UP);
            BigDecimal concentrationLimit = BigDecimal.valueOf(0.15); // 15%
            
            boolean passed = concentrationRatio.compareTo(concentrationLimit) <= 0;
            
            // 섹터 집중도 검증
            BigDecimal sectorConcentration = calculateSectorConcentration(order.getPortfolioId(), sector);
            BigDecimal sectorLimit = BigDecimal.valueOf(0.40); // 40%
            boolean sectorPassed = sectorConcentration.compareTo(sectorLimit) <= 0;
            
            passed = passed && sectorPassed;
            
            return RiskCheckResult.builder()
                .checkType("POSITION_CONCENTRATION")
                .passed(passed)
                .description("포지션 집중도 검증")
                .currentValue(concentrationRatio)
                .limitValue(concentrationLimit)
                .severity(passed ? "LOW" : "HIGH")
                .build();
                
        } catch (Exception e) {
            log.error("포지션 집중도 검증 오류: {}", e.getMessage());
            return RiskCheckResult.builder()
                .checkType("POSITION_CONCENTRATION")
                .passed(false)
                .description("포지션 집중도 검증 오류: " + e.getMessage())
                .severity("CRITICAL")
                .build();
        }
    }

    private RiskCheckResult validatePortfolioVaR(Order order) {
        try {
            // 현재 포트폴리오 VaR 계산
            BigDecimal currentVaR = calculateSimulatedVaR(order.getPortfolioId(), "portfolio");
            
            // 주문 추가 후 예상 VaR 계산
            BigDecimal projectedVaR = calculateProjectedVaR(order);
            BigDecimal incrementalVaR = projectedVaR.subtract(currentVaR);
            
            // VaR 한도 설정 (포트폴리오 가치의 5%)
            BigDecimal portfolioValue = BigDecimal.valueOf(1000000); // 시뮬레이션
            BigDecimal varLimit = portfolioValue.multiply(BigDecimal.valueOf(0.05));
            
            boolean passed = projectedVaR.compareTo(varLimit) <= 0;
            
            return RiskCheckResult.builder()
                .checkType("PORTFOLIO_VAR")
                .passed(passed)
                .description("포트폴리오 VaR 검증")
                .currentValue(projectedVaR)
                .limitValue(varLimit)
                .severity(passed ? "LOW" : "HIGH")
                .build();
                
        } catch (Exception e) {
            log.error("포트폴리오 VaR 검증 오류: {}", e.getMessage());
            return RiskCheckResult.builder()
                .checkType("PORTFOLIO_VAR")
                .passed(false)
                .description("VaR 검증 오류: " + e.getMessage())
                .severity("CRITICAL")
                .build();
        }
    }

    private RiskCheckResult validateLiquidityRisk(Order order) {
        try {
            BigDecimal liquidityScore = calculateLiquidityScore(order.getSymbol());
            BigDecimal minimumLiquidity = BigDecimal.valueOf(0.3); // 30% 이상
            
            boolean passed = liquidityScore.compareTo(minimumLiquidity) >= 0;
            
            return RiskCheckResult.builder()
                .checkType("LIQUIDITY_RISK")
                .passed(passed)
                .description("유동성 리스크 검증")
                .currentValue(liquidityScore)
                .limitValue(minimumLiquidity)
                .severity(passed ? "LOW" : "MEDIUM")
                .build();
                
        } catch (Exception e) {
            log.error("유동성 리스크 검증 오류: {}", e.getMessage());
            return RiskCheckResult.builder()
                .checkType("LIQUIDITY_RISK")
                .passed(false)
                .description("유동성 검증 오류: " + e.getMessage())
                .severity("HIGH")
                .build();
        }
    }

    private RiskCheckResult validateMarketImpact(Order order) {
        try {
            BigDecimal tradeValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
            BigDecimal averageDailyVolume = getAverageDailyVolume(order.getSymbol());
            BigDecimal marketImpact = tradeValue.divide(averageDailyVolume, 4, RoundingMode.HALF_UP);
            
            BigDecimal impactLimit = BigDecimal.valueOf(0.05); // 5% 이하
            boolean passed = marketImpact.compareTo(impactLimit) <= 0;
            
            return RiskCheckResult.builder()
                .checkType("MARKET_IMPACT")
                .passed(passed)
                .description("시장 영향 검증")
                .currentValue(marketImpact)
                .limitValue(impactLimit)
                .severity(passed ? "LOW" : "MEDIUM")
                .build();
                
        } catch (Exception e) {
            log.error("시장 영향 검증 오류: {}", e.getMessage());
            return RiskCheckResult.builder()
                .checkType("MARKET_IMPACT")
                .passed(false)
                .description("시장 영향 검증 오류: " + e.getMessage())
                .severity("HIGH")
                .build();
        }
    }

    private void updateRealTimeRiskMetrics(String portfolioId, Trade trade) {
        try {
            log.debug("실시간 리스크 지표 업데이트: Portfolio={}, Trade={}", portfolioId, trade.getTradeId());
            
            // 1. 현재 포트폴리오 구성 가져오기 (시뮬레이션)
            Map<String, BigDecimal> currentPositions = getCurrentPositionsSimulated(portfolioId);
            
            // 2. 거래 반영하여 포지션 업데이트
            updatePositionAfterTrade(currentPositions, trade);
            
            // 3. 리스크 지표 재계산
            Map<String, BigDecimal> updatedMetrics = calculateRiskMetrics(currentPositions);
            
            // 4. 이전 지표와 비교하여 변화량 계산
            Map<String, BigDecimal> previousMetrics = getPreviousRiskMetricsSimulated(portfolioId);
            Map<String, BigDecimal> changes = calculateMetricChanges(updatedMetrics, previousMetrics);
            
            // 5. 임계값 위반 검사
            checkRiskThresholds(portfolioId, updatedMetrics, changes);
            
            log.debug("리스크 지표 업데이트 완료: Portfolio={}", portfolioId);
            
        } catch (Exception e) {
            log.error("실시간 리스크 지표 업데이트 실패: {} - {}", portfolioId, e.getMessage(), e);
            createRiskAlert(portfolioId, "RISK_METRICS_UPDATE_FAILURE", 
                "리스크 지표 업데이트 실패: " + e.getMessage(), "HIGH");
        }
    }

    private boolean shouldTriggerRebalancing(Trade trade) {
        try {
            // 간단한 리밸런싱 필요성 판단 로직
            BigDecimal tradeValue = trade.getQuantity().multiply(trade.getPrice());
            BigDecimal portfolioValue = BigDecimal.valueOf(1000000); // 시뮬레이션
            BigDecimal impactRatio = tradeValue.divide(portfolioValue, 4, RoundingMode.HALF_UP);
            
            // 거래가 포트폴리오의 10% 이상을 차지하면 리밸런싱 고려
            return impactRatio.compareTo(BigDecimal.valueOf(0.10)) > 0;
            
        } catch (Exception e) {
            log.error("리밸런싱 필요성 판단 오류: {}", e.getMessage());
            return false;
        }
    }

    // Simulation helper methods
    
    private String getStockSectorSimulated(String symbol) {
        Map<String, String> sectorMapping = Map.of(
            "AAPL", "TECHNOLOGY",
            "GOOGL", "TECHNOLOGY", 
            "MSFT", "TECHNOLOGY",
            "AMZN", "CONSUMER_DISCRETIONARY",
            "TSLA", "CONSUMER_DISCRETIONARY",
            "META", "TECHNOLOGY",
            "NVDA", "TECHNOLOGY",
            "JPM", "FINANCIALS",
            "JNJ", "HEALTHCARE",
            "PG", "CONSUMER_STAPLES"
        );
        
        return sectorMapping.getOrDefault(symbol, "DIVERSIFIED");
    }
    
    private BigDecimal getExistingPositionValue(String portfolioId, String symbol) {
        Map<String, BigDecimal> symbolValues = Map.of(
            "AAPL", BigDecimal.valueOf(50000),
            "GOOGL", BigDecimal.valueOf(75000),
            "MSFT", BigDecimal.valueOf(60000),
            "TSLA", BigDecimal.valueOf(30000),
            "AMZN", BigDecimal.valueOf(45000)
        );
        
        return symbolValues.getOrDefault(symbol, BigDecimal.valueOf(10000));
    }
    
    private BigDecimal calculateSectorConcentration(String portfolioId, String sector) {
        Map<String, BigDecimal> sectorConcentrations = Map.of(
            "TECHNOLOGY", BigDecimal.valueOf(0.45), // 45%
            "FINANCIALS", BigDecimal.valueOf(0.15), // 15%
            "HEALTHCARE", BigDecimal.valueOf(0.12), // 12%
            "CONSUMER_DISCRETIONARY", BigDecimal.valueOf(0.18), // 18%
            "CONSUMER_STAPLES", BigDecimal.valueOf(0.10) // 10%
        );
        
        return sectorConcentrations.getOrDefault(sector, BigDecimal.valueOf(0.05));
    }
    
    private BigDecimal calculateSimulatedVaR(String portfolioId, String symbol) {
        Map<String, BigDecimal> symbolRisks = Map.of(
            "AAPL", BigDecimal.valueOf(0.015),
            "GOOGL", BigDecimal.valueOf(0.018),
            "MSFT", BigDecimal.valueOf(0.012),
            "TSLA", BigDecimal.valueOf(0.035),
            "AMZN", BigDecimal.valueOf(0.020),
            "META", BigDecimal.valueOf(0.025),
            "NVDA", BigDecimal.valueOf(0.030)
        );
        
        BigDecimal baseVaR = BigDecimal.valueOf(50000);
        BigDecimal symbolRisk = symbolRisks.getOrDefault(symbol, BigDecimal.valueOf(0.020));
        return baseVaR.multiply(BigDecimal.ONE.add(symbolRisk));
    }
    
    private BigDecimal calculateProjectedVaR(Order order) {
        BigDecimal currentVaR = calculateSimulatedVaR(order.getPortfolioId(), order.getSymbol());
        BigDecimal orderValue = order.getQuantity().multiply(order.getPrice() != null ? order.getPrice() : BigDecimal.valueOf(100));
        BigDecimal portfolioValue = BigDecimal.valueOf(1000000);
        BigDecimal weightIncrease = orderValue.divide(portfolioValue, 4, RoundingMode.HALF_UP);
        
        return currentVaR.multiply(BigDecimal.ONE.add(weightIncrease.multiply(BigDecimal.valueOf(1.2))));
    }
    
    private BigDecimal calculateLiquidityScore(String symbol) {
        Map<String, BigDecimal> liquidityScores = Map.of(
            "AAPL", BigDecimal.valueOf(0.95),
            "GOOGL", BigDecimal.valueOf(0.90),
            "MSFT", BigDecimal.valueOf(0.92),
            "TSLA", BigDecimal.valueOf(0.75),
            "AMZN", BigDecimal.valueOf(0.88)
        );
        
        return liquidityScores.getOrDefault(symbol, BigDecimal.valueOf(0.50));
    }
    
    private BigDecimal getAverageDailyVolume(String symbol) {
        Map<String, BigDecimal> volumes = Map.of(
            "AAPL", BigDecimal.valueOf(50000000),
            "GOOGL", BigDecimal.valueOf(30000000),
            "MSFT", BigDecimal.valueOf(45000000),
            "TSLA", BigDecimal.valueOf(85000000),
            "AMZN", BigDecimal.valueOf(35000000)
        );
        
        return volumes.getOrDefault(symbol, BigDecimal.valueOf(10000000));
    }
    
    private Map<String, BigDecimal> getCurrentPositionsSimulated(String portfolioId) {
        Map<String, BigDecimal> positions = new HashMap<>();
        positions.put("AAPL", BigDecimal.valueOf(500));
        positions.put("GOOGL", BigDecimal.valueOf(50));
        positions.put("MSFT", BigDecimal.valueOf(300));
        positions.put("TSLA", BigDecimal.valueOf(100));
        positions.put("AMZN", BigDecimal.valueOf(75));
        
        return positions;
    }
    
    private void updatePositionAfterTrade(Map<String, BigDecimal> positions, Trade trade) {
        String symbol = trade.getSymbol();
        BigDecimal currentPosition = positions.getOrDefault(symbol, BigDecimal.ZERO);
        
        if ("BUY".equals(trade.getSide().name())) {
            positions.put(symbol, currentPosition.add(trade.getQuantity()));
        } else {
            positions.put(symbol, currentPosition.subtract(trade.getQuantity()));
        }
    }
    
    private Map<String, BigDecimal> calculateRiskMetrics(Map<String, BigDecimal> positions) {
        Map<String, BigDecimal> metrics = new HashMap<>();
        metrics.put("PORTFOLIO_BETA", BigDecimal.valueOf(1.1));
        metrics.put("PORTFOLIO_VOLATILITY", BigDecimal.valueOf(0.15));
        metrics.put("SHARPE_RATIO", BigDecimal.valueOf(1.5));
        metrics.put("MAX_DRAWDOWN", BigDecimal.valueOf(0.12));
        
        return metrics;
    }
    
    private Map<String, BigDecimal> getPreviousRiskMetricsSimulated(String portfolioId) {
        Map<String, BigDecimal> previousMetrics = new HashMap<>();
        previousMetrics.put("PORTFOLIO_BETA", BigDecimal.valueOf(1.05));
        previousMetrics.put("PORTFOLIO_VOLATILITY", BigDecimal.valueOf(0.14));
        previousMetrics.put("SHARPE_RATIO", BigDecimal.valueOf(1.45));
        previousMetrics.put("MAX_DRAWDOWN", BigDecimal.valueOf(0.11));
        
        return previousMetrics;
    }
    
    private Map<String, BigDecimal> calculateMetricChanges(Map<String, BigDecimal> current, Map<String, BigDecimal> previous) {
        Map<String, BigDecimal> changes = new HashMap<>();
        for (String key : current.keySet()) {
            BigDecimal currentValue = current.get(key);
            BigDecimal previousValue = previous.getOrDefault(key, BigDecimal.ZERO);
            changes.put(key, currentValue.subtract(previousValue));
        }
        return changes;
    }
    
    private void checkRiskThresholds(String portfolioId, Map<String, BigDecimal> metrics, Map<String, BigDecimal> changes) {
        // 리스크 임계값 검사 및 알림 생성 시뮬레이션
        BigDecimal betaThreshold = BigDecimal.valueOf(1.5);
        BigDecimal volatilityThreshold = BigDecimal.valueOf(0.25);
        
        BigDecimal currentBeta = metrics.get("PORTFOLIO_BETA");
        BigDecimal currentVolatility = metrics.get("PORTFOLIO_VOLATILITY");
        
        if (currentBeta.compareTo(betaThreshold) > 0) {
            createRiskAlert(portfolioId, "BETA_THRESHOLD", 
                "포트폴리오 베타가 임계값을 초과했습니다: " + currentBeta, "MEDIUM");
        }
        
        if (currentVolatility.compareTo(volatilityThreshold) > 0) {
            createRiskAlert(portfolioId, "VOLATILITY_THRESHOLD", 
                "포트폴리오 변동성이 임계값을 초과했습니다: " + currentVolatility, "HIGH");
        }
    }
    
    private void createRiskAlert(String portfolioId, String alertType, String message, String severity) {
        log.info("리스크 알림 생성: Portfolio={}, Type={}, Severity={}, Message={}", 
                portfolioId, alertType, severity, message);
        // 실제 구현에서는 RiskAlertService.createAlert() 호출
    }
}