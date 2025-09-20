package com.stockquest.adapter.out.auth;

import com.stockquest.application.common.port.JwtTokenProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 토큰 제공자 어댑터 구현체
 */
@Component
public class JwtTokenAdapter implements JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long expirationMillis;
    
    public JwtTokenAdapter(@Value("${jwt.secret}") String secret,
                          @Value("${jwt.expiration}") long expirationMillis) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }
    
    @Override
    public String generateToken(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효한 사용자 ID가 필요합니다");
        }
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);
        
        return Jwts.builder()
                  .setSubject(userId.toString())
                  .setIssuedAt(now)
                  .setExpiration(expiration)
                  .signWith(secretKey)
                  .compact();
    }
    
    @Override
    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                              .verifyWith(secretKey)
                              .build()
                              .parseSignedClaims(token)
                              .getPayload();
            
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    @Override
    public LocalDateTime getTokenExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                              .verifyWith(secretKey)
                              .build()
                              .parseSignedClaims(token)
                              .getPayload();
            
            return claims.getExpiration()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }
}