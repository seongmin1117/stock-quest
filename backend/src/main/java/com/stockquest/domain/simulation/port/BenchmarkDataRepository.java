package com.stockquest.domain.simulation.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 벤치마크 지수 수익률 계산을 위한 포트 인터페이스
 * S&P 500, NASDAQ 등 벤치마크 지수 대비 성과 비교를 위한 데이터 제공
 */
public interface BenchmarkDataRepository {

    /**
     * S&P 500 지수 기반 동일 조건 투자 수익률 계산
     *
     * @param totalInvestmentAmount 총 투자금액
     * @param startDate 투자 시작일
     * @param endDate 투자 종료일
     * @return S&P 500 지수 기반 최종 포트폴리오 가치
     */
    BigDecimal calculateSP500Return(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * NASDAQ 지수 기반 동일 조건 투자 수익률 계산
     *
     * @param totalInvestmentAmount 총 투자금액
     * @param startDate 투자 시작일
     * @param endDate 투자 종료일
     * @return NASDAQ 지수 기반 최종 포트폴리오 가치
     */
    BigDecimal calculateNASDAQReturn(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate);
}