package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.PortfolioPositionJpaEntity;
import com.stockquest.adapter.out.persistence.repository.PortfolioPositionJpaRepository;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 포트폴리오 저장소 어댑터
 * Domain PortfolioRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class PortfolioRepositoryAdapter implements PortfolioRepository {

    private final PortfolioPositionJpaRepository jpaRepository;

    public PortfolioRepositoryAdapter(PortfolioPositionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PortfolioPosition save(PortfolioPosition position) {
        PortfolioPositionJpaEntity jpaEntity = PortfolioPositionJpaEntity.fromDomain(position);
        PortfolioPositionJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<PortfolioPosition> findById(Long id) {
        return jpaRepository.findById(id)
                .map(PortfolioPositionJpaEntity::toDomain);
    }

    @Override
    public List<PortfolioPosition> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId)
                .stream()
                .map(PortfolioPositionJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PortfolioPosition> findBySessionIdAndInstrumentKey(Long sessionId, String instrumentKey) {
        return jpaRepository.findBySessionIdAndInstrumentKey(sessionId, instrumentKey)
                .map(PortfolioPositionJpaEntity::toDomain);
    }

    @Override
    public List<PortfolioPosition> findActivePositionsBySessionId(Long sessionId) {
        return jpaRepository.findActivePositionsBySessionId(sessionId)
                .stream()
                .map(PortfolioPositionJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioPosition> findActivePositionsByUserId(Long userId) {
        // TODO: This requires a more complex query joining with session table
        // For now, returning empty list as a placeholder
        return List.of();
    }

    @Override
    public List<PortfolioPositionSummary> findPortfolioSummaryBySessionId(Long sessionId) {
        // TODO: This requires implementing the projection interface and query
        // For now, returning empty list as a placeholder
        return List.of();
    }

    @Override
    public List<PortfolioPositionSummary> findAllActivePortfolioSummaries() {
        // TODO: This requires implementing the projection interface and complex query
        // For now, returning empty list as a placeholder
        return List.of();
    }

    @Override
    public List<PortfolioPosition> findPositionChangesAfterTimestamp(Long sessionId, Instant timestamp) {
        // TODO: This requires adding timestamp filtering to JPA repository
        // For now, returning empty list as a placeholder
        return List.of();
    }
}