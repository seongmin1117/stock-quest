package com.stockquest.domain.simulation;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 월별 투자 기록
 * DCA 시뮬레이션의 각 투자 시점별 상세 정보를 관리
 */
@Getter
public class MonthlyInvestmentRecord {

    private final LocalDateTime investmentDate;
    private final BigDecimal investmentAmount;
    private final BigDecimal stockPrice;
    private final BigDecimal sharesPurchased;
    private final BigDecimal portfolioValue;

    public MonthlyInvestmentRecord(
            LocalDateTime investmentDate,
            BigDecimal investmentAmount,
            BigDecimal stockPrice,
            BigDecimal sharesPurchased,
            BigDecimal portfolioValue) {

        validateInputs(investmentDate, investmentAmount, stockPrice, sharesPurchased, portfolioValue);

        this.investmentDate = investmentDate;
        this.investmentAmount = investmentAmount;
        this.stockPrice = stockPrice;
        this.sharesPurchased = sharesPurchased;
        this.portfolioValue = portfolioValue;
    }

    private void validateInputs(
            LocalDateTime date,
            BigDecimal amount,
            BigDecimal price,
            BigDecimal shares,
            BigDecimal value) {

        if (date == null) {
            throw new IllegalArgumentException("투자 날짜는 필수입니다");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("투자 금액은 0보다 커야 합니다");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("주식 가격은 0보다 커야 합니다");
        }

        if (shares == null || shares.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("매수 주식 수는 0 이상이어야 합니다");
        }

        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("포트폴리오 가치는 0 이상이어야 합니다");
        }
    }

    @Override
    public String toString() {
        return String.format(
            "투자일: %s, 투자금액: %s원, 주가: %s원, 매수주식: %s주, 포트폴리오: %s원",
            investmentDate.toLocalDate(),
            investmentAmount,
            stockPrice,
            sharesPurchased,
            portfolioValue
        );
    }
}