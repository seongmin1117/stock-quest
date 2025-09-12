package com.stockquest.domain.ml;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 심플 룰 기반 트레이딩 모델 (Smile ML 대체 구현)
 * 기술적 지표 기반 규칙을 통해 매매 신호를 생성하는 fallback 모델
 */
@Slf4j
@Getter
public class SimpleTradingModel {
    
    private final String symbol;
    private final LocalDateTime createdAt;
    private final List<TradingRule> rules;
    private final Map<String, Double> ruleWeights;
    
    // 모델 성능 메트릭
    private double accuracy = 0.75; // 기본 정확도 75%
    private int totalPredictions = 0;
    private int correctPredictions = 0;
    
    public SimpleTradingModel(String symbol) {
        this.symbol = symbol;
        this.createdAt = LocalDateTime.now();
        this.rules = initializeTradingRules();
        this.ruleWeights = initializeRuleWeights();
        log.info("SimpleTradingModel 초기화 완료: symbol={}", symbol);
    }
    
    /**
     * 트레이딩 규칙 초기화
     */
    private List<TradingRule> initializeTradingRules() {
        List<TradingRule> ruleList = new ArrayList<>();
        
        // RSI 기반 규칙
        ruleList.add(new TradingRule("RSI_OVERSOLD", "RSI < 30", 0.8));
        ruleList.add(new TradingRule("RSI_OVERBOUGHT", "RSI > 70", -0.8));
        ruleList.add(new TradingRule("RSI_BULLISH", "RSI > 50", 0.3));
        ruleList.add(new TradingRule("RSI_BEARISH", "RSI < 50", -0.3));
        
        // MACD 기반 규칙
        ruleList.add(new TradingRule("MACD_BULLISH_CROSS", "MACD > Signal", 0.7));
        ruleList.add(new TradingRule("MACD_BEARISH_CROSS", "MACD < Signal", -0.7));
        ruleList.add(new TradingRule("MACD_POSITIVE", "MACD > 0", 0.4));
        ruleList.add(new TradingRule("MACD_NEGATIVE", "MACD < 0", -0.4));
        
        // 볼린저 밴드 기반 규칙
        ruleList.add(new TradingRule("BB_OVERSOLD", "Price < Lower Band", 0.6));
        ruleList.add(new TradingRule("BB_OVERBOUGHT", "Price > Upper Band", -0.6));
        ruleList.add(new TradingRule("BB_MIDDLE_CROSS_UP", "Price crosses middle up", 0.5));
        ruleList.add(new TradingRule("BB_MIDDLE_CROSS_DOWN", "Price crosses middle down", -0.5));
        
        // 가격 모멘텀 기반 규칙
        ruleList.add(new TradingRule("PRICE_MOMENTUM_STRONG_UP", "Price momentum > 5%", 0.8));
        ruleList.add(new TradingRule("PRICE_MOMENTUM_STRONG_DOWN", "Price momentum < -5%", -0.8));
        ruleList.add(new TradingRule("PRICE_MOMENTUM_UP", "Price momentum > 0%", 0.3));
        ruleList.add(new TradingRule("PRICE_MOMENTUM_DOWN", "Price momentum < 0%", -0.3));
        
        // 거래량 기반 규칙
        ruleList.add(new TradingRule("VOLUME_SURGE_WITH_PRICE_UP", "Volume surge + price up", 0.6));
        ruleList.add(new TradingRule("VOLUME_SURGE_WITH_PRICE_DOWN", "Volume surge + price down", -0.6));
        
        return ruleList;
    }
    
    /**
     * 규칙별 가중치 초기화
     */
    private Map<String, Double> initializeRuleWeights() {
        Map<String, Double> weights = new HashMap<>();
        
        // RSI 가중치
        weights.put("RSI_OVERSOLD", 0.25);
        weights.put("RSI_OVERBOUGHT", 0.25);
        weights.put("RSI_BULLISH", 0.10);
        weights.put("RSI_BEARISH", 0.10);
        
        // MACD 가중치
        weights.put("MACD_BULLISH_CROSS", 0.20);
        weights.put("MACD_BEARISH_CROSS", 0.20);
        weights.put("MACD_POSITIVE", 0.08);
        weights.put("MACD_NEGATIVE", 0.08);
        
        // 볼린저 밴드 가중치
        weights.put("BB_OVERSOLD", 0.15);
        weights.put("BB_OVERBOUGHT", 0.15);
        weights.put("BB_MIDDLE_CROSS_UP", 0.12);
        weights.put("BB_MIDDLE_CROSS_DOWN", 0.12);
        
        // 가격 모멘텀 가중치
        weights.put("PRICE_MOMENTUM_STRONG_UP", 0.18);
        weights.put("PRICE_MOMENTUM_STRONG_DOWN", 0.18);
        weights.put("PRICE_MOMENTUM_UP", 0.08);
        weights.put("PRICE_MOMENTUM_DOWN", 0.08);
        
        // 거래량 가중치
        weights.put("VOLUME_SURGE_WITH_PRICE_UP", 0.12);
        weights.put("VOLUME_SURGE_WITH_PRICE_DOWN", 0.12);
        
        return weights;
    }
    
