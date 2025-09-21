package com.stockquest.domain.risk;

import com.stockquest.domain.portfolio.PortfolioPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 실시간 리스크 모니터링 도메인 서비스
 * 포트폴리오 리스크 한도 관리, 알림, 자동 대응 등
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeRiskMonitor {

    private String monitorId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;

    // 리스크 한도 설정
    private Map<String, BigDecimal> riskLimits;           // 리스크 유형별 한도
    private Map<String, Double> alertThresholds;          // 알림 임계값 (한도 대비 %)
    private Map<String, Integer> monitoringFrequency;     // 모니터링 빈도 (분)

    // 현재 리스크 상태
    private Map<String, BigDecimal> currentRiskLevels;    // 현재 리스크 수준
    private Map<String, String> riskStatuses;            // 리스크 상태 (GREEN, YELLOW, RED)
    private List<RiskAlert> activeAlerts;                 // 활성 알림 목록

    // 리스크 이력
    private Map<LocalDateTime, Map<String, BigDecimal>> riskHistory; // 리스크 이력
    private Integer maxHistorySize;                       // 최대 이력 보관 건수


    /**
     * 리스크 한도 설정
     */
    public void setRiskLimit(String riskType, BigDecimal limit, Double alertThreshold) {
        validateRiskType(riskType);
        validateLimit(limit);
        validateThreshold(alertThreshold);

        this.riskLimits.put(riskType, limit);
        this.alertThresholds.put(riskType, alertThreshold);
        this.lastUpdateAt = LocalDateTime.now();
    }

    /**
     * 실시간 리스크 레벨 업데이트
     */
    public RiskMonitoringResult updateRiskLevels(
            Long portfolioId,
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            StressTestResult latestStressTest) {

        LocalDateTime updateTime = LocalDateTime.now();

        // 현재 리스크 레벨 계산
        Map<String, BigDecimal> newRiskLevels = calculateCurrentRiskLevels(
                positions, currentPrices, latestStressTest);

        // 리스크 상태 업데이트
        Map<String, String> newRiskStatuses = updateRiskStatuses(newRiskLevels);

        // 새로운 알림 생성
        List<RiskAlert> newAlerts = generateAlerts(portfolioId, newRiskLevels, updateTime);

        // 상태 업데이트
        this.currentRiskLevels.putAll(newRiskLevels);
        this.riskStatuses.putAll(newRiskStatuses);
        this.activeAlerts.addAll(newAlerts);
        this.lastUpdateAt = updateTime;

        // 이력 저장
        addToHistory(updateTime, newRiskLevels);

        return RiskMonitoringResult.builder()
                .portfolioId(portfolioId)
                .updateTime(updateTime)
                .riskLevels(newRiskLevels)
                .riskStatuses(newRiskStatuses)
                .newAlerts(newAlerts)
                .overallRiskScore(calculateOverallRiskScore(newRiskLevels))
                .recommendation(generateRecommendation(newRiskLevels, newRiskStatuses))
                .build();
    }

    /**
     * 포지션 크기 자동 조절 권장사항
     */
    public Map<String, BigDecimal> recommendPositionAdjustments(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            Map<String, BigDecimal> riskContributions) {

        Map<String, BigDecimal> adjustments = new HashMap<>();

        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;

            String instrument = position.getInstrumentKey();
            BigDecimal riskContribution = riskContributions.get(instrument);

            if (riskContribution != null) {
                String riskStatus = this.riskStatuses.getOrDefault("OVERALL", "GREEN");
                BigDecimal adjustmentFactor = calculateAdjustmentFactor(riskStatus, riskContribution);

                // 현재 포지션 대비 조정 비율
                BigDecimal currentQuantity = position.getQuantity();
                BigDecimal recommendedQuantity = currentQuantity.multiply(adjustmentFactor);
                BigDecimal adjustment = recommendedQuantity.subtract(currentQuantity);

                if (adjustment.abs().compareTo(BigDecimal.valueOf(0.01)) > 0) {
                    adjustments.put(instrument, adjustment);
                }
            }
        }

        return adjustments;
    }

    /**
     * 상관관계 실시간 추적
     */
    public Map<String, Double> trackCorrelationChanges(
            Map<String, List<BigDecimal>> recentReturns,
            Map<String, Map<String, Double>> baselineCorrelations) {

        Map<String, Double> correlationChanges = new HashMap<>();

        if (recentReturns.size() < 2) {
            return correlationChanges;
        }

        List<String> assets = new ArrayList<>(recentReturns.keySet());

        for (int i = 0; i < assets.size(); i++) {
            for (int j = i + 1; j < assets.size(); j++) {
                String asset1 = assets.get(i);
                String asset2 = assets.get(j);
                String pairKey = asset1 + "_" + asset2;

                List<BigDecimal> returns1 = recentReturns.get(asset1);
                List<BigDecimal> returns2 = recentReturns.get(asset2);

                if (returns1.size() == returns2.size() && returns1.size() >= 10) {
                    double currentCorrelation = calculateCorrelation(returns1, returns2);
                    double baselineCorrelation = getBaselineCorrelation(asset1, asset2, baselineCorrelations);
                    double change = currentCorrelation - baselineCorrelation;

                    correlationChanges.put(pairKey, change);
                }
            }
        }

        return correlationChanges;
    }

    /**
     * 리스크 한도 위반 검사
     */
    public List<RiskLimitViolation> checkRiskLimitViolations() {
        List<RiskLimitViolation> violations = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : this.currentRiskLevels.entrySet()) {
            String riskType = entry.getKey();
            BigDecimal currentLevel = entry.getValue();
            BigDecimal limit = this.riskLimits.get(riskType);

            if (limit != null && currentLevel.compareTo(limit) > 0) {
                BigDecimal excess = currentLevel.subtract(limit);
                double excessPercentage = excess.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100;

                violations.add(RiskLimitViolation.builder()
                        .riskType(riskType)
                        .currentLevel(currentLevel)
                        .limitLevel(limit)
                        .excessAmount(excess)
                        .excessPercentage(excessPercentage)
                        .detectedAt(LocalDateTime.now())
                        .severity(calculateViolationSeverity(excessPercentage))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 자동 리스크 대응 권장사항
     */
    public List<String> generateAutomaticResponseRecommendations() {
        List<String> recommendations = new ArrayList<>();
        List<RiskLimitViolation> violations = checkRiskLimitViolations();

        for (RiskLimitViolation violation : violations) {
            switch (violation.getSeverity()) {
                case "CRITICAL" -> {
                    recommendations.add("즉시 포지션 50% 축소 - " + violation.getRiskType());
                    recommendations.add("모든 신규 거래 중단");
                    recommendations.add("헤지 포지션 대폭 확대");
                }
                case "HIGH" -> {
                    recommendations.add("포지션 30% 축소 고려 - " + violation.getRiskType());
                    recommendations.add("신규 거래 제한");
                    recommendations.add("방어적 자산 비중 증가");
                }
                case "MEDIUM" -> {
                    recommendations.add("포지션 모니터링 강화 - " + violation.getRiskType());
                    recommendations.add("리밸런싱 검토");
                }
            }
        }

        // 전체적인 권장사항
        String overallStatus = this.riskStatuses.getOrDefault("OVERALL", "GREEN");
        switch (overallStatus) {
            case "RED" -> {
                recommendations.add("포트폴리오 전면 재검토 필요");
                recommendations.add("현금 비중 확대");
                recommendations.add("상관관계 낮은 자산으로 다각화");
            }
            case "YELLOW" -> {
                recommendations.add("리스크 모니터링 빈도 증가");
                recommendations.add("스톱로스 주문 검토");
            }
        }

        return recommendations.stream().distinct().collect(Collectors.toList());
    }

    // === Private Helper Methods ===

    private void validateRiskType(String riskType) {
        if (riskType == null || riskType.trim().isEmpty()) {
            throw new IllegalArgumentException("리스크 타입은 필수입니다");
        }
    }

    private void validateLimit(BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("리스크 한도는 양수여야 합니다");
        }
    }

    private void validateThreshold(Double threshold) {
        if (threshold == null || threshold <= 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("알림 임계값은 0과 1 사이여야 합니다");
        }
    }

    private Map<String, BigDecimal> calculateCurrentRiskLevels(
            List<PortfolioPosition> positions,
            Map<String, BigDecimal> currentPrices,
            StressTestResult latestStressTest) {

        Map<String, BigDecimal> riskLevels = new HashMap<>();

        if (latestStressTest != null) {
            // VaR 기반 리스크
            if (latestStressTest.getValueAtRisk99() != null) {
                riskLevels.put("VAR_99", latestStressTest.getValueAtRisk99());
            }
            if (latestStressTest.getConditionalVaR() != null) {
                riskLevels.put("CVAR", latestStressTest.getConditionalVaR());
            }
            if (latestStressTest.getMaximumDrawdown() != null) {
                riskLevels.put("MAX_DRAWDOWN", latestStressTest.getMaximumDrawdown());
            }
        }

        // 집중도 리스크 계산
        BigDecimal concentrationRisk = calculateConcentrationRisk(positions, currentPrices);
        riskLevels.put("CONCENTRATION", concentrationRisk);

        // 유동성 리스크 계산
        BigDecimal liquidityRisk = calculateLiquidityRisk(positions);
        riskLevels.put("LIQUIDITY", liquidityRisk);

        return riskLevels;
    }

    private Map<String, String> updateRiskStatuses(Map<String, BigDecimal> riskLevels) {
        Map<String, String> statuses = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : riskLevels.entrySet()) {
            String riskType = entry.getKey();
            BigDecimal currentLevel = entry.getValue();
            BigDecimal limit = this.riskLimits.get(riskType);
            Double alertThreshold = this.alertThresholds.get(riskType);

            if (limit != null && alertThreshold != null) {
                BigDecimal yellowThreshold = limit.multiply(BigDecimal.valueOf(alertThreshold));
                BigDecimal redThreshold = limit;

                if (currentLevel.compareTo(redThreshold) >= 0) {
                    statuses.put(riskType, "RED");
                } else if (currentLevel.compareTo(yellowThreshold) >= 0) {
                    statuses.put(riskType, "YELLOW");
                } else {
                    statuses.put(riskType, "GREEN");
                }
            } else {
                statuses.put(riskType, "GREEN");
            }
        }

        // 전체 상태 계산
        long redCount = statuses.values().stream().mapToLong(status -> "RED".equals(status) ? 1L : 0L).sum();
        long yellowCount = statuses.values().stream().mapToLong(status -> "YELLOW".equals(status) ? 1L : 0L).sum();

        if (redCount > 0) {
            statuses.put("OVERALL", "RED");
        } else if (yellowCount > 0) {
            statuses.put("OVERALL", "YELLOW");
        } else {
            statuses.put("OVERALL", "GREEN");
        }

        return statuses;
    }

    private List<RiskAlert> generateAlerts(Long portfolioId, Map<String, BigDecimal> riskLevels, LocalDateTime updateTime) {
        List<RiskAlert> alerts = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : riskLevels.entrySet()) {
            String riskType = entry.getKey();
            BigDecimal currentLevel = entry.getValue();
            BigDecimal limit = this.riskLimits.get(riskType);
            Double alertThreshold = this.alertThresholds.get(riskType);

            if (limit != null && alertThreshold != null) {
                BigDecimal alertLevel = limit.multiply(BigDecimal.valueOf(alertThreshold));

                if (currentLevel.compareTo(alertLevel) >= 0) {
                    // 중복 알림 체크
                    boolean isDuplicate = this.activeAlerts.stream()
                            .anyMatch(alert -> alert.getRiskType().equals(riskType) &&
                                    alert.getPortfolioId().equals(portfolioId) &&
                                    !alert.isResolved());

                    if (!isDuplicate) {
                        String severity = currentLevel.compareTo(limit) >= 0 ? "HIGH" : "MEDIUM";

                        alerts.add(RiskAlert.builder()
                                .alertId(UUID.randomUUID().toString())
                                .portfolioId(portfolioId)
                                .riskType(riskType)
                                .currentLevel(currentLevel)
                                .limitLevel(limit)
                                .severity(severity)
                                .message(generateAlertMessage(riskType, currentLevel, limit))
                                .createdAt(updateTime)
                                .resolved(false)
                                .build());
                    }
                }
            }
        }

        return alerts;
    }

    private Integer calculateOverallRiskScore(Map<String, BigDecimal> riskLevels) {
        if (riskLevels.isEmpty()) {
            return 0;
        }

        double totalScore = 0.0;
        int validCounts = 0;

        for (Map.Entry<String, BigDecimal> entry : riskLevels.entrySet()) {
            String riskType = entry.getKey();
            BigDecimal currentLevel = entry.getValue();
            BigDecimal limit = this.riskLimits.get(riskType);

            if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = currentLevel.divide(limit, 4, RoundingMode.HALF_UP).doubleValue();
                totalScore += Math.min(ratio * 100, 100);
                validCounts++;
            }
        }

        return validCounts > 0 ? (int) Math.round(totalScore / validCounts) : 0;
    }

    private String generateRecommendation(Map<String, BigDecimal> riskLevels, Map<String, String> riskStatuses) {
        String overallStatus = riskStatuses.getOrDefault("OVERALL", "GREEN");

        return switch (overallStatus) {
            case "RED" -> "즉시 포지션 축소 및 헤지 전략 강화가 필요합니다.";
            case "YELLOW" -> "리스크 모니터링을 강화하고 포트폴리오 리밸런싱을 고려하세요.";
            default -> "현재 리스크 수준은 양호합니다. 정기적인 모니터링을 유지하세요.";
        };
    }

    private void addToHistory(LocalDateTime updateTime, Map<String, BigDecimal> riskLevels) {
        this.riskHistory.put(updateTime, new HashMap<>(riskLevels));

        // 이력 크기 제한
        if (this.riskHistory.size() > this.maxHistorySize) {
            LocalDateTime oldestTime = this.riskHistory.keySet().stream()
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            if (oldestTime != null) {
                this.riskHistory.remove(oldestTime);
            }
        }
    }

    private BigDecimal calculateAdjustmentFactor(String riskStatus, BigDecimal riskContribution) {
        return switch (riskStatus) {
            case "RED" -> BigDecimal.valueOf(0.5);     // 50% 축소
            case "YELLOW" -> BigDecimal.valueOf(0.8);  // 20% 축소
            default -> BigDecimal.ONE;                 // 변경 없음
        };
    }

    private double calculateCorrelation(List<BigDecimal> returns1, List<BigDecimal> returns2) {
        if (returns1.size() != returns2.size() || returns1.size() < 2) {
            return 0.0;
        }

        BigDecimal mean1 = returns1.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns1.size()), 6, RoundingMode.HALF_UP);

        BigDecimal mean2 = returns2.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns2.size()), 6, RoundingMode.HALF_UP);

        double covariance = 0.0;
        double variance1 = 0.0;
        double variance2 = 0.0;

        for (int i = 0; i < returns1.size(); i++) {
            double dev1 = returns1.get(i).subtract(mean1).doubleValue();
            double dev2 = returns2.get(i).subtract(mean2).doubleValue();

            covariance += dev1 * dev2;
            variance1 += dev1 * dev1;
            variance2 += dev2 * dev2;
        }

        covariance /= (returns1.size() - 1);
        variance1 /= (returns1.size() - 1);
        variance2 /= (returns2.size() - 1);

        double stdDev1 = Math.sqrt(variance1);
        double stdDev2 = Math.sqrt(variance2);

        return (stdDev1 * stdDev2) == 0.0 ? 0.0 : covariance / (stdDev1 * stdDev2);
    }

    private double getBaselineCorrelation(String asset1, String asset2,
                                         Map<String, Map<String, Double>> baselineCorrelations) {
        if (baselineCorrelations != null &&
                baselineCorrelations.containsKey(asset1) &&
                baselineCorrelations.get(asset1).containsKey(asset2)) {
            return baselineCorrelations.get(asset1).get(asset2);
        }
        return 0.0;
    }

    private BigDecimal calculateConcentrationRisk(List<PortfolioPosition> positions, Map<String, BigDecimal> currentPrices) {
        if (positions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalValue = positions.stream()
                .filter(PortfolioPosition::hasPosition)
                .map(position -> {
                    BigDecimal price = currentPrices.get(position.getInstrumentKey());
                    return price != null ? position.calculateCurrentValue(price) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 헤르핀달-허쉬만 지수 계산
        BigDecimal hhi = positions.stream()
                .filter(PortfolioPosition::hasPosition)
                .map(position -> {
                    BigDecimal price = currentPrices.get(position.getInstrumentKey());
                    if (price != null) {
                        BigDecimal positionValue = position.calculateCurrentValue(price);
                        BigDecimal weight = positionValue.divide(totalValue, 6, RoundingMode.HALF_UP);
                        return weight.pow(2);
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return hhi;
    }

    private BigDecimal calculateLiquidityRisk(List<PortfolioPosition> positions) {
        // 단순화된 유동성 리스크 계산
        // 실제로는 자산별 거래량, 스프레드 등을 고려해야 함
        return BigDecimal.valueOf(0.05); // 5% 기본값
    }

    private String calculateViolationSeverity(double excessPercentage) {
        if (excessPercentage >= 50.0) return "CRITICAL";
        if (excessPercentage >= 20.0) return "HIGH";
        if (excessPercentage >= 5.0) return "MEDIUM";
        return "LOW";
    }

    private String generateAlertMessage(String riskType, BigDecimal currentLevel, BigDecimal limit) {
        BigDecimal excess = currentLevel.subtract(limit);
        double excessPercentage = excess.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100;

        return String.format("%s 리스크가 한도를 %.1f%% 초과했습니다. (현재: %s, 한도: %s)",
                riskType, excessPercentage, currentLevel.toPlainString(), limit.toPlainString());
    }

    // === Inner Classes ===

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskMonitoringResult {
        private Long portfolioId;
        private LocalDateTime updateTime;
        private Map<String, BigDecimal> riskLevels;
        private Map<String, String> riskStatuses;
        private List<RiskAlert> newAlerts;
        private Integer overallRiskScore;
        private String recommendation;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAlert {
        private String alertId;
        private Long portfolioId;
        private String riskType;
        private BigDecimal currentLevel;
        private BigDecimal limitLevel;
        private String severity;
        private String message;
        private LocalDateTime createdAt;
        private boolean resolved;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskLimitViolation {
        private String riskType;
        private BigDecimal currentLevel;
        private BigDecimal limitLevel;
        private BigDecimal excessAmount;
        private Double excessPercentage;
        private LocalDateTime detectedAt;
        private String severity;
    }
}