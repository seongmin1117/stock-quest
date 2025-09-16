package com.stockquest.adapter.in.web.company.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.List;

/**
 * Company Search Request DTO - Web Layer
 *
 * Request DTO for company search operations.
 */
@Builder
public record CompanySearchRequest(
    String query,
    List<String> categories,
    String sector,
    Long minMarketCap,
    Long maxMarketCap,
    @Min(0) Integer minPopularityScore,
    String sortBy,
    String sortDirection,
    @Min(1) @Max(100) Integer limit,
    @Min(0) Integer offset
) {

    public CompanySearchRequest {
        // Set defaults
        limit = limit != null ? limit : 20;
        offset = offset != null ? offset : 0;
        sortBy = sortBy != null ? sortBy : "popularityScore";
        sortDirection = sortDirection != null ? sortDirection : "desc";
    }
}