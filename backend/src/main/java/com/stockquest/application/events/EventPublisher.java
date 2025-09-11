package com.stockquest.application.events;

import com.stockquest.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 도메인 이벤트 발행자
 * Spring Event 기반 이벤트 발행 및 관리
 */
@Service
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 동기 이벤트 발행
     */
    public void publishEvent(DomainEvent event) {
        try {
            logger.debug("Publishing synchronous event: {}", event.getEventType());
            applicationEventPublisher.publishEvent(event);
            logger.debug("Successfully published event: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Failed to publish synchronous event: {}", event.getEventId(), e);
            throw new EventPublishException("Failed to publish event", e);
        }
    }
    
    /**
     * 비동기 이벤트 발행
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> publishEventAsync(DomainEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Publishing asynchronous event: {}", event.getEventType());
                applicationEventPublisher.publishEvent(event);
                logger.debug("Successfully published async event: {}", event.getEventId());
            } catch (Exception e) {
                logger.error("Failed to publish async event: {}", event.getEventId(), e);
                throw new EventPublishException("Failed to publish async event", e);
            }
        });
    }
    
    /**
     * 우선순위 기반 이벤트 발행
     */
    public void publishWithPriority(DomainEvent event) {
        if (event.getPriority() >= 8) {
            // 높은 우선순위는 동기 처리
            publishEvent(event);
        } else {
            // 낮은 우선순위는 비동기 처리
            publishEventAsync(event);
        }
    }
    
    /**
     * 배치 이벤트 발행
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> publishBatch(DomainEvent... events) {
        return CompletableFuture.runAsync(() -> {
            logger.debug("Publishing batch of {} events", events.length);
            
            int successCount = 0;
            int failureCount = 0;
            
            for (DomainEvent event : events) {
                try {
                    applicationEventPublisher.publishEvent(event);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to publish event in batch: {}", event.getEventId(), e);
                    failureCount++;
                }
            }
            
            logger.info("Batch publication completed: {} success, {} failures", successCount, failureCount);
        });
    }
    
    /**
     * 조건부 이벤트 발행
     */
    public void publishIf(DomainEvent event, boolean condition) {
        if (condition) {
            publishWithPriority(event);
        } else {
            logger.debug("Event not published due to condition: {}", event.getEventType());
        }
    }
    
    /**
     * 지연 이벤트 발행
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> publishDelayed(DomainEvent event, long delayMillis) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMillis);
                publishEvent(event);
                logger.debug("Delayed event published after {}ms: {}", delayMillis, event.getEventId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Delayed event publication interrupted: {}", event.getEventId());
            } catch (Exception e) {
                logger.error("Failed to publish delayed event: {}", event.getEventId(), e);
            }
        });
    }
    
    /**
     * 이벤트 발행 예외
     */
    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}