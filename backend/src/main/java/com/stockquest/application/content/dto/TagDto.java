package com.stockquest.application.content.dto;

import com.stockquest.domain.content.tag.TagType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Tag Data Transfer Object - Application Layer
 */
@Builder
public record TagDto(
    Long id,
    String name,
    String slug,
    String description,
    TagType type,
    String colorCode,
    Long usageCount,
    Boolean popular,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Get type display in Korean
     */
    public String getTypeDisplay() {
        return type != null ? type.getDisplayName() : "알 수 없음";
    }

    /**
     * Check if tag is popular
     */
    public boolean isPopular() {
        return popular != null && popular;
    }

    /**
     * Check if tag is active
     */
    public boolean isActive() {
        return active != null && active;
    }
}