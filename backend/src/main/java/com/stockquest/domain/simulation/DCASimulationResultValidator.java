package com.stockquest.domain.simulation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * TDD 검증용 임시 클래스 - DCASimulationResult가 올바르게 작동하는지 확인
 */
public class DCASimulationResultValidator {

    public static void main(String[] args) {
        System.out.println("=== DCASimulationResult TDD 검증 ===");

        // 테스트 데이터 준비
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );

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
                new BigDecimal("110"),
                new BigDecimal("909.09"),
                new BigDecimal("200000")
            )
        );

        // 테스트 1: 정상적인 결과 생성
        System.out.println("\n테스트 1: 정상적인 결과 생성");
        DCASimulationResult result = new DCASimulationResult(
            parameters,
            new BigDecimal("6000000"), // 총 투자금
            new BigDecimal("8500000"), // 최종 가치
            records,
            new BigDecimal("7200000"), // S&P 500
            new BigDecimal("9100000")  // NASDAQ
        );
        System.out.println("결과 객체 생성 성공");
        assert result != null : "결과 객체가 정상적으로 생성되어야 함";

        // 테스트 2: 총 수익률 계산
        System.out.println("\n테스트 2: 총 수익률 계산");
        BigDecimal totalReturn = result.getTotalReturnPercentage();
        System.out.println("총 수익률: " + totalReturn + "%");
        // (8500000 - 6000000) / 6000000 * 100 = 41.67%
        assert Math.abs(totalReturn.doubleValue() - 41.67) < 0.01 : "총 수익률이 약 41.67%가 되어야 함";

        // 테스트 3: 연평균 수익률 계산
        System.out.println("\n테스트 3: 연평균 수익률 계산");
        BigDecimal annualReturn = result.getAnnualizedReturn();
        System.out.println("연평균 수익률: " + annualReturn + "%");
        assert Math.abs(annualReturn.doubleValue() - 7.25) < 0.1 : "연평균 수익률이 약 7.25%가 되어야 함";

        // 테스트 4: S&P 500 대비 성과
        System.out.println("\n테스트 4: S&P 500 대비 성과");
        BigDecimal sp500Performance = result.getOutperformanceVsSP500();
        System.out.println("S&P 500 대비: " + sp500Performance + "%");
        // AAPL: 41.67%, S&P 500: (7200000-6000000)/6000000*100 = 20%
        // 차이: 41.67% - 20% = 21.67%
        assert Math.abs(sp500Performance.doubleValue() - 21.67) < 0.01 : "S&P 500 대비 약 21.67% 초과 성과여야 함";

        // 테스트 5: NASDAQ 대비 성과
        System.out.println("\n테스트 5: NASDAQ 대비 성과");
        BigDecimal nasdaqPerformance = result.getOutperformanceVsNASDAQ();
        System.out.println("NASDAQ 대비: " + nasdaqPerformance + "%");
        // NASDAQ: (9100000-6000000)/6000000*100 = 51.67%
        // 차이: 41.67% - 51.67% = -10.00%
        assert Math.abs(nasdaqPerformance.doubleValue() - (-10.0)) < 0.5 : "NASDAQ 대비 약 -10% 성과여야 함";

        // 테스트 6: 최고 포트폴리오 가치
        System.out.println("\n테스트 6: 최고 포트폴리오 가치");
        BigDecimal maxValue = result.getMaxPortfolioValue();
        System.out.println("최고 포트폴리오 가치: " + maxValue + "원");
        assert maxValue.equals(new BigDecimal("200000")) : "최고 가치는 200000원이어야 함";

        // 테스트 7: 결과 요약 정보
        System.out.println("\n테스트 7: 결과 요약 정보");
        String summary = result.getSummary();
        System.out.println(summary);
        assert summary.contains("AAPL") : "요약에 종목명이 포함되어야 함";
        assert summary.contains("6,000,000") : "요약에 투자 원금이 포함되어야 함";
        assert summary.contains("8,500,000") : "요약에 최종 가치가 포함되어야 함";

        // 테스트 8: 예외 상황 - null 투자금액
        System.out.println("\n테스트 8: 예외 상황 - null 투자금액");
        try {
            new DCASimulationResult(
                parameters,
                null, // null 투자금액
                new BigDecimal("8500000"),
                records,
                new BigDecimal("7200000"),
                new BigDecimal("9100000")
            );
            assert false : "null 투자금액에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "투자 금액은 필수입니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 9: 예외 상황 - 음수 포트폴리오 가치
        System.out.println("\n테스트 9: 예외 상황 - 음수 포트폴리오 가치");
        try {
            new DCASimulationResult(
                parameters,
                new BigDecimal("6000000"),
                new BigDecimal("-1000"), // 음수 가치
                records,
                new BigDecimal("7200000"),
                new BigDecimal("9100000")
            );
            assert false : "음수 포트폴리오 가치에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "포트폴리오 가치는 0 이상이어야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 10: 짧은 투자 기간 (3개월) - Edge Case
        System.out.println("\n테스트 10: 짧은 투자 기간 (3개월) - Edge Case");
        testShortPeriodInvestment();

        // 테스트 11: 매우 짧은 투자 기간 (1주) - Edge Case
        System.out.println("\n테스트 11: 매우 짧은 투자 기간 (1주) - Edge Case");
        testVeryShortPeriodInvestment();

        // 테스트 12: 손실 상황에서의 계산
        System.out.println("\n테스트 12: 손실 상황에서의 계산");
        testLossScenario();

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCASimulationResult가 올바르게 구현되었습니다.");
    }

    private static void testShortPeriodInvestment() {
        // 3개월 투자 - 이전에 NaN 오류가 발생했던 케이스
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 4, 1, 0, 0), // 3개월
            InvestmentFrequency.MONTHLY
        );

        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("110000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("105"),
                new BigDecimal("952.38"),
                new BigDecimal("225000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 3, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("110"),
                new BigDecimal("909.09"),
                new BigDecimal("330000")
            )
        );

        DCASimulationResult result = new DCASimulationResult(
            parameters,
            new BigDecimal("300000"), // 총 투자금
            new BigDecimal("330000"), // 최종 가치 (10% 수익)
            records,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

        BigDecimal totalReturn = result.getTotalReturnPercentage();
        BigDecimal annualizedReturn = result.getAnnualizedReturn();

        System.out.println("투자기간: " + parameters.getInvestmentPeriodInYears() + "년");
        System.out.println("총 수익률: " + totalReturn + "%");
        System.out.println("연평균 수익률: " + annualizedReturn + "%");

        assert totalReturn.equals(new BigDecimal("10.00")) : "총 수익률이 10%가 되어야 함";
        assert !annualizedReturn.toString().contains("NaN") : "연평균 수익률이 NaN이 되면 안됨";
        assert !annualizedReturn.toString().contains("Infinity") : "연평균 수익률이 Infinity가 되면 안됨";
        // 3개월 (0.249년) 투자의 연평균 수익률: (1.10)^(1/0.249) - 1 = 약 46.60%
        assert Math.abs(annualizedReturn.doubleValue() - 46.60) < 1.0 : "3개월 10% 수익의 연평균 수익률은 약 46.60%가 되어야 함";
    }

    private static void testVeryShortPeriodInvestment() {
        // 1주 투자 - 매우 짧은 기간
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 8, 0, 0), // 1주
            InvestmentFrequency.WEEKLY
        );

        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("105000")
            )
        );

        DCASimulationResult result = new DCASimulationResult(
            parameters,
            new BigDecimal("100000"), // 총 투자금
            new BigDecimal("105000"), // 최종 가치 (5% 수익)
            records,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

        BigDecimal totalReturn = result.getTotalReturnPercentage();
        BigDecimal annualizedReturn = result.getAnnualizedReturn();

        System.out.println("투자기간: " + parameters.getInvestmentPeriodInYears() + "년");
        System.out.println("총 수익률: " + totalReturn + "%");
        System.out.println("연평균 수익률: " + annualizedReturn + "%");

        assert totalReturn.equals(new BigDecimal("5.00")) : "총 수익률이 5%가 되어야 함";
        assert !annualizedReturn.toString().contains("NaN") : "연평균 수익률이 NaN이 되면 안됨";
        assert !annualizedReturn.toString().contains("Infinity") : "연평균 수익률이 Infinity가 되면 안됨";
        // 1주 (0.0192년) 5% 수익의 연평균: (1.05)^(1/0.0192) - 1 = 약 1175%
        assert Math.abs(annualizedReturn.doubleValue() - 1175.34) < 50.0 : "1주 5% 수익의 연평균 수익률은 약 1175%가 되어야 함";
    }

    private static void testLossScenario() {
        // 손실 상황에서의 계산
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2020, 7, 1, 0, 0), // 6개월
            InvestmentFrequency.MONTHLY
        );

        List<MonthlyInvestmentRecord> records = Arrays.asList(
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("90000")
            ),
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 6, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("80"),
                new BigDecimal("1250.00"),
                new BigDecimal("480000")
            )
        );

        DCASimulationResult result = new DCASimulationResult(
            parameters,
            new BigDecimal("600000"), // 총 투자금
            new BigDecimal("480000"), // 최종 가치 (20% 손실)
            records,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

        BigDecimal totalReturn = result.getTotalReturnPercentage();
        BigDecimal annualizedReturn = result.getAnnualizedReturn();

        System.out.println("투자기간: " + parameters.getInvestmentPeriodInYears() + "년");
        System.out.println("총 수익률: " + totalReturn + "%");
        System.out.println("연평균 수익률: " + annualizedReturn + "%");

        assert totalReturn.equals(new BigDecimal("-20.00")) : "총 수익률이 -20%가 되어야 함";
        assert !annualizedReturn.toString().contains("NaN") : "연평균 수익률이 NaN이 되면 안됨";
        assert !annualizedReturn.toString().contains("Infinity") : "연평균 수익률이 Infinity가 되면 안됨";
        // 6개월 (0.5년) -20% 손실의 연평균: (0.80)^(1/0.5) - 1 = 약 -36%
        assert Math.abs(annualizedReturn.doubleValue() - (-36.0)) < 5.0 : "6개월 -20% 손실의 연평균 수익률은 약 -36%가 되어야 함";
    }
}