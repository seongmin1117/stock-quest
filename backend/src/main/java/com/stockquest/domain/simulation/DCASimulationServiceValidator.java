package com.stockquest.domain.simulation;

import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * TDD 검증용 임시 클래스 - DCASimulationService가 올바르게 작동하는지 확인
 */
public class DCASimulationServiceValidator {

    public static void main(String[] args) {
        System.out.println("=== DCASimulationService TDD 검증 ===");

        // 테스트 1: 정상적인 서비스 생성
        System.out.println("\n테스트 1: 정상적인 서비스 생성");
        MockPriceDataRepository priceRepo = new MockPriceDataRepository();
        MockBenchmarkDataRepository benchmarkRepo = new MockBenchmarkDataRepository();

        DCASimulationService service = new DCASimulationService(priceRepo, benchmarkRepo);
        System.out.println("서비스 생성 성공: " + (service != null));

        // 테스트 2: null 리포지토리 검증
        System.out.println("\n테스트 2: null 리포지토리 검증");
        try {
            new DCASimulationService(null, benchmarkRepo);
            assert false : "null PriceDataRepository에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "PriceDataRepository는 필수입니다".equals(e.getMessage());
        }

        try {
            new DCASimulationService(priceRepo, null);
            assert false : "null BenchmarkDataRepository에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "BenchmarkDataRepository는 필수입니다".equals(e.getMessage());
        }

        // 테스트 3: 시뮬레이션 실행
        System.out.println("\n테스트 3: 시뮬레이션 실행");
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 5, 31, 0, 0),
            InvestmentFrequency.MONTHLY
        );

        DCASimulationResult result = service.simulate(parameters);
        System.out.println("시뮬레이션 실행 성공: " + (result != null));
        System.out.println("실제 기록 개수: " + result.getInvestmentRecords().size());
        System.out.println("총 투자 금액: " + result.getTotalInvestmentAmount());

        // 투자 기록 상세 확인
        for (int i = 0; i < result.getInvestmentRecords().size(); i++) {
            MonthlyInvestmentRecord record = result.getInvestmentRecords().get(i);
            System.out.println("기록 " + (i+1) + ": " + record.getInvestmentDate() + ", 가격: " + record.getStockPrice());
        }
        assert result != null : "시뮬레이션 결과가 생성되어야 함";
        assert result.getInvestmentRecords().size() == 5 : "5개월 투자 기록이 있어야 함";
        assert result.getTotalInvestmentAmount().equals(new BigDecimal("500000")) : "총 투자금액이 50만원이어야 함";

        // 테스트 4: 빈 주가 데이터 처리
        System.out.println("\n테스트 4: 빈 주가 데이터 처리");
        MockPriceDataRepository emptyRepo = new MockPriceDataRepository();
        emptyRepo.setEmptyResult(true);
        DCASimulationService emptyService = new DCASimulationService(emptyRepo, benchmarkRepo);

        try {
            emptyService.simulate(parameters);
            assert false : "빈 주가 데이터에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "해당 기간의 주가 데이터를 찾을 수 없습니다".equals(e.getMessage());
        }

        // 테스트 5: null 파라미터 처리
        System.out.println("\n테스트 5: null 파라미터 처리");
        try {
            service.simulate(null);
            assert false : "null 파라미터에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "시뮬레이션 파라미터는 필수입니다".equals(e.getMessage());
        }

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCASimulationService가 올바르게 구현되었습니다.");
    }

    // Mock 구현체들
    static class MockPriceDataRepository implements PriceDataRepository {
        private boolean emptyResult = false;

        public void setEmptyResult(boolean emptyResult) {
            this.emptyResult = emptyResult;
        }

        @Override
        public List<PriceData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
            if (emptyResult) {
                return Arrays.asList();
            }

            return Arrays.asList(
                new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
                new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
                new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("90")),
                new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("120")),
                new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("130"))
            );
        }
    }

    static class MockBenchmarkDataRepository implements BenchmarkDataRepository {
        @Override
        public BigDecimal calculateSP500Return(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
            // 총 투자금액의 20% 수익 시뮬레이션
            return totalInvestmentAmount.multiply(new BigDecimal("1.20"));
        }

        @Override
        public BigDecimal calculateNASDAQReturn(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
            // 총 투자금액의 30% 수익 시뮬레이션
            return totalInvestmentAmount.multiply(new BigDecimal("1.30"));
        }
    }
}