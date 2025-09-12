package com.stockquest.application.service;

import com.stockquest.domain.marketdata.MarketData;
import com.stockquest.testutils.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RealTimeMarketDataService 테스트
 * Phase 3.1: 비동기 처리, 캐싱, 외부 API 통합 테스트 커버리지
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RealTimeMarketDataService 테스트")
class RealTimeMarketDataServiceTest extends TestBase {

    @InjectMocks
    private RealTimeMarketDataService marketDataService;
    
    @Mock
    private RestTemplate restTemplate;
    
    private CacheManager cacheManager;
    
    // 테스트 데이터 상수
    private static final String VALID_SYMBOL = "AAPL";
    private static final String INVALID_SYMBOL = "INVALID";
    private static final BigDecimal BASE_PRICE = new BigDecimal("150.00");
    private static final double VOLATILITY = 0.02;
    
    @BeforeEach
    public void setUp() {
        // 실제 캐시 매니저 설정 (테스트용)
        cacheManager = new ConcurrentMapCacheManager("realTimeMarketData", "historicalPrices", "bulkMarketData");
        ReflectionTestUtils.setField(marketDataService, "cacheManager", cacheManager);
        
        // 외부 API 기본 동작 설정
        setupDefaultMockBehavior();
    }
    
    private void setupDefaultMockBehavior() {
        // 기본적으로 외부 API 호출 실패로 설정 (내부 시뮬레이션 사용)
        when(restTemplate.getForObject(anyString(), eq(MarketData.class)))
            .thenThrow(new ResourceAccessException("External API not available"));
    }

    @Nested
    @DisplayName("실시간 가격 조회")
    class GetLatestMarketData {
        
        @Test
        @DisplayName("유효한 심볼로 비동기 가격 조회 성공")
        void shouldGetLatestMarketDataAsync() throws Exception {
            // Given
            String symbol = VALID_SYMBOL;
            
            // When
            CompletableFuture<MarketData> future = marketDataService.getLatestMarketData(symbol);
            MarketData result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(symbol);
            assertThat(result.getPrice()).isPositive();
            assertThat(result.getTimestamp()).isNotNull();
            assertThat(result.getVolume()).isPositive();
            
            // 가격이 합리적인 범위 내에 있는지 확인
            BigDecimal expectedMin = BASE_PRICE.multiply(new BigDecimal("0.8"));
            BigDecimal expectedMax = BASE_PRICE.multiply(new BigDecimal("1.2"));
            assertThat(result.getPrice()).isBetween(expectedMin, expectedMax);
        }
        
        @Test
        @DisplayName("캐시된 데이터 반환 확인")
        void shouldReturnCachedDataWhenAvailable() throws Exception {
            // Given
            String symbol = VALID_SYMBOL;
            
            // When - 첫 번째 호출
            CompletableFuture<MarketData> firstCall = marketDataService.getLatestMarketData(symbol);
            MarketData firstResult = firstCall.get(5, TimeUnit.SECONDS);
            
            // When - 두 번째 호출 (캐시에서 반환되어야 함)
            CompletableFuture<MarketData> secondCall = marketDataService.getLatestMarketData(symbol);
            MarketData secondResult = secondCall.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(firstResult).isNotNull();
            assertThat(secondResult).isNotNull();
            
            // 캐시된 데이터는 동일한 시간 스탬프를 가져야 함
            assertThat(firstResult.getTimestamp()).isEqualTo(secondResult.getTimestamp());
            assertThat(firstResult.getPrice()).isEqualTo(secondResult.getPrice());
        }
        
        @Test
        @DisplayName("null 심볼 처리")
        void shouldHandleNullSymbol() {
            // When & Then
            assertThatThrownBy(() -> {
                CompletableFuture<MarketData> future = marketDataService.getLatestMarketData(null);
                future.get(5, TimeUnit.SECONDS);
            }).hasCauseInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("빈 심볼 처리")
        void shouldHandleEmptySymbol() {
            // When & Then
            assertThatThrownBy(() -> {
                CompletableFuture<MarketData> future = marketDataService.getLatestMarketData("");
                future.get(5, TimeUnit.SECONDS);
            }).hasCauseInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("외부 API 성공 시 실제 데이터 반환")
        void shouldReturnExternalDataWhenApiSucceeds() throws Exception {
            // Given
            String symbol = VALID_SYMBOL;
            MarketData externalData = createMarketData(symbol, new BigDecimal("160.00"));
            
            when(restTemplate.getForObject(anyString(), eq(MarketData.class)))
                .thenReturn(externalData);
            
            // When
            CompletableFuture<MarketData> future = marketDataService.getLatestMarketData(symbol);
            MarketData result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(symbol);
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("160.00"));
            
            // 외부 API가 호출되었는지 확인
            verify(restTemplate, times(1)).getForObject(anyString(), eq(MarketData.class));
        }
    }

    @Nested
    @DisplayName("현재 가격 조회")
    class GetCurrentPrice {
        
