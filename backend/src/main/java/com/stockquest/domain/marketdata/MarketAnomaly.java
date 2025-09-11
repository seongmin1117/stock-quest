package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 시장 이상 징후 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnomaly {
    
    private String anomalyId;
    private String symbol;
    private String anomalyType; // VOLUME_SPIKE, PRICE_GAP, UNUSUAL_ACTIVITY
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private BigDecimal currentValue;
    private BigDecimal expectedValue;
    private BigDecimal deviationPercent;
    private LocalDateTime detectedAt;
    private LocalDateTime expiry;
}