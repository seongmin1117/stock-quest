package com.stockquest.application.port.out;

import com.stockquest.domain.portfolio.RebalancingAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 리밸런싱 액션 Repository Port
 * Phase 2.3: 비즈니스 로직 고도화 - 리밸런싱 액션 데이터 액세스
 */
public interface RebalancingActionRepositoryPort {

    /**
     * 리밸런싱 액션 저장
     */
    RebalancingAction save(RebalancingAction action);

    /**
     * ID로 리밸런싱 액션 조회
     */
    Optional<RebalancingAction> findById(Long id);

    /**
     * 포트폴리오별 리밸런싱 액션 이력 조회 (최신순)
     */
    List<RebalancingAction> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId, int limit);

    /**
     * 기간별 리밸런싱 액션 조회
     */
    List<RebalancingAction> findByPortfolioIdAndExecutedAtBetween(Long portfolioId, LocalDateTime from, LocalDateTime to);

    /**
     * 상태별 리밸런싱 액션 조회
     */
    List<RebalancingAction> findByStatus(RebalancingAction.ActionStatus status);

    /**
     * 우선순위별 리밸런싱 액션 조회
     */
    List<RebalancingAction> findByPriority(RebalancingAction.ActionPriority priority);

    /**
     * 실행 대기 중인 액션 조회
     */
    List<RebalancingAction> findPendingActions();

    /**
     * 예약된 액션 조회 (특정 시간 이전)
     */
    List<RebalancingAction> findScheduledActionsBefore(LocalDateTime scheduledTime);

    /**
     * 마지막 실행 시간 조회
     */
    LocalDateTime findLatestExecutionTime(Long portfolioId);

    /**
     * 전략별 액션 조회
     */
    List<RebalancingAction> findByStrategyId(Long strategyId);

    /**
     * 심볼별 액션 조회
     */
    List<RebalancingAction> findBySymbol(String symbol);

    /**
     * 리밸런싱 액션 삭제
     */
    void deleteById(Long id);

    /**
     * 오래된 완료된 액션 정리 (보관 기간 초과)
     */
    int deleteCompletedActionsBefore(LocalDateTime cutoffDate);
}