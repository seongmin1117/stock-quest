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
 * ML-powered 트레이딩 시그널 도메인 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingSignal {
    
    /**
     * 시그널 고유 ID
     */
    private String signalId;
    
    /**
     * 대상 주식 심볼
     */
    private String symbol;
    
    /**
     * 시그널 타입 (BUY, SELL, HOLD)
     */
    private SignalType signalType;
    
    /**
     * 신호 강도 (0.0 ~ 1.0)
     */
    private BigDecimal strength;
    
    /**
     * 신뢰도 점수 (0.0 ~ 1.0)
     */
    private BigDecimal confidence;
    
    /**
     * 예상 수익률 (%)
     */
    private BigDecimal expectedReturn;
    
    /**
     * 예상 위험도 (표준편차 %)
     */
    private BigDecimal expectedRisk;
    
    /**
     * 시간 지평 (일)
     */
    private Integer timeHorizon;
    
    /**
     * 목표 가격
     */
    private BigDecimal targetPrice;
    
    /**
     * 손절매 가격
     */
    private BigDecimal stopLossPrice;
    
    /**
     * 시그널 생성 시간
     */
    private LocalDateTime generatedAt;
    
    /**
     * 시그널 만료 시간
     */
    private LocalDateTime expiresAt;
    
    /**
     * ML 모델 정보
     */
    private ModelInfo modelInfo;
    
    /**
     * 시그널 근거 (feature importance)
     */
    private List<SignalReason> reasons;
    
    /**
     * 시장 조건 정보
     */
    private MarketCondition marketCondition;
    
    /**
     * 성과 추적 정보
     */
    private PerformanceTracking performanceTracking;
    
    /**
     * 시그널 상태
     */
    @Builder.Default
    private SignalStatus status = SignalStatus.ACTIVE;
    
    public enum SignalType {
        STRONG_BUY,
        BUY,
        WEAK_BUY,
        HOLD,
        NEUTRAL,
        WEAK_SELL,
        SELL,
        STRONG_SELL
    }
    
    public enum SignalStatus {
        ACTIVE,
        EXECUTED,
        EXPIRED,
        CANCELLED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelInfo {
        
        /**
         * 사용된 ML 모델명
         */
        private String modelName;
        
        /**
         * 모델 버전
         */
        private String modelVersion;
        
        /**
         * 모델 정확도
         */
        private BigDecimal modelAccuracy;
        
        /**
         * 모델 훈련 데이터 기간
         */
        private String trainingPeriod;
        
        /**
         * 사용된 특성 개수
         */
        private Integer featureCount;
        
        /**
         * 모델 타입 (RF, XGBoost, LSTM, etc.)
         */
        private String algorithmType;
        
        /**
         * 모델 메타데이터
         */
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalReason {
        
        /**
         * 특성명
         */
        private String featureName;
        
        /**
         * 중요도 점수 (0.0 ~ 1.0)
         */
        private BigDecimal importance;
        
        /**
         * 특성 값
         */
        private BigDecimal value;
        
        /**
         * 설명
         */
        private String description;
        
        /**
         * 카테고리 (TECHNICAL, FUNDAMENTAL, SENTIMENT, etc.)
         */
        private ReasonCategory category;
    }
    
    public enum ReasonCategory {
        TECHNICAL,
        FUNDAMENTAL,
        SENTIMENT,
        MACRO_ECONOMIC,
        VOLATILITY,
        MOMENTUM,
        MEAN_REVERSION,
        VOLUME,
        CORRELATION
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketCondition {
        
        /**
         * 시장 체제 (BULL, BEAR, SIDEWAYS)
         */
        private MarketRegime regime;
        
        /**
         * 변동성 수준 (LOW, MEDIUM, HIGH)
         */
        private VolatilityLevel volatility;
        
        /**
         * 유동성 상태
         */
        private LiquidityCondition liquidity;
        
        /**
         * 전반적 시장 심리 (-1.0 ~ 1.0)
         */
        private BigDecimal marketSentiment;
        
        /**
         * VIX 수준
         */
        private BigDecimal vixLevel;
        
        /**
         * 섹터 강도
         */
        private Map<String, BigDecimal> sectorStrengths;
    }
    
    public enum MarketRegime {
        BULL_MARKET,
        BEAR_MARKET,
        SIDEWAYS_MARKET,
        HIGH_VOLATILITY,
        LOW_VOLATILITY
    }
    
    public enum VolatilityLevel {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
    
    public enum LiquidityCondition {
        HIGH_LIQUIDITY,
        NORMAL_LIQUIDITY,
        LOW_LIQUIDITY,
        STRESSED_LIQUIDITY
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceTracking {
        
        /**
         * 시그널 실행 가격
         */
        private BigDecimal executionPrice;
        
        /**
         * 실행 시간
         */
        private LocalDateTime executionTime;
        
        /**
         * 현재 가격
         */
        private BigDecimal currentPrice;
        
        /**
         * 현재까지의 수익률
         */
        private BigDecimal unrealizedReturn;
        
        /**
         * 최대 수익률
         */
        private BigDecimal maxReturn;
        
        /**
         * 최대 손실률
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 시그널 정확도 (실현시)
         */
        private BigDecimal accuracy;
        
        /**
         * 성과 업데이트 시간
         */
        private LocalDateTime lastUpdated;
        
        /**
         * 위험 조정 수익률
         */
        private BigDecimal riskAdjustedReturn;
    }
    
    /**
     * 시그널이 여전히 유효한지 확인
     */
    public boolean isActive() {
        return status == SignalStatus.ACTIVE && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * 시그널의 리스크-수익 비율 계산
     */
    public BigDecimal getRiskRewardRatio() {
        if (expectedRisk != null && expectedRisk.compareTo(BigDecimal.ZERO) > 0) {
            return expectedReturn.divide(expectedRisk, 4, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 신호 점수 계산 (강도 × 신뢰도)
     */
    public BigDecimal getSignalScore() {
        return strength.multiply(confidence);
    }
    
    /**
     * 상위 3개 시그널 근거 반환
     */
    public List<SignalReason> getTopReasons() {
        if (reasons == null || reasons.isEmpty()) {
            return List.of();
        }
        
        return reasons.stream()
            .sorted((a, b) -> b.getImportance().compareTo(a.getImportance()))
            .limit(3)
            .toList();
    }
    
    /**
     * 카테고리별 중요도 집계
     */
    public Map<ReasonCategory, BigDecimal> getCategoryImportance() {
        if (reasons == null || reasons.isEmpty()) {
            return Map.of();
        }
        
        return reasons.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                SignalReason::getCategory,
                java.util.stream.Collectors.reducing(
                    BigDecimal.ZERO,
                    SignalReason::getImportance,
                    BigDecimal::add
                )
            ));
    }
}