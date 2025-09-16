package com.stockquest.adapter.in.web.company.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Company Response DTO - Web Layer
 *
 * Response DTO for company information in REST APIs.
 * This DTO represents the external contract for company data.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyResponse(
    Long id,
    String symbol,
    String nameKr,
    String nameEn,
    String sector,
    Long marketCap,
    String marketCapDisplay,
    String logoPath,
    String descriptionKr,
    String descriptionEn,
    String exchange,
    String currency,
    boolean isActive,
    int popularityScore,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<String> categories
) {
}