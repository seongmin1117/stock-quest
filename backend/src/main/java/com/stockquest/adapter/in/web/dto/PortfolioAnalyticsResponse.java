package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Portfolio Analytics Response DTO
 * 포트폴리오 종합 분석 응답 DTO
 */
@Data
@Builder
@Schema(description = "포트폴리오 종합 분석 결과")
public class PortfolioAnalyticsResponse {

    @Schema(description = "세션 ID", example = "12345")
    private Long sessionId;

    @Schema(description = "분석 수행 시각")
    private LocalDateTime analysisDate;

    @Schema(description = "분석 기간", example = "1Y")
    private String timeframe;

    // === 기본 메트릭 ===
    
    @Schema(description = "포트폴리오 총 가치", example = "1500000")
    private BigDecimal totalValue;

    @Schema(description = "총 투자 원금", example = "1200000")
    private BigDecimal totalCost;

    @Schema(description = "총 수익금액", example = "300000")
    private BigDecimal totalReturn;

    @Schema(description = "총 수익률 (%)", example = "25.5")
    private Double totalReturnPercent;

    @Schema(description = "일간 변화 금액", example = "15000")
    private BigDecimal dailyChange;

    @Schema(description = "일간 변화율 (%)", example = "1.2")
    private Double dailyChangePercent;

    // === 위험 지표 ===

    @Schema(description = "변동성 (연환산)", example = "0.18")
    private Double volatility;

    @Schema(description = "포트폴리오 베타", example = "1.15")
    private Double portfolioBeta;

    @Schema(description = "샤프 비율", example = "1.25")
    private Double sharpeRatio;

    @Schema(description = "소르티노 비율", example = "1.68")
    private Double sortinoRatio;

    @Schema(description = "최대 낙폭", example = "0.08")
    private Double maxDrawdown;

    @Schema(description = "위험가치 (VaR)", example = "0.05")
    private Double valueAtRisk;

    @Schema(description = "조건부 위험가치 (CVaR)", example = "0.07")
    private Double expectedShortfall;

    // === 성과 지표 ===

    @Schema(description = "연환산 수익률", example = "0.15")
    private Double annualizedReturn;

    @Schema(description = "승률 (%)", example = "65.0")
    private Double winRate;

    @Schema(description = "수익 인수", example = "1.8")
    private Double profitFactor;

    @Schema(description = "칼마 비율", example = "1.9")
    private Double calmarRatio;

    @Schema(description = "정보 비율", example = "0.8")
    private Double informationRatio;

    // === 배분 분석 ===

    @Schema(description = "섹터별 배분 분석")
    private List<SectorAllocationResponse> sectorAllocation;

    @Schema(description = "자산클래스별 배분 분석")
    private List<AssetAllocationResponse> assetAllocation;

    @Schema(description = "집중 위험도", example = "0.25")
    private Double concentrationRisk;

    @Schema(description = "분산 점수", example = "0.8")
    private Double diversificationScore;

    // === 추천사항 ===

    @Schema(description = "포트폴리오 개선 추천사항")
    private List<PortfolioRecommendationResponse> recommendations;

    // === 추가 메타데이터 ===

    @Schema(description = "포지션 개수", example = "12")
    private Integer positionCount;

    @Schema(description = "활성 종목 수", example = "10")
    private Integer activeInstrumentCount;

    @Schema(description = "마지막 업데이트 시각")
    private LocalDateTime lastUpdated;
}