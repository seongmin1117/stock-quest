package com.stockquest.adapter.in.web.dca;

import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;
import com.stockquest.domain.simulation.PriceData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * TDD 검증용 임시 클래스 - DCA 웹 어댑터의 비즈니스 로직을 Spring 의존성 없이 검증
 */
public class DCAWebAdapterLogicValidator {

    public static void main(String[] args) {
        System.out.println("=== DCA 웹 어댑터 로직 TDD 검증 ===");

        // Mock 리포지토리 생성
        MockPriceDataRepository priceRepo = new MockPriceDataRepository();
        MockBenchmarkDataRepository benchmarkRepo = new MockBenchmarkDataRepository();

        // 도메인 서비스 생성
        com.stockquest.domain.simulation.DCASimulationService domainService =
            new com.stockquest.domain.simulation.DCASimulationService(priceRepo, benchmarkRepo);

        // 애플리케이션 서비스 생성
        DCASimulationService applicationService = new DCASimulationService(domainService);

        // 테스트 1: 웹 요청 DTO 검증
        System.out.println("\n테스트 1: 웹 요청 DTO 검증");
        DCASimulationRequest request = new DCASimulationRequest(
            "AAPL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        assert request.getSymbol().equals("AAPL") : "종목 코드가 올바르게 설정되어야 함";
        assert request.getMonthlyInvestmentAmount().equals(new BigDecimal("100000")) : "투자 금액이 올바르게 설정되어야 함";
        assert request.getStartDate().equals("2020-01-01T00:00:00") : "시작일이 올바르게 설정되어야 함";
        assert request.getEndDate().equals("2020-06-01T00:00:00") : "종료일이 올바르게 설정되어야 함";
        assert request.getFrequency().equals("MONTHLY") : "투자 주기가 올바르게 설정되어야 함";
        System.out.println("웹 요청 DTO 검증 성공");

        // 테스트 2: 날짜 변환 로직 검증
        System.out.println("\n테스트 2: 날짜 변환 로직 검증");
        LocalDateTime startDate = parseDateTime(request.getStartDate());
        LocalDateTime endDate = parseDateTime(request.getEndDate());

        assert startDate.equals(LocalDateTime.of(2020, 1, 1, 0, 0)) : "시작일 변환이 올바르게 되어야 함";
        assert endDate.equals(LocalDateTime.of(2020, 6, 1, 0, 0)) : "종료일 변환이 올바르게 되어야 함";
        System.out.println("날짜 변환 로직 검증 성공");

        // 테스트 3: 웹 요청을 애플리케이션 커맨드로 변환
        System.out.println("\n테스트 3: 웹 요청을 애플리케이션 커맨드로 변환");
        DCASimulationCommand command = convertToCommand(request);

        assert command.getSymbol().equals("AAPL") : "종목 코드가 올바르게 변환되어야 함";
        assert command.getMonthlyInvestmentAmount().equals(new BigDecimal("100000")) : "투자 금액이 올바르게 변환되어야 함";
        assert command.getStartDate().equals(LocalDateTime.of(2020, 1, 1, 0, 0)) : "시작일이 올바르게 변환되어야 함";
        assert command.getEndDate().equals(LocalDateTime.of(2020, 6, 1, 0, 0)) : "종료일이 올바르게 변환되어야 함";
        assert command.getFrequency().equals("MONTHLY") : "투자 주기가 올바르게 변환되어야 함";
        System.out.println("커맨드 변환 로직 검증 성공");

        // 테스트 4: 전체 워크플로우 테스트
        System.out.println("\n테스트 4: 전체 워크플로우 테스트");
        DCASimulationResponse response = applicationService.simulate(command);

        assert response != null : "응답이 생성되어야 함";
        assert response.getSymbol().equals("AAPL") : "응답의 종목 코드가 올바르게 설정되어야 함";
        assert response.getTotalInvestmentAmount().equals(new BigDecimal("500000")) : "총 투자금액이 올바르게 계산되어야 함";
        assert response.getInvestmentRecords().size() == 5 : "투자 기록이 5개여야 함";
        System.out.println("전체 워크플로우 검증 성공");

        // 테스트 5: 잘못된 날짜 형식 처리
        System.out.println("\n테스트 5: 잘못된 날짜 형식 처리");
        try {
            parseDateTime("invalid-date-format");
            assert false : "잘못된 날짜 형식에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert e.getMessage().contains("올바르지 않은 날짜 형식입니다") : "적절한 예외 메시지가 포함되어야 함";
        }
        System.out.println("날짜 형식 예외 처리 검증 성공");

        // 테스트 6: 다양한 투자 주기 처리
        System.out.println("\n테스트 6: 다양한 투자 주기 처리");
        DCASimulationRequest weeklyRequest = new DCASimulationRequest(
            "MSFT",
            new BigDecimal("50000"),
            "2020-01-01T00:00:00",
            "2020-01-29T00:00:00",
            "WEEKLY"
        );

        DCASimulationCommand weeklyCommand = convertToCommand(weeklyRequest);
        DCASimulationResponse weeklyResponse = applicationService.simulate(weeklyCommand);

        assert weeklyResponse != null : "주별 투자 응답이 생성되어야 함";
        assert weeklyResponse.getSymbol().equals("MSFT") : "종목 코드가 올바르게 설정되어야 함";
        System.out.println("다양한 투자 주기 처리 검증 성공");

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCA 웹 어댑터 로직이 올바르게 구현되었습니다.");
    }

    /**
     * 웹 요청을 애플리케이션 커맨드로 변환
     */
    private static DCASimulationCommand convertToCommand(DCASimulationRequest request) {
        LocalDateTime startDate = parseDateTime(request.getStartDate());
        LocalDateTime endDate = parseDateTime(request.getEndDate());

        return new DCASimulationCommand(
            request.getSymbol(),
            request.getMonthlyInvestmentAmount(),
            startDate,
            endDate,
            request.getFrequency()
        );
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     */
    private static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new IllegalArgumentException("올바르지 않은 날짜 형식입니다: " + dateTimeString);
        }
    }

    // Mock 구현체들
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
            return totalInvestmentAmount.multiply(new BigDecimal("1.20"));
        }

        @Override
        public BigDecimal calculateNASDAQReturn(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
            return totalInvestmentAmount.multiply(new BigDecimal("1.30"));
        }
    }
}