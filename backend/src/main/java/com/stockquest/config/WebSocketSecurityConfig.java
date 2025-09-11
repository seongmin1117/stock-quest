package com.stockquest.config;

import com.stockquest.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket 보안 설정
 * JWT 토큰을 통한 WebSocket 연결 인증
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * WebSocket 연결시 JWT 토큰 검증 인터셉터
     */
    public HandshakeInterceptor createJwtHandshakeInterceptor() {
        return new JwtHandshakeInterceptor(jwtTokenProvider);
    }

    /**
     * JWT 토큰 기반 WebSocket 핸드셰이크 인터셉터
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtTokenProvider jwtTokenProvider;

        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request, 
                ServerHttpResponse response,
                WebSocketHandler wsHandler, 
                Map<String, Object> attributes) throws Exception {

            try {
                String token = extractTokenFromRequest(request);
                
                if (token == null) {
                    log.warn("❌ WebSocket handshake failed: No JWT token provided");
                    return false;
                }

                // JWT 토큰 유효성 검증
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("❌ WebSocket handshake failed: Invalid JWT token");
                    return false;
                }

                // 사용자 정보 추출 및 속성에 저장
                String username = jwtTokenProvider.getUsername(token);
                Long userId = jwtTokenProvider.getUserId(token);
                
                attributes.put("username", username);
                attributes.put("userId", userId);
                attributes.put("token", token);
                
                // 보안 컨텍스트 설정 (선택적)
                var authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.info("✅ WebSocket handshake successful for user: {} (ID: {})", username, userId);
                return true;
                
            } catch (Exception e) {
                log.error("❌ WebSocket handshake error: {}", e.getMessage());
                return false;
            }
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request, 
                ServerHttpResponse response,
                WebSocketHandler wsHandler, 
                Exception exception) {
            
            if (exception != null) {
                log.error("❌ WebSocket handshake completed with error: {}", exception.getMessage());
            } else {
                log.debug("✅ WebSocket handshake completed successfully");
            }
        }

        /**
         * HTTP 요청에서 JWT 토큰 추출
         * 우선순위: 1) query parameter 2) Authorization header
         */
        private String extractTokenFromRequest(ServerHttpRequest request) {
            try {
                URI uri = request.getURI();
                
                // 1. Query parameter에서 토큰 추출 (WebSocket 연결시 일반적)
                String tokenFromQuery = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .getFirst("token");
                
                if (tokenFromQuery != null && !tokenFromQuery.isEmpty()) {
                    log.debug("🔑 JWT token extracted from query parameter");
                    return tokenFromQuery;
                }
                
                // 2. Authorization header에서 토큰 추출
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    log.debug("🔑 JWT token extracted from Authorization header");
                    return authHeader.substring(7);
                }
                
                // 3. Cookie에서 토큰 추출 (선택적)
                String cookieHeader = request.getHeaders().getFirst("Cookie");
                if (cookieHeader != null) {
                    String[] cookies = cookieHeader.split(";");
                    for (String cookie : cookies) {
                        String[] parts = cookie.trim().split("=", 2);
                        if (parts.length == 2 && "access_token".equals(parts[0])) {
                            log.debug("🔑 JWT token extracted from cookie");
                            return parts[1];
                        }
                    }
                }
                
                log.debug("❌ No JWT token found in request");
                return null;
                
            } catch (Exception e) {
                log.error("❌ Error extracting token from request: {}", e.getMessage());
                return null;
            }
        }
    }
    
    /**
     * WebSocket 연결별 사용자 정보 추출 유틸리티
     */
    public static class WebSocketAuthUtils {
        
        /**
         * WebSocket 세션 속성에서 사용자 ID 추출
         */
        public static Long getUserIdFromAttributes(Map<String, Object> attributes) {
            Object userId = attributes.get("userId");
            return userId instanceof Long ? (Long) userId : null;
        }
        
        /**
         * WebSocket 세션 속성에서 사용자명 추출
         */
        public static String getUsernameFromAttributes(Map<String, Object> attributes) {
            Object username = attributes.get("username");
            return username instanceof String ? (String) username : null;
        }
        
        /**
         * WebSocket 세션 속성에서 JWT 토큰 추출
         */
        public static String getTokenFromAttributes(Map<String, Object> attributes) {
            Object token = attributes.get("token");
            return token instanceof String ? (String) token : null;
        }
    }
}