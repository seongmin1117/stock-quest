package com.stockquest.domain.simulation;

/**
 * TDD 검증용 임시 클래스 - InvestmentFrequency가 올바르게 작동하는지 확인
 */
public class InvestmentFrequencyValidator {

    public static void main(String[] args) {
        System.out.println("=== InvestmentFrequency TDD 검증 ===");

        // 테스트 1: 투자 주기 값들이 올바르게 정의되어야 한다
        System.out.println("테스트 1: 투자 주기 값들 확인");
        System.out.println("MONTHLY: " + InvestmentFrequency.MONTHLY);
        System.out.println("WEEKLY: " + InvestmentFrequency.WEEKLY);
        System.out.println("DAILY: " + InvestmentFrequency.DAILY);

        // 테스트 2: 월별 투자 주기의 일수가 올바르게 계산되어야 한다
        System.out.println("\n테스트 2: 월별 투자 주기 일수 확인");
        int monthlyDays = InvestmentFrequency.MONTHLY.getDaysPerPeriod();
        System.out.println("Monthly days per period: " + monthlyDays);
        assert monthlyDays == 30 : "월별 투자 주기는 30일이어야 함";

        // 테스트 3: 주별 투자 주기의 일수가 올바르게 계산되어야 한다
        System.out.println("\n테스트 3: 주별 투자 주기 일수 확인");
        int weeklyDays = InvestmentFrequency.WEEKLY.getDaysPerPeriod();
        System.out.println("Weekly days per period: " + weeklyDays);
        assert weeklyDays == 7 : "주별 투자 주기는 7일이어야 함";

        // 테스트 4: 일별 투자 주기의 일수가 올바르게 계산되어야 한다
        System.out.println("\n테스트 4: 일별 투자 주기 일수 확인");
        int dailyDays = InvestmentFrequency.DAILY.getDaysPerPeriod();
        System.out.println("Daily days per period: " + dailyDays);
        assert dailyDays == 1 : "일별 투자 주기는 1일이어야 함";

        // 테스트 5: 투자 주기 설명이 올바르게 반환되어야 한다
        System.out.println("\n테스트 5: 투자 주기 설명 확인");
        String monthlyDesc = InvestmentFrequency.MONTHLY.getDescription();
        String weeklyDesc = InvestmentFrequency.WEEKLY.getDescription();
        String dailyDesc = InvestmentFrequency.DAILY.getDescription();

        System.out.println("Monthly description: " + monthlyDesc);
        System.out.println("Weekly description: " + weeklyDesc);
        System.out.println("Daily description: " + dailyDesc);

        assert "월별".equals(monthlyDesc) : "월별 설명이 올바르지 않음";
        assert "주별".equals(weeklyDesc) : "주별 설명이 올바르지 않음";
        assert "일별".equals(dailyDesc) : "일별 설명이 올바르지 않음";

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("InvestmentFrequency Enum이 올바르게 구현되었습니다.");
    }
}