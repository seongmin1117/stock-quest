package com.stockquest.adapter.out.auth;

import com.stockquest.application.common.port.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security 기반 비밀번호 암호화 어댑터
 */
@Component
public class SpringPasswordEncoder implements PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder;
    
    public SpringPasswordEncoder() {
        this.encoder = new BCryptPasswordEncoder();
    }
    
    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}