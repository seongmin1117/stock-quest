package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Category JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    /**
     * slug로 카테고리 찾기
     */
    Optional<CategoryJpaEntity> findBySlug(String slug);

    /**
     * 활성화된 모든 카테고리 조회
     */
    @Query("SELECT c FROM CategoryJpaEntity c " +
           "WHERE c.isActive = true " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryJpaEntity> findAllActive();

    /**
     * 추천 카테고리 목록 조회
     */
    @Query("SELECT c FROM CategoryJpaEntity c " +
           "WHERE c.isFeatured = true " +
           "AND c.isActive = true " +
           "ORDER BY c.sortOrder ASC")
    List<CategoryJpaEntity> findFeaturedCategories();

    /**
     * 부모 카테고리 ID로 하위 카테고리 조회
     */
    List<CategoryJpaEntity> findByParentIdAndIsActiveTrue(Long parentId);

    /**
     * 최상위 카테고리 조회
     */
    @Query("SELECT c FROM CategoryJpaEntity c " +
           "WHERE c.parentId IS NULL " +
           "AND c.isActive = true " +
           "ORDER BY c.sortOrder ASC")
    List<CategoryJpaEntity> findRootCategories();

    /**
     * 카테고리 이름으로 검색
     */
    @Query("SELECT c FROM CategoryJpaEntity c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND c.isActive = true " +
           "ORDER BY c.sortOrder ASC")
    List<CategoryJpaEntity> searchByName(@Param("name") String name);

    /**
     * 특정 카테고리의 모든 하위 카테고리 ID 조회 (재귀)
     * 단순 구현을 위해 직접 하위만 반환
     */
    @Query("SELECT c.id FROM CategoryJpaEntity c " +
           "WHERE c.parentId = :parentId " +
           "AND c.isActive = true")
    List<Long> findChildCategoryIds(@Param("parentId") Long parentId);

    /**
     * 카테고리 이름 중복 확인
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 카테고리 slug 중복 확인
     */
    boolean existsBySlugAndIdNot(String slug, Long id);
}