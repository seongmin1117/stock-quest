package com.stockquest.application.exception;

/**
 * 챌린지를 찾을 수 없는 경우의 예외
 */
public class ChallengeNotFoundException extends DomainException {
    
    public ChallengeNotFoundException(String message) {
        super(message);
    }
    
    public ChallengeNotFoundException(Long challengeId) {
        super("Challenge not found with ID: " + challengeId);
    }
}