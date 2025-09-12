package com.stockquest.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP WebSocket 설정 (백테스팅 실시간 업데이트용)
 * Phase 8.2: Enhanced Trading Intelligence - STOMP 메시징 지원
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 활성화 - 클라이언트가 구독할 주제들
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // 클라이언트에서 서버로 보내는 메시지의 접두사 설정
        config.setApplicationDestinationPrefixes("/app");
        
        // 사용자별 메시지 전송을 위한 사용자 접두사 설정
        config.setUserDestinationPrefix("/user");
        
        log.info("STOMP 메시지 브로커 설정 완료");
        log.info("- 구독 채널: /topic/*, /queue/*, /user/*");
        log.info("- 애플리케이션 접두사: /app");
        log.info("- 사용자 접두사: /user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 백테스팅 WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/backtesting")
                .setAllowedOriginPatterns("*") // 개발 환경용
                .withSockJS(); // SockJS 지원
        
        // ML 시그널 STOMP 엔드포인트 (기존 웹소켓과 호환)
        registry.addEndpoint("/ws/ml-signals-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 포트폴리오 최적화 WebSocket 엔드포인트
        registry.addEndpoint("/ws/portfolio-optimization")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 종합 대시보드 WebSocket 엔드포인트
        registry.addEndpoint("/ws/dashboard")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        log.info("STOMP 엔드포인트 등록 완료:");
        log.info("- 백테스팅: /ws/backtesting");
        log.info("- ML 시그널 (STOMP): /ws/ml-signals-stomp");
        log.info("- 포트폴리오 최적화: /ws/portfolio-optimization");
        log.info("- 종합 대시보드: /ws/dashboard");
    }
}