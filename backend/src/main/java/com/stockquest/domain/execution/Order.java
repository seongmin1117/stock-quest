package com.stockquest.domain.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 주문 도메인 모델
 * Phase 8.4: Real-time Execution Engine - Order Management System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    /**
     * 주문 고유 ID
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
     * 주문 타입
     */
    private OrderType orderType;
    
    /**
     * 주문 사이드 (매수/매도)
     */
    private OrderSide side;
    
    /**
     * 주문 수량
     */
    private BigDecimal quantity;
    
    /**
     * 실행된 수량
     */
    private BigDecimal executedQuantity;
    
    /**
     * 남은 수량
     */
    private BigDecimal remainingQuantity;
    
    /**
     * 주문 가격 (Limit 주문용)
     */
    private BigDecimal price;
    
    /**
     * 스톱 가격 (Stop 주문용)
     */
    private BigDecimal stopPrice;
    
    /**
     * 평균 실행 가격
     */
    private BigDecimal avgExecutionPrice;
    
    /**
     * 주문 상태
     */
    private OrderStatus status;
    
    /**
     * 시간 조건
     */
    private TimeInForce timeInForce;
    
    /**
     * 주문 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 주문 만료 시간
     */
    private LocalDateTime expirationTime;
    
    /**
     * 실행 알고리즘 타입
     */
    private ExecutionAlgorithm executionAlgorithm;
    
    /**
     * 알고리즘 파라미터
     */
    private ExecutionParameters executionParameters;
    
    /**
     * 주문 우선순위
     */
    private Integer priority;
    
    /**
     * 실행 목표 (TWAP, VWAP 등)
     */
    private ExecutionObjective executionObjective;
    
    /**
     * 관련 거래 목록
     */
    private List<Trade> trades;
    
    /**
     * 주문 이벤트 히스토리
     */
    private List<OrderEvent> orderEvents;
    
    /**
     * 리스크 검증 결과
     */
    private RiskValidationResult riskValidation;
    
    /**
     * 실행 통계
     */
    private ExecutionStatistics executionStats;
    
    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata;
    
    /**
     * 주문 타입 열거형
     */
    public enum OrderType {
        MARKET("시장가", "Market order - immediate execution at best available price"),
        LIMIT("지정가", "Limit order - execution only at specified price or better"),
        STOP("스톱", "Stop order - becomes market order when stop price is reached"),
        STOP_LIMIT("스톱지정가", "Stop-limit order - becomes limit order when stop price is reached"),
        MARKET_ON_CLOSE("종가", "Market on close order"),
        LIMIT_ON_CLOSE("종가지정가", "Limit on close order"),
        TWAP("시간가중평균", "Time-weighted average price algorithm"),
        VWAP("거래량가중평균", "Volume-weighted average price algorithm"),
        IMPLEMENTATION_SHORTFALL("구현편차", "Implementation shortfall algorithm"),
        PARTICIPATION_RATE("참여율", "Participation rate algorithm"),
        TARGET_CLOSE("목표종가", "Target close algorithm");
        
        private final String koreanName;
        private final String description;
        
        OrderType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 주문 사이드 열거형
     */
    public enum OrderSide {
        BUY("매수", "Buy order"),
        SELL("매도", "Sell order"),
        SELL_SHORT("공매도", "Short sell order"),
        BUY_TO_COVER("단기매수", "Buy to cover short position");
        
        private final String koreanName;
        private final String description;
        
        OrderSide(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 주문 상태 열거형
     */
    public enum OrderStatus {
        PENDING_NEW("신규대기", "Order submitted but not yet accepted"),
        NEW("신규", "Order accepted and active"),
        PARTIALLY_FILLED("부분체결", "Order partially executed"),
        FILLED("완전체결", "Order fully executed"),
        PENDING_CANCEL("취소대기", "Cancel request submitted"),
        CANCELED("취소됨", "Order canceled"),
        REJECTED("거부됨", "Order rejected"),
        EXPIRED("만료됨", "Order expired"),
        SUSPENDED("정지됨", "Order suspended"),
        REPLACED("수정됨", "Order replaced with new parameters");
        
        private final String koreanName;
        private final String description;
        
        OrderStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
        
        public boolean isActiveStatus() {
            return this == NEW || this == PARTIALLY_FILLED;
        }
        
        public boolean isTerminalStatus() {
            return this == FILLED || this == CANCELED || this == REJECTED || this == EXPIRED;
        }
    }
    
    /**
     * 시간 조건 열거형
     */
    public enum TimeInForce {
        DAY("당일", "Order valid for current trading day"),
        GTC("성사시까지", "Good Till Canceled - valid until manually canceled"),
        IOC("즉시성사잔량취소", "Immediate or Cancel - execute immediately, cancel remainder"),
        FOK("전량즉시성사취소", "Fill or Kill - execute completely or cancel entirely"),
        GTD("지정일까지", "Good Till Date - valid until specified date"),
        AT_THE_OPEN("시초가", "Execute at market opening"),
        AT_THE_CLOSE("종가", "Execute at market closing");
        
        private final String koreanName;
        private final String description;
        
        TimeInForce(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 실행 알고리즘 열거형
     */
    public enum ExecutionAlgorithm {
        NONE("없음", "No algorithm - simple order"),
        TWAP("TWAP", "Time-Weighted Average Price"),
        VWAP("VWAP", "Volume-Weighted Average Price"),
        IMPLEMENTATION_SHORTFALL("IS", "Implementation Shortfall"),
        PARTICIPATION_RATE("POV", "Percentage of Volume"),
        TARGET_CLOSE("TC", "Target Close"),
        ARRIVAL_PRICE("AP", "Arrival Price"),
        SMART_ORDER_ROUTING("SOR", "Smart Order Routing"),
        ICEBERG("빙산", "Iceberg order - large order split into smaller pieces"),
        STEALTH("스텔스", "Stealth algorithm to minimize market impact");
        
        private final String shortName;
        private final String description;
        
        ExecutionAlgorithm(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }
        
        public String getShortName() { return shortName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 실행 목표 열거형
     */
    public enum ExecutionObjective {
        MINIMIZE_COST("비용최소화", "Minimize total execution cost"),
        MINIMIZE_RISK("리스크최소화", "Minimize execution risk"),
        MINIMIZE_IMPACT("시장영향최소화", "Minimize market impact"),
        MAXIMIZE_LIQUIDITY("유동성최대화", "Maximize liquidity capture"),
        SPEED("속도우선", "Prioritize execution speed"),
        PARTICIPATE("참여율유지", "Maintain participation rate"),
        CLOSE_TRACKING("종가추종", "Track closing price"),
        BALANCE("균형", "Balance cost, risk, and speed");
        
        private final String koreanName;
        private final String description;
        
        ExecutionObjective(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 실행 파라미터
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionParameters {
        
        /**
         * 참여율 (TWAP, POV 알고리즘용)
         */
        private BigDecimal participationRate;
        
        /**
         * 실행 기간 (분)
         */
        private Integer executionPeriod;
        
        /**
         * 최대 시장영향비율
         */
        private BigDecimal maxMarketImpact;
        
        /**
         * 긴급성 수준 (0.0-1.0)
         */
        private BigDecimal urgencyLevel;
        
        /**
         * 가격 편차 허용범위
         */
        private BigDecimal priceTolerance;
        
        /**
         * 최소 주문 크기
         */
        private BigDecimal minOrderSize;
        
        /**
         * 최대 주문 크기
         */
        private BigDecimal maxOrderSize;
        
        /**
         * 실행 간격 (초)
         */
        private Integer executionInterval;
        
        /**
         * 리스크 한도
         */
        private BigDecimal riskLimit;
        
        /**
         * 추가 알고리즘 설정
         */
        private Map<String, Object> algorithmSettings;
    }
    
    /**
     * 주문 이벤트
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderEvent {
        private String eventId;
        private OrderEventType eventType;
        private LocalDateTime timestamp;
        private String description;
        private Map<String, Object> eventData;
        private String userId;
    }
    
    /**
     * 주문 이벤트 타입
     */
    public enum OrderEventType {
        ORDER_SUBMITTED("주문제출"),
        ORDER_ACCEPTED("주문접수"),
        ORDER_REJECTED("주문거부"),
        PARTIAL_FILL("부분체결"),
        FULL_FILL("완전체결"),
        ORDER_CANCELED("주문취소"),
        ORDER_REPLACED("주문수정"),
        ORDER_EXPIRED("주문만료"),
        RISK_CHECK_PASSED("리스크검증통과"),
        RISK_CHECK_FAILED("리스크검증실패");
        
        private final String koreanName;
        
        OrderEventType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
    
    /**
     * 리스크 검증 결과
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskValidationResult {
        private boolean passed;
        private LocalDateTime validationTime;
        private List<RiskCheckResult> riskChecks;
        private String overallRiskLevel;
        private List<String> warnings;
        private List<String> violations;
    }
    
    /**
     * 개별 리스크 검증 결과
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskCheckResult {
        private String checkType;
        private boolean passed;
        private String description;
        private BigDecimal currentValue;
        private BigDecimal limitValue;
        private String severity;
    }
    
    /**
     * 실행 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStatistics {
        
        /**
         * 실행 시작 시간
         */
        private LocalDateTime executionStartTime;
        
        /**
         * 실행 종료 시간
         */
        private LocalDateTime executionEndTime;
        
        /**
         * 총 실행 시간 (밀리초)
         */
        private Long totalExecutionTime;
        
        /**
         * 평균 체결가격
         */
        private BigDecimal averageExecutionPrice;
        
        /**
         * 시장가격 대비 슬리피지
         */
        private BigDecimal slippage;
        
        /**
         * 시장 영향 비용
         */
        private BigDecimal marketImpactCost;
        
        /**
         * 타이밍 비용
         */
        private BigDecimal timingCost;
        
        /**
         * 총 거래 비용
         */
        private BigDecimal totalTradingCost;
        
        /**
         * 실행 품질 점수 (0-100)
         */
        private BigDecimal executionQualityScore;
        
        /**
         * 벤치마크 대비 성과
         */
        private BigDecimal benchmarkPerformance;
        
        /**
         * 체결률
         */
        private BigDecimal fillRate;
        
        /**
         * 거래 횟수
         */
        private Integer numberOfTrades;
        
        /**
         * 평균 거래 크기
         */
        private BigDecimal averageTradeSize;
    }
    
    /**
     * 주문 유효성 검증
     */
    public boolean isValid() {
        return orderId != null && 
               symbol != null && 
               orderType != null && 
               side != null && 
               quantity != null && 
               quantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 활성 주문 여부 확인
     */
    public boolean isActive() {
        return status != null && status.isActiveStatus();
    }
    
    /**
     * 완료된 주문 여부 확인
     */
    public boolean isTerminal() {
        return status != null && status.isTerminalStatus();
    }
    
    /**
     * 체결률 계산
     */
    public BigDecimal getFillRate() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (executedQuantity == null) {
            return BigDecimal.ZERO;
        }
        return executedQuantity.divide(quantity, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 남은 수량 계산 및 업데이트
     */
    public void updateRemainingQuantity() {
        if (quantity != null && executedQuantity != null) {
            this.remainingQuantity = quantity.subtract(executedQuantity);
        }
    }
    
    /**
     * 주문 상태 업데이트 및 이벤트 추가
     */
    public void updateStatus(OrderStatus newStatus, String description) {
        this.status = newStatus;
        this.lastUpdatedAt = LocalDateTime.now();
        
        if (orderEvents != null) {
            OrderEvent event = OrderEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(mapStatusToEventType(newStatus))
                .timestamp(LocalDateTime.now())
                .description(description)
                .build();
            orderEvents.add(event);
        }
    }
    
    /**
     * 상태를 이벤트 타입으로 매핑
     */
    private OrderEventType mapStatusToEventType(OrderStatus status) {
        return switch (status) {
            case NEW -> OrderEventType.ORDER_ACCEPTED;
            case PARTIALLY_FILLED -> OrderEventType.PARTIAL_FILL;
            case FILLED -> OrderEventType.FULL_FILL;
            case CANCELED -> OrderEventType.ORDER_CANCELED;
            case REJECTED -> OrderEventType.ORDER_REJECTED;
            case EXPIRED -> OrderEventType.ORDER_EXPIRED;
            default -> OrderEventType.ORDER_ACCEPTED;
        };
    }
    
    /**
     * 실행 진행률 계산
     */
    public BigDecimal getExecutionProgress() {
        return getFillRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 예상 완료 시간 계산
     */
    public LocalDateTime getEstimatedCompletionTime() {
        if (executionParameters == null || executionParameters.getExecutionPeriod() == null) {
            return null;
        }
        
        if (createdAt != null) {
            return createdAt.plusMinutes(executionParameters.getExecutionPeriod());
        }
        
        return null;
    }
}