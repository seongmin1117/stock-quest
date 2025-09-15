package com.stockquest.domain.company.repository;

import com.stockquest.domain.company.CompanyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 카테고리 리포지토리
 */
@Repository
public interface CompanyCategoryRepository extends JpaRepository<CompanyCategory, Long> {

    /**
     * 카테고리 ID로 조회
     */
    Optional<CompanyCategory> findByCategoryId(String categoryId);

    /**
     * 활성화된 카테고리를 정렬 순서대로 조회
     */
    List<CompanyCategory> findByIsActiveTrueOrderBySortOrder();

    /**
     * 활성화된 카테고리 중 회사 수와 함께 조회
     */
    @Query("""
        SELECT cc, COUNT(ccm)
        FROM CompanyCategory cc
        LEFT JOIN CompanyCategoryMapping ccm ON cc.categoryId = ccm.categoryId
        LEFT JOIN Company c ON ccm.company = c AND c.isActive = true
        WHERE cc.isActive = true
        GROUP BY cc
        ORDER BY cc.sortOrder
        """)
    List<Object[]> findCategoriesWithCompanyCount();

    /**
     * 카테고리명으로 검색
     */
    @Query("""
        SELECT cc FROM CompanyCategory cc
        WHERE cc.isActive = true
        AND (
            LOWER(cc.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(cc.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(cc.categoryId) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY cc.sortOrder
        """)
    List<CompanyCategory> searchCategories(String query);
}