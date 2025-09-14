package com.stockquest.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 고도화된 Redis 캐싱 전략
 * 데이터 특성별 TTL 및 캐시 정책 최적화
 */
@Configuration
@EnableCaching
public class AdvancedCacheConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Duration defaultTtl;

    @Value("${spring.cache.redis.cache-null-values:false}")
    private boolean cacheNullValues;

    @Value("${spring.cache.redis.key-prefix:stockquest:}")
    private String keyPrefix;
    
    /**
     * Redis Template 설정 (성능 최적화)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String 기반 Key Serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // JSON 기반 Value Serializer (JavaTimeModule 포함된 ObjectMapper 사용)
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 캐시별 세분화된 TTL 설정
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> {
            // 데이터 특성별 캐시 설정
            Map<String, RedisCacheConfiguration> configMap = createCacheConfigurations();
            builder.withInitialCacheConfigurations(configMap);
        };
    }
    
    /**
     * 캐시 매니저 설정
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .prefixCacheNameWith(keyPrefix);

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(createCacheConfigurations())
                .transactionAware() // 트랜잭션 인식
                .build();
    }
    
    /**
     * 데이터 특성별 캐시 설정 생성
     */
    private Map<String, RedisCacheConfiguration> createCacheConfigurations() {
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        
        // 실시간 데이터 - 짧은 TTL
        configMap.put("latestPrice", createCacheConfig(Duration.ofSeconds(30), "실시간 주가"));
        configMap.put("marketStatus", createCacheConfig(Duration.ofMinutes(1), "장 상태"));
        configMap.put("currentSessions", createCacheConfig(Duration.ofMinutes(1), "현재 세션"));
        
        // 시계열 데이터 - 중간 TTL
        configMap.put("dailyCandles", createCacheConfig(Duration.ofMinutes(5), "일봉 데이터"));
        configMap.put("hourlyCandles", createCacheConfig(Duration.ofMinutes(10), "시간봉 데이터"));
        configMap.put("minuteCandles", createCacheConfig(Duration.ofMinutes(1), "분봉 데이터"));
        
        // 리더보드 데이터 - 중간 TTL
        configMap.put("leaderboard", createCacheConfig(Duration.ofMinutes(5), "리더보드"));
        configMap.put("userRanking", createCacheConfig(Duration.ofMinutes(5), "사용자 순위"));
        configMap.put("challengeStats", createCacheConfig(Duration.ofMinutes(10), "챌린지 통계"));
        
        // 사용자 데이터 - 긴 TTL
        configMap.put("userProfile", createCacheConfig(Duration.ofMinutes(30), "사용자 프로필"));
        configMap.put("userSessions", createCacheConfig(Duration.ofMinutes(15), "사용자 세션 목록"));
        configMap.put("userPortfolio", createCacheConfig(Duration.ofMinutes(5), "사용자 포트폴리오"));
        
        // 정적 데이터 - 매우 긴 TTL
        configMap.put("challengeList", createCacheConfig(Duration.ofHours(1), "챌린지 목록"));
        configMap.put("challengeDetail", createCacheConfig(Duration.ofMinutes(30), "챌린지 상세"));
        configMap.put("systemConfig", createCacheConfig(Duration.ofHours(6), "시스템 설정"));
        
        // 커뮤니티 데이터 - 중간 TTL
        configMap.put("communityPosts", createCacheConfig(Duration.ofMinutes(10), "커뮤니티 글"));
        configMap.put("postComments", createCacheConfig(Duration.ofMinutes(5), "댓글 목록"));
        
        // 분석 데이터 - 긴 TTL
        configMap.put("performanceAnalysis", createCacheConfig(Duration.ofMinutes(30), "성과 분석"));
        configMap.put("riskMetrics", createCacheConfig(Duration.ofMinutes(15), "위험 지표"));
        
        return configMap;
    }
    
    /**
     * 캐시 설정 생성 헬퍼 메소드
     */
    private RedisCacheConfiguration createCacheConfig(Duration ttl, String description) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .prefixCacheNameWith(keyPrefix + description + ":");
    }
    
    /**
     * 캐시 워밍업을 위한 스케줄러 설정
     */
    @Bean
    public CacheWarmupService cacheWarmupService() {
        return new CacheWarmupService();
    }
}