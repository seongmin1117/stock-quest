package com.stockquest.domain.auth.port;

import com.stockquest.domain.auth.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token 저장소 포트
 * 토큰 관리 및 보안 정책 적용
 */
public interface RefreshTokenRepository {
    
    /**
     * 리프레시 토큰 저장
     */
    RefreshToken save(RefreshToken refreshToken);
    
    /**
     * 토큰으로 조회
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * 사용자 ID로 조회 (최신순)
     */
    List<RefreshToken> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 사용자의 유효한 토큰 조회
     */
    List<RefreshToken> findValidTokensByUserId(Long userId);
    
    /**
     * 사용자의 모든 토큰 폐기
     */
    void revokeAllTokensByUserId(Long userId);
    
    /**
     * 특정 토큰 삭제
     */
    void deleteByToken(String token);
    
    /**
     * 사용자의 특정 토큰을 제외한 모든 토큰 폐기
     * (새 토큰 발급 시 기존 토큰들 정리)
     */
    void revokeOtherTokensByUserId(Long userId, String currentToken);
    
    /**
     * 만료된 토큰 정리
     */
    void deleteExpiredTokens();
    
    /**
     * 사용자별 활성 토큰 수 조회
     */
    long countActiveTokensByUserId(Long userId);
    
    /**
     * 특정 날짜 이전의 폐기된 토큰 삭제
     */
    void deleteRevokedTokensBeforeDate(LocalDateTime cutoffDate);
}