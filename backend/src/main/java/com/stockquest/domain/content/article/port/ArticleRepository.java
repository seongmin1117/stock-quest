package com.stockquest.domain.content.article.port;

import com.stockquest.domain.content.article.Article;
import com.stockquest.domain.content.article.ArticleStatus;
import com.stockquest.domain.content.article.ArticleDifficulty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 블로그 글 저장소 포트 (출력 포트)
 */
public interface ArticleRepository {

    /**
     * 글 저장
     */
    Article save(Article article);

    /**
     * ID로 글 조회
     */
    Optional<Article> findById(Long id);

    /**
     * slug로 글 조회 (SEO 친화적 URL)
     */
    Optional<Article> findBySlug(String slug);

    /**
     * 제목으로 글 조회 (중복 확인용)
     */
    Optional<Article> findByTitle(String title);

    /**
     * 상태별 글 목록 조회
     */
    List<Article> findByStatus(ArticleStatus status);

    /**
     * 카테고리별 발행된 글 목록 조회
     */
    List<Article> findPublishedByCategory(Long categoryId, int page, int size);

    /**
     * 작성자별 글 목록 조회
     */
    List<Article> findByAuthor(Long authorId, int page, int size);

    /**
     * 난이도별 발행된 글 목록 조회
     */
    List<Article> findPublishedByDifficulty(ArticleDifficulty difficulty, int page, int size);

    /**
     * 추천 글 목록 조회
     */
    List<Article> findFeaturedArticles(int limit);

    /**
     * 최근 발행된 글 목록 조회
     */
    List<Article> findRecentlyPublished(int limit);

    /**
     * 인기 글 목록 조회 (조회수 기준)
     */
    List<Article> findPopularArticles(int limit);

    /**
     * 관련 글 목록 조회 (같은 카테고리 또는 태그)
     */
    List<Article> findRelatedArticles(Long articleId, int limit);

    /**
     * 검색 (제목, 내용, 요약에서 검색)
     */
    List<Article> searchPublishedArticles(String keyword, int page, int size);

    /**
     * 고급 검색 (여러 조건 조합)
     */
    List<Article> searchArticles(
        String keyword,
        Long categoryId,
        ArticleDifficulty difficulty,
        ArticleStatus status,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        int page,
        int size
    );

    /**
     * 전체 발행된 글 수 조회
     */
    long countPublishedArticles();

    /**
     * 카테고리별 발행된 글 수 조회
     */
    long countPublishedByCategory(Long categoryId);

    /**
     * 작성자별 글 수 조회
     */
    long countByAuthor(Long authorId);

    /**
     * 특정 기간 내 발행된 글 수 조회
     */
    long countPublishedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * slug 중복 확인
     */
    boolean existsBySlug(String slug);

    /**
     * 제목 중복 확인
     */
    boolean existsByTitle(String title);

    /**
     * 글 삭제 (하드 삭제)
     */
    void deleteById(Long id);

    /**
     * 조회수 증가 (배치 처리용)
     */
    void incrementViewCount(Long articleId);

    /**
     * 좋아요 수 업데이트 (배치 처리용)
     */
    void updateLikeCount(Long articleId, Long likeCount);

    /**
     * 댓글 수 업데이트 (배치 처리용)
     */
    void updateCommentCount(Long articleId, Long commentCount);

    /**
     * 시간별 조회수 통계 조회
     */
    List<ViewStatistics> getViewStatistics(Long articleId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 조회수 통계 데이터 클래스
     */
    record ViewStatistics(
        LocalDateTime timestamp,
        Long viewCount,
        Long uniqueViewCount
    ) {}

    /**
     * slug와 상태로 글 조회 (SEO 친화적 URL + 상태 필터)
     */
    default Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status) {
        return findBySlug(slug)
                .filter(article -> article.getStatus() == status);
    }

    /**
     * 추천 발행된 글 목록 조회
     */
    default List<Article> findFeaturedPublished(int limit) {
        return findFeaturedArticles(limit).stream()
                .filter(article -> article.getStatus() == ArticleStatus.PUBLISHED)
                .limit(limit)
                .toList();
    }

    /**
     * 최근 발행된 글 목록 조회 (별칭)
     */
    default List<Article> findRecentPublished(int limit) {
        return findRecentlyPublished(limit);
    }
}