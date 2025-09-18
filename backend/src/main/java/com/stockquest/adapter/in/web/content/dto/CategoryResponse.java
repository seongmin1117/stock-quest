package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Response DTO - Web Layer
 */
@Builder
public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Long parentId,
    String parentName,
    String colorCode,
    String icon,
    Long articleCount,
    String articleCountDisplay,
    Integer sortOrder,
    Boolean active,
    Boolean featuredOnHome,

    // SEO Metadata
    String seoTitle,
    String metaDescription,
    String seoKeywords,

    // Hierarchy
    List<CategoryResponse> children,
    Integer level,
    String levelIndent,

    // Timestamps
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}