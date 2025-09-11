package com.stockquest.application.common.port;

/**
 * 비밀번호 암호화 포트 (출력 포트)
 */
public interface PasswordEncoder {
    
    /**
     * 비밀번호 암호화
     */
    String encode(String rawPassword);
    
    /**
     * 비밀번호 일치 여부 확인
     */
    boolean matches(String rawPassword, String encodedPassword);
}