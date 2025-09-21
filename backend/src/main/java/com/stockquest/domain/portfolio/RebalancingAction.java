package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 리밸런싱 액션 도메인 엔티티
 * Phase 2.3: 비즈니스 로직 고도화 - 구체적인 리밸런싱 거래 액션
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingAction {

    private Long id;
    private Long portfolioId;
    private Long strategyId;
    private String symbol;
    private ActionType actionType;
    private BigDecimal quantity;
    private BigDecimal targetPrice;
    private BigDecimal currentWeight;
    private BigDecimal targetWeight;
    private BigDecimal weightDeviation;
    private BigDecimal estimatedTransactionCost;
    private BigDecimal estimatedTaxImpact;
    private ActionPriority priority;
    private ActionStatus status;
    private String reason;
    private LocalDateTime scheduledAt;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;

    /**
     * 액션의 거래 금액 계산
     */
    public BigDecimal getTradeAmount(BigDecimal currentPrice) {
        if (quantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice);
    }

    /**
     * 예상 손익 영향 계산
     */
    public BigDecimal getEstimatedImpact(BigDecimal portfolioValue) {
        if (portfolioValue == null || portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCost = estimatedTransactionCost.add(estimatedTaxImpact);
        return totalCost.divide(portfolioValue, 6, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 액션 실행 완료 처리
     */
    public RebalancingAction markExecuted(LocalDateTime executionTime) {
        return RebalancingAction.builder()
            .id(this.id)
            .portfolioId(this.portfolioId)
            .strategyId(this.strategyId)
            .symbol(this.symbol)
            .actionType(this.actionType)
            .quantity(this.quantity)
            .targetPrice(this.targetPrice)
            .currentWeight(this.currentWeight)
            .targetWeight(this.targetWeight)
            .weightDeviation(this.weightDeviation)
            .estimatedTransactionCost(this.estimatedTransactionCost)
            .estimatedTaxImpact(this.estimatedTaxImpact)
            .priority(this.priority)
            .status(ActionStatus.EXECUTED)
            .reason(this.reason)
            .scheduledAt(this.scheduledAt)
            .executedAt(executionTime)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 액션 실행 실패 처리
     */
    public RebalancingAction markFailed(String failureReason) {
        return RebalancingAction.builder()
            .id(this.id)
            .portfolioId(this.portfolioId)
            .strategyId(this.strategyId)
            .symbol(this.symbol)
            .actionType(this.actionType)
            .quantity(this.quantity)
            .targetPrice(this.targetPrice)
            .currentWeight(this.currentWeight)
            .targetWeight(this.targetWeight)
            .weightDeviation(this.weightDeviation)
            .estimatedTransactionCost(this.estimatedTransactionCost)
            .estimatedTaxImpact(this.estimatedTaxImpact)
            .priority(this.priority)
            .status(ActionStatus.FAILED)
            .reason(this.reason + " | 실패 원인: " + failureReason)
            .scheduledAt(this.scheduledAt)
            .executedAt(null)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 액션 타입 열거형
     */
    public enum ActionType {
        BUY("매수", "Buy securities to increase position"),
        SELL("매도", "Sell securities to decrease position"),
        HOLD("보유", "Maintain current position");

        private final String koreanName;
        private final String description;

        ActionType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }

    /**
     * 액션 우선순위 열거형
     */
    public enum ActionPriority {
        CRITICAL("긴급", 1, "Immediate execution required"),
        HIGH("높음", 2, "High priority execution"),
        MEDIUM("보통", 3, "Medium priority execution"),
        LOW("낮음", 4, "Low priority execution"),
        OPTIONAL("선택", 5, "Optional execution");

        private final String koreanName;
        private final int level;
        private final String description;

        ActionPriority(String koreanName, int level, String description) {
            this.koreanName = koreanName;
            this.level = level;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }

    /**
     * 액션 상태 열거형
     */
    public enum ActionStatus {
        PENDING("대기", "Waiting for execution"),
        SCHEDULED("예약", "Scheduled for execution"),
        EXECUTING("실행중", "Currently being executed"),
        EXECUTED("완료", "Successfully executed"),
        FAILED("실패", "Execution failed"),
        CANCELLED("취소", "Cancelled by user or system");

        private final String koreanName;
        private final String description;

        ActionStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
}