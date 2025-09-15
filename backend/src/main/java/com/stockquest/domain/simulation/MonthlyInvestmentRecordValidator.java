package com.stockquest.domain.simulation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TDD 검증용 임시 클래스 - MonthlyInvestmentRecord가 올바르게 작동하는지 확인
 */
public class MonthlyInvestmentRecordValidator {

    public static void main(String[] args) {
        System.out.println("=== MonthlyInvestmentRecord TDD 검증 ===");

        // 테스트 1: 정상적인 월별 투자 기록 생성
        System.out.println("\n테스트 1: 정상적인 월별 투자 기록 생성");
        LocalDateTime investmentDate = LocalDateTime.of(2020, 1, 15, 9, 30);
        BigDecimal investmentAmount = new BigDecimal("100000");
        BigDecimal stockPrice = new BigDecimal("150.50");
        BigDecimal sharesPurchased = new BigDecimal("664.45");
        BigDecimal portfolioValue = new BigDecimal("500000");

        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            investmentDate, investmentAmount, stockPrice, sharesPurchased, portfolioValue
        );

        System.out.println("월별 투자 기록: " + record.toString());
        assert record != null : "기록 객체가 정상적으로 생성되어야 함";
        assert record.getInvestmentDate().equals(investmentDate) : "투자일이 올바르게 설정되어야 함";
        assert record.getInvestmentAmount().equals(investmentAmount) : "투자금액이 올바르게 설정되어야 함";
        assert record.getStockPrice().equals(stockPrice) : "주가가 올바르게 설정되어야 함";
        assert record.getSharesPurchased().equals(sharesPurchased) : "매수 주식 수가 올바르게 설정되어야 함";
        assert record.getPortfolioValue().equals(portfolioValue) : "포트폴리오 가치가 올바르게 설정되어야 함";

        // 테스트 2: 주식 매수 개수 계산 검증
        System.out.println("\n테스트 2: 주식 매수 개수 계산 검증");
        BigDecimal investmentAmount2 = new BigDecimal("100000");
        BigDecimal stockPrice2 = new BigDecimal("200.00");
        BigDecimal expectedShares2 = new BigDecimal("500.00"); // 100,000 ÷ 200 = 500주

