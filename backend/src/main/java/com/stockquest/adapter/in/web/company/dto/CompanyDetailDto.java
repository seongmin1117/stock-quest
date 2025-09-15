package com.stockquest.adapter.in.web.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회사 상세 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회사 상세 정보")
public class CompanyDetailDto {

    @Schema(description = "회사 심볼", example = "005930")
    private String symbol;

    @Schema(description = "한국어 회사명", example = "삼성전자")
    private String nameKr;

    @Schema(description = "영어 회사명", example = "Samsung Electronics")
    private String nameEn;

    @Schema(description = "섹터", example = "반도체")
    private String sector;

    @Schema(description = "시가총액", example = "360000000000000")
    private Long marketCap;

    @Schema(description = "시가총액 표시", example = "360조원")
    private String marketCapDisplay;

    @Schema(description = "로고 경로", example = "/logos/samsung.png")
    private String logoPath;

    @Schema(description = "한국어 설명", example = "글로벌 반도체, 스마트폰 제조업체")
    private String descriptionKr;

    @Schema(description = "영어 설명", example = "Global semiconductor and smartphone manufacturer")
    private String descriptionEn;

    @Schema(description = "거래소", example = "KRX")
    private String exchange;

    @Schema(description = "통화", example = "KRW")
    private String currency;

    @Schema(description = "인기도 점수", example = "100")
    private Integer popularityScore;
}