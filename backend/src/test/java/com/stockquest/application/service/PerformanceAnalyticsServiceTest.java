package com.stockquest.application.service;

import com.stockquest.application.service.analytics.*;
import com.stockquest.domain.backtesting.BacktestResult;
import com.stockquest.testutils.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PerformanceAnalyticsService 테스트
 * Phase 4.1: 핵심 서비스 테스트 스위트 - 리팩토링된 오케스트레이터 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceAnalyticsService 테스트")
class PerformanceAnalyticsServiceTest extends TestBase {

    @Mock private RiskAnalysisService riskAnalysisService;
    @Mock private TradingAnalysisService tradingAnalysisService;
    @Mock private TimeSeriesAnalysisService timeSeriesAnalysisService;
    @Mock private AttributionAnalysisService attributionAnalysisService;
    @Mock private BenchmarkAnalysisService benchmarkAnalysisService;
    @Mock private ScenarioAnalysisService scenarioAnalysisService;

    @InjectMocks
    private PerformanceAnalyticsService performanceAnalyticsService;

    private BacktestResult sampleBacktestResult;

    @BeforeEach
    void setUp() {
        sampleBacktestResult = BacktestResult.builder()
            .backtestId("BT_001")
            .strategyName("Sample Strategy")
            .startDate(LocalDate.now().minusDays(30))
            .endDate(LocalDate.now())
            .initialCapital(BigDecimal.valueOf(10000000))
            .finalValue(BigDecimal.valueOf(11000000))
            .totalReturn(BigDecimal.valueOf(0.10))
            .annualizedReturn(BigDecimal.valueOf(0.12))
            .maxDrawdown(BigDecimal.valueOf(-0.05))
            .sharpeRatio(BigDecimal.valueOf(1.5))
            .build();
    }

    @Nested
    @DisplayName("종합 성과 분석 테스트")
    class ComprehensiveAnalysisTests {

        @Test
        @DisplayName("모든 분석 서비스가 정상적으로 호출되어야 한다")
        void shouldCallAllAnalysisServices() throws Exception {
            // Given
            when(riskAnalysisService.performRiskAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockRiskAnalysis());
            when(tradingAnalysisService.performTradingAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTradingAnalysis());
            when(timeSeriesAnalysisService.performTimeSeriesAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTimeSeriesAnalysis());
            when(attributionAnalysisService.performAttributionAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockAttributionAnalysis());
            when(benchmarkAnalysisService.performBenchmarkAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockBenchmarkAnalysis());
            when(scenarioAnalysisService.performScenarioAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockScenarioAnalysis());

            // When
            CompletableFuture<PerformanceAnalyticsService.ComprehensiveAnalysisResult> future = 
                performanceAnalyticsService.performComprehensiveAnalysis(sampleBacktestResult);
            PerformanceAnalyticsService.ComprehensiveAnalysisResult result = future.get();

            // Then
            verify(riskAnalysisService).performRiskAnalysis(sampleBacktestResult);
            verify(tradingAnalysisService).performTradingAnalysis(sampleBacktestResult);
            verify(timeSeriesAnalysisService).performTimeSeriesAnalysis(sampleBacktestResult);
            verify(attributionAnalysisService).performAttributionAnalysis(sampleBacktestResult);
            verify(benchmarkAnalysisService).performBenchmarkAnalysis(sampleBacktestResult);
            verify(scenarioAnalysisService).performScenarioAnalysis(sampleBacktestResult);

            assertThat(result).isNotNull();
            assertThat(result.getBacktestId()).isEqualTo("BT_001");
            assertThat(result.getAnalysisTime()).isNotNull();
            assertThat(result.getRiskAnalysis()).isNotNull();
            assertThat(result.getTradingAnalysis()).isNotNull();
            assertThat(result.getTimeSeriesAnalysis()).isNotNull();
            assertThat(result.getAttributionAnalysis()).isNotNull();
            assertThat(result.getBenchmarkAnalysis()).isNotNull();
            assertThat(result.getScenarioAnalysis()).isNotNull();
        }

