package com.stockquest.application.auth;

import com.stockquest.application.auth.port.in.LogoutUseCase;
import com.stockquest.domain.auth.port.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그아웃 서비스
 * 토큰 폐기 및 로그아웃 처리 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Override
    public LogoutResult logout(LogoutCommand command) {
        try {
            if (command.logoutFromAllDevices()) {
                // 모든 디바이스에서 로그아웃 - 사용자의 모든 리프레시 토큰 폐기
                refreshTokenRepository.revokeAllTokensByUserId(command.userId());
                log.info("All tokens revoked for userId: {}", command.userId());
                return new LogoutResult(true, "모든 디바이스에서 로그아웃되었습니다.");
                
            } else if (command.refreshToken() != null && !command.refreshToken().isBlank()) {
                // 특정 리프레시 토큰만 폐기
                refreshTokenRepository.deleteByToken(command.refreshToken());
                log.info("Specific token revoked for userId: {}", command.userId());
                return new LogoutResult(true, "현재 디바이스에서 로그아웃되었습니다.");
                
            } else {
                // 기본적으로 현재 세션만 로그아웃 (Access Token은 클라이언트에서 삭제)
                // 서버에서 추적하는 세션이나 캐시가 있다면 여기서 정리
                log.info("Session logout for userId: {}", command.userId());
                return new LogoutResult(true, "로그아웃되었습니다.");
            }
        } catch (Exception e) {
            log.error("Logout failed for userId: {}", command.userId(), e);
            return new LogoutResult(false, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 만료된 토큰 정리 (스케줄러에서 사용)
     */
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens();
            log.info("Expired tokens cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }
    
    /**
     * 사용자별 활성 토큰 수 조회
     */
    public long getActiveTokenCount(Long userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId);
    }
}