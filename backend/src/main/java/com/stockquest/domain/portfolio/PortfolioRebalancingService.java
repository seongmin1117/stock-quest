package com.stockquest.domain.portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 포트폴리오 리밸런싱 도메인 서비스
 * Phase 2.3: 비즈니스 로직 고도화 - 포트폴리오 자동 리밸런싱 알고리즘
 *
 * 핵심 기능:
 * - 동적 포트폴리오 최적화
 * - 세금 효율성 고려
 * - 거래 비용 최소화
 * - 리스크 조정 수익률 최대화
 */
public class PortfolioRebalancingService {

    private static final BigDecimal DEFAULT_TRANSACTION_COST_RATE = BigDecimal.valueOf(0.0025); // 0.25%
    private static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.valueOf(0.22); // 22%
    private static final BigDecimal MINIMUM_TRADE_THRESHOLD = BigDecimal.valueOf(1000); // 최소 거래 금액
    private static final BigDecimal PRICE_IMPACT_FACTOR = BigDecimal.valueOf(0.001); // 가격 영향 계수

    /**
     * 포트폴리오 리밸런싱 제안 생성
     */
    public RebalancingResult generateRebalancingProposal(
            Portfolio portfolio,
            RebalancingStrategy strategy,
            Map<String, BigDecimal> currentPrices) {

        validateInputs(portfolio, strategy, currentPrices);

        if (!strategy.requiresRebalancing(portfolio)) {
            return createNoActionResult(portfolio, strategy);
        }

        List<RebalancingAction> actions = calculateRebalancingActions(
            portfolio, strategy, currentPrices);

        // 세금 최적화 적용
        if (strategy.isTaxOptimized()) {
            actions = optimizeForTaxes(actions, portfolio);
        }

        // 거래 비용 최적화
        if (strategy.isConsiderTransactionCosts()) {
            actions = optimizeForTransactionCosts(actions, strategy.getMinimumTradeAmount());
        }

        return RebalancingResult.builder()
            .portfolioId(portfolio.getId())
            .strategyId(strategy.getId())
            .actions(actions)
            .totalTransactionCost(calculateTotalTransactionCost(actions))
            .totalTaxImpact(calculateTotalTaxImpact(actions, portfolio))
            .expectedImprovementScore(calculateImprovementScore(portfolio, strategy, actions))
            .weightChanges(calculateWeightChanges(portfolio, strategy, actions))
            .proposedAt(LocalDateTime.now())
            .status(RebalancingResult.RebalancingResultStatus.PROPOSED)
            .build();
    }

    /**
     * 리밸런싱 액션 계산
     */
    private List<RebalancingAction> calculateRebalancingActions(
            Portfolio portfolio,
            RebalancingStrategy strategy,
            Map<String, BigDecimal> currentPrices) {

        Map<String, BigDecimal> currentWeights = portfolio.getWeights();
        Map<String, BigDecimal> targetWeights = strategy.getTargetWeights();
        Map<String, BigDecimal> deviations = strategy.calculateDeviations(portfolio);

        List<RebalancingAction> actions = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : targetWeights.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal targetWeight = entry.getValue();
            BigDecimal currentWeight = currentWeights.getOrDefault(symbol, BigDecimal.ZERO);
            BigDecimal deviation = deviations.get(symbol);

            // 편차가 임계치를 초과하는 경우에만 액션 생성
            if (deviation.abs().compareTo(strategy.getToleranceThreshold()) > 0) {
                RebalancingAction action = createRebalancingAction(
                    portfolio, strategy, symbol, currentWeight, targetWeight,
                    deviation, currentPrices.get(symbol));

                if (action != null) {
                    actions.add(action);
                }
            }
        }

