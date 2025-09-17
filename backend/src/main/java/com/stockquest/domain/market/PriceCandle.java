package com.stockquest.domain.market;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가격 캔들 도메인 엔티티 (OHLC 데이터)
 * Yahoo Finance에서 수집한 과거 시장 데이터
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PriceCandle {
    private Long id;
    private String ticker;           // 실제 티커 심볼
    private LocalDate date;          // 거래일
    private BigDecimal openPrice;    // 시가
    private BigDecimal highPrice;    // 고가
    private BigDecimal lowPrice;     // 저가
    private BigDecimal closePrice;   // 종가
    private Long volume;             // 거래량
    private CandleTimeframe timeframe; // 시간대 (일봉, 주봉 등)
    
    
    public PriceCandle(String ticker, LocalDate date, BigDecimal openPrice, 
                      BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice, 
                      Long volume, CandleTimeframe timeframe) {
        validateTicker(ticker);
        validateDate(date);
        validatePrices(openPrice, highPrice, lowPrice, closePrice);
        validateVolume(volume);
        
        this.ticker = ticker;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.timeframe = timeframe != null ? timeframe : CandleTimeframe.DAILY;
    }
    
    private void validateTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("티커는 필수입니다");
        }
        if (!ticker.matches("^[A-Z]{1,10}$")) {
            throw new IllegalArgumentException("유효한 티커 형식이 아닙니다");
        }
    }
    
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜는 허용되지 않습니다");
        }
    }
    
    private void validatePrices(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        if (open == null || high == null || low == null || close == null) {
            throw new IllegalArgumentException("모든 가격 정보는 필수입니다");
        }
        if (open.compareTo(BigDecimal.ZERO) <= 0 || high.compareTo(BigDecimal.ZERO) <= 0 ||
            low.compareTo(BigDecimal.ZERO) <= 0 || close.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException("고가는 저가보다 크거나 같아야 합니다");
        }
        if (open.compareTo(low) < 0 || open.compareTo(high) > 0) {
            throw new IllegalArgumentException("시가는 고가와 저가 사이에 있어야 합니다");
        }
        if (close.compareTo(low) < 0 || close.compareTo(high) > 0) {
            throw new IllegalArgumentException("종가는 고가와 저가 사이에 있어야 합니다");
        }
    }
    
    private void validateVolume(Long volume) {
        if (volume == null || volume < 0) {
            throw new IllegalArgumentException("거래량은 0 이상이어야 합니다");
        }
    }

    /**
     * symbol 대신 ticker를 사용하는 경우를 위한 메서드
     */
    public String getSymbol() {
        return ticker;
    }
    
    /**
     * 해당 날짜의 일중 변동률 계산
     */
    public BigDecimal calculateDailyReturn() {
        if (openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice)
                        .divide(openPrice, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
    }
    
    /**
     * 변동성 계산 (고가-저가 범위)
     */
    public BigDecimal calculateVolatility() {
        if (openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return highPrice.subtract(lowPrice)
                       .divide(openPrice, 4, BigDecimal.ROUND_HALF_UP)
                       .multiply(new BigDecimal("100"));
    }
    
}