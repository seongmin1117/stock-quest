package com.stockquest.config;

import com.stockquest.adapter.in.websocket.MarketDataWebSocketHandler;
import com.stockquest.adapter.in.websocket.TradingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 설정 클래스
 * 실시간 시장 데이터 및 트레이딩 기능을 위한 WebSocket 엔드포인트 등록
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarketDataWebSocketHandler marketDataHandler;
    private final TradingWebSocketHandler tradingHandler;
    private final WebSocketSecurityConfig webSocketSecurityConfig;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("🔌 Registering WebSocket handlers for real-time features");
        
        // 실시간 시장 데이터 스트리밍 엔드포인트
        registry.addHandler(marketDataHandler, "/ws/market-data")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001", "https://stockquest.com")
                .withSockJS()
                .setHeartbeatTime(25000) // 25초마다 heartbeat
                .setDisconnectDelay(5000); // 5초 연결 끊김 지연
        
        // 실시간 트레이딩 및 포트폴리오 업데이트 엔드포인트 (인증 필요)
        registry.addHandler(tradingHandler, "/ws/trading")
                .addInterceptors(webSocketSecurityConfig.createJwtHandshakeInterceptor())
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001", "https://stockquest.com")
                .withSockJS()
                .setHeartbeatTime(15000) // 15초마다 heartbeat (더 빠른 응답)
                .setDisconnectDelay(3000); // 3초 연결 끊김 지연
        
        log.info("✅ WebSocket handlers registered successfully");
        log.info("📊 Market Data: /ws/market-data");
        log.info("💼 Trading: /ws/trading");
    }
}