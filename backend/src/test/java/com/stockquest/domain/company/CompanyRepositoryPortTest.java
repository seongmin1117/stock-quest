package com.stockquest.domain.company;

import com.stockquest.domain.company.port.CompanyRepositoryPort;
import com.stockquest.domain.company.port.CompanyCategoryRepositoryPort;
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
 * Company Repository Port Test - TDD for Hexagonal Architecture
 *
 * Tests the domain ports for company functionality following clean architecture.
 * This ensures the domain layer remains pure and framework-agnostic.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Company Repository Port Tests")
class CompanyRepositoryPortTest {

    @Autowired
    private CompanyRepositoryPort companyRepositoryPort;

    @Autowired
    private CompanyCategoryRepositoryPort companyCategoryRepositoryPort;

    @Test
    @DisplayName("Should find Samsung Electronics by symbol")
    void shouldFindSamsungBySymbol() {
        // When
        Optional<Company> company = companyRepositoryPort.findBySymbol("005930");

        // Then
        assertThat(company).isPresent();
        assertThat(company.get().getSymbol()).isEqualTo("005930");
        assertThat(company.get().getNameKr()).isEqualTo("삼성전자");
        assertThat(company.get().getNameEn()).isEqualTo("Samsung Electronics");
    }

    @Test
    @DisplayName("Should find companies by category")
    void shouldFindCompaniesByCategory() {
        // When
        List<Company> techCompanies = companyRepositoryPort.findByCategory("tech");

        // Then
        assertThat(techCompanies).isNotEmpty();
        assertThat(techCompanies)
            .extracting(Company::getSymbol)
            .containsAnyOf("005930", "035720", "035420"); // Samsung, Kakao, Naver
    }

    @Test
    @DisplayName("Should search companies by Korean name")
    void shouldSearchCompaniesByKoreanName() {
        // When
        List<Company> results = companyRepositoryPort.searchByName("삼성");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results)
            .extracting(Company::getNameKr)
            .allMatch(name -> name.contains("삼성"));
    }

    @Test
    @DisplayName("Should search companies by English name")
    void shouldSearchCompaniesByEnglishName() {
        // When
        List<Company> results = companyRepositoryPort.searchByName("Samsung");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results)
            .extracting(Company::getNameEn)
            .allMatch(name -> name.contains("Samsung"));
    }

    @Test
    @DisplayName("Should find top companies by popularity")
    void shouldFindTopCompaniesByPopularity() {
        // When
        List<Company> topCompanies = companyRepositoryPort.findTopByPopularity(5);

        // Then
        assertThat(topCompanies).hasSize(5);
        assertThat(topCompanies).isSortedAccordingTo(
            (c1, c2) -> Integer.compare(c2.getPopularityScore(), c1.getPopularityScore())
        );
    }

    @Test
    @DisplayName("Should find all active categories")
    void shouldFindAllActiveCategories() {
        // When
        List<CompanyCategory> categories = companyCategoryRepositoryPort.findAllActive();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).allMatch(CompanyCategory::isActive);
        assertThat(categories)
            .extracting(CompanyCategory::getCategoryId)
            .containsAnyOf("tech", "semiconductor", "automotive", "finance");
    }

    @Test
    @DisplayName("Should find category by ID")
    void shouldFindCategoryById() {
        // When
        Optional<CompanyCategory> category = companyCategoryRepositoryPort.findByCategoryId("tech");

        // Then
        assertThat(category).isPresent();
        assertThat(category.get().getCategoryId()).isEqualTo("tech");
        assertThat(category.get().getNameKr()).isEqualTo("기술");
        assertThat(category.get().getNameEn()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("Should find companies by multiple categories")
    void shouldFindCompaniesByMultipleCategories() {
        // When
        List<Company> companies = companyRepositoryPort.findByCategories(
            List.of("tech", "semiconductor")
        );

        // Then
        assertThat(companies).isNotEmpty();
        // Samsung should be in both tech and semiconductor
        assertThat(companies)
            .extracting(Company::getSymbol)
            .contains("005930");
    }

    @Test
    @DisplayName("Should return empty for non-existent symbol")
    void shouldReturnEmptyForNonExistentSymbol() {
        // When
        Optional<Company> company = companyRepositoryPort.findBySymbol("999999");

        // Then
        assertThat(company).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for non-existent category")
    void shouldReturnEmptyListForNonExistentCategory() {
        // When
        List<Company> companies = companyRepositoryPort.findByCategory("non-existent");

        // Then
        assertThat(companies).isEmpty();
    }
}