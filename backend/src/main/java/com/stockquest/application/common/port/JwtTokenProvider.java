package com.stockquest.application.common.port;

import java.time.LocalDateTime;

/**
 * JWT 토큰 제공자 포트 (출력 포트)
 */
public interface JwtTokenProvider {
    
    /**
     * 사용자 ID로 JWT 토큰 생성
     */
    String generateToken(Long userId);
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    Long extractUserId(String token);
    
    /**
     * JWT 토큰 유효성 검증
     */
    boolean validateToken(String token);
    
    /**
     * JWT 토큰 만료 시간 조회
     */
    LocalDateTime getTokenExpiration(String token);
}