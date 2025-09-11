package com.stockquest.application.analysis;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class StockAnalysisResult {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double targetPrice;
    private int rating; // 1-5 scale
    private String recommendedAction; // BUY, SELL, HOLD
    private String riskLevel; // LOW, MEDIUM, HIGH
    private double confidence;
    private LocalDateTime analysisTimestamp;
    private Map<String, Object> technicalIndicators;
    private Map<String, Object> fundamentalMetrics;
    private String analysisReason;
}