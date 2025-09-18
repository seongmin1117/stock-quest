package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import java.util.List;

/**
 * Article Search Response DTO - Web Layer
 *
 * Response object containing search results and pagination metadata
 */
@Builder
public record ArticleSearchResponse(
    List<ArticleResponse> articles,
    Long totalCount,
    Integer currentPage,
    Integer pageSize,
    Integer totalPages,
    String query,
    Long categoryId,
    List<Long> tagIds,
    String sortBy,
    String sortDirection,
    Boolean hasNext,
    Boolean hasPrevious,
    String paginationDisplay
) {
}