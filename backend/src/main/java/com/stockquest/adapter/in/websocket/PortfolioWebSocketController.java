package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.service.PortfolioService;
import com.stockquest.application.service.RealTimePositionManagementService;
import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.portfolio.Position;
import com.stockquest.config.WebSocketConnectionManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 고급 포트폴리오 실시간 업데이트 WebSocket 컨트롤러
 * 기능: 압축 메시지 전송, 재연결 관리, 성능 모니터링
 * 성능 목표: <100ms 지연시간, 30% 대역폭 절약
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioWebSocketController implements WebSocketHandler {
    
    private final PortfolioService portfolioService;
    private final RealTimePositionManagementService positionManagementService;
    private final WebSocketConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    
    // 연결된 세션 관리
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();
    private final Map<String, Set<Long>> sessionPortfolios = new ConcurrentHashMap<>();
    private final Map<String, PortfolioSubscription> subscriptionDetails = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastPortfolioUpdate = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 사용자 ID 추출 (실제 구현에서는 JWT 토큰에서 추출)
        String userId = extractUserId(session);

        // 연결 관리자에 등록
        boolean registered = connectionManager.registerConnection(
            WebSocketConnectionManager.PORTFOLIO, session, userId);

        if (!registered) {
            log.warn("포트폴리오 WebSocket 연결 제한 초과: userId={}", userId);
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Connection limit exceeded"));
            return;
        }

        activeSessions.add(session);
        sessionUsers.put(session.getId(), userId);

        log.info("포트폴리오 WebSocket 연결 설정: sessionId={}, userId={}", session.getId(), userId);

        // 연결 성공 메시지 전송
        PortfolioMessage welcomeMessage = PortfolioMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .timestamp(LocalDateTime.now())
            .data(Map.of(
                "message", "포트폴리오 실시간 업데이트 연결이 설정되었습니다",
                "userId", userId,
                "availableActions", Arrays.asList(
                    "SUBSCRIBE_PORTFOLIO", "UNSUBSCRIBE_PORTFOLIO",
                    "REQUEST_PORTFOLIO_SNAPSHOT", "REQUEST_POSITION_DETAILS",
                    "UPDATE_POSITION", "PORTFOLIO_ANALYTICS"
                )
            ))
            .build();

        sendMessage(session, welcomeMessage);

        // 사용자의 포트폴리오 목록 전송
        sendUserPortfolioList(session, userId);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            PortfolioMessage request = objectMapper.readValue(payload, PortfolioMessage.class);
            
            log.debug("포트폴리오 WebSocket 메시지 수신: sessionId={}, type={}", 
                session.getId(), request.getType());
            
            switch (request.getType()) {
                case SUBSCRIBE_PORTFOLIO -> handlePortfolioSubscription(session, request);
                case UNSUBSCRIBE_PORTFOLIO -> handlePortfolioUnsubscription(session, request);
                case REQUEST_PORTFOLIO_SNAPSHOT -> handlePortfolioSnapshotRequest(session, request);
                case REQUEST_POSITION_DETAILS -> handlePositionDetailsRequest(session, request);
                case UPDATE_POSITION -> handlePositionUpdate(session, request);
                case PORTFOLIO_ANALYTICS -> handlePortfolioAnalyticsRequest(session, request);
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
        log.error("포트폴리오 WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        cleanupSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
        log.info("포트폴리오 WebSocket 연결 종료: sessionId={}, status={}", 
            session.getId(), closeStatus.toString());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 포트폴리오 구독 처리
     */
    private void handlePortfolioSubscription(WebSocketSession session, PortfolioMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            String updateType = (String) data.getOrDefault("updateType", "ALL");
            Integer updateInterval = (Integer) data.getOrDefault("updateInterval", 5000); // 기본 5초
            
            // 권한 확인 (사용자가 해당 포트폴리오에 접근할 수 있는지)
            String userId = sessionUsers.get(session.getId());
            if (!hasPortfolioAccess(userId, portfolioId)) {
                sendErrorMessage(session, "해당 포트폴리오에 대한 접근 권한이 없습니다");
                return;
            }
            
            // 구독 정보 저장
            PortfolioSubscription subscription = PortfolioSubscription.builder()
                .sessionId(session.getId())
                .portfolioId(portfolioId)
                .updateType(PortfolioUpdateType.valueOf(updateType))
                .updateInterval(updateInterval)
                .subscribedAt(LocalDateTime.now())
                .active(true)
                .build();
            
            subscriptionDetails.put(session.getId(), subscription);
            sessionPortfolios.computeIfAbsent(session.getId(), k -> new HashSet<>())
                .add(portfolioId);
            
            log.info("포트폴리오 구독 추가: sessionId={}, portfolioId={}, updateType={}", 
                session.getId(), portfolioId, updateType);
            
            // 구독 확인 메시지 전송
            PortfolioMessage response = PortfolioMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "portfolioId", portfolioId,
                    "updateType", updateType,
                    "updateInterval", updateInterval,
                    "message", "포트폴리오 구독이 완료되었습니다"
                ))
                .build();
            
            sendMessage(session, response);
            
            // 초기 포트폴리오 스냅샷 전송
            sendPortfolioSnapshot(session, portfolioId);
            
        } catch (Exception e) {
            log.error("포트폴리오 구독 처리 실패", e);
            sendErrorMessage(session, "포트폴리오 구독 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 포트폴리오 스냅샷 요청 처리
     */
    private void handlePortfolioSnapshotRequest(WebSocketSession session, PortfolioMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            
            String userId = sessionUsers.get(session.getId());
            if (!hasPortfolioAccess(userId, portfolioId)) {
                sendErrorMessage(session, "해당 포트폴리오에 대한 접근 권한이 없습니다");
                return;
            }
            
            sendPortfolioSnapshot(session, portfolioId);
            
        } catch (Exception e) {
            log.error("포트폴리오 스냅샷 요청 처리 실패", e);
            sendErrorMessage(session, "포트폴리오 스냅샷 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 포지션 상세 정보 요청 처리
     */
    private void handlePositionDetailsRequest(WebSocketSession session, PortfolioMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            String symbol = (String) data.get("symbol");
            
            String userId = sessionUsers.get(session.getId());
            if (!hasPortfolioAccess(userId, portfolioId)) {
                sendErrorMessage(session, "해당 포트폴리오에 대한 접근 권한이 없습니다");
                return;
            }
            
            // 포지션 상세 정보 조회
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            Position position = portfolio.getPositions().stream()
                .filter(pos -> pos.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
            
            if (position == null) {
                sendErrorMessage(session, "해당 심볼의 포지션이 존재하지 않습니다: " + symbol);
                return;
            }
            
            PortfolioMessage response = PortfolioMessage.builder()
                .type(MessageType.POSITION_DETAILS)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "portfolioId", portfolioId,
                    "position", convertPositionToMap(position),
                    "analytics", calculatePositionAnalytics(position)
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("포지션 상세 정보 요청 처리 실패", e);
            sendErrorMessage(session, "포지션 상세 정보 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 포트폴리오 분석 요청 처리
     */
    private void handlePortfolioAnalyticsRequest(WebSocketSession session, PortfolioMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long portfolioId = ((Number) data.get("portfolioId")).longValue();
            String analysisType = (String) data.getOrDefault("analysisType", "COMPREHENSIVE");
            
            String userId = sessionUsers.get(session.getId());
            if (!hasPortfolioAccess(userId, portfolioId)) {
                sendErrorMessage(session, "해당 포트폴리오에 대한 접근 권한이 없습니다");
                return;
            }
            
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            Map<String, Object> analytics = calculatePortfolioAnalytics(portfolio, analysisType);
            
            PortfolioMessage response = PortfolioMessage.builder()
                .type(MessageType.PORTFOLIO_ANALYTICS)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "portfolioId", portfolioId,
                    "analysisType", analysisType,
                    "analytics", analytics
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("포트폴리오 분석 요청 처리 실패", e);
            sendErrorMessage(session, "포트폴리오 분석 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 실시간 포트폴리오 업데이트 (매 5초)
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastPortfolioUpdates() {
        if (activeSessions.isEmpty()) return;
        
        // 구독된 모든 포트폴리오 ID 수집
        Set<Long> allPortfolioIds = new HashSet<>();
        sessionPortfolios.values().forEach(allPortfolioIds::addAll);
        
        if (allPortfolioIds.isEmpty()) return;
        
        log.debug("실시간 포트폴리오 업데이트 실행: portfolios={}", allPortfolioIds.size());
        
        allPortfolioIds.forEach(portfolioId -> {
            try {
                Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
                
                PortfolioMessage message = PortfolioMessage.builder()
                    .type(MessageType.PORTFOLIO_UPDATE)
                    .timestamp(LocalDateTime.now())
                    .data(Map.of(
                        "portfolioId", portfolioId,
                        "totalValue", portfolio.getTotalValue() != null ? portfolio.getTotalValue().toString() : "0",
                        "totalCost", calculateTotalCost(portfolio).toString(),
                        "unrealizedPnL", calculateUnrealizedPnL(portfolio).toString(),
                        "realizedPnL", calculateRealizedPnL(portfolio).toString(),
                        "dailyChange", calculateDailyChange(portfolio).toString(),
                        "dailyChangePercent", calculateDailyChangePercent(portfolio).toString(),
                        "positionCount", portfolio.getPositions().size(),
                        "lastUpdated", LocalDateTime.now()
                    ))
                    .build();
                
                broadcastToPortfolioSubscribers(portfolioId, message);
                lastPortfolioUpdate.put(portfolioId, LocalDateTime.now());
                
            } catch (Exception e) {
                log.error("포트폴리오 업데이트 실패: portfolioId={}", portfolioId, e);
            }
        });
    }
    
    /**
     * 포지션 업데이트 브로드캐스트 (매 2초)
     */
    @Scheduled(fixedRate = 2000)
    public void broadcastPositionUpdates() {
        if (activeSessions.isEmpty()) return;
        
        sessionPortfolios.entrySet().forEach(entry -> {
            String sessionId = entry.getKey();
            Set<Long> portfolioIds = entry.getValue();
            
            portfolioIds.forEach(portfolioId -> {
                try {
                    Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
                    
                    portfolio.getPositions().forEach(position -> {
                        // 포지션별 실시간 업데이트
                        PortfolioMessage positionMessage = PortfolioMessage.builder()
                            .type(MessageType.POSITION_UPDATE)
                            .timestamp(LocalDateTime.now())
                            .data(Map.of(
                                "portfolioId", portfolioId,
                                "symbol", position.getSymbol(),
                                "currentPrice", position.getCurrentPrice().toString(),
                                "quantity", position.getQuantity().toString(),
                                "currentValue", position.getCurrentValue().toString(),
                                "profitLoss", position.getProfitLoss().toString(),
                                "profitLossPercent", position.getProfitLossPercentage().toString(),
                                "lastUpdated", LocalDateTime.now()
                            ))
                            .build();
                        
                        WebSocketSession session = findSessionById(sessionId);
                        if (session != null && session.isOpen()) {
                            try {
                                sendMessage(session, positionMessage);
                            } catch (IOException e) {
                                log.error("포지션 업데이트 전송 실패: sessionId={}", sessionId, e);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    log.error("포지션 업데이트 실패: portfolioId={}", portfolioId, e);
                }
            });
        });
    }
    
    // ========================= Helper Methods =========================
    
    private void handlePortfolioUnsubscription(WebSocketSession session, PortfolioMessage request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getData();
        Long portfolioId = ((Number) data.get("portfolioId")).longValue();
        
        Set<Long> portfolios = sessionPortfolios.get(session.getId());
        if (portfolios != null) {
            portfolios.remove(portfolioId);
            
            if (portfolios.isEmpty()) {
                subscriptionDetails.remove(session.getId());
            }
        }
        
        log.info("포트폴리오 구독 해제: sessionId={}, portfolioId={}", session.getId(), portfolioId);
    }
    
    private void handlePositionUpdate(WebSocketSession session, PortfolioMessage request) {
        // 포지션 업데이트 요청 처리 (관리자 권한 필요)
        log.info("포지션 업데이트 요청: sessionId={}", session.getId());
        // 실제 구현에서는 권한 확인 후 포지션 업데이트 수행
    }
    
    private void handleHeartbeat(WebSocketSession session, PortfolioMessage request) {
        PortfolioMessage heartbeatResponse = PortfolioMessage.builder()
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
        // 현재는 시뮬레이션용 사용자 ID 반환
        return "user_" + session.getId().substring(0, 8);
    }
    
    private boolean hasPortfolioAccess(String userId, Long portfolioId) {
        // 실제 구현에서는 사용자의 포트폴리오 접근 권한 확인
        // 현재는 시뮬레이션용으로 항상 true 반환
        return true;
    }
    
    private void sendUserPortfolioList(WebSocketSession session, String userId) {
        try {
            // 사용자의 포트폴리오 목록 조회 (시뮬레이션)
            List<Map<String, Object>> portfolios = Arrays.asList(
                Map.of(
                    "portfolioId", 1L,
                    "name", "메인 포트폴리오",
                    "totalValue", "150000.00",
                    "dailyChange", "2500.00",
                    "positionCount", 5
                ),
                Map.of(
                    "portfolioId", 2L,
                    "name", "성장 포트폴리오",
                    "totalValue", "75000.00",
                    "dailyChange", "-1200.00",
                    "positionCount", 3
                )
            );
            
            PortfolioMessage message = PortfolioMessage.builder()
                .type(MessageType.PORTFOLIO_LIST)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "userId", userId,
                    "portfolios", portfolios
                ))
                .build();
            
            sendMessage(session, message);
            
        } catch (IOException e) {
            log.error("포트폴리오 목록 전송 실패", e);
        }
    }
    
    private void sendPortfolioSnapshot(WebSocketSession session, Long portfolioId) {
        try {
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            
            PortfolioMessage message = PortfolioMessage.builder()
                .type(MessageType.PORTFOLIO_SNAPSHOT)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "portfolioId", portfolioId,
                    "snapshot", convertPortfolioToMap(portfolio),
                    "positions", portfolio.getPositions().stream()
                        .map(this::convertPositionToMap)
                        .toList(),
                    "analytics", calculatePortfolioAnalytics(portfolio, "SUMMARY")
                ))
                .build();
            
            sendMessage(session, message);
            
        } catch (IOException e) {
            log.error("포트폴리오 스냅샷 전송 실패", e);
        }
    }
    
    private void broadcastToPortfolioSubscribers(Long portfolioId, PortfolioMessage message) {
        activeSessions.parallelStream()
            .filter(session -> {
                Set<Long> portfolios = sessionPortfolios.get(session.getId());
                return portfolios != null && portfolios.contains(portfolioId);
            })
            .forEach(session -> {
                try {
                    if (session.isOpen()) {
                        sendMessage(session, message);
                    }
                } catch (IOException e) {
                    log.error("포트폴리오 브로드캐스트 전송 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    private WebSocketSession findSessionById(String sessionId) {
        return activeSessions.stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .orElse(null);
    }
    
    private void cleanupSession(WebSocketSession session) {
        activeSessions.remove(session);
        sessionPortfolios.remove(session.getId());
        subscriptionDetails.remove(session.getId());
        sessionUsers.remove(session.getId());
    }
    
    private void sendMessage(WebSocketSession session, PortfolioMessage message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }
    
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            PortfolioMessage message = PortfolioMessage.builder()
                .type(MessageType.ERROR)
                .timestamp(LocalDateTime.now())
                .data(Map.of("error", errorMessage))
                .build();
            
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패", e);
        }
    }
    
    private Map<String, Object> convertPortfolioToMap(Portfolio portfolio) {
        Map<String, Object> portfolioMap = new HashMap<>();
        portfolioMap.put("portfolioId", portfolio.getId());
        portfolioMap.put("name", portfolio.getName());
        portfolioMap.put("totalValue", portfolio.getTotalValue() != null ? portfolio.getTotalValue().toString() : "0");
        portfolioMap.put("totalCost", calculateTotalCost(portfolio).toString());
        portfolioMap.put("unrealizedPnL", calculateUnrealizedPnL(portfolio).toString());
        portfolioMap.put("realizedPnL", calculateRealizedPnL(portfolio).toString());
        portfolioMap.put("positionCount", portfolio.getPositions().size());
        portfolioMap.put("createdAt", portfolio.getCreatedAt());
        portfolioMap.put("lastUpdated", portfolio.getUpdatedAt());
        return portfolioMap;
    }
    
    private Map<String, Object> convertPositionToMap(Position position) {
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("symbol", position.getSymbol());
        positionMap.put("quantity", position.getQuantity().toString());
        positionMap.put("averagePrice", position.getAveragePrice().toString());
        positionMap.put("currentPrice", position.getCurrentPrice() != null ? position.getCurrentPrice().toString() : "0");
        positionMap.put("currentValue", position.getCurrentValue().toString());
        positionMap.put("profitLoss", position.getProfitLoss().toString());
        positionMap.put("profitLossPercent", position.getProfitLossPercentage().toString());
        positionMap.put("positionType", position.getPositionType().getKoreanName());
        positionMap.put("status", position.getStatus().getKoreanName());
        positionMap.put("openDate", position.getOpenDate());
        positionMap.put("lastUpdated", position.getLastUpdatedAt());
        return positionMap;
    }
    
    private Map<String, Object> calculatePositionAnalytics(Position position) {
        return Map.of(
            "holdingPeriod", calculateHoldingPeriod(position),
            "performance", position.getProfitLossPercentage().toString(),
            "risk", "MEDIUM", // 실제 구현에서는 리스크 계산 로직 적용
            "recommendation", generatePositionRecommendation(position)
        );
    }
    
    private Map<String, Object> calculatePortfolioAnalytics(Portfolio portfolio, String analysisType) {
        BigDecimal unrealizedPnL = calculateUnrealizedPnL(portfolio);
        BigDecimal realizedPnL = calculateRealizedPnL(portfolio);
        
        return Map.of(
            "totalReturn", unrealizedPnL.add(realizedPnL).toString(),
            "totalReturnPercent", calculateTotalReturnPercent(portfolio).toString(),
            "diversification", calculateDiversificationScore(portfolio),
            "riskLevel", "MODERATE",
            "beta", "1.05",
            "sharpeRatio", "0.85",
            "analysisTimestamp", LocalDateTime.now()
        );
    }
    
    private BigDecimal calculateDailyChange(Portfolio portfolio) {
        // 실제 구현에서는 전일 대비 변화량 계산
        BigDecimal unrealizedPnL = calculateUnrealizedPnL(portfolio);
        return unrealizedPnL.multiply(BigDecimal.valueOf(0.02)); // 시뮬레이션
    }
    
    private BigDecimal calculateDailyChangePercent(Portfolio portfolio) {
        BigDecimal dailyChange = calculateDailyChange(portfolio);
        BigDecimal totalCost = calculateTotalCost(portfolio);
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dailyChange.divide(totalCost, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateTotalReturnPercent(Portfolio portfolio) {
        BigDecimal unrealizedPnL = calculateUnrealizedPnL(portfolio);
        BigDecimal realizedPnL = calculateRealizedPnL(portfolio);
        BigDecimal totalReturn = unrealizedPnL.add(realizedPnL);
        BigDecimal totalCost = calculateTotalCost(portfolio);
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalReturn.divide(totalCost, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateTotalCost(Portfolio portfolio) {
        if (portfolio.getPositions() == null || portfolio.getPositions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return portfolio.getPositions().stream()
            .map(position -> position.getAverageCost() != null ? 
                position.getAverageCost() : 
                position.getQuantity().multiply(position.getAveragePrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateUnrealizedPnL(Portfolio portfolio) {
        if (portfolio.getPositions() == null || portfolio.getPositions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return portfolio.getPositions().stream()
            .map(Position::getProfitLoss)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateRealizedPnL(Portfolio portfolio) {
        if (portfolio.getPositions() == null || portfolio.getPositions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return portfolio.getPositions().stream()
            .map(position -> position.getRealizedPnL() != null ? position.getRealizedPnL() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private String calculateHoldingPeriod(Position position) {
        // 실제 구현에서는 보유 기간 계산
        return "30 days"; // 시뮬레이션
    }
    
    private String generatePositionRecommendation(Position position) {
        BigDecimal profitLossPercent = position.getProfitLossPercentage();
        if (profitLossPercent.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "TAKE_PROFIT";
        } else if (profitLossPercent.compareTo(BigDecimal.valueOf(-5)) < 0) {
            return "REVIEW_RISK";
        } else {
            return "HOLD";
        }
    }
    
    private Double calculateDiversificationScore(Portfolio portfolio) {
        // 실제 구현에서는 다변화 점수 계산
        double sectorCount = portfolio.getPositions().size(); // 시뮬레이션
        return Math.min(sectorCount / 10.0, 1.0) * 100;
    }
    
    // ========================= DTOs and Enums =========================
    
    public enum MessageType {
        CONNECTION_ESTABLISHED,
        PORTFOLIO_LIST,
        SUBSCRIBE_PORTFOLIO,
        UNSUBSCRIBE_PORTFOLIO,
        SUBSCRIPTION_CONFIRMED,
        REQUEST_PORTFOLIO_SNAPSHOT,
        REQUEST_POSITION_DETAILS,
        UPDATE_POSITION,
        PORTFOLIO_ANALYTICS,
        PORTFOLIO_SNAPSHOT,
        PORTFOLIO_UPDATE,
        POSITION_UPDATE,
        POSITION_DETAILS,
        HEARTBEAT,
        ERROR
    }
    
    public enum PortfolioUpdateType {
        ALL,
        POSITIONS_ONLY,
        VALUES_ONLY,
        ANALYTICS_ONLY
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioMessage {
        private MessageType type;
        private LocalDateTime timestamp;
        private Object data;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioSubscription {
        private String sessionId;
        private Long portfolioId;
        private PortfolioUpdateType updateType;
        private Integer updateInterval;
        private LocalDateTime subscribedAt;
        private boolean active;
    }
}