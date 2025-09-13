package com.stockquest.application.service;

import com.stockquest.application.service.ExecutionAnalyticsService.*;
import com.stockquest.domain.execution.Order;
import com.stockquest.domain.execution.Trade;
import com.stockquest.domain.execution.Trade.ExecutionGrade;
import com.stockquest.domain.execution.Trade.ExecutionQualityMetrics;
import com.stockquest.domain.execution.Trade.TradeSide;
import com.stockquest.domain.execution.Trade.TradeStatus;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExecutionAnalyticsService 테스트
 * Phase 4.1: 핵심 서비스 테스트 스위트 - ExecutionAnalyticsService 종합 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionAnalyticsService 테스트")
class ExecutionAnalyticsServiceTest extends TestBase {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExecutionAnalyticsService executionAnalyticsService;

    private Trade sampleTrade;
    private Order sampleOrder;
    private ExecutionQualityMetrics sampleMetrics;

    @BeforeEach
    void setUp() {
        // 샘플 ExecutionQualityMetrics 생성
        sampleMetrics = ExecutionQualityMetrics.builder()
            .realizedSlippage(BigDecimal.valueOf(0.001))
            .marketImpact(BigDecimal.valueOf(0.005))
            .timingCost(BigDecimal.valueOf(0.002))
            .executionEfficiency(BigDecimal.valueOf(85))
            .executionSpeed(250L) // 250ms
            .arrivalPricePerformance(BigDecimal.valueOf(0.98))
            .executionGrade(ExecutionGrade.GOOD)
            .build();

        // 샘플 Trade 생성
        sampleTrade = Trade.builder()
            .tradeId("TRADE_001")
            .orderId("ORDER_001")
            .portfolioId("PORTFOLIO_001")
            .userId("USER_001")
            .symbol("005930") // Samsung
            .side(TradeSide.BUY)
            .quantity(BigDecimal.valueOf(100))
            .price(BigDecimal.valueOf(80000))
            .amount(BigDecimal.valueOf(8000000))
            .tradeTime(LocalDateTime.now())
            .status(TradeStatus.EXECUTED)
            .executionMetrics(sampleMetrics)
            .build();

        // 샘플 Order 생성
        sampleOrder = Order.builder()
            .orderId("ORDER_001")
            .symbol("005930")
            .orderType(Order.OrderType.MARKET)
            .executionAlgorithm(Order.ExecutionAlgorithm.VWAP)
            .executedQuantity(BigDecimal.valueOf(100))
            .avgExecutionPrice(BigDecimal.valueOf(80000))
            .fillRate(BigDecimal.valueOf(1.0)) // 100% 체결
            .trades(Arrays.asList(sampleTrade))
            .build();
    }

    @Nested
    @DisplayName("거래 실행 품질 분석 테스트")
    class TradeExecutionAnalysisTests {

        @Test
        @DisplayName("정상적인 거래 분석이 성공해야 한다")
        void shouldAnalyzeTradeExecutionSuccessfully() throws Exception {
            // When
            CompletableFuture<ExecutionAnalysis> future = executionAnalyticsService.analyzeTradeExecution(sampleTrade);
            ExecutionAnalysis analysis = future.get();

            // Then
            assertThat(analysis).isNotNull();
            assertThat(analysis.getTradeId()).isEqualTo("TRADE_001");
            assertThat(analysis.getSymbol()).isEqualTo("005930");
            assertThat(analysis.getAnalysisTime()).isNotNull();
            assertThat(analysis.getSlippage()).isEqualByComparingTo(BigDecimal.valueOf(0.001));
            assertThat(analysis.getMarketImpact()).isEqualByComparingTo(BigDecimal.valueOf(0.005));
            assertThat(analysis.getExecutionGrade()).isEqualTo(ExecutionGrade.GOOD);
        }

        @Test
        @DisplayName("메트릭이 없는 거래도 분석할 수 있어야 한다")
        void shouldAnalyzeTradeWithoutMetrics() throws Exception {
            // Given
            Trade tradeWithoutMetrics = Trade.builder()
                .tradeId("TRADE_002")
                .orderId("ORDER_002")
                .symbol("000660")
                .side(TradeSide.SELL)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(45000))
                .amount(BigDecimal.valueOf(2250000))
                .tradeTime(LocalDateTime.now())
                .status(TradeStatus.EXECUTED)
                .executionMetrics(null) // 메트릭 없음
                .build();

            // When
            CompletableFuture<ExecutionAnalysis> future = executionAnalyticsService.analyzeTradeExecution(tradeWithoutMetrics);
            ExecutionAnalysis analysis = future.get();

