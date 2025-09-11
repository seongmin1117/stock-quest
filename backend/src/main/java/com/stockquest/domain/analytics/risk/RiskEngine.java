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
 * 리스크 엔진 도메인 모델 - Monte Carlo 시뮬레이션 및 리스크 분석 조정
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEngine {
    
    /**
     * 엔진 ID
     */
    private String engineId;
    
    /**
     * 엔진 이름
     */
    private String name;
    
    /**
     * 엔진 버전
     */
    private String version;
    
    /**
     * 엔진 상태
     */
    private EngineStatus status;
    
    /**
     * 엔진 설정
     */
    private EngineConfiguration configuration;
    
    /**
     * 지원하는 리스크 모델 목록
     */
    private List<RiskModel> supportedModels;
    
    /**
     * 실행 중인 시뮬레이션 목록
     */
    private List<String> runningSimulations;
    
    /**
     * 엔진 성능 메트릭
     */
    private EngineMetrics metrics;
    
    /**
     * 마지막 업데이트 시점
     */
    private LocalDateTime lastUpdate;
    
    /**
     * 엔진 생성 시점
     */
    private LocalDateTime createdAt;
    
    public enum EngineStatus {
        IDLE,
        RUNNING,
        BUSY,
        MAINTENANCE,
        ERROR,
        SHUTDOWN
    }
    
    public enum RiskModel {
        MONTE_CARLO_BASIC,
        MONTE_CARLO_ADVANCED,
        HISTORICAL_SIMULATION,
        PARAMETRIC_VAR,
        EXTREME_VALUE_THEORY,
        COPULA_BASED,
        MACHINE_LEARNING_VAR
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngineConfiguration {
        
        /**
         * 최대 동시 실행 시뮬레이션 수
         */
        @Builder.Default
        private Integer maxConcurrentSimulations = 5;
        
        /**
         * 기본 시뮬레이션 반복 횟수
         */
        @Builder.Default
        private Integer defaultIterations = 10000;
        
        /**
         * 기본 신뢰구간 수준
         */
        @Builder.Default
        private BigDecimal defaultConfidenceLevel = new BigDecimal("0.95");
        
        /**
         * 시뮬레이션 타임아웃 (분)
         */
        @Builder.Default
        private Integer simulationTimeoutMinutes = 30;
        
        /**
         * 결과 캐시 TTL (시간)
         */
        @Builder.Default
        private Integer resultCacheTtlHours = 24;
        
        /**
         * 병렬 처리 스레드 풀 크기
         */
        @Builder.Default
        private Integer threadPoolSize = 8;
        
        /**
         * 메모리 제한 (MB)
         */
        @Builder.Default
        private Long memoryLimitMb = 2048L;
        
        /**
         * 자동 GC 임계값 (%)
         */
        @Builder.Default
        private Integer autoGcThreshold = 80;
        
        /**
         * 결과 저장 정밀도 (소수점 자릿수)
         */
        @Builder.Default
        private Integer resultPrecision = 6;
        
        /**
         * 알림 설정
         */
        private AlertConfiguration alerts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertConfiguration {
        
        /**
         * 극한 VaR 임계값 (%)
         */
        @Builder.Default
        private BigDecimal extremeVarThreshold = new BigDecimal("20.0");
        
        /**
         * 집중도 리스크 임계값 (%)
         */
        @Builder.Default
        private BigDecimal concentrationThreshold = new BigDecimal("30.0");
        
        /**
         * 상관관계 리스크 임계값
         */
        @Builder.Default
        private BigDecimal correlationThreshold = new BigDecimal("0.8");
        
        /**
         * 이메일 알림 활성화
         */
        @Builder.Default
        private Boolean emailNotificationEnabled = true;
        
        /**
         * 실시간 알림 활성화
         */
        @Builder.Default
        private Boolean realtimeNotificationEnabled = true;
        
        /**
         * 알림 이메일 주소 목록
         */
        private List<String> notificationEmails;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngineMetrics {
        
        /**
         * 총 실행된 시뮬레이션 수
         */
        private Long totalSimulationsExecuted;
        
        /**
         * 평균 실행 시간 (밀리초)
         */
        private Long averageExecutionTimeMs;
        
        /**
         * 성공률 (%)
         */
        private BigDecimal successRate;
        
        /**
         * 현재 메모리 사용량 (MB)
         */
        private Long currentMemoryUsageMb;
        
        /**
         * 최대 메모리 사용량 (MB)
         */
        private Long peakMemoryUsageMb;
        
        /**
         * CPU 사용률 (%)
         */
        private BigDecimal cpuUsagePercent;
        
        /**
         * 처리량 (시뮬레이션/시간)
         */
        private BigDecimal throughputPerHour;
        
        /**
         * 에러 카운트
         */
        private Map<String, Long> errorCounts;
        
        /**
         * 마지막 성능 측정 시점
         */
        private LocalDateTime lastMeasurementTime;
        
        /**
         * 가동 시간 (분)
         */
        private Long uptimeMinutes;
    }
}