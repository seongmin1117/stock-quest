package com.stockquest.adapter.in.web.company.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

/**
 * Company Search Response DTO - Web Layer
 *
 * Response DTO for company search operations including pagination metadata.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanySearchResponse(
    List<CompanyResponse> companies,
    long totalCount,
    int limit,
    int offset,
    boolean hasMore,
    int currentPage,
    int totalPages,
    String searchQuery,
    List<String> appliedCategories
) {
}