        // 우선순위 정렬 (편차가 큰 순서)
        return actions.stream()
            .sorted((a, b) -> b.getWeightDeviation().abs().compareTo(a.getWeightDeviation().abs()))
            .collect(Collectors.toList());
    }

    /**
     * 개별 리밸런싱 액션 생성
     */
    private RebalancingAction createRebalancingAction(
            Portfolio portfolio,
            RebalancingStrategy strategy,
            String symbol,
            BigDecimal currentWeight,
            BigDecimal targetWeight,
            BigDecimal deviation,
            BigDecimal currentPrice) {

        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal portfolioValue = portfolio.getTotalValue();
        BigDecimal targetValue = portfolioValue.multiply(targetWeight);
        BigDecimal currentValue = portfolioValue.multiply(currentWeight);
        BigDecimal valueChange = targetValue.subtract(currentValue);

        // 거래량 계산
        BigDecimal quantity = valueChange.divide(currentPrice, 0, java.math.RoundingMode.HALF_UP);

        // 최소 거래 금액 확인
        if (quantity.multiply(currentPrice).abs().compareTo(strategy.getMinimumTradeAmount()) < 0) {
            return null;
        }

        RebalancingAction.ActionType actionType = quantity.compareTo(BigDecimal.ZERO) > 0
            ? RebalancingAction.ActionType.BUY
            : RebalancingAction.ActionType.SELL;

        return RebalancingAction.builder()
            .portfolioId(portfolio.getId())
            .strategyId(strategy.getId())
            .symbol(symbol)
            .actionType(actionType)
            .quantity(quantity.abs())
            .targetPrice(currentPrice)
            .currentWeight(currentWeight)
            .targetWeight(targetWeight)
            .weightDeviation(deviation)
            .estimatedTransactionCost(calculateTransactionCost(quantity, currentPrice))
            .estimatedTaxImpact(calculateTaxImpact(quantity, currentPrice, actionType))
            .priority(calculatePriority(deviation, strategy.getToleranceThreshold()))
            .status(RebalancingAction.ActionStatus.PENDING)
            .reason(generateActionReason(deviation, targetWeight, currentWeight))
            .scheduledAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * 세금 최적화
     */
    private List<RebalancingAction> optimizeForTaxes(List<RebalancingAction> actions, Portfolio portfolio) {
        // 단기 보유 포지션의 매도를 가능한 피하고, 장기 보유 포지션의 매도를 우선
        return actions.stream()
            .map(action -> {
                if (action.getActionType() == RebalancingAction.ActionType.SELL) {
                    Position position = portfolio.getPosition(action.getSymbol());
                    if (position != null && isShortTermHolding(position)) {
                        // 단기 보유 포지션은 우선순위를 낮춤
                        return RebalancingAction.builder()
                            .id(action.getId())
                            .portfolioId(action.getPortfolioId())
                            .strategyId(action.getStrategyId())
                            .symbol(action.getSymbol())
                            .actionType(action.getActionType())
                            .quantity(action.getQuantity())
                            .targetPrice(action.getTargetPrice())
                            .currentWeight(action.getCurrentWeight())
                            .targetWeight(action.getTargetWeight())
                            .weightDeviation(action.getWeightDeviation())
                            .estimatedTransactionCost(action.getEstimatedTransactionCost())
                            .estimatedTaxImpact(action.getEstimatedTaxImpact().multiply(BigDecimal.valueOf(1.5))) // 세금 증가
                            .priority(reducePriority(action.getPriority()))
                            .status(action.getStatus())
                            .reason(action.getReason() + " (세금최적화: 단기보유)")
                            .scheduledAt(action.getScheduledAt())
                            .createdAt(action.getCreatedAt())
                            .build();
                    }
                }
                return action;
            })
            .collect(Collectors.toList());
    }

    /**
     * 거래 비용 최적화
     */
    private List<RebalancingAction> optimizeForTransactionCosts(
            List<RebalancingAction> actions,
            BigDecimal minimumTradeAmount) {

        return actions.stream()
            .filter(action -> {
                BigDecimal tradeAmount = action.getQuantity().multiply(action.getTargetPrice());
                return tradeAmount.compareTo(minimumTradeAmount) >= 0;
            })
            .collect(Collectors.toList());
    }

    /**
     * 거래 비용 계산
     */
    private BigDecimal calculateTransactionCost(BigDecimal quantity, BigDecimal price) {
        BigDecimal tradeValue = quantity.abs().multiply(price);
        return tradeValue.multiply(DEFAULT_TRANSACTION_COST_RATE);
    }

    /**
     * 세금 영향 계산
     */
    private BigDecimal calculateTaxImpact(BigDecimal quantity, BigDecimal price, RebalancingAction.ActionType actionType) {
        if (actionType != RebalancingAction.ActionType.SELL) {
            return BigDecimal.ZERO;
        }

        // 매도시에만 양도소득세 적용 (간소화된 계산)
        BigDecimal sellValue = quantity.abs().multiply(price);
        BigDecimal estimatedGain = sellValue.multiply(BigDecimal.valueOf(0.1)); // 10% 추정 수익률
        return estimatedGain.multiply(DEFAULT_TAX_RATE);
    }

    /**
     * 액션 우선순위 계산
     */
    private RebalancingAction.ActionPriority calculatePriority(BigDecimal deviation, BigDecimal threshold) {
        BigDecimal deviationRatio = deviation.abs().divide(threshold, 2, java.math.RoundingMode.HALF_UP);

        if (deviationRatio.compareTo(BigDecimal.valueOf(3.0)) >= 0) {
            return RebalancingAction.ActionPriority.CRITICAL;
        } else if (deviationRatio.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
            return RebalancingAction.ActionPriority.HIGH;
        } else if (deviationRatio.compareTo(BigDecimal.valueOf(1.5)) >= 0) {
            return RebalancingAction.ActionPriority.MEDIUM;
        } else {
            return RebalancingAction.ActionPriority.LOW;
        }
    }

    /**
     * 액션 사유 생성
     */
    private String generateActionReason(BigDecimal deviation, BigDecimal targetWeight, BigDecimal currentWeight) {
        String direction = deviation.compareTo(BigDecimal.ZERO) > 0 ? "과소" : "과다";
        return String.format("목표비중 %s%%에서 현재비중 %s%% (%s배분 %s%%)",
            targetWeight.multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP),
            currentWeight.multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP),
            direction,
            deviation.abs().multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP));
    }

    /**
     * 총 거래 비용 계산
     */
    private BigDecimal calculateTotalTransactionCost(List<RebalancingAction> actions) {
        return actions.stream()
            .map(RebalancingAction::getEstimatedTransactionCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 총 세금 영향 계산
     */
    private BigDecimal calculateTotalTaxImpact(List<RebalancingAction> actions, Portfolio portfolio) {
        return actions.stream()
            .map(RebalancingAction::getEstimatedTaxImpact)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 개선 점수 계산
     */
    private BigDecimal calculateImprovementScore(Portfolio portfolio, RebalancingStrategy strategy, List<RebalancingAction> actions) {
        BigDecimal currentDeviation = strategy.getMaxDeviation(portfolio);
        BigDecimal totalCosts = calculateTotalTransactionCost(actions)
            .add(calculateTotalTaxImpact(actions, portfolio));

        // 편차 개선 대비 비용 비율로 점수 계산
        if (totalCosts.compareTo(BigDecimal.ZERO) == 0) {
            return currentDeviation;
        }

        return currentDeviation.divide(totalCosts, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 비중 변화 계산
     */
    private Map<String, BigDecimal> calculateWeightChanges(Portfolio portfolio, RebalancingStrategy strategy, List<RebalancingAction> actions) {
        Map<String, BigDecimal> currentWeights = portfolio.getWeights();
        Map<String, BigDecimal> targetWeights = strategy.getTargetWeights();

        return targetWeights.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    String symbol = entry.getKey();
                    BigDecimal target = entry.getValue();
                    BigDecimal current = currentWeights.getOrDefault(symbol, BigDecimal.ZERO);
                    return target.subtract(current);
                }
            ));
    }

    /**
     * 액션이 필요없는 경우의 결과 생성
     */
    private RebalancingResult createNoActionResult(Portfolio portfolio, RebalancingStrategy strategy) {
        return RebalancingResult.builder()
            .portfolioId(portfolio.getId())
            .strategyId(strategy.getId())
            .actions(List.of())
            .totalTransactionCost(BigDecimal.ZERO)
            .totalTaxImpact(BigDecimal.ZERO)
            .expectedImprovementScore(BigDecimal.ZERO)
            .weightChanges(Map.of())
            .proposedAt(LocalDateTime.now())
            .status(RebalancingResult.RebalancingResultStatus.PROPOSED)
            .build();
    }

    /**
     * 입력값 검증
     */
    private void validateInputs(Portfolio portfolio, RebalancingStrategy strategy, Map<String, BigDecimal> currentPrices) {
        if (portfolio == null) {
            throw new IllegalArgumentException("포트폴리오는 필수입니다");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("리밸런싱 전략은 필수입니다");
        }
        if (!strategy.isValid()) {
            throw new IllegalArgumentException("유효하지 않은 리밸런싱 전략입니다");
        }
        if (currentPrices == null || currentPrices.isEmpty()) {
            throw new IllegalArgumentException("현재 가격 정보는 필수입니다");
        }
    }

    /**
     * 단기 보유 여부 확인 (1년 미만)
     */
    private boolean isShortTermHolding(Position position) {
        if (position.getOpenDate() == null) {
            return true; // 정보가 없으면 단기로 간주
        }
        return position.getOpenDate().isAfter(java.time.LocalDate.now().minusYears(1));
    }

    /**
     * 우선순위 낮춤
     */
    private RebalancingAction.ActionPriority reducePriority(RebalancingAction.ActionPriority current) {
        return switch (current) {
            case CRITICAL -> RebalancingAction.ActionPriority.HIGH;
            case HIGH -> RebalancingAction.ActionPriority.MEDIUM;
            case MEDIUM -> RebalancingAction.ActionPriority.LOW;
            case LOW, OPTIONAL -> RebalancingAction.ActionPriority.OPTIONAL;
        };
    }
}