package com.stockquest.application.exception;

/**
 * 세션을 찾을 수 없는 경우의 예외
 */
public class SessionNotFoundException extends DomainException {
    
    public SessionNotFoundException(String message) {
        super(message);
    }
    
    public SessionNotFoundException(Long sessionId) {
        super("Session not found with ID: " + sessionId);
    }
}