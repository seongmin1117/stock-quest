package com.stockquest.application.port.out;

import com.stockquest.domain.portfolio.RebalancingStrategy;

import java.util.List;
import java.util.Optional;

/**
 * 리밸런싱 전략 Repository Port
 * Phase 2.3: 비즈니스 로직 고도화 - 리밸런싱 전략 데이터 액세스
 */
public interface RebalancingStrategyRepositoryPort {

    /**
     * 리밸런싱 전략 저장
     */
    RebalancingStrategy save(RebalancingStrategy strategy);

    /**
     * ID로 리밸런싱 전략 조회
     */
    Optional<RebalancingStrategy> findById(Long id);

    /**
     * 사용자별 리밸런싱 전략 목록 조회
     */
    List<RebalancingStrategy> findByUserId(Long userId);

    /**
     * 활성화된 리밸런싱 전략 목록 조회
     */
    List<RebalancingStrategy> findActiveStrategies();

    /**
     * 리밸런싱 전략 삭제
     */
    void deleteById(Long id);

    /**
     * 전략 타입별 조회
     */
    List<RebalancingStrategy> findByType(RebalancingStrategy.RebalancingType type);

    /**
     * 주기별 조회
     */
    List<RebalancingStrategy> findByFrequency(RebalancingStrategy.RebalancingFrequency frequency);
}