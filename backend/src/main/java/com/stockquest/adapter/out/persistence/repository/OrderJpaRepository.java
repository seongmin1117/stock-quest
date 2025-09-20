package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.OrderJpaEntity;
import com.stockquest.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 주문 JPA 저장소
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    
    List<OrderJpaEntity> findBySessionIdOrderByOrderedAtDesc(Long sessionId);
    
    List<OrderJpaEntity> findBySessionIdAndStatus(Long sessionId, OrderStatus status);
    
    List<OrderJpaEntity> findBySessionIdAndInstrumentKeyOrderByOrderedAtDesc(Long sessionId, String instrumentKey);
}