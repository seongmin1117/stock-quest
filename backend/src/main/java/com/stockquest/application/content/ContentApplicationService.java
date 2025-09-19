package com.stockquest.application.content;

import com.stockquest.application.content.dto.*;
import com.stockquest.application.content.port.in.GetArticlesUseCase;
import com.stockquest.application.content.port.in.GetCategoriesUseCase;
import com.stockquest.application.content.port.in.GetTagsUseCase;
import com.stockquest.application.content.port.in.ManageArticlesUseCase;
import com.stockquest.domain.content.article.Article;
import com.stockquest.domain.content.article.ArticleStatus;
import com.stockquest.domain.content.article.SeoMetadata;
import com.stockquest.domain.content.article.port.ArticleRepository;
import com.stockquest.domain.content.category.Category;
import com.stockquest.domain.content.category.port.CategoryRepository;
import com.stockquest.domain.content.tag.Tag;
import com.stockquest.domain.content.tag.TagType;
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
 * Implements Use Case interfaces for proper hexagonal architecture.
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
public class ContentApplicationService implements
    GetArticlesUseCase,
    GetCategoriesUseCase,
    GetTagsUseCase,
    ManageArticlesUseCase {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    /**
     * Get published articles with pagination and filtering
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
     * Get related articles
     */
    @Override
    public List<ArticleDto> getRelatedArticles(Long articleId, int limit) {
        log.debug("Getting {} related articles for article: {}", limit, articleId);

        if (articleId == null) {
            return List.of();
        }

        try {
            List<Article> articles = articleRepository.findRelatedArticles(articleId, Math.min(limit, 10));
            return articles.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting related articles: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get all active categories in hierarchical structure
     */
    @Override
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
     * Get category by ID
     */
    @Override
    public Optional<CategoryDto> getCategoryById(Long id) {
        log.debug("Getting category by id: {}", id);

        if (id == null) {
            return Optional.empty();
        }

        try {
            return categoryRepository.findById(id)
                    .filter(Category::isActive)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting category by id: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get featured categories for home page
     */
    @Override
    public List<CategoryDto> getFeaturedCategories() {
        log.debug("Getting featured categories");

        try {
            List<Category> categories = categoryRepository.findFeaturedCategories();
            return categories.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting featured categories: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get navigation categories
     */
    @Override
    public List<CategoryDto> getNavigationCategories() {
        log.debug("Getting navigation categories");

        try {
            List<Category> categories = categoryRepository.findNavigationCategories();
            return categories.stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting navigation categories: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get category by slug
     */
    @Override
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
    @Override
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
    @Override
    public List<TagDto> getTagsByType(TagType type) {
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
     * Get tag by ID
     */
    @Override
    public Optional<TagDto> getTagById(Long id) {
        log.debug("Getting tag by id: {}", id);

        if (id == null) {
            return Optional.empty();
        }

        try {
            return tagRepository.findById(id)
                    .filter(Tag::isActive)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting tag by id: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get tag by name
     */
    @Override
    public Optional<TagDto> getTagByName(String name) {
        log.debug("Getting tag by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return tagRepository.findByName(name.trim())
                    .filter(Tag::isActive)
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error getting tag by name: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Search tags for autocomplete
     */
    @Override
    public List<TagDto> searchTags(String keyword, int limit) {
        log.debug("Searching tags with keyword: {}, limit: {}", keyword, limit);

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        try {
            List<Tag> tags = tagRepository.searchByName(keyword.trim());
            return tags.stream()
                    .filter(Tag::isActive)
                    .limit(Math.min(limit, 20))
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching tags: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get tags by article ID
     */
    @Override
    public List<TagDto> getTagsByArticleId(Long articleId) {
        log.debug("Getting tags for article: {}", articleId);

        if (articleId == null) {
            return List.of();
        }

        try {
            List<ArticleTag> articleTags = articleTagRepository.findByArticleIdOrderByTagOrder(articleId);
            return articleTags.stream()
                    .map(articleTag -> tagRepository.findById(articleTag.getTagId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(Tag::isActive)
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting tags by article id: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // ===== ManageArticlesUseCase Implementation =====

    /**
     * Create new article
     */
    @Override
    @Transactional
    public ArticleDto createArticle(CreateArticleCommand command) {
        log.debug("Creating new article: {}", command.title());

        try {
            // 1. Create Article domain entity
            Article article = new Article(
                command.title(),
                command.content(),
                command.summary(),
                command.authorId(),
                command.categoryId(),
                command.difficulty()
            );

            // 2. Set featured status
            article.setFeatured(command.featured());

            // 3. Set SEO metadata if provided
            if (command.seoMetadata() != null) {
                SeoMetadata seoMetadata = convertToSeoMetadata(command.seoMetadata());
                article.setSeoMetadata(seoMetadata);
            }

            // 4. Save article to get ID
            Article savedArticle = articleRepository.save(article);
            log.debug("Article saved with ID: {}", savedArticle.getId());

            // 5. Create tag associations if tags provided
            if (command.tagIds() != null && !command.tagIds().isEmpty()) {
                createArticleTagAssociations(savedArticle.getId(), command.tagIds());
            }

            // 6. Convert to DTO and return
            return convertToDto(savedArticle);
        } catch (Exception e) {
            log.error("Error creating article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create article", e);
        }
    }

    /**
     * Update existing article
     */
    @Override
    @Transactional
    public ArticleDto updateArticle(Long articleId, UpdateArticleCommand command) {
        log.debug("Updating article: {}", articleId);

        try {
            // 1. Find existing article
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            if (articleOpt.isEmpty()) {
                throw new IllegalArgumentException("Article not found: " + articleId);
            }

            Article article = articleOpt.get();
            log.debug("Found article: {}", article.getTitle());

            // 2. Update basic fields if provided
            if (command.title() != null || command.content() != null ||
                command.summary() != null || command.categoryId() != null ||
                command.difficulty() != null) {

                article.update(
                    command.title() != null ? command.title() : article.getTitle(),
                    command.content() != null ? command.content() : article.getContent(),
                    command.summary() != null ? command.summary() : article.getSummary(),
                    command.categoryId() != null ? command.categoryId() : article.getCategoryId(),
                    command.difficulty() != null ? command.difficulty() : article.getDifficulty()
                );
            }

            // 3. Update featured status if provided
            if (command.featured() != null) {
                article.setFeatured(command.featured());
            }

            // 4. Update SEO metadata if provided
            if (command.seoMetadata() != null) {
                SeoMetadata seoMetadata = convertToSeoMetadata(command.seoMetadata());
                article.setSeoMetadata(seoMetadata);
            }

            // 5. Save article first to ensure it's persisted
            Article savedArticle = articleRepository.save(article);
            log.debug("Article updated successfully: {}", savedArticle.getId());

            // 6. Update tag associations if provided
            if (command.tagIds() != null) {
                updateArticleTagAssociations(savedArticle.getId(), command.tagIds());
            }

            // 7. Convert to DTO and return
            return convertToDto(savedArticle);
        } catch (IllegalArgumentException e) {
            log.warn("Update validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update article", e);
        }
    }

    /**
     * Publish article
     */
    @Override
    @Transactional
    public ArticleDto publishArticle(Long articleId) {
        log.debug("Publishing article: {}", articleId);

        try {
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            if (articleOpt.isEmpty()) {
                throw new IllegalArgumentException("Article not found: " + articleId);
            }

            Article article = articleOpt.get();
            article.publish();
            Article saved = articleRepository.save(article);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error publishing article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish article", e);
        }
    }

    /**
     * Unpublish article (change to draft)
     */
    @Override
    @Transactional
    public ArticleDto unpublishArticle(Long articleId) {
        log.debug("Unpublishing article: {}", articleId);

        try {
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            if (articleOpt.isEmpty()) {
                throw new IllegalArgumentException("Article not found: " + articleId);
            }

            Article article = articleOpt.get();
            article.unpublish();
            Article saved = articleRepository.save(article);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error unpublishing article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unpublish article", e);
        }
    }

    /**
     * Delete article
     */
    @Override
    @Transactional
    public void deleteArticle(Long articleId) {
        log.debug("Deleting article: {}", articleId);

        try {
            if (!articleRepository.findById(articleId).isPresent()) {
                throw new IllegalArgumentException("Article not found: " + articleId);
            }

            // Delete article tags first
            articleTagRepository.deleteByArticleId(articleId);

            // Delete article
            articleRepository.deleteById(articleId);
        } catch (Exception e) {
            log.error("Error deleting article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete article", e);
        }
    }

    /**
     * Increment view count
     */
    @Override
    @Transactional
    public void incrementViewCount(Long articleId) {
        log.debug("Incrementing view count for article: {}", articleId);

        try {
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            if (articleOpt.isPresent()) {
                Article article = articleOpt.get();
                article.incrementViewCount();
                articleRepository.save(article);
            }
        } catch (Exception e) {
            log.error("Error incrementing view count: {}", e.getMessage(), e);
            // Don't throw exception for view count failures
        }
    }

    /**
     * Set article featured status
     */
    @Override
    @Transactional
    public ArticleDto setFeatured(Long articleId, boolean featured) {
        log.debug("Setting featured status for article: {} to {}", articleId, featured);

        try {
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            if (articleOpt.isEmpty()) {
                throw new IllegalArgumentException("Article not found: " + articleId);
            }

            Article article = articleOpt.get();
            article.setFeatured(featured);
            Article saved = articleRepository.save(article);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error setting featured status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set featured status", e);
        }
    }

    /**
     * Internal method to search articles (used by both public and admin endpoints)
     */
    private ArticleSearchResult searchArticles(ArticleSearchQuery query) {
        log.debug("Searching articles with query: {}", query);

        try {
            List<Article> articles;
            long totalCount = 0;

            // Handle different search scenarios
            if (query.hasTagFilters()) {
                // Tag-based search - get articles that have specified tags
                articles = searchArticlesByTags(query);
                totalCount = countArticlesByTags(query);
            } else {
                // Use repository search methods
                int page = query.getOffsetOrDefault() / query.getLimitOrDefault();

                if (query.hasTextQuery() || query.hasCategoryFilter() || query.hasAuthorFilter()) {
                    // Advanced search with multiple filters
                    articles = articleRepository.searchArticles(
                        query.query(),
                        query.categoryId(),
                        query.difficulty(),
                        getEffectiveStatus(query),
                        null, // dateFrom - could be added to query in future
                        null, // dateTo - could be added to query in future
                        page,
                        query.getLimitOrDefault()
                    );
                    // Count would need a separate count method - for now estimate
                    totalCount = articles.size();
                } else {
                    // Simple published articles query
                    articles = query.isPublishedOnly() ?
                        articleRepository.findByStatus(ArticleStatus.PUBLISHED) :
                        articleRepository.findRecentlyPublished(query.getLimitOrDefault());
                    totalCount = articles.size();
                }
            }

            // Apply additional filters
            articles = applyAdditionalFilters(articles, query);

            // Convert to DTOs
            List<ArticleDto> articleDtos = articles.stream()
                    .map(this::convertToDto)
                    .toList();

            // Create paginated result
            return ArticleSearchResult.paginated(
                articleDtos,
                totalCount,
                query.getLimitOrDefault(),
                query.getOffsetOrDefault(),
                query.query(),
                query.categoryId(),
                query.tagIds(),
                query.getSortByOrDefault(),
                query.getSortDirectionOrDefault()
            );

        } catch (Exception e) {
            log.error("Error searching articles: {}", e.getMessage(), e);
            return ArticleSearchResult.empty(query.query(), query.categoryId(), query.tagIds());
        }
    }

    /**
     * Convert SeoMetadataCommand to SeoMetadata domain object (CreateArticleCommand)
     */
    private SeoMetadata convertToSeoMetadata(CreateArticleCommand.SeoMetadataCommand seoCommand) {
        return SeoMetadata.builder()
                .seoTitle(seoCommand.seoTitle())
                .metaDescription(seoCommand.metaDescription())
                .keywords(seoCommand.keywords())
                .canonicalUrl(seoCommand.canonicalUrl())
                .ogTitle(seoCommand.ogTitle())
                .ogDescription(seoCommand.ogDescription())
                .ogImageUrl(seoCommand.ogImageUrl())
                .twitterTitle(seoCommand.twitterTitle())
                .twitterDescription(seoCommand.twitterDescription())
                .twitterImageUrl(seoCommand.twitterImageUrl())
                .indexable(seoCommand.indexable())
                .followable(seoCommand.followable())
                .build();
    }

    /**
     * Convert SeoMetadataCommand to SeoMetadata domain object (UpdateArticleCommand)
     */
    private SeoMetadata convertToSeoMetadata(UpdateArticleCommand.SeoMetadataCommand seoCommand) {
        return SeoMetadata.builder()
                .seoTitle(seoCommand.seoTitle())
                .metaDescription(seoCommand.metaDescription())
                .keywords(seoCommand.keywords())
                .canonicalUrl(seoCommand.canonicalUrl())
                .ogTitle(seoCommand.ogTitle())
                .ogDescription(seoCommand.ogDescription())
                .ogImageUrl(seoCommand.ogImageUrl())
                .twitterTitle(seoCommand.twitterTitle())
                .twitterDescription(seoCommand.twitterDescription())
                .twitterImageUrl(seoCommand.twitterImageUrl())
                .indexable(seoCommand.indexable())
                .followable(seoCommand.followable())
                .build();
    }

    /**
     * Create article-tag associations
     */
    private void createArticleTagAssociations(Long articleId, List<Long> tagIds) {
        for (int i = 0; i < tagIds.size(); i++) {
            Long tagId = tagIds.get(i);
            ArticleTag articleTag = ArticleTag.builder()
                    .articleId(articleId)
                    .tagId(tagId)
                    .tagOrder(i + 1) // Order starts from 1
                    .relevanceScore(1.0) // Default relevance
                    .autoGenerated(false) // Manual assignment
                    .build();

            articleTagRepository.save(articleTag);
        }
        log.debug("Created {} tag associations for article {}", tagIds.size(), articleId);
    }

    /**
     * Update article-tag associations (remove old, add new)
     */
    private void updateArticleTagAssociations(Long articleId, List<Long> newTagIds) {
        log.debug("Updating tag associations for article {}", articleId);

        // 1. Remove existing tag associations
        articleTagRepository.deleteByArticleId(articleId);
        log.debug("Removed existing tag associations for article {}", articleId);

        // 2. Add new tag associations if provided
        if (newTagIds != null && !newTagIds.isEmpty()) {
            createArticleTagAssociations(articleId, newTagIds);
        } else {
            log.debug("No new tags to associate");
        }
    }

    /**
     * Search articles by tag IDs
     */
    private List<Article> searchArticlesByTags(ArticleSearchQuery query) {
        // Get article IDs that have all specified tags
        List<Long> articleIds = findArticleIdsByTags(query.tagIds());

        // Fetch articles by IDs and apply other filters
        return articleIds.stream()
                .map(articleRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(article -> matchesQuery(article, query))
                .limit(query.getLimitOrDefault())
                .toList();
    }

    /**
     * Count articles by tag IDs
     */
    private long countArticlesByTags(ArticleSearchQuery query) {
        List<Long> articleIds = findArticleIdsByTags(query.tagIds());
        return articleIds.stream()
                .map(articleRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(article -> matchesQuery(article, query))
                .count();
    }

    /**
     * Find article IDs that have all specified tags
     */
    private List<Long> findArticleIdsByTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }

        // For each tag, get articles that have it
        List<List<Long>> articleIdLists = tagIds.stream()
                .map(tagId -> articleTagRepository.findByTagId(tagId).stream()
                        .map(ArticleTag::getArticleId)
                        .toList())
                .toList();

        // Find intersection - articles that have ALL tags
        if (articleIdLists.isEmpty()) {
            return List.of();
        }

        List<Long> result = articleIdLists.get(0);
        for (int i = 1; i < articleIdLists.size(); i++) {
            result = result.stream()
                    .filter(articleIdLists.get(i)::contains)
                    .toList();
        }

        return result;
    }

    /**
     * Check if article matches search query
     */
    private boolean matchesQuery(Article article, ArticleSearchQuery query) {
        // Status filter
        if (query.isPublishedOnly() && article.getStatus() != ArticleStatus.PUBLISHED) {
            return false;
        }
        if (query.status() != null && article.getStatus() != query.status()) {
            return false;
        }

        // Category filter
        if (query.hasCategoryFilter() && !query.categoryId().equals(article.getCategoryId())) {
            return false;
        }

        // Author filter
        if (query.hasAuthorFilter() && !query.authorId().equals(article.getAuthorId())) {
            return false;
        }

        // Difficulty filter
        if (query.difficulty() != null && article.getDifficulty() != query.difficulty()) {
            return false;
        }

        // Text search in title, content, summary
        if (query.hasTextQuery()) {
            String searchText = query.query().toLowerCase();
            return article.getTitle().toLowerCase().contains(searchText) ||
                   article.getContent().toLowerCase().contains(searchText) ||
                   article.getSummary().toLowerCase().contains(searchText);
        }

        return true;
    }

    /**
     * Get effective status for repository search
     */
    private ArticleStatus getEffectiveStatus(ArticleSearchQuery query) {
        if (query.status() != null) {
            return query.status();
        }
        if (query.isPublishedOnly()) {
            return ArticleStatus.PUBLISHED;
        }
        return null; // No status filter
    }

    /**
     * Apply additional filters to article list
     */
    private List<Article> applyAdditionalFilters(List<Article> articles, ArticleSearchQuery query) {
        return articles.stream()
                .filter(article -> {
                    // Featured filter
                    if (query.isFeaturedOnly() && !article.isFeatured()) {
                        return false;
                    }
                    return true;
                })
                .toList();
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