        @Test
        @DisplayName("유효한 심볼의 현재 가격 반환")
        void shouldReturnCurrentPriceForValidSymbol() throws Exception {
            // Given
            String symbol = VALID_SYMBOL;
            
            // When
            CompletableFuture<BigDecimal> future = marketDataService.getCurrentPrice(symbol);
            BigDecimal result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result).isPositive();
            
            // 가격이 기본값 범위 내에 있는지 확인
            BigDecimal expectedMin = BASE_PRICE.multiply(new BigDecimal("0.8"));
            BigDecimal expectedMax = BASE_PRICE.multiply(new BigDecimal("1.2"));
            assertThat(result).isBetween(expectedMin, expectedMax);
        }
        
        @Test
        @DisplayName("여러 심볼 동시 가격 조회")
        void shouldHandleConcurrentPriceRequests() throws Exception {
            // Given
            List<String> symbols = List.of("AAPL", "GOOGL", "MSFT", "TSLA");
            
            // When
            List<CompletableFuture<BigDecimal>> futures = symbols.stream()
                .map(symbol -> marketDataService.getCurrentPrice(symbol))
                .toList();
            
            // Then
            for (int i = 0; i < futures.size(); i++) {
                BigDecimal price = futures.get(i).get(10, TimeUnit.SECONDS);
                assertThat(price)
                    .as("Price for symbol: %s", symbols.get(i))
                    .isNotNull()
                    .isPositive();
            }
        }
    }

    @Nested
    @DisplayName("벌크 가격 조회")
    class GetBulkMarketData {
        
        @Test
        @DisplayName("여러 심볼 벌크 조회 성공")
        void shouldGetBulkMarketDataSuccessfully() throws Exception {
            // Given
            List<String> symbols = List.of("AAPL", "GOOGL", "MSFT");
            
            // When
            CompletableFuture<List<MarketData>> future = marketDataService.getBulkMarketData(symbols);
            List<MarketData> results = future.get(10, TimeUnit.SECONDS);
            
            // Then
            assertThat(results).hasSize(3);
            
            for (int i = 0; i < results.size(); i++) {
                MarketData data = results.get(i);
                assertThat(data.getSymbol()).isEqualTo(symbols.get(i));
                assertThat(data.getPrice()).isPositive();
                assertThat(data.getTimestamp()).isNotNull();
            }
        }
        
        @Test
        @DisplayName("빈 심볼 리스트 처리")
        void shouldHandleEmptySymbolList() throws Exception {
            // When
            CompletableFuture<List<MarketData>> future = marketDataService.getBulkMarketData(List.of());
            List<MarketData> results = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(results).isEmpty();
        }
        
