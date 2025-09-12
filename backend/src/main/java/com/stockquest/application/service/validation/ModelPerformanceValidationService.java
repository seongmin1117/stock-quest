package com.stockquest.application.service.validation;

import com.stockquest.domain.ml.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model Performance Validation Service
 * Phase 4.1: Code Quality Enhancement - 모델 성능 검증 전문 서비스
 * 
 * ML 모델의 성능 추적, 피로도 평가, 재훈련 필요성을 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelPerformanceValidationService {

    // 성능 추적기 저장소
    private final Map<String, SignalPerformanceTracker> performanceTrackers = new ConcurrentHashMap<>();
    
    // 성능 임계값
    private static final BigDecimal MIN_ACCURACY_THRESHOLD = BigDecimal.valueOf(0.55);
    private static final BigDecimal PERFORMANCE_DEGRADATION_THRESHOLD = BigDecimal.valueOf(0.10);
    private static final int MODEL_FATIGUE_DAYS = 30;
    private static final int MODEL_REFRESH_DAYS = 60;

    /**
     * 모델 성능 기반 검증
     * 
     * @param signal 검증할 거래 신호
     * @return ModelPerformanceValidation 모델 성능 검증 결과
     */
    public ModelPerformanceValidation validateModelPerformance(TradingSignal signal) {
        try {
            log.debug("모델 성능 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());

            String modelKey = getModelKey(signal);
            SignalPerformanceTracker tracker = getOrCreateTracker(modelKey);
            
            // 현재 모델 성능 지표 계산
            ModelPerformanceMetrics currentMetrics = calculateCurrentMetrics(tracker);
            
            // 성능 트렌드 분석
            PerformanceTrend trend = analyzePerformanceTrend(tracker);
            
            // 모델 피로도 평가
            ModelFatigue fatigue = assessModelFatigue(tracker);
            
            // 재훈련 필요성 평가
            RetrainingRecommendation retrainingRecommendation = evaluateRetrainingNeed(
                currentMetrics, fatigue);
            
            // 성능 점수 계산
            BigDecimal performanceScore = calculatePerformanceScore(currentMetrics, trend);

            ModelPerformanceValidation result = ModelPerformanceValidation.builder()
                .modelKey(modelKey)
                .currentMetrics(currentMetrics)
                .performanceTrend(trend)
                .modelFatigue(fatigue)
                .retrainingRecommendation(retrainingRecommendation)
                .performanceScore(performanceScore)
                .build();

            log.debug("모델 성능 검증 완료: {} (성능 점수: {})", modelKey, performanceScore);
            return result;
            
        } catch (Exception e) {
            log.error("모델 성능 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("모델 성능 검증 중 오류 발생", e);
        }
    }

    /**
     * 모델 키 생성
     */
    private String getModelKey(TradingSignal signal) {
        return "model_" + signal.getSymbol() + "_" + signal.getSignalType().name();
    }

    /**
     * 성능 추적기 가져오거나 생성
     */
    private SignalPerformanceTracker getOrCreateTracker(String modelKey) {
        return performanceTrackers.computeIfAbsent(modelKey, this::initializePerformanceTracker);
    }

    /**
     * 새 성능 추적기 초기화
     */
    private SignalPerformanceTracker initializePerformanceTracker(String modelKey) {
        log.info("새 모델 성능 추적기 초기화: {}", modelKey);
        return SignalPerformanceTracker.builder()
            .modelKey(modelKey)
            .creationTime(LocalDateTime.now())
            .totalPredictions(0)
            .correctPredictions(0)
            .recentAccuracy(BigDecimal.valueOf(0.5)) // 기본값
            .build();
    }

    /**
     * 현재 모델 성능 지표 계산
     */
    private ModelPerformanceMetrics calculateCurrentMetrics(SignalPerformanceTracker tracker) {
        if (tracker.getTotalPredictions() == 0) {
            return ModelPerformanceMetrics.builder()
                .accuracy(BigDecimal.valueOf(0.5))
                .precision(BigDecimal.valueOf(0.5))
                .recall(BigDecimal.valueOf(0.5))
                .f1Score(BigDecimal.valueOf(0.5))
                .totalPredictions(0)
                .recentPerformance(BigDecimal.valueOf(0.5))
                .build();
        }

        // 정확도 계산 (실제 구현에서는 더 정교해야 함)
        double accuracy = (double) tracker.getCorrectPredictions() / tracker.getTotalPredictions();
        
        // 정밀도와 재현율은 시뮬레이션 (실제로는 true/false positive/negative 필요)
        double precision = Math.min(accuracy + 0.05, 1.0);
        double recall = Math.max(accuracy - 0.05, 0.0);
        double f1Score = 2 * (precision * recall) / (precision + recall);

        return ModelPerformanceMetrics.builder()
            .accuracy(BigDecimal.valueOf(accuracy))
            .precision(BigDecimal.valueOf(precision))
            .recall(BigDecimal.valueOf(recall))
            .f1Score(BigDecimal.valueOf(f1Score))
            .totalPredictions(tracker.getTotalPredictions())
            .recentPerformance(tracker.getRecentAccuracy())
            .build();
    }

    /**
     * 성능 트렌드 분석
     */
    private PerformanceTrend analyzePerformanceTrend(SignalPerformanceTracker tracker) {
        if (tracker.getTotalPredictions() < 10) {
            return PerformanceTrend.builder()
                .trend("INSUFFICIENT_DATA")
                .trendStrength(BigDecimal.ZERO)
                .confidence(BigDecimal.valueOf(0.3))
                .build();
        }

        // 최근 성능과 전체 성능 비교 (단순화)
        double overallAccuracy = (double) tracker.getCorrectPredictions() / tracker.getTotalPredictions();
        double recentAccuracy = tracker.getRecentAccuracy().doubleValue();
        
        double trendDifference = recentAccuracy - overallAccuracy;
        String trend;
        BigDecimal trendStrength;
        
        if (Math.abs(trendDifference) < 0.05) {
            trend = "STABLE";
            trendStrength = BigDecimal.valueOf(Math.abs(trendDifference));
        } else if (trendDifference > 0) {
            trend = "IMPROVING";
            trendStrength = BigDecimal.valueOf(trendDifference);
        } else {
            trend = "DECLINING";
            trendStrength = BigDecimal.valueOf(Math.abs(trendDifference));
        }

        return PerformanceTrend.builder()
            .trend(trend)
            .trendStrength(trendStrength)
            .confidence(BigDecimal.valueOf(0.8))
            .build();
    }

    /**
     * 모델 피로도 평가
     */
    private ModelFatigue assessModelFatigue(SignalPerformanceTracker tracker) {
        long daysSinceCreation = ChronoUnit.DAYS.between(tracker.getCreationTime(), LocalDateTime.now());
        
        String fatigueLevel;
        boolean needsRefresh = daysSinceCreation > MODEL_REFRESH_DAYS;
        double fatigueScore = Math.min(1.0, (double) daysSinceCreation / MODEL_REFRESH_DAYS);
        
        if (daysSinceCreation < MODEL_FATIGUE_DAYS) {
            fatigueLevel = "LOW";
        } else if (daysSinceCreation < MODEL_REFRESH_DAYS) {
            fatigueLevel = "MODERATE";
        } else {
            fatigueLevel = "HIGH";
        }

        return ModelFatigue.builder()
            .fatigueLevel(fatigueLevel)
            .daysSinceTraining((int) daysSinceCreation)
            .needsRefresh(needsRefresh)
            .fatigueScore(BigDecimal.valueOf(fatigueScore))
            .build();
    }

    /**
     * 재훈련 필요성 평가
     */
    private RetrainingRecommendation evaluateRetrainingNeed(ModelPerformanceMetrics currentMetrics, 
                                                           ModelFatigue fatigue) {
        boolean needsRetraining = false;
        String urgency = "LOW";
        BigDecimal estimatedImprovement = BigDecimal.ZERO;
        
        // 정확도가 임계값 미달
        if (currentMetrics.getAccuracy().compareTo(MIN_ACCURACY_THRESHOLD) < 0) {
            needsRetraining = true;
            urgency = "HIGH";
            estimatedImprovement = MIN_ACCURACY_THRESHOLD.subtract(currentMetrics.getAccuracy());
        }
        // 모델이 피로함
        else if (fatigue.isNeedsRefresh()) {
            needsRetraining = true;
            urgency = fatigue.getFatigueLevel().equals("HIGH") ? "HIGH" : "MEDIUM";
            estimatedImprovement = BigDecimal.valueOf(0.05); // 예상 개선폭
        }

        return RetrainingRecommendation.builder()
            .recommendRetraining(needsRetraining)
            .urgency(urgency)
            .estimatedImprovement(estimatedImprovement)
            .reason(determineRetrainingReason(currentMetrics, fatigue))
            .build();
    }

    /**
     * 재훈련 필요 사유 결정
     */
    private String determineRetrainingReason(ModelPerformanceMetrics metrics, ModelFatigue fatigue) {
        if (metrics.getAccuracy().compareTo(MIN_ACCURACY_THRESHOLD) < 0) {
            return "성능 저하 - 정확도 임계값 미달";
        } else if (fatigue.isNeedsRefresh()) {
            return "모델 노화 - 생성 후 " + fatigue.getDaysSinceTraining() + "일 경과";
        } else {
            return "재훈련 불필요";
        }
    }

    /**
     * 성능 점수 계산
     */
    private BigDecimal calculatePerformanceScore(ModelPerformanceMetrics metrics, PerformanceTrend trend) {
        // 현재 정확도 (70%)
        BigDecimal accuracyScore = metrics.getAccuracy().multiply(BigDecimal.valueOf(0.7));
        
        // F1 점수 (20%)
        BigDecimal f1Score = metrics.getF1Score().multiply(BigDecimal.valueOf(0.2));
        
        // 트렌드 보정 (10%)
        BigDecimal trendAdjustment = BigDecimal.ZERO;
        if ("IMPROVING".equals(trend.getTrend())) {
            trendAdjustment = trend.getTrendStrength().multiply(BigDecimal.valueOf(0.1));
        } else if ("DECLINING".equals(trend.getTrend())) {
            trendAdjustment = trend.getTrendStrength().multiply(BigDecimal.valueOf(-0.1));
        }
        
        return accuracyScore.add(f1Score).add(trendAdjustment)
            .max(BigDecimal.ZERO).min(BigDecimal.ONE);
    }

    // DTO Classes

    public static class ModelPerformanceValidation {
        private final String modelKey;
        private final ModelPerformanceMetrics currentMetrics;
        private final PerformanceTrend performanceTrend;
        private final ModelFatigue modelFatigue;
        private final RetrainingRecommendation retrainingRecommendation;
        private final BigDecimal performanceScore;

        public static ModelPerformanceValidationBuilder builder() {
            return new ModelPerformanceValidationBuilder();
        }

        private ModelPerformanceValidation(ModelPerformanceValidationBuilder builder) {
            this.modelKey = builder.modelKey;
            this.currentMetrics = builder.currentMetrics;
            this.performanceTrend = builder.performanceTrend;
            this.modelFatigue = builder.modelFatigue;
            this.retrainingRecommendation = builder.retrainingRecommendation;
            this.performanceScore = builder.performanceScore;
        }

        // Getters
        public String getModelKey() { return modelKey; }
        public ModelPerformanceMetrics getCurrentMetrics() { return currentMetrics; }
        public PerformanceTrend getPerformanceTrend() { return performanceTrend; }
        public ModelFatigue getModelFatigue() { return modelFatigue; }
        public RetrainingRecommendation getRetrainingRecommendation() { return retrainingRecommendation; }
        public BigDecimal getPerformanceScore() { return performanceScore; }

        public static class ModelPerformanceValidationBuilder {
            private String modelKey;
            private ModelPerformanceMetrics currentMetrics;
            private PerformanceTrend performanceTrend;
            private ModelFatigue modelFatigue;
            private RetrainingRecommendation retrainingRecommendation;
            private BigDecimal performanceScore;

            public ModelPerformanceValidationBuilder modelKey(String modelKey) {
                this.modelKey = modelKey;
                return this;
            }

            public ModelPerformanceValidationBuilder currentMetrics(ModelPerformanceMetrics currentMetrics) {
                this.currentMetrics = currentMetrics;
                return this;
            }

            public ModelPerformanceValidationBuilder performanceTrend(PerformanceTrend performanceTrend) {
                this.performanceTrend = performanceTrend;
                return this;
            }

            public ModelPerformanceValidationBuilder modelFatigue(ModelFatigue modelFatigue) {
                this.modelFatigue = modelFatigue;
                return this;
            }

            public ModelPerformanceValidationBuilder retrainingRecommendation(RetrainingRecommendation retrainingRecommendation) {
                this.retrainingRecommendation = retrainingRecommendation;
                return this;
            }

            public ModelPerformanceValidationBuilder performanceScore(BigDecimal performanceScore) {
                this.performanceScore = performanceScore;
                return this;
            }

            public ModelPerformanceValidation build() {
                return new ModelPerformanceValidation(this);
            }
        }
    }

    public static class ModelPerformanceMetrics {
        private final BigDecimal accuracy;
        private final BigDecimal precision;
        private final BigDecimal recall;
        private final BigDecimal f1Score;
        private final int totalPredictions;
        private final BigDecimal recentPerformance;

        public static ModelPerformanceMetricsBuilder builder() {
            return new ModelPerformanceMetricsBuilder();
        }

        private ModelPerformanceMetrics(ModelPerformanceMetricsBuilder builder) {
            this.accuracy = builder.accuracy;
            this.precision = builder.precision;
            this.recall = builder.recall;
            this.f1Score = builder.f1Score;
            this.totalPredictions = builder.totalPredictions;
            this.recentPerformance = builder.recentPerformance;
        }

        // Getters
        public BigDecimal getAccuracy() { return accuracy; }
        public BigDecimal getPrecision() { return precision; }
        public BigDecimal getRecall() { return recall; }
        public BigDecimal getF1Score() { return f1Score; }
        public int getTotalPredictions() { return totalPredictions; }
        public BigDecimal getRecentPerformance() { return recentPerformance; }

        public static class ModelPerformanceMetricsBuilder {
            private BigDecimal accuracy;
            private BigDecimal precision;
            private BigDecimal recall;
            private BigDecimal f1Score;
            private int totalPredictions;
            private BigDecimal recentPerformance;

            public ModelPerformanceMetricsBuilder accuracy(BigDecimal accuracy) {
                this.accuracy = accuracy;
                return this;
            }

            public ModelPerformanceMetricsBuilder precision(BigDecimal precision) {
                this.precision = precision;
                return this;
            }

            public ModelPerformanceMetricsBuilder recall(BigDecimal recall) {
                this.recall = recall;
                return this;
            }

            public ModelPerformanceMetricsBuilder f1Score(BigDecimal f1Score) {
                this.f1Score = f1Score;
                return this;
            }

            public ModelPerformanceMetricsBuilder totalPredictions(int totalPredictions) {
                this.totalPredictions = totalPredictions;
                return this;
            }

            public ModelPerformanceMetricsBuilder recentPerformance(BigDecimal recentPerformance) {
                this.recentPerformance = recentPerformance;
                return this;
            }

            public ModelPerformanceMetrics build() {
                return new ModelPerformanceMetrics(this);
            }
        }
    }

    public static class SignalPerformanceTracker {
        private final String modelKey;
        private final LocalDateTime creationTime;
        private final int totalPredictions;
        private final int correctPredictions;
        private final BigDecimal recentAccuracy;

        public static SignalPerformanceTrackerBuilder builder() {
            return new SignalPerformanceTrackerBuilder();
        }

        private SignalPerformanceTracker(SignalPerformanceTrackerBuilder builder) {
            this.modelKey = builder.modelKey;
            this.creationTime = builder.creationTime;
            this.totalPredictions = builder.totalPredictions;
            this.correctPredictions = builder.correctPredictions;
            this.recentAccuracy = builder.recentAccuracy;
        }

        // Getters
        public String getModelKey() { return modelKey; }
        public LocalDateTime getCreationTime() { return creationTime; }
        public int getTotalPredictions() { return totalPredictions; }
        public int getCorrectPredictions() { return correctPredictions; }
        public BigDecimal getRecentAccuracy() { return recentAccuracy; }

        public static class SignalPerformanceTrackerBuilder {
            private String modelKey;
            private LocalDateTime creationTime;
            private int totalPredictions;
            private int correctPredictions;
            private BigDecimal recentAccuracy;

            public SignalPerformanceTrackerBuilder modelKey(String modelKey) {
                this.modelKey = modelKey;
                return this;
            }

            public SignalPerformanceTrackerBuilder creationTime(LocalDateTime creationTime) {
                this.creationTime = creationTime;
                return this;
            }

            public SignalPerformanceTrackerBuilder totalPredictions(int totalPredictions) {
                this.totalPredictions = totalPredictions;
                return this;
            }

            public SignalPerformanceTrackerBuilder correctPredictions(int correctPredictions) {
                this.correctPredictions = correctPredictions;
                return this;
            }

            public SignalPerformanceTrackerBuilder recentAccuracy(BigDecimal recentAccuracy) {
                this.recentAccuracy = recentAccuracy;
                return this;
            }

            public SignalPerformanceTracker build() {
                return new SignalPerformanceTracker(this);
            }
        }
    }

    public static class PerformanceTrend {
        private final String trend;
        private final BigDecimal trendStrength;
        private final BigDecimal confidence;

        public static PerformanceTrendBuilder builder() {
            return new PerformanceTrendBuilder();
        }

        private PerformanceTrend(PerformanceTrendBuilder builder) {
            this.trend = builder.trend;
            this.trendStrength = builder.trendStrength;
            this.confidence = builder.confidence;
        }

        // Getters
        public String getTrend() { return trend; }
        public BigDecimal getTrendStrength() { return trendStrength; }
        public BigDecimal getConfidence() { return confidence; }

        public static class PerformanceTrendBuilder {
            private String trend;
            private BigDecimal trendStrength;
            private BigDecimal confidence;

            public PerformanceTrendBuilder trend(String trend) {
                this.trend = trend;
                return this;
            }

            public PerformanceTrendBuilder trendStrength(BigDecimal trendStrength) {
                this.trendStrength = trendStrength;
                return this;
            }

            public PerformanceTrendBuilder confidence(BigDecimal confidence) {
                this.confidence = confidence;
                return this;
            }

            public PerformanceTrend build() {
                return new PerformanceTrend(this);
            }
        }
    }

    public static class ModelFatigue {
        private final String fatigueLevel;
        private final int daysSinceTraining;
        private final boolean needsRefresh;
        private final BigDecimal fatigueScore;

        public static ModelFatigueBuilder builder() {
            return new ModelFatigueBuilder();
        }

        private ModelFatigue(ModelFatigueBuilder builder) {
            this.fatigueLevel = builder.fatigueLevel;
            this.daysSinceTraining = builder.daysSinceTraining;
            this.needsRefresh = builder.needsRefresh;
            this.fatigueScore = builder.fatigueScore;
        }

        // Getters
        public String getFatigueLevel() { return fatigueLevel; }
        public int getDaysSinceTraining() { return daysSinceTraining; }
        public boolean isNeedsRefresh() { return needsRefresh; }
        public BigDecimal getFatigueScore() { return fatigueScore; }

        public static class ModelFatigueBuilder {
            private String fatigueLevel;
            private int daysSinceTraining;
            private boolean needsRefresh;
            private BigDecimal fatigueScore;

            public ModelFatigueBuilder fatigueLevel(String fatigueLevel) {
                this.fatigueLevel = fatigueLevel;
                return this;
            }

            public ModelFatigueBuilder daysSinceTraining(int daysSinceTraining) {
                this.daysSinceTraining = daysSinceTraining;
                return this;
            }

            public ModelFatigueBuilder needsRefresh(boolean needsRefresh) {
                this.needsRefresh = needsRefresh;
                return this;
            }

            public ModelFatigueBuilder fatigueScore(BigDecimal fatigueScore) {
                this.fatigueScore = fatigueScore;
                return this;
            }

            public ModelFatigue build() {
                return new ModelFatigue(this);
            }
        }
    }

    public static class RetrainingRecommendation {
        private final boolean recommendRetraining;
        private final String urgency;
        private final BigDecimal estimatedImprovement;
        private final String reason;

        public static RetrainingRecommendationBuilder builder() {
            return new RetrainingRecommendationBuilder();
        }

        private RetrainingRecommendation(RetrainingRecommendationBuilder builder) {
            this.recommendRetraining = builder.recommendRetraining;
            this.urgency = builder.urgency;
            this.estimatedImprovement = builder.estimatedImprovement;
            this.reason = builder.reason;
        }

        // Getters
        public boolean isRecommendRetraining() { return recommendRetraining; }
        public String getUrgency() { return urgency; }
        public BigDecimal getEstimatedImprovement() { return estimatedImprovement; }
        public String getReason() { return reason; }

        public static class RetrainingRecommendationBuilder {
            private boolean recommendRetraining;
            private String urgency;
            private BigDecimal estimatedImprovement;
            private String reason;

            public RetrainingRecommendationBuilder recommendRetraining(boolean recommendRetraining) {
                this.recommendRetraining = recommendRetraining;
                return this;
            }

            public RetrainingRecommendationBuilder urgency(String urgency) {
                this.urgency = urgency;
                return this;
            }

            public RetrainingRecommendationBuilder estimatedImprovement(BigDecimal estimatedImprovement) {
                this.estimatedImprovement = estimatedImprovement;
                return this;
            }

            public RetrainingRecommendationBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }

            public RetrainingRecommendation build() {
                return new RetrainingRecommendation(this);
            }
        }
    }
}