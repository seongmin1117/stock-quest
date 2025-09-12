package com.stockquest.application.service.ml;

import com.stockquest.domain.marketdata.MarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 특성 엔지니어링 및 기술적 지표 계산 서비스
 * Feature extraction, technical indicator calculation, and comprehensive feature vectors for ML
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureEngineeringService {
    
    private static final int FEATURE_COUNT = 25;
    
    /**
     * 특성 벡터 추출
     */
    public double[] extractFeatures(List<MarketData> data, int index) {
        try {
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
            
            log.debug("특성 추출 완료: index={}, featureCount={}", index, FEATURE_COUNT);
            return features;
            
        } catch (Exception e) {
            log.error("특성 추출 실패: index={}", index, e);
            // 폴백: 기본 특성 벡터 반환
            return new double[FEATURE_COUNT];
        }
    }
    
    /**
     * 시그널 레이블 생성 (미래 수익률 기준)
     */
    public int generateLabel(List<MarketData> data, int index) {
        try {
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
            
        } catch (Exception e) {
            log.error("레이블 생성 실패: index={}", index, e);
            return 1; // HOLD (기본값)
        }
    }
    
    // 기술적 지표 계산 메소드들
    
    /**
     * RSI 계산
     */
    public double calculateRSI(List<MarketData> data, int index, int period) {
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
    
    /**
     * MACD 계산
     */
    public double calculateMACD(List<MarketData> data, int index) {
        if (index < 26) return 0.0;
        
        double ema12 = calculateEMA(data, index, 12);
        double ema26 = calculateEMA(data, index, 26);
        
        return ema12 - ema26;
    }
    
    /**
     * EMA 계산
     */
    public double calculateEMA(List<MarketData> data, int index, int period) {
        if (index < period - 1) return data.get(index).getPrice().doubleValue();
        
        double multiplier = 2.0 / (period + 1);
        double ema = data.get(index - period + 1).getPrice().doubleValue();
        
        for (int i = index - period + 2; i <= index; i++) {
            ema = (data.get(i).getPrice().doubleValue() * multiplier) + (ema * (1 - multiplier));
        }
        
        return ema;
    }
    
    /**
     * 볼린저 밴드 포지션 계산
     */
    public double calculateBollingerBandPosition(List<MarketData> data, int index) {
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
}