package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.service.MLTradingSignalService;
import com.stockquest.application.service.PortfolioOptimizationService;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.PortfolioOptimization;
import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.application.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ML 시그널 및 포트폴리오 최적화 WebSocket 컨트롤러
 * 실시간 ML 시그널, 최적화 결과, 성과 업데이트를 클라이언트에게 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MLSignalsWebSocketController implements WebSocketHandler {
    
    private final MLTradingSignalService mlTradingSignalService;
    private final PortfolioOptimizationService portfolioOptimizationService;
    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;
    
    // 연결된 세션 관리
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastSignalUpdate = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        log.info("ML 시그널 WebSocket 연결 설정: sessionId={}", session.getId());
        
        // 연결 성공 메시지 전송
        MLWebSocketMessage welcomeMessage = MLWebSocketMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .timestamp(LocalDateTime.now())
            .data(Map.of("message", "ML Signals WebSocket 연결이 설정되었습니다"))
            .build();
        
        sendMessage(session, welcomeMessage);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            MLWebSocketMessage request = objectMapper.readValue(payload, MLWebSocketMessage.class);
            
            log.debug("ML WebSocket 메시지 수신: sessionId={}, type={}", 
                session.getId(), request.getType());
            
            switch (request.getType()) {
                case SUBSCRIBE_SIGNALS -> handleSignalSubscription(session, request);
                case UNSUBSCRIBE_SIGNALS -> handleSignalUnsubscription(session, request);
                case SUBSCRIBE_OPTIMIZATION -> handleOptimizationSubscription(session, request);
                case UNSUBSCRIBE_OPTIMIZATION -> handleOptimizationUnsubscription(session, request);
                case REQUEST_SIGNAL_GENERATION -> handleSignalGenerationRequest(session, request);
                case REQUEST_PORTFOLIO_OPTIMIZATION -> handlePortfolioOptimizationRequest(session, request);
                default -> log.warn("알 수 없는 메시지 타입: {}", request.getType());
            }
            
        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 실패: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("ML WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        cleanupSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
        log.info("ML WebSocket 연결 종료: sessionId={}, status={}", 
            session.getId(), closeStatus.toString());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 시그널 구독 처리
     */
    private void handleSignalSubscription(WebSocketSession session, MLWebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            @SuppressWarnings("unchecked")
            List<String> symbols = (List<String>) data.get("symbols");
            
            if (symbols == null || symbols.isEmpty()) {
                sendErrorMessage(session, "구독할 심볼이 지정되지 않았습니다");
                return;
            }
            
            // 세션별 구독 정보 저장
            sessionSubscriptions.computeIfAbsent(session.getId(), k -> new HashSet<>())
                .addAll(symbols);
            
            log.info("시그널 구독 추가: sessionId={}, symbols={}", session.getId(), symbols);
            
            // 구독 확인 메시지 전송
            MLWebSocketMessage response = MLWebSocketMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "subscriptionType", "signals",
                    "symbols", symbols,
                    "message", "시그널 구독이 완료되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
            // 초기 시그널 데이터 전송
            generateAndSendInitialSignals(session, symbols);
            
        } catch (Exception e) {
            log.error("시그널 구독 처리 실패", e);
            sendErrorMessage(session, "시그널 구독 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 포트폴리오 최적화 구독 처리
     */
    private void handleOptimizationSubscription(WebSocketSession session, MLWebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            
            // 포트폴리오별 구독 키 생성
            String subscriptionKey = "portfolio_" + portfolioId;
            sessionSubscriptions.computeIfAbsent(session.getId(), k -> new HashSet<>())
                .add(subscriptionKey);
            
            log.info("포트폴리오 최적화 구독 추가: sessionId={}, portfolioId={}", 
                session.getId(), portfolioId);
            
            MLWebSocketMessage response = MLWebSocketMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "subscriptionType", "optimization",
                    "portfolioId", portfolioId,
                    "message", "포트폴리오 최적화 구독이 완료되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("최적화 구독 처리 실패", e);
            sendErrorMessage(session, "최적화 구독 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 실시간 시그널 생성 요청 처리
     */
    private void handleSignalGenerationRequest(WebSocketSession session, MLWebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            
            if (symbol == null || symbol.trim().isEmpty()) {
                sendErrorMessage(session, "심볼이 지정되지 않았습니다");
                return;
            }
            
            // 비동기로 시그널 생성
            mlTradingSignalService.generateTradingSignal(symbol)
                .thenAccept(signal -> {
                    MLWebSocketMessage response = MLWebSocketMessage.builder()
                        .type(MessageType.SIGNAL_GENERATED)
                        .timestamp(LocalDateTime.now())
                        .data(Map.of(
                            "signal", convertSignalToMap(signal),
                            "symbol", symbol
                        ))
                        .build();
                    
                    try {
                        sendMessage(session, response);
                    } catch (IOException e) {
                        log.error("시그널 생성 결과 전송 실패", e);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("시그널 생성 실패: symbol={}", symbol, throwable);
                    sendErrorMessage(session, "시그널 생성 중 오류가 발생했습니다: " + symbol);
                    return null;
                });
                
        } catch (Exception e) {
            log.error("시그널 생성 요청 처리 실패", e);
            sendErrorMessage(session, "시그널 생성 요청 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 포트폴리오 최적화 요청 처리
     */
    private void handlePortfolioOptimizationRequest(WebSocketSession session, MLWebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            String optimizationType = (String) data.getOrDefault("optimizationType", "MODERN_PORTFOLIO_THEORY");
            String objective = (String) data.getOrDefault("objective", "MAXIMIZE_SHARPE_RATIO");
            
            // 포트폴리오 조회
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            
            // 비동기로 최적화 실행
            portfolioOptimizationService.optimizePortfolio(
                    portfolio,
                    PortfolioOptimization.OptimizationType.valueOf(optimizationType),
                    PortfolioOptimization.OptimizationObjective.valueOf(objective),
                    null)
                .thenAccept(optimization -> {
                    MLWebSocketMessage response = MLWebSocketMessage.builder()
                        .type(MessageType.OPTIMIZATION_COMPLETED)
                        .timestamp(LocalDateTime.now())
                        .data(Map.of(
                            "optimization", convertOptimizationToMap(optimization),
                            "portfolioId", portfolioId
                        ))
                        .build();
                    
                    try {
                        sendMessage(session, response);
                        // 구독자들에게도 브로드캐스트
                        broadcastOptimizationUpdate(portfolioId, optimization);
                    } catch (IOException e) {
                        log.error("최적화 결과 전송 실패", e);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("포트폴리오 최적화 실패: portfolioId={}", portfolioId, throwable);
                    sendErrorMessage(session, "포트폴리오 최적화 중 오류가 발생했습니다");
                    return null;
                });
                
        } catch (Exception e) {
            log.error("최적화 요청 처리 실패", e);
            sendErrorMessage(session, "최적화 요청 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 정기적인 시그널 업데이트 (매 30초)
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastPeriodicSignalUpdates() {
        if (activeSessions.isEmpty()) return;
        
        // 모든 구독된 심볼 수집
        Set<String> allSubscribedSymbols = new HashSet<>();
        sessionSubscriptions.values().forEach(symbols -> 
            symbols.stream()
                .filter(symbol -> !symbol.startsWith("portfolio_"))
                .forEach(allSubscribedSymbols::add));
        
        if (allSubscribedSymbols.isEmpty()) return;
        
        log.debug("정기 시그널 업데이트 실행: symbols={}", allSubscribedSymbols.size());
        
        // 배치로 시그널 생성
        List<String> symbolList = new ArrayList<>(allSubscribedSymbols);
        mlTradingSignalService.generateBatchSignals(symbolList)
            .thenAccept(signals -> {
                signals.forEach(signal -> {
                    MLWebSocketMessage message = MLWebSocketMessage.builder()
                        .type(MessageType.SIGNAL_UPDATE)
                        .timestamp(LocalDateTime.now())
                        .data(Map.of(
                            "signal", convertSignalToMap(signal),
                            "symbol", signal.getSymbol()
                        ))
                        .build();
                    
                    broadcastToSubscribers(signal.getSymbol(), message);
                    lastSignalUpdate.put(signal.getSymbol(), LocalDateTime.now());
                });
            })
            .exceptionally(throwable -> {
                log.error("정기 시그널 업데이트 실패", throwable);
                return null;
            });
    }
    
    /**
     * 초기 시그널 데이터 전송
     */
    private void generateAndSendInitialSignals(WebSocketSession session, List<String> symbols) {
        mlTradingSignalService.generateBatchSignals(symbols)
            .thenAccept(signals -> {
                MLWebSocketMessage message = MLWebSocketMessage.builder()
                    .type(MessageType.INITIAL_SIGNALS)
                    .timestamp(LocalDateTime.now())
                    .data(Map.of(
                        "signals", signals.stream()
                            .map(this::convertSignalToMap)
                            .toList()
                    ))
                    .build();
                
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    log.error("초기 시그널 전송 실패", e);
                }
            })
            .exceptionally(throwable -> {
                log.error("초기 시그널 생성 실패: symbols={}", symbols, throwable);
                return null;
            });
    }
    
    /**
     * 특정 심볼 구독자들에게 브로드캐스트
     */
    private void broadcastToSubscribers(String symbol, MLWebSocketMessage message) {
        activeSessions.parallelStream()
            .filter(session -> {
                Set<String> subscriptions = sessionSubscriptions.get(session.getId());
                return subscriptions != null && subscriptions.contains(symbol);
            })
            .forEach(session -> {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    log.error("브로드캐스트 전송 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    /**
     * 최적화 업데이트 브로드캐스트
     */
    private void broadcastOptimizationUpdate(Long portfolioId, PortfolioOptimization optimization) {
        String subscriptionKey = "portfolio_" + portfolioId;
        MLWebSocketMessage message = MLWebSocketMessage.builder()
            .type(MessageType.OPTIMIZATION_UPDATE)
            .timestamp(LocalDateTime.now())
            .data(Map.of(
                "optimization", convertOptimizationToMap(optimization),
                "portfolioId", portfolioId
            ))
            .build();
        
        activeSessions.parallelStream()
            .filter(session -> {
                Set<String> subscriptions = sessionSubscriptions.get(session.getId());
                return subscriptions != null && subscriptions.contains(subscriptionKey);
            })
            .forEach(session -> {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    log.error("최적화 브로드캐스트 전송 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    private void handleSignalUnsubscription(WebSocketSession session, MLWebSocketMessage request) {
        // 구독 해제 로직
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        @SuppressWarnings("unchecked")
        List<String> symbols = (List<String>) data.get("symbols");
        
        if (symbols != null) {
            Set<String> subscriptions = sessionSubscriptions.get(session.getId());
            if (subscriptions != null) {
                subscriptions.removeAll(symbols);
            }
        }
    }
    
    private void handleOptimizationUnsubscription(WebSocketSession session, MLWebSocketMessage request) {
        // 최적화 구독 해제 로직
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        Long portfolioId = ((Number) data.get("portfolioId")).longValue();
        String subscriptionKey = "portfolio_" + portfolioId;
        
        Set<String> subscriptions = sessionSubscriptions.get(session.getId());
        if (subscriptions != null) {
            subscriptions.remove(subscriptionKey);
        }
    }
    
    private void cleanupSession(WebSocketSession session) {
        activeSessions.remove(session);
        sessionSubscriptions.remove(session.getId());
    }
    
    private void sendMessage(WebSocketSession session, MLWebSocketMessage message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }
    
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            MLWebSocketMessage message = MLWebSocketMessage.builder()
                .type(MessageType.ERROR)
                .timestamp(LocalDateTime.now())
                .data(Map.of("error", errorMessage))
                .build();
            
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패", e);
        }
    }
    
    private Map<String, Object> convertSignalToMap(TradingSignal signal) {
        return Map.of(
            "signalId", signal.getSignalId(),
            "symbol", signal.getSymbol(),
            "signalType", signal.getSignalType().name(),
            "strength", signal.getStrength().toString(),
            "confidence", signal.getConfidence().toString(),
            "expectedReturn", signal.getExpectedReturn().toString(),
            "targetPrice", signal.getTargetPrice() != null ? signal.getTargetPrice().toString() : "N/A",
            "generatedAt", signal.getGeneratedAt().toString(),
            "status", signal.getStatus().name()
        );
    }
    
    private Map<String, Object> convertOptimizationToMap(PortfolioOptimization optimization) {
        return Map.of(
            "optimizationId", optimization.getOptimizationId(),
            "portfolioId", optimization.getPortfolioId(),
            "optimizationType", optimization.getOptimizationType().name(),
            "expectedReturn", optimization.getExpectedPerformance().getExpectedReturn().toString(),
            "expectedRisk", optimization.getExpectedPerformance().getExpectedVolatility().toString(),
            "sharpeRatio", optimization.getExpectedPerformance().getExpectedSharpeRatio().toString(),
            "rebalancingRequired", optimization.requiresRebalancing(),
            "generatedAt", optimization.getGeneratedAt().toString()
        );
    }
    
    // 메시지 타입 및 DTO
    public enum MessageType {
        CONNECTION_ESTABLISHED,
        SUBSCRIBE_SIGNALS,
        UNSUBSCRIBE_SIGNALS,
        SUBSCRIBE_OPTIMIZATION,
        UNSUBSCRIBE_OPTIMIZATION,
        REQUEST_SIGNAL_GENERATION,
        REQUEST_PORTFOLIO_OPTIMIZATION,
        SUBSCRIPTION_CONFIRMED,
        SIGNAL_GENERATED,
        SIGNAL_UPDATE,
        INITIAL_SIGNALS,
        OPTIMIZATION_COMPLETED,
        OPTIMIZATION_UPDATE,
        ERROR
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MLWebSocketMessage {
        private MessageType type;
        private LocalDateTime timestamp;
        private Object data;
    }
}