package com.stockquest.application.company;

import com.stockquest.application.company.dto.CompanyDto;
import com.stockquest.application.company.dto.CompanyCategoryDto;
import com.stockquest.application.company.dto.CompanySearchQuery;
import com.stockquest.application.company.dto.CompanySearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Company Application Service Test - TDD for Application Layer
 *
 * Tests the application service that orchestrates company business logic
 * following clean architecture principles.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Company Application Service Tests")
class CompanyApplicationServiceTest {

    @Autowired
    private CompanyApplicationService companyApplicationService;

    @Test
    @DisplayName("Should get company by symbol")
    void shouldGetCompanyBySymbol() {
        // When
        Optional<CompanyDto> company = companyApplicationService.getCompanyBySymbol("005930");

        // Then
        assertThat(company).isPresent();
        CompanyDto companyDto = company.get();
        assertThat(companyDto.symbol()).isEqualTo("005930");
        assertThat(companyDto.nameKr()).isEqualTo("삼성전자");
        assertThat(companyDto.nameEn()).isEqualTo("Samsung Electronics");
        assertThat(companyDto.sector()).isEqualTo("반도체");
        assertThat(companyDto.exchange()).isEqualTo("KRX");
        assertThat(companyDto.currency()).isEqualTo("KRW");
        assertThat(companyDto.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should search companies with query")
    void shouldSearchCompaniesWithQuery() {
        // Given
        CompanySearchQuery query = CompanySearchQuery.builder()
                .query("삼성")
                .categories(List.of("tech", "semiconductor"))
                .limit(10)
                .build();

        // When
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        // Then
        assertThat(result.companies()).isNotEmpty();
        assertThat(result.companies())
                .extracting(CompanyDto::nameKr)
                .allMatch(name -> name.contains("삼성"));
        assertThat(result.totalCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should get all categories")
    void shouldGetAllCategories() {
        // When
        List<CompanyCategoryDto> categories = companyApplicationService.getAllCategories();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories)
                .extracting(CompanyCategoryDto::categoryId)
                .containsAnyOf("tech", "semiconductor", "automotive", "finance");

        // Categories should be sorted by sort order
        assertThat(categories).isSortedAccordingTo(
                (c1, c2) -> Integer.compare(c1.sortOrder(), c2.sortOrder())
        );
    }

    @Test
    @DisplayName("Should get companies by category")
    void shouldGetCompaniesByCategory() {
        // When
        List<CompanyDto> techCompanies = companyApplicationService.getCompaniesByCategory("tech");

        // Then
        assertThat(techCompanies).isNotEmpty();
        assertThat(techCompanies)
                .extracting(CompanyDto::symbol)
                .containsAnyOf("005930", "035720", "035420"); // Samsung, Kakao, Naver
    }

    @Test
    @DisplayName("Should get top companies by popularity")
    void shouldGetTopCompaniesByPopularity() {
        // When
        List<CompanyDto> topCompanies = companyApplicationService.getTopCompaniesByPopularity(5);

        // Then
        assertThat(topCompanies).hasSize(5);
        assertThat(topCompanies).isSortedAccordingTo(
                (c1, c2) -> Integer.compare(c2.popularityScore(), c1.popularityScore())
        );
        // Samsung should be #1 with highest popularity
        assertThat(topCompanies.get(0).symbol()).isEqualTo("005930");
    }

    @Test
    @DisplayName("Should search companies by English name")
    void shouldSearchCompaniesByEnglishName() {
        // Given
        CompanySearchQuery query = CompanySearchQuery.builder()
                .query("Samsung")
                .limit(10)
                .build();

        // When
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        // Then
        assertThat(result.companies()).isNotEmpty();
        assertThat(result.companies())
                .extracting(CompanyDto::nameEn)
                .allMatch(name -> name.contains("Samsung"));
    }

    @Test
    @DisplayName("Should filter companies by multiple categories")
    void shouldFilterCompaniesByMultipleCategories() {
        // Given
        CompanySearchQuery query = CompanySearchQuery.builder()
                .categories(List.of("tech", "semiconductor"))
                .limit(20)
                .build();

        // When
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        // Then
        assertThat(result.companies()).isNotEmpty();
        // Should include Samsung (both tech and semiconductor)
        assertThat(result.companies())
                .extracting(CompanyDto::symbol)
                .contains("005930");
    }

    @Test
    @DisplayName("Should return empty for non-existent company")
    void shouldReturnEmptyForNonExistentCompany() {
        // When
        Optional<CompanyDto> company = companyApplicationService.getCompanyBySymbol("999999");

        // Then
        assertThat(company).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for invalid category")
    void shouldReturnEmptyListForInvalidCategory() {
        // When
        List<CompanyDto> companies = companyApplicationService.getCompaniesByCategory("invalid-category");

        // Then
        assertThat(companies).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty search query")
    void shouldHandleEmptySearchQuery() {
        // Given
        CompanySearchQuery query = CompanySearchQuery.builder()
                .query("")
                .limit(10)
                .build();

        // When
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        // Then
        // Should return top companies when query is empty
        assertThat(result.companies()).isNotEmpty();
        assertThat(result.companies()).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Should respect limit in search results")
    void shouldRespectLimitInSearchResults() {
        // Given
        CompanySearchQuery query = CompanySearchQuery.builder()
                .limit(3)
                .build();

        // When
        CompanySearchResult result = companyApplicationService.searchCompanies(query);

        // Then
        assertThat(result.companies()).hasSizeLessThanOrEqualTo(3);
    }
}