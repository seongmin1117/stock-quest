package com.stockquest.domain.auth;

import java.time.LocalDateTime;

/**
 * Access Token과 Refresh Token 쌍
 * 인증 응답에서 함께 반환되는 토큰들
 */
public class TokenPair {
    
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime accessTokenExpiresAt;
    private final LocalDateTime refreshTokenExpiresAt;
    private final String tokenType;
    
    public TokenPair(String accessToken, String refreshToken, 
                    LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.tokenType = "Bearer";
    }
    
    /**
     * Access Token 만료까지 남은 초
     */
    public long getAccessTokenExpiresInSeconds() {
        return java.time.Duration.between(LocalDateTime.now(), accessTokenExpiresAt).getSeconds();
    }
    
    /**
     * Refresh Token 만료까지 남은 초
     */
    public long getRefreshTokenExpiresInSeconds() {
        return java.time.Duration.between(LocalDateTime.now(), refreshTokenExpiresAt).getSeconds();
    }
    
    /**
     * Access Token 만료 여부
     */
    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * Refresh Token 만료 여부
     */
    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    // Getters
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public LocalDateTime getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }
    
    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    @Override
    public String toString() {
        return "TokenPair{" +
                "accessToken='" + accessToken.substring(0, 10) + "...' " +
                ", refreshToken='" + refreshToken.substring(0, 10) + "...' " +
                ", tokenType='" + tokenType + '\'' +
                ", accessTokenExpiresAt=" + accessTokenExpiresAt +
                ", refreshTokenExpiresAt=" + refreshTokenExpiresAt +
                '}';
    }
}