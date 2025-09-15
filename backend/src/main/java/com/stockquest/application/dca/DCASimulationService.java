package com.stockquest.application.dca;

import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import com.stockquest.domain.simulation.DCASimulationParameters;
import com.stockquest.domain.simulation.DCASimulationResult;
import com.stockquest.domain.simulation.InvestmentFrequency;
import com.stockquest.domain.simulation.MonthlyInvestmentRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DCA 시뮬레이션 애플리케이션 서비스
 * 사용자 요청을 받아 도메인 서비스를 호출하고 응답을 변환
 */
public class DCASimulationService {

    private final com.stockquest.domain.simulation.DCASimulationService domainSimulationService;

    public DCASimulationService(com.stockquest.domain.simulation.DCASimulationService domainSimulationService) {
        if (domainSimulationService == null) {
            throw new IllegalArgumentException("DCASimulationService는 필수입니다");
        }
        this.domainSimulationService = domainSimulationService;
    }

    /**
     * DCA 시뮬레이션 실행
     *
     * @param command 시뮬레이션 요청 커맨드
     * @return 시뮬레이션 결과 응답
     */
    public DCASimulationResponse simulate(DCASimulationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("시뮬레이션 요청은 필수입니다");
        }

        // 커맨드를 도메인 파라미터로 변환
        DCASimulationParameters parameters = convertToParameters(command);

        // 도메인 서비스 호출
        DCASimulationResult domainResult = domainSimulationService.simulate(parameters);

        // 도메인 결과를 응답 DTO로 변환
        return convertToResponse(domainResult);
    }

    /**
     * 커맨드를 도메인 파라미터로 변환
     */
    private DCASimulationParameters convertToParameters(DCASimulationCommand command) {
        InvestmentFrequency frequency = convertFrequency(command.getFrequency());

        return new DCASimulationParameters(
            command.getSymbol(),
            command.getMonthlyInvestmentAmount(),
            command.getStartDate(),
            command.getEndDate(),
            frequency
        );
    }

    /**
     * 문자열 주기를 InvestmentFrequency enum으로 변환
     */
    private InvestmentFrequency convertFrequency(String frequency) {
        try {
            return InvestmentFrequency.valueOf(frequency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 투자 주기입니다: " + frequency);
        }
    }

    /**
     * 도메인 결과를 응답 DTO로 변환
     */
    private DCASimulationResponse convertToResponse(DCASimulationResult domainResult) {
        // 투자 기록을 DTO로 변환
        List<DCASimulationResponse.MonthlyInvestmentRecordDto> recordDtos = domainResult.getInvestmentRecords()
            .stream()
            .map(this::convertToRecordDto)
            .collect(Collectors.toList());

        // 벤치마크 대비 성과 계산
        BigDecimal outperformanceVsSP500 = calculateOutperformance(
            domainResult.getTotalReturnPercentage(),
            domainResult.getTotalInvestmentAmount(),
            domainResult.getSp500Return()
        );

        BigDecimal outperformanceVsNASDAQ = calculateOutperformance(
            domainResult.getTotalReturnPercentage(),
            domainResult.getTotalInvestmentAmount(),
            domainResult.getNasdaqReturn()
        );

        return new DCASimulationResponse(
            domainResult.getParameters().getSymbol(),
            domainResult.getTotalInvestmentAmount(),
            domainResult.getFinalPortfolioValue(),
            domainResult.getTotalReturnPercentage(),
            domainResult.getAnnualizedReturn(),
            recordDtos,
            domainResult.getSp500Return(),
            domainResult.getNasdaqReturn(),
            outperformanceVsSP500,
            outperformanceVsNASDAQ,
            domainResult.getMaxPortfolioValue()
        );
    }

    /**
     * 월별 투자 기록을 DTO로 변환
     */
    private DCASimulationResponse.MonthlyInvestmentRecordDto convertToRecordDto(MonthlyInvestmentRecord record) {
        return new DCASimulationResponse.MonthlyInvestmentRecordDto(
            record.getInvestmentDate(),
            record.getInvestmentAmount(),
            record.getStockPrice(),
            record.getSharesPurchased(),
            record.getPortfolioValue()
        );
    }

    /**
     * 벤치마크 대비 성과 계산
     */
    private BigDecimal calculateOutperformance(BigDecimal totalReturnPercentage,
                                              BigDecimal totalInvestment,
                                              BigDecimal benchmarkAmount) {
        if (benchmarkAmount.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        // 벤치마크 수익률 계산
        BigDecimal benchmarkProfit = benchmarkAmount.subtract(totalInvestment);
        BigDecimal benchmarkReturnPercentage = benchmarkProfit
            .divide(totalInvestment, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);

        // 초과 성과 = 실제 수익률 - 벤치마크 수익률
        return totalReturnPercentage.subtract(benchmarkReturnPercentage);
    }
}