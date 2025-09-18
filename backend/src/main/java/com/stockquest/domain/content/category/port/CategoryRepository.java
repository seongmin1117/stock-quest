package com.stockquest.domain.content.category.port;

import com.stockquest.domain.content.category.Category;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 저장소 포트 (출력 포트)
 */
public interface CategoryRepository {

    /**
     * 카테고리 저장
     */
    Category save(Category category);

    /**
     * ID로 카테고리 조회
     */
    Optional<Category> findById(Long id);

    /**
     * slug로 카테고리 조회
     */
    Optional<Category> findBySlug(String slug);

    /**
     * 이름으로 카테고리 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 모든 활성화된 카테고리 조회 (계층 구조)
     */
    List<Category> findAllActiveOrderByParentAndSort();

    /**
     * 최상위 카테고리 목록 조회 (정렬 순서대로)
     */
    List<Category> findRootCategoriesOrderBySort();

    /**
     * 특정 부모의 하위 카테고리 목록 조회
     */
    List<Category> findByParentIdOrderBySort(Long parentId);

    /**
     * 카테고리 계층 경로 조회 (부모 → 자식 순서)
     */
    List<Category> findCategoryPath(Long categoryId);

    /**
     * 카테고리와 모든 하위 카테고리 ID 목록 조회
     */
    List<Long> findCategoryAndDescendantIds(Long categoryId);

    /**
     * 추천 카테고리 목록 조회
     */
    List<Category> findFeaturedCategories();

    /**
     * 메인 페이지 노출 카테고리 목록 조회
     */
    List<Category> findHomepageCategories();

    /**
     * 네비게이션 메뉴용 카테고리 목록 조회
     */
    List<Category> findNavigationCategories();

    /**
     * 검색 가능한 카테고리 목록 조회
     */
    List<Category> findSearchableCategories();

    /**
     * RSS 피드 포함 카테고리 목록 조회
     */
    List<Category> findRssIncludedCategories();

    /**
     * 글이 있는 카테고리 목록 조회
     */
    List<Category> findCategoriesWithArticles();

    /**
     * 카테고리별 글 수 통계 조회
     */
    List<CategoryStats> getCategoryStats();

    /**
     * 특정 깊이 레벨의 카테고리 조회
     */
    List<Category> findByDepthLevel(int depth);

    /**
     * 이름으로 카테고리 검색 (부분 일치)
     */
    List<Category> searchByName(String keyword);

    /**
     * 전체 카테고리 수 조회
     */
    long countAll();

    /**
     * 활성화된 카테고리 수 조회
     */
    long countActive();

    /**
     * 특정 부모의 하위 카테고리 수 조회
     */
    long countByParentId(Long parentId);

    /**
     * slug 중복 확인
     */
    boolean existsBySlug(String slug);

    /**
     * 이름 중복 확인 (같은 부모 내에서)
     */
    boolean existsByNameAndParentId(String name, Long parentId);

    /**
     * 특정 카테고리가 다른 카테고리의 조상인지 확인
     */
    boolean isAncestorOf(Long ancestorId, Long descendantId);

    /**
     * 카테고리 삭제 (하드 삭제)
     */
    void deleteById(Long id);

    /**
     * 카테고리 글 수 업데이트 (배치 처리용)
     */
    void updateArticleCount(Long categoryId, Long articleCount);

    /**
     * 모든 카테고리의 글 수 재계산
     */
    void recalculateAllArticleCounts();

    /**
     * 정렬 순서 일괄 업데이트
     */
    void updateSortOrders(List<CategorySortUpdate> updates);

    /**
     * 카테고리 통계 데이터 클래스
     */
    record CategoryStats(
        Long categoryId,
        String categoryName,
        String categorySlug,
        Long articleCount,
        Long publishedArticleCount,
        Double averageViewCount,
        Long totalLikeCount
    ) {}

    /**
     * 정렬 순서 업데이트 데이터 클래스
     */
    record CategorySortUpdate(
        Long categoryId,
        Integer sortOrder
    ) {}

    /**
     * 카테고리 트리 구조 조회 (재귀적)
     */
    List<CategoryTreeNode> getCategoryTree();

    /**
     * 카테고리 트리 노드 데이터 클래스
     */
    record CategoryTreeNode(
        Category category,
        List<CategoryTreeNode> children,
        int depth,
        boolean hasArticles
    ) {}

    /**
     * 카테고리 이동 (부모 변경) 시 유효성 검증
     */
    boolean canMoveCategory(Long categoryId, Long newParentId);

    /**
     * 카테고리 순서 변경을 위한 인접 카테고리 조회
     */
    List<Category> findAdjacentCategories(Long categoryId, Long parentId);

    /**
     * 빈 카테고리 (글이 없는 카테고리) 조회
     */
    List<Category> findEmptyCategories();

    /**
     * 사용되지 않는 카테고리 조회 (하위 카테고리도 글도 없는 카테고리)
     */
    List<Category> findUnusedCategories();

    /**
     * 모든 활성화된 카테고리 조회 (계층 구조) - 별칭
     */
    default List<Category> findAllActiveOrderByHierarchy() {
        return findAllActiveOrderByParentAndSort();
    }

    /**
     * slug와 활성화 상태로 카테고리 조회
     */
    default Optional<Category> findBySlugAndActive(String slug, boolean active) {
        return findBySlug(slug)
                .filter(category -> category.isActive() == active);
    }
}