package com.stockquest.domain.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 세션 도메인 엔티티
 * 특정 사용자의 특정 챌린지 참여 세션
 */
@Getter
@Builder
@AllArgsConstructor
public class ChallengeSession {
    
    public enum SessionStatus {
        READY("준비"),        // 생성됨, 아직 시작되지 않음
        ACTIVE("진행중"),      // 활성화됨, 거래 가능
        COMPLETED("완료"),     // 정상 완료됨, 거래 불가
        CANCELLED("취소"),     // 사용자가 취소함, 거래 불가
        ENDED("종료");         // 종료됨 (레거시 지원)
        
        private final String description;
        
        SessionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isCompleted() {
            return this == COMPLETED || this == CANCELLED || this == ENDED;
        }
        
        public boolean isActive() {
            return this == ACTIVE;
        }
        
        public boolean canStartNewSession() {
            return isCompleted();
        }
    }
    private Long id;
    private Long challengeId;
    private Long userId;
    private BigDecimal initialBalance;  // 시작 자금 (JPA 엔티티와 매칭)
    private BigDecimal currentBalance; // 현재 현금 잔고
    private BigDecimal returnRate;     // 수익률
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt; // endedAt -> completedAt (JPA 엔티티와 매칭)
    private LocalDateTime createdAt;
    
    protected ChallengeSession() {}
    
    public ChallengeSession(Long challengeId, Long userId, BigDecimal initialBalance) {
        validateChallengeId(challengeId);
        validateUserId(userId);
        validateInitialBalance(initialBalance);
        
        this.challengeId = challengeId;
        this.userId = userId;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance;
        this.status = SessionStatus.READY;
        this.createdAt = LocalDateTime.now();
    }
    
    private void validateChallengeId(Long challengeId) {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("유효한 챌린지 ID가 필요합니다");
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효한 사용자 ID가 필요합니다");
        }
    }
    
    private void validateInitialBalance(BigDecimal initialBalance) {
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("시드 머니는 0보다 커야 합니다");
        }
        if (initialBalance.compareTo(new BigDecimal("100000000")) > 0) {
            throw new IllegalArgumentException("시드 머니는 1억원을 초과할 수 없습니다");
        }
    }
    
    public void start() {
        if (this.status != SessionStatus.READY) {
            throw new IllegalStateException("준비 상태의 세션만 시작할 수 있습니다");
        }
        this.status = SessionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }
    
    public void end() {
        if (this.status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 세션만 종료할 수 있습니다");
        }
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (this.status != SessionStatus.ACTIVE && this.status != SessionStatus.READY) {
            throw new IllegalStateException("진행 중이거나 준비 상태의 세션만 취소할 수 있습니다");
        }
        this.status = SessionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean canStart() {
        return this.status == SessionStatus.READY;
    }
    
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }
    
    public boolean isCompleted() {
        return this.status.isCompleted();
    }
    
    public void updateBalance(BigDecimal newBalance) {
        if (newBalance == null) {
            throw new IllegalArgumentException("잔고는 null일 수 없습니다");
        }
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("잔고는 음수일 수 없습니다");
        }
        this.currentBalance = newBalance;
    }
    
    public boolean canPlaceOrder(BigDecimal orderValue) {
        return this.status == SessionStatus.ACTIVE && 
               this.currentBalance.compareTo(orderValue) >= 0;
    }
    
    public BigDecimal calculateTotalPnL(BigDecimal portfolioValue) {
        return portfolioValue.add(currentBalance).subtract(initialBalance);
    }
    
    public BigDecimal calculateReturnPercentage(BigDecimal portfolioValue) {
        BigDecimal totalValue = portfolioValue.add(currentBalance);
        this.returnRate = totalValue.subtract(initialBalance)
                        .divide(initialBalance, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
        return this.returnRate;
    }
}