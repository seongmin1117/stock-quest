package com.stockquest.domain.market;

/**
 * 캔들 시간대 구분
 */
public enum CandleTimeframe {
    DAILY("일봉"),
    WEEKLY("주봉"),
    MONTHLY("월봉");
    
    private final String description;
    
    CandleTimeframe(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}