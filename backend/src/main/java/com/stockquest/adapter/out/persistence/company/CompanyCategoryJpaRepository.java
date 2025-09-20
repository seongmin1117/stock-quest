package com.stockquest.adapter.out.persistence.company;

import com.stockquest.domain.company.CompanyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Company Category JPA Repository - Persistence Layer
 *
 * JPA repository interface for company category data access.
 * Used by CompanyCategoryRepositoryAdapter to implement the domain port.
 */
public interface CompanyCategoryJpaRepository extends JpaRepository<CompanyCategory, Long> {

    /**
     * Find category by category ID
     */
    Optional<CompanyCategory> findByCategoryId(String categoryId);

    /**
     * Check if category exists by category ID
     */
    boolean existsByCategoryId(String categoryId);

    /**
     * Find all active categories ordered by sort order
     */
    List<CompanyCategory> findByIsActiveTrueOrderBySortOrder();

    /**
     * Find all categories ordered by sort order
     */
    List<CompanyCategory> findAllByOrderBySortOrder();

    /**
     * Find categories by Korean name
     */
    List<CompanyCategory> findByNameKrContainingIgnoreCase(String nameKr);

    /**
     * Find categories by English name
     */
    List<CompanyCategory> findByNameEnContainingIgnoreCase(String nameEn);

    /**
     * Count total active categories
     */
    long countByIsActiveTrue();

    /**
     * Find categories with company counts
     */
    @Query("SELECT c FROM CompanyCategory c " +
           "WHERE c.isActive = true " +
           "ORDER BY c.sortOrder")
    List<CompanyCategory> findActiveCategoriesWithCounts();

    /**
     * Find category by ID with company count
     */
    @Query("SELECT c FROM CompanyCategory c " +
           "WHERE c.categoryId = :categoryId AND c.isActive = true")
    Optional<CompanyCategory> findActiveByCategoryId(@Param("categoryId") String categoryId);
}