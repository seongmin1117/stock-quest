package com.stockquest.domain.company.port;

import com.stockquest.domain.company.CompanyCategory;

import java.util.List;
import java.util.Optional;

/**
 * 회사 카테고리 저장소 포트 (출력 포트)
 * 헥사고날 아키텍처: 프레임워크 독립적인 순수 포트 인터페이스
 */
public interface CompanyCategoryRepository {

    /**
     * 카테고리 저장
     */
    CompanyCategory save(CompanyCategory category);

    /**
     * ID로 카테고리 조회
     */
    Optional<CompanyCategory> findById(String id);

    /**
     * 모든 활성 카테고리 조회 (정렬된 순서대로)
     */
    List<CompanyCategory> findAllActiveOrderBySortOrder();

    /**
     * 이름으로 카테고리 조회
     */
    Optional<CompanyCategory> findByName(String name);

    /**
     * 부모 카테고리로 자식 카테고리들 조회
     */
    List<CompanyCategory> findByParentCategory(String parentCategoryId);

    /**
     * 최상위 카테고리들 조회 (부모가 없는 카테고리들)
     */
    List<CompanyCategory> findRootCategories();

    /**
     * 카테고리 삭제
     */
    void delete(CompanyCategory category);

    /**
     * ID로 카테고리 삭제
     */
    void deleteById(String id);

    /**
     * 카테고리 존재 여부 확인
     */
    boolean existsById(String id);

    /**
     * 이름으로 카테고리 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 특정 부모 하위에 자식 카테고리가 있는지 확인
     */
    boolean existsByParentCategory(String parentCategoryId);
}