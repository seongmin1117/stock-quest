package com.stockquest.domain.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 거래 도메인 모델
 * Phase 8.4: Real-time Execution Engine - Individual Trade Execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    
    /**
     * 거래 고유 ID
     */
    private String tradeId;
    
    /**
     * 원래 주문 ID
     */
    private String orderId;
    
    /**
     * 포트폴리오 ID
     */
    private String portfolioId;
    
    /**
     * 사용자 ID
     */
    private String userId;
    
    /**
     * 심볼
     */
    private String symbol;
    
    /**
     * 거래 사이드
     */
    private TradeSide side;
    
    /**
     * 거래 수량
     */
    private BigDecimal quantity;
    
    /**
     * 거래 가격
     */
    private BigDecimal price;
    
    /**
     * 거래 금액
     */
    private BigDecimal amount;
    
    /**
     * 거래 시간
     */
    private LocalDateTime tradeTime;
    
    /**
     * 결제일
     */
    private LocalDate settlementDate;
    
    /**
     * 거래 타입
     */
    private TradeType tradeType;
    
    /**
     * 실행 장소
     */
    private ExecutionVenue executionVenue;
    
    /**
     * 거래 상태
     */
    private TradeStatus status;
    
    /**
     * 시장 상태 (거래 시점)
     */
    private MarketConditions marketConditions;
    
    /**
     * 실행 품질 메트릭
     */
    private ExecutionQualityMetrics executionMetrics;
    
    /**
     * 수수료 정보
     */
    private CommissionInfo commission;
    
    /**
     * 세금 정보
     */
    private TaxInfo taxInfo;
    
    /**
     * 리스크 메트릭
     */
    private TradeRiskMetrics riskMetrics;
    
    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata;
    
    /**
     * 거래 사이드 열거형
     */
    public enum TradeSide {
        BUY("매수", 1),
        SELL("매도", -1),
        SELL_SHORT("공매도", -1),
        BUY_TO_COVER("단기매수", 1);
        
        private final String koreanName;
        private final int positionMultiplier;
        
        TradeSide(String koreanName, int positionMultiplier) {
            this.koreanName = koreanName;
            this.positionMultiplier = positionMultiplier;
        }
        
        public String getKoreanName() { return koreanName; }
        public int getPositionMultiplier() { return positionMultiplier; }
    }
    
    /**
     * 거래 타입 열거형
     */
    public enum TradeType {
        REGULAR("일반거래", "Regular market trade"),
        BLOCK("블록거래", "Large block trade"),
        DARK_POOL("다크풀", "Dark pool execution"),
        CROSSING("크로싱", "Crossing network trade"),
        ICEBERG("빙산거래", "Iceberg order execution"),
        ALGORITHM("알고리즘", "Algorithm-driven trade"),
        MANUAL("수동거래", "Manual execution"),
        OPENING("시초가", "Opening auction trade"),
        CLOSING("종가", "Closing auction trade"),
        AFTER_HOURS("시간외", "After-hours trade");
        
        private final String koreanName;
        private final String description;
        
        TradeType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 실행 장소 열거형
     */
    public enum ExecutionVenue {
        KRX("한국거래소", "Korea Exchange"),
        KOSDAQ("코스닥", "KOSDAQ"),
        DARK_POOL_A("다크풀A", "Dark Pool A"),
        DARK_POOL_B("다크풀B", "Dark Pool B"),
        CROSSING_NETWORK("크로싱네트워크", "Electronic Crossing Network"),
        INTERNAL("내부거래", "Internal execution"),
        SIMULATION("시뮬레이션", "Simulated execution for Stock Quest");
        
        private final String koreanName;
        private final String description;
        
        ExecutionVenue(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 거래 상태 열거형
     */
    public enum TradeStatus {
        PENDING("처리대기", "Trade pending processing"),
        EXECUTED("체결완료", "Trade executed successfully"),
        SETTLED("결제완료", "Trade settled"),
        FAILED("실패", "Trade execution failed"),
        CANCELED("취소됨", "Trade canceled"),
        DISPUTED("분쟁", "Trade under dispute");
        
        private final String koreanName;
        private final String description;
        
        TradeStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 시장 상황 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketConditions {
        
        /**
         * 거래 시점 시장 가격
         */
        private BigDecimal marketPrice;
        
        /**
         * 호가창 상황
         */
        private OrderBookSnapshot orderBook;
        
        /**
         * 거래량 정보
         */
        private VolumeInfo volumeInfo;
        
        /**
         * 변동성 수준
         */
        private BigDecimal volatility;
        
        /**
         * 유동성 수준
         */
        private BigDecimal liquidityLevel;
        
        /**
         * 스프레드
         */
        private BigDecimal bidAskSpread;
        
        /**
         * 시장 트렌드
         */
        private MarketTrend marketTrend;
    }
    
    /**
     * 호가창 스냅샷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookSnapshot {
        private BigDecimal bestBid;
        private BigDecimal bestAsk;
        private BigDecimal bidSize;
        private BigDecimal askSize;
        private BigDecimal midPrice;
        private LocalDateTime snapshotTime;
    }
    
    /**
     * 거래량 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeInfo {
        private BigDecimal currentVolume;
        private BigDecimal averageDailyVolume;
        private BigDecimal volumeRate;
        private BigDecimal vwap;
    }
    
    /**
     * 시장 트렌드
     */
    public enum MarketTrend {
        BULLISH("상승"), BEARISH("하락"), SIDEWAYS("보합"), VOLATILE("변동성높음");
        
        private final String koreanName;
        
        MarketTrend(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 실행 품질 메트릭
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionQualityMetrics {
        
        /**
         * 실현 슬리피지
         */
        private BigDecimal realizedSlippage;
        
        /**
         * 시장 영향
         */
        private BigDecimal marketImpact;
        
        /**
         * 타이밍 비용
         */
        private BigDecimal timingCost;
        
        /**
         * 기회 비용
         */
        private BigDecimal opportunityCost;
        
        /**
         * 실행 효율성 점수 (0-100)
         */
        private BigDecimal executionEfficiency;
        
        /**
         * VWAP 대비 성과
         */
        private BigDecimal vwapPerformance;
        
        /**
         * 도착가격 대비 성과
         */
        private BigDecimal arrivalPricePerformance;
        
        /**
         * 실행 속도 (밀리초)
         */
        private Long executionSpeed;
        
        /**
         * 유동성 소비량
         */
        private BigDecimal liquidityConsumed;
        
        /**
         * 실행 품질 등급
         */
        private ExecutionGrade executionGrade;
    }
    
    /**
     * 실행 품질 등급
     */
    public enum ExecutionGrade {
        EXCELLENT("우수", 90),
        GOOD("양호", 70),
        FAIR("보통", 50),
        POOR("미흡", 30),
        VERY_POOR("매우미흡", 0);
        
        private final String koreanName;
        private final int minScore;
        
        ExecutionGrade(String koreanName, int minScore) {
            this.koreanName = koreanName;
            this.minScore = minScore;
        }
        
        public String getKoreanName() { return koreanName; }
        public int getMinScore() { return minScore; }
        
        public static ExecutionGrade fromScore(BigDecimal score) {
            double scoreValue = score.doubleValue();
            for (ExecutionGrade grade : values()) {
                if (scoreValue >= grade.minScore) {
                    return grade;
                }
            }
            return VERY_POOR;
        }
    }
    
    /**
     * 수수료 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommissionInfo {
        
        /**
         * 기본 수수료
         */
        private BigDecimal baseCommission;
        
        /**
         * 거래세
         */
        private BigDecimal transactionTax;
        
        /**
         * 거래소 수수료
         */
        private BigDecimal exchangeFee;
        
        /**
         * 기타 수수료
         */
        private BigDecimal otherFees;
        
        /**
         * 총 수수료
         */
        private BigDecimal totalCommission;
        
        /**
         * 수수료율
         */
        private BigDecimal commissionRate;
    }
    
    /**
     * 세금 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxInfo {
        
        /**
         * 증권거래세
         */
        private BigDecimal securitiesTransactionTax;
        
        /**
         * 농특세
         */
        private BigDecimal ruralDevelopmentTax;
        
        /**
         * 총 세금
         */
        private BigDecimal totalTax;
        
        /**
         * 세율
         */
        private BigDecimal taxRate;
    }
    
    /**
     * 거래 리스크 메트릭
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRiskMetrics {
        
        /**
         * 거래 VaR
         */
        private BigDecimal tradeVaR;
        
        /**
         * 예상 손실
         */
        private BigDecimal expectedLoss;
        
        /**
         * 최대 손실
         */
        private BigDecimal maximumLoss;
        
        /**
         * 리스크 조정 수익률
         */
        private BigDecimal riskAdjustedReturn;
        
        /**
         * 거래 리스크 등급
         */
        private TradeRiskLevel riskLevel;
    }
    
    /**
     * 거래 리스크 등급
     */
    public enum TradeRiskLevel {
        VERY_LOW("매우낮음"), LOW("낮음"), MODERATE("보통"), HIGH("높음"), VERY_HIGH("매우높음");
        
        private final String koreanName;
        
        TradeRiskLevel(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 거래 유효성 검증
     */
    public boolean isValid() {
        return tradeId != null && 
               orderId != null &&
               symbol != null && 
               side != null && 
               quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0 &&
               price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 순 거래 금액 계산 (수수료 및 세금 제외)
     */
    public BigDecimal getNetAmount() {
        BigDecimal totalCosts = BigDecimal.ZERO;
        
        if (commission != null && commission.getTotalCommission() != null) {
            totalCosts = totalCosts.add(commission.getTotalCommission());
        }
        
        if (taxInfo != null && taxInfo.getTotalTax() != null) {
            totalCosts = totalCosts.add(taxInfo.getTotalTax());
        }
        
        return amount.subtract(totalCosts);
    }
    
    /**
     * 포지션에 미치는 영향 계산
     */
    public BigDecimal getPositionImpact() {
        return quantity.multiply(BigDecimal.valueOf(side.getPositionMultiplier()));
    }
    
    /**
     * 거래 수익률 계산 (벤치마크 가격 대비)
     */
    public BigDecimal calculateReturn(BigDecimal benchmarkPrice) {
        if (benchmarkPrice == null || benchmarkPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal priceDifference = price.subtract(benchmarkPrice);
        return priceDifference.divide(benchmarkPrice, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)); // 백분율
    }
    
    /**
     * 거래 효율성 평가
     */
    public boolean isEfficientExecution() {
        if (executionMetrics == null || executionMetrics.getExecutionEfficiency() == null) {
            return false;
        }
        return executionMetrics.getExecutionEfficiency().compareTo(BigDecimal.valueOf(70)) >= 0;
    }
    
    /**
     * 실행 품질 등급 계산
     */
    public ExecutionGrade calculateExecutionGrade() {
        if (executionMetrics == null || executionMetrics.getExecutionEfficiency() == null) {
            return ExecutionGrade.FAIR;
        }
        return ExecutionGrade.fromScore(executionMetrics.getExecutionEfficiency());
    }
    
    /**
     * 총 거래 비용 계산
     */
    public BigDecimal getTotalTradingCost() {
        BigDecimal totalCost = BigDecimal.ZERO;
        
        // 수수료 추가
        if (commission != null && commission.getTotalCommission() != null) {
            totalCost = totalCost.add(commission.getTotalCommission());
        }
        
        // 세금 추가
        if (taxInfo != null && taxInfo.getTotalTax() != null) {
            totalCost = totalCost.add(taxInfo.getTotalTax());
        }
        
        // 시장 영향 비용 추가
        if (executionMetrics != null && executionMetrics.getMarketImpact() != null) {
            totalCost = totalCost.add(executionMetrics.getMarketImpact());
        }
        
        // 타이밍 비용 추가
        if (executionMetrics != null && executionMetrics.getTimingCost() != null) {
            totalCost = totalCost.add(executionMetrics.getTimingCost());
        }
        
        return totalCost;
    }
    
    /**
     * 거래 성과 요약
     */
    public String getPerformanceSummary() {
        ExecutionGrade grade = calculateExecutionGrade();
        BigDecimal totalCost = getTotalTradingCost();
        
        return String.format("실행등급: %s, 총비용: %s, 효율성: %s", 
            grade.getKoreanName(),
            totalCost,
            executionMetrics != null ? executionMetrics.getExecutionEfficiency() : "N/A");
    }
}