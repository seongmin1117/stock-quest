package com.stockquest.adapter.in.web.content.dto;

import com.stockquest.domain.content.tag.TagType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Tag Response DTO - Web Layer
 */
@Builder
public record TagResponse(
    Long id,
    String name,
    String slug,
    String description,
    TagType type,
    String typeDisplay,
    String colorCode,
    Long usageCount,
    Boolean popular,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}