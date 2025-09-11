package com.stockquest.application.auth.port.in;

/**
 * 현재 사용자 조회 유스케이스 (입력 포트)
 */
public interface GetCurrentUserUseCase {
    
    /**
     * 현재 인증된 사용자 정보 조회
     */
    CurrentUserResult getCurrentUser(Long userId);
    
    record CurrentUserResult(
        Long userId,
        String email,
        String nickname,
        java.time.LocalDateTime createdAt
    ) {}
}