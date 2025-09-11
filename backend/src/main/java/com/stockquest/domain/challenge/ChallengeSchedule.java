package com.stockquest.domain.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 챌린지 스케줄 도메인 엔티티
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSchedule {
    private Long id;
    private Long challengeId;
    private ScheduleType scheduleType;
    private String recurrencePattern;   // CRON expression for recurring challenges
    private LocalDateTime activationDate;
    private LocalDateTime deactivationDate;
    private String timezone;
    private Boolean isActive;
    private String metadata;   // Additional scheduling metadata as JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 스케줄 유형
     */
    public enum ScheduleType {
        ONE_TIME("일회성", "한 번만 실행되는 스케줄"),
        RECURRING("반복", "주기적으로 반복되는 스케줄"),
        EVENT_BASED("이벤트 기반", "특정 이벤트에 의해 트리거되는 스케줄");
        
        private final String displayName;
        private final String description;
        
        ScheduleType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 현재 시점에서 활성화되어야 하는지 확인
     */
    public boolean shouldBeActiveAt(LocalDateTime dateTime) {
        if (!isActive) return false;
        
        boolean afterActivation = activationDate == null || !dateTime.isBefore(activationDate);
        boolean beforeDeactivation = deactivationDate == null || !dateTime.isAfter(deactivationDate);
        
        return afterActivation && beforeDeactivation;
    }
    
    /**
     * 다음 활성화 시간 계산 (반복 스케줄의 경우)
     */
    public LocalDateTime getNextActivationTime(LocalDateTime from) {
        if (scheduleType != ScheduleType.RECURRING || recurrencePattern == null) {
            return null;
        }
        
        // CRON 표현식 파싱 및 다음 실행 시간 계산 로직
        // 실제 구현에서는 CRON 라이브러리 사용 (예: cron-parser)
        return null; // Placeholder
    }
}