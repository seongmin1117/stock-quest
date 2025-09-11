package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 내역 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    
    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private Long quantity;
    private BigDecimal amount;
    private String side; // BUY, SELL
    private LocalDateTime timestamp;
    private String exchange;
}