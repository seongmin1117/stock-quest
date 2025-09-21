package com.stockquest.application.service;

import com.stockquest.domain.portfolio.*;
import com.stockquest.application.port.out.PortfolioRepositoryPort;
import com.stockquest.application.port.out.RebalancingStrategyRepositoryPort;
import com.stockquest.application.port.out.RebalancingActionRepositoryPort;
import com.stockquest.application.port.out.MarketDataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 포트폴리오 리밸런싱 애플리케이션 서비스
 * Phase 2.3: 비즈니스 로직 고도화 - 리밸런싱 워크플로우 오케스트레이션
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "stockquest.features.portfolio-rebalancing.enabled", havingValue = "true", matchIfMissing = false)
public class PortfolioRebalancingApplicationService {

    private final PortfolioRepositoryPort portfolioRepository;
    private final RebalancingStrategyRepositoryPort strategyRepository;
    private final RebalancingActionRepositoryPort actionRepository;
    private final MarketDataPort marketDataPort;
    private final PortfolioRebalancingService rebalancingService;

    /**
     * 포트폴리오 리밸런싱 제안 생성
     */
    @Transactional(readOnly = true)
    public RebalancingResult generateRebalancingProposal(Long portfolioId, Long strategyId) {
        log.info("포트폴리오 리밸런싱 제안 생성 시작: portfolioId={}, strategyId={}", portfolioId, strategyId);

        // 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + portfolioId));

        // 리밸런싱 전략 조회
        RebalancingStrategy strategy = strategyRepository.findById(strategyId)
            .orElseThrow(() -> new IllegalArgumentException("리밸런싱 전략을 찾을 수 없습니다: " + strategyId));

        // 현재 가격 정보 조회
        List<String> symbols = new ArrayList<>(strategy.getTargetWeights().keySet());
        Map<String, BigDecimal> currentPrices = marketDataPort.getCurrentPrices(symbols);

        // 리밸런싱 제안 생성
        RebalancingResult result = rebalancingService.generateRebalancingProposal(
            portfolio, strategy, currentPrices);

        log.info("리밸런싱 제안 생성 완료: portfolioId={}, actionCount={}, totalCost={}",
            portfolioId, result.getActionCount(), result.getTotalCost());

        return result;
    }

    /**
     * 자동 리밸런싱 실행 (예약된 작업)
     */
    public List<RebalancingResult> executeScheduledRebalancing() {
        log.info("예약된 자동 리밸런싱 실행 시작");

        List<RebalancingResult> results = new ArrayList<>();

        // 활성화된 리밸런싱 전략 조회
        List<RebalancingStrategy> activeStrategies = strategyRepository.findActiveStrategies();

        for (RebalancingStrategy strategy : activeStrategies) {
            try {
                List<Portfolio> portfolios = portfolioRepository.findByStrategyId(strategy.getId());

                for (Portfolio portfolio : portfolios) {
                    if (shouldExecuteRebalancing(portfolio, strategy)) {
                        RebalancingResult result = generateAndExecuteRebalancing(portfolio, strategy);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("자동 리밸런싱 실행 중 오류: strategyId={}", strategy.getId(), e);
            }
        }

        log.info("예약된 자동 리밸런싱 실행 완료: 처리된 포트폴리오 수={}", results.size());
        return results;
    }

    /**
     * 수동 리밸런싱 실행
     */
    public RebalancingResult executeManualRebalancing(Long portfolioId, Long strategyId, boolean forceExecution) {
        log.info("수동 리밸런싱 실행: portfolioId={}, strategyId={}, force={}", portfolioId, strategyId, forceExecution);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + portfolioId));

        RebalancingStrategy strategy = strategyRepository.findById(strategyId)
            .orElseThrow(() -> new IllegalArgumentException("리밸런싱 전략을 찾을 수 없습니다: " + strategyId));

        if (!forceExecution && !shouldExecuteRebalancing(portfolio, strategy)) {
            log.info("리밸런싱 실행 조건 미충족: portfolioId={}", portfolioId);
            return null;
        }

        return generateAndExecuteRebalancing(portfolio, strategy);
    }

    /**
     * 리밸런싱 액션 상태 업데이트
     */
    public void updateActionStatus(Long actionId, RebalancingAction.ActionStatus status, String reason) {
        log.info("리밸런싱 액션 상태 업데이트: actionId={}, status={}", actionId, status);

        RebalancingAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new IllegalArgumentException("리밸런싱 액션을 찾을 수 없습니다: " + actionId));

        RebalancingAction updatedAction = switch (status) {
            case EXECUTED -> action.markExecuted(LocalDateTime.now());
            case FAILED -> action.markFailed(reason);
            default -> action;
        };

        actionRepository.save(updatedAction);

