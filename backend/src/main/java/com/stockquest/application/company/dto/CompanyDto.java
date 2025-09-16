package com.stockquest.application.company.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Company Data Transfer Object - Application Layer
 *
 * Represents company information for API responses and inter-layer communication.
 * This DTO is used to transfer company data between application and presentation layers.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyDto(
    Long id,
    String symbol,
    String nameKr,
    String nameEn,
    String sector,
    Long marketCap,
    String marketCapDisplay,
    String logoPath,
    String descriptionKr,
    String descriptionEn,
    String exchange,
    String currency,
    boolean isActive,
    int popularityScore,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<String> categories
) {

    /**
     * Create a simplified DTO for autocomplete/search results
     */
    public static CompanyDto forAutocomplete(String symbol, String nameKr, String nameEn,
                                             String sector, String logoPath, int popularityScore) {
        return CompanyDto.builder()
                .symbol(symbol)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .sector(sector)
                .logoPath(logoPath)
                .popularityScore(popularityScore)
                .isActive(true)
                .build();
    }

    /**
     * Create a DTO with essential information for listings
     */
    public static CompanyDto forListing(String symbol, String nameKr, String nameEn,
                                        String sector, String marketCapDisplay,
                                        String logoPath, int popularityScore,
                                        List<String> categories) {
        return CompanyDto.builder()
                .symbol(symbol)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .sector(sector)
                .marketCapDisplay(marketCapDisplay)
                .logoPath(logoPath)
                .popularityScore(popularityScore)
                .categories(categories)
                .isActive(true)
                .build();
    }

    /**
     * Check if this company matches the search term
     */
    public boolean matchesSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowercaseQuery = query.toLowerCase().trim();
        return (nameKr != null && nameKr.toLowerCase().contains(lowercaseQuery)) ||
               (nameEn != null && nameEn.toLowerCase().contains(lowercaseQuery)) ||
               (symbol != null && symbol.toLowerCase().contains(lowercaseQuery));
    }

    /**
     * Check if this company belongs to any of the specified categories
     */
    public boolean belongsToCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return true;
        }
        if (categories == null || categories.isEmpty()) {
            return false;
        }
        return categories.stream().anyMatch(categoryIds::contains);
    }
}