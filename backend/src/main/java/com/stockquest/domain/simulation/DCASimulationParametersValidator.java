package com.stockquest.domain.simulation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TDD 검증용 임시 클래스 - DCASimulationParameters가 올바르게 작동하는지 확인
 */
public class DCASimulationParametersValidator {

    public static void main(String[] args) {
        System.out.println("=== DCASimulationParameters TDD 검증 ===");

        // 테스트 1: 정상적인 파라미터 생성
        System.out.println("\n테스트 1: 정상적인 파라미터 생성");
        DCASimulationParameters parameters = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("100000"),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.MONTHLY
        );
        System.out.println("파라미터 생성: " + parameters.toString());
        assert parameters != null : "파라미터가 정상적으로 생성되어야 함";
        assert "AAPL".equals(parameters.getSymbol()) : "심볼이 올바르게 설정되어야 함";

        // 테스트 2: 투자 기간 계산
        System.out.println("\n테스트 2: 투자 기간 계산");
        double years = parameters.getInvestmentPeriodInYears();
        System.out.println("투자 기간: " + years + "년");
        assert Math.abs(years - 5.0) < 0.1 : "5년 투자 기간이 올바르게 계산되어야 함";

        // 테스트 3: 총 투자 횟수 계산 (월별)
        System.out.println("\n테스트 3: 총 투자 횟수 계산 (월별)");
        int totalCount = parameters.getTotalInvestmentCount();
        System.out.println("총 투자 횟수: " + totalCount + "회");
        assert totalCount == 60 : "5년 × 12개월 = 60회가 되어야 함";

        // 테스트 4: 총 원금 계산
        System.out.println("\n테스트 4: 총 원금 계산");
        BigDecimal totalPrincipal = parameters.getTotalPrincipal();
        System.out.println("총 투자 원금: " + totalPrincipal + "원");
        assert totalPrincipal.equals(new BigDecimal("6000000")) : "100,000 × 60 = 6,000,000원이 되어야 함";

        // 테스트 5: 주별 투자 테스트
        System.out.println("\n테스트 5: 주별 투자 테스트");
        DCASimulationParameters weeklyParams = new DCASimulationParameters(
            "AAPL",
            new BigDecimal("50000"),
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            InvestmentFrequency.WEEKLY
        );
        int weeklyCount = weeklyParams.getTotalInvestmentCount();
        System.out.println("주별 투자 횟수: " + weeklyCount + "회");
        assert weeklyCount == 52 : "1년 ≈ 52주가 되어야 함";

        // 테스트 6: 예외 상황 테스트 - 음수 금액
        System.out.println("\n테스트 6: 예외 상황 테스트 - 음수 금액");
        try {
            new DCASimulationParameters(
                "AAPL",
                new BigDecimal("-100000"),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                InvestmentFrequency.MONTHLY
            );
            assert false : "음수 금액에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "투자 금액은 0보다 커야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 7: 예외 상황 테스트 - 잘못된 날짜 순서
        System.out.println("\n테스트 7: 예외 상황 테스트 - 잘못된 날짜 순서");
        try {
            new DCASimulationParameters(
                "AAPL",
                new BigDecimal("100000"),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                InvestmentFrequency.MONTHLY
            );
            assert false : "잘못된 날짜 순서에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "시작일은 종료일보다 빨라야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 8: 예외 상황 테스트 - null 심볼
        System.out.println("\n테스트 8: 예외 상황 테스트 - null 심볼");
        try {
            new DCASimulationParameters(
                null,
                new BigDecimal("100000"),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                InvestmentFrequency.MONTHLY
            );
            assert false : "null 심볼에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "심볼은 필수입니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("DCASimulationParameters가 올바르게 구현되었습니다.");
    }
}