        log.info("리밸런싱 액션 상태 업데이트 완료: actionId={}, newStatus={}", actionId, status);
    }

    /**
     * 포트폴리오별 리밸런싱 이력 조회
     */
    @Transactional(readOnly = true)
    public List<RebalancingAction> getRebalancingHistory(Long portfolioId, int limit) {
        return actionRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId, limit);
    }

    /**
     * 리밸런싱 성과 분석
     */
    @Transactional(readOnly = true)
    public RebalancingPerformanceAnalysis analyzeRebalancingPerformance(Long portfolioId, LocalDateTime from, LocalDateTime to) {
        log.info("리밸런싱 성과 분석: portfolioId={}, period={} to {}", portfolioId, from, to);

        List<RebalancingAction> actions = actionRepository.findByPortfolioIdAndExecutedAtBetween(portfolioId, from, to);

        return RebalancingPerformanceAnalysis.builder()
            .portfolioId(portfolioId)
            .analysisStart(from)
            .analysisEnd(to)
            .totalActions(actions.size())
            .executedActions((int) actions.stream().filter(a -> a.getStatus() == RebalancingAction.ActionStatus.EXECUTED).count())
            .totalTransactionCosts(calculateTotalTransactionCosts(actions))
            .totalTaxImpact(calculateTotalTaxImpact(actions))
            .averageExecutionTime(calculateAverageExecutionTime(actions))
            .successRate(calculateSuccessRate(actions))
            .build();
    }

    // =============== Private Helper Methods ===============

    /**
     * 리밸런싱 실행 여부 판단
     */
    private boolean shouldExecuteRebalancing(Portfolio portfolio, RebalancingStrategy strategy) {
        // 빈도 기반 실행 여부 확인
        if (strategy.getFrequency() != RebalancingStrategy.RebalancingFrequency.THRESHOLD_BASED) {
            LocalDateTime lastRebalancing = getLastRebalancingTime(portfolio.getId());
            if (lastRebalancing != null) {
                LocalDateTime nextScheduled = lastRebalancing.plusDays(strategy.getFrequency().getDays());
                if (LocalDateTime.now().isBefore(nextScheduled)) {
                    return false;
                }
            }
        }

        // 편차 기반 실행 여부 확인
        return strategy.requiresRebalancing(portfolio);
    }

    /**
     * 리밸런싱 생성 및 실행
     */
    private RebalancingResult generateAndExecuteRebalancing(Portfolio portfolio, RebalancingStrategy strategy) {
        try {
            // 리밸런싱 제안 생성
            RebalancingResult result = generateRebalancingProposal(portfolio.getId(), strategy.getId());

            if (result.getActionCount() == 0) {
                log.info("리밸런싱 액션 없음: portfolioId={}", portfolio.getId());
                return result;
            }

            // 높은 우선순위 액션만 실행
            List<RebalancingAction> highPriorityActions = result.getHighPriorityActions();

            for (RebalancingAction action : highPriorityActions) {
                try {
                    executeRebalancingAction(action);
                } catch (Exception e) {
                    log.error("리밸런싱 액션 실행 실패: actionId={}", action.getId(), e);
                    updateActionStatus(action.getId(), RebalancingAction.ActionStatus.FAILED, e.getMessage());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("리밸런싱 실행 중 오류: portfolioId={}", portfolio.getId(), e);
            throw new RuntimeException("리밸런싱 실행 실패", e);
        }
    }

    /**
     * 개별 리밸런싱 액션 실행 (모의 실행)
     */
    private void executeRebalancingAction(RebalancingAction action) {
        log.info("리밸런싱 액션 실행: symbol={}, type={}, quantity={}",
            action.getSymbol(), action.getActionType(), action.getQuantity());

        // 실제 구현에서는 주문 실행 서비스를 호출
        // 여기서는 모의 실행으로 성공 처리
        updateActionStatus(action.getId(), RebalancingAction.ActionStatus.EXECUTED, "모의 실행 완료");
    }

    /**
     * 마지막 리밸런싱 시간 조회
     */
    private LocalDateTime getLastRebalancingTime(Long portfolioId) {
        return actionRepository.findLatestExecutionTime(portfolioId);
    }

    /**
     * 총 거래 비용 계산
     */
    private BigDecimal calculateTotalTransactionCosts(List<RebalancingAction> actions) {
        return actions.stream()
            .map(RebalancingAction::getEstimatedTransactionCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 총 세금 영향 계산
     */
    private BigDecimal calculateTotalTaxImpact(List<RebalancingAction> actions) {
        return actions.stream()
            .map(RebalancingAction::getEstimatedTaxImpact)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 평균 실행 시간 계산 (분)
     */
    private BigDecimal calculateAverageExecutionTime(List<RebalancingAction> actions) {
        List<RebalancingAction> executedActions = actions.stream()
            .filter(a -> a.getStatus() == RebalancingAction.ActionStatus.EXECUTED)
            .filter(a -> a.getScheduledAt() != null && a.getExecutedAt() != null)
            .toList();

        if (executedActions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long totalMinutes = executedActions.stream()
            .mapToLong(action -> java.time.Duration.between(action.getScheduledAt(), action.getExecutedAt()).toMinutes())
            .sum();

        return BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(executedActions.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 성공률 계산
     */
    private BigDecimal calculateSuccessRate(List<RebalancingAction> actions) {
        if (actions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long executedCount = actions.stream()
            .filter(a -> a.getStatus() == RebalancingAction.ActionStatus.EXECUTED)
            .count();

        return BigDecimal.valueOf(executedCount)
            .divide(BigDecimal.valueOf(actions.size()), 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}

/**
 * 리밸런싱 성과 분석 결과
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class RebalancingPerformanceAnalysis {
    private Long portfolioId;
    private LocalDateTime analysisStart;
    private LocalDateTime analysisEnd;
    private int totalActions;
    private int executedActions;
    private BigDecimal totalTransactionCosts;
    private BigDecimal totalTaxImpact;
    private BigDecimal averageExecutionTime; // in minutes
    private BigDecimal successRate; // percentage
}