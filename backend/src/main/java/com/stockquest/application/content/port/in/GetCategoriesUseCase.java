package com.stockquest.application.content.port.in;

import com.stockquest.application.content.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 조회 Use Case (입력 포트)
 */
public interface GetCategoriesUseCase {

    /**
     * 모든 활성 카테고리 조회 (계층 구조 유지)
     *
     * @return 카테고리 목록
     */
    List<CategoryDto> getAllCategories();

    /**
     * 슬러그로 카테고리 조회
     *
     * @param slug 카테고리 슬러그
     * @return 카테고리 정보
     */
    Optional<CategoryDto> getCategoryBySlug(String slug);

    /**
     * ID로 카테고리 조회
     *
     * @param id 카테고리 ID
     * @return 카테고리 정보
     */
    Optional<CategoryDto> getCategoryById(Long id);

    /**
     * 홈페이지 표시용 추천 카테고리 조회
     *
     * @return 추천 카테고리 목록
     */
    List<CategoryDto> getFeaturedCategories();

    /**
     * 네비게이션용 카테고리 조회
     *
     * @return 네비게이션 카테고리 목록
     */
    List<CategoryDto> getNavigationCategories();
}