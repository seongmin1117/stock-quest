package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 상태 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatus {
    
    private String symbol;
    private Boolean isActive;
    private Integer subscriberCount;
    private LocalDateTime lastUpdated;
    private String dataType; // QUOTES, TECHNICAL_INDICATORS, MARKET_DEPTH
    private Integer updateFrequencyMs;
}