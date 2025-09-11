package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.adapter.in.websocket.dto.RiskDashboardMessage;
import com.stockquest.application.service.RealTimeRiskAssessmentService;
import com.stockquest.domain.analytics.risk.MonteCarloSimulation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 실시간 리스크 모니터링 WebSocket 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RiskMonitoringWebSocketController extends TextWebSocketHandler {
    
    private final RealTimeRiskAssessmentService riskAssessmentService;
    private final ObjectMapper objectMapper;
    
    // 연결된 세션들 관리
    private final CopyOnWriteArrayList<WebSocketSession> activeSessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, WebSocketSessionInfo> sessionInfo = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        
        String sessionId = session.getId();
        String userId = extractUserId(session);
        
        WebSocketSessionInfo info = WebSocketSessionInfo.builder()
            .sessionId(sessionId)
            .userId(userId)
            .connectedAt(LocalDateTime.now())
            .subscriptions(new CopyOnWriteArrayList<>())
            .build();
            
        sessionInfo.put(sessionId, info);
        
        log.info("리스크 모니터링 WebSocket 연결 수립: sessionId={}, userId={}, 총 연결 수={}", 
            sessionId, userId, activeSessions.size());
        
        // 연결 시 환영 메시지 및 현재 상태 전송
        sendWelcomeMessage(session);
        sendCurrentRiskStatus(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("리스크 모니터링 메시지 수신: sessionId={}, message={}", session.getId(), payload);
        
        try {
            RiskDashboardMessage dashboardMessage = objectMapper.readValue(payload, RiskDashboardMessage.class);
            handleDashboardMessage(session, dashboardMessage);
        } catch (Exception e) {
            log.error("리스크 모니터링 메시지 처리 오류: sessionId={}, message={}", session.getId(), payload, e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeSessions.remove(session);
        sessionInfo.remove(session.getId());
        
        log.info("리스크 모니터링 WebSocket 연결 종료: sessionId={}, status={}, 남은 연결 수={}", 
            session.getId(), status, activeSessions.size());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("리스크 모니터링 WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        
        if (session.isOpen()) {
            sendErrorMessage(session, "연결 오류가 발생했습니다. 연결을 확인해주세요.");
        }
        
        // 세션 정리
        activeSessions.remove(session);
        sessionInfo.remove(session.getId());
    }
    
    /**
     * 대시보드 메시지 처리
     */
    private void handleDashboardMessage(WebSocketSession session, RiskDashboardMessage message) {
        String sessionId = session.getId();
        WebSocketSessionInfo info = sessionInfo.get(sessionId);
        
        if (info == null) {
            log.warn("세션 정보를 찾을 수 없습니다: sessionId={}", sessionId);
            return;
        }
        
        switch (message.getType()) {
            case "SUBSCRIBE_PORTFOLIO" -> handlePortfolioSubscription(session, message, info);
            case "UNSUBSCRIBE_PORTFOLIO" -> handlePortfolioUnsubscription(session, message, info);
            case "REQUEST_RISK_ASSESSMENT" -> handleRiskAssessmentRequest(session, message);
            case "GET_SIMULATION_STATUS" -> handleSimulationStatusRequest(session, message);
            case "HEARTBEAT" -> handleHeartbeat(session);
            default -> {
                log.warn("알 수 없는 메시지 타입: sessionId={}, type={}", sessionId, message.getType());
                sendErrorMessage(session, "지원하지 않는 메시지 타입입니다: " + message.getType());
            }
        }
    }
    
    /**
     * 포트폴리오 구독 처리
     */
    private void handlePortfolioSubscription(WebSocketSession session, RiskDashboardMessage message, WebSocketSessionInfo info) {
        try {
            Long portfolioId = Long.valueOf(message.getData().toString());
            
            if (!info.getSubscriptions().contains(portfolioId)) {
                info.getSubscriptions().add(portfolioId);
                log.info("포트폴리오 구독 추가: sessionId={}, portfolioId={}", session.getId(), portfolioId);
                
                // 구독 성공 메시지 전송
                sendMessage(session, RiskDashboardMessage.builder()
                    .type("SUBSCRIPTION_CONFIRMED")
                    .portfolioId(portfolioId)
                    .timestamp(LocalDateTime.now())
                    .data("포트폴리오 " + portfolioId + " 실시간 모니터링을 시작합니다.")
                    .build());
                
                // 해당 포트폴리오의 최신 리스크 정보 전송
                sendPortfolioRiskUpdate(session, portfolioId);
            }
        } catch (Exception e) {
            log.error("포트폴리오 구독 처리 오류: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "포트폴리오 구독에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 포트폴리오 구독 해제 처리
     */
    private void handlePortfolioUnsubscription(WebSocketSession session, RiskDashboardMessage message, WebSocketSessionInfo info) {
        try {
            Long portfolioId = Long.valueOf(message.getData().toString());
            
            if (info.getSubscriptions().remove(portfolioId)) {
                log.info("포트폴리오 구독 해제: sessionId={}, portfolioId={}", session.getId(), portfolioId);
                
                sendMessage(session, RiskDashboardMessage.builder()
                    .type("SUBSCRIPTION_CANCELLED")
                    .portfolioId(portfolioId)
                    .timestamp(LocalDateTime.now())
                    .data("포트폴리오 " + portfolioId + " 모니터링을 중단합니다.")
                    .build());
            }
        } catch (Exception e) {
            log.error("포트폴리오 구독 해제 처리 오류: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "포트폴리오 구독 해제에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 리스크 평가 요청 처리
     */
    @Async("websocketTaskExecutor")
    private void handleRiskAssessmentRequest(WebSocketSession session, RiskDashboardMessage message) {
        try {
            Long portfolioId = message.getPortfolioId();
            String scenarioId = message.getData().toString();
            
            log.info("리스크 평가 요청 처리: sessionId={}, portfolioId={}, scenarioId={}", 
                session.getId(), portfolioId, scenarioId);
            
            // 비동기 리스크 평가 실행
            riskAssessmentService.assessPortfolioRisk(portfolioId, scenarioId)
                .thenAccept(result -> {
                    sendMessage(session, RiskDashboardMessage.builder()
                        .type("RISK_ASSESSMENT_COMPLETED")
                        .portfolioId(portfolioId)
                        .timestamp(LocalDateTime.now())
                        .data(result)
                        .build());
                })
                .exceptionally(throwable -> {
                    log.error("리스크 평가 실행 오류: portfolioId={}", portfolioId, throwable);
                    sendErrorMessage(session, "리스크 평가 중 오류가 발생했습니다: " + throwable.getMessage());
                    return null;
                });
                
            // 시작 확인 메시지 전송
            sendMessage(session, RiskDashboardMessage.builder()
                .type("RISK_ASSESSMENT_STARTED")
                .portfolioId(portfolioId)
                .timestamp(LocalDateTime.now())
                .data("리스크 평가를 시작합니다. 완료되면 결과를 전송합니다.")
                .build());
                
        } catch (Exception e) {
            log.error("리스크 평가 요청 처리 오류: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "리스크 평가 요청 처리에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 시뮬레이션 상태 요청 처리
     */
    private void handleSimulationStatusRequest(WebSocketSession session, RiskDashboardMessage message) {
        try {
            List<MonteCarloSimulation> runningSimulations = riskAssessmentService.getRunningSimulations();
            
            sendMessage(session, RiskDashboardMessage.builder()
                .type("SIMULATION_STATUS")
                .timestamp(LocalDateTime.now())
                .data(runningSimulations)
                .build());
                
        } catch (Exception e) {
            log.error("시뮬레이션 상태 조회 오류: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "시뮬레이션 상태 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 하트비트 처리
     */
    private void handleHeartbeat(WebSocketSession session) {
        try {
            sendMessage(session, RiskDashboardMessage.builder()
                .type("HEARTBEAT_ACK")
                .timestamp(LocalDateTime.now())
                .data("pong")
                .build());
        } catch (Exception e) {
            log.error("하트비트 응답 오류: sessionId={}", session.getId(), e);
        }
    }
    
    /**
     * 환영 메시지 전송
     */
    private void sendWelcomeMessage(WebSocketSession session) {
        sendMessage(session, RiskDashboardMessage.builder()
            .type("CONNECTION_ESTABLISHED")
            .timestamp(LocalDateTime.now())
            .data("실시간 리스크 모니터링 대시보드에 연결되었습니다.")
            .build());
    }
    
    /**
     * 현재 리스크 상태 전송
     */
    private void sendCurrentRiskStatus(WebSocketSession session) {
        try {
            List<MonteCarloSimulation> runningSimulations = riskAssessmentService.getRunningSimulations();
            
            sendMessage(session, RiskDashboardMessage.builder()
                .type("CURRENT_STATUS")
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "runningSimulations", runningSimulations.size(),
                    "activeConnections", activeSessions.size()
                ))
                .build());
        } catch (Exception e) {
            log.error("현재 상태 전송 오류: sessionId={}", session.getId(), e);
        }
    }
    
    /**
     * 포트폴리오 리스크 업데이트 전송
     */
    private void sendPortfolioRiskUpdate(WebSocketSession session, Long portfolioId) {
        try {
            List<MonteCarloSimulation> recentAssessments = riskAssessmentService.getRecentRiskAssessments(portfolioId);
            
            if (!recentAssessments.isEmpty()) {
                MonteCarloSimulation latest = recentAssessments.get(0);
                
                sendMessage(session, RiskDashboardMessage.builder()
                    .type("PORTFOLIO_RISK_UPDATE")
                    .portfolioId(portfolioId)
                    .timestamp(LocalDateTime.now())
                    .data(latest)
                    .build());
            }
        } catch (Exception e) {
            log.error("포트폴리오 리스크 업데이트 전송 오류: sessionId={}, portfolioId={}", session.getId(), portfolioId, e);
        }
    }
    
    /**
     * 오류 메시지 전송
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        sendMessage(session, RiskDashboardMessage.builder()
            .type("ERROR")
            .timestamp(LocalDateTime.now())
            .data(Map.of("error", errorMessage))
            .build());
    }
    
    /**
     * 메시지 전송
     */
    private void sendMessage(WebSocketSession session, RiskDashboardMessage message) {
        if (session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                log.error("메시지 전송 오류: sessionId={}", session.getId(), e);
            }
        }
    }
    
    /**
     * 모든 활성 세션에 브로드캐스트
     */
    public void broadcastToAll(RiskDashboardMessage message) {
        activeSessions.forEach(session -> sendMessage(session, message));
    }
    
    /**
     * 특정 포트폴리오 구독자들에게 브로드캐스트
     */
    public void broadcastToPortfolioSubscribers(Long portfolioId, RiskDashboardMessage message) {
        sessionInfo.values().stream()
            .filter(info -> info.getSubscriptions().contains(portfolioId))
            .forEach(info -> {
                WebSocketSession session = activeSessions.stream()
                    .filter(s -> s.getId().equals(info.getSessionId()))
                    .findFirst()
                    .orElse(null);
                    
                if (session != null) {
                    sendMessage(session, message);
                }
            });
    }
    
    /**
     * 주기적으로 리스크 상태 업데이트 브로드캐스트 (5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void broadcastRiskStatusUpdate() {
        if (!activeSessions.isEmpty()) {
            try {
                List<MonteCarloSimulation> runningSimulations = riskAssessmentService.getRunningSimulations();
                
                RiskDashboardMessage statusUpdate = RiskDashboardMessage.builder()
                    .type("PERIODIC_STATUS_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .data(Map.of(
                        "runningSimulations", runningSimulations.size(),
                        "activeConnections", activeSessions.size(),
                        "timestamp", LocalDateTime.now()
                    ))
                    .build();
                    
                broadcastToAll(statusUpdate);
                log.debug("주기적 리스크 상태 업데이트 브로드캐스트 완료: 연결 수={}", activeSessions.size());
                
            } catch (Exception e) {
                log.error("주기적 리스크 상태 업데이트 오류", e);
            }
        }
    }
    
    /**
     * 사용자 ID 추출
     */
    private String extractUserId(WebSocketSession session) {
        // JWT 토큰에서 사용자 ID 추출 로직
        // 현재는 세션 ID 사용
        return session.getId();
    }
    
    /**
     * WebSocket 세션 정보
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class WebSocketSessionInfo {
        private String sessionId;
        private String userId;
        private LocalDateTime connectedAt;
        private CopyOnWriteArrayList<Long> subscriptions;
    }
}