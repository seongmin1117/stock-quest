package com.stockquest.adapter.in.web.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회사 카테고리 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회사 카테고리 정보")
public class CategoryDto {

    @Schema(description = "카테고리 ID", example = "tech")
    private String id;

    @Schema(description = "한국어 카테고리명", example = "기술")
    private String name;

    @Schema(description = "영어 카테고리명", example = "Technology")
    private String nameEn;

    @Schema(description = "카테고리 설명", example = "IT 서비스, 소프트웨어, 인터넷")
    private String description;

    @Schema(description = "카테고리 내 회사 수", example = "15")
    private Integer count;
}