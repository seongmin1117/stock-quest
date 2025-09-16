package com.stockquest.adapter.in.web.company;

import com.stockquest.adapter.in.web.company.dto.CompanyResponse;
import com.stockquest.adapter.in.web.company.dto.CompanyCategoryResponse;
import com.stockquest.adapter.in.web.company.dto.CompanySearchRequest;
import com.stockquest.adapter.in.web.company.dto.CompanySearchResponse;
import com.stockquest.application.company.CompanyApplicationService;
import com.stockquest.application.company.dto.CompanyDto;
import com.stockquest.application.company.dto.CompanyCategoryDto;
import com.stockquest.application.company.dto.CompanySearchQuery;
import com.stockquest.application.company.dto.CompanySearchResult;
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
import java.util.Optional;

/**
 * Company Web Adapter - Presentation Layer
 *
 * REST controller for company-related endpoints.
 * This adapter converts HTTP requests to application service calls
 * and formats responses for the web layer.
 *
 * Follows hexagonal architecture by:
 * - Using application services (not domain directly)
 * - Converting between web DTOs and application DTOs
 * - Handling HTTP-specific concerns (status codes, validation)
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Companies", description = "Company and category management APIs")
public class CompanyWebAdapter {

    private final CompanyApplicationService companyApplicationService;

    @Operation(summary = "Get company by symbol", description = "Retrieve a specific company by its stock symbol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{symbol}")
    public ResponseEntity<CompanyResponse> getCompanyBySymbol(
            @Parameter(description = "Stock symbol (e.g., 005930)", example = "005930")
            @PathVariable @NotBlank String symbol) {

        log.info("GET /api/v1/companies/{} - Getting company by symbol", symbol);

        Optional<CompanyDto> company = companyApplicationService.getCompanyBySymbol(symbol);

        return company.map(dto -> {
            log.info("Found company: {} ({})", dto.nameKr(), dto.symbol());
            return ResponseEntity.ok(convertToResponse(dto));
        }).orElseGet(() -> {
            log.warn("Company not found for symbol: {}", symbol);
            return ResponseEntity.notFound().build();
        });
    }

    @Operation(summary = "Search companies", description = "Search companies with filters and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @PostMapping("/search")
    public ResponseEntity<CompanySearchResponse> searchCompanies(
            @Parameter(description = "Search parameters")
            @Valid @RequestBody CompanySearchRequest request) {

        log.info("POST /api/v1/companies/search - Searching companies with query: {}", request.query());

        CompanySearchQuery query = convertToQuery(request);
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        CompanySearchResponse response = convertToSearchResponse(result);
        log.info("Search completed: found {} companies out of {} total",
                response.companies().size(), response.totalCount());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get companies by category", description = "Get all companies in a specific category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CompanyResponse>> getCompaniesByCategory(
            @Parameter(description = "Category ID", example = "tech")
            @PathVariable @NotBlank String categoryId) {

        log.info("GET /api/v1/companies/category/{} - Getting companies by category", categoryId);

        List<CompanyDto> companies = companyApplicationService.getCompaniesByCategory(categoryId);
        List<CompanyResponse> response = companies.stream()
                .map(this::convertToResponse)
                .toList();

        log.info("Found {} companies in category: {}", response.size(), categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get top companies", description = "Get top companies by popularity")
    @GetMapping("/top")
    public ResponseEntity<List<CompanyResponse>> getTopCompanies(
            @Parameter(description = "Number of companies to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        log.info("GET /api/v1/companies/top?limit={} - Getting top companies", limit);

        List<CompanyDto> companies = companyApplicationService.getTopCompaniesByPopularity(limit);
        List<CompanyResponse> response = companies.stream()
                .map(this::convertToResponse)
                .toList();

        log.info("Found {} top companies", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all categories", description = "Get all active company categories")
    @GetMapping("/categories")
    public ResponseEntity<List<CompanyCategoryResponse>> getAllCategories() {

        log.info("GET /api/v1/companies/categories - Getting all categories");

        List<CompanyCategoryDto> categories = companyApplicationService.getAllCategories();
        List<CompanyCategoryResponse> response = categories.stream()
                .map(this::convertToResponse)
                .toList();

        log.info("Found {} categories", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search companies (GET)", description = "Search companies using query parameters")
    @GetMapping("/search")
    public ResponseEntity<CompanySearchResponse> searchCompaniesGet(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String q,
            @Parameter(description = "Category filter")
            @RequestParam(required = false) List<String> categories,
            @Parameter(description = "Sector filter")
            @RequestParam(required = false) String sector,
            @Parameter(description = "Minimum popularity score")
            @RequestParam(required = false) Integer minPopularity,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "popularityScore") String sort,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String order,
            @Parameter(description = "Number of results")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(description = "Offset for pagination")
            @RequestParam(defaultValue = "0") @Min(0) int offset) {

        log.info("GET /api/v1/companies/search - Searching companies with query: {}", q);

        CompanySearchQuery query = CompanySearchQuery.builder()
                .query(q)
                .categories(categories)
                .sector(sector)
                .minPopularityScore(minPopularity)
                .sortBy(sort)
                .sortDirection(order)
                .limit(limit)
                .offset(offset)
                .activeOnly(true)
                .build();

        CompanySearchResult result = companyApplicationService.searchCompanies(query);
        CompanySearchResponse response = convertToSearchResponse(result);

        log.info("Search completed: found {} companies", response.companies().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Convert application DTO to web response DTO
     */
    private CompanyResponse convertToResponse(CompanyDto dto) {
        return CompanyResponse.builder()
                .id(dto.id())
                .symbol(dto.symbol())
                .nameKr(dto.nameKr())
                .nameEn(dto.nameEn())
                .sector(dto.sector())
                .marketCap(dto.marketCap())
                .marketCapDisplay(dto.marketCapDisplay())
                .logoPath(dto.logoPath())
                .descriptionKr(dto.descriptionKr())
                .descriptionEn(dto.descriptionEn())
                .exchange(dto.exchange())
                .currency(dto.currency())
                .isActive(dto.isActive())
                .popularityScore(dto.popularityScore())
                .categories(dto.categories())
                .build();
    }

    /**
     * Convert category DTO to web response DTO
     */
    private CompanyCategoryResponse convertToResponse(CompanyCategoryDto dto) {
        return CompanyCategoryResponse.builder()
                .id(dto.id())
                .categoryId(dto.categoryId())
                .nameKr(dto.nameKr())
                .nameEn(dto.nameEn())
                .descriptionKr(dto.descriptionKr())
                .descriptionEn(dto.descriptionEn())
                .sortOrder(dto.sortOrder())
                .isActive(dto.isActive())
                .companyCount(dto.companyCount())
                .build();
    }

    /**
     * Convert search request to application query
     */
    private CompanySearchQuery convertToQuery(CompanySearchRequest request) {
        return CompanySearchQuery.builder()
                .query(request.query())
                .categories(request.categories())
                .sector(request.sector())
                .minMarketCap(request.minMarketCap())
                .maxMarketCap(request.maxMarketCap())
                .minPopularityScore(request.minPopularityScore())
                .activeOnly(true) // Always filter to active companies
                .sortBy(request.sortBy())
                .sortDirection(request.sortDirection())
                .limit(request.limit())
                .offset(request.offset())
                .build();
    }

    /**
     * Convert search result to web response
     */
    private CompanySearchResponse convertToSearchResponse(CompanySearchResult result) {
        List<CompanyResponse> companies = result.companies().stream()
                .map(this::convertToResponse)
                .toList();

        return CompanySearchResponse.builder()
                .companies(companies)
                .totalCount(result.totalCount())
                .limit(result.limit())
                .offset(result.offset())
                .hasMore(result.hasMore())
                .currentPage(result.getCurrentPage())
                .totalPages(result.getTotalPages())
                .searchQuery(result.searchQuery())
                .appliedCategories(result.appliedCategories())
                .build();
    }
}