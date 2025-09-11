package com.stockquest.application.simulation;

import com.stockquest.application.market.MarketDataService;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시뮬레이션 포트폴리오 평가 서비스
 * 특정 시뮬레이션 날짜 기준으로 포트폴리오 평가금액 계산
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationPortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;
    
    /**
     * 특정 시뮬레이션 날짜 기준 포트폴리오 총 평가금액 계산
     */
    public BigDecimal calculatePortfolioValue(Long sessionId, Challenge challenge, LocalDate simulationDate) {
        log.debug("포트폴리오 평가 시작: sessionId={}, date={}", sessionId, simulationDate);
        
        // 1. 세션의 모든 포트폴리오 포지션 조회
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        
        if (positions.isEmpty()) {
            log.debug("포트폴리오 포지션 없음: sessionId={}", sessionId);
            return BigDecimal.ZERO;
        }
        
        // 2. 시뮬레이션 날짜의 시장가 조회
        Map<String, BigDecimal> marketPrices = getMarketPricesForDate(challenge, simulationDate);
        
        // 3. 각 포지션별 평가금액 계산
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) {
                continue; // 보유 수량이 없으면 스킵
            }
            
            String instrumentKey = position.getInstrumentKey();
            BigDecimal marketPrice = marketPrices.get(instrumentKey);
            
            if (marketPrice == null) {
                log.warn("시장가 조회 실패, 평균단가 사용: instrumentKey={}, sessionId={}", 
                        instrumentKey, sessionId);
                marketPrice = position.getAveragePrice(); // 평균단가로 대체
            }
            
            BigDecimal positionValue = position.calculateCurrentValue(marketPrice);
            totalValue = totalValue.add(positionValue);
            
            log.debug("포지션 평가: instrumentKey={}, quantity={}, price={}, value={}", 
                     instrumentKey, position.getQuantity(), marketPrice, positionValue);
        }
        
        log.debug("포트폴리오 총 평가금액: sessionId={}, date={}, value={}", 
                 sessionId, simulationDate, totalValue);
        
        return totalValue;
    }
    
    /**
     * 특정 날짜의 챌린지 종목들 시장가 조회
     */
    private Map<String, BigDecimal> getMarketPricesForDate(Challenge challenge, LocalDate date) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        for (ChallengeInstrument instrument : challenge.getInstruments()) {
            String instrumentKey = instrument.getInstrumentKey();
            
            try {
                // InstrumentKey를 실제 Ticker로 매핑
                String ticker = mapInstrumentKeyToTicker(instrumentKey);
                
                // 특정 날짜의 일봉 데이터 조회 (1일치만)
                List<PriceCandle> candles = marketDataService.getDailyCandles(ticker, date, date);
                
                if (!candles.isEmpty()) {
                    PriceCandle candle = candles.get(0);
                    prices.put(instrumentKey, candle.getClosePrice());
                    log.debug("과거 시장가 조회 성공: {}({}) = {}", instrumentKey, ticker, candle.getClosePrice());
                } else {
                    // 해당 날짜 데이터가 없으면 가장 가까운 과거 데이터 사용
                    BigDecimal fallbackPrice = getFallbackPrice(ticker, date);
                    prices.put(instrumentKey, fallbackPrice);
                    log.debug("과거 시장가 대체값 사용: {}({}) = {}", instrumentKey, ticker, fallbackPrice);
                }
                
            } catch (Exception e) {
                log.warn("시장가 조회 실패: instrumentKey={}, date={}, error={}", 
                        instrumentKey, date, e.getMessage());
                
                // 기본값 사용
                BigDecimal defaultPrice = getDefaultPriceForInstrument(instrumentKey);
                prices.put(instrumentKey, defaultPrice);
            }
        }
        
        return prices;
    }
    
    /**
     * 해당 날짜 데이터가 없을 때 가장 가까운 과거 시장가 조회
     */
    private BigDecimal getFallbackPrice(String ticker, LocalDate targetDate) {
        try {
            // 최대 30일 전까지 조회
            LocalDate startDate = targetDate.minusDays(30);
            List<PriceCandle> candles = marketDataService.getDailyCandles(ticker, startDate, targetDate);
            
            if (!candles.isEmpty()) {
                // 가장 가까운 날짜의 데이터 사용
                PriceCandle closestCandle = candles.stream()
                    .filter(c -> !c.getDate().isAfter(targetDate))
                    .max((c1, c2) -> c1.getDate().compareTo(c2.getDate()))
                    .orElse(candles.get(candles.size() - 1));
                
                return closestCandle.getClosePrice();
            }
            
        } catch (Exception e) {
            log.warn("대체 시장가 조회 실패: ticker={}, error={}", ticker, e.getMessage());
        }
        
        // 최후의 수단으로 현재 시장가 사용
        PriceCandle latestPrice = marketDataService.getLatestPrice(ticker);
        return latestPrice != null ? latestPrice.getClosePrice() : new BigDecimal("100.00");
    }
    
    /**
     * InstrumentKey를 Ticker로 매핑 (임시 구현)
     */
    private String mapInstrumentKeyToTicker(String instrumentKey) {
        return switch (instrumentKey.toUpperCase()) {
            case "AAPL", "A" -> "AAPL";
            case "MSFT", "B" -> "MSFT";
            case "GOOGL", "C" -> "GOOGL";
            case "TSLA", "D" -> "TSLA";
            case "AMZN", "E" -> "AMZN";
            case "META", "F" -> "META";
            case "NVDA", "G" -> "NVDA";
            case "NFLX", "H" -> "NFLX";
            default -> {
                log.warn("알 수 없는 instrumentKey, AAPL로 대체: {}", instrumentKey);
                yield "AAPL";
            }
        };
    }
    
    /**
     * 기본 가격 반환 (모든 조회가 실패했을 때)
     */
    private BigDecimal getDefaultPriceForInstrument(String instrumentKey) {
        return switch (instrumentKey.toUpperCase()) {
            case "AAPL", "A" -> new BigDecimal("150.00");
            case "MSFT", "B" -> new BigDecimal("350.00");
            case "GOOGL", "C" -> new BigDecimal("2800.00");
            case "TSLA", "D" -> new BigDecimal("200.00");
            case "AMZN", "E" -> new BigDecimal("3000.00");
            case "META", "F" -> new BigDecimal("300.00");
            case "NVDA", "G" -> new BigDecimal("800.00");
            case "NFLX", "H" -> new BigDecimal("400.00");
            default -> new BigDecimal("100.00");
        };
    }
    
    /**
     * 포트폴리오 상세 평가 정보 조회 (디버깅용)
     */
    public Map<String, Object> getPortfolioEvaluationDetail(Long sessionId, Challenge challenge, LocalDate simulationDate) {
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(sessionId);
        Map<String, BigDecimal> marketPrices = getMarketPricesForDate(challenge, simulationDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("simulationDate", simulationDate);
        result.put("positionCount", positions.size());
        result.put("marketPrices", marketPrices);
        
        BigDecimal totalValue = BigDecimal.ZERO;
        List<Map<String, Object>> positionDetails = new java.util.ArrayList<>();
        
        for (PortfolioPosition position : positions) {
            if (!position.hasPosition()) continue;
            
            String instrumentKey = position.getInstrumentKey();
            BigDecimal marketPrice = marketPrices.getOrDefault(instrumentKey, position.getAveragePrice());
            BigDecimal positionValue = position.calculateCurrentValue(marketPrice);
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("instrumentKey", instrumentKey);
            detail.put("quantity", position.getQuantity());
            detail.put("averagePrice", position.getAveragePrice());
            detail.put("marketPrice", marketPrice);
            detail.put("positionValue", positionValue);
            detail.put("unrealizedPnL", positionValue.subtract(position.getTotalCost()));
            
            positionDetails.add(detail);
            totalValue = totalValue.add(positionValue);
        }
        
        result.put("positions", positionDetails);
        result.put("totalValue", totalValue);
        
        return result;
    }
}