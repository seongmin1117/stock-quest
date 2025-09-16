package com.stockquest.application.company.dto;

import lombok.Builder;

import java.util.List;

/**
 * Company Search Query DTO - Application Layer
 *
 * Represents search parameters for company queries.
 * Provides builder pattern for flexible query construction.
 */
@Builder
public record CompanySearchQuery(
    String query,
    List<String> categories,
    String sector,
    Long minMarketCap,
    Long maxMarketCap,
    Integer minPopularityScore,
    Boolean activeOnly,
    String sortBy,
    String sortDirection,
    Integer limit,
    Integer offset
) {

    /**
     * Create a simple text search query
     */
    public static CompanySearchQuery simpleSearch(String query, int limit) {
        return CompanySearchQuery.builder()
                .query(query)
                .limit(limit)
                .activeOnly(true)
                .build();
    }

    /**
     * Create a category filter query
     */
    public static CompanySearchQuery byCategory(String categoryId, int limit) {
        return CompanySearchQuery.builder()
                .categories(List.of(categoryId))
                .limit(limit)
                .activeOnly(true)
                .sortBy("popularityScore")
                .sortDirection("desc")
                .build();
    }

    /**
     * Create a multi-category filter query
     */
    public static CompanySearchQuery byCategories(List<String> categoryIds, int limit) {
        return CompanySearchQuery.builder()
                .categories(categoryIds)
                .limit(limit)
                .activeOnly(true)
                .sortBy("popularityScore")
                .sortDirection("desc")
                .build();
    }

    /**
     * Create a popularity-based query
     */
    public static CompanySearchQuery topCompanies(int limit) {
        return CompanySearchQuery.builder()
                .limit(limit)
                .activeOnly(true)
                .sortBy("popularityScore")
                .sortDirection("desc")
                .build();
    }

    /**
     * Get limit with default value
     */
    public int getLimitOrDefault() {
        return limit != null ? limit : 20;
    }

    /**
     * Get offset with default value
     */
    public int getOffsetOrDefault() {
        return offset != null ? offset : 0;
    }

    /**
     * Check if query is empty
     */
    public boolean hasTextQuery() {
        return query != null && !query.trim().isEmpty();
    }

    /**
     * Check if category filters are applied
     */
    public boolean hasCategoryFilter() {
        return categories != null && !categories.isEmpty();
    }

    /**
     * Check if market cap filters are applied
     */
    public boolean hasMarketCapFilter() {
        return minMarketCap != null || maxMarketCap != null;
    }

    /**
     * Get sort field with default
     */
    public String getSortByOrDefault() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return hasTextQuery() ? "popularityScore" : "popularityScore";
        }
        return sortBy;
    }

    /**
     * Get sort direction with default
     */
    public String getSortDirectionOrDefault() {
        return sortDirection != null && sortDirection.equalsIgnoreCase("asc") ? "asc" : "desc";
    }
}