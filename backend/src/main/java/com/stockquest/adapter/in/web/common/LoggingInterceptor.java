package com.stockquest.adapter.in.web.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * MDC(Mapped Diagnostic Context) 로깅 인터셉터
 * 요청마다 고유한 추적 ID와 사용자 정보를 MDC에 설정
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String CLIENT_IP = "clientIp";
    private static final String REQUEST_URI = "requestUri";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String USER_AGENT = "userAgent";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 추적 ID 생성 (UUID)
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);
        
        // 사용자 정보 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            MDC.put(USER_ID, authentication.getName());
        } else {
            MDC.put(USER_ID, "anonymous");
        }
        
        // 요청 정보 설정
        MDC.put(CLIENT_IP, getClientIpAddress(request));
        MDC.put(REQUEST_URI, request.getRequestURI());
        MDC.put(HTTP_METHOD, request.getMethod());
        
        // User-Agent (모바일/데스크톱 구분 등에 활용)
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 100) {
            userAgent = userAgent.substring(0, 100) + "...";
        }
        MDC.put(USER_AGENT, userAgent != null ? userAgent : "unknown");
        
        // 요청 시작 시간 기록
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 응답 헤더에 추적 ID 추가
        response.setHeader("X-Trace-Id", traceId);
        
        log.info("Request started - {} {} from {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            getClientIpAddress(request)
        );
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {
        
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
        
        if (ex != null) {
            log.error("Request completed with error - {} {} - Status: {} - Duration: {}ms - Error: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                response.getStatus(),
                duration,
                ex.getMessage()
            );
        } else {
            log.info("Request completed - {} {} - Status: {} - Duration: {}ms", 
                request.getMethod(), 
                request.getRequestURI(),
                response.getStatus(),
                duration
            );
        }
        
        // MDC 클리어 (메모리 리크 방지)
        MDC.clear();
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시 서버를 거치는 경우 실제 클라이언트 IP 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 첫 번째 IP가 실제 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}