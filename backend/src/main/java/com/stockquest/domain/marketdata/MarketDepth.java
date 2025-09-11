package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시장 깊이 (호가창) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDepth {
    
    private String symbol;
    private List<PriceLevel> bids;
    private List<PriceLevel> asks;
    private BigDecimal bidAskSpread;
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceLevel {
        private BigDecimal price;
        private Long quantity;
        private Integer orders;
    }
}