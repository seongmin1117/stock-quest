package com.stockquest.application.order;

import com.stockquest.application.challenge.InstrumentMappingService;
import com.stockquest.application.order.port.in.PlaceOrderUseCase;
import com.stockquest.application.market.MarketDataService;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.port.OrderRepository;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 주문 접수 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {
    
    private final OrderRepository orderRepository;
    private final ChallengeSessionRepository sessionRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;
    private final InstrumentMappingService instrumentMappingService;
    
    @Value("${stockquest.trading.slippage.min:0.5}")
    private double minSlippagePercent; // 최소 슬리피지 0.5%
    
    @Value("${stockquest.trading.slippage.max:2.0}")
    private double maxSlippagePercent; // 최대 슬리피지 2.0%
    
    private final Random random = new Random();
    
    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        log.info("주문 접수 시작: 세션={}, 상품={}, 사이드={}, 수량={}", 
            command.sessionId(), command.instrumentKey(), command.side(), command.quantity());
        
        // 1. 세션 유효성 확인
        var session = sessionRepository.findById(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + command.sessionId()));
            
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 세션에서만 주문할 수 있습니다");
        }
        
        // 2. 주문 생성
        var order = Order.builder()
            .sessionId(command.sessionId())
            .instrumentKey(command.instrumentKey())
            .side(command.side())
            .quantity(command.quantity())
            .orderType(command.orderType())
            .limitPrice(command.limitPrice())
            .status(com.stockquest.domain.order.OrderStatus.PENDING)
            .build();
        
        // 3. 실시간 시장가 조회 - 동적 매핑 사용
        BigDecimal marketPrice = getRealMarketPrice(session.getChallengeId(), command.instrumentKey());
        
        // 4. 슬리피지 계산
        BigDecimal slippageRate = calculateSlippageRate();
        
        // 5. 매수의 경우 잔고 확인 및 차감
        if (command.side() == OrderSide.BUY) {
            BigDecimal orderAmount = command.quantity().multiply(marketPrice);
            if (!session.canPlaceOrder(orderAmount)) {
                throw new IllegalStateException("주문 금액이 현재 잔고를 초과합니다");
            }
            
            // 잔고 차감
            BigDecimal newBalance = session.getCurrentBalance().subtract(orderAmount);
            session.updateBalance(newBalance);
        }
        
        // 6. 주문 체결
        order.execute(marketPrice, slippageRate);
        var savedOrder = orderRepository.save(order);
        
        // 7. 포트폴리오 업데이트
        updatePortfolio(session, savedOrder);
        
        // 8. 세션 업데이트
        sessionRepository.save(session);
        
        log.info("주문 체결 완료: ID={}, 체결가={}, 슬리피지={}%", 
            savedOrder.getId(), savedOrder.getExecutedPrice(), savedOrder.getSlippageRate());
        
        return new PlaceOrderResult(
            savedOrder.getId(),
            savedOrder.getInstrumentKey(),
            savedOrder.getSide(),
            savedOrder.getQuantity(),
            savedOrder.getExecutedPrice(),
            savedOrder.getSlippageRate(),
            savedOrder.getExecutedAt(),
            session.getCurrentBalance()
        );
    }
    
    /**
     * 실시간 시장가 조회 - 동적 매핑 사용
     * MarketDataService를 통해 Yahoo Finance에서 최신 가격 조회
     */
    private BigDecimal getRealMarketPrice(Long challengeId, String instrumentKey) {
        String ticker = instrumentKey; // 기본값으로 instrumentKey 사용
        
        try {
            // 실제 티커 심볼로 변환 - 동적 매핑 사용
            ticker = instrumentMappingService.resolveActualTicker(challengeId, instrumentKey);
            log.debug("티커 매핑 성공: {} -> {}", instrumentKey, ticker);
            
        } catch (Exception e) {
            log.warn("티커 매핑 실패, instrumentKey를 티커로 사용: challengeId={}, instrumentKey={}, error={}", 
                     challengeId, instrumentKey, e.getMessage());
            // ticker는 이미 instrumentKey로 설정되어 있음
        }
        
        try {
            PriceCandle latestPrice = marketDataService.getLatestPrice(ticker);
            
            if (latestPrice != null) {
                log.debug("실시간 시장가 조회 성공: {}({})={}", instrumentKey, ticker, latestPrice.getClosePrice());
                return latestPrice.getClosePrice();
            }
            
            log.warn("실시간 시장가 조회 결과 없음, 기본값 사용: {} -> {}", instrumentKey, ticker);
            
        } catch (Exception e) {
            log.warn("실시간 시장가 조회 중 오류 발생, 기본값 사용: challengeId={}, instrumentKey={}, ticker={}, error={}", 
                     challengeId, instrumentKey, ticker, e.getMessage());
        }
        
        // 모든 경우에 대해 기본값 반환
        BigDecimal defaultPrice = getDefaultPrice(ticker);
        log.info("기본 가격 사용: {}({}) = {}", instrumentKey, ticker, defaultPrice);
        return defaultPrice;
    }
    
    
    /**
     * 기본 가격 조회 (실시간 데이터 조회 실패 시 사용)
     * 티커 기반으로 합리적인 기본값 제공
     */
    private BigDecimal getDefaultPrice(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            log.warn("Empty ticker provided, using default fallback price");
            ticker = "DEFAULT";
        }
        
        // 티커별 현실적인 기본 가격 (2024년 기준)
        BigDecimal basePrice;
        switch (ticker.toUpperCase().trim()) {
            case "AAPL":
                basePrice = new BigDecimal("180.00");
                break;
            case "MSFT":
                basePrice = new BigDecimal("420.00");
                break;
            case "GOOGL":
                basePrice = new BigDecimal("140.00");
                break;
            case "TSLA":
                basePrice = new BigDecimal("250.00");
                break;
            case "AMZN":
                basePrice = new BigDecimal("150.00");
                break;
            case "NVDA":
                basePrice = new BigDecimal("450.00");
                break;
            case "META":
                basePrice = new BigDecimal("350.00");
                break;
            case "NFLX":
                basePrice = new BigDecimal("400.00");
                break;
            case "GOOG":
                basePrice = new BigDecimal("140.00");
                break;
            case "AMD":
                basePrice = new BigDecimal("120.00");
                break;
            case "INTC":
                basePrice = new BigDecimal("35.00");
                break;
            default:
                // 알 수 없는 티커의 경우 중간 가격대로 설정
                basePrice = new BigDecimal("100.00");
                log.info("Unknown ticker for default price: {}, using fallback price: {}", ticker, basePrice);
                break;
        }
        
        // 약간의 변동성 추가 (±5%)
        BigDecimal variation = BigDecimal.valueOf(0.95 + (random.nextDouble() * 0.1));
        BigDecimal finalPrice = basePrice.multiply(variation).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        log.debug("Default price calculated for {}: base={}, variation={}, final={}", 
                 ticker, basePrice, variation, finalPrice);
        
        return finalPrice;
    }
    
    /**
     * 랜덤 슬리피지 계산
     */
    private BigDecimal calculateSlippageRate() {
        double slippage = minSlippagePercent + 
            (random.nextDouble() * (maxSlippagePercent - minSlippagePercent));
        return BigDecimal.valueOf(slippage).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 포트폴리오 포지션 업데이트
     */
    private void updatePortfolio(ChallengeSession session, Order executedOrder) {
        var existingPosition = portfolioRepository.findBySessionIdAndInstrumentKey(
            session.getId(), executedOrder.getInstrumentKey());
        
        PortfolioPosition position;
        if (existingPosition.isPresent()) {
            position = existingPosition.get();
        } else {
            position = PortfolioPosition.builder()
                .sessionId(session.getId())
                .instrumentKey(executedOrder.getInstrumentKey())
                .quantity(BigDecimal.ZERO)
                .averagePrice(BigDecimal.ZERO)
                .totalCost(BigDecimal.ZERO)
                .build();
        }
        
        if (executedOrder.getSide() == OrderSide.BUY) {
            position.addPosition(executedOrder.getQuantity(), executedOrder.getExecutedPrice());
        } else {
            // 매도 시 현금 잔고 증가
            BigDecimal saleProceeds = executedOrder.getQuantity().multiply(executedOrder.getExecutedPrice());
            BigDecimal newBalance = session.getCurrentBalance().add(saleProceeds);
            session.updateBalance(newBalance);
            
            // 포지션 감소
            position.reducePosition(executedOrder.getQuantity(), executedOrder.getExecutedPrice());
        }
        
        portfolioRepository.save(position);
    }
}