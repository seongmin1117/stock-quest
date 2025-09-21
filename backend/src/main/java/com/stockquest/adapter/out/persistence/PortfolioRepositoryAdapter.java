package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.PortfolioPositionJpaEntity;
import com.stockquest.adapter.out.persistence.projection.PortfolioPositionSummaryImpl;
import com.stockquest.adapter.out.persistence.repository.PortfolioPositionJpaRepository;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 포트폴리오 저장소 어댑터
 * Domain PortfolioRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Slf4j
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
        log.debug("Finding active positions for userId: {}", userId);
        return jpaRepository.findActivePositionsByUserId(userId)
                .stream()
                .map(PortfolioPositionJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioPositionSummary> findPortfolioSummaryBySessionId(Long sessionId) {
        log.debug("Finding portfolio summary for sessionId: {}", sessionId);
        return jpaRepository.findPortfolioSummaryBySessionId(sessionId)
                .stream()
                .map(PortfolioPositionSummaryImpl::fromProjection)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioPositionSummary> findAllActivePortfolioSummaries() {
        log.debug("Finding all active portfolio summaries");
        return jpaRepository.findAllActivePortfolioSummaries()
                .stream()
                .map(PortfolioPositionSummaryImpl::fromProjection)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioPosition> findPositionChangesAfterTimestamp(Long sessionId, Instant timestamp) {
        log.debug("Finding position changes for sessionId: {} after timestamp: {}", sessionId, timestamp);
        // Convert Instant to LocalDateTime for JPA query
        LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
        return jpaRepository.findPositionChangesAfterTimestamp(sessionId, localDateTime)
                .stream()
                .map(PortfolioPositionJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}