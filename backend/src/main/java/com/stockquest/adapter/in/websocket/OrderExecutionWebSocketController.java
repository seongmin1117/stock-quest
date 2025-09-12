package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.service.OrderManagementService;
import com.stockquest.application.service.TradeExecutionService;
import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 주문 실행 알림 WebSocket 컨트롤러
 * Phase 2.2: WebSocket 실시간 기능 구현 - 주문 실행 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExecutionWebSocketController implements WebSocketHandler {
    
    private final OrderManagementService orderManagementService;
    private final TradeExecutionService tradeExecutionService;
    private final ObjectMapper objectMapper;
    
    // 연결된 세션 관리
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();
    private final Map<String, Set<String>> sessionOrderSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, OrderSubscription> subscriptionDetails = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastOrderUpdate = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        
        // 사용자 ID 추출 (실제 구현에서는 JWT 토큰에서 추출)
        String userId = extractUserId(session);
        sessionUsers.put(session.getId(), userId);
        
        log.info("주문 실행 WebSocket 연결 설정: sessionId={}, userId={}", session.getId(), userId);
        
        // 연결 성공 메시지 전송
        OrderMessage welcomeMessage = OrderMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .timestamp(LocalDateTime.now())
            .data(Map.of(
                "message", "주문 실행 알림 연결이 설정되었습니다",
                "userId", userId,
                "availableActions", Arrays.asList(
                    "SUBSCRIBE_ORDERS", "UNSUBSCRIBE_ORDERS",
                    "REQUEST_ORDER_STATUS", "REQUEST_ORDER_HISTORY",
                    "SUBMIT_ORDER", "CANCEL_ORDER", "MODIFY_ORDER"
                ),
                "notificationTypes", Arrays.asList(
                    "ORDER_SUBMITTED", "ORDER_FILLED", "ORDER_PARTIALLY_FILLED",
                    "ORDER_CANCELLED", "ORDER_REJECTED", "EXECUTION_REPORT"
                )
            ))
            .build();
        
        sendMessage(session, welcomeMessage);
        
        // 사용자의 활성 주문 목록 전송
        sendActiveOrdersList(session, userId);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            OrderMessage request = objectMapper.readValue(payload, OrderMessage.class);
            
            log.debug("주문 실행 WebSocket 메시지 수신: sessionId={}, type={}", 
                session.getId(), request.getType());
            
            switch (request.getType()) {
                case SUBSCRIBE_ORDERS -> handleOrderSubscription(session, request);
                case UNSUBSCRIBE_ORDERS -> handleOrderUnsubscription(session, request);
                case REQUEST_ORDER_STATUS -> handleOrderStatusRequest(session, request);
                case REQUEST_ORDER_HISTORY -> handleOrderHistoryRequest(session, request);
                case SUBMIT_ORDER -> handleOrderSubmission(session, request);
                case CANCEL_ORDER -> handleOrderCancellation(session, request);
                case MODIFY_ORDER -> handleOrderModification(session, request);
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
        log.error("주문 실행 WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        cleanupSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
        log.info("주문 실행 WebSocket 연결 종료: sessionId={}, status={}", 
            session.getId(), closeStatus.toString());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 주문 구독 처리
     */
    private void handleOrderSubscription(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            @SuppressWarnings("unchecked")
            List<String> orderIds = (List<String>) data.get("orderIds");
            String subscriptionType = (String) data.getOrDefault("subscriptionType", "ALL");
            @SuppressWarnings("unchecked")
            List<String> symbols = (List<String>) data.get("symbols"); // 특정 심볼 구독
            
            String userId = sessionUsers.get(session.getId());
            
            // 구독 정보 저장
            OrderSubscription subscription = OrderSubscription.builder()
                .sessionId(session.getId())
                .userId(userId)
                .orderIds(orderIds != null ? new HashSet<>(orderIds) : new HashSet<>())
                .symbols(symbols != null ? new HashSet<>(symbols) : new HashSet<>())
                .subscriptionType(OrderSubscriptionType.valueOf(subscriptionType))
                .subscribedAt(LocalDateTime.now())
                .active(true)
                .build();
            
            subscriptionDetails.put(session.getId(), subscription);
            
            // 세션별 구독 정보 저장
            Set<String> subscriptions = new HashSet<>();
            if (orderIds != null) subscriptions.addAll(orderIds);
            if (symbols != null) subscriptions.addAll(symbols);
            sessionOrderSubscriptions.put(session.getId(), subscriptions);
            
            log.info("주문 구독 추가: sessionId={}, orderIds={}, symbols={}, type={}", 
                session.getId(), orderIds, symbols, subscriptionType);
            
            // 구독 확인 메시지 전송
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderIds", orderIds != null ? orderIds : Collections.emptyList(),
                    "symbols", symbols != null ? symbols : Collections.emptyList(),
                    "subscriptionType", subscriptionType,
                    "message", "주문 알림 구독이 완료되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("주문 구독 처리 실패", e);
            sendErrorMessage(session, "주문 구독 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 상태 요청 처리
     */
    private void handleOrderStatusRequest(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String orderId = (String) data.get("orderId");
            
            if (orderId == null || orderId.trim().isEmpty()) {
                sendErrorMessage(session, "주문 ID가 지정되지 않았습니다");
                return;
            }
            
            String userId = sessionUsers.get(session.getId());
            if (!hasOrderAccess(userId, orderId)) {
                sendErrorMessage(session, "해당 주문에 대한 접근 권한이 없습니다");
                return;
            }
            
            // 주문 상태 조회 (시뮬레이션)
            Map<String, Object> orderStatus = getOrderStatus(orderId);
            
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.ORDER_STATUS)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "status", orderStatus
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("주문 상태 요청 처리 실패", e);
            sendErrorMessage(session, "주문 상태 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 이력 요청 처리
     */
    private void handleOrderHistoryRequest(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            Integer days = (Integer) data.getOrDefault("days", 7);
            
            String userId = sessionUsers.get(session.getId());
            List<Map<String, Object>> orderHistory = getOrderHistory(userId, symbol, days);
            
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.ORDER_HISTORY)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "symbol", symbol != null ? symbol : "ALL",
                    "days", days,
                    "orders", orderHistory
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("주문 이력 요청 처리 실패", e);
            sendErrorMessage(session, "주문 이력 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 제출 처리
     */
    private void handleOrderSubmission(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String symbol = (String) data.get("symbol");
            String orderType = (String) data.get("orderType");
            String side = (String) data.get("side");
            BigDecimal quantity = new BigDecimal(data.get("quantity").toString());
            BigDecimal price = data.get("price") != null ? new BigDecimal(data.get("price").toString()) : null;
            
            String userId = sessionUsers.get(session.getId());
            
            // 주문 제출 (시뮬레이션)
            String orderId = "ORDER_" + System.currentTimeMillis();
            
            log.info("주문 제출: userId={}, symbol={}, type={}, side={}, quantity={}, price={}", 
                userId, symbol, orderType, side, quantity, price);
            
            // 주문 제출 확인 메시지
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.ORDER_SUBMITTED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "symbol", symbol,
                    "orderType", orderType,
                    "side", side,
                    "quantity", quantity.toString(),
                    "price", price != null ? price.toString() : "MARKET",
                    "status", "SUBMITTED",
                    "message", "주문이 성공적으로 제출되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
            // 주문 실행 시뮬레이션
            simulateOrderExecution(orderId, symbol, quantity, price, session);
            
        } catch (Exception e) {
            log.error("주문 제출 처리 실패", e);
            sendErrorMessage(session, "주문 제출 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 취소 처리
     */
    private void handleOrderCancellation(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String orderId = (String) data.get("orderId");
            
            String userId = sessionUsers.get(session.getId());
            if (!hasOrderAccess(userId, orderId)) {
                sendErrorMessage(session, "해당 주문에 대한 접근 권한이 없습니다");
                return;
            }
            
            log.info("주문 취소: userId={}, orderId={}", userId, orderId);
            
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.ORDER_CANCELLED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "status", "CANCELLED",
                    "message", "주문이 성공적으로 취소되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("주문 취소 처리 실패", e);
            sendErrorMessage(session, "주문 취소 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 수정 처리
     */
    private void handleOrderModification(WebSocketSession session, OrderMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String orderId = (String) data.get("orderId");
            BigDecimal newQuantity = data.get("newQuantity") != null ? 
                new BigDecimal(data.get("newQuantity").toString()) : null;
            BigDecimal newPrice = data.get("newPrice") != null ? 
                new BigDecimal(data.get("newPrice").toString()) : null;
            
            String userId = sessionUsers.get(session.getId());
            if (!hasOrderAccess(userId, orderId)) {
                sendErrorMessage(session, "해당 주문에 대한 접근 권한이 없습니다");
                return;
            }
            
            log.info("주문 수정: userId={}, orderId={}, newQuantity={}, newPrice={}", 
                userId, orderId, newQuantity, newPrice);
            
            OrderMessage response = OrderMessage.builder()
                .type(MessageType.ORDER_MODIFIED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "newQuantity", newQuantity != null ? newQuantity.toString() : "UNCHANGED",
                    "newPrice", newPrice != null ? newPrice.toString() : "UNCHANGED",
                    "status", "MODIFIED",
                    "message", "주문이 성공적으로 수정되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("주문 수정 처리 실패", e);
            sendErrorMessage(session, "주문 수정 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 실행 이벤트 처리 (외부에서 호출)
     */
    @EventListener
    @Async
    public void handleOrderExecutionEvent(OrderExecutionEvent event) {
        try {
            Order order = event.getOrder();
            Trade trade = event.getTrade();
            
            OrderMessage notification = OrderMessage.builder()
                .type(getMessageTypeFromEvent(event.getEventType()))
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", order.getOrderId(),
                    "symbol", order.getSymbol(),
                    "executedQuantity", trade != null ? trade.getQuantity().toString() : "0",
                    "executionPrice", trade != null ? trade.getPrice().toString() : "0",
                    "remainingQuantity", order.getRemainingQuantity().toString(),
                    "status", order.getStatus().name(),
                    "timestamp", LocalDateTime.now(),
                    "eventType", event.getEventType()
                ))
                .build();
            
            // 구독자들에게 브로드캐스트
            broadcastOrderNotification(order.getOrderId(), order.getSymbol(), notification);
            
        } catch (Exception e) {
            log.error("주문 실행 이벤트 처리 실패", e);
        }
    }
    
    /**
     * 주문 실행 시뮬레이션
     */
    @Async
    public void simulateOrderExecution(String orderId, String symbol, BigDecimal quantity, BigDecimal price, WebSocketSession session) {
        try {
            // 2초 후 부분 체결 시뮬레이션
            Thread.sleep(2000);
            
            BigDecimal partialQuantity = quantity.multiply(BigDecimal.valueOf(0.5));
            BigDecimal executionPrice = price != null ? price : BigDecimal.valueOf(150.00);
            
            OrderMessage partialFillMessage = OrderMessage.builder()
                .type(MessageType.ORDER_PARTIALLY_FILLED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "symbol", symbol,
                    "executedQuantity", partialQuantity.toString(),
                    "executionPrice", executionPrice.toString(),
                    "remainingQuantity", quantity.subtract(partialQuantity).toString(),
                    "status", "PARTIALLY_FILLED",
                    "fillRatio", "50%"
                ))
                .build();
            
            sendMessage(session, partialFillMessage);
            broadcastOrderNotification(orderId, symbol, partialFillMessage);
            
            // 3초 후 완전 체결 시뮬레이션
            Thread.sleep(3000);
            
            OrderMessage fullFillMessage = OrderMessage.builder()
                .type(MessageType.ORDER_FILLED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "orderId", orderId,
                    "symbol", symbol,
                    "executedQuantity", quantity.toString(),
                    "averageExecutionPrice", executionPrice.toString(),
                    "remainingQuantity", "0",
                    "status", "FILLED",
                    "fillRatio", "100%",
                    "totalExecutionValue", quantity.multiply(executionPrice).toString()
                ))
                .build();
            
            sendMessage(session, fullFillMessage);
            broadcastOrderNotification(orderId, symbol, fullFillMessage);
            
        } catch (Exception e) {
            log.error("주문 실행 시뮬레이션 실패", e);
        }
    }
    
    // ========================= Helper Methods =========================
    
    private void handleOrderUnsubscription(WebSocketSession session, OrderMessage request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        @SuppressWarnings("unchecked")
        List<String> orderIds = (List<String>) data.get("orderIds");
        @SuppressWarnings("unchecked")
        List<String> symbols = (List<String>) data.get("symbols");
        
        Set<String> subscriptions = sessionOrderSubscriptions.get(session.getId());
        if (subscriptions != null) {
            if (orderIds != null) subscriptions.removeAll(orderIds);
            if (symbols != null) subscriptions.removeAll(symbols);
            
            if (subscriptions.isEmpty()) {
                subscriptionDetails.remove(session.getId());
            }
        }
        
        log.info("주문 구독 해제: sessionId={}, orderIds={}, symbols={}", 
            session.getId(), orderIds, symbols);
    }
    
    private void handleHeartbeat(WebSocketSession session, OrderMessage request) {
        OrderMessage heartbeatResponse = OrderMessage.builder()
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
    
    private String extractUserId(WebSocketSession session) {
        // 실제 구현에서는 JWT 토큰에서 사용자 ID 추출
        return "user_" + session.getId().substring(0, 8);
    }
    
    private boolean hasOrderAccess(String userId, String orderId) {
        // 실제 구현에서는 사용자의 주문 접근 권한 확인
        return true; // 시뮬레이션용
    }
    
    private Map<String, Object> getOrderStatus(String orderId) {
        // 시뮬레이션 데이터
        return Map.of(
            "orderId", orderId,
            "symbol", "AAPL",
            "status", "PARTIALLY_FILLED",
            "orderType", "LIMIT",
            "side", "BUY",
            "quantity", "100",
            "executedQuantity", "50",
            "remainingQuantity", "50",
            "averagePrice", "150.25",
            "lastUpdated", LocalDateTime.now()
        );
    }
    
    private List<Map<String, Object>> getOrderHistory(String userId, String symbol, Integer days) {
        // 시뮬레이션 데이터
        return Arrays.asList(
            Map.of(
                "orderId", "ORDER_001",
                "symbol", "AAPL",
                "status", "FILLED",
                "orderType", "MARKET",
                "side", "BUY",
                "quantity", "100",
                "executionPrice", "149.50",
                "executionTime", LocalDateTime.now().minusHours(2)
            ),
            Map.of(
                "orderId", "ORDER_002",
                "symbol", "GOOGL",
                "status", "CANCELLED",
                "orderType", "LIMIT",
                "side", "SELL",
                "quantity", "50",
                "limitPrice", "2800.00",
                "cancelTime", LocalDateTime.now().minusHours(1)
            )
        );
    }
    
    private void sendActiveOrdersList(WebSocketSession session, String userId) {
        try {
            List<Map<String, Object>> activeOrders = Arrays.asList(
                Map.of(
                    "orderId", "ORDER_ACTIVE_001",
                    "symbol", "TSLA",
                    "status", "NEW",
                    "orderType", "LIMIT",
                    "side", "BUY",
                    "quantity", "50",
                    "limitPrice", "200.00",
                    "submittedAt", LocalDateTime.now().minusMinutes(30)
                ),
                Map.of(
                    "orderId", "ORDER_ACTIVE_002",
                    "symbol", "MSFT",
                    "status", "PARTIALLY_FILLED",
                    "orderType", "MARKET",
                    "side", "SELL",
                    "quantity", "75",
                    "executedQuantity", "25",
                    "submittedAt", LocalDateTime.now().minusMinutes(15)
                )
            );
            
            OrderMessage message = OrderMessage.builder()
                .type(MessageType.ACTIVE_ORDERS)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "userId", userId,
                    "activeOrders", activeOrders,
                    "totalActiveOrders", activeOrders.size()
                ))
                .build();
            
            sendMessage(session, message);
            
        } catch (IOException e) {
            log.error("활성 주문 목록 전송 실패", e);
        }
    }
    
    private void broadcastOrderNotification(String orderId, String symbol, OrderMessage message) {
        activeSessions.parallelStream()
            .filter(session -> {
                OrderSubscription subscription = subscriptionDetails.get(session.getId());
                if (subscription == null || !subscription.isActive()) {
                    return false;
                }
                
                // 주문 ID 또는 심볼 기반 구독 확인
                return subscription.getOrderIds().contains(orderId) || 
                       subscription.getSymbols().contains(symbol) ||
                       subscription.getSubscriptionType() == OrderSubscriptionType.ALL;
            })
            .forEach(session -> {
                try {
                    if (session.isOpen()) {
                        sendMessage(session, message);
                    }
                } catch (IOException e) {
                    log.error("주문 알림 브로드캐스트 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    private MessageType getMessageTypeFromEvent(String eventType) {
        return switch (eventType) {
            case "PARTIAL_FILL" -> MessageType.ORDER_PARTIALLY_FILLED;
            case "FULL_FILL" -> MessageType.ORDER_FILLED;
            case "CANCELLED" -> MessageType.ORDER_CANCELLED;
            case "REJECTED" -> MessageType.ORDER_REJECTED;
            default -> MessageType.EXECUTION_REPORT;
        };
    }
    
    private void cleanupSession(WebSocketSession session) {
        activeSessions.remove(session);
        sessionOrderSubscriptions.remove(session.getId());
        subscriptionDetails.remove(session.getId());
        sessionUsers.remove(session.getId());
    }
    
    private void sendMessage(WebSocketSession session, OrderMessage message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }
    
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            OrderMessage message = OrderMessage.builder()
                .type(MessageType.ERROR)
                .timestamp(LocalDateTime.now())
                .data(Map.of("error", errorMessage))
                .build();
            
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패", e);
        }
    }
    
    // ========================= DTOs and Enums =========================
    
    public enum MessageType {
        CONNECTION_ESTABLISHED,
        ACTIVE_ORDERS,
        SUBSCRIBE_ORDERS,
        UNSUBSCRIBE_ORDERS,
        SUBSCRIPTION_CONFIRMED,
        REQUEST_ORDER_STATUS,
        REQUEST_ORDER_HISTORY,
        SUBMIT_ORDER,
        CANCEL_ORDER,
        MODIFY_ORDER,
        ORDER_STATUS,
        ORDER_HISTORY,
        ORDER_SUBMITTED,
        ORDER_FILLED,
        ORDER_PARTIALLY_FILLED,
        ORDER_CANCELLED,
        ORDER_REJECTED,
        ORDER_MODIFIED,
        EXECUTION_REPORT,
        HEARTBEAT,
        ERROR
    }
    
    public enum OrderSubscriptionType {
        ALL,
        ORDER_IDS,
        SYMBOLS,
        STATUS_UPDATES_ONLY,
        EXECUTIONS_ONLY
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderMessage {
        private MessageType type;
        private LocalDateTime timestamp;
        private Object data;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderSubscription {
        private String sessionId;
        private String userId;
        private Set<String> orderIds;
        private Set<String> symbols;
        private OrderSubscriptionType subscriptionType;
        private LocalDateTime subscribedAt;
        private boolean active;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderExecutionEvent {
        private String eventType;
        private Order order;
        private Trade trade;
        private LocalDateTime timestamp;
    }
}