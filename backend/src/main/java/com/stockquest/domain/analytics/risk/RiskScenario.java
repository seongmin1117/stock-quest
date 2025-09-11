package com.stockquest.domain.analytics.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Monte Carlo 시뮬레이션 리스크 시나리오 도메인 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScenario {
    
    /**
     * 시나리오 ID
     */
    private String scenarioId;
    
    /**
     * 시나리오 이름
     */
    private String name;
    
    /**
     * 시나리오 설명
     */
    private String description;
    
    /**
     * 시장 변동성 (연간 기준)
     */
    private BigDecimal marketVolatility;
    
    /**
     * 예상 수익률 (연간 기준)
     */
    private BigDecimal expectedReturn;
    
    /**
     * 상관관계 매트릭스
     */
    private Map<String, Map<String, BigDecimal>> correlationMatrix;
    
    /**
     * 시뮬레이션 기간 (일)
     */
    private Integer simulationDays;
    
    /**
     * 시뮬레이션 반복 횟수
     */
    private Integer iterations;
    
    /**
     * 신뢰구간 수준 (예: 0.95 = 95%)
     */
    private BigDecimal confidenceLevel;
    
    /**
     * 시나리오 생성 시점
     */
    private LocalDateTime createdAt;
    
    /**
     * 시나리오 업데이트 시점
     */
    private LocalDateTime updatedAt;
    
    /**
     * 시나리오 활성화 여부
     */
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * 리스크 팩터 가중치
     */
    private Map<RiskFactor, BigDecimal> riskFactorWeights;
    
    public enum RiskFactor {
        MARKET_VOLATILITY,
        INTEREST_RATE,
        CURRENCY_RISK,
        SECTOR_CONCENTRATION,
        LIQUIDITY_RISK,
        CORRELATION_RISK,
        TAIL_RISK
    }
}