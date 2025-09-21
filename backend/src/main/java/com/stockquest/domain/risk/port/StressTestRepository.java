package com.stockquest.domain.risk.port;

import com.stockquest.domain.risk.StressTestResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 스트레스 테스트 결과 저장소 포트 인터페이스
 */
public interface StressTestRepository {

    /**
     * 스트레스 테스트 결과 저장
     */
    StressTestResult save(StressTestResult result);

    /**
     * 테스트 ID로 결과 조회
     */
    Optional<StressTestResult> findById(String testId);

    /**
     * 포트폴리오별 스트레스 테스트 결과 조회
     */
    List<StressTestResult> findByPortfolioId(Long portfolioId);

    /**
     * 시나리오별 스트레스 테스트 결과 조회
     */
    List<StressTestResult> findByScenarioId(String scenarioId);

    /**
     * 포트폴리오의 최신 스트레스 테스트 결과 조회
     */
    Optional<StressTestResult> findLatestByPortfolioId(Long portfolioId);

    /**
     * 특정 기간 내 스트레스 테스트 결과 조회
     */
    List<StressTestResult> findByExecutedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 포트폴리오별 특정 기간 내 결과 조회
     */
    List<StressTestResult> findByPortfolioIdAndExecutedAtBetween(
            Long portfolioId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 리스크 점수 범위로 조회
     */
    List<StressTestResult> findByOverallRiskScoreBetween(Integer minScore, Integer maxScore);

    /**
     * VaR 임계값 초과 결과 조회
     */
    List<StressTestResult> findByValueAtRisk99GreaterThan(java.math.BigDecimal threshold);

    /**
     * 모든 스트레스 테스트 결과 조회
     */
    List<StressTestResult> findAll();

    /**
     * 스트레스 테스트 결과 삭제
     */
    void deleteById(String testId);

    /**
     * 포트폴리오별 결과 삭제
     */
    void deleteByPortfolioId(Long portfolioId);

    /**
     * 오래된 테스트 결과 정리
     */
    int deleteOldResults(LocalDateTime cutoffTime);

    /**
     * 스트레스 테스트 결과 존재 여부 확인
     */
    boolean existsById(String testId);

    /**
     * 포트폴리오별 결과 카운트
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * 전체 결과 카운트
     */
    long count();
}