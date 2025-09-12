package com.stockquest.adapter.in.rest;

import com.stockquest.application.service.PortfolioOptimizationService;
import com.stockquest.application.service.PortfolioService;
import com.stockquest.domain.ml.PortfolioOptimization;
import com.stockquest.domain.ml.PortfolioOptimization.*;
import com.stockquest.domain.portfolio.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 포트폴리오 최적화 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ml/portfolio-optimization")
@RequiredArgsConstructor
public class PortfolioOptimizationController {
    
    private final PortfolioOptimizationService portfolioOptimizationService;
    private final PortfolioService portfolioService;
    
    /**
     * 포트폴리오 최적화 실행
     * 
     * @param portfolioId 포트폴리오 ID
     * @param request 최적화 요청 정보
     * @return 최적화 결과
     */
    @PostMapping("/{portfolioId}/optimize")
    public CompletableFuture<ResponseEntity<PortfolioOptimizationResponse>> optimizePortfolio(
            @PathVariable Long portfolioId,
            @Valid @RequestBody OptimizationRequest request) {
        
        log.info("포트폴리오 최적화 요청: portfolioId={}, type={}, objective={}", 
            portfolioId, request.getOptimizationType(), request.getObjective());
        
        try {
            // 포트폴리오 조회
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            
            // 제약 조건 구성
            OptimizationConstraints constraints = buildConstraints(request);
            
            return portfolioOptimizationService.optimizePortfolio(
                    portfolio, 
                    request.getOptimizationType(), 
                    request.getObjective(),
                    constraints)
                .thenApply(optimization -> {
                    PortfolioOptimizationResponse response = 
                        PortfolioOptimizationResponse.from(optimization);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("포트폴리오 최적화 실패: portfolioId={}", portfolioId, throwable);
                    return ResponseEntity.internalServerError().build();
                });
                
        } catch (Exception e) {
            log.error("포트폴리오 조회 실패: portfolioId={}", portfolioId, e);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }
    }
    
