package com.stockquest.application.service;

import com.stockquest.domain.ml.*;
import com.stockquest.domain.ml.TradingSignal.SignalType;
import com.stockquest.application.service.validation.*;
import com.stockquest.application.service.validation.BasicSignalQualityService.BasicSignalQuality;
import com.stockquest.application.service.validation.ModelPerformanceValidationService.ModelPerformanceValidation;
import com.stockquest.application.service.validation.EnsembleValidationService.EnsembleValidation;
import com.stockquest.application.service.validation.MarketContextValidationService.MarketContextValidation;
import com.stockquest.application.service.validation.MarketContextValidationService.MarketRegime;
import com.stockquest.application.service.validation.MarketContextValidationService.MarketStressLevel;
import com.stockquest.application.service.validation.StatisticalValidationService.StatisticalValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * ML 신호 검증 및 최적화 서비스 (리팩토링됨)
 * Phase 8.2: Enhanced Trading Intelligence - ML 모델 성능 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
public class MLSignalValidationService {
    
    private final MLTradingSignalService mlTradingSignalService;
    
    // 전문 검증 서비스들
    private final BasicSignalQualityService basicSignalQualityService;
    private final ModelPerformanceValidationService modelPerformanceValidationService;
    private final EnsembleValidationService ensembleValidationService;
    private final MarketContextValidationService marketContextValidationService;
    private final StatisticalValidationService statisticalValidationService;
    
    // 성능 임계값 (전문 서비스들이 필요한 경우)
    private static final BigDecimal MIN_CONFIDENCE_THRESHOLD = BigDecimal.valueOf(0.60);

