package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ArticleJpaEntity;
import com.stockquest.domain.content.article.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Article JPA Repository
 */
public interface ArticleJpaRepository extends JpaRepository<ArticleJpaEntity, Long> {

    /**
     * slug로 글 찾기
     */
    Optional<ArticleJpaEntity> findBySlug(String slug);

    /**
     * 검색 조건에 따른 글 목록 조회
     */
    @Query("SELECT a FROM ArticleJpaEntity a " +
           "WHERE (:categoryId IS NULL OR a.categoryId = :categoryId) " +
           "AND (:featured IS NULL OR a.featured = :featured) " +
           "AND a.status = 'PUBLISHED' " +
           "ORDER BY a.publishedAt DESC")
    Page<ArticleJpaEntity> findBySearchCriteria(@Param("categoryId") Long categoryId,
                                                @Param("featured") Boolean featured,
                                                Pageable pageable);

    /**
     * 추천 글 목록 조회
     */
    @Query("SELECT a FROM ArticleJpaEntity a " +
           "WHERE a.featured = true " +
           "AND a.status = 'PUBLISHED' " +
           "ORDER BY a.publishedAt DESC")
    List<ArticleJpaEntity> findFeaturedArticles(Pageable pageable);

    /**
     * 최신 글 목록 조회
     */
    @Query("SELECT a FROM ArticleJpaEntity a " +
           "WHERE a.status = 'PUBLISHED' " +
           "ORDER BY a.publishedAt DESC")
    List<ArticleJpaEntity> findRecentArticles(Pageable pageable);

    /**
     * 상태별 글 개수
     */
    long countByStatus(ArticleStatus status);

    /**
     * 카테고리별 글 개수
     */
    long countByCategoryIdAndStatus(Long categoryId, ArticleStatus status);

    /**
     * 작성자별 글 목록 조회
     */
    @Query("SELECT a FROM ArticleJpaEntity a " +
           "WHERE a.authorId = :authorId " +
           "AND a.status = 'PUBLISHED' " +
           "ORDER BY a.publishedAt DESC")
    Page<ArticleJpaEntity> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * 제목 또는 내용으로 검색
     */
    @Query("SELECT a FROM ArticleJpaEntity a " +
           "WHERE (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND a.status = 'PUBLISHED' " +
           "ORDER BY a.publishedAt DESC")
    Page<ArticleJpaEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}