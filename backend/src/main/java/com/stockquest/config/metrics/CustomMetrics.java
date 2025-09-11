package com.stockquest.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 커스텀 비즈니스 메트릭
 * 애플리케이션 특화 지표들을 Prometheus/Micrometer로 노출
 */
@Component
@RequiredArgsConstructor
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    // 메트릭 카운터들
    private Counter orderPlacedCounter;
    private Counter orderExecutedCounter;
    private Counter orderFailedCounter;
    private Counter userRegistrationCounter;
    private Counter userLoginCounter;
    private Counter challengeStartedCounter;
    private Counter challengeCompletedCounter;
    
    // 메트릭 타이머들
    private Timer orderProcessingTimer;
    private Timer simulationTickTimer;
    private Timer marketDataFetchTimer;
    
    // 게이지용 원자적 정수들
    private final AtomicInteger activeUsersGauge = new AtomicInteger(0);
    private final AtomicInteger activeSessionsGauge = new AtomicInteger(0);

    @PostConstruct
    public void initializeMetrics() {
        // 카운터 메트릭
        orderPlacedCounter = Counter.builder("stockquest.orders.placed")
                .description("Total number of orders placed")
                .tag("type", "business")
                .register(meterRegistry);
                
        orderExecutedCounter = Counter.builder("stockquest.orders.executed")
                .description("Total number of orders successfully executed")
                .tag("type", "business")
                .register(meterRegistry);
                
        orderFailedCounter = Counter.builder("stockquest.orders.failed")
                .description("Total number of failed orders")
                .tag("type", "business")
                .register(meterRegistry);
                
        userRegistrationCounter = Counter.builder("stockquest.users.registered")
                .description("Total number of user registrations")
                .tag("type", "user")
                .register(meterRegistry);
                
        userLoginCounter = Counter.builder("stockquest.users.login")
                .description("Total number of user logins")
                .tag("type", "user")
                .register(meterRegistry);
                
        challengeStartedCounter = Counter.builder("stockquest.challenges.started")
                .description("Total number of challenges started")
                .tag("type", "challenge")
                .register(meterRegistry);
                
        challengeCompletedCounter = Counter.builder("stockquest.challenges.completed")
                .description("Total number of challenges completed")
                .tag("type", "challenge")
                .register(meterRegistry);

        // 타이머 메트릭
        orderProcessingTimer = Timer.builder("stockquest.orders.processing.duration")
                .description("Order processing time")
                .tag("type", "performance")
                .register(meterRegistry);
                
        simulationTickTimer = Timer.builder("stockquest.simulation.tick.duration")
                .description("Simulation tick processing time")
                .tag("type", "performance")
                .register(meterRegistry);
                
        marketDataFetchTimer = Timer.builder("stockquest.marketdata.fetch.duration")
                .description("Market data fetch time")
                .tag("type", "performance")
                .register(meterRegistry);

        // 게이지 메트릭
        Gauge.builder("stockquest.users.active", this::getActiveUsers)
                .description("Number of currently active users")
                .tag("type", "user")
                .register(meterRegistry);
                
        Gauge.builder("stockquest.sessions.active", this::getActiveSessions)
                .description("Number of currently active simulation sessions")
                .tag("type", "simulation")
                .register(meterRegistry);
                
        // Redis gauge temporarily disabled due to API compatibility issues
        // TODO: Fix Redis info() method return type issue
        // Gauge.builder("stockquest.redis.memory.usage", this::getRedisMemoryUsage)
        //         .description("Redis memory usage in bytes")
        //         .tag("type", "system")
        //         .register(meterRegistry);
    }

    // 카운터 증가 메서드들
    public void incrementOrderPlaced() {
        orderPlacedCounter.increment();
    }
    
    public void incrementOrderPlaced(String orderType) {
        Counter.builder("stockquest.orders.placed")
                .description("Total number of orders placed by type")
                .tag("type", "business")
                .tag("order_type", orderType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementOrderExecuted() {
        orderExecutedCounter.increment();
    }

    public void incrementOrderFailed(String reason) {
        Counter.builder("stockquest.orders.failed")
                .description("Total number of failed orders by reason")
                .tag("type", "business")
                .tag("failure_reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
    }

    public void incrementUserLogin() {
        userLoginCounter.increment();
    }

    public void incrementChallengeStarted() {
        challengeStartedCounter.increment();
    }

    public void incrementChallengeCompleted() {
        challengeCompletedCounter.increment();
    }

    // 타이머 메서드들
    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordOrderProcessingTime(Timer.Sample sample) {
        sample.stop(orderProcessingTimer);
    }

    public Timer.Sample startSimulationTickTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordSimulationTickTime(Timer.Sample sample) {
        sample.stop(simulationTickTimer);
    }

    public Timer.Sample startMarketDataFetchTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordMarketDataFetchTime(Timer.Sample sample) {
        sample.stop(marketDataFetchTimer);
    }

    // 게이지 값 업데이트
    public void updateActiveUsers(int count) {
        activeUsersGauge.set(count);
    }

    public void updateActiveSessions(int count) {
        activeSessionsGauge.set(count);
    }

    // 게이지 값 조회 (private, Gauge가 호출)
    private double getActiveUsers() {
        return activeUsersGauge.get();
    }

    private double getActiveSessions() {
        return activeSessionsGauge.get();
    }

    private double getRedisMemoryUsage() {
        try {
            // Redis INFO 명령으로 메모리 사용량 조회 - temporarily disabled due to API issues
            // TODO: Fix Redis connection factory info() return type
            return 0.0; // Placeholder
        } catch (Exception e) {
            return -1.0; // 에러 표시
        }
    }
}