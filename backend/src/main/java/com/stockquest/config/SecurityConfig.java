package com.stockquest.config;

import com.stockquest.adapter.in.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security 보안 설정
 * CORS, CSRF, 인증/인가, Rate Limiting 등 전반적인 보안 정책 구성
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 인해)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 관리 - Stateless (JWT 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 보안 헤더 설정 (강화)
            .headers(headers -> headers
                .frameOptions(frameOptionsConfig -> frameOptionsConfig.deny())
                .contentTypeOptions(contentTypeOptionsConfig -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1년
                    .includeSubDomains(true)
                    .preload(true)
                )
                .referrerPolicy(referrerPolicyConfig -> 
                    referrerPolicyConfig.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // 추가 보안 헤더들
                .addHeaderWriter((request, response) -> {
                    // Content Security Policy
                    response.setHeader("Content-Security-Policy", 
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self'; " +
                        "font-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");
                    
                    // Permission Policy (Feature Policy 후속)
                    response.setHeader("Permissions-Policy", 
                        "camera=(), microphone=(), geolocation=(), payment=()");
                    
                    // X-XSS-Protection (추가 XSS 보호)
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    
                    // X-Permitted-Cross-Domain-Policies
                    response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
                    
                    // Cache-Control for sensitive pages
                    if (request.getRequestURI().contains("/api/auth/") ||
                        request.getRequestURI().contains("/api/admin/")) {
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                        response.setHeader("Pragma", "no-cache");
                        response.setHeader("Expires", "0");
                    }
                })
            )
            
            // 인증/인가 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 엔드포인트
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/openapi/**"
                ).permitAll()
                
                // 관리자 전용 엔드포인트
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Actuator 엔드포인트 (관리자만)
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // 읽기 전용 작업
                .requestMatchers(HttpMethod.GET, "/api/challenges/**").permitAll()
                
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 예외 처리
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "인증이 필요합니다.");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(403, "접근 권한이 없습니다.");
                })
            );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용된 Origin 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        
        // 허용된 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // 허용된 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers", "X-CSRF-Token"
        ));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials"
        ));
        
        // Pre-flight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(Duration.ofHours(1).getSeconds());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 강력한 해시 강도
    }
}