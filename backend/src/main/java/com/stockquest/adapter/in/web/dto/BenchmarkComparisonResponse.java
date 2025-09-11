package com.stockquest.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 벤치마크 비교 분석 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkComparisonResponse {
    
    /**
     * 분석 기준 시간
     */
    private LocalDateTime analysisTime;
    
    /**
     * 벤치마크 심볼 (예: SPY, QQQ)
     */
    private String benchmarkSymbol;
    
    /**
     * 벤치마크 이름
     */
    private String benchmarkName;
    
    /**
     * 분석 기간
     */
    private String timeframe;
    
    /**
     * 포트폴리오 총 수익률
     */
    private Double portfolioReturn;
    
    /**
     * 벤치마크 수익률
     */
    private Double benchmarkReturn;
    
    /**
     * 초과 수익률 (Alpha)
     */
    private Double excessReturn;
    
    /**
     * 베타 (시장 민감도)
     */
    private Double beta;
    
    /**
     * 추적 오차 (Tracking Error)
     */
    private Double trackingError;
    
    /**
     * 정보 비율 (Information Ratio)
     */
    private Double informationRatio;
    
    /**
     * 상관관계 (Correlation)
     */
    private Double correlation;
    
    /**
     * 변동성 비교
     */
    private VolatilityComparison volatilityComparison;
    
    /**
     * 기간별 성과 비교
     */
    private List<PeriodPerformance> periodPerformances;
    
    /**
     * 성과 분석 요약
     */
    private String performanceSummary;
    
    /**
     * 개선 권고사항
     */
    private String recommendations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolatilityComparison {
        
        /**
         * 포트폴리오 변동성
         */
        private Double portfolioVolatility;
        
        /**
         * 벤치마크 변동성
         */
        private Double benchmarkVolatility;
        
        /**
         * 변동성 차이 (상대적)
         */
        private Double volatilityDifference;
        
        /**
         * 리스크 조정 수익률
         */
        private Double riskAdjustedReturn;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodPerformance {
        
        /**
         * 기간 (1D, 1W, 1M, 3M, 6M, 1Y)
         */
        private String period;
        
        /**
         * 포트폴리오 수익률
         */
        private Double portfolioReturn;
        
        /**
         * 벤치마크 수익률
         */
        private Double benchmarkReturn;
        
        /**
         * 초과 수익률
         */
        private Double excessReturn;
        
        /**
         * 승률 (Win Rate)
         */
        private Double winRate;
    }
}