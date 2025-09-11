package com.stockquest.domain.analytics.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Monte Carlo 시뮬레이션 실행 결과 도메인 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonteCarloSimulation {
    
    /**
     * 시뮬레이션 ID
     */
    private String simulationId;
    
    /**
     * 포트폴리오 ID
     */
    private Long portfolioId;
    
    /**
     * 사용된 시나리오 ID
     */
    private String scenarioId;
    
    /**
     * 시뮬레이션 시작 시점
     */
    private LocalDateTime startTime;
    
    /**
     * 시뮬레이션 완료 시점
     */
    private LocalDateTime endTime;
    
    /**
     * 총 실행 시간 (밀리초)
     */
    private Long executionTimeMs;
    
    /**
     * 시뮬레이션 상태
     */
    private SimulationStatus status;
    
    /**
     * 시뮬레이션 결과 통계
     */
    private SimulationStatistics statistics;
    
    /**
     * 시뮬레이션 경로별 결과 (샘플링된 경로들)
     */
    private List<SimulationPath> samplePaths;
    
    /**
     * 리스크 메트릭 결과
     */
    private Map<String, BigDecimal> riskMetrics;
    
    /**
     * 오류 메시지 (실패시)
     */
    private String errorMessage;
    
    /**
     * 시뮬레이션 설정 파라미터
     */
    private SimulationParameters parameters;
    
    public enum SimulationStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationStatistics {
        
        /**
         * 평균 수익률
         */
        private BigDecimal meanReturn;
        
        /**
         * 표준편차
         */
        private BigDecimal standardDeviation;
        
        /**
         * 최대 수익률
         */
        private BigDecimal maxReturn;
        
        /**
         * 최소 수익률
         */
        private BigDecimal minReturn;
        
        /**
         * 중간값
         */
        private BigDecimal median;
        
        /**
         * VaR (Value at Risk) - 95% 신뢰구간
         */
        private BigDecimal var95;
        
        /**
         * VaR (Value at Risk) - 99% 신뢰구간
         */
        private BigDecimal var99;
        
        /**
         * CVaR (Conditional Value at Risk) - 95%
         */
        private BigDecimal cvar95;
        
        /**
         * CVaR (Conditional Value at Risk) - 99%
         */
        private BigDecimal cvar99;
        
        /**
         * 샤프 비율
         */
        private BigDecimal sharpeRatio;
        
        /**
         * 최대 낙폭 (Maximum Drawdown)
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 손실 확률 (Probability of Loss)
         */
        private BigDecimal probabilityOfLoss;
        
        /**
         * 백분위수 분포 (5%, 25%, 75%, 95%)
         */
        private Map<Integer, BigDecimal> percentiles;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationPath {
        
        /**
         * 경로 ID
         */
        private String pathId;
        
        /**
         * 시간별 포트폴리오 가치 변화
         */
        private List<PathPoint> points;
        
        /**
         * 최종 수익률
         */
        private BigDecimal finalReturn;
        
        /**
         * 경로별 최대 낙폭
         */
        private BigDecimal maxDrawdown;
        
        /**
         * 경로별 변동성
         */
        private BigDecimal volatility;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathPoint {
        
        /**
         * 시점 (일차)
         */
        private Integer day;
        
        /**
         * 포트폴리오 가치
         */
        private BigDecimal value;
        
        /**
         * 누적 수익률
         */
        private BigDecimal cumulativeReturn;
        
        /**
         * 일일 수익률
         */
        private BigDecimal dailyReturn;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationParameters {
        
        /**
         * 시뮬레이션 반복 횟수
         */
        private Integer iterations;
        
        /**
         * 시뮬레이션 기간 (일)
         */
        private Integer days;
        
        /**
         * 초기 포트폴리오 가치
         */
        private BigDecimal initialValue;
        
        /**
         * 무위험 수익률 (연간)
         */
        private BigDecimal riskFreeRate;
        
        /**
         * 리밸런싱 주기 (일)
         */
        private Integer rebalancingFrequency;
        
        /**
         * 난수 시드값
         */
        private Long randomSeed;
        
        /**
         * 병렬 처리 스레드 수
         */
        private Integer parallelThreads;
        
        /**
         * 결과 저장할 경로 개수 (샘플링)
         */
        private Integer samplePathCount;
    }
}