        @Test
        @DisplayName("서비스 호출 실패 시 적절히 처리되어야 한다")
        void shouldHandleServiceFailuresGracefully() throws Exception {
            // Given
            when(riskAnalysisService.performRiskAnalysis(any(BacktestResult.class)))
                .thenThrow(new RuntimeException("Risk analysis failed"));
            when(tradingAnalysisService.performTradingAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTradingAnalysis());
            when(timeSeriesAnalysisService.performTimeSeriesAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTimeSeriesAnalysis());
            when(attributionAnalysisService.performAttributionAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockAttributionAnalysis());
            when(benchmarkAnalysisService.performBenchmarkAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockBenchmarkAnalysis());
            when(scenarioAnalysisService.performScenarioAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockScenarioAnalysis());

            // When & Then
            CompletableFuture<PerformanceAnalyticsService.ComprehensiveAnalysisResult> future = 
                performanceAnalyticsService.performComprehensiveAnalysis(sampleBacktestResult);

            // 예외가 발생하더라도 다른 분석은 계속되어야 함
            assertThatCode(() -> future.get()).doesNotThrowAnyException();
            
            PerformanceAnalyticsService.ComprehensiveAnalysisResult result = future.get();
            assertThat(result).isNotNull();
            assertThat(result.getRiskAnalysis()).isNull(); // 실패한 분석은 null
        }

        @Test
        @DisplayName("비동기 실행이 올바르게 작동해야 한다")
        void shouldExecuteAsynchronously() {
            // Given
            when(riskAnalysisService.performRiskAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockRiskAnalysis());
            when(tradingAnalysisService.performTradingAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTradingAnalysis());
            when(timeSeriesAnalysisService.performTimeSeriesAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTimeSeriesAnalysis());
            when(attributionAnalysisService.performAttributionAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockAttributionAnalysis());
            when(benchmarkAnalysisService.performBenchmarkAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockBenchmarkAnalysis());
            when(scenarioAnalysisService.performScenarioAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockScenarioAnalysis());

            // When
            CompletableFuture<PerformanceAnalyticsService.ComprehensiveAnalysisResult> future = 
                performanceAnalyticsService.performComprehensiveAnalysis(sampleBacktestResult);

            // Then
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse(); // 비동기 실행 확인
        }
    }

    @Nested
    @DisplayName("개별 분석 서비스 위임 테스트")
    class IndividualAnalysisTests {

        @Test
        @DisplayName("리스크 분석이 올바르게 위임되어야 한다")
        void shouldDelegateRiskAnalysisCorrectly() throws Exception {
            // Given
            RiskAnalysisService.RiskAnalysis mockRiskAnalysis = createMockRiskAnalysis();
            when(riskAnalysisService.performRiskAnalysis(sampleBacktestResult))
                .thenReturn(mockRiskAnalysis);

            // When
            CompletableFuture<RiskAnalysisService.RiskAnalysis> future = 
                performanceAnalyticsService.performRiskAnalysis(sampleBacktestResult);
            RiskAnalysisService.RiskAnalysis result = future.get();

            // Then
            verify(riskAnalysisService).performRiskAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockRiskAnalysis);
        }

