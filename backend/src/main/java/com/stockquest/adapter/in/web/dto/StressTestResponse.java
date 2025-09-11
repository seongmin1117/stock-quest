package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class StressTestResponse {
    private String scenario;
    private double portfolioReturn;
    private double portfolioValue;
    private double maxDrawdown;
    private Map<String, Double> positionImpacts;
}