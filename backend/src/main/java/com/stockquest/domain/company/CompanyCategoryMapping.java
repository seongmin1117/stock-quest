package com.stockquest.domain.company;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 회사-카테고리 매핑 엔티티
 */
@Entity
@Table(name = "company_category_mapping")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyCategoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "category_id", nullable = false, length = 20)
    private String categoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 매핑 생성자
     */
    public CompanyCategoryMapping(Company company, String categoryId) {
        this.company = company;
        this.categoryId = categoryId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}