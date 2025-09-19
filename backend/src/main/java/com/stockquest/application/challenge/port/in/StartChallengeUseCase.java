package com.stockquest.application.challenge.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 시작 유스케이스 (입력 포트)
 */
public interface StartChallengeUseCase {
    
    /**
     * 챌린지 참여 시작
     */
    StartChallengeResult start(StartChallengeCommand command);
    
    record StartChallengeCommand(
        Long userId,
        Long challengeId,
        boolean forceRestart
    ) {}
    
    record StartChallengeResult(
        Long sessionId,
        Long challengeId,
        String challengeTitle,
        BigDecimal seedBalance,
        LocalDateTime startedAt
    ) {}
}