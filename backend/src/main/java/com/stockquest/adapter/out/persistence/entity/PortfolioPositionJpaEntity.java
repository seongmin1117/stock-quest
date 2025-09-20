package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.portfolio.PortfolioPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 포지션 JPA 엔티티
 */
@Entity
@Table(name = "portfolio_position")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioPositionJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "session_id")
    private Long sessionId;
    
    @Column(nullable = false, length = 10, name = "instrument_key")
    private String instrumentKey;
    
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 12, scale = 4, name = "average_price")
    private BigDecimal averagePrice;
    
    @Column(nullable = false, precision = 18, scale = 2, name = "total_cost")
    private BigDecimal totalCost;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public PortfolioPosition toDomain() {
        return PortfolioPosition.builder()
                .id(id)
                .sessionId(sessionId)
                .instrumentKey(instrumentKey)
                .quantity(quantity)
                .averagePrice(averagePrice)
                .totalCost(totalCost)
                .build();
    }
    
    public static PortfolioPositionJpaEntity fromDomain(PortfolioPosition position) {
        return PortfolioPositionJpaEntity.builder()
                .id(position.getId())
                .sessionId(position.getSessionId())
                .instrumentKey(position.getInstrumentKey())
                .quantity(position.getQuantity())
                .averagePrice(position.getAveragePrice())
                .totalCost(position.getTotalCost())
                .build();
    }
}