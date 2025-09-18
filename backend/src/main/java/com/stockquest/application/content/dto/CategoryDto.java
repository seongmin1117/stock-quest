package com.stockquest.application.content.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Data Transfer Object - Application Layer
 */
@Builder
public record CategoryDto(
    Long id,
    String name,
    String slug,
    String description,
    Long parentId,
    String parentName,
    String colorCode,
    String icon,
    Long articleCount,
    Integer sortOrder,
    Boolean active,
    Boolean featuredOnHome,

    // SEO Metadata
    String seoTitle,
    String metaDescription,
    String seoKeywords,

    // Hierarchy
    List<CategoryDto> children,
    Integer level,

    // Timestamps
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Check if category is root level (no parent)
     */
    public boolean isRootLevel() {
        return parentId == null;
    }

    /**
     * Check if category has children
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Check if category is active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Check if category is featured on home page
     */
    public boolean isFeaturedOnHome() {
        return featuredOnHome != null && featuredOnHome;
    }

    /**
     * Get article count display
     */
    public String getArticleCountDisplay() {
        if (articleCount == null) {
            return "0개";
        }
        return articleCount + "개";
    }

    /**
     * Get level display for indentation
     */
    public String getLevelIndent() {
        if (level == null) {
            return "";
        }
        return "  ".repeat(level);
    }
}