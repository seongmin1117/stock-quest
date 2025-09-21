package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 포트폴리오 리밸런싱 전략 도메인 엔티티
 * Phase 2.3: 비즈니스 로직 고도화 - 포트폴리오 자동 리밸런싱
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingStrategy {

    private Long id;
    private String name;
    private String description;
    private RebalancingType type;
    private RebalancingFrequency frequency;
    private Map<String, BigDecimal> targetWeights;
    private BigDecimal toleranceThreshold;
    private BigDecimal minimumTradeAmount;
    private boolean taxOptimized;
    private boolean considerTransactionCosts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 리밸런싱 필요 여부 판단
     */
    public boolean requiresRebalancing(Portfolio portfolio) {
        if (targetWeights == null || targetWeights.isEmpty()) {
            return false;
        }

        Map<String, BigDecimal> currentWeights = portfolio.getWeights();

        return targetWeights.entrySet().stream()
            .anyMatch(entry -> {
                String symbol = entry.getKey();
                BigDecimal targetWeight = entry.getValue();
                BigDecimal currentWeight = currentWeights.getOrDefault(symbol, BigDecimal.ZERO);
                BigDecimal deviation = targetWeight.subtract(currentWeight).abs();
                return deviation.compareTo(toleranceThreshold) > 0;
            });
    }

    /**
     * 리밸런싱 편차 계산
     */
    public Map<String, BigDecimal> calculateDeviations(Portfolio portfolio) {
        Map<String, BigDecimal> currentWeights = portfolio.getWeights();

        return targetWeights.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    String symbol = entry.getKey();
                    BigDecimal targetWeight = entry.getValue();
                    BigDecimal currentWeight = currentWeights.getOrDefault(symbol, BigDecimal.ZERO);
                    return targetWeight.subtract(currentWeight);
                }
            ));
    }

    /**
     * 최대 편차 계산
     */
    public BigDecimal getMaxDeviation(Portfolio portfolio) {
        return calculateDeviations(portfolio).values().stream()
            .map(BigDecimal::abs)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    /**
     * 리밸런싱 전략 유효성 검증
     */
    public boolean isValid() {
        if (targetWeights == null || targetWeights.isEmpty()) {
            return false;
        }

        // 목표 비중 합계가 100%인지 확인
        BigDecimal totalWeight = targetWeights.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedTotal = BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.valueOf(0.0001); // 0.01% tolerance

        return totalWeight.subtract(expectedTotal).abs().compareTo(tolerance) <= 0;
    }

    /**
     * 리밸런싱 타입 열거형
     */
    public enum RebalancingType {
        STRATEGIC("전략적", "Long-term strategic allocation"),
        TACTICAL("전술적", "Short-term tactical adjustment"),
        RISK_PARITY("리스크패리티", "Risk-based allocation"),
        MOMENTUM("모멘텀", "Momentum-based allocation"),
        MEAN_REVERSION("평균회귀", "Mean reversion strategy"),
        MARKET_CAP("시가총액", "Market capitalization weighted"),
        EQUAL_WEIGHT("동일비중", "Equal weight allocation");

        private final String koreanName;
        private final String description;

        RebalancingType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }

    /**
     * 리밸런싱 주기 열거형
     */
    public enum RebalancingFrequency {
        DAILY("매일", 1),
        WEEKLY("매주", 7),
        MONTHLY("매월", 30),
        QUARTERLY("분기별", 90),
        SEMI_ANNUALLY("반기별", 180),
        ANNUALLY("연간", 365),
        THRESHOLD_BASED("임계치기반", 0); // 편차가 임계치 초과시에만

        private final String koreanName;
        private final int days;

        RebalancingFrequency(String koreanName, int days) {
            this.koreanName = koreanName;
            this.days = days;
        }

        public String getKoreanName() { return koreanName; }
        public int getDays() { return days; }
    }
}