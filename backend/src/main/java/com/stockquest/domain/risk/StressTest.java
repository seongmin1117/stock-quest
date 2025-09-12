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
 * 스트레스 테스트 도메인 모델
 * Phase 8.3: Advanced Risk Management - 스트레스 테스트 및 시나리오 분석 시스템
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StressTest {
    
    /**
     * 스트레스 테스트 고유 ID
     */
    private String testId;
    
    /**
     * 포트폴리오 ID
     */
    private String portfolioId;
    
    /**
     * 테스트 이름
     */
    private String testName;
    
    /**
     * 테스트 설명
     */
    private String description;
    
    /**
     * 테스트 타입
     */
    private StressTestType testType;
    
    /**
     * 테스트 상태
     */
    private TestStatus status;
    
    /**
     * 시나리오 목록
     */
    private List<StressScenario> scenarios;
    
    /**
     * 테스트 결과
     */
    private StressTestResult result;
    
    /**
     * 테스트 설정
     */
    private StressTestConfiguration configuration;
    
    /**
     * 시장 충격 요소
     */
    private List<MarketShock> marketShocks;
    
    /**
     * 테스트 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 테스트 시작 시간
     */
    private LocalDateTime startedAt;
    
    /**
     * 테스트 완료 시간
     */
    private LocalDateTime completedAt;
    
    /**
     * 다음 실행 예정 시간
     */
    private LocalDateTime nextScheduledRun;
    
    /**
     * 테스트 실행자
     */
    private String executedBy;
    
    /**
     * 테스트 우선순위
     */
    private TestPriority priority;
    
    /**
     * 규제 요구사항 충족 여부
     */
    @Builder.Default
    private Boolean regulatoryCompliant = false;
    
    /**
     * 테스트 메타데이터
     */
    private Map<String, Object> metadata;
    
    public enum StressTestType {
        REGULATORY("규제 스트레스 테스트", "Basel III, CCAR 등 규제 요구사항"),
        HISTORICAL("역사적 시나리오", "과거 금융위기 시나리오 재현"),
        HYPOTHETICAL("가상 시나리오", "가정적 극한 상황 시나리오"),
        TAIL_RISK("꼬리 위험", "극단적 손실 시나리오"),
        CONCENTRATION("집중도 테스트", "특정 자산/섹터 집중 위험"),
        LIQUIDITY("유동성 테스트", "유동성 위기 시나리오"),
        CORRELATION("상관관계 붕괴", "자산 간 상관관계 급변"),
        OPERATIONAL("운영 리스크", "운영상 문제 발생 시나리오"),
        CREDIT("신용 리스크", "신용 스프레드 확대"),
        MARKET("시장 리스크", "시장 급락/급등"),
        INTEREST_RATE("금리 리스크", "금리 급변동"),
        CURRENCY("환율 리스크", "환율 급변동"),
        REVERSE("역 스트레스", "긍정적 시나리오");
        
        private final String description;
        private final String details;
        
        StressTestType(String description, String details) {
            this.description = description;
            this.details = details;
        }
        
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public enum TestStatus {
        PENDING("대기중", "테스트 실행 대기"),
        RUNNING("실행중", "테스트 진행중"),
        COMPLETED("완료", "테스트 성공적으로 완료"),
        FAILED("실패", "테스트 실행 실패"),
        CANCELLED("취소됨", "테스트가 취소됨"),
        SCHEDULED("예약됨", "향후 실행 예약"),
        PAUSED("일시정지", "테스트 일시 정지");
        
        private final String description;
        private final String details;
        
        TestStatus(String description, String details) {
            this.description = description;
            this.details = details;
        }
        
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public enum TestPriority {
        CRITICAL("치명적", 1),
        HIGH("높음", 2),
        MEDIUM("보통", 3),
        LOW("낮음", 4);
        
        private final String description;
        private final int level;
        
        TestPriority(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() { return description; }
        public int getLevel() { return level; }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressScenario {
        
        /**
         * 시나리오 ID
         */
        private String scenarioId;
        
        /**
         * 시나리오 이름
         */
        private String name;
        
        /**
         * 시나리오 설명
         */
        private String description;
        
        /**
         * 시나리오 타입
         */
        private ScenarioType scenarioType;
        
        /**
         * 발생 확률 (%)
         */
        private BigDecimal probability;
        
        /**
         * 시나리오 심각도
         */
        private ScenarioSeverity severity;
        
        /**
         * 시장 요소별 충격
         */
        private List<FactorShock> factorShocks;
        
        /**
         * 시나리오 기간 (일)
         */
        private Integer durationDays;
        
        /**
         * 충격 적용 순서
         */
        private ShockSequence shockSequence;
        
        /**
         * 회복 시나리오 포함 여부
         */
        @Builder.Default
        private Boolean includeRecovery = false;
        
        /**
         * 회복 기간 (일)
         */
        private Integer recoveryDays;
        
        /**
         * 시나리오 결과
         */
        private ScenarioResult result;
        
        public enum ScenarioType {
            BLACK_SWAN("블랙 스완", "극도로 드물고 예측 불가능한 사건"),
            FINANCIAL_CRISIS("금융위기", "2008년 금융위기 수준"),
            RECESSION("경기침체", "경제 침체 시나리오"),
            MARKET_CRASH("시장 폭락", "주식시장 급락"),
            PANDEMIC("팬데믹", "코로나19 수준 팬데믹"),
            GEOPOLITICAL("지정학적", "전쟁, 테러 등 지정학적 위험"),
            NATURAL_DISASTER("자연재해", "대규모 자연재해"),
            CYBER_ATTACK("사이버 공격", "시스템 마비 수준 사이버 공격"),
            CURRENCY_CRISIS("통화위기", "아시아 외환위기 수준"),
            COMMODITY_SHOCK("원자재 충격", "유가 폭등/폭락 등"),
            INTEREST_RATE_SHOCK("금리 충격", "급격한 금리 변동"),
            INFLATION_SPIKE("인플레이션 급등", "인플레이션 급상승"),
            DEFLATION("디플레이션", "경제 전반적 물가하락"),
            CREDIT_CRUNCH("신용경색", "대출 급격한 축소"),
            REGULATORY_CHANGE("규제 변화", "갑작스러운 규제 강화");
            
            private final String description;
            private final String details;
            
            ScenarioType(String description, String details) {
                this.description = description;
                this.details = details;
            }
            
            public String getDescription() { return description; }
            public String getDetails() { return details; }
        }
        
        public enum ScenarioSeverity {
            MILD("경미", 1.0, 2.0),
            MODERATE("보통", 2.0, 3.5),
            SEVERE("심각", 3.5, 5.0),
            EXTREME("극심", 5.0, 10.0),
            CATASTROPHIC("파멸적", 10.0, Double.MAX_VALUE);
            
            private final String description;
            private final double minSigma;
            private final double maxSigma;
            
            ScenarioSeverity(String description, double minSigma, double maxSigma) {
                this.description = description;
                this.minSigma = minSigma;
                this.maxSigma = maxSigma;
            }
            
            public String getDescription() { return description; }
            public double getMinSigma() { return minSigma; }
            public double getMaxSigma() { return maxSigma; }
        }
        
        public enum ShockSequence {
            SIMULTANEOUS("동시", "모든 충격 동시 적용"),
            SEQUENTIAL("순차", "충격 순차적 적용"),
            CASCADING("연쇄", "연쇄적 충격 전파"),
            RANDOM("무작위", "무작위 순서 적용");
            
            private final String description;
            private final String details;
            
            ShockSequence(String description, String details) {
                this.description = description;
                this.details = details;
            }
            
            public String getDescription() { return description; }
            public String getDetails() { return details; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactorShock {
        
        /**
         * 충격 요소 이름
         */
        private String factorName;
        
        /**
         * 충격 크기 (%)
         */
        private BigDecimal shockSize;
        
        /**
         * 충격 방향
         */
        private ShockDirection direction;
        
        /**
         * 적용 대상
         */
        private List<String> targets;
        
        /**
         * 충격 분포 타입
         */
        private ShockDistribution distribution;
        
        /**
         * 지속 기간 (일)
         */
        private Integer durationDays;
        
        /**
         * 회복률 (일별 %)
         */
        private BigDecimal recoveryRate;
        
        public enum ShockDirection {
            POSITIVE("상승", "양의 충격"),
            NEGATIVE("하락", "음의 충격"),
            BIDIRECTIONAL("양방향", "양/음 모두 가능");
            
            private final String description;
            private final String details;
            
            ShockDirection(String description, String details) {
                this.description = description;
                this.details = details;
            }
            
            public String getDescription() { return description; }
            public String getDetails() { return details; }
        }
        
        public enum ShockDistribution {
            UNIFORM("균등분포"),
            NORMAL("정규분포"),
            EXPONENTIAL("지수분포"),
            EXTREME_VALUE("극값분포"),
            CUSTOM("사용자정의");
            
            private final String description;
            
            ShockDistribution(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketShock {
        
        /**
         * 시장 요소
         */
        private MarketFactor marketFactor;
        
        /**
         * 기준값
         */
        private BigDecimal baselineValue;
        
        /**
         * 스트레스 값
         */
        private BigDecimal stressValue;
        
        /**
         * 충격 크기
         */
        private BigDecimal shockMagnitude;
        
        /**
         * 영향 강도
         */
        private ImpactIntensity intensity;
        
        /**
         * 영향받는 자산 클래스
         */
        private List<AssetClass> affectedAssetClasses;
        
        public enum MarketFactor {
            EQUITY_PRICE("주가지수"),
            INTEREST_RATE("금리"),
            CREDIT_SPREAD("신용스프레드"),
            EXCHANGE_RATE("환율"),
            COMMODITY_PRICE("원자재 가격"),
            VOLATILITY("변동성"),
            CORRELATION("상관관계"),
            LIQUIDITY("유동성"),
            INFLATION("인플레이션"),
            GDP_GROWTH("GDP 성장률"),
            UNEMPLOYMENT("실업률"),
            HOUSING_PRICE("주택가격");
            
            private final String description;
            
            MarketFactor(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        public enum ImpactIntensity {
            LOW("낮음", 0.5),
            MODERATE("보통", 1.0),
            HIGH("높음", 1.5),
            EXTREME("극심", 2.0);
            
            private final String description;
            private final double multiplier;
            
            ImpactIntensity(String description, double multiplier) {
                this.description = description;
                this.multiplier = multiplier;
            }
            
            public String getDescription() { return description; }
            public double getMultiplier() { return multiplier; }
        }
        
        public enum AssetClass {
            EQUITY("주식"),
            BOND("채권"),
            COMMODITY("원자재"),
            REAL_ESTATE("부동산"),
            CURRENCY("통화"),
            ALTERNATIVE("대체투자");
            
            private final String description;
            
            AssetClass(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestResult {
        
        /**
         * 전체 테스트 통과 여부
         */
        private Boolean testPassed;
        
        /**
         * 총 손실 금액
         */
        private BigDecimal totalLoss;
        
        /**
         * 최대 손실 금액
         */
        private BigDecimal maxLoss;
        
        /**
         * 손실 비율 (%)
         */
        private BigDecimal lossPercentage;
        
        /**
         * 시나리오별 결과
         */
        private List<ScenarioResult> scenarioResults;
        
        /**
         * 생존 확률 (%)
         */
        private BigDecimal survivalProbability;
        
        /**
         * 자본 적정성
         */
        private CapitalAdequacy capitalAdequacy;
        
        /**
         * 리스크 메트릭
         */
        private StressTestRiskMetrics riskMetrics;
        
        /**
         * 권장사항
         */
        private List<StressTestRecommendation> recommendations;
        
        /**
         * 테스트 실행 통계
         */
        private TestExecutionStats executionStats;
        
        /**
         * 규제 준수 상태
         */
        private RegulatoryCompliance regulatoryCompliance;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenarioResult {
        
        /**
         * 시나리오 ID
         */
        private String scenarioId;
        
        /**
         * 시나리오 통과 여부
         */
        private Boolean scenarioPassed;
        
        /**
         * 손실 금액
         */
        private BigDecimal lossAmount;
        
        /**
         * 손실 비율 (%)
         */
        private BigDecimal lossPercentage;
        
        /**
         * 최대 낙폭
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 회복 소요 시간 (일)
         */
        private Integer recoveryDays;
        
        /**
         * VaR 위반 횟수
         */
        private Integer varBreaches;
        
        /**
         * 유동성 위기 기간 (일)
         */
        private Integer liquidityCrisisDays;
        
        /**
         * 자산별 영향도
         */
        private Map<String, BigDecimal> assetImpacts;
        
        /**
         * 시간대별 손익
         */
        private List<DailyPnL> dailyPnLs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPnL {
        private LocalDateTime date;
        private BigDecimal portfolioValue;
        private BigDecimal dailyReturn;
        private BigDecimal cumulativeReturn;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapitalAdequacy {
        
        /**
         * 현재 자본비율 (%)
         */
        private BigDecimal currentCapitalRatio;
        
        /**
         * 최소 필요 자본비율 (%)
         */
        private BigDecimal minimumCapitalRatio;
        
        /**
         * 자본 충분성
         */
        private CapitalSufficiency sufficiency;
        
        /**
         * 추가 필요 자본
         */
        private BigDecimal additionalCapitalRequired;
        
        public enum CapitalSufficiency {
            ADEQUATE("충분", "자본이 충분함"),
            MARGINAL("여유있음", "자본이 여유있음"),
            INSUFFICIENT("부족", "자본이 부족함"),
            CRITICAL("심각", "자본 부족 심각");
            
            private final String description;
            private final String details;
            
            CapitalSufficiency(String description, String details) {
                this.description = description;
                this.details = details;
            }
            
            public String getDescription() { return description; }
            public String getDetails() { return details; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestRiskMetrics {
        
        /**
         * 스트레스 VaR
         */
        private BigDecimal stressVar;
        
        /**
         * 스트레스 CVaR
         */
        private BigDecimal stressCVar;
        
        /**
         * 최대 예상 손실
         */
        private BigDecimal expectedShortfall;
        
        /**
         * 손실 분산
         */
        private BigDecimal lossVariance;
        
        /**
         * 집중도 지수
         */
        private BigDecimal concentrationIndex;
        
        /**
         * 상관관계 붕괴 영향
         */
        private BigDecimal correlationBreakdownImpact;
        
        /**
         * 유동성 조정 VaR
         */
        private BigDecimal liquidityAdjustedVar;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestRecommendation {
        
        /**
         * 권장사항 타입
         */
        private RecommendationType recommendationType;
        
        /**
         * 권장사항 제목
         */
        private String title;
        
        /**
         * 권장사항 설명
         */
        private String description;
        
        /**
         * 우선순위
         */
        private Integer priority;
        
        /**
         * 예상 효과
         */
        private String expectedImpact;
        
        /**
         * 구현 복잡도
         */
        private ImplementationComplexity complexity;
        
        public enum RecommendationType {
            IMMEDIATE_ACTION("즉시 조치"),
            PORTFOLIO_ADJUSTMENT("포트폴리오 조정"),
            RISK_LIMIT_CHANGE("위험 한도 변경"),
            HEDGING_STRATEGY("헤지 전략"),
            CAPITAL_INCREASE("자본 증액"),
            DIVERSIFICATION("분산화 강화"),
            STRESS_TEST_FREQUENCY("테스트 빈도 조정"),
            MONITORING_ENHANCEMENT("모니터링 강화");
            
            private final String description;
            
            RecommendationType(String description) {
                this.description = description;
            }
            
            public String getDescription() { return description; }
        }
        
        public enum ImplementationComplexity {
            SIMPLE("간단", 1),
            MODERATE("보통", 2),
            COMPLEX("복잡", 3),
            VERY_COMPLEX("매우 복잡", 4);
            
            private final String description;
            private final int level;
            
            ImplementationComplexity(String description, int level) {
                this.description = description;
                this.level = level;
            }
            
            public String getDescription() { return description; }
            public int getLevel() { return level; }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestExecutionStats {
        
        /**
         * 실행 소요 시간 (밀리초)
         */
        private Long executionTimeMs;
        
        /**
         * 시뮬레이션 횟수
         */
        private Integer simulationCount;
        
        /**
         * 메모리 사용량 (MB)
         */
        private Long memoryUsageMb;
        
        /**
         * CPU 사용률 (%)
         */
        private BigDecimal cpuUsagePercent;
        
        /**
         * 처리된 데이터 포인트 수
         */
        private Long processedDataPoints;
        
        /**
         * 에러 발생 횟수
         */
        private Integer errorCount;
        
        /**
         * 경고 발생 횟수
         */
        private Integer warningCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegulatoryCompliance {
        
        /**
         * Basel III 준수 여부
         */
        @Builder.Default
        private Boolean baselIIICompliant = false;
        
        /**
         * CCAR 준수 여부
         */
        @Builder.Default
        private Boolean ccarCompliant = false;
        
        /**
         * 규제 점수
         */
        private BigDecimal regulatoryScore;
        
        /**
         * 미준수 항목 목록
         */
        private List<String> nonComplianceItems;
        
        /**
         * 규제 권장사항
         */
        private List<String> regulatoryRecommendations;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestConfiguration {
        
        /**
         * 시뮬레이션 횟수
         */
        @Builder.Default
        private Integer simulationCount = 10000;
        
        /**
         * 신뢰구간 수준
         */
        @Builder.Default
        private BigDecimal confidenceLevel = new BigDecimal("0.99");
        
        /**
         * 시간 지평 (일)
         */
        @Builder.Default
        private Integer timeHorizonDays = 250;
        
        /**
         * 병렬 처리 활성화
         */
        @Builder.Default
        private Boolean parallelProcessingEnabled = true;
        
        /**
         * 최대 스레드 수
         */
        @Builder.Default
        private Integer maxThreads = 8;
        
        /**
         * 결과 저장 정밀도
         */
        @Builder.Default
        private Integer resultPrecision = 6;
        
        /**
         * 중간 결과 저장 간격
         */
        @Builder.Default
        private Integer saveIntervalMinutes = 5;
        
        /**
         * 자동 보고서 생성
         */
        @Builder.Default
        private Boolean autoReportGeneration = true;
        
        /**
         * 알림 설정
         */
        private NotificationSettings notificationSettings;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NotificationSettings {
            
            @Builder.Default
            private Boolean emailNotification = true;
            
            @Builder.Default
            private Boolean slackNotification = false;
            
            private List<String> emailRecipients;
            
            private String slackChannel;
        }
    }
    
    /**
     * 테스트가 실행 중인지 확인
     */
    public boolean isRunning() {
        return status == TestStatus.RUNNING;
    }
    
    /**
     * 테스트가 완료되었는지 확인
     */
    public boolean isCompleted() {
        return status == TestStatus.COMPLETED;
    }
    
    /**
     * 규제 요구사항을 충족하는지 확인
     */
    public boolean meetsRegulatoryRequirements() {
        return regulatoryCompliant && 
               result != null && 
               result.getRegulatoryCompliance() != null &&
               result.getRegulatoryCompliance().getBaselIIICompliant();
    }
    
    /**
     * 테스트 실행 시간 계산 (밀리초)
     */
    public Long calculateExecutionTime() {
        if (startedAt == null || completedAt == null) {
            return null;
        }
        return java.time.Duration.between(startedAt, completedAt).toMillis();
    }
    
    /**
     * 전체 위험도 점수 계산
     */
    public BigDecimal calculateOverallRiskScore() {
        if (result == null || result.getScenarioResults().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalLoss = result.getScenarioResults().stream()
            .map(ScenarioResult::getLossPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int scenarioCount = result.getScenarioResults().size();
        return totalLoss.divide(new BigDecimal(scenarioCount), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 최악 시나리오 결과 반환
     */
    public ScenarioResult getWorstCaseScenario() {
        if (result == null || result.getScenarioResults().isEmpty()) {
            return null;
        }
        
        return result.getScenarioResults().stream()
            .max((s1, s2) -> s1.getLossPercentage().compareTo(s2.getLossPercentage()))
            .orElse(null);
    }
    
    /**
     * 스트레스 테스트 요약 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("스트레스 테스트: %s [%s]\n", testName, status.getDescription()));
        
        if (result != null) {
            summary.append(String.format("전체 통과 여부: %s\n", 
                result.getTestPassed() ? "통과" : "실패"));
            summary.append(String.format("최대 손실: %.2f%%\n", 
                result.getLossPercentage()));
            summary.append(String.format("시나리오 수: %d개\n", 
                result.getScenarioResults().size()));
        }
        
        return summary.toString();
    }
}