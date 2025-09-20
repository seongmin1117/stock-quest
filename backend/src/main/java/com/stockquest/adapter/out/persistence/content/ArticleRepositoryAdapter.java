package com.stockquest.adapter.out.persistence.content;

import com.stockquest.adapter.out.persistence.entity.ArticleJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ArticleJpaRepository;
import com.stockquest.domain.content.article.Article;
import com.stockquest.domain.content.article.ArticleDifficulty;
import com.stockquest.domain.content.article.ArticleStatus;
import com.stockquest.domain.content.article.SeoMetadata;
import com.stockquest.domain.content.article.port.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryAdapter implements ArticleRepository {

    private final ArticleJpaRepository jpaRepository;

    @Override
    public Optional<Article> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomainModel);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug)
                .map(this::toDomainModel);
    }

    @Override
    public List<Article> findBySearchCriteria(Long categoryId, Boolean featured, Integer limit, Integer offset) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        return jpaRepository.findBySearchCriteria(categoryId, featured, pageRequest)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Article> findFeaturedArticles(Integer limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return jpaRepository.findFeaturedArticles(pageRequest)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Article> findRecentArticles(Integer limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return jpaRepository.findRecentArticles(pageRequest)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(ArticleStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public Article save(Article article) {
        ArticleJpaEntity entity = toJpaEntity(article);
        ArticleJpaEntity saved = jpaRepository.save(entity);
        return toDomainModel(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Article toDomainModel(ArticleJpaEntity entity) {
        SeoMetadata seoMetadata = null;
        if (entity.getSeoTitle() != null || entity.getMetaDescription() != null || entity.getSeoKeywords() != null) {
            seoMetadata = new SeoMetadata(
                    entity.getSeoTitle(),
                    entity.getMetaDescription(),
                    entity.getSeoKeywords() != null ? List.of(entity.getSeoKeywords().split(",")) : null,
                    entity.getCanonicalUrl()
            );
        }

        return Article.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .content(entity.getContent())
                .summary(entity.getSummary())
                .authorId(entity.getAuthorId())
                .categoryId(entity.getCategoryId())
                .status(entity.getStatus())
                .difficulty(entity.getDifficulty())
                .featured(entity.getFeatured())
                .viewCount(entity.getViewCount())
                .readingTimeMinutes(entity.getReadingTimeMinutes())
                .seoMetadata(seoMetadata)
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ArticleJpaEntity toJpaEntity(Article article) {
        ArticleJpaEntity.ArticleJpaEntityBuilder builder = ArticleJpaEntity.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .content(article.getContent())
                .summary(article.getSummary())
                .authorId(article.getAuthorId())
                .categoryId(article.getCategoryId())
                .status(article.getStatus())
                .difficulty(article.getDifficulty())
                .featured(article.isFeatured())
                .viewCount(article.getViewCount())
                .readingTimeMinutes(article.getReadingTimeMinutes())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt());

        if (article.getSeoMetadata() != null) {
            SeoMetadata seo = article.getSeoMetadata();
            builder.seoTitle(seo.getSeoTitle())
                    .metaDescription(seo.getMetaDescription())
                    .seoKeywords(seo.getSeoKeywords() != null ? String.join(",", seo.getSeoKeywords()) : null)
                    .canonicalUrl(seo.getCanonicalUrl());
        }

        return builder.build();
    }
}