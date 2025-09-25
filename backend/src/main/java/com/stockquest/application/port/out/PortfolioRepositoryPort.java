package com.stockquest.application.port.out;

import com.stockquest.domain.portfolio.Portfolio;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 Repository Port
 * Phase 2.3: 비즈니스 로직 고도화 - 포트폴리오 데이터 액세스 확장
 */
public interface PortfolioRepositoryPort {

    /**
     * 포트폴리오 저장
     */
    Portfolio save(Portfolio portfolio);

    /**
     * ID로 포트폴리오 조회
     */
    Optional<Portfolio> findById(Long id);

    /**
     * 사용자별 포트폴리오 목록 조회
     */
    List<Portfolio> findByUserId(Long userId);

    /**
     * 전략별 포트폴리오 조회 (리밸런싱 전략 적용된 포트폴리오)
     */
    List<Portfolio> findByStrategyId(Long strategyId);

    /**
     * 활성 포트폴리오 조회
     */
    List<Portfolio> findActivePortfolios();

    /**
     * 포트폴리오 삭제
     */
    void deleteById(Long id);

    /**
     * 포트폴리오 존재 여부 확인
     */
    boolean existsById(Long id);

    /**
     * 사용자의 포트폴리오 개수 조회
     */
    int countByUserId(Long userId);
}