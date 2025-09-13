package com.stockquest.application.service.analytics;

import com.stockquest.application.service.analytics.RiskAnalysisService.RiskAnalysis;
import com.stockquest.domain.backtesting.BacktestResult;
import com.stockquest.testutils.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RiskAnalysisService 테스트
 * Phase 4.1: 핵심 서비스 테스트 스위트 - 리스크 분석 전문 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RiskAnalysisService 테스트")
class RiskAnalysisServiceTest extends TestBase {

    @InjectMocks
    private RiskAnalysisService riskAnalysisService;

    private BacktestResult sampleBacktestResult;
    private BacktestResult highRiskBacktestResult;
    private BacktestResult lowRiskBacktestResult;

    @BeforeEach
    void setUp() {
        // 일반적인 백테스트 결과
        sampleBacktestResult = BacktestResult.builder()
            .backtestId("BT_001")
            .strategyName("Sample Strategy")
            .startDate(LocalDate.now().minusDays(365))
            .endDate(LocalDate.now())
            .initialCapital(BigDecimal.valueOf(10000000))
            .finalValue(BigDecimal.valueOf(11500000))
            .totalReturn(BigDecimal.valueOf(0.15))
            .annualizedReturn(BigDecimal.valueOf(0.15))
            .maxDrawdown(BigDecimal.valueOf(-0.08))
            .sharpeRatio(BigDecimal.valueOf(1.2))
            .volatility(BigDecimal.valueOf(0.12))
            .build();

        // 고위험 백테스트 결과
        highRiskBacktestResult = BacktestResult.builder()
            .backtestId("BT_HIGH_RISK")
            .strategyName("High Risk Strategy")
            .startDate(LocalDate.now().minusDays(365))
            .endDate(LocalDate.now())
            .initialCapital(BigDecimal.valueOf(10000000))
            .finalValue(BigDecimal.valueOf(13000000))
            .totalReturn(BigDecimal.valueOf(0.30))
            .annualizedReturn(BigDecimal.valueOf(0.30))
            .maxDrawdown(BigDecimal.valueOf(-0.25))
            .sharpeRatio(BigDecimal.valueOf(0.8))
            .volatility(BigDecimal.valueOf(0.35))
            .build();

        // 저위험 백테스트 결과
        lowRiskBacktestResult = BacktestResult.builder()
            .backtestId("BT_LOW_RISK")
            .strategyName("Low Risk Strategy")
            .startDate(LocalDate.now().minusDays(365))
            .endDate(LocalDate.now())
            .initialCapital(BigDecimal.valueOf(10000000))
            .finalValue(BigDecimal.valueOf(10500000))
            .totalReturn(BigDecimal.valueOf(0.05))
            .annualizedReturn(BigDecimal.valueOf(0.05))
            .maxDrawdown(BigDecimal.valueOf(-0.02))
            .sharpeRatio(BigDecimal.valueOf(2.5))
            .volatility(BigDecimal.valueOf(0.05))
            .build();
    }

    @Nested
    @DisplayName("리스크 분석 기본 기능 테스트")
    class BasicRiskAnalysisTests {

        @Test
        @DisplayName("정상적인 리스크 분석이 수행되어야 한다")
        void shouldPerformBasicRiskAnalysis() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBacktestId()).isEqualTo("BT_001");
            assertThat(result.getAnalysisTime()).isNotNull();
            assertThat(result.getAnalysisTime()).isBeforeOrEqualTo(LocalDateTime.now());

            // 기본 리스크 지표들
            assertThat(result.getValueAtRisk()).isNotNull();
            assertThat(result.getConditionalVaR()).isNotNull();
            assertThat(result.getMaxDrawdown()).isNotNull();
            assertThat(result.getVolatility()).isNotNull();
            assertThat(result.getSharpeRatio()).isNotNull();

