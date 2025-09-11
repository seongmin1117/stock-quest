package com.stockquest.application.simulation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 시뮬레이션 상태 관리 객체
 * 각 세션의 현재 시뮬레이션 진행 상태를 추적
 */
@Data
@Builder
public class SimulationState {
    
    // 세션 식별자
    private Long sessionId;
    private Long challengeId;
    
    // 시뮬레이션 설정
    private int speedFactor;           // 시간 가속 배율
    private LocalDate periodStart;     // 시뮬레이션 시작일
    private LocalDate periodEnd;       // 시뮬레이션 종료일
    
    // 현재 상태
    private LocalDate currentSimulationDate;  // 현재 시뮬레이션 날짜
    private LocalDateTime simulationStartedAt; // 시뮬레이션 시작 시점 (실제 시간)
    private LocalDateTime lastProcessedAt;     // 마지막 처리 시점
    
    // 로깅 및 모니터링
    @Builder.Default
    private int lastLoggedProgress = 0;  // 마지막으로 로깅한 진행률 (10% 단위)
    
    /**
     * 시뮬레이션 진행률 계산 (0-100%)
     */
    public double calculateProgress() {
        if (periodStart == null || periodEnd == null || currentSimulationDate == null) {
            return 0.0;
        }
        
        long totalDays = periodStart.datesUntil(periodEnd.plusDays(1)).count();
        long elapsedDays = periodStart.datesUntil(currentSimulationDate.plusDays(1)).count();
        
        if (totalDays <= 0) {
            return 100.0;
        }
        
        return Math.min(100.0, (double) elapsedDays * 100.0 / totalDays);
    }
    
    /**
     * 시뮬레이션 완료 여부
     */
    public boolean isCompleted() {
        return currentSimulationDate != null && 
               periodEnd != null && 
               !currentSimulationDate.isBefore(periodEnd);
    }
    
    /**
     * 시뮬레이션 실행 시간 (실제 경과 시간)
     */
    public long getElapsedRealTimeMinutes() {
        if (simulationStartedAt == null || lastProcessedAt == null) {
            return 0;
        }
        
        return java.time.Duration.between(simulationStartedAt, lastProcessedAt).toMinutes();
    }
    
    /**
     * 예상 완료 시간 계산
     */
    public LocalDateTime getEstimatedCompletionTime() {
        if (isCompleted() || simulationStartedAt == null) {
            return LocalDateTime.now();
        }
        
        double progress = calculateProgress();
        if (progress <= 0) {
            return null; // 예측 불가
        }
        
        long elapsedRealMs = java.time.Duration.between(simulationStartedAt, LocalDateTime.now()).toMillis();
        long estimatedTotalMs = (long) (elapsedRealMs / progress * 100);
        long remainingMs = estimatedTotalMs - elapsedRealMs;
        
        return LocalDateTime.now().plusNanos(remainingMs * 1_000_000);
    }
    
    /**
     * 상태 요약 정보
     */
    public String getSummary() {
        return String.format(
            "Session[%d] %.1f%% complete - SimDate: %s, Speed: %dx, Elapsed: %d min",
            sessionId,
            calculateProgress(),
            currentSimulationDate,
            speedFactor,
            getElapsedRealTimeMinutes()
        );
    }
}