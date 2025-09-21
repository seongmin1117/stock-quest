package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.PortfolioPositionJpaEntity;
import com.stockquest.adapter.out.persistence.projection.PortfolioPositionSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 포지션 JPA 저장소
 */
@Repository
public interface PortfolioPositionJpaRepository extends JpaRepository<PortfolioPositionJpaEntity, Long> {

    List<PortfolioPositionJpaEntity> findBySessionId(Long sessionId);

    Optional<PortfolioPositionJpaEntity> findBySessionIdAndInstrumentKey(Long sessionId, String instrumentKey);

    @Query("SELECT p FROM PortfolioPositionJpaEntity p WHERE p.sessionId = ?1 AND p.quantity > 0")
    List<PortfolioPositionJpaEntity> findActivePositionsBySessionId(Long sessionId);

    /**
     * 사용자의 모든 활성 포지션 조회 (세션과 조인)
     * 성능 최적화를 위해 INNER JOIN 사용
     */
    @Query("SELECT p FROM PortfolioPositionJpaEntity p " +
           "INNER JOIN ChallengeSessionJpaEntity s ON p.sessionId = s.id " +
           "WHERE s.userId = :userId AND p.quantity > 0 " +
           "ORDER BY p.updatedAt DESC")
    List<PortfolioPositionJpaEntity> findActivePositionsByUserId(@Param("userId") Long userId);

    /**
     * 세션별 포트폴리오 요약 정보 조회 (프로젝션 사용)
     * 필요한 필드만 선택하여 성능 최적화
     */
    @Query("SELECT p.sessionId as sessionId, " +
           "s.userId as userId, " +
           "p.instrumentKey as instrumentKey, " +
           "p.quantity as currentQuantity, " +
           "p.averagePrice as averagePrice, " +
           "(p.quantity * p.averagePrice) as currentValue, " +
           "p.updatedAt as lastUpdated " +
           "FROM PortfolioPositionJpaEntity p " +
           "INNER JOIN ChallengeSessionJpaEntity s ON p.sessionId = s.id " +
           "WHERE p.sessionId = :sessionId AND p.quantity > 0 " +
           "ORDER BY p.totalCost DESC")
    List<PortfolioPositionSummaryProjection> findPortfolioSummaryBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 모든 활성 포트폴리오 요약 정보 조회
     * 리더보드 계산 등을 위한 배치 조회
     */
    @Query("SELECT p.sessionId as sessionId, " +
           "s.userId as userId, " +
           "p.instrumentKey as instrumentKey, " +
           "p.quantity as currentQuantity, " +
           "p.averagePrice as averagePrice, " +
           "(p.quantity * p.averagePrice) as currentValue, " +
           "p.updatedAt as lastUpdated " +
           "FROM PortfolioPositionJpaEntity p " +
           "INNER JOIN ChallengeSessionJpaEntity s ON p.sessionId = s.id " +
           "WHERE p.quantity > 0 AND s.status = 'ACTIVE' " +
           "ORDER BY s.userId, p.sessionId, p.totalCost DESC")
    List<PortfolioPositionSummaryProjection> findAllActivePortfolioSummaries();

    /**
     * 특정 시간 이후 변경된 포지션 조회
     * 실시간 업데이트를 위한 증분 조회
     */
    @Query("SELECT p FROM PortfolioPositionJpaEntity p " +
           "WHERE p.sessionId = :sessionId " +
           "AND p.updatedAt > :timestamp " +
           "ORDER BY p.updatedAt DESC")
    List<PortfolioPositionJpaEntity> findPositionChangesAfterTimestamp(
            @Param("sessionId") Long sessionId,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * 성능 최적화된 포지션 개수 조회
     */
    @Query("SELECT COUNT(p) FROM PortfolioPositionJpaEntity p " +
           "WHERE p.sessionId = :sessionId AND p.quantity > 0")
    long countActivePositionsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 세션의 총 포트폴리오 가치 계산
     */
    @Query("SELECT COALESCE(SUM(p.quantity * p.averagePrice), 0) " +
           "FROM PortfolioPositionJpaEntity p " +
           "WHERE p.sessionId = :sessionId AND p.quantity > 0")
    BigDecimal calculateTotalPortfolioValue(@Param("sessionId") Long sessionId);
}