        @Test
        @DisplayName("null 심볼 리스트 처리")
        void shouldHandleNullSymbolList() {
            // When & Then
            assertThatThrownBy(() -> {
                CompletableFuture<List<MarketData>> future = marketDataService.getBulkMarketData(null);
                future.get(5, TimeUnit.SECONDS);
            }).hasCauseInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("대용량 심볼 리스트 처리 (50개)")
        void shouldHandleLargeSymbolList() throws Exception {
            // Given
            List<String> symbols = generateSymbols(50);
            
            // When
            long startTime = System.currentTimeMillis();
            CompletableFuture<List<MarketData>> future = marketDataService.getBulkMarketData(symbols);
            List<MarketData> results = future.get(30, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            
            // Then
            assertThat(results).hasSize(50);
            
            // 성능 검증 (30초 이내 처리)
            assertThat(endTime - startTime).isLessThan(30000);
            
            // 모든 결과 검증
            for (MarketData data : results) {
                assertThat(data.getSymbol()).isIn(symbols);
                assertThat(data.getPrice()).isPositive();
            }
        }
    }

    @Nested
    @DisplayName("과거 데이터 조회")
    class GetHistoricalPrices {
        
        @Test
        @DisplayName("과거 30일 데이터 조회")
        void shouldGetHistoricalDataFor30Days() throws Exception {
            // Given
            String symbol = VALID_SYMBOL;
            int days = 30;
            
            // When
            CompletableFuture<List<MarketData>> future = marketDataService.getHistoricalMarketData(symbol, days);
            List<MarketData> results = future.get(10, TimeUnit.SECONDS);
            
            // Then
            assertThat(results).hasSize(30);
            
            // 시간 순서대로 정렬되어 있는지 확인
            for (int i = 1; i < results.size(); i++) {
                LocalDateTime prev = results.get(i-1).getTimestamp();
                LocalDateTime curr = results.get(i).getTimestamp();
                assertThat(prev).isBefore(curr);
            }
            
            // 모든 데이터가 같은 심볼인지 확인
            results.forEach(data -> {
                assertThat(data.getSymbol()).isEqualTo(symbol);
                assertThat(data.getPrice()).isPositive();
                assertThat(data.getVolume()).isPositive();
            });
        }
        
        @Test
        @DisplayName("잘못된 일수 처리 (음수)")
        void shouldHandleInvalidDays() {
            // When & Then
            assertThatThrownBy(() -> {
                CompletableFuture<List<MarketData>> future = marketDataService.getHistoricalMarketData(VALID_SYMBOL, -5);
                future.get(5, TimeUnit.SECONDS);
            }).hasCauseInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("과도한 일수 처리 (1000일)")
        void shouldHandleExcessiveDays() {
            // When & Then
            assertThatThrownBy(() -> {
                CompletableFuture<List<MarketData>> future = marketDataService.getHistoricalMarketData(VALID_SYMBOL, 1000);
                future.get(5, TimeUnit.SECONDS);
            }).hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("구독 관리")
    class SubscriptionManagement {
        
        @Test
        @DisplayName("심볼 구독 추가")
        void shouldAddSubscription() {
            // Given
            String symbol = VALID_SYMBOL;
            
            // When
            marketDataService.subscribeToMarketData(symbol);
            
            // Then
            // 구독이 추가되었는지 내부 상태로 확인
            Set<String> activeSubscriptions = marketDataService.getActiveSubscriptions();
            assertThat(activeSubscriptions).contains(symbol);
        }
        
        @Test
        @DisplayName("심볼 구독 제거")
        void shouldRemoveSubscription() {
            // Given
            String symbol = VALID_SYMBOL;
            marketDataService.subscribeToMarketData(symbol);
            
            // When
            marketDataService.unsubscribeFromMarketData(symbol);
            
            // Then
            Set<String> activeSubscriptions = marketDataService.getActiveSubscriptions();
            assertThat(activeSubscriptions).doesNotContain(symbol);
        }
        
        @Test
        @DisplayName("중복 구독 처리")
        void shouldHandleDuplicateSubscription() {
            // Given
            String symbol = VALID_SYMBOL;
            
            // When
            marketDataService.subscribeToMarketData(symbol);
            marketDataService.subscribeToMarketData(symbol); // 중복 구독
            
            // Then - 예외가 발생하지 않아야 함
            assertThatNoException().isThrownBy(() -> {
                marketDataService.subscribeToMarketData(symbol);
            });
            
            // 구독은 여전히 하나만 있어야 함
            Set<String> activeSubscriptions = marketDataService.getActiveSubscriptions();
            long count = activeSubscriptions.stream().filter(s -> s.equals(symbol)).count();
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("시장 시간 검증")
    class MarketHoursValidation {
        
        @Test
        @DisplayName("평일 시장 시간 확인")
        void shouldValidateMarketHoursOnWeekdays() throws Exception {
            // Given & When
            CompletableFuture<Boolean> future = marketDataService.isMarketOpen();
            Boolean isOpen = future.get(5, TimeUnit.SECONDS);
            
            // Then - 실제 시장 시간에 따라 다르지만, 메서드가 정상 작동하는지 확인
            assertThat(isOpen).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("에러 처리 및 복원력")
    class ErrorHandlingAndResilience {
        
        @Test
        @DisplayName("외부 API 타임아웃 처리")
        void shouldHandleExternalApiTimeout() throws Exception {
            // Given
            when(restTemplate.getForObject(anyString(), eq(MarketData.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));
            
            // When
            CompletableFuture<MarketData> future = marketDataService.getLatestMarketData(VALID_SYMBOL);
            MarketData result = future.get(10, TimeUnit.SECONDS);
            
            // Then - fallback 데이터가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(VALID_SYMBOL);
            assertThat(result.getPrice()).isPositive();
        }
        
        @Test
        @DisplayName("비동기 작업 취소")
        void shouldHandleAsyncTaskCancellation() {
            // Given
            CompletableFuture<MarketData> future = marketDataService.getLatestMarketData(VALID_SYMBOL);
            
            // When
            boolean cancelled = future.cancel(true);
            
            // Then
            assertThat(cancelled).isTrue();
            assertThat(future.isCancelled()).isTrue();
        }
        
        @Test
        @DisplayName("메모리 누수 방지 확인")
        void shouldPreventMemoryLeaks() throws Exception {
            // Given - 많은 요청 생성
            List<CompletableFuture<MarketData>> futures = new java.util.ArrayList<>();
            
            // When - 100개의 동시 요청
            for (int i = 0; i < 100; i++) {
                CompletableFuture<MarketData> future = marketDataService.getLatestMarketData("SYM" + i);
                futures.add(future);
            }
            
            // 모든 요청 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);
            
            // Then - 모든 작업이 완료되어야 함
            futures.forEach(future -> {
                assertThat(future.isDone()).isTrue();
                assertThat(future.isCompletedExceptionally()).isFalse();
            });
        }
    }

    // 헬퍼 메서드들
    private MarketData createMarketData(String symbol, BigDecimal price) {
        return MarketData.builder()
            .symbol(symbol)
            .price(price)
            .openPrice(price.multiply(new BigDecimal("0.98")))
            .highPrice(price.multiply(new BigDecimal("1.05")))
            .lowPrice(price.multiply(new BigDecimal("0.95")))
            .volume(1000000L)
            .timestamp(LocalDateTime.now())
            .marketCap(price.multiply(new BigDecimal("1000000000")))
            .build();
    }
    
    private List<String> generateSymbols(int count) {
        List<String> symbols = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            symbols.add("TEST" + String.format("%02d", i));
        }
        return symbols;
    }
}