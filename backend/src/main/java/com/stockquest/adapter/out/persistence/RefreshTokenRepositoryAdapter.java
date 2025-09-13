package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import com.stockquest.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import com.stockquest.domain.auth.RefreshToken;
import com.stockquest.domain.auth.port.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 저장소 어댑터 구현체
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    
    private final RefreshTokenJpaRepository jpaRepository;
    
    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.fromDomain(refreshToken);
        RefreshTokenJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                           .map(RefreshTokenJpaEntity::toDomain);
    }
    
    @Override
    public List<RefreshToken> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                           .stream()
                           .map(RefreshTokenJpaEntity::toDomain)
                           .toList();
    }
    
    @Override
    public List<RefreshToken> findValidTokensByUserId(Long userId) {
        return jpaRepository.findValidTokensByUserId(userId, LocalDateTime.now())
                           .stream()
                           .map(RefreshTokenJpaEntity::toDomain)
                           .toList();
    }
    
    @Override
    public void revokeAllTokensByUserId(Long userId) {
        jpaRepository.revokeAllTokensByUserId(userId);
    }
    
    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }
    
    @Override
    public void revokeOtherTokensByUserId(Long userId, String currentToken) {
        jpaRepository.revokeOtherTokensByUserId(userId, currentToken);
    }
    
    @Override
    public void deleteExpiredTokens() {
        jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    @Override
    public long countActiveTokensByUserId(Long userId) {
        return jpaRepository.countActiveTokensByUserId(userId, LocalDateTime.now());
    }
    
    @Override
    public void deleteRevokedTokensBeforeDate(LocalDateTime cutoffDate) {
        jpaRepository.deleteRevokedTokensBeforeDate(cutoffDate);
    }
}