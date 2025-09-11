package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 시장 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketStatistics {
    
    private Integer activeSymbols;
    private BigDecimal totalMarketCap;
    private Double averageVolatility;
    private Double marketSentiment; // 0.0-1.0, 0.5 = neutral
    private List<String> topGainers;
    private List<String> topLosers;
    private List<String> highestVolume;
    private Map<String, Map<String, Double>> realtimeCorrelations;
    private Double volatilityIndex; // VIX-like indicator
    private LocalDateTime timestamp;
}