package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradingStrategyResponse {
    private String strategy;
    private String recommendation;
    private double confidence;
}