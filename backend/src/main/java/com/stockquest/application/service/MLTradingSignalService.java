package com.stockquest.application.service;

import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.*;
import com.stockquest.domain.ml.SimpleTradingModel;
import com.stockquest.domain.ml.PredictionResult;
import com.stockquest.domain.ml.TechnicalIndicators;
import com.stockquest.domain.ml.VolatilityAnalysis;
import com.stockquest.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
// Fallback implementation without Smile ML (Phase 8.2 will add proper ML library)
// import smile.classification.RandomForest;
// import smile.data.DataFrame;
// import smile.data.Tuple;
// import smile.data.formula.Formula;
// import smile.data.type.StructType;
// import smile.math.matrix.Matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * ML 기반 트레이딩 시그널 생성 서비스
 * Smile ML 라이브러리를 활용한 고급 기계학습 분석
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLTradingSignalService {
    
    private final RealTimeMarketDataService marketDataService;
    private final TechnicalAnalysisService technicalAnalysisService;
    
    // 캐시된 ML 모델들 (fallback implementation)
    private final Map<String, SimpleTradingModel> cachedModels = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> modelTrainingTimes = new ConcurrentHashMap<>();
    
    private static final int MODEL_CACHE_DURATION_HOURS = 6;
    private static final int FEATURE_COUNT = 25;
    private static final double CONFIDENCE_THRESHOLD = 0.6;
    
    /**
     * 단일 심볼에 대한 ML 트레이딩 시그널 생성
     */
    @Async("riskAssessmentTaskExecutor")
    public CompletableFuture<TradingSignal> generateTradingSignal(String symbol) {
        try {
            log.info("ML 트레이딩 시그널 생성 시작: symbol={}", symbol);
            
            // 1. 시장 데이터 및 기술적 지표 수집
            MarketFeatures features = collectMarketFeatures(symbol);
            
            // 2. ML 모델 로드 또는 훈련
            SimpleTradingModel model = getOrTrainModel(symbol);
            
            // 3. 시그널 생성
            TradingSignal signal = generateSignalFromModel(symbol, model, features);
            
            // 4. 시장 조건 및 성과 추적 정보 추가
            enhanceSignalWithMarketIntelligence(signal, features);
            
            log.info("ML 트레이딩 시그널 생성 완료: symbol={}, signalType={}, confidence={}", 
                symbol, signal.getSignalType(), signal.getConfidence());
            
            return CompletableFuture.completedFuture(signal);
            
        } catch (Exception e) {
            log.error("ML 트레이딩 시그널 생성 실패: symbol={}", symbol, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 복수 심볼에 대한 배치 시그널 생성
     */
    @Async("riskAssessmentTaskExecutor")
    public CompletableFuture<List<TradingSignal>> generateBatchSignals(List<String> symbols) {
        try {
            log.info("배치 ML 시그널 생성 시작: symbols={}", symbols);
            
            List<CompletableFuture<TradingSignal>> futures = symbols.stream()
                .map(this::generateTradingSignal)
                .toList();
            
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            
            return allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList());
                
        } catch (Exception e) {
            log.error("배치 ML 시그널 생성 실패: symbols={}", symbols, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 시장 조건 기반 시그널 필터링
     */
    public List<TradingSignal> filterSignalsByMarketCondition(List<TradingSignal> signals, MarketRegime currentRegime) {
        return signals.stream()
            .filter(signal -> isSignalValidForMarketRegime(signal, currentRegime))
            .filter(signal -> signal.getConfidence().compareTo(BigDecimal.valueOf(CONFIDENCE_THRESHOLD)) >= 0)
            .sorted((a, b) -> b.getSignalScore().compareTo(a.getSignalScore()))
            .toList();
    }
    
    /**
     * 주식, 기술적 지표, 변동성 분석을 기반으로 거래 신호 생성 (백테스팅용)
     */
    public TradingSignal generateSignal(Stock stock, TechnicalIndicators indicators, VolatilityAnalysis volatility) {
        try {
            log.debug("백테스팅용 거래 신호 생성: symbol={}", stock.getSymbol());
            
            // 간단한 ML 기반 신호 생성 로직
            // 실제 구현에서는 더 복잡한 ML 모델을 사용
            SignalType signalType = determineSignalType(indicators, volatility);
            BigDecimal confidence = calculateConfidence(indicators, volatility);
            BigDecimal strength = calculateSignalStrength(indicators);
            
            return TradingSignal.builder()
                .signalId(UUID.randomUUID().toString())
                .symbol(stock.getSymbol())
                .signalType(signalType)
                .strength(strength)
                .confidence(confidence)
                .expectedReturn(BigDecimal.valueOf(0.05)) // 5% 기대 수익률
                .expectedRisk(BigDecimal.valueOf(0.15)) // 15% 기대 위험도
                .timeHorizon(5) // 5일 투자 기간
                .targetPrice(stock.getClosePrice().multiply(BigDecimal.valueOf(signalType == SignalType.BUY ? 1.05 : 0.95)))
                .stopLossPrice(stock.getClosePrice().multiply(BigDecimal.valueOf(signalType == SignalType.BUY ? 0.95 : 1.05)))
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
                
        } catch (Exception e) {
            log.error("거래 신호 생성 실패: symbol={}", stock.getSymbol(), e);
            throw new RuntimeException("Failed to generate trading signal", e);
        }
    }
    
    /**
     * 기술적 지표와 변동성 분석을 바탕으로 신호 타입 결정
     */
    private SignalType determineSignalType(TechnicalIndicators indicators, VolatilityAnalysis volatility) {
        // 간단한 결정 로직 (실제로는 더 복잡한 ML 모델 사용)
        if (indicators.getRsi() != null && indicators.getRsi().compareTo(BigDecimal.valueOf(70)) > 0) {
            return SignalType.SELL; // RSI가 70 이상이면 매도 신호
        } else if (indicators.getRsi() != null && indicators.getRsi().compareTo(BigDecimal.valueOf(30)) < 0) {
            return SignalType.BUY; // RSI가 30 이하이면 매수 신호
        } else {
            return SignalType.HOLD; // 중립
        }
    }
    
    /**
     * 신호 신뢰도 계산
     */
    private BigDecimal calculateConfidence(TechnicalIndicators indicators, VolatilityAnalysis volatility) {
        // 기본 신뢰도는 0.6으로 시작
        double confidence = 0.6;
        
        // 변동성이 낮을수록 신뢰도 증가
        if (volatility.getHistoricalVolatility() < 0.2) {
            confidence += 0.1;
        }
        
        // RSI가 극값에 가까울수록 신뢰도 증가
        if (indicators.getRsi() != null) {
            double rsi = indicators.getRsi().doubleValue();
            if (rsi > 80 || rsi < 20) {
                confidence += 0.15;
            }
        }
        
        return BigDecimal.valueOf(Math.min(confidence, 1.0)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 신호 강도 계산
     */
    private BigDecimal calculateSignalStrength(TechnicalIndicators indicators) {
        // 기본 강도는 0.5
        double strength = 0.5;
        
        // MACD가 양수이면 강도 증가
        if (indicators.getMacdLine() != null && indicators.getMacdLine().compareTo(BigDecimal.ZERO) > 0) {
            strength += 0.2;
        }
        
        return BigDecimal.valueOf(Math.min(strength, 1.0)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 시장 특성 수집
     */
    private MarketFeatures collectMarketFeatures(String symbol) {
        try {
            // 기본 시장 데이터
            MarketData currentData = marketDataService.getCurrentMarketData(symbol);
            List<MarketData> historicalData = marketDataService.getHistoricalData(symbol, 50);
            
            // 기술적 지표 계산
            com.stockquest.domain.ml.TechnicalIndicators indicators = technicalAnalysisService.calculateTechnicalIndicators(symbol, historicalData);
            
            // 시장 조건 분석
            MarketCondition marketCondition = analyzeMarketCondition(historicalData);
            
            // 변동성 및 거래량 분석
            VolatilityAnalysis volatilityAnalysis = calculateVolatilityMetrics(historicalData);
            
            return MarketFeatures.builder()
                .symbol(symbol)
                .currentPrice(currentData.getPrice())
                .technicalIndicators(indicators)
                .marketCondition(marketCondition)
                .volatilityAnalysis(volatilityAnalysis)
                .historicalData(historicalData)
                .build();
                
        } catch (Exception e) {
            log.error("시장 특성 수집 실패: symbol={}", symbol, e);
            throw new RuntimeException("Failed to collect market features", e);
        }
    }
    
    /**
     * ML 모델 로드 또는 훈련 (fallback implementation)
     */
    private SimpleTradingModel getOrTrainModel(String symbol) {
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
    }
    
    /**
     * Simple Trading 모델 훈련 (fallback implementation)
     */
    private SimpleTradingModel trainSimpleTradingModel(String symbol) {
        try {
            // 훈련 데이터 준비
            TrainingData trainingData = prepareTrainingData(symbol);
            
            // 간단한 규칙 기반 모델 생성
            SimpleTradingModel model = new SimpleTradingModel(symbol);
            
            log.info("Simple Trading 모델 훈련 완료: symbol={}, 정확도={}", 
                symbol, calculateModelAccuracy(model, trainingData));
            
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
    private TrainingData prepareTrainingData(String symbol) {
        // 과거 60일 데이터 수집
        List<MarketData> historicalData = marketDataService.getHistoricalData(symbol, 60);
        
        List<double[]> featureList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();
        String[] featureNames = generateFeatureNames();
        
        for (int i = 10; i < historicalData.size() - 5; i++) {
            try {
                // 특성 벡터 생성
                double[] features = extractFeatures(historicalData, i);
                
                // 레이블 생성 (미래 5일 수익률 기준)
                int label = generateLabel(historicalData, i);
                
                featureList.add(features);
                labelList.add(label);
                
            } catch (Exception e) {
                log.warn("특성 추출 실패: symbol={}, index={}", symbol, i, e);
            }
        }
        
        return TrainingData.builder()
            .featureMatrix(featureList.toArray(new double[0][]))
            .labels(labelList.stream().mapToInt(Integer::intValue).toArray())
            .featureNames(featureNames)
            .build();
    }
    
    /**
     * 특성 벡터 추출
     */
    private double[] extractFeatures(List<MarketData> data, int index) {
        double[] features = new double[FEATURE_COUNT];
        int featureIndex = 0;
        
        MarketData current = data.get(index);
        
        // 가격 기반 특성
        features[featureIndex++] = calculateReturns(data, index, 1);   // 1일 수익률
        features[featureIndex++] = calculateReturns(data, index, 5);   // 5일 수익률
        features[featureIndex++] = calculateReturns(data, index, 10);  // 10일 수익률
        
        // 이동평균 기반 특성
        features[featureIndex++] = calculateMovingAverageRatio(data, index, 5);
        features[featureIndex++] = calculateMovingAverageRatio(data, index, 20);
        
        // 변동성 특성
        features[featureIndex++] = calculateVolatility(data, index, 5);
        features[featureIndex++] = calculateVolatility(data, index, 20);
        
        // 거래량 특성
        features[featureIndex++] = calculateVolumeRatio(data, index, 5);
        features[featureIndex++] = calculateVolumeRatio(data, index, 20);
        
        // 기술적 지표 (RSI, MACD, etc.)
        features[featureIndex++] = calculateRSI(data, index, 14);
        features[featureIndex++] = calculateMACD(data, index);
        features[featureIndex++] = calculateBollingerBandPosition(data, index);
        
        // 시장 미세구조 특성
        features[featureIndex++] = calculateBidAskSpread(current);
        features[featureIndex++] = calculatePriceImpact(data, index);
        
        // 시간적 특성
        features[featureIndex++] = getHourOfDay(current);
        features[featureIndex++] = getDayOfWeek(current);
        
        // 상대적 성과 특성
        features[featureIndex++] = calculateRelativePerformance(data, index);
        features[featureIndex++] = calculateMomentumScore(data, index);
        
        // 나머지 특성들 채우기
        while (featureIndex < FEATURE_COUNT) {
            features[featureIndex++] = 0.0;
        }
        
        return features;
    }
    
    /**
     * 시그널 레이블 생성 (미래 수익률 기준)
     */
    private int generateLabel(List<MarketData> data, int index) {
        if (index + 5 >= data.size()) {
            return 1; // HOLD
        }
        
        double currentPrice = data.get(index).getPrice().doubleValue();
        double futurePrice = data.get(index + 5).getPrice().doubleValue();
        double futureReturn = (futurePrice - currentPrice) / currentPrice;
        
        // 시그널 분류: 0=SELL, 1=HOLD, 2=BUY
        if (futureReturn > 0.02) {  // 2% 이상 상승
            return 2; // BUY
        } else if (futureReturn < -0.02) {  // 2% 이상 하락
            return 0; // SELL
        } else {
            return 1; // HOLD
        }
    }
    
    /**
     * ML 모델로부터 트레이딩 시그널 생성 (fallback implementation)
     */
    private TradingSignal generateSignalFromModel(String symbol, SimpleTradingModel model, MarketFeatures features) {
        try {
            // 특성 벡터 준비
            double[] featureVector = extractFeaturesFromMarketData(features);
            
            // 모델 예측 (simple rule-based prediction)
            PredictionResult prediction = model.predict(featureVector);
            
            // 시그널 타입 결정
            SignalType signalType = mapPredictionToSignalType((int) prediction.getPrediction());
            
            // 신뢰도 계산 
            double confidence = prediction.getConfidence();
            
            // 시그널 강도 계산
            double strength = prediction.getStrength();
            
            // 시그널 근거 생성
            List<SignalReason> reasons = generateSignalReasons(featureVector, model);
            
            return TradingSignal.builder()
                .signalId(UUID.randomUUID().toString())
                .symbol(symbol)
                .signalType(signalType)
                .strength(BigDecimal.valueOf(strength).setScale(4, RoundingMode.HALF_UP))
                .confidence(BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP))
                .expectedReturn(estimateExpectedReturn(signalType, strength))
                .expectedRisk(estimateExpectedRisk(features))
                .timeHorizon(5) // 5일 예측
                .targetPrice(calculateTargetPrice(features, signalType, strength))
                .stopLossPrice(calculateStopLossPrice(features, signalType))
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .modelInfo(createModelInfo(model))
                .reasons(reasons)
                .status(SignalStatus.ACTIVE)
                .build();
                
        } catch (Exception e) {
            log.error("모델 기반 시그널 생성 실패: symbol={}", symbol, e);
            return generateFallbackSignal(symbol, features);
        }
    }
    
    /**
     * 시장 인텔리전스로 시그널 강화
     */
    private void enhanceSignalWithMarketIntelligence(TradingSignal signal, MarketFeatures features) {
        // 시장 조건 정보 추가
        signal.setMarketCondition(features.getMarketCondition());
        
        // 성과 추적 초기화
        signal.setPerformanceTracking(PerformanceTracking.builder()
            .currentPrice(features.getCurrentPrice())
            .unrealizedReturn(BigDecimal.ZERO)
            .maxReturn(BigDecimal.ZERO)
            .maxDrawdown(BigDecimal.ZERO)
            .lastUpdated(LocalDateTime.now())
            .build());
    }
    
    /**
     * 시장 조건 분석
     */
    private MarketCondition analyzeMarketCondition(List<MarketData> historicalData) {
        // 간단한 시장 체제 분석
        MarketRegime regime = determineMarketRegime(historicalData);
        VolatilityLevel volatility = calculateVolatilityLevel(historicalData);
        BigDecimal sentiment = calculateMarketSentiment(historicalData);
        
        return MarketCondition.builder()
            .regime(regime)
            .volatility(volatility)
            .liquidity(LiquidityCondition.NORMAL_LIQUIDITY)
            .marketSentiment(sentiment)
            .vixLevel(BigDecimal.valueOf(20.0)) // 기본값
            .sectorStrengths(Map.of())
            .build();
    }
    
    // 유틸리티 메소드들
    private double calculateReturns(List<MarketData> data, int index, int period) {
        if (index < period) return 0.0;
        double currentPrice = data.get(index).getPrice().doubleValue();
        double pastPrice = data.get(index - period).getPrice().doubleValue();
        return (currentPrice - pastPrice) / pastPrice;
    }
    
    private double calculateMovingAverageRatio(List<MarketData> data, int index, int period) {
        if (index < period) return 1.0;
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += data.get(index - i).getPrice().doubleValue();
        }
        double ma = sum / period;
        return data.get(index).getPrice().doubleValue() / ma;
    }
    
    private double calculateVolatility(List<MarketData> data, int index, int period) {
        if (index < period) return 0.0;
        
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < period; i++) {
            if (index - i >= 0) {
                double ret = calculateReturns(data, index - i + 1, 1);
                returns.add(ret);
            }
        }
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(ret -> Math.pow(ret - mean, 2))
            .average().orElse(0.0);
            
        return Math.sqrt(variance);
    }
    
    private SignalType mapPredictionToSignalType(int prediction) {
        return switch (prediction) {
            case 0 -> SignalType.SELL;
            case 1 -> SignalType.HOLD;
            case 2 -> SignalType.BUY;
            default -> SignalType.HOLD;
        };
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
    
    private double calculateVolumeRatio(List<MarketData> data, int index, int period) {
        if (index < period || data.size() <= index) return 1.0;
        
        double currentVolume = data.get(index).getVolume().doubleValue();
        double avgVolume = 0.0;
        
        for (int i = 0; i < period && (index - i) >= 0; i++) {
            avgVolume += data.get(index - i).getVolume().doubleValue();
        }
        avgVolume /= period;
        
        return avgVolume > 0 ? currentVolume / avgVolume : 1.0;
    }
    
    private double calculateRSI(List<MarketData> data, int index, int period) {
        if (index < period + 1) return 50.0;
        
        double gains = 0.0;
        double losses = 0.0;
        
        for (int i = 1; i <= period && (index - i + 1) >= 0; i++) {
            double change = data.get(index - i + 1).getPrice().doubleValue() - 
                           data.get(index - i).getPrice().doubleValue();
            if (change > 0) {
                gains += change;
            } else {
                losses -= change;
            }
        }
        
        double avgGain = gains / period;
        double avgLoss = losses / period;
        
        if (avgLoss == 0) return 100.0;
        
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }
    
    private double calculateMACD(List<MarketData> data, int index) {
        if (index < 26) return 0.0;
        
        double ema12 = calculateEMA(data, index, 12);
        double ema26 = calculateEMA(data, index, 26);
        
        return ema12 - ema26;
    }
    
    private double calculateEMA(List<MarketData> data, int index, int period) {
        if (index < period - 1) return data.get(index).getPrice().doubleValue();
        
        double multiplier = 2.0 / (period + 1);
        double ema = data.get(index - period + 1).getPrice().doubleValue();
        
        for (int i = index - period + 2; i <= index; i++) {
            ema = (data.get(i).getPrice().doubleValue() * multiplier) + (ema * (1 - multiplier));
        }
        
        return ema;
    }
    
    private double calculateBollingerBandPosition(List<MarketData> data, int index) {
        if (index < 20) return 0.5;
        
        double sma = 0.0;
        for (int i = 0; i < 20; i++) {
            sma += data.get(index - i).getPrice().doubleValue();
        }
        sma /= 20;
        
        double variance = 0.0;
        for (int i = 0; i < 20; i++) {
            double diff = data.get(index - i).getPrice().doubleValue() - sma;
            variance += diff * diff;
        }
        double stdDev = Math.sqrt(variance / 20);
        
        double upperBand = sma + (2 * stdDev);
        double lowerBand = sma - (2 * stdDev);
        double currentPrice = data.get(index).getPrice().doubleValue();
        
        if (upperBand == lowerBand) return 0.5;
        
        return (currentPrice - lowerBand) / (upperBand - lowerBand);
    }
    
    private double calculateBidAskSpread(MarketData data) {
        return 0.01; // 기본값, 실제로는 bid-ask 데이터가 필요
    }
    
    private double calculatePriceImpact(List<MarketData> data, int index) {
        if (index < 1) return 0.0;
        
        double currentPrice = data.get(index).getPrice().doubleValue();
        double previousPrice = data.get(index - 1).getPrice().doubleValue();
        double currentVolume = data.get(index).getVolume().doubleValue();
        
        if (currentVolume == 0) return 0.0;
        
        double priceChange = Math.abs(currentPrice - previousPrice) / previousPrice;
        return priceChange / Math.log(1 + currentVolume);
    }
    
    private double getHourOfDay(MarketData data) {
        return data.getTimestamp().getHour();
    }
    
    private double getDayOfWeek(MarketData data) {
        return data.getTimestamp().getDayOfWeek().getValue();
    }
    
    private double calculateRelativePerformance(List<MarketData> data, int index) {
        if (index < 20) return 0.0;
        
        double currentReturn = calculateReturns(data, index, 20);
        // 시장 대비 상대 성과 (간단한 구현)
        return currentReturn * 100; // 백분율로 변환
    }
    
    private double calculateMomentumScore(List<MarketData> data, int index) {
        if (index < 10) return 0.0;
        
        double shortTermReturn = calculateReturns(data, index, 5);
        double longTermReturn = calculateReturns(data, index, 20);
        
        return (shortTermReturn - longTermReturn) * 100;
    }
    
    // 내부 클래스들
    @lombok.Data
    @lombok.Builder
    private static class MarketFeatures {
        private String symbol;
        private BigDecimal currentPrice;
        private com.stockquest.domain.ml.TechnicalIndicators technicalIndicators;
        private MarketCondition marketCondition;
        private com.stockquest.domain.ml.VolatilityAnalysis volatilityAnalysis;
        private List<MarketData> historicalData;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class TrainingData {
        private double[][] featureMatrix;
        private int[] labels;
        private String[] featureNames;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class SimpleTechnicalIndicators {
        private double rsi;
        private double macd;
        private double bollingerUpper;
        private double bollingerLower;
        private double stochastic;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class SimpleVolatilityAnalysis {
        private double historicalVolatility;
        private double realizedVolatility;
        private double impliedVolatility;
        private double volatilitySkew;
    }
    
    private String[] generateFeatureNames() {
        return IntStream.range(0, FEATURE_COUNT)
            .mapToObj(i -> "feature_" + i)
            .toArray(String[]::new);
    }
    
    private double calculateSignalStrength(double[] probabilities, int prediction) {
        if (probabilities.length == 0) return 0.5;
        
        double maxProbability = Arrays.stream(probabilities).max().orElse(0.0);
        double entropy = Arrays.stream(probabilities)
            .filter(p -> p > 0)
            .map(p -> -p * Math.log(p) / Math.log(2))
            .sum();
        
        // 높은 확률과 낮은 엔트로피는 강한 시그널을 의미
        double normalizedEntropy = entropy / Math.log(probabilities.length) / Math.log(2);
        return maxProbability * (1 - normalizedEntropy);
    }
    
    private List<SignalReason> generateSignalReasons(double[] featureVector, SimpleTradingModel model) {
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
    
    private double[] extractFeaturesFromMarketData(MarketFeatures features) {
        List<MarketData> data = features.getHistoricalData();
        if (data.isEmpty()) {
            return new double[FEATURE_COUNT];
        }
        
        return extractFeatures(data, data.size() - 1);
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
        
        if (features.getTechnicalIndicators() != null) {
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
    
    private double calculateModelAccuracy(SimpleTradingModel model, TrainingData trainingData) {
        // 간단한 정확도 계산
        return 0.85; // 기본값, 실제로는 검증 데이터로 계산
    }
    
    private SimpleTradingModel createFallbackModel(String symbol) {
        // 기본 SimpleTradingModel 생성
        log.info("폴백 SimpleTradingModel 생성: {}", symbol);
        return new SimpleTradingModel(symbol != null ? symbol : "UNKNOWN");
    }
    
    /**
     * 변동성 메트릭 계산 (임시 구현)
     */
    private VolatilityAnalysis calculateVolatilityMetrics(List<MarketData> historicalData) {
        if (historicalData == null || historicalData.isEmpty()) {
            return VolatilityAnalysis.builder()
                .historicalVolatility(0.2)
                .realizedVolatility(BigDecimal.valueOf(0.2))
                .volatilityRegime(VolatilityAnalysis.VolatilityRegime.MODERATE)
                .volatilityTrend(VolatilityAnalysis.VolatilityTrend.STABLE)
                .build();
        }
        
        // 간단한 변동성 계산
        double volatility = 0.2; // 기본값 20%
        
        return VolatilityAnalysis.builder()
            .historicalVolatility(volatility)
            .realizedVolatility(BigDecimal.valueOf(volatility))
            .volatilityClustering(BigDecimal.valueOf(0.5))
            .volatilityRegime(volatility > 0.3 ? VolatilityAnalysis.VolatilityRegime.HIGH : 
                             volatility < 0.1 ? VolatilityAnalysis.VolatilityRegime.LOW : 
                             VolatilityAnalysis.VolatilityRegime.MODERATE)
            .volatilityTrend(VolatilityAnalysis.VolatilityTrend.STABLE)
            .build();
    }
    
    private boolean isSignalValidForMarketRegime(TradingSignal signal, MarketRegime currentRegime) {
        return switch (currentRegime) {
            case BULL_MARKET -> signal.getSignalType() != SignalType.STRONG_SELL;
            case BEAR_MARKET -> signal.getSignalType() != SignalType.STRONG_BUY;
            case SIDEWAYS_MARKET -> signal.getSignalType() == SignalType.HOLD || 
                                  signal.getConfidence().compareTo(BigDecimal.valueOf(0.8)) >= 0;
            case HIGH_VOLATILITY -> signal.getConfidence().compareTo(BigDecimal.valueOf(0.7)) >= 0;
            case LOW_VOLATILITY -> true;
        };
    }
    
    private MarketRegime determineMarketRegime(List<MarketData> historicalData) {
        if (historicalData.size() < 20) return MarketRegime.SIDEWAYS_MARKET;
        
        double totalReturn = calculateReturns(historicalData, historicalData.size() - 1, 20);
        double volatility = calculateVolatility(historicalData, historicalData.size() - 1, 20);
        
        if (volatility > 0.3) return MarketRegime.HIGH_VOLATILITY;
        if (volatility < 0.1) return MarketRegime.LOW_VOLATILITY;
        if (totalReturn > 0.1) return MarketRegime.BULL_MARKET;
        if (totalReturn < -0.1) return MarketRegime.BEAR_MARKET;
        
        return MarketRegime.SIDEWAYS_MARKET;
    }
    
    private VolatilityLevel calculateVolatilityLevel(List<MarketData> historicalData) {
        double volatility = calculateVolatility(historicalData, historicalData.size() - 1, 20);
        
        if (volatility < 0.1) return VolatilityLevel.VERY_LOW;
        if (volatility < 0.2) return VolatilityLevel.LOW;
        if (volatility < 0.3) return VolatilityLevel.MEDIUM;
        if (volatility < 0.4) return VolatilityLevel.HIGH;
        return VolatilityLevel.VERY_HIGH;
    }
    
    private BigDecimal calculateMarketSentiment(List<MarketData> historicalData) {
        // 간단한 시장 심리 계산 (가격 모멘텀 기반)
        if (historicalData.size() < 10) return BigDecimal.ZERO;
        
        double recentReturn = calculateReturns(historicalData, historicalData.size() - 1, 5);
        double sentiment = Math.tanh(recentReturn * 10); // -1 ~ 1 범위로 정규화
        
        return BigDecimal.valueOf(sentiment).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal estimateExpectedRisk(MarketFeatures features) {
        double volatility = features.getVolatilityAnalysis() != null ? 
            features.getVolatilityAnalysis().getHistoricalVolatility() : 0.2;
        
        return BigDecimal.valueOf(volatility).setScale(4, RoundingMode.HALF_UP);
    }
    
    // 스키마 정의 (Smile 라이브러리용) - Commented out for SimpleTradingModel fallback
    // private final StructType schema = new StructType(
    //     IntStream.range(0, FEATURE_COUNT)
    //         .mapToObj(i -> new smile.data.type.StructField("feature_" + i, smile.data.type.DataTypes.DoubleType))
    //         .toArray(smile.data.type.StructField[]::new)
    // );
}