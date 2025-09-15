package com.stockquest.domain.company;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 회사 카테고리 도메인 엔티티
 */
@Entity
@Table(name = "company_category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false, unique = true, length = 20)
    private String categoryId;

    @Column(name = "name_kr", nullable = false, length = 50)
    private String nameKr;

    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;

    @Column(name = "description_kr", columnDefinition = "TEXT")
    private String descriptionKr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
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
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 카테고리 정보 업데이트
     */
    public void updateInfo(String nameKr, String nameEn, String descriptionKr, String descriptionEn) {
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.descriptionKr = descriptionKr;
        this.descriptionEn = descriptionEn;
    }

    /**
     * 정렬 순서 업데이트
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 활성화 상태 토글
     */
    public void toggleActiveStatus() {
        this.isActive = !this.isActive;
    }

    /**
     * 표시명 반환 (한국어 + 영어)
     */
    public String getDisplayName() {
        return String.format("%s (%s)", nameKr, nameEn);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}