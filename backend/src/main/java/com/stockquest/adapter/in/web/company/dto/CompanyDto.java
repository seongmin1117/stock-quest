package com.stockquest.adapter.in.web.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회사 기본 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회사 기본 정보")
public class CompanyDto {

    @Schema(description = "회사 심볼", example = "005930")
    private String symbol;

    @Schema(description = "한국어 회사명", example = "삼성전자")
    private String nameKr;

    @Schema(description = "영어 회사명", example = "Samsung Electronics")
    private String nameEn;

    @Schema(description = "섹터", example = "반도체")
    private String sector;

    @Schema(description = "로고 경로", example = "/logos/samsung.png")
    private String logoPath;

    @Schema(description = "시가총액 표시", example = "360조원")
    private String marketCapDisplay;
}