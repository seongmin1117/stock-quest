package com.stockquest.application.auth.port.in;

/**
 * 로그인 유스케이스 (입력 포트)
 */
public interface LoginUseCase {
    
    /**
     * 사용자 인증 및 토큰 발급
     */
    LoginResult login(LoginCommand command);
    
    record LoginCommand(
        String email,
        String password
    ) {}
    
    record LoginResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String nickname,
        java.time.LocalDateTime accessTokenExpiresAt,
        java.time.LocalDateTime refreshTokenExpiresAt,
        String message
    ) {}
}