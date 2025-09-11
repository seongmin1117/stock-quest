package com.stockquest.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 성과 이력 분석 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceHistoryResponse {
    
    /**
     * 분석 기준 시간
     */
    private LocalDateTime analysisTime;
    
    /**
     * 분석 기간
     */
    private String timeframe;
    
    /**
     * 데이터 간격 (daily, weekly, monthly)
     */
    private String interval;
    
    /**
     * 시작 포트폴리오 가치
     */
    private BigDecimal startValue;
    
    /**
     * 현재 포트폴리오 가치
     */
    private BigDecimal currentValue;
    
    /**
     * 총 수익률
     */
    private Double totalReturn;
    
    /**
     * 연환산 수익률
     */
    private Double annualizedReturn;
    
    /**
     * 기간별 성과 데이터
     */
    private List<PerformanceDataPoint> performanceData;
    
    /**
     * 최고 수익률
     */
    private Double maxReturn;
    
    /**
     * 최저 수익률
     */
    private Double minReturn;
    
    /**
     * 평균 수익률
     */
    private Double averageReturn;
    
    /**
     * 변동성 (표준편차)
     */
    private Double volatility;
    
    /**
     * 최대 연속 상승 기간
     */
    private Integer maxWinningStreak;
    
    /**
     * 최대 연속 하락 기간
     */
    private Integer maxLosingStreak;
    
    /**
     * 승률 (수익 발생 비율)
     */
    private Double winRate;
    
    /**
     * 평균 수익폭
     */
    private Double averageWin;
    
    /**
     * 평균 손실폭
     */
    private Double averageLoss;
    
    /**
     * 수익/손실 비율
     */
    private Double profitLossRatio;
    
    /**
     * 성과 요약
     */
    private String performanceSummary;
    
    /**
     * 성과 등급 (A+, A, B+, B, C+, C, D)
     */
    private String performanceGrade;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceDataPoint {
        
        /**
         * 날짜
         */
        private LocalDateTime date;
        
        /**
         * 포트폴리오 가치
         */
        private BigDecimal portfolioValue;
        
        /**
         * 일간/기간 수익률
         */
        private Double periodReturn;
        
        /**
         * 누적 수익률
         */
        private Double cumulativeReturn;
        
        /**
         * 일간/기간 거래량
         */
        private Long volume;
        
        /**
         * 주요 이벤트 (선택적)
         */
        private String event;
    }
}