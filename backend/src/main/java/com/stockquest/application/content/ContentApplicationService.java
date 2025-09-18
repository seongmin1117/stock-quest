package com.stockquest.application.content;

import com.stockquest.application.content.dto.*;
import com.stockquest.domain.content.article.Article;
import com.stockquest.domain.content.article.port.ArticleRepository;
import com.stockquest.domain.content.category.Category;
import com.stockquest.domain.content.category.port.CategoryRepository;
import com.stockquest.domain.content.tag.Tag;
import com.stockquest.domain.content.tag.port.TagRepository;
import com.stockquest.domain.content.articletag.ArticleTag;
import com.stockquest.domain.content.articletag.port.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Content Application Service - Application Layer
 *
 * Orchestrates blog content business logic following clean architecture principles.
 * Handles articles, categories, tags, and their relationships.
 *
 * This service is responsible for:
 * - Converting between domain entities and DTOs
 * - Orchestrating complex business operations
 * - Transaction management
 * - Input validation and error handling
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContentApplicationService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    /**
     * Get published articles with pagination and filtering
     */
    public ArticleSearchResult searchPublishedArticles(ArticleSearchQuery query) {
        log.debug("Searching published articles with query: {}", query);

        try {
            // For public access, always ensure we only get published articles
            ArticleSearchQuery publishedQuery = ArticleSearchQuery.builder()
                    .query(query.query())
                    .categoryId(query.categoryId())
                    .tagIds(query.tagIds())
                    .status(com.stockquest.domain.content.article.ArticleStatus.PUBLISHED)
                    .difficulty(query.difficulty())
                    .featured(query.featured())
                    .publishedOnly(true)
                    .sortBy(query.getSortByOrDefault())
                    .sortDirection(query.getSortDirectionOrDefault())
                    .limit(query.getLimitOrDefault())
                    .offset(query.getOffsetOrDefault())
                    .build();

            return searchArticles(publishedQuery);
        } catch (Exception e) {
            log.error("Error searching published articles: {}", e.getMessage(), e);
            return ArticleSearchResult.empty(query.query(), query.categoryId(), query.tagIds());
        }
    }

    /**
     * Get article by slug (for SEO-friendly URLs)
     */
    public Optional<ArticleDto> getPublishedArticleBySlug(String slug) {
        log.debug("Getting published article by slug: {}", slug);

        if (slug == null || slug.trim().isEmpty()) {
            log.warn("Attempted to get article with empty slug");
            return Optional.empty();
        }

        try {
            return articleRepository.findBySlugAndStatus(slug.trim(),
                    com.stockquest.domain.content.article.ArticleStatus.PUBLISHED)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting article by slug: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get article by ID
     */
    public Optional<ArticleDto> getArticleById(Long id) {
        log.debug("Getting article by id: {}", id);

        if (id == null) {
            return Optional.empty();
        }

        try {
            return articleRepository.findById(id)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting article by id: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get featured articles for home page
     */
    public List<ArticleDto> getFeaturedArticles(int limit) {
        log.debug("Getting {} featured articles", limit);

        try {
            List<Article> articles = articleRepository.findFeaturedPublished(Math.min(limit, 20));
            return articles.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting featured articles: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get recent articles
     */
    public List<ArticleDto> getRecentArticles(int limit) {
        log.debug("Getting {} recent articles", limit);

        try {
            List<Article> articles = articleRepository.findRecentPublished(Math.min(limit, 20));
            return articles.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting recent articles: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get all active categories in hierarchical structure
     */
    public List<CategoryDto> getAllCategories() {
        log.debug("Getting all active categories");

        try {
            List<Category> categories = categoryRepository.findAllActiveOrderByHierarchy();
            return categories.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get category by slug
     */
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        log.debug("Getting category by slug: {}", slug);

        if (slug == null || slug.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return categoryRepository.findBySlugAndActive(slug.trim(), true)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting category by slug: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get popular tags
     */
    public List<TagDto> getPopularTags(int limit) {
        log.debug("Getting {} popular tags", limit);

        try {
            List<Tag> tags = tagRepository.findPopularActive(Math.min(limit, 50));
            return tags.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting popular tags: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get tags by type
     */
    public List<TagDto> getTagsByType(com.stockquest.domain.content.tag.TagType type) {
        log.debug("Getting tags by type: {}", type);

        try {
            List<Tag> tags = tagRepository.findByTypeAndActive(type, true);
            return tags.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting tags by type: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Internal method to search articles (used by both public and admin endpoints)
     */
    private ArticleSearchResult searchArticles(ArticleSearchQuery query) {
        // TODO: Implement actual search logic with repository
        // For now, return empty result
        return ArticleSearchResult.empty(query.query(), query.categoryId(), query.tagIds());
    }

    /**
     * Convert Article domain entity to DTO
     */
    private ArticleDto convertToDto(Article article) {
        // Get article tags
        List<ArticleTag> articleTags = articleTagRepository.findByArticleIdOrderByTagOrder(article.getId());
        List<TagDto> tags = articleTags.stream()
                .map(articleTag -> tagRepository.findById(articleTag.getTagId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToDto)
                .toList();

        // Get category info
        Category category = categoryRepository.findById(article.getCategoryId()).orElse(null);
        String categoryName = category != null ? category.getName() : null;

        // Get SEO metadata
        var seoMetadata = article.getSeoMetadata();

        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .summary(article.getSummary())
                .content(article.getContent())
                .authorId(article.getAuthorId())
                .authorNickname("작성자") // TODO: Get from user repository
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .status(article.getStatus())
                .featured(article.isFeatured())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .readingTimeMinutes(article.getReadingTimeMinutes())
                .difficulty(article.getDifficulty())
                .seoTitle(seoMetadata != null ? seoMetadata.getSeoTitle() : null)
                .metaDescription(seoMetadata != null ? seoMetadata.getMetaDescription() : null)
                .seoKeywords(seoMetadata != null ? seoMetadata.getKeywordsAsString() : null)
                .canonicalUrl(seoMetadata != null ? seoMetadata.getCanonicalUrl() : null)
                .ogTitle(seoMetadata != null ? seoMetadata.getOgTitle() : null)
                .ogDescription(seoMetadata != null ? seoMetadata.getOgDescription() : null)
                .ogImageUrl(seoMetadata != null ? seoMetadata.getOgImageUrl() : null)
                .twitterCardType(seoMetadata != null ? seoMetadata.getTwitterCardType() : null)
                .twitterTitle(seoMetadata != null ? seoMetadata.getTwitterTitle() : null)
                .twitterDescription(seoMetadata != null ? seoMetadata.getTwitterDescription() : null)
                .twitterImageUrl(seoMetadata != null ? seoMetadata.getTwitterImageUrl() : null)
                .indexable(seoMetadata != null ? seoMetadata.isIndexable() : true)
                .followable(seoMetadata != null ? seoMetadata.isFollowable() : true)
                .schemaType(seoMetadata != null ? seoMetadata.getSchemaType() : null)
                .tags(tags)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    /**
     * Convert Category domain entity to DTO
     */
    private CategoryDto convertToDto(Category category) {
        // Get parent category name if exists
        String parentName = null;
        if (category.getParentId() != null) {
            parentName = categoryRepository.findById(category.getParentId())
                    .map(Category::getName)
                    .orElse(null);
        }

        // Get metadata for SEO fields
        var metadata = category.getMetadata();

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .parentName(parentName)
                .colorCode(category.getColorCode())
                .icon(category.getIcon())
                .articleCount(category.getArticleCount())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .featuredOnHome(metadata != null ? metadata.isShowOnHomepage() : false)
                .seoTitle(metadata != null ? metadata.getSeoTitle() : null)
                .metaDescription(metadata != null ? metadata.getMetaDescription() : null)
                .seoKeywords(metadata != null ? metadata.getSeoKeywordsAsString() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Convert Tag domain entity to DTO
     */
    private TagDto convertToDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .description(tag.getDescription())
                .type(tag.getType())
                .colorCode(tag.getColorCode())
                .usageCount(tag.getUsageCount())
                .popular(tag.isPopular())
                .active(tag.isActive())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}