package com.stockquest.application.content.port.in;

import com.stockquest.application.content.dto.ArticleDto;
import com.stockquest.application.content.dto.ArticleSearchQuery;
import com.stockquest.application.content.dto.ArticleSearchResult;

import java.util.List;
import java.util.Optional;

/**
 * 블로그 글 조회 Use Case (입력 포트)
 *
 * 헥사고날 아키텍처의 입력 포트로서, 블로그 글 조회와 관련된 모든 비즈니스 로직을 정의합니다.
 * 이 인터페이스는 웹 어댑터(Controller)에서 호출되며, Application Service에서 구현됩니다.
 */
public interface GetArticlesUseCase {

    /**
     * 발행된 글 검색 (공개 API용)
     *
     * @param query 검색 조건
     * @return 검색 결과
     */
    ArticleSearchResult searchPublishedArticles(ArticleSearchQuery query);

    /**
     * 슬러그로 발행된 글 조회 (SEO 친화적 URL)
     *
     * @param slug 글 슬러그
     * @return 글 정보 (발행된 글만)
     */
    Optional<ArticleDto> getPublishedArticleBySlug(String slug);

    /**
     * ID로 글 조회
     *
     * @param id 글 ID
     * @return 글 정보
     */
    Optional<ArticleDto> getArticleById(Long id);

    /**
     * 추천 글 목록 조회 (홈페이지용)
     *
     * @param limit 조회할 글 수
     * @return 추천 글 목록
     */
    List<ArticleDto> getFeaturedArticles(int limit);

    /**
     * 최근 글 목록 조회
     *
     * @param limit 조회할 글 수
     * @return 최근 글 목록
     */
    List<ArticleDto> getRecentArticles(int limit);

    /**
     * 관련 글 목록 조회
     *
     * @param articleId 기준 글 ID
     * @param limit 조회할 글 수
     * @return 관련 글 목록
     */
    List<ArticleDto> getRelatedArticles(Long articleId, int limit);
}