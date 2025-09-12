package com.stockquest.domain.portfolio;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 포지션 도메인 엔터티
 * 헥사고날 아키텍처 - 순수 도메인 로직
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Position {
    
    private Long id;
    private String portfolioId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal averageCost;
    private BigDecimal unrealizedPnL;
    private BigDecimal realizedPnL;
    private PositionType positionType;
    private PositionStatus status;
    private LocalDate openDate;
    private LocalDate closeDate;
    private LocalDateTime entryDate;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<PositionEvent> positionEvents = new ArrayList<>();
    
    /**
     * 포지션의 현재 시장 가치 계산
     */
    public BigDecimal getValue() {
        if (quantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice);
    }
    
    /**
     * 포지션의 현재 시장 가치 계산 (alias method)
     */
    public BigDecimal getCurrentValue() {
        return getValue();
    }
    
    /**
     * 포지션의 손익 계산
     */
    public BigDecimal getProfitLoss() {
        if (quantity == null || currentPrice == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice.subtract(averagePrice));
    }
    
    /**
     * 포지션의 손익률 계산 (%)
     */
    public BigDecimal getProfitLossPercentage() {
        if (averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profitLoss = getProfitLoss();
        BigDecimal totalCost = quantity.multiply(averagePrice);
        
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return profitLoss.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 포지션 비중 계산 (전체 포트폴리오 대비)
     */
    public BigDecimal getWeight(BigDecimal totalPortfolioValue) {
        if (totalPortfolioValue == null || totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getValue().divide(totalPortfolioValue, 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 포지션 종료 (도메인 비즈니스 로직)
     */
    public Position close(LocalDate closeDate) {
        validateCloseOperation();
        return Position.builder()
                .id(this.id)
                .portfolioId(this.portfolioId)
                .symbol(this.symbol)
                .quantity(this.quantity)
                .averagePrice(this.averagePrice)
                .currentPrice(this.currentPrice)
                .averageCost(this.averageCost)
                .unrealizedPnL(this.unrealizedPnL)
                .realizedPnL(this.realizedPnL)
                .positionType(this.positionType)
                .status(PositionStatus.CLOSED)
                .openDate(this.openDate)
                .closeDate(closeDate)
                .entryDate(this.entryDate)
                .lastUpdatedAt(LocalDateTime.now())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .positionEvents(this.positionEvents)
                .build();
    }
    
    /**
     * 가격 업데이트 (불변성 유지)
     */
    public Position updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
        
        return Position.builder()
                .id(this.id)
                .portfolioId(this.portfolioId)
                .symbol(this.symbol)
                .quantity(this.quantity)
                .averagePrice(this.averagePrice)
                .currentPrice(newPrice)
                .averageCost(this.averageCost)
                .unrealizedPnL(calculateUnrealizedPnL(newPrice))
                .realizedPnL(this.realizedPnL)
                .positionType(this.positionType)
                .status(this.status)
                .openDate(this.openDate)
                .closeDate(this.closeDate)
                .entryDate(this.entryDate)
                .lastUpdatedAt(LocalDateTime.now())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .positionEvents(this.positionEvents)
                .build();
    }
    
    /**
     * 도메인 검증 로직
     */
    private void validateCloseOperation() {
        if (status == PositionStatus.CLOSED) {
            throw new IllegalStateException("이미 종료된 포지션입니다");
        }
    }
    
    /**
     * 미실현 손익 계산
     */
    private BigDecimal calculateUnrealizedPnL(BigDecimal currentPrice) {
        if (quantity == null || currentPrice == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice.subtract(averagePrice));
    }
    
    /**
     * 포지션 타입 열거형
     */
    public enum PositionType {
        LONG("매수", "Long position"),
        SHORT("매도", "Short position");
        
        private final String koreanName;
        private final String description;
        
        PositionType(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 포지션 상태 열거형
     */
    public enum PositionStatus {
        OPEN("오픈", "Position is open"),
        CLOSED("종료", "Position is closed"),
        PARTIALLY_CLOSED("부분종료", "Position is partially closed");
        
        private final String koreanName;
        private final String description;
        
        PositionStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 포지션 이벤트
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionEvent {
        private String eventId;
        private PositionEventType eventType;
        private LocalDateTime timestamp;
        private String description;
        private BigDecimal quantity;
        private BigDecimal price;
        private String details;
    }
    
    /**
     * 포지션 이벤트 타입
     */
    public enum PositionEventType {
        POSITION_OPENED("포지션오픈"),
        POSITION_INCREASED("포지션증가"),
        POSITION_DECREASED("포지션감소"),
        POSITION_CLOSED("포지션종료"),
        PRICE_UPDATE("가격업데이트"),
        PNL_REALIZED("손익실현");
        
        private final String koreanName;
        
        PositionEventType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() { return koreanName; }
    }
}