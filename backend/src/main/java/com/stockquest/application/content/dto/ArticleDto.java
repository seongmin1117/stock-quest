package com.stockquest.application.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import com.stockquest.domain.content.article.ArticleStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Article Data Transfer Object - Application Layer
 *
 * Used for data transfer between application service and adapters.
 * Contains all article information including SEO metadata and analytics.
 */
@Builder
public record ArticleDto(
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
    ArticleDifficulty difficulty,

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
    List<TagDto> tags,

    // Timestamps
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Check if article is published
     */
    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED && publishedAt != null;
    }

    /**
     * Check if article is featured
     */
    public boolean isFeatured() {
        return featured != null && featured;
    }

    /**
     * Get reading time display
     */
    public String getReadingTimeDisplay() {
        if (readingTimeMinutes == null) {
            return "알 수 없음";
        }
        return readingTimeMinutes + "분";
    }

    /**
     * Get difficulty display in Korean
     */
    public String getDifficultyDisplay() {
        if (difficulty == null) {
            return "알 수 없음";
        }
        return switch (difficulty) {
            case BEGINNER -> "초급";
            case INTERMEDIATE -> "중급";
            case ADVANCED -> "고급";
            case EXPERT -> "전문가";
        };
    }

    /**
     * Get tag names as comma-separated string
     */
    public String getTagNames() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(TagDto::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}