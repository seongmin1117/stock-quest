package com.stockquest.application.content.port.in;

import com.stockquest.application.content.dto.ArticleDto;
import com.stockquest.application.content.dto.CreateArticleCommand;
import com.stockquest.application.content.dto.UpdateArticleCommand;

/**
 * 블로그 글 관리 Use Case (입력 포트)
 *
 * 글 생성, 수정, 삭제 등의 관리 기능을 정의합니다.
 * 관리자 권한이 필요한 기능들을 포함합니다.
 */
public interface ManageArticlesUseCase {

    /**
     * 새 글 작성
     *
     * @param command 글 작성 명령
     * @return 생성된 글 정보
     */
    ArticleDto createArticle(CreateArticleCommand command);

    /**
     * 글 수정
     *
     * @param articleId 글 ID
     * @param command 글 수정 명령
     * @return 수정된 글 정보
     */
    ArticleDto updateArticle(Long articleId, UpdateArticleCommand command);

    /**
     * 글 발행
     *
     * @param articleId 글 ID
     * @return 발행된 글 정보
     */
    ArticleDto publishArticle(Long articleId);

    /**
     * 글 발행 취소 (임시저장으로 변경)
     *
     * @param articleId 글 ID
     * @return 변경된 글 정보
     */
    ArticleDto unpublishArticle(Long articleId);

    /**
     * 글 삭제
     *
     * @param articleId 글 ID
     */
    void deleteArticle(Long articleId);

    /**
     * 글 조회수 증가
     *
     * @param articleId 글 ID
     */
    void incrementViewCount(Long articleId);

    /**
     * 글 추천 설정/해제
     *
     * @param articleId 글 ID
     * @param featured 추천 여부
     * @return 변경된 글 정보
     */
    ArticleDto setFeatured(Long articleId, boolean featured);
}