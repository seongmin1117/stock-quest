package com.stockquest.application.auth.port.in;

/**
 * 회원가입 유스케이스 (입력 포트)
 */
public interface SignupUseCase {
    
    /**
     * 새 사용자 등록
     */
    SignupResult signup(SignupCommand command);
    
    record SignupCommand(
        String email,
        String password,
        String nickname
    ) {}
    
    record SignupResult(
        Long userId,
        String email,
        String nickname,
        java.time.LocalDateTime createdAt,
        String message
    ) {}
}