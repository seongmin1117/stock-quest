package com.stockquest.domain.simulation;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * DCA(Dollar Cost Averaging) 시뮬레이션 파라미터
 * 정액 투자법 시뮬레이션을 위한 입력 파라미터를 관리
 */
@Getter
public class DCASimulationParameters {

    private final String symbol;
    private final BigDecimal monthlyInvestmentAmount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final InvestmentFrequency frequency;

    public DCASimulationParameters(
            String symbol,
            BigDecimal monthlyInvestmentAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            InvestmentFrequency frequency) {

        validateInputs(symbol, monthlyInvestmentAmount, startDate, endDate, frequency);

        this.symbol = symbol;
        this.monthlyInvestmentAmount = monthlyInvestmentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
    }

    private void validateInputs(
            String symbol,
            BigDecimal amount,
            LocalDateTime start,
            LocalDateTime end,
            InvestmentFrequency freq) {

        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("심볼은 필수입니다");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("투자 금액은 0보다 커야 합니다");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("시작일은 종료일보다 빨라야 합니다");
        }

        if (freq == null) {
            throw new IllegalArgumentException("투자 주기는 필수입니다");
        }
    }

    /**
     * 투자 기간을 연 단위로 계산 (소수점 포함)
     */
    public double getInvestmentPeriodInYears() {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        return totalDays / 365.25; // 윤년 고려
    }

    /**
     * 총 투자 횟수 계산
     * 투자 주기에 따라 총 몇 번 투자하게 되는지 계산
     */
    public int getTotalInvestmentCount() {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) (totalDays / frequency.getDaysPerPeriod());
    }

    /**
     * 총 투자 원금 계산
     */
    public BigDecimal getTotalPrincipal() {
        int investmentCount = getTotalInvestmentCount();
        return monthlyInvestmentAmount.multiply(BigDecimal.valueOf(investmentCount));
    }

    @Override
    public String toString() {
        return String.format(
            "DCA 시뮬레이션 - 종목: %s, 투자금액: %s원, 주기: %s, 기간: %.2f년 (%s ~ %s)",
            symbol,
            monthlyInvestmentAmount,
            frequency.getDescription(),
            getInvestmentPeriodInYears(),
            startDate.toLocalDate(),
            endDate.toLocalDate()
        );
    }
}