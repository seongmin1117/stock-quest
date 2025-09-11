package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.application.session.dto.GetSessionDetailResult;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderStatus;
import com.stockquest.domain.order.OrderType;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 세션 상세 조회 응답 DTO
 */
@Builder
public record SessionDetailResponse(
    Long sessionId,
    Long challengeId,
    String challengeTitle,
    SessionStatus status,
    BigDecimal initialBalance,
    BigDecimal currentBalance,
    BigDecimal returnRate,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    List<PortfolioItem> portfolio,
    List<OrderItem> orders
) {
    
    @Builder
    public record PortfolioItem(
        String instrumentKey,
        BigDecimal quantity,
        BigDecimal averagePrice,
        BigDecimal totalCost,
        BigDecimal currentValue,
        BigDecimal unrealizedPnl
    ) {
        
        public static PortfolioItem from(PortfolioPosition position) {
            // Mock current price for display (in real app, would get from market data service)
            BigDecimal mockCurrentPrice = getMockCurrentPrice(position.getInstrumentKey());
            BigDecimal currentValue = position.calculateCurrentValue(mockCurrentPrice);
            BigDecimal unrealizedPnl = position.calculateUnrealizedPnL(mockCurrentPrice);
            
            return PortfolioItem.builder()
                    .instrumentKey(position.getInstrumentKey())
                    .quantity(position.getQuantity())
                    .averagePrice(position.getAveragePrice())
                    .totalCost(position.getTotalCost())
                    .currentValue(currentValue)
                    .unrealizedPnl(unrealizedPnl)
                    .build();
        }
        
        private static BigDecimal getMockCurrentPrice(String instrumentKey) {
            // Mock prices (실제로는 MarketDataService에서 조회)
            return switch (instrumentKey) {
                case "A" -> new BigDecimal("152.50");
                case "B" -> new BigDecimal("355.75");
                case "C" -> new BigDecimal("2825.00");
                default -> new BigDecimal("102.00");
            };
        }
    }
    
    @Builder
    public record OrderItem(
        Long orderId,
        String instrumentKey,
        OrderSide side,
        BigDecimal quantity,
        OrderType orderType,
        BigDecimal limitPrice,
        BigDecimal executedPrice,
        BigDecimal slippageRate,
        OrderStatus status,
        LocalDateTime orderedAt,
        LocalDateTime executedAt
    ) {
        
        public static OrderItem from(Order order) {
            return OrderItem.builder()
                    .orderId(order.getId())
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
    
    public static SessionDetailResponse from(GetSessionDetailResult result) {
        List<PortfolioItem> portfolioItems = result.portfolio()
                .stream()
                .map(PortfolioItem::from)
                .collect(Collectors.toList());
        
        List<OrderItem> orderItems = result.orders()
                .stream()
                .map(OrderItem::from)
                .collect(Collectors.toList());
        
        return SessionDetailResponse.builder()
                .sessionId(result.session().getId())
                .challengeId(result.session().getChallengeId())
                .challengeTitle(result.challengeTitle())
                .status(result.session().getStatus())
                .initialBalance(result.session().getInitialBalance())
                .currentBalance(result.session().getCurrentBalance())
                .returnRate(result.session().getReturnRate())
                .startedAt(result.session().getStartedAt())
                .completedAt(result.session().getCompletedAt())
                .portfolio(portfolioItems)
                .orders(orderItems)
                .build();
    }
}