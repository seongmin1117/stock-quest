package com.stockquest.application.session;

import com.stockquest.application.session.dto.GetOrdersQuery;
import com.stockquest.application.session.dto.GetOrdersResult;
import com.stockquest.application.session.port.in.GetOrdersUseCase;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.port.OrderRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 주문 내역 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetOrdersService implements GetOrdersUseCase {

    private final ChallengeSessionRepository sessionRepository;
    private final OrderRepository orderRepository;

    @Override
    public GetOrdersResult getOrders(GetOrdersQuery query) {
        // 세션 조회 및 권한 확인
        ChallengeSession session = sessionRepository.findById(query.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + query.sessionId()));

        if (!session.getUserId().equals(query.userId())) {
            throw new IllegalArgumentException("세션에 대한 접근 권한이 없습니다.");
        }

        // 주문 내역 조회
        List<Order> orders = orderRepository.findBySessionId(query.sessionId());

        return new GetOrdersResult(
                session.getId(),
                orders
        );
    }
}