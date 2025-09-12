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
 * Value-at-Risk (VaR) 계산 결과 도메인 모델
 * Phase 8.3: Advanced Risk Management - VaR 계산 시스템
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaRCalculation {
    
    /**
     * VaR 계산 고유 ID
     */
    private String calculationId;
    
    /**
     * 포트폴리오 ID
     */
    private String portfolioId;
    
    /**
     * VaR 방법론
     */
    private VaRMethod method;
    
    /**
     * 신뢰도 수준 (예: 95%, 99%)
     */
    private BigDecimal confidenceLevel;
    
    /**
     * 보유 기간 (일)
     */
    private Integer holdingPeriod;
    
    /**
     * VaR 값 (절대값)
     */
    private BigDecimal varValue;
    
    /**
     * VaR 백분율 (포트폴리오 대비)
     */
    private BigDecimal varPercentage;
    
    /**
     * Expected Shortfall (CVaR)
     */
    private BigDecimal expectedShortfall;
    
    /**
     * 계산 시간
     */
    private LocalDateTime calculationTime;
    
    /**
     * 유효성 종료 시간
     */
    private LocalDateTime expirationTime;
    
    /**
     * 계산 품질 지표
     */
    private VaRQualityMetrics qualityMetrics;
    
    /**
     * 구성 요소별 기여도
     */
    private List<VaRComponent> components;
    
    /**
     * 시나리오 분석 결과 (Monte Carlo용)
     */
    private ScenarioAnalysis scenarioAnalysis;
    
    /**
     * 백테스팅 결과
     */
    private BacktestingResults backtestingResults;
    
    /**
     * 계산 파라미터
     */
    private VaRParameters parameters;
    
    public enum VaRMethod {
        HISTORICAL_SIMULATION("Historical Simulation", "역사적 시뮬레이션"),
        PARAMETRIC("Parametric", "모수적 방법"),
        MONTE_CARLO("Monte Carlo", "몬테카를로 시뮬레이션"),
        FILTERED_HISTORICAL("Filtered Historical", "필터링된 역사적 방법"),
        EXTREME_VALUE_THEORY("Extreme Value Theory", "극값 이론");
        
        private final String englishName;
        private final String koreanName;
        
        VaRMethod(String englishName, String koreanName) {
            this.englishName = englishName;
            this.koreanName = koreanName;
        }
        
        public String getEnglishName() { return englishName; }
        public String getKoreanName() { return koreanName; }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VaRQualityMetrics {
        
        /**
         * 모델 정확도 점수 (0-1)
         */
        private BigDecimal accuracyScore;
        
        /**
         * 백테스팅 위반 횟수
         */
        private Integer backtestViolations;
        
        /**
         * 예상 위반 횟수
         */
        private Integer expectedViolations;
        
        /**
         * Kupiec 검정 p-value
         */
        private BigDecimal kupiecPValue;
        
        /**
         * 모델 신뢰도 등급
         */
        private ModelConfidenceLevel confidenceLevel;
        
        /**
         * 계산 소요 시간 (밀리초)
         */
        private Long calculationDuration;
        
        /**
         * 데이터 품질 점수
         */
        private BigDecimal dataQualityScore;
    }
    
    public enum ModelConfidenceLevel {
        VERY_HIGH("매우 높음", 0.95),
        HIGH("높음", 0.85),
        MODERATE("보통", 0.70),
        LOW("낮음", 0.50),
        VERY_LOW("매우 낮음", 0.30);
        
        private final String description;
        private final double threshold;
        
        ModelConfidenceLevel(String description, double threshold) {
            this.description = description;
            this.threshold = threshold;
        }
        
        public String getDescription() { return description; }
        public double getThreshold() { return threshold; }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VaRComponent {
        
        /**
         * 구성 요소 ID (주식, 채권, 섹터 등)
         */
        private String componentId;
        
        /**
         * 구성 요소 이름
         */
        private String componentName;
        
        /**
         * 구성 요소 타입
         */
        private ComponentType componentType;
        
        /**
         * 포트폴리오 내 비중
         */
        private BigDecimal weight;
        
        /**
         * VaR 기여도 (절대값)
         */
        private BigDecimal varContribution;
        
        /**
         * VaR 기여도 (백분율)
         */
        private BigDecimal varContributionPercentage;
        
        /**
         * 개별 VaR (구성 요소 단독)
         */
        private BigDecimal individualVaR;
        
        /**
         * 상관관계 효과
         */
        private BigDecimal correlationEffect;
        
        /**
         * 다변화 편익
         */
        private BigDecimal diversificationBenefit;
    }
    
    public enum ComponentType {
        STOCK("개별 주식"),
        SECTOR("섹터"),
        ASSET_CLASS("자산군"),
        GEOGRAPHY("지역"),
        CURRENCY("통화"),
        RISK_FACTOR("위험 요소");
        
        private final String description;
        
        ComponentType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenarioAnalysis {
        
        /**
         * 시뮬레이션 시나리오 수
         */
        private Integer numberOfScenarios;
        
        /**
         * 최악 시나리오 손실
         */
        private BigDecimal worstCaseScenario;
        
        /**
         * 최선 시나리오 수익
         */
        private BigDecimal bestCaseScenario;
        
        /**
         * 평균 시나리오
         */
        private BigDecimal averageScenario;
        
        /**
         * 표준편차
         */
        private BigDecimal standardDeviation;
        
        /**
         * 왜도 (Skewness)
         */
        private BigDecimal skewness;
        
        /**
         * 첨도 (Kurtosis)
         */
        private BigDecimal kurtosis;
        
        /**
         * 분위수별 손실 분포
         */
        private Map<String, BigDecimal> quantileDistribution;
        
        /**
         * 시나리오 신뢰구간
         */
        private List<ConfidenceInterval> confidenceIntervals;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceInterval {
        private BigDecimal confidenceLevel;
        private BigDecimal lowerBound;
        private BigDecimal upperBound;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestingResults {
        
        /**
         * 백테스팅 기간
         */
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        
        /**
         * 총 관측 일수
         */
        private Integer totalObservations;
        
        /**
         * VaR 위반 횟수
         */
        private Integer violations;
        
        /**
         * 위반 비율
         */
        private BigDecimal violationRate;
        
        /**
         * 예상 위반 비율
         */
        private BigDecimal expectedViolationRate;
        
        /**
         * 독립성 검정 결과
         */
        private IndependenceTest independenceTest;
        
        /**
         * 조건부 커버리지 검정
         */
        private ConditionalCoverageTest conditionalCoverageTest;
        
        /**
         * 평균 초과 손실
         */
        private BigDecimal averageExcessLoss;
        
        /**
         * 최대 초과 손실
         */
        private BigDecimal maximumExcessLoss;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndependenceTest {
        private String testName;
        private BigDecimal testStatistic;
        private BigDecimal pValue;
        private Boolean rejected;
        private String conclusion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionalCoverageTest {
        private String testName;
        private BigDecimal testStatistic;
        private BigDecimal pValue;
        private Boolean rejected;
        private String conclusion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VaRParameters {
        
        /**
         * 역사적 데이터 기간 (일)
         */
        private Integer historicalPeriod;
        
        /**
         * 데이터 가중치 방식
         */
        private WeightingScheme weightingScheme;
        
        /**
         * 시뮬레이션 시나리오 수 (Monte Carlo용)
         */
        private Integer numberOfSimulations;
        
        /**
         * 난수 시드 (재현성을 위해)
         */
        private Long randomSeed;
        
        /**
         * 변동성 모델 타입
         */
        private VolatilityModel volatilityModel;
        
        /**
         * 상관관계 모델 타입
         */
        private CorrelationModel correlationModel;
        
        /**
         * 분포 가정
         */
        private DistributionAssumption distributionAssumption;
        
        /**
         * 신뢰도 수준 (예: 95%, 99%)
         */
        private BigDecimal confidenceLevel;
        
        /**
         * 보유 기간 (일)
         */
        private Integer holdingPeriod;
        
        /**
         * 추가 설정 매개변수
         */
        private Map<String, Object> additionalParameters;
    }
    
    public enum WeightingScheme {
        EQUAL_WEIGHTED("동일 가중"),
        EXPONENTIALLY_WEIGHTED("지수 가중"),
        VOLATILITY_WEIGHTED("변동성 가중"),
        CUSTOM("사용자 정의");
        
        private final String description;
        
        WeightingScheme(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public enum VolatilityModel {
        HISTORICAL("역사적 변동성"),
        GARCH("GARCH 모델"),
        EWMA("지수 가중 이동평균"),
        IMPLIED("내재 변동성");
        
        private final String description;
        
        VolatilityModel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public enum CorrelationModel {
        HISTORICAL("역사적 상관관계"),
        DCC_GARCH("DCC-GARCH"),
        EWMA_CORRELATION("EWMA 상관관계"),
        FACTOR_MODEL("팩터 모델");
        
        private final String description;
        
        CorrelationModel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public enum DistributionAssumption {
        NORMAL("정규 분포"),
        T_DISTRIBUTION("t 분포"),
        EMPIRICAL("경험적 분포"),
        MIXTURE_MODEL("혼합 분포");
        
        private final String description;
        
        DistributionAssumption(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * VaR 계산이 유효한지 확인
     */
    public boolean isValid() {
        return varValue != null && 
               varValue.compareTo(BigDecimal.ZERO) >= 0 &&
               expirationTime != null &&
               expirationTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * VaR 계산의 신뢰도 수준 평가
     */
    public ModelConfidenceLevel assessConfidenceLevel() {
        if (qualityMetrics == null || qualityMetrics.getAccuracyScore() == null) {
            return ModelConfidenceLevel.LOW;
        }
        
        double accuracy = qualityMetrics.getAccuracyScore().doubleValue();
        
        for (ModelConfidenceLevel level : ModelConfidenceLevel.values()) {
            if (accuracy >= level.getThreshold()) {
                return level;
            }
        }
        
        return ModelConfidenceLevel.VERY_LOW;
    }
    
    /**
     * 포트폴리오 대비 VaR 위험도 평가
     */
    public RiskLevel assessRiskLevel() {
        if (varPercentage == null) {
            return RiskLevel.UNKNOWN;
        }
        
        double varPercent = varPercentage.doubleValue();
        
        if (varPercent >= 0.20) return RiskLevel.VERY_HIGH;
        if (varPercent >= 0.15) return RiskLevel.HIGH;
        if (varPercent >= 0.10) return RiskLevel.MODERATE;
        if (varPercent >= 0.05) return RiskLevel.LOW;
        return RiskLevel.VERY_LOW;
    }
    
    public enum RiskLevel {
        VERY_HIGH("매우 높음"),
        HIGH("높음"),
        MODERATE("보통"),
        LOW("낮음"),
        VERY_LOW("매우 낮음"),
        UNKNOWN("알 수 없음");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * VaR 계산의 다변화 효과 계산
     */
    public BigDecimal calculateDiversificationEffect() {
        if (components == null || components.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sumOfIndividualVaRs = components.stream()
            .map(VaRComponent::getIndividualVaR)
            .filter(var -> var != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sumOfIndividualVaRs.compareTo(BigDecimal.ZERO) == 0 || varValue == null) {
            return BigDecimal.ZERO;
        }
        
        return sumOfIndividualVaRs.subtract(varValue)
                .divide(sumOfIndividualVaRs, 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 백테스팅 성과 요약
     */
    public String getBacktestingSummary() {
        if (backtestingResults == null) {
            return "백테스팅 결과 없음";
        }
        
        BacktestingResults results = backtestingResults;
        double violationRate = results.getViolationRate().doubleValue() * 100;
        double expectedRate = results.getExpectedViolationRate().doubleValue() * 100;
        
        return String.format("백테스팅 기간: %d일, 위반율: %.2f%% (예상: %.2f%%)", 
            results.getTotalObservations(), violationRate, expectedRate);
    }
}