    /**
     * ML 신호 실시간 검증
     */
    public SignalValidationResult validateSignal(TradingSignal signal) {
        try {
            log.debug("ML 신호 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());
            
            // 전문 검증 서비스들을 통한 검증
            var basicQuality = basicSignalQualityService.validateBasicQuality(signal);
            
            var modelValidation = modelPerformanceValidationService.validateModelPerformance(signal);
            
            var ensembleValidation = ensembleValidationService.performEnsembleValidation(signal);
            
            var contextValidation = marketContextValidationService.validateMarketContext(signal);
            
            var statisticalValidation = statisticalValidationService.performStatisticalValidation(signal);
            
            // 전체 신뢰도 점수 계산
            BigDecimal overallConfidence = calculateOverallConfidence(
                basicQuality, modelValidation, ensembleValidation, 
                contextValidation, statisticalValidation
            );
            
            // 검증 결과 생성
            SignalValidationResult result = SignalValidationResult.builder()
                .signalId(UUID.randomUUID().toString())
                .symbol(signal.getSymbol())
                .originalSignal(signal.getSignalType())
                .originalConfidence(signal.getConfidence())
                .validatedConfidence(overallConfidence)
                .validationTimestamp(LocalDateTime.now())
                .basicQuality(basicQuality)
                .modelValidation(modelValidation)
                .ensembleValidation(ensembleValidation)
                .contextValidation(contextValidation)
                .statisticalValidation(statisticalValidation)
                .recommendedAction(determineRecommendedAction(overallConfidence, signal))
                .qualityGrade(determineQualityGrade(overallConfidence))
                .riskAssessment(assessSignalRisk(signal, overallConfidence))
                .build();
            
            // 성능 추적은 각 전문 서비스에서 처리됨
            
            log.debug("신호 검증 완료: {} (신뢰도: {} -> {})", 
                signal.getSymbol(), signal.getConfidence(), overallConfidence);
            
            return result;
            
        } catch (Exception e) {
            log.error("ML 신호 검증 실패: {}", e.getMessage(), e);
            return createFailsafeValidationResult(signal, e.getMessage());
        }
    }

    // 기본 검증 헬퍼 메서드들은 각 전문 서비스로 이동됨
    
    /**
     * 전체 신뢰도 점수 계산
     */
    private BigDecimal calculateOverallConfidence(BasicSignalQuality basic, ModelPerformanceValidation model, 
                                                  EnsembleValidation ensemble, MarketContextValidation context, 
                                                  StatisticalValidation statistical) {
        // 가중 평균으로 전체 점수 계산
        BigDecimal weightedScore = basic.getQualityScore().multiply(BigDecimal.valueOf(0.25))
            .add(model.getPerformanceScore().multiply(BigDecimal.valueOf(0.25)))
            .add(ensemble.getEnsembleScore().multiply(BigDecimal.valueOf(0.20)))
            .add(context.getContextScore().multiply(BigDecimal.valueOf(0.15)))
            .add(statistical.getValidationScore().multiply(BigDecimal.valueOf(0.15)));
        
        // 0과 1 사이로 정규화
        return weightedScore.max(BigDecimal.ZERO).min(BigDecimal.ONE);
    }

    // 모델 최적화 및 모니터링 기능은 별도 서비스로 분리 가능

    // 모델 재훈련 기능도 별도 서비스로 분리 가능
    
    // 헬퍼 메서드들은 각각의 전문 검증 서비스로 이동됨
    
    private String determineRecommendedAction(BigDecimal confidence, TradingSignal signal) {
        if (confidence.compareTo(BigDecimal.valueOf(0.8)) >= 0) {
            return "EXECUTE";
        } else if (confidence.compareTo(BigDecimal.valueOf(0.6)) >= 0) {
            return "PROCEED_WITH_CAUTION";
        } else {
            return "REJECT";
        }
    }
    
    private String determineQualityGrade(BigDecimal confidence) {
        if (confidence.compareTo(BigDecimal.valueOf(0.9)) >= 0) return "A+";
        else if (confidence.compareTo(BigDecimal.valueOf(0.8)) >= 0) return "A";
        else if (confidence.compareTo(BigDecimal.valueOf(0.7)) >= 0) return "B";
        else if (confidence.compareTo(BigDecimal.valueOf(0.6)) >= 0) return "C";
        else return "D";
    }
    
    private SignalRiskAssessment assessSignalRisk(TradingSignal signal, BigDecimal confidence) {
        RiskLevel riskLevel = RiskLevel.MEDIUM;
        if (confidence.compareTo(BigDecimal.valueOf(0.8)) >= 0) riskLevel = RiskLevel.LOW;
        else if (confidence.compareTo(BigDecimal.valueOf(0.5)) < 0) riskLevel = RiskLevel.HIGH;
        
        return SignalRiskAssessment.builder()
            .riskLevel(riskLevel)
            .riskScore(BigDecimal.ONE.subtract(confidence))
            .riskFactors(identifyRiskFactors(signal, confidence))
            .build();
    }
    
    private List<String> identifyRiskFactors(TradingSignal signal, BigDecimal confidence) {
        List<String> factors = new ArrayList<>();
        
        if (confidence.compareTo(BigDecimal.valueOf(0.6)) < 0) {
            factors.add("낮은 신뢰도");
        }
        
        if (signal.getConfidence().compareTo(MIN_CONFIDENCE_THRESHOLD) < 0) {
            factors.add("임계값 미달");
        }
        
        return factors;
    }
    // 성능 추적은 각 전문 서비스에서 처리됨
    
    private SignalValidationResult createFailsafeValidationResult(TradingSignal signal, String errorMessage) {
        return SignalValidationResult.builder()
            .signalId(UUID.randomUUID().toString())
            .symbol(signal.getSymbol())
            .originalSignal(signal.getSignalType())
            .originalConfidence(signal.getConfidence())
            .validatedConfidence(BigDecimal.valueOf(0.3)) // 낮은 신뢰도
            .validationTimestamp(LocalDateTime.now())
            .recommendedAction("REJECT")
            .qualityGrade("F")
            .riskAssessment(SignalRiskAssessment.builder()
                .riskLevel(RiskLevel.HIGH)
                .riskScore(BigDecimal.valueOf(0.7))
                .riskFactors(Arrays.asList("검증 오류: " + errorMessage))
                .build())
            .build();
    }
    
    // SignalValidationResult DTO는 여전히 필요함
    
    public static class SignalValidationResult {
        private String signalId;
        private String symbol;
        private SignalType originalSignal;
        private BigDecimal originalConfidence;
        private BigDecimal validatedConfidence;
        private LocalDateTime validationTimestamp;
        private BasicSignalQuality basicQuality;
        private ModelPerformanceValidation modelValidation;
        private EnsembleValidation ensembleValidation;
        private MarketContextValidation contextValidation;
        private StatisticalValidation statisticalValidation;
        private String recommendedAction;
        private String qualityGrade;
        private SignalRiskAssessment riskAssessment;
        
        public static SignalValidationResultBuilder builder() { return new SignalValidationResultBuilder(); }
        
        public static class SignalValidationResultBuilder {
            private String signalId, symbol, recommendedAction, qualityGrade;
            private SignalType originalSignal;
            private BigDecimal originalConfidence, validatedConfidence;
            private LocalDateTime validationTimestamp;
            private BasicSignalQuality basicQuality;
            private ModelPerformanceValidation modelValidation;
            private EnsembleValidation ensembleValidation;
            private MarketContextValidation contextValidation;
            private StatisticalValidation statisticalValidation;
            private SignalRiskAssessment riskAssessment;
            
            public SignalValidationResultBuilder signalId(String signalId) { this.signalId = signalId; return this; }
            public SignalValidationResultBuilder symbol(String symbol) { this.symbol = symbol; return this; }
            public SignalValidationResultBuilder originalSignal(SignalType originalSignal) { this.originalSignal = originalSignal; return this; }
            public SignalValidationResultBuilder originalConfidence(BigDecimal originalConfidence) { this.originalConfidence = originalConfidence; return this; }
            public SignalValidationResultBuilder validatedConfidence(BigDecimal validatedConfidence) { this.validatedConfidence = validatedConfidence; return this; }
            public SignalValidationResultBuilder validationTimestamp(LocalDateTime validationTimestamp) { this.validationTimestamp = validationTimestamp; return this; }
            public SignalValidationResultBuilder basicQuality(BasicSignalQuality basicQuality) { this.basicQuality = basicQuality; return this; }
            public SignalValidationResultBuilder modelValidation(ModelPerformanceValidation modelValidation) { this.modelValidation = modelValidation; return this; }
            public SignalValidationResultBuilder ensembleValidation(EnsembleValidation ensembleValidation) { this.ensembleValidation = ensembleValidation; return this; }
            public SignalValidationResultBuilder contextValidation(MarketContextValidation contextValidation) { this.contextValidation = contextValidation; return this; }
            public SignalValidationResultBuilder statisticalValidation(StatisticalValidation statisticalValidation) { this.statisticalValidation = statisticalValidation; return this; }
            public SignalValidationResultBuilder recommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; return this; }
            public SignalValidationResultBuilder qualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; return this; }
            public SignalValidationResultBuilder riskAssessment(SignalRiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; return this; }
            
            public SignalValidationResult build() {
                SignalValidationResult result = new SignalValidationResult();
                result.signalId = this.signalId;
                result.symbol = this.symbol;
                result.originalSignal = this.originalSignal;
                result.originalConfidence = this.originalConfidence;
                result.validatedConfidence = this.validatedConfidence;
                result.validationTimestamp = this.validationTimestamp;
                result.basicQuality = this.basicQuality;
                result.modelValidation = this.modelValidation;
                result.ensembleValidation = this.ensembleValidation;
                result.contextValidation = this.contextValidation;
                result.statisticalValidation = this.statisticalValidation;
                result.recommendedAction = this.recommendedAction;
                result.qualityGrade = this.qualityGrade;
                result.riskAssessment = this.riskAssessment;
                return result;
            }
        }
        
