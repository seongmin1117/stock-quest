package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.auth.RefreshToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RefreshToken JPA 엔티티
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_token", columnList = "token"),
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_expires_at", columnList = "expires_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(nullable = false)
    private boolean revoked;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public RefreshToken toDomain() {
        RefreshToken refreshToken = new RefreshToken(userId, token, expiresAt);
        refreshToken.setId(id);
        refreshToken.setCreatedAt(createdAt);
        refreshToken.setLastUsedAt(lastUsedAt);
        refreshToken.setRevoked(revoked);
        return refreshToken;
    }
    
    public static RefreshTokenJpaEntity fromDomain(RefreshToken refreshToken) {
        return RefreshTokenJpaEntity.builder()
                .id(refreshToken.getId())
                .userId(refreshToken.getUserId())
                .token(refreshToken.getToken())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .lastUsedAt(refreshToken.getLastUsedAt())
                .revoked(refreshToken.isRevoked())
                .build();
    }
}