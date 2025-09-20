package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 템플릿 JPA 엔티티
 */
@Entity
@Table(name = "challenge_template")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ChallengeTemplateJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private ChallengeDifficulty difficulty = ChallengeDifficulty.BEGINNER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private ChallengeType templateType = ChallengeType.BULL_MARKET;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "JSON", nullable = false)
    private Map<String, Object> config;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "JSON")
    private List<String> tags;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "success_criteria", columnDefinition = "JSON")
    private Map<String, Object> successCriteria;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "market_scenario", columnDefinition = "JSON")
    private Map<String, Object> marketScenario;
    
    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;
    
    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes = 30;
    
    @Column(name = "initial_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialBalance = new BigDecimal("100000.00");
    
    @Column(name = "speed_factor", nullable = false)
    private Integer speedFactor = 10;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_by")
    private Long createdBy;
    
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
    public static ChallengeTemplateJpaEntity create(String name, String description, 
            ChallengeDifficulty difficulty, ChallengeType templateType) {
        ChallengeTemplateJpaEntity entity = new ChallengeTemplateJpaEntity();
        entity.name = name;
        entity.description = description;
        entity.difficulty = difficulty != null ? difficulty : ChallengeDifficulty.BEGINNER;
        entity.templateType = templateType != null ? templateType : ChallengeType.BULL_MARKET;
        entity.isActive = true;
        return entity;
    }
}