        // Getters
        public String getSignalId() { return signalId; }
        public String getSymbol() { return symbol; }
        public SignalType getOriginalSignal() { return originalSignal; }
        public BigDecimal getOriginalConfidence() { return originalConfidence; }
        public BigDecimal getValidatedConfidence() { return validatedConfidence; }
        public LocalDateTime getValidationTimestamp() { return validationTimestamp; }
        public BasicSignalQuality getBasicQuality() { return basicQuality; }
        public ModelPerformanceValidation getModelValidation() { return modelValidation; }
        public EnsembleValidation getEnsembleValidation() { return ensembleValidation; }
        public MarketContextValidation getContextValidation() { return contextValidation; }
        public StatisticalValidation getStatisticalValidation() { return statisticalValidation; }
        public String getRecommendedAction() { return recommendedAction; }
        public String getQualityGrade() { return qualityGrade; }
        public SignalRiskAssessment getRiskAssessment() { return riskAssessment; }
    }
    
    // 나머지 DTO 클래스들은 각각의 전문 검증 서비스로 이동됨
    
    public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
    
    public static class SignalRiskAssessment {
        private RiskLevel riskLevel;
        private BigDecimal riskScore;
        private List<String> riskFactors;
        
        public static SignalRiskAssessmentBuilder builder() { return new SignalRiskAssessmentBuilder(); }
        
        public static class SignalRiskAssessmentBuilder {
            private RiskLevel riskLevel;
            private BigDecimal riskScore;
            private List<String> riskFactors;
            
            public SignalRiskAssessmentBuilder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
            public SignalRiskAssessmentBuilder riskScore(BigDecimal riskScore) { this.riskScore = riskScore; return this; }
            public SignalRiskAssessmentBuilder riskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; return this; }
            
            public SignalRiskAssessment build() {
                SignalRiskAssessment assessment = new SignalRiskAssessment();
                assessment.riskLevel = this.riskLevel;
                assessment.riskScore = this.riskScore;
                assessment.riskFactors = this.riskFactors;
                return assessment;
            }
        }
        
        public RiskLevel getRiskLevel() { return riskLevel; }
        public BigDecimal getRiskScore() { return riskScore; }
        public List<String> getRiskFactors() { return riskFactors; }
    }
    
    // 최적화 관련 클래스들도 별도 서비스로 분리 가능 (현재는 유지)
    
    // 나머지 최적화 및 재훈련 관련 메서드들은 별도 서비스로 분리될 수 있음
    // 현재는 기존 구현 유지
}