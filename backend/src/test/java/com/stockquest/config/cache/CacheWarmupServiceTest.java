package com.stockquest.config.cache;

import com.stockquest.application.challenge.GetChallengesService;
import com.stockquest.application.leaderboard.GetLeaderboardService;
import com.stockquest.application.marketdata.GetMarketDataService;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.leaderboard.LeaderboardEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheWarmupService 테스트")
class CacheWarmupServiceTest {

    @Mock
    private GetChallengesService challengesService;

    @Mock
    private GetMarketDataService marketDataService;

    @Mock
    private GetLeaderboardService leaderboardService;

    @InjectMocks
    private CacheWarmupService cacheWarmupService;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        // 필요한 설정이 있다면 여기에 추가
    }

    @Test
    @DisplayName("챌린지 데이터 워밍업이 정상적으로 동작해야 한다")
    void shouldWarmupChallengeData() throws Exception {
        // given
        List<Challenge> activeChallenges = List.of(mock(Challenge.class));
        List<Long> popularIds = List.of(1L, 2L, 3L);

        when(challengesService.getActiveChallenges()).thenReturn(activeChallenges);
        when(challengesService.getPopularChallengeIds(10)).thenReturn(popularIds);
        when(challengesService.getChallengeDetail(anyLong())).thenReturn(mock(Challenge.class));

        // when
        CompletableFuture<Void> result = cacheWarmupService.warmupChallengeData();
        result.join(); // 완료까지 대기

        // then
        verify(challengesService).getActiveChallenges();
        verify(challengesService).getPopularChallengeIds(10);
        verify(challengesService, times(3)).getChallengeDetail(anyLong());
    }

    @Test
    @DisplayName("마켓 데이터 워밍업이 정상적으로 동작해야 한다")
    void shouldWarmupMarketData() throws Exception {
        // given
        when(marketDataService.getLatestPrice(anyString())).thenReturn(BigDecimal.valueOf(100.0));
        when(marketDataService.getDailyCandles(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // when
        CompletableFuture<Void> result = cacheWarmupService.warmupMarketData();
        result.join(); // 완료까지 대기

        // then
        verify(marketDataService, times(10)).getLatestPrice(anyString()); // 10개 주요 종목
        verify(marketDataService, times(10)).getDailyCandles(anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("리더보드 데이터 워밍업이 정상적으로 동작해야 한다")
    void shouldWarmupLeaderboardData() throws Exception {
        // given
        List<Long> activeChallengeIds = List.of(1L, 2L, 3L);
        when(challengesService.getActiveChallengeIds()).thenReturn(activeChallengeIds);
        when(leaderboardService.getLeaderboard(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(mock(LeaderboardEntry.class)));

        // when
        CompletableFuture<Void> result = cacheWarmupService.warmupLeaderboardData();
        result.join(); // 완료까지 대기

        // then
        verify(challengesService).getActiveChallengeIds();
        verify(leaderboardService, times(3)).getLeaderboard(anyLong(), eq(0), eq(50));
    }

    @Test
    @DisplayName("핵심 데이터 워밍업이 정상적으로 동작해야 한다")
    void shouldWarmupCriticalData() throws Exception {
        // given
        when(challengesService.getActiveChallengeCount()).thenReturn(5L);
        when(marketDataService.getTopVolumeStocks(24)).thenReturn(List.of("AAPL", "GOOGL"));

        // when
        CompletableFuture<Void> result = cacheWarmupService.warmupCriticalData();
        result.join(); // 완료까지 대기

        // then
        verify(challengesService).getActiveChallengeCount();
        verify(marketDataService).getTopVolumeStocks(24);
    }

    @Test
    @DisplayName("서비스가 null인 경우 예외가 발생하지 않아야 한다")
    void shouldHandleNullServicesGracefully() throws Exception {
        // given - 모든 서비스를 null로 설정
        CacheWarmupService serviceWithNullDependencies = new CacheWarmupService();

        // when & then - 예외가 발생하지 않아야 함
        CompletableFuture<Void> challengeResult = serviceWithNullDependencies.warmupChallengeData();
        CompletableFuture<Void> marketResult = serviceWithNullDependencies.warmupMarketData();
        CompletableFuture<Void> leaderboardResult = serviceWithNullDependencies.warmupLeaderboardData();
        CompletableFuture<Void> criticalResult = serviceWithNullDependencies.warmupCriticalData();

        challengeResult.join();
        marketResult.join();
        leaderboardResult.join();
        criticalResult.join();
    }

    @Test
    @DisplayName("캐시 무효화 및 재워밍업이 정상적으로 동작해야 한다")
    void shouldInvalidateAndWarmupSpecificCache() {
        // given
        when(challengesService.getActiveChallenges()).thenReturn(List.of());

        // when
        cacheWarmupService.invalidateAndWarmup("challenge");

        // then
        verify(challengesService).getActiveChallenges();
    }
}