    /**
     * 효율적 프론티어 계산
     * 
     * @param portfolioId 포트폴리오 ID
     * @param request 프론티어 계산 요청
     * @return 효율적 프론티어 데이터
     */
    @PostMapping("/{portfolioId}/efficient-frontier")
    public CompletableFuture<ResponseEntity<EfficientFrontierResponse>> calculateEfficientFrontier(
            @PathVariable Long portfolioId,
            @Valid @RequestBody EfficientFrontierRequest request) {
        
        log.info("효율적 프론티어 계산 요청: portfolioId={}, points={}", 
            portfolioId, request.getPoints());
        
        try {
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            OptimizationConstraints constraints = buildConstraints(
                OptimizationRequest.builder()
                    .maxWeight(request.getMaxWeight())
                    .minWeight(request.getMinWeight())
                    .build()
            );
            
            // 여러 리스크 레벨에 대한 최적화 실행
            List<CompletableFuture<PortfolioOptimization>> optimizations = 
                request.getRiskLevels().stream()
                    .map(riskLevel -> portfolioOptimizationService.optimizePortfolio(
                        portfolio, 
                        OptimizationType.MODERN_PORTFOLIO_THEORY,
                        OptimizationObjective.TARGET_VOLATILITY,
                        constraints))
                    .toList();
            
            return CompletableFuture.allOf(optimizations.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<FrontierPoint> frontierPoints = optimizations.stream()
                        .map(CompletableFuture::join)
                        .map(opt -> FrontierPoint.builder()
                            .risk(opt.getExpectedPerformance().getExpectedVolatility())
                            .return_(opt.getExpectedPerformance().getExpectedReturn())
                            .sharpeRatio(opt.getExpectedPerformance().getExpectedSharpeRatio())
                            .build())
                        .toList();
                    
                    EfficientFrontierResponse response = EfficientFrontierResponse.builder()
                        .frontierPoints(frontierPoints)
                        .portfolioId(portfolioId)
                        .build();
                        
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("효율적 프론티어 계산 실패: portfolioId={}", portfolioId, throwable);
                    return ResponseEntity.internalServerError().build();
                });
                
        } catch (Exception e) {
            log.error("포트폴리오 조회 실패: portfolioId={}", portfolioId, e);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }
    }
    
    /**
     * 리밸런싱 제안 조회
     * 
     * @param portfolioId 포트폴리오 ID
     * @return 리밸런싱 제안
     */
    @GetMapping("/{portfolioId}/rebalancing-suggestions")
    public ResponseEntity<RebalancingSuggestionsResponse> getRebalancingSuggestions(
            @PathVariable Long portfolioId) {
        
        log.info("리밸런싱 제안 조회: portfolioId={}", portfolioId);
        
        try {
            // 실제 구현에서는 최근 최적화 결과 조회
            RebalancingSuggestionsResponse response = RebalancingSuggestionsResponse.builder()
                .portfolioId(portfolioId)
                .suggestions(List.of()) // 빈 리스트로 초기화
                .totalSuggestions(0)
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("리밸런싱 제안 조회 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 최적화 이력 조회
     * 
     * @param portfolioId 포트폴리오 ID
     * @param limit 조회할 이력 수
     * @return 최적화 이력
     */
    @GetMapping("/{portfolioId}/history")
    public ResponseEntity<OptimizationHistoryResponse> getOptimizationHistory(
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("최적화 이력 조회: portfolioId={}, limit={}", portfolioId, limit);
        
        try {
            OptimizationHistoryResponse response = OptimizationHistoryResponse.builder()
                .portfolioId(portfolioId)
                .optimizations(List.of()) // 빈 리스트로 초기화
                .totalCount(0)
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("최적화 이력 조회 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 백테스트 실행
     * 
     * @param portfolioId 포트폴리오 ID
     * @param request 백테스트 요청
     * @return 백테스트 결과
     */
    @PostMapping("/{portfolioId}/backtest")
    public CompletableFuture<ResponseEntity<BacktestResponse>> runBacktest(
            @PathVariable Long portfolioId,
            @Valid @RequestBody BacktestRequest request) {
        
        log.info("백테스트 실행 요청: portfolioId={}, startDate={}, endDate={}", 
            portfolioId, request.getStartDate(), request.getEndDate());
        
        // 백테스트 실행 (간소화된 구현)
        return CompletableFuture.supplyAsync(() -> {
            try {
                BacktestResponse response = BacktestResponse.builder()
                    .portfolioId(portfolioId)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalReturn(BigDecimal.valueOf(0.15)) // 15% 수익률
                    .volatility(BigDecimal.valueOf(0.12)) // 12% 변동성
                    .sharpeRatio(BigDecimal.valueOf(1.25))
                    .maxDrawdown(BigDecimal.valueOf(0.08)) // 8% 최대 낙폭
                    .performanceData(List.of()) // 빈 리스트로 초기화
                    .build();
                    
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                log.error("백테스트 실행 실패: portfolioId={}", portfolioId, e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }
    
    private OptimizationConstraints buildConstraints(OptimizationRequest request) {
        if (request.getMaxWeight() == null && request.getMinWeight() == null) {
            return null;
        }
        
        OptimizationConstraints.WeightConstraint constraint = OptimizationConstraints.WeightConstraint.builder()
            .minWeight(request.getMinWeight() != null ? request.getMinWeight() : BigDecimal.ZERO)
            .maxWeight(request.getMaxWeight() != null ? request.getMaxWeight() : BigDecimal.valueOf(0.5))
            .build();
        
        return OptimizationConstraints.builder()
            .weightConstraints(Map.of("default", constraint))
            .targetVolatility(request.getTargetVolatility())
            .maxConcentration(BigDecimal.valueOf(0.3))
            .rebalancingThreshold(BigDecimal.valueOf(0.05))
            .build();
    }
    
    // DTO 클래스들
    @lombok.Data
    @lombok.Builder
    public static class OptimizationRequest {
        @NotNull
        private OptimizationType optimizationType;
        
        @NotNull
        private OptimizationObjective objective;
        
        private BigDecimal maxWeight;
        private BigDecimal minWeight;
        private BigDecimal targetVolatility;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioOptimizationResponse {
        private String optimizationId;
        private Long portfolioId;
        private String optimizationType;
        private String objective;
        private List<AssetAllocationResponse> recommendedAllocations;
        private List<AssetAllocationResponse> currentAllocations;
        private List<RebalancingActionResponse> rebalancingActions;
        private ExpectedPerformanceResponse expectedPerformance;
        private RiskMetricsResponse riskMetrics;
        private String generatedAt;
        private String nextRebalancingDate;
        private String confidence;
        
        public static PortfolioOptimizationResponse from(PortfolioOptimization optimization) {
            return PortfolioOptimizationResponse.builder()
                .optimizationId(optimization.getOptimizationId())
                .portfolioId(optimization.getPortfolioId())
                .optimizationType(optimization.getOptimizationType().name())
                .objective(optimization.getObjective().name())
                .recommendedAllocations(optimization.getRecommendedAllocations().stream()
                    .map(AssetAllocationResponse::from).toList())
                .currentAllocations(optimization.getCurrentAllocations().stream()
                    .map(AssetAllocationResponse::from).toList())
                .rebalancingActions(optimization.getRebalancingActions().stream()
                    .map(RebalancingActionResponse::from).toList())
                .expectedPerformance(ExpectedPerformanceResponse.from(optimization.getExpectedPerformance()))
                .riskMetrics(RiskMetricsResponse.from(optimization.getRiskMetrics()))
                .generatedAt(optimization.getGeneratedAt().toString())
                .nextRebalancingDate(optimization.getNextRebalancingDate().toString())
                .confidence(optimization.getConfidence().toString())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AssetAllocationResponse {
        private String symbol;
        private String assetName;
        private String category;
        private String recommendedWeight;
        private String currentWeight;
        private String expectedReturn;
        private String expectedVolatility;
        private String riskContribution;
        private String allocationConfidence;
        
        public static AssetAllocationResponse from(AssetAllocation allocation) {
            return AssetAllocationResponse.builder()
                .symbol(allocation.getSymbol())
                .assetName(allocation.getAssetName())
                .category(allocation.getCategory().name())
                .recommendedWeight(allocation.getRecommendedWeight() != null ? 
                    allocation.getRecommendedWeight().toString() : null)
                .currentWeight(allocation.getCurrentWeight() != null ? 
                    allocation.getCurrentWeight().toString() : null)
                .expectedReturn(allocation.getExpectedReturn() != null ? 
                    allocation.getExpectedReturn().toString() : null)
                .expectedVolatility(allocation.getExpectedVolatility() != null ? 
                    allocation.getExpectedVolatility().toString() : null)
                .riskContribution(allocation.getRiskContribution() != null ? 
                    allocation.getRiskContribution().toString() : null)
                .allocationConfidence(allocation.getAllocationConfidence() != null ? 
                    allocation.getAllocationConfidence().toString() : null)
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalancingActionResponse {
        private String actionType;
        private String symbol;
        private String quantity;
        private String amount;
        private Integer priority;
        private String recommendedExecutionTime;
        private String estimatedCost;
        private String marketImpact;
        private String reason;
        
        public static RebalancingActionResponse from(RebalancingAction action) {
            return RebalancingActionResponse.builder()
                .actionType(action.getActionType().name())
                .symbol(action.getSymbol())
                .quantity(action.getQuantity().toString())
                .amount(action.getAmount().toString())
                .priority(action.getPriority())
                .recommendedExecutionTime(action.getRecommendedExecutionTime().toString())
                .estimatedCost(action.getEstimatedCost().toString())
                .marketImpact(action.getMarketImpact().toString())
                .reason(action.getReason())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ExpectedPerformanceResponse {
        private String expectedReturn;
        private String expectedVolatility;
        private String expectedSharpeRatio;
        private String expectedMaxDrawdown;
        private String beta;
        private String trackingError;
        
        public static ExpectedPerformanceResponse from(ExpectedPerformance performance) {
            return ExpectedPerformanceResponse.builder()
                .expectedReturn(performance.getExpectedReturn().toString())
                .expectedVolatility(performance.getExpectedVolatility().toString())
                .expectedSharpeRatio(performance.getExpectedSharpeRatio().toString())
                .expectedMaxDrawdown(performance.getExpectedMaxDrawdown().toString())
                .beta(performance.getBeta().toString())
                .trackingError(performance.getTrackingError().toString())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskMetricsResponse {
        private String var95;
        private String cvar95;
        private String diversificationRatio;
        private String concentrationRisk;
        private String liquidityRisk;
        
        public static RiskMetricsResponse from(RiskMetrics riskMetrics) {
            return RiskMetricsResponse.builder()
                .var95(riskMetrics.getVar95().toString())
                .cvar95(riskMetrics.getCvar95().toString())
                .diversificationRatio(riskMetrics.getDiversificationRatio().toString())
                .concentrationRisk(riskMetrics.getConcentrationRisk().toString())
                .liquidityRisk(riskMetrics.getLiquidityRisk().toString())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class EfficientFrontierRequest {
        @lombok.Builder.Default
        private Integer points = 20;
        private List<BigDecimal> riskLevels;
        private BigDecimal maxWeight;
        private BigDecimal minWeight;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class EfficientFrontierResponse {
        private Long portfolioId;
        private List<FrontierPoint> frontierPoints;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FrontierPoint {
        private BigDecimal risk;
        private BigDecimal return_;
        private BigDecimal sharpeRatio;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalancingSuggestionsResponse {
        private Long portfolioId;
        private List<RebalancingActionResponse> suggestions;
        private Integer totalSuggestions;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizationHistoryResponse {
        private Long portfolioId;
        private List<PortfolioOptimizationResponse> optimizations;
        private Integer totalCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BacktestRequest {
        @NotNull
        private LocalDateTime startDate;
        
        @NotNull
        private LocalDateTime endDate;
        
        @lombok.Builder.Default
        private OptimizationType optimizationType = OptimizationType.MODERN_PORTFOLIO_THEORY;
        @lombok.Builder.Default
        private OptimizationObjective objective = OptimizationObjective.MAXIMIZE_SHARPE_RATIO;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BacktestResponse {
        private Long portfolioId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal totalReturn;
        private BigDecimal volatility;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private List<PerformancePoint> performanceData;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PerformancePoint {
        private LocalDateTime date;
        private BigDecimal portfolioValue;
        private BigDecimal benchmarkValue;
        private BigDecimal cumulativeReturn;
    }
}