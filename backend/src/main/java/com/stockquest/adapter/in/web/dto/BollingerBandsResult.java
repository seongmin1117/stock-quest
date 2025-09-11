package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BollingerBandsResult {
    private double upperBand;
    private double middleBand;
    private double lowerBand;
    private double currentPrice;
    private boolean oversold;
    private boolean overbought;
    private double bandWidth;
    private double percentB;
}