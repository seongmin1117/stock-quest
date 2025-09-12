package com.stockquest.config;

import com.stockquest.adapter.in.websocket.RiskMonitoringWebSocketController;
import com.stockquest.adapter.in.websocket.MLSignalsWebSocketController;
import com.stockquest.adapter.in.websocket.MarketDataWebSocketController;
import com.stockquest.adapter.in.websocket.PortfolioWebSocketController;
import com.stockquest.adapter.in.websocket.OrderExecutionWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 설정
 * Phase 2.2: WebSocket 실시간 기능 구현 - 완전한 실시간 시스템
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    
    private final RiskMonitoringWebSocketController riskMonitoringController;
    private final MLSignalsWebSocketController mlSignalsController;
    private final MarketDataWebSocketController marketDataController;
    private final PortfolioWebSocketController portfolioController;
    private final OrderExecutionWebSocketController orderExecutionController;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 리스크 모니터링 WebSocket 엔드포인트 등록
        registry.addHandler(riskMonitoringController, "/ws/risk-monitoring")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
        
        // ML 시그널 WebSocket 엔드포인트 등록        
        registry.addHandler(mlSignalsController, "/ws/ml-signals")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
                
        // 시장 데이터 WebSocket 엔드포인트 등록
        registry.addHandler(marketDataController, "/ws/market-data")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
                
        // 포트폴리오 실시간 업데이트 WebSocket 엔드포인트 등록
        registry.addHandler(portfolioController, "/ws/portfolio")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
                
        // 주문 실행 알림 WebSocket 엔드포인트 등록
        registry.addHandler(orderExecutionController, "/ws/orders")
                .setAllowedOrigins("*") // 개발 환경용 - 프로덕션에서는 구체적인 도메인 설정 필요
                .withSockJS(); // SockJS 지원 활성화
                
        log.info("리스크 모니터링 WebSocket 엔드포인트 등록 완료: /ws/risk-monitoring");
        log.info("ML 시그널 WebSocket 엔드포인트 등록 완료: /ws/ml-signals");
        log.info("시장 데이터 WebSocket 엔드포인트 등록 완료: /ws/market-data");
        log.info("포트폴리오 WebSocket 엔드포인트 등록 완료: /ws/portfolio");
        log.info("주문 실행 WebSocket 엔드포인트 등록 완료: /ws/orders");
        log.info("============= Phase 2.2: WebSocket 실시간 시스템 구성 완료 =============");
    }
}