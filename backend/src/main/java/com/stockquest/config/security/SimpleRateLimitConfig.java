package com.stockquest.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 간단한 Rate Limiting 설정
 * Redis 기반으로 분산 환경에서 동작
 */
@Configuration
public class SimpleRateLimitConfig {
    
    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Value("${app.rate-limit.default.requests:100}")
    private int defaultRequests;
    
    @Value("${app.rate-limit.default.duration:60}")
    private int defaultDurationSeconds;
    
    private final Map<String, RateLimitPolicy> endpointPolicies = new ConcurrentHashMap<>();
    
    public SimpleRateLimitConfig() {
        initializeEndpointPolicies();
    }
    
    /**
     * 엔드포인트별 Rate Limit 정책 초기화
     */
    private void initializeEndpointPolicies() {
        // 인증 관련 엔드포인트 - 더 엄격한 제한
        endpointPolicies.put("/api/auth/login", 
            new RateLimitPolicy(5, Duration.ofMinutes(15), "로그인"));
        endpointPolicies.put("/api/auth/signup", 
            new RateLimitPolicy(3, Duration.ofHours(1), "회원가입"));
        endpointPolicies.put("/api/auth/refresh", 
            new RateLimitPolicy(10, Duration.ofMinutes(5), "토큰갱신"));
        
        // 거래 관련 엔드포인트 - 중간 제한
        endpointPolicies.put("/api/sessions/*/orders", 
            new RateLimitPolicy(50, Duration.ofMinutes(1), "주문실행"));
        endpointPolicies.put("/api/sessions/*/close", 
            new RateLimitPolicy(10, Duration.ofMinutes(10), "세션종료"));
        
        // 조회 엔드포인트 - 관대한 제한
        endpointPolicies.put("/api/challenges", 
            new RateLimitPolicy(200, Duration.ofMinutes(1), "챌린지조회"));
        endpointPolicies.put("/api/sessions/*", 
            new RateLimitPolicy(100, Duration.ofMinutes(1), "세션조회"));
        endpointPolicies.put("/api/leaderboard/*", 
            new RateLimitPolicy(50, Duration.ofMinutes(1), "리더보드"));
    }
    
    /**
     * Rate Limit 검증 (Redis 기반)
     */
    public boolean isAllowed(RedisTemplate<String, String> redisTemplate, String key, String endpoint) {
        if (!rateLimitEnabled) {
            return true;
        }
        
        RateLimitPolicy policy = getEndpointPolicy(endpoint);
        String redisKey = "rate_limit:" + key + ":" + endpoint;
        
        try {
            // 현재 요청 수 조회
            String currentCountStr = redisTemplate.opsForValue().get(redisKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            if (currentCount >= policy.requests()) {
                return false; // 제한 초과
            }
            
            // 요청 수 증가
            if (currentCount == 0) {
                // 첫 번째 요청 시 TTL 설정
                redisTemplate.opsForValue().set(redisKey, "1", 
                    policy.duration().getSeconds(), TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().increment(redisKey);
            }
            
            return true;
            
        } catch (Exception e) {
            // Redis 오류 시 허용 (서비스 가용성 우선)
            return true;
        }
    }
    
    /**
     * 남은 허용 요청 수 조회
     */
    public int getRemainingRequests(RedisTemplate<String, String> redisTemplate, String key, String endpoint) {
        if (!rateLimitEnabled) {
            return Integer.MAX_VALUE;
        }
        
        RateLimitPolicy policy = getEndpointPolicy(endpoint);
        String redisKey = "rate_limit:" + key + ":" + endpoint;
        
        try {
            String currentCountStr = redisTemplate.opsForValue().get(redisKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            return Math.max(0, policy.requests() - currentCount);
        } catch (Exception e) {
            return policy.requests();
        }
    }
    
    /**
     * Rate Limit 리셋 (관리자 기능)
     */
    public void resetRateLimit(RedisTemplate<String, String> redisTemplate, String key, String endpoint) {
        String redisKey = "rate_limit:" + key + ":" + endpoint;
        redisTemplate.delete(redisKey);
    }
    
    /**
     * 엔드포인트별 정책 조회
     */
    public RateLimitPolicy getEndpointPolicy(String endpoint) {
        return endpointPolicies.getOrDefault(endpoint, 
            new RateLimitPolicy(defaultRequests, Duration.ofSeconds(defaultDurationSeconds), "기본"));
    }
    
    /**
     * Rate Limit 정책 정보
     */
    public record RateLimitPolicy(
        int requests,
        Duration duration,
        String description
    ) {
        public long getDurationSeconds() {
            return duration.getSeconds();
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d requests per %d seconds", 
                description, requests, duration.getSeconds());
        }
    }
    
    /**
     * Rate Limit 설정 활성화 여부
     */
    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }
    
    /**
     * 동적 정책 업데이트 (운영 중 조정)
     */
    public void updateEndpointPolicy(String endpoint, int requests, Duration duration, String description) {
        endpointPolicies.put(endpoint, new RateLimitPolicy(requests, duration, description));
    }
    
    /**
     * 모든 정책 조회
     */
    public Map<String, RateLimitPolicy> getAllPolicies() {
        return new ConcurrentHashMap<>(endpointPolicies);
    }
}