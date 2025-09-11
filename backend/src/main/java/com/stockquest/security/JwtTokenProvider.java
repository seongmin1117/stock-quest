package com.stockquest.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider - Stub Implementation
 * TODO: Implement proper JWT token validation
 */
@Component
public class JwtTokenProvider {

    public boolean validateToken(String token) {
        // TODO: Implement proper JWT token validation
        return token != null && !token.isEmpty();
    }

    public String getUsername(String token) {
        // TODO: Extract username from JWT token
        return "user";
    }

    public Long getUserId(String token) {
        // TODO: Extract user ID from JWT token
        return 1L;
    }

    public Authentication getAuthentication(String token) {
        // TODO: Create proper Authentication object from JWT token
        return null;
    }
}