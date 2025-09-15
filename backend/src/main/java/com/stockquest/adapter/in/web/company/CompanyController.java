package com.stockquest.adapter.in.web.company;

import com.stockquest.adapter.in.web.company.dto.*;
import com.stockquest.application.company.CompanyService;
import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.CompanyCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 회사 정보 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company API", description = "회사 정보 관련 API")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/search")
    @Operation(
        summary = "회사 검색",
        description = "회사명(한글/영문), 심볼로 검색합니다. 자동완성에 사용됩니다."
    )
    public ResponseEntity<CompanySearchResponse> searchCompanies(
            @Parameter(description = "검색어") @RequestParam(required = false) String q,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) String category,
            @Parameter(description = "섹터") @RequestParam(required = false) String sector,
            @Parameter(description = "결과 제한 수") @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("회사 검색 API 호출: q={}, category={}, sector={}, limit={}", q, category, sector, limit);

        List<Company> companies;

        if (category != null || sector != null) {
            // 복합 검색
            companies = companyService.searchCompanies(q, category, sector);
        } else {
            // 단순 검색
            companies = companyService.searchCompanies(q);
        }

        // 제한 수 적용
        if (companies.size() > limit) {
            companies = companies.subList(0, limit);
        }

        List<CompanyDto> companyDtos = companies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new CompanySearchResponse(companyDtos));
    }

    @GetMapping("/popular")
    @Operation(
        summary = "인기 회사 목록",
        description = "인기도 순으로 정렬된 회사 목록을 반환합니다."
    )
    public ResponseEntity<List<PopularCompanyDto>> getPopularCompanies(
            @Parameter(description = "결과 제한 수") @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("인기 회사 목록 API 호출: limit={}", limit);

        List<Company> companies = companyService.getPopularCompanies(limit);

        List<PopularCompanyDto> popularCompanies = companies.stream()
                .map(this::convertToPopularDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(popularCompanies);
    }

    @GetMapping("/categories")
    @Operation(
        summary = "회사 카테고리 목록",
        description = "회사 수와 함께 카테고리 목록을 반환합니다."
    )
    public ResponseEntity<List<CategoryDto>> getCategories() {
        log.debug("카테고리 목록 API 호출");

        Map<CompanyCategory, Long> categoriesWithCount = companyService.getCategoriesWithCompanyCount();

        List<CategoryDto> categories = categoriesWithCount.entrySet().stream()
                .map(entry -> new CategoryDto(
                        entry.getKey().getCategoryId(),
                        entry.getKey().getNameKr(),
                        entry.getKey().getNameEn(),
                        entry.getKey().getDescriptionKr(),
                        entry.getValue().intValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "카테고리별 회사 목록",
        description = "특정 카테고리에 속한 회사들을 반환합니다."
    )
    public ResponseEntity<CompanySearchResponse> getCompaniesByCategory(
            @Parameter(description = "카테고리 ID") @PathVariable String categoryId,
            @Parameter(description = "결과 제한 수") @RequestParam(defaultValue = "50") int limit
    ) {
        log.debug("카테고리별 회사 목록 API 호출: categoryId={}, limit={}", categoryId, limit);

        List<Company> companies = companyService.getCompaniesByCategory(categoryId);

        if (companies.size() > limit) {
            companies = companies.subList(0, limit);
        }

        List<CompanyDto> companyDtos = companies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new CompanySearchResponse(companyDtos));
    }

    @GetMapping("/{symbol}")
    @Operation(
        summary = "회사 상세 정보",
        description = "심볼을 이용해 회사의 상세 정보를 조회합니다."
    )
    public ResponseEntity<CompanyDetailDto> getCompanyBySymbol(
            @Parameter(description = "회사 심볼") @PathVariable String symbol
    ) {
        log.debug("회사 상세 정보 API 호출: symbol={}", symbol);

        return companyService.getCompanyBySymbol(symbol)
                .map(this::convertToDetailDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/korean-market")
    @Operation(
        summary = "한국 시장 회사 목록",
        description = "한국 시장(KRX)에 상장된 회사들을 반환합니다."
    )
    public ResponseEntity<CompanySearchResponse> getKoreanMarketCompanies(
            @Parameter(description = "결과 제한 수") @RequestParam(defaultValue = "100") int limit
    ) {
        log.debug("한국 시장 회사 목록 API 호출: limit={}", limit);

        List<Company> companies = companyService.getKoreanMarketCompanies();

        if (companies.size() > limit) {
            companies = companies.subList(0, limit);
        }

        List<CompanyDto> companyDtos = companies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new CompanySearchResponse(companyDtos));
    }

    @GetMapping("/sector/{sector}")
    @Operation(
        summary = "섹터별 회사 목록",
        description = "특정 섹터에 속한 회사들을 반환합니다."
    )
    public ResponseEntity<CompanySearchResponse> getCompaniesBySector(
            @Parameter(description = "섹터명") @PathVariable String sector,
            @Parameter(description = "결과 제한 수") @RequestParam(defaultValue = "50") int limit
    ) {
        log.debug("섹터별 회사 목록 API 호출: sector={}, limit={}", sector, limit);

        List<Company> companies = companyService.getCompaniesBySector(sector);

        if (companies.size() > limit) {
            companies = companies.subList(0, limit);
        }

        List<CompanyDto> companyDtos = companies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new CompanySearchResponse(companyDtos));
    }

    /**
     * Company 엔티티를 CompanyDto로 변환
     */
    private CompanyDto convertToDto(Company company) {
        return new CompanyDto(
                company.getSymbol(),
                company.getNameKr(),
                company.getNameEn(),
                company.getSector(),
                company.getLogoPath(),
                company.getMarketCapDisplay()
        );
    }

    /**
     * Company 엔티티를 PopularCompanyDto로 변환
     */
    private PopularCompanyDto convertToPopularDto(Company company) {
        return new PopularCompanyDto(
                company.getSymbol(),
                company.getNameKr(),
                company.getNameEn(),
                company.getFormattedMarketCap(),
                company.getSector(),
                company.getLogoPath(),
                company.getPopularityScore()
        );
    }

    /**
     * Company 엔티티를 CompanyDetailDto로 변환
     */
    private CompanyDetailDto convertToDetailDto(Company company) {
        return new CompanyDetailDto(
                company.getSymbol(),
                company.getNameKr(),
                company.getNameEn(),
                company.getSector(),
                company.getMarketCap(),
                company.getMarketCapDisplay(),
                company.getLogoPath(),
                company.getDescriptionKr(),
                company.getDescriptionEn(),
                company.getExchange(),
                company.getCurrency(),
                company.getPopularityScore()
        );
    }
}