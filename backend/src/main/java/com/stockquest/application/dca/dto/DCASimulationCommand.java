package com.stockquest.application.dca.dto;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DCA 시뮬레이션 요청 커맨드
 * 사용자 요청을 애플리케이션 레이어로 전달하기 위한 DTO
 */
@Getter
public class DCASimulationCommand {

    private final String symbol;
    private final BigDecimal monthlyInvestmentAmount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String frequency;

    public DCASimulationCommand(String symbol,
                               BigDecimal monthlyInvestmentAmount,
                               LocalDateTime startDate,
                               LocalDateTime endDate,
                               String frequency) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다");
        }
        if (monthlyInvestmentAmount == null || monthlyInvestmentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("월 투자 금액은 0보다 커야 합니다");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("시작일은 필수입니다");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("종료일은 필수입니다");
        }
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 늦어야 합니다");
        }
        if (frequency == null || frequency.trim().isEmpty()) {
            throw new IllegalArgumentException("투자 주기는 필수입니다");
        }

        this.symbol = symbol.trim().toUpperCase();
        this.monthlyInvestmentAmount = monthlyInvestmentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency.trim().toUpperCase();
    }
}