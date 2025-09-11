package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BenchmarkData {
    private String symbol;
    private String name;
    private List<PricePoint> prices;
    private double totalReturn;
    private double annualizedReturn;
    private double volatility;
    
    @Data
    @Builder
    public static class PricePoint {
        private LocalDate date;
        private double price;
        private double return_;
    }
}