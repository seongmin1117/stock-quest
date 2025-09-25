package com.stockquest.application.port.out;

import com.stockquest.domain.analytics.risk.MonteCarloSimulation;
import com.stockquest.domain.analytics.risk.RiskEngine;
import com.stockquest.domain.analytics.risk.RiskScenario;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 리스크 평가 포트 인터페이스
 */
public interface RiskAssessmentPort {
    
    /**
     * 리스크 엔진 생성
     */
    RiskEngine createRiskEngine(RiskEngine engine);
    
    /**
     * 리스크 엔진 조회
     */
    Optional<RiskEngine> findRiskEngine(String engineId);
    
    /**
     * 리스크 엔진 업데이트
     */
    RiskEngine updateRiskEngine(RiskEngine engine);
    
    /**
     * 리스크 시나리오 저장
     */
    RiskScenario saveRiskScenario(RiskScenario scenario);
    
    /**
     * 리스크 시나리오 조회
     */
    Optional<RiskScenario> findRiskScenario(String scenarioId);
    
    /**
     * 활성 리스크 시나리오 목록 조회
     */
    List<RiskScenario> findActiveRiskScenarios();
    
    /**
     * Monte Carlo 시뮬레이션 저장
     */
    MonteCarloSimulation saveMonteCarloSimulation(MonteCarloSimulation simulation);
    
    /**
     * Monte Carlo 시뮬레이션 조회
     */
    Optional<MonteCarloSimulation> findMonteCarloSimulation(String simulationId);
    
    /**
     * 포트폴리오별 최근 시뮬레이션 결과 조회
     */
    List<MonteCarloSimulation> findRecentSimulationsByPortfolio(Long portfolioId, int limit);
    
    /**
     * 비동기 시뮬레이션 실행 요청
     */
    CompletableFuture<MonteCarloSimulation> executeSimulationAsync(String scenarioId, Long portfolioId);
    
    /**
     * 시뮬레이션 상태 업데이트
     */
    MonteCarloSimulation updateSimulationStatus(String simulationId, MonteCarloSimulation.SimulationStatus status);
    
    /**
     * 실행 중인 시뮬레이션 목록 조회
     */
    List<MonteCarloSimulation> findRunningSimulations();
    
    /**
     * 시뮬레이션 결과 캐시 저장
     */
    void cacheSimulationResult(String cacheKey, MonteCarloSimulation result, long ttlSeconds);
    
    /**
     * 시뮬레이션 결과 캐시 조회
     */
    Optional<MonteCarloSimulation> getCachedSimulationResult(String cacheKey);
    
    /**
     * 리스크 알림 발송
     */
    void sendRiskAlert(String engineId, String alertMessage, String severity);
}