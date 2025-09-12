package com.stockquest.domain.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 포지션 도메인 모델
 * Phase 8.4: Real-time Execution Engine - Real-time Position Management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    
    /**
     * 포지션 고유 ID
     */
    private String positionId;
    
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
     * 자산명
     */
    private String assetName;
    
    /**
     * 자산 유형
     */
    private AssetType assetType;
    
    /**
     * 현재 수량
     */
    private BigDecimal quantity;
    
    /**
     * 사용 가능 수량 (주문으로 묶이지 않은 수량)
     */
    private BigDecimal availableQuantity;
    
    /**
     * 주문 중인 수량
     */
    private BigDecimal pendingQuantity;
    
    /**
     * 평균 매입가
     */
    private BigDecimal averageCost;
    
    /**
     * 현재 시장가
     */
    private BigDecimal currentPrice;
    
    /**
     * 포지션 가치 (수량 × 현재가격)
     */
    private BigDecimal marketValue;
    
    /**
     * 실현 손익
     */
    private BigDecimal realizedPnL;
    
    /**
     * 미실현 손익
     */
    private BigDecimal unrealizedPnL;
    
    /**
     * 총 손익
     */
    private BigDecimal totalPnL;
    
    /**
     * 수익률 (%)
     */
    private BigDecimal returnPercentage;
    
    /**
     * 첫 매입일
     */
    private LocalDate firstPurchaseDate;
    
    /**
     * 마지막 거래일
     */
    private LocalDate lastTradeDate;
    
    /**
     * 보유 일수
     */
    private Integer holdingDays;
    
    /**
     * 포지션 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 포지션 상태
     */
    private PositionStatus status;
    
    /**
     * 포지션 타입
     */
    private PositionType positionType;
    
    /**
     * 리스크 메트릭
     */
    private PositionRiskMetrics riskMetrics;
    
    /**
     * 성과 메트릭
     */
    private PerformanceMetrics performanceMetrics;
    
    /**
     * 포지션 이벤트 히스토리
     */
    private List<PositionEvent> positionHistory;
    
    /**
     * 배당금 정보
     */
    private List<DividendInfo> dividends;
    
    /**
     * 포지션 태그 (분류용)
     */
    private List<String> tags;
    
    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata;
    
    /**
     * 자산 타입 열거형
     */
    public enum AssetType {
        STOCK("주식", "Common stock"),
        BOND("채권", "Bond instrument"),
        ETF("ETF", "Exchange Traded Fund"),
        MUTUAL_FUND("뮤추얼펀드", "Mutual Fund"),
        DERIVATIVE("파생상품", "Derivative instrument"),
        CRYPTO("암호화폐", "Cryptocurrency"),
        COMMODITY("원자재", "Commodity"),
        FOREX("외환", "Foreign exchange"),
        REIT("리츠", "Real Estate Investment Trust"),
        CASH("현금", "Cash position");
        
        private final String koreanName;
        private final String description;
        
        AssetType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 포지션 상태 열거형
     */
    public enum PositionStatus {
        OPEN("오픈", "Open position with holdings"),
        ACTIVE("활성", "Active position with holdings"),
        CLOSED("종료", "Position completely closed"),
        SUSPENDED("정지", "Position suspended from trading"),
        LIQUIDATING("청산중", "Position being liquidated"),
        PENDING("보류", "Position pending settlement"),
        ERROR("오류", "Position with errors");
        
        private final String koreanName;
        private final String description;
        
        PositionStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 포지션 타입 열거형
     */
    public enum PositionType {
        LONG("롱포지션", "Long position - bullish"),
        SHORT("숏포지션", "Short position - bearish"),
        FLAT("플랫", "No position");
        
        private final String koreanName;
        private final String description;
        
        PositionType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 포지션 리스크 메트릭
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionRiskMetrics {
        
        /**
         * 포지션 VaR
         */
        private BigDecimal positionVaR;
        
        /**
         * Value at Risk
         */
        private BigDecimal valueAtRisk;
        
        /**
         * Expected Shortfall
         */
        private BigDecimal expectedShortfall;
        
        /**
         * Market Correlation
         */
        private BigDecimal marketCorrelation;
        
        /**
         * 포지션 베타
         */
        private BigDecimal beta;
        
        /**
         * 포지션 변동성
         */
        private BigDecimal volatility;
        
        /**
         * 최대낙폭
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 샤프 비율
         */
        private BigDecimal sharpeRatio;
        
        /**
         * 소티노 비율
         */
        private BigDecimal sortinoRatio;
        
        /**
         * 트래킹 에러
         */
        private BigDecimal trackingError;
        
        /**
         * 정보 비율
         */
        private BigDecimal informationRatio;
        
        /**
         * 집중도 리스크 (포트폴리오 내 비중)
         */
        private BigDecimal concentrationRisk;
        
        /**
         * 유동성 리스크
         */
        private LiquidityRisk liquidityRisk;
        
        /**
         * 신용 리스크 등급
         */
        private CreditRating creditRating;
        
        /**
         * 리스크 등급
         */
        private RiskLevel riskLevel;
    }
    
    /**
     * 유동성 리스크 열거형
     */
    public enum LiquidityRisk {
        VERY_HIGH("매우높음", "Daily volume < 10x position size"),
        HIGH("높음", "Daily volume < 20x position size"),
        MODERATE("보통", "Daily volume < 50x position size"),
        LOW("낮음", "Daily volume > 50x position size"),
        VERY_LOW("매우낮음", "Daily volume > 100x position size");
        
        private final String koreanName;
        private final String description;
        
        LiquidityRisk(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 신용 등급 열거형
     */
    public enum CreditRating {
        AAA("AAA", "Highest credit quality"),
        AA("AA", "Very high credit quality"),
        A("A", "High credit quality"),
        BBB("BBB", "Good credit quality"),
        BB("BB", "Speculative"),
        B("B", "Highly speculative"),
        CCC("CCC", "Extremely speculative"),
        CC("CC", "Default imminent"),
        C("C", "Default"),
        D("D", "In default"),
        NR("NR", "Not rated");
        
        private final String rating;
        private final String description;
        
        CreditRating(String rating, String description) {
            this.rating = rating;
            this.description = description;
        }
        
        public String getRating() { return rating; }
        public String getDescription() { return description; }
    }
    
    /**
     * 리스크 등급 열거형
     */
    public enum RiskLevel {
        VERY_LOW(1, "매우낮음"),
        LOW(2, "낮음"),
        MODERATE(3, "보통"),
        HIGH(4, "높음"),
        VERY_HIGH(5, "매우높음");
        
        private final int level;
        private final String koreanName;
        
        RiskLevel(int level, String koreanName) {
            this.level = level;
            this.koreanName = koreanName;
        }
        
        public int getLevel() { return level; }
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 성과 메트릭
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        
        /**
         * 누적 수익률
         */
        private BigDecimal cumulativeReturn;
        
        /**
         * 연환산 수익률
         */
        private BigDecimal annualizedReturn;
        
        /**
         * 일평균 수익률
         */
        private BigDecimal dailyAverageReturn;
        
        /**
         * 승률 (수익 거래 비율)
         */
        private BigDecimal winRate;
        
        /**
         * 평균 승부 비율
         */
        private BigDecimal averageWinLossRatio;
        
        /**
         * 최대 수익률
         */
        private BigDecimal maxReturn;
        
        /**
         * 최대 손실률
         */
        private BigDecimal maxLoss;
        
        /**
         * 벤치마크 대비 성과
         */
        private BigDecimal benchmarkOutperformance;
        
        /**
         * 칼마 비율
         */
        private BigDecimal calmarRatio;
        
        /**
         * 샤프 비율
         */
        private BigDecimal sharpeRatio;
        
        /**
         * 정보 비율
         */
        private BigDecimal informationRatio;
        
        /**
         * 최대낙폭
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 성과 등급
         */
        private PerformanceGrade performanceGrade;
    }
    
    /**
     * 성과 등급 열거형
     */
    public enum PerformanceGrade {
        EXCELLENT("우수", 20),
        GOOD("양호", 10),
        FAIR("보통", 0),
        POOR("미흡", -10),
        VERY_POOR("매우미흡", -20);
        
        private final String koreanName;
        private final int minReturnPercent;
        
        PerformanceGrade(String koreanName, int minReturnPercent) {
            this.koreanName = koreanName;
            this.minReturnPercent = minReturnPercent;
        }
        
        public String getKoreanName() { return koreanName; }
        public int getMinReturnPercent() { return minReturnPercent; }
        
        public static PerformanceGrade fromReturn(BigDecimal returnPercentage) {
            double returnValue = returnPercentage.doubleValue();
            for (PerformanceGrade grade : values()) {
                if (returnValue >= grade.minReturnPercent) {
                    return grade;
                }
            }
            return VERY_POOR;
        }
    }
    
    /**
     * 포지션 이벤트
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionEvent {
        private String eventId;
        private PositionEventType eventType;
        private LocalDateTime timestamp;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal amount;
        private String description;
        private String tradeId;
        private Map<String, Object> eventData;
        private Map<String, Object> positionSnapshot;
    }
    
    /**
     * 포지션 이벤트 타입
     */
    public enum PositionEventType {
        POSITION_OPENED("포지션개설"),
        POSITION_INCREASED("포지션증가"),
        POSITION_DECREASED("포지션감소"),
        POSITION_CLOSED("포지션종료"),
        TRADE_EXECUTED("거래체결"),
        DIVIDEND_RECEIVED("배당금수령"),
        STOCK_SPLIT("주식분할"),
        STOCK_DIVIDEND("주식배당"),
        SPIN_OFF("스핀오프"),
        MERGER("합병"),
        RIGHTS_ISSUE("유상증자"),
        POSITION_REBALANCED("포지션리밸런싱");
        
        private final String koreanName;
        
        PositionEventType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 배당금 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendInfo {
        private String dividendId;
        private LocalDate exDividendDate;
        private LocalDate paymentDate;
        private BigDecimal dividendPerShare;
        private BigDecimal totalDividend;
        private DividendType dividendType;
        private String description;
    }
    
    /**
     * 배당금 타입
     */
    public enum DividendType {
        CASH("현금배당"),
        STOCK("주식배당"),
        SPECIAL("특별배당"),
        INTERIM("중간배당");
        
        private final String koreanName;
        
        DividendType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 포지션 유효성 검증
     */
    public boolean isValid() {
        return positionId != null && 
               portfolioId != null && 
               symbol != null && 
               quantity != null;
    }
    
    /**
     * 활성 포지션 여부 확인
     */
    public boolean isActive() {
        return status == PositionStatus.ACTIVE && 
               quantity != null && 
               quantity.compareTo(BigDecimal.ZERO) != 0;
    }
    
    /**
     * 롱/숏 포지션 타입 결정
     */
    public PositionType determinePositionType() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return PositionType.FLAT;
        }
        return quantity.compareTo(BigDecimal.ZERO) > 0 ? PositionType.LONG : PositionType.SHORT;
    }
    
    /**
     * 미실현 손익 계산
     */
    public void calculateUnrealizedPnL() {
        if (quantity != null && currentPrice != null && averageCost != null) {
            BigDecimal currentValue = quantity.multiply(currentPrice);
            BigDecimal costBasis = quantity.multiply(averageCost);
            this.unrealizedPnL = currentValue.subtract(costBasis);
        }
    }
    
    /**
     * 총 손익 계산
     */
    public void calculateTotalPnL() {
        this.totalPnL = BigDecimal.ZERO;
        if (realizedPnL != null) {
            this.totalPnL = this.totalPnL.add(realizedPnL);
        }
        if (unrealizedPnL != null) {
            this.totalPnL = this.totalPnL.add(unrealizedPnL);
        }
    }
    
    /**
     * 수익률 계산
     */
    public void calculateReturnPercentage() {
        if (averageCost != null && quantity != null && averageCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costBasis = quantity.multiply(averageCost);
            if (totalPnL != null && costBasis.compareTo(BigDecimal.ZERO) > 0) {
                this.returnPercentage = totalPnL.divide(costBasis, 6, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
        }
    }
    
    /**
     * 시장가치 업데이트
     */
    public void updateMarketValue() {
        if (quantity != null && currentPrice != null) {
            this.marketValue = quantity.multiply(currentPrice);
        }
    }
    
    /**
     * 포지션 업데이트 (새로운 거래 반영)
     */
    public void updateWithTrade(Trade trade) {
        if (!trade.getSymbol().equals(this.symbol)) {
            throw new IllegalArgumentException("Trade symbol doesn't match position symbol");
        }
        
        BigDecimal tradeQuantity = trade.getQuantity();
        BigDecimal tradePrice = trade.getPrice();
        
        if (trade.getSide() == Trade.TradeSide.SELL || trade.getSide() == Trade.TradeSide.SELL_SHORT) {
            tradeQuantity = tradeQuantity.negate();
        }
        
        // 평균 매입가 업데이트
        if (this.quantity != null && this.averageCost != null) {
            BigDecimal currentValue = this.quantity.multiply(this.averageCost);
            BigDecimal tradeValue = trade.getQuantity().multiply(tradePrice);
            
            if (tradeQuantity.compareTo(BigDecimal.ZERO) > 0) { // 매수
                BigDecimal newQuantity = this.quantity.add(tradeQuantity);
                if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    this.averageCost = currentValue.add(tradeValue).divide(newQuantity, 6, java.math.RoundingMode.HALF_UP);
                }
            } else { // 매도
                // 실현 손익 계산
                BigDecimal soldValue = trade.getQuantity().multiply(tradePrice);
                BigDecimal costBasis = trade.getQuantity().multiply(this.averageCost);
                BigDecimal realizedGain = soldValue.subtract(costBasis);
                
                if (this.realizedPnL == null) {
                    this.realizedPnL = realizedGain;
                } else {
                    this.realizedPnL = this.realizedPnL.add(realizedGain);
                }
            }
            
            // 수량 업데이트
            this.quantity = this.quantity.add(tradeQuantity);
            
        } else { // 첫 거래
            this.quantity = tradeQuantity;
            this.averageCost = tradePrice;
        }
        
        // 포지션 타입 재결정
        this.positionType = determinePositionType();
        
        // 마지막 거래일 업데이트
        this.lastTradeDate = trade.getTradeTime().toLocalDate();
        this.lastUpdatedAt = LocalDateTime.now();
        
        // 포지션 이벤트 추가
        if (positionHistory != null) {
            PositionEvent event = PositionEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(determineEventType(tradeQuantity))
                .timestamp(LocalDateTime.now())
                .quantity(trade.getQuantity())
                .price(tradePrice)
                .amount(trade.getAmount())
                .tradeId(trade.getTradeId())
                .description(String.format("%s %s shares at %s", 
                    trade.getSide().getKoreanName(), trade.getQuantity(), tradePrice))
                .build();
            positionHistory.add(event);
        }
        
        // 메트릭 재계산
        updateMarketValue();
        calculateUnrealizedPnL();
        calculateTotalPnL();
        calculateReturnPercentage();
    }
    
    /**
     * 거래량에 따른 이벤트 타입 결정
     */
    private PositionEventType determineEventType(BigDecimal tradeQuantity) {
        if (this.quantity == null || this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            return PositionEventType.POSITION_OPENED;
        } else if (tradeQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return PositionEventType.POSITION_INCREASED;
        } else if (this.quantity.add(tradeQuantity).compareTo(BigDecimal.ZERO) == 0) {
            return PositionEventType.POSITION_CLOSED;
        } else {
            return PositionEventType.POSITION_DECREASED;
        }
    }
    
    /**
     * 성과 등급 계산
     */
    public PerformanceGrade calculatePerformanceGrade() {
        if (returnPercentage == null) {
            return PerformanceGrade.FAIR;
        }
        return PerformanceGrade.fromReturn(returnPercentage);
    }
    
    /**
     * 포지션 요약 정보
     */
    public String getPositionSummary() {
        return String.format("%s: %s주, 수익률: %s%%, 평가액: %s", 
            symbol, 
            quantity != null ? quantity : "0",
            returnPercentage != null ? returnPercentage : "N/A",
            marketValue != null ? marketValue : "N/A");
    }
    
    /**
     * 보유기간 업데이트
     */
    public void updateHoldingDays() {
        if (firstPurchaseDate != null) {
            this.holdingDays = (int) java.time.temporal.ChronoUnit.DAYS.between(firstPurchaseDate, LocalDate.now());
        }
    }
    
    // Additional methods for service compatibility
    
    public BigDecimal getCurrentValue() {
        if (quantity != null && currentPrice != null) {
            return quantity.multiply(currentPrice).abs();
        }
        return BigDecimal.ZERO;
    }
    
    public LocalDate getOpenDate() {
        return firstPurchaseDate;
    }
    
    public BigDecimal getTotalReturn() {
        if (averageCost != null && currentPrice != null && averageCost.compareTo(BigDecimal.ZERO) > 0) {
            return currentPrice.subtract(averageCost).divide(averageCost, 6, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getDailyReturn() {
        // Simplified daily return calculation
        return getTotalReturn().divide(BigDecimal.valueOf(Math.max(holdingDays, 1)), 6, java.math.RoundingMode.HALF_UP);
    }
    
    public Integer getHoldingPeriodDays() {
        return holdingDays;
    }
    
    public List<PositionEvent> getPositionEvents() {
        return positionHistory;
    }
    
    public void setPositionEvents(List<PositionEvent> events) {
        this.positionHistory = events;
    }
}