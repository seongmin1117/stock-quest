package com.stockquest.application.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import com.stockquest.domain.content.article.ArticleStatus;
import lombok.Builder;

import java.util.List;

/**
 * Article Search Query - Application Layer
 *
 * Query object for searching and filtering articles
 */
@Builder
public record ArticleSearchQuery(
    String query,
    Long categoryId,
    List<Long> tagIds,
    ArticleStatus status,
    ArticleDifficulty difficulty,
    Boolean featured,
    Long authorId,
    Boolean publishedOnly,
    String sortBy,      // title, published_at, view_count, like_count, created_at
    String sortDirection, // asc, desc
    Integer limit,
    Integer offset
) {

    // Default values
    private static final int DEFAULT_LIMIT = 20;
    private static final int DEFAULT_OFFSET = 0;
    private static final String DEFAULT_SORT_BY = "published_at";
    private static final String DEFAULT_SORT_DIRECTION = "desc";

    /**
     * Check if query has text search
     */
    public boolean hasTextQuery() {
        return query != null && !query.trim().isEmpty();
    }

    /**
     * Check if query has category filter
     */
    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    /**
     * Check if query has tag filters
     */
    public boolean hasTagFilters() {
        return tagIds != null && !tagIds.isEmpty();
    }

    /**
     * Check if query has author filter
     */
    public boolean hasAuthorFilter() {
        return authorId != null;
    }

    /**
     * Get limit with default value
     */
    public int getLimitOrDefault() {
        return limit != null ? Math.min(limit, 100) : DEFAULT_LIMIT;
    }

    /**
     * Get offset with default value
     */
    public int getOffsetOrDefault() {
        return offset != null ? Math.max(offset, 0) : DEFAULT_OFFSET;
    }

    /**
     * Get sort by with default value
     */
    public String getSortByOrDefault() {
        return sortBy != null ? sortBy : DEFAULT_SORT_BY;
    }

    /**
     * Get sort direction with default value
     */
    public String getSortDirectionOrDefault() {
        return sortDirection != null ? sortDirection : DEFAULT_SORT_DIRECTION;
    }

    /**
     * Check if published only filter is enabled
     */
    public boolean isPublishedOnly() {
        return publishedOnly != null && publishedOnly;
    }

    /**
     * Check if featured filter is enabled
     */
    public boolean isFeaturedOnly() {
        return featured != null && featured;
    }
}