package com.stockquest.application.dca;

import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import com.stockquest.domain.simulation.DCASimulationParameters;
import com.stockquest.domain.simulation.DCASimulationResult;
import com.stockquest.domain.simulation.DCASimulationService;
import com.stockquest.domain.simulation.InvestmentFrequency;
import com.stockquest.domain.simulation.MonthlyInvestmentRecord;
import com.stockquest.domain.simulation.PriceData;
import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * TDD 검증용 임시 클래스 - DCA 애플리케이션 서비스가 올바르게 작동하는지 확인
 */
public class DCAApplicationServiceValidator {

    public static void main(String[] args) {
        System.out.println("=== DCA 애플리케이션 서비스 TDD 검증 ===");

        // Mock 리포지토리 생성
        MockPriceDataRepository priceRepo = new MockPriceDataRepository();
        MockBenchmarkDataRepository benchmarkRepo = new MockBenchmarkDataRepository();

        // 도메인 서비스 생성
        DCASimulationService domainService = new DCASimulationService(priceRepo, benchmarkRepo);

        // 애플리케이션 서비스 생성
        com.stockquest.application.dca.DCASimulationService applicationService =
            new com.stockquest.application.dca.DCASimulationService(domainService);

        // 테스트 1: 정상적인 시뮬레이션 요청 처리
        System.out.println("\n테스트 1: 정상적인 시뮬레이션 요청 처리");
        DCASimulationCommand command = new DCASimulationCommand(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 6, 1, 0, 0),
            "MONTHLY"
        );

        DCASimulationResponse response = applicationService.simulate(command);
        System.out.println("응답 생성 성공: " + (response != null));
        assert response != null : "응답이 생성되어야 함";
        assert response.getSymbol().equals("AAPL") : "종목 코드가 올바르게 설정되어야 함";
        assert response.getTotalInvestmentAmount().equals(new BigDecimal("500000")) : "총 투자금액이 올바르게 계산되어야 함";
        assert response.getInvestmentRecords().size() == 5 : "투자 기록이 5개여야 함";

        // 테스트 2: 다양한 투자 주기 처리
        System.out.println("\n테스트 2: 다양한 투자 주기 처리");
        DCASimulationCommand weeklyCommand = new DCASimulationCommand(
            "MSFT",
            new BigDecimal("50000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 29, 0, 0),
            "WEEKLY"
        );

        DCASimulationResponse weeklyResponse = applicationService.simulate(weeklyCommand);
        System.out.println("주별 투자 응답 생성 성공: " + (weeklyResponse != null));
        assert weeklyResponse != null : "주별 투자 응답이 생성되어야 함";
        assert weeklyResponse.getSymbol().equals("MSFT") : "종목 코드가 올바르게 설정되어야 함";

        // 테스트 3: 잘못된 투자 주기 처리
        System.out.println("\n테스트 3: 잘못된 투자 주기 처리");
        try {
            DCASimulationCommand invalidCommand = new DCASimulationCommand(
                "TSLA",
                new BigDecimal("100000"),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2020, 6, 1, 0, 0),
                "INVALID"
            );
            applicationService.simulate(invalidCommand);
            assert false : "잘못된 투자 주기에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "지원하지 않는 투자 주기입니다: INVALID".equals(e.getMessage());
        }

        // 테스트 4: null 커맨드 처리
        System.out.println("\n테스트 4: null 커맨드 처리");
        try {
            applicationService.simulate(null);
            assert false : "null 커맨드에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "시뮬레이션 요청은 필수입니다".equals(e.getMessage());
        }

        // 테스트 5: 벤치마크 대비 성과 계산 확인
        System.out.println("\n테스트 5: 벤치마크 대비 성과 계산 확인");
        DCASimulationResponse performanceResponse = applicationService.simulate(command);
        System.out.println("S&P 500 대비 성과: " + performanceResponse.getOutperformanceVsSP500() + "%");
        System.out.println("NASDAQ 대비 성과: " + performanceResponse.getOutperformanceVsNASDAQ() + "%");
        assert performanceResponse.getOutperformanceVsSP500() != null : "S&P 500 대비 성과가 계산되어야 함";
        assert performanceResponse.getOutperformanceVsNASDAQ() != null : "NASDAQ 대비 성과가 계산되어야 함";

        // 테스트 6: 투자 기록 DTO 변환 확인
        System.out.println("\n테스트 6: 투자 기록 DTO 변환 확인");
        DCASimulationResponse.MonthlyInvestmentRecordDto firstRecord = performanceResponse.getInvestmentRecords().get(0);
        assert firstRecord.getInvestmentDate() != null : "투자일이 설정되어야 함";
        assert firstRecord.getInvestmentAmount().equals(new BigDecimal("100000")) : "투자금액이 올바르게 설정되어야 함";
        assert firstRecord.getStockPrice().compareTo(BigDecimal.ZERO) > 0 : "주가가 0보다 커야 함";
        assert firstRecord.getSharesPurchased().compareTo(BigDecimal.ZERO) > 0 : "매수 주식 수가 0보다 커야 함";
        assert firstRecord.getPortfolioValue().compareTo(BigDecimal.ZERO) > 0 : "포트폴리오 가치가 0보다 커야 함";

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCA 애플리케이션 서비스가 올바르게 구현되었습니다.");
    }

    // Mock 구현체들 (도메인 서비스 검증에서 재사용)
    static class MockPriceDataRepository implements PriceDataRepository {
        @Override
        public List<PriceData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
            return Arrays.asList(
                new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
                new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
                new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("90")),
                new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("120")),
                new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("130")),
                new PriceData(LocalDateTime.of(2020, 1, 8, 0, 0), new BigDecimal("105")),
                new PriceData(LocalDateTime.of(2020, 1, 15, 0, 0), new BigDecimal("108")),
                new PriceData(LocalDateTime.of(2020, 1, 22, 0, 0), new BigDecimal("102"))
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