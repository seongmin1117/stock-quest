package com.stockquest.application.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import lombok.Builder;

import java.util.List;

/**
 * 글 수정 명령 DTO
 */
@Builder
public record UpdateArticleCommand(
    String title,
    String content,
    String summary,
    Long categoryId,
    ArticleDifficulty difficulty,
    Boolean featured,
    List<Long> tagIds,
    SeoMetadataCommand seoMetadata
) {
    /**
     * SEO 메타데이터 명령 DTO
     */
    @Builder
    public record SeoMetadataCommand(
        String seoTitle,
        String metaDescription,
        List<String> keywords,
        String canonicalUrl,
        String ogTitle,
        String ogDescription,
        String ogImageUrl,
        String twitterTitle,
        String twitterDescription,
        String twitterImageUrl,
        Boolean indexable,
        Boolean followable
    ) {}
}