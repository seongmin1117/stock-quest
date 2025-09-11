package com.stockquest.adapter.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 실시간 리스크 대시보드 WebSocket 메시지 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskDashboardMessage {
    
    /**
     * 메시지 타입
     */
    private String type;
    
    /**
     * 포트폴리오 ID (관련된 경우)
     */
    private Long portfolioId;
    
    /**
     * 메시지 타임스탬프
     */
    private LocalDateTime timestamp;
    
    /**
     * 메시지 데이터
     */
    private Object data;
    
    /**
     * 메시지 ID (추적용)
     */
    private String messageId;
    
    /**
     * 사용자 ID (권한 확인용)
     */
    private String userId;
    
    /**
     * 메시지 우선순위
     */
    @Builder.Default
    private MessagePriority priority = MessagePriority.NORMAL;
    
    /**
     * 메시지 카테고리
     */
    private MessageCategory category;
    
    /**
     * 추가 메타데이터
     */
    private MessageMetadata metadata;
    
    public enum MessagePriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    public enum MessageCategory {
        CONNECTION,
        SUBSCRIPTION,
        RISK_ASSESSMENT,
        SIMULATION_STATUS,
        MARKET_DATA,
        ALERT,
        SYSTEM,
        ERROR
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageMetadata {
        
        /**
         * 메시지 크기 (바이트)
         */
        private Integer messageSize;
        
        /**
         * 압축 여부
         */
        private Boolean compressed;
        
        /**
         * 재시도 횟수
         */
        private Integer retryCount;
        
        /**
         * 만료 시간
         */
        private LocalDateTime expiresAt;
        
        /**
         * 수신 확인 필요 여부
         */
        private Boolean requiresAck;
        
        /**
         * 메시지 버전
         */
        private String version;
        
        /**
         * 관련 시뮬레이션 ID
         */
        private String simulationId;
        
        /**
         * 관련 시나리오 ID
         */
        private String scenarioId;
        
        /**
         * 처리 시간 (밀리초)
         */
        private Long processingTimeMs;
    }
    
    /**
     * 연결 확립 메시지 생성
     */
    public static RiskDashboardMessage connectionEstablished() {
        return RiskDashboardMessage.builder()
            .type("CONNECTION_ESTABLISHED")
            .category(MessageCategory.CONNECTION)
            .priority(MessagePriority.NORMAL)
            .timestamp(LocalDateTime.now())
            .data("실시간 리스크 모니터링 대시보드에 연결되었습니다.")
            .build();
    }
    
    /**
     * 오류 메시지 생성
     */
    public static RiskDashboardMessage error(String errorMessage) {
        return RiskDashboardMessage.builder()
            .type("ERROR")
            .category(MessageCategory.ERROR)
            .priority(MessagePriority.HIGH)
            .timestamp(LocalDateTime.now())
            .data(java.util.Map.of("error", errorMessage))
            .build();
    }
    
    /**
     * 포트폴리오 구독 확인 메시지 생성
     */
    public static RiskDashboardMessage subscriptionConfirmed(Long portfolioId) {
        return RiskDashboardMessage.builder()
            .type("SUBSCRIPTION_CONFIRMED")
            .category(MessageCategory.SUBSCRIPTION)
            .priority(MessagePriority.NORMAL)
            .portfolioId(portfolioId)
            .timestamp(LocalDateTime.now())
            .data("포트폴리오 " + portfolioId + " 실시간 모니터링을 시작합니다.")
            .build();
    }
    
    /**
     * 리스크 평가 시작 메시지 생성
     */
    public static RiskDashboardMessage riskAssessmentStarted(Long portfolioId, String simulationId) {
        return RiskDashboardMessage.builder()
            .type("RISK_ASSESSMENT_STARTED")
            .category(MessageCategory.RISK_ASSESSMENT)
            .priority(MessagePriority.NORMAL)
            .portfolioId(portfolioId)
            .timestamp(LocalDateTime.now())
            .data("리스크 평가를 시작합니다.")
            .metadata(MessageMetadata.builder()
                .simulationId(simulationId)
                .requiresAck(false)
                .build())
            .build();
    }
    
    /**
     * 리스크 평가 완료 메시지 생성
     */
    public static RiskDashboardMessage riskAssessmentCompleted(Long portfolioId, Object result, String simulationId) {
        return RiskDashboardMessage.builder()
            .type("RISK_ASSESSMENT_COMPLETED")
            .category(MessageCategory.RISK_ASSESSMENT)
            .priority(MessagePriority.HIGH)
            .portfolioId(portfolioId)
            .timestamp(LocalDateTime.now())
            .data(result)
            .metadata(MessageMetadata.builder()
                .simulationId(simulationId)
                .requiresAck(true)
                .build())
            .build();
    }
    
    /**
     * 리스크 알림 메시지 생성
     */
    public static RiskDashboardMessage riskAlert(Long portfolioId, String alertMessage, String severity) {
        MessagePriority priority = switch (severity.toUpperCase()) {
            case "CRITICAL" -> MessagePriority.CRITICAL;
            case "HIGH" -> MessagePriority.HIGH;
            case "MEDIUM" -> MessagePriority.NORMAL;
            default -> MessagePriority.LOW;
        };
        
        return RiskDashboardMessage.builder()
            .type("RISK_ALERT")
            .category(MessageCategory.ALERT)
            .priority(priority)
            .portfolioId(portfolioId)
            .timestamp(LocalDateTime.now())
            .data(java.util.Map.of(
                "message", alertMessage,
                "severity", severity,
                "portfolioId", portfolioId
            ))
            .metadata(MessageMetadata.builder()
                .requiresAck(true)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build())
            .build();
    }
    
    /**
     * 시뮬레이션 상태 업데이트 메시지 생성
     */
    public static RiskDashboardMessage simulationStatusUpdate(Object statusData) {
        return RiskDashboardMessage.builder()
            .type("SIMULATION_STATUS_UPDATE")
            .category(MessageCategory.SIMULATION_STATUS)
            .priority(MessagePriority.NORMAL)
            .timestamp(LocalDateTime.now())
            .data(statusData)
            .build();
    }
    
    /**
     * 하트비트 응답 메시지 생성
     */
    public static RiskDashboardMessage heartbeatAck() {
        return RiskDashboardMessage.builder()
            .type("HEARTBEAT_ACK")
            .category(MessageCategory.SYSTEM)
            .priority(MessagePriority.LOW)
            .timestamp(LocalDateTime.now())
            .data("pong")
            .build();
    }
    
    /**
     * 포트폴리오 리스크 업데이트 메시지 생성
     */
    public static RiskDashboardMessage portfolioRiskUpdate(Long portfolioId, Object riskData) {
        return RiskDashboardMessage.builder()
            .type("PORTFOLIO_RISK_UPDATE")
            .category(MessageCategory.RISK_ASSESSMENT)
            .priority(MessagePriority.NORMAL)
            .portfolioId(portfolioId)
            .timestamp(LocalDateTime.now())
            .data(riskData)
            .metadata(MessageMetadata.builder()
                .version("1.0")
                .build())
            .build();
    }
    
    /**
     * 시장 데이터 업데이트 메시지 생성
     */
    public static RiskDashboardMessage marketDataUpdate(Object marketData) {
        return RiskDashboardMessage.builder()
            .type("MARKET_DATA_UPDATE")
            .category(MessageCategory.MARKET_DATA)
            .priority(MessagePriority.NORMAL)
            .timestamp(LocalDateTime.now())
            .data(marketData)
            .build();
    }
}