package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Portfolio Recommendation Response DTO
 * 포트폴리오 추천사항 응답 DTO
 */
@Data
@Builder
@Schema(description = "포트폴리오 개선 추천사항")
public class PortfolioRecommendationResponse {

    @Schema(description = "추천 유형", example = "REBALANCE")
    private String type;

    @Schema(description = "우선순위", example = "HIGH")
    private String priority;

    @Schema(description = "추천 제목", example = "섹터 리밸런싱 필요")
    private String title;

    @Schema(description = "상세 설명", example = "기술주 비중이 목표 대비 과도합니다.")
    private String description;

    @Schema(description = "권장 조치", example = "기술주 비중을 줄이고 금융주 비중을 늘리세요.")
    private String action;

    @Schema(description = "예상 효과", example = "위험 감소 및 포트폴리오 안정성 향상")
    private String impact;

    @Schema(description = "추천 근거", example = "섹터 집중으로 인한 상관관계 위험 증가")
    private String reasoning;

    @Schema(description = "관련 종목 목록")
    private List<String> affectedSymbols;

    @Schema(description = "목표 배분 비율 (%)", example = "25.0")
    private Double targetAllocation;

    @Schema(description = "현재 배분 비율 (%)", example = "35.0")
    private Double currentAllocation;

    @Schema(description = "위험 점수 (1-10)", example = "7")
    private Integer riskScore;

    @Schema(description = "예상 수익률 개선 (%)", example = "2.5")
    private Double expectedImprovement;
}