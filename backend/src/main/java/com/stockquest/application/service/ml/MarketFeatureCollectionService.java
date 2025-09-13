package com.stockquest.application.service.ml;

import com.stockquest.application.service.RealTimeMarketDataService;
import com.stockquest.application.service.TechnicalAnalysisService;
import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.domain.ml.TechnicalIndicators;
import com.stockquest.domain.ml.VolatilityAnalysis;
import com.stockquest.domain.ml.TradingSignal.MarketCondition;
import com.stockquest.domain.ml.TradingSignal.MarketRegime;
import com.stockquest.domain.ml.TradingSignal.VolatilityLevel;
import com.stockquest.domain.ml.TradingSignal.LiquidityCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 시장 특성 수집 및 기본 시장 조건 분석 서비스
 * Market data collection and integration with basic market condition analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketFeatureCollectionService {
    
    private final RealTimeMarketDataService marketDataService;
    private final TechnicalAnalysisService technicalAnalysisService;
    
    /**
     * 시장 특성 수집
     */
    public MarketFeatures collectMarketFeatures(String symbol) {
        try {
            log.debug("시장 특성 수집 시작: symbol={}", symbol);
            
            // 기본 시장 데이터
            MarketData currentData = marketDataService.getCurrentMarketData(symbol);
            List<MarketData> historicalData = marketDataService.getHistoricalData(symbol, 50);
            
            // 기술적 지표 계산
            TechnicalIndicators indicators = technicalAnalysisService.calculateTechnicalIndicators(symbol, historicalData);
            
            // 시장 조건 분석
            MarketCondition marketCondition = analyzeMarketCondition(historicalData);
            
            // 변동성 및 거래량 분석
            VolatilityAnalysis volatilityAnalysis = calculateVolatilityMetrics(historicalData);
            
            MarketFeatures features = MarketFeatures.builder()
                .symbol(symbol)
                .currentPrice(currentData.getPrice())
                .technicalIndicators(indicators)
                .marketCondition(marketCondition)
                .volatilityAnalysis(volatilityAnalysis)
                .historicalData(historicalData)
                .build();
                
            log.debug("시장 특성 수집 완료: symbol={}, dataPoints={}", symbol, historicalData.size());
            return features;
                
        } catch (Exception e) {
            log.error("시장 특성 수집 실패: symbol={}", symbol, e);
            throw new RuntimeException("Failed to collect market features for symbol: " + symbol, e);
        }
    }
    
    /**
     * 시장 조건 분석
     */
    public MarketCondition analyzeMarketCondition(List<MarketData> historicalData) {
        try {
            log.debug("시장 조건 분석 시작: dataPoints={}", historicalData.size());
            
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
                
        } catch (Exception e) {
            log.error("시장 조건 분석 실패", e);
            // 폴백: 기본 시장 조건 반환
            return MarketCondition.builder()
                .regime(MarketRegime.SIDEWAYS_MARKET)
                .volatility(VolatilityLevel.MEDIUM)
                .liquidity(LiquidityCondition.NORMAL_LIQUIDITY)
                .marketSentiment(BigDecimal.ZERO)
                .vixLevel(BigDecimal.valueOf(20.0))
                .sectorStrengths(Map.of())
                .build();
        }
    }
    
    /**
     * 변동성 메트릭 계산
     */
    public VolatilityAnalysis calculateVolatilityMetrics(List<MarketData> historicalData) {
        try {
            if (historicalData == null || historicalData.isEmpty()) {
                return createDefaultVolatilityAnalysis();
            }
            
            // 간단한 변동성 계산
            double volatility = calculateVolatility(historicalData, historicalData.size() - 1, 20);
            
            return VolatilityAnalysis.builder()
                .historicalVolatility(volatility)
                .realizedVolatility(BigDecimal.valueOf(volatility))
                .volatilityClustering(BigDecimal.valueOf(0.5))
                .volatilityRegime(determineVolatilityRegime(volatility))
                .volatilityTrend(VolatilityAnalysis.VolatilityTrend.STABLE)
                .build();
                
        } catch (Exception e) {
            log.error("변동성 메트릭 계산 실패", e);
            return createDefaultVolatilityAnalysis();
        }
    }
    
    // 유틸리티 메소드들
    private double calculateReturns(List<MarketData> data, int index, int period) {
        if (index < period) return 0.0;
        double currentPrice = data.get(index).getPrice().doubleValue();
        double pastPrice = data.get(index - period).getPrice().doubleValue();
        return (currentPrice - pastPrice) / pastPrice;
    }
    
    private double calculateVolatility(List<MarketData> data, int index, int period) {
        if (index < period) return 0.2; // 기본값
        
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
    
    private VolatilityAnalysis.VolatilityRegime determineVolatilityRegime(double volatility) {
        if (volatility > 0.3) return VolatilityAnalysis.VolatilityRegime.HIGH;
        if (volatility < 0.1) return VolatilityAnalysis.VolatilityRegime.LOW;
        return VolatilityAnalysis.VolatilityRegime.MODERATE;
    }
    
    private VolatilityAnalysis createDefaultVolatilityAnalysis() {
        return VolatilityAnalysis.builder()
            .historicalVolatility(0.2)
            .realizedVolatility(BigDecimal.valueOf(0.2))
            .volatilityRegime(VolatilityAnalysis.VolatilityRegime.MODERATE)
            .volatilityTrend(VolatilityAnalysis.VolatilityTrend.STABLE)
            .build();
    }
    
    /**
     * MarketFeatures DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class MarketFeatures {
        private String symbol;
        private BigDecimal currentPrice;
        private TechnicalIndicators technicalIndicators;
        private MarketCondition marketCondition;
        private VolatilityAnalysis volatilityAnalysis;
        private List<MarketData> historicalData;
    }
}