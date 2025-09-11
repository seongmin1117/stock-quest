package com.stockquest.application.exception;

import java.math.BigDecimal;

/**
 * 자금이 부족한 경우의 예외
 */
public class InsufficientFundsException extends DomainException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient funds. Required: %s, Available: %s", required, available));
    }
}