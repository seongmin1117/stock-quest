package com.stockquest.domain.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회사-카테고리 매핑 도메인 엔티티
 * 헥사고날 아키텍처 준수 - 순수한 비즈니스 로직만 포함
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCategoryMapping {

    private Long id;
    private Long companyId;
    private String categoryId;
    private LocalDateTime createdAt;

    /**
     * 매핑 생성자
     */
    public CompanyCategoryMapping(Long companyId, String categoryId) {
        this.companyId = companyId;
        this.categoryId = categoryId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 매핑 유효성 검증
     */
    public boolean isValid() {
        return companyId != null && companyId > 0 &&
               categoryId != null && !categoryId.trim().isEmpty();
    }

    /**
     * 매핑이 특정 회사에 대한 것인지 확인
     */
    public boolean isForCompany(Long companyId) {
        return this.companyId != null && this.companyId.equals(companyId);
    }

    /**
     * 매핑이 특정 카테고리에 대한 것인지 확인
     */
    public boolean isForCategory(String categoryId) {
        return this.categoryId != null && this.categoryId.equals(categoryId);
    }

    /**
     * 매핑이 특정 회사와 카테고리 조합인지 확인
     */
    public boolean matches(Long companyId, String categoryId) {
        return isForCompany(companyId) && isForCategory(categoryId);
    }

    /**
     * 시스템 카테고리 매핑인지 확인
     */
    public boolean isSystemCategoryMapping() {
        return categoryId != null &&
               (categoryId.startsWith("SYS_") || categoryId.startsWith("DEFAULT_"));
    }

    /**
     * 사용자 정의 카테고리 매핑인지 확인
     */
    public boolean isCustomCategoryMapping() {
        return !isSystemCategoryMapping();
    }

    /**
     * 생성일 기준 나이 계산 (일 단위)
     */
    public long getAgeInDays() {
        if (createdAt == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * 최근 매핑인지 확인 (7일 이내)
     */
    public boolean isRecentMapping() {
        return getAgeInDays() <= 7;
    }

    /**
     * 매핑 비교 (회사 ID 우선, 카테고리 ID 차순)
     */
    public int compareTo(CompanyCategoryMapping other) {
        if (other == null) return 1;

        // 회사 ID로 우선 정렬
        int companyComparison = Long.compare(this.companyId, other.companyId);
        if (companyComparison != 0) return companyComparison;

        // 카테고리 ID로 정렬
        return this.categoryId.compareTo(other.categoryId);
    }

    /**
     * 동일한 매핑인지 확인 (회사 ID와 카테고리 ID 기준)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CompanyCategoryMapping other = (CompanyCategoryMapping) obj;
        return companyId != null && companyId.equals(other.companyId) &&
               categoryId != null && categoryId.equals(other.categoryId);
    }

    /**
     * 해시코드 생성 (회사 ID와 카테고리 ID 기준)
     */
    @Override
    public int hashCode() {
        int result = companyId != null ? companyId.hashCode() : 0;
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        return result;
    }

    /**
     * 매핑 정보 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("CompanyCategoryMapping{companyId=%d, categoryId='%s', createdAt=%s}",
                companyId, categoryId, createdAt);
    }

    /**
     * 매핑 복사 (새로운 ID 생성)
     */
    public CompanyCategoryMapping copy() {
        return CompanyCategoryMapping.builder()
                .companyId(this.companyId)
                .categoryId(this.categoryId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 다른 회사로 매핑 복사
     */
    public CompanyCategoryMapping copyToCompany(Long newCompanyId) {
        return CompanyCategoryMapping.builder()
                .companyId(newCompanyId)
                .categoryId(this.categoryId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 다른 카테고리로 매핑 복사
     */
    public CompanyCategoryMapping copyToCategory(String newCategoryId) {
        return CompanyCategoryMapping.builder()
                .companyId(this.companyId)
                .categoryId(newCategoryId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}