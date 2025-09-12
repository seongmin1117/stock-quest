package com.stockquest.adapter.in.web.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 인증 응답 DTO - 완전한 토큰 정보 및 리다이렉트 지원
 */
@Schema(description = "인증 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    
    @Schema(description = "JWT 인증 토큰")
    String accessToken,
    
    @Schema(description = "리프레시 토큰")
    String refreshToken,
    
    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,
    
    @Schema(description = "액세스 토큰 만료 시간")
    LocalDateTime accessTokenExpiresAt,
    
    @Schema(description = "리프레시 토큰 만료 시간")
    LocalDateTime refreshTokenExpiresAt,
    
    @Schema(description = "액세스 토큰 만료까지 남은 초")
    Long expiresIn,
    
    @Schema(description = "사용자 ID")
    Long userId,
    
    @Schema(description = "사용자 이메일")
    String email,
    
    @Schema(description = "사용자 닉네임")
    String nickname,
    
    @Schema(description = "로그인 후 리다이렉트 URL")
    String redirectUrl
) {
    
    /**
     * 기본 생성자 (이전 버전 호환성 유지)
     */
    public static AuthResponse basic(String accessToken, Long userId, String email, String nickname) {
        return new AuthResponse(
            accessToken, null, "Bearer", null, null, null,
            userId, email, nickname, null
        );
    }
    
    /**
     * 완전한 토큰 정보로 생성
     */
    public static AuthResponse withTokenPair(
        String accessToken, String refreshToken, 
        LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
        Long userId, String email, String nickname, String redirectUrl
    ) {
        long expiresIn = accessTokenExpiresAt != null ? 
            java.time.Duration.between(LocalDateTime.now(), accessTokenExpiresAt).getSeconds() : 0;
            
        return new AuthResponse(
            accessToken, refreshToken, "Bearer",
            accessTokenExpiresAt, refreshTokenExpiresAt, expiresIn,
            userId, email, nickname, redirectUrl
        );
    }
}