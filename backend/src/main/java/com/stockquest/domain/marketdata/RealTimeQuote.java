package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 실시간 주식 시세 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeQuote {
    
    private String symbol;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Long volume;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private BigDecimal previousClose;
    private BigDecimal marketCap;
    private LocalDateTime timestamp;
    private String exchange;
    private String currency;
}