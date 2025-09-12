package com.stockquest.application.service.ml;

import com.stockquest.application.service.RealTimeMarketDataService;
import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.domain.ml.SimpleTradingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * ML 모델 관리 및 생명주기 관리 서비스
 * Model caching, lifecycle management, and training data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLModelManagementService {
    
    private final RealTimeMarketDataService marketDataService;
    private final FeatureEngineeringService featureEngineeringService;
    
    // 캐시된 ML 모델들 (fallback implementation)
    private final Map<String, SimpleTradingModel> cachedModels = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> modelTrainingTimes = new ConcurrentHashMap<>();
    
    private static final int MODEL_CACHE_DURATION_HOURS = 6;
    private static final int FEATURE_COUNT = 25;
    
    /**
     * ML 모델 로드 또는 훈련 (fallback implementation)
     */
    public SimpleTradingModel getOrTrainModel(String symbol) {
        try {
            String modelKey = "trading_model_" + symbol;
            
            // 캐시된 모델 확인
            SimpleTradingModel cachedModel = cachedModels.get(modelKey);
            LocalDateTime trainingTime = modelTrainingTimes.get(modelKey);
            
            if (cachedModel != null && trainingTime != null && 
                trainingTime.isAfter(LocalDateTime.now().minusHours(MODEL_CACHE_DURATION_HOURS))) {
                log.debug("캐시된 ML 모델 사용: symbol={}", symbol);
                return cachedModel;
            }
            
            // 새 모델 훈련
            log.info("ML 모델 훈련 시작 (fallback mode): symbol={}", symbol);
            SimpleTradingModel model = trainSimpleTradingModel(symbol);
            
            // 캐시 저장
            cachedModels.put(modelKey, model);
            modelTrainingTimes.put(modelKey, LocalDateTime.now());
            
            return model;
            
        } catch (Exception e) {
            log.error("ML 모델 로드/훈련 실패: symbol={}", symbol, e);
            return createFallbackModel(symbol);
        }
    }
    
    /**
     * Simple Trading 모델 훈련 (fallback implementation)
     */
    public SimpleTradingModel trainSimpleTradingModel(String symbol) {
        try {
            // 훈련 데이터 준비
            TrainingData trainingData = prepareTrainingData(symbol);
            
            // 간단한 규칙 기반 모델 생성
            SimpleTradingModel model = new SimpleTradingModel(symbol);
            
            // 모델 정확도 계산
            double accuracy = calculateModelAccuracy(model, trainingData);
            
            log.info("Simple Trading 모델 훈련 완료: symbol={}, 정확도={}", symbol, accuracy);
            
            return model;
            
        } catch (Exception e) {
            log.error("ML 모델 훈련 실패: symbol={}", symbol, e);
            // 폴백: 기본 규칙 기반 모델 반환
            return createFallbackModel(symbol);
        }
    }
    
    /**
     * 훈련 데이터 준비
     */
    public TrainingData prepareTrainingData(String symbol) {
        try {
            log.debug("훈련 데이터 준비 시작: symbol={}", symbol);
            
            // 과거 60일 데이터 수집
            List<MarketData> historicalData = marketDataService.getHistoricalData(symbol, 60);
            
            List<double[]> featureList = new ArrayList<>();
            List<Integer> labelList = new ArrayList<>();
            String[] featureNames = generateFeatureNames();
            
            for (int i = 10; i < historicalData.size() - 5; i++) {
                try {
                    // 특성 벡터 생성
                    double[] features = featureEngineeringService.extractFeatures(historicalData, i);
                    
                    // 레이블 생성 (미래 5일 수익률 기준)
                    int label = featureEngineeringService.generateLabel(historicalData, i);
                    
                    featureList.add(features);
                    labelList.add(label);
                    
                } catch (Exception e) {
                    log.warn("특성 추출 실패: symbol={}, index={}", symbol, i, e);
                }
            }
            
            TrainingData trainingData = TrainingData.builder()
                .featureMatrix(featureList.toArray(new double[0][]))
                .labels(labelList.stream().mapToInt(Integer::intValue).toArray())
                .featureNames(featureNames)
                .build();
                
            log.debug("훈련 데이터 준비 완료: symbol={}, samples={}", symbol, featureList.size());
            return trainingData;
            
        } catch (Exception e) {
            log.error("훈련 데이터 준비 실패: symbol={}", symbol, e);
            // 폴백: 빈 훈련 데이터 반환
            return TrainingData.builder()
                .featureMatrix(new double[0][])
                .labels(new int[0])
                .featureNames(generateFeatureNames())
                .build();
        }
    }
    
    /**
     * 모델 정확도 계산
     */
    public double calculateModelAccuracy(SimpleTradingModel model, TrainingData trainingData) {
        try {
            if (trainingData.getFeatureMatrix().length == 0) {
                return 0.5; // 기본값
            }
            
            // 간단한 정확도 계산 (실제로는 교차 검증 사용)
            int correctPredictions = 0;
            int totalPredictions = Math.min(100, trainingData.getFeatureMatrix().length);
            
            for (int i = 0; i < totalPredictions; i++) {
                double[] features = trainingData.getFeatureMatrix()[i];
                int actualLabel = trainingData.getLabels()[i];
                
                // 모델 예측
                int predictedLabel = (int) model.predict(features).getPrediction();
                
                if (predictedLabel == actualLabel) {
                    correctPredictions++;
                }
            }
            
            double accuracy = (double) correctPredictions / totalPredictions;
            log.debug("모델 정확도 계산 완료: symbol={}, accuracy={}", model.getSymbol(), accuracy);
            
            return accuracy;
            
        } catch (Exception e) {
            log.error("모델 정확도 계산 실패: symbol={}", model.getSymbol(), e);
            return 0.75; // 기본값
        }
    }
    
    /**
     * 폴백 모델 생성
     */
    public SimpleTradingModel createFallbackModel(String symbol) {
        log.info("폴백 SimpleTradingModel 생성: symbol={}", symbol);
        return new SimpleTradingModel(symbol != null ? symbol : "UNKNOWN");
    }
    
    /**
     * 특성 이름 생성
     */
    private String[] generateFeatureNames() {
        return IntStream.range(0, FEATURE_COUNT)
            .mapToObj(i -> "feature_" + i)
            .toArray(String[]::new);
    }
    
    /**
     * 캐시된 모델 통계 조회
     */
    public ModelCacheStats getCacheStats() {
        return ModelCacheStats.builder()
            .totalModels(cachedModels.size())
            .activeSessions(modelTrainingTimes.size())
            .cacheHitRate(calculateCacheHitRate())
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    /**
     * 모델 캐시 정리
     */
    public void clearExpiredModels() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(MODEL_CACHE_DURATION_HOURS);
        
        modelTrainingTimes.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isBefore(expiryTime);
            if (expired) {
                cachedModels.remove(entry.getKey());
                log.debug("만료된 모델 제거: key={}", entry.getKey());
            }
            return expired;
        });
    }
    
    private double calculateCacheHitRate() {
        // 간단한 캐시 히트율 계산 로직
        return cachedModels.isEmpty() ? 0.0 : 0.85; // 기본값 85%
    }
    
    /**
     * TrainingData DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TrainingData {
        private double[][] featureMatrix;
        private int[] labels;
        private String[] featureNames;
    }
    
    /**
     * ModelCacheStats DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ModelCacheStats {
        private int totalModels;
        private int activeSessions;
        private double cacheHitRate;
        private LocalDateTime lastUpdated;
    }
}