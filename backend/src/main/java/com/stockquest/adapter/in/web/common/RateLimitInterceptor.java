package com.stockquest.adapter.in.web.common;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.function.Supplier;

/**
 * Rate Limiting 인터셉터
 * 요청 경로별로 다른 Rate Limiting 정책 적용
 */
// @Component - Temporarily disabled due to bucket4j dependency
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> apiRateLimitConfig;
    private final Supplier<BucketConfiguration> loginRateLimitConfig;
    private final Supplier<BucketConfiguration> signupRateLimitConfig;
    private final Supplier<BucketConfiguration> orderRateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // 경로별 Rate Limiting 정책 결정
        Supplier<BucketConfiguration> configSupplier = determineRateLimitConfig(requestPath, method);
        if (configSupplier == null) {
            return true; // Rate Limiting 미적용
        }
        
        // 버킷 키 생성 (IP + 경로 기반)
        String bucketKey = createBucketKey(clientIp, requestPath, method);
        
        // 버킷 생성 및 토큰 소비 시도
        Bucket bucket = proxyManager.builder()
                .build(bucketKey, configSupplier);
                
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // 요청 허용 - 남은 토큰 수를 헤더에 추가
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate Limit 초과
            long waitTime = probe.getNanosToWaitForRefill() / 1_000_000_000; // 초 단위로 변환
            
            response.setStatus(429); // Too Many Requests
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("Retry-After", String.valueOf(waitTime));
            response.setContentType("application/json");
            
            String errorJson = String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"요청이 너무 많습니다. %d초 후 다시 시도해주세요.\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now(),
                waitTime,
                requestPath
            );
            
            response.getWriter().write(errorJson);
            
            log.warn("Rate limit exceeded for IP: {}, Path: {}, Wait time: {}s", clientIp, requestPath, waitTime);
            
            return false;
        }
    }

    /**
     * 경로와 메서드에 따른 Rate Limiting 설정 결정
     */
    private Supplier<BucketConfiguration> determineRateLimitConfig(String path, String method) {
        // 로그인 엔드포인트
        if (path.equals("/api/auth/login") && "POST".equals(method)) {
            return loginRateLimitConfig;
        }
        
        // 회원가입 엔드포인트
        if (path.equals("/api/auth/signup") && "POST".equals(method)) {
            return signupRateLimitConfig;
        }
        
        // 주문 관련 엔드포인트
        if (path.startsWith("/api/sessions/") && path.endsWith("/orders") && "POST".equals(method)) {
            return orderRateLimitConfig;
        }
        
        // 일반 API 엔드포인트
        if (path.startsWith("/api/")) {
            return apiRateLimitConfig;
        }
        
        // Rate Limiting 미적용
        return null;
    }

    /**
     * 버킷 키 생성
     */
    private String createBucketKey(String clientIp, String path, String method) {
        // 로그인/회원가입의 경우 IP별로 제한
        if (path.equals("/api/auth/login") || path.equals("/api/auth/signup")) {
            return String.format("rate_limit:auth:%s", clientIp);
        }
        
        // 주문의 경우 IP별로 제한
        if (path.startsWith("/api/sessions/") && path.endsWith("/orders")) {
            return String.format("rate_limit:order:%s", clientIp);
        }
        
        // 일반 API의 경우 IP별로 제한
        return String.format("rate_limit:api:%s", clientIp);
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}