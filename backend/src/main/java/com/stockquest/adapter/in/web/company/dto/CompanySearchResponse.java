package com.stockquest.adapter.in.web.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 회사 검색 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회사 검색 응답")
public class CompanySearchResponse {

    @Schema(description = "검색된 회사 목록")
    private List<CompanyDto> companies;
}