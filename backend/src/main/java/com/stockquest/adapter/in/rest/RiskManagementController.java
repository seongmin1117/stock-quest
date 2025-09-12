package com.stockquest.adapter.in.rest;

import com.stockquest.application.service.RiskAlertService;
import com.stockquest.application.service.VaRCalculationService;
import com.stockquest.application.service.NotificationService;
import com.stockquest.application.service.RiskConfigurationService;
import com.stockquest.domain.risk.RiskAlert;
import com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel;
import com.stockquest.domain.risk.VaRCalculation;
import com.stockquest.domain.risk.RiskBudget;
import com.stockquest.domain.risk.StressTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 리스크 관리 REST API 컨트롤러
 * Phase 8.3: Advanced Risk Management - REST API Layer
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskManagementController {

    private final VaRCalculationService varCalculationService;
    private final RiskAlertService riskAlertService;
    private final NotificationService notificationService;
    private final RiskConfigurationService riskConfigurationService;

    /**
     * 포트폴리오 VaR 계산 실행
     */
    @PostMapping("/portfolios/{portfolioId}/var")
    public CompletableFuture<ResponseEntity<VaRCalculationResponse>> calculatePortfolioVaR(
            @PathVariable String portfolioId,
            @Valid @RequestBody VaRCalculationRequest request) {

        log.info("VaR 계산 요청: portfolioId={}, method={}, confidenceLevel={}", 
            portfolioId, request.getMethod(), request.getConfidenceLevel());

        VaRCalculation.VaRParameters parameters = VaRCalculation.VaRParameters.builder()
            .historicalPeriod(request.getHistoricalPeriod())
            .confidenceLevel(request.getConfidenceLevel())
            .holdingPeriod(request.getHoldingPeriod())
            .numberOfSimulations(request.getNumberOfSimulations())
            .volatilityModel(request.getVolatilityModel())
            .correlationModel(request.getCorrelationModel())
            .distributionAssumption(request.getDistributionAssumption())
            .build();

        return varCalculationService.calculatePortfolioVaR(portfolioId, request.getMethod(), parameters)
            .thenApply(varCalculation -> {
                VaRCalculationResponse response = VaRCalculationResponse.from(varCalculation);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                log.error("VaR 계산 실패: portfolioId={}", portfolioId, throwable);
                return ResponseEntity.internalServerError().build();
            });
    }

    /**
     * VaR 계산 이력 조회
     */
    @GetMapping("/portfolios/{portfolioId}/var/history")
    public ResponseEntity<Page<VaRCalculationResponse>> getVaRHistory(
            @PathVariable String portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) VaRCalculation.VaRMethod method) {

        log.info("VaR 계산 이력 조회: portfolioId={}, page={}, size={}", portfolioId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        
        // 실제 구현에서는 데이터베이스에서 조회
        List<VaRCalculation> calculations = List.of(); // 빈 리스트로 시뮬레이션
        List<VaRCalculationResponse> responseList = calculations.stream()
            .map(VaRCalculationResponse::from)
            .toList();

        Page<VaRCalculationResponse> responsePage = new PageImpl<>(responseList, pageable, responseList.size());
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 리스크 알림 목록 조회
     */
    @GetMapping("/alerts")
    public ResponseEntity<Page<RiskAlertResponse>> getRiskAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String portfolioId,
            @RequestParam(required = false) RiskAlert.AlertSeverity severity,
            @RequestParam(required = false) RiskAlert.AlertStatus status) {

        log.info("리스크 알림 조회: page={}, size={}, portfolioId={}, severity={}", 
            page, size, portfolioId, severity);

        Pageable pageable = PageRequest.of(page, size);
        
        // 실제 구현에서는 데이터베이스에서 조회
        List<RiskAlert> alerts = List.of(); // 빈 리스트로 시뮬레이션
        List<RiskAlertResponse> responseList = alerts.stream()
            .map(RiskAlertResponse::from)
            .toList();

        Page<RiskAlertResponse> responsePage = new PageImpl<>(responseList, pageable, responseList.size());
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 특정 리스크 알림 조회
     */
    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<RiskAlertResponse> getRiskAlert(@PathVariable String alertId) {
        log.info("리스크 알림 상세 조회: alertId={}", alertId);

        // 실제 구현에서는 데이터베이스에서 조회
        return ResponseEntity.notFound().build();
    }

    /**
     * 리스크 알림 상태 업데이트
     */
    @PatchMapping("/alerts/{alertId}/status")
    public ResponseEntity<RiskAlertResponse> updateAlertStatus(
            @PathVariable String alertId,
            @Valid @RequestBody AlertStatusUpdateRequest request) {

        log.info("리스크 알림 상태 업데이트: alertId={}, status={}", alertId, request.getStatus());

        // 실제 구현에서는 서비스 레이어에서 처리
        return ResponseEntity.notFound().build();
    }

    /**
     * 알림 재전송
     */
    @PostMapping("/alerts/{alertId}/resend")
    public CompletableFuture<ResponseEntity<NotificationResponse>> resendAlert(
            @PathVariable String alertId,
            @Valid @RequestBody ResendNotificationRequest request) {

        log.info("알림 재전송: alertId={}, channels={}", alertId, request.getChannels());

        // 실제 구현에서는 알림 서비스에서 재전송 처리
        return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
    }

    /**
     * 리스크 대시보드 데이터 조회
     */
    @GetMapping("/dashboard")
    public ResponseEntity<RiskDashboardResponse> getRiskDashboard(
            @RequestParam(required = false) String portfolioId,
            @RequestParam(defaultValue = "7") int days) {

        log.info("리스크 대시보드 데이터 조회: portfolioId={}, days={}", portfolioId, days);

        RiskDashboardResponse response = RiskDashboardResponse.builder()
            .totalPortfolios(5)
            .activeAlerts(2)
            .totalVaRCalculations(150)
            .averageVaR(BigDecimal.valueOf(5.2))
            .riskTrend("STABLE")
            .lastUpdated(LocalDateTime.now())
            .portfolioRiskSummary(List.of(
                PortfolioRiskSummary.builder()
                    .portfolioId("1")
                    .portfolioName("Growth Portfolio")
                    .currentVaR(BigDecimal.valueOf(4.8))
                    .varLimit(BigDecimal.valueOf(8.0))
                    .utilizationRate(BigDecimal.valueOf(60.0))
                    .riskLevel("MODERATE")
                    .lastCalculation(LocalDateTime.now().minusHours(1))
                    .build()
            ))
            .recentAlerts(List.of())
            .systemStatus(Map.of(
                "varCalculationEngine", "OPERATIONAL",
                "alertSystem", "OPERATIONAL", 
                "notificationService", "OPERATIONAL"
            ))
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 스트레스 테스트 실행
     */
    @PostMapping("/portfolios/{portfolioId}/stress-test")
    public CompletableFuture<ResponseEntity<StressTestResponse>> runStressTest(
            @PathVariable String portfolioId,
            @Valid @RequestBody StressTestRequest request) {

        log.info("스트레스 테스트 실행: portfolioId={}, type={}", portfolioId, request.getTestType());

        // 실제 구현에서는 스트레스 테스트 서비스에서 처리
        return CompletableFuture.supplyAsync(() -> {
            StressTestResponse response = StressTestResponse.builder()
                .testId("stress_" + System.currentTimeMillis())
                .portfolioId(portfolioId)
                .testType(request.getTestType())
                .status("COMPLETED")
                .startTime(LocalDateTime.now().minusMinutes(5))
                .endTime(LocalDateTime.now())
                .results(Map.of(
                    "baselineValue", BigDecimal.valueOf(1000000),
                    "stressedValue", BigDecimal.valueOf(850000),
                    "loss", BigDecimal.valueOf(150000),
                    "lossPercentage", BigDecimal.valueOf(15.0)
                ))
                .build();

            return ResponseEntity.ok(response);
        });
    }

    /**
     * 리스크 예산 설정 조회
     */
    @GetMapping("/portfolios/{portfolioId}/risk-budget")
    public ResponseEntity<RiskBudgetResponse> getRiskBudget(@PathVariable String portfolioId) {
        log.info("리스크 예산 조회: portfolioId={}", portfolioId);

        RiskBudgetResponse response = RiskBudgetResponse.builder()
            .portfolioId(portfolioId)
            .budgetType(RiskBudget.BudgetType.VAR_BASED)
            .totalBudget(BigDecimal.valueOf(10.0))
            .usedBudget(BigDecimal.valueOf(6.5))
            .availableBudget(BigDecimal.valueOf(3.5))
            .utilizationRate(BigDecimal.valueOf(65.0))
            .allocationByAsset(Map.of(
                "AAPL", BigDecimal.valueOf(2.1),
                "GOOGL", BigDecimal.valueOf(1.8),
                "MSFT", BigDecimal.valueOf(2.6)
            ))
            .budgetStatus("WITHIN_LIMIT")
            .nextReview(LocalDateTime.now().plusDays(30))
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 리스크 예산 업데이트
     */
    @PutMapping("/portfolios/{portfolioId}/risk-budget")
    public ResponseEntity<RiskBudgetResponse> updateRiskBudget(
            @PathVariable String portfolioId,
            @Valid @RequestBody RiskBudgetUpdateRequest request) {

        log.info("리스크 예산 업데이트: portfolioId={}, totalBudget={}", 
            portfolioId, request.getTotalBudget());

        // 실제 구현에서는 리스크 설정 서비스에서 처리
        return ResponseEntity.notFound().build();
    }

    /**
     * 알림 설정 조회
     */
    @GetMapping("/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings(
            @RequestParam(required = false) String portfolioId) {

        log.info("알림 설정 조회: portfolioId={}", portfolioId);

        NotificationSettingsResponse response = NotificationSettingsResponse.builder()
            .portfolioId(portfolioId)
            .enabledChannels(List.of(
                NotificationChannel.EMAIL,
                NotificationChannel.SLACK,
                NotificationChannel.IN_APP
            ))
            .severityThresholds(Map.of(
                RiskAlert.AlertSeverity.CRITICAL, true,
                RiskAlert.AlertSeverity.HIGH, true,
                RiskAlert.AlertSeverity.MEDIUM, false,
                RiskAlert.AlertSeverity.LOW, false
            ))
            .quietHours(Map.of(
                "enabled", true,
                "startTime", "22:00",
                "endTime", "08:00"
            ))
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 알림 설정 업데이트
     */
    @PutMapping("/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsUpdateRequest request) {

        log.info("알림 설정 업데이트: portfolioId={}", request.getPortfolioId());

        // 실제 구현에서는 알림 설정 서비스에서 처리
        return ResponseEntity.notFound().build();
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class VaRCalculationRequest {
        @NotNull
        private VaRCalculation.VaRMethod method;
        
        @NotNull
        private BigDecimal confidenceLevel;
        
        @NotNull
        private Integer holdingPeriod;
        
        @lombok.Builder.Default
        private Integer historicalPeriod = 252;
        
        @lombok.Builder.Default
        private Integer numberOfSimulations = 10000;
        
        @lombok.Builder.Default
        private VaRCalculation.VolatilityModel volatilityModel = VaRCalculation.VolatilityModel.HISTORICAL;
        
        @lombok.Builder.Default
        private VaRCalculation.CorrelationModel correlationModel = VaRCalculation.CorrelationModel.HISTORICAL;
        
        @lombok.Builder.Default
        private VaRCalculation.DistributionAssumption distributionAssumption = VaRCalculation.DistributionAssumption.NORMAL;
    }

    @lombok.Data
    @lombok.Builder
    public static class VaRCalculationResponse {
        private String calculationId;
        private String portfolioId;
        private String method;
        private String confidenceLevel;
        private String varValue;
        private String varPercentage;
        private String expectedShortfall;
        private String calculationTime;
        private String riskLevel;
        private String qualityScore;
        private boolean isValid;

        public static VaRCalculationResponse from(VaRCalculation calculation) {
            return VaRCalculationResponse.builder()
                .calculationId(calculation.getCalculationId())
                .portfolioId(calculation.getPortfolioId())
                .method(calculation.getMethod().name())
                .confidenceLevel(calculation.getConfidenceLevel().toString())
                .varValue(calculation.getVarValue().toString())
                .varPercentage(calculation.getVarPercentage().toString())
                .expectedShortfall(calculation.getExpectedShortfall() != null ? 
                    calculation.getExpectedShortfall().toString() : null)
                .calculationTime(calculation.getCalculationTime().toString())
                .riskLevel(calculation.assessRiskLevel().name())
                .qualityScore(calculation.getQualityMetrics() != null ? 
                    calculation.getQualityMetrics().getAccuracyScore().toString() : "N/A")
                .isValid(calculation.isValid())
                .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class RiskAlertResponse {
        private String alertId;
        private String portfolioId;
        private String alertType;
        private String severity;
        private String status;
        private String title;
        private String message;
        private String triggerTime;
        private String acknowledgedAt;
        private String resolvedAt;
        private List<String> recommendedActions;

        public static RiskAlertResponse from(RiskAlert alert) {
            return RiskAlertResponse.builder()
                .alertId(alert.getAlertId())
                .portfolioId(alert.getPortfolioId())
                .alertType(alert.getAlertType().name())
                .severity(alert.getSeverity().name())
                .status(alert.getStatus().name())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .triggerTime(alert.getCreatedAt().toString())
                .acknowledgedAt(alert.getResolvedAt() != null ? 
                    alert.getResolvedAt().toString() : null)
                .resolvedAt(alert.getResolvedAt() != null ? 
                    alert.getResolvedAt().toString() : null)
                .recommendedActions(alert.getRecommendedActions().stream()
                    .map(action -> action.getDescription())
                    .toList())
                .build();
        }
    }

    @lombok.Data
    public static class AlertStatusUpdateRequest {
        @NotNull
        private RiskAlert.AlertStatus status;
        private String comment;
    }

    @lombok.Data
    public static class ResendNotificationRequest {
        @NotNull
        private List<NotificationChannel> channels;
        private String customMessage;
    }

    @lombok.Data
    @lombok.Builder
    public static class NotificationResponse {
        private String notificationId;
        private String status;
        private String message;
        private LocalDateTime sentAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class RiskDashboardResponse {
        private Integer totalPortfolios;
        private Integer activeAlerts;
        private Integer totalVaRCalculations;
        private BigDecimal averageVaR;
        private String riskTrend;
        private LocalDateTime lastUpdated;
        private List<PortfolioRiskSummary> portfolioRiskSummary;
        private List<RiskAlertResponse> recentAlerts;
        private Map<String, String> systemStatus;
    }

    @lombok.Data
    @lombok.Builder
    public static class PortfolioRiskSummary {
        private String portfolioId;
        private String portfolioName;
        private BigDecimal currentVaR;
        private BigDecimal varLimit;
        private BigDecimal utilizationRate;
        private String riskLevel;
        private LocalDateTime lastCalculation;
    }

    @lombok.Data
    @lombok.Builder
    public static class StressTestRequest {
        @NotNull
        private StressTest.StressTestType testType;
        private Map<String, Object> scenarioParameters;
        private BigDecimal shockSize;
        private Integer timeHorizon;
    }

    @lombok.Data
    @lombok.Builder
    public static class StressTestResponse {
        private String testId;
        private String portfolioId;
        private StressTest.StressTestType testType;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, BigDecimal> results;
    }

    @lombok.Data
    @lombok.Builder
    public static class RiskBudgetResponse {
        private String portfolioId;
        private RiskBudget.BudgetType budgetType;
        private BigDecimal totalBudget;
        private BigDecimal usedBudget;
        private BigDecimal availableBudget;
        private BigDecimal utilizationRate;
        private Map<String, BigDecimal> allocationByAsset;
        private String budgetStatus;
        private LocalDateTime nextReview;
    }

    @lombok.Data
    public static class RiskBudgetUpdateRequest {
        @NotNull
        private RiskBudget.BudgetType budgetType;
        @NotNull
        private BigDecimal totalBudget;
        private Map<String, BigDecimal> assetLimits;
    }

    @lombok.Data
    @lombok.Builder
    public static class NotificationSettingsResponse {
        private String portfolioId;
        private List<NotificationChannel> enabledChannels;
        private Map<RiskAlert.AlertSeverity, Boolean> severityThresholds;
        private Map<String, Object> quietHours;
    }

    @lombok.Data
    public static class NotificationSettingsUpdateRequest {
        private String portfolioId;
        private List<NotificationChannel> enabledChannels;
        private Map<RiskAlert.AlertSeverity, Boolean> severityThresholds;
        private Map<String, Object> quietHours;
    }
}