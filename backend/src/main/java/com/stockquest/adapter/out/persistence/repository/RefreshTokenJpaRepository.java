package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken JPA 저장소
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    
    Optional<RefreshTokenJpaEntity> findByToken(String token);
    
    List<RefreshTokenJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT rt FROM RefreshTokenJpaEntity rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshTokenJpaEntity> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenJpaEntity rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllTokensByUserId(@Param("userId") Long userId);
    
    void deleteByToken(String token);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenJpaEntity rt SET rt.revoked = true WHERE rt.userId = :userId AND rt.token <> :currentToken")
    void revokeOtherTokensByUserId(@Param("userId") Long userId, @Param("currentToken") String currentToken);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenJpaEntity rt WHERE rt.expiresAt < :cutoffTime")
    void deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(rt) FROM RefreshTokenJpaEntity rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenJpaEntity rt WHERE rt.revoked = true AND rt.createdAt < :cutoffDate")
    void deleteRevokedTokensBeforeDate(@Param("cutoffDate") LocalDateTime cutoffDate);
}