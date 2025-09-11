package com.stockquest.application.marketdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// Market Data DTOs
import com.stockquest.domain.marketdata.*;

/**
 * Real-Time Market Data Service
 * 실시간 시장 데이터 서비스
 * 
 * 고성능 실시간 시장 데이터 처리 및 배포 시스템:
 * - 실시간 가격 스트리밍
 * - 시장 뎁스(호가) 정보
 * - 거래량 및 시장 통계
 * - 기술적 지표 실시간 계산
 * - 시장 이상 탐지 및 알림
 * - 고빈도 데이터 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeMarketDataService {

    private final WebClient.Builder webClientBuilder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MarketDataRepository marketDataRepository;
    
    // 실시간 데이터 캐시
    private final Map<String, RealTimeQuote> realtimeQuotes = new ConcurrentHashMap<>();
    private final Map<String, MarketDepth> marketDepthCache = new ConcurrentHashMap<>();
    private final Map<String, List<Trade>> recentTrades = new ConcurrentHashMap<>();
    private final Map<String, TechnicalIndicators> technicalIndicatorsCache = new ConcurrentHashMap<>();
    
    // 구독 관리
    private final Map<String, Set<String>> symbolSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastUpdateTime = new ConcurrentHashMap<>();

    /**
     * 실시간 가격 스트림 구독
     * 
     * @param symbols 구독할 종목 목록
     * @return 실시간 가격 스트림
     */
    public Flux<RealTimeQuote> subscribeToRealTimeQuotes(List<String> symbols) {
        log.info("실시간 가격 스트림 구독 시작 - symbols: {}", symbols);
        
        // 구독 목록에 추가
        symbols.forEach(symbol -> {
            symbolSubscriptions.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet())
                    .add("price_stream");
        });
        
        return Flux.interval(Duration.ofMillis(100)) // 100ms마다 업데이트
                .publishOn(Schedulers.parallel())
                .flatMap(tick -> {
                    List<RealTimeQuote> quotes = symbols.parallelStream()
                            .map(this::generateRealtimeQuote)
                            .collect(Collectors.toList());
                    
                    // 캐시 업데이트
                    quotes.forEach(quote -> {
                        realtimeQuotes.put(quote.getSymbol(), quote);
                        lastUpdateTime.put(quote.getSymbol(), LocalDateTime.now());
                    });
                    
                    return Flux.fromIterable(quotes);
                })
                .doOnError(error -> log.error("실시간 가격 스트림 오류: {}", error.getMessage()))
                .retry(3)
                .onErrorResume(error -> {
                    log.error("실시간 스트림 복구 불가: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * 시장 뎁스 (호가) 정보 조회
     * 
     * @param symbol 종목 코드
     * @param depth 호가 단계 (기본 10단계)
     * @return 시장 뎁스 정보
     */
    @Cacheable(value = "market-depth", key = "#symbol + '_' + #depth")
    public Mono<MarketDepth> getMarketDepth(String symbol, Integer depth) {
        log.debug("시장 뎁스 조회 - symbol: {}, depth: {}", symbol, depth);
        
        return Mono.fromCallable(() -> generateMarketDepth(symbol, depth))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(marketDepth -> {
                    marketDepthCache.put(symbol, marketDepth);
                    publishMarketDepthUpdate(symbol, marketDepth);
                })
                .doOnError(error -> log.error("시장 뎁스 조회 실패 - symbol: {}, error: {}", symbol, error.getMessage()));
    }

    /**
     * 최근 거래 내역 조회
     * 
     * @param symbol 종목 코드
     * @param limit 조회할 거래 수 (기본 100건)
     * @return 최근 거래 내역
     */
    public Flux<Trade> getRecentTrades(String symbol, Integer limit) {
        log.debug("최근 거래 내역 조회 - symbol: {}, limit: {}", symbol, limit);
        
        return Flux.fromIterable(generateRecentTrades(symbol, limit))
                .publishOn(Schedulers.parallel())
                .doOnComplete(() -> log.debug("거래 내역 조회 완료 - symbol: {}", symbol));
    }

    /**
     * 실시간 기술적 지표 계산
     * 
     * @param symbol 종목 코드
     * @param indicators 계산할 지표 목록
     * @param timeframe 시간 프레임
     * @return 기술적 지표 값들
     */
    public Mono<TechnicalIndicators> calculateRealTimeTechnicalIndicators(
            String symbol, List<String> indicators, String timeframe) {
        
        log.debug("실시간 기술적 지표 계산 - symbol: {}, indicators: {}, timeframe: {}", 
                symbol, indicators, timeframe);
        
        return Mono.fromCallable(() -> {
                    // 최근 가격 데이터 조회
                    List<PriceData> priceHistory = getPriceHistory(symbol, timeframe, 200);
                    
                    // 요청된 지표들 계산
                    TechnicalIndicators technicalIndicators = TechnicalIndicators.builder()
                            .symbol(symbol)
                            .timeframe(timeframe)
                            .timestamp(LocalDateTime.now())
                            .build();
                    
                    for (String indicator : indicators) {
                        calculateIndicator(indicator, priceHistory, technicalIndicators);
                    }
                    
                    return technicalIndicators;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(indicators -> {
                    technicalIndicatorsCache.put(symbol, indicators);
                    publishTechnicalIndicatorUpdate(symbol, indicators);
                })
                .doOnError(error -> 
                        log.error("기술적 지표 계산 실패 - symbol: {}, error: {}", symbol, error.getMessage()));
    }

    /**
     * 시장 이상 탐지
     * 
     * @param symbols 모니터링할 종목 목록
     * @return 탐지된 이상 현상들
     */
    public Flux<MarketAnomaly> detectMarketAnomalies(List<String> symbols) {
        log.info("시장 이상 탐지 시작 - symbols: {}", symbols.size());
        
        return Flux.interval(Duration.ofSeconds(1))
                .publishOn(Schedulers.parallel())
                .flatMap(tick -> {
                    return Flux.fromIterable(symbols)
                            .parallel()
                            .map(this::checkForAnomalies)
                            .filter(Objects::nonNull)
                            .sequential();
                })
                .distinctUntilKeyChanged(MarketAnomaly::getUniqueKey)
                .doOnNext(anomaly -> {
                    log.warn("시장 이상 탐지: {}", anomaly);
                    publishAnomalyAlert(anomaly);
                });
    }

    /**
     * 고빈도 거래량 분석
     * 
     * @param symbol 종목 코드
     * @param windowMinutes 분석 윈도우 (분)
     * @return 거래량 분석 결과
     */
    public Mono<VolumeAnalysis> analyzeHighFrequencyVolume(String symbol, Integer windowMinutes) {
        log.debug("고빈도 거래량 분석 - symbol: {}, window: {}분", symbol, windowMinutes);
        
        return Mono.fromCallable(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime windowStart = now.minusMinutes(windowMinutes);
                    
                    // 윈도우 내 거래 데이터 수집
                    List<Trade> windowTrades = getTradesInWindow(symbol, windowStart, now);
                    
                    return VolumeAnalysis.builder()
                            .symbol(symbol)
                            .windowStart(windowStart)
                            .windowEnd(now)
                            .totalVolume(calculateTotalVolume(windowTrades))
                            .averageTradeSize(calculateAverageTradeSize(windowTrades))
                            .vwap(calculateVWAP(windowTrades))
                            .buyVolumeRatio(calculateBuyVolumeRatio(windowTrades))
                            .priceImpact(calculatePriceImpact(windowTrades))
                            .volumeProfile(generateVolumeProfile(windowTrades))
                            .liquidityScore(calculateLiquidityScore(windowTrades))
                            .marketImpactCost(estimateMarketImpactCost(windowTrades))
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> 
                        log.error("거래량 분석 실패 - symbol: {}, error: {}", symbol, error.getMessage()));
    }

    /**
     * 실시간 시장 통계
     * 
     * @param symbols 종목 목록
     * @return 시장 통계 스트림
     */
    public Flux<MarketStatistics> streamMarketStatistics(List<String> symbols) {
        log.info("실시간 시장 통계 스트림 시작 - symbols: {}", symbols.size());
        
        return Flux.interval(Duration.ofSeconds(5))
                .publishOn(Schedulers.parallel())
                .map(tick -> {
                    Map<String, RealTimeQuote> currentQuotes = symbols.stream()
                            .collect(Collectors.toMap(
                                    symbol -> symbol,
                                    symbol -> realtimeQuotes.getOrDefault(symbol, generateRealtimeQuote(symbol))
                            ));
                    
                    return MarketStatistics.builder()
                            .timestamp(LocalDateTime.now())
                            .totalSymbols(symbols.size())
                            .activeSymbols(countActiveSymbols(currentQuotes))
                            .totalMarketCap(calculateTotalMarketCap(currentQuotes))
                            .averageVolatility(calculateAverageVolatility(currentQuotes))
                            .marketSentiment(calculateMarketSentiment(currentQuotes))
                            .topGainers(getTopGainers(currentQuotes, 5))
                            .topLosers(getTopLosers(currentQuotes, 5))
                            .highestVolume(getHighestVolumeSymbols(currentQuotes, 5))
                            .correlationMatrix(calculateRealtimeCorrelations(currentQuotes))
                            .volatilityIndex(calculateVolatilityIndex(currentQuotes))
                            .build();
                })
                .distinctUntilKeyChanged(stats -> stats.getTimestamp().withSecond(0)) // 초 단위 중복 제거
                .doOnNext(stats -> cacheMarketStatistics(stats));
    }

    /**
     * 심볼별 구독 상태 조회
     * 
     * @return 구독 상태 맵
     */
    public Map<String, SubscriptionStatus> getSubscriptionStatuses() {
        return symbolSubscriptions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> SubscriptionStatus.builder()
                                .symbol(entry.getKey())
                                .subscriberCount(entry.getValue().size())
                                .lastUpdate(lastUpdateTime.get(entry.getKey()))
                                .isActive(isSubscriptionActive(entry.getKey()))
                                .dataQuality(calculateDataQuality(entry.getKey()))
                                .build()
                ));
    }

    // === Private Helper Methods ===

    private RealTimeQuote generateRealtimeQuote(String symbol) {
        // 실제 구현에서는 외부 데이터 소스에서 조회
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextDouble() * 400); // $100-500
        BigDecimal change = basePrice.multiply(BigDecimal.valueOf((random.nextDouble() - 0.5) * 0.05)); // ±2.5%
        BigDecimal price = basePrice.add(change);
        
        return RealTimeQuote.builder()
                .symbol(symbol)
                .price(price.setScale(2, RoundingMode.HALF_UP))
                .change(change.setScale(2, RoundingMode.HALF_UP))
                .changePercent(change.divide(basePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                .volume(random.nextLong(10000, 1000000))
                .bid(price.subtract(BigDecimal.valueOf(0.01)))
                .ask(price.add(BigDecimal.valueOf(0.01)))
                .high52Week(price.multiply(BigDecimal.valueOf(1.8)))
                .low52Week(price.multiply(BigDecimal.valueOf(0.6)))
                .marketCap(BigDecimal.valueOf(random.nextLong(1000000000L, 100000000000L)))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private MarketDepth generateMarketDepth(String symbol, Integer depth) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextDouble() * 400);
        
        List<OrderBookEntry> bids = new ArrayList<>();
        List<OrderBookEntry> asks = new ArrayList<>();
        
        // 매수 호가 (bid)
        for (int i = 0; i < depth; i++) {
            BigDecimal price = basePrice.subtract(BigDecimal.valueOf((i + 1) * 0.01));
            Long quantity = random.nextLong(100, 10000);
            bids.add(OrderBookEntry.builder()
                    .price(price.setScale(2, RoundingMode.HALF_UP))
                    .quantity(quantity)
                    .orderCount(random.nextInt(1, 20))
                    .side("BID")
                    .build());
        }
        
        // 매도 호가 (ask)
        for (int i = 0; i < depth; i++) {
            BigDecimal price = basePrice.add(BigDecimal.valueOf((i + 1) * 0.01));
            Long quantity = random.nextLong(100, 10000);
            asks.add(OrderBookEntry.builder()
                    .price(price.setScale(2, RoundingMode.HALF_UP))
                    .quantity(quantity)
                    .orderCount(random.nextInt(1, 20))
                    .side("ASK")
                    .build());
        }
        
        return MarketDepth.builder()
                .symbol(symbol)
                .bids(bids)
                .asks(asks)
                .spread(asks.get(0).getPrice().subtract(bids.get(0).getPrice()))
                .totalBidVolume(bids.stream().mapToLong(OrderBookEntry::getQuantity).sum())
                .totalAskVolume(asks.stream().mapToLong(OrderBookEntry::getQuantity).sum())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private List<Trade> generateRecentTrades(String symbol, Integer limit) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Trade> trades = new ArrayList<>();
        
        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextDouble() * 400);
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < limit; i++) {
            BigDecimal price = basePrice.multiply(BigDecimal.valueOf(0.98 + random.nextDouble() * 0.04)); // ±2%
            Long quantity = random.nextLong(10, 1000);
            
            trades.add(Trade.builder()
                    .symbol(symbol)
                    .price(price.setScale(2, RoundingMode.HALF_UP))
                    .quantity(quantity)
                    .side(random.nextBoolean() ? "BUY" : "SELL")
                    .timestamp(now.minusSeconds(i * 2)) // 2초 간격
                    .tradeId(UUID.randomUUID().toString())
                    .build());
        }
        
        return trades;
    }

    private List<PriceData> getPriceHistory(String symbol, String timeframe, int periods) {
        // Mock 가격 히스토리 데이터 생성
        List<PriceData> history = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        BigDecimal basePrice = BigDecimal.valueOf(100);
        LocalDateTime startTime = LocalDateTime.now().minusHours(periods);
        
        for (int i = 0; i < periods; i++) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(0.99 + random.nextDouble() * 0.02)); // ±1% 변동
            
            history.add(PriceData.builder()
                    .symbol(symbol)
                    .timestamp(startTime.plusHours(i))
                    .open(basePrice)
                    .high(basePrice.multiply(BigDecimal.valueOf(1.01)))
                    .low(basePrice.multiply(BigDecimal.valueOf(0.99)))
                    .close(basePrice)
                    .volume(random.nextLong(10000, 100000))
                    .build());
        }
        
        return history;
    }

    private void calculateIndicator(String indicator, List<PriceData> priceHistory, TechnicalIndicators indicators) {
        // 실제 구현에서는 각 지표별 계산 로직
        switch (indicator) {
            case "SMA_20":
                indicators.setSma20(calculateSMA(priceHistory, 20));
                break;
            case "EMA_12":
                indicators.setEma12(calculateEMA(priceHistory, 12));
                break;
            case "RSI_14":
                indicators.setRsi14(calculateRSI(priceHistory, 14));
                break;
            case "MACD":
                indicators.setMacd(calculateMACD(priceHistory));
                break;
            case "BOLLINGER_BANDS":
                indicators.setBollingerBands(calculateBollingerBands(priceHistory, 20, 2));
                break;
            default:
                log.warn("지원하지 않는 기술적 지표: {}", indicator);
        }
    }

    private MarketAnomaly checkForAnomalies(String symbol) {
        RealTimeQuote quote = realtimeQuotes.get(symbol);
        if (quote == null) return null;
        
        // 이상 조건 체크
        if (Math.abs(quote.getChangePercent().doubleValue()) > 10) { // 10% 이상 급등락
            return MarketAnomaly.builder()
                    .symbol(symbol)
                    .type("PRICE_SPIKE")
                    .severity("HIGH")
                    .description(String.format("%s가 %.2f%% 급변동", symbol, quote.getChangePercent()))
                    .currentPrice(quote.getPrice())
                    .threshold(BigDecimal.valueOf(10))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        if (quote.getVolume() > 5000000) { // 대량 거래
            return MarketAnomaly.builder()
                    .symbol(symbol)
                    .type("VOLUME_SPIKE")
                    .severity("MEDIUM")
                    .description(String.format("%s에서 대량 거래 발생: %,d주", symbol, quote.getVolume()))
                    .currentVolume(quote.getVolume())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        return null;
    }

    // 기술적 지표 계산 메서드들 (간소화된 버전)
    private BigDecimal calculateSMA(List<PriceData> prices, int period) {
        return prices.stream()
                .skip(Math.max(0, prices.size() - period))
                .map(PriceData::getClose)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.min(period, prices.size())), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEMA(List<PriceData> prices, int period) {
        if (prices.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = prices.get(0).getClose();
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal price = prices.get(i).getClose();
            ema = price.multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRSI(List<PriceData> prices, int period) {
        if (prices.size() < period + 1) return BigDecimal.valueOf(50);
        
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        
        for (int i = 1; i <= period; i++) {
            BigDecimal change = prices.get(i).getClose().subtract(prices.get(i-1).getClose());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.subtract(change);
            }
        }
        
        BigDecimal avgGain = gains.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100);
        
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
    }

    private MACDResult calculateMACD(List<PriceData> prices) {
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        BigDecimal macdLine = ema12.subtract(ema26);
        BigDecimal signalLine = macdLine.multiply(BigDecimal.valueOf(0.9)); // 간소화
        BigDecimal histogram = macdLine.subtract(signalLine);
        
        return MACDResult.builder()
                .macdLine(macdLine)
                .signalLine(signalLine)
                .histogram(histogram)
                .build();
    }

    private BollingerBandsResult calculateBollingerBands(List<PriceData> prices, int period, double stdDev) {
        BigDecimal sma = calculateSMA(prices, period);
        
        // 표준편차 계산 (간소화)
        BigDecimal variance = prices.stream()
                .skip(Math.max(0, prices.size() - period))
                .map(p -> p.getClose().subtract(sma).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.min(period, prices.size())), 4, RoundingMode.HALF_UP);
        
        BigDecimal stdDevValue = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal multiplier = BigDecimal.valueOf(stdDev);
        
        return BollingerBandsResult.builder()
                .upperBand(sma.add(stdDevValue.multiply(multiplier)))
                .middleBand(sma)
                .lowerBand(sma.subtract(stdDevValue.multiply(multiplier)))
                .build();
    }

    // 기타 헬퍼 메서드들...
    private void publishMarketDepthUpdate(String symbol, MarketDepth marketDepth) {
        // Redis Pub/Sub 또는 WebSocket으로 실시간 업데이트 발송
        log.debug("시장 뎁스 업데이트 발송 - symbol: {}", symbol);
    }

    private void publishTechnicalIndicatorUpdate(String symbol, TechnicalIndicators indicators) {
        log.debug("기술적 지표 업데이트 발송 - symbol: {}", symbol);
    }

    private void publishAnomalyAlert(MarketAnomaly anomaly) {
        log.warn("시장 이상 알림 발송: {}", anomaly);
    }

    private List<Trade> getTradesInWindow(String symbol, LocalDateTime start, LocalDateTime end) {
        return recentTrades.getOrDefault(symbol, Collections.emptyList()).stream()
                .filter(trade -> trade.getTimestamp().isAfter(start) && trade.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }

    private Long calculateTotalVolume(List<Trade> trades) {
        return trades.stream().mapToLong(Trade::getQuantity).sum();
    }

    private BigDecimal calculateAverageTradeSize(List<Trade> trades) {
        return trades.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(calculateTotalVolume(trades))
                        .divide(BigDecimal.valueOf(trades.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVWAP(List<Trade> trades) {
        if (trades.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalValue = trades.stream()
                .map(trade -> trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalVolume = calculateTotalVolume(trades);
        
        return totalVolume > 0 ? totalValue.divide(BigDecimal.valueOf(totalVolume), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private Double calculateBuyVolumeRatio(List<Trade> trades) {
        long buyVolume = trades.stream()
                .filter(trade -> "BUY".equals(trade.getSide()))
                .mapToLong(Trade::getQuantity).sum();
        
        long totalVolume = calculateTotalVolume(trades);
        return totalVolume > 0 ? (double) buyVolume / totalVolume : 0.5;
    }

    private BigDecimal calculatePriceImpact(List<Trade> trades) {
        if (trades.size() < 2) return BigDecimal.ZERO;
        
        BigDecimal firstPrice = trades.get(0).getPrice();
        BigDecimal lastPrice = trades.get(trades.size() - 1).getPrice();
        
        return lastPrice.subtract(firstPrice).divide(firstPrice, 4, RoundingMode.HALF_UP);
    }

    private VolumeProfile generateVolumeProfile(List<Trade> trades) {
        // 가격대별 거래량 분포 계산
        return VolumeProfile.builder()
                .priceVolumePairs(Collections.emptyList()) // 간소화
                .pocPrice(BigDecimal.valueOf(150)) // Point of Control
                .valueAreaHigh(BigDecimal.valueOf(155))
                .valueAreaLow(BigDecimal.valueOf(145))
                .build();
    }

    private Double calculateLiquidityScore(List<Trade> trades) {
        // 유동성 점수 계산 (0-1)
        return Math.min(1.0, trades.size() / 1000.0);
    }

    private BigDecimal estimateMarketImpactCost(List<Trade> trades) {
        // 시장 충격 비용 추정
        return calculatePriceImpact(trades).abs().multiply(BigDecimal.valueOf(0.1)); // 10% 가중치
    }

    // 시장 통계 관련 메서드들...
    private Integer countActiveSymbols(Map<String, RealTimeQuote> quotes) {
        return (int) quotes.values().stream()
                .filter(quote -> quote.getVolume() > 1000)
                .count();
    }

    private BigDecimal calculateTotalMarketCap(Map<String, RealTimeQuote> quotes) {
        return quotes.values().stream()
                .map(RealTimeQuote::getMarketCap)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double calculateAverageVolatility(Map<String, RealTimeQuote> quotes) {
        return quotes.values().stream()
                .mapToDouble(quote -> Math.abs(quote.getChangePercent().doubleValue()))
                .average().orElse(0.0);
    }

    private Double calculateMarketSentiment(Map<String, RealTimeQuote> quotes) {
        long gainers = quotes.values().stream()
                .mapToLong(quote -> quote.getChangePercent().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
                .sum();
        
        return quotes.isEmpty() ? 0.5 : (double) gainers / quotes.size();
    }

    private List<String> getTopGainers(Map<String, RealTimeQuote> quotes, int count) {
        return quotes.entrySet().stream()
                .sorted((a, b) -> b.getValue().getChangePercent().compareTo(a.getValue().getChangePercent()))
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> getTopLosers(Map<String, RealTimeQuote> quotes, int count) {
        return quotes.entrySet().stream()
                .sorted((a, b) -> a.getValue().getChangePercent().compareTo(b.getValue().getChangePercent()))
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> getHighestVolumeSymbols(Map<String, RealTimeQuote> quotes, int count) {
        return quotes.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().getVolume(), a.getValue().getVolume()))
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Map<String, Double>> calculateRealtimeCorrelations(Map<String, RealTimeQuote> quotes) {
        // 실시간 상관관계 계산 (간소화)
        Map<String, Map<String, Double>> correlations = new HashMap<>();
        for (String symbol1 : quotes.keySet()) {
            Map<String, Double> symbolCorrelations = new HashMap<>();
            for (String symbol2 : quotes.keySet()) {
                if (!symbol1.equals(symbol2)) {
                    symbolCorrelations.put(symbol2, ThreadLocalRandom.current().nextDouble(-0.5, 0.8));
                }
            }
            correlations.put(symbol1, symbolCorrelations);
        }
        return correlations;
    }

    private Double calculateVolatilityIndex(Map<String, RealTimeQuote> quotes) {
        return calculateAverageVolatility(quotes) * 100; // VIX와 유사한 지수
    }

    private void cacheMarketStatistics(MarketStatistics stats) {
        // Redis에 시장 통계 캐싱
        try {
            redisTemplate.opsForValue().set("market:stats:latest", stats, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.warn("시장 통계 캐싱 실패: {}", e.getMessage());
        }
    }

    private Boolean isSubscriptionActive(String symbol) {
        LocalDateTime lastUpdate = lastUpdateTime.get(symbol);
        return lastUpdate != null && lastUpdate.isAfter(LocalDateTime.now().minusSeconds(10));
    }

    private Double calculateDataQuality(String symbol) {
        // 데이터 품질 점수 계산 (0-1)
        Boolean isActive = isSubscriptionActive(symbol);
        Boolean hasRecentData = realtimeQuotes.containsKey(symbol);
        
        if (isActive && hasRecentData) return 1.0;
        if (hasRecentData) return 0.7;
        return 0.3;
    }
}