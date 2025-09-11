package com.stockquest.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트 기본 클래스
 * 이벤트 기반 아키텍처의 핵심 추상 클래스
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredAt;
    private final String eventType;
    private final Long aggregateId;
    private final String userId;
    
    protected DomainEvent(Long aggregateId, String userId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
        this.aggregateId = aggregateId;
        this.userId = userId;
    }
    
    /**
     * 이벤트 우선순위 (높을수록 먼저 처리)
     */
    public abstract int getPriority();
    
    /**
     * 이벤트 재시도 가능 여부
     */
    public boolean isRetryable() {
        return true;
    }
    
    /**
     * 최대 재시도 횟수
     */
    public int getMaxRetryCount() {
        return 3;
    }
    
    /**
     * 이벤트 만료 시간 (분)
     */
    public int getExpirationMinutes() {
        return 60;
    }
    
    /**
     * 이벤트 분류 (모니터링용)
     */
    public abstract EventCategory getCategory();
    
    // Getters
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public Long getAggregateId() {
        return aggregateId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    /**
     * 이벤트가 만료되었는지 확인
     */
    public boolean isExpired() {
        return occurredAt.plusMinutes(getExpirationMinutes()).isBefore(LocalDateTime.now());
    }
    
    /**
     * 이벤트 발생 후 경과 시간 (분)
     */
    public long getMinutesElapsed() {
        return java.time.Duration.between(occurredAt, LocalDateTime.now()).toMinutes();
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId=%d, userId='%s', occurredAt=%s}",
                eventType, eventId, aggregateId, userId, occurredAt);
    }
    
    /**
     * 이벤트 카테고리 열거형
     */
    public enum EventCategory {
        TRADING("거래", 1),
        USER("사용자", 2),
        SYSTEM("시스템", 3),
        NOTIFICATION("알림", 4),
        ANALYTICS("분석", 5);
        
        private final String description;
        private final int order;
        
        EventCategory(String description, int order) {
            this.description = description;
            this.order = order;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getOrder() {
            return order;
        }
    }
}