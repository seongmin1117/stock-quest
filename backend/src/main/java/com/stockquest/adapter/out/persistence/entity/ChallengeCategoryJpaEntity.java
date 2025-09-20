package com.stockquest.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 챌린지 카테고리 JPA 엔티티
 */
@Entity
@Table(name = "challenge_category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ChallengeCategoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "color", nullable = false, length = 7)
    private String color = "#6366f1";
    
    @Column(name = "icon", nullable = false, length = 50)
    private String icon = "chart-line";
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 팩토리 메서드
    public static ChallengeCategoryJpaEntity create(String name, String description, String color, String icon, Integer sortOrder) {
        ChallengeCategoryJpaEntity entity = new ChallengeCategoryJpaEntity();
        entity.name = name;
        entity.description = description;
        entity.color = color != null ? color : "#6366f1";
        entity.icon = icon != null ? icon : "chart-line";
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        entity.isActive = true;
        return entity;
    }
}