package com.stockquest.domain.simulation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD: RED - MonthlyInvestmentRecord 도메인 엔티티 테스트
 * 월별 투자 기록의 동작을 검증
 */
class MonthlyInvestmentRecordTest {

    @Test
    void 월별_투자_기록이_올바르게_생성되어야_한다() {
        // given
        LocalDateTime investmentDate = LocalDateTime.of(2020, 1, 15, 9, 30);
        BigDecimal investmentAmount = new BigDecimal("100000");
        BigDecimal stockPrice = new BigDecimal("150.50");
        BigDecimal sharesPurchased = new BigDecimal("664.45");
        BigDecimal portfolioValue = new BigDecimal("500000");

        // when
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            investmentDate, investmentAmount, stockPrice, sharesPurchased, portfolioValue
        );

        // then
        assertThat(record).isNotNull();
        assertThat(record.getInvestmentDate()).isEqualTo(investmentDate);
        assertThat(record.getInvestmentAmount()).isEqualTo(investmentAmount);
        assertThat(record.getStockPrice()).isEqualTo(stockPrice);
        assertThat(record.getSharesPurchased()).isEqualTo(sharesPurchased);
        assertThat(record.getPortfolioValue()).isEqualTo(portfolioValue);
    }

    @Test
    void 주식_매수_개수_계산이_정확해야_한다() {
        // given - 100,000원으로 주가 200원인 주식 매수
        BigDecimal investmentAmount = new BigDecimal("100000");
        BigDecimal stockPrice = new BigDecimal("200.00");
        BigDecimal expectedShares = new BigDecimal("500.00"); // 100,000 ÷ 200 = 500주

        // when
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            investmentAmount,
            stockPrice,
            expectedShares,
            new BigDecimal("100000")
        );

        // then
        assertThat(record.getSharesPurchased()).isEqualTo(expectedShares);
    }

    @Test
    void 소수점_주식_매수도_정확하게_기록되어야_한다() {
        // given - 100,000원으로 주가 333.33원인 주식 매수
        BigDecimal investmentAmount = new BigDecimal("100000");
        BigDecimal stockPrice = new BigDecimal("333.33");
        BigDecimal expectedShares = new BigDecimal("300.003"); // 약 300.003주

        // when
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            investmentAmount,
            stockPrice,
            expectedShares,
            new BigDecimal("100000")
        );

        // then
        assertThat(record.getSharesPurchased()).isEqualTo(expectedShares);
        assertThat(record.getSharesPurchased().scale()).isGreaterThanOrEqualTo(3); // 소수점 3자리 이상
    }

    @Test
    void 투자일이_null일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            null, // null 날짜
            new BigDecimal("100000"),
            new BigDecimal("150"),
            new BigDecimal("666.67"),
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("투자 날짜는 필수입니다");
    }

    @Test
    void 투자_금액이_0_이하일_경우_예외가_발생해야_한다() {
        // when & then - 0원
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            BigDecimal.ZERO, // 0원
            new BigDecimal("150"),
            new BigDecimal("0"),
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("투자 금액은 0보다 커야 합니다");

        // when & then - 음수
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("-50000"), // 음수
            new BigDecimal("150"),
            new BigDecimal("333.33"),
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("투자 금액은 0보다 커야 합니다");
    }

    @Test
    void 주식_가격이_0_이하일_경우_예외가_발생해야_한다() {
        // when & then - 0원
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            BigDecimal.ZERO, // 주가 0원
            new BigDecimal("0"),
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("주식 가격은 0보다 커야 합니다");

        // when & then - 음수
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("-150"), // 주가 음수
            new BigDecimal("666.67"),
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("주식 가격은 0보다 커야 합니다");
    }

    @Test
    void 매수_주식_수가_음수일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            new BigDecimal("-100"), // 음수 주식 수
            new BigDecimal("100000")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("매수 주식 수는 0 이상이어야 합니다");
    }

    @Test
    void 매수_주식_수가_0이어도_허용되어야_한다() {
        // given - 주식을 매수하지 않은 경우 (예: 휴장일 등)
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            BigDecimal.ZERO, // 0주 매수
            new BigDecimal("100000")
        );

        // when & then
        assertThat(record.getSharesPurchased()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void 포트폴리오_가치가_음수일_경우_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            new BigDecimal("666.67"),
            new BigDecimal("-50000") // 음수 포트폴리오 가치
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("포트폴리오 가치는 0 이상이어야 합니다");
    }

    @Test
    void 포트폴리오_가치가_0이어도_허용되어야_한다() {
        // given - 주식이 완전히 가치를 잃은 경우
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            new BigDecimal("100000"),
            new BigDecimal("150"),
            new BigDecimal("666.67"),
            BigDecimal.ZERO // 포트폴리오 가치 0원
        );

        // when & then
        assertThat(record.getPortfolioValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void 투자_기록을_문자열로_표현할_수_있어야_한다() {
        // given
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 3, 15, 9, 30),
            new BigDecimal("100000"),
            new BigDecimal("125.75"),
            new BigDecimal("795.23"),
            new BigDecimal("380000")
        );

        // when
        String description = record.toString();

        // then
        assertThat(description)
            .contains("2020-03-15") // 투자일
            .contains("100000") // 투자금액
            .contains("125.75") // 주가
            .contains("795.23") // 매수주식
            .contains("380000"); // 포트폴리오 가치
    }

    @Test
    void 높은_주가에서_소량_매수_기록도_정확해야_한다() {
        // given - 고가 주식 (버크셔 해서웨이 수준)
        BigDecimal highPrice = new BigDecimal("500000"); // 50만원 주가
        BigDecimal investmentAmount = new BigDecimal("100000"); // 10만원 투자
        BigDecimal fractionalShares = new BigDecimal("0.2"); // 0.2주 매수

        // when
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            investmentAmount,
            highPrice,
            fractionalShares,
            new BigDecimal("100000")
        );

        // then
        assertThat(record.getSharesPurchased()).isEqualTo(fractionalShares);
        assertThat(record.getStockPrice()).isEqualTo(highPrice);
    }

    @Test
    void 시간_정보도_정확하게_저장되어야_한다() {
        // given - 구체적인 시간까지 지정
        LocalDateTime specificTime = LocalDateTime.of(2020, 6, 15, 14, 30, 25);

        // when
        MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
            specificTime,
            new BigDecimal("100000"),
            new BigDecimal("200"),
            new BigDecimal("500"),
            new BigDecimal("100000")
        );

        // then
        assertThat(record.getInvestmentDate()).isEqualTo(specificTime);
        assertThat(record.getInvestmentDate().getHour()).isEqualTo(14);
        assertThat(record.getInvestmentDate().getMinute()).isEqualTo(30);
        assertThat(record.getInvestmentDate().getSecond()).isEqualTo(25);
    }
}