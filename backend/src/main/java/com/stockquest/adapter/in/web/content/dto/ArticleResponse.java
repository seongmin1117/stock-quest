package com.stockquest.adapter.in.web.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import com.stockquest.domain.content.article.ArticleStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Article Response DTO - Web Layer
 *
 * Response object for article API endpoints
 */
@Builder
public record ArticleResponse(
    Long id,
    String title,
    String slug,
    String summary,
    String content,
    Long authorId,
    String authorNickname,
    Long categoryId,
    String categoryName,
    ArticleStatus status,
    Boolean featured,
    Long viewCount,
    Long likeCount,
    Long commentCount,
    Integer readingTimeMinutes,
    String readingTimeDisplay,
    ArticleDifficulty difficulty,
    String difficultyDisplay,

    // SEO Metadata
    String seoTitle,
    String metaDescription,
    String seoKeywords,
    String canonicalUrl,
    String ogTitle,
    String ogDescription,
    String ogImageUrl,
    String twitterCardType,
    String twitterTitle,
    String twitterDescription,
    String twitterImageUrl,
    Boolean indexable,
    Boolean followable,
    String schemaType,

    // Tags
    List<TagResponse> tags,
    String tagNames,

    // Timestamps
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}