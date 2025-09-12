package com.stockquest.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주식 데이터 도메인 모델 (백테스팅용)
 * Phase 8.2: Enhanced Trading Intelligence - 주식 데이터 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    
    /**
     * 주식 심볼
     */
    private String symbol;
    
    /**
     * 데이터 타임스탬프
     */
    private LocalDateTime timestamp;
    
    /**
     * 시가
     */
    private BigDecimal openPrice;
    
    /**
     * 고가
     */
    private BigDecimal highPrice;
    
    /**
     * 저가
     */
    private BigDecimal lowPrice;
    
    /**
     * 종가
     */
    private BigDecimal closePrice;
    
    /**
     * 거래량
     */
    private Long volume;
    
    /**
     * 조정 종가
     */
    private BigDecimal adjustedClosePrice;
    
    /**
     * 시가총액
     */
    private BigDecimal marketCap;
    
    /**
     * 주가수익비율 (PER)
     */
    private BigDecimal priceToEarningsRatio;
    
    /**
     * 주가순자산비율 (PBR)
     */
    private BigDecimal priceToBookRatio;
    
    /**
     * 배당수익률 (%)
     */
    private BigDecimal dividendYield;
    
    /**
     * 52주 최고가
     */
    private BigDecimal fiftyTwoWeekHigh;
    
    /**
     * 52주 최저가
     */
    private BigDecimal fiftyTwoWeekLow;
    
    /**
     * 베타 계수
     */
    private BigDecimal beta;
    
    /**
     * 평균 거래량 (20일)
     */
    private Long averageVolume;
    
    /**
     * 주식 분류
     */
    private StockType stockType;
    
    /**
     * 섹터
     */
    private String sector;
    
    /**
     * 산업군
     */
    private String industry;
    
    /**
     * 회사명
     */
    private String companyName;
    
    /**
     * 설명
     */
    private String description;
    
    /**
     * 통화
     */
    @Builder.Default
    private String currency = "USD";
    
    /**
     * 국가
     */
    @Builder.Default
    private String country = "US";
    
    /**
     * 거래소
     */
    private String exchange;
    
    /**
     * 상장일
     */
    private LocalDateTime listingDate;
    
    public enum StockType {
        COMMON_STOCK,
        PREFERRED_STOCK,
        ETF,
        MUTUAL_FUND,
        INDEX,
        BOND,
        OPTION,
        FUTURE,
        CRYPTOCURRENCY
    }
    
    /**
     * 현재 가격 반환 (종가 기준)
     */
    public BigDecimal getCurrentPrice() {
        return closePrice != null ? closePrice : adjustedClosePrice;
    }
    
    /**
     * 일간 변동률 계산 (%)
     */
    public BigDecimal getDailyChangePercent() {
        if (openPrice == null || closePrice == null || 
            openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return closePrice.subtract(openPrice)
                .divide(openPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 일간 변동량 계산
     */
    public BigDecimal getDailyChange() {
        if (openPrice == null || closePrice == null) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice);
    }
    
    /**
     * 일간 레인지 계산 (고가 - 저가)
     */
    public BigDecimal getDailyRange() {
        if (highPrice == null || lowPrice == null) {
            return BigDecimal.ZERO;
        }
        return highPrice.subtract(lowPrice);
    }
    
    /**
     * 상장 이후 경과 일수
     */
    public long getDaysFromListing() {
        if (listingDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(listingDate, LocalDateTime.now());
    }
    
    /**
     * 주식이 유효한 거래 데이터를 가지고 있는지 확인
     */
    public boolean isValidTradingData() {
        return symbol != null && !symbol.trim().isEmpty() &&
               timestamp != null &&
               closePrice != null && closePrice.compareTo(BigDecimal.ZERO) > 0 &&
               volume != null && volume > 0;
    }
    
    /**
     * 고배당주인지 확인 (배당수익률 4% 이상)
     */
    public boolean isHighDividendStock() {
        return dividendYield != null && 
               dividendYield.compareTo(BigDecimal.valueOf(4.0)) >= 0;
    }
    
    /**
     * 대형주인지 확인 (시가총액 100억 달러 이상)
     */
    public boolean isLargeCapStock() {
        return marketCap != null && 
               marketCap.compareTo(BigDecimal.valueOf(10_000_000_000L)) >= 0;
    }
    
    /**
     * 중형주인지 확인 (시가총액 20억~100억 달러)
     */
    public boolean isMidCapStock() {
        return marketCap != null && 
               marketCap.compareTo(BigDecimal.valueOf(2_000_000_000L)) >= 0 &&
               marketCap.compareTo(BigDecimal.valueOf(10_000_000_000L)) < 0;
    }
    
    /**
     * 소형주인지 확인 (시가총액 20억 달러 미만)
     */
    public boolean isSmallCapStock() {
        return marketCap != null && 
               marketCap.compareTo(BigDecimal.valueOf(2_000_000_000L)) < 0;
    }
    
    /**
     * 고베타 주식인지 확인 (베타 1.5 이상)
     */
    public boolean isHighBetaStock() {
        return beta != null && 
               beta.compareTo(BigDecimal.valueOf(1.5)) >= 0;
    }
    
    /**
     * 저베타 주식인지 확인 (베타 0.7 이하)
     */
    public boolean isLowBetaStock() {
        return beta != null && 
               beta.compareTo(BigDecimal.valueOf(0.7)) <= 0;
    }
    
    /**
     * 52주 고점 근처인지 확인 (고점의 95% 이상)
     */
    public boolean isNearFiftyTwoWeekHigh() {
        if (fiftyTwoWeekHigh == null || closePrice == null) {
            return false;
        }
        
        BigDecimal threshold = fiftyTwoWeekHigh.multiply(BigDecimal.valueOf(0.95));
        return closePrice.compareTo(threshold) >= 0;
    }
    
    /**
     * 52주 저점 근처인지 확인 (저점의 105% 이하)
     */
    public boolean isNearFiftyTwoWeekLow() {
        if (fiftyTwoWeekLow == null || closePrice == null) {
            return false;
        }
        
        BigDecimal threshold = fiftyTwoWeekLow.multiply(BigDecimal.valueOf(1.05));
        return closePrice.compareTo(threshold) <= 0;
    }
}