package com.stockquest.application.company.dto;

import lombok.Builder;

/**
 * Company Category Data Transfer Object - Application Layer
 *
 * Represents company category information for API responses.
 */
@Builder
public record CompanyCategoryDto(
    Long id,
    String categoryId,
    String nameKr,
    String nameEn,
    String descriptionKr,
    String descriptionEn,
    int sortOrder,
    boolean isActive,
    long companyCount
) {

    /**
     * Create a simple category DTO for dropdown/selection lists
     */
    public static CompanyCategoryDto forSelection(String categoryId, String nameKr,
                                                  String nameEn, int sortOrder) {
        return CompanyCategoryDto.builder()
                .categoryId(categoryId)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .sortOrder(sortOrder)
                .isActive(true)
                .companyCount(0)
                .build();
    }

    /**
     * Create a detailed category DTO with company count
     */
    public static CompanyCategoryDto withCount(String categoryId, String nameKr, String nameEn,
                                               String descriptionKr, String descriptionEn,
                                               int sortOrder, long companyCount) {
        return CompanyCategoryDto.builder()
                .categoryId(categoryId)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .descriptionKr(descriptionKr)
                .descriptionEn(descriptionEn)
                .sortOrder(sortOrder)
                .isActive(true)
                .companyCount(companyCount)
                .build();
    }
}