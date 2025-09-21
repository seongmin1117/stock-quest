package com.stockquest.domain.risk;

/**
 * 리스크 시나리오 심각도
 */
public enum ScenarioSeverity {

    /**
     * 경미한 수준 - 일상적인 시장 변동성 내
     */
    MILD("경미", "일상적인 시장 변동성 내", 1.0, 2.0),

    /**
     * 보통 수준 - 주의가 필요한 변동성
     */
    MODERATE("보통", "주의가 필요한 변동성", 2.0, 3.5),

    /**
     * 심각한 수준 - 상당한 손실 가능성
     */
    SEVERE("심각", "상당한 손실 가능성", 3.5, 5.0),

    /**
     * 극심한 수준 - 대규모 손실 위험
     */
    EXTREME("극심", "대규모 손실 위험", 5.0, 8.0),

    /**
     * 재앙적 수준 - 파멸적 손실 가능성
     */
    CATASTROPHIC("재앙", "파멸적 손실 가능성", 8.0, Double.POSITIVE_INFINITY);

    private final String displayName;
    private final String description;
    private final Double minImpactMultiplier;
    private final Double maxImpactMultiplier;

    ScenarioSeverity(String displayName, String description,
                    Double minImpactMultiplier, Double maxImpactMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.minImpactMultiplier = minImpactMultiplier;
        this.maxImpactMultiplier = maxImpactMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Double getMinImpactMultiplier() {
        return minImpactMultiplier;
    }

    public Double getMaxImpactMultiplier() {
        return maxImpactMultiplier;
    }

    /**
     * 심각도별 권장 신뢰 구간
     */
    public Double getRecommendedConfidenceLevel() {
        return switch (this) {
            case MILD -> 0.90;
            case MODERATE -> 0.95;
            case SEVERE -> 0.99;
            case EXTREME -> 0.995;
            case CATASTROPHIC -> 0.999;
        };
    }

    /**
     * 심각도별 모니터링 빈도 (분)
     */
    public Integer getMonitoringFrequency() {
        return switch (this) {
            case MILD -> 60;          // 1시간
            case MODERATE -> 30;      // 30분
            case SEVERE -> 15;        // 15분
            case EXTREME -> 5;        // 5분
            case CATASTROPHIC -> 1;   // 1분
        };
    }

    /**
     * 심각도별 알림 임계값 (%)
     */
    public Double getAlertThreshold() {
        return switch (this) {
            case MILD -> 0.05;        // 5%
            case MODERATE -> 0.03;    // 3%
            case SEVERE -> 0.02;      // 2%
            case EXTREME -> 0.01;     // 1%
            case CATASTROPHIC -> 0.005; // 0.5%
        };
    }

    /**
     * 심각도 수치 점수 (0-100)
     */
    public Integer getSeverityScore() {
        return switch (this) {
            case MILD -> 20;
            case MODERATE -> 40;
            case SEVERE -> 60;
            case EXTREME -> 80;
            case CATASTROPHIC -> 100;
        };
    }

    /**
     * 최소 백테스트 기간 (일)
     */
    public Integer getMinBacktestPeriod() {
        return switch (this) {
            case MILD -> 90;          // 3개월
            case MODERATE -> 180;     // 6개월
            case SEVERE -> 365;       // 1년
            case EXTREME -> 730;      // 2년
            case CATASTROPHIC -> 1825; // 5년
        };
    }

    /**
     * 심각도에 따른 권장 대응 조치
     */
    public java.util.List<String> getRecommendedActions() {
        return switch (this) {
            case MILD -> java.util.List.of(
                "일반적 모니터링 유지",
                "정기적 리밸런싱"
            );
            case MODERATE -> java.util.List.of(
                "리스크 모니터링 강화",
                "포지션 크기 점검",
                "헤지 비율 검토"
            );
            case SEVERE -> java.util.List.of(
                "포지션 축소 고려",
                "방어적 자산 증가",
                "스톱로스 설정 강화",
                "유동성 확보"
            );
            case EXTREME -> java.util.List.of(
                "대규모 포지션 축소",
                "헤지 포지션 대폭 증가",
                "현금 비중 확대",
                "리스크 한도 재설정"
            );
            case CATASTROPHIC -> java.util.List.of(
                "포트폴리오 전면 재검토",
                "긴급 리스크 위원회 소집",
                "모든 비핵심 포지션 정리",
                "크라이시스 매뉴얼 활성화"
            );
        };
    }
}