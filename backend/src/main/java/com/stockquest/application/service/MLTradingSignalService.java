package com.stockquest.application.service;

import com.stockquest.application.service.ml.MarketFeatureCollectionService;
import com.stockquest.application.service.ml.MLModelManagementService;
import com.stockquest.application.service.ml.SignalGenerationService;
import com.stockquest.application.service.ml.MarketIntelligenceService;
import com.stockquest.application.service.ml.MarketFeatureCollectionService.MarketFeatures;
import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.*;
import com.stockquest.domain.ml.SimpleTradingModel;
import com.stockquest.domain.ml.TechnicalIndicators;
import com.stockquest.domain.ml.VolatilityAnalysis;
import com.stockquest.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * ML 기반 트레이딩 시그널 생성 서비스
 * Smile ML 라이브러리를 활용한 고급 기계학습 분석
 * 
 * 이제 5개의 전문화된 서비스로 기능이 분리되었습니다:
 * - MarketFeatureCollectionService: 시장 특성 수집
 * - MLModelManagementService: ML 모델 관리
 * - FeatureEngineeringService: 특성 엔지니어링
 * - SignalGenerationService: 시그널 생성
 * - MarketIntelligenceService: 시장 인텔리전스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLTradingSignalService {
    
    private final MarketFeatureCollectionService marketFeatureCollectionService;
    private final MLModelManagementService mlModelManagementService;
    private final SignalGenerationService signalGenerationService;
    private final MarketIntelligenceService marketIntelligenceService;
    
    private static final double CONFIDENCE_THRESHOLD = 0.6;
    
    /**
     * 단일 심볼에 대한 ML 트레이딩 시그널 생성
     */
    @Async("riskAssessmentTaskExecutor")
    public CompletableFuture<TradingSignal> generateTradingSignal(String symbol) {
        try {
            log.info("ML 트레이딩 시그널 생성 시작: symbol={}", symbol);
            
            // 1. 시장 데이터 및 기술적 지표 수집
            MarketFeatures features = marketFeatureCollectionService.collectMarketFeatures(symbol);
            
            // 2. ML 모델 로드 또는 훈련
            SimpleTradingModel model = mlModelManagementService.getOrTrainModel(symbol);
            
            // 3. 시그널 생성
            TradingSignal signal = signalGenerationService.generateSignalFromModel(symbol, model, features);
            
            // 4. 시장 조건 및 성과 추적 정보 추가
            marketIntelligenceService.enhanceSignalWithMarketIntelligence(signal, features);
            
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
            .filter(signal -> marketIntelligenceService.isSignalValidForMarketRegime(signal, currentRegime))
            .filter(signal -> signal.getConfidence().compareTo(BigDecimal.valueOf(CONFIDENCE_THRESHOLD)) >= 0)
            .sorted((a, b) -> b.getSignalScore().compareTo(a.getSignalScore()))
            .toList();
    }
    
    /**
     * 주식, 기술적 지표, 변동성 분석을 기반으로 거래 신호 생성 (백테스팅용)
     * 
     * 이 메소드는 백테스팅을 위해 유지됩니다.
     * 실제 ML 시그널 생성은 generateTradingSignal() 메소드를 사용하세요.
     */
    public TradingSignal generateSignal(Stock stock, TechnicalIndicators indicators, VolatilityAnalysis volatility) {
        try {
            log.debug("백테스팅용 거래 신호 생성: symbol={}", stock.getSymbol());
            
            // 간단한 ML 기반 신호 생성 로직
            // 실제 구현에서는 더 복잡한 ML 모델을 사용
            SignalType signalType = determineSignalTypeFromIndicators(indicators, volatility);
            BigDecimal confidence = calculateConfidenceFromIndicators(indicators, volatility);
            BigDecimal strength = calculateSignalStrengthFromIndicators(indicators);
            
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
    
    // 백테스팅용 간단한 헬퍼 메소드들 (기존 인터페이스 호환성을 위해 유지)
    
    /**
     * 기술적 지표와 변동성 분석을 바탕으로 신호 타입 결정
     */
    private SignalType determineSignalTypeFromIndicators(TechnicalIndicators indicators, VolatilityAnalysis volatility) {
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
    private BigDecimal calculateConfidenceFromIndicators(TechnicalIndicators indicators, VolatilityAnalysis volatility) {
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
    private BigDecimal calculateSignalStrengthFromIndicators(TechnicalIndicators indicators) {
        // 기본 강도는 0.5
        double strength = 0.5;
        
        // MACD가 양수이면 강도 증가
        if (indicators.getMacdLine() != null && indicators.getMacdLine().compareTo(BigDecimal.ZERO) > 0) {
            strength += 0.2;
        }
        
        return BigDecimal.valueOf(Math.min(strength, 1.0)).setScale(2, RoundingMode.HALF_UP);
    }
}