package com.stockquest.application.market;

import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.port.ExternalMarketDataClient;
import com.stockquest.domain.market.port.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 시장 데이터 통합 서비스
 * 외부 API에서 데이터를 수집하고 로컬 저장소에 캐싱하는 역할
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MarketDataService {
    
    private final ExternalMarketDataClient externalClient;
    private final MarketDataRepository marketDataRepository;
    
    /**
     * 실시간 주가 조회 (캐시 우선)
     * 1. Redis 캐시에서 조회
     * 2. 없으면 외부 API 호출
     * 3. 결과를 로컬 DB 및 캐시에 저장
     */
    @Cacheable(value = "latestPrice", key = "#ticker")
    public PriceCandle getLatestPrice(String ticker) {
        log.info("실시간 주가 조회: {}", ticker);
        
        try {
            // 1. 로컬 DB에서 최신 데이터 확인 (오늘 데이터가 있는지)
            Optional<PriceCandle> localCandle = marketDataRepository
                .findLatestByTicker(ticker, CandleTimeframe.DAILY);
            
            if (localCandle.isPresent() && 
                localCandle.get().getDate().equals(LocalDate.now())) {
                log.debug("로컬 DB에서 최신 데이터 반환: {}", ticker);
                return localCandle.get();
            }
            
            // 2. 외부 API에서 최신 데이터 조회
            PriceCandle externalCandle = externalClient.fetchLatestPrice(ticker);
            
            if (externalCandle != null) {
                // 3. 로컬 DB에 저장
                PriceCandle savedCandle = marketDataRepository.save(externalCandle);
                log.info("외부 API에서 최신 데이터 조회 및 저장 완료: {} ({})", ticker, savedCandle.getClosePrice());
                return savedCandle;
            }
            
            // 4. 외부 데이터가 없으면 가장 최근 로컬 데이터 반환
            return localCandle.orElse(null);
            
        } catch (Exception e) {
            log.error("최신 주가 조회 중 오류 발생: ticker={}, error={}", ticker, e.getMessage(), e);
            
            // 에러 발생 시 로컬 데이터라도 반환
            return marketDataRepository.findLatestByTicker(ticker, CandleTimeframe.DAILY)
                                     .orElse(null);
        }
    }
    
    /**
     * 여러 티커의 최신 가격 일괄 조회
     */
    public Map<String, PriceCandle> getLatestPrices(List<String> tickers) {
        log.info("여러 티커 최신 가격 일괄 조회: {}", tickers);
        
        return tickers.parallelStream()
                     .collect(Collectors.toMap(
                         ticker -> ticker,
                         this::getLatestPrice,
                         (existing, replacement) -> replacement // 중복 키 처리
                     ))
                     .entrySet().stream()
                     .filter(entry -> entry.getValue() != null)
                     .collect(Collectors.toMap(
                         Map.Entry::getKey,
                         Map.Entry::getValue
                     ));
    }
    
    /**
     * 과거 일봉 데이터 조회 및 캐싱
     */
    @Cacheable(value = "dailyCandles", key = "#ticker + '_' + #startDate + '_' + #endDate")
    public List<PriceCandle> getDailyCandles(String ticker, LocalDate startDate, LocalDate endDate) {
        log.info("일봉 데이터 조회: {} ({} ~ {})", ticker, startDate, endDate);
        
        try {
            // 1. 로컬 DB에서 기존 데이터 확인
            List<PriceCandle> localCandles = marketDataRepository
                .findByTickerAndDateBetween(ticker, startDate, endDate, CandleTimeframe.DAILY);
            
            // 2. 요청 기간과 로컬 데이터 비교
            long requestedDays = startDate.datesUntil(endDate.plusDays(1)).count();
            
            if (localCandles.size() >= requestedDays * 0.8) { // 80% 이상 데이터가 있으면 로컬 데이터 사용
                log.debug("로컬 DB 데이터 충분 ({}%): ticker={}, 로컬={}, 요청일수={}", 
                         (localCandles.size() * 100 / requestedDays), ticker, localCandles.size(), requestedDays);
                return localCandles;
            }
            
            // 3. 외부 API에서 데이터 조회
            List<PriceCandle> externalCandles = externalClient.fetchDailyCandles(ticker, startDate, endDate);
            
            if (!externalCandles.isEmpty()) {
                // 4. 로컬 DB에 일괄 저장 (중복 제거)
                List<PriceCandle> newCandles = externalCandles.stream()
                    .filter(candle -> localCandles.stream()
                                                 .noneMatch(local -> local.getDate().equals(candle.getDate())))
                    .collect(Collectors.toList());
                
                if (!newCandles.isEmpty()) {
                    marketDataRepository.saveAll(newCandles);
                    log.info("외부 API에서 새로운 일봉 데이터 저장: ticker={}, 신규={}", ticker, newCandles.size());
                }
                
                return externalCandles;
            }
            
            // 5. 외부 데이터가 없으면 로컬 데이터 반환
            return localCandles;
            
        } catch (Exception e) {
            log.error("일봉 데이터 조회 중 오류 발생: ticker={}, error={}", ticker, e.getMessage(), e);
            
            // 에러 발생 시 로컬 데이터라도 반환
            return marketDataRepository.findByTickerAndDateBetween(ticker, startDate, endDate, CandleTimeframe.DAILY);
        }
    }
    
    /**
     * 시장 데이터 동기화 (비동기)
     * 주요 티커들의 데이터를 미리 수집해서 캐싱
     */
    @Async
    public CompletableFuture<Void> syncMarketData(List<String> tickers) {
        log.info("시장 데이터 동기화 시작: {} 티커", tickers.size());
        
        try {
            // 최근 30일 데이터 수집
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            for (String ticker : tickers) {
                try {
                    getDailyCandles(ticker, startDate, endDate);
                    getLatestPrice(ticker);
                    
                    // API 호출 제한 방지를 위한 짧은 대기
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.warn("티커 동기화 실패: {}, error={}", ticker, e.getMessage());
                }
            }
            
            log.info("시장 데이터 동기화 완료: {} 티커", tickers.size());
            
        } catch (Exception e) {
            log.error("시장 데이터 동기화 중 전체 오류: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 특정 가격으로 주문 가능한지 검증
     * 시장가 기준 ±5% 범위 내인지 확인
     */
    public boolean isValidOrderPrice(String ticker, BigDecimal orderPrice, BigDecimal slippagePercent) {
        PriceCandle latestPrice = getLatestPrice(ticker);
        
        if (latestPrice == null) {
            log.warn("시장가 조회 불가로 주문가 검증 불가: {}", ticker);
            return false;
        }
        
        BigDecimal marketPrice = latestPrice.getClosePrice();
        BigDecimal maxSlippage = slippagePercent.add(new BigDecimal("5.0")); // 기본 5% + 슬리피지
        
        BigDecimal lowerBound = marketPrice.multiply(
            BigDecimal.ONE.subtract(maxSlippage.divide(new BigDecimal("100"))));
        BigDecimal upperBound = marketPrice.multiply(
            BigDecimal.ONE.add(maxSlippage.divide(new BigDecimal("100"))));
        
        boolean isValid = orderPrice.compareTo(lowerBound) >= 0 && orderPrice.compareTo(upperBound) <= 0;
        
        log.debug("주문가 검증: ticker={}, 시장가={}, 주문가={}, 유효={}", 
                 ticker, marketPrice, orderPrice, isValid);
        
        return isValid;
    }
}