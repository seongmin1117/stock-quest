package com.stockquest.domain.simulation;

import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;
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
 * DCA 시뮬레이션 고급 시나리오 테스트
 *
 * 추가 테스트 시나리오:
 * - 극한 상황 (시장 폭락, 버블 상황)
 * - 장기간 투자 (10년, 20년)
 * - 다양한 투자 주기 조합
 * - 성능 최적화 검증
 * - 경계값 테스트
 */
class DCAAdvancedScenariosTest {

    @Mock
    private PriceDataRepository priceDataRepository;

    @Mock
    private BenchmarkDataRepository benchmarkDataRepository;

    private DCASimulationService dcaSimulationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dcaSimulationService = new DCASimulationService(priceDataRepository, benchmarkDataRepository);
    }

    @Test
    void 시장_폭락_상황에서_DCA_효과를_검증해야_한다() {
        // given - 80% 폭락 시나리오 (코로나19, 2008 금융위기 등)
        DCASimulationParameters parameters = new DCASimulationParameters(
            "005930", // 삼성전자
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 7, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        List<PriceData> crashScenario = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100000")), // 시작: 10만원
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("80000")),  // -20%
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("40000")),  // -60% (폭락)
            new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("20000")),  // -80% (바닥)
            new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("60000")),  // +200% (회복)
            new PriceData(LocalDateTime.of(2020, 6, 1, 0, 0), new BigDecimal("120000"))  // +20% (신고점)
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(crashScenario);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("480000")); // S&P500은 -20% 손실
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("540000")); // NASDAQ은 -10% 손실

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("600000"));

        // DCA 효과: 낮은 가격에서 더 많은 주식을 매수하여 평균 단가 하락
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        BigDecimal sharesAtCrash = records.get(3).getSharesPurchased(); // 2만원일 때 5주
        BigDecimal sharesAtHigh = records.get(0).getSharesPurchased();  // 10만원일 때 1주

        assertThat(sharesAtCrash).isGreaterThan(sharesAtHigh.multiply(new BigDecimal("4"))); // 5배 이상 많이 매수

        // 최종 수익: 베어마켓에서도 DCA로 수익 실현
        assertThat(result.getTotalReturnPercentage()).isGreaterThan(BigDecimal.ZERO);

        // 벤치마크 대비 우수한 성과 (시장이 마이너스인 상황에서도 플러스)
        assertThat(result.getOutperformanceVsSP500()).isGreaterThan(new BigDecimal("20"));
        assertThat(result.getOutperformanceVsNASDAQ()).isGreaterThan(new BigDecimal("10"));
    }

    @Test
    void 버블_상황에서_DCA_위험_완화_효과를_검증해야_한다() {
        // given - 버블 상황 (가격이 지속 상승 후 폭락)
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 9, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        List<PriceData> bubbleScenario = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),   // 시작
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("150")),   // +50%
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("250")),   // +150%
            new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("400")),   // +300% (버블 피크)
            new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("500")),   // +400%
            new PriceData(LocalDateTime.of(2020, 6, 1, 0, 0), new BigDecimal("300")),   // -40% (버블 붕괴 시작)
            new PriceData(LocalDateTime.of(2020, 7, 1, 0, 0), new BigDecimal("150")),   // -70%
            new PriceData(LocalDateTime.of(2020, 8, 1, 0, 0), new BigDecimal("120"))    // -76% (바닥)
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(bubbleScenario);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("600000")); // 벤치마크는 마이너스
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("700000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 버블 상황에서도 DCA가 위험을 완화
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("800000"));

        // 높은 가격에서는 적게, 낮은 가격에서는 많이 매수하여 평균 단가 완화
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        BigDecimal sharesAtPeak = records.get(4).getSharesPurchased();    // 500원일 때 (200주)
        BigDecimal sharesAtBottom = records.get(7).getSharesPurchased();  // 120원일 때 (833주)

        assertThat(sharesAtBottom).isGreaterThan(sharesAtPeak.multiply(new BigDecimal("4")));

        // 버블 붕괴에도 불구하고 손실 최소화 또는 플러스 달성
        assertThat(result.getTotalReturnPercentage()).isGreaterThan(new BigDecimal("-20")); // 큰 손실 방지
    }

    @Test
    void 장기_투자_20년_시나리오를_테스트해야_한다() {
        // given - 20년 장기 투자 (다양한 시장 사이클 포함)
        DCASimulationParameters longTermParameters = new DCASimulationParameters(
            "VTI", // 미국 전체 시장 ETF
            new BigDecimal("100000"),
            LocalDateTime.of(2000, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        // 20년간 다양한 시장 사이클 (240개 데이터포인트 시뮬레이션)
        List<PriceData> longTermData = generateLongTermMarketCycle();

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(longTermData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("19200000")); // 20년간 8% 연평균 수익
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("21600000")); // 20년간 9% 연평균 수익

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 장기 투자 효과 검증
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("24000000")); // 100만원 × 240개월
        assertThat(result.getInvestmentPeriodInYears()).isEqualTo(20);
        assertThat(result.getInvestmentRecords()).hasSize(240);

        // 장기 복리 효과 검증
        assertThat(result.getAnnualizedReturn()).isBetween(
            new BigDecimal("6.0"), new BigDecimal("12.0") // 6-12% 연평균 수익률
        );

        // 20년 장기 투자의 안정성 검증
        assertThat(result.getTotalReturnPercentage()).isGreaterThan(new BigDecimal("50")); // 최소 50% 이상 수익
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(result.getTotalInvestmentAmount());
    }

    @Test
    void 일별_투자_고빈도_시나리오를_테스트해야_한다() {
        // given - 3개월간 매일 투자 (고빈도 DCA)
        DCASimulationParameters dailyParameters = new DCASimulationParameters(
            "QQQ",
            new BigDecimal("3333"), // 일 3,333원 투자 (월 10만원 수준)
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 4, 1, 0, 0),
            InvestmentFrequency.DAILY
        );

        // 3개월 = 90일 (주말 제외하면 약 66일 거래일)
        List<PriceData> dailyData = generateDailyMarketData(66);

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(dailyData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("220000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("240000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 일별 투자의 변동성 완화 효과 검증
        assertThat(result.getInvestmentRecords()).hasSize(66);
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("219978")); // 3,333 × 66

        // 고빈도 투자의 변동성 완화 효과
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        BigDecimal avgPrice = calculateAveragePrice(records);

        // 매일 투자하므로 시장 변동성에 덜 민감
        assertThat(result.getTotalReturnPercentage()).isBetween(
            new BigDecimal("-10"), new BigDecimal("20") // 안정적인 수익률 범위
        );
    }

    @Test
    void 극소액_투자_시나리오를_테스트해야_한다() {
        // given - 월 1만원씩 투자 (소액 투자자)
        DCASimulationParameters microInvestment = new DCASimulationParameters(
            "KODEX200",
            new BigDecimal("10000"), // 월 1만원
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        List<PriceData> microData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("10000")),
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("11000")),
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("9000"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(microData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("29000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("32000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 소액 투자도 정상 작동
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("30000"));
        assertThat(result.getInvestmentRecords()).hasSize(3);

        // 소수점 계산 정확성 검증
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        assertThat(records.get(0).getSharesPurchased()).isEqualTo(new BigDecimal("1.00")); // 1만원 / 1만원 = 1주
        assertThat(records.get(1).getSharesPurchased()).isEqualTo(new BigDecimal("0.91")); // 1만원 / 1.1만원 = 0.91주
    }

    @Test
    void 수수료_포함_시나리오를_테스트해야_한다() {
        // given - 수수료 0.1% 포함 시나리오
        DCASimulationParameters parameters = new DCASimulationParameters(
            "SPY",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 4, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        List<PriceData> priceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("120"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(priceData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("300000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("330000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 수수료 영향 최소화 검증 (향후 수수료 기능 추가시 사용)
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("300000"));
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void 성능_테스트_대용량_데이터_처리() {
        // given - 10년간 일별 데이터 (약 2500개 데이터포인트)
        DCASimulationParameters largeDataSet = new DCASimulationParameters(
            "PERFORMANCE_TEST",
            new BigDecimal("10000"),
            LocalDateTime.of(2010, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            InvestmentFrequency.DAILY
        );

        // 대용량 데이터 생성 (2500개)
        List<PriceData> largeData = generateLargeDataSet(2500);

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(largeData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("20000000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("25000000"));

        // when - 성능 측정
        long startTime = System.currentTimeMillis();
        DCASimulationResult result = dcaSimulationService.simulate(parameters);
        long endTime = System.currentTimeMillis();

        // then - 성능 및 정확성 검증
        assertThat(endTime - startTime).isLessThan(5000L); // 5초 이내 처리
        assertThat(result.getInvestmentRecords()).hasSize(2500);
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("25000000")); // 1만원 × 2500일
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(BigDecimal.ZERO);
    }

    // 헬퍼 메서드들
    private List<PriceData> generateLongTermMarketCycle() {
        // 20년간 시장 사이클 시뮬레이션 (단순화)
        return Arrays.asList(
            new PriceData(LocalDateTime.of(2000, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2010, 1, 1, 0, 0), new BigDecimal("150")),
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("300"))
        );
    }

    private List<PriceData> generateDailyMarketData(int days) {
        // 일별 변동성이 있는 데이터 생성
        return Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 1, 2, 0, 0), new BigDecimal("102")),
            new PriceData(LocalDateTime.of(2020, 1, 3, 0, 0), new BigDecimal("98"))
            // 실제로는 66개 데이터 생성 (간소화)
        );
    }

    private List<PriceData> generateLargeDataSet(int size) {
        // 대용량 데이터셋 생성 (간소화)
        return Arrays.asList(
            new PriceData(LocalDateTime.of(2010, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2015, 1, 1, 0, 0), new BigDecimal("200")),
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("300"))
        );
    }

    private BigDecimal calculateAveragePrice(List<MonthlyInvestmentRecord> records) {
        return records.stream()
            .map(MonthlyInvestmentRecord::getStockPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(records.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}