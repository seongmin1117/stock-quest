package com.stockquest.domain.simulation;

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
 * TDD: RED - DCASimulationService 도메인 서비스 테스트
 * DCA 시뮬레이션 실행 로직을 검증
 */
class DCASimulationServiceTest {

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
    void DCA_시뮬레이션을_정상적으로_실행해야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();

        // Mock 주가 데이터 (5개월치)
        List<PriceData> mockPriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("90")),
            new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("120")),
            new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("130"))
        );

        // Mock 벤치마크 데이터
        when(priceDataRepository.findBySymbolAndDateRange(
            eq("AAPL"), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(mockPriceData);

        when(benchmarkDataRepository.calculateSP500Return(
            any(BigDecimal.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("450000")); // 50만원이 45만원 (10% 손실)

        when(benchmarkDataRepository.calculateNASDAQReturn(
            any(BigDecimal.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("600000")); // 50만원이 60만원 (20% 수익)

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getParameters()).isEqualTo(parameters);
        assertThat(result.getInvestmentRecords()).hasSize(5);
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("500000")); // 10만원 × 5회
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(BigDecimal.ZERO);

        // 벤치마크 비교 데이터 확인
        assertThat(result.getSp500Return()).isEqualTo(new BigDecimal("450000"));
        assertThat(result.getNasdaqReturn()).isEqualTo(new BigDecimal("600000"));

        // Mock 호출 검증
        verify(priceDataRepository).findBySymbolAndDateRange(eq("AAPL"), any(), any());
        verify(benchmarkDataRepository).calculateSP500Return(any(), any(), any());
        verify(benchmarkDataRepository).calculateNASDAQReturn(any(), any(), any());
    }

    @Test
    void 월별_투자_기록이_정확하게_계산되어야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();

        List<PriceData> mockPriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("200"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(mockPriceData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("180000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("220000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();

        // 첫 번째 투자: 100,000원으로 100원 주식 → 1,000주
        MonthlyInvestmentRecord firstRecord = records.get(0);
        assertThat(firstRecord.getInvestmentAmount()).isEqualTo(new BigDecimal("100000"));
        assertThat(firstRecord.getStockPrice()).isEqualTo(new BigDecimal("100"));
        assertThat(firstRecord.getSharesPurchased()).isEqualTo(new BigDecimal("1000.00"));

        // 두 번째 투자: 100,000원으로 200원 주식 → 500주
        MonthlyInvestmentRecord secondRecord = records.get(1);
        assertThat(secondRecord.getInvestmentAmount()).isEqualTo(new BigDecimal("100000"));
        assertThat(secondRecord.getStockPrice()).isEqualTo(new BigDecimal("200"));
        assertThat(secondRecord.getSharesPurchased()).isEqualTo(new BigDecimal("500.00"));

        // 포트폴리오 가치 확인 (총 1,500주 × 최종 주가 200원)
        assertThat(secondRecord.getPortfolioValue()).isEqualTo(new BigDecimal("300000"));
    }

    @Test
    void 주가_데이터가_없을_경우_예외가_발생해야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();
        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(Arrays.asList()); // 빈 리스트

        // when & then
        assertThatThrownBy(() -> dcaSimulationService.simulate(parameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 기간의 주가 데이터를 찾을 수 없습니다");
    }

    @Test
    void 투자_날짜와_주가_날짜_매칭이_정확해야_한다() {
        // given
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 15, 0, 0), // 15일 시작
            LocalDateTime.of(2020, 3, 15, 0, 0), // 3월 15일 종료
            InvestmentFrequency.MONTHLY
        );

        // 주가 데이터는 매월 1일 기준
        List<PriceData> mockPriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("120"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(mockPriceData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("210000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("240000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        assertThat(records).hasSize(2); // 1월, 2월 투자 (3월은 종료일 이후)

        // 투자일과 가장 가까운 주가 데이터 사용 확인
        assertThat(records.get(0).getStockPrice()).isEqualTo(new BigDecimal("100")); // 1월 데이터
        assertThat(records.get(1).getStockPrice()).isEqualTo(new BigDecimal("110")); // 2월 데이터
    }

    @Test
    void 주별_투자_주기도_정상적으로_작동해야_한다() {
        // given
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("25000"), // 주당 2.5만원
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 15, 0, 0), // 2주간
            InvestmentFrequency.WEEKLY
        );

        List<PriceData> mockPriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
            new PriceData(LocalDateTime.of(2020, 1, 8, 0, 0), new BigDecimal("105"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(mockPriceData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("52500"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("55000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();
        assertThat(records).hasSize(2); // 2주 투자
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("50000")); // 2.5만원 × 2회
    }

    @Test
    void 벤치마크_데이터_조회_실패시_기본값을_사용해야_한다() {
        // given
        DCASimulationParameters parameters = createTestParameters();

        List<PriceData> mockPriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100"))
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(mockPriceData);

        // 벤치마크 데이터 조회 실패 상황
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenThrow(new RuntimeException("데이터 조회 실패"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenThrow(new RuntimeException("데이터 조회 실패"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then - 벤치마크 실패시에도 시뮬레이션은 계속 진행
        assertThat(result).isNotNull();
        assertThat(result.getSp500Return()).isEqualTo(BigDecimal.ZERO); // 기본값
        assertThat(result.getNasdaqReturn()).isEqualTo(BigDecimal.ZERO); // 기본값
    }

    @Test
    void 파라미터가_null일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> dcaSimulationService.simulate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시뮬레이션 파라미터는 필수입니다");
    }

    @Test
    void 저장소가_null일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new DCASimulationService(null, benchmarkDataRepository))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PriceDataRepository는 필수입니다");

        assertThatThrownBy(() -> new DCASimulationService(priceDataRepository, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("BenchmarkDataRepository는 필수입니다");
    }

    @Test
    void 가격_변동이_심한_상황에서도_정확하게_계산해야_한다() {
        // given - 가격이 크게 오르고 내리는 상황
        DCASimulationParameters parameters = createTestParameters();

        List<PriceData> volatilePriceData = Arrays.asList(
            new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),  // 시작: 100원
            new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("200")),  // 2배 상승
            new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("50")),   // 75% 하락
            new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("300")),  // 6배 상승
            new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("150"))   // 50% 하락
        );

        when(priceDataRepository.findBySymbolAndDateRange(any(), any(), any()))
            .thenReturn(volatilePriceData);
        when(benchmarkDataRepository.calculateSP500Return(any(), any(), any()))
            .thenReturn(new BigDecimal("600000"));
        when(benchmarkDataRepository.calculateNASDAQReturn(any(), any(), any()))
            .thenReturn(new BigDecimal("750000"));

        // when
        DCASimulationResult result = dcaSimulationService.simulate(parameters);

        // then
        assertThat(result.getInvestmentRecords()).hasSize(5);
        assertThat(result.getTotalInvestmentAmount()).isEqualTo(new BigDecimal("500000"));

        // DCA 효과 확인: 변동성이 큰 상황에서도 안정적인 투자
        List<MonthlyInvestmentRecord> records = result.getInvestmentRecords();

        // 높은 가격일 때는 적게, 낮은 가격일 때는 많이 매수
        BigDecimal sharesAt200 = records.get(1).getSharesPurchased(); // 200원일 때: 500주
        BigDecimal sharesAt50 = records.get(2).getSharesPurchased();  // 50원일 때: 2000주

        assertThat(sharesAt50).isGreaterThan(sharesAt200); // 낮은 가격에 더 많이 매수
        assertThat(result.getFinalPortfolioValue()).isGreaterThan(BigDecimal.ZERO);
    }

    private DCASimulationParameters createTestParameters() {
        return new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 6, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );
    }
}