package com.stockquest.application.service.ml;

import com.stockquest.application.service.ml.MarketFeatureCollectionService.MarketFeatures;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.MarketCondition;
import com.stockquest.domain.ml.TradingSignal.MarketRegime;
import com.stockquest.domain.ml.TradingSignal.PerformanceTracking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시장 인텔리전스 및 시그널 강화 서비스
 * Market regime analysis, signal enhancement with market context, and market stress assessment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketIntelligenceService {
    
    /**
     * 시장 인텔리전스로 시그널 강화
     */
    public void enhanceSignalWithMarketIntelligence(TradingSignal signal, MarketFeatures features) {
        try {
            log.debug("시그널 시장 인텔리전스 강화 시작: signalId={}", signal.getSignalId());
            
            // 시장 조건 정보 추가
            signal.setMarketCondition(features.getMarketCondition());
            
            // 시장 스트레스 레벨 분석 및 적용
            double stressLevel = analyzeMarketStressLevel(features);
            adjustSignalForStressLevel(signal, stressLevel);
            
            // 성과 추적 초기화
            signal.setPerformanceTracking(createInitialPerformanceTracking(features));
            
            // 시장 체제별 신호 강도 조정
            adjustSignalForMarketRegime(signal, features.getMarketCondition().getRegime());
            
            log.debug("시그널 시장 인텔리전스 강화 완료: signalId={}, adjustedConfidence={}", 
                signal.getSignalId(), signal.getConfidence());
                
        } catch (Exception e) {
            log.error("시그널 시장 인텔리전스 강화 실패: signalId={}", signal.getSignalId(), e);
            // 오류 발생 시 원본 시그널 유지
        }
    }
    
    /**
     * 시장 스트레스 레벨 분석
     */
    public double analyzeMarketStressLevel(MarketFeatures features) {
        try {
            double stressLevel = 0.0;
            
            // 변동성 기반 스트레스
            if (features.getVolatilityAnalysis() != null) {
                double volatility = features.getVolatilityAnalysis().getHistoricalVolatility();
                stressLevel += Math.min(0.4, volatility * 2); // 변동성의 2배, 최대 0.4
            }
            
            // 시장 체제 기반 스트레스
            if (features.getMarketCondition() != null) {
                MarketRegime regime = features.getMarketCondition().getRegime();
                stressLevel += switch (regime) {
                    case HIGH_VOLATILITY -> 0.3;
                    case BEAR_MARKET -> 0.25;
                    case SIDEWAYS_MARKET -> 0.1;
                    case BULL_MARKET -> 0.05;
                    case LOW_VOLATILITY -> 0.0;
                };
            }
            
            // 시장 심리 기반 스트레스
            if (features.getMarketCondition() != null && features.getMarketCondition().getMarketSentiment() != null) {
                double sentiment = features.getMarketCondition().getMarketSentiment().doubleValue();
                // 극단적 sentiment는 스트레스 증가
                stressLevel += Math.min(0.2, Math.abs(sentiment) * 0.3);
            }
            
            // 0과 1 사이로 제한
            stressLevel = Math.max(0.0, Math.min(1.0, stressLevel));
            
            log.debug("시장 스트레스 레벨 계산 완료: level={}", stressLevel);
            return stressLevel;
            
        } catch (Exception e) {
            log.error("시장 스트레스 레벨 분석 실패", e);
            return 0.2; // 기본값
        }
    }
    
    /**
     * 시장 조건 기반 시그널 필터링 검증
     */
    public boolean isSignalValidForMarketRegime(TradingSignal signal, MarketRegime currentRegime) {
        try {
            return switch (currentRegime) {
                case BULL_MARKET -> signal.getSignalType() != TradingSignal.SignalType.STRONG_SELL;
                case BEAR_MARKET -> signal.getSignalType() != TradingSignal.SignalType.STRONG_BUY;
                case SIDEWAYS_MARKET -> signal.getSignalType() == TradingSignal.SignalType.HOLD || 
                                      signal.getConfidence().compareTo(BigDecimal.valueOf(0.8)) >= 0;
                case HIGH_VOLATILITY -> signal.getConfidence().compareTo(BigDecimal.valueOf(0.7)) >= 0;
                case LOW_VOLATILITY -> true;
            };
            
        } catch (Exception e) {
            log.error("시그널 유효성 검증 실패: signalId={}", signal.getSignalId(), e);
            return true; // 기본값
        }
    }
    
    /**
     * 시장 체제 기반 포트폴리오 권장사항 생성
     */
    public PortfolioRecommendation generatePortfolioRecommendation(List<TradingSignal> signals, MarketCondition marketCondition) {
        try {
            log.debug("포트폴리오 권장사항 생성 시작: signalCount={}, regime={}", 
                signals.size(), marketCondition.getRegime());
            
            double riskAdjustment = calculateRiskAdjustment(marketCondition);
            double positionSizing = calculateOptimalPositionSizing(marketCondition);
            
            // 시장 체제별 전략 권장
            String strategy = switch (marketCondition.getRegime()) {
                case BULL_MARKET -> "적극적 성장 전략 - 성장주 비중 확대";
                case BEAR_MARKET -> "방어적 전략 - 현금 비중 증가, 안전자산 선호";
                case SIDEWAYS_MARKET -> "중립 전략 - 균형잡힌 포트폴리오 유지";
                case HIGH_VOLATILITY -> "위험 관리 전략 - 포지션 축소, 헤징 강화";
                case LOW_VOLATILITY -> "기회 포착 전략 - 선별적 투자 확대";
            };
            
            return PortfolioRecommendation.builder()
                .marketRegime(marketCondition.getRegime())
                .recommendedStrategy(strategy)
                .riskAdjustment(riskAdjustment)
                .positionSizing(positionSizing)
                .maxPortfolioRisk(calculateMaxPortfolioRisk(marketCondition))
                .rebalanceFrequency(getRebalanceFrequency(marketCondition))
                .build();
                
        } catch (Exception e) {
            log.error("포트폴리오 권장사항 생성 실패", e);
            return createDefaultPortfolioRecommendation();
        }
    }
    
    // Private 메소드들
    
    private void adjustSignalForStressLevel(TradingSignal signal, double stressLevel) {
        // 스트레스 레벨에 따른 신뢰도 조정
        if (stressLevel > 0.7) {
            // 높은 스트레스: 신뢰도 감소
            BigDecimal adjustedConfidence = signal.getConfidence()
                .multiply(BigDecimal.valueOf(0.8))
                .setScale(4, java.math.RoundingMode.HALF_UP);
            signal.setConfidence(adjustedConfidence);
        } else if (stressLevel < 0.3) {
            // 낮은 스트레스: 신뢰도 소폭 증가
            BigDecimal adjustedConfidence = signal.getConfidence()
                .multiply(BigDecimal.valueOf(1.1))
                .min(BigDecimal.ONE)
                .setScale(4, java.math.RoundingMode.HALF_UP);
            signal.setConfidence(adjustedConfidence);
        }
    }
    
    private PerformanceTracking createInitialPerformanceTracking(MarketFeatures features) {
        return PerformanceTracking.builder()
            .currentPrice(features.getCurrentPrice())
            .unrealizedReturn(BigDecimal.ZERO)
            .maxReturn(BigDecimal.ZERO)
            .maxDrawdown(BigDecimal.ZERO)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    private void adjustSignalForMarketRegime(TradingSignal signal, MarketRegime regime) {
        // 시장 체제별 시그널 강도 조정
        BigDecimal adjustmentFactor = switch (regime) {
            case BULL_MARKET -> BigDecimal.valueOf(1.2); // 강세장에서 매수 신호 강화
            case BEAR_MARKET -> BigDecimal.valueOf(0.8); // 약세장에서 매수 신호 약화
            case HIGH_VOLATILITY -> BigDecimal.valueOf(0.9); // 고변동성에서 보수적 조정
            default -> BigDecimal.ONE;
        };
        
        // 매수 신호의 경우에만 조정 적용
        if (signal.getSignalType() == TradingSignal.SignalType.BUY || 
            signal.getSignalType() == TradingSignal.SignalType.STRONG_BUY) {
            BigDecimal adjustedStrength = signal.getStrength()
                .multiply(adjustmentFactor)
                .min(BigDecimal.ONE)
                .setScale(4, java.math.RoundingMode.HALF_UP);
            signal.setStrength(adjustedStrength);
        }
    }
    
    private double calculateRiskAdjustment(MarketCondition marketCondition) {
        double baseRisk = 1.0;
        
        switch (marketCondition.getRegime()) {
            case HIGH_VOLATILITY, BEAR_MARKET -> baseRisk = 0.6; // 위험 감소
            case BULL_MARKET -> baseRisk = 1.3; // 위험 증가 허용
            case LOW_VOLATILITY -> baseRisk = 1.1; // 소폭 위험 증가
            default -> baseRisk = 1.0;
        }
        
        return baseRisk;
    }
    
    private double calculateOptimalPositionSizing(MarketCondition marketCondition) {
        return switch (marketCondition.getRegime()) {
            case HIGH_VOLATILITY -> 0.3; // 30% 포지션 크기
            case BEAR_MARKET -> 0.4; // 40% 포지션 크기
            case SIDEWAYS_MARKET -> 0.6; // 60% 포지션 크기
            case BULL_MARKET -> 0.8; // 80% 포지션 크기
            case LOW_VOLATILITY -> 0.7; // 70% 포지션 크기
        };
    }
    
    private double calculateMaxPortfolioRisk(MarketCondition marketCondition) {
        return switch (marketCondition.getRegime()) {
            case HIGH_VOLATILITY -> 0.15; // 15% 최대 위험
            case BEAR_MARKET -> 0.12; // 12% 최대 위험
            case SIDEWAYS_MARKET -> 0.20; // 20% 최대 위험
            case BULL_MARKET -> 0.25; // 25% 최대 위험
            case LOW_VOLATILITY -> 0.22; // 22% 최대 위험
        };
    }
    
    private String getRebalanceFrequency(MarketCondition marketCondition) {
        return switch (marketCondition.getRegime()) {
            case HIGH_VOLATILITY -> "weekly"; // 주간 리밸런싱
            case BEAR_MARKET, BULL_MARKET -> "bi-weekly"; // 격주 리밸런싱
            case SIDEWAYS_MARKET, LOW_VOLATILITY -> "monthly"; // 월간 리밸런싱
        };
    }
    
    private PortfolioRecommendation createDefaultPortfolioRecommendation() {
        return PortfolioRecommendation.builder()
            .marketRegime(MarketRegime.SIDEWAYS_MARKET)
            .recommendedStrategy("중립 전략 - 균형잡힌 포트폴리오 유지")
            .riskAdjustment(1.0)
            .positionSizing(0.5)
            .maxPortfolioRisk(0.20)
            .rebalanceFrequency("monthly")
            .build();
    }
    
    /**
     * PortfolioRecommendation DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PortfolioRecommendation {
        private MarketRegime marketRegime;
        private String recommendedStrategy;
        private double riskAdjustment;
        private double positionSizing;
        private double maxPortfolioRisk;
        private String rebalanceFrequency;
    }
}