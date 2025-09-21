package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.company.CompanyCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

/**
 * 회사 카테고리 JPA 엔티티
 * Hibernate Second-level Cache 적용 (READ_MOSTLY 전략)
 * CompanyCategory 도메인 엔티티와 매핑 담당
 */
@Entity
@Table(name = "company_category")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompanyCategoryJpaEntity {

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

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * 도메인 엔티티로 변환
     */
    public CompanyCategory toDomain() {
        return CompanyCategory.builder()
                .id(id)
                .categoryId(categoryId)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .descriptionKr(descriptionKr)
                .descriptionEn(descriptionEn)
                .sortOrder(sortOrder)
                .isActive(isActive)
                .createdAt(createdAt)
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 포함)
     */
    public static CompanyCategoryJpaEntity fromDomain(CompanyCategory domain) {
        return CompanyCategoryJpaEntity.builder()
                .id(domain.getId())
                .categoryId(domain.getCategoryId())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .sortOrder(domain.getSortOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 제외 - 새로운 엔티티 생성용)
     */
    public static CompanyCategoryJpaEntity fromDomainForCreate(CompanyCategory domain) {
        return CompanyCategoryJpaEntity.builder()
                .categoryId(domain.getCategoryId())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .sortOrder(domain.getSortOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * 기존 JPA 엔티티를 도메인 엔티티 정보로 업데이트
     */
    public CompanyCategoryJpaEntity updateFromDomain(CompanyCategory domain) {
        return CompanyCategoryJpaEntity.builder()
                .id(this.id) // 기존 ID 유지
                .categoryId(domain.getCategoryId())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .sortOrder(domain.getSortOrder())
                .isActive(domain.getIsActive())
                .createdAt(this.createdAt) // 기존 생성일 유지
                .build();
    }
}