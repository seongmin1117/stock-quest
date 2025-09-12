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
 * 백테스팅 결과 도메인 엔터티
 * Phase 8.2: Enhanced Trading Intelligence - 백테스팅 프레임워크
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResult {
    
    /**
     * 백테스트 실행 ID
     */
    private String backtestId;
    
    /**
     * 테스트 대상 심볼
     */
    private String symbol;
    
    /**
     * 백테스트 기간
     */
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    /**
     * 초기 자본
     */
    private BigDecimal initialCapital;
    
    /**
     * 최종 포트폴리오 가치
     */
    private BigDecimal finalValue;
    
    /**
     * 총 수익률 (%)
     */
    private BigDecimal totalReturn;
    
    /**
     * 연간 수익률 (%)
     */
    private BigDecimal annualizedReturn;
    
    /**
     * 변동성 (표준편차)
     */
    private BigDecimal volatility;
    
    /**
     * 샤프 비율
     */
    private BigDecimal sharpeRatio;
    
    /**
     * 최대 손실 (Maximum Drawdown)
     */
    private BigDecimal maxDrawdown;
    
    /**
     * 승률 (Win Rate)
     */
    private BigDecimal winRate;
    
    /**
     * 평균 수익
     */
    private BigDecimal averageWin;
    
    /**
     * 평균 손실
     */
    private BigDecimal averageLoss;
    
    /**
     * 수익/손실 비율
     */
    private BigDecimal profitLossRatio;
    
    /**
     * 총 거래 횟수
     */
    private Integer totalTrades;
    
    /**
     * 수익 거래 수
     */
    private Integer winningTrades;
    
    /**
     * 손실 거래 수
     */
    private Integer losingTrades;
    
    /**
     * 칼마 비율 (Calmar Ratio)
     */
    private BigDecimal calmarRatio;
    
    /**
     * 소르티노 비율 (Sortino Ratio)
     */
    private BigDecimal sortinoRatio;
    
    /**
     * 베타 (시장 대비)
     */
    private BigDecimal beta;
    
    /**
     * 알파 (초과 수익)
     */
    private BigDecimal alpha;
    
    /**
     * 정보 비율 (Information Ratio)
     */
    private BigDecimal informationRatio;
    
    /**
     * 트레이닝 비율 (Treynor Ratio)
     */
    private BigDecimal treynorRatio;
    
    /**
     * 개별 거래 내역
     */
    private List<TradeRecord> trades;
    
    /**
     * 일별 수익률 시계열
     */
    private List<DailyReturn> dailyReturns;
    
    /**
     * 성과 지표별 상세 분석
     */
    private Map<String, Object> detailedMetrics;
    
    /**
     * 백테스트 설정 파라미터
     */
    private BacktestParameters parameters;
    
    /**
     * 위험 지표
     */
    private RiskMetrics riskMetrics;
    
    /**
     * 벤치마크 대비 성과
     */
    private BenchmarkComparison benchmarkComparison;
    
    /**
     * 백테스트 실행 시간
     */
    private LocalDateTime executionTime;
    
    /**
     * 백테스트 실행 소요시간 (ms)
     */
    private Long executionDuration;
    
    /**
     * ML 모델 성과 평가
     */
    private MLModelPerformance mlPerformance;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRecord {
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private String action; // BUY, SELL
        private BigDecimal entryPrice;
        private BigDecimal exitPrice;
        private BigDecimal quantity;
        private BigDecimal pnl;
        private BigDecimal pnlPercent;
        private String signal; // ML signal that triggered trade
        private BigDecimal confidence; // ML confidence score
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyReturn {
        private LocalDateTime date;
        private BigDecimal portfolioValue;
        private BigDecimal dailyReturn;
        private BigDecimal cumulativeReturn;
        private BigDecimal drawdown;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskMetrics {
        private BigDecimal var95; // 95% Value at Risk
        private BigDecimal var99; // 99% Value at Risk
        private BigDecimal cvar95; // 95% Conditional VaR
        private BigDecimal skewness;
        private BigDecimal kurtosis;
        private BigDecimal downsideDeviation;
        private Integer maxConsecutiveLosses;
        private BigDecimal maxLossStreak;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkComparison {
        private String benchmarkSymbol;
        private BigDecimal benchmarkReturn;
        private BigDecimal excessReturn;
        private BigDecimal trackingError;
        private BigDecimal beta;
        private BigDecimal alpha;
        private BigDecimal correlationCoefficient;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MLModelPerformance {
        private BigDecimal signalAccuracy; // Signal prediction accuracy
        private BigDecimal precisionScore;
        private BigDecimal recallScore;
        private BigDecimal f1Score;
        private Integer truePositives;
        private Integer trueNegatives;
        private Integer falsePositives;
        private Integer falseNegatives;
        private BigDecimal confidenceCorrelation; // Correlation between confidence and outcome
        private List<ConfidenceBucket> confidenceBuckets;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceBucket {
        private BigDecimal confidenceMin;
        private BigDecimal confidenceMax;
        private Integer tradeCount;
        private BigDecimal winRate;
        private BigDecimal averagePnL;
    }
}