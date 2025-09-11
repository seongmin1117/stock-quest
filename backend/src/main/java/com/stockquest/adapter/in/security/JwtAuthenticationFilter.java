package com.stockquest.adapter.in.security;

import com.stockquest.adapter.out.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 * HTTP 요청에서 JWT 토큰을 추출하고 인증 정보를 SecurityContext에 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            log.debug("❌ 요청 [{}] 에서 Authorization 헤더 없음", requestURI);
        } else {
            log.debug("🔑 요청 [{}] 에서 토큰 감지: {}", requestURI, token.substring(0, Math.min(15, token.length())) + "...");

            if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                String email = jwtTokenProvider.getEmail(token);

                log.info("✅ 유효한 토큰 - userId={}, email={}, uri={}", userId, email, requestURI);

                UserDetails userDetails = User.builder()
                    .username(userId.toString())
                    .password("") // 이미 인증된 상태이므로 빈 값
                    .authorities(Collections.emptyList())
                    .build();

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                log.warn("⚠️ 잘못된 JWT 토큰 - uri={}", requestURI);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request에서 JWT 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
