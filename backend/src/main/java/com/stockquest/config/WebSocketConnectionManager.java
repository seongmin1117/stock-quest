package com.stockquest.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 연결 관리 서비스
 * Phase 2.2: WebSocket 실시간 기능 구현 - 클라이언트 연결 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionManager {
    
    // 전체 활성 연결 관리
    private final Map<String, Set<WebSocketSession>> connectionsByType = new ConcurrentHashMap<>();
    private final Map<String, ConnectionInfo> sessionInfo = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger totalUsers = new AtomicInteger(0);
    
    // 연결 타입 상수
    public static final String MARKET_DATA = "market-data";
    public static final String PORTFOLIO = "portfolio";
    public static final String ORDER_EXECUTION = "order-execution";
    public static final String RISK_MONITORING = "risk-monitoring";
    public static final String ML_SIGNALS = "ml-signals";
    
    /**
     * 새로운 WebSocket 연결 등록
     */
    public boolean registerConnection(String connectionType, WebSocketSession session, String userId) {
        try {
            // 연결 정보 생성
            ConnectionInfo info = ConnectionInfo.builder()
                .sessionId(session.getId())
                .connectionType(connectionType)
                .userId(userId)
                .connectedAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .active(true)
                .build();
            
            // 연결 정보 저장
            sessionInfo.put(session.getId(), info);
            sessionUsers.put(session.getId(), userId);
            
            // 타입별 연결 그룹에 추가
            connectionsByType.computeIfAbsent(connectionType, k -> new CopyOnWriteArraySet<>())
                .add(session);
            
            // 통계 업데이트
            totalConnections.incrementAndGet();
            updateUniqueUserCount();
            
            log.info("WebSocket 연결 등록: type={}, sessionId={}, userId={}, 총 연결 수={}",
                connectionType, session.getId(), userId, totalConnections.get());

            return true;
        } catch (Exception e) {
            log.error("WebSocket 연결 등록 실패: sessionId={}", session.getId(), e);
            return false;
        }
    }
    
    /**
     * WebSocket 연결 해제
     */
    public void unregisterConnection(String connectionType, WebSocketSession session) {
        try {
            String sessionId = session.getId();
            ConnectionInfo info = sessionInfo.get(sessionId);
            
            if (info != null) {
                info.setActive(false);
                info.setDisconnectedAt(LocalDateTime.now());
                
                log.info("WebSocket 연결 해제: type={}, sessionId={}, userId={}, 연결 시간={}", 
                    connectionType, sessionId, info.getUserId(), 
                    info.getConnectionDuration());
            }
            
            // 연결 정보 정리
            connectionsByType.getOrDefault(connectionType, Collections.emptySet()).remove(session);
            sessionInfo.remove(sessionId);
            sessionUsers.remove(sessionId);
            
            // 통계 업데이트
            totalConnections.decrementAndGet();
            updateUniqueUserCount();
            
        } catch (Exception e) {
            log.error("WebSocket 연결 해제 실패: sessionId={}", session.getId(), e);
        }
    }
    
    /**
     * 연결 활동 업데이트
     */
    public void updateActivity(String sessionId) {
        ConnectionInfo info = sessionInfo.get(sessionId);
        if (info != null) {
            info.setLastActivity(LocalDateTime.now());
        }
    }
    
    /**
     * 특정 타입의 모든 활성 세션 조회
     */
    public Set<WebSocketSession> getActiveSessionsByType(String connectionType) {
        return new HashSet<>(connectionsByType.getOrDefault(connectionType, Collections.emptySet()));
    }
    
    /**
     * 특정 사용자의 모든 활성 세션 조회
     */
    public Set<WebSocketSession> getActiveSessionsByUser(String userId) {
        Set<WebSocketSession> userSessions = new HashSet<>();
        
        sessionUsers.entrySet().stream()
            .filter(entry -> userId.equals(entry.getValue()))
            .forEach(entry -> {
                String sessionId = entry.getKey();
                connectionsByType.values().forEach(sessions -> {
                    sessions.stream()
                        .filter(session -> session.getId().equals(sessionId))
                        .findFirst()
                        .ifPresent(userSessions::add);
                });
            });
        
        return userSessions;
    }
    
    /**
     * 연결 통계 조회
     */
    public ConnectionStatistics getConnectionStatistics() {
        Map<String, Integer> connectionsByTypeCount = new HashMap<>();
        connectionsByType.forEach((type, sessions) -> 
            connectionsByTypeCount.put(type, sessions.size()));
        
        Map<String, Long> connectionsByDuration = new HashMap<>();
        sessionInfo.values().forEach(info -> {
            String durationType = getDurationType(info.getConnectionDuration());
            connectionsByDuration.put(durationType, 
                connectionsByDuration.getOrDefault(durationType, 0L) + 1);
        });
        
        return ConnectionStatistics.builder()
            .totalConnections(totalConnections.get())
            .uniqueUsers(totalUsers.get())
            .connectionsByType(connectionsByTypeCount)
            .connectionsByDuration(connectionsByDuration)
            .averageConnectionDuration(calculateAverageConnectionDuration())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 비활성 연결 정리 (매 5분)
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupInactiveConnections() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        int cleanedCount = 0;
        
        List<String> inactiveSessions = sessionInfo.entrySet().stream()
            .filter(entry -> entry.getValue().getLastActivity().isBefore(threshold))
            .map(Map.Entry::getKey)
            .toList();
        
        for (String sessionId : inactiveSessions) {
            ConnectionInfo info = sessionInfo.get(sessionId);
            if (info != null && info.isActive()) {
                // 비활성 세션을 찾아서 정리
                connectionsByType.values().forEach(sessions -> 
                    sessions.removeIf(session -> session.getId().equals(sessionId)));
                
                info.setActive(false);
                info.setDisconnectedAt(LocalDateTime.now());
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            log.info("비활성 WebSocket 연결 정리 완료: {} 개 연결 정리", cleanedCount);
            totalConnections.addAndGet(-cleanedCount);
            updateUniqueUserCount();
        }
    }
    
    /**
     * 연결 상태 모니터링 및 로깅 (매 1분)
     */
    @Scheduled(fixedRate = 60000)
    public void logConnectionStatistics() {
        ConnectionStatistics stats = getConnectionStatistics();
        
        log.info("WebSocket 연결 통계 - 총 연결: {}, 사용자: {}, 평균 연결 시간: {}분", 
            stats.getTotalConnections(), 
            stats.getUniqueUsers(),
            stats.getAverageConnectionDuration());
        
        stats.getConnectionsByType().forEach((type, count) -> 
            log.debug("  {} 연결: {} 개", type, count));
    }
    
    /**
     * 특정 타입의 연결에 메시지 브로드캐스트
     */
    public void broadcastToType(String connectionType, Object message) {
        Set<WebSocketSession> sessions = getActiveSessionsByType(connectionType);
        
        sessions.parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    // 실제 구현에서는 적절한 메시지 전송 로직 사용
                    updateActivity(session.getId());
                    log.debug("메시지 브로드캐스트: type={}, sessionId={}", 
                        connectionType, session.getId());
                } catch (Exception e) {
                    log.error("브로드캐스트 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    /**
     * 특정 사용자의 모든 연결에 메시지 전송
     */
    public void sendToUser(String userId, Object message) {
        Set<WebSocketSession> userSessions = getActiveSessionsByUser(userId);
        
        userSessions.parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    // 실제 구현에서는 적절한 메시지 전송 로직 사용
                    updateActivity(session.getId());
                    log.debug("사용자별 메시지 전송: userId={}, sessionId={}", 
                        userId, session.getId());
                } catch (Exception e) {
                    log.error("사용자별 메시지 전송 실패: sessionId={}", session.getId(), e);
                }
            });
    }
    
    /**
     * 연결 상태 확인
     */
    public boolean isSessionActive(String sessionId) {
        ConnectionInfo info = sessionInfo.get(sessionId);
        return info != null && info.isActive();
    }
    
    /**
     * 사용자 연결 수 조회
     */
    public int getUserConnectionCount(String userId) {
        return getActiveSessionsByUser(userId).size();
    }
    
    /**
     * 최대 동시 연결 수 제한 확인
     */
    public boolean canAcceptNewConnection(String userId) {
        int userConnections = getUserConnectionCount(userId);
        int maxUserConnections = 10; // 사용자당 최대 10개 연결
        
        return userConnections < maxUserConnections && totalConnections.get() < 1000; // 전체 최대 1000 연결
    }
    
    // ========================= Private Helper Methods =========================
    
    private void updateUniqueUserCount() {
        Set<String> uniqueUsers = new HashSet<>(sessionUsers.values());
        totalUsers.set(uniqueUsers.size());
    }
    
    private String getDurationType(long durationMinutes) {
        if (durationMinutes < 5) {
            return "0-5min";
        } else if (durationMinutes < 30) {
            return "5-30min";
        } else if (durationMinutes < 60) {
            return "30-60min";
        } else {
            return "60min+";
        }
    }
    
    private long calculateAverageConnectionDuration() {
        OptionalDouble average = sessionInfo.values().stream()
            .filter(ConnectionInfo::isActive)
            .mapToLong(ConnectionInfo::getConnectionDuration)
            .average();
        
        return Math.round(average.orElse(0.0));
    }
    
    // ========================= Inner Classes =========================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConnectionInfo {
        private String sessionId;
        private String connectionType;
        private String userId;
        private LocalDateTime connectedAt;
        private LocalDateTime disconnectedAt;
        private LocalDateTime lastActivity;
        private boolean active;
        
        public long getConnectionDuration() {
            LocalDateTime endTime = disconnectedAt != null ? disconnectedAt : LocalDateTime.now();
            return java.time.Duration.between(connectedAt, endTime).toMinutes();
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public void setDisconnectedAt(LocalDateTime disconnectedAt) {
            this.disconnectedAt = disconnectedAt;
        }
        
        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConnectionStatistics {
        private int totalConnections;
        private int uniqueUsers;
        private Map<String, Integer> connectionsByType;
        private Map<String, Long> connectionsByDuration;
        private long averageConnectionDuration;
        private LocalDateTime timestamp;
    }

}