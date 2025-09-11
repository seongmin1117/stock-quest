package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Sector Allocation Response DTO
 * 섹터별 자산 배분 응답 DTO
 */
@Data
@Builder
@Schema(description = "섹터별 자산 배분 분석")
public class SectorAllocationResponse {

    @Schema(description = "섹터명", example = "Technology")
    private String sector;

    @Schema(description = "섹터 한글명", example = "기술")
    private String sectorNameKr;

    @Schema(description = "섹터 투자 가치", example = "750000")
    private BigDecimal value;

    @Schema(description = "포트폴리오 대비 비중 (%)", example = "35.5")
    private Double percentage;

    @Schema(description = "목표 비중 (%)", example = "25.0")
    private Double targetPercentage;

    @Schema(description = "비중 차이 (%)", example = "10.5")
    private Double allocationDifference;

    @Schema(description = "섹터 수익률 (%)", example = "18.5")
    private Double sectorReturn;

    @Schema(description = "섹터 위험도", example = "0.22")
    private Double sectorRisk;

    @Schema(description = "포지션 개수", example = "4")
    private Integer positionCount;

    @Schema(description = "평균 보유일", example = "85")
    private Integer averageHoldingDays;

    @Schema(description = "배분 상태", example = "OVERWEIGHT")
    private String allocationStatus;

    @Schema(description = "리밸런싱 권장 여부", example = "true")
    private Boolean rebalanceRecommended;

    @Schema(description = "리밸런싱 권장 금액", example = "75000")
    private BigDecimal recommendedAdjustment;
}