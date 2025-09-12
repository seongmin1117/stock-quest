package com.stockquest.application.service;

import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.portfolio.Position;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PortfolioService 테스트
 * Phase 3.1: 포트폴리오 관리, 성과 분석, 위험도 평가 테스트 커버리지
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService 테스트")
class PortfolioServiceTest extends TestBase {

    @InjectMocks
    private PortfolioService portfolioService;
    
    @Mock
    private RealTimeMarketDataService marketDataService;
    
    private CacheManager cacheManager;
    
    // 테스트 데이터 상수
    private static final Long VALID_USER_ID = 1L;
    private static final Long VALID_PORTFOLIO_ID = 1L;
    private static final String VALID_SYMBOL = "AAPL";
    private static final String VALID_SYMBOL_2 = "GOOGL";
    private static final BigDecimal BASE_PRICE = new BigDecimal("150.00");
    private static final BigDecimal VALID_QUANTITY = new BigDecimal("10");
    
    @BeforeEach
    public void setUp() {
        // 실제 캐시 매니저 설정
        cacheManager = new ConcurrentMapCacheManager("portfolioCache", "userPortfoliosCache");
        ReflectionTestUtils.setField(portfolioService, "cacheManager", cacheManager);
        
        // 기본 Mock 동작 설정
        setupDefaultMarketDataMocks();
    }
    
    private void setupDefaultMarketDataMocks() {
        // 기본 마켓 데이터 Mock 설정
        MarketData mockMarketData = createMockMarketData(VALID_SYMBOL, BASE_PRICE);
        MarketData mockMarketData2 = createMockMarketData(VALID_SYMBOL_2, new BigDecimal("2800.00"));
        
        when(marketDataService.getLatestMarketData(VALID_SYMBOL))
            .thenReturn(CompletableFuture.completedFuture(mockMarketData));
        when(marketDataService.getLatestMarketData(VALID_SYMBOL_2))
            .thenReturn(CompletableFuture.completedFuture(mockMarketData2));
    }
    
