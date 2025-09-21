package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 리밸런싱 결과 도메인 밸류 오브젝트
 * Phase 2.3: 비즈니스 로직 고도화 - 리밸런싱 실행 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingResult {

    private Long portfolioId;
    private Long strategyId;
    private List<RebalancingAction> actions;
    private BigDecimal totalTransactionCost;
    private BigDecimal totalTaxImpact;
    private BigDecimal expectedImprovementScore;
    private Map<String, BigDecimal> weightChanges;
    private LocalDateTime proposedAt;
    private RebalancingResultStatus status;

    /**
     * 총 거래 수수료 계산
     */
    public BigDecimal getTotalCost() {
        return totalTransactionCost.add(totalTaxImpact);
    }

    /**
     * 거래 액션 수 반환
     */
    public int getActionCount() {
        return actions != null ? actions.size() : 0;
    }

    /**
     * 높은 우선순위 액션만 필터링
     */
    public List<RebalancingAction> getHighPriorityActions() {
        if (actions == null) {
            return List.of();
        }

        return actions.stream()
            .filter(action -> action.getPriority().getLevel() <= 2) // CRITICAL, HIGH
            .toList();
    }

    /**
     * 리밸런싱 결과 상태 열거형
     */
    public enum RebalancingResultStatus {
        PROPOSED("제안", "Rebalancing proposal generated"),
        APPROVED("승인", "Rebalancing approved for execution"),
        REJECTED("거부", "Rebalancing proposal rejected"),
        EXECUTED("실행완료", "Rebalancing executed"),
        PARTIALLY_EXECUTED("부분실행", "Partially executed");

        private final String koreanName;
        private final String description;

        RebalancingResultStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
}