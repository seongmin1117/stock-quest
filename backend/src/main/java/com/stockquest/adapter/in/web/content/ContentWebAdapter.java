package com.stockquest.adapter.in.web.content;

import com.stockquest.adapter.in.web.content.dto.*;
import com.stockquest.application.content.ContentApplicationService;
import com.stockquest.application.content.dto.*;
import com.stockquest.domain.content.tag.TagType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Content Web Adapter - Presentation Layer
 *
 * REST controller for blog content endpoints (articles, categories, tags).
 * This adapter converts HTTP requests to application service calls
 * and formats responses for the web layer.
 *
 * Provides public APIs for:
 * - Article browsing and search
 * - Category navigation
 * - Tag exploration
 * - Featured content
 *
 * All endpoints return only published content for public access.
 */
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Blog Content", description = "Blog articles, categories, and tags APIs")
public class ContentWebAdapter {

    private final ContentApplicationService contentApplicationService;

    /**
     * Search published articles with filters and pagination
     */
    @GetMapping("/articles")
    @Operation(summary = "Search published articles",
               description = "Search and filter published blog articles with pagination support")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Articles retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ArticleSearchResponse> searchArticles(
            @Parameter(description = "Search query text")
            @RequestParam(required = false) String query,

            @Parameter(description = "Category ID filter")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Tag ID filters (comma-separated)")
            @RequestParam(required = false) List<Long> tagIds,

            @Parameter(description = "Difficulty filter")
            @RequestParam(required = false) com.stockquest.domain.content.article.ArticleDifficulty difficulty,

            @Parameter(description = "Featured articles only")
            @RequestParam(required = false) Boolean featured,

            @Parameter(description = "Sort by field (title, published_at, view_count, like_count)")
            @RequestParam(defaultValue = "published_at") String sortBy,

            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDirection,

            @Parameter(description = "Number of articles per page (1-100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Offset for pagination (0+)")
            @RequestParam(defaultValue = "0") @Min(0) Integer offset) {

        log.debug("Searching articles: query={}, categoryId={}, limit={}, offset={}",
                 query, categoryId, limit, offset);

        try {
            ArticleSearchQuery searchQuery = ArticleSearchQuery.builder()
                    .query(query)
                    .categoryId(categoryId)
                    .tagIds(tagIds)
                    .difficulty(difficulty)
                    .featured(featured)
                    .publishedOnly(true)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .limit(limit)
                    .offset(offset)
                    .build();

            ArticleSearchResult result = contentApplicationService.searchPublishedArticles(searchQuery);
            ArticleSearchResponse response = convertToWebResponse(result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching articles: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get article by slug (SEO-friendly URL)
     */
    @GetMapping("/articles/{slug}")
    @Operation(summary = "Get article by slug",
               description = "Retrieve a published article by its SEO-friendly slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Article found"),
        @ApiResponse(responseCode = "404", description = "Article not found")
    })
    public ResponseEntity<ArticleResponse> getArticleBySlug(
            @Parameter(description = "Article slug")
            @PathVariable @NotBlank String slug) {

        log.debug("Getting article by slug: {}", slug);

        return contentApplicationService.getPublishedArticleBySlug(slug)
                .map(this::convertToWebResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get featured articles for home page
     */
    @GetMapping("/articles/featured")
    @Operation(summary = "Get featured articles",
               description = "Retrieve featured articles for display on home page")
    @ApiResponse(responseCode = "200", description = "Featured articles retrieved")
    public ResponseEntity<List<ArticleResponse>> getFeaturedArticles(
            @Parameter(description = "Maximum number of articles (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) Integer limit) {

        log.debug("Getting {} featured articles", limit);

        try {
            List<ArticleDto> articles = contentApplicationService.getFeaturedArticles(limit);
            List<ArticleResponse> response = articles.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting featured articles: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get recent articles
     */
    @GetMapping("/articles/recent")
    @Operation(summary = "Get recent articles",
               description = "Retrieve recently published articles")
    @ApiResponse(responseCode = "200", description = "Recent articles retrieved")
    public ResponseEntity<List<ArticleResponse>> getRecentArticles(
            @Parameter(description = "Maximum number of articles (1-20)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit) {

        log.debug("Getting {} recent articles", limit);

        try {
            List<ArticleDto> articles = contentApplicationService.getRecentArticles(limit);
            List<ArticleResponse> response = articles.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting recent articles: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get all categories in hierarchical structure
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all categories",
               description = "Retrieve all active categories in hierarchical structure")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.debug("Getting all categories");

        try {
            List<CategoryDto> categories = contentApplicationService.getAllCategories();
            List<CategoryResponse> response = categories.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get category by slug
     */
    @GetMapping("/categories/{slug}")
    @Operation(summary = "Get category by slug",
               description = "Retrieve a category by its SEO-friendly slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category slug")
            @PathVariable @NotBlank String slug) {

        log.debug("Getting category by slug: {}", slug);

        return contentApplicationService.getCategoryBySlug(slug)
                .map(this::convertToWebResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get popular tags
     */
    @GetMapping("/tags/popular")
    @Operation(summary = "Get popular tags",
               description = "Retrieve popular tags for tag cloud or navigation")
    @ApiResponse(responseCode = "200", description = "Popular tags retrieved")
    public ResponseEntity<List<TagResponse>> getPopularTags(
            @Parameter(description = "Maximum number of tags (1-50)")
            @RequestParam(defaultValue = "30") @Min(1) @Max(50) Integer limit) {

        log.debug("Getting {} popular tags", limit);

        try {
            List<TagDto> tags = contentApplicationService.getPopularTags(limit);
            List<TagResponse> response = tags.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting popular tags: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get tags by type
     */
    @GetMapping("/tags/by-type/{type}")
    @Operation(summary = "Get tags by type",
               description = "Retrieve tags filtered by investment type")
    @ApiResponse(responseCode = "200", description = "Tags retrieved by type")
    public ResponseEntity<List<TagResponse>> getTagsByType(
            @Parameter(description = "Tag type")
            @PathVariable TagType type) {

        log.debug("Getting tags by type: {}", type);

        try {
            List<TagDto> tags = contentApplicationService.getTagsByType(type);
            List<TagResponse> response = tags.stream()
                    .map(this::convertToWebResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting tags by type: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Convert application DTO to web response
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
                .readingTimeDisplay(dto.getReadingTimeDisplay())
                .difficulty(dto.difficulty())
                .difficultyDisplay(dto.getDifficultyDisplay())
                .seoTitle(dto.seoTitle())
                .metaDescription(dto.metaDescription())
                .seoKeywords(dto.seoKeywords())
                .canonicalUrl(dto.canonicalUrl())
                .ogTitle(dto.ogTitle())
                .ogDescription(dto.ogDescription())
                .ogImageUrl(dto.ogImageUrl())
                .twitterCardType(dto.twitterCardType())
                .twitterTitle(dto.twitterTitle())
                .twitterDescription(dto.twitterDescription())
                .twitterImageUrl(dto.twitterImageUrl())
                .indexable(dto.indexable())
                .followable(dto.followable())
                .schemaType(dto.schemaType())
                .tags(tagResponses)
                .tagNames(dto.getTagNames())
                .publishedAt(dto.publishedAt())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }

    /**
     * Convert application search result to web response
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
                .paginationDisplay(result.getPaginationDisplay())
                .build();
    }

    /**
     * Convert category DTO to web response
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

    /**
     * Convert tag DTO to web response
     */
    private TagResponse convertToWebResponse(TagDto dto) {
        return TagResponse.builder()
                .id(dto.id())
                .name(dto.name())
                .slug(dto.slug())
                .description(dto.description())
                .type(dto.type())
                .typeDisplay(dto.getTypeDisplay())
                .colorCode(dto.colorCode())
                .usageCount(dto.usageCount())
                .popular(dto.popular())
                .active(dto.active())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }
}