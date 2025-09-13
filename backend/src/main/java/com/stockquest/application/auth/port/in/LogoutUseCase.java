package com.stockquest.application.auth.port.in;

/**
 * 로그아웃 유스케이스 (입력 포트)
 */
public interface LogoutUseCase {
    
    /**
     * 사용자 로그아웃 및 토큰 무효화
     */
    LogoutResult logout(LogoutCommand command);
    
    record LogoutCommand(
        Long userId,
        String refreshToken,
        boolean logoutFromAllDevices
    ) {}
    
    record LogoutResult(
        boolean success,
        String message
    ) {}
}