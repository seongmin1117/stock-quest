package com.stockquest.adapter.in.web.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인기 회사 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인기 회사 정보")
public class PopularCompanyDto {

    @Schema(description = "회사 심볼", example = "005930")
    private String symbol;

    @Schema(description = "한국어 회사명", example = "삼성전자")
    private String nameKr;

    @Schema(description = "영어 회사명", example = "Samsung Electronics")
    private String nameEn;

    @Schema(description = "시가총액 표시", example = "360조원")
    private String marketCap;

    @Schema(description = "섹터", example = "반도체")
    private String sector;

    @Schema(description = "로고 경로", example = "/logos/samsung.png")
    private String logoPath;

    @Schema(description = "인기도 점수", example = "100")
    private Integer popularityScore;
}