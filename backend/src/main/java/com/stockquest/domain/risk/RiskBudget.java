package com.stockquest.domain.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 리스크 예산 도메인 모델
 * Phase 8.3: Advanced Risk Management - 리스크 예산 할당 및 모니터링 시스템
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskBudget {
    
    /**
     * 리스크 예산 고유 ID
     */
    private String budgetId;
    
    /**
     * 포트폴리오 ID
     */
    private String portfolioId;
    
    /**
     * 예산 이름
     */
    private String budgetName;
    
    /**
     * 예산 설명
     */
    private String description;
    
    /**
     * 예산 타입
     */
    private BudgetType budgetType;
    
    /**
     * 예산 상태
     */
    private BudgetStatus status;
    
    /**
     * 총 리스크 예산 (전체 한도)
     */
    private BigDecimal totalRiskBudget;
    
    /**
     * 사용된 리스크 예산
     */
    private BigDecimal usedRiskBudget;
    
    /**
     * 남은 리스크 예산
     */
    private BigDecimal remainingRiskBudget;
    
    /**
     * 예산 사용률 (%)
     */
    private BigDecimal utilizationRate;
    
    /**
     * 리스크 메트릭 타입
     */
    private RiskMetricType metricType;
    
    /**
     * 할당 단위
     */
    private AllocationUnit allocationUnit;
    
    /**
     * 자산별 할당
     */
    private List<AssetAllocation> assetAllocations;
    
    /**
     * 섹터별 할당
     */
    private List<SectorAllocation> sectorAllocations;
    
    /**
     * 전략별 할당
     */
    private List<StrategyAllocation> strategyAllocations;
    
    /**
     * 예산 한도 설정
     */
    private BudgetLimits budgetLimits;
    
    /**
     * 모니터링 설정
     */
    private MonitoringConfiguration monitoring;
    
    /**
     * 예산 히스토리
     */
    private BudgetHistory history;
    
    /**
     * 예산 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 예산 유효 기간 시작
     */
    private LocalDateTime validFrom;
    
    /**
     * 예산 유효 기간 종료
     */
    private LocalDateTime validTo;
    
    /**
     * 다음 재검토 일정
     */
    private LocalDateTime nextReviewDate;
    
    /**
     * 승인자 정보
     */
    private String approvedBy;
    
    /**
     * 승인 시간
     */
    private LocalDateTime approvedAt;
    
    /**
     * 예산 메타데이터
     */
    private Map<String, Object> metadata;
    
    public enum BudgetType {
        VAR_BASED("VaR 기반", "Value-at-Risk 기반 예산"),
        VOLATILITY_BASED("변동성 기반", "변동성 기반 예산"),
        TRACKING_ERROR_BASED("추적오차 기반", "벤치마크 대비 추적오차 기반"),
        DRAWDOWN_BASED("낙폭 기반", "최대 낙폭 기반 예산"),
        CAPITAL_BASED("자본 기반", "자본금 기반 예산"),
        NOTIONAL_BASED("명목액 기반", "거래 명목액 기반 예산"),
        CONCENTRATION_BASED("집중도 기반", "포지션 집중도 기반 예산"),
        LEVERAGE_BASED("레버리지 기반", "레버리지 비율 기반 예산"),
        COMPOSITE("복합형", "여러 리스크 지표 조합"),
        REGULATORY("규제 기반", "규제 요구사항 기반 예산");
        
        private final String description;
        private final String details;
        
        BudgetType(String description, String details) {
            this.description = description;
            this.details = details;
        }
        
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public enum BudgetStatus {
        DRAFT("초안", "예산 작성 중"),
        PENDING_APPROVAL("승인 대기", "승인 대기 중"),
        APPROVED("승인됨", "승인된 예산"),
        ACTIVE("활성화", "현재 사용 중인 예산"),
        SUSPENDED("일시정지", "일시적으로 정지된 예산"),
        EXPIRED("만료됨", "유효기간 만료"),
        CANCELLED("취소됨", "취소된 예산"),
        UNDER_REVIEW("검토중", "정기 검토 중");
        
        private final String description;
        private final String details;
        
        BudgetStatus(String description, String details) {
            this.description = description;
            this.details = details;
        }
        
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public enum RiskMetricType {
        VALUE_AT_RISK("Value at Risk", "VaR", "%"),
        CONDITIONAL_VAR("Conditional VaR", "CVaR", "%"),
        VOLATILITY("변동성", "Vol", "%"),
        TRACKING_ERROR("추적 오차", "TE", "%"),
        MAXIMUM_DRAWDOWN("최대 낙폭", "MDD", "%"),
        BETA("베타", "Beta", "ratio"),
        LEVERAGE("레버리지", "Leverage", "ratio"),
        CONCENTRATION("집중도", "HHI", "index"),
        NOTIONAL_AMOUNT("명목 거래액", "Notional", "currency"),
        MARKET_VALUE("시가총액", "MV", "currency");
        
        private final String description;
        private final String shortName;
        private final String unit;
        
        RiskMetricType(String description, String shortName, String unit) {
            this.description = description;
            this.shortName = shortName;
            this.unit = unit;
        }
        
        public String getDescription() { return description; }
        public String getShortName() { return shortName; }
        public String getUnit() { return unit; }
    }
    
    public enum AllocationUnit {
        PERCENTAGE("퍼센트", "%"),
        ABSOLUTE_VALUE("절대값", "currency"),
        BASIS_POINTS("베이시스 포인트", "bp"),
        STANDARD_DEVIATION("표준편차", "σ"),
        DOLLAR_AMOUNT("달러 금액", "$");
        
        private final String description;
        private final String symbol;
        
        AllocationUnit(String description, String symbol) {
            this.description = description;
            this.symbol = symbol;
        }
        
        public String getDescription() { return description; }
        public String getSymbol() { return symbol; }
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
         * 자산 클래스
         */
        private AssetClass assetClass;
        
        /**
         * 할당된 리스크 예산
         */
        private BigDecimal allocatedBudget;
        
        /**
         * 사용된 리스크 예산
         */
        private BigDecimal usedBudget;
        
        /**
         * 예산 사용률 (%)
         */
        private BigDecimal utilizationRate;
        
        /**
         * 최대 허용 한도
         */
        private BigDecimal maxLimit;
        
        /**
         * 최소 할당 한도
         */
        private BigDecimal minLimit;
        
        /**
         * 현재 포지션 크기
         */
        private BigDecimal currentPosition;
        
        /**
         * 리스크 기여도
         */
        private BigDecimal riskContribution;
        
        /**
         * 한도 초과 여부
         */
        @Builder.Default
        private Boolean limitExceeded = false;
        
        /**
         * 마지막 업데이트 시간
         */
        private LocalDateTime lastUpdatedAt;
        
        public enum AssetClass {
            EQUITY("주식", 1.0),
            BOND("채권", 0.3),
            COMMODITY("원자재", 1.5),
            REAL_ESTATE("부동산", 0.8),
            CURRENCY("통화", 0.5),
            CRYPTOCURRENCY("암호화폐", 2.0),
            DERIVATIVE("파생상품", 3.0),
            ALTERNATIVE("대체투자", 1.2),
            CASH("현금", 0.0);
            
            private final String description;
            private final double riskWeight;
            
            AssetClass(String description, double riskWeight) {
                this.description = description;
                this.riskWeight = riskWeight;
            }
            
            public String getDescription() { return description; }
            public double getRiskWeight() { return riskWeight; }
        }
        
        /**
         * 예산 초과 금액 계산
         */
        public BigDecimal calculateBudgetExcess() {
            if (usedBudget == null || allocatedBudget == null) {
                return BigDecimal.ZERO;
            }
            BigDecimal excess = usedBudget.subtract(allocatedBudget);
            return excess.compareTo(BigDecimal.ZERO) > 0 ? excess : BigDecimal.ZERO;
        }
        
        /**
         * 예산 여유분 계산
         */
        public BigDecimal calculateBudgetBuffer() {
            if (allocatedBudget == null || usedBudget == null) {
                return allocatedBudget != null ? allocatedBudget : BigDecimal.ZERO;
            }
            return allocatedBudget.subtract(usedBudget);
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorAllocation {
        
        /**
         * 섹터 코드
         */
        private String sectorCode;
        
        /**
         * 섹터 이름
         */
        private String sectorName;
        
        /**
         * 산업 분류
         */
        private IndustryClassification classification;
        
        /**
         * 할당된 리스크 예산
         */
        private BigDecimal allocatedBudget;
        
        /**
         * 사용된 리스크 예산
         */
        private BigDecimal usedBudget;
        
        /**
         * 예산 사용률 (%)
         */
        private BigDecimal utilizationRate;
        
        /**
         * 섹터 내 자산 수
         */
        private Integer assetCount;
        
        /**
         * 섹터 집중도 지수
         */
        private BigDecimal concentrationIndex;
        
        /**
         * 섹터 베타
         */
        private BigDecimal sectorBeta;
        
        /**
         * 섹터 변동성
         */
        private BigDecimal sectorVolatility;
        
        public enum IndustryClassification {
            TECHNOLOGY("기술"),
            FINANCIAL_SERVICES("금융 서비스"),
            HEALTHCARE("헬스케어"),
            CONSUMER_DISCRETIONARY("소비재"),
            CONSUMER_STAPLES("필수소비재"),
            INDUSTRIALS("산업재"),
            ENERGY("에너지"),
            UTILITIES("유틸리티"),
            MATERIALS("소재"),
            REAL_ESTATE("부동산"),
            TELECOMMUNICATIONS("통신");
            
            private final String description;
            
            IndustryClassification(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategyAllocation {
        
        /**
         * 전략 ID
         */
        private String strategyId;
        
        /**
         * 전략 이름
         */
        private String strategyName;
        
        /**
         * 전략 타입
         */
        private StrategyType strategyType;
        
        /**
         * 할당된 리스크 예산
         */
        private BigDecimal allocatedBudget;
        
        /**
         * 사용된 리스크 예산
         */
        private BigDecimal usedBudget;
        
        /**
         * 예산 사용률 (%)
         */
        private BigDecimal utilizationRate;
        
        /**
         * 전략 성과 (Sharpe Ratio)
         */
        private BigDecimal strategyPerformance;
        
        /**
         * 리스크 조정 수익률
         */
        private BigDecimal riskAdjustedReturn;
        
        /**
         * 활성 포지션 수
         */
        private Integer activePositions;
        
        public enum StrategyType {
            LONG_ONLY("롱 온리"),
            LONG_SHORT("롱/숏"),
            MARKET_NEUTRAL("마켓 뉴트럴"),
            MOMENTUM("모멘텀"),
            MEAN_REVERSION("평균 회귀"),
            ARBITRAGE("차익거래"),
            QUANTITATIVE("퀀트"),
            FUNDAMENTAL("펀더멘털"),
            TREND_FOLLOWING("추세 추종"),
            PAIRS_TRADING("페어 트레이딩"),
            ALGORITHMIC("알고리즘"),
            HIGH_FREQUENCY("고빈도 거래");
            
            private final String description;
            
            StrategyType(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetLimits {
        
        /**
         * 개별 자산 최대 한도 (%)
         */
        @Builder.Default
        private BigDecimal maxSingleAssetLimit = new BigDecimal("10.0");
        
        /**
         * 섹터별 최대 한도 (%)
         */
        @Builder.Default
        private BigDecimal maxSectorLimit = new BigDecimal("25.0");
        
        /**
         * 전략별 최대 한도 (%)
         */
        @Builder.Default
        private BigDecimal maxStrategyLimit = new BigDecimal("50.0");
        
        /**
         * 총 예산 대비 최대 사용률 (%)
         */
        @Builder.Default
        private BigDecimal maxUtilizationRate = new BigDecimal("95.0");
        
        /**
         * 집중도 한도 (HHI)
         */
        @Builder.Default
        private BigDecimal concentrationLimit = new BigDecimal("0.25");
        
        /**
         * 레버리지 한도 (배수)
         */
        @Builder.Default
        private BigDecimal leverageLimit = new BigDecimal("3.0");
        
        /**
         * 유동성 최소 비율 (%)
         */
        @Builder.Default
        private BigDecimal minLiquidityRatio = new BigDecimal("5.0");
        
        /**
         * 경고 임계값 (%)
         */
        @Builder.Default
        private BigDecimal warningThreshold = new BigDecimal("80.0");
        
        /**
         * 위험 임계값 (%)
         */
        @Builder.Default
        private BigDecimal criticalThreshold = new BigDecimal("90.0");
        
        /**
         * 자동 재조정 임계값 (%)
         */
        @Builder.Default
        private BigDecimal autoRebalanceThreshold = new BigDecimal("85.0");
        
        /**
         * 스트레스 테스트 한도
         */
        private StressTestLimits stressTestLimits;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StressTestLimits {
            
            /**
             * 최대 허용 스트레스 손실 (%)
             */
            @Builder.Default
            private BigDecimal maxStressLoss = new BigDecimal("15.0");
            
            /**
             * VaR 한도 (%)
             */
            @Builder.Default
            private BigDecimal varLimit = new BigDecimal("5.0");
            
            /**
             * CVaR 한도 (%)
             */
            @Builder.Default
            private BigDecimal cvarLimit = new BigDecimal("8.0");
            
            /**
             * 최대 낙폭 한도 (%)
             */
            @Builder.Default
            private BigDecimal maxDrawdownLimit = new BigDecimal("20.0");
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitoringConfiguration {
        
        /**
         * 모니터링 빈도 (분)
         */
        @Builder.Default
        private Integer monitoringFrequencyMinutes = 15;
        
        /**
         * 실시간 모니터링 활성화
         */
        @Builder.Default
        private Boolean realTimeMonitoring = true;
        
        /**
         * 한도 위반 알림
         */
        @Builder.Default
        private Boolean limitBreachAlert = true;
        
        /**
         * 일일 보고서 생성
         */
        @Builder.Default
        private Boolean dailyReporting = true;
        
        /**
         * 주간 요약 보고서
         */
        @Builder.Default
        private Boolean weeklyReporting = true;
        
        /**
         * 월간 검토 보고서
         */
        @Builder.Default
        private Boolean monthlyReporting = true;
        
        /**
         * 자동 재조정 활성화
         */
        @Builder.Default
        private Boolean autoRebalancing = false;
        
        /**
         * 알림 채널
         */
        private List<NotificationChannel> notificationChannels;
        
        /**
         * 대시보드 설정
         */
        private DashboardConfiguration dashboardConfig;
        
        public enum NotificationChannel {
            EMAIL("이메일"),
            SMS("SMS"),
            SLACK("슬랙"),
            TEAMS("팀즈"),
            WEBHOOK("웹훅"),
            MOBILE_PUSH("모바일 푸시");
            
            private final String description;
            
            NotificationChannel(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DashboardConfiguration {
            
            /**
             * 실시간 차트 활성화
             */
            @Builder.Default
            private Boolean realTimeCharts = true;
            
            /**
             * 히트맵 표시
             */
            @Builder.Default
            private Boolean heatmapDisplay = true;
            
            /**
             * 트렌드 분석 표시
             */
            @Builder.Default
            private Boolean trendAnalysis = true;
            
            /**
             * 예측 모델 표시
             */
            @Builder.Default
            private Boolean forecastDisplay = false;
            
            /**
             * 새로고침 간격 (초)
             */
            @Builder.Default
            private Integer refreshIntervalSeconds = 30;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetHistory {
        
        /**
         * 이전 버전 목록
         */
        private List<BudgetVersion> versions;
        
        /**
         * 한도 위반 히스토리
         */
        private List<LimitBreach> limitBreaches;
        
        /**
         * 재조정 히스토리
         */
        private List<RebalanceEvent> rebalanceEvents;
        
        /**
         * 성과 히스토리
         */
        private List<PerformanceRecord> performanceHistory;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BudgetVersion {
            private String versionId;
            private BigDecimal totalBudget;
            private LocalDateTime createdAt;
            private String createdBy;
            private String changeReason;
            private Map<String, Object> changes;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class LimitBreach {
            private LocalDateTime breachTime;
            private String breachType;
            private String affectedAsset;
            private BigDecimal breachAmount;
            private BigDecimal limitValue;
            private String resolution;
            private LocalDateTime resolvedAt;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RebalanceEvent {
            private LocalDateTime rebalanceTime;
            private String trigger;
            private List<String> affectedAssets;
            private BigDecimal totalAdjustment;
            private String outcome;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerformanceRecord {
            private LocalDateTime recordDate;
            private BigDecimal utilizationRate;
            private BigDecimal riskContribution;
            private BigDecimal returnContribution;
            private BigDecimal efficiency;
        }
    }
    
    /**
     * 예산이 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == BudgetStatus.ACTIVE && 
               (validTo == null || validTo.isAfter(LocalDateTime.now()));
    }
    
    /**
     * 한도 초과 여부 확인
     */
    public boolean isLimitExceeded() {
        return utilizationRate != null && 
               budgetLimits != null &&
               utilizationRate.compareTo(budgetLimits.getCriticalThreshold()) > 0;
    }
    
    /**
     * 경고 수준 도달 여부 확인
     */
    public boolean isWarningLevel() {
        return utilizationRate != null && 
               budgetLimits != null &&
               utilizationRate.compareTo(budgetLimits.getWarningThreshold()) > 0;
    }
    
    /**
     * 재조정이 필요한지 확인
     */
    public boolean requiresRebalancing() {
        return utilizationRate != null && 
               budgetLimits != null &&
               budgetLimits.getAutoRebalanceThreshold() != null &&
               utilizationRate.compareTo(budgetLimits.getAutoRebalanceThreshold()) > 0;
    }
    
    /**
     * 예산 사용률 계산
     */
    public BigDecimal calculateUtilizationRate() {
        if (totalRiskBudget == null || totalRiskBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (usedRiskBudget == null) {
            return BigDecimal.ZERO;
        }
        
        return usedRiskBudget.divide(totalRiskBudget, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 남은 예산 계산
     */
    public BigDecimal calculateRemainingBudget() {
        if (totalRiskBudget == null) {
            return BigDecimal.ZERO;
        }
        
        if (usedRiskBudget == null) {
            return totalRiskBudget;
        }
        
        return totalRiskBudget.subtract(usedRiskBudget);
    }
    
    /**
     * 집중도 지수 계산 (HHI)
     */
    public BigDecimal calculateConcentrationIndex() {
        if (assetAllocations == null || assetAllocations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalAllocation = assetAllocations.stream()
            .map(AssetAllocation::getAllocatedBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAllocation.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal hhi = assetAllocations.stream()
            .map(allocation -> {
                BigDecimal weight = allocation.getAllocatedBudget().divide(
                    totalAllocation, 4, java.math.RoundingMode.HALF_UP);
                return weight.multiply(weight);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return hhi;
    }
    
    /**
     * 리스크 효율성 계산
     */
    public BigDecimal calculateRiskEfficiency() {
        if (assetAllocations == null || assetAllocations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalReturn = assetAllocations.stream()
            .map(allocation -> allocation.getRiskContribution() != null ? 
                allocation.getRiskContribution() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (usedRiskBudget == null || usedRiskBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalReturn.divide(usedRiskBudget, 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 예산 요약 정보 생성
     */
    public String generateBudgetSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("리스크 예산: %s [%s]\n", budgetName, status.getDescription()));
        summary.append(String.format("총 예산: %s, 사용: %s (%.2f%%)\n", 
            totalRiskBudget, usedRiskBudget, utilizationRate));
        summary.append(String.format("남은 예산: %s\n", remainingRiskBudget));
        
        if (assetAllocations != null) {
            summary.append(String.format("자산 배분: %d개\n", assetAllocations.size()));
        }
        
        if (isWarningLevel()) {
            summary.append("⚠️ 경고: 예산 사용률이 임계치에 근접했습니다.\n");
        }
        
        if (isLimitExceeded()) {
            summary.append("🚨 위험: 예산 한도를 초과했습니다!\n");
        }
        
        return summary.toString();
    }
    
    /**
     * 다음 검토 일정 계산
     */
    public LocalDateTime calculateNextReviewDate() {
        if (validFrom == null) {
            return LocalDateTime.now().plusMonths(1);
        }
        
        // 일반적으로 매월 검토
        return validFrom.plusMonths(1);
    }
}