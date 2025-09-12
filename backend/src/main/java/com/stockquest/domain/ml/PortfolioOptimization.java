package com.stockquest.domain.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ML-기반 포트폴리오 최적화 도메인 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioOptimization {
    
    /**
     * 최적화 고유 ID
     */
    private String optimizationId;
    
    /**
     * 포트폴리오 ID
     */
    private Long portfolioId;
    
    /**
     * 최적화 타입
     */
    private OptimizationType optimizationType;
    
    /**
     * 최적화 목표
     */
    private OptimizationObjective objective;
    
    /**
     * 권장 자산 배분
     */
    private List<AssetAllocation> recommendedAllocations;
    
    /**
     * 현재 자산 배분
     */
    private List<AssetAllocation> currentAllocations;
    
    /**
     * 리밸런싱 액션 목록
     */
    private List<RebalancingAction> rebalancingActions;
    
    /**
     * 예상 성과 지표
     */
    private ExpectedPerformance expectedPerformance;
    
    /**
     * 리스크 메트릭
     */
    private RiskMetrics riskMetrics;
    
    /**
     * 최적화 제약 조건
     */
    private OptimizationConstraints constraints;
    
    /**
     * 사용된 ML 모델 정보
     */
    private OptimizationModelInfo modelInfo;
    
    /**
     * 시장 전망 정보
     */
    private MarketOutlook marketOutlook;
    
    /**
     * 최적화 생성 시간
     */
    private LocalDateTime generatedAt;
    
    /**
     * 다음 리밸런싱 권장 시점
     */
    private LocalDateTime nextRebalancingDate;
    
    /**
     * 최적화 신뢰도
     */
    private BigDecimal confidence;
    
    /**
     * 최적화 상태
     */
    @Builder.Default
    private OptimizationStatus status = OptimizationStatus.ACTIVE;
    
    public enum OptimizationType {
        MODERN_PORTFOLIO_THEORY,
        RISK_PARITY,
        BLACK_LITTERMAN,
        MINIMUM_VARIANCE,
        MAXIMUM_SHARPE,
        HIERARCHICAL_RISK_PARITY,
        MACHINE_LEARNING_BASED
    }
    
    public enum OptimizationObjective {
        MAXIMIZE_RETURN,
        MINIMIZE_RISK,
        MAXIMIZE_SHARPE_RATIO,
        MINIMIZE_DRAWDOWN,
        EQUAL_RISK_CONTRIBUTION,
        TARGET_VOLATILITY,
        ESG_OPTIMIZED
    }
    
    public enum OptimizationStatus {
        ACTIVE,
        EXECUTED,
        PARTIALLY_EXECUTED,
        EXPIRED,
        CANCELLED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAllocation {
        
        /**
         * 자산 심볼
         */
        private String symbol;
        
        /**
         * 자산 이름
         */
        private String assetName;
        
        /**
         * 자산 카테고리
         */
        private AssetCategory category;
        
        /**
         * 권장 비중 (%)
         */
        private BigDecimal recommendedWeight;
        
        /**
         * 현재 비중 (%)
         */
        private BigDecimal currentWeight;
        
        /**
         * 목표 수량
         */
        private BigDecimal targetQuantity;
        
        /**
         * 현재 수량
         */
        private BigDecimal currentQuantity;
        
        /**
         * 예상 수익률 (연간)
         */
        private BigDecimal expectedReturn;
        
        /**
         * 예상 변동성 (연간)
         */
        private BigDecimal expectedVolatility;
        
        /**
         * 위험 기여도 (%)
         */
        private BigDecimal riskContribution;
        
        /**
         * 수익 기여도 (%)
         */
        private BigDecimal returnContribution;
        
        /**
         * 배분 신뢰도
         */
        private BigDecimal allocationConfidence;
    }
    
    public enum AssetCategory {
        EQUITY,
        BOND,
        COMMODITY,
        REAL_ESTATE,
        CRYPTO,
        CASH,
        ALTERNATIVE
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalancingAction {
        
        /**
         * 액션 타입
         */
        private ActionType actionType;
        
        /**
         * 대상 자산
         */
        private String symbol;
        
        /**
         * 거래 수량
         */
        private BigDecimal quantity;
        
        /**
         * 거래 금액 (예상)
         */
        private BigDecimal amount;
        
        /**
         * 우선순위 (1=최고)
         */
        private Integer priority;
        
        /**
         * 실행 권장 시점
         */
        private LocalDateTime recommendedExecutionTime;
        
        /**
         * 예상 거래 비용
         */
        private BigDecimal estimatedCost;
        
        /**
         * 시장 임팩트 예상
         */
        private BigDecimal marketImpact;
        
        /**
         * 액션 이유
         */
        private String reason;
        
        public enum ActionType {
            BUY,
            SELL,
            HOLD,
            REDUCE_POSITION,
            INCREASE_POSITION
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpectedPerformance {
        
        /**
         * 예상 연간 수익률 (%)
         */
        private BigDecimal expectedReturn;
        
        /**
         * 예상 연간 변동성 (%)
         */
        private BigDecimal expectedVolatility;
        
        /**
         * 예상 샤프 비율
         */
        private BigDecimal expectedSharpeRatio;
        
        /**
         * 예상 최대 낙폭 (%)
         */
        private BigDecimal expectedMaxDrawdown;
        
        /**
         * 예상 정보 비율
         */
        private BigDecimal expectedInformationRatio;
        
        /**
         * 예상 칼마 비율
         */
        private BigDecimal expectedCalmarRatio;
        
        /**
         * 베타 계수 (시장 대비)
         */
        private BigDecimal beta;
        
        /**
         * 추적 오차
         */
        private BigDecimal trackingError;
        
        /**
         * 성과 예측 구간
         */
        private PerformanceRange performanceRange;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerformanceRange {
            private BigDecimal optimisticReturn;
            private BigDecimal pessimisticReturn;
            private BigDecimal medianReturn;
            private BigDecimal confidenceInterval;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskMetrics {
        
        /**
         * 포트폴리오 VaR (95%)
         */
        private BigDecimal var95;
        
        /**
         * 포트폴리오 CVaR (95%)
         */
        private BigDecimal cvar95;
        
        /**
         * 분산도 측정
         */
        private BigDecimal diversificationRatio;
        
        /**
         * 집중도 위험 (HHI)
         */
        private BigDecimal concentrationRisk;
        
        /**
         * 유동성 위험
         */
        private BigDecimal liquidityRisk;
        
        /**
         * 꼬리 위험 (tail risk)
         */
        private BigDecimal tailRisk;
        
        /**
         * 상관관계 위험
         */
        private BigDecimal correlationRisk;
        
        /**
         * 리스크 예산 활용도 (%)
         */
        private BigDecimal riskBudgetUtilization;
        
        /**
         * 섹터별 리스크 배분
         */
        private Map<String, BigDecimal> sectorRiskAllocation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationConstraints {
        
        /**
         * 최소/최대 자산 비중 제약
         */
        private Map<String, WeightConstraint> weightConstraints;
        
        /**
         * 섹터별 비중 제한
         */
        private Map<String, WeightConstraint> sectorConstraints;
        
        /**
         * 목표 변동성
         */
        private BigDecimal targetVolatility;
        
        /**
         * 최대 집중도
         */
        private BigDecimal maxConcentration;
        
        /**
         * ESG 점수 최소값
         */
        private BigDecimal minESGScore;
        
        /**
         * 거래 비용 한도
         */
        private BigDecimal maxTransactionCost;
        
        /**
         * 리밸런싱 임계값 (%)
         */
        private BigDecimal rebalancingThreshold;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WeightConstraint {
            private BigDecimal minWeight;
            private BigDecimal maxWeight;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationModelInfo {
        
        /**
         * 사용된 최적화 알고리즘
         */
        private String algorithmName;
        
        /**
         * 모델 버전
         */
        private String modelVersion;
        
        /**
         * 백테스트 성과 (샤프비율)
         */
        private BigDecimal backtestSharpeRatio;
        
        /**
         * 데이터 기간
         */
        private String dataPeriod;
        
        /**
         * 사용된 팩터 수
         */
        private Integer factorCount;
        
        /**
         * 수렴 상태
         */
        private ConvergenceStatus convergenceStatus;
        
        /**
         * 최적화 실행 시간 (밀리초)
         */
        private Long executionTimeMs;
        
        public enum ConvergenceStatus {
            CONVERGED,
            MAX_ITERATIONS_REACHED,
            INFEASIBLE,
            ERROR
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketOutlook {
        
        /**
         * 시장 체제 전망
         */
        private MarketRegime expectedRegime;
        
        /**
         * 변동성 전망
         */
        private VolatilityOutlook volatilityOutlook;
        
        /**
         * 금리 전망
         */
        private InterestRateOutlook interestRateOutlook;
        
        /**
         * 섹터 전망
         */
        private Map<String, SectorOutlook> sectorOutlooks;
        
        /**
         * 전망 시간 지평 (일)
         */
        private Integer forecastHorizon;
        
        public enum MarketRegime {
            BULL_MARKET,
            BEAR_MARKET,
            SIDEWAYS_MARKET,
            HIGH_VOLATILITY,
            LOW_VOLATILITY
        }
        
        public enum VolatilityOutlook {
            DECREASING,
            STABLE,
            INCREASING,
            VOLATILE
        }
        
        public enum InterestRateOutlook {
            RISING,
            FALLING,
            STABLE,
            UNCERTAIN
        }
        
        public enum SectorOutlook {
            OUTPERFORM,
            NEUTRAL,
            UNDERPERFORM
        }
    }
    
    /**
     * 리밸런싱이 필요한지 확인
     */
    public boolean requiresRebalancing() {
        if (rebalancingActions == null || rebalancingActions.isEmpty()) {
            return false;
        }
        
        return rebalancingActions.stream()
            .anyMatch(action -> action.getActionType() != RebalancingAction.ActionType.HOLD);
    }
    
    /**
     * 총 거래 비용 계산
     */
    public BigDecimal getTotalTransactionCost() {
        if (rebalancingActions == null) {
            return BigDecimal.ZERO;
        }
        
        return rebalancingActions.stream()
            .map(action -> action.getEstimatedCost())
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 현재 대비 예상 개선도 계산
     */
    public BigDecimal getExpectedImprovement() {
        if (expectedPerformance == null || expectedPerformance.getExpectedSharpeRatio() == null) {
            return BigDecimal.ZERO;
        }
        
        // 현재 포트폴리오의 샤프 비율과 최적화된 포트폴리오의 차이
        // 실제 구현시 현재 포트폴리오 성과 계산 필요
        return expectedPerformance.getExpectedSharpeRatio();
    }
    
    /**
     * 고위험 자산 비율 계산
     */
    public BigDecimal getHighRiskAssetRatio() {
        if (recommendedAllocations == null) {
            return BigDecimal.ZERO;
        }
        
        return recommendedAllocations.stream()
            .filter(allocation -> isHighRiskAsset(allocation))
            .map(AssetAllocation::getRecommendedWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private boolean isHighRiskAsset(AssetAllocation allocation) {
        return allocation.getExpectedVolatility() != null && 
               allocation.getExpectedVolatility().compareTo(new BigDecimal("0.20")) > 0;
    }
}