package com.stockquest.application.exception;

/**
 * 챌린지 상태가 유효하지 않은 경우의 예외
 */
public class InvalidChallengeStateException extends DomainException {
    
    public InvalidChallengeStateException(String message) {
        super(message);
    }
    
    public InvalidChallengeStateException(String currentState, String requiredState) {
        super(String.format("Invalid challenge state. Current: %s, Required: %s", currentState, requiredState));
    }
}