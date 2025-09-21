package com.stockquest.domain.risk;

/**
 * 리스크 시나리오 유형
 */
public enum ScenarioType {

    /**
     * 블랙스완 이벤트 - 예측 불가능한 극단적 사건
     */
    BLACK_SWAN("블랙스완", "예측 불가능한 극단적 사건", 0.01),

    /**
     * 시장 급락 - 광범위한 시장 하락
     */
    MARKET_CRASH("시장급락", "광범위한 시장 하락", 0.05),

    /**
     * 상관관계 붕괴 - 자산 간 상관관계 급변
     */
    CORRELATION_BREAKDOWN("상관관계붕괴", "자산 간 상관관계 급변", 0.15),

    /**
     * 유동성 위기 - 시장 유동성 급격한 감소
     */
    LIQUIDITY_CRISIS("유동성위기", "시장 유동성 급격한 감소", 0.10),

    /**
     * 시스템적 리스크 - 금융시스템 전체 위험
     */
    SYSTEMIC_RISK("시스템위험", "금융시스템 전체 위험", 0.08),

    /**
     * 금리 충격 - 급격한 금리 변동
     */
    INTEREST_RATE_SHOCK("금리충격", "급격한 금리 변동", 0.20);

    private final String displayName;
    private final String description;
    private final Double typicalProbability;

    ScenarioType(String displayName, String description, Double typicalProbability) {
        this.displayName = displayName;
        this.description = description;
        this.typicalProbability = typicalProbability;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Double getTypicalProbability() {
        return typicalProbability;
    }

    /**
     * 시나리오 유형별 권장 분석 기간 (일)
     */
    public Integer getRecommendedAnalysisPeriod() {
        return switch (this) {
            case BLACK_SWAN -> 1825;      // 5년
            case MARKET_CRASH -> 1095;    // 3년
            case CORRELATION_BREAKDOWN -> 365;  // 1년
            case LIQUIDITY_CRISIS -> 180; // 6개월
            case SYSTEMIC_RISK -> 1095;   // 3년
            case INTEREST_RATE_SHOCK -> 730; // 2년
        };
    }

    /**
     * 시나리오 유형별 기본 변동성 배수
     */
    public Double getDefaultVolatilityMultiplier() {
        return switch (this) {
            case BLACK_SWAN -> 5.0;
            case MARKET_CRASH -> 3.0;
            case CORRELATION_BREAKDOWN -> 2.0;
            case LIQUIDITY_CRISIS -> 2.5;
            case SYSTEMIC_RISK -> 4.0;
            case INTEREST_RATE_SHOCK -> 2.2;
        };
    }

    /**
     * 시나리오 유형별 기본 스트레스 지속 기간 (일)
     */
    public Integer getDefaultStressDuration() {
        return switch (this) {
            case BLACK_SWAN -> 30;        // 1개월
            case MARKET_CRASH -> 180;     // 6개월
            case CORRELATION_BREAKDOWN -> 90;  // 3개월
            case LIQUIDITY_CRISIS -> 60;  // 2개월
            case SYSTEMIC_RISK -> 365;    // 1년
            case INTEREST_RATE_SHOCK -> 120; // 4개월
        };
    }
}