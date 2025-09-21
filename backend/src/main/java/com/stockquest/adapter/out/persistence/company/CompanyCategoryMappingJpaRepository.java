package com.stockquest.adapter.out.persistence.company;

import com.stockquest.adapter.out.persistence.entity.CompanyCategoryMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Company Category Mapping JPA Repository - Persistence Layer
 *
 * JPA repository interface for company-category mapping data access.
 * 헥사고날 아키텍처 준수 - CompanyCategoryMappingJpaEntity 사용
 */
public interface CompanyCategoryMappingJpaRepository extends JpaRepository<CompanyCategoryMappingJpaEntity, Long> {

    /**
     * Find mappings by company ID
     */
    List<CompanyCategoryMappingJpaEntity> findByCompanyId(Long companyId);

    /**
     * Find mappings by category ID
     */
    List<CompanyCategoryMappingJpaEntity> findByCategoryId(String categoryId);

    /**
     * Find specific mapping by company ID and category ID
     */
    Optional<CompanyCategoryMappingJpaEntity> findByCompanyIdAndCategoryId(Long companyId, String categoryId);

    /**
     * Check if mapping exists
     */
    boolean existsByCompanyIdAndCategoryId(Long companyId, String categoryId);

    /**
     * Delete mapping by company ID and category ID
     */
    @Modifying
    @Query("DELETE FROM CompanyCategoryMappingJpaEntity cm WHERE cm.company.id = :companyId AND cm.categoryId = :categoryId")
    void deleteByCompanyIdAndCategoryId(@Param("companyId") Long companyId, @Param("categoryId") String categoryId);

    /**
     * Delete all mappings for a company
     */
    @Modifying
    @Query("DELETE FROM CompanyCategoryMappingJpaEntity cm WHERE cm.company.id = :companyId")
    void deleteByCompanyId(@Param("companyId") Long companyId);

    /**
     * Delete all mappings for a category
     */
    @Modifying
    @Query("DELETE FROM CompanyCategoryMappingJpaEntity cm WHERE cm.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Count mappings by company ID
     */
    @Query("SELECT COUNT(cm) FROM CompanyCategoryMappingJpaEntity cm WHERE cm.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * Count mappings by category ID
     */
    @Query("SELECT COUNT(cm) FROM CompanyCategoryMappingJpaEntity cm WHERE cm.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find mappings by multiple company IDs
     */
    @Query("SELECT cm FROM CompanyCategoryMappingJpaEntity cm WHERE cm.company.id IN :companyIds")
    List<CompanyCategoryMappingJpaEntity> findByCompanyIds(@Param("companyIds") List<Long> companyIds);

    /**
     * Find mappings by multiple category IDs
     */
    @Query("SELECT cm FROM CompanyCategoryMappingJpaEntity cm WHERE cm.categoryId IN :categoryIds")
    List<CompanyCategoryMappingJpaEntity> findByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * Find recent mappings (created in last N days)
     */
    @Query("SELECT cm FROM CompanyCategoryMappingJpaEntity cm WHERE cm.createdAt >= CURRENT_TIMESTAMP - :days DAY")
    List<CompanyCategoryMappingJpaEntity> findRecentMappings(@Param("days") int days);

    /**
     * Find companies in multiple categories (intersection)
     */
    @Query("SELECT cm.company.id FROM CompanyCategoryMappingJpaEntity cm " +
           "WHERE cm.categoryId IN :categoryIds " +
           "GROUP BY cm.company.id " +
           "HAVING COUNT(DISTINCT cm.categoryId) = :categoryCount")
    List<Long> findCompaniesInAllCategories(@Param("categoryIds") List<String> categoryIds,
                                            @Param("categoryCount") long categoryCount);

    /**
     * Get category statistics (count of companies per category)
     */
    @Query("SELECT cm.categoryId, COUNT(cm.company.id) " +
           "FROM CompanyCategoryMappingJpaEntity cm " +
           "GROUP BY cm.categoryId " +
           "ORDER BY COUNT(cm.company.id) DESC")
    List<Object[]> getCategoryStatistics();

    /**
     * Find orphaned mappings (categories that don't exist)
     */
    @Query("SELECT cm FROM CompanyCategoryMappingJpaEntity cm " +
           "WHERE NOT EXISTS (SELECT 1 FROM CompanyCategoryJpaEntity cc WHERE cc.categoryId = cm.categoryId)")
    List<CompanyCategoryMappingJpaEntity> findOrphanedMappings();

    /**
     * Find companies with no category mappings
     */
    @Query("SELECT c.id FROM CompanyJpaEntity c " +
           "WHERE NOT EXISTS (SELECT 1 FROM CompanyCategoryMappingJpaEntity cm WHERE cm.company.id = c.id)")
    List<Long> findCompaniesWithoutCategories();
}