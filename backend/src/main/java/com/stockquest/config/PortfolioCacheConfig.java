package com.stockquest.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Portfolio-specific caching configuration for performance optimization
 * Implements smart caching strategies for trading operations
 */
@Configuration
@EnableCaching
public class PortfolioCacheConfig {
    
    /**
     * Redis cache configuration with optimized TTL for different data types
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
     * Default cache configuration with reasonable defaults
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