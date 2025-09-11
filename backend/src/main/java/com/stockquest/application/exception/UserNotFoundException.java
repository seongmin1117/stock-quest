package com.stockquest.application.exception;

/**
 * 사용자를 찾을 수 없는 경우의 예외
 */
public class UserNotFoundException extends DomainException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}