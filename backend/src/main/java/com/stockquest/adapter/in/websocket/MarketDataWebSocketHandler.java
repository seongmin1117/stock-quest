package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.marketdata.RealTimeMarketDataService;
import com.stockquest.adapter.in.websocket.dto.WebSocketMessage;
import com.stockquest.adapter.in.websocket.dto.MarketDataSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 실시간 시장 데이터 WebSocket 핸들러
 * 클라이언트에게 실시간 주식 시세, 기술적 지표, 시장 깊이 데이터를 스트리밍
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataWebSocketHandler implements WebSocketHandler {

    private final RealTimeMarketDataService marketDataService;
    private final ObjectMapper objectMapper;
    
    // 세션별 구독 관리
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, List<Disposable>> sessionDisposables = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionSubscriptions.put(sessionId, ConcurrentHashMap.newKeySet());
        sessionDisposables.put(sessionId, new CopyOnWriteArrayList<>());
        
        log.info("📡 Market data WebSocket connection established: {}", sessionId);
        
        // 연결 확인 메시지 전송
        WebSocketMessage welcomeMessage = WebSocketMessage.builder()
            .type("CONNECTION_ESTABLISHED")
            .data(Map.of(
                "message", "실시간 시장 데이터 연결이 성공적으로 설정되었습니다.",
                "sessionId", sessionId,
                "availableCommands", List.of("SUBSCRIBE", "UNSUBSCRIBE", "GET_MARKET_STATUS")
            ))
            .timestamp(System.currentTimeMillis())
            .build();
            
        sendMessage(session, welcomeMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        
        try {
            WebSocketMessage incomingMessage = objectMapper.readValue(
                ((TextMessage) message).getPayload(), 
                WebSocketMessage.class
            );
            
            log.debug("📥 Received message from {}: {}", sessionId, incomingMessage.getType());
            
            switch (incomingMessage.getType()) {
                case "SUBSCRIBE" -> handleSubscribe(session, incomingMessage);
                case "UNSUBSCRIBE" -> handleUnsubscribe(session, incomingMessage);
                case "GET_MARKET_STATUS" -> handleGetMarketStatus(session);
                case "GET_SUBSCRIPTIONS" -> handleGetSubscriptions(session);
                default -> {
                    log.warn("⚠️ Unknown message type: {}", incomingMessage.getType());
                    sendErrorMessage(session, "Unknown message type: " + incomingMessage.getType());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error handling WebSocket message from {}: {}", sessionId, e.getMessage());
            sendErrorMessage(session, "Message processing error: " + e.getMessage());
        }
    }

    private void handleSubscribe(WebSocketSession session, WebSocketMessage message) {
        String sessionId = session.getId();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            MarketDataSubscription subscription = objectMapper.convertValue(data, MarketDataSubscription.class);
            
            List<String> symbols = subscription.getSymbols();
            String dataType = subscription.getDataType(); // "QUOTES", "TECHNICAL_INDICATORS", "MARKET_DEPTH"
            
            log.info("📊 Subscribing {} to {} data for symbols: {}", sessionId, dataType, symbols);
            
            Set<String> currentSubscriptions = sessionSubscriptions.get(sessionId);
            List<Disposable> disposables = sessionDisposables.get(sessionId);
            
            // 실시간 데이터 스트림 구독 시작
            switch (dataType) {
                case "QUOTES" -> {
                    Disposable quotesDisposable = marketDataService
                        .subscribeToRealTimeQuotes(symbols)
                        .subscribe(
                            quote -> sendQuoteUpdate(session, quote),
                            error -> {
                                log.error("❌ Quote stream error for {}: {}", sessionId, error.getMessage());
                                sendErrorMessage(session, "Quote stream error: " + error.getMessage());
                            }
                        );
                    disposables.add(quotesDisposable);
                }
                
                case "TECHNICAL_INDICATORS" -> {
                    // 기술적 지표는 5초마다 업데이트
                    Disposable indicatorsDisposable = Flux.interval(Duration.ofSeconds(5))
                        .flatMap(tick -> Flux.fromIterable(symbols)
                            .map(marketDataService::calculateRealTimeTechnicalIndicators)
                        )
                        .subscribe(
                            indicators -> sendTechnicalIndicatorsUpdate(session, indicators),
                            error -> {
                                log.error("❌ Technical indicators stream error for {}: {}", sessionId, error.getMessage());
                                sendErrorMessage(session, "Indicators stream error: " + error.getMessage());
                            }
                        );
                    disposables.add(indicatorsDisposable);
                }
                
                case "MARKET_DEPTH" -> {
                    // 시장 깊이 데이터는 1초마다 업데이트
                    Disposable depthDisposable = Flux.interval(Duration.ofSeconds(1))
                        .flatMap(tick -> Flux.fromIterable(symbols)
                            .map(marketDataService::getMarketDepth)
                        )
                        .subscribe(
                            depth -> sendMarketDepthUpdate(session, depth),
                            error -> {
                                log.error("❌ Market depth stream error for {}: {}", sessionId, error.getMessage());
                                sendErrorMessage(session, "Market depth stream error: " + error.getMessage());
                            }
                        );
                    disposables.add(depthDisposable);
                }
                
                default -> {
                    sendErrorMessage(session, "Unsupported data type: " + dataType);
                    return;
                }
            }
            
            // 구독 정보 저장
            symbols.forEach(symbol -> currentSubscriptions.add(symbol + ":" + dataType));
            
            // 구독 성공 응답
            WebSocketMessage response = WebSocketMessage.builder()
                .type("SUBSCRIPTION_SUCCESS")
                .data(Map.of(
                    "symbols", symbols,
                    "dataType", dataType,
                    "message", String.format("%s 데이터 구독이 성공적으로 시작되었습니다.", dataType)
                ))
                .timestamp(System.currentTimeMillis())
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("❌ Subscription error for {}: {}", sessionId, e.getMessage());
            sendErrorMessage(session, "Subscription error: " + e.getMessage());
        }
    }

    private void handleUnsubscribe(WebSocketSession session, WebSocketMessage message) {
        String sessionId = session.getId();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            
            @SuppressWarnings("unchecked")
            List<String> symbols = (List<String>) data.get("symbols");
            String dataType = (String) data.get("dataType");
            
            log.info("🚫 Unsubscribing {} from {} data for symbols: {}", sessionId, dataType, symbols);
            
            Set<String> currentSubscriptions = sessionSubscriptions.get(sessionId);
            symbols.forEach(symbol -> currentSubscriptions.remove(symbol + ":" + dataType));
            
            // 구독 해제 성공 응답
            WebSocketMessage response = WebSocketMessage.builder()
                .type("UNSUBSCRIPTION_SUCCESS")
                .data(Map.of(
                    "symbols", symbols,
                    "dataType", dataType,
                    "message", "구독이 성공적으로 해제되었습니다."
                ))
                .timestamp(System.currentTimeMillis())
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("❌ Unsubscription error for {}: {}", sessionId, e.getMessage());
            sendErrorMessage(session, "Unsubscription error: " + e.getMessage());
        }
    }

    private void handleGetMarketStatus(WebSocketSession session) {
        var marketStatus = marketDataService.getMarketStatus();
        
        WebSocketMessage response = WebSocketMessage.builder()
            .type("MARKET_STATUS")
            .data(marketStatus)
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, response);
    }

    private void handleGetSubscriptions(WebSocketSession session) {
        String sessionId = session.getId();
        Set<String> subscriptions = sessionSubscriptions.get(sessionId);
        
        WebSocketMessage response = WebSocketMessage.builder()
            .type("CURRENT_SUBSCRIPTIONS")
            .data(Map.of(
                "subscriptions", subscriptions,
                "count", subscriptions.size()
            ))
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, response);
    }

    private void sendQuoteUpdate(WebSocketSession session, Object quote) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("QUOTE_UPDATE")
            .data(quote)
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, message);
    }

    private void sendTechnicalIndicatorsUpdate(WebSocketSession session, Object indicators) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("TECHNICAL_INDICATORS_UPDATE")
            .data(indicators)
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, message);
    }

    private void sendMarketDepthUpdate(WebSocketSession session, Object depth) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("MARKET_DEPTH_UPDATE")
            .data(depth)
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, message);
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("❌ Failed to send message to {}: {}", session.getId(), e.getMessage());
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        WebSocketMessage errorMessage = WebSocketMessage.builder()
            .type("ERROR")
            .data(Map.of("error", error))
            .timestamp(System.currentTimeMillis())
            .build();
        
        sendMessage(session, errorMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("🚨 WebSocket transport error for {}: {}", sessionId, exception.getMessage());
        
        // 세션 정리
        cleanupSession(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("🔌 Market data WebSocket connection closed: {} (Status: {})", sessionId, closeStatus);
        
        // 세션 정리
        cleanupSession(sessionId);
    }

    private void cleanupSession(String sessionId) {
        // 모든 구독 해제
        List<Disposable> disposables = sessionDisposables.remove(sessionId);
        if (disposables != null) {
            disposables.forEach(disposable -> {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            });
        }
        
        // 세션 정보 정리
        sessions.remove(sessionId);
        sessionSubscriptions.remove(sessionId);
        
        log.debug("🧹 Session cleanup completed for: {}", sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}