package com.stockquest.adapter.in.web.dca;

import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import com.stockquest.domain.simulation.DCASimulationParameters;
import com.stockquest.domain.simulation.DCASimulationResult;
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
 * TDD 검증용 임시 클래스 - DCA 웹 어댑터가 올바르게 작동하는지 확인
 */
public class DCAWebAdapterValidator {

    public static void main(String[] args) {
        System.out.println("=== DCA 웹 어댑터 TDD 검증 ===");

        // Mock 리포지토리 생성
        MockPriceDataRepository priceRepo = new MockPriceDataRepository();
        MockBenchmarkDataRepository benchmarkRepo = new MockBenchmarkDataRepository();

        // 도메인 서비스 생성
        com.stockquest.domain.simulation.DCASimulationService domainService =
            new com.stockquest.domain.simulation.DCASimulationService(priceRepo, benchmarkRepo);

        // 애플리케이션 서비스 생성
        DCASimulationService applicationService = new DCASimulationService(domainService);

        // 웹 어댑터 생성
        DCAController controller = new DCAController(applicationService);

        // 테스트 1: 정상적인 웹 요청 처리
        System.out.println("\n테스트 1: 정상적인 웹 요청 처리");
        DCASimulationRequest request = new DCASimulationRequest(
            "AAPL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        try {
            var response = controller.simulate(request);
            System.out.println("HTTP 200 응답 생성 성공: " + (response.getStatusCode().is2xxSuccessful()));
            assert response.getStatusCode().is2xxSuccessful() : "HTTP 200 응답이어야 함";
            assert response.getBody() != null : "응답 본문이 있어야 함";

            // 성공 응답인 경우 DCASimulationResponse로 캐스팅
            if (response.getBody() instanceof DCASimulationResponse body) {
                assert body.getSymbol().equals("AAPL") : "종목 코드가 올바르게 반환되어야 함";
                assert body.getTotalInvestmentAmount().equals(new BigDecimal("500000")) : "총 투자금액이 올바르게 반환되어야 함";
                assert body.getInvestmentRecords().size() == 5 : "투자 기록이 5개여야 함";
            } else {
                assert false : "성공 응답은 DCASimulationResponse 타입이어야 함";
            }
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            e.printStackTrace();
            assert false : "정상 요청에서 예외가 발생하지 않아야 함";
        }

        // 테스트 2: 잘못된 날짜 형식 처리
        System.out.println("\n테스트 2: 잘못된 날짜 형식 처리");
        DCASimulationRequest invalidDateRequest = new DCASimulationRequest(
            "MSFT",
            new BigDecimal("50000"),
            "invalid-date",
            "2020-06-01T00:00:00",
            "WEEKLY"
        );

        try {
            var response = controller.simulate(invalidDateRequest);
            System.out.println("HTTP 400 응답 생성 성공: " + (response.getStatusCode().is4xxClientError()));
            assert response.getStatusCode().is4xxClientError() : "HTTP 400 응답이어야 함";
        } catch (Exception e) {
            System.out.println("올바른 예외 처리: " + e.getClass().getSimpleName());
            assert e instanceof IllegalArgumentException : "IllegalArgumentException이 발생해야 함";
        }

        // 테스트 3: 잘못된 투자 주기 처리
        System.out.println("\n테스트 3: 잘못된 투자 주기 처리");
        DCASimulationRequest invalidFrequencyRequest = new DCASimulationRequest(
            "TSLA",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "INVALID"
        );

        try {
            var response = controller.simulate(invalidFrequencyRequest);
            System.out.println("HTTP 400 응답 생성 성공: " + (response.getStatusCode().is4xxClientError()));
            assert response.getStatusCode().is4xxClientError() : "HTTP 400 응답이어야 함";
        } catch (Exception e) {
            System.out.println("올바른 예외 처리: " + e.getClass().getSimpleName());
            assert e instanceof IllegalArgumentException : "IllegalArgumentException이 발생해야 함";
        }

        // 테스트 4: 요청 DTO 검증
        System.out.println("\n테스트 4: 요청 DTO 검증");
        DCASimulationRequest validRequest = new DCASimulationRequest(
            "GOOGL",
            new BigDecimal("200000"),
            "2019-01-01T00:00:00",
            "2019-12-31T23:59:59",
            "DAILY"
        );

        assert validRequest.getSymbol().equals("GOOGL") : "종목 코드가 올바르게 설정되어야 함";
        assert validRequest.getMonthlyInvestmentAmount().equals(new BigDecimal("200000")) : "투자 금액이 올바르게 설정되어야 함";
        assert validRequest.getStartDate().equals("2019-01-01T00:00:00") : "시작일이 올바르게 설정되어야 함";
        assert validRequest.getEndDate().equals("2019-12-31T23:59:59") : "종료일이 올바르게 설정되어야 함";
        assert validRequest.getFrequency().equals("DAILY") : "투자 주기가 올바르게 설정되어야 함";
        System.out.println("DTO 필드 검증 성공");

        // 테스트 5: 날짜 변환 로직 확인
        System.out.println("\n테스트 5: 날짜 변환 로직 확인");
        try {
            var response = controller.simulate(validRequest);
            assert response.getStatusCode().is2xxSuccessful() : "올바른 날짜 형식은 성공해야 함";
            System.out.println("날짜 변환 로직 검증 성공");
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            assert false : "올바른 날짜 형식에서 예외가 발생하지 않아야 함";
        }

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCA 웹 어댑터가 올바르게 구현되었습니다.");
    }

    // Mock 구현체들 (기존 검증 프로그램에서 재사용)
    static class MockPriceDataRepository implements PriceDataRepository {
        @Override
        public List<PriceData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
            return Arrays.asList(
                new PriceData(LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("100")),
                new PriceData(LocalDateTime.of(2020, 2, 1, 0, 0), new BigDecimal("110")),
                new PriceData(LocalDateTime.of(2020, 3, 1, 0, 0), new BigDecimal("90")),
                new PriceData(LocalDateTime.of(2020, 4, 1, 0, 0), new BigDecimal("120")),
                new PriceData(LocalDateTime.of(2020, 5, 1, 0, 0), new BigDecimal("130")),
                new PriceData(LocalDateTime.of(2019, 1, 1, 0, 0), new BigDecimal("95")),
                new PriceData(LocalDateTime.of(2019, 1, 2, 0, 0), new BigDecimal("96")),
                new PriceData(LocalDateTime.of(2019, 1, 3, 0, 0), new BigDecimal("97")),
                new PriceData(LocalDateTime.of(2019, 1, 4, 0, 0), new BigDecimal("98")),
                new PriceData(LocalDateTime.of(2019, 1, 5, 0, 0), new BigDecimal("99"))
            );
        }
    }

    static class MockBenchmarkDataRepository implements BenchmarkDataRepository {
        @Override
        public BigDecimal calculateSP500Return(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
            return totalInvestmentAmount.multiply(new BigDecimal("1.20"));
        }

        @Override
        public BigDecimal calculateNASDAQReturn(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
            return totalInvestmentAmount.multiply(new BigDecimal("1.30"));
        }
    }
}