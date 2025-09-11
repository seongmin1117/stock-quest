package com.stockquest.domain.auth;

import java.time.LocalDateTime;

/**
 * 로그인 시도 기록
 * 계정 보안을 위한 로그인 실패 추적
 */
public class LoginAttempt {
    
    private Long id;
    private String identifier; // 이메일 또는 IP 주소
    private LoginAttemptType type;
    private boolean successful;
    private LocalDateTime attemptedAt;
    private String userAgent;
    private String ipAddress;
    private String failureReason;
    
    // 생성자
    private LoginAttempt() {}
    
    public LoginAttempt(String identifier, LoginAttemptType type, boolean successful, 
                       String userAgent, String ipAddress) {
        this.identifier = identifier;
        this.type = type;
        this.successful = successful;
        this.attemptedAt = LocalDateTime.now();
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }
    
    /**
     * 실패한 로그인 시도 생성
     */
    public static LoginAttempt failed(String identifier, LoginAttemptType type, 
                                    String failureReason, String userAgent, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(identifier, type, false, userAgent, ipAddress);
        attempt.failureReason = failureReason;
        return attempt;
    }
    
    /**
     * 성공한 로그인 시도 생성
     */
    public static LoginAttempt successful(String identifier, LoginAttemptType type, 
                                        String userAgent, String ipAddress) {
        return new LoginAttempt(identifier, type, true, userAgent, ipAddress);
    }
    
    /**
     * 시도가 특정 시간 이후인지 확인
     */
    public boolean isAfter(LocalDateTime dateTime) {
        return attemptedAt.isAfter(dateTime);
    }
    
    /**
     * 시도가 특정 시간 이전인지 확인
     */
    public boolean isBefore(LocalDateTime dateTime) {
        return attemptedAt.isBefore(dateTime);
    }
    
    /**
     * 시도 시간으로부터 경과된 분
     */
    public long getMinutesElapsed() {
        return java.time.Duration.between(attemptedAt, LocalDateTime.now()).toMinutes();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public LoginAttemptType getType() {
        return type;
    }
    
    public void setType(LoginAttemptType type) {
        this.type = type;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
    
    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    /**
     * 로그인 시도 유형
     */
    public enum LoginAttemptType {
        EMAIL("이메일"),
        IP_ADDRESS("IP 주소");
        
        private final String description;
        
        LoginAttemptType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return "LoginAttempt{" +
                "identifier='" + identifier + '\'' +
                ", type=" + type +
                ", successful=" + successful +
                ", attemptedAt=" + attemptedAt +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}