package com.stockquest.adapter.out.security;

import com.stockquest.config.security.SecureKeyGenerator;
import com.stockquest.domain.auth.TokenPair;
import com.stockquest.domain.auth.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 강화된 JWT 토큰 제공자
 * Access Token, Refresh Token 생성, 검증, 파싱 담당
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MINIMUM_KEY_LENGTH = 32; // 256비트
    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 7;
    
    private final String secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final SecureKeyGenerator keyGenerator;
    
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity:3600000}") long accessTokenValidityInMilliseconds,
            SecureKeyGenerator keyGenerator) {
        
        // 키 보안성 검증
        validateSecretKey(secretKey);
        
        this.secretKey = secretKey;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.keyGenerator = keyGenerator;
        
        logger.info("JWT Token Provider initialized with {} ms access token validity", 
                   accessTokenValidityInMilliseconds);
    }
    
    /**
     * 비밀키 보안성 검증
     */
    private void validateSecretKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key must not be null or empty");
        }
        
        if (key.getBytes(StandardCharsets.UTF_8).length < MINIMUM_KEY_LENGTH) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes)");
        }
        
        // 개발용 기본키 사용 금지
        if (key.contains("StockQuestSecretKeyForJWTTokenGeneration") || 
            key.equals("stockquest-jwt-secret-key-for-development-only")) {
            throw new IllegalArgumentException("Default development JWT key is not allowed in production");
        }
    }
    
    /**
     * Access Token과 Refresh Token 쌍 생성
     */
    public TokenPair createTokenPair(Long userId, String email) {
        Date now = new Date();
        Date accessTokenExpiry = new Date(now.getTime() + accessTokenValidityInMilliseconds);
        
        // Access Token 생성
        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(accessTokenExpiry)
                .signWith(getSigningKey())
                .compact();
        
        // Refresh Token 생성 (UUID 기반 랜덤 토큰)
        String refreshTokenValue = keyGenerator.generateRandomString(64);
        
        LocalDateTime accessTokenExpiresAt = accessTokenExpiry.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        
        return new TokenPair(accessToken, refreshTokenValue, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
    
    /**
     * Access Token 생성 (기존 호환성 유지)
     */
    public String createAccessToken(Long userId, String email) {
        return createTokenPair(userId, email).getAccessToken();
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
    
    /**
     * 토큰 유효성 검증 (강화된 보안)
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.debug("Token validation failed: token is null or empty");
            return false;
        }
        
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // 만료 시간 검증
            if (expiration.before(now)) {
                logger.debug("Token validation failed: token expired at {}", expiration);
                return false;
            }
            
            // 토큰 타입 검증 (Access Token인지 확인)
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                logger.debug("Token validation failed: invalid token type {}", tokenType);
                return false;
            }
            
            return true;
        } catch (JwtException e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Unexpected error during token validation", e);
            return false;
        }
    }
    
    /**
     * 토큰에서 만료시간 추출
     */
    public LocalDateTime getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * 새로운 Access Token 생성 (Refresh Token 사용)
     */
    public String createAccessTokenFromRefresh(Long userId, String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(validity)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 토큰에서 Claims 추출 (예외 처리 강화)
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.debug("Failed to parse JWT claims: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 서명키 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}