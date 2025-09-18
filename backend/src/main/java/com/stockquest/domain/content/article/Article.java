package com.stockquest.domain.content.article;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 투자 교육 블로그 글 도메인 엔티티
 * SEO 최적화와 투자 교육 컨텐츠 관리를 위한 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Article {

    private Long id;

    /**
     * 글 제목
     */
    private String title;

    /**
     * URL slug (SEO 친화적 URL)
     */
    private String slug;

    /**
     * 글 요약 (검색 결과 및 카드에 표시)
     */
    private String summary;

    /**
     * 글 본문 (Markdown 형식)
     */
    private String content;

    /**
     * 작성자 ID
     */
    private Long authorId;

    /**
     * 카테고리 ID
     */
    private Long categoryId;

    /**
     * 발행 상태
     */
    private ArticleStatus status;

    /**
     * 추천 글 여부
     */
    private boolean featured;

    /**
     * 조회수
     */
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 좋아요 수
     */
    @Builder.Default
    private Long likeCount = 0L;

    /**
     * 댓글 수
     */
    @Builder.Default
    private Long commentCount = 0L;

    /**
     * 예상 읽기 시간 (분)
     */
    private Integer readingTimeMinutes;

    /**
     * 난이도 (초급, 중급, 고급)
     */
    private ArticleDifficulty difficulty;

    /**
     * SEO 메타데이터
     */
    private SeoMetadata seoMetadata;

    /**
     * 발행일
     */
    private LocalDateTime publishedAt;

    /**
     * 생성일
     */
    private LocalDateTime createdAt;

    /**
     * 수정일
     */
    private LocalDateTime updatedAt;

    /**
     * 도메인 생성자
     */
    public Article(String title, String content, String summary, Long authorId, Long categoryId, ArticleDifficulty difficulty) {
        validateTitle(title);
        validateContent(content);
        validateSummary(summary);

        this.title = title;
        this.slug = generateSlug(title);
        this.content = content;
        this.summary = summary;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.status = ArticleStatus.DRAFT;
        this.featured = false;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
        this.readingTimeMinutes = calculateReadingTime(content);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자를 초과할 수 없습니다");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("본문은 필수입니다");
        }
        if (content.length() < 100) {
            throw new IllegalArgumentException("본문은 최소 100자 이상이어야 합니다");
        }
    }

    private void validateSummary(String summary) {
        if (summary == null || summary.trim().isEmpty()) {
            throw new IllegalArgumentException("요약은 필수입니다");
        }
        if (summary.length() > 500) {
            throw new IllegalArgumentException("요약은 500자를 초과할 수 없습니다");
        }
    }

    /**
     * 제목으로부터 URL slug 생성
     */
    private String generateSlug(String title) {
        return title.toLowerCase()
                   .replaceAll("[^a-z0-9가-힣\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }

    /**
     * 컨텐츠 길이 기반 읽기 시간 계산 (분)
     * 한국어 기준 분당 약 300자 읽기 속도
     */
    private Integer calculateReadingTime(String content) {
        int wordsPerMinute = 300;
        int contentLength = content.length();
        return Math.max(1, contentLength / wordsPerMinute);
    }

    /**
     * 글 발행
     */
    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("이미 발행된 글입니다");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 글 발행 취소 (초안으로 되돌리기)
     */
    public void unpublish() {
        if (this.status != ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("발행된 글만 발행 취소할 수 있습니다");
        }
        this.status = ArticleStatus.DRAFT;
        this.publishedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 추천 글로 설정
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 글 수정
     */
    public void update(String title, String content, String summary, Long categoryId, ArticleDifficulty difficulty) {
        validateTitle(title);
        validateContent(content);
        validateSummary(summary);

        this.title = title;
        this.slug = generateSlug(title);
        this.content = content;
        this.summary = summary;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.readingTimeMinutes = calculateReadingTime(content);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 수 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 수 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 댓글 수 업데이트
     */
    public void updateCommentCount(Long commentCount) {
        this.commentCount = commentCount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * SEO 메타데이터 설정
     */
    public void setSeoMetadata(SeoMetadata seoMetadata) {
        this.seoMetadata = seoMetadata;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 발행 가능 여부 확인
     */
    public boolean canPublish() {
        return this.status == ArticleStatus.DRAFT &&
               this.title != null && !this.title.trim().isEmpty() &&
               this.content != null && !this.content.trim().isEmpty() &&
               this.summary != null && !this.summary.trim().isEmpty();
    }

    /**
     * 발행된 글인지 확인
     */
    public boolean isPublished() {
        return this.status == ArticleStatus.PUBLISHED;
    }

    /**
     * 추천 글인지 확인
     */
    public boolean isFeatured() {
        return this.featured;
    }
}