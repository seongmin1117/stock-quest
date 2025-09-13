package com.stockquest.application.service.validation;

import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.SignalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ensemble Validation Service
 * Phase 4.1: Code Quality Enhancement - 앙상블 검증 전문 서비스
 * 
 * 여러 모델의 신호 일치도, 다양성, 예측 불확실성을 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnsembleValidationService {

    // 앙상블 설정
    private static final int MIN_ENSEMBLE_SIZE = 3;
    private static final int MAX_ENSEMBLE_SIZE = 7;
    private static final BigDecimal MIN_CONSENSUS_THRESHOLD = BigDecimal.valueOf(0.6);
    private static final BigDecimal MAX_UNCERTAINTY_THRESHOLD = BigDecimal.valueOf(0.4);

    /**
     * 앙상블 검증 수행
     * 
     * @param signal 검증할 거래 신호
     * @return EnsembleValidation 앙상블 검증 결과
     */
    public EnsembleValidation performEnsembleValidation(TradingSignal signal) {
        try {
            log.debug("앙상블 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());

            // 여러 모델의 신호 생성 (시뮬레이션)
            List<TradingSignal> ensembleSignals = generateEnsembleSignals(signal);
            
            // 신호 일치도 계산
            SignalConsensus consensus = calculateSignalConsensus(ensembleSignals);
            
            // 신뢰도 가중평균 계산
            BigDecimal weightedConfidence = calculateWeightedConfidence(ensembleSignals);
            
            // 앙상블 다양성 평가
            EnsembleDiversity diversity = assessEnsembleDiversity(ensembleSignals);
            
            // 예측 불확실성 계산
            PredictionUncertainty uncertainty = calculatePredictionUncertainty(ensembleSignals);
            
            // 앙상블 점수 계산
            BigDecimal ensembleScore = calculateEnsembleScore(consensus, diversity, uncertainty);

            EnsembleValidation result = EnsembleValidation.builder()
                .ensembleSize(ensembleSignals.size())
                .signalConsensus(consensus)
                .weightedConfidence(weightedConfidence)
                .ensembleDiversity(diversity)
                .predictionUncertainty(uncertainty)
                .ensembleScore(ensembleScore)
                .build();

            log.debug("앙상블 검증 완료: {} (앙상블 점수: {})", signal.getSymbol(), ensembleScore);
            return result;
                
        } catch (Exception e) {
            log.warn("앙상블 검증 실패: {}", e.getMessage());
            return createFailsafeEnsembleValidation();
        }
    }

    /**
     * 앙상블 신호 생성 (시뮬레이션)
     */
    private List<TradingSignal> generateEnsembleSignals(TradingSignal originalSignal) {
        // 실제 구현에서는 여러 다른 모델에서 신호를 가져와야 함
        // 여기서는 원본 신호를 기반으로 변형된 신호들을 생성
        
        return Arrays.asList(
            originalSignal, // 원본 신호
            
            // 변형된 신호 1: 신뢰도 약간 낮춤
            TradingSignal.builder()
                .symbol(originalSignal.getSymbol())
                .signalType(originalSignal.getSignalType())
                .confidence(originalSignal.getConfidence().multiply(BigDecimal.valueOf(0.9)))
                .generatedAt(originalSignal.getGeneratedAt())
                .expectedReturn(originalSignal.getExpectedReturn())
                .build(),
                
            // 변형된 신호 2: 신뢰도 조금 높임
            TradingSignal.builder()
                .symbol(originalSignal.getSymbol())
                .signalType(originalSignal.getSignalType())
                .confidence(originalSignal.getConfidence().multiply(BigDecimal.valueOf(1.1))
                    .min(BigDecimal.ONE))
                .generatedAt(originalSignal.getGeneratedAt())
                .expectedReturn(originalSignal.getExpectedReturn())
                .build(),
                
            // 변형된 신호 3: 반대 신호 (다양성을 위해)
            TradingSignal.builder()
                .symbol(originalSignal.getSymbol())
                .signalType(getOppositeSignalType(originalSignal.getSignalType()))
                .confidence(BigDecimal.valueOf(0.4)) // 낮은 신뢰도
                .generatedAt(originalSignal.getGeneratedAt())
                .expectedReturn(originalSignal.getExpectedReturn() != null ? 
                    originalSignal.getExpectedReturn().negate() : null)
                .build(),
                
            // 변형된 신호 4: 중성 신호
            TradingSignal.builder()
                .symbol(originalSignal.getSymbol())
                .signalType(SignalType.HOLD)
                .confidence(BigDecimal.valueOf(0.5))
                .generatedAt(originalSignal.getGeneratedAt())
                .expectedReturn(BigDecimal.ZERO)
                .build()
        );
    }

    /**
     * 반대 신호 타입 반환
     */
    private SignalType getOppositeSignalType(SignalType signalType) {
        return switch (signalType) {
            case STRONG_BUY, BUY, WEAK_BUY -> SignalType.SELL;
            case STRONG_SELL, SELL, WEAK_SELL -> SignalType.BUY;
            case HOLD, NEUTRAL -> SignalType.HOLD;
        };
    }
    
    /**
     * 매수 신호 판별
     */
    private boolean isBuySignal(SignalType signalType) {
        return signalType == SignalType.STRONG_BUY || signalType == SignalType.BUY || signalType == SignalType.WEAK_BUY;
    }
    
    /**
     * 매도 신호 판별
     */
    private boolean isSellSignal(SignalType signalType) {
        return signalType == SignalType.STRONG_SELL || signalType == SignalType.SELL || signalType == SignalType.WEAK_SELL;
    }
    
    /**
     * 보유 신호 판별
     */
    private boolean isHoldSignal(SignalType signalType) {
        return signalType == SignalType.HOLD || signalType == SignalType.NEUTRAL;
    }

    /**
     * 신호 일치도 계산
     */
    private SignalConsensus calculateSignalConsensus(List<TradingSignal> signals) {
        if (signals.isEmpty()) {
            return SignalConsensus.builder()
                .consensusSignal(SignalType.HOLD)
                .consensusStrength(BigDecimal.ZERO)
                .agreementRatio(BigDecimal.ZERO)
                .build();
        }

        // 각 신호 타입별 개수 계산
        long buyCount = signals.stream()
            .mapToLong(s -> isBuySignal(s.getSignalType()) ? 1 : 0).sum();
        long sellCount = signals.stream()
            .mapToLong(s -> isSellSignal(s.getSignalType()) ? 1 : 0).sum();
        long holdCount = signals.stream()
            .mapToLong(s -> isHoldSignal(s.getSignalType()) ? 1 : 0).sum();

        // 다수 신호 결정
        SignalType consensusSignal;
        long maxCount;
        
        if (buyCount >= sellCount && buyCount >= holdCount) {
            consensusSignal = SignalType.BUY;
            maxCount = buyCount;
        } else if (sellCount >= holdCount) {
            consensusSignal = SignalType.SELL;
            maxCount = sellCount;
        } else {
            consensusSignal = SignalType.HOLD;
            maxCount = holdCount;
        }

        // 일치 비율 계산
        double agreementRatio = (double) maxCount / signals.size();
        
        // 일치 강도 계산 (0.5를 기준으로 얼마나 벗어났는지)
        double consensusStrength = Math.abs(agreementRatio - 0.5) * 2;

        return SignalConsensus.builder()
            .consensusSignal(consensusSignal)
            .consensusStrength(BigDecimal.valueOf(consensusStrength))
            .agreementRatio(BigDecimal.valueOf(agreementRatio))
            .build();
    }

    /**
     * 가중평균 신뢰도 계산
     */
    private BigDecimal calculateWeightedConfidence(List<TradingSignal> signals) {
        if (signals.isEmpty()) {
            return BigDecimal.valueOf(0.5);
        }

        double totalWeight = 0;
        double weightedSum = 0;

        for (TradingSignal signal : signals) {
            double confidence = signal.getConfidence().doubleValue();
            // 신뢰도 자체를 가중치로 사용
            totalWeight += confidence;
            weightedSum += confidence * confidence;
        }

        return totalWeight > 0 ? 
            BigDecimal.valueOf(weightedSum / totalWeight).setScale(4, RoundingMode.HALF_UP) :
            BigDecimal.valueOf(0.5);
    }

    /**
     * 앙상블 다양성 평가
     */
    private EnsembleDiversity assessEnsembleDiversity(List<TradingSignal> signals) {
        if (signals.isEmpty()) {
            return EnsembleDiversity.builder()
                .diversityScore(BigDecimal.ZERO)
                .modelCount(0)
                .signalVariation(BigDecimal.ZERO)
                .build();
        }

        // 신뢰도 분산 계산
        double[] confidences = signals.stream()
            .mapToDouble(s -> s.getConfidence().doubleValue())
            .toArray();
        DescriptiveStatistics confidenceStats = new DescriptiveStatistics(confidences);
        double confidenceVariation = confidenceStats.getStandardDeviation();

        // 신호 타입 다양성 계산
        long uniqueSignalTypes = signals.stream()
            .map(TradingSignal::getSignalType)
            .distinct()
            .count();
        
        double signalVariation = (double) uniqueSignalTypes / 3.0; // 3가지 신호 타입 (BUY, SELL, HOLD)

        // 전체 다양성 점수 (신뢰도 분산 70% + 신호 다양성 30%)
        double diversityScore = confidenceVariation * 0.7 + signalVariation * 0.3;

        return EnsembleDiversity.builder()
            .diversityScore(BigDecimal.valueOf(diversityScore).setScale(4, RoundingMode.HALF_UP))
            .modelCount(signals.size())
            .signalVariation(BigDecimal.valueOf(signalVariation).setScale(4, RoundingMode.HALF_UP))
            .build();
    }

    /**
     * 예측 불확실성 계산
     */
    private PredictionUncertainty calculatePredictionUncertainty(List<TradingSignal> signals) {
        if (signals.isEmpty()) {
            return PredictionUncertainty.builder()
                .uncertaintyScore(BigDecimal.valueOf(0.5))
                .confidenceRange(BigDecimal.valueOf(0.5))
                .build();
        }

        // 신뢰도 범위 계산
        double[] confidences = signals.stream()
            .mapToDouble(s -> s.getConfidence().doubleValue())
            .toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(confidences);
        
        double confidenceRange = stats.getMax() - stats.getMin();
        double avgConfidence = stats.getMean();
        
        // 불확실성 점수: 높은 범위와 낮은 평균 신뢰도는 높은 불확실성
        double uncertaintyScore = (confidenceRange * 0.6) + ((1 - avgConfidence) * 0.4);

        return PredictionUncertainty.builder()
            .uncertaintyScore(BigDecimal.valueOf(uncertaintyScore).setScale(4, RoundingMode.HALF_UP))
            .confidenceRange(BigDecimal.valueOf(confidenceRange).setScale(4, RoundingMode.HALF_UP))
            .build();
    }

    /**
     * 앙상블 점수 계산
     */
    private BigDecimal calculateEnsembleScore(SignalConsensus consensus, EnsembleDiversity diversity, 
                                            PredictionUncertainty uncertainty) {
        // 일치도 점수 (50%)
        BigDecimal consensusScore = consensus.getConsensusStrength().multiply(BigDecimal.valueOf(0.5));
        
        // 다양성 점수 (30%) - 적당한 다양성이 좋음
        BigDecimal diversityScore = calculateOptimalDiversityScore(diversity.getDiversityScore())
            .multiply(BigDecimal.valueOf(0.3));
        
        // 확실성 점수 (20%) - 낮은 불확실성이 좋음
        BigDecimal certaintyScore = BigDecimal.ONE.subtract(uncertainty.getUncertaintyScore())
            .multiply(BigDecimal.valueOf(0.2));

        return consensusScore.add(diversityScore).add(certaintyScore)
            .max(BigDecimal.ZERO).min(BigDecimal.ONE);
    }

    /**
     * 최적 다양성 점수 계산 (너무 높거나 낮으면 패널티)
     */
    private BigDecimal calculateOptimalDiversityScore(BigDecimal diversityScore) {
        double diversity = diversityScore.doubleValue();
        
        // 최적 다양성은 0.3~0.7 범위
        if (diversity < 0.3) {
            return BigDecimal.valueOf(diversity / 0.3); // 너무 낮으면 선형 감소
        } else if (diversity > 0.7) {
            return BigDecimal.valueOf((1.0 - diversity) / 0.3); // 너무 높으면 선형 감소
        } else {
            return BigDecimal.ONE; // 최적 범위
        }
    }

    /**
     * 실패 시 안전 앙상블 검증 결과 생성
     */
    private EnsembleValidation createFailsafeEnsembleValidation() {
        return EnsembleValidation.builder()
            .ensembleSize(1)
            .signalConsensus(SignalConsensus.builder()
                .consensusSignal(SignalType.HOLD)
                .consensusStrength(BigDecimal.valueOf(0.5))
                .agreementRatio(BigDecimal.valueOf(0.5))
                .build())
            .weightedConfidence(BigDecimal.valueOf(0.5))
            .ensembleDiversity(EnsembleDiversity.builder()
                .diversityScore(BigDecimal.valueOf(0.5))
                .modelCount(1)
                .signalVariation(BigDecimal.ZERO)
                .build())
            .predictionUncertainty(PredictionUncertainty.builder()
                .uncertaintyScore(BigDecimal.valueOf(0.5))
                .confidenceRange(BigDecimal.ZERO)
                .build())
            .ensembleScore(BigDecimal.valueOf(0.5))
            .build();
    }

    // DTO Classes

    public static class EnsembleValidation {
        private final int ensembleSize;
        private final SignalConsensus signalConsensus;
        private final BigDecimal weightedConfidence;
        private final EnsembleDiversity ensembleDiversity;
        private final PredictionUncertainty predictionUncertainty;
        private final BigDecimal ensembleScore;

        public static EnsembleValidationBuilder builder() {
            return new EnsembleValidationBuilder();
        }

        private EnsembleValidation(EnsembleValidationBuilder builder) {
            this.ensembleSize = builder.ensembleSize;
            this.signalConsensus = builder.signalConsensus;
            this.weightedConfidence = builder.weightedConfidence;
            this.ensembleDiversity = builder.ensembleDiversity;
            this.predictionUncertainty = builder.predictionUncertainty;
            this.ensembleScore = builder.ensembleScore;
        }

        // Getters
        public int getEnsembleSize() { return ensembleSize; }
        public SignalConsensus getSignalConsensus() { return signalConsensus; }
        public BigDecimal getWeightedConfidence() { return weightedConfidence; }
        public EnsembleDiversity getEnsembleDiversity() { return ensembleDiversity; }
        public PredictionUncertainty getPredictionUncertainty() { return predictionUncertainty; }
        public BigDecimal getEnsembleScore() { return ensembleScore; }

        public static class EnsembleValidationBuilder {
            private int ensembleSize;
            private SignalConsensus signalConsensus;
            private BigDecimal weightedConfidence;
            private EnsembleDiversity ensembleDiversity;
            private PredictionUncertainty predictionUncertainty;
            private BigDecimal ensembleScore;

            public EnsembleValidationBuilder ensembleSize(int ensembleSize) {
                this.ensembleSize = ensembleSize;
                return this;
            }

            public EnsembleValidationBuilder signalConsensus(SignalConsensus signalConsensus) {
                this.signalConsensus = signalConsensus;
                return this;
            }

            public EnsembleValidationBuilder weightedConfidence(BigDecimal weightedConfidence) {
                this.weightedConfidence = weightedConfidence;
                return this;
            }

            public EnsembleValidationBuilder ensembleDiversity(EnsembleDiversity ensembleDiversity) {
                this.ensembleDiversity = ensembleDiversity;
                return this;
            }

            public EnsembleValidationBuilder predictionUncertainty(PredictionUncertainty predictionUncertainty) {
                this.predictionUncertainty = predictionUncertainty;
                return this;
            }

            public EnsembleValidationBuilder ensembleScore(BigDecimal ensembleScore) {
                this.ensembleScore = ensembleScore;
                return this;
            }

            public EnsembleValidation build() {
                return new EnsembleValidation(this);
            }
        }
    }

    public static class SignalConsensus {
        private final SignalType consensusSignal;
        private final BigDecimal consensusStrength;
        private final BigDecimal agreementRatio;

        public static SignalConsensusBuilder builder() {
            return new SignalConsensusBuilder();
        }

        private SignalConsensus(SignalConsensusBuilder builder) {
            this.consensusSignal = builder.consensusSignal;
            this.consensusStrength = builder.consensusStrength;
            this.agreementRatio = builder.agreementRatio;
        }

        // Getters
        public SignalType getConsensusSignal() { return consensusSignal; }
        public BigDecimal getConsensusStrength() { return consensusStrength; }
        public BigDecimal getAgreementRatio() { return agreementRatio; }

        public static class SignalConsensusBuilder {
            private SignalType consensusSignal;
            private BigDecimal consensusStrength;
            private BigDecimal agreementRatio;

            public SignalConsensusBuilder consensusSignal(SignalType consensusSignal) {
                this.consensusSignal = consensusSignal;
                return this;
            }

            public SignalConsensusBuilder consensusStrength(BigDecimal consensusStrength) {
                this.consensusStrength = consensusStrength;
                return this;
            }

            public SignalConsensusBuilder agreementRatio(BigDecimal agreementRatio) {
                this.agreementRatio = agreementRatio;
                return this;
            }

            public SignalConsensus build() {
                return new SignalConsensus(this);
            }
        }
    }

    public static class EnsembleDiversity {
        private final BigDecimal diversityScore;
        private final int modelCount;
        private final BigDecimal signalVariation;

        public static EnsembleDiversityBuilder builder() {
            return new EnsembleDiversityBuilder();
        }

        private EnsembleDiversity(EnsembleDiversityBuilder builder) {
            this.diversityScore = builder.diversityScore;
            this.modelCount = builder.modelCount;
            this.signalVariation = builder.signalVariation;
        }

        // Getters
        public BigDecimal getDiversityScore() { return diversityScore; }
        public int getModelCount() { return modelCount; }
        public BigDecimal getSignalVariation() { return signalVariation; }

        public static class EnsembleDiversityBuilder {
            private BigDecimal diversityScore;
            private int modelCount;
            private BigDecimal signalVariation;

            public EnsembleDiversityBuilder diversityScore(BigDecimal diversityScore) {
                this.diversityScore = diversityScore;
                return this;
            }

            public EnsembleDiversityBuilder modelCount(int modelCount) {
                this.modelCount = modelCount;
                return this;
            }

            public EnsembleDiversityBuilder signalVariation(BigDecimal signalVariation) {
                this.signalVariation = signalVariation;
                return this;
            }

            public EnsembleDiversity build() {
                return new EnsembleDiversity(this);
            }
        }
    }

    public static class PredictionUncertainty {
        private final BigDecimal uncertaintyScore;
        private final BigDecimal confidenceRange;

        public static PredictionUncertaintyBuilder builder() {
            return new PredictionUncertaintyBuilder();
        }

        private PredictionUncertainty(PredictionUncertaintyBuilder builder) {
            this.uncertaintyScore = builder.uncertaintyScore;
            this.confidenceRange = builder.confidenceRange;
        }

        // Getters
        public BigDecimal getUncertaintyScore() { return uncertaintyScore; }
        public BigDecimal getConfidenceRange() { return confidenceRange; }

        public static class PredictionUncertaintyBuilder {
            private BigDecimal uncertaintyScore;
            private BigDecimal confidenceRange;

            public PredictionUncertaintyBuilder uncertaintyScore(BigDecimal uncertaintyScore) {
                this.uncertaintyScore = uncertaintyScore;
                return this;
            }

            public PredictionUncertaintyBuilder confidenceRange(BigDecimal confidenceRange) {
                this.confidenceRange = confidenceRange;
                return this;
            }

            public PredictionUncertainty build() {
                return new PredictionUncertainty(this);
            }
        }
    }
}