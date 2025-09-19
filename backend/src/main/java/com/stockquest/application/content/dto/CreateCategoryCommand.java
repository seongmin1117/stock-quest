package com.stockquest.application.content.dto;

import lombok.Builder;

/**
 * Command for creating new category
 */
@Builder
public record CreateCategoryCommand(
    String name,
    String description,
    String slug,
    Boolean showOnHomepage
) {

    /**
     * Get showOnHomepage with default value
     */
    public boolean getShowOnHomepageOrDefault() {
        return showOnHomepage != null ? showOnHomepage : false;
    }
}