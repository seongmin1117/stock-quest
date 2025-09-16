package com.stockquest.adapter.in.web.company.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Company Category Response DTO - Web Layer
 *
 * Response DTO for company category information in REST APIs.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyCategoryResponse(
    Long id,
    String categoryId,
    String nameKr,
    String nameEn,
    String descriptionKr,
    String descriptionEn,
    int sortOrder,
    boolean isActive,
    long companyCount
) {
}