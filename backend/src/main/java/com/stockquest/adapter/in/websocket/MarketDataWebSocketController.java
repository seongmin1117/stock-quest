package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.service.MarketDataService;
import com.stockquest.application.service.RealTimeMarketDataService;
import com.stockquest.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 실시간 시장 데이터 WebSocket 컨트롤러
 * Phase 2.2: WebSocket 실시간 기능 구현 - 시장 데이터 스트리밍
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataWebSocketController implements WebSocketHandler {
    
    private final MarketDataService marketDataService;
    private final RealTimeMarketDataService realTimeMarketDataService;
    private final ObjectMapper objectMapper;
    
    // 연결된 세션 관리
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, MarketDataSubscription> subscriptionDetails = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastUpdateTime = new ConcurrentHashMap<>();
    
    // 인기 종목 및 기본 구독 목록
    private static final List<String> DEFAULT_SYMBOLS = Arrays.asList(
        "AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "META", "NVDA", "SPY", "QQQ", "BTC"
    );
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        log.info("시장 데이터 WebSocket 연결 설정: sessionId={}", session.getId());
        
        // 연결 성공 메시지 전송
        MarketDataMessage welcomeMessage = MarketDataMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .timestamp(LocalDateTime.now())
            .data(Map.of(
                "message", "시장 데이터 WebSocket 연결이 설정되었습니다",
                "availableSymbols", DEFAULT_SYMBOLS,
                "subscriptionTypes", Arrays.asList("REAL_TIME", "QUOTES", "VOLUME", "ANALYTICS")
            ))
            .build();
        
        sendMessage(session, welcomeMessage);
        
        // 기본 시장 개요 전송
        sendMarketOverview(session);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            MarketDataMessage request = objectMapper.readValue(payload, MarketDataMessage.class);
            
            log.debug("시장 데이터 WebSocket 메시지 수신: sessionId={}, type={}", 
                session.getId(), request.getType());
            
            switch (request.getType()) {
                case SUBSCRIBE -> handleSubscription(session, request);
                case UNSUBSCRIBE -> handleUnsubscription(session, request);
                case REQUEST_QUOTE -> handleQuoteRequest(session, request);
                case REQUEST_HISTORICAL -> handleHistoricalRequest(session, request);
                case REQUEST_ANALYTICS -> handleAnalyticsRequest(session, request);
                case UPDATE_SUBSCRIPTION -> handleSubscriptionUpdate(session, request);
                case HEARTBEAT -> handleHeartbeat(session, request);
                default -> log.warn("알 수 없는 메시지 타입: {}", request.getType());
            }
            
        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 실패: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("시장 데이터 WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        cleanupSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
        log.info("시장 데이터 WebSocket 연결 종료: sessionId={}, status={}", 
            session.getId(), closeStatus.toString());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 구독 처리
     */
    private void handleSubscription(WebSocketSession session, MarketDataMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            @SuppressWarnings("unchecked")
            List<String> symbols = (List<String>) data.get("symbols");
            String subscriptionType = (String) data.getOrDefault("subscriptionType", "REAL_TIME");
            Integer updateInterval = (Integer) data.getOrDefault("updateInterval", 1000); // 기본 1초
            
            if (symbols == null || symbols.isEmpty()) {
                sendErrorMessage(session, "구독할 심볼이 지정되지 않았습니다");
                return;
            }
            
            // 구독 정보 저장
            MarketDataSubscription subscription = MarketDataSubscription.builder()
                .sessionId(session.getId())
                .symbols(new HashSet<>(symbols))
                .subscriptionType(SubscriptionType.valueOf(subscriptionType))
                .updateInterval(updateInterval)
                .subscribedAt(LocalDateTime.now())
                .active(true)
                .build();
            
            subscriptionDetails.put(session.getId(), subscription);
            sessionSubscriptions.computeIfAbsent(session.getId(), k -> new HashSet<>())
                .addAll(symbols);
            
            log.info("시장 데이터 구독 추가: sessionId={}, symbols={}, type={}", 
                session.getId(), symbols, subscriptionType);
            
            // 구독 확인 메시지 전송
            MarketDataMessage response = MarketDataMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbols", symbols,
                    "subscriptionType", subscriptionType,
                    "updateInterval", updateInterval,
                    "message", "시장 데이터 구독이 완료되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
            // 초기 데이터 전송
            sendInitialMarketData(session, symbols);
            
        } catch (Exception e) {
            log.error("구독 처리 실패", e);
            sendErrorMessage(session, "구독 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 실시간 시세 요청 처리
     */
    private void handleQuoteRequest(WebSocketSession session, MarketDataMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            
            if (symbol == null || symbol.trim().isEmpty()) {
                sendErrorMessage(session, "심볼이 지정되지 않았습니다");
                return;
            }
            
            // 최신 시장 데이터 조회
            Stock marketData = marketDataService.getLatestMarketData(symbol);
            
            MarketDataMessage response = MarketDataMessage.builder()
                .type(MessageType.QUOTE_RESPONSE)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbol", symbol,
                    "quote", convertStockToMap(marketData),
                    "timestamp", marketData.getTimestamp()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("시세 요청 처리 실패", e);
            sendErrorMessage(session, "시세 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 역사적 데이터 요청 처리
     */
    private void handleHistoricalRequest(WebSocketSession session, MarketDataMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            Integer days = (Integer) data.getOrDefault("days", 30);
            
            if (symbol == null || symbol.trim().isEmpty()) {
                sendErrorMessage(session, "심볼이 지정되지 않았습니다");
                return;
            }
            
            // 역사적 데이터 조회
            List<Stock> historicalData = marketDataService.getHistoricalData(
                Collections.singletonList(symbol), days);
            
            MarketDataMessage response = MarketDataMessage.builder()
                .type(MessageType.HISTORICAL_DATA)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbol", symbol,
                    "days", days,
                    "data", historicalData.stream()
                        .map(this::convertStockToMap)
                        .toList()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("역사적 데이터 요청 처리 실패", e);
            sendErrorMessage(session, "역사적 데이터 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 분석 데이터 요청 처리
     */
    private void handleAnalyticsRequest(WebSocketSession session, MarketDataMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            Integer period = (Integer) data.getOrDefault("period", 20);
            
            if (symbol == null || symbol.trim().isEmpty()) {
                sendErrorMessage(session, "심볼이 지정되지 않았습니다");
                return;
            }
            
            // 분석 데이터 계산
            BigDecimal volatility = marketDataService.getVolatility(symbol, period);
            List<BigDecimal> dailyReturns = marketDataService.getDailyReturns(symbol, period);
            
            MarketDataMessage response = MarketDataMessage.builder()
                .type(MessageType.ANALYTICS_DATA)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbol", symbol,
                    "period", period,
                    "volatility", volatility.toString(),
                    "dailyReturns", dailyReturns.stream()
                        .map(BigDecimal::toString)
                        .toList(),
                    "averageReturn", dailyReturns.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(dailyReturns.size()), 6, BigDecimal.ROUND_HALF_UP)
                        .toString()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("분석 데이터 요청 처리 실패", e);
            sendErrorMessage(session, "분석 데이터 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 실시간 시장 데이터 업데이트 (매 1초)
     */
    @Scheduled(fixedRate = 1000)
    public void broadcastRealTimeMarketData() {
        if (activeSessions.isEmpty()) return;
        
        // 모든 구독된 심볼 수집
        Set<String> allSubscribedSymbols = new HashSet<>();
        sessionSubscriptions.values().forEach(allSubscribedSymbols::addAll);
        
        if (allSubscribedSymbols.isEmpty()) return;
        
        log.debug("실시간 시장 데이터 업데이트 실행: symbols={}", allSubscribedSymbols.size());
        
        // 각 심볼에 대한 실시간 데이터 생성 및 전송
        allSubscribedSymbols.forEach(symbol -> {
            try {
                Stock marketData = marketDataService.getLatestMarketData(symbol);
                
                MarketDataMessage message = MarketDataMessage.builder()
                    .type(MessageType.REAL_TIME_UPDATE)
                    .timestamp(LocalDateTime.now())
                    .data(Map.of(
                        "symbol", symbol,
                        "quote", convertStockToMap(marketData),
                        "change", calculatePriceChange(symbol, marketData.getClosePrice()),
                        "changePercent", calculatePercentChange(symbol, marketData.getClosePrice())
                    ))
                    .build();
                
                broadcastToSubscribers(symbol, message);
                lastUpdateTime.put(symbol, LocalDateTime.now());
                
            } catch (Exception e) {
                log.error("실시간 데이터 업데이트 실패: symbol={}", symbol, e);
            }
        });
    }
    
    /**
     * 시장 개요 업데이트 (매 30초)
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastMarketOverview() {
        if (activeSessions.isEmpty()) return;
        
        try {
            // 주요 지수 및 인기 종목 데이터
            List<Stock> majorIndices = marketDataService.getLatestMarketData(DEFAULT_SYMBOLS);
            
            MarketDataMessage overviewMessage = MarketDataMessage.builder()
                .type(MessageType.MARKET_OVERVIEW)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "majorIndices", majorIndices.stream()
                        .map(this::convertStockToMap)
                        .toList(),
                    "marketStatus", getCurrentMarketStatus(),
                    "tradingVolume", calculateTotalVolume(majorIndices),
                    "marketSentiment", calculateMarketSentiment(majorIndices)
                ))
                .build();
            
            // 모든 활성 세션에 브로드캐스트
            activeSessions.parallelStream()
                .forEach(session -> {
                    try {
                        sendMessage(session, overviewMessage);
                    } catch (IOException e) {
                        log.error("시장 개요 브로드캐스트 실패: sessionId={}", session.getId(), e);
                    }
                });
                
        } catch (Exception e) {
            log.error("시장 개요 업데이트 실패", e);
        }
    }
    
    // ========================= Helper Methods =========================
    
    private void handleUnsubscription(WebSocketSession session, MarketDataMessage request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        @SuppressWarnings("unchecked")
        List<String> symbols = (List<String>) data.get("symbols");
        
        if (symbols != null) {
            Set<String> subscriptions = sessionSubscriptions.get(session.getId());
            if (subscriptions != null) {
                subscriptions.removeAll(symbols);
                
                // 구독이 모두 해제되면 구독 정보 정리
                if (subscriptions.isEmpty()) {
                    subscriptionDetails.remove(session.getId());
                }
            }
            
            log.info("시장 데이터 구독 해제: sessionId={}, symbols={}", session.getId(), symbols);
        }
    }
    
    private void handleSubscriptionUpdate(WebSocketSession session, MarketDataMessage request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        Integer newInterval = (Integer) data.get("updateInterval");
        
        MarketDataSubscription subscription = subscriptionDetails.get(session.getId());
        if (subscription != null && newInterval != null) {
            subscription.setUpdateInterval(newInterval);
            log.info("구독 업데이트: sessionId={}, newInterval={}", session.getId(), newInterval);
        }
    }
    
    private void handleHeartbeat(WebSocketSession session, MarketDataMessage request) {
        MarketDataMessage heartbeatResponse = MarketDataMessage.builder()
            .type(MessageType.HEARTBEAT)
            .timestamp(LocalDateTime.now())
            .data(Map.of("status", "alive", "serverTime", LocalDateTime.now()))
            .build();
        
        try {
            sendMessage(session, heartbeatResponse);
        } catch (IOException e) {
            log.error("하트비트 응답 전송 실패", e);
        }
    }
    
    private void sendInitialMarketData(WebSocketSession session, List<String> symbols) {
        try {
            List<Stock> initialData = marketDataService.getLatestMarketData(symbols);
            
            MarketDataMessage message = MarketDataMessage.builder()
                .type(MessageType.INITIAL_DATA)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbols", symbols,
                    "data", initialData.stream()
                        .map(this::convertStockToMap)
                        .toList()
                ))
                .build();
            
            sendMessage(session, message);
            
        } catch (IOException e) {
            log.error("초기 시장 데이터 전송 실패", e);
        }
    }
    
    private void sendMarketOverview(WebSocketSession session) {
        try {
            List<Stock> overview = marketDataService.getLatestMarketData(DEFAULT_SYMBOLS.subList(0, 5));
            
            MarketDataMessage message = MarketDataMessage.builder()
                .type(MessageType.MARKET_OVERVIEW)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "overview", overview.stream()
                        .map(this::convertStockToMap)
                        .toList(),
                    "marketStatus", getCurrentMarketStatus()
                ))
                .build();
            
            sendMessage(session, message);
            
        } catch (IOException e) {
            log.error("시장 개요 전송 실패", e);
        }
    }
    
    private void broadcastToSubscribers(String symbol, MarketDataMessage message) {
        activeSessions.parallelStream()
            .filter(session -> {
                Set<String> subscriptions = sessionSubscriptions.get(session.getId());
                return subscriptions != null && subscriptions.contains(symbol);
            })
            .forEach(session -> {
                try {
                    MarketDataSubscription subscription = subscriptionDetails.get(session.getId());
                    if (subscription != null && subscription.isActive()) {
                        sendMessage(session, message);
                    }
                } catch (IOException e) {
                    log.error("브로드캐스트 전송 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    private void cleanupSession(WebSocketSession session) {
        activeSessions.remove(session);
        sessionSubscriptions.remove(session.getId());
        subscriptionDetails.remove(session.getId());
    }
    
    private void sendMessage(WebSocketSession session, MarketDataMessage message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }
    
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            MarketDataMessage message = MarketDataMessage.builder()
                .type(MessageType.ERROR)
                .timestamp(LocalDateTime.now())
                .data(Map.of("error", errorMessage))
                .build();
            
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패", e);
        }
    }
    
    private Map<String, Object> convertStockToMap(Stock stock) {
        return Map.of(
            "symbol", stock.getSymbol(),
            "openPrice", stock.getOpenPrice().toString(),
            "highPrice", stock.getHighPrice().toString(),
            "lowPrice", stock.getLowPrice().toString(),
            "closePrice", stock.getClosePrice().toString(),
            "volume", stock.getVolume(),
            "timestamp", stock.getTimestamp()
        );
    }
    
    private BigDecimal calculatePriceChange(String symbol, BigDecimal currentPrice) {
        // 이전 가격과 비교하여 변화량 계산 (실제 구현에서는 캐시된 이전 가격 사용)
        BigDecimal previousPrice = currentPrice.multiply(BigDecimal.valueOf(0.999)); // 시뮬레이션
        return currentPrice.subtract(previousPrice);
    }
    
    private BigDecimal calculatePercentChange(String symbol, BigDecimal currentPrice) {
        BigDecimal change = calculatePriceChange(symbol, currentPrice);
        BigDecimal previousPrice = currentPrice.subtract(change);
        
        if (previousPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return change.divide(previousPrice, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private String getCurrentMarketStatus() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // 미국 시장 기준 (9:30-16:00 EST)
        if (hour >= 9 && hour <= 16) {
            return "OPEN";
        } else if (hour >= 4 && hour < 9) {
            return "PRE_MARKET";
        } else if (hour > 16 && hour <= 20) {
            return "AFTER_HOURS";
        } else {
            return "CLOSED";
        }
    }
    
    private long calculateTotalVolume(List<Stock> stocks) {
        return stocks.stream()
            .mapToLong(Stock::getVolume)
            .sum();
    }
    
    private String calculateMarketSentiment(List<Stock> stocks) {
        long positive = stocks.stream()
            .mapToLong(stock -> {
                BigDecimal change = calculatePriceChange(stock.getSymbol(), stock.getClosePrice());
                return change.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
            })
            .sum();
        
        double positiveRatio = (double) positive / stocks.size();
        
        if (positiveRatio > 0.6) {
            return "BULLISH";
        } else if (positiveRatio < 0.4) {
            return "BEARISH";
        } else {
            return "NEUTRAL";
        }
    }
    
    // ========================= DTOs and Enums =========================
    
    public enum MessageType {
        CONNECTION_ESTABLISHED,
        SUBSCRIBE,
        UNSUBSCRIBE,
        UPDATE_SUBSCRIPTION,
        SUBSCRIPTION_CONFIRMED,
        REQUEST_QUOTE,
        REQUEST_HISTORICAL,
        REQUEST_ANALYTICS,
        QUOTE_RESPONSE,
        HISTORICAL_DATA,
        ANALYTICS_DATA,
        REAL_TIME_UPDATE,
        INITIAL_DATA,
        MARKET_OVERVIEW,
        HEARTBEAT,
        ERROR
    }
    
    public enum SubscriptionType {
        REAL_TIME,
        QUOTES,
        VOLUME,
        ANALYTICS
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketDataMessage {
        private MessageType type;
        private LocalDateTime timestamp;
        private Object data;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketDataSubscription {
        private String sessionId;
        private Set<String> symbols;
        private SubscriptionType subscriptionType;
        private Integer updateInterval;
        private LocalDateTime subscribedAt;
        private boolean active;
        
        public void setUpdateInterval(Integer updateInterval) {
            this.updateInterval = updateInterval;
        }
    }
}