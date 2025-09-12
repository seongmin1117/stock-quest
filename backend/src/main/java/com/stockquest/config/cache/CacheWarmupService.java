package com.stockquest.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.stockquest.application.challenge.GetChallengesService;
import com.stockquest.application.leaderboard.GetLeaderboardService;
import com.stockquest.application.marketdata.GetMarketDataService;
import com.stockquest.domain.challenge.ChallengeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 캐시 워밍업 서비스
 * 애플리케이션 시작 시 및 주기적으로 중요한 데이터를 미리 캐싱
 */
@Service
public class CacheWarmupService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);
    
    @Autowired(required = false)
    private GetChallengesService challengesService;
    
    @Autowired(required = false)
    private GetMarketDataService marketDataService;
    
    @Autowired(required = false)
    private GetLeaderboardService leaderboardService;
    
    /**
     * 애플리케이션 시작 시 캐시 워밍업
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmupCacheOnStartup() {
        logger.info("Starting cache warmup on application startup...");
        
        try {
            CompletableFuture<Void> challengeWarmup = warmupChallengeData();
            CompletableFuture<Void> marketWarmup = warmupMarketData();
            CompletableFuture<Void> leaderboardWarmup = warmupLeaderboardData();
            
            // 모든 워밍업 작업이 완료될 때까지 대기
            CompletableFuture.allOf(challengeWarmup, marketWarmup, leaderboardWarmup)
                    .thenRun(() -> logger.info("Cache warmup completed successfully"))
                    .exceptionally(throwable -> {
                        logger.error("Cache warmup failed", throwable);
                        return null;
                    });
                    
        } catch (Exception e) {
            logger.error("Failed to start cache warmup", e);
        }
    }
    
    /**
     * 주기적 캐시 워밍업 (매 시간)
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    @Async
    public void scheduledCacheWarmup() {
        logger.debug("Starting scheduled cache warmup...");
        
        try {
            // 핵심 데이터만 주기적으로 갱신
            warmupCriticalData().join();
            logger.debug("Scheduled cache warmup completed");
        } catch (Exception e) {
            logger.warn("Scheduled cache warmup failed", e);
        }
    }
    
    /**
     * 챌린지 데이터 워밍업
     */
    @Async
    public CompletableFuture<Void> warmupChallengeData() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Warming up challenge data...");
                
                // 활성 챌린지 목록 캐싱
                if (challengesService != null) {
                    challengesService.getActiveChallenges();
                    
                    // 인기 챌린지 상세 정보 캐싱 (상위 10개)
                    List<Long> popularChallengeIds = challengesService.getPopularChallengeIds(10);
                    for (Long challengeId : popularChallengeIds) {
                        challengesService.getChallengeDetail(challengeId);
                    }
                } else {
                    logger.debug("ChallengesService not available, skipping challenge warmup");
                }
                
                logger.debug("Challenge data warmup completed");
            } catch (Exception e) {
                logger.warn("Failed to warmup challenge data", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 마켓 데이터 워밍업
     */
    @Async
    public CompletableFuture<Void> warmupMarketData() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Warming up market data...");
                
                // 주요 종목 최신 가격 캐싱
                if (marketDataService != null) {
                    String[] majorTickers = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "META", "NVDA", "JPM", "V", "JNJ"};
                    LocalDate endDate = LocalDate.now();
                    LocalDate startDate = endDate.minusDays(30);
                    
                    for (String ticker : majorTickers) {
                        marketDataService.getLatestPrice(ticker);
                        marketDataService.getDailyCandles(ticker, startDate, endDate);
                    }
                } else {
                    logger.debug("MarketDataService not available, skipping market data warmup");
                }
                
                logger.debug("Market data warmup completed");
            } catch (Exception e) {
                logger.warn("Failed to warmup market data", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 리더보드 데이터 워밍업
     */
    @Async
    public CompletableFuture<Void> warmupLeaderboardData() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Warming up leaderboard data...");
                
                // 활성 챌린지들의 리더보드 캐싱
                if (leaderboardService != null && challengesService != null) {
                    List<Long> activeChallengeIds = challengesService.getActiveChallengeIds();
                    for (Long challengeId : activeChallengeIds) {
                        leaderboardService.getLeaderboard(challengeId, 0, 50); // 상위 50명
                    }
                } else {
                    logger.debug("LeaderboardService or ChallengesService not available, skipping leaderboard warmup");
                }
                
                logger.debug("Leaderboard data warmup completed");
            } catch (Exception e) {
                logger.warn("Failed to warmup leaderboard data", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 핵심 데이터 워밍업 (자주 접근되는 데이터)
     */
    @Async
    public CompletableFuture<Void> warmupCriticalData() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Warming up critical data...");
                
                // 현재 활성 챌린지 수 캐싱
                if (challengesService != null) {
                    challengesService.getActiveChallengeCount();
                }
                
                // 최근 24시간 거래량 Top 종목
                if (marketDataService != null) {
                    marketDataService.getTopVolumeStocks(24);
                }
                
                logger.debug("Critical data warmup completed");
            } catch (Exception e) {
                logger.warn("Failed to warmup critical data", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 캐시 무효화 및 재워밍업 (데이터 변경 시 호출)
     */
    @Async
    public void invalidateAndWarmup(String cacheType, Object... params) {
        logger.info("Invalidating and rewarming cache type: {}", cacheType);
        
        try {
            switch (cacheType.toLowerCase()) {
                case "challenge":
                    // 챌린지 캐시 무효화 및 재워밍업
                    warmupChallengeData().join();
                    break;
                case "market":
                    // 마켓 데이터 캐시 무효화 및 재워밍업
                    warmupMarketData().join();
                    break;
                case "leaderboard":
                    // 리더보드 캐시 무효화 및 재워밍업
                    warmupLeaderboardData().join();
                    break;
                default:
                    logger.warn("Unknown cache type for invalidation: {}", cacheType);
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate and warmup cache type: {}", cacheType, e);
        }
    }
    
    /**
     * 캐시 상태 모니터링
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    public void monitorCacheHealth() {
        try {
            logger.debug("Monitoring cache health at {}", LocalDateTime.now());
            
            // TODO: 캐시 히트/미스 비율 체크
            // TODO: 메모리 사용량 모니터링
            // TODO: 캐시 크기 모니터링
            
        } catch (Exception e) {
            logger.warn("Cache health monitoring failed", e);
        }
    }
}