package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.PortfolioPositionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
}