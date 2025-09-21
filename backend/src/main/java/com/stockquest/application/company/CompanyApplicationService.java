package com.stockquest.application.company;

import com.stockquest.application.company.dto.*;
import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.CompanyCategory;
import com.stockquest.domain.company.port.CompanyRepositoryPort;
import com.stockquest.domain.company.port.CompanyCategoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Company Application Service - Application Layer
 *
 * Orchestrates company business logic following clean architecture principles.
 * Uses domain ports to interact with persistence layer adapters.
 *
 * This service is responsible for:
 * - Converting between domain entities and DTOs
 * - Orchestrating complex business operations
 * - Transaction management
 * - Input validation and error handling
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompanyApplicationService {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final CompanyCategoryRepositoryPort companyCategoryRepositoryPort;

    /**
     * Get a company by its symbol
     *
     * @param symbol the stock symbol
     * @return company DTO if found
     */
    public Optional<CompanyDto> getCompanyBySymbol(String symbol) {
        log.debug("Getting company by symbol: {}", symbol);

        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Attempted to search for company with empty symbol");
            return Optional.empty();
        }

        return companyRepositoryPort.findBySymbol(symbol.trim().toUpperCase())
                .map(this::convertToDto);
    }

    /**
     * Search companies based on query parameters
     *
     * @param query search query with filters
     * @return search result with companies and metadata
     */
    public CompanySearchResult searchCompanies(CompanySearchQuery query) {
        log.debug("Searching companies with query: {}", query);

        try {
            List<Company> companies;

            // Determine search strategy based on query parameters
            if (query.hasTextQuery()) {
                companies = companyRepositoryPort.searchByName(query.query().trim());
                log.debug("Found {} companies by name search", companies.size());
            } else if (query.hasCategoryFilter()) {
                companies = companyRepositoryPort.findByCategories(query.categories());
                log.debug("Found {} companies by category filter", companies.size());
            } else {
                // Default to top companies by popularity
                companies = companyRepositoryPort.findTopByPopularity(query.getLimitOrDefault());
                log.debug("Found {} top companies by popularity", companies.size());
            }

            // Apply additional filters
            companies = applyFilters(companies, query);

            // Apply sorting and pagination
            companies = applySortingAndPagination(companies, query);

            // Convert to DTOs
            List<CompanyDto> companyDtos = companies.stream()
                    .map(this::convertToDto)
                    .toList();

            log.debug("Returning {} companies after filtering and pagination", companyDtos.size());

            return CompanySearchResult.paginated(
                    companyDtos,
                    companyDtos.size(), // In a real implementation, we'd need a count query
                    query.getLimitOrDefault(),
                    query.getOffsetOrDefault(),
                    query.query(),
                    query.categories()
            );

        } catch (Exception e) {
            log.error("Error searching companies: {}", e.getMessage(), e);
            return CompanySearchResult.empty(query.query(), query.categories());
        }
    }

    /**
     * Get all active categories
     *
     * @return list of category DTOs
     */
    public List<CompanyCategoryDto> getAllCategories() {
        log.debug("Getting all active categories");

        try {
            List<CompanyCategory> categories = companyCategoryRepositoryPort.findAllActive();
            return categories.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get companies by category
     *
     * @param categoryId the category identifier
     * @return list of company DTOs
     */
    public List<CompanyDto> getCompaniesByCategory(String categoryId) {
        log.debug("Getting companies by category: {}", categoryId);

        if (categoryId == null || categoryId.trim().isEmpty()) {
            log.warn("Attempted to search for companies with empty category");
            return List.of();
        }

        try {
            List<Company> companies = companyRepositoryPort.findByCategory(categoryId.trim());
            return companies.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting companies by category: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get top companies by popularity
     *
     * @param limit maximum number of companies to return
     * @return list of top company DTOs
     */
    public List<CompanyDto> getTopCompaniesByPopularity(int limit) {
        log.debug("Getting top {} companies by popularity", limit);

        if (limit <= 0) {
            log.warn("Invalid limit for top companies: {}", limit);
            return List.of();
        }

        try {
            List<Company> companies = companyRepositoryPort.findTopByPopularity(limit);
            return companies.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting top companies: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Convert Company domain entity to DTO
     */
    private CompanyDto convertToDto(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .symbol(company.getSymbol())
                .nameKr(company.getNameKr())
                .nameEn(company.getNameEn())
                .sector(company.getSector())
                .marketCap(company.getMarketCap())
                .marketCapDisplay(company.getMarketCapDisplay())
                .logoPath(company.getLogoPath())
                .descriptionKr(company.getDescriptionKr())
                .descriptionEn(company.getDescriptionEn())
                .exchange(company.getExchange())
                .currency(company.getCurrency())
                .isActive(company.getIsActive())
                .popularityScore(company.getPopularityScore())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .categories(company.getCategoryIds())
                .build();
    }

    /**
     * Convert CompanyCategory domain entity to DTO
     */
    private CompanyCategoryDto convertToDto(CompanyCategory category) {
        return CompanyCategoryDto.builder()
                .id(category.getId())
                .categoryId(category.getCategoryId())
                .nameKr(category.getNameKr())
                .nameEn(category.getNameEn())
                .descriptionKr(category.getDescriptionKr())
                .descriptionEn(category.getDescriptionEn())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .build();
    }

    /**
     * Apply additional filters to the company list
     */
    private List<Company> applyFilters(List<Company> companies, CompanySearchQuery query) {
        return companies.stream()
                .filter(company -> query.activeOnly() == null || !query.activeOnly() || company.getIsActive())
                .filter(company -> query.sector() == null ||
                        company.getSector() != null && company.getSector().equalsIgnoreCase(query.sector()))
                .filter(company -> query.minMarketCap() == null ||
                        company.getMarketCap() == null || company.getMarketCap() >= query.minMarketCap())
                .filter(company -> query.maxMarketCap() == null ||
                        company.getMarketCap() == null || company.getMarketCap() <= query.maxMarketCap())
                .filter(company -> query.minPopularityScore() == null ||
                        company.getPopularityScore() >= query.minPopularityScore())
                .toList();
    }

    /**
     * Apply sorting and pagination to the company list
     */
    private List<Company> applySortingAndPagination(List<Company> companies, CompanySearchQuery query) {
        // Apply sorting (simplified - in real implementation, use database sorting)
        companies = companies.stream()
                .sorted((c1, c2) -> {
                    int comparison = switch (query.getSortByOrDefault()) {
                        case "nameKr" -> c1.getNameKr().compareTo(c2.getNameKr());
                        case "nameEn" -> c1.getNameEn().compareTo(c2.getNameEn());
                        case "marketCap" -> Long.compare(
                                c1.getMarketCap() != null ? c1.getMarketCap() : 0,
                                c2.getMarketCap() != null ? c2.getMarketCap() : 0);
                        default -> Integer.compare(c1.getPopularityScore(), c2.getPopularityScore());
                    };
                    return "asc".equalsIgnoreCase(query.getSortDirectionOrDefault()) ?
                            comparison : -comparison;
                })
                .toList();

        // Apply pagination (simplified - in real implementation, use database pagination)
        int offset = query.getOffsetOrDefault();
        int limit = query.getLimitOrDefault();
        int toIndex = Math.min(offset + limit, companies.size());

        if (offset >= companies.size()) {
            return List.of();
        }

        return companies.subList(offset, toIndex);
    }
}