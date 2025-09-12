package com.stockquest.domain.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 리스크 알림 도메인 모델
 * Phase 8.3: Advanced Risk Management - 실시간 리스크 모니터링 및 알림 시스템
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlert {
    
    /**
     * 알림 고유 ID
     */
    private String alertId;
    
    /**
     * 포트폴리오 ID
     */
    private String portfolioId;
    
    /**
     * 알림 타입
     */
    private AlertType alertType;
    
    /**
     * 알림 심각도 수준
     */
    private AlertSeverity severity;
    
    /**
     * 알림 상태
     */
    private AlertStatus status;
    
    /**
     * 알림 제목
     */
    private String title;
    
    /**
     * 알림 메시지
     */
    private String message;
    
    /**
     * 상세 설명
     */
    private String description;
    
    /**
     * 현재 위험 값
     */
    private BigDecimal currentValue;
    
    /**
     * 임계값
     */
    private BigDecimal threshold;
    
    /**
     * 임계값 초과 비율 (%)
     */
    private BigDecimal exceedancePercentage;
    
    /**
     * 위험 메트릭 타입
     */
    private RiskMetricType metricType;
    
    /**
     * 영향받는 자산 목록
     */
    private List<String> affectedAssets;
    
    /**
     * 알림 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 해결 시간
     */
    private LocalDateTime resolvedAt;
    
    /**
     * 알림 만료 시간
     */
    private LocalDateTime expiresAt;
    
    /**
     * 권장 조치사항
     */
    private List<RecommendedAction> recommendedActions;
    
    /**
     * 알림 설정 정보
     */
    private AlertConfiguration configuration;
    
    /**
     * 히스토리 정보
     */
    private AlertHistory history;
    
    /**
     * 관련 계산 정보
     */
    private RelatedCalculations relatedCalculations;
    
    /**
     * 알림 메타데이터
     */
    private Map<String, Object> metadata;
    
    public enum AlertType {
        VAR_BREACH("VaR 위반", "포트폴리오 VaR 한도 초과"),
        CONCENTRATION_RISK("집중도 위험", "포트폴리오 집중도 위험 증가"),
        CORRELATION_SPIKE("상관관계 급증", "자산 간 상관관계 급격한 증가"),
        LIQUIDITY_SHORTAGE("유동성 부족", "포트폴리오 유동성 부족"),
        DRAWDOWN_LIMIT("최대낙폭 한도", "최대 낙폭 한도 초과"),
        VOLATILITY_SURGE("변동성 급증", "포트폴리오 변동성 급격한 증가"),
        MARGIN_CALL("마진콜", "증거금 부족으로 인한 마진콜"),
        EXPOSURE_LIMIT("익스포저 한도", "단일 자산 또는 섹터 익스포저 한도 초과"),
        MODEL_DEGRADATION("모델 성능 저하", "ML 모델 성능 임계치 미달"),
        STRESS_TEST_FAILURE("스트레스 테스트 실패", "스트레스 테스트 시나리오 실패"),
        COMPLIANCE_VIOLATION("컴플라이언스 위반", "규제 준수 요건 위반"),
        TAIL_RISK("꼬리 위험", "극단적 시나리오 위험 증가");
        
        private final String title;
        private final String description;
        
        AlertType(String title, String description) {
            this.title = title;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
    
    public enum AlertSeverity {
        CRITICAL("치명적", 1, "#FF0000"),
        HIGH("높음", 2, "#FF6600"),
        MEDIUM("보통", 3, "#FFCC00"),
        LOW("낮음", 4, "#00CC00"),
        INFO("정보", 5, "#0066CC");
        
        private final String description;
        private final int priority;
        private final String colorCode;
        
        AlertSeverity(String description, int priority, String colorCode) {
            this.description = description;
            this.priority = priority;
            this.colorCode = colorCode;
        }
        
        public String getDescription() { return description; }
        public int getPriority() { return priority; }
        public String getColorCode() { return colorCode; }
    }
    
    public enum AlertStatus {
        ACTIVE("활성화", "알림이 활성 상태"),
        ACKNOWLEDGED("확인됨", "알림이 확인되었지만 아직 해결되지 않음"),
        RESOLVED("해결됨", "알림이 해결됨"),
        EXPIRED("만료됨", "알림이 만료됨"),
        SUPPRESSED("억제됨", "알림이 일시적으로 억제됨"),
        ESCALATED("에스컬레이션", "상위 관리자에게 에스컬레이션됨");
        
        private final String description;
        private final String details;
        
        AlertStatus(String description, String details) {
            this.description = description;
            this.details = details;
        }
        
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public enum RiskMetricType {
        VALUE_AT_RISK("Value at Risk"),
        CONDITIONAL_VAR("Conditional VaR"),
        VOLATILITY("변동성"),
        CORRELATION("상관관계"),
        CONCENTRATION("집중도"),
        LIQUIDITY("유동성"),
        LEVERAGE("레버리지"),
        TRACKING_ERROR("추적 오차"),
        INFORMATION_RATIO("정보 비율"),
        MAXIMUM_DRAWDOWN("최대 낙폭"),
        SHARPE_RATIO("샤프 비율"),
        BETA("베타"),
        TAIL_EXPECTATION("꼬리 기댓값");
        
        private final String description;
        
        RiskMetricType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedAction {
        
        /**
         * 조치 타입
         */
        private ActionType actionType;
        
        /**
         * 조치 제목
         */
        private String title;
        
        /**
         * 조치 상세 설명
         */
        private String description;
        
        /**
         * 우선순위 (1=최고)
         */
        private Integer priority;
        
        /**
         * 예상 효과
         */
        private String expectedImpact;
        
        /**
         * 실행 복잡도
         */
        private ComplexityLevel complexity;
        
        /**
         * 예상 소요 시간 (분)
         */
        private Integer estimatedTimeMinutes;
        
        /**
         * 필요한 권한 수준
         */
        private AuthorizationLevel authorizationRequired;
        
        /**
         * 관련 자산 또는 전략
         */
        private List<String> targetAssets;
        
        public enum ActionType {
            REDUCE_POSITION("포지션 축소"),
            HEDGE_EXPOSURE("헤지 실행"),
            REBALANCE_PORTFOLIO("포트폴리오 리밸런싱"),
            INCREASE_CASH("현금 비중 증가"),
            DIVERSIFY_HOLDINGS("분산화 개선"),
            ADJUST_STOP_LOSS("손절매 조정"),
            REVIEW_STRATEGY("전략 재검토"),
            CONTACT_MANAGER("매니저 연락"),
            EMERGENCY_EXIT("긴급 청산"),
            MONITOR_CLOSELY("면밀 모니터링"),
            UPDATE_LIMITS("한도 재설정"),
            STRESS_TEST_PORTFOLIO("포트폴리오 스트레스 테스트");
            
            private final String description;
            
            ActionType(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        public enum ComplexityLevel {
            SIMPLE("단순", 1),
            MODERATE("보통", 2),
            COMPLEX("복잡", 3),
            VERY_COMPLEX("매우 복잡", 4);
            
            private final String description;
            private final int level;
            
            ComplexityLevel(String description, int level) {
                this.description = description;
                this.level = level;
            }
            
            public String getDescription() { return description; }
            public int getLevel() { return level; }
        }
        
        public enum AuthorizationLevel {
            USER("사용자"),
            SUPERVISOR("감독자"),
            MANAGER("매니저"),
            SENIOR_MANAGER("시니어 매니저"),
            BOARD("이사회");
            
            private final String description;
            
            AuthorizationLevel(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertConfiguration {
        
        /**
         * 알림 규칙 ID
         */
        private String ruleId;
        
        /**
         * 알림 규칙 이름
         */
        private String ruleName;
        
        /**
         * 모니터링 빈도 (분)
         */
        private Integer monitoringFrequencyMinutes;
        
        /**
         * 알림 전송 채널
         */
        private List<NotificationChannel> notificationChannels;
        
        /**
         * 에스컬레이션 규칙
         */
        private EscalationRule escalationRule;
        
        /**
         * 억제 규칙
         */
        private SuppressionRule suppressionRule;
        
        /**
         * 자동 해결 활성화
         */
        @Builder.Default
        private Boolean autoResolveEnabled = false;
        
        /**
         * 자동 해결 조건
         */
        private AutoResolveCondition autoResolveCondition;
        
        public enum NotificationChannel {
            EMAIL("이메일"),
            SMS("SMS"),
            SLACK("슬랙"),
            TEAMS("팀즈"),
            WEBHOOK("웹훅"),
            IN_APP("앱 내 알림"),
            MOBILE_PUSH("모바일 푸시"),
            DASHBOARD("대시보드");
            
            private final String description;
            
            NotificationChannel(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EscalationRule {
            private Integer escalationDelayMinutes;
            private List<String> escalationRecipients;
            private AlertSeverity escalationSeverity;
            private Integer maxEscalationLevel;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SuppressionRule {
            private Integer suppressionDurationMinutes;
            private List<AlertType> suppressedAlertTypes;
            private BigDecimal suppressionThreshold;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AutoResolveCondition {
            private BigDecimal resolveThreshold;
            private Integer confirmationPeriodMinutes;
            private Boolean requiresManualConfirmation;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertHistory {
        
        /**
         * 이전 발생 횟수
         */
        private Integer previousOccurrences;
        
        /**
         * 마지막 발생 시간
         */
        private LocalDateTime lastOccurrence;
        
        /**
         * 평균 해결 시간 (분)
         */
        private Integer averageResolutionMinutes;
        
        /**
         * 발생 빈도 (일간)
         */
        private Double dailyFrequency;
        
        /**
         * 트렌드 방향
         */
        private TrendDirection trendDirection;
        
        /**
         * 관련 이벤트 히스토리
         */
        private List<RelatedEvent> relatedEvents;
        
        public enum TrendDirection {
            INCREASING("증가"),
            STABLE("안정"),
            DECREASING("감소");
            
            private final String description;
            
            TrendDirection(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RelatedEvent {
            private LocalDateTime eventTime;
            private String eventType;
            private String description;
            private BigDecimal impact;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedCalculations {
        
        /**
         * VaR 계산 ID
         */
        private String varCalculationId;
        
        /**
         * 스트레스 테스트 ID
         */
        private String stressTestId;
        
        /**
         * 시나리오 분석 ID
         */
        private String scenarioAnalysisId;
        
        /**
         * 관련 백테스트 ID
         */
        private String backtestId;
        
        /**
         * ML 모델 예측 ID
         */
        private String mlPredictionId;
        
        /**
         * 계산 신뢰도
         */
        private BigDecimal calculationConfidence;
        
        /**
         * 데이터 품질 점수
         */
        private BigDecimal dataQualityScore;
        
        /**
         * 계산 방법론
         */
        private CalculationMethodology methodology;
        
        public enum CalculationMethodology {
            HISTORICAL_SIMULATION("역사적 시뮬레이션"),
            MONTE_CARLO("몬테카를로"),
            PARAMETRIC("모수적 방법"),
            EXTREME_VALUE_THEORY("극값 이론"),
            MACHINE_LEARNING("머신러닝");
            
            private final String description;
            
            CalculationMethodology(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    /**
     * 알림이 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == AlertStatus.ACTIVE && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * 알림이 치명적 수준인지 확인
     */
    public boolean isCritical() {
        return severity == AlertSeverity.CRITICAL;
    }
    
    /**
     * 에스컬레이션이 필요한지 확인
     */
    public boolean requiresEscalation() {
        if (configuration == null || configuration.getEscalationRule() == null) {
            return false;
        }
        
        LocalDateTime escalationTime = createdAt.plusMinutes(
            configuration.getEscalationRule().getEscalationDelayMinutes()
        );
        
        return status == AlertStatus.ACTIVE && 
               LocalDateTime.now().isAfter(escalationTime) &&
               severity.getPriority() <= 2; // CRITICAL or HIGH
    }
    
    /**
     * 임계값 초과 비율 계산
     */
    public BigDecimal calculateExceedancePercentage() {
        if (currentValue == null || threshold == null || 
            threshold.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return currentValue.subtract(threshold)
                .divide(threshold, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 알림 우선순위 점수 계산
     */
    public int calculatePriorityScore() {
        int score = severity.getPriority() * 10;
        
        // 임계값 초과 비율에 따른 가중치
        if (exceedancePercentage != null) {
            score += exceedancePercentage.intValue() / 10;
        }
        
        // 과거 발생 빈도에 따른 가중치
        if (history != null && history.getDailyFrequency() != null) {
            score += history.getDailyFrequency().intValue() * 5;
        }
        
        // 영향받는 자산 수에 따른 가중치
        if (affectedAssets != null) {
            score += affectedAssets.size() * 2;
        }
        
        return Math.max(score, 1); // 최소 1점
    }
    
    /**
     * 알림 요약 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("[%s] %s", severity.getDescription(), title));
        
        if (currentValue != null && threshold != null) {
            summary.append(String.format(" - 현재값: %s, 임계값: %s", 
                currentValue.toPlainString(), threshold.toPlainString()));
        }
        
        if (exceedancePercentage != null) {
            summary.append(String.format(" (%+.2f%%)", exceedancePercentage));
        }
        
        if (affectedAssets != null && !affectedAssets.isEmpty()) {
            summary.append(String.format(" [영향자산: %d개]", affectedAssets.size()));
        }
        
        return summary.toString();
    }
    
    /**
     * 다음 권장 조치사항 반환
     */
    public RecommendedAction getNextRecommendedAction() {
        if (recommendedActions == null || recommendedActions.isEmpty()) {
            return null;
        }
        
        return recommendedActions.stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .findFirst()
                .orElse(null);
    }
}