package com.stockquest.application.analytics;

import com.stockquest.domain.portfolio.PortfolioPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 포트폴리오 리스크 계산 서비스
 * 다양한 리스크 지표를 계산하여 포트폴리오 분석에 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculationService {

    /**
     * VaR (Value at Risk) 계산
     * @param positions 포트폴리오 포지션 목록
     * @param confidenceLevel 신뢰수준 (예: 0.95)
     * @param timeframe 기간 (예: "1D", "1W")
     * @return VaR 값
     */
    public BigDecimal calculateVaR(List<PortfolioPosition> positions, Double confidenceLevel, String timeframe) {
        log.debug("VaR 계산 시작 - 포지션 수: {}, 신뢰수준: {}, 기간: {}", 
                 positions.size(), confidenceLevel, timeframe);
        
        // 기본적인 VaR 계산 (파라메트릭 방법)
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 간단한 VaR 추정 (실제로는 더 복잡한 계산 필요)
        double riskFactor = getRiskFactor(confidenceLevel, timeframe);
        BigDecimal var = totalValue.multiply(BigDecimal.valueOf(riskFactor))
                .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("계산된 VaR: {}", var);
        return var;
    }
    
    /**
     * 샤프 비율 계산
     * @param positions 포트폴리오 포지션 목록
     * @param riskFreeRate 무위험 수익률
     * @param timeframe 기간
     * @return 샤프 비율
     */
    public Double calculateSharpeRatio(List<PortfolioPosition> positions, Double riskFreeRate, String timeframe) {
        log.debug("샤프 비율 계산 시작 - 포지션 수: {}, 무위험 수익률: {}, 기간: {}", 
                 positions.size(), riskFreeRate, timeframe);
        
        // 포트폴리오 수익률 계산
        Double portfolioReturn = calculatePortfolioReturn(positions, timeframe);
        
        // 포트폴리오 변동성 계산
        Double portfolioVolatility = calculatePortfolioVolatility(positions, timeframe);
        
        // 샤프 비율 = (포트폴리오 수익률 - 무위험 수익률) / 포트폴리오 변동성
        Double sharpeRatio = (portfolioReturn - riskFreeRate) / portfolioVolatility;
        
        log.debug("계산된 샤프 비율: {}", sharpeRatio);
        return sharpeRatio;
    }
    
    /**
     * 베타 계산 (시장 대비)
     * @param positions 포트폴리오 포지션 목록
     * @param marketBenchmark 시장 벤치마크
     * @param timeframe 기간
     * @return 베타 값
     */
    public Double calculateBeta(List<PortfolioPosition> positions, String marketBenchmark, String timeframe) {
        log.debug("베타 계산 시작 - 포지션 수: {}, 벤치마크: {}, 기간: {}", 
                 positions.size(), marketBenchmark, timeframe);
        
        // 기본적인 베타 계산 (실제로는 회귀분석 필요)
        double beta = 1.0; // 기본값
        
        // 각 포지션의 가중평균 베타 계산
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            beta = positions.stream()
                    .mapToDouble(pos -> {
                        double weight = pos.getCurrentValue().divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue();
                        double positionBeta = estimatePositionBeta(pos.getSymbol());
                        return weight * positionBeta;
                    })
                    .sum();
        }
        
        log.debug("계산된 베타: {}", beta);
        return beta;
    }
    
    /**
     * 최대 낙폭(Maximum Drawdown) 계산
     * @param positions 포트폴리오 포지션 목록
     * @param timeframe 기간
     * @return 최대 낙폭 비율
     */
    public Double calculateMaxDrawdown(List<PortfolioPosition> positions, String timeframe) {
        log.debug("최대 낙폭 계산 시작 - 포지션 수: {}, 기간: {}", positions.size(), timeframe);
        
        // 기본적인 최대 낙폭 계산
        Double maxDrawdown = 0.15; // 기본값 (실제로는 히스토리컬 데이터 필요)
        
        log.debug("계산된 최대 낙폭: {}%", maxDrawdown * 100);
        return maxDrawdown;
    }
    
    /**
     * 포트폴리오 변동성 계산
     * @param positions 포트폴리오 포지션 목록
     * @param timeframe 기간
     * @return 변동성
     */
    public Double calculatePortfolioVolatility(List<PortfolioPosition> positions, String timeframe) {
        // 간단한 변동성 계산 (실제로는 공분산 매트릭스 필요)
        double volatility = 0.20; // 기본값 20%
        
        // 포지션별 변동성의 가중평균 계산
        BigDecimal totalValue = positions.stream()
                .map(pos -> pos.getCurrentValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            volatility = positions.stream()
                    .mapToDouble(pos -> {
                        double weight = pos.getCurrentValue().divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue();
                        double positionVolatility = estimatePositionVolatility(pos.getSymbol(), timeframe);
                        return weight * weight * positionVolatility * positionVolatility;
                    })
                    .sum();
            
            volatility = Math.sqrt(volatility);
        }
        
        return volatility;
    }
    
    // Helper 메소드들
    private Double calculatePortfolioReturn(List<PortfolioPosition> positions, String timeframe) {
        // 간단한 수익률 계산
        return positions.stream()
                .mapToDouble(pos -> pos.getGainLoss().doubleValue())
                .average()
                .orElse(0.0) / 100.0;
    }
    
    private double getRiskFactor(Double confidenceLevel, String timeframe) {
        // VaR 계산을 위한 리스크 팩터
        double factor = 1.645; // 95% 신뢰수준 기본값
        
        if (confidenceLevel >= 0.99) {
            factor = 2.326;
        } else if (confidenceLevel >= 0.95) {
            factor = 1.645;
        } else if (confidenceLevel >= 0.90) {
            factor = 1.282;
        }
        
        // 기간별 조정
        switch (timeframe) {
            case "1D" -> factor *= 0.05;
            case "1W" -> factor *= 0.12;
            case "1M" -> factor *= 0.25;
            default -> factor *= 0.15;
        }
        
        return factor;
    }
    
    private double estimatePositionBeta(String symbol) {
        // 실제로는 히스토리컬 데이터를 통한 베타 계산이 필요
        // 기본적인 추정값 반환
        return switch (symbol.toUpperCase()) {
            case "AAPL", "MSFT", "GOOGL" -> 1.1;
            case "TSLA", "NVDA" -> 1.5;
            case "JPM", "BAC" -> 1.2;
            default -> 1.0;
        };
    }
    
    private double estimatePositionVolatility(String symbol, String timeframe) {
        // 실제로는 히스토리컬 데이터를 통한 변동성 계산이 필요
        // 기본적인 추정값 반환
        double baseVolatility = switch (symbol.toUpperCase()) {
            case "AAPL", "MSFT", "GOOGL" -> 0.25;
            case "TSLA", "NVDA" -> 0.40;
            case "JPM", "BAC" -> 0.30;
            default -> 0.20;
        };
        
        // 기간별 조정
        return switch (timeframe) {
            case "1D" -> baseVolatility * 0.063; // sqrt(1/252)
            case "1W" -> baseVolatility * 0.141; // sqrt(5/252)
            case "1M" -> baseVolatility * 0.289; // sqrt(21/252)
            default -> baseVolatility;
        };
    }
}