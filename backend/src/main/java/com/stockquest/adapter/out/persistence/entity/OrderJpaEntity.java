package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderStatus;
import com.stockquest.domain.order.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 JPA 엔티티
 */
@Entity
@Table(name = "order_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "session_id")
    private Long sessionId;
    
    @Column(nullable = false, length = 10, name = "instrument_key")
    private String instrumentKey;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;
    
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "order_type")
    private OrderType orderType;
    
    @Column(precision = 12, scale = 4, name = "limit_price")
    private BigDecimal limitPrice;
    
    @Column(precision = 12, scale = 4, name = "executed_price")
    private BigDecimal executedPrice;
    
    @Column(precision = 5, scale = 2, name = "slippage_rate")
    private BigDecimal slippageRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false, name = "ordered_at")
    private LocalDateTime orderedAt;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @PrePersist
    protected void onCreate() {
        if (orderedAt == null) {
            orderedAt = LocalDateTime.now();
        }
    }
    
    public Order toDomain() {
        return Order.builder()
                .id(id)
                .sessionId(sessionId)
                .instrumentKey(instrumentKey)
                .side(side)
                .quantity(quantity)
                .orderType(orderType)
                .limitPrice(limitPrice)
                .executedPrice(executedPrice)
                .slippageRate(slippageRate)
                .status(status)
                .orderedAt(orderedAt)
                .executedAt(executedAt)
                .build();
    }
    
    public static OrderJpaEntity fromDomain(Order order) {
        return OrderJpaEntity.builder()
                .id(order.getId())
                .sessionId(order.getSessionId())
                .instrumentKey(order.getInstrumentKey())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .orderType(order.getOrderType())
                .limitPrice(order.getLimitPrice())
                .executedPrice(order.getExecutedPrice())
                .slippageRate(order.getSlippageRate())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .executedAt(order.getExecutedAt())
                .build();
    }
}