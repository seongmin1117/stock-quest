package com.stockquest.domain.user;

/**
 * 사용자 권한 역할
 */
public enum Role {
    USER("일반 사용자"),
    ADMIN("관리자");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 관리자 권한 확인
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * 사용자 권한 확인
     */
    public boolean isUser() {
        return this == USER;
    }
}