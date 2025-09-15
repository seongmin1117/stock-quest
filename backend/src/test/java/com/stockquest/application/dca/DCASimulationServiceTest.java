package com.stockquest.application.dca;

import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import com.stockquest.domain.simulation.DCASimulationService;
import com.stockquest.domain.simulation.DCASimulationParameters;
import com.stockquest.domain.simulation.DCASimulationResult;
import com.stockquest.domain.simulation.InvestmentFrequency;
import com.stockquest.domain.simulation.MonthlyInvestmentRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD: RED - DCA 시뮬레이션 애플리케이션 서비스 테스트
 * 사용자 요청을 받아 도메인 서비스를 호출하고 응답을 변환하는 기능 검증
 */
class DCASimulationServiceTest {

    @Mock
    private DCASimulationService domainSimulationService;

    private com.stockquest.application.dca.DCASimulationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new com.stockquest.application.dca.DCASimulationService(domainSimulationService);
    }

    @Test
    void DCA_시뮬레이션_요청을_정상적으로_처리해야_한다() {
        // given
        DCASimulationCommand command = new DCASimulationCommand(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 6, 1, 0, 0),
            "MONTHLY"
        );

        DCASimulationResult domainResult = createMockDomainResult();
        when(domainSimulationService.simulate(any(DCASimulationParameters.class)))
            .thenReturn(domainResult);

        // when
        DCASimulationResponse response = applicationService.simulate(command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("500000"));
        assertThat(response.getFinalPortfolioValue()).isEqualTo(new BigDecimal("650000"));
        assertThat(response.getTotalReturnPercentage()).isEqualTo(new BigDecimal("30.00"));
        assertThat(response.getInvestmentRecords()).hasSize(5);
        assertThat(response.getSp500ReturnAmount()).isEqualTo(new BigDecimal("600000"));
        assertThat(response.getNasdaqReturnAmount()).isEqualTo(new BigDecimal("650000"));

        // 도메인 서비스 호출 검증
        verify(domainSimulationService).simulate(argThat(params ->
            params.getSymbol().equals("AAPL") &&
            params.getMonthlyInvestmentAmount().equals(new BigDecimal("100000")) &&
            params.getFrequency() == InvestmentFrequency.MONTHLY
        ));
    }

    @Test
    void 다양한_투자_주기를_올바르게_변환해야_한다() {
        // given - WEEKLY
        DCASimulationCommand weeklyCommand = new DCASimulationCommand(
            "MSFT",
            new BigDecimal("50000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 2, 1, 0, 0),
            "WEEKLY"
        );

        DCASimulationResult mockResult = createMockDomainResult();
        when(domainSimulationService.simulate(any(DCASimulationParameters.class)))
            .thenReturn(mockResult);

        // when
        applicationService.simulate(weeklyCommand);

        // then
        verify(domainSimulationService).simulate(argThat(params ->
            params.getFrequency() == InvestmentFrequency.WEEKLY
        ));
    }

    @Test
    void 투자_기록을_올바른_DTO로_변환해야_한다() {
        // given
        DCASimulationCommand command = new DCASimulationCommand(
            "GOOGL",
            new BigDecimal("200000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 3, 1, 0, 0),
            "MONTHLY"
        );

        DCASimulationResult domainResult = createMockDomainResult();
        when(domainSimulationService.simulate(any(DCASimulationParameters.class)))
            .thenReturn(domainResult);

        // when
        DCASimulationResponse response = applicationService.simulate(command);

        // then
        assertThat(response.getInvestmentRecords()).hasSize(5);

        DCASimulationResponse.MonthlyInvestmentRecordDto firstRecord = response.getInvestmentRecords().get(0);
        assertThat(firstRecord.getInvestmentDate()).isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0));
        assertThat(firstRecord.getInvestmentAmount()).isEqualTo(new BigDecimal("100000"));
        assertThat(firstRecord.getStockPrice()).isEqualTo(new BigDecimal("100"));
        assertThat(firstRecord.getSharesPurchased()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(firstRecord.getPortfolioValue()).isEqualTo(new BigDecimal("100000"));
    }

    @Test
    void 잘못된_투자_주기가_전달되면_예외가_발생해야_한다() {
        // given
        DCASimulationCommand command = new DCASimulationCommand(
            "TSLA",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 6, 1, 0, 0),
            "INVALID"
        );

        // when & then
        assertThatThrownBy(() -> applicationService.simulate(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("지원하지 않는 투자 주기입니다: INVALID");
    }

    @Test
    void null_커맨드가_전달되면_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> applicationService.simulate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시뮬레이션 요청은 필수입니다");
    }

    @Test
    void 도메인_서비스가_null이면_생성자에서_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new com.stockquest.application.dca.DCASimulationService(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("DCASimulationService는 필수입니다");
    }

    @Test
    void 벤치마크_대비_성과를_올바르게_계산해야_한다() {
        // given
        DCASimulationCommand command = new DCASimulationCommand(
            "NFLX",
            new BigDecimal("150000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 7, 1, 0, 0),
            "MONTHLY"
        );

        // 수익률: 30%, S&P 500: 20%, NASDAQ: 30%
        DCASimulationResult domainResult = createMockDomainResult();
        when(domainSimulationService.simulate(any(DCASimulationParameters.class)))
            .thenReturn(domainResult);

        // when
        DCASimulationResponse response = applicationService.simulate(command);

        // then
        assertThat(response.getOutperformanceVsSP500()).isEqualTo(new BigDecimal("10.00"));
        assertThat(response.getOutperformanceVsNASDAQ()).isEqualTo(new BigDecimal("0.00"));
    }

    private DCASimulationResult createMockDomainResult() {
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 6, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("100000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("110"),
                new BigDecimal("909.09"),
                new BigDecimal("200000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 3, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("90"),
                new BigDecimal("1111.11"),
                new BigDecimal("300000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 4, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("120"),
                new BigDecimal("833.33"),
                new BigDecimal("400000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 5, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("130"),
                new BigDecimal("769.23"),
                new BigDecimal("500000")
            )
        );

        return new DCASimulationResult(
            parameters,
            new BigDecimal("500000"), // 총 투자금
            new BigDecimal("650000"), // 최종 가치 (30% 수익)
            records,
            new BigDecimal("600000"), // S&P 500 (20% 수익)
            new BigDecimal("650000")  // NASDAQ (30% 수익)
        );
    }
}