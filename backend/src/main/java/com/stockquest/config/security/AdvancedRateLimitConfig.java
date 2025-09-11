package com.stockquest.config.security;

import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 고도화된 Rate Limiting 설정 (비활성화됨 - bucket4j 의존성 문제)
 * 엔드포인트별, 사용자별, IP별 세분화된 제한
 */
// @Configuration - Temporarily disabled due to bucket4j dependency
public class AdvancedRateLimitConfig {
    
    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Value("${app.rate-limit.default.requests:100}")
    private int defaultRequests;
    
    @Value("${app.rate-limit.default.duration:60}")
    private int defaultDurationSeconds;
    
    private final Map<String, RateLimitPolicy> endpointPolicies = new ConcurrentHashMap<>();
    
    public AdvancedRateLimitConfig() {
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
        
        // 커뮤니티 엔드포인트 - 스팸 방지
        endpointPolicies.put("/api/community/posts", 
            new RateLimitPolicy(10, Duration.ofMinutes(10), "게시글작성"));
        endpointPolicies.put("/api/community/*/comments", 
            new RateLimitPolicy(20, Duration.ofMinutes(5), "댓글작성"));
    }
    
    /**
     * 엔드포인트별 정책 조회
     */
    public RateLimitPolicy getEndpointPolicy(String endpoint) {
        return endpointPolicies.getOrDefault(endpoint, 
            new RateLimitPolicy(defaultRequests, Duration.ofSeconds(defaultDurationSeconds), "기본"));
    }
    
    /**
     * IP별 정책 조회 (사용자별 정책보다 더 엄격)
     */
    private RateLimitPolicy getIpPolicy(String endpoint) {
        RateLimitPolicy userPolicy = getEndpointPolicy(endpoint);
        // IP 기반은 사용자 기반 제한의 50% 적용
        return new RateLimitPolicy(
            Math.max(1, userPolicy.getRequests() / 2),
            userPolicy.getDuration(),
            userPolicy.getDescription() + "(IP)"
        );
    }
    
    /**
     * 글로벌 정책 조회 (전체 시스템 보호)
     */
    private RateLimitPolicy getGlobalPolicy(String endpoint) {
        RateLimitPolicy userPolicy = getEndpointPolicy(endpoint);
        // 글로벌은 사용자 기반 제한의 10배 적용
        return new RateLimitPolicy(
            userPolicy.getRequests() * 10,
            userPolicy.getDuration(),
            userPolicy.getDescription() + "(글로벌)"
        );
    }
    
    /**
     * Rate Limit 정책 정보
     */
    public static class RateLimitPolicy {
        private final int requests;
        private final Duration duration;
        private final String description;
        
        public RateLimitPolicy(int requests, Duration duration, String description) {
            this.requests = requests;
            this.duration = duration;
            this.description = description;
        }
        
        public int getRequests() {
            return requests;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public String getDescription() {
            return description;
        }
        
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
     * 정책 제거
     */
    public void removeEndpointPolicy(String endpoint) {
        endpointPolicies.remove(endpoint);
    }
    
    /**
     * 모든 정책 조회
     */
    public Map<String, RateLimitPolicy> getAllPolicies() {
        return new ConcurrentHashMap<>(endpointPolicies);
    }
}