package com.stockquest.domain.simulation;

import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DCA 시뮬레이션 도메인 서비스
 * 달러 평균 투자법(Dollar Cost Averaging) 시뮬레이션을 실행하고 결과를 계산
 *
 * 헥사고날 아키텍처: 순수 도메인 서비스 (프레임워크 의존성 없음)
 */
public class DCASimulationService {

    private final PriceDataRepository priceDataRepository;
    private final BenchmarkDataRepository benchmarkDataRepository;

    public DCASimulationService(PriceDataRepository priceDataRepository,
                               BenchmarkDataRepository benchmarkDataRepository) {
        if (priceDataRepository == null) {
            throw new IllegalArgumentException("PriceDataRepository는 필수입니다");
        }
        if (benchmarkDataRepository == null) {
            throw new IllegalArgumentException("BenchmarkDataRepository는 필수입니다");
        }

        this.priceDataRepository = priceDataRepository;
        this.benchmarkDataRepository = benchmarkDataRepository;
    }

    /**
     * DCA 시뮬레이션 실행
     *
     * @param parameters 시뮬레이션 파라미터
     * @return 시뮬레이션 결과
     */
    public DCASimulationResult simulate(DCASimulationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("시뮬레이션 파라미터는 필수입니다");
        }

        // 주가 데이터 조회
        List<PriceData> priceDataList = priceDataRepository.findBySymbolAndDateRange(
            parameters.getSymbol(),
            parameters.getStartDate(),
            parameters.getEndDate()
        );

        if (priceDataList.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("해당 기간(%s ~ %s)의 '%s' 종목 주가 데이터를 찾을 수 없습니다. " +
                             "데이터가 있는 종목과 기간을 확인해주세요.",
                             parameters.getStartDate().toLocalDate(),
                             parameters.getEndDate().toLocalDate(),
                             parameters.getSymbol())
            );
        }

        // 월별 투자 실행 및 기록 생성
        List<MonthlyInvestmentRecord> investmentRecords = executeMonthlyInvestments(parameters, priceDataList);

        // 벤치마크 수익률 계산 (실패시 0으로 처리)
        BigDecimal sp500Return = calculateBenchmarkSafely(() ->
            benchmarkDataRepository.calculateSP500Return(
                parameters.getTotalPrincipal(),
                parameters.getStartDate(),
                parameters.getEndDate()
            )
        );

        BigDecimal nasdaqReturn = calculateBenchmarkSafely(() ->
            benchmarkDataRepository.calculateNASDAQReturn(
                parameters.getTotalPrincipal(),
                parameters.getStartDate(),
                parameters.getEndDate()
            )
        );

        // 최종 포트폴리오 가치 계산
        BigDecimal finalPortfolioValue = calculateFinalPortfolioValue(investmentRecords, priceDataList);

        return new DCASimulationResult(
            parameters,
            parameters.getTotalPrincipal(),
            finalPortfolioValue,
            investmentRecords,
            sp500Return,
            nasdaqReturn
        );
    }

    /**
     * 월별 투자를 실행하고 기록을 생성
     */
    private List<MonthlyInvestmentRecord> executeMonthlyInvestments(DCASimulationParameters parameters,
                                                                   List<PriceData> priceDataList) {
        List<MonthlyInvestmentRecord> records = new ArrayList<>();
        BigDecimal totalShares = BigDecimal.ZERO;

        LocalDateTime currentInvestmentDate = parameters.getStartDate();

        while (currentInvestmentDate.isBefore(parameters.getEndDate())) {

            // 투자일에 가장 가까운 주가 찾기
            PriceData closestPrice = findClosestPriceData(currentInvestmentDate, priceDataList);

            if (closestPrice != null) {
                // 매수 주식 수 계산 (소수점 2자리)
                BigDecimal sharesPurchased = parameters.getMonthlyInvestmentAmount()
                    .divide(closestPrice.getPrice(), 2, RoundingMode.HALF_UP);

                totalShares = totalShares.add(sharesPurchased);

                // 현재 포트폴리오 가치 계산
                BigDecimal portfolioValue = totalShares.multiply(closestPrice.getPrice());

                MonthlyInvestmentRecord record = new MonthlyInvestmentRecord(
                    currentInvestmentDate,
                    parameters.getMonthlyInvestmentAmount(),
                    closestPrice.getPrice(),
                    sharesPurchased,
                    portfolioValue
                );

                records.add(record);
            }

            // 다음 투자일로 이동
            currentInvestmentDate = getNextInvestmentDate(currentInvestmentDate, parameters.getFrequency());
        }

        return records;
    }

    /**
     * 투자일에 가장 가까운 주가 데이터 찾기
     */
    private PriceData findClosestPriceData(LocalDateTime investmentDate, List<PriceData> priceDataList) {
        PriceData closestPrice = null;
        long minDifference = Long.MAX_VALUE;

        for (PriceData priceData : priceDataList) {
            long difference = Math.abs(investmentDate.toLocalDate().toEpochDay() -
                                     priceData.getDate().toLocalDate().toEpochDay());

            if (difference < minDifference) {
                minDifference = difference;
                closestPrice = priceData;
            }
        }

        return closestPrice;
    }

    /**
     * 최종 포트폴리오 가치 계산
     */
    private BigDecimal calculateFinalPortfolioValue(List<MonthlyInvestmentRecord> records,
                                                   List<PriceData> priceDataList) {
        if (records.isEmpty() || priceDataList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 총 보유 주식 수 계산
        BigDecimal totalShares = records.stream()
            .map(MonthlyInvestmentRecord::getSharesPurchased)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 최종 주가로 포트폴리오 가치 계산
        PriceData finalPrice = priceDataList.get(priceDataList.size() - 1);
        return totalShares.multiply(finalPrice.getPrice());
    }

    /**
     * 다음 투자일 계산
     */
    private LocalDateTime getNextInvestmentDate(LocalDateTime currentDate, InvestmentFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            default:
                throw new IllegalArgumentException("지원하지 않는 투자 주기입니다: " + frequency);
        }
    }

    /**
     * 벤치마크 계산을 안전하게 실행 (실패시 0 반환)
     */
    private BigDecimal calculateBenchmarkSafely(BenchmarkSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @FunctionalInterface
    private interface BenchmarkSupplier {
        BigDecimal get() throws Exception;
    }
}