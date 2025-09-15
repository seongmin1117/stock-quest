package com.stockquest.application.dca.dto;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DCA 시뮬레이션 응답 DTO
 * 도메인 결과를 웹 레이어로 전달하기 위한 DTO
 */
@Getter
public class DCASimulationResponse {

    private final String symbol;
    private final BigDecimal totalInvestmentAmount;
    private final BigDecimal finalPortfolioValue;
    private final BigDecimal totalReturnPercentage;
    private final BigDecimal annualizedReturn;
    private final List<MonthlyInvestmentRecordDto> investmentRecords;
    private final BigDecimal sp500ReturnAmount;
    private final BigDecimal nasdaqReturnAmount;
    private final BigDecimal outperformanceVsSP500;
    private final BigDecimal outperformanceVsNASDAQ;
    private final BigDecimal maxPortfolioValue;

    public DCASimulationResponse(String symbol,
                                BigDecimal totalInvestmentAmount,
                                BigDecimal finalPortfolioValue,
                                BigDecimal totalReturnPercentage,
                                BigDecimal annualizedReturn,
                                List<MonthlyInvestmentRecordDto> investmentRecords,
                                BigDecimal sp500ReturnAmount,
                                BigDecimal nasdaqReturnAmount,
                                BigDecimal outperformanceVsSP500,
                                BigDecimal outperformanceVsNASDAQ,
                                BigDecimal maxPortfolioValue) {
        this.symbol = symbol;
        this.totalInvestmentAmount = totalInvestmentAmount;
        this.finalPortfolioValue = finalPortfolioValue;
        this.totalReturnPercentage = totalReturnPercentage;
        this.annualizedReturn = annualizedReturn;
        this.investmentRecords = investmentRecords;
        this.sp500ReturnAmount = sp500ReturnAmount;
        this.nasdaqReturnAmount = nasdaqReturnAmount;
        this.outperformanceVsSP500 = outperformanceVsSP500;
        this.outperformanceVsNASDAQ = outperformanceVsNASDAQ;
        this.maxPortfolioValue = maxPortfolioValue;
    }

    /**
     * 월별 투자 기록 DTO
     */
    @Getter
    public static class MonthlyInvestmentRecordDto {
        private final LocalDateTime investmentDate;
        private final BigDecimal investmentAmount;
        private final BigDecimal stockPrice;
        private final BigDecimal sharesPurchased;
        private final BigDecimal portfolioValue;

        public MonthlyInvestmentRecordDto(LocalDateTime investmentDate,
                                         BigDecimal investmentAmount,
                                         BigDecimal stockPrice,
                                         BigDecimal sharesPurchased,
                                         BigDecimal portfolioValue) {
            this.investmentDate = investmentDate;
            this.investmentAmount = investmentAmount;
            this.stockPrice = stockPrice;
            this.sharesPurchased = sharesPurchased;
            this.portfolioValue = portfolioValue;
        }
    }
}