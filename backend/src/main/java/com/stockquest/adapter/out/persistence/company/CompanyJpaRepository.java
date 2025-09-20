package com.stockquest.adapter.out.persistence.company;

import com.stockquest.domain.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Company JPA Repository - Persistence Layer
 *
 * JPA repository interface for company data access.
 * Used by CompanyRepositoryAdapter to implement the domain port.
 */
public interface CompanyJpaRepository extends JpaRepository<Company, Long> {

    /**
     * Find company by symbol
     */
    Optional<Company> findBySymbol(String symbol);

    /**
     * Check if company exists by symbol
     */
    boolean existsBySymbol(String symbol);

    /**
     * Find companies by name (Korean or English) using case-insensitive search
     */
    @Query("SELECT c FROM Company c WHERE " +
           "LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.symbol) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Company> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find companies by category through mapping table
     */
    @Query("SELECT DISTINCT c FROM Company c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId = :categoryId AND c.isActive = true")
    List<Company> findByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find companies by multiple categories
     */
    @Query("SELECT DISTINCT c FROM Company c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId IN :categoryIds AND c.isActive = true")
    List<Company> findByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * Find top companies by popularity
     */
    List<Company> findTop50ByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * Find all active companies
     */
    List<Company> findByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * Find companies by sector
     */
    List<Company> findBySectorAndIsActiveTrue(String sector);

    /**
     * Find companies by market cap range
     */
    @Query("SELECT c FROM Company c WHERE " +
           "c.isActive = true AND " +
           "(:minMarketCap IS NULL OR c.marketCap >= :minMarketCap) AND " +
           "(:maxMarketCap IS NULL OR c.marketCap <= :maxMarketCap) " +
           "ORDER BY c.popularityScore DESC")
    List<Company> findByMarketCapRange(@Param("minMarketCap") Long minMarketCap,
                                       @Param("maxMarketCap") Long maxMarketCap);

    /**
     * Find companies with pagination
     */
    @Query("SELECT c FROM Company c WHERE c.isActive = true ORDER BY c.popularityScore DESC")
    List<Company> findAllActiveOrderByPopularity();

    /**
     * Count companies by category
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Company c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId = :categoryId AND c.isActive = true")
    long countByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find companies with minimum popularity score
     */
    List<Company> findByPopularityScoreGreaterThanEqualAndIsActiveTrueOrderByPopularityScoreDesc(
            int minPopularityScore);
}