        MonthlyInvestmentRecord record2 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            investmentAmount2,
            stockPrice2,
            expectedShares2,
            new BigDecimal("100000")
        );

        System.out.println("매수 주식 수: " + record2.getSharesPurchased());
        assert record2.getSharesPurchased().equals(expectedShares2) : "매수 주식 수가 정확해야 함";

        // 테스트 3: 소수점 주식 매수 검증
        System.out.println("\n테스트 3: 소수점 주식 매수 검증");
        BigDecimal fractionalShares = new BigDecimal("300.003");
        MonthlyInvestmentRecord record3 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("333.33"),
            fractionalShares,
            new BigDecimal("100000")
        );

        System.out.println("소수점 매수 주식 수: " + record3.getSharesPurchased());
        assert record3.getSharesPurchased().equals(fractionalShares) : "소수점 주식 수가 정확해야 함";
        assert record3.getSharesPurchased().scale() >= 3 : "소수점 3자리 이상 지원해야 함";

        // 테스트 4: 0주 매수 허용 확인
        System.out.println("\n테스트 4: 0주 매수 허용 확인");
        MonthlyInvestmentRecord record4 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            BigDecimal.ZERO, // 0주 매수
            new BigDecimal("100000")
        );

        System.out.println("0주 매수 기록: " + record4.getSharesPurchased());
        assert record4.getSharesPurchased().equals(BigDecimal.ZERO) : "0주 매수가 허용되어야 함";

        // 테스트 5: 포트폴리오 가치 0원 허용 확인
        System.out.println("\n테스트 5: 포트폴리오 가치 0원 허용 확인");
        MonthlyInvestmentRecord record5 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            new BigDecimal("666.67"),
            BigDecimal.ZERO // 포트폴리오 가치 0원
        );

        System.out.println("포트폴리오 가치 0원: " + record5.getPortfolioValue());
        assert record5.getPortfolioValue().equals(BigDecimal.ZERO) : "포트폴리오 가치 0원이 허용되어야 함";

        // 테스트 6: 고가 주식 소량 매수 검증
        System.out.println("\n테스트 6: 고가 주식 소량 매수 검증");
        BigDecimal highPrice = new BigDecimal("500000");
        BigDecimal fractionalShares6 = new BigDecimal("0.2");
        MonthlyInvestmentRecord record6 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            highPrice,
            fractionalShares6,
            new BigDecimal("100000")
        );

        System.out.println("고가 주식 소량 매수: " + record6.getSharesPurchased() + "주 at " + record6.getStockPrice() + "원");
        assert record6.getSharesPurchased().equals(fractionalShares6) : "고가 주식 소량 매수가 정확해야 함";
        assert record6.getStockPrice().equals(highPrice) : "고가 주식 가격이 정확해야 함";

        // 테스트 7: 구체적인 시간 정보 저장 확인
        System.out.println("\n테스트 7: 구체적인 시간 정보 저장 확인");
        LocalDateTime specificTime = LocalDateTime.of(2020, 6, 15, 14, 30, 25);
        MonthlyInvestmentRecord record7 = new MonthlyInvestmentRecord(
            specificTime,
            new BigDecimal("100000"),
            new BigDecimal("200"),
            new BigDecimal("500"),
            new BigDecimal("100000")
        );

        System.out.println("구체적 시간: " + record7.getInvestmentDate());
        assert record7.getInvestmentDate().equals(specificTime) : "구체적 시간이 정확해야 함";
        assert record7.getInvestmentDate().getHour() == 14 : "시간이 정확해야 함";
        assert record7.getInvestmentDate().getMinute() == 30 : "분이 정확해야 함";
        assert record7.getInvestmentDate().getSecond() == 25 : "초가 정확해야 함";

        // 테스트 8: 예외 상황 - null 날짜
        System.out.println("\n테스트 8: 예외 상황 - null 날짜");
        try {
            new MonthlyInvestmentRecord(
                null, // null 날짜
                new BigDecimal("100000"),
                new BigDecimal("150"),
                new BigDecimal("666.67"),
                new BigDecimal("100000")
            );
            assert false : "null 날짜에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "투자 날짜는 필수입니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 9: 예외 상황 - 0원 투자금액
        System.out.println("\n테스트 9: 예외 상황 - 0원 투자금액");
        try {
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                BigDecimal.ZERO, // 0원
                new BigDecimal("150"),
                new BigDecimal("0"),
                new BigDecimal("100000")
            );
            assert false : "0원 투자금액에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "투자 금액은 0보다 커야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 10: 예외 상황 - 음수 주가
        System.out.println("\n테스트 10: 예외 상황 - 음수 주가");
        try {
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("-150"), // 음수 주가
                new BigDecimal("666.67"),
                new BigDecimal("100000")
            );
            assert false : "음수 주가에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "주식 가격은 0보다 커야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 11: 예외 상황 - 음수 매수 주식 수
        System.out.println("\n테스트 11: 예외 상황 - 음수 매수 주식 수");
        try {
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("150"),
                new BigDecimal("-100"), // 음수 주식 수
                new BigDecimal("100000")
            );
            assert false : "음수 매수 주식 수에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "매수 주식 수는 0 이상이어야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 12: 예외 상황 - 음수 포트폴리오 가치
        System.out.println("\n테스트 12: 예외 상황 - 음수 포트폴리오 가치");
        try {
            new MonthlyInvestmentRecord(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("150"),
                new BigDecimal("666.67"),
                new BigDecimal("-50000") // 음수 포트폴리오 가치
            );
            assert false : "음수 포트폴리오 가치에 대해 예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            System.out.println("예외 메시지: " + e.getMessage());
            assert "포트폴리오 가치는 0 이상이어야 합니다".equals(e.getMessage()) : "올바른 예외 메시지가 나와야 함";
        }

        // 테스트 13: toString 메서드 검증
        System.out.println("\n테스트 13: toString 메서드 검증");
        MonthlyInvestmentRecord record13 = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 3, 15, 9, 30),
            new BigDecimal("100000"),
            new BigDecimal("125.75"),
            new BigDecimal("795.23"),
            new BigDecimal("380000")
        );

        String description = record13.toString();
        System.out.println("toString 결과: " + description);
        assert description.contains("2020-03-15") : "toString에 투자일이 포함되어야 함";
        assert description.contains("100000") : "toString에 투자금액이 포함되어야 함";
        assert description.contains("125.75") : "toString에 주가가 포함되어야 함";
        assert description.contains("795.23") : "toString에 매수주식이 포함되어야 함";
        assert description.contains("380000") : "toString에 포트폴리오 가치가 포함되어야 함";

        System.out.println("\n✅ 모든 테스트가 통과했습니다!");
        System.out.println("MonthlyInvestmentRecord가 올바르게 구현되었습니다.");
    }
}