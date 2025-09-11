package com.stockquest.domain.auth;

import java.time.LocalDateTime;

/**
 * Refresh Token 도메인 모델
 * Access Token 갱신을 위한 장기 토큰
 */
public class RefreshToken {
    
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private boolean revoked;
    
    // 생성자
    private RefreshToken() {}
    
    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.revoked = false;
    }
    
    /**
     * 팩토리 메소드: 새 리프레시 토큰 생성
     */
    public static RefreshToken create(Long userId, String token, int validityDays) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validityDays);
        return new RefreshToken(userId, token, expiresAt);
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean isValid() {
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * 토큰 만료 확인
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 토큰 폐기
     */
    public void revoke() {
        this.revoked = true;
    }
    
    /**
     * 토큰 사용 기록 업데이트
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
    
    /**
     * 토큰 만료까지 남은 시간(분)
     */
    public long getMinutesUntilExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getToken() {
        return token;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public boolean isRevoked() {
        return revoked;
    }
    
    // Setters (JPA용)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", token='" + token.substring(0, 8) + "...' " +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                '}';
    }
}