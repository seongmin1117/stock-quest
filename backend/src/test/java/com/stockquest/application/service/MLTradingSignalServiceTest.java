package com.stockquest.application.service;

import com.stockquest.application.service.ml.*;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.SignalType;
import com.stockquest.domain.ml.TradingSignal.ConfidenceLevel;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MLTradingSignalService 테스트
 * Phase 4.1: 핵심 서비스 테스트 스위트 - 리팩토링된 ML 오케스트레이터 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MLTradingSignalService 테스트")
class MLTradingSignalServiceTest extends TestBase {

    @Mock private MarketFeatureCollectionService marketFeatureCollectionService;
    @Mock private MLModelManagementService mlModelManagementService;
    @Mock private FeatureEngineeringService featureEngineeringService;
    @Mock private SignalGenerationService signalGenerationService;
    @Mock private MarketIntelligenceService marketIntelligenceService;

    @InjectMocks
    private MLTradingSignalService mlTradingSignalService;

    private String sampleSymbol = "005930";
    private MarketFeatureCollectionService.MarketFeatures sampleMarketFeatures;
    private MLModelManagementService.SimpleTradingModel sampleModel;
    private TradingSignal sampleTradingSignal;

    @BeforeEach
    void setUp() {
        // 샘플 MarketFeatures 생성
        sampleMarketFeatures = MarketFeatureCollectionService.MarketFeatures.builder()
            .symbol(sampleSymbol)
            .collectTime(LocalDateTime.now())
            .currentPrice(BigDecimal.valueOf(80000))
            .volume(BigDecimal.valueOf(1000000))
            .volatility(BigDecimal.valueOf(0.15))
            .marketCondition("NORMAL")
            .build();

        // 샘플 SimpleTradingModel 생성
        sampleModel = MLModelManagementService.SimpleTradingModel.builder()
            .modelId("MODEL_001")
            .symbol(sampleSymbol)
            .accuracy(BigDecimal.valueOf(0.85))
            .lastTrainingTime(LocalDateTime.now().minusDays(1))
            .modelType("LOGISTIC_REGRESSION")
            .build();

        // 샘플 TradingSignal 생성
        sampleTradingSignal = TradingSignal.builder()
            .signalId("SIGNAL_001")
            .symbol(sampleSymbol)
            .signalType(SignalType.BUY)
            .confidence(BigDecimal.valueOf(0.85))
            .confidenceLevel(ConfidenceLevel.HIGH)
            .strength(BigDecimal.valueOf(0.7))
            .targetPrice(BigDecimal.valueOf(82000))
            .generatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("단일 심볼 시그널 생성 테스트")
    class SingleSymbolSignalGenerationTests {

        @Test
        @DisplayName("정상적인 시그널 생성 워크플로우가 실행되어야 한다")
        void shouldExecuteNormalSignalGenerationWorkflow() throws Exception {
            // Given
            when(marketFeatureCollectionService.collectMarketFeatures(sampleSymbol))
                .thenReturn(sampleMarketFeatures);
            when(mlModelManagementService.getOrTrainModel(sampleSymbol))
                .thenReturn(sampleModel);
            when(signalGenerationService.generateSignalFromModel(eq(sampleSymbol), eq(sampleModel), eq(sampleMarketFeatures)))
                .thenReturn(sampleTradingSignal);
            when(marketIntelligenceService.enhanceSignalWithMarketIntelligence(eq(sampleTradingSignal), eq(sampleMarketFeatures)))
                .thenReturn(sampleTradingSignal);

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService.generateTradingSignal(sampleSymbol);
            TradingSignal result = future.get();

            // Then
            verify(marketFeatureCollectionService).collectMarketFeatures(sampleSymbol);
            verify(mlModelManagementService).getOrTrainModel(sampleSymbol);
            verify(signalGenerationService).generateSignalFromModel(sampleSymbol, sampleModel, sampleMarketFeatures);
            verify(marketIntelligenceService).enhanceSignalWithMarketIntelligence(sampleTradingSignal, sampleMarketFeatures);

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(sampleSymbol);
            assertThat(result.getSignalType()).isEqualTo(SignalType.BUY);
            assertThat(result.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
        }

        @Test
        @DisplayName("마켓 피처 수집 실패 시 적절히 처리되어야 한다")
        void shouldHandleMarketFeatureCollectionFailure() throws Exception {
            // Given
            when(marketFeatureCollectionService.collectMarketFeatures(sampleSymbol))
                .thenThrow(new RuntimeException("Market data service unavailable"));

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService.generateTradingSignal(sampleSymbol);
            TradingSignal result = future.get();

            // Then
            verify(marketFeatureCollectionService).collectMarketFeatures(sampleSymbol);
            verifyNoInteractions(mlModelManagementService);
            verifyNoInteractions(signalGenerationService);
            verifyNoInteractions(marketIntelligenceService);

            assertThat(result).isNotNull();
            assertThat(result.getSignalType()).isEqualTo(SignalType.HOLD);
            assertThat(result.getConfidenceLevel()).isEqualTo(ConfidenceLevel.LOW);
        }

        @Test
        @DisplayName("모델 로딩/훈련 실패 시 적절히 처리되어야 한다")
        void shouldHandleModelLoadingFailure() throws Exception {
            // Given
            when(marketFeatureCollectionService.collectMarketFeatures(sampleSymbol))
                .thenReturn(sampleMarketFeatures);
            when(mlModelManagementService.getOrTrainModel(sampleSymbol))
                .thenThrow(new RuntimeException("Model training failed"));

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService.generateTradingSignal(sampleSymbol);
            TradingSignal result = future.get();

            // Then
            verify(marketFeatureCollectionService).collectMarketFeatures(sampleSymbol);
            verify(mlModelManagementService).getOrTrainModel(sampleSymbol);
            verifyNoInteractions(signalGenerationService);
            verifyNoInteractions(marketIntelligenceService);

            assertThat(result).isNotNull();
            assertThat(result.getSignalType()).isEqualTo(SignalType.HOLD);
            assertThat(result.getConfidenceLevel()).isEqualTo(ConfidenceLevel.LOW);
        }

        @Test
        @DisplayName("비동기 실행이 올바르게 작동해야 한다")
        void shouldExecuteAsynchronously() {
            // Given
            when(marketFeatureCollectionService.collectMarketFeatures(sampleSymbol))
                .thenReturn(sampleMarketFeatures);
            when(mlModelManagementService.getOrTrainModel(sampleSymbol))
                .thenReturn(sampleModel);
            when(signalGenerationService.generateSignalFromModel(any(), any(), any()))
                .thenReturn(sampleTradingSignal);
            when(marketIntelligenceService.enhanceSignalWithMarketIntelligence(any(), any()))
                .thenReturn(sampleTradingSignal);

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService.generateTradingSignal(sampleSymbol);

            // Then
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse(); // 비동기 실행 확인
        }
    }

    @Nested
    @DisplayName("배치 시그널 생성 테스트")
    class BatchSignalGenerationTests {

        @Test
        @DisplayName("여러 심볼에 대한 배치 시그널 생성이 성공해야 한다")
        void shouldGenerateBatchSignalsSuccessfully() throws Exception {
            // Given
            List<String> symbols = Arrays.asList("005930", "000660", "035420");
            
            for (String symbol : symbols) {
                when(marketFeatureCollectionService.collectMarketFeatures(symbol))
                    .thenReturn(createMarketFeatures(symbol));
                when(mlModelManagementService.getOrTrainModel(symbol))
                    .thenReturn(createModel(symbol));
                when(signalGenerationService.generateSignalFromModel(eq(symbol), any(), any()))
                    .thenReturn(createTradingSignal(symbol));
                when(marketIntelligenceService.enhanceSignalWithMarketIntelligence(any(), any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            }

            // When
            CompletableFuture<List<TradingSignal>> future = mlTradingSignalService.generateBatchTradingSignals(symbols);
            List<TradingSignal> results = future.get();

            // Then
            assertThat(results).hasSize(3);
            for (int i = 0; i < symbols.size(); i++) {
                assertThat(results.get(i).getSymbol()).isEqualTo(symbols.get(i));
            }

            // 모든 서비스가 각 심볼에 대해 호출되었는지 확인
            for (String symbol : symbols) {
                verify(marketFeatureCollectionService).collectMarketFeatures(symbol);
                verify(mlModelManagementService).getOrTrainModel(symbol);
                verify(signalGenerationService).generateSignalFromModel(eq(symbol), any(), any());
                verify(marketIntelligenceService).enhanceSignalWithMarketIntelligence(any(), any());
            }
        }

        @Test
        @DisplayName("일부 심볼에서 실패가 발생해도 다른 심볼은 처리되어야 한다")
        void shouldContinueProcessingOtherSymbolsOnPartialFailure() throws Exception {
            // Given
            List<String> symbols = Arrays.asList("005930", "000660", "035420");
            
            // 첫 번째 심볼은 성공
            when(marketFeatureCollectionService.collectMarketFeatures("005930"))
                .thenReturn(createMarketFeatures("005930"));
            when(mlModelManagementService.getOrTrainModel("005930"))
                .thenReturn(createModel("005930"));
            when(signalGenerationService.generateSignalFromModel(eq("005930"), any(), any()))
                .thenReturn(createTradingSignal("005930"));
            when(marketIntelligenceService.enhanceSignalWithMarketIntelligence(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // 두 번째 심볼은 실패
            when(marketFeatureCollectionService.collectMarketFeatures("000660"))
                .thenThrow(new RuntimeException("Data unavailable"));

            // 세 번째 심볼은 성공
            when(marketFeatureCollectionService.collectMarketFeatures("035420"))
                .thenReturn(createMarketFeatures("035420"));
            when(mlModelManagementService.getOrTrainModel("035420"))
                .thenReturn(createModel("035420"));
            when(signalGenerationService.generateSignalFromModel(eq("035420"), any(), any()))
                .thenReturn(createTradingSignal("035420"));

            // When
            CompletableFuture<List<TradingSignal>> future = mlTradingSignalService.generateBatchTradingSignals(symbols);
            List<TradingSignal> results = future.get();

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getSymbol()).isEqualTo("005930");
            assertThat(results.get(1).getSignalType()).isEqualTo(SignalType.HOLD); // 실패로 인한 기본값
            assertThat(results.get(2).getSymbol()).isEqualTo("035420");
        }

        @Test
        @DisplayName("빈 심볼 리스트에 대해 빈 결과를 반환해야 한다")
        void shouldReturnEmptyResultForEmptySymbolList() throws Exception {
            // Given
            List<String> emptySymbols = Arrays.asList();

            // When
            CompletableFuture<List<TradingSignal>> future = mlTradingSignalService.generateBatchTradingSignals(emptySymbols);
            List<TradingSignal> results = future.get();

            // Then
            assertThat(results).isEmpty();
            verifyNoInteractions(marketFeatureCollectionService);
            verifyNoInteractions(mlModelManagementService);
            verifyNoInteractions(signalGenerationService);
            verifyNoInteractions(marketIntelligenceService);
        }
    }

    @Nested
    @DisplayName("백테스팅용 시그널 생성 테스트")
    class BacktestingSignalGenerationTests {

        @Test
        @DisplayName("백테스팅 시그널 생성이 정상적으로 작동해야 한다")
        void shouldGenerateBacktestingSignalSuccessfully() throws Exception {
            // Given
            LocalDateTime historicalTime = LocalDateTime.now().minusDays(10);
            when(marketFeatureCollectionService.collectHistoricalMarketFeatures(sampleSymbol, historicalTime))
                .thenReturn(sampleMarketFeatures);
            when(mlModelManagementService.getOrTrainModel(sampleSymbol))
                .thenReturn(sampleModel);
            when(signalGenerationService.generateSignalFromModel(eq(sampleSymbol), eq(sampleModel), eq(sampleMarketFeatures)))
                .thenReturn(sampleTradingSignal);

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService
                .generateTradingSignalForBacktest(sampleSymbol, historicalTime);
            TradingSignal result = future.get();

            // Then
            verify(marketFeatureCollectionService).collectHistoricalMarketFeatures(sampleSymbol, historicalTime);
            verify(mlModelManagementService).getOrTrainModel(sampleSymbol);
            verify(signalGenerationService).generateSignalFromModel(sampleSymbol, sampleModel, sampleMarketFeatures);
            // 백테스팅에서는 마켓 인텔리전스 향상을 생략
            verifyNoInteractions(marketIntelligenceService);

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(sampleSymbol);
        }

        @Test
        @DisplayName("과거 데이터 수집 실패 시 적절히 처리되어야 한다")
        void shouldHandleHistoricalDataCollectionFailure() throws Exception {
            // Given
            LocalDateTime historicalTime = LocalDateTime.now().minusDays(10);
            when(marketFeatureCollectionService.collectHistoricalMarketFeatures(sampleSymbol, historicalTime))
                .thenThrow(new RuntimeException("Historical data not available"));

            // When
            CompletableFuture<TradingSignal> future = mlTradingSignalService
                .generateTradingSignalForBacktest(sampleSymbol, historicalTime);
            TradingSignal result = future.get();

            // Then
            verify(marketFeatureCollectionService).collectHistoricalMarketFeatures(sampleSymbol, historicalTime);
            verifyNoInteractions(mlModelManagementService);
            verifyNoInteractions(signalGenerationService);
            verifyNoInteractions(marketIntelligenceService);

            assertThat(result).isNotNull();
            assertThat(result.getSignalType()).isEqualTo(SignalType.HOLD);
            assertThat(result.getConfidenceLevel()).isEqualTo(ConfidenceLevel.LOW);
        }
    }

    @Nested
    @DisplayName("오류 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("null 심볼에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullSymbol() {
            // When & Then
            assertThatThrownBy(() -> mlTradingSignalService.generateTradingSignal(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol cannot be null or empty");
        }

        @Test
        @DisplayName("빈 심볼에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForEmptySymbol() {
            // When & Then
            assertThatThrownBy(() -> mlTradingSignalService.generateTradingSignal(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol cannot be null or empty");
        }

        @Test
        @DisplayName("null 심볼 리스트에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullSymbolList() {
            // When & Then
            assertThatThrownBy(() -> mlTradingSignalService.generateBatchTradingSignals(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol list cannot be null");
        }

        @Test
        @DisplayName("과거 시간이 null일 때 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullHistoricalTime() {
            // When & Then
            assertThatThrownBy(() -> mlTradingSignalService.generateTradingSignalForBacktest(sampleSymbol, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Historical time cannot be null");
        }

        @Test
        @DisplayName("미래 시간에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForFutureTime() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

            // When & Then
            assertThatThrownBy(() -> mlTradingSignalService.generateTradingSignalForBacktest(sampleSymbol, futureTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Historical time cannot be in the future");
        }
    }

    // 헬퍼 메서드
    private MarketFeatureCollectionService.MarketFeatures createMarketFeatures(String symbol) {
        return MarketFeatureCollectionService.MarketFeatures.builder()
            .symbol(symbol)
            .collectTime(LocalDateTime.now())
            .currentPrice(BigDecimal.valueOf(80000))
            .volume(BigDecimal.valueOf(1000000))
            .volatility(BigDecimal.valueOf(0.15))
            .marketCondition("NORMAL")
            .build();
    }

    private MLModelManagementService.SimpleTradingModel createModel(String symbol) {
        return MLModelManagementService.SimpleTradingModel.builder()
            .modelId("MODEL_" + symbol)
            .symbol(symbol)
            .accuracy(BigDecimal.valueOf(0.85))
            .lastTrainingTime(LocalDateTime.now().minusDays(1))
            .modelType("LOGISTIC_REGRESSION")
            .build();
    }

    private TradingSignal createTradingSignal(String symbol) {
        return TradingSignal.builder()
            .signalId("SIGNAL_" + symbol)
            .symbol(symbol)
            .signalType(SignalType.BUY)
            .confidence(BigDecimal.valueOf(0.85))
            .confidenceLevel(ConfidenceLevel.HIGH)
            .strength(BigDecimal.valueOf(0.7))
            .targetPrice(BigDecimal.valueOf(82000))
            .generatedAt(LocalDateTime.now())
            .build();
    }
}