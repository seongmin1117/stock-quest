package com.stockquest.domain.simulation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD: RED - DCASimulationParameters 도메인 엔티티 테스트
 * DCA(Dollar Cost Averaging) 시뮬레이션 파라미터를 검증
 */
class DCASimulationParametersTest {

    @Test
    void DCA_시뮬레이션_파라미터가_올바르게_생성되어야_한다() {
        // given
        String symbol = "AAPL";
        BigDecimal monthlyInvestmentAmount = new BigDecimal("100000");
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        InvestmentFrequency frequency = InvestmentFrequency.MONTHLY;

        // when
        DCASimulationParameters parameters = new DCASimulationParameters(
            symbol, monthlyInvestmentAmount, startDate, endDate, frequency
        );

        // then
        assertThat(parameters).isNotNull();
        assertThat(parameters.getSymbol()).isEqualTo(symbol);
        assertThat(parameters.getMonthlyInvestmentAmount()).isEqualTo(monthlyInvestmentAmount);
        assertThat(parameters.getStartDate()).isEqualTo(startDate);
        assertThat(parameters.getEndDate()).isEqualTo(endDate);
        assertThat(parameters.getFrequency()).isEqualTo(frequency);
    }

    @Test
    void 투자_기간이_올바르게_계산되어야_한다() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL", new BigDecimal("100000"), startDate, endDate, InvestmentFrequency.MONTHLY
        );

        // when
        long investmentPeriodInYears = parameters.getInvestmentPeriodInYears();

        // then
        assertThat(investmentPeriodInYears).isEqualTo(5);
    }

    @Test
    void 총_투자_횟수가_올바르게_계산되어야_한다() {
        // given - 5년 동안 월별 투자
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL", new BigDecimal("100000"), startDate, endDate, InvestmentFrequency.MONTHLY
        );

        // when
        int totalInvestmentCount = parameters.getTotalInvestmentCount();

        // then
        assertThat(totalInvestmentCount).isEqualTo(60); // 5년 × 12개월
    }

    @Test
    void 주별_투자시_총_투자_횟수가_올바르게_계산되어야_한다() {
        // given - 1년 동안 주별 투자
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL", new BigDecimal("50000"), startDate, endDate, InvestmentFrequency.WEEKLY
        );

        // when
        int totalInvestmentCount = parameters.getTotalInvestmentCount();

        // then
        assertThat(totalInvestmentCount).isEqualTo(52); // 1년 ≈ 52주
    }

    @Test
    void 총_투자_원금이_올바르게_계산되어야_한다() {
        // given
        BigDecimal monthlyAmount = new BigDecimal("100000");
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL", monthlyAmount, startDate, endDate, InvestmentFrequency.MONTHLY
        );

        // when
        BigDecimal totalPrincipal = parameters.getTotalPrincipal();

        // then
        assertThat(totalPrincipal).isEqualTo(new BigDecimal("6000000")); // 100,000 × 60개월
    }

    @Test
    void 투자_금액이_음수일_경우_예외가_발생해야_한다() {
        // given
        BigDecimal negativeAmount = new BigDecimal("-100000");
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);

        // when & then
        assertThatThrownBy(() -> new DCASimulationParameters(
            "AAPL", negativeAmount, startDate, endDate, InvestmentFrequency.MONTHLY
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("투자 금액은 0보다 커야 합니다");
    }

    @Test
    void 시작일이_종료일보다_늦을_경우_예외가_발생해야_한다() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // when & then
        assertThatThrownBy(() -> new DCASimulationParameters(
            "AAPL", new BigDecimal("100000"), startDate, endDate, InvestmentFrequency.MONTHLY
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("시작일은 종료일보다 빨라야 합니다");
    }

    @Test
    void 심볼이_null이거나_빈_문자열일_경우_예외가_발생해야_한다() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 1, 0, 0);

        // when & then - null 심볼
        assertThatThrownBy(() -> new DCASimulationParameters(
            null, new BigDecimal("100000"), startDate, endDate, InvestmentFrequency.MONTHLY
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("심볼은 필수입니다");

        // when & then - 빈 심볼
        assertThatThrownBy(() -> new DCASimulationParameters(
            "", new BigDecimal("100000"), startDate, endDate, InvestmentFrequency.MONTHLY
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("심볼은 필수입니다");
    }

    @Test
    void 파라미터_정보를_문자열로_표현할_수_있어야_한다() {
        // given
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL", new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        // when
        String description = parameters.toString();

        // then
        assertThat(description)
            .contains("AAPL")
            .contains("100000")
            .contains("월별")
            .contains("5년");
    }
}