package com.stockquest.domain.company.port;

import com.stockquest.domain.company.CompanyCategory;

import java.util.List;
import java.util.Optional;

/**
 * Company Category Repository Port - Domain Interface for Hexagonal Architecture
 *
 * This port defines the contract for company category data access operations.
 * It remains in the domain layer to maintain clean architecture principles.
 *
 * Implementations should be in the adapter layer (out/persistence).
 */
public interface CompanyCategoryRepositoryPort {

    /**
     * Find a category by its identifier
     *
     * @param categoryId the category identifier
     * @return optional category if found
     */
    Optional<CompanyCategory> findByCategoryId(String categoryId);

    /**
     * Find all active categories
     *
     * @return list of active categories ordered by sort order
     */
    List<CompanyCategory> findAllActive();

    /**
     * Find all categories (active and inactive)
     *
     * @return list of all categories ordered by sort order
     */
    List<CompanyCategory> findAll();

    /**
     * Find categories by Korean name
     *
     * @param nameKr Korean name to search for
     * @return list of matching categories
     */
    List<CompanyCategory> findByNameKr(String nameKr);

    /**
     * Find categories by English name
     *
     * @param nameEn English name to search for
     * @return list of matching categories
     */
    List<CompanyCategory> findByNameEn(String nameEn);

    /**
     * Save or update a category
     *
     * @param category the category to save
     * @return saved category
     */
    CompanyCategory save(CompanyCategory category);

    /**
     * Delete a category
     *
     * @param category the category to delete
     */
    void delete(CompanyCategory category);

    /**
     * Check if a category exists by ID
     *
     * @param categoryId the category identifier
     * @return true if category exists
     */
    boolean existsByCategoryId(String categoryId);

    /**
     * Count total number of categories
     *
     * @return total count
     */
    long count();

    /**
     * Count active categories
     *
     * @return count of active categories
     */
    long countActive();
}