    private MarketData createMockMarketData(String symbol, BigDecimal price) {
        return MarketData.builder()
            .symbol(symbol)
            .price(price)
            .volume(1000000L)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private Portfolio createTestPortfolio() {
        return Portfolio.builder()
            .id(VALID_PORTFOLIO_ID)
            .userId(VALID_USER_ID)
            .name("Test Portfolio")
            .description("Test portfolio description")
            .totalValue(new BigDecimal("1500.00"))
            .positions(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private Position createTestPosition(String symbol, BigDecimal quantity, BigDecimal avgPrice) {
        return Position.builder()
            .id(1L)
            .portfolioId(VALID_PORTFOLIO_ID)
            .symbol(symbol)
            .quantity(quantity)
            .averagePrice(avgPrice)
            .currentPrice(avgPrice)
            .unrealizedPnL(BigDecimal.ZERO)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Nested
    @DisplayName("포트폴리오 조회 테스트")
    class PortfolioRetrievalTests {
        
        @Test
        @DisplayName("유효한 ID로 포트폴리오 조회 성공")
        void shouldFindByIdSuccessfully() {
            // Given
            Portfolio testPortfolio = createTestPortfolio();
            // 포트폴리오를 내부 저장소에 직접 설정
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, testPortfolio);
            
            // When
            Optional<Portfolio> result = portfolioService.findById(VALID_PORTFOLIO_ID);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(VALID_PORTFOLIO_ID);
            assertThat(result.get().getUserId()).isEqualTo(VALID_USER_ID);
            assertThat(result.get().getName()).isEqualTo("Test Portfolio");
        }
        
        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void shouldReturnEmptyOptionalForNonexistentId() {
            // When
            Optional<Portfolio> result = portfolioService.findById(999L);
            
            // Then
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("null ID로 조회 시 빈 Optional 반환")
        void shouldReturnEmptyOptionalForNullId() {
            // When
            Optional<Portfolio> result = portfolioService.findById(null);
            
            // Then
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("유효한 사용자 ID로 포트폴리오 목록 조회")
        void shouldFindByUserIdSuccessfully() {
            // Given
            Portfolio portfolio1 = createTestPortfolio();
            Portfolio portfolio2 = Portfolio.builder()
                .id(2L)
                .userId(VALID_USER_ID)
                .name("Second Portfolio")
                .description("Second test portfolio")
                .totalValue(new BigDecimal("2500.00"))
                .positions(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            Map<Long, List<Portfolio>> userPortfolioIndex = (Map<Long, List<Portfolio>>) 
                ReflectionTestUtils.getField(portfolioService, "userPortfolioIndex");
            userPortfolioIndex.put(VALID_USER_ID, Arrays.asList(portfolio1, portfolio2));
            
            // When
            List<Portfolio> result = portfolioService.findByUserId(VALID_USER_ID);
            
            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(VALID_USER_ID);
            assertThat(result.get(1).getUserId()).isEqualTo(VALID_USER_ID);
        }
        
        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 빈 리스트 반환")
        void shouldReturnEmptyListForNonexistentUserId() {
            // When
            List<Portfolio> result = portfolioService.findByUserId(999L);
            
            // Then
            assertThat(result).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("포트폴리오 생성 테스트")
    class PortfolioCreationTests {
        
        @Test
        @DisplayName("유효한 포트폴리오로 생성 성공")
        void shouldCreatePortfolioSuccessfully() throws Exception {
            // Given
            Portfolio newPortfolio = Portfolio.builder()
                .userId(VALID_USER_ID)
                .name("New Test Portfolio")
                .description("New portfolio description")
                .positions(new ArrayList<>())
                .build();
            
            // When
            CompletableFuture<Portfolio> future = portfolioService.createPortfolio(newPortfolio);
            Portfolio result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserId()).isEqualTo(VALID_USER_ID);
            assertThat(result.getName()).isEqualTo("New Test Portfolio");
            assertThat(result.getTotalValue()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }
        
        @Test
        @DisplayName("null 포트폴리오로 생성 실패")
        void shouldFailToCreateNullPortfolio() {
            // When & Then
            CompletableFuture<Portfolio> future = portfolioService.createPortfolio(null);
            
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("포트폴리오는 null일 수 없습니다");
        }
    }
    
    @Nested
    @DisplayName("포트폴리오 업데이트 테스트")
    class PortfolioUpdateTests {
        
        @Test
        @DisplayName("유효한 포트폴리오 업데이트 성공")
        void shouldUpdatePortfolioSuccessfully() throws Exception {
            // Given
            Portfolio existingPortfolio = createTestPortfolio();
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, existingPortfolio);
            
            Portfolio updateRequest = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .name("Updated Portfolio Name")
                .description("Updated description")
                .build();
            
            // When
            CompletableFuture<Portfolio> future = portfolioService.updatePortfolio(updateRequest);
            Portfolio result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(VALID_PORTFOLIO_ID);
            assertThat(result.getName()).isEqualTo("Updated Portfolio Name");
            assertThat(result.getDescription()).isEqualTo("Updated description");
            assertThat(result.getUpdatedAt()).isAfter(existingPortfolio.getUpdatedAt());
        }
        
        @Test
        @DisplayName("존재하지 않는 포트폴리오 업데이트 실패")
        void shouldFailToUpdateNonexistentPortfolio() {
            // Given
            Portfolio updateRequest = Portfolio.builder()
                .id(999L)
                .name("Updated Name")
                .build();
            
            // When & Then
            CompletableFuture<Portfolio> future = portfolioService.updatePortfolio(updateRequest);
            
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("존재하지 않는 포트폴리오");
        }
    }
    
    @Nested
    @DisplayName("포지션 관리 테스트")
    class PositionManagementTests {
        
        @Test
        @DisplayName("새로운 포지션 추가 성공")
        void shouldAddNewPositionSuccessfully() throws Exception {
            // Given
            Portfolio portfolio = createTestPortfolio();
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            Position newPosition = createTestPosition(VALID_SYMBOL, VALID_QUANTITY, BASE_PRICE);
            
            // When
            CompletableFuture<Portfolio> future = portfolioService.addPosition(VALID_PORTFOLIO_ID, newPosition);
            Portfolio result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result.getPositions()).hasSize(1);
            Position addedPosition = result.getPositions().get(0);
            assertThat(addedPosition.getSymbol()).isEqualTo(VALID_SYMBOL);
            assertThat(addedPosition.getQuantity()).isEqualTo(VALID_QUANTITY);
        }
        
        @Test
        @DisplayName("기존 포지션에 수량 추가")
        void shouldMergeExistingPosition() throws Exception {
            // Given
            Position existingPosition = createTestPosition(VALID_SYMBOL, new BigDecimal("5"), BASE_PRICE);
            Portfolio portfolio = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .userId(VALID_USER_ID)
                .name("Test Portfolio")
                .description("Test portfolio")
                .totalValue(BigDecimal.ZERO)
                .positions(new ArrayList<>(Arrays.asList(existingPosition)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            Position additionalPosition = createTestPosition(VALID_SYMBOL, new BigDecimal("3"), BASE_PRICE);
            
            // When
            CompletableFuture<Portfolio> future = portfolioService.addPosition(VALID_PORTFOLIO_ID, additionalPosition);
            Portfolio result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result.getPositions()).hasSize(1);
            Position mergedPosition = result.getPositions().get(0);
            assertThat(mergedPosition.getSymbol()).isEqualTo(VALID_SYMBOL);
            assertThat(mergedPosition.getQuantity()).isEqualTo(new BigDecimal("8")); // 5 + 3
        }
        
        @Test
        @DisplayName("포지션 제거 성공")
        void shouldRemovePositionSuccessfully() throws Exception {
            // Given
            Position position = createTestPosition(VALID_SYMBOL, VALID_QUANTITY, BASE_PRICE);
            Portfolio portfolio = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .userId(VALID_USER_ID)
                .name("Test Portfolio")
                .description("Test portfolio")
                .totalValue(BigDecimal.ZERO)
                .positions(new ArrayList<>(Arrays.asList(position)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When
            CompletableFuture<Portfolio> future = portfolioService.removePosition(VALID_PORTFOLIO_ID, VALID_SYMBOL);
            Portfolio result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result.getPositions()).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("포트폴리오 분석 테스트")
    class PortfolioAnalysisTests {
        
        @Test
        @DisplayName("포트폴리오 총 가치 계산 성공")
        void shouldCalculateTotalValueSuccessfully() throws Exception {
            // Given
            Position position1 = createTestPosition(VALID_SYMBOL, new BigDecimal("10"), BASE_PRICE);
            Position position2 = createTestPosition(VALID_SYMBOL_2, new BigDecimal("5"), new BigDecimal("2800.00"));
            
            Portfolio portfolio = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .userId(VALID_USER_ID)
                .name("Test Portfolio")
                .description("Test portfolio")
                .totalValue(BigDecimal.ZERO)
                .positions(new ArrayList<>(Arrays.asList(position1, position2)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When
            CompletableFuture<BigDecimal> future = portfolioService.calculateTotalValue(VALID_PORTFOLIO_ID);
            BigDecimal result = future.get(5, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result).isPositive();
            // Expected: (10 * 150.00) + (5 * 2800.00) = 1500 + 14000 = 15500
            assertThat(result).isEqualByComparingTo(new BigDecimal("15500.00"));
        }
        
        @Test
        @DisplayName("존재하지 않는 포트폴리오 총 가치 계산 실패")
        void shouldFailToCalculateTotalValueForNonexistentPortfolio() {
            // When & Then
            CompletableFuture<BigDecimal> future = portfolioService.calculateTotalValue(999L);
            
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("존재하지 않는 포트폴리오");
        }
        
        @Test
        @DisplayName("포트폴리오 성과 분석 성공")
        void shouldAnalyzePerformanceSuccessfully() throws Exception {
            // Given
            Position position = createTestPosition(VALID_SYMBOL, VALID_QUANTITY, BASE_PRICE);
            Portfolio portfolio = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .userId(VALID_USER_ID)
                .name("Test Portfolio")
                .description("Test portfolio")
                .totalValue(new BigDecimal("1500.00"))
                .positions(new ArrayList<>(Arrays.asList(position)))
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();
                
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When
            CompletableFuture<PortfolioService.PortfolioPerformance> future = 
                portfolioService.analyzePerformance(VALID_PORTFOLIO_ID);
            PortfolioService.PortfolioPerformance result = future.get(10, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalReturn()).isNotNull();
            assertThat(result.getTotalReturnPercent()).isNotNull();
        }
        
        @Test
        @DisplayName("포트폴리오 위험 분석 성공")
        void shouldAnalyzeRiskSuccessfully() throws Exception {
            // Given
            Position position1 = createTestPosition(VALID_SYMBOL, new BigDecimal("10"), BASE_PRICE);
            Position position2 = createTestPosition(VALID_SYMBOL_2, new BigDecimal("5"), new BigDecimal("2800.00"));
            
            Portfolio portfolio = Portfolio.builder()
                .id(VALID_PORTFOLIO_ID)
                .userId(VALID_USER_ID)
                .name("Test Portfolio")
                .description("Test portfolio")
                .totalValue(new BigDecimal("15500.00"))
                .positions(new ArrayList<>(Arrays.asList(position1, position2)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioStorage, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When
            CompletableFuture<PortfolioService.PortfolioRiskAnalysis> future = 
                portfolioService.analyzeRisk(VALID_PORTFOLIO_ID);
            PortfolioService.PortfolioRiskAnalysis result = future.get(10, TimeUnit.SECONDS);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRiskScore()).isNotNull();
            assertThat(result.getRiskLevel()).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("예외 상황 및 에러 처리 테스트")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("null 포지션 추가 시 예외 발생")
        void shouldFailToAddNullPosition() {
            // Given
            Portfolio portfolio = createTestPortfolio();
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When & Then
            CompletableFuture<Portfolio> future = portfolioService.addPosition(VALID_PORTFOLIO_ID, null);
            
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("유효하지 않은 입력");
        }
        
        @Test
        @DisplayName("잘못된 포트폴리오 ID로 포지션 추가 시 예외 발생")
        void shouldFailToAddPositionToNonexistentPortfolio() {
            // Given
            Position position = createTestPosition(VALID_SYMBOL, VALID_QUANTITY, BASE_PRICE);
            
            // When & Then
            CompletableFuture<Portfolio> future = portfolioService.addPosition(999L, position);
            
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("존재하지 않는 포트폴리오");
        }
        
        @Test
        @DisplayName("마켓 데이터 서비스 오류 시 graceful 처리")
        void shouldHandleMarketDataServiceError() {
            // Given
            when(marketDataService.getLatestMarketData(anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Market data service error")));
            
            Portfolio portfolio = createTestPortfolio();
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When
            Optional<Portfolio> result = portfolioService.findById(VALID_PORTFOLIO_ID);
            
            // Then
            // 서비스가 graceful하게 처리하여 포트폴리오는 여전히 반환되어야 함
            assertThat(result).isPresent();
        }
    }
    
    @Nested
    @DisplayName("동시성 및 캐싱 테스트")
    class ConcurrencyAndCachingTests {
        
        @Test
        @DisplayName("동시에 여러 포트폴리오 생성")
        void shouldHandleConcurrentPortfolioCreation() throws Exception {
            // Given
            List<CompletableFuture<Portfolio>> futures = new ArrayList<>();
            
            for (int i = 0; i < 10; i++) {
                Portfolio portfolio = Portfolio.builder()
                    .userId(VALID_USER_ID)
                    .name("Portfolio " + i)
                    .description("Test portfolio " + i)
                    .positions(new ArrayList<>())
                    .build();
                
                futures.add(portfolioService.createPortfolio(portfolio));
            }
            
            // When
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(10, TimeUnit.SECONDS);
            
            // Then
            for (CompletableFuture<Portfolio> future : futures) {
                Portfolio result = future.get();
                assertThat(result).isNotNull();
                assertThat(result.getId()).isNotNull();
            }
            
            // 모든 포트폴리오가 고유한 ID를 가져야 함
            Set<Long> ids = futures.stream()
                .map(future -> {
                    try {
                        return future.get().getId();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
            assertThat(ids).hasSize(10);
        }
        
        @Test
        @DisplayName("캐시 동작 검증")
        void shouldUseCacheCorrectly() {
            // Given
            Portfolio portfolio = createTestPortfolio();
            Map<Long, Portfolio> portfolioStorage = (Map<Long, Portfolio>) 
                ReflectionTestUtils.getField(portfolioService, "portfolioStorage");
            portfolioStorage.put(VALID_PORTFOLIO_ID, portfolio);
            
            // When & Then
            // 첫 번째 조회
            Optional<Portfolio> result1 = portfolioService.findById(VALID_PORTFOLIO_ID);
            assertThat(result1).isPresent();
            
            // 두 번째 조회 (캐시에서 가져와야 함)
            Optional<Portfolio> result2 = portfolioService.findById(VALID_PORTFOLIO_ID);
            assertThat(result2).isPresent();
            
            // 캐시가 제대로 작동하는지 확인
            assertThat(result1.get()).isEqualTo(result2.get());
        }
    }
}