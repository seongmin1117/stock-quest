package com.stockquest.domain.simulation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD: RED - DCASimulationResult 도메인 엔티티 테스트
 * DCA 시뮬레이션 결과를 검증
 */
class DCASimulationResultTest {

    @Test
    void DCA_시뮬레이션_결과가_올바르게_생성되어야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();
        BigDecimal totalInvestmentAmount = new BigDecimal("6000000");
        BigDecimal finalPortfolioValue = new BigDecimal("8500000");
        List<MonthlyInvestmentRecord> investmentRecords = createTestRecords();
        BigDecimal sp500Return = new BigDecimal("7200000");
        BigDecimal nasdaqReturn = new BigDecimal("9100000");

        // when
        DCASimulationResult result = new DCASimulationResult(
            parameters,
            totalInvestmentAmount,
            finalPortfolioValue,
            investmentRecords,
            sp500Return,
            nasdaqReturn
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getParameters()).isEqualTo(parameters);
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(totalInvestmentAmount);
        assertThat(result.getFinalPortfolioValue()).isEqualTo(finalPortfolioValue);
        assertThat(result.getInvestmentRecords()).hasSize(2);
        assertThat(result.getSp500Return()).isEqualTo(sp500Return);
        assertThat(result.getNasdaqReturn()).isEqualTo(nasdaqReturn);
    }

    @Test
    void 총_수익률이_올바르게_계산되어야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();
        BigDecimal totalInvestment = new BigDecimal("6000000"); // 600만원 투자
        BigDecimal finalValue = new BigDecimal("8500000");      // 850만원으로 성장
        DCASimulationResult result = new DCASimulationResult(
            parameters, totalInvestment, finalValue, createTestRecords(),
            new BigDecimal("7200000"), new BigDecimal("9100000")
        );

        // when
        BigDecimal totalReturn = result.getTotalReturnPercentage();

        // then
        // 계산: ((850 - 600) / 600) * 100 = 41.67%
        assertThat(totalReturn).isEqualTo(new BigDecimal("41.67"));
    }

    @Test
    void 연평균_수익률이_올바르게_계산되어야_한다() {
        // given - 5년 투자
        DCASimulationParameters parameters = createTestParameters();
        BigDecimal totalInvestment = new BigDecimal("6000000");
        BigDecimal finalValue = new BigDecimal("8500000");
        DCASimulationResult result = new DCASimulationResult(
            parameters, totalInvestment, finalValue, createTestRecords(),
            new BigDecimal("7200000"), new BigDecimal("9100000")
        );

        // when
        BigDecimal annualReturn = result.getAnnualizedReturn();

        // then
        // 계산: ((8500000/6000000)^(1/5) - 1) * 100 ≈ 7.25%
        assertThat(annualReturn.doubleValue()).isCloseTo(7.25, within(0.1));
    }

    @Test
    void SP500_대비_성과가_올바르게_계산되어야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();
        BigDecimal totalInvestment = new BigDecimal("6000000");
        BigDecimal finalValue = new BigDecimal("8500000");      // 41.67% 수익
        BigDecimal sp500Value = new BigDecimal("7200000");      // 20% 수익
        DCASimulationResult result = new DCASimulationResult(
            parameters, totalInvestment, finalValue, createTestRecords(),
            sp500Value, new BigDecimal("9100000")
        );

        // when
        BigDecimal outperformance = result.getOutperformanceVsSP500();

        // then
        // 계산: 41.67% - 20% = 21.67%
        assertThat(outperformance).isEqualTo(new BigDecimal("21.67"));
    }

    @Test
    void NASDAQ_대비_성과가_올바르게_계산되어야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();
        BigDecimal totalInvestment = new BigDecimal("6000000");
        BigDecimal finalValue = new BigDecimal("8500000");      // 41.67% 수익
        BigDecimal nasdaqValue = new BigDecimal("9100000");     // 51.67% 수익
        DCASimulationResult result = new DCASimulationResult(
            parameters, totalInvestment, finalValue, createTestRecords(),
            new BigDecimal("7200000"), nasdaqValue
        );

        // when
        BigDecimal outperformance = result.getOutperformanceVsNASDAQ();

        // then
        // 계산: 41.67% - 51.67% = -10%
        assertThat(outperformance).isEqualTo(new BigDecimal("-10.00"));
    }

    @Test
    void 최고_포트폴리오_가치가_올바르게_계산되어야_한다() {
        // given
        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000"),
                new BigDecimal("100000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("80"),
                new BigDecimal("1250"),
                new BigDecimal("280000") // 최고값
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 3, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("120"),
                new BigDecimal("833.33"),
                new BigDecimal("250000")
            )
        );

        DCASimulationResult result = new DCASimulationResult(
            createTestParameters(), new BigDecimal("300000"), new BigDecimal("250000"),
            records, new BigDecimal("320000"), new BigDecimal("380000")
        );

        // when
        BigDecimal maxValue = result.getMaxPortfolioValue();

        // then
        assertThat(maxValue).isEqualTo(new BigDecimal("280000"));
    }

    @Test
    void 최대_손실률이_올바르게_계산되어야_한다() {
        // given
        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000"),
                new BigDecimal("100000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("80"),
                new BigDecimal("1250"),
                new BigDecimal("280000") // 최고값
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 3, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("60"),
                new BigDecimal("1666.67"),
                new BigDecimal("200000") // 최대 손실 시점
            )
        );

        DCASimulationResult result = new DCASimulationResult(
            createTestParameters(), new BigDecimal("300000"), new BigDecimal("250000"),
            records, new BigDecimal("320000"), new BigDecimal("380000")
        );

        // when
        BigDecimal maxDrawdown = result.getMaxDrawdown();

        // then
        // 계산: (200000 - 280000) / 280000 * 100 = -28.57%
        assertThat(maxDrawdown.doubleValue()).isCloseTo(-28.57, within(0.1));
    }

    @Test
    void 시뮬레이션_성과_요약_정보를_제공해야_한다() {
        // given
        DCASimulationResult result = new DCASimulationResult(
            createTestParameters(),
            new BigDecimal("6000000"),
            new BigDecimal("8500000"),
            createTestRecords(),
            new BigDecimal("7200000"),
            new BigDecimal("9100000")
        );

        // when
        String summary = result.getSummary();

        // then
        assertThat(summary)
            .contains("AAPL")
            .contains("6,000,000원")
            .contains("8,500,000원")
            .contains("41.67%")
            .contains("S&P 500")
            .contains("NASDAQ");
    }

    @Test
    void 투자_금액이_null일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new DCASimulationResult(
            createTestParameters(),
            null,
            new BigDecimal("8500000"),
            createTestRecords(),
            new BigDecimal("7200000"),
            new BigDecimal("9100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("투자 금액은 필수입니다");
    }

    @Test
    void 최종_포트폴리오_가치가_음수일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new DCASimulationResult(
            createTestParameters(),
            new BigDecimal("6000000"),
            new BigDecimal("-1000"),
            createTestRecords(),
            new BigDecimal("7200000"),
            new BigDecimal("9100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("포트폴리오 가치는 0 이상이어야 합니다");
    }

    private DCASimulationParameters createTestParameters() {
        return new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );
    }

    private List<MonthlyInvestmentRecord> createTestRecords() {
        return Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000"),
                new BigDecimal("100000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("110"),
                new BigDecimal("909.09"),
                new BigDecimal("200000")
            )
        );
    }
}