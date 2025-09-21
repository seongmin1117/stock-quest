package com.stockquest.domain.company.port;

import com.stockquest.domain.company.Company;

import java.util.List;
import java.util.Optional;

/**
 * 회사 정보 저장소 포트 (출력 포트)
 * 헥사고날 아키텍처: 프레임워크 독립적인 순수 포트 인터페이스
 */
public interface CompanyRepository {

    /**
     * 회사 저장
     */
    Company save(Company company);

    /**
     * 심볼로 회사 조회
     */
    Optional<Company> findBySymbol(String symbol);

    /**
     * ID로 회사 조회
     */
    Optional<Company> findById(Long id);

    /**
     * 활성화된 회사만 조회 (인기도 순)
     */
    List<Company> findActiveCompaniesOrderByPopularity();

    /**
     * 인기도 순으로 상위 N개 회사 조회
     */
    List<Company> findTopCompaniesByPopularity(int limit);

    /**
     * 회사명 검색 (한국어/영어/심볼)
     */
    List<Company> searchByNameOrSymbol(String query);

    /**
     * 특정 카테고리에 속한 회사들 조회
     */
    List<Company> findByCategoryId(String categoryId);

    /**
     * 섹터별 회사 조회
     */
    List<Company> findBySectorOrderByPopularity(String sector);

    /**
     * 시가총액 범위로 회사 조회
     */
    List<Company> findByMarketCapRange(Long minMarketCap, Long maxMarketCap);

    /**
     * 복합 검색 (이름, 섹터, 카테고리)
     */
    List<Company> searchCompanies(String query, String categoryId, String sector);

    /**
     * 카테고리별 회사 수 조회
     */
    List<CategoryCompanyCount> countCompaniesByCategory();

    /**
     * 최근 업데이트된 회사들 조회
     */
    List<Company> findRecentlyUpdatedCompanies();

    /**
     * 한국 시장 회사만 조회
     */
    List<Company> findKoreanMarketCompanies();

    /**
     * 모든 활성 회사 조회
     */
    List<Company> findAllActive();

    /**
     * 회사 삭제
     */
    void delete(Company company);

    /**
     * ID로 회사 삭제
     */
    void deleteById(Long id);

    /**
     * 회사 존재 여부 확인
     */
    boolean existsById(Long id);

    /**
     * 심볼로 회사 존재 여부 확인
     */
    boolean existsBySymbol(String symbol);

    /**
     * 카테고리별 회사 수 집계를 위한 DTO
     */
    interface CategoryCompanyCount {
        String getCategoryId();
        Long getCount();
    }
}