package com.stockquest.adapter.out.persistence.company;

import com.stockquest.adapter.out.persistence.entity.CompanyJpaEntity;
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
 * 헥사고날 아키텍처 준수 - CompanyJpaEntity 사용
 */
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {

    /**
     * Find company by symbol
     */
    Optional<CompanyJpaEntity> findBySymbol(String symbol);

    /**
     * Check if company exists by symbol
     */
    boolean existsBySymbol(String symbol);

    /**
     * Find companies by name (Korean or English) using case-insensitive search
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE " +
           "LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.symbol) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CompanyJpaEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find companies by category through mapping table
     */
    @Query("SELECT DISTINCT c FROM CompanyJpaEntity c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId = :categoryId AND c.isActive = true")
    List<CompanyJpaEntity> findByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find companies by multiple categories
     */
    @Query("SELECT DISTINCT c FROM CompanyJpaEntity c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId IN :categoryIds AND c.isActive = true")
    List<CompanyJpaEntity> findByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * Find top companies by popularity
     */
    List<CompanyJpaEntity> findTop50ByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * Find all active companies
     */
    List<CompanyJpaEntity> findByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * Find companies by sector
     */
    List<CompanyJpaEntity> findBySectorAndIsActiveTrue(String sector);

    /**
     * Find companies by market cap range
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE " +
           "c.isActive = true AND " +
           "(:minMarketCap IS NULL OR c.marketCap >= :minMarketCap) AND " +
           "(:maxMarketCap IS NULL OR c.marketCap <= :maxMarketCap) " +
           "ORDER BY c.popularityScore DESC")
    List<CompanyJpaEntity> findByMarketCapRange(@Param("minMarketCap") Long minMarketCap,
                                                @Param("maxMarketCap") Long maxMarketCap);

    /**
     * Find companies with pagination
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.isActive = true ORDER BY c.popularityScore DESC")
    List<CompanyJpaEntity> findAllActiveOrderByPopularity();

    /**
     * Count companies by category
     */
    @Query("SELECT COUNT(DISTINCT c) FROM CompanyJpaEntity c " +
           "JOIN c.categories cm " +
           "WHERE cm.categoryId = :categoryId AND c.isActive = true")
    long countByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find companies with minimum popularity score
     */
    List<CompanyJpaEntity> findByPopularityScoreGreaterThanEqualAndIsActiveTrueOrderByPopularityScoreDesc(
            int minPopularityScore);

    /**
     * Find companies by exchange (e.g., KRX)
     */
    List<CompanyJpaEntity> findByExchangeAndIsActiveTrueOrderByPopularityScoreDesc(String exchange);

    /**
     * Find companies by currency
     */
    List<CompanyJpaEntity> findByCurrencyAndIsActiveTrueOrderByPopularityScoreDesc(String currency);

    /**
     * Find large cap companies (시가총액 기준)
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE " +
           "c.isActive = true AND c.marketCap >= :minMarketCap " +
           "ORDER BY c.marketCap DESC")
    List<CompanyJpaEntity> findLargeCapCompanies(@Param("minMarketCap") Long minMarketCap);

    /**
     * Search companies with pagination and sorting
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE " +
           "c.isActive = true AND (" +
           "LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.sector) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY c.popularityScore DESC")
    List<CompanyJpaEntity> searchCompanies(@Param("query") String query);
}