package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 기술적 지표 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicators {
    
    private String symbol;
    private String timeframe;
    
    // 기본 가격 정보
    private BigDecimal price;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private Long volume;
    
    // 기술적 지표
    private Double rsi;
    private Double macd;
    private Double macdSignal;
    private Double macdHistogram;
    
    // 이동평균
    private BigDecimal sma20;
    private BigDecimal sma50;
    private BigDecimal sma200;
    private BigDecimal ema12;
    private BigDecimal ema26;
    
    // 볼린저 밴드
    private BigDecimal bollingerUpper;
    private BigDecimal bollingerMiddle;
    private BigDecimal bollingerLower;
    
    // 기타 지표
    private Double stochasticK;
    private Double stochasticD;
    private Double williams;
    private Double atr; // Average True Range
    
    private LocalDateTime timestamp;
    private Map<String, Object> additionalIndicators;
}