package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.order.PlaceOrderService;
import com.stockquest.application.analytics.PortfolioAnalyticsService;
import com.stockquest.application.challenge.ChallengeManagementService;
import com.stockquest.adapter.in.websocket.dto.WebSocketMessage;
import com.stockquest.adapter.in.websocket.dto.TradingCommand;
import com.stockquest.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.Disposable;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 실시간 트레이딩 WebSocket 핸들러
 * 주문 실행, 포트폴리오 업데이트, 거래 알림을 실시간으로 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradingWebSocketHandler implements WebSocketHandler {

    private final PlaceOrderService placeOrderService;
    private final PortfolioAnalyticsService portfolioAnalyticsService;
    private final ChallengeManagementService challengeManagementService;
    private final ObjectMapper objectMapper;
    
    // 세션별 사용자 정보 및 구독 관리
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>(); // sessionId -> userId
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>(); // userId -> sessionId
    private final Map<String, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>(); // sessionId -> challengeSessionIds
    private final Map<String, List<Disposable>> sessionDisposables = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionSubscriptions.put(sessionId, ConcurrentHashMap.newKeySet());
        sessionDisposables.put(sessionId, new CopyOnWriteArrayList<>());
        
        // 사용자 인증 정보 추출 (실제로는 JWT 토큰에서 추출해야 함)
        // 여기서는 간소화된 버전으로 구현
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            sessionUsers.put(sessionId, userId);
            userSessions.put(userId, sessionId);
            
            log.info("💼 Trading WebSocket connection established: {} (User: {})", sessionId, userId);
            
            // 포트폴리오 실시간 업데이트 구독 시작
            startPortfolioUpdates(session, userId);
            
        } else {
            log.warn("⚠️ Trading WebSocket connection without valid user: {}", sessionId);
        }
        
        // 연결 확인 메시지 전송
        WebSocketMessage<?> welcomeMessage = WebSocketMessage.success(
            "TRADING_CONNECTION_ESTABLISHED",
            Map.of(
                "message", "실시간 트레이딩 연결이 성공적으로 설정되었습니다.",
                "sessionId", sessionId,
                "userId", userId,
                "availableCommands", List.of(
                    "PLACE_ORDER", "CANCEL_ORDER", "GET_PORTFOLIO", 
                    "SUBSCRIBE_CHALLENGE", "GET_POSITIONS", "GET_ORDERS"
                )
            )
        );
        
        sendMessage(session, welcomeMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionUsers.get(sessionId);
        
        if (userId == null) {
            sendErrorMessage(session, "사용자 인증이 필요합니다.");
            return;
        }
        
        try {
            WebSocketMessage<?> incomingMessage = objectMapper.readValue(
                ((TextMessage) message).getPayload(), 
                WebSocketMessage.class
            );
            
            log.debug("📥 Trading message from user {}: {}", userId, incomingMessage.getType());
            
            switch (incomingMessage.getType()) {
                case "PLACE_ORDER" -> handlePlaceOrder(session, userId, incomingMessage);
                case "CANCEL_ORDER" -> handleCancelOrder(session, userId, incomingMessage);
                case "GET_PORTFOLIO" -> handleGetPortfolio(session, userId, incomingMessage);
                case "SUBSCRIBE_CHALLENGE" -> handleSubscribeChallenge(session, userId, incomingMessage);
                case "UNSUBSCRIBE_CHALLENGE" -> handleUnsubscribeChallenge(session, userId, incomingMessage);
                case "GET_POSITIONS" -> handleGetPositions(session, userId, incomingMessage);
                case "GET_ORDERS" -> handleGetOrders(session, userId, incomingMessage);
                case "PING" -> handlePing(session);
                default -> {
                    log.warn("⚠️ Unknown trading command: {}", incomingMessage.getType());
                    sendErrorMessage(session, "Unknown command: " + incomingMessage.getType());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error handling trading message from user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Message processing error: " + e.getMessage());
        }
    }

    private void handlePlaceOrder(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            TradingCommand.PlaceOrderCommand orderCommand = 
                objectMapper.convertValue(data, TradingCommand.PlaceOrderCommand.class);
            
            log.info("📈 Placing order for user {}: {} {} shares of {} at ${}", 
                userId, orderCommand.getOrderType(), orderCommand.getQuantity(), 
                orderCommand.getSymbol(), orderCommand.getPrice());
            
            // 주문 실행 (비동기)
            // 실제로는 placeOrderService.executeOrder를 호출해야 함
            // 여기서는 시뮬레이션으로 구현
            
            // 주문 접수 확인 메시지 즉시 전송
            WebSocketMessage<?> orderAccepted = WebSocketMessage.success(
                "ORDER_ACCEPTED",
                Map.of(
                    "orderId", "ORDER_" + System.currentTimeMillis(),
                    "symbol", orderCommand.getSymbol(),
                    "quantity", orderCommand.getQuantity(),
                    "orderType", orderCommand.getOrderType(),
                    "status", "PENDING",
                    "message", "주문이 접수되었습니다."
                )
            );
            sendMessage(session, orderAccepted);
            
            // 1-3초 후 주문 체결 시뮬레이션
            Flux.delay(Duration.ofSeconds(1 + (long)(Math.random() * 2)))
                .subscribe(tick -> {
                    WebSocketMessage<?> orderExecuted = WebSocketMessage.success(
                        "ORDER_EXECUTED",
                        Map.of(
                            "orderId", "ORDER_" + System.currentTimeMillis(),
                            "symbol", orderCommand.getSymbol(),
                            "quantity", orderCommand.getQuantity(),
                            "executedPrice", orderCommand.getPrice() * (0.995 + Math.random() * 0.01), // 약간의 슬리피지
                            "status", "EXECUTED",
                            "message", "주문이 체결되었습니다."
                        )
                    );
                    sendMessage(session, orderExecuted);
                    
                    // 포트폴리오 업데이트 트리거
                    triggerPortfolioUpdate(session, userId);
                });
                
        } catch (Exception e) {
            log.error("❌ Error placing order for user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Order placement error: " + e.getMessage());
        }
    }

    private void handleCancelOrder(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String orderId = (String) data.get("orderId");
            
            log.info("❌ Cancelling order {} for user {}", orderId, userId);
            
            WebSocketMessage<?> orderCancelled = WebSocketMessage.success(
                "ORDER_CANCELLED",
                Map.of(
                    "orderId", orderId,
                    "status", "CANCELLED",
                    "message", "주문이 취소되었습니다."
                )
            );
            sendMessage(session, orderCancelled);
            
        } catch (Exception e) {
            log.error("❌ Error cancelling order for user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Order cancellation error: " + e.getMessage());
        }
    }

    private void handleGetPortfolio(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            Long challengeSessionId = Long.valueOf(data.get("challengeSessionId").toString());
            
            // 포트폴리오 분석 데이터 가져오기
            var portfolioAnalytics = portfolioAnalyticsService.calculatePortfolioAnalytics(
                challengeSessionId, "1M"
            );
            
            WebSocketMessage<?> portfolioResponse = WebSocketMessage.success(
                "PORTFOLIO_DATA",
                portfolioAnalytics
            );
            sendMessage(session, portfolioResponse);
            
        } catch (Exception e) {
            log.error("❌ Error getting portfolio for user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Portfolio retrieval error: " + e.getMessage());
        }
    }

    private void handleSubscribeChallenge(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            Long challengeSessionId = Long.valueOf(data.get("challengeSessionId").toString());
            
            String sessionId = session.getId();
            Set<Long> subscriptions = sessionSubscriptions.get(sessionId);
            subscriptions.add(challengeSessionId);
            
            log.info("📊 User {} subscribed to challenge session {}", userId, challengeSessionId);
            
            WebSocketMessage<?> subscriptionSuccess = WebSocketMessage.success(
                "CHALLENGE_SUBSCRIPTION_SUCCESS",
                Map.of(
                    "challengeSessionId", challengeSessionId,
                    "message", "챌린지 세션 구독이 완료되었습니다."
                )
            );
            sendMessage(session, subscriptionSuccess);
            
        } catch (Exception e) {
            log.error("❌ Error subscribing to challenge for user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Challenge subscription error: " + e.getMessage());
        }
    }

    private void handleUnsubscribeChallenge(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            Long challengeSessionId = Long.valueOf(data.get("challengeSessionId").toString());
            
            String sessionId = session.getId();
            Set<Long> subscriptions = sessionSubscriptions.get(sessionId);
            subscriptions.remove(challengeSessionId);
            
            log.info("🚫 User {} unsubscribed from challenge session {}", userId, challengeSessionId);
            
            WebSocketMessage<?> unsubscriptionSuccess = WebSocketMessage.success(
                "CHALLENGE_UNSUBSCRIPTION_SUCCESS",
                Map.of(
                    "challengeSessionId", challengeSessionId,
                    "message", "챌린지 세션 구독이 해제되었습니다."
                )
            );
            sendMessage(session, unsubscriptionSuccess);
            
        } catch (Exception e) {
            log.error("❌ Error unsubscribing from challenge for user {}: {}", userId, e.getMessage());
            sendErrorMessage(session, "Challenge unsubscription error: " + e.getMessage());
        }
    }

    private void handleGetPositions(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        // 실제로는 포지션 서비스에서 데이터를 가져와야 함
        WebSocketMessage<?> positionsResponse = WebSocketMessage.success(
            "POSITIONS_DATA",
            Map.of(
                "positions", List.of(), // 실제 포지션 데이터
                "message", "현재 포지션 정보입니다."
            )
        );
        sendMessage(session, positionsResponse);
    }

    private void handleGetOrders(WebSocketSession session, Long userId, WebSocketMessage<?> message) {
        // 실제로는 주문 이력 서비스에서 데이터를 가져와야 함
        WebSocketMessage<?> ordersResponse = WebSocketMessage.success(
            "ORDERS_DATA",
            Map.of(
                "orders", List.of(), // 실제 주문 이력 데이터
                "message", "주문 이력 정보입니다."
            )
        );
        sendMessage(session, ordersResponse);
    }

    private void handlePing(WebSocketSession session) {
        WebSocketMessage<?> pongResponse = WebSocketMessage.success(
            "PONG",
            Map.of("timestamp", System.currentTimeMillis())
        );
        sendMessage(session, pongResponse);
    }

    private void startPortfolioUpdates(WebSocketSession session, Long userId) {
        String sessionId = session.getId();
        
        // 30초마다 포트폴리오 업데이트 전송
        Disposable portfolioUpdates = Flux.interval(Duration.ofSeconds(30))
            .subscribe(tick -> {
                if (session.isOpen()) {
                    triggerPortfolioUpdate(session, userId);
                }
            });
        
        sessionDisposables.get(sessionId).add(portfolioUpdates);
    }

    private void triggerPortfolioUpdate(WebSocketSession session, Long userId) {
        try {
            // 실제로는 사용자의 활성 챌린지 세션들을 조회해야 함
            WebSocketMessage<?> portfolioUpdate = WebSocketMessage.success(
                "PORTFOLIO_UPDATE",
                Map.of(
                    "totalValue", 100000.0 + Math.random() * 10000, // 모의 데이터
                    "dailyChange", -500 + Math.random() * 1000,
                    "dailyChangePercent", -0.5 + Math.random() * 1.0,
                    "timestamp", System.currentTimeMillis()
                )
            );
            sendMessage(session, portfolioUpdate);
            
        } catch (Exception e) {
            log.error("❌ Error sending portfolio update to user {}: {}", userId, e.getMessage());
        }
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        // 실제로는 JWT 토큰이나 세션에서 사용자 ID를 추출해야 함
        // 여기서는 간소화된 버전으로 구현
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                return ((User) auth.getPrincipal()).getId();
            }
        } catch (Exception e) {
            log.debug("No authentication context available for session: {}", session.getId());
        }
        
        // 테스트를 위한 기본값 (실제 프로덕션에서는 제거해야 함)
        return 1L;
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("❌ Failed to send trading message to {}: {}", session.getId(), e.getMessage());
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        WebSocketMessage<?> errorMessage = WebSocketMessage.error(error);
        sendMessage(session, errorMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionUsers.get(sessionId);
        log.error("🚨 Trading WebSocket transport error for user {}: {}", userId, exception.getMessage());
        
        cleanupSession(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionUsers.get(sessionId);
        log.info("🔌 Trading WebSocket connection closed for user {}: {} (Status: {})", 
            userId, sessionId, closeStatus);
        
        cleanupSession(sessionId);
    }

    private void cleanupSession(String sessionId) {
        // 사용자 매핑 정리
        Long userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            userSessions.remove(userId);
        }
        
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
        
        log.debug("🧹 Trading session cleanup completed for user {} (session: {})", userId, sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 외부에서 특정 사용자에게 메시지를 보낼 수 있는 메서드
    public void sendMessageToUser(Long userId, WebSocketMessage<?> message) {
        String sessionId = userSessions.get(userId);
        if (sessionId != null) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }
}