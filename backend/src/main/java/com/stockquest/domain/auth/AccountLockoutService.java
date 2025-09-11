package com.stockquest.domain.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계정 잠금 도메인 서비스
 * 로그인 실패 추적 및 계정 보안 관리
 */
public class AccountLockoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutService.class);
    
    // 계정 잠금 정책 설정
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int ATTEMPT_WINDOW_MINUTES = 15;
    
    /**
     * 계정 잠금 상태 확인
     */
    public AccountLockoutStatus checkLockoutStatus(String email, List<LoginAttempt> recentAttempts) {
        if (email == null || recentAttempts == null) {
            return AccountLockoutStatus.notLocked();
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(ATTEMPT_WINDOW_MINUTES);
        
        // 지정된 시간 창 내의 실패한 시도만 계산
        List<LoginAttempt> failedAttemptsInWindow = recentAttempts.stream()
                .filter(attempt -> !attempt.isSuccessful())
                .filter(attempt -> attempt.isAfter(windowStart))
                .toList();
        
        if (failedAttemptsInWindow.size() < MAX_FAILED_ATTEMPTS) {
            return AccountLockoutStatus.notLocked();
        }
        
        // 가장 최근 실패 시도부터 잠금 시간 계산
        LoginAttempt lastFailedAttempt = failedAttemptsInWindow.stream()
                .max((a1, a2) -> a1.getAttemptedAt().compareTo(a2.getAttemptedAt()))
                .orElse(null);
        
        if (lastFailedAttempt == null) {
            return AccountLockoutStatus.notLocked();
        }
        
        LocalDateTime lockoutEndsAt = lastFailedAttempt.getAttemptedAt().plusMinutes(LOCKOUT_DURATION_MINUTES);
        
        if (now.isBefore(lockoutEndsAt)) {
            long remainingMinutes = java.time.Duration.between(now, lockoutEndsAt).toMinutes();
            logger.info("Account {} is locked. Remaining time: {} minutes", email, remainingMinutes);
            return AccountLockoutStatus.locked(lockoutEndsAt, remainingMinutes);
        }
        
        return AccountLockoutStatus.notLocked();
    }
    
    /**
     * IP 주소 기반 잠금 상태 확인
     */
    public AccountLockoutStatus checkIpLockoutStatus(String ipAddress, List<LoginAttempt> recentAttempts) {
        // IP 기반은 더 엄격한 정책 적용 (3회 실패 시 1시간 잠금)
        final int MAX_IP_FAILED_ATTEMPTS = 3;
        final int IP_LOCKOUT_DURATION_MINUTES = 60;
        
        if (ipAddress == null || recentAttempts == null) {
            return AccountLockoutStatus.notLocked();
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(ATTEMPT_WINDOW_MINUTES);
        
        List<LoginAttempt> failedAttemptsInWindow = recentAttempts.stream()
                .filter(attempt -> !attempt.isSuccessful())
                .filter(attempt -> attempt.isAfter(windowStart))
                .toList();
        
        if (failedAttemptsInWindow.size() < MAX_IP_FAILED_ATTEMPTS) {
            return AccountLockoutStatus.notLocked();
        }
        
        LoginAttempt lastFailedAttempt = failedAttemptsInWindow.stream()
                .max((a1, a2) -> a1.getAttemptedAt().compareTo(a2.getAttemptedAt()))
                .orElse(null);
        
        if (lastFailedAttempt == null) {
            return AccountLockoutStatus.notLocked();
        }
        
        LocalDateTime lockoutEndsAt = lastFailedAttempt.getAttemptedAt().plusMinutes(IP_LOCKOUT_DURATION_MINUTES);
        
        if (now.isBefore(lockoutEndsAt)) {
            long remainingMinutes = java.time.Duration.between(now, lockoutEndsAt).toMinutes();
            logger.warn("IP address {} is locked. Remaining time: {} minutes", ipAddress, remainingMinutes);
            return AccountLockoutStatus.locked(lockoutEndsAt, remainingMinutes);
        }
        
        return AccountLockoutStatus.notLocked();
    }
    
    /**
     * 로그인 성공 시 실패 기록 정리
     */
    public void handleSuccessfulLogin(String email, String ipAddress) {
        logger.info("Successful login for email: {} from IP: {}", email, ipAddress);
        // 성공한 로그인 후에는 해당 계정과 IP의 실패 카운터를 리셋
        // (실제 구현에서는 Repository를 통해 처리)
    }
    
    /**
     * 계정 잠금 해제 (관리자 기능)
     */
    public void unlockAccount(String email) {
        logger.info("Admin unlocking account: {}", email);
        // 관리자가 수동으로 계정 잠금 해제
        // (실제 구현에서는 Repository를 통해 실패 기록 삭제)
    }
    
    /**
     * 잠금 정책 정보 조회
     */
    public LockoutPolicyInfo getPolicyInfo() {
        return new LockoutPolicyInfo(
            MAX_FAILED_ATTEMPTS,
            LOCKOUT_DURATION_MINUTES,
            ATTEMPT_WINDOW_MINUTES
        );
    }
    
    /**
     * 계정 잠금 상태 정보
     */
    public static class AccountLockoutStatus {
        private final boolean locked;
        private final LocalDateTime lockedUntil;
        private final long remainingMinutes;
        
        private AccountLockoutStatus(boolean locked, LocalDateTime lockedUntil, long remainingMinutes) {
            this.locked = locked;
            this.lockedUntil = lockedUntil;
            this.remainingMinutes = remainingMinutes;
        }
        
        public static AccountLockoutStatus locked(LocalDateTime lockedUntil, long remainingMinutes) {
            return new AccountLockoutStatus(true, lockedUntil, remainingMinutes);
        }
        
        public static AccountLockoutStatus notLocked() {
            return new AccountLockoutStatus(false, null, 0);
        }
        
        public boolean isLocked() {
            return locked;
        }
        
        public LocalDateTime getLockedUntil() {
            return lockedUntil;
        }
        
        public long getRemainingMinutes() {
            return remainingMinutes;
        }
        
        public String getLockoutMessage() {
            if (!locked) {
                return null;
            }
            return String.format("계정이 잠겨있습니다. %d분 후에 다시 시도해주세요.", remainingMinutes);
        }
    }
    
    /**
     * 잠금 정책 정보
     */
    public static class LockoutPolicyInfo {
        private final int maxFailedAttempts;
        private final int lockoutDurationMinutes;
        private final int attemptWindowMinutes;
        
        public LockoutPolicyInfo(int maxFailedAttempts, int lockoutDurationMinutes, int attemptWindowMinutes) {
            this.maxFailedAttempts = maxFailedAttempts;
            this.lockoutDurationMinutes = lockoutDurationMinutes;
            this.attemptWindowMinutes = attemptWindowMinutes;
        }
        
        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }
        
        public int getLockoutDurationMinutes() {
            return lockoutDurationMinutes;
        }
        
        public int getAttemptWindowMinutes() {
            return attemptWindowMinutes;
        }
    }
}