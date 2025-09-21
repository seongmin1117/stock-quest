package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회사 JPA 엔티티
 * Hibernate Second-level Cache 적용 (READ_MOSTLY 전략)
 * Company 도메인 엔티티와 매핑 담당
 */
@Entity
@Table(name = "company")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompanyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "sector", length = 50)
    private String sector;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "market_cap_display", length = 20)
    private String marketCapDisplay;

    @Column(name = "logo_path", length = 200)
    private String logoPath;

    @Column(name = "description_kr", columnDefinition = "TEXT")
    private String descriptionKr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Builder.Default
    @Column(name = "exchange", length = 10)
    private String exchange = "KRX";

    @Builder.Default
    @Column(name = "currency", length = 3)
    private String currency = "KRW";

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "popularity_score")
    private Integer popularityScore = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyCategoryMappingJpaEntity> categories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 도메인 엔티티로 변환
     */
    public Company toDomain() {
        List<String> categoryIds = categories.stream()
                .map(CompanyCategoryMappingJpaEntity::getCategoryId)
                .toList();

        return Company.builder()
                .id(id)
                .symbol(symbol)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .sector(sector)
                .marketCap(marketCap)
                .marketCapDisplay(marketCapDisplay)
                .logoPath(logoPath)
                .descriptionKr(descriptionKr)
                .descriptionEn(descriptionEn)
                .exchange(exchange)
                .currency(currency)
                .isActive(isActive)
                .popularityScore(popularityScore)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .categoryIds(categoryIds)
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 포함)
     */
    public static CompanyJpaEntity fromDomain(Company domain) {
        return CompanyJpaEntity.builder()
                .id(domain.getId())
                .symbol(domain.getSymbol())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .sector(domain.getSector())
                .marketCap(domain.getMarketCap())
                .marketCapDisplay(domain.getMarketCapDisplay())
                .logoPath(domain.getLogoPath())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .exchange(domain.getExchange())
                .currency(domain.getCurrency())
                .isActive(domain.getIsActive())
                .popularityScore(domain.getPopularityScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * 도메인 엔티티에서 JPA 엔티티로 변환 (ID 제외 - 새로운 엔티티 생성용)
     */
    public static CompanyJpaEntity fromDomainForCreate(Company domain) {
        return CompanyJpaEntity.builder()
                .symbol(domain.getSymbol())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .sector(domain.getSector())
                .marketCap(domain.getMarketCap())
                .marketCapDisplay(domain.getMarketCapDisplay())
                .logoPath(domain.getLogoPath())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .exchange(domain.getExchange())
                .currency(domain.getCurrency())
                .isActive(domain.getIsActive())
                .popularityScore(domain.getPopularityScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * 기존 JPA 엔티티를 도메인 엔티티 정보로 업데이트
     */
    public CompanyJpaEntity updateFromDomain(Company domain) {
        return CompanyJpaEntity.builder()
                .id(this.id) // 기존 ID 유지
                .symbol(domain.getSymbol())
                .nameKr(domain.getNameKr())
                .nameEn(domain.getNameEn())
                .sector(domain.getSector())
                .marketCap(domain.getMarketCap())
                .marketCapDisplay(domain.getMarketCapDisplay())
                .logoPath(domain.getLogoPath())
                .descriptionKr(domain.getDescriptionKr())
                .descriptionEn(domain.getDescriptionEn())
                .exchange(domain.getExchange())
                .currency(domain.getCurrency())
                .isActive(domain.getIsActive())
                .popularityScore(domain.getPopularityScore())
                .createdAt(this.createdAt) // 기존 생성일 유지
                .updatedAt(LocalDateTime.now()) // 업데이트 시간 갱신
                .categories(this.categories) // 기존 카테고리 관계 유지
                .build();
    }
}