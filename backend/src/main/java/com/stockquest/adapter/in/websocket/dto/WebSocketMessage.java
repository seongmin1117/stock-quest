package com.stockquest.adapter.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 메시지 표준 형식
 * 클라이언트-서버 간 실시간 통신을 위한 메시지 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage<T> {
    
    /**
     * 메시지 타입 (예: QUOTE_UPDATE, PORTFOLIO_UPDATE, TRADE_EXECUTED)
     */
    private String type;
    
    /**
     * 메시지 데이터 (타입에 따라 다른 구조)
     */
    private T data;
    
    /**
     * 메시지 타임스탬프 (Unix timestamp in milliseconds)
     */
    private Long timestamp;
    
    /**
     * 에러 정보 (에러 발생시에만 사용)
     */
    private String error;
    
    /**
     * 메시지 ID (추적을 위한 고유 식별자)
     */
    private String messageId;
    
    /**
     * 세션 ID (특정 세션에만 전송할 때 사용)
     */
    private String sessionId;
    
    /**
     * 사용자 ID (특정 사용자에게만 전송할 때 사용)
     */
    private Long userId;
    
    // Factory methods for common message types
    
    public static <T> WebSocketMessage<T> success(String type, T data) {
        return WebSocketMessage.<T>builder()
            .type(type)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static WebSocketMessage<String> error(String error) {
        return WebSocketMessage.<String>builder()
            .type("ERROR")
            .error(error)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> WebSocketMessage<T> userMessage(String type, T data, Long userId) {
        return WebSocketMessage.<T>builder()
            .type(type)
            .data(data)
            .userId(userId)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> WebSocketMessage<T> sessionMessage(String type, T data, String sessionId) {
        return WebSocketMessage.<T>builder()
            .type(type)
            .data(data)
            .sessionId(sessionId)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}