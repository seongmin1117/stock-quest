package com.stockquest.config.metrics;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 메트릭 수집 Aspect
 * 비즈니스 로직 실행 시 자동으로 메트릭을 수집하고 기록
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final CustomMetrics customMetrics;

    /**
     * 주문 처리 메서드에 대한 메트릭 수집
     */
    @Around("execution(* com.stockquest.application.session.SessionService.placeOrder(..))")
    public Object measureOrderProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = customMetrics.startOrderProcessingTimer();
        
        try {
            // 주문 시작 카운트
            customMetrics.incrementOrderPlaced();
            
            Object result = joinPoint.proceed();
            
            // 성공 카운트
            customMetrics.incrementOrderExecuted();
            
            return result;
            
        } catch (Exception e) {
            // 실패 카운트
            String failureReason = determineFailureReason(e);
            customMetrics.incrementOrderFailed(failureReason);
            throw e;
            
        } finally {
            // 처리 시간 기록
            customMetrics.recordOrderProcessingTime(sample);
        }
    }

    /**
     * 시뮬레이션 틱 처리 메트릭 수집
     */
    @Around("execution(* com.stockquest.application.simulation.ChallengeSimulationService.processSimulationTick(..))")
    public Object measureSimulationTick(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = customMetrics.startSimulationTickTimer();
        
        try {
            return joinPoint.proceed();
        } finally {
            customMetrics.recordSimulationTickTime(sample);
        }
    }

    /**
     * 시장 데이터 조회 메트릭 수집
     */
    @Around("execution(* com.stockquest.adapter.out.market.YahooFinanceAdapter.fetchLatestPrice(..))")
    public Object measureMarketDataFetch(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = customMetrics.startMarketDataFetchTimer();
        
        try {
            return joinPoint.proceed();
        } finally {
            customMetrics.recordMarketDataFetchTime(sample);
        }
    }

    /**
     * 사용자 등록 메트릭 수집
     */
    @Around("execution(* com.stockquest.application.auth.AuthService.signup(..))")
    public Object measureUserRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            customMetrics.incrementUserRegistration();
            return result;
        } catch (Exception e) {
            // 실패한 경우에는 카운트하지 않음
            throw e;
        }
    }

    /**
     * 사용자 로그인 메트릭 수집
     */
    @Around("execution(* com.stockquest.application.auth.AuthService.login(..))")
    public Object measureUserLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            customMetrics.incrementUserLogin();
            return result;
        } catch (Exception e) {
            // 실패한 경우에는 카운트하지 않음 (보안상 로그인 실패는 별도 처리)
            throw e;
        }
    }

    /**
     * 챌린지 시작 메트릭 수집
     */
    @Around("execution(* com.stockquest.application.challenge.ChallengeService.startChallenge(..))")
    public Object measureChallengeStart(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            customMetrics.incrementChallengeStarted();
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 예외에 따른 실패 사유 분류
     */
    private String determineFailureReason(Exception e) {
        String className = e.getClass().getSimpleName();
        
        if (className.contains("InsufficientFunds")) {
            return "insufficient_funds";
        } else if (className.contains("InvalidOrder")) {
            return "invalid_order";
        } else if (className.contains("MarketClosed")) {
            return "market_closed";
        } else if (className.contains("Timeout")) {
            return "timeout";
        } else {
            return "unknown";
        }
    }
}