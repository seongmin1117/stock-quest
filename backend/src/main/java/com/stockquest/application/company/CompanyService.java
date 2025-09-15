package com.stockquest.application.company;

import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.CompanyCategory;
import com.stockquest.domain.company.repository.CompanyCategoryRepository;
import com.stockquest.domain.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 회사 관련 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyCategoryRepository companyCategoryRepository;

    /**
     * 회사 검색 (자동완성용)
     * 한국어명, 영어명, 심볼로 검색 가능
     */
    public List<Company> searchCompanies(String query) {
        log.debug("회사 검색: query={}", query);

        if (query == null || query.trim().isEmpty()) {
            return companyRepository.findTop10ByIsActiveTrueOrderByPopularityScoreDesc();
        }

        List<Company> results = companyRepository.searchByNameOrSymbol(query.trim());
        log.debug("검색 결과: {} 개 회사", results.size());

        return results;
    }

    /**
     * 복합 검색 (이름, 카테고리, 섹터 조건)
     */
    public List<Company> searchCompanies(String query, String categoryId, String sector) {
        log.debug("복합 검색: query={}, categoryId={}, sector={}", query, categoryId, sector);

        return companyRepository.searchCompanies(query, categoryId, sector);
    }

    /**
     * 인기 회사 목록 조회 (상위 10개)
     */
    public List<Company> getPopularCompanies() {
        log.debug("인기 회사 목록 조회");
        return companyRepository.findTop10ByIsActiveTrueOrderByPopularityScoreDesc();
    }

    /**
     * 인기 회사 목록 조회 (상위 N개)
     */
    public List<Company> getPopularCompanies(int limit) {
        log.debug("인기 회사 목록 조회: limit={}", limit);
        List<Company> companies = companyRepository.findByIsActiveTrueOrderByPopularityScoreDesc();

        return companies.size() > limit ? companies.subList(0, limit) : companies;
    }

    /**
     * 특정 카테고리의 회사들 조회
     */
    public List<Company> getCompaniesByCategory(String categoryId) {
        log.debug("카테고리별 회사 조회: categoryId={}", categoryId);
        return companyRepository.findByCategoryId(categoryId);
    }

    /**
     * 심볼로 회사 조회
     */
    public Optional<Company> getCompanyBySymbol(String symbol) {
        log.debug("심볼로 회사 조회: symbol={}", symbol);
        return companyRepository.findBySymbol(symbol);
    }

    /**
     * 모든 활성 카테고리 조회
     */
    public List<CompanyCategory> getAllCategories() {
        log.debug("모든 카테고리 조회");
        return companyCategoryRepository.findByIsActiveTrueOrderBySortOrder();
    }

    /**
     * 회사 수와 함께 카테고리 조회
     */
    public Map<CompanyCategory, Long> getCategoriesWithCompanyCount() {
        log.debug("카테고리별 회사 수 조회");

        List<Object[]> results = companyCategoryRepository.findCategoriesWithCompanyCount();
        Map<CompanyCategory, Long> categoryCountMap = new HashMap<>();

        for (Object[] result : results) {
            CompanyCategory category = (CompanyCategory) result[0];
            Long count = (Long) result[1];
            categoryCountMap.put(category, count);
        }

        return categoryCountMap;
    }

    /**
     * 한국 시장 회사들만 조회
     */
    public List<Company> getKoreanMarketCompanies() {
        log.debug("한국 시장 회사 조회");
        return companyRepository.findKoreanMarketCompanies();
    }

    /**
     * 섹터별 회사 조회
     */
    public List<Company> getCompaniesBySector(String sector) {
        log.debug("섹터별 회사 조회: sector={}", sector);
        return companyRepository.findBySectorAndIsActiveTrueOrderByPopularityScoreDesc(sector);
    }

    /**
     * 시가총액 범위로 회사 조회
     */
    public List<Company> getCompaniesByMarketCapRange(Long minMarketCap, Long maxMarketCap) {
        log.debug("시가총액 범위 회사 조회: min={}, max={}", minMarketCap, maxMarketCap);
        return companyRepository.findByMarketCapRange(minMarketCap, maxMarketCap);
    }

    /**
     * 최근 업데이트된 회사들 조회
     */
    public List<Company> getRecentlyUpdatedCompanies() {
        log.debug("최근 업데이트 회사 조회");
        return companyRepository.findRecentlyUpdatedCompanies();
    }

    /**
     * 회사 인기도 점수 업데이트
     */
    @Transactional
    public void updatePopularityScore(Long companyId, Integer score) {
        log.debug("인기도 점수 업데이트: companyId={}, score={}", companyId, score);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다: " + companyId));

        company.updatePopularityScore(score);
        companyRepository.save(company);

        log.info("회사 인기도 점수 업데이트 완료: {} -> {}", company.getDisplayName(), score);
    }

    /**
     * 회사 기본 정보 업데이트
     */
    @Transactional
    public void updateCompanyInfo(Long companyId, String nameKr, String nameEn,
                                 String sector, String descriptionKr, String descriptionEn) {
        log.debug("회사 정보 업데이트: companyId={}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다: " + companyId));

        company.updateInfo(nameKr, nameEn, sector, descriptionKr, descriptionEn);
        companyRepository.save(company);

        log.info("회사 정보 업데이트 완료: {}", company.getDisplayName());
    }

    /**
     * 회사 시가총액 업데이트
     */
    @Transactional
    public void updateMarketCap(Long companyId, Long marketCap, String marketCapDisplay) {
        log.debug("시가총액 업데이트: companyId={}, marketCap={}", companyId, marketCap);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다: " + companyId));

        company.updateMarketCap(marketCap, marketCapDisplay);
        companyRepository.save(company);

        log.info("시가총액 업데이트 완료: {} -> {}", company.getDisplayName(), marketCapDisplay);
    }
}