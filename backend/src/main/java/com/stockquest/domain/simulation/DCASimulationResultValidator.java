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

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCASimulationResult가 올바르게 구현되었습니다.");
    }
}