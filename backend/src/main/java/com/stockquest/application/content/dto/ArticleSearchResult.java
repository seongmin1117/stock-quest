package com.stockquest.application.content.dto;

import lombok.Builder;

import java.util.List;

/**
 * Article Search Result - Application Layer
 *
 * Result object containing search results and pagination metadata
 */
@Builder
public record ArticleSearchResult(
    List<ArticleDto> articles,
    Long totalCount,
    Integer currentPage,
    Integer pageSize,
    Integer totalPages,
    String query,
    Long categoryId,
    List<Long> tagIds,
    String sortBy,
    String sortDirection,
    Boolean hasNext,
    Boolean hasPrevious
) {

    /**
     * Create paginated search result
     */
    public static ArticleSearchResult paginated(
            List<ArticleDto> articles,
            long totalCount,
            int pageSize,
            int offset,
            String query,
            Long categoryId,
            List<Long> tagIds,
            String sortBy,
            String sortDirection) {

        int currentPage = (offset / pageSize) + 1;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = currentPage < totalPages;
        boolean hasPrevious = currentPage > 1;

        return ArticleSearchResult.builder()
                .articles(articles)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .query(query)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    /**
     * Create empty search result
     */
    public static ArticleSearchResult empty(String query, Long categoryId, List<Long> tagIds) {
        return ArticleSearchResult.builder()
                .articles(List.of())
                .totalCount(0L)
                .currentPage(1)
                .pageSize(20)
                .totalPages(0)
                .query(query)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    /**
     * Check if result has articles
     */
    public boolean hasArticles() {
        return articles != null && !articles.isEmpty();
    }

    /**
     * Get result size
     */
    public int size() {
        return articles != null ? articles.size() : 0;
    }

    /**
     * Get pagination info display
     */
    public String getPaginationDisplay() {
        if (totalCount == 0) {
            return "검색 결과가 없습니다";
        }
        return String.format("총 %d개 중 %d페이지 (%d~%d번)",
                totalCount, currentPage,
                ((currentPage - 1) * pageSize) + 1,
                Math.min(currentPage * pageSize, totalCount.intValue()));
    }
}