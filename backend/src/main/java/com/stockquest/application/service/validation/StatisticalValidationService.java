package com.stockquest.application.service.validation;

import com.stockquest.domain.ml.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statistical Validation Service
 * Phase 4.1: Code Quality Enhancement - 통계적 검증 전문 서비스
 * 
 * ML 신호의 통계적 유의성을 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticalValidationService {

    // 성과 데이터 저장소 (실제로는 데이터베이스 사용)
    private final Map<String, PerformanceData> modelPerformanceData = new ConcurrentHashMap<>();
    
    // 통계적 검증 설정
    private static final int MIN_SAMPLE_SIZE = 30;
    private static final int SUFFICIENT_SAMPLE_SIZE = 100;
    private static final BigDecimal SIGNIFICANCE_LEVEL = BigDecimal.valueOf(0.05);
    private static final BigDecimal CONFIDENCE_LEVEL = BigDecimal.valueOf(0.95);

    /**
     * 통계적 유의성 검증
     * 
     * @param signal 검증할 거래 신호
     * @return StatisticalValidation 통계적 검증 결과
     */
    public StatisticalValidation performStatisticalValidation(TradingSignal signal) {
        try {
            log.debug("통계적 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());

            String modelKey = getModelKey(signal);
            PerformanceData performanceData = getOrCreatePerformanceData(modelKey);
            
            // 표본 크기 확인
            if (performanceData.getOutcomes().size() < MIN_SAMPLE_SIZE) {
                return createInsufficientDataValidation(performanceData.getOutcomes().size());
            }
            
            List<Double> outcomes = performanceData.getOutcomes();
            
            // 정확도의 신뢰구간 계산
            List<BigDecimal> confidenceInterval = calculateAccuracyConfidenceInterval(outcomes);
            
            // 통계적 유의성 검정 (정확도가 50%보다 유의미하게 높은지)
            BigDecimal pValue = performAccuracySignificanceTest(outcomes);
            
            // 표본 크기 충분성 평가
            boolean sufficientSample = outcomes.size() >= SUFFICIENT_SAMPLE_SIZE;
            
            // 통계적 유의성 판정
            boolean statisticallySignificant = pValue.compareTo(SIGNIFICANCE_LEVEL) < 0 && sufficientSample;
            
            // 효과 크기 계산 (Cohen's d)
            BigDecimal effectSize = calculateEffectSize(outcomes);
            
            // 검정력 계산
            BigDecimal statisticalPower = calculateStatisticalPower(outcomes.size(), effectSize);
            
            // 통계적 검증 점수 계산
            BigDecimal validationScore = calculateStatisticalValidationScore(
                statisticallySignificant, pValue, outcomes.size(), effectSize);

            StatisticalValidation result = StatisticalValidation.builder()
                .sampleSize(outcomes.size())
                .confidenceInterval(confidenceInterval)
                .pValue(pValue)
                .statisticallySignificant(statisticallySignificant)
                .sufficientSample(sufficientSample)
                .effectSize(effectSize)
                .statisticalPower(statisticalPower)
                .validationScore(validationScore)
                .build();

            log.debug("통계적 검증 완료: {} (p-value: {}, 유의성: {})", 
                signal.getSymbol(), pValue, statisticallySignificant);
            return result;
            
        } catch (Exception e) {
            log.error("통계적 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("통계적 검증 중 오류 발생", e);
        }
    }

    /**
     * 모델 키 생성
     */
    private String getModelKey(TradingSignal signal) {
        return "model_" + signal.getSymbol() + "_" + signal.getSignalType().name();
    }

    /**
     * 성과 데이터 가져오거나 생성
     */
    private PerformanceData getOrCreatePerformanceData(String modelKey) {
        return modelPerformanceData.computeIfAbsent(modelKey, this::generateSimulatedPerformanceData);
    }

    /**
     * 시뮬레이션 성과 데이터 생성
     */
    private PerformanceData generateSimulatedPerformanceData(String modelKey) {
        // 실제 구현에서는 데이터베이스에서 과거 성과를 조회
        // 여기서는 시뮬레이션 데이터 생성
        
        int sampleSize = 50 + (int) (Math.random() * 100); // 50~150개 샘플
        PerformanceData data = new PerformanceData(modelKey);
        
        // 모델 성능에 따른 정확도 시뮬레이션
        double baseAccuracy = 0.52 + Math.random() * 0.2; // 0.52~0.72
        
        for (int i = 0; i < sampleSize; i++) {
            // 노이즈가 있는 정확도 데이터
            double outcome = baseAccuracy + (Math.random() - 0.5) * 0.3;
            outcome = Math.max(0.0, Math.min(1.0, outcome)); // 0~1 범위로 제한
            data.addOutcome(outcome);
        }
        
        log.info("모델 {} 시뮬레이션 성과 데이터 생성 완료: {} 샘플, 평균 정확도: {:.3f}", 
            modelKey, sampleSize, baseAccuracy);
        
        return data;
    }

    /**
     * 불충분한 데이터 검증 결과 생성
     */
    private StatisticalValidation createInsufficientDataValidation(int sampleSize) {
        return StatisticalValidation.builder()
            .sampleSize(sampleSize)
            .statisticallySignificant(false)
            .sufficientSample(false)
            .confidenceInterval(Arrays.asList(BigDecimal.ZERO, BigDecimal.ONE))
            .pValue(BigDecimal.valueOf(0.5))
            .effectSize(BigDecimal.ZERO)
            .statisticalPower(BigDecimal.valueOf(0.1))
            .validationScore(BigDecimal.valueOf(0.2))
            .build();
    }

    /**
     * 정확도의 신뢰구간 계산
     */
    private List<BigDecimal> calculateAccuracyConfidenceInterval(List<Double> outcomes) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        outcomes.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        int n = outcomes.size();
        
        // t-분포를 사용한 신뢰구간 계산
        TDistribution tDist = new TDistribution(n - 1);
        double tValue = tDist.inverseCumulativeProbability(0.975); // 95% 신뢰구간
        
        double marginOfError = tValue * (stdDev / Math.sqrt(n));
        
        BigDecimal lowerBound = BigDecimal.valueOf(Math.max(0.0, mean - marginOfError))
            .setScale(4, RoundingMode.HALF_UP);
        BigDecimal upperBound = BigDecimal.valueOf(Math.min(1.0, mean + marginOfError))
            .setScale(4, RoundingMode.HALF_UP);
        
        return Arrays.asList(lowerBound, upperBound);
    }

    /**
     * 정확도 유의성 검정 (단일 표본 t-검정)
     */
    private BigDecimal performAccuracySignificanceTest(List<Double> outcomes) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        outcomes.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        int n = outcomes.size();
        
        // H0: μ = 0.5 (정확도가 50%와 같다)
        // H1: μ > 0.5 (정확도가 50%보다 크다)
        double hypothesizedMean = 0.5;
        
        // t 통계량 계산
        double tStatistic = (mean - hypothesizedMean) / (stdDev / Math.sqrt(n));
        
        // p-value 계산 (단측 검정)
        TDistribution tDist = new TDistribution(n - 1);
        double pValue = 1.0 - tDist.cumulativeProbability(tStatistic);
        
        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, pValue)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 효과 크기 계산 (Cohen's d)
     */
    private BigDecimal calculateEffectSize(List<Double> outcomes) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        outcomes.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        double hypothesizedMean = 0.5;
        
        // Cohen's d = (표본평균 - 모집단평균) / 표준편차
        double cohensD = (mean - hypothesizedMean) / stdDev;
        
        return BigDecimal.valueOf(cohensD).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 검정력 계산
     */
    private BigDecimal calculateStatisticalPower(int sampleSize, BigDecimal effectSize) {
        // 단순화된 검정력 계산 (실제로는 더 복잡한 계산 필요)
        double effect = Math.abs(effectSize.doubleValue());
        double power;
        
        // 경험적 공식을 사용한 검정력 추정
        if (sampleSize < 30) {
            power = Math.min(0.8, effect * sampleSize * 0.1);
        } else {
            double z = effect * Math.sqrt(sampleSize) / Math.sqrt(2);
            NormalDistribution normal = new NormalDistribution();
            power = 1 - normal.cumulativeProbability(1.96 - z);
        }
        
        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, power)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 통계적 검증 점수 계산
     */
    private BigDecimal calculateStatisticalValidationScore(boolean significant, BigDecimal pValue, 
                                                         int sampleSize, BigDecimal effectSize) {
        double score = 0.0;
        
        // 유의성 기여도 (40%)
        if (significant) {
            score += 0.4;
        } else {
            // p-value가 낮을수록 높은 점수
            score += (1 - pValue.doubleValue()) * 0.2;
        }
        
        // 표본 크기 기여도 (30%)
        double sampleScore = Math.min(1.0, (double) sampleSize / SUFFICIENT_SAMPLE_SIZE);
        score += sampleScore * 0.3;
        
        // 효과 크기 기여도 (20%)
        double effectScore = Math.min(1.0, Math.abs(effectSize.doubleValue()) / 0.5);
        score += effectScore * 0.2;
        
        // 신뢰도 기여도 (10%)
        if (sampleSize >= SUFFICIENT_SAMPLE_SIZE && significant) {
            score += 0.1;
        }
        
        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    // 헬퍼 클래스

    private static class PerformanceData {
        private final String modelKey;
        private final List<Double> outcomes;

        public PerformanceData(String modelKey) {
            this.modelKey = modelKey;
            this.outcomes = new java.util.ArrayList<>();
        }

        public void addOutcome(double outcome) {
            outcomes.add(outcome);
        }

        public String getModelKey() { return modelKey; }
        public List<Double> getOutcomes() { return outcomes; }
    }

    // DTO Classes

    public static class StatisticalValidation {
        private final int sampleSize;
        private final List<BigDecimal> confidenceInterval;
        private final BigDecimal pValue;
        private final boolean statisticallySignificant;
        private final boolean sufficientSample;
        private final BigDecimal effectSize;
        private final BigDecimal statisticalPower;
        private final BigDecimal validationScore;

        public static StatisticalValidationBuilder builder() {
            return new StatisticalValidationBuilder();
        }

        private StatisticalValidation(StatisticalValidationBuilder builder) {
            this.sampleSize = builder.sampleSize;
            this.confidenceInterval = builder.confidenceInterval;
            this.pValue = builder.pValue;
            this.statisticallySignificant = builder.statisticallySignificant;
            this.sufficientSample = builder.sufficientSample;
            this.effectSize = builder.effectSize;
            this.statisticalPower = builder.statisticalPower;
            this.validationScore = builder.validationScore;
        }

        // Getters
        public int getSampleSize() { return sampleSize; }
        public List<BigDecimal> getConfidenceInterval() { return confidenceInterval; }
        public BigDecimal getPValue() { return pValue; }
        public boolean isStatisticallySignificant() { return statisticallySignificant; }
        public boolean isSufficientSample() { return sufficientSample; }
        public BigDecimal getEffectSize() { return effectSize; }
        public BigDecimal getStatisticalPower() { return statisticalPower; }
        public BigDecimal getValidationScore() { return validationScore; }

        public static class StatisticalValidationBuilder {
            private int sampleSize;
            private List<BigDecimal> confidenceInterval;
            private BigDecimal pValue;
            private boolean statisticallySignificant;
            private boolean sufficientSample;
            private BigDecimal effectSize;
            private BigDecimal statisticalPower;
            private BigDecimal validationScore;

            public StatisticalValidationBuilder sampleSize(int sampleSize) {
                this.sampleSize = sampleSize;
                return this;
            }

            public StatisticalValidationBuilder confidenceInterval(List<BigDecimal> confidenceInterval) {
                this.confidenceInterval = confidenceInterval;
                return this;
            }

            public StatisticalValidationBuilder pValue(BigDecimal pValue) {
                this.pValue = pValue;
                return this;
            }

            public StatisticalValidationBuilder statisticallySignificant(boolean statisticallySignificant) {
                this.statisticallySignificant = statisticallySignificant;
                return this;
            }

            public StatisticalValidationBuilder sufficientSample(boolean sufficientSample) {
                this.sufficientSample = sufficientSample;
                return this;
            }

            public StatisticalValidationBuilder effectSize(BigDecimal effectSize) {
                this.effectSize = effectSize;
                return this;
            }

            public StatisticalValidationBuilder statisticalPower(BigDecimal statisticalPower) {
                this.statisticalPower = statisticalPower;
                return this;
            }

            public StatisticalValidationBuilder validationScore(BigDecimal validationScore) {
                this.validationScore = validationScore;
                return this;
            }

            public StatisticalValidation build() {
                return new StatisticalValidation(this);
            }
        }
    }
}