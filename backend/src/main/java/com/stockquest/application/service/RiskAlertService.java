package com.stockquest.application.service;

import com.stockquest.domain.risk.RiskAlert;
import com.stockquest.domain.risk.RiskAlert.*;
import com.stockquest.domain.risk.VaRCalculation;
import com.stockquest.domain.risk.StressTest;
import com.stockquest.domain.risk.RiskBudget;
import com.stockquest.domain.portfolio.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.OptionalDouble;

/**
 * 리스크 알림 서비스
 * Phase 8.3: Advanced Risk Management - 리스크 모니터링 및 알림 시스템
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertService {
    
    private final NotificationService notificationService;
    private final PortfolioService portfolioService;
    private final RiskConfigurationService riskConfigurationService;
    
    // 알림 저장소 (실제 구현에서는 데이터베이스)
    private final Map<String, RiskAlert> activeAlerts = new HashMap<>();
    private final List<RiskAlert> alertHistory = new ArrayList<>();
    
    /**
     * VaR 한도 위반 알림 생성
     */
    public CompletableFuture<RiskAlert> generateVaRAlert(VaRCalculation varCalculation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Generating VaR breach alert for portfolio: {}", varCalculation.getPortfolioId());
                
                // VaR 한도 조회
                BigDecimal varLimit = riskConfigurationService.getVaRLimit(varCalculation.getPortfolioId());
                
                // 임계값 초과 비율 계산
                BigDecimal exceedancePercentage = calculateExceedancePercentage(
                    varCalculation.getVarPercentage(), varLimit);
                
                // 심각도 수준 결정
                AlertSeverity severity = determineVaRAlertSeverity(exceedancePercentage);
                
                // 영향받는 자산 식별
                List<String> affectedAssets = identifyAffectedAssets(varCalculation);
                
                // 권장 조치사항 생성
                List<RecommendedAction> recommendedActions = generateVaRRecommendedActions(
                    varCalculation, exceedancePercentage);
                
                // 관련 계산 정보
                RelatedCalculations relatedCalculations = RelatedCalculations.builder()
                    .varCalculationId(varCalculation.getCalculationId())
                    .calculationConfidence(varCalculation.getQualityMetrics() != null ? 
                        varCalculation.getQualityMetrics().getAccuracyScore() : null)
                    .methodology(RelatedCalculations.CalculationMethodology.valueOf(
                        varCalculation.getMethod().name()))
                    .build();
                
                // 알림 구성
                RiskAlert alert = RiskAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .portfolioId(varCalculation.getPortfolioId())
                    .alertType(AlertType.VAR_BREACH)
                    .severity(severity)
                    .status(AlertStatus.ACTIVE)
                    .title("VaR 한도 위반")
                    .message(String.format("포트폴리오 VaR이 한도를 %.2f%% 초과했습니다", exceedancePercentage))
                    .description(generateVaRAlertDescription(varCalculation, varLimit))
                    .currentValue(varCalculation.getVarPercentage())
                    .threshold(varLimit)
                    .exceedancePercentage(exceedancePercentage)
                    .metricType(RiskMetricType.VALUE_AT_RISK)
                    .affectedAssets(affectedAssets)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .recommendedActions(recommendedActions)
                    .configuration(createDefaultAlertConfiguration())
                    .history(createAlertHistory(varCalculation.getPortfolioId(), AlertType.VAR_BREACH))
                    .relatedCalculations(relatedCalculations)
                    .build();
                
                // 알림 저장 및 전송
                storeAlert(alert);
                sendAlert(alert);
                
                log.info("VaR alert generated: {} with severity: {}", alert.getAlertId(), severity);
                return alert;
                
            } catch (Exception e) {
                log.error("Failed to generate VaR alert for portfolio: {}", varCalculation.getPortfolioId(), e);
                throw new RuntimeException("VaR alert generation failed", e);
            }
        });
    }
    
    /**
     * 모델 성능 저하 알림 생성
     */
    public CompletableFuture<RiskAlert> generateModelDegradationAlert(VaRCalculation varCalculation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Generating model degradation alert for portfolio: {}", varCalculation.getPortfolioId());
                
                BigDecimal accuracyScore = varCalculation.getQualityMetrics().getAccuracyScore();
                BigDecimal minAccuracyThreshold = riskConfigurationService.getMinAccuracyThreshold();
                
                // 성능 저하 정도 계산
                BigDecimal degradationPercentage = calculateDegradationPercentage(accuracyScore, minAccuracyThreshold);
                
                // 심각도 수준 결정
                AlertSeverity severity = determineModelDegradationSeverity(degradationPercentage);
                
                // 권장 조치사항 생성
                List<RecommendedAction> recommendedActions = generateModelDegradationActions(varCalculation);
                
                RiskAlert alert = RiskAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .portfolioId(varCalculation.getPortfolioId())
                    .alertType(AlertType.MODEL_DEGRADATION)
                    .severity(severity)
                    .status(AlertStatus.ACTIVE)
                    .title("ML 모델 성능 저하")
                    .message(String.format("VaR 모델의 정확도가 %.2f%%로 임계치를 하회했습니다", 
                        accuracyScore.multiply(BigDecimal.valueOf(100))))
                    .description(generateModelDegradationDescription(varCalculation))
                    .currentValue(accuracyScore)
                    .threshold(minAccuracyThreshold)
                    .exceedancePercentage(degradationPercentage)
                    .metricType(RiskMetricType.VALUE_AT_RISK)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(48))
                    .recommendedActions(recommendedActions)
                    .configuration(createModelDegradationConfiguration())
                    .relatedCalculations(RelatedCalculations.builder()
                        .varCalculationId(varCalculation.getCalculationId())
                        .calculationConfidence(accuracyScore)
                        .methodology(RelatedCalculations.CalculationMethodology.valueOf(
                            varCalculation.getMethod().name()))
                        .build())
                    .build();
                
                storeAlert(alert);
                sendAlert(alert);
                
                return alert;
                
            } catch (Exception e) {
                log.error("Failed to generate model degradation alert", e);
                throw new RuntimeException("Model degradation alert generation failed", e);
            }
        });
    }
    
    /**
     * 스트레스 테스트 실패 알림 생성
     */
    public CompletableFuture<RiskAlert> generateStressTestFailureAlert(StressTest stressTest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Generating stress test failure alert for portfolio: {}", stressTest.getPortfolioId());
                
                // 최악 시나리오 결과
                StressTest.ScenarioResult worstScenario = stressTest.getResult().getScenarioResults().stream()
                    .max((s1, s2) -> s1.getLossPercentage().compareTo(s2.getLossPercentage()))
                    .orElse(null);
                
                AlertSeverity severity = worstScenario != null && 
                    worstScenario.getLossPercentage().compareTo(new BigDecimal("20.0")) > 0 ? 
                    AlertSeverity.CRITICAL : AlertSeverity.HIGH;
                
                // 권장 조치사항
                List<RecommendedAction> recommendedActions = generateStressTestActions(stressTest);
                
                RiskAlert alert = RiskAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .portfolioId(stressTest.getPortfolioId())
                    .alertType(AlertType.STRESS_TEST_FAILURE)
                    .severity(severity)
                    .status(AlertStatus.ACTIVE)
                    .title("스트레스 테스트 실패")
                    .message(String.format("%s 스트레스 테스트에서 %.2f%% 손실 발생", 
                        stressTest.getTestType().getDescription(),
                        worstScenario != null ? worstScenario.getLossPercentage() : BigDecimal.ZERO))
                    .description(generateStressTestDescription(stressTest))
                    .currentValue(worstScenario != null ? worstScenario.getLossPercentage() : BigDecimal.ZERO)
                    .threshold(new BigDecimal("15.0")) // 허용 한도
                    .metricType(RiskMetricType.MAXIMUM_DRAWDOWN)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .recommendedActions(recommendedActions)
                    .configuration(createStressTestConfiguration())
                    .relatedCalculations(RelatedCalculations.builder()
                        .stressTestId(stressTest.getTestId())
                        .calculationConfidence(new BigDecimal("0.95"))
                        .methodology(RelatedCalculations.CalculationMethodology.MONTE_CARLO)
                        .build())
                    .build();
                
                storeAlert(alert);
                sendAlert(alert);
                
                return alert;
                
            } catch (Exception e) {
                log.error("Failed to generate stress test failure alert", e);
                throw new RuntimeException("Stress test failure alert generation failed", e);
            }
        });
    }
    
    /**
     * 리스크 예산 초과 알림 생성
     */
    public CompletableFuture<RiskAlert> generateRiskBudgetExceededAlert(RiskBudget riskBudget) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Generating risk budget exceeded alert for portfolio: {}", riskBudget.getPortfolioId());
                
                // 초과 정도에 따른 심각도
                AlertSeverity severity = riskBudget.getUtilizationRate().compareTo(new BigDecimal("100.0")) > 0 ?
                    AlertSeverity.CRITICAL : AlertSeverity.HIGH;
                
                // 초과한 자산들 식별
                List<String> exceededAssets = riskBudget.getAssetAllocations().stream()
                    .filter(asset -> asset.getLimitExceeded())
                    .map(RiskBudget.AssetAllocation::getSymbol)
                    .collect(Collectors.toList());
                
                // 권장 조치사항
                List<RecommendedAction> recommendedActions = generateRiskBudgetActions(riskBudget);
                
                RiskAlert alert = RiskAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .portfolioId(riskBudget.getPortfolioId())
                    .alertType(AlertType.EXPOSURE_LIMIT)
                    .severity(severity)
                    .status(AlertStatus.ACTIVE)
                    .title("리스크 예산 초과")
                    .message(String.format("리스크 예산 사용률이 %.2f%%에 도달했습니다", riskBudget.getUtilizationRate()))
                    .description(generateRiskBudgetDescription(riskBudget))
                    .currentValue(riskBudget.getUtilizationRate())
                    .threshold(new BigDecimal("95.0"))
                    .exceedancePercentage(riskBudget.getUtilizationRate().subtract(new BigDecimal("95.0")))
                    .metricType(RiskMetricType.CONCENTRATION)
                    .affectedAssets(exceededAssets)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(12))
                    .recommendedActions(recommendedActions)
                    .configuration(createRiskBudgetConfiguration())
                    .build();
                
                storeAlert(alert);
                sendAlert(alert);
                
                return alert;
                
            } catch (Exception e) {
                log.error("Failed to generate risk budget exceeded alert", e);
                throw new RuntimeException("Risk budget exceeded alert generation failed", e);
            }
        });
    }
    
    /**
     * 알림 상태 업데이트
     */
    public CompletableFuture<RiskAlert> updateAlertStatus(String alertId, AlertStatus newStatus) {
        return CompletableFuture.supplyAsync(() -> {
            RiskAlert alert = activeAlerts.get(alertId);
            if (alert == null) {
                throw new IllegalArgumentException("Alert not found: " + alertId);
            }
            
            AlertStatus previousStatus = alert.getStatus();
            alert.setStatus(newStatus);
            alert.setLastUpdatedAt(LocalDateTime.now());
            
            if (newStatus == AlertStatus.RESOLVED) {
                alert.setResolvedAt(LocalDateTime.now());
                // 해결된 알림을 히스토리로 이동
                alertHistory.add(alert);
                activeAlerts.remove(alertId);
            }
            
            log.info("Alert status updated: {} from {} to {}", alertId, previousStatus, newStatus);
            
            // 에스컬레이션이나 상태 변경 알림 전송
            if (alert.requiresEscalation()) {
                escalateAlert(alert);
            }
            
            return alert;
        });
    }
    
    /**
     * 활성 알림 목록 조회
     */
    public List<RiskAlert> getActiveAlerts(String portfolioId) {
        return activeAlerts.values().stream()
            .filter(alert -> portfolioId == null || portfolioId.equals(alert.getPortfolioId()))
            .sorted((a1, a2) -> Integer.compare(a1.getSeverity().getPriority(), a2.getSeverity().getPriority()))
            .collect(Collectors.toList());
    }
    
    /**
     * 치명적 알림 목록 조회
     */
    public List<RiskAlert> getCriticalAlerts() {
        return activeAlerts.values().stream()
            .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL)
            .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * 알림 통계 생성
     */
    public AlertStatistics generateAlertStatistics(String portfolioId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<RiskAlert> relevantAlerts = alertHistory.stream()
            .filter(alert -> portfolioId == null || portfolioId.equals(alert.getPortfolioId()))
            .filter(alert -> alert.getCreatedAt().isAfter(fromDate) && alert.getCreatedAt().isBefore(toDate))
            .collect(Collectors.toList());
        
        Map<AlertType, Long> alertTypeCount = relevantAlerts.stream()
            .collect(Collectors.groupingBy(RiskAlert::getAlertType, Collectors.counting()));
        
        Map<AlertSeverity, Long> severityCount = relevantAlerts.stream()
            .collect(Collectors.groupingBy(RiskAlert::getSeverity, Collectors.counting()));
        
        double averageResolutionMinutes = relevantAlerts.stream()
            .filter(alert -> alert.getResolvedAt() != null)
            .mapToLong(alert -> java.time.Duration.between(alert.getCreatedAt(), alert.getResolvedAt()).toMinutes())
            .average()
            .orElse(0.0);
        
        return AlertStatistics.builder()
            .portfolioId(portfolioId)
            .period(String.format("%s to %s", fromDate, toDate))
            .totalAlerts(relevantAlerts.size())
            .alertsByType(alertTypeCount)
            .alertsBySeverity(severityCount)
            .averageResolutionMinutes(averageResolutionMinutes)
            .criticalAlerts(severityCount.getOrDefault(AlertSeverity.CRITICAL, 0L))
            .build();
    }
    
    // ========================= 헬퍼 메서드들 =========================
    
    private BigDecimal calculateExceedancePercentage(BigDecimal currentValue, BigDecimal threshold) {
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(threshold)
               .divide(threshold, 4, RoundingMode.HALF_UP)
               .multiply(BigDecimal.valueOf(100));
    }
    
    private AlertSeverity determineVaRAlertSeverity(BigDecimal exceedancePercentage) {
        if (exceedancePercentage.compareTo(new BigDecimal("50.0")) > 0) {
            return AlertSeverity.CRITICAL;
        } else if (exceedancePercentage.compareTo(new BigDecimal("25.0")) > 0) {
            return AlertSeverity.HIGH;
        } else if (exceedancePercentage.compareTo(new BigDecimal("10.0")) > 0) {
            return AlertSeverity.MEDIUM;
        } else {
            return AlertSeverity.LOW;
        }
    }
    
    private AlertSeverity determineModelDegradationSeverity(BigDecimal degradationPercentage) {
        if (degradationPercentage.compareTo(new BigDecimal("30.0")) > 0) {
            return AlertSeverity.CRITICAL;
        } else if (degradationPercentage.compareTo(new BigDecimal("20.0")) > 0) {
            return AlertSeverity.HIGH;
        } else {
            return AlertSeverity.MEDIUM;
        }
    }
    
    private BigDecimal calculateDegradationPercentage(BigDecimal currentAccuracy, BigDecimal minThreshold) {
        if (minThreshold.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return minThreshold.subtract(currentAccuracy)
               .divide(minThreshold, 4, RoundingMode.HALF_UP)
               .multiply(BigDecimal.valueOf(100));
    }
    
    private List<String> identifyAffectedAssets(VaRCalculation varCalculation) {
        if (varCalculation.getComponents() == null) {
            return new ArrayList<>();
        }
        
        return varCalculation.getComponents().stream()
            .sorted((c1, c2) -> c2.getVarContributionPercentage().compareTo(c1.getVarContributionPercentage()))
            .limit(5) // 상위 5개 기여도 자산
            .map(VaRCalculation.VaRComponent::getComponentId)
            .collect(Collectors.toList());
    }
    
    private List<RecommendedAction> generateVaRRecommendedActions(VaRCalculation varCalculation, BigDecimal exceedancePercentage) {
        List<RecommendedAction> actions = new ArrayList<>();
        
        // 포지션 축소
        actions.add(RecommendedAction.builder()
            .actionType(RecommendedAction.ActionType.REDUCE_POSITION)
            .title("고위험 포지션 축소")
            .description("VaR 기여도가 높은 자산의 포지션을 축소하여 리스크를 감소시키세요")
            .priority(1)
            .expectedImpact(String.format("VaR을 약 %.1f%% 감소시킬 것으로 예상", exceedancePercentage.multiply(new BigDecimal("0.3"))))
            .complexity(RecommendedAction.ComplexityLevel.MODERATE)
            .estimatedTimeMinutes(30)
            .authorizationRequired(RecommendedAction.AuthorizationLevel.SUPERVISOR)
            .targetAssets(identifyAffectedAssets(varCalculation))
            .build());
        
        // 헤지 실행
        if (exceedancePercentage.compareTo(new BigDecimal("25.0")) > 0) {
            actions.add(RecommendedAction.builder()
                .actionType(RecommendedAction.ActionType.HEDGE_EXPOSURE)
                .title("헤지 포지션 구축")
                .description("파생상품을 이용하여 포트폴리오 리스크를 헤지하세요")
                .priority(2)
                .expectedImpact("다운사이드 리스크를 50% 이상 감소")
                .complexity(RecommendedAction.ComplexityLevel.COMPLEX)
                .estimatedTimeMinutes(60)
                .authorizationRequired(RecommendedAction.AuthorizationLevel.MANAGER)
                .build());
        }
        
        // 면밀 모니터링
        actions.add(RecommendedAction.builder()
            .actionType(RecommendedAction.ActionType.MONITOR_CLOSELY)
            .title("리스크 모니터링 강화")
            .description("포트폴리오 VaR을 실시간으로 모니터링하고 추가 악화 시 즉시 조치하세요")
            .priority(3)
            .expectedImpact("조기 경보를 통한 리스크 관리")
            .complexity(RecommendedAction.ComplexityLevel.SIMPLE)
            .estimatedTimeMinutes(10)
            .authorizationRequired(RecommendedAction.AuthorizationLevel.USER)
            .build());
        
        return actions;
    }
    
    private List<RecommendedAction> generateModelDegradationActions(VaRCalculation varCalculation) {
        List<RecommendedAction> actions = new ArrayList<>();
        
        actions.add(RecommendedAction.builder()
            .actionType(RecommendedAction.ActionType.REVIEW_STRATEGY)
            .title("모델 재검토 및 재보정")
            .description("VaR 모델의 파라미터를 재검토하고 최신 시장 데이터로 재보정하세요")
            .priority(1)
            .expectedImpact("모델 정확도 개선")
            .complexity(RecommendedAction.ComplexityLevel.COMPLEX)
            .estimatedTimeMinutes(240)
            .authorizationRequired(RecommendedAction.AuthorizationLevel.SENIOR_MANAGER)
            .build());
        
        return actions;
    }
    
    private List<RecommendedAction> generateStressTestActions(StressTest stressTest) {
        List<RecommendedAction> actions = new ArrayList<>();
        
        actions.add(RecommendedAction.builder()
            .actionType(RecommendedAction.ActionType.REBALANCE_PORTFOLIO)
            .title("포트폴리오 리밸런싱")
            .description("스트레스 시나리오에서 취약한 섹터의 비중을 줄이고 방어적 자산 비중을 늘리세요")
            .priority(1)
            .expectedImpact("스트레스 시나리오 손실을 20-30% 감소")
            .complexity(RecommendedAction.ComplexityLevel.MODERATE)
            .estimatedTimeMinutes(120)
            .authorizationRequired(RecommendedAction.AuthorizationLevel.MANAGER)
            .build());
        
        return actions;
    }
    
    private List<RecommendedAction> generateRiskBudgetActions(RiskBudget riskBudget) {
        List<RecommendedAction> actions = new ArrayList<>();
        
        actions.add(RecommendedAction.builder()
            .actionType(RecommendedAction.ActionType.REDUCE_POSITION)
            .title("예산 초과 포지션 축소")
            .description("예산을 초과한 자산들의 포지션을 즉시 축소하여 예산 범위 내로 조정하세요")
            .priority(1)
            .expectedImpact("리스크 예산 준수")
            .complexity(RecommendedAction.ComplexityLevel.MODERATE)
            .estimatedTimeMinutes(45)
            .authorizationRequired(RecommendedAction.AuthorizationLevel.SUPERVISOR)
            .build());
        
        return actions;
    }
    
    private String generateVaRAlertDescription(VaRCalculation varCalculation, BigDecimal varLimit) {
        return String.format(
            "포트폴리오의 %s VaR이 %.2f%%로 계산되어 설정된 한도 %.2f%%를 초과했습니다. " +
            "신뢰구간: %.1f%%, 보유기간: %d일, 계산 방법: %s",
            varCalculation.getMethod().getKoreanName(),
            varCalculation.getVarPercentage(),
            varLimit,
            varCalculation.getConfidenceLevel().multiply(BigDecimal.valueOf(100)),
            varCalculation.getHoldingPeriod(),
            varCalculation.getMethod().getKoreanName()
        );
    }
    
    private String generateModelDegradationDescription(VaRCalculation varCalculation) {
        return String.format(
            "VaR 모델의 정확도 점수가 %.2f%%로 측정되어 최소 요구 수준을 하회했습니다. " +
            "백테스팅 위반 횟수: %d회, Kupiec 검정 p-value: %.4f",
            varCalculation.getQualityMetrics().getAccuracyScore().multiply(BigDecimal.valueOf(100)),
            varCalculation.getQualityMetrics().getBacktestViolations(),
            varCalculation.getQualityMetrics().getKupiecPValue()
        );
    }
    
    private String generateStressTestDescription(StressTest stressTest) {
        return String.format(
            "%s 스트레스 테스트에서 포트폴리오가 실패했습니다. " +
            "테스트 시나리오: %d개, 최대 예상 손실: %.2f%%, 생존 확률: %.2f%%",
            stressTest.getTestType().getDescription(),
            stressTest.getScenarios().size(),
            stressTest.getResult().getMaxLoss(),
            stressTest.getResult().getSurvivalProbability()
        );
    }
    
    private String generateRiskBudgetDescription(RiskBudget riskBudget) {
        return String.format(
            "리스크 예산 '%s'의 사용률이 %.2f%%에 도달했습니다. " +
            "총 예산: %s, 사용 예산: %s, 남은 예산: %s",
            riskBudget.getBudgetName(),
            riskBudget.getUtilizationRate(),
            riskBudget.getTotalRiskBudget(),
            riskBudget.getUsedRiskBudget(),
            riskBudget.getRemainingRiskBudget()
        );
    }
    
    private void storeAlert(RiskAlert alert) {
        activeAlerts.put(alert.getAlertId(), alert);
        log.debug("Alert stored: {}", alert.getAlertId());
    }
    
    private void sendAlert(RiskAlert alert) {
        try {
            if (alert.getConfiguration() != null && alert.getConfiguration().getNotificationChannels() != null) {
                for (AlertConfiguration.NotificationChannel channel : alert.getConfiguration().getNotificationChannels()) {
                    notificationService.sendNotification(alert, channel);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send alert notification: {}", alert.getAlertId(), e);
        }
    }
    
    private void escalateAlert(RiskAlert alert) {
        log.info("Escalating alert: {} to higher management", alert.getAlertId());
        
        if (alert.getConfiguration() != null && 
            alert.getConfiguration().getEscalationRule() != null) {
            
            AlertConfiguration.EscalationRule escalationRule = alert.getConfiguration().getEscalationRule();
            
            // 에스컬레이션 수신자들에게 알림 전송
            for (String recipient : escalationRule.getEscalationRecipients()) {
                notificationService.sendEscalationNotification(alert, recipient);
            }
            
            // 알림 심각도 상승
            alert.setSeverity(escalationRule.getEscalationSeverity());
            alert.setStatus(AlertStatus.ESCALATED);
        }
    }
    
    private AlertConfiguration createDefaultAlertConfiguration() {
        return AlertConfiguration.builder()
            .ruleId("default-var-rule")
            .ruleName("기본 VaR 알림 규칙")
            .monitoringFrequencyMinutes(15)
            .notificationChannels(Arrays.asList(
                AlertConfiguration.NotificationChannel.EMAIL,
                AlertConfiguration.NotificationChannel.IN_APP
            ))
            .escalationRule(AlertConfiguration.EscalationRule.builder()
                .escalationDelayMinutes(60)
                .escalationSeverity(AlertSeverity.HIGH)
                .maxEscalationLevel(2)
                .build())
            .autoResolveEnabled(false)
            .build();
    }
    
    private AlertConfiguration createModelDegradationConfiguration() {
        return createDefaultAlertConfiguration().toBuilder()
            .ruleId("model-degradation-rule")
            .ruleName("모델 성능 저하 알림 규칙")
            .escalationRule(AlertConfiguration.EscalationRule.builder()
                .escalationDelayMinutes(30)
                .escalationSeverity(AlertSeverity.CRITICAL)
                .maxEscalationLevel(3)
                .build())
            .build();
    }
    
    private AlertConfiguration createStressTestConfiguration() {
        return createDefaultAlertConfiguration().toBuilder()
            .ruleId("stress-test-rule")
            .ruleName("스트레스 테스트 실패 알림 규칙")
            .build();
    }
    
    private AlertConfiguration createRiskBudgetConfiguration() {
        return createDefaultAlertConfiguration().toBuilder()
            .ruleId("risk-budget-rule")
            .ruleName("리스크 예산 초과 알림 규칙")
            .autoResolveEnabled(true)
            .autoResolveCondition(AlertConfiguration.AutoResolveCondition.builder()
                .resolveThreshold(new BigDecimal("90.0"))
                .confirmationPeriodMinutes(30)
                .requiresManualConfirmation(false)
                .build())
            .build();
    }
    
    private AlertHistory createAlertHistory(String portfolioId, AlertType alertType) {
        // 과거 동일한 타입의 알림 히스토리 조회
        List<RiskAlert> previousAlerts = alertHistory.stream()
            .filter(alert -> alert.getPortfolioId().equals(portfolioId) && alert.getAlertType() == alertType)
            .collect(Collectors.toList());
        
        return AlertHistory.builder()
            .previousOccurrences(previousAlerts.size())
            .lastOccurrence(previousAlerts.isEmpty() ? null : 
                previousAlerts.get(previousAlerts.size() - 1).getCreatedAt())
            .averageResolutionMinutes(calculateAverageResolutionTime(previousAlerts))
            .dailyFrequency(calculateDailyFrequency(previousAlerts))
            .trendDirection(AlertHistory.TrendDirection.STABLE)
            .build();
    }
    
    private Integer calculateAverageResolutionTime(List<RiskAlert> alerts) {
        OptionalDouble average = alerts.stream()
            .filter(alert -> alert.getResolvedAt() != null)
            .mapToInt(alert -> (int) java.time.Duration.between(alert.getCreatedAt(), alert.getResolvedAt()).toMinutes())
            .average();
        
        return average.isPresent() ? (int) average.getAsDouble() : 0;
    }
    
    private Double calculateDailyFrequency(List<RiskAlert> alerts) {
        if (alerts.isEmpty()) return 0.0;
        
        LocalDateTime earliest = alerts.stream()
            .map(RiskAlert::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        
        long daysSinceEarliest = java.time.Duration.between(earliest, LocalDateTime.now()).toDays();
        return daysSinceEarliest > 0 ? (double) alerts.size() / daysSinceEarliest : 0.0;
    }
    
    // 알림 통계 클래스
    @lombok.Data
    @lombok.Builder
    public static class AlertStatistics {
        private String portfolioId;
        private String period;
        private int totalAlerts;
        private Map<AlertType, Long> alertsByType;
        private Map<AlertSeverity, Long> alertsBySeverity;
        private double averageResolutionMinutes;
        private Long criticalAlerts;
    }
}