package com.stockquest.adapter.out.persistence;

import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 벤치마크 데이터 저장소 어댑터
 * Domain BenchmarkDataRepository 인터페이스를 구현하여 벤치마크 지수 수익률 계산 제공
 * 현재는 시뮬레이션용 기본 구현으로, 실제 벤치마크 데이터를 사용하지 않고
 * 평균적인 시장 수익률을 기반으로 한 계산을 제공
 */
@Component
public class BenchmarkDataRepositoryAdapter implements BenchmarkDataRepository {

    // 연평균 수익률 상수 (실제 시장 데이터 기반 평균값)
    private static final BigDecimal SP500_ANNUAL_RETURN = new BigDecimal("0.10"); // 10%
    private static final BigDecimal NASDAQ_ANNUAL_RETURN = new BigDecimal("0.12"); // 12%

    @Override
    public BigDecimal calculateSP500Return(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
        return calculateBenchmarkReturn(totalInvestmentAmount, startDate, endDate, SP500_ANNUAL_RETURN);
    }

    @Override
    public BigDecimal calculateNASDAQReturn(BigDecimal totalInvestmentAmount, LocalDateTime startDate, LocalDateTime endDate) {
        return calculateBenchmarkReturn(totalInvestmentAmount, startDate, endDate, NASDAQ_ANNUAL_RETURN);
    }

    /**
     * 벤치마크 수익률 계산 공통 로직
     *
     * @param totalInvestmentAmount 총 투자금액
     * @param startDate 투자 시작일
     * @param endDate 투자 종료일
     * @param annualReturnRate 연평균 수익률
     * @return 계산된 최종 포트폴리오 가치
     */
    private BigDecimal calculateBenchmarkReturn(BigDecimal totalInvestmentAmount,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               BigDecimal annualReturnRate) {

        // 투자 기간(년) 계산
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal yearsInvested = new BigDecimal(daysBetween).divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);

        // 복리 계산: 최종가치 = 원금 * (1 + 연수익률)^투자년수
        BigDecimal onePlusRate = BigDecimal.ONE.add(annualReturnRate);

        // 년수가 0 이하인 경우 원금 반환
        if (yearsInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return totalInvestmentAmount;
        }

        // 복리 계산 (간단한 거듭제곱 계산)
        BigDecimal finalValue = totalInvestmentAmount;
        BigDecimal yearsFraction = yearsInvested;

        // 1년 이상인 경우 정수 부분 계산
        if (yearsFraction.compareTo(BigDecimal.ONE) >= 0) {
            int wholeYears = yearsFraction.intValue();
            for (int i = 0; i < wholeYears; i++) {
                finalValue = finalValue.multiply(onePlusRate);
            }
            yearsFraction = yearsFraction.subtract(new BigDecimal(wholeYears));
        }

        // 1년 미만 부분 계산 (선형 근사)
        if (yearsFraction.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fractionalReturn = annualReturnRate.multiply(yearsFraction);
            finalValue = finalValue.multiply(BigDecimal.ONE.add(fractionalReturn));
        }

        return finalValue.setScale(2, RoundingMode.HALF_UP);
    }
}