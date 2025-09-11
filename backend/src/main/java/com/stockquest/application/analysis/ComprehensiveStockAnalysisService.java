package com.stockquest.application.analysis;

import com.stockquest.application.marketdata.RealTimeMarketDataService;
import com.stockquest.application.strategy.TradingStrategyEngine;
import com.stockquest.domain.stock.Stock;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 종합적인 주식 분석 서비스
 * 실시간 데이터, AI 전략, 기술적/기본적 분석을 통합하여 포괄적인 주식 분석 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComprehensiveStockAnalysisService {

    private final RealTimeMarketDataService realTimeMarketDataService;
    private final TradingStrategyEngine tradingStrategyEngine;
    
    // 분석 결과 캐시 (5분 TTL)
    private final Map<String, StockAnalysisResult> analysisCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    
    private static final int CACHE_TTL_MINUTES = 5;

    /**
     * 종합 주식 분석 실행
     * 실시간 데이터 + AI 전략 + 기술적 분석 + 기본적 분석을 통합
     */
    public Mono<StockAnalysisResult> performComprehensiveAnalysis(String symbol) {
        
        // 캐시 확인 (5분 이내)
        if (isCacheValid(symbol)) {
            log.info("🎯 Cache hit for stock analysis: {}", symbol);
            return Mono.just(analysisCache.get(symbol));
        }
        
        log.info("🔍 Starting comprehensive analysis for symbol: {}", symbol);
        
        return Mono.fromCallable(() -> {
            // 1. 실시간 시장 데이터 수집
            // TODO: Implement proper data retrieval from RealTimeMarketDataService
            Object realTimeData = createMockRealTimeData(symbol);
            Object marketDepth = createMockMarketDepth(symbol);
            Object technicalIndicators = createMockTechnicalIndicators(symbol);
            
            // 2. AI 전략 분석
            var strategyAnalysis = performAIStrategyAnalysis(symbol, realTimeData);
            
            // 3. 기술적 분석
            var technicalAnalysis = performTechnicalAnalysis(symbol, technicalIndicators);
            
            // 4. 기본적 분석 (재무 데이터 기반)
            var fundamentalAnalysis = performFundamentalAnalysis(symbol);
            
            // 5. 종합 평가 및 점수 계산
            var overallRating = calculateOverallRating(strategyAnalysis, technicalAnalysis, fundamentalAnalysis);
            
            // 6. 리스크 평가
            var riskAssessment = performRiskAssessment(symbol, realTimeData, marketDepth);
            
            // 7. 투자 추천 생성
            var investmentRecommendation = generateInvestmentRecommendation(
                overallRating, riskAssessment, strategyAnalysis
            );
            
            // Cast mock objects to access methods (temporary solution)
            var mockRealTimeData = (Object) realTimeData;
            BigDecimal currentPrice = BigDecimal.valueOf(100 + Math.random() * 400);
            Double changePercent = (Math.random() - 0.5) * 10;
            Long volume = (long)(Math.random() * 1000000);
            
            StockAnalysisResult result = StockAnalysisResult.builder()
                .symbol(symbol)
                .analysisTimestamp(LocalDateTime.now())
                .currentPrice(currentPrice)
                .priceChangePercent(changePercent)
                .volume24h(volume)
                .marketCap(calculateMarketCap(symbol, currentPrice))
                .strategyAnalysis(strategyAnalysis)
                .technicalAnalysis(technicalAnalysis)
                .fundamentalAnalysis(fundamentalAnalysis)
                .overallRating(overallRating)
                .riskAssessment(riskAssessment)
                .investmentRecommendation(investmentRecommendation)
                .keyInsights(generateKeyInsights(strategyAnalysis, technicalAnalysis, fundamentalAnalysis))
                .priceTargets(calculatePriceTargets(currentPrice, overallRating))
                .build();
            
            // 캐시에 저장
            analysisCache.put(symbol, result);
            cacheTimestamps.put(symbol, LocalDateTime.now());
            
            log.info("✅ Comprehensive analysis completed for {}: Overall Rating = {}", 
                symbol, overallRating);
            
            return result;
            
        }).doOnError(error -> 
            log.error("❌ Error during comprehensive analysis for {}: {}", symbol, error.getMessage())
        );
    }
    
    /**
     * 다중 주식 동시 분석 (병렬 처리)
     */
    public Flux<StockAnalysisResult> performBulkAnalysis(List<String> symbols) {
        log.info("🔄 Starting bulk analysis for {} symbols", symbols.size());
        
        return Flux.fromIterable(symbols)
            .parallel(4) // 4개 병렬 스레드
            .flatMap(this::performComprehensiveAnalysis)
            .sequential()
            .doOnComplete(() -> log.info("✅ Bulk analysis completed for {} symbols", symbols.size()));
    }
    
    /**
     * AI 전략 기반 분석
     */
    private StrategyAnalysisResult performAIStrategyAnalysis(String symbol, Object realTimeData) {
        
        // 가상의 세션 ID로 전략 분석 수행 (실제로는 사용자별 세션 사용)
        Long mockSessionId = 1L;
        List<String> allStrategies = Arrays.asList(
            "MOMENTUM", "VALUE", "GROWTH", "MEAN_REVERSION", "QUANTITATIVE"
        );
        
        try {
            var strategyResponse = tradingStrategyEngine.analyzeAndRecommend(
                mockSessionId, allStrategies, 5, "MEDIUM"
            );
            
            return StrategyAnalysisResult.builder()
                .recommendedAction(strategyResponse.getOverallRecommendation().getAction())
                .confidence(strategyResponse.getOverallRecommendation().getConfidence())
                .primaryStrategy(strategyResponse.getTopStrategy().getType())
                .strategyScores(extractStrategyScores(strategyResponse))
                .aiInsights(strategyResponse.getOverallRecommendation().getReasoning())
                .build();
                
        } catch (Exception e) {
            log.warn("⚠️ Strategy analysis failed for {}, using fallback: {}", symbol, e.getMessage());
            return createFallbackStrategyAnalysis(symbol, realTimeData);
        }
    }
    
    /**
     * 기술적 분석 수행
     */
    private TechnicalAnalysisResult performTechnicalAnalysis(String symbol, Object indicators) {
        
        // Generate mock technical indicator values
        double rsi = 30 + Math.random() * 40;
        double macd = (Math.random() - 0.5) * 2;
        double macdSignal = (Math.random() - 0.5) * 2;
        BigDecimal price = BigDecimal.valueOf(100 + Math.random() * 400);
        BigDecimal bollUpper = price.multiply(BigDecimal.valueOf(1.02));
        BigDecimal bollLower = price.multiply(BigDecimal.valueOf(0.98));
        
        // RSI 기반 과매수/과매도 판단
        String rsiSignal = "NEUTRAL";
        if (rsi > 70) rsiSignal = "OVERBOUGHT";
        else if (rsi < 30) rsiSignal = "OVERSOLD";
        
        // MACD 기반 추세 판단
        String trendSignal = macd > macdSignal ? "BULLISH" : "BEARISH";
        
        // 볼린저 밴드 기반 변동성 분석
        String volatilityLevel = "MEDIUM";
        double bollSpread = bollUpper.subtract(bollLower).doubleValue();
        if (bollSpread > price.doubleValue() * 0.1) volatilityLevel = "HIGH";
        else if (bollSpread < price.doubleValue() * 0.05) volatilityLevel = "LOW";
        
        // 기술적 종합 점수 (1-10)
        int technicalScore = calculateTechnicalScore(indicators, rsiSignal, trendSignal);
        
        return TechnicalAnalysisResult.builder()
            .rsi(rsi)
            .rsiSignal(rsiSignal)
            .macd(macd)
            .macdSignal(macdSignal)
            .trendDirection(trendSignal)
            .bollingerPosition("MIDDLE") // Mock bollinger position
            .volatilityLevel(volatilityLevel)
            .technicalScore(technicalScore)
            .supportLevel(price.doubleValue() * 0.95) // 5% 하방 지지선
            .resistanceLevel(price.doubleValue() * 1.05) // 5% 상방 저항선
            .technicalSummary(generateTechnicalSummary(rsiSignal, trendSignal, volatilityLevel))
            .build();
    }
    
    /**
     * 기본적 분석 수행 (모의 데이터 기반)
     */
    private FundamentalAnalysisResult performFundamentalAnalysis(String symbol) {
        
        // 실제로는 외부 API에서 재무 데이터를 가져와야 함
        // 여기서는 모의 데이터로 기본적 분석 시뮬레이션
        
        Random random = new Random(symbol.hashCode()); // 일관된 모의 데이터를 위한 시드
        
        double pe = 15 + random.nextDouble() * 20; // 15-35 P/E 비율
        double pb = 1 + random.nextDouble() * 3;   // 1-4 P/B 비율
        double roe = 5 + random.nextDouble() * 25; // 5-30% ROE
        double debtRatio = random.nextDouble() * 0.6; // 0-60% 부채비율
        double revenueGrowth = -10 + random.nextDouble() * 30; // -10% ~ +20% 매출 성장률
        
        // 기본적 분석 점수 계산
        int fundamentalScore = calculateFundamentalScore(pe, pb, roe, debtRatio, revenueGrowth);
        
        return FundamentalAnalysisResult.builder()
            .peRatio(BigDecimal.valueOf(pe).setScale(2, RoundingMode.HALF_UP))
            .pbRatio(BigDecimal.valueOf(pb).setScale(2, RoundingMode.HALF_UP))
            .roe(BigDecimal.valueOf(roe).setScale(2, RoundingMode.HALF_UP))
            .debtToEquityRatio(BigDecimal.valueOf(debtRatio).setScale(3, RoundingMode.HALF_UP))
            .revenueGrowth(BigDecimal.valueOf(revenueGrowth).setScale(2, RoundingMode.HALF_UP))
            .fundamentalScore(fundamentalScore)
            .valueRating(pe < 20 ? "UNDERVALUED" : pe > 30 ? "OVERVALUED" : "FAIR_VALUE")
            .financialHealthRating(debtRatio < 0.3 ? "STRONG" : debtRatio > 0.5 ? "WEAK" : "MODERATE")
            .growthPotential(revenueGrowth > 10 ? "HIGH" : revenueGrowth < 0 ? "LOW" : "MODERATE")
            .fundamentalSummary(generateFundamentalSummary(pe, roe, revenueGrowth))
            .build();
    }
    
    /**
     * 종합 평가 점수 계산 (1-10점)
     */
    private int calculateOverallRating(
        StrategyAnalysisResult strategy, 
        TechnicalAnalysisResult technical, 
        FundamentalAnalysisResult fundamental
    ) {
        // 가중 평균: 전략 40%, 기술적 30%, 기본적 30%
        double weightedScore = 
            (strategy.getConfidence() * 10 * 0.4) +
            (technical.getTechnicalScore() * 0.3) +
            (fundamental.getFundamentalScore() * 0.3);
            
        return Math.max(1, Math.min(10, (int) Math.round(weightedScore)));
    }
    
    /**
     * 리스크 평가 수행
     */
    private RiskAssessmentResult performRiskAssessment(String symbol, Object realTimeData, Object marketDepth) {
        
        // Generate mock risk assessment values
        double changePercent = (Math.random() - 0.5) * 10;
        long volume = (long)(Math.random() * 1000000);
        BigDecimal price = BigDecimal.valueOf(100 + Math.random() * 400);
        BigDecimal bidAskSpread = BigDecimal.valueOf(0.01 + Math.random() * 0.05);
        
        // 변동성 리스크 (최근 가격 변화 기반)
        double volatilityRisk = Math.abs(changePercent) > 5 ? 0.8 : 0.4;
        
        // 유동성 리스크 (거래량 기반)
        double liquidityRisk = volume < 100000 ? 0.7 : 0.3;
        
        // 시장 심도 리스크 (호가창 분석)
        double marketDepthRisk = bidAskSpread.doubleValue() > price.doubleValue() * 0.01 ? 0.6 : 0.3;
        
        // 종합 리스크 점수 (0-1)
        double overallRisk = (volatilityRisk + liquidityRisk + marketDepthRisk) / 3;
        
        String riskLevel = overallRisk > 0.7 ? "HIGH" : overallRisk > 0.4 ? "MEDIUM" : "LOW";
        
        return RiskAssessmentResult.builder()
            .overallRiskScore(BigDecimal.valueOf(overallRisk).setScale(3, RoundingMode.HALF_UP))
            .riskLevel(riskLevel)
            .volatilityRisk(BigDecimal.valueOf(volatilityRisk).setScale(3, RoundingMode.HALF_UP))
            .liquidityRisk(BigDecimal.valueOf(liquidityRisk).setScale(3, RoundingMode.HALF_UP))
            .marketDepthRisk(BigDecimal.valueOf(marketDepthRisk).setScale(3, RoundingMode.HALF_UP))
            .riskFactors(identifyRiskFactors(volatilityRisk, liquidityRisk, marketDepthRisk))
            .riskMitigationSuggestions(generateRiskMitigationSuggestions(riskLevel))
            .build();
    }
    
    // Helper methods...
    
    private boolean isCacheValid(String symbol) {
        LocalDateTime cacheTime = cacheTimestamps.get(symbol);
        return cacheTime != null && 
               cacheTime.isAfter(LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES));
    }
    
    private BigDecimal calculateMarketCap(String symbol, BigDecimal price) {
        // 모의 시가총액 계산 (실제로는 발행주식수 * 주가)
        return price.multiply(BigDecimal.valueOf(1000000 + symbol.hashCode() % 10000000));
    }
    
    private Map<String, Integer> extractStrategyScores(Object strategyResponse) {
        // Mock 전략별 점수 추출
        Map<String, Integer> scores = new HashMap<>();
        scores.put("MOMENTUM", (int)(Math.random() * 10) + 1);
        scores.put("VALUE", (int)(Math.random() * 10) + 1);
        scores.put("GROWTH", (int)(Math.random() * 10) + 1);
        scores.put("MEAN_REVERSION", (int)(Math.random() * 10) + 1);
        scores.put("QUANTITATIVE", (int)(Math.random() * 10) + 1);
        return scores;
    }
    
    private StrategyAnalysisResult createFallbackStrategyAnalysis(String symbol, Object realTimeData) {
        // 전략 분석 실패시 폴백
        return StrategyAnalysisResult.builder()
            .recommendedAction("HOLD")
            .confidence(0.5)
            .primaryStrategy("QUANTITATIVE")
            .strategyScores(Map.of("QUANTITATIVE", 5))
            .aiInsights("기본적인 분석만 가능한 상태입니다.")
            .build();
    }
    
    // Additional helper methods for calculations...
    private int calculateTechnicalScore(Object indicators, String rsiSignal, String trendSignal) {
        int score = 5; // 기본 점수
        
        if ("OVERSOLD".equals(rsiSignal)) score += 2;
        else if ("OVERBOUGHT".equals(rsiSignal)) score -= 2;
        
        if ("BULLISH".equals(trendSignal)) score += 1;
        else if ("BEARISH".equals(trendSignal)) score -= 1;
        
        return Math.max(1, Math.min(10, score));
    }
    
    private String calculateBollingerPosition(Object indicators) {
        // Mock bollinger band position calculation
        double position = Math.random(); // Random position between 0 and 1
        
        if (position > 0.8) return "UPPER";
        else if (position < 0.2) return "LOWER";
        else return "MIDDLE";
    }
    
    private String generateTechnicalSummary(String rsiSignal, String trendSignal, String volatilityLevel) {
        return String.format("RSI: %s, 추세: %s, 변동성: %s", rsiSignal, trendSignal, volatilityLevel);
    }
    
    private int calculateFundamentalScore(double pe, double pb, double roe, double debtRatio, double revenueGrowth) {
        int score = 5; // 기본 점수
        
        if (pe < 15) score += 2; else if (pe > 25) score -= 2;
        if (pb < 1.5) score += 1; else if (pb > 3) score -= 1;
        if (roe > 15) score += 2; else if (roe < 5) score -= 2;
        if (debtRatio < 0.3) score += 1; else if (debtRatio > 0.5) score -= 1;
        if (revenueGrowth > 10) score += 2; else if (revenueGrowth < 0) score -= 2;
        
        return Math.max(1, Math.min(10, score));
    }
    
    private String generateFundamentalSummary(double pe, double roe, double revenueGrowth) {
        return String.format("P/E: %.1f, ROE: %.1f%%, 매출성장률: %.1f%%", pe, roe, revenueGrowth);
    }
    
    private String generateInvestmentRecommendation(int overallRating, RiskAssessmentResult risk, StrategyAnalysisResult strategy) {
        if (overallRating >= 8 && "LOW".equals(risk.getRiskLevel())) {
            return "강력 매수 - 높은 수익 잠재력과 낮은 리스크";
        } else if (overallRating >= 6 && "MEDIUM".equals(risk.getRiskLevel())) {
            return "매수 - 양호한 투자 기회";
        } else if (overallRating <= 4 || "HIGH".equals(risk.getRiskLevel())) {
            return "매도 또는 회피 - 높은 리스크 또는 낮은 수익성";
        } else {
            return "보유 - 현재 포지션 유지 권장";
        }
    }
    
    private List<String> generateKeyInsights(StrategyAnalysisResult strategy, TechnicalAnalysisResult technical, FundamentalAnalysisResult fundamental) {
        List<String> insights = new ArrayList<>();
        
        insights.add("AI 전략 분석: " + strategy.getAiInsights());
        insights.add("기술적 분석: " + technical.getTechnicalSummary());
        insights.add("기본적 분석: " + fundamental.getFundamentalSummary());
        
        return insights;
    }
    
    private PriceTargetResult calculatePriceTargets(BigDecimal currentPrice, int overallRating) {
        double multiplier = 1.0 + (overallRating - 5) * 0.05; // 등급에 따른 목표가 조정
        
        return PriceTargetResult.builder()
            .shortTermTarget(currentPrice.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP))
            .mediumTermTarget(currentPrice.multiply(BigDecimal.valueOf(multiplier * 1.1)).setScale(2, RoundingMode.HALF_UP))
            .longTermTarget(currentPrice.multiply(BigDecimal.valueOf(multiplier * 1.2)).setScale(2, RoundingMode.HALF_UP))
            .stopLoss(currentPrice.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP))
            .build();
    }
    
    private List<String> identifyRiskFactors(double volatilityRisk, double liquidityRisk, double marketDepthRisk) {
        List<String> factors = new ArrayList<>();
        
        if (volatilityRisk > 0.6) factors.add("높은 가격 변동성");
        if (liquidityRisk > 0.6) factors.add("낮은 거래량");
        if (marketDepthRisk > 0.6) factors.add("넓은 호가 스프레드");
        
        return factors;
    }
    
    private List<String> generateRiskMitigationSuggestions(String riskLevel) {
        List<String> suggestions = new ArrayList<>();
        
        switch (riskLevel) {
            case "HIGH":
                suggestions.add("소량 분할 매수로 위험 분산");
                suggestions.add("손절매 주문 설정 필수");
                suggestions.add("포지션 크기 최소화");
                break;
            case "MEDIUM":
                suggestions.add("적절한 포지션 크기 유지");
                suggestions.add("시장 상황 모니터링");
                break;
            case "LOW":
                suggestions.add("정상적인 포지션 크기 가능");
                suggestions.add("정기적인 수익 실현 고려");
                break;
        }
        
        return suggestions;
    }
    
    // TODO: Remove these mock methods once proper DTOs are implemented
    private Object createMockRealTimeData(String symbol) {
        return new Object() {
            public BigDecimal getPrice() { return BigDecimal.valueOf(100 + Math.random() * 400); }
            public Double getChangePercent() { return (Math.random() - 0.5) * 10; }
            public Long getVolume() { return (long)(Math.random() * 1000000); }
        };
    }
    
    private Object createMockMarketDepth(String symbol) {
        return new Object() {
            public BigDecimal getBidAskSpread() { return BigDecimal.valueOf(0.01 + Math.random() * 0.05); }
        };
    }
    
    private Object createMockTechnicalIndicators(String symbol) {
        return new Object() {
            public Double getRsi() { return 30 + Math.random() * 40; }
            public Double getMacd() { return (Math.random() - 0.5) * 2; }
            public Double getMacdSignal() { return (Math.random() - 0.5) * 2; }
            public BigDecimal getPrice() { return BigDecimal.valueOf(100 + Math.random() * 400); }
            public BigDecimal getBollingerUpper() { return getPrice().multiply(BigDecimal.valueOf(1.02)); }
            public BigDecimal getBollingerLower() { return getPrice().multiply(BigDecimal.valueOf(0.98)); }
        };
    }
}

