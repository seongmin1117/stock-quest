package com.stockquest.adapter.out.persistence.company;

import com.stockquest.adapter.out.persistence.entity.CompanyCategoryJpaEntity;
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
 * 헥사고날 아키텍처 준수 - CompanyCategoryJpaEntity 사용
 */
public interface CompanyCategoryJpaRepository extends JpaRepository<CompanyCategoryJpaEntity, Long> {

    /**
     * Find category by category ID
     */
    Optional<CompanyCategoryJpaEntity> findByCategoryId(String categoryId);

    /**
     * Check if category exists by category ID
     */
    boolean existsByCategoryId(String categoryId);

    /**
     * Find all active categories ordered by sort order
     */
    List<CompanyCategoryJpaEntity> findByIsActiveTrueOrderBySortOrder();

    /**
     * Find all categories ordered by sort order
     */
    List<CompanyCategoryJpaEntity> findAllByOrderBySortOrder();

    /**
     * Find categories by Korean name
     */
    List<CompanyCategoryJpaEntity> findByNameKrContainingIgnoreCase(String nameKr);

    /**
     * Find categories by English name
     */
    List<CompanyCategoryJpaEntity> findByNameEnContainingIgnoreCase(String nameEn);

    /**
     * Count total active categories
     */
    long countByIsActiveTrue();

    /**
     * Find categories with company counts
     */
    @Query("SELECT c FROM CompanyCategoryJpaEntity c " +
           "WHERE c.isActive = true " +
           "ORDER BY c.sortOrder")
    List<CompanyCategoryJpaEntity> findActiveCategoriesWithCounts();

    /**
     * Find category by ID with company count
     */
    @Query("SELECT c FROM CompanyCategoryJpaEntity c " +
           "WHERE c.categoryId = :categoryId AND c.isActive = true")
    Optional<CompanyCategoryJpaEntity> findActiveByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Search categories by name (Korean or English)
     */
    @Query("SELECT c FROM CompanyCategoryJpaEntity c WHERE " +
           "c.isActive = true AND (" +
           "LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.categoryId) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY c.sortOrder")
    List<CompanyCategoryJpaEntity> searchCategories(@Param("query") String query);

    /**
     * Find categories by sort order range
     */
    @Query("SELECT c FROM CompanyCategoryJpaEntity c WHERE " +
           "c.isActive = true AND " +
           "c.sortOrder BETWEEN :minOrder AND :maxOrder " +
           "ORDER BY c.sortOrder")
    List<CompanyCategoryJpaEntity> findBySortOrderRange(@Param("minOrder") int minOrder,
                                                         @Param("maxOrder") int maxOrder);

    /**
     * Find categories containing specific text in description
     */
    @Query("SELECT c FROM CompanyCategoryJpaEntity c WHERE " +
           "c.isActive = true AND (" +
           "LOWER(c.descriptionKr) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
           "LOWER(c.descriptionEn) LIKE LOWER(CONCAT('%', :text, '%'))" +
           ") ORDER BY c.sortOrder")
    List<CompanyCategoryJpaEntity> findByDescriptionContaining(@Param("text") String text);
}