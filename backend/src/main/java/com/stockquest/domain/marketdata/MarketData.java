package com.stockquest.domain.marketdata;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(name = "open_price")
    private BigDecimal openPrice;
    
    @Column(name = "high_price")  
    private BigDecimal highPrice;
    
    @Column(name = "low_price")
    private BigDecimal lowPrice;
    
    private Long volume;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "market_cap")
    private BigDecimal marketCap;
}