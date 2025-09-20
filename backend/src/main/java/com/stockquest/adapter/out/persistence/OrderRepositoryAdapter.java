package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.OrderJpaEntity;
import com.stockquest.adapter.out.persistence.repository.OrderJpaRepository;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderStatus;
import com.stockquest.domain.order.port.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 주문 저장소 어댑터
 * Domain OrderRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity jpaEntity = OrderJpaEntity.fromDomain(order);
        OrderJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionIdOrderByOrderedAtDesc(sessionId)
                .stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findBySessionIdAndStatus(Long sessionId, OrderStatus status) {
        return jpaRepository.findBySessionIdAndStatus(sessionId, status)
                .stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findBySessionIdAndInstrumentKey(Long sessionId, String instrumentKey) {
        return jpaRepository.findBySessionIdAndInstrumentKeyOrderByOrderedAtDesc(sessionId, instrumentKey)
                .stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }
}