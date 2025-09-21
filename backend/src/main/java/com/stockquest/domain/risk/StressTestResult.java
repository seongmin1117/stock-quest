package com.stockquest.domain.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 스트레스 테스트 결과 도메인 엔티티
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StressTestResult {

    private String testId;
    private String scenarioId;
    private Long portfolioId;
    private LocalDateTime executedAt;
    private Integer simulationRuns;
    private Double confidenceLevel;

    // 핵심 리스크 메트릭
    private BigDecimal worstCaseLoss;          // 최악의 경우 손실
    private BigDecimal expectedLoss;           // 기댓값 손실
    private BigDecimal valueAtRisk95;          // 95% VaR
    private BigDecimal valueAtRisk99;          // 99% VaR
    private BigDecimal conditionalVaR;         // CVaR (Expected Shortfall)
    private BigDecimal maximumDrawdown;        // 최대 낙폭
    private Integer drawdownDuration;          // 최대 낙폭 지속 기간

    // 시나리오별 상세 결과
    private Map<String, BigDecimal> scenarioLosses;     // 시나리오별 손실
    private Map<String, Double> scenarioProbabilities;  // 시나리오별 발생 확률
    private Map<String, BigDecimal> assetContributions; // 자산별 손실 기여도

    // 민감도 분석
    private Map<String, BigDecimal> deltaSensitivity;   // 가격 민감도
    private Map<String, BigDecimal> gammaSensitivity;   // 2차 민감도
    private Map<String, BigDecimal> vegaSensitivity;    // 변동성 민감도
    private Map<String, BigDecimal> thetaSensitivity;   // 시간 민감도

    // 포트폴리오 메트릭
    private BigDecimal portfolioValue;         // 포트폴리오 가치
    private BigDecimal riskAdjustedReturn;     // 위험조정수익률
    private Double sharpeRatio;                // 샤프 비율
    private Double sortinoRatio;               // 소르티노 비율
    private Double informationRatio;           // 정보 비율

    // 집중도 및 다각화
    private Double concentrationRisk;          // 집중도 위험
    private Double diversificationRatio;       // 다각화 비율
    private Integer effectiveAssetCount;       // 유효 자산 수

    // 유동성 분석
    private BigDecimal liquidationCost;        // 청산 비용
    private Integer liquidationTimeframe;      // 청산 소요 시간 (일)
    private Map<String, BigDecimal> liquidityScores; // 자산별 유동성 점수


    /**
     * 테스트 결과 유효성 검증
     */
    public void validate() {
        if (testId == null || testId.trim().isEmpty()) {
            throw new IllegalArgumentException("테스트 ID는 필수입니다");
        }
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("시나리오 ID는 필수입니다");
        }
        if (portfolioId == null || portfolioId <= 0) {
            throw new IllegalArgumentException("포트폴리오 ID는 유효해야 합니다");
        }
        if (simulationRuns != null && simulationRuns <= 0) {
            throw new IllegalArgumentException("시뮬레이션 횟수는 양수여야 합니다");
        }
        if (confidenceLevel != null && (confidenceLevel <= 0.0 || confidenceLevel >= 1.0)) {
            throw new IllegalArgumentException("신뢰 구간은 0과 1 사이여야 합니다");
        }
    }

    /**
     * 종합 리스크 점수 계산 (0-100)
     */
    public Integer calculateOverallRiskScore() {
        double score = 0.0;
        double weightSum = 0.0;

        // VaR 기여도 (30%)
        if (valueAtRisk99 != null && portfolioValue != null && portfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            double varRatio = valueAtRisk99.abs().divide(portfolioValue, 4, java.math.RoundingMode.HALF_UP).doubleValue();
            score += Math.min(varRatio * 1000, 100) * 0.30;
            weightSum += 0.30;
        }

        // 최대 낙폭 기여도 (25%)
        if (maximumDrawdown != null) {
            double ddRatio = maximumDrawdown.abs().doubleValue();
            score += Math.min(ddRatio * 100, 100) * 0.25;
            weightSum += 0.25;
        }

        // 집중도 위험 기여도 (20%)
        if (concentrationRisk != null) {
            score += Math.min(concentrationRisk * 100, 100) * 0.20;
            weightSum += 0.20;
        }

        // 유동성 위험 기여도 (15%)
        if (liquidationCost != null && portfolioValue != null && portfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            double liquidityRatio = liquidationCost.divide(portfolioValue, 4, java.math.RoundingMode.HALF_UP).doubleValue();
            score += Math.min(liquidityRatio * 500, 100) * 0.15;
            weightSum += 0.15;
        }

        // 샤프 비율 (역수, 10%)
        if (sharpeRatio != null && sharpeRatio > 0) {
            double sharpeScore = Math.max(0, 100 - (sharpeRatio * 20));
            score += sharpeScore * 0.10;
            weightSum += 0.10;
        }

        return weightSum > 0 ? Math.min((int) Math.round(score / weightSum), 100) : 0;
    }

    /**
     * 리스크 등급 계산
     */
    public String calculateRiskLevel() {
        Integer score = calculateOverallRiskScore();

        if (score >= 80) return "VERY_HIGH";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        if (score >= 20) return "LOW";
        return "VERY_LOW";
    }

    /**
     * 최대 허용 손실 대비 초과 여부 확인
     */
    public boolean exceedsRiskLimit(BigDecimal riskLimit) {
        if (riskLimit == null || valueAtRisk99 == null) {
            return false;
        }
        return valueAtRisk99.abs().compareTo(riskLimit) > 0;
    }

    /**
     * 시나리오별 손실 기여도 순위
     */
    public List<Map.Entry<String, BigDecimal>> getTopLossContributors(int topN) {
        if (assetContributions == null) {
            return List.of();
        }

        return assetContributions.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .toList();
    }

    /**
     * 다각화 효과 계산
     */
    public Double calculateDiversificationBenefit() {
        if (diversificationRatio == null) {
            return 0.0;
        }
        // 다각화 비율이 높을수록 리스크 감소 효과
        return Math.max(0.0, (diversificationRatio - 1.0) * 100);
    }

    /**
     * 예상 청산 시간 계산 (일)
     */
    public Integer calculateExpectedLiquidationTime() {
        if (liquidationTimeframe == null) {
            return Integer.MAX_VALUE;
        }

        // 집중도 위험이 높을수록 청산 시간 증가
        if (concentrationRisk != null && concentrationRisk > 0.5) {
            return (int) (liquidationTimeframe * (1 + concentrationRisk));
        }

        return liquidationTimeframe;
    }

    /**
     * 위기 상황 대응 권장사항
     */
    public List<String> getRecommendedActions() {
        List<String> actions = new java.util.ArrayList<>();

        Integer riskScore = calculateOverallRiskScore();

        if (riskScore >= 80) {
            actions.add("즉시 포지션 축소 검토");
            actions.add("헤지 전략 강화");
            actions.add("현금 비중 확대");
        } else if (riskScore >= 60) {
            actions.add("리스크 모니터링 강화");
            actions.add("포트폴리오 리밸런싱 고려");
            actions.add("방어적 자산 비중 증가");
        } else if (riskScore >= 40) {
            actions.add("정기적 리스크 검토");
            actions.add("다각화 수준 점검");
        }

        // 집중도 위험이 높은 경우
        if (concentrationRisk != null && concentrationRisk > 0.3) {
            actions.add("포트폴리오 집중도 완화");
            actions.add("추가 자산군 분산 투자");
        }

        // 유동성 위험이 높은 경우
        if (liquidationTimeframe != null && liquidationTimeframe > 30) {
            actions.add("유동성 높은 자산 비중 증가");
            actions.add("거래량 많은 종목으로 교체");
        }

        return actions;
    }
}