            // VaR은 포트폴리오 가치의 일정 비율이어야 함
            assertThat(result.getValueAtRisk()).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.getValueAtRisk()).isLessThan(sampleBacktestResult.getFinalValue());
        }

        @Test
        @DisplayName("분석 시간이 현재 시간으로 설정되어야 한다")
        void shouldSetAnalysisTimeToCurrentTime() {
            // Given
            LocalDateTime beforeAnalysis = LocalDateTime.now().minusSeconds(1);

            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            LocalDateTime afterAnalysis = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getAnalysisTime()).isAfter(beforeAnalysis);
            assertThat(result.getAnalysisTime()).isBefore(afterAnalysis);
        }

        @Test
        @DisplayName("백테스트 ID가 올바르게 설정되어야 한다")
        void shouldSetBacktestIdCorrectly() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getBacktestId()).isEqualTo(sampleBacktestResult.getBacktestId());
        }
    }

    @Nested
    @DisplayName("VaR 계산 테스트")
    class VaRCalculationTests {

        @Test
        @DisplayName("VaR이 포트폴리오 가치에 비례하여 계산되어야 한다")
        void shouldCalculateVaRProportionalToPortfolioValue() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            BigDecimal expectedVaRRange = sampleBacktestResult.getFinalValue()
                .multiply(BigDecimal.valueOf(0.1)); // 10% 내외
            assertThat(result.getValueAtRisk()).isLessThan(expectedVaRRange);
            assertThat(result.getValueAtRisk()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("조건부 VaR이 VaR보다 커야 한다")
        void shouldHaveConditionalVaRGreaterThanVaR() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getConditionalVaR()).isGreaterThan(result.getValueAtRisk());
        }

        @Test
        @DisplayName("고위험 전략의 VaR이 더 높아야 한다")
        void shouldHaveHigherVaRForHighRiskStrategy() {
            // When
            RiskAnalysis normalRisk = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);
            RiskAnalysis highRisk = riskAnalysisService.performRiskAnalysis(highRiskBacktestResult);

            // Then
            assertThat(highRisk.getValueAtRisk()).isGreaterThan(normalRisk.getValueAtRisk());
            assertThat(highRisk.getConditionalVaR()).isGreaterThan(normalRisk.getConditionalVaR());
        }
    }

    @Nested
    @DisplayName("스트레스 테스트 기능 테스트")
    class StressTestingTests {

        @Test
        @DisplayName("스트레스 테스트 결과가 포함되어야 한다")
        void shouldIncludeStressTestResults() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getStressTestResults()).isNotNull();
            assertThat(result.getStressTestResults()).isNotEmpty();
            
            // 주요 스트레스 시나리오들이 포함되어야 함
            assertThat(result.getStressTestResults()).containsKeys(
                "MARKET_CRASH_2008",
                "COVID_2020", 
                "BLACK_MONDAY_1987"
            );
        }

        @Test
        @DisplayName("스트레스 테스트 결과가 음수 수익률을 가져야 한다")
        void shouldHaveNegativeReturnsInStressTests() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            result.getStressTestResults().values().forEach(stressReturn -> {
                assertThat(stressReturn).isLessThan(BigDecimal.ZERO);
            });
        }

        @Test
        @DisplayName("최악의 스트레스 시나리오가 식별되어야 한다")
        void shouldIdentifyWorstStressScenario() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getWorstCaseScenario()).isNotNull();
            assertThat(result.getWorstCaseScenarioLoss()).isNotNull();
            
            // 최악 시나리오의 손실이 실제로 최대값이어야 함
            BigDecimal worstLoss = result.getStressTestResults().values().stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            assertThat(result.getWorstCaseScenarioLoss()).isEqualByComparingTo(worstLoss);
        }
    }

    @Nested
    @DisplayName("리스크 등급 분류 테스트")
    class RiskGradingTests {

        @Test
        @DisplayName("고위험 전략이 HIGH_RISK로 분류되어야 한다")
        void shouldClassifyHighRiskStrategyAsHighRisk() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(highRiskBacktestResult);

            // Then
            assertThat(result.getRiskGrade()).isEqualTo(RiskAnalysisService.RiskGrade.HIGH_RISK);
            assertThat(result.getRiskScore()).isGreaterThan(BigDecimal.valueOf(70));
        }

        @Test
        @DisplayName("저위험 전략이 LOW_RISK로 분류되어야 한다")
        void shouldClassifyLowRiskStrategyAsLowRisk() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(lowRiskBacktestResult);

            // Then
            assertThat(result.getRiskGrade()).isEqualTo(RiskAnalysisService.RiskGrade.LOW_RISK);
            assertThat(result.getRiskScore()).isLessThan(BigDecimal.valueOf(30));
        }

        @Test
        @DisplayName("중간 위험 전략이 MEDIUM_RISK로 분류되어야 한다")
        void shouldClassifyMediumRiskStrategyAsMediumRisk() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getRiskGrade()).isEqualTo(RiskAnalysisService.RiskGrade.MEDIUM_RISK);
            assertThat(result.getRiskScore()).isBetween(BigDecimal.valueOf(30), BigDecimal.valueOf(70));
        }

        @Test
        @DisplayName("리스크 점수가 0-100 범위 내에 있어야 한다")
        void shouldHaveRiskScoreWithinRange() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getRiskScore()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(result.getRiskScore()).isLessThanOrEqualTo(BigDecimal.valueOf(100));
        }
    }

    @Nested
    @DisplayName("리스크 지표 계산 테스트")
    class RiskMetricsCalculationTests {

        @Test
        @DisplayName("변동성이 올바르게 계산되어야 한다")
        void shouldCalculateVolatilityCorrectly() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getVolatility()).isNotNull();
            assertThat(result.getVolatility()).isGreaterThan(BigDecimal.ZERO);
            // 백테스트 결과의 변동성과 유사해야 함
            assertThat(result.getVolatility()).isCloseTo(
                sampleBacktestResult.getVolatility(),
                within(BigDecimal.valueOf(0.05))
            );
        }

        @Test
        @DisplayName("샤프 비율이 올바르게 계산되어야 한다")
        void shouldCalculateSharpeRatioCorrectly() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getSharpeRatio()).isNotNull();
            // 백테스트 결과의 샤프 비율과 유사해야 함
            assertThat(result.getSharpeRatio()).isCloseTo(
                sampleBacktestResult.getSharpeRatio(),
                within(BigDecimal.valueOf(0.2))
            );
        }

        @Test
        @DisplayName("최대 낙폭이 올바르게 설정되어야 한다")
        void shouldSetMaxDrawdownCorrectly() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getMaxDrawdown()).isNotNull();
            assertThat(result.getMaxDrawdown()).isLessThan(BigDecimal.ZERO); // 음수여야 함
            assertThat(result.getMaxDrawdown()).isEqualByComparingTo(sampleBacktestResult.getMaxDrawdown());
        }

        @Test
        @DisplayName("베타 값이 계산되어야 한다")
        void shouldCalculateBeta() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getBeta()).isNotNull();
            assertThat(result.getBeta()).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.getBeta()).isLessThan(BigDecimal.valueOf(3.0)); // 일반적인 범위
        }
    }

    @Nested
    @DisplayName("리스크 권고사항 테스트")
    class RiskRecommendationTests {

        @Test
        @DisplayName("고위험 전략에 대해 적절한 권고사항이 제공되어야 한다")
        void shouldProvideRecommendationsForHighRiskStrategy() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(highRiskBacktestResult);

            // Then
            assertThat(result.getRecommendations()).isNotNull();
            assertThat(result.getRecommendations()).isNotEmpty();
            
            // 고위험 전략에 대한 권고사항들
            List<String> recommendations = result.getRecommendations();
            boolean hasRiskReduction = recommendations.stream()
                .anyMatch(rec -> rec.contains("위험") || rec.contains("분산") || rec.contains("헤지"));
            assertThat(hasRiskReduction).isTrue();
        }

        @Test
        @DisplayName("저위험 전략에 대해 적절한 권고사항이 제공되어야 한다")
        void shouldProvideRecommendationsForLowRiskStrategy() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(lowRiskBacktestResult);

            // Then
            assertThat(result.getRecommendations()).isNotNull();
            assertThat(result.getRecommendations()).isNotEmpty();
            
            // 저위험 전략에 대한 권고사항들 (수익률 개선 관련)
            List<String> recommendations = result.getRecommendations();
            boolean hasReturnImprovement = recommendations.stream()
                .anyMatch(rec -> rec.contains("수익") || rec.contains("성과") || rec.contains("개선"));
            assertThat(hasReturnImprovement).isTrue();
        }

        @Test
        @DisplayName("권고사항이 최소 3개 이상 제공되어야 한다")
        void shouldProvideAtLeastThreeRecommendations() {
            // When
            RiskAnalysis result = riskAnalysisService.performRiskAnalysis(sampleBacktestResult);

            // Then
            assertThat(result.getRecommendations()).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("오류 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("null 백테스트 결과에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullBacktestResult() {
            // When & Then
            assertThatThrownBy(() -> riskAnalysisService.performRiskAnalysis(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BacktestResult cannot be null");
        }

        @Test
        @DisplayName("백테스트 ID가 없는 경우 예외를 발생시켜야 한다")
        void shouldThrowExceptionForMissingBacktestId() {
            // Given
            BacktestResult resultWithoutId = BacktestResult.builder()
                .backtestId(null)
                .strategyName("Test Strategy")
                .build();

            // When & Then
            assertThatThrownBy(() -> riskAnalysisService.performRiskAnalysis(resultWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Backtest ID cannot be null or empty");
        }

        @Test
        @DisplayName("유효하지 않은 포트폴리오 가치에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForInvalidPortfolioValue() {
            // Given
            BacktestResult invalidResult = BacktestResult.builder()
                .backtestId("BT_INVALID")
                .strategyName("Invalid Strategy")
                .finalValue(BigDecimal.ZERO) // 0 또는 음수
                .build();

            // When & Then
            assertThatThrownBy(() -> riskAnalysisService.performRiskAnalysis(invalidResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Portfolio value must be positive");
        }
    }
}