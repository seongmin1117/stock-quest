package com.stockquest.domain.risk.port;

import com.stockquest.domain.risk.RiskScenario;
import com.stockquest.domain.risk.ScenarioType;
import com.stockquest.domain.risk.ScenarioSeverity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 리스크 시나리오 저장소 포트 인터페이스
 */
public interface RiskScenarioRepository {

    /**
     * 시나리오 저장
     */
    RiskScenario save(RiskScenario scenario);

    /**
     * ID로 시나리오 조회
     */
    Optional<RiskScenario> findById(String scenarioId);

    /**
     * 시나리오 이름으로 조회
     */
    Optional<RiskScenario> findByName(String name);

    /**
     * 유형별 시나리오 조회
     */
    List<RiskScenario> findByType(ScenarioType type);

    /**
     * 심각도별 시나리오 조회
     */
    List<RiskScenario> findBySeverity(ScenarioSeverity severity);

    /**
     * 활성 시나리오 조회 (유효기간 내)
     */
    List<RiskScenario> findActiveScenarios(LocalDateTime currentTime);

    /**
     * 발생 확률 범위로 조회
     */
    List<RiskScenario> findByProbabilityRange(Double minProbability, Double maxProbability);

    /**
     * 모든 시나리오 조회
     */
    List<RiskScenario> findAll();

    /**
     * 시나리오 삭제
     */
    void deleteById(String scenarioId);

    /**
     * 만료된 시나리오 정리
     */
    int deleteExpiredScenarios(LocalDateTime currentTime);

    /**
     * 시나리오 존재 여부 확인
     */
    boolean existsById(String scenarioId);

    /**
     * 시나리오 카운트 조회
     */
    long count();

    /**
     * 유형별 시나리오 카운트
     */
    long countByType(ScenarioType type);
}