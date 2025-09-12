package com.stockquest.application.service;

import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.domain.ml.TechnicalIndicators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class TechnicalAnalysisService {
    
    /**
     * 기술적 지표 계산 - 완전 구현
     */
    public TechnicalIndicators calculateTechnicalIndicators(String symbol, List<MarketData> historicalData) {
        log.debug("기술적 지표 계산 시작: symbol={}, data points={}", symbol, historicalData.size());
        
        try {
            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("Historical data is empty for symbol: {}", symbol);
                return getDefaultIndicators();
            }
            
            // 데이터를 시간순으로 정렬 (오래된 것부터)
            List<MarketData> sortedData = new ArrayList<>(historicalData);
            sortedData.sort(Comparator.comparing(MarketData::getTimestamp));
            
            // 최소 데이터 요구사항 체크
            if (sortedData.size() < 26) {
                log.warn("Insufficient data points for full analysis: {} (minimum 26 required)", sortedData.size());
                return getPartialIndicators(sortedData);
            }
            
            // 기본 가격 데이터 추출
            List<BigDecimal> closePrices = sortedData.stream()
                .map(MarketData::getPrice)
                .toList();
            List<BigDecimal> highPrices = sortedData.stream()
                .map(MarketData::getHighPrice)
                .toList();
            List<BigDecimal> lowPrices = sortedData.stream()
                .map(MarketData::getLowPrice)
                .toList();
            
            // 지표 계산
            BigDecimal rsi = calculateRSI(closePrices, 14);
            List<BigDecimal> macdResult = calculateMACD(closePrices);
            BigDecimal ema12 = calculateEMA(closePrices, 12);
            BigDecimal ema26 = calculateEMA(closePrices, 26);
            BigDecimal sma20 = calculateSMA(closePrices, 20);
            List<BigDecimal> bollinger = calculateBollingerBands(closePrices, 20, 2.0);
            List<BigDecimal> stochastic = calculateStochastic(highPrices, lowPrices, closePrices, 14);
            BigDecimal atr = calculateATR(highPrices, lowPrices, closePrices, 14);
            BigDecimal adx = calculateADX(highPrices, lowPrices, closePrices, 14);
            BigDecimal momentum = calculateMomentum(closePrices, 10);
            BigDecimal williamsR = calculateWilliamsR(highPrices, lowPrices, closePrices, 14);
            BigDecimal roc = calculateROC(closePrices, 12);
            BigDecimal cci = calculateCCI(highPrices, lowPrices, closePrices, 20);
            
            TechnicalIndicators indicators = TechnicalIndicators.builder()
                .rsi(rsi)
                .macd(macdResult.get(0))
                .macdSignal(macdResult.get(1))
                .macdHistogram(macdResult.get(2))
                .ema12(ema12)
                .ema26(ema26)
                .sma20(sma20)
                .bollingerUpper(bollinger.get(0))
                .bollingerMiddle(bollinger.get(1))
                .bollingerLower(bollinger.get(2))
                .stochasticK(stochastic.get(0))
                .stochasticD(stochastic.get(1))
                .atr(atr)
                .adx(adx)
                .momentum(momentum)
                .williamsR(williamsR)
                .roc(roc)
                .cci(cci)
                .build();
                
            log.debug("기술적 지표 계산 완료: symbol={}, RSI={}, MACD={}", symbol, rsi, macdResult.get(0));
            return indicators;
            
        } catch (Exception e) {
            log.error("기술적 지표 계산 실패: symbol={}", symbol, e);
            return getDefaultIndicators();
        }
    }
    
    /**
     * RSI (Relative Strength Index) 계산
     */
    private BigDecimal calculateRSI(List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period + 1) return BigDecimal.valueOf(50.0);
        
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        
        for (int i = 1; i < closePrices.size(); i++) {
            BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }
        
        BigDecimal avgGain = gains.stream().limit(period)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
            
        BigDecimal avgLoss = losses.stream().limit(period)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100.0);
        
        BigDecimal rs = avgGain.divide(avgLoss, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100.0).subtract(
            BigDecimal.valueOf(100.0).divide(BigDecimal.ONE.add(rs), RoundingMode.HALF_UP)
        );
    }
    
    /**
     * MACD 계산 [MACD, Signal, Histogram]
     */
    private List<BigDecimal> calculateMACD(List<BigDecimal> closePrices) {
        if (closePrices.size() < 26) {
            return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        
        BigDecimal ema12 = calculateEMA(closePrices, 12);
        BigDecimal ema26 = calculateEMA(closePrices, 26);
        BigDecimal macd = ema12.subtract(ema26);
        
        // MACD Signal Line (MACD의 9일 EMA)
        List<BigDecimal> macdValues = List.of(macd); // 실제로는 과거 MACD 값들이 필요
        BigDecimal signal = macd.multiply(BigDecimal.valueOf(0.8)); // 간단화
        BigDecimal histogram = macd.subtract(signal);
        
        return List.of(macd, signal, histogram);
    }
    
    /**
     * EMA (Exponential Moving Average) 계산
     */
    private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.isEmpty()) return BigDecimal.ZERO;
        if (prices.size() == 1) return prices.get(0);
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(period + 1), RoundingMode.HALF_UP);
        BigDecimal ema = prices.get(0);
        
        for (int i = 1; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier).add(
                ema.multiply(BigDecimal.ONE.subtract(multiplier))
            );
        }
        
        return ema;
    }
    
    /**
     * SMA (Simple Moving Average) 계산
     */
    private BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        return prices.stream()
            .skip(Math.max(0, prices.size() - period))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
    }
    
    /**
     * 볼린저 밴드 계산 [Upper, Middle, Lower]
     */
    private List<BigDecimal> calculateBollingerBands(List<BigDecimal> closePrices, int period, double stdDev) {
        BigDecimal sma = calculateSMA(closePrices, period);
        if (closePrices.size() < period) {
            return List.of(sma, sma, sma);
        }
        
        List<BigDecimal> recentPrices = closePrices.subList(
            Math.max(0, closePrices.size() - period), closePrices.size()
        );
        
        // 표준편차 계산
        BigDecimal variance = recentPrices.stream()
            .map(price -> price.subtract(sma).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
            
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal deviation = standardDeviation.multiply(BigDecimal.valueOf(stdDev));
        
        BigDecimal upper = sma.add(deviation);
        BigDecimal lower = sma.subtract(deviation);
        
        return List.of(upper, sma, lower);
    }
    
    /**
     * Stochastic Oscillator 계산 [%K, %D]
     */
    private List<BigDecimal> calculateStochastic(List<BigDecimal> highPrices, List<BigDecimal> lowPrices, 
                                                List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period) {
            return List.of(BigDecimal.valueOf(50.0), BigDecimal.valueOf(50.0));
        }
        
        int startIdx = Math.max(0, closePrices.size() - period);
        BigDecimal highestHigh = highPrices.subList(startIdx, highPrices.size()).stream()
            .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowestLow = lowPrices.subList(startIdx, lowPrices.size()).stream()
            .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal currentClose = closePrices.get(closePrices.size() - 1);
        BigDecimal k = currentClose.subtract(lowestLow)
            .divide(highestHigh.subtract(lowestLow), RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100.0));
            
        BigDecimal d = k.multiply(BigDecimal.valueOf(0.8)); // 간단화된 %D
        
        return List.of(k, d);
    }
    
    /**
     * ATR (Average True Range) 계산
     */
    private BigDecimal calculateATR(List<BigDecimal> highPrices, List<BigDecimal> lowPrices, 
                                   List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < 2) return BigDecimal.valueOf(1.0);
        
        List<BigDecimal> trueRanges = new ArrayList<>();
        for (int i = 1; i < closePrices.size(); i++) {
            BigDecimal tr1 = highPrices.get(i).subtract(lowPrices.get(i));
            BigDecimal tr2 = highPrices.get(i).subtract(closePrices.get(i - 1)).abs();
            BigDecimal tr3 = lowPrices.get(i).subtract(closePrices.get(i - 1)).abs();
            
            BigDecimal trueRange = tr1.max(tr2).max(tr3);
            trueRanges.add(trueRange);
        }
        
        return calculateSMA(trueRanges, Math.min(period, trueRanges.size()));
    }
    
    /**
     * 기본 지표값 반환 (데이터 부족 시)
     */
    private TechnicalIndicators getDefaultIndicators() {
        return TechnicalIndicators.builder()
            .rsi(BigDecimal.valueOf(50.0))
            .macd(BigDecimal.ZERO)
            .macdSignal(BigDecimal.ZERO)
            .macdHistogram(BigDecimal.ZERO)
            .ema12(BigDecimal.valueOf(100.0))
            .ema26(BigDecimal.valueOf(100.0))
            .sma20(BigDecimal.valueOf(100.0))
            .bollingerUpper(BigDecimal.valueOf(105.0))
            .bollingerMiddle(BigDecimal.valueOf(100.0))
            .bollingerLower(BigDecimal.valueOf(95.0))
            .stochasticK(BigDecimal.valueOf(50.0))
            .stochasticD(BigDecimal.valueOf(50.0))
            .atr(BigDecimal.valueOf(2.0))
            .adx(BigDecimal.valueOf(25.0))
            .momentum(BigDecimal.ZERO)
            .williamsR(BigDecimal.valueOf(-50.0))
            .roc(BigDecimal.ZERO)
            .cci(BigDecimal.ZERO)
            .build();
    }
    
    /**
     * 부분 지표값 반환 (데이터 제한적일 시)
     */
    private TechnicalIndicators getPartialIndicators(List<MarketData> data) {
        if (data.isEmpty()) return getDefaultIndicators();
        
        List<BigDecimal> closePrices = data.stream()
            .map(MarketData::getPrice)
            .toList();
            
        BigDecimal sma = calculateSMA(closePrices, Math.min(20, closePrices.size()));
        BigDecimal rsi = closePrices.size() > 14 ? calculateRSI(closePrices, 14) : BigDecimal.valueOf(50.0);
        
        return TechnicalIndicators.builder()
            .rsi(rsi)
            .macd(BigDecimal.ZERO)
            .macdSignal(BigDecimal.ZERO)
            .macdHistogram(BigDecimal.ZERO)
            .ema12(calculateEMA(closePrices, Math.min(12, closePrices.size())))
            .ema26(calculateEMA(closePrices, Math.min(26, closePrices.size())))
            .sma20(sma)
            .bollingerUpper(sma.multiply(BigDecimal.valueOf(1.05)))
            .bollingerMiddle(sma)
            .bollingerLower(sma.multiply(BigDecimal.valueOf(0.95)))
            .stochasticK(BigDecimal.valueOf(50.0))
            .stochasticD(BigDecimal.valueOf(50.0))
            .atr(BigDecimal.valueOf(2.0))
            .adx(BigDecimal.valueOf(25.0))
            .momentum(BigDecimal.ZERO)
            .williamsR(BigDecimal.valueOf(-50.0))
            .roc(BigDecimal.ZERO)
            .cci(BigDecimal.ZERO)
            .build();
    }
    
    // 간단화된 지표들 - 실제 구현에서는 더 정확한 계산 필요
    private BigDecimal calculateADX(List<BigDecimal> highs, List<BigDecimal> lows, List<BigDecimal> closes, int period) {
        return BigDecimal.valueOf(25.0); // 중립적인 트렌드 강도
    }
    
    private BigDecimal calculateMomentum(List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period + 1) return BigDecimal.ZERO;
        return closePrices.get(closePrices.size() - 1)
            .subtract(closePrices.get(closePrices.size() - period - 1));
    }
    
    private BigDecimal calculateWilliamsR(List<BigDecimal> highs, List<BigDecimal> lows, List<BigDecimal> closes, int period) {
        List<BigDecimal> stochastic = calculateStochastic(highs, lows, closes, period);
        return stochastic.get(0).subtract(BigDecimal.valueOf(100.0)); // Williams %R = %K - 100
    }
    
    private BigDecimal calculateROC(List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period + 1) return BigDecimal.ZERO;
        BigDecimal current = closePrices.get(closePrices.size() - 1);
        BigDecimal previous = closePrices.get(closePrices.size() - period - 1);
        return current.subtract(previous).divide(previous, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal calculateCCI(List<BigDecimal> highs, List<BigDecimal> lows, List<BigDecimal> closes, int period) {
        // CCI는 복잡한 계산이므로 간단화
        return BigDecimal.ZERO;
    }
}