package com.stockquest.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator 엔드포인트 보안 설정
 * Health Check는 공개, 나머지는 관리자만 접근 가능
 */
@Configuration
public class ActuatorSecurityConfig {

    /**
     * Actuator 엔드포인트용 보안 필터 체인
     * 기본 보안 설정보다 우선 적용
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authz -> authz
                // Health와 Info는 공개
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                // 나머지는 ADMIN 권한 필요
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
            );
        
        return http.build();
    }
}