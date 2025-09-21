package com.stockquest.domain.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회사 카테고리 도메인 엔티티
 * 헥사고날 아키텍처 준수 - 순수한 비즈니스 로직만 포함
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCategory {

    private Long id;
    private String categoryId;
    private String nameKr;
    private String nameEn;
    private String descriptionKr;
    private String descriptionEn;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    /**
     * 카테고리 생성자
     */
    public CompanyCategory(String categoryId, String nameKr, String nameEn, String descriptionKr, String descriptionEn) {
        this.categoryId = categoryId;
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.descriptionKr = descriptionKr;
        this.descriptionEn = descriptionEn;
        this.sortOrder = 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 카테고리 정보 업데이트
     */
    public CompanyCategory updateInfo(String nameKr, String nameEn, String descriptionKr, String descriptionEn) {
        return this.toBuilder()
                .nameKr(nameKr)
                .nameEn(nameEn)
                .descriptionKr(descriptionKr)
                .descriptionEn(descriptionEn)
                .build();
    }

    /**
     * 정렬 순서 업데이트
     */
    public CompanyCategory updateSortOrder(Integer sortOrder) {
        return this.toBuilder()
                .sortOrder(sortOrder)
                .build();
    }

    /**
     * 활성화 상태 토글
     */
    public CompanyCategory toggleActiveStatus() {
        return this.toBuilder()
                .isActive(!this.isActive)
                .build();
    }

    /**
     * 카테고리 활성화
     */
    public CompanyCategory activate() {
        return this.toBuilder()
                .isActive(true)
                .build();
    }

    /**
     * 카테고리 비활성화
     */
    public CompanyCategory deactivate() {
        return this.toBuilder()
                .isActive(false)
                .build();
    }

    /**
     * 표시명 반환 (한국어 + 영어)
     */
    public String getDisplayName() {
        return String.format("%s (%s)", nameKr, nameEn);
    }

    /**
     * 카테고리 유효성 검증
     */
    public boolean isValid() {
        return categoryId != null && !categoryId.trim().isEmpty() &&
               nameKr != null && !nameKr.trim().isEmpty() &&
               nameEn != null && !nameEn.trim().isEmpty();
    }

    /**
     * 검색 매칭 점수 계산 (0-100)
     */
    public int calculateSearchScore(String query) {
        if (query == null || query.trim().isEmpty()) {
            return 0;
        }

        String lowerQuery = query.toLowerCase();
        int score = 0;

        // 정확한 카테고리 ID 매칭 (최고 점수)
        if (categoryId.toLowerCase().equals(lowerQuery)) {
            score += 100;
        } else if (categoryId.toLowerCase().contains(lowerQuery)) {
            score += 80;
        }

        // 한국어 이름 매칭
        if (nameKr.toLowerCase().contains(lowerQuery)) {
            score += 70;
        }

        // 영어 이름 매칭
        if (nameEn.toLowerCase().contains(lowerQuery)) {
            score += 60;
        }

        // 한국어 설명 매칭
        if (descriptionKr != null && descriptionKr.toLowerCase().contains(lowerQuery)) {
            score += 30;
        }

        // 영어 설명 매칭
        if (descriptionEn != null && descriptionEn.toLowerCase().contains(lowerQuery)) {
            score += 25;
        }

        // 활성 상태 보너스
        if (isActive) {
            score += 5;
        }

        return Math.min(score, 100);
    }

    /**
     * 정렬 우선순위 비교
     */
    public int compareTo(CompanyCategory other) {
        if (other == null) return 1;

        // 활성 상태 우선 정렬
        if (this.isActive && !other.isActive) return -1;
        if (!this.isActive && other.isActive) return 1;

        // 정렬 순서로 정렬
        int sortComparison = Integer.compare(this.sortOrder, other.sortOrder);
        if (sortComparison != 0) return sortComparison;

        // 한국어 이름으로 정렬
        return this.nameKr.compareTo(other.nameKr);
    }

    /**
     * 카테고리가 기본 카테고리인지 확인
     */
    public boolean isSystemCategory() {
        return categoryId != null &&
               (categoryId.startsWith("SYS_") || categoryId.startsWith("DEFAULT_"));
    }

    /**
     * 카테고리가 사용자 정의 카테고리인지 확인
     */
    public boolean isCustomCategory() {
        return !isSystemCategory();
    }
}