package com.stockquest.domain.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 기술적 지표 데이터 클래스 (임시 구현)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicators {
    
    private BigDecimal rsi;
    private BigDecimal macd;
    private BigDecimal macdSignal;
    private BigDecimal macdHistogram;
    private BigDecimal ema12;
    private BigDecimal ema26;
    private BigDecimal sma20;
    private BigDecimal bollingerUpper;
    private BigDecimal bollingerLower;
    private BigDecimal bollingerMiddle;
    private BigDecimal stochasticK;
    private BigDecimal stochasticD;
    private BigDecimal atr;
    private BigDecimal adx;
    private BigDecimal momentum;
    private BigDecimal williamsR;
    private BigDecimal roc;
    private BigDecimal cci;
    
    /**
     * MACD Line getter method - returns the main MACD value
     */
    public BigDecimal getMacdLine() {
        return macd;
    }
}