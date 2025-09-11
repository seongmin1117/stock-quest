package com.stockquest.domain.challenge;

/**
 * 금융상품 유형
 */
public enum InstrumentType {
    STOCK("주식"),
    DEPOSIT("예금"),
    BOND("채권");
    
    private final String description;
    
    InstrumentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}