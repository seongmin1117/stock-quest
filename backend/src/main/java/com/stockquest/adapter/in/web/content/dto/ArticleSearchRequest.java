package com.stockquest.adapter.in.web.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import lombok.Builder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * Article Search Request DTO - Web Layer
 *
 * Request object for searching articles via REST API
 */
@Builder
public record ArticleSearchRequest(
    String query,
    Long categoryId,
    List<Long> tagIds,
    ArticleDifficulty difficulty,
    Boolean featured,
    Long authorId,
    String sortBy,      // title, published_at, view_count, like_count, created_at
    String sortDirection, // asc, desc
    @Min(1) @Max(100) Integer limit,
    @Min(0) Integer offset
) {

    /**
     * Convert to application search query
     */
    public com.stockquest.application.content.dto.ArticleSearchQuery toSearchQuery() {
        return com.stockquest.application.content.dto.ArticleSearchQuery.builder()
                .query(query)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .difficulty(difficulty)
                .featured(featured)
                .authorId(authorId)
                .publishedOnly(true) // Web API only shows published articles
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .limit(limit)
                .offset(offset)
                .build();
    }
}