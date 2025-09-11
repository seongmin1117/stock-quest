package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskContributionResponse {
    private String symbol;
    private String name;
    private double weight;
    private double riskContribution;
    private double marginalRiskContribution;
    private double componentVaR;
}