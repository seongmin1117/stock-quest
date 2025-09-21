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
 * 리스크 시나리오 도메인 엔티티
 * 블랙스완 이벤트, 시장 충격 등의 시나리오를 모델링
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScenario {

    private String scenarioId;
    private String name;
    private String description;
    private ScenarioType type;
    private ScenarioSeverity severity;
    private Double probability;
    private LocalDateTime createdAt;
    private LocalDateTime validUntil;

    // 시나리오 파라미터
    private Map<String, BigDecimal> marketShocks;      // 시장별 충격 크기
    private Map<String, BigDecimal> correlationBreakdown; // 상관관계 변화
    private BigDecimal volatilityMultiplier;           // 변동성 배수
    private BigDecimal liquidityImpact;                // 유동성 영향
    private Integer stressDuration;                    // 지속 기간 (일)

    // 역사적 기준점
    private LocalDateTime historicalStart;
    private LocalDateTime historicalEnd;
    private String historicalDescription;


    /**
     * 시나리오 유효성 검증
     */
    public void validate() {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("시나리오 ID는 필수입니다");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("시나리오 이름은 필수입니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("시나리오 타입은 필수입니다");
        }
        if (severity == null) {
            throw new IllegalArgumentException("시나리오 심각도는 필수입니다");
        }
        if (probability != null && (probability < 0.0 || probability > 1.0)) {
            throw new IllegalArgumentException("발생 확률은 0과 1 사이여야 합니다");
        }
        if (stressDuration != null && stressDuration <= 0) {
            throw new IllegalArgumentException("스트레스 지속 기간은 양수여야 합니다");
        }
    }

    /**
     * 시장 충격 적용
     */
    public BigDecimal applyMarketShock(String market, BigDecimal originalValue) {
        if (marketShocks == null || !marketShocks.containsKey(market)) {
            return originalValue;
        }

        BigDecimal shock = marketShocks.get(market);
        return originalValue.multiply(BigDecimal.ONE.add(shock));
    }

    /**
     * 변동성 충격 적용
     */
    public BigDecimal applyVolatilityShock(BigDecimal originalVolatility) {
        if (volatilityMultiplier == null) {
            return originalVolatility;
        }
        return originalVolatility.multiply(volatilityMultiplier);
    }

    /**
     * 상관관계 변화 적용
     */
    public BigDecimal applyCorrelationShock(String assetPair, BigDecimal originalCorrelation) {
        if (correlationBreakdown == null || !correlationBreakdown.containsKey(assetPair)) {
            return originalCorrelation;
        }

        BigDecimal breakdown = correlationBreakdown.get(assetPair);
        return originalCorrelation.add(breakdown);
    }

    /**
     * 유동성 영향 계산
     */
    public BigDecimal calculateLiquidityImpact(BigDecimal tradingVolume) {
        if (liquidityImpact == null) {
            return BigDecimal.ZERO;
        }

        // 거래량에 비례한 유동성 영향
        return tradingVolume.multiply(liquidityImpact);
    }

    /**
     * 시나리오 강도 점수 계산 (0-100)
     */
    public Integer calculateIntensityScore() {
        double score = 0.0;

        // 심각도 기본 점수
        switch (severity) {
            case MILD -> score += 20;
            case MODERATE -> score += 40;
            case SEVERE -> score += 60;
            case EXTREME -> score += 80;
            case CATASTROPHIC -> score += 100;
        }

        // 변동성 배수 가중치
        if (volatilityMultiplier != null) {
            double volImpact = Math.min((volatilityMultiplier.doubleValue() - 1.0) * 50, 20);
            score += volImpact;
        }

        // 지속 기간 가중치
        if (stressDuration != null) {
            double durationImpact = Math.min(stressDuration / 30.0 * 10, 10);
            score += durationImpact;
        }

        return Math.min((int) Math.round(score), 100);
    }

    /**
     * 시나리오 유형별 권장 대응 전략
     */
    public List<String> getRecommendedMitigationStrategies() {
        return switch (type) {
            case BLACK_SWAN -> List.of(
                "포트폴리오 다각화 강화",
                "헤지 포지션 증가",
                "현금 비중 확대",
                "극단값 위험 모니터링"
            );
            case MARKET_CRASH -> List.of(
                "방어형 자산 비중 증가",
                "베타 중립 전략",
                "변동성 헤지",
                "유동성 확보"
            );
            case CORRELATION_BREAKDOWN -> List.of(
                "자산군별 분산 투자",
                "대안 투자 고려",
                "다이나믹 헤지",
                "리스크 패리티 전략"
            );
            case LIQUIDITY_CRISIS -> List.of(
                "유동성 프리미엄 확보",
                "거래 규모 축소",
                "주문 분할 실행",
                "대체 거래소 활용"
            );
            case SYSTEMIC_RISK -> List.of(
                "시스템적 리스크 헤지",
                "카운터파티 리스크 관리",
                "규제 변화 대응",
                "시나리오 기반 계획"
            );
            case INTEREST_RATE_SHOCK -> List.of(
                "듀레이션 조정",
                "금리 헤지",
                "플로팅 레이트 자산",
                "인플레이션 보호"
            );
        };
    }

    /**
     * 시나리오가 만료되었는지 확인
     */
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }
}