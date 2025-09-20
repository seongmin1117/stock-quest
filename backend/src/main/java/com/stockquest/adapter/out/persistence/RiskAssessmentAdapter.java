package com.stockquest.adapter.out.persistence;

import com.stockquest.application.port.out.RiskAssessmentPort;
import com.stockquest.domain.analytics.risk.MonteCarloSimulation;
import com.stockquest.domain.analytics.risk.RiskEngine;
import com.stockquest.domain.analytics.risk.RiskScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 리스크 평가 포트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskAssessmentAdapter implements RiskAssessmentPort {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    
    // 메모리 기반 임시 저장소 (실제 구현시 데이터베이스 사용)
    private final Map<String, RiskEngine> riskEngines = new ConcurrentHashMap<>();
    private final Map<String, RiskScenario> riskScenarios = new ConcurrentHashMap<>();
    private final Map<String, MonteCarloSimulation> simulations = new ConcurrentHashMap<>();
    
    @Override
    public RiskEngine createRiskEngine(RiskEngine engine) {
        if (engine.getEngineId() == null) {
            engine.setEngineId(UUID.randomUUID().toString());
        }
        engine.setCreatedAt(LocalDateTime.now());
        engine.setLastUpdate(LocalDateTime.now());
        
        riskEngines.put(engine.getEngineId(), engine);
        log.info("리스크 엔진 생성: engineId={}", engine.getEngineId());
        
        return engine;
    }
    
    @Override
    public Optional<RiskEngine> findRiskEngine(String engineId) {
        return Optional.ofNullable(riskEngines.get(engineId));
    }
    
    @Override
    public RiskEngine updateRiskEngine(RiskEngine engine) {
        engine.setLastUpdate(LocalDateTime.now());
        riskEngines.put(engine.getEngineId(), engine);
        log.info("리스크 엔진 업데이트: engineId={}", engine.getEngineId());
        
        return engine;
    }
    
    @Override
    public RiskScenario saveRiskScenario(RiskScenario scenario) {
        if (scenario.getScenarioId() == null) {
            scenario.setScenarioId(UUID.randomUUID().toString());
        }
        scenario.setUpdatedAt(LocalDateTime.now());
        if (scenario.getCreatedAt() == null) {
            scenario.setCreatedAt(LocalDateTime.now());
        }
        
        riskScenarios.put(scenario.getScenarioId(), scenario);
        log.info("리스크 시나리오 저장: scenarioId={}", scenario.getScenarioId());
        
        return scenario;
    }
    
    @Override
    public Optional<RiskScenario> findRiskScenario(String scenarioId) {
        return Optional.ofNullable(riskScenarios.get(scenarioId));
    }
    
    @Override
    public List<RiskScenario> findActiveRiskScenarios() {
        return riskScenarios.values().stream()
            .filter(scenario -> scenario.getIsActive() != null && scenario.getIsActive())
            .toList();
    }
    
    @Override
    public MonteCarloSimulation saveMonteCarloSimulation(MonteCarloSimulation simulation) {
        simulations.put(simulation.getSimulationId(), simulation);
        log.info("Monte Carlo 시뮬레이션 저장: simulationId={}", simulation.getSimulationId());
        
        return simulation;
    }
    
    @Override
    public Optional<MonteCarloSimulation> findMonteCarloSimulation(String simulationId) {
        return Optional.ofNullable(simulations.get(simulationId));
    }
    
    @Override
    public List<MonteCarloSimulation> findRecentSimulationsByPortfolio(Long portfolioId, int limit) {
        return simulations.values().stream()
            .filter(sim -> sim.getPortfolioId().equals(portfolioId))
            .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
            .limit(limit)
            .toList();
    }
    
    @Async("riskAssessmentTaskExecutor")
    @Override
    public CompletableFuture<MonteCarloSimulation> executeSimulationAsync(String scenarioId, Long portfolioId) {
        // 비동기 시뮬레이션 실행은 RealTimeRiskAssessmentService에서 처리
        // 이 메소드는 포트 인터페이스 일관성을 위한 것
        log.info("비동기 시뮬레이션 실행 요청: scenarioId={}, portfolioId={}", scenarioId, portfolioId);
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public MonteCarloSimulation updateSimulationStatus(String simulationId, MonteCarloSimulation.SimulationStatus status) {
        MonteCarloSimulation simulation = simulations.get(simulationId);
        if (simulation != null) {
            simulation.setStatus(status);
            simulations.put(simulationId, simulation);
            log.info("시뮬레이션 상태 업데이트: simulationId={}, status={}", simulationId, status);
        }
        return simulation;
    }
    
    @Override
    public List<MonteCarloSimulation> findRunningSimulations() {
        return simulations.values().stream()
            .filter(sim -> sim.getStatus() == MonteCarloSimulation.SimulationStatus.RUNNING)
            .toList();
    }
    
    @Override
    public void cacheSimulationResult(String cacheKey, MonteCarloSimulation result, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(cacheKey, result, ttlSeconds, TimeUnit.SECONDS);
            log.debug("시뮬레이션 결과 캐시 저장: key={}, ttl={}초", cacheKey, ttlSeconds);
        } catch (Exception e) {
            log.error("시뮬레이션 결과 캐시 저장 실패: key={}", cacheKey, e);
        }
    }
    
    @Override
    public Optional<MonteCarloSimulation> getCachedSimulationResult(String cacheKey) {
        try {
            MonteCarloSimulation result = (MonteCarloSimulation) redisTemplate.opsForValue().get(cacheKey);
            if (result != null) {
                log.debug("캐시된 시뮬레이션 결과 조회: key={}", cacheKey);
            }
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("시뮬레이션 결과 캐시 조회 실패: key={}", cacheKey, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void sendRiskAlert(String engineId, String alertMessage, String severity) {
        log.warn("리스크 알림: engineId={}, severity={}, message={}", engineId, severity, alertMessage);
        
        try {
            // 이메일 알림 발송 (실제 구현시 설정된 수신자 목록 사용)
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("admin@stockquest.com"); // 실제 구현시 설정에서 가져옴
            message.setSubject(String.format("[StockQuest] 리스크 알림 - %s", severity));
            message.setText(String.format(
                "리스크 엔진: %s\n심각도: %s\n메시지: %s\n시간: %s", 
                engineId, severity, alertMessage, LocalDateTime.now()
            ));
            
            // 메일 발송은 별도 스레드에서 비동기 처리
            CompletableFuture.runAsync(() -> {
                try {
                    mailSender.send(message);
                    log.info("리스크 알림 이메일 발송 완료: engineId={}", engineId);
                } catch (Exception e) {
                    log.error("리스크 알림 이메일 발송 실패: engineId={}", engineId, e);
                }
            });
            
        } catch (Exception e) {
            log.error("리스크 알림 처리 오류: engineId={}", engineId, e);
        }
    }
    
    /**
     * 기본 리스크 시나리오 생성 (초기화용)
     */
    public void initializeDefaultScenarios() {
        // 보수적 시나리오
        RiskScenario conservativeScenario = RiskScenario.builder()
            .scenarioId("conservative-default")
            .name("보수적 시나리오")
            .description("낮은 변동성과 안정적인 수익률을 가정한 보수적 리스크 시나리오")
            .marketVolatility(new java.math.BigDecimal("0.15"))
            .expectedReturn(new java.math.BigDecimal("0.06"))
            .simulationDays(252)
            .iterations(10000)
            .confidenceLevel(new java.math.BigDecimal("0.95"))
            .isActive(true)
            .build();
            
        // 적극적 시나리오
        RiskScenario aggressiveScenario = RiskScenario.builder()
            .scenarioId("aggressive-default")
            .name("적극적 시나리오")
            .description("높은 변동성과 높은 기대수익률을 가정한 적극적 리스크 시나리오")
            .marketVolatility(new java.math.BigDecimal("0.25"))
            .expectedReturn(new java.math.BigDecimal("0.12"))
            .simulationDays(252)
            .iterations(10000)
            .confidenceLevel(new java.math.BigDecimal("0.95"))
            .isActive(true)
            .build();
            
        // 스트레스 테스트 시나리오
        RiskScenario stressTestScenario = RiskScenario.builder()
            .scenarioId("stress-test-default")
            .name("스트레스 테스트 시나리오")
            .description("극단적 시장 상황을 가정한 스트레스 테스트 시나리오")
            .marketVolatility(new java.math.BigDecimal("0.40"))
            .expectedReturn(new java.math.BigDecimal("-0.05"))
            .simulationDays(252)
            .iterations(50000)
            .confidenceLevel(new java.math.BigDecimal("0.99"))
            .isActive(true)
            .build();
            
        saveRiskScenario(conservativeScenario);
        saveRiskScenario(aggressiveScenario);
        saveRiskScenario(stressTestScenario);
        
        log.info("기본 리스크 시나리오 초기화 완료: 3개 시나리오 생성");
    }
    
    /**
     * 기본 리스크 엔진 생성 (초기화용)
     */
    public void initializeDefaultRiskEngine() {
        RiskEngine defaultEngine = RiskEngine.builder()
            .engineId("main-risk-engine")
            .name("메인 리스크 엔진")
            .version("1.0.0")
            .status(RiskEngine.EngineStatus.IDLE)
            .configuration(RiskEngine.EngineConfiguration.builder()
                .maxConcurrentSimulations(5)
                .defaultIterations(10000)
                .simulationTimeoutMinutes(30)
                .threadPoolSize(Runtime.getRuntime().availableProcessors())
                .build())
            .supportedModels(List.of(
                RiskEngine.RiskModel.MONTE_CARLO_BASIC,
                RiskEngine.RiskModel.MONTE_CARLO_ADVANCED,
                RiskEngine.RiskModel.PARAMETRIC_VAR
            ))
            .metrics(RiskEngine.EngineMetrics.builder()
                .totalSimulationsExecuted(0L)
                .averageExecutionTimeMs(0L)
                .successRate(new java.math.BigDecimal("1.0"))
                .uptimeMinutes(0L)
                .build())
            .build();
            
        createRiskEngine(defaultEngine);
        log.info("기본 리스크 엔진 초기화 완료: engineId={}", defaultEngine.getEngineId());
    }
}