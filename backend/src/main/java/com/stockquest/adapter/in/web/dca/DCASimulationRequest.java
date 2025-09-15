package com.stockquest.adapter.in.web.dca;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DCA 시뮬레이션 웹 요청 DTO
 * 클라이언트로부터 받은 HTTP 요청을 애플리케이션 레이어로 전달하기 위한 DTO
 */
@Getter
public class DCASimulationRequest {

    @NotBlank(message = "종목 코드는 필수입니다")
    private final String symbol;

    @NotNull(message = "월 투자 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "월 투자 금액은 0보다 커야 합니다")
    private final BigDecimal monthlyInvestmentAmount;

    @NotBlank(message = "시작일은 필수입니다")
    private final String startDate;

    @NotBlank(message = "종료일은 필수입니다")
    private final String endDate;

    @NotBlank(message = "투자 주기는 필수입니다")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "투자 주기는 DAILY, WEEKLY, MONTHLY 중 하나여야 합니다")
    private final String frequency;

    @JsonCreator
    public DCASimulationRequest(
        @JsonProperty("symbol") String symbol,
        @JsonProperty("monthlyInvestmentAmount") BigDecimal monthlyInvestmentAmount,
        @JsonProperty("startDate") String startDate,
        @JsonProperty("endDate") String endDate,
        @JsonProperty("frequency") String frequency
    ) {
        this.symbol = symbol;
        this.monthlyInvestmentAmount = monthlyInvestmentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
    }
}