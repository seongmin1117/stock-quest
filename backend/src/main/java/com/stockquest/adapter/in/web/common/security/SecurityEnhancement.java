package com.stockquest.adapter.in.web.common.security;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 보안 강화 유틸리티 클래스
 * Phase 3.1: 입력 검증, XSS 방지, SQL 인젝션 방지 등 보안 기능 제공
 */
@Slf4j
@UtilityClass
public class SecurityEnhancement {
    
    // XSS 방지를 위한 위험한 패턴들
    private static final Pattern XSS_SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern XSS_HTML_PATTERN = Pattern.compile(
        "<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_JAVASCRIPT_PATTERN = Pattern.compile(
        "javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_VBSCRIPT_PATTERN = Pattern.compile(
        "vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_EVENT_PATTERN = Pattern.compile(
        "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    
    // SQL 인젝션 방지를 위한 위험한 패턴들
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(union|select|insert|update|delete|drop|create|alter|exec|execute)\\s", 
        Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile(
        "(--|#|/\\*.*?\\*/)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // 파일 경로 순회 공격 방지
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "\\.\\./|\\.\\.\\\\" );
    
    // 심볼 검증 (주식 심볼)
    private static final Pattern VALID_SYMBOL_PATTERN = Pattern.compile(
        "^[A-Z]{1,10}$");
    
    // Rate limiting을 위한 간단한 메모리 기반 카운터
    private static final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    /**
     * XSS 공격 패턴 검사 및 정화
     */
    public static String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        log.debug("입력 검증 시작: length={}", input.length());
        
        String sanitized = input;
        
        // XSS 패턴 제거
        sanitized = XSS_SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = XSS_HTML_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = XSS_JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = XSS_VBSCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = XSS_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // HTML 엔티티 인코딩
        sanitized = htmlEscape(sanitized);
        
        // 길이 제한 적용
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
            log.warn("입력 길이 제한 적용: 원본={}, 제한={}", input.length(), 1000);
        }
        
        log.debug("입력 검증 완료: 원본={}, 정화={}", input.length(), sanitized.length());
        return sanitized;
    }
    
    /**
     * SQL 인젝션 패턴 검사
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        boolean hasSqlPattern = SQL_INJECTION_PATTERN.matcher(input).find() ||
                               SQL_COMMENT_PATTERN.matcher(input).find();
        
        if (hasSqlPattern) {
            log.warn("SQL 인젝션 시도 탐지: {}", input.substring(0, Math.min(50, input.length())));
        }
        
        return hasSqlPattern;
    }
    
    /**
     * 경로 순회 공격 패턴 검사
     */
    public static boolean containsPathTraversal(String input) {
        if (input == null) {
            return false;
        }
        
        boolean hasPathTraversal = PATH_TRAVERSAL_PATTERN.matcher(input).find();
        
        if (hasPathTraversal) {
            log.warn("경로 순회 공격 시도 탐지: {}", input);
        }
        
        return hasPathTraversal;
    }
    
    /**
     * 주식 심볼 유효성 검사
     */
    public static boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        return VALID_SYMBOL_PATTERN.matcher(symbol.trim()).matches();
    }
    
    /**
     * Rate Limiting 체크 (IP 기반 간단한 구현)
     */
    public static boolean isRateLimitExceeded(String clientId, int maxRequests, int windowMinutes) {
        if (clientId == null) {
            return false;
        }
        
        RequestCounter counter = requestCounters.computeIfAbsent(clientId, k -> new RequestCounter());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(windowMinutes);
        
        // 오래된 요청 기록 정리
        counter.requests.entrySet().removeIf(entry -> entry.getKey().isBefore(windowStart));
        
        // 현재 윈도우 내 요청 수 계산
        long requestCount = counter.requests.size();
        
        if (requestCount >= maxRequests) {
            log.warn("Rate limit 초과: clientId={}, requests={}, limit={}", 
                clientId, requestCount, maxRequests);
            return true;
        }
        
        // 현재 요청 기록
        counter.requests.put(now, true);
        
        return false;
    }
    
    /**
     * HTML 문자 이스케이프
     */
    private static String htmlEscape(String input) {
        if (input == null) {
            return null;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
    
    /**
     * 정화된 입력 검증 (모든 보안 체크 포함)
     */
    public static ValidationResult validateInput(String input, String fieldName) {
        if (input == null) {
            return ValidationResult.valid();
        }
        
        // SQL 인젝션 체크
        if (containsSqlInjection(input)) {
            return ValidationResult.invalid("SQL 인젝션 패턴 탐지됨: " + fieldName);
        }
        
        // 경로 순회 체크
        if (containsPathTraversal(input)) {
            return ValidationResult.invalid("경로 순회 공격 패턴 탐지됨: " + fieldName);
        }
        
        // XSS 패턴 체크 및 정화
        String sanitized = sanitizeInput(input);
        
        return ValidationResult.valid(sanitized);
    }
    
    /**
     * 요청 횟수 추적을 위한 내부 클래스
     */
    private static class RequestCounter {
        private final Map<LocalDateTime, Boolean> requests = new ConcurrentHashMap<>();
    }
    
    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String sanitizedValue;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String sanitizedValue, String errorMessage) {
            this.valid = valid;
            this.sanitizedValue = sanitizedValue;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null, null);
        }
        
        public static ValidationResult valid(String sanitizedValue) {
            return new ValidationResult(true, sanitizedValue, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, null, errorMessage);
        }
        
        public boolean isValid() { return valid; }
        public String getSanitizedValue() { return sanitizedValue; }
        public String getErrorMessage() { return errorMessage; }
    }
}