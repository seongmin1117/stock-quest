package com.stockquest.domain.simulation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * TDD: RED - InvestmentFrequency 도메인 Enum 테스트
 * 투자 주기를 나타내는 Enum의 동작을 검증
 */
class InvestmentFrequencyTest {

    @Test
    void 투자_주기_값들이_올바르게_정의되어야_한다() {
        // given & when & then
        assertThat(InvestmentFrequency.MONTHLY).isNotNull();
        assertThat(InvestmentFrequency.WEEKLY).isNotNull();
        assertThat(InvestmentFrequency.DAILY).isNotNull();
    }

    @Test
    void 월별_투자_주기의_일수가_올바르게_계산되어야_한다() {
        // given
        InvestmentFrequency monthly = InvestmentFrequency.MONTHLY;

        // when
        int daysPerPeriod = monthly.getDaysPerPeriod();

        // then
        assertThat(daysPerPeriod).isEqualTo(30);
    }

    @Test
    void 주별_투자_주기의_일수가_올바르게_계산되어야_한다() {
        // given
        InvestmentFrequency weekly = InvestmentFrequency.WEEKLY;

        // when
        int daysPerPeriod = weekly.getDaysPerPeriod();

        // then
        assertThat(daysPerPeriod).isEqualTo(7);
    }

    @Test
    void 일별_투자_주기의_일수가_올바르게_계산되어야_한다() {
        // given
        InvestmentFrequency daily = InvestmentFrequency.DAILY;

        // when
        int daysPerPeriod = daily.getDaysPerPeriod();

        // then
        assertThat(daysPerPeriod).isEqualTo(1);
    }

    @Test
    void 투자_주기_설명이_올바르게_반환되어야_한다() {
        // given & when & then
        assertThat(InvestmentFrequency.MONTHLY.getDescription()).isEqualTo("월별");
        assertThat(InvestmentFrequency.WEEKLY.getDescription()).isEqualTo("주별");
        assertThat(InvestmentFrequency.DAILY.getDescription()).isEqualTo("일별");
    }
}