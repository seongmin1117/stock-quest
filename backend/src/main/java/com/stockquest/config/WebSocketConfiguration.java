package com.stockquest.config;

import com.stockquest.adapter.in.websocket.RiskMonitoringWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 설정
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    
    private final RiskMonitoringWebSocketController riskMonitoringController;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 리스크 모니터링 WebSocket 엔드포인트 등록
        registry.addHandler(riskMonitoringController, "/ws/risk-monitoring")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
                
        log.info("리스크 모니터링 WebSocket 엔드포인트 등록 완료: /ws/risk-monitoring");
    }
}