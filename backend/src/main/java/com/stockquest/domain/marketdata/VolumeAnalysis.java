package com.stockquest.domain.marketdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 거래량 분석 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeAnalysis {
    
    private String symbol;
    private Integer windowMinutes;
    private Long totalVolume;
    private Long averageVolume;
    private BigDecimal volumeWeightedAveragePrice;
    private Double volumeRatio; // Current vs Average
    private Map<String, Long> volumeByPriceLevel;
    private LocalDateTime analysisTime;
}