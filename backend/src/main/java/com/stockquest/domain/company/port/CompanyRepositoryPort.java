package com.stockquest.domain.company.port;

import com.stockquest.domain.company.Company;

import java.util.List;
import java.util.Optional;

/**
 * Company Repository Port - Domain Interface for Hexagonal Architecture
 *
 * This port defines the contract for company data access operations.
 * It remains in the domain layer to maintain clean architecture principles.
 *
 * Implementations should be in the adapter layer (out/persistence).
 */
public interface CompanyRepositoryPort {

    /**
     * Find a company by its stock symbol (ticker)
     *
     * @param symbol the stock symbol (e.g., "005930")
     * @return optional company if found
     */
    Optional<Company> findBySymbol(String symbol);

    /**
     * Find companies belonging to a specific category
     *
     * @param categoryId the category identifier
     * @return list of companies in the category
     */
    List<Company> findByCategory(String categoryId);

    /**
     * Find companies belonging to any of the specified categories
     *
     * @param categoryIds list of category identifiers
     * @return list of companies matching any category
     */
    List<Company> findByCategories(List<String> categoryIds);

    /**
     * Search companies by name (Korean or English)
     *
     * @param name partial or full name to search for
     * @return list of companies matching the search term
     */
    List<Company> searchByName(String name);

    /**
     * Find top companies by popularity score
     *
     * @param limit maximum number of companies to return
     * @return list of companies ordered by popularity descending
     */
    List<Company> findTopByPopularity(int limit);

    /**
     * Find all active companies
     *
     * @return list of all active companies
     */
    List<Company> findAllActive();

    /**
     * Save or update a company
     *
     * @param company the company to save
     * @return saved company
     */
    Company save(Company company);

    /**
     * Delete a company
     *
     * @param company the company to delete
     */
    void delete(Company company);

    /**
     * Check if a company exists by symbol
     *
     * @param symbol the stock symbol
     * @return true if company exists
     */
    boolean existsBySymbol(String symbol);

    /**
     * Count total number of companies
     *
     * @return total count
     */
    long count();

    /**
     * Find companies with pagination support
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of companies
     */
    List<Company> findAll(int page, int size);
}