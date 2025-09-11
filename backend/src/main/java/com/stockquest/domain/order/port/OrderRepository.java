package com.stockquest.domain.order.port;

import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * 주문 저장소 포트 (출력 포트)
 */
public interface OrderRepository {
    
    /**
     * 주문 저장
     */
    Order save(Order order);
    
    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(Long id);
    
    /**
     * 세션별 주문 목록 조회
     */
    List<Order> findBySessionId(Long sessionId);
    
    /**
     * 세션 및 상태별 주문 목록 조회
     */
    List<Order> findBySessionIdAndStatus(Long sessionId, OrderStatus status);
    
    /**
     * 세션 및 상품키별 주문 목록 조회
     */
    List<Order> findBySessionIdAndInstrumentKey(Long sessionId, String instrumentKey);
}