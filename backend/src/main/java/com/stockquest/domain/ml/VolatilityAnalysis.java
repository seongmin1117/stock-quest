package com.stockquest.domain.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 변동성 분석 데이터 클래스 (임시 구현)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolatilityAnalysis {
    
    /**
     * 히스토리컬 변동성
     */
    private double historicalVolatility;
    
    /**
     * 실현 변동성
     */
    private BigDecimal realizedVolatility;
    
    /**
     * 변동성 클러스터링 지표
     */
    private BigDecimal volatilityClustering;
    
    /**
     * GARCH 모델 변동성
     */
    private BigDecimal garchVolatility;
    
    /**
     * 파킨슨 변동성
     */
    private BigDecimal parkinsonVolatility;
    
    /**
     * 가만-클래스 변동성
     */
    private BigDecimal garmanKlassVolatility;
    
    /**
     * 로저스-사칠 변동성
     */
    private BigDecimal rogersSatchellVolatility;
    
    /**
     * 변동성 체제 (높음/보통/낮음)
     */
    private VolatilityRegime volatilityRegime;
    
    /**
     * 변동성 추세 (상승/하락/안정)
     */
    private VolatilityTrend volatilityTrend;
    
    public enum VolatilityRegime {
        HIGH, MODERATE, LOW
    }
    
    public enum VolatilityTrend {
        INCREASING, DECREASING, STABLE
    }
}