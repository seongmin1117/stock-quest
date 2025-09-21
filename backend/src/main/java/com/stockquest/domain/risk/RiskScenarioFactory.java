package com.stockquest.domain.risk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 리스크 시나리오 팩토리
 * 다양한 유형의 리스크 시나리오를 생성하는 팩토리 클래스
 */
public class RiskScenarioFactory {

    /**
     * 2008년 금융위기 시나리오 생성
     */
    public static RiskScenario create2008FinancialCrisisScenario() {
        return RiskScenario.builder()
                .scenarioId("CRISIS_2008_" + UUID.randomUUID().toString().substring(0, 8))
                .name("2008년 금융위기 재현")
                .description("서브프라임 모기지 사태로 인한 글로벌 금융위기 시나리오")
                .type(ScenarioType.SYSTEMIC_RISK)
                .severity(ScenarioSeverity.EXTREME)
                .probability(0.02)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusYears(1))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.40),      // 40% 하락
                        "FINANCIAL", BigDecimal.valueOf(-0.60),    // 금융주 60% 하락
                        "REAL_ESTATE", BigDecimal.valueOf(-0.50),  // 부동산 50% 하락
                        "COMMODITIES", BigDecimal.valueOf(-0.30)   // 원자재 30% 하락
                ))
                .correlationBreakdown(Map.of(
                        "STOCK_BOND", BigDecimal.valueOf(-0.3),    // 주식-채권 상관관계 하락
                        "GLOBAL_MARKETS", BigDecimal.valueOf(0.5)  // 글로벌 시장 상관관계 증가
                ))
                .volatilityMultiplier(BigDecimal.valueOf(4.0))
                .liquidityImpact(BigDecimal.valueOf(0.15))
                .stressDuration(540)  // 18개월
                .historicalStart(LocalDateTime.of(2007, 7, 1, 0, 0))
                .historicalEnd(LocalDateTime.of(2009, 3, 1, 0, 0))
                .historicalDescription("서브프라임 모기지 사태 시작부터 시장 바닥까지")
                .build();
    }

    /**
     * COVID-19 팬데믹 시나리오 생성
     */
    public static RiskScenario createCovid19PandemicScenario() {
        return RiskScenario.builder()
                .scenarioId("COVID19_" + UUID.randomUUID().toString().substring(0, 8))
                .name("COVID-19 팬데믹 재현")
                .description("코로나19 팬데믹으로 인한 글로벌 경제 봉쇄 시나리오")
                .type(ScenarioType.BLACK_SWAN)
                .severity(ScenarioSeverity.SEVERE)
                .probability(0.01)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusYears(1))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.35),      // 35% 하락
                        "TRAVEL", BigDecimal.valueOf(-0.70),       // 여행업 70% 하락
                        "HOSPITALITY", BigDecimal.valueOf(-0.65),  // 호스피탈리티 65% 하락
                        "TECH", BigDecimal.valueOf(0.20),          // 기술주 20% 상승
                        "HEALTHCARE", BigDecimal.valueOf(0.15)     // 헬스케어 15% 상승
                ))
                .correlationBreakdown(Map.of(
                        "SECTOR_CORRELATION", BigDecimal.valueOf(-0.4) // 섹터 간 상관관계 급격히 변화
                ))
                .volatilityMultiplier(BigDecimal.valueOf(3.5))
                .liquidityImpact(BigDecimal.valueOf(0.12))
                .stressDuration(90)  // 3개월
                .historicalStart(LocalDateTime.of(2020, 2, 1, 0, 0))
                .historicalEnd(LocalDateTime.of(2020, 5, 1, 0, 0))
                .historicalDescription("코로나19 확산과 글로벌 봉쇄 조치")
                .build();
    }

    /**
     * 블랙 먼데이 시나리오 생성 (1987년 10월 19일)
     */
    public static RiskScenario createBlackMondayScenario() {
        return RiskScenario.builder()
                .scenarioId("BLACK_MONDAY_" + UUID.randomUUID().toString().substring(0, 8))
                .name("1987년 블랙 먼데이")
                .description("1987년 10월 19일 주식시장 대폭락 시나리오")
                .type(ScenarioType.MARKET_CRASH)
                .severity(ScenarioSeverity.EXTREME)
                .probability(0.005)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusYears(1))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.22),      // 22% 하락 (1일)
                        "US_STOCKS", BigDecimal.valueOf(-0.23),    // 미국 23% 하락
                        "GLOBAL_STOCKS", BigDecimal.valueOf(-0.25) // 글로벌 25% 하락
                ))
                .correlationBreakdown(Map.of(
                        "GLOBAL_CORRELATION", BigDecimal.valueOf(0.7) // 글로벌 동조화 급증
                ))
                .volatilityMultiplier(BigDecimal.valueOf(8.0))
                .liquidityImpact(BigDecimal.valueOf(0.25))
                .stressDuration(1)    // 1일
                .historicalStart(LocalDateTime.of(1987, 10, 19, 0, 0))
                .historicalEnd(LocalDateTime.of(1987, 10, 19, 23, 59))
                .historicalDescription("프로그램 매매와 시장 유동성 부족으로 인한 급락")
                .build();
    }

    /**
     * 유동성 위기 시나리오 생성
     */
    public static RiskScenario createLiquidityCrisisScenario() {
        return RiskScenario.builder()
                .scenarioId("LIQUIDITY_CRISIS_" + UUID.randomUUID().toString().substring(0, 8))
                .name("유동성 위기 시나리오")
                .description("시장 유동성 급격한 감소와 거래 중단 위험")
                .type(ScenarioType.LIQUIDITY_CRISIS)
                .severity(ScenarioSeverity.SEVERE)
                .probability(0.08)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMonths(6))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.15),      // 15% 하락
                        "SMALL_CAP", BigDecimal.valueOf(-0.25),    // 소형주 25% 하락
                        "EMERGING", BigDecimal.valueOf(-0.30)      // 신흥시장 30% 하락
                ))
                .correlationBreakdown(Map.of(
                        "SIZE_FACTOR", BigDecimal.valueOf(0.3)     // 시가총액별 차별화 심화
                ))
                .volatilityMultiplier(BigDecimal.valueOf(2.5))
                .liquidityImpact(BigDecimal.valueOf(0.35))         // 높은 유동성 영향
                .stressDuration(60)   // 2개월
                .build();
    }

    /**
     * 금리 급등 시나리오 생성
     */
    public static RiskScenario createInterestRateShockScenario() {
        return RiskScenario.builder()
                .scenarioId("RATE_SHOCK_" + UUID.randomUUID().toString().substring(0, 8))
                .name("급격한 금리 상승")
                .description("중앙은행 정책 변화로 인한 급격한 금리 상승")
                .type(ScenarioType.INTEREST_RATE_SHOCK)
                .severity(ScenarioSeverity.MODERATE)
                .probability(0.15)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMonths(12))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.10),      // 10% 하락
                        "GROWTH_STOCKS", BigDecimal.valueOf(-0.20), // 성장주 20% 하락
                        "REIT", BigDecimal.valueOf(-0.15),         // 리츠 15% 하락
                        "BONDS", BigDecimal.valueOf(-0.08),        // 채권 8% 하락
                        "FINANCIALS", BigDecimal.valueOf(0.05)     // 금융주 5% 상승
                ))
                .volatilityMultiplier(BigDecimal.valueOf(2.2))
                .liquidityImpact(BigDecimal.valueOf(0.08))
                .stressDuration(120)  // 4개월
                .build();
    }

    /**
     * 상관관계 붕괴 시나리오 생성
     */
    public static RiskScenario createCorrelationBreakdownScenario() {
        return RiskScenario.builder()
                .scenarioId("CORRELATION_BREAKDOWN_" + UUID.randomUUID().toString().substring(0, 8))
                .name("자산 간 상관관계 붕괴")
                .description("전통적인 자산 간 상관관계가 급격히 변화하는 시나리오")
                .type(ScenarioType.CORRELATION_BREAKDOWN)
                .severity(ScenarioSeverity.MODERATE)
                .probability(0.12)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMonths(9))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.05)       // 5% 하락
                ))
                .correlationBreakdown(Map.of(
                        "STOCK_BOND", BigDecimal.valueOf(0.6),     // 주식-채권 상관관계 급증
                        "COMMODITIES", BigDecimal.valueOf(-0.4),   // 원자재 상관관계 역전
                        "CURRENCIES", BigDecimal.valueOf(0.5),     // 통화 상관관계 증가
                        "SECTORS", BigDecimal.valueOf(-0.3)        // 섹터 간 상관관계 감소
                ))
                .volatilityMultiplier(BigDecimal.valueOf(2.0))
                .liquidityImpact(BigDecimal.valueOf(0.10))
                .stressDuration(90)   // 3개월
                .build();
    }

    /**
     * 지정학적 위기 시나리오 생성
     */
    public static RiskScenario createGeopoliticalCrisisScenario() {
        return RiskScenario.builder()
                .scenarioId("GEOPOLITICAL_" + UUID.randomUUID().toString().substring(0, 8))
                .name("지정학적 위기")
                .description("지정학적 갈등과 무역 분쟁으로 인한 시장 불안")
                .type(ScenarioType.BLACK_SWAN)
                .severity(ScenarioSeverity.SEVERE)
                .probability(0.06)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMonths(18))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.18),      // 18% 하락
                        "DEFENSE", BigDecimal.valueOf(0.10),       // 방산업체 10% 상승
                        "ENERGY", BigDecimal.valueOf(0.25),        // 에너지 25% 상승
                        "AIRLINES", BigDecimal.valueOf(-0.30),     // 항공업 30% 하락
                        "EXPORT_HEAVY", BigDecimal.valueOf(-0.25)  // 수출 의존업종 25% 하락
                ))
                .correlationBreakdown(Map.of(
                        "REGIONAL_CORRELATION", BigDecimal.valueOf(-0.2) // 지역별 상관관계 감소
                ))
                .volatilityMultiplier(BigDecimal.valueOf(3.0))
                .liquidityImpact(BigDecimal.valueOf(0.15))
                .stressDuration(180)  // 6개월
                .build();
    }

    /**
     * 기술 버블 붕괴 시나리오 생성
     */
    public static RiskScenario createTechBubbleBurstScenario() {
        return RiskScenario.builder()
                .scenarioId("TECH_BUBBLE_" + UUID.randomUUID().toString().substring(0, 8))
                .name("기술주 버블 붕괴")
                .description("과도한 기술주 평가로 인한 버블 붕괴")
                .type(ScenarioType.MARKET_CRASH)
                .severity(ScenarioSeverity.SEVERE)
                .probability(0.10)
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusYears(2))
                .marketShocks(Map.of(
                        "GENERAL", BigDecimal.valueOf(-0.25),      // 25% 하락
                        "TECH", BigDecimal.valueOf(-0.45),         // 기술주 45% 하락
                        "GROWTH", BigDecimal.valueOf(-0.35),       // 성장주 35% 하락
                        "VALUE", BigDecimal.valueOf(-0.10),        // 가치주 10% 하락
                        "CRYPTO", BigDecimal.valueOf(-0.60)        // 암호화폐 60% 하락
                ))
                .correlationBreakdown(Map.of(
                        "GROWTH_VALUE", BigDecimal.valueOf(-0.3)   // 성장-가치주 상관관계 감소
                ))
                .volatilityMultiplier(BigDecimal.valueOf(3.5))
                .liquidityImpact(BigDecimal.valueOf(0.20))
                .stressDuration(365)  // 1년
                .build();
    }

    /**
     * 사용자 정의 시나리오 생성
     */
    public static RiskScenario createCustomScenario(
            String name,
            String description,
            ScenarioType type,
            ScenarioSeverity severity,
            Map<String, BigDecimal> marketShocks,
            BigDecimal volatilityMultiplier,
            Integer stressDuration) {

        return RiskScenario.builder()
                .scenarioId("CUSTOM_" + UUID.randomUUID().toString().substring(0, 8))
                .name(name)
                .description(description)
                .type(type)
                .severity(severity)
                .probability(type.getTypicalProbability())
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusYears(1))
                .marketShocks(marketShocks)
                .volatilityMultiplier(volatilityMultiplier)
                .liquidityImpact(BigDecimal.valueOf(0.10))
                .stressDuration(stressDuration)
                .build();
    }

    /**
     * 모든 표준 시나리오를 반환
     */
    public static java.util.List<RiskScenario> getAllStandardScenarios() {
        return java.util.List.of(
                create2008FinancialCrisisScenario(),
                createCovid19PandemicScenario(),
                createBlackMondayScenario(),
                createLiquidityCrisisScenario(),
                createInterestRateShockScenario(),
                createCorrelationBreakdownScenario(),
                createGeopoliticalCrisisScenario(),
                createTechBubbleBurstScenario()
        );
    }
}