    /**
     * 피처 벡터를 입력받아 예측 결과를 반환
     * 
     * @param featureVector 기술적 지표들이 포함된 피처 벡터
     *                     [RSI, MACD, MACD_Signal, BB_Position, Price_Momentum, Volume_Ratio, EMA_Position, ...]
     * @return 예측 결과
     */
    public PredictionResult predict(double[] featureVector) {
        if (featureVector == null || featureVector.length < 7) {
            log.warn("Invalid feature vector for prediction: length={}", 
                featureVector != null ? featureVector.length : 0);
            return createNeutralPrediction("Invalid feature vector");
        }
        
        try {
            // 피처 벡터에서 지표 추출
            double rsi = featureVector[0];
            double macd = featureVector[1];
            double macdSignal = featureVector[2];
            double bbPosition = featureVector[3]; // -1 (lower) to 1 (upper)
            double priceMomentum = featureVector[4]; // %
            double volumeRatio = featureVector[5]; // 현재/평균 거래량 비율
            double emaPosition = featureVector[6]; // 현재가/EMA 비율
            
            // 각 규칙 평가 및 점수 계산
            double totalScore = 0.0;
            double totalWeight = 0.0;
            StringBuilder reasons = new StringBuilder();
            
            // RSI 규칙 평가
            if (rsi < 30) {
                double score = applyRule("RSI_OVERSOLD", 0.8);
                totalScore += score;
                totalWeight += ruleWeights.get("RSI_OVERSOLD");
                reasons.append("RSI 과매도(").append(String.format("%.1f", rsi)).append("); ");
            } else if (rsi > 70) {
                double score = applyRule("RSI_OVERBOUGHT", -0.8);
                totalScore += score;
                totalWeight += ruleWeights.get("RSI_OVERBOUGHT");
                reasons.append("RSI 과매수(").append(String.format("%.1f", rsi)).append("); ");
            } else if (rsi > 50) {
                double score = applyRule("RSI_BULLISH", 0.3);
                totalScore += score;
                totalWeight += ruleWeights.get("RSI_BULLISH");
            } else {
                double score = applyRule("RSI_BEARISH", -0.3);
                totalScore += score;
                totalWeight += ruleWeights.get("RSI_BEARISH");
            }
            
            // MACD 규칙 평가
            if (macd > macdSignal) {
                double score = applyRule("MACD_BULLISH_CROSS", 0.7);
                totalScore += score;
                totalWeight += ruleWeights.get("MACD_BULLISH_CROSS");
                reasons.append("MACD 상승 크로스; ");
            } else {
                double score = applyRule("MACD_BEARISH_CROSS", -0.7);
                totalScore += score;
                totalWeight += ruleWeights.get("MACD_BEARISH_CROSS");
                reasons.append("MACD 하락 크로스; ");
            }
            
            // 볼린저 밴드 규칙 평가
            if (bbPosition < -0.8) {
                double score = applyRule("BB_OVERSOLD", 0.6);
                totalScore += score;
                totalWeight += ruleWeights.get("BB_OVERSOLD");
                reasons.append("볼린저밴드 하단 접근; ");
            } else if (bbPosition > 0.8) {
                double score = applyRule("BB_OVERBOUGHT", -0.6);
                totalScore += score;
                totalWeight += ruleWeights.get("BB_OVERBOUGHT");
                reasons.append("볼린저밴드 상단 접근; ");
            }
            
            // 가격 모멘텀 규칙 평가
            if (priceMomentum > 5.0) {
                double score = applyRule("PRICE_MOMENTUM_STRONG_UP", 0.8);
                totalScore += score;
                totalWeight += ruleWeights.get("PRICE_MOMENTUM_STRONG_UP");
                reasons.append("강한 상승 모멘텀(").append(String.format("%.1f%%", priceMomentum)).append("); ");
            } else if (priceMomentum < -5.0) {
                double score = applyRule("PRICE_MOMENTUM_STRONG_DOWN", -0.8);
                totalScore += score;
                totalWeight += ruleWeights.get("PRICE_MOMENTUM_STRONG_DOWN");
                reasons.append("강한 하락 모멘텀(").append(String.format("%.1f%%", priceMomentum)).append("); ");
            } else if (priceMomentum > 0) {
                double score = applyRule("PRICE_MOMENTUM_UP", 0.3);
                totalScore += score;
                totalWeight += ruleWeights.get("PRICE_MOMENTUM_UP");
            } else {
                double score = applyRule("PRICE_MOMENTUM_DOWN", -0.3);
                totalScore += score;
                totalWeight += ruleWeights.get("PRICE_MOMENTUM_DOWN");
            }
            
            // 거래량 규칙 평가
            if (volumeRatio > 1.5) {
                if (priceMomentum > 0) {
                    double score = applyRule("VOLUME_SURGE_WITH_PRICE_UP", 0.6);
                    totalScore += score;
                    totalWeight += ruleWeights.get("VOLUME_SURGE_WITH_PRICE_UP");
                    reasons.append("거래량 급증 + 상승; ");
                } else {
                    double score = applyRule("VOLUME_SURGE_WITH_PRICE_DOWN", -0.6);
                    totalScore += score;
                    totalWeight += ruleWeights.get("VOLUME_SURGE_WITH_PRICE_DOWN");
                    reasons.append("거래량 급증 + 하락; ");
                }
            }
            
            // 최종 예측값 계산 (가중 평균)
            double finalPrediction = totalWeight > 0 ? totalScore / totalWeight : 0.0;
            
            // 예측값을 -1.0 ~ 1.0 범위로 제한
            finalPrediction = Math.max(-1.0, Math.min(1.0, finalPrediction));
            
            // 신뢰도 계산 (더 많은 규칙이 적용될수록, 더 극단적인 값일수록 신뢰도 높음)
            double confidence = calculateConfidence(totalWeight, Math.abs(finalPrediction));
            
            // 시그널 강도 계산
            double strength = Math.abs(finalPrediction);
            
            // 예측 통계 업데이트
            updatePredictionStats();
            
            return PredictionResult.builder()
                .prediction(finalPrediction)
                .confidence(confidence)
                .strength(strength)
                .reason(reasons.toString().trim())
                .metadata(String.format("총 가중치: %.2f, 적용 규칙 수: %d", 
                    totalWeight, reasons.toString().split(";").length))
                .build();
                
        } catch (Exception e) {
            log.error("Prediction error for symbol {}: {}", symbol, e.getMessage());
            return createNeutralPrediction("Prediction error: " + e.getMessage());
        }
    }
    
