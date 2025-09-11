package com.stockquest.application.exception;

/**
 * 도메인 계층의 기본 예외 클래스
 */
public abstract class DomainException extends RuntimeException {
    
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}