package com.stockquest.domain.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 시장 데이터 도메인 엔티티
 */
@Getter
@Builder
@AllArgsConstructor
public class MarketData {
    private Long id;
    private String symbol;
    private String companyName;
    private BigDecimal price;
    private LocalDateTime dataTime;
    private Long volume;
}