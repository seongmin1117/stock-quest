package com.stockquest.application.company.dto;

import lombok.Builder;

import java.util.List;

/**
 * Company Search Result DTO - Application Layer
 *
 * Represents the result of a company search operation including pagination information.
 */
@Builder
public record CompanySearchResult(
    List<CompanyDto> companies,
    long totalCount,
    int limit,
    int offset,
    boolean hasMore,
    String searchQuery,
    List<String> appliedCategories
) {

    /**
     * Create a simple search result
     */
    public static CompanySearchResult of(List<CompanyDto> companies, long totalCount) {
        return CompanySearchResult.builder()
                .companies(companies)
                .totalCount(totalCount)
                .limit(companies.size())
                .offset(0)
                .hasMore(companies.size() < totalCount)
                .build();
    }

    /**
     * Create a paginated search result
     */
    public static CompanySearchResult paginated(List<CompanyDto> companies, long totalCount,
                                                int limit, int offset, String searchQuery,
                                                List<String> appliedCategories) {
        return CompanySearchResult.builder()
                .companies(companies)
                .totalCount(totalCount)
                .limit(limit)
                .offset(offset)
                .hasMore(offset + companies.size() < totalCount)
                .searchQuery(searchQuery)
                .appliedCategories(appliedCategories)
                .build();
    }

    /**
     * Create an empty result
     */
    public static CompanySearchResult empty(String searchQuery, List<String> appliedCategories) {
        return CompanySearchResult.builder()
                .companies(List.of())
                .totalCount(0)
                .limit(0)
                .offset(0)
                .hasMore(false)
                .searchQuery(searchQuery)
                .appliedCategories(appliedCategories)
                .build();
    }

    /**
     * Get current page number (0-based)
     */
    public int getCurrentPage() {
        return limit > 0 ? offset / limit : 0;
    }

    /**
     * Get total number of pages
     */
    public int getTotalPages() {
        return limit > 0 ? (int) Math.ceil((double) totalCount / limit) : 1;
    }

    /**
     * Check if there are previous results
     */
    public boolean hasPrevious() {
        return offset > 0;
    }

    /**
     * Get next offset for pagination
     */
    public int getNextOffset() {
        return hasMore ? offset + limit : offset;
    }

    /**
     * Get previous offset for pagination
     */
    public int getPreviousOffset() {
        return Math.max(0, offset - limit);
    }
}