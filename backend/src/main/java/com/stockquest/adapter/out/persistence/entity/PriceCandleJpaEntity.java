package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.CandleTimeframe;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가격 캔들 JPA 엔티티
 */
@Entity
@Table(name = "price_candle")
public class PriceCandleJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String ticker;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "open_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal openPrice;
    
    @Column(name = "high_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal highPrice;
    
    @Column(name = "low_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal lowPrice;
    
    @Column(name = "close_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal closePrice;
    
    @Column(nullable = false)
    private Long volume;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CandleTimeframe timeframe;
    
    protected PriceCandleJpaEntity() {}
    
    public PriceCandleJpaEntity(String ticker, LocalDate date, BigDecimal openPrice,
                               BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                               Long volume, CandleTimeframe timeframe) {
        this.ticker = ticker;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.timeframe = timeframe;
    }
    
    /**
     * 도메인 객체로부터 JPA 엔티티 생성
     */
    public static PriceCandleJpaEntity fromDomain(PriceCandle priceCandle) {
        PriceCandleJpaEntity entity = new PriceCandleJpaEntity(
            priceCandle.getTicker(),
            priceCandle.getDate(),
            priceCandle.getOpenPrice(),
            priceCandle.getHighPrice(),
            priceCandle.getLowPrice(),
            priceCandle.getClosePrice(),
            priceCandle.getVolume(),
            priceCandle.getTimeframe()
        );
        entity.id = priceCandle.getId();
        return entity;
    }
    
    /**
     * JPA 엔티티를 도메인 객체로 변환
     */
    public PriceCandle toDomain() {
        PriceCandle priceCandle = new PriceCandle(
            this.ticker,
            this.date,
            this.openPrice,
            this.highPrice,
            this.lowPrice,
            this.closePrice,
            this.volume,
            this.timeframe
        );
        // Reflection을 사용하여 ID 설정 (실제 프로젝트에서는 Builder 패턴이나 생성자 사용 권장)
        try {
            var idField = PriceCandle.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(priceCandle, this.id);
        } catch (Exception e) {
            // ID 설정 실패 시 로그만 기록하고 계속 진행
        }
        return priceCandle;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    
    public CandleTimeframe getTimeframe() { return timeframe; }
    public void setTimeframe(CandleTimeframe timeframe) { this.timeframe = timeframe; }
}