// DTO Classes...

@Data
@Builder
class StockAnalysisResult {
    private String symbol;
    private LocalDateTime analysisTimestamp;
    private BigDecimal currentPrice;
    private Double priceChangePercent;
    private Long volume24h;
    private BigDecimal marketCap;
    
    private StrategyAnalysisResult strategyAnalysis;
    private TechnicalAnalysisResult technicalAnalysis;
    private FundamentalAnalysisResult fundamentalAnalysis;
    
    private Integer overallRating; // 1-10
    private RiskAssessmentResult riskAssessment;
    private String investmentRecommendation;
    private List<String> keyInsights;
    private PriceTargetResult priceTargets;
}

@Data
@Builder
class StrategyAnalysisResult {
    private String recommendedAction; // BUY, SELL, HOLD
    private Double confidence; // 0.0-1.0
    private String primaryStrategy;
    private Map<String, Integer> strategyScores; // 전략별 점수
    private String aiInsights;
}

@Data
@Builder
class TechnicalAnalysisResult {
    private Double rsi;
    private String rsiSignal; // OVERBOUGHT, OVERSOLD, NEUTRAL
    private Double macd;
    private Double macdSignal;
    private String trendDirection; // BULLISH, BEARISH
    private String bollingerPosition; // UPPER, MIDDLE, LOWER
    private String volatilityLevel; // HIGH, MEDIUM, LOW
    private Integer technicalScore; // 1-10
    private BigDecimal supportLevel;
    private BigDecimal resistanceLevel;
    private String technicalSummary;
}

@Data
@Builder
class FundamentalAnalysisResult {
    private BigDecimal peRatio;
    private BigDecimal pbRatio;
    private BigDecimal roe;
    private BigDecimal debtToEquityRatio;
    private BigDecimal revenueGrowth;
    private Integer fundamentalScore; // 1-10
    private String valueRating; // UNDERVALUED, FAIR_VALUE, OVERVALUED
    private String financialHealthRating; // STRONG, MODERATE, WEAK
    private String growthPotential; // HIGH, MODERATE, LOW
    private String fundamentalSummary;
}

@Data
@Builder
class RiskAssessmentResult {
    private BigDecimal overallRiskScore; // 0.0-1.0
    private String riskLevel; // HIGH, MEDIUM, LOW
    private BigDecimal volatilityRisk;
    private BigDecimal liquidityRisk;
    private BigDecimal marketDepthRisk;
    private List<String> riskFactors;
    private List<String> riskMitigationSuggestions;
}

@Data
@Builder
class PriceTargetResult {
    private BigDecimal shortTermTarget; // 1-3개월
    private BigDecimal mediumTermTarget; // 3-6개월
    private BigDecimal longTermTarget; // 6-12개월
    private BigDecimal stopLoss; // 손절매 기준
}