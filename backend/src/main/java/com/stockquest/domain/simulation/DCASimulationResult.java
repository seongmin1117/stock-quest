package com.stockquest.domain.simulation;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * DCA(Dollar Cost Averaging) 시뮬레이션 결과
 * 정액 투자법 시뮬레이션 결과와 성과 분석을 관리
 */
@Getter
public class DCASimulationResult {

    private final DCASimulationParameters parameters;
    private final BigDecimal totalInvestmentAmount;
    private final BigDecimal finalPortfolioValue;
    private final List<MonthlyInvestmentRecord> investmentRecords;
    private final BigDecimal sp500Return;
    private final BigDecimal nasdaqReturn;

    public DCASimulationResult(
            DCASimulationParameters parameters,
            BigDecimal totalInvestmentAmount,
            BigDecimal finalPortfolioValue,
            List<MonthlyInvestmentRecord> investmentRecords,
            BigDecimal sp500Return,
            BigDecimal nasdaqReturn) {

        validateInputs(totalInvestmentAmount, finalPortfolioValue, investmentRecords);

        this.parameters = parameters;
        this.totalInvestmentAmount = totalInvestmentAmount;
        this.finalPortfolioValue = finalPortfolioValue;
        this.investmentRecords = investmentRecords;
        this.sp500Return = sp500Return;
        this.nasdaqReturn = nasdaqReturn;
    }

    private void validateInputs(
            BigDecimal totalInvestment,
            BigDecimal finalValue,
            List<MonthlyInvestmentRecord> records) {

        if (totalInvestment == null) {
            throw new IllegalArgumentException("투자 금액은 필수입니다");
        }

        if (finalValue == null || finalValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("포트폴리오 가치는 0 이상이어야 합니다");
        }

        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("투자 기록은 필수입니다");
        }
    }

    /**
     * 총 수익률 계산 (백분율)
     * 계산식: ((최종가치 - 투자원금) / 투자원금) * 100
     */
    public BigDecimal getTotalReturnPercentage() {
        BigDecimal profit = finalPortfolioValue.subtract(totalInvestmentAmount);
        return profit.divide(totalInvestmentAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 연평균 수익률 계산 (CAGR - Compound Annual Growth Rate)
     * 계산식: ((최종가치/투자원금)^(1/투자년수) - 1) * 100
     */
    public BigDecimal getAnnualizedReturn() {
        double finalValue = finalPortfolioValue.doubleValue();
        double initialValue = totalInvestmentAmount.doubleValue();
        double years = parameters.getInvestmentPeriodInYears();

        double cagr = Math.pow(finalValue / initialValue, 1.0 / years) - 1;
        return new BigDecimal(cagr * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * S&P 500 대비 초과 성과 계산
     */
    public BigDecimal getOutperformanceVsSP500() {
        BigDecimal sp500ReturnRate = calculateReturnRate(sp500Return);
        BigDecimal portfolioReturnRate = getTotalReturnPercentage();
        return portfolioReturnRate.subtract(sp500ReturnRate);
    }

    /**
     * NASDAQ 대비 초과 성과 계산
     */
    public BigDecimal getOutperformanceVsNASDAQ() {
        BigDecimal nasdaqReturnRate = calculateReturnRate(nasdaqReturn);
        BigDecimal portfolioReturnRate = getTotalReturnPercentage();
        return portfolioReturnRate.subtract(nasdaqReturnRate);
    }

    /**
     * 최고 포트폴리오 가치 계산
     */
    public BigDecimal getMaxPortfolioValue() {
        return investmentRecords.stream()
                .map(MonthlyInvestmentRecord::getPortfolioValue)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 최대 손실률 계산 (Maximum Drawdown)
     * 계산식: (최저값 - 최고값) / 최고값 * 100
     */
    public BigDecimal getMaxDrawdown() {
        BigDecimal maxValue = getMaxPortfolioValue();
        BigDecimal minValue = investmentRecords.stream()
                .map(MonthlyInvestmentRecord::getPortfolioValue)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (maxValue.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return minValue.subtract(maxValue)
                      .divide(maxValue, 2, RoundingMode.HALF_UP)
                      .multiply(new BigDecimal("100"));
    }

    /**
     * 시뮬레이션 결과 요약 정보
     */
    public String getSummary() {
        DecimalFormat formatter = new DecimalFormat("#,##0");

        return String.format(
            "=== DCA 시뮬레이션 결과 요약 ===\n" +
            "종목: %s\n" +
            "투자 원금: %s원\n" +
            "최종 포트폴리오 가치: %s원\n" +
            "총 수익률: %s%%\n" +
            "연평균 수익률: %s%%\n" +
            "S&P 500 대비: %s%%\n" +
            "NASDAQ 대비: %s%%\n" +
            "최대 손실률: %s%%",
            parameters.getSymbol(),
            formatter.format(totalInvestmentAmount),
            formatter.format(finalPortfolioValue),
            getTotalReturnPercentage(),
            getAnnualizedReturn(),
            getOutperformanceVsSP500(),
            getOutperformanceVsNASDAQ(),
            getMaxDrawdown()
        );
    }

    private BigDecimal calculateReturnRate(BigDecimal benchmarkValue) {
        BigDecimal profit = benchmarkValue.subtract(totalInvestmentAmount);
        return profit.divide(totalInvestmentAmount, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
    }
}