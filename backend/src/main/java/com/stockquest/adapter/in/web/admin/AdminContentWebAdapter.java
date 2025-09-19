package com.stockquest.adapter.in.web.admin;

import com.stockquest.application.content.ContentApplicationService;
import com.stockquest.application.content.dto.*;
import com.stockquest.adapter.in.web.content.dto.*;
import com.stockquest.domain.content.article.ArticleStatus;
import com.stockquest.domain.content.article.ArticleDifficulty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/content")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Content Management", description = "Admin APIs for managing blog content")
public class AdminContentWebAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminContentWebAdapter.class);
    private final ContentApplicationService contentApplicationService;

    // Basic status endpoint to verify admin access
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        log.info("Admin checking content status");
        return ResponseEntity.ok("Content admin interface is ready. All CRUD operations are now available.");
    }

    // ===== Article Management Endpoints =====

    /**
     * Create new article
     */
    @PostMapping("/articles")
    public ResponseEntity<ArticleResponse> createArticle(
            @Valid @RequestBody CreateArticleRequest request) {
        log.info("Admin creating article: {}", request.title());

        try {
            CreateArticleCommand command = CreateArticleCommand.builder()
                    .title(request.title())
                    .content(request.content())
                    .summary(request.summary())
                    .authorId(request.authorId())
                    .categoryId(request.categoryId())
                    .difficulty(request.difficulty())
                    .featured(request.featured() != null ? request.featured() : false)
                    .tagIds(request.tagIds())
                    .seoMetadata(convertToSeoCommand(request.seoMetadata()))
                    .build();

            ArticleDto result = contentApplicationService.createArticle(command);
            ArticleResponse response = convertToWebResponse(result);

            log.info("Article created successfully with ID: {}", result.id());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid article creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating article: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update existing article
     */
    @PutMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        log.info("Admin updating article: {}", id);

        try {
            UpdateArticleCommand command = UpdateArticleCommand.builder()
                    .title(request.title())
                    .content(request.content())
                    .summary(request.summary())
                    .categoryId(request.categoryId())
                    .difficulty(request.difficulty())
                    .featured(request.featured())
                    .tagIds(request.tagIds())
                    .seoMetadata(convertToUpdateSeoCommand(request.seoMetadata()))
                    .build();

            ArticleDto result = contentApplicationService.updateArticle(id, command);
            ArticleResponse response = convertToWebResponse(result);

            log.info("Article updated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Article not found or invalid update request: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating article: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete article
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        log.info("Admin deleting article: {}", id);

        try {
            contentApplicationService.deleteArticle(id);
            log.info("Article deleted successfully: {}", id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("Article not found for deletion: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting article: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Publish article
     */
    @PostMapping("/articles/{id}/publish")
    public ResponseEntity<ArticleResponse> publishArticle(@PathVariable Long id) {
        log.info("Admin publishing article: {}", id);

        try {
            ArticleDto result = contentApplicationService.publishArticle(id);
            ArticleResponse response = convertToWebResponse(result);

            log.info("Article published successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Article not found for publishing: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error publishing article: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Unpublish article (change to draft)
     */
    @PostMapping("/articles/{id}/unpublish")
    public ResponseEntity<ArticleResponse> unpublishArticle(@PathVariable Long id) {
        log.info("Admin unpublishing article: {}", id);

        try {
            ArticleDto result = contentApplicationService.unpublishArticle(id);
            ArticleResponse response = convertToWebResponse(result);

            log.info("Article unpublished successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Article not found for unpublishing: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error unpublishing article: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Set article featured status
     */
    @PatchMapping("/articles/{id}/featured")
    public ResponseEntity<ArticleResponse> setFeatured(
            @PathVariable Long id,
            @RequestParam boolean featured) {
        log.info("Admin setting featured status for article: {} to {}", id, featured);

        try {
            ArticleDto result = contentApplicationService.setFeatured(id, featured);
            ArticleResponse response = convertToWebResponse(result);

            log.info("Article featured status updated: {} -> {}", id, featured);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Article not found for featured update: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating featured status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all articles (including drafts and unpublished)
     */
    @GetMapping("/articles")
    public ResponseEntity<ArticleSearchResponse> getAllArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) ArticleDifficulty difficulty,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "updated_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset) {

        log.debug("Admin getting all articles: query={}, status={}, limit={}",
                 query, status, limit);

        try {
            ArticleSearchQuery searchQuery = ArticleSearchQuery.builder()
                    .query(query)
                    .categoryId(categoryId)
                    .tagIds(tagIds)
                    .status(status)
                    .difficulty(difficulty)
                    .featured(featured)
                    .publishedOnly(false) // Admin can see all articles
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .limit(limit)
                    .offset(offset)
                    .build();

            ArticleSearchResult result = contentApplicationService.searchPublishedArticles(searchQuery);
            ArticleSearchResponse response = convertToWebResponse(result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting admin articles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get article by ID (admin access)
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable Long id) {
        log.debug("Admin getting article by ID: {}", id);

        try {
            return contentApplicationService.getArticleById(id)
                    .map(this::convertToWebResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error getting article by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Category Management Endpoints =====

    /**
     * Create new category
     */
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("Admin creating category: {}", request.name());

        try {
            // TODO: Implement createCategory in ContentApplicationService
            // CategoryDto result = contentApplicationService.createCategory(command);
            // CategoryResponse response = convertToWebResponse(result);
            // return ResponseEntity.ok(response);

            log.warn("Category creation not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update existing category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("Admin updating category: {}", id);

        try {
            // TODO: Implement updateCategory in ContentApplicationService
            // CategoryDto result = contentApplicationService.updateCategory(id, command);
            // CategoryResponse response = convertToWebResponse(result);
            // return ResponseEntity.ok(response);

            log.warn("Category update not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete category
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Admin deleting category: {}", id);

        try {
            // TODO: Implement deleteCategory in ContentApplicationService
            // contentApplicationService.deleteCategory(id);

            log.warn("Category deletion not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error deleting category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all categories (admin view with inactive ones)
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesAdmin() {
        log.debug("Admin getting all categories");

        try {
            // This method already exists in ContentApplicationService
            List<CategoryDto> categories = contentApplicationService.getAllCategories();
            List<CategoryResponse> response = categories.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting admin categories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get category by ID (admin access)
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryByIdAdmin(@PathVariable Long id) {
        log.debug("Admin getting category by ID: {}", id);

        try {
            return contentApplicationService.getCategoryById(id)
                    .map(this::convertToWebResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error getting category by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Tag Management Endpoints =====

    /**
     * Create new tag
     */
    @PostMapping("/tags")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        log.info("Admin creating tag: {}", request.name());

        try {
            // TODO: Implement createTag in ContentApplicationService
            // TagDto result = contentApplicationService.createTag(command);
            // TagResponse response = convertToWebResponse(result);
            // return ResponseEntity.ok(response);

            log.warn("Tag creation not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error creating tag: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update existing tag
     */
    @PutMapping("/tags/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        log.info("Admin updating tag: {}", id);

        try {
            // TODO: Implement updateTag in ContentApplicationService
            // TagDto result = contentApplicationService.updateTag(id, command);
            // TagResponse response = convertToWebResponse(result);
            // return ResponseEntity.ok(response);

            log.warn("Tag update not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error updating tag: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete tag
     */
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("Admin deleting tag: {}", id);

        try {
            // TODO: Implement deleteTag in ContentApplicationService
            // contentApplicationService.deleteTag(id);

            log.warn("Tag deletion not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error deleting tag: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search tags for autocomplete
     */
    @GetMapping("/tags/search")
    public ResponseEntity<List<TagResponse>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        log.debug("Admin searching tags with keyword: {}, limit: {}", keyword, limit);

        try {
            // This method already exists in ContentApplicationService
            List<TagDto> tags = contentApplicationService.searchTags(keyword, limit);
            List<TagResponse> response = tags.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching tags: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all tags (admin view)
     */
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTagsAdmin(
            @RequestParam(required = false) com.stockquest.domain.content.tag.TagType type,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer limit) {
        log.debug("Admin getting tags by type: {}, limit: {}", type, limit);

        try {
            List<TagDto> tags;
            if (type != null) {
                tags = contentApplicationService.getTagsByType(type);
            } else {
                // Get popular tags as default admin view
                tags = contentApplicationService.getPopularTags(limit);
            }

            List<TagResponse> response = tags.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting admin tags: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get tag by ID (admin access)
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<TagResponse> getTagByIdAdmin(@PathVariable Long id) {
        log.debug("Admin getting tag by ID: {}", id);

        try {
            return contentApplicationService.getTagById(id)
                    .map(this::convertToWebResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error getting tag by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Analytics Endpoints =====

    /**
     * Get admin dashboard stats
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        log.debug("Admin getting dashboard stats");

        try {
            // TODO: Implement dashboard analytics in ContentApplicationService
            // AdminDashboardResponse response = contentApplicationService.getDashboardStats();
            // return ResponseEntity.ok(response);

            log.warn("Dashboard analytics not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error getting dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get article analytics
     */
    @GetMapping("/analytics/articles/{id}")
    public ResponseEntity<ArticleAnalyticsResponse> getArticleAnalytics(@PathVariable Long id) {
        log.debug("Admin getting analytics for article: {}", id);

        try {
            // TODO: Implement article analytics in ContentApplicationService
            // ArticleAnalyticsResponse response = contentApplicationService.getArticleAnalytics(id);
            // return ResponseEntity.ok(response);

            log.warn("Article analytics not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error getting article analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top performing articles
     */
    @GetMapping("/analytics/top-articles")
    public ResponseEntity<ArticleStatsResponse> getTopArticles(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        log.debug("Admin getting top articles for {} days, limit: {}", days, limit);

        try {
            // TODO: Implement top articles analytics in ContentApplicationService
            // ArticleStatsResponse response = contentApplicationService.getTopArticles(days, limit);
            // return ResponseEntity.ok(response);

            log.warn("Top articles analytics not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error getting top articles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Helper Methods =====

    /**
     * Convert SeoMetadataRequest to CreateArticleCommand.SeoMetadataCommand
     */
    private CreateArticleCommand.SeoMetadataCommand convertToSeoCommand(SeoMetadataRequest request) {
        if (request == null) {
            return null;
        }

        return CreateArticleCommand.SeoMetadataCommand.builder()
                .seoTitle(request.seoTitle())
                .metaDescription(request.metaDescription())
                .keywords(request.keywords())
                .canonicalUrl(request.canonicalUrl())
                .build();
    }

    /**
     * Convert SeoMetadataRequest to UpdateArticleCommand.SeoMetadataCommand
     */
    private UpdateArticleCommand.SeoMetadataCommand convertToUpdateSeoCommand(SeoMetadataRequest request) {
        if (request == null) {
            return null;
        }

        return UpdateArticleCommand.SeoMetadataCommand.builder()
                .seoTitle(request.seoTitle())
                .metaDescription(request.metaDescription())
                .keywords(request.keywords())
                .canonicalUrl(request.canonicalUrl())
                .build();
    }

    /**
     * Convert ArticleDto to ArticleResponse
     */
    private ArticleResponse convertToWebResponse(ArticleDto dto) {
        List<TagResponse> tagResponses = dto.tags() != null ?
            dto.tags().stream().map(this::convertToWebResponse).toList() :
            List.of();

        return ArticleResponse.builder()
                .id(dto.id())
                .title(dto.title())
                .slug(dto.slug())
                .summary(dto.summary())
                .content(dto.content())
                .authorId(dto.authorId())
                .authorNickname(dto.authorNickname())
                .categoryId(dto.categoryId())
                .categoryName(dto.categoryName())
                .status(dto.status())
                .featured(dto.featured())
                .viewCount(dto.viewCount())
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .readingTimeMinutes(dto.readingTimeMinutes())
                .difficulty(dto.difficulty())
                .seoTitle(dto.seoTitle())
                .metaDescription(dto.metaDescription())
                .seoKeywords(dto.seoKeywords())
                .canonicalUrl(dto.canonicalUrl())
                .ogTitle(dto.ogTitle())
                .ogDescription(dto.ogDescription())
                .ogImageUrl(dto.ogImageUrl())
                .twitterTitle(dto.twitterTitle())
                .twitterDescription(dto.twitterDescription())
                .twitterImageUrl(dto.twitterImageUrl())
                .indexable(dto.indexable())
                .followable(dto.followable())
                .schemaType(dto.schemaType())
                .tags(tagResponses)
                .publishedAt(dto.publishedAt())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }

    /**
     * Convert ArticleSearchResult to ArticleSearchResponse
     */
    private ArticleSearchResponse convertToWebResponse(ArticleSearchResult result) {
        List<ArticleResponse> articleResponses = result.articles().stream()
                .map(this::convertToWebResponse)
                .toList();

        return ArticleSearchResponse.builder()
                .articles(articleResponses)
                .totalCount(result.totalCount())
                .currentPage(result.currentPage())
                .pageSize(result.pageSize())
                .totalPages(result.totalPages())
                .query(result.query())
                .categoryId(result.categoryId())
                .tagIds(result.tagIds())
                .sortBy(result.sortBy())
                .sortDirection(result.sortDirection())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    /**
     * Convert TagDto to TagResponse
     */
    private TagResponse convertToWebResponse(TagDto dto) {
        return TagResponse.builder()
                .id(dto.id())
                .name(dto.name())
                .slug(dto.slug())
                .description(dto.description())
                .type(dto.type())
                .colorCode(dto.colorCode())
                .usageCount(dto.usageCount())
                .popular(dto.popular())
                .active(dto.active())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }

    /**
     * Convert CategoryDto to CategoryResponse
     */
    private CategoryResponse convertToWebResponse(CategoryDto dto) {
        List<CategoryResponse> childrenResponses = dto.children() != null ?
            dto.children().stream().map(this::convertToWebResponse).toList() :
            List.of();

        return CategoryResponse.builder()
                .id(dto.id())
                .name(dto.name())
                .slug(dto.slug())
                .description(dto.description())
                .parentId(dto.parentId())
                .parentName(dto.parentName())
                .colorCode(dto.colorCode())
                .icon(dto.icon())
                .articleCount(dto.articleCount())
                .articleCountDisplay(dto.getArticleCountDisplay())
                .sortOrder(dto.sortOrder())
                .active(dto.active())
                .featuredOnHome(dto.featuredOnHome())
                .seoTitle(dto.seoTitle())
                .metaDescription(dto.metaDescription())
                .seoKeywords(dto.seoKeywords())
                .children(childrenResponses)
                .level(dto.level())
                .levelIndent(dto.getLevelIndent())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }
}