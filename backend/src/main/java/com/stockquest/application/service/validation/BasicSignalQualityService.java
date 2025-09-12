package com.stockquest.application.service.validation;

import com.stockquest.domain.ml.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Signal Quality Validation Service
 * Phase 4.1: Code Quality Enhancement - 기본 신호 품질 검증 전문 서비스
 * 
 * ML 거래 신호의 기본적인 품질을 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSignalQualityService {

    // 품질 임계값
    private static final BigDecimal MIN_CONFIDENCE_THRESHOLD = BigDecimal.valueOf(0.60);
    private static final int MAX_SIGNAL_AGE_MINUTES = 30;

    /**
     * 기본 신호 품질 검증
     * 
     * @param signal 검증할 거래 신호
     * @return BasicSignalQuality 기본 품질 검증 결과
     */
    public BasicSignalQuality validateBasicQuality(TradingSignal signal) {
        try {
            log.debug("기본 신호 품질 검증 시작: {} - {}", signal.getSymbol(), signal.getSignalType());

            List<String> issues = new ArrayList<>();
            
            // 신뢰도 임계값 검증
            boolean confidenceOk = validateConfidenceThreshold(signal, issues);
            
            // 신호 일관성 검증
            boolean signalConsistent = validateSignalConsistency(signal, issues);
            
            // 데이터 품질 검증
            boolean dataQualityOk = validateDataQuality(signal, issues);
            
            // 시간적 유효성 검증
            boolean timeValidOk = validateTimeValidity(signal, issues);
            
            // 품질 점수 계산 (가중 평균)
            BigDecimal qualityScore = calculateQualityScore(
                confidenceOk, signalConsistent, dataQualityOk, timeValidOk);
            
            BasicSignalQuality result = BasicSignalQuality.builder()
                .confidenceThresholdMet(confidenceOk)
                .signalConsistent(signalConsistent)
                .dataQualityGood(dataQualityOk)
                .timeValid(timeValidOk)
                .qualityScore(qualityScore)
                .issues(issues)
                .build();

            log.debug("기본 신호 품질 검증 완료: {} (점수: {})", signal.getSymbol(), qualityScore);
            return result;
            
        } catch (Exception e) {
            log.error("기본 신호 품질 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("기본 신호 품질 검증 중 오류 발생", e);
        }
    }

    /**
     * 신뢰도 임계값 검증
     */
    private boolean validateConfidenceThreshold(TradingSignal signal, List<String> issues) {
        boolean confidenceOk = signal.getConfidence().compareTo(MIN_CONFIDENCE_THRESHOLD) >= 0;
        if (!confidenceOk) {
            issues.add("신뢰도가 임계값 미달: " + signal.getConfidence());
            log.warn("신호 신뢰도 부족: {} - 현재: {}, 최소: {}", 
                signal.getSymbol(), signal.getConfidence(), MIN_CONFIDENCE_THRESHOLD);
        }
        return confidenceOk;
    }

    /**
     * 신호 일관성 검증
     */
    private boolean validateSignalConsistency(TradingSignal signal, List<String> issues) {
        // 신호 타입과 기대 수익률의 방향성 일치 확인
        boolean directionConsistent = true;
        
        if (signal.getExpectedReturn() != null) {
            boolean signalBullish = signal.getSignalType().toString().contains("BUY") || 
                                   signal.getSignalType().toString().contains("LONG");
            boolean returnPositive = signal.getExpectedReturn().compareTo(BigDecimal.ZERO) > 0;
            
            directionConsistent = (signalBullish == returnPositive);
        }
        
        // 신뢰도와 신호 강도의 일관성 확인
        boolean confidenceConsistent = true;
        if (signal.getStrength() != null) {
            // 높은 신뢰도는 높은 신호 강도와 일치해야 함
            boolean highConfidence = signal.getConfidence().compareTo(BigDecimal.valueOf(0.8)) >= 0;
            boolean highStrength = signal.getStrength().compareTo(BigDecimal.valueOf(0.7)) >= 0;
            
            // 불일치 허용 범위 (완전히 일치할 필요는 없음)
            if (highConfidence && signal.getStrength().compareTo(BigDecimal.valueOf(0.3)) < 0) {
                confidenceConsistent = false;
            }
        }
        
        boolean consistent = directionConsistent && confidenceConsistent;
        
        if (!consistent) {
            issues.add("신호 일관성 부족 - 방향성 또는 신뢰도/강도 불일치");
        }
        
        return consistent;
    }

    /**
     * 데이터 품질 검증
     */
    private boolean validateDataQuality(TradingSignal signal, List<String> issues) {
        // 필수 필드 검증
        if (signal.getSymbol() == null || signal.getSymbol().trim().isEmpty()) {
            issues.add("심볼 정보 누락");
            return false;
        }
        
        if (signal.getSignalType() == null) {
            issues.add("신호 타입 누락");
            return false;
        }
        
        if (signal.getConfidence() == null) {
            issues.add("신뢰도 정보 누락");
            return false;
        }
        
        // 신뢰도 범위 검증 (0.0 ~ 1.0)
        if (signal.getConfidence().compareTo(BigDecimal.ZERO) < 0 || 
            signal.getConfidence().compareTo(BigDecimal.ONE) > 0) {
            issues.add("신뢰도 값 범위 오류: " + signal.getConfidence());
            return false;
        }
        
        // 기대 수익률 범위 검증 (있는 경우)
        if (signal.getExpectedReturn() != null) {
            // 일반적으로 -100% ~ +1000% 범위를 벗어나면 이상
            if (signal.getExpectedReturn().compareTo(BigDecimal.valueOf(-1.0)) < 0 || 
                signal.getExpectedReturn().compareTo(BigDecimal.valueOf(10.0)) > 0) {
                issues.add("기대 수익률 값 범위 의심: " + signal.getExpectedReturn());
            }
        }
        
        return true;
    }

    /**
     * 시간적 유효성 검증
     */
    private boolean validateTimeValidity(TradingSignal signal, List<String> issues) {
        if (signal.getGeneratedAt() == null) {
            issues.add("신호 생성 시간 정보 누락");
            return false;
        }
        
        // 신호 생성 후 경과 시간 확인
        LocalDateTime now = LocalDateTime.now();
        long minutesOld = ChronoUnit.MINUTES.between(signal.getGeneratedAt(), now);
        
        if (minutesOld > MAX_SIGNAL_AGE_MINUTES) {
            issues.add("신호가 너무 오래됨: " + minutesOld + "분 경과");
            return false;
        }
        
        // 미래 시간 신호 검증
        if (signal.getGeneratedAt().isAfter(now.plusMinutes(5))) {
            issues.add("미래 시간 신호 감지");
            return false;
        }
        
        return true;
    }

    /**
     * 품질 점수 계산 (가중 평균)
     */
    private BigDecimal calculateQualityScore(boolean confidenceOk, boolean signalConsistent, 
                                            boolean dataQualityOk, boolean timeValidOk) {
        // 가중치: 신뢰도(40%), 일관성(30%), 데이터품질(20%), 시간유효성(10%)
        return BigDecimal.valueOf(
            (confidenceOk ? 0.4 : 0) + 
            (signalConsistent ? 0.3 : 0) + 
            (dataQualityOk ? 0.2 : 0) + 
            (timeValidOk ? 0.1 : 0)
        );
    }

    // DTO Class for Basic Signal Quality
    
    public static class BasicSignalQuality {
        private final boolean confidenceThresholdMet;
        private final boolean signalConsistent;
        private final boolean dataQualityGood;
        private final boolean timeValid;
        private final BigDecimal qualityScore;
        private final List<String> issues;

        public static BasicSignalQualityBuilder builder() {
            return new BasicSignalQualityBuilder();
        }

        private BasicSignalQuality(BasicSignalQualityBuilder builder) {
            this.confidenceThresholdMet = builder.confidenceThresholdMet;
            this.signalConsistent = builder.signalConsistent;
            this.dataQualityGood = builder.dataQualityGood;
            this.timeValid = builder.timeValid;
            this.qualityScore = builder.qualityScore;
            this.issues = builder.issues;
        }

        // Getters
        public boolean isConfidenceThresholdMet() { return confidenceThresholdMet; }
        public boolean isSignalConsistent() { return signalConsistent; }
        public boolean isDataQualityGood() { return dataQualityGood; }
        public boolean isTimeValid() { return timeValid; }
        public BigDecimal getQualityScore() { return qualityScore; }
        public List<String> getIssues() { return issues; }

        public static class BasicSignalQualityBuilder {
            private boolean confidenceThresholdMet;
            private boolean signalConsistent;
            private boolean dataQualityGood;
            private boolean timeValid;
            private BigDecimal qualityScore;
            private List<String> issues;

            public BasicSignalQualityBuilder confidenceThresholdMet(boolean confidenceThresholdMet) {
                this.confidenceThresholdMet = confidenceThresholdMet;
                return this;
            }

            public BasicSignalQualityBuilder signalConsistent(boolean signalConsistent) {
                this.signalConsistent = signalConsistent;
                return this;
            }

            public BasicSignalQualityBuilder dataQualityGood(boolean dataQualityGood) {
                this.dataQualityGood = dataQualityGood;
                return this;
            }

            public BasicSignalQualityBuilder timeValid(boolean timeValid) {
                this.timeValid = timeValid;
                return this;
            }

            public BasicSignalQualityBuilder qualityScore(BigDecimal qualityScore) {
                this.qualityScore = qualityScore;
                return this;
            }

            public BasicSignalQualityBuilder issues(List<String> issues) {
                this.issues = issues;
                return this;
            }

            public BasicSignalQuality build() {
                return new BasicSignalQuality(this);
            }
        }
    }
}