    /**
     * 규칙 적용 및 가중치 계산
     */
    private double applyRule(String ruleName, double ruleScore) {
        Double weight = ruleWeights.get(ruleName);
        return weight != null ? ruleScore * weight : 0.0;
    }
    
    /**
     * 신뢰도 계산
     */
    private double calculateConfidence(double totalWeight, double absScore) {
        // 기본 신뢰도는 적용된 가중치 비율
        double baseConfidence = Math.min(totalWeight / 2.0, 1.0);
        
        // 극단적인 예측값일수록 신뢰도 가산점
        double scoreBonus = absScore * 0.2;
        
        // 모델 전체 정확도 반영
        double accuracyFactor = accuracy * 0.3;
        
        double confidence = baseConfidence + scoreBonus + accuracyFactor;
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * 중립 예측 결과 생성
     */
    private PredictionResult createNeutralPrediction(String reason) {
        return PredictionResult.builder()
            .prediction(0.0)
            .confidence(0.5)
            .strength(0.0)
            .reason(reason)
            .metadata("Neutral prediction")
            .build();
    }
    
    /**
     * 예측 통계 업데이트
     */
    private void updatePredictionStats() {
        totalPredictions++;
        // 실제 정확도 계산은 백테스팅에서 수행
    }
    
    /**
     * 모델 정확도 업데이트 (백테스팅 결과 반영)
     */
    public void updateAccuracy(double newAccuracy) {
        this.accuracy = Math.max(0.0, Math.min(1.0, newAccuracy));
        log.info("Model accuracy updated for {}: {:.2f}%", symbol, accuracy * 100);
    }
    
    /**
     * 모델 정보 반환
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("symbol", symbol);
        info.put("modelType", "SimpleTradingModel");
        info.put("version", "1.0");
        info.put("createdAt", createdAt);
        info.put("accuracy", accuracy);
        info.put("totalPredictions", totalPredictions);
        info.put("ruleCount", rules.size());
        return info;
    }
    
    /**
     * 트레이딩 규칙 정의 클래스
     */
    @Getter
    private static class TradingRule {
        private final String name;
        private final String description;
        private final double baseScore;
        
        public TradingRule(String name, String description, double baseScore) {
            this.name = name;
            this.description = description;
            this.baseScore = baseScore;
        }
    }
}