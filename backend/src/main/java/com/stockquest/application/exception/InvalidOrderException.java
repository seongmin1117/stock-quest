package com.stockquest.application.exception;

/**
 * 잘못된 주문 요청의 경우의 예외
 */
public class InvalidOrderException extends DomainException {
    
    public InvalidOrderException(String message) {
        super(message);
    }
    
    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}