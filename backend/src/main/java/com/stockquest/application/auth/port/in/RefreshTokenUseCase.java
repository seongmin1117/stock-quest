package com.stockquest.application.auth.port.in;

/**
 * 토큰 갱신 유스케이스 (입력 포트)
 */
public interface RefreshTokenUseCase {
    
    /**
     * 리프레시 토큰을 이용한 액세스 토큰 갱신
     */
    RefreshTokenResult refreshToken(RefreshTokenCommand command);
    
    record RefreshTokenCommand(
        String refreshToken
    ) {}
    
    record RefreshTokenResult(
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