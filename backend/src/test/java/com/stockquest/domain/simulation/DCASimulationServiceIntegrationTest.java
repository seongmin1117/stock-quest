package com.stockquest.domain.simulation;

import com.stockquest.adapter.out.persistence.simulation.BenchmarkDataRepositoryAdapter;
import com.stockquest.adapter.out.persistence.simulation.PriceDataRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD: DCA 시뮬레이션 도메인 서비스 통합 테스트
 * 실제 데이터베이스와 연동하여 DCA 시뮬레이션이 올바르게 작동하는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DCASimulationServiceIntegrationTest {

    @Autowired
    private DCASimulationService dcaSimulationService;

    @Autowired
    private PriceDataRepositoryAdapter priceDataRepository;

    @Autowired
    private BenchmarkDataRepositoryAdapter benchmarkDataRepository;

    @Test
    @DisplayName("Samsung Electronics 데이터로 DCA 시뮬레이션이 정상적으로 실행되어야 한다")
    void should_simulate_DCA_successfully_with_Samsung_data() {
        // given
        String symbol = "005930";
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 6, 1, 0, 0);
        BigDecimal monthlyAmount = new BigDecimal("100000");
        InvestmentFrequency frequency = InvestmentFrequency.MONTHLY;

        DCASimulationParameters parameters = new DCASimulationParameters(
            symbol, monthlyAmount, startDate, endDate, frequency
        );

        // when - DCA 시뮬레이션 실행
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getParameters()).isNotNull();
        assertThat(result.getParameters().getSymbol()).isEqualTo(symbol);
        assertThat(result.getTotalInvestmentAmount()).isNotNull();
        assertThat(result.getFinalPortfolioValue()).isNotNull();
        assertThat(result.getInvestmentRecords()).isNotEmpty();

        // 투자 기록이 월별로 존재하는지 확인 (1월~5월, 5개월)
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        assertThat(records).hasSizeGreaterThanOrEqualTo(5);

        // 총 투자금액 = 월별 투자금액 * 투자 횟수
        BigDecimal expectedTotalInvestment = monthlyAmount.multiply(new BigDecimal(records.size()));
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(expectedTotalInvestment);

        // 최종 포트폴리오 가치가 양수여야 함
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(BigDecimal.ZERO);

        System.out.println("=== DCA 시뮬레이션 결과 ===");
        System.out.println("총 투자금액: " + result.getTotalInvestmentAmount());
        System.out.println("최종 포트폴리오 가치: " + result.getFinalPortfolioValue());
        System.out.println("총 수익률: " + result.getTotalReturnPercentage() + "%");
        System.out.println("연환산 수익률: " + result.getAnnualizedReturn() + "%");
        System.out.println("투자 기록 수: " + records.size());
    }

    @Test
    @DisplayName("가격 데이터가 올바르게 조회되어야 한다")
    void should_retrieve_price_data_correctly() {
        // given
        String symbol = "005930";
        LocalDateTime startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 6, 1, 0, 0);

        // when
        List<PriceData> priceDataList = priceDataRepository.findBySymbolAndDateRange(symbol, startDate, endDate);

        // then
        assertThat(priceDataList).isNotEmpty();
        assertThat(priceDataList.size()).isGreaterThan(70); // 약 5개월간의 일별 데이터

        // 첫 번째와 마지막 데이터 확인
        PriceData firstData = priceDataList.get(0);
        PriceData lastData = priceDataList.get(priceDataList.size() - 1);

        assertThat(firstData.getPrice()).isGreaterThan(BigDecimal.ZERO);
        assertThat(lastData.getPrice()).isGreaterThan(BigDecimal.ZERO);

        System.out.println("=== 가격 데이터 조회 결과 ===");
        System.out.println("총 데이터 수: " + priceDataList.size());
        System.out.println("첫 번째 데이터: " + firstData.getDate() + " -> " + firstData.getPrice());
        System.out.println("마지막 데이터: " + lastData.getDate() + " -> " + lastData.getPrice());
    }

    @Test
    @DisplayName("DCA 파라미터 검증이 올바르게 작동해야 한다")
    void should_validate_DCA_parameters_correctly() {
        // given - null 파라미터
        DCASimulationParameters nullParameters = null;

        // when & then - null 파라미터는 예외 발생
        assertThatThrownBy(() -> dcaSimulationService.simulate(nullParameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("시뮬레이션 파라미터는 필수입니다");

        // given - 잘못된 날짜 범위 (시작일이 종료일보다 늦음)
        DCASimulationParameters invalidDateRange = new DCASimulationParameters(
            "005930",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 6, 1, 0, 0), // 시작일
            LocalDateTime.of(2020, 1, 1, 0, 0), // 종료일 (시작일보다 이전)
            InvestmentFrequency.MONTHLY
        );

        // when & then - 잘못된 날짜 범위는 빈 결과 또는 예외 발생
        assertThatThrownBy(() -> dcaSimulationService.simulate(invalidDateRange))
            .isInstanceOf(IllegalArgumentException.class);
    }
}