            // Then
            assertThat(analysis).isNotNull();
            assertThat(analysis.getTradeId()).isEqualTo("TRADE_002");
            assertThat(analysis.getMarketImpact()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(analysis.getTimingCost()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("비동기 실행이 올바르게 작동해야 한다")
        void shouldExecuteAsynchronously() {
            // When
            CompletableFuture<ExecutionAnalysis> future = executionAnalyticsService.analyzeTradeExecution(sampleTrade);

            // Then
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse(); // 비동기 실행 확인
        }
    }

    @Nested
    @DisplayName("주문 실행 품질 종합 분석 테스트")
    class OrderExecutionAnalysisTests {

        @Test
        @DisplayName("정상적인 주문 분석이 성공해야 한다")
        void shouldAnalyzeOrderExecutionSuccessfully() throws Exception {
            // When
            CompletableFuture<OrderExecutionAnalysis> future = executionAnalyticsService.analyzeOrderExecution(sampleOrder);
            OrderExecutionAnalysis analysis = future.get();

            // Then
            assertThat(analysis).isNotNull();
            assertThat(analysis.getOrderId()).isEqualTo("ORDER_001");
            assertThat(analysis.getSymbol()).isEqualTo("005930");
            assertThat(analysis.getOrderType()).isEqualTo(Order.OrderType.MARKET);
            assertThat(analysis.getExecutionAlgorithm()).isEqualTo(Order.ExecutionAlgorithm.VWAP);
            assertThat(analysis.getTotalTrades()).isEqualTo(1);
            assertThat(analysis.getTotalQuantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(analysis.getAverageExecutionPrice()).isEqualByComparingTo(BigDecimal.valueOf(80000));
            assertThat(analysis.getFillRate()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        }

        @Test
        @DisplayName("거래가 없는 주문은 상태만 설정되어야 한다")
        void shouldHandleOrderWithoutTrades() throws Exception {
            // Given
            Order orderWithoutTrades = Order.builder()
                .orderId("ORDER_002")
                .symbol("000660")
                .orderType(Order.OrderType.LIMIT)
                .executionAlgorithm(Order.ExecutionAlgorithm.TWAP)
                .trades(List.of()) // 빈 거래 리스트
                .build();

            // When
            CompletableFuture<OrderExecutionAnalysis> future = executionAnalyticsService.analyzeOrderExecution(orderWithoutTrades);
            OrderExecutionAnalysis analysis = future.get();

            // Then
            assertThat(analysis).isNotNull();
            assertThat(analysis.getOrderId()).isEqualTo("ORDER_002");
            assertThat(analysis.getStatus()).isEqualTo("No trades executed");
        }

        @Test
        @DisplayName("복수 거래가 있는 주문 분석이 올바르게 계산되어야 한다")
        void shouldAnalyzeOrderWithMultipleTrades() throws Exception {
            // Given
            Trade trade1 = createTradeWithMetrics("TRADE_001", 50, 80000, 0.001);
            Trade trade2 = createTradeWithMetrics("TRADE_002", 30, 80100, 0.002);
            Trade trade3 = createTradeWithMetrics("TRADE_003", 20, 79900, 0.0015);

            Order multiTradeOrder = Order.builder()
                .orderId("ORDER_MULTI")
                .symbol("005930")
                .orderType(Order.OrderType.MARKET)
                .executionAlgorithm(Order.ExecutionAlgorithm.VWAP)
                .trades(Arrays.asList(trade1, trade2, trade3))
                .executedQuantity(BigDecimal.valueOf(100))
                .avgExecutionPrice(BigDecimal.valueOf(80000))
                .fillRate(BigDecimal.valueOf(1.0))
                .build();

            // When
            CompletableFuture<OrderExecutionAnalysis> future = executionAnalyticsService.analyzeOrderExecution(multiTradeOrder);
            OrderExecutionAnalysis analysis = future.get();

            // Then
            assertThat(analysis.getTotalTrades()).isEqualTo(3);
            assertThat(analysis.getAverageSlippage()).isNotNull();
            assertThat(analysis.getAverageMarketImpact()).isNotNull();
            assertThat(analysis.getOverallGrade()).isNotNull();
        }
    }

    @Nested
    @DisplayName("베스트 실행 상태 조회 테스트")
    class BestExecutionStatusTests {

        @Test
        @DisplayName("데이터가 없을 때 NO_DATA 상태를 반환해야 한다")
        void shouldReturnNoDataStatusWhenNoMetrics() throws Exception {
            // When
            CompletableFuture<BestExecutionStatus> future = executionAnalyticsService.getBestExecutionStatus();
            BestExecutionStatus status = future.get();

            // Then
            assertThat(status).isNotNull();
            assertThat(status.getOverallStatus()).isEqualTo("NO_DATA");
            assertThat(status.getReportDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("상태 시간이 올바르게 설정되어야 한다")
        void shouldSetStatusTimeCorrectly() throws Exception {
            // Given
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

            // When
            CompletableFuture<BestExecutionStatus> future = executionAnalyticsService.getBestExecutionStatus();
            BestExecutionStatus status = future.get();
            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

            // Then
            assertThat(status.getStatusTime()).isAfter(beforeCall);
            assertThat(status.getStatusTime()).isBefore(afterCall);
        }
    }

    @Nested
    @DisplayName("벤치마크 성과 분석 테스트")
    class BenchmarkAnalysisTests {

        @Test
        @DisplayName("기간 내 거래가 없을 때 적절한 상태를 반환해야 한다")
        void shouldReturnNoTradesStatusWhenNoTradesInPeriod() throws Exception {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // When
            CompletableFuture<BenchmarkAnalysis> future = executionAnalyticsService
                .analyzeBenchmarkPerformance("000660", startDate, endDate);
            BenchmarkAnalysis analysis = future.get();

            // Then
            assertThat(analysis).isNotNull();
            assertThat(analysis.getSymbol()).isEqualTo("000660");
            assertThat(analysis.getStartDate()).isEqualTo(startDate);
            assertThat(analysis.getEndDate()).isEqualTo(endDate);
            assertThat(analysis.getStatus()).isEqualTo("No trades found for the period");
        }

        @Test
        @DisplayName("분석 시간이 현재 시간으로 설정되어야 한다")
        void shouldSetAnalysisTimeToCurrentTime() throws Exception {
            // Given
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // When
            CompletableFuture<BenchmarkAnalysis> future = executionAnalyticsService
                .analyzeBenchmarkPerformance("005930", startDate, endDate);
            BenchmarkAnalysis analysis = future.get();
            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

            // Then
            assertThat(analysis.getAnalysisTime()).isAfter(beforeCall);
            assertThat(analysis.getAnalysisTime()).isBefore(afterCall);
        }
    }

    @Nested
    @DisplayName("실행 히스토리 조회 테스트")
    class ExecutionHistoryTests {

        @Test
        @DisplayName("빈 히스토리에서 빈 리스트를 반환해야 한다")
        void shouldReturnEmptyListWhenNoHistory() {
            // When
            List<ExecutionAnalysis> history = executionAnalyticsService.getExecutionHistory("000660", 7);

            // Then
            assertThat(history).isNotNull();
            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("유효한 일수 범위가 적용되어야 한다")
        void shouldApplyValidDayRange() throws Exception {
            // Given - 먼저 거래를 분석하여 히스토리에 추가
            CompletableFuture<ExecutionAnalysis> analysisTask = executionAnalyticsService.analyzeTradeExecution(sampleTrade);
            analysisTask.get(); // 완료까지 대기

            // When
            List<ExecutionAnalysis> history7Days = executionAnalyticsService.getExecutionHistory("005930", 7);
            List<ExecutionAnalysis> history1Day = executionAnalyticsService.getExecutionHistory("005930", 1);

            // Then
            assertThat(history7Days).hasSize(1);
            assertThat(history1Day).hasSize(1);
        }
    }

    @Nested
    @DisplayName("알림 시스템 테스트")
    class NotificationSystemTests {

        @Test
        @DisplayName("최근 거래 품질 검사가 실행되어야 한다")
        void shouldExecuteRecentTradeQualityCheck() {
            // When
            assertThatCode(() -> executionAnalyticsService.monitorBestExecution())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("베스트 실행 모니터링이 예외 없이 실행되어야 한다")
        void shouldExecuteBestExecutionMonitoringWithoutException() {
            // When & Then
            assertThatCode(() -> executionAnalyticsService.monitorBestExecution())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("일일 베스트 실행 리포트 생성이 예외 없이 실행되어야 한다")
        void shouldGenerateDailyBestExecutionReportWithoutException() {
            // When & Then
            assertThatCode(() -> executionAnalyticsService.generateDailyBestExecutionReport())
                .doesNotThrowAnyException();
        }
    }

    // 헬퍼 메서드
    private Trade createTradeWithMetrics(String tradeId, int quantity, int price, double slippage) {
        ExecutionQualityMetrics metrics = ExecutionQualityMetrics.builder()
            .realizedSlippage(BigDecimal.valueOf(slippage))
            .marketImpact(BigDecimal.valueOf(0.005))
            .timingCost(BigDecimal.valueOf(0.002))
            .executionEfficiency(BigDecimal.valueOf(85))
            .executionSpeed(200L)
            .build();

        return Trade.builder()
            .tradeId(tradeId)
            .orderId("ORDER_001")
            .symbol("005930")
            .side(TradeSide.BUY)
            .quantity(BigDecimal.valueOf(quantity))
            .price(BigDecimal.valueOf(price))
            .amount(BigDecimal.valueOf(quantity * price))
            .tradeTime(LocalDateTime.now())
            .status(TradeStatus.EXECUTED)
            .executionMetrics(metrics)
            .build();
    }
}