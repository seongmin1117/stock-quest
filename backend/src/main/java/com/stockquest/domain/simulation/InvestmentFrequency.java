package com.stockquest.domain.simulation;

import lombok.Getter;

/**
 * DCA 투자 주기를 나타내는 Enum
 * Dollar Cost Averaging 시뮬레이션에서 투자 빈도를 정의
 */
@Getter
public enum InvestmentFrequency {

    DAILY("일별", 1),
    WEEKLY("주별", 7),
    MONTHLY("월별", 30);

    private final String description;
    private final int daysPerPeriod;

    InvestmentFrequency(String description, int daysPerPeriod) {
        this.description = description;
        this.daysPerPeriod = daysPerPeriod;
    }
}