        @Test
        @DisplayName("거래 분석이 올바르게 위임되어야 한다")
        void shouldDelegateTradingAnalysisCorrectly() throws Exception {
            // Given
            TradingAnalysisService.TradingAnalysis mockTradingAnalysis = createMockTradingAnalysis();
            when(tradingAnalysisService.performTradingAnalysis(sampleBacktestResult))
                .thenReturn(mockTradingAnalysis);

            // When
            CompletableFuture<TradingAnalysisService.TradingAnalysis> future = 
                performanceAnalyticsService.performTradingAnalysis(sampleBacktestResult);
            TradingAnalysisService.TradingAnalysis result = future.get();

            // Then
            verify(tradingAnalysisService).performTradingAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockTradingAnalysis);
        }

        @Test
        @DisplayName("시계열 분석이 올바르게 위임되어야 한다")
        void shouldDelegateTimeSeriesAnalysisCorrectly() throws Exception {
            // Given
            TimeSeriesAnalysisService.TimeSeriesAnalysis mockTimeSeriesAnalysis = createMockTimeSeriesAnalysis();
            when(timeSeriesAnalysisService.performTimeSeriesAnalysis(sampleBacktestResult))
                .thenReturn(mockTimeSeriesAnalysis);

            // When
            CompletableFuture<TimeSeriesAnalysisService.TimeSeriesAnalysis> future = 
                performanceAnalyticsService.performTimeSeriesAnalysis(sampleBacktestResult);
            TimeSeriesAnalysisService.TimeSeriesAnalysis result = future.get();

            // Then
            verify(timeSeriesAnalysisService).performTimeSeriesAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockTimeSeriesAnalysis);
        }

        @Test
        @DisplayName("어트리뷰션 분석이 올바르게 위임되어야 한다")
        void shouldDelegateAttributionAnalysisCorrectly() throws Exception {
            // Given
            AttributionAnalysisService.AttributionAnalysis mockAttributionAnalysis = createMockAttributionAnalysis();
            when(attributionAnalysisService.performAttributionAnalysis(sampleBacktestResult))
                .thenReturn(mockAttributionAnalysis);

            // When
            CompletableFuture<AttributionAnalysisService.AttributionAnalysis> future = 
                performanceAnalyticsService.performAttributionAnalysis(sampleBacktestResult);
            AttributionAnalysisService.AttributionAnalysis result = future.get();

            // Then
            verify(attributionAnalysisService).performAttributionAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockAttributionAnalysis);
        }

        @Test
        @DisplayName("벤치마크 분석이 올바르게 위임되어야 한다")
        void shouldDelegateBenchmarkAnalysisCorrectly() throws Exception {
            // Given
            BenchmarkAnalysisService.BenchmarkAnalysis mockBenchmarkAnalysis = createMockBenchmarkAnalysis();
            when(benchmarkAnalysisService.performBenchmarkAnalysis(sampleBacktestResult))
                .thenReturn(mockBenchmarkAnalysis);

            // When
            CompletableFuture<BenchmarkAnalysisService.BenchmarkAnalysis> future = 
                performanceAnalyticsService.performBenchmarkAnalysis(sampleBacktestResult);
            BenchmarkAnalysisService.BenchmarkAnalysis result = future.get();

            // Then
            verify(benchmarkAnalysisService).performBenchmarkAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockBenchmarkAnalysis);
        }

        @Test
        @DisplayName("시나리오 분석이 올바르게 위임되어야 한다")
        void shouldDelegateScenarioAnalysisCorrectly() throws Exception {
            // Given
            ScenarioAnalysisService.ScenarioAnalysis mockScenarioAnalysis = createMockScenarioAnalysis();
            when(scenarioAnalysisService.performScenarioAnalysis(sampleBacktestResult))
                .thenReturn(mockScenarioAnalysis);

            // When
            CompletableFuture<ScenarioAnalysisService.ScenarioAnalysis> future = 
                performanceAnalyticsService.performScenarioAnalysis(sampleBacktestResult);
            ScenarioAnalysisService.ScenarioAnalysis result = future.get();

            // Then
            verify(scenarioAnalysisService).performScenarioAnalysis(sampleBacktestResult);
            assertThat(result).isEqualTo(mockScenarioAnalysis);
        }
    }

    @Nested
    @DisplayName("오류 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("null 백테스트 결과에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullBacktestResult() {
            // When & Then
            assertThatThrownBy(() -> performanceAnalyticsService.performComprehensiveAnalysis(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BacktestResult cannot be null");
        }

        @Test
        @DisplayName("잘못된 백테스트 ID에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForInvalidBacktestId() {
            // Given
            BacktestResult invalidResult = BacktestResult.builder()
                .backtestId(null) // null ID
                .strategyName("Test Strategy")
                .build();

            // When & Then
            assertThatThrownBy(() -> performanceAnalyticsService.performComprehensiveAnalysis(invalidResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Backtest ID cannot be null or empty");
        }

        @Test
        @DisplayName("개별 분석 서비스 실패 시 로깅하고 계속 진행해야 한다")
        void shouldLogAndContinueOnIndividualServiceFailure() throws Exception {
            // Given
            when(riskAnalysisService.performRiskAnalysis(any(BacktestResult.class)))
                .thenThrow(new RuntimeException("Database connection failed"));
            when(tradingAnalysisService.performTradingAnalysis(any(BacktestResult.class)))
                .thenReturn(createMockTradingAnalysis());

            // When
            CompletableFuture<PerformanceAnalyticsService.ComprehensiveAnalysisResult> future = 
                performanceAnalyticsService.performComprehensiveAnalysis(sampleBacktestResult);
            PerformanceAnalyticsService.ComprehensiveAnalysisResult result = future.get();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRiskAnalysis()).isNull(); // 실패한 분석
            assertThat(result.getTradingAnalysis()).isNotNull(); // 성공한 분석
        }
    }

    // 목 객체 생성 헬퍼 메서드들
    private RiskAnalysisService.RiskAnalysis createMockRiskAnalysis() {
        return RiskAnalysisService.RiskAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .valueAtRisk(BigDecimal.valueOf(500000))
            .conditionalVaR(BigDecimal.valueOf(750000))
            .maxDrawdown(BigDecimal.valueOf(-0.05))
            .volatility(BigDecimal.valueOf(0.15))
            .sharpeRatio(BigDecimal.valueOf(1.5))
            .build();
    }

    private TradingAnalysisService.TradingAnalysis createMockTradingAnalysis() {
        return TradingAnalysisService.TradingAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .totalTrades(100)
            .winRate(BigDecimal.valueOf(0.6))
            .averageWin(BigDecimal.valueOf(12000))
            .averageLoss(BigDecimal.valueOf(-8000))
            .profitFactor(BigDecimal.valueOf(1.8))
            .build();
    }

    private TimeSeriesAnalysisService.TimeSeriesAnalysis createMockTimeSeriesAnalysis() {
        return TimeSeriesAnalysisService.TimeSeriesAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .trendDirection("BULLISH")
            .trendStrength(BigDecimal.valueOf(0.7))
            .volatilityRegime("MODERATE")
            .seasonalityScore(BigDecimal.valueOf(0.3))
            .build();
    }

    private AttributionAnalysisService.AttributionAnalysis createMockAttributionAnalysis() {
        return AttributionAnalysisService.AttributionAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .alphaContribution(BigDecimal.valueOf(0.08))
            .betaContribution(BigDecimal.valueOf(0.02))
            .totalActiveReturn(BigDecimal.valueOf(0.10))
            .informationRatio(BigDecimal.valueOf(1.2))
            .build();
    }

    private BenchmarkAnalysisService.BenchmarkAnalysis createMockBenchmarkAnalysis() {
        return BenchmarkAnalysisService.BenchmarkAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .benchmarkReturn(BigDecimal.valueOf(0.05))
            .activeReturn(BigDecimal.valueOf(0.05))
            .trackingError(BigDecimal.valueOf(0.02))
            .informationRatio(BigDecimal.valueOf(2.5))
            .build();
    }

    private ScenarioAnalysisService.ScenarioAnalysis createMockScenarioAnalysis() {
        return ScenarioAnalysisService.ScenarioAnalysis.builder()
            .backtestId("BT_001")
            .analysisTime(LocalDateTime.now())
            .stressTestResults(java.util.Map.of(
                "BEAR_MARKET", BigDecimal.valueOf(-0.15),
                "BULL_MARKET", BigDecimal.valueOf(0.25),
                "HIGH_VOLATILITY", BigDecimal.valueOf(-0.08)
            ))
            .worstCaseScenario("BEAR_MARKET")
            .bestCaseScenario("BULL_MARKET")
            .build();
    }
}