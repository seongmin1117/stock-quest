package com.stockquest.domain.company.repository;

import com.stockquest.domain.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 정보 리포지토리
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 심볼로 회사 조회
     */
    Optional<Company> findBySymbol(String symbol);

    /**
     * 활성화된 회사만 조회
     */
    List<Company> findByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * 인기도 순으로 상위 N개 회사 조회
     */
    List<Company> findTop10ByIsActiveTrueOrderByPopularityScoreDesc();

    /**
     * 회사명 검색 (한국어/영어/심볼)
     */
    @Query("""
        SELECT c FROM Company c
        WHERE c.isActive = true
        AND (
            LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(c.symbol) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY c.popularityScore DESC
        """)
    List<Company> searchByNameOrSymbol(@Param("query") String query);

    /**
     * 특정 카테고리에 속한 회사들 조회
     */
    @Query("""
        SELECT c FROM Company c
        JOIN c.categories cc
        WHERE c.isActive = true
        AND cc.categoryId = :categoryId
        ORDER BY c.popularityScore DESC
        """)
    List<Company> findByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 섹터별 회사 조회
     */
    List<Company> findBySectorAndIsActiveTrueOrderByPopularityScoreDesc(String sector);

    /**
     * 시가총액 범위로 회사 조회
     */
    @Query("""
        SELECT c FROM Company c
        WHERE c.isActive = true
        AND c.marketCap BETWEEN :minMarketCap AND :maxMarketCap
        ORDER BY c.marketCap DESC
        """)
    List<Company> findByMarketCapRange(@Param("minMarketCap") Long minMarketCap,
                                      @Param("maxMarketCap") Long maxMarketCap);

    /**
     * 복합 검색 (이름, 섹터, 카테고리)
     */
    @Query("""
        SELECT DISTINCT c FROM Company c
        LEFT JOIN c.categories cc
        WHERE c.isActive = true
        AND (
            (:query IS NULL OR :query = '') OR
            LOWER(c.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(c.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(c.sector) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND (
            (:categoryId IS NULL OR :categoryId = '') OR
            cc.categoryId = :categoryId
        )
        AND (
            (:sector IS NULL OR :sector = '') OR
            c.sector = :sector
        )
        ORDER BY c.popularityScore DESC
        """)
    List<Company> searchCompanies(@Param("query") String query,
                                 @Param("categoryId") String categoryId,
                                 @Param("sector") String sector);

    /**
     * 카테고리별 회사 수 조회
     */
    @Query("""
        SELECT cc.categoryId, COUNT(c)
        FROM Company c
        JOIN c.categories cc
        WHERE c.isActive = true
        GROUP BY cc.categoryId
        """)
    List<Object[]> countCompaniesByCategory();

    /**
     * 최근 업데이트된 회사들 조회
     */
    @Query("""
        SELECT c FROM Company c
        WHERE c.isActive = true
        ORDER BY c.updatedAt DESC
        """)
    List<Company> findRecentlyUpdatedCompanies();

    /**
     * 한국 시장 회사만 조회
     */
    @Query("""
        SELECT c FROM Company c
        WHERE c.isActive = true
        AND c.exchange = 'KRX'
        AND c.currency = 'KRW'
        ORDER BY c.popularityScore DESC
        """)
    List<Company> findKoreanMarketCompanies();
}