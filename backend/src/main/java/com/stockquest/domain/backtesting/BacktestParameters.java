package com.stockquest.domain.backtesting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 백테스팅 파라미터 설정
 * Phase 8.2: Enhanced Trading Intelligence - 백테스팅 구성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestParameters {
    
    /**
     * 백테스트 이름/ID
     */
    private String backtestName;
    
    /**
     * 테스트 기간
     */
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    /**
     * 테스트 대상 심볼들
     */
    private List<String> symbols;
    
    /**
     * 초기 자본
     */
    private BigDecimal initialCapital;
    
    /**
     * 벤치마크 심볼 (SPY, QQQ 등)
     */
    private String benchmarkSymbol;
    
    /**
     * 거래 전략 설정
     */
    private TradingStrategy strategy;
    
    /**
     * ML 모델 설정
     */
    private MLModelConfig mlConfig;
    
    /**
     * 위험 관리 설정
     */
    private RiskManagementConfig riskConfig;
    
    /**
     * 거래 비용 설정
     */
    private TradingCosts costs;
    
    /**
     * 포지션 관리 설정
     */
    private PositionManagement positionConfig;
    
    /**
     * 데이터 설정
     */
    private DataConfig dataConfig;
    
    /**
     * 성과 분석 설정
     */
    private PerformanceAnalysisConfig performanceConfig;
    
    /**
     * 사용자 정의 메타데이터
     */
    private Map<String, Object> metadata;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingStrategy {
        private String strategyType; // ML_SIGNALS, TECHNICAL_ANALYSIS, HYBRID
        private BigDecimal signalThreshold; // Minimum confidence for trades
        private BigDecimal stopLossPercent;
        private BigDecimal takeProfitPercent;
        private Integer maxHoldingPeriod; // days
        private Integer minHoldingPeriod; // days
        private boolean useTrailingStop;
        private BigDecimal trailingStopPercent;
        private boolean allowShortSelling;
        private BigDecimal maxPositionSize; // % of portfolio
        private Integer maxConcurrentPositions;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MLModelConfig {
        private String modelType; // SimpleTradingModel, RandomForest, etc.
        private Integer trainingPeriod; // days
        private Integer retrainingFrequency; // days
        private BigDecimal confidenceThreshold;
        private boolean useEnsemble;
        private List<String> features;
        private Map<String, Object> hyperparameters;
        private boolean enableContinuousLearning;
        private BigDecimal learningRate;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskManagementConfig {
        private BigDecimal maxPortfolioRisk; // % of portfolio value
        private BigDecimal maxDailyLoss; // Maximum daily loss limit
        private BigDecimal var95Limit; // 95% VaR limit
        private BigDecimal maxDrawdownLimit; // Maximum drawdown before stopping
        private BigDecimal correlationLimit; // Max correlation between positions
        private boolean usePositionSizing;
        private String positionSizingMethod; // FIXED, KELLY, RISK_PARITY
        private BigDecimal riskFreeRate; // For Sharpe ratio calculation
        private boolean enableDynamicHedging;
        private BigDecimal volatilityTargeting;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingCosts {
        private BigDecimal commissionPerTrade; // Fixed commission per trade
        private BigDecimal commissionPercent; // % commission
        private BigDecimal bidAskSpread; // % bid-ask spread
        private BigDecimal slippage; // % slippage
        private BigDecimal borrowCost; // % annual cost for short positions
        private BigDecimal marginInterest; // % annual margin interest
        private boolean includeMarketImpact;
        private BigDecimal marketImpactFactor;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionManagement {
        private String rebalancingFrequency; // DAILY, WEEKLY, MONTHLY, SIGNAL_BASED
        private BigDecimal minTradeSize; // Minimum trade size
        private BigDecimal maxTradeSize; // Maximum trade size
        private boolean enablePartialFills;
        private String orderType; // MARKET, LIMIT, STOP
        private BigDecimal limitOrderTolerance; // % above/below current price
        private Integer orderTimeoutMinutes;
        private boolean enablePositionNetting;
        private BigDecimal cashReservePercent; // % to keep in cash
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataConfig {
        private String dataFrequency; // DAILY, HOURLY, MINUTE
        private boolean adjustForSplits;
        private boolean adjustForDividends;
        private boolean includeAfterHours;
        private String dataProvider; // YAHOO, ALPHA_VANTAGE, etc.
        private boolean enableDataValidation;
        private Integer maxMissingDataPercent;
        private String missingDataStrategy; // SKIP, INTERPOLATE, FORWARD_FILL
        private boolean enableOutlierDetection;
        private BigDecimal outlierThreshold;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceAnalysisConfig {
        private boolean calculateDetailedMetrics;
        private boolean generateTradeAnalysis;
        private boolean enableRiskAnalysis;
        private boolean performBenchmarkComparison;
        private boolean generateReports;
        private String reportFormat; // PDF, HTML, JSON
        private boolean enableVisualization;
        private List<String> customMetrics;
        private Integer rollingWindowDays; // For rolling statistics
        private boolean enableSectorAnalysis;
    }
}