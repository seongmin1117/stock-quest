package com.stockquest.application.service.ml;

import com.stockquest.application.service.ml.MarketFeatureCollectionService.MarketFeatures;
import com.stockquest.domain.ml.PredictionResult;
import com.stockquest.domain.ml.SimpleTradingModel;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 시그널 생성 및 예측 결과 처리 서비스
 * Signal generation from ML predictions, confidence calculation, and signal strength assessment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalGenerationService {
    
    private final FeatureEngineeringService featureEngineeringService;
    
    private static final int FEATURE_COUNT = 25;
    
    /**
     * ML 모델로부터 트레이딩 시그널 생성 (fallback implementation)
     */
    public TradingSignal generateSignalFromModel(String symbol, SimpleTradingModel model, MarketFeatures features) {
        try {
            log.debug("모델 기반 시그널 생성 시작: symbol={}", symbol);
            
            // 특성 벡터 준비
            double[] featureVector = extractFeaturesFromMarketData(features);
            
            // 모델 예측 (simple rule-based prediction)
            PredictionResult prediction = model.predict(featureVector);
            
            // 시그널 타입 결정
            SignalType signalType = determineSignalType((int) prediction.getPrediction());
            
            // 신뢰도 계산 
            BigDecimal confidence = calculateConfidence(prediction.getConfidence(), features);
            
            // 시그널 강도 계산
            BigDecimal strength = calculateSignalStrength(prediction.getStrength(), featureVector);
            
            // 시그널 근거 생성
            List<SignalReason> reasons = generateSignalReasons(featureVector, model);
            
            TradingSignal signal = TradingSignal.builder()
                .signalId(UUID.randomUUID().toString())
                .symbol(symbol)
                .signalType(signalType)
                .strength(strength)
                .confidence(confidence)
                .expectedReturn(estimateExpectedReturn(signalType, strength.doubleValue()))
                .expectedRisk(estimateExpectedRisk(features))
                .timeHorizon(5) // 5일 예측
                .targetPrice(calculateTargetPrice(features, signalType, strength.doubleValue()))
                .stopLossPrice(calculateStopLossPrice(features, signalType))
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .modelInfo(createModelInfo(model))
                .reasons(reasons)
                .status(SignalStatus.ACTIVE)
                .build();
                
            log.debug("모델 기반 시그널 생성 완료: symbol={}, signalType={}, confidence={}", 
                symbol, signalType, confidence);
            return signal;
                
        } catch (Exception e) {
            log.error("모델 기반 시그널 생성 실패: symbol={}", symbol, e);
            return generateFallbackSignal(symbol, features);
        }
    }
    
    /**
     * 시그널 타입 결정
     */
    public SignalType determineSignalType(int prediction) {
        return switch (prediction) {
            case 0 -> SignalType.SELL;
            case 1 -> SignalType.HOLD;
            case 2 -> SignalType.BUY;
            default -> SignalType.HOLD;
        };
    }
    
    /**
     * 신뢰도 계산
     */
    public BigDecimal calculateConfidence(double modelConfidence, MarketFeatures features) {
        try {
            double adjustedConfidence = modelConfidence;
            
            // 시장 조건에 따른 신뢰도 조정
            if (features.getVolatilityAnalysis() != null) {
                double volatility = features.getVolatilityAnalysis().getHistoricalVolatility();
                if (volatility < 0.2) {
                    adjustedConfidence += 0.1; // 낮은 변동성에서 신뢰도 증가
                } else if (volatility > 0.4) {
                    adjustedConfidence -= 0.1; // 높은 변동성에서 신뢰도 감소
                }
            }
            
            // 기술적 지표에 따른 신뢰도 조정
            if (features.getTechnicalIndicators() != null && features.getTechnicalIndicators().getRsi() != null) {
                double rsi = features.getTechnicalIndicators().getRsi().doubleValue();
                if (rsi > 80 || rsi < 20) {
                    adjustedConfidence += 0.15; // RSI 극값에서 신뢰도 증가
                }
            }
            
            // 0과 1 사이로 제한
            adjustedConfidence = Math.max(0.0, Math.min(1.0, adjustedConfidence));
            
            return BigDecimal.valueOf(adjustedConfidence).setScale(4, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.error("신뢰도 계산 실패", e);
            return BigDecimal.valueOf(0.5); // 기본값
        }
    }
    
    /**
     * 시그널 강도 계산
     */
    public BigDecimal calculateSignalStrength(double modelStrength, double[] featureVector) {
        try {
            double adjustedStrength = modelStrength;
            
            // 특성 기반 강도 조정
            if (featureVector.length > 10) {
                // MACD 신호
                double macd = featureVector[10];
                if (Math.abs(macd) > 0.5) {
                    adjustedStrength += 0.2;
                }
                
                // 모멘텀 점수
                if (featureVector.length > 17) {
                    double momentum = featureVector[17];
                    adjustedStrength += Math.min(0.3, Math.abs(momentum) / 100.0);
                }
            }
            
            // 0과 1 사이로 제한
            adjustedStrength = Math.max(0.0, Math.min(1.0, adjustedStrength));
            
            return BigDecimal.valueOf(adjustedStrength).setScale(4, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.error("신호 강도 계산 실패", e);
            return BigDecimal.valueOf(0.5); // 기본값
        }
    }
    
    /**
     * 시그널 근거 생성
     */
    private List<SignalReason> generateSignalReasons(double[] featureVector, SimpleTradingModel model) {
        try {
            List<SignalReason> reasons = new ArrayList<>();
            String[] featureNames = generateFeatureNames();
            
            // 특성 중요도 기반으로 상위 5개 근거 생성
            for (int i = 0; i < Math.min(5, featureVector.length); i++) {
                if (Math.abs(featureVector[i]) > 0.1) { // 임계값 이상인 특성만
                    SignalReason reason = SignalReason.builder()
                        .featureName(featureNames[i])
                        .importance(BigDecimal.valueOf(Math.abs(featureVector[i])).setScale(4, RoundingMode.HALF_UP))
                        .value(BigDecimal.valueOf(featureVector[i]).setScale(4, RoundingMode.HALF_UP))
                        .description(generateFeatureDescription(i, featureVector[i]))
                        .category(mapFeatureToCategory(i))
                        .build();
                    reasons.add(reason);
                }
            }
            
            return reasons.stream()
                .sorted((a, b) -> b.getImportance().compareTo(a.getImportance()))
                .limit(3)
                .toList();
                
        } catch (Exception e) {
            log.error("시그널 근거 생성 실패", e);
            return new ArrayList<>();
        }
    }
    
    // 유틸리티 메소드들
    
    private double[] extractFeaturesFromMarketData(MarketFeatures features) {
        List<com.stockquest.domain.marketdata.MarketData> data = features.getHistoricalData();
        if (data.isEmpty()) {
            return new double[FEATURE_COUNT];
        }
        
        return featureEngineeringService.extractFeatures(data, data.size() - 1);
    }
    
    private BigDecimal estimateExpectedReturn(SignalType signalType, double strength) {
        double baseReturn = switch (signalType) {
            case BUY, STRONG_BUY -> 0.05;
            case SELL, STRONG_SELL -> -0.05;
            case HOLD -> 0.0;
            default -> 0.0;
        };
        
        return BigDecimal.valueOf(baseReturn * strength).setScale(4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal estimateExpectedRisk(MarketFeatures features) {
        double volatility = features.getVolatilityAnalysis() != null ? 
            features.getVolatilityAnalysis().getHistoricalVolatility() : 0.2;
        
        return BigDecimal.valueOf(volatility).setScale(4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateTargetPrice(MarketFeatures features, SignalType signalType, double strength) {
        BigDecimal currentPrice = features.getCurrentPrice();
        double multiplier = switch (signalType) {
            case STRONG_BUY -> 1.0 + (0.1 * strength);
            case BUY -> 1.0 + (0.05 * strength);
            case WEAK_BUY -> 1.0 + (0.025 * strength);
            case WEAK_SELL -> 1.0 - (0.025 * strength);
            case SELL -> 1.0 - (0.05 * strength);
            case STRONG_SELL -> 1.0 - (0.1 * strength);
            default -> 1.0;
        };
        
        return currentPrice.multiply(BigDecimal.valueOf(multiplier))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateStopLossPrice(MarketFeatures features, SignalType signalType) {
        BigDecimal currentPrice = features.getCurrentPrice();
        double stopLossRatio = switch (signalType) {
            case STRONG_BUY, BUY, WEAK_BUY -> 0.95; // 5% 손절
            case STRONG_SELL, SELL, WEAK_SELL -> 1.05; // 5% 손절
            default -> 0.98;
        };
        
        return currentPrice.multiply(BigDecimal.valueOf(stopLossRatio))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private ModelInfo createModelInfo(SimpleTradingModel model) {
        return ModelInfo.builder()
            .modelName("Simple Trading Model")
            .modelVersion("1.0")
            .modelAccuracy(BigDecimal.valueOf(0.75)) // fallback 모델 정확도
            .trainingPeriod("60 days")
            .featureCount(FEATURE_COUNT)
            .algorithmType("Rule-based")
            .metadata(Map.of(
                "type", "fallback",
                "ruleCount", 10,
                "symbol", model.getSymbol()
            ))
            .build();
    }
    
    private TradingSignal generateFallbackSignal(String symbol, MarketFeatures features) {
        // 기본 규칙 기반 시그널
        SignalType signalType = SignalType.HOLD;
        
        if (features.getTechnicalIndicators() != null && features.getTechnicalIndicators().getRsi() != null) {
            double rsi = features.getTechnicalIndicators().getRsi().doubleValue();
            if (rsi < 30) {
                signalType = SignalType.BUY;
            } else if (rsi > 70) {
                signalType = SignalType.SELL;
            }
        }
        
        return TradingSignal.builder()
            .signalId(UUID.randomUUID().toString())
            .symbol(symbol)
            .signalType(signalType)
            .strength(BigDecimal.valueOf(0.5))
            .confidence(BigDecimal.valueOf(0.3))
            .expectedReturn(BigDecimal.ZERO)
            .expectedRisk(BigDecimal.valueOf(0.1))
            .timeHorizon(5)
            .generatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(12))
            .status(SignalStatus.ACTIVE)
            .build();
    }
    
    private String[] generateFeatureNames() {
        return IntStream.range(0, FEATURE_COUNT)
            .mapToObj(i -> "feature_" + i)
            .toArray(String[]::new);
    }
    
    private String generateFeatureDescription(int featureIndex, double value) {
        return switch (featureIndex) {
            case 0 -> String.format("1일 수익률: %.2f%%", value * 100);
            case 1 -> String.format("5일 수익률: %.2f%%", value * 100);
            case 2 -> String.format("10일 수익률: %.2f%%", value * 100);
            case 3 -> String.format("5일 이동평균 대비: %.2f%%", (value - 1) * 100);
            case 4 -> String.format("20일 이동평균 대비: %.2f%%", (value - 1) * 100);
            case 9 -> String.format("RSI 지표: %.1f", value);
            case 10 -> String.format("MACD 지표: %.4f", value);
            default -> String.format("특성 %d: %.4f", featureIndex, value);
        };
    }
    
    private ReasonCategory mapFeatureToCategory(int featureIndex) {
        return switch (featureIndex) {
            case 0, 1, 2 -> ReasonCategory.MOMENTUM;
            case 3, 4 -> ReasonCategory.TECHNICAL;
            case 5, 6 -> ReasonCategory.VOLATILITY;
            case 7, 8 -> ReasonCategory.VOLUME;
            case 9, 10, 11 -> ReasonCategory.TECHNICAL;
            default -> ReasonCategory.TECHNICAL;
        };
    }
}