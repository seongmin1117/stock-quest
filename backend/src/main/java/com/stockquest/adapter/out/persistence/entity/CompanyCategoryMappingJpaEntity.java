package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.company.CompanyCategoryMapping;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

/**
 * 회사-카테고리 매핑 JPA 엔티티
 * Hibernate Second-level Cache 적용 (FREQUENT_UPDATE 전략)
 * CompanyCategoryMapping 도메인 엔티티와 매핑 담당
 */
@Entity
@Table(name = "company_category_mapping")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompanyCategoryMappingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;

    @Column(name = "category_id", nullable = false, length = 20)
    private String categoryId;

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
    public CompanyCategoryMapping toDomain() {
        return CompanyCategoryMapping.builder()
                .id(id)
                .companyId(company != null ? company.getId() : null)
                .categoryId(categoryId)
                .createdAt(createdAt)
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 포함, company 엔티티 필요)
     */
    public static CompanyCategoryMappingJpaEntity fromDomain(CompanyCategoryMapping domain, CompanyJpaEntity companyEntity) {
        return CompanyCategoryMappingJpaEntity.builder()
                .id(domain.getId())
                .company(companyEntity)
                .categoryId(domain.getCategoryId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 제외 - 새로운 엔티티 생성용)
     */
    public static CompanyCategoryMappingJpaEntity fromDomainForCreate(CompanyCategoryMapping domain, CompanyJpaEntity companyEntity) {
        return CompanyCategoryMappingJpaEntity.builder()
                .company(companyEntity)
                .categoryId(domain.getCategoryId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * 회사 ID와 카테고리 ID로 새로운 매핑 엔티티 생성
     */
    public static CompanyCategoryMappingJpaEntity create(CompanyJpaEntity companyEntity, String categoryId) {
        return CompanyCategoryMappingJpaEntity.builder()
                .company(companyEntity)
                .categoryId(categoryId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 매핑이 특정 회사에 대한 것인지 확인
     */
    public boolean isForCompany(Long companyId) {
        return company != null && company.getId() != null && company.getId().equals(companyId);
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
}