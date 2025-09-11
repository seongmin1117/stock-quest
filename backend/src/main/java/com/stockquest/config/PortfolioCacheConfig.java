package com.stockquest.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import org.springframework.data.redis.RedisConnectionFailureException;

/**
 * Portfolio-specific caching configuration for performance optimization in trading operations.
 * 
 * <p>Implements intelligent Redis caching strategies with optimized TTL values
 * based on data volatility and access patterns in the StockQuest trading platform.
 * 
 * <p>Cache performance targets:
 * <ul>
 *   <li>Portfolio calculations: 90%+ cache hit rate with 1-minute TTL</li>
 *   <li>Leaderboard queries: 95%+ cache hit rate with 5-minute TTL</li>
 *   <li>Market data: 80%+ cache hit rate with 30-second TTL for real-time updates</li>
 * </ul>
 * 
 * <p>Memory optimization:
 * <ul>
 *   <li>JSON serialization for cross-language compatibility</li>
 *   <li>Null value caching disabled to prevent memory bloat</li>
 *   <li>TTL-based automatic eviction for memory efficiency</li>
 * </ul>
 * 
 * @author StockQuest Performance Team
 * @since 1.0
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.data.redis.cache.RedisCacheManager
 */
@Configuration
@EnableCaching
public class PortfolioCacheConfig {
    
    /**
     * Redis cache configuration with data-specific TTL optimization for trading operations.
     * 
     * <p>Configures multiple cache regions with TTL values optimized for different
     * data types based on their update frequency and business criticality:
     * 
     * <ul>
     *   <li><strong>portfolio-values</strong>: 1-minute TTL for frequent portfolio updates</li>
     *   <li><strong>leaderboard</strong>: 5-minute TTL for computationally expensive rankings</li>
     *   <li><strong>market-data</strong>: 30-second TTL for real-time market information</li>
     *   <li><strong>portfolio-summary</strong>: 2-minute TTL for user dashboard data</li>
     *   <li><strong>challenge-stats</strong>: 10-minute TTL for stable challenge metrics</li>
     *   <li><strong>heavy-calculations</strong>: 15-minute TTL for complex analytics</li>
     * </ul>
     * 
     * @return Customizer for RedisCacheManager with optimized cache configurations
     * @throws RedisConnectionException if Redis server is unreachable
     * @see RedisCacheManagerBuilderCustomizer
     * @see RedisCacheConfiguration
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
            // Portfolio calculations - 1분 cache (빈번한 업데이트)
            .withCacheConfiguration("portfolio-values",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(1))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues())
            
            // Leaderboard calculations - 5분 cache (계산 비용이 높음)
            .withCacheConfiguration("leaderboard",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues())
            
            // Market data - 30초 cache (실시간성 중요)
            .withCacheConfiguration("market-data",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(30))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues())
            
            // User portfolios summary - 2분 cache
            .withCacheConfiguration("portfolio-summary",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(2))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues())
            
            // Challenge statistics - 10분 cache (상대적으로 stable)
            .withCacheConfiguration("challenge-stats",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues())
            
            // Heavy calculations - 15분 cache
            .withCacheConfiguration("heavy-calculations",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(15))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues());
    }
    
    /**
     * Default cache configuration with balanced performance and memory usage.
     * 
     * <p>Provides fallback caching behavior for any cache regions not explicitly
     * configured with custom TTL values. Uses moderate 5-minute TTL as a balance
     * between data freshness and cache effectiveness.
     * 
     * <p>Default configuration features:
     * <ul>
     *   <li>5-minute TTL for general-purpose caching</li>
     *   <li>JSON serialization for structured data storage</li>
     *   <li>Null value caching disabled to prevent cache pollution</li>
     *   <li>Compatible with all Spring Cache annotations</li>
     * </ul>
     * 
     * @return Default RedisCacheConfiguration for unspecified cache regions
     * @see RedisCacheConfiguration#defaultCacheConfig()
     * @see GenericJackson2JsonRedisSerializer
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Default 5분 TTL
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
    }
}