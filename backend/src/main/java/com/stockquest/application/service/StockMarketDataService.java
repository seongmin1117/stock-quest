package com.stockquest.application.service;

import com.stockquest.domain.stock.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 주식 시장 데이터 서비스
 * Phase 8.3: Advanced Risk Management - 시뮬레이션 기반 시장 데이터 조회 및 관리
 */
@Slf4j
@Service
public class StockMarketDataService {
    
    /**
     * 특정 심볼들에 대한 역사적 데이터 조회
     */
    public List<Stock> getHistoricalData(List<String> symbols, int days) {
        log.debug("Fetching historical data for {} symbols, {} days", symbols.size(), days);
        
        // 실제 구현에서는 데이터베이스나 외부 API에서 조회
        // 현재는 시뮬레이션 데이터 생성
        return symbols.stream()
            .flatMap(symbol -> generateHistoricalData(symbol, days).stream())
            .toList();
    }
    
    /**
     * 특정 심볼의 최신 시장 데이터 조회
     */
    public Stock getLatestMarketData(String symbol) {
        log.debug("Fetching latest market data for symbol: {}", symbol);
        
        return generateSingleStockData(symbol, LocalDateTime.now());
    }
    
    /**
     * 여러 심볼의 최신 시장 데이터 조회
     */
    public List<Stock> getLatestMarketData(List<String> symbols) {
        log.debug("Fetching latest market data for {} symbols", symbols.size());
        
        return symbols.stream()
            .map(this::getLatestMarketData)
            .toList();
    }
    
    /**
     * 특정 기간의 일간 수익률 데이터 조회
     */
    public List<BigDecimal> getDailyReturns(String symbol, int days) {
        log.debug("Fetching daily returns for symbol: {}, days: {}", symbol, days);
        
        List<Stock> historicalData = generateHistoricalData(symbol, days);
        return calculateDailyReturns(historicalData);
    }
    
    /**
     * 시장 변동성 데이터 조회
     */
    public BigDecimal getVolatility(String symbol, int days) {
        log.debug("Fetching volatility for symbol: {}, days: {}", symbol, days);
        
        List<BigDecimal> returns = getDailyReturns(symbol, days);
        return calculateVolatility(returns);
    }
    
    /**
     * 자산 간 상관관계 조회
     */
    public BigDecimal getCorrelation(String symbol1, String symbol2, int days) {
        log.debug("Fetching correlation between {} and {}, days: {}", symbol1, symbol2, days);
        
        List<BigDecimal> returns1 = getDailyReturns(symbol1, days);
        List<BigDecimal> returns2 = getDailyReturns(symbol2, days);
        
        return calculateCorrelation(returns1, returns2);
    }
    
    // ========================= 헬퍼 메서드들 =========================
    
    private List<Stock> generateHistoricalData(String symbol, int days) {
        return IntStream.range(0, days)
            .mapToObj(i -> {
                LocalDateTime date = LocalDateTime.now().minusDays(days - i);
                return generateSingleStockData(symbol, date);
            })
            .toList();
    }
    
    private Stock generateSingleStockData(String symbol, LocalDateTime timestamp) {
        // 시뮬레이션 데이터 생성 (실제 구현에서는 실제 시장 데이터 사용)
        double basePrice = 100.0 + (symbol.hashCode() % 900); // 심볼 기반 기본 가격
        double volatility = 0.02; // 2% 일일 변동성
        double randomChange = (Math.random() - 0.5) * 2 * volatility;
        
        BigDecimal openPrice = BigDecimal.valueOf(basePrice);
        BigDecimal closePrice = BigDecimal.valueOf(basePrice * (1 + randomChange));
        BigDecimal highPrice = BigDecimal.valueOf(Math.max(openPrice.doubleValue(), closePrice.doubleValue()) * (1 + Math.random() * 0.01));
        BigDecimal lowPrice = BigDecimal.valueOf(Math.min(openPrice.doubleValue(), closePrice.doubleValue()) * (1 - Math.random() * 0.01));
        
        return Stock.builder()
            .symbol(symbol)
            .timestamp(timestamp)
            .openPrice(openPrice)
            .highPrice(highPrice)
            .lowPrice(lowPrice)
            .closePrice(closePrice)
            .volume(1000000L + (long)(Math.random() * 5000000))
            .build();
    }
    
    private List<BigDecimal> calculateDailyReturns(List<Stock> historicalData) {
        return IntStream.range(1, historicalData.size())
            .mapToObj(i -> {
                BigDecimal currentPrice = historicalData.get(i).getClosePrice();
                BigDecimal previousPrice = historicalData.get(i - 1).getClosePrice();
                
                if (previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO;
                }
                
                return currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, java.math.RoundingMode.HALF_UP);
            })
            .toList();
    }
    
    private BigDecimal calculateVolatility(List<BigDecimal> returns) {
        if (returns.size() <= 1) {
            return BigDecimal.ZERO;
        }
        
        // 평균 수익률 계산
        BigDecimal mean = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns.size()), 8, java.math.RoundingMode.HALF_UP);
        
        // 분산 계산
        BigDecimal variance = returns.stream()
            .map(ret -> ret.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns.size() - 1), 8, java.math.RoundingMode.HALF_UP);
        
        // 표준편차 (변동성)
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
    
    private BigDecimal calculateCorrelation(List<BigDecimal> returns1, List<BigDecimal> returns2) {
        if (returns1.size() != returns2.size() || returns1.size() <= 1) {
            return BigDecimal.ZERO;
        }
        
        // 평균 계산
        BigDecimal mean1 = returns1.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns1.size()), 8, java.math.RoundingMode.HALF_UP);
        
        BigDecimal mean2 = returns2.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns2.size()), 8, java.math.RoundingMode.HALF_UP);
        
        // 공분산과 표준편차 계산
        BigDecimal covariance = BigDecimal.ZERO;
        BigDecimal variance1 = BigDecimal.ZERO;
        BigDecimal variance2 = BigDecimal.ZERO;
        
        for (int i = 0; i < returns1.size(); i++) {
            BigDecimal diff1 = returns1.get(i).subtract(mean1);
            BigDecimal diff2 = returns2.get(i).subtract(mean2);
            
            covariance = covariance.add(diff1.multiply(diff2));
            variance1 = variance1.add(diff1.pow(2));
            variance2 = variance2.add(diff2.pow(2));
        }
        
        // 표준편차
        BigDecimal stdDev1 = BigDecimal.valueOf(Math.sqrt(variance1.doubleValue()));
        BigDecimal stdDev2 = BigDecimal.valueOf(Math.sqrt(variance2.doubleValue()));
        
        // 상관계수
        if (stdDev1.compareTo(BigDecimal.ZERO) == 0 || stdDev2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return covariance.divide(stdDev1.multiply(stdDev2), 8, java.math.RoundingMode.HALF_UP);
    }
}