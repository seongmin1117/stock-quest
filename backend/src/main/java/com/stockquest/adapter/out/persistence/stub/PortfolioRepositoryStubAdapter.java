package com.stockquest.adapter.out.persistence.stub;

import com.stockquest.application.port.out.PortfolioRepositoryPort;
import com.stockquest.domain.portfolio.Portfolio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Portfolio Repository Stub Adapter (임시 구현)
 *
 * PortfolioRepositoryPort 인터페이스의 스텁 구현체
 * 실제 JPA 구현이 완료될 때까지 애플리케이션 실행을 위한 임시 어댑터
 *
 * TODO: Portfolio JPA 엔터티 및 실제 리포지터리 구현 시 교체 필요
 */
@Slf4j
@Component
public class PortfolioRepositoryStubAdapter implements PortfolioRepositoryPort {

    private final ConcurrentHashMap<Long, Portfolio> inMemoryStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Portfolio save(Portfolio portfolio) {
        log.debug("Saving portfolio: {}", portfolio.getName());

        if (portfolio.getId() == null) {
            // 새로운 포트폴리오 생성
            Long newId = idGenerator.getAndIncrement();
            Portfolio newPortfolio = Portfolio.builder()
                    .id(newId)
                    .name(portfolio.getName())
                    .description(portfolio.getDescription())
                    .userId(portfolio.getUserId())
                    .totalValue(portfolio.getTotalValue())
                    .positions(portfolio.getPositions())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            inMemoryStore.put(newId, newPortfolio);
            return newPortfolio;
        } else {
            // 기존 포트폴리오 업데이트
            Portfolio updatedPortfolio = Portfolio.builder()
                    .id(portfolio.getId())
                    .name(portfolio.getName())
                    .description(portfolio.getDescription())
                    .userId(portfolio.getUserId())
                    .totalValue(portfolio.getTotalValue())
                    .positions(portfolio.getPositions())
                    .createdAt(portfolio.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            inMemoryStore.put(portfolio.getId(), updatedPortfolio);
            return updatedPortfolio;
        }
    }

    @Override
    public Optional<Portfolio> findById(Long id) {
        log.debug("Finding portfolio by id: {}", id);
        return Optional.ofNullable(inMemoryStore.get(id));
    }

    @Override
    public List<Portfolio> findByUserId(Long userId) {
        log.debug("Finding portfolios by userId: {}", userId);
        return inMemoryStore.values().stream()
                .filter(portfolio -> portfolio.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<Portfolio> findByStrategyId(Long strategyId) {
        log.debug("Finding portfolios by strategyId: {} (stub implementation)", strategyId);
        // 스텁 구현: 전략 ID 필터링 로직 없음
        return new ArrayList<>();
    }

    @Override
    public List<Portfolio> findActivePortfolios() {
        log.debug("Finding all active portfolios (stub implementation)");
        // 스텁 구현: 모든 포트폴리오를 활성으로 간주
        return new ArrayList<>(inMemoryStore.values());
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting portfolio by id: {}", id);
        inMemoryStore.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking existence of portfolio id: {}", id);
        return inMemoryStore.containsKey(id);
    }

    @Override
    public int countByUserId(Long userId) {
        log.debug("Counting portfolios for userId: {}", userId);
        return (int) inMemoryStore.values().stream()
                .filter(portfolio -> portfolio.getUserId().equals(userId))
                .count();
    }
}