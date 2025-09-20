package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.content.article.ArticleDifficulty;
import com.stockquest.domain.content.article.ArticleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 블로그 글 JPA 엔티티
 */
@Entity
@Table(name = "article")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ArticleDifficulty difficulty;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    // SEO 메타데이터 필드들
    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "seo_keywords", length = 500)
    private String seoKeywords;

    @Column(name = "canonical_url", length = 255)
    private String canonicalUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) viewCount = 0L;
        if (likeCount == null) likeCount = 0L;
        if (commentCount == null) commentCount = 0L;
        if (featured == null) featured = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}