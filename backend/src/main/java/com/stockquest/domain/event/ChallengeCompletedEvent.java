package com.stockquest.domain.event;

import java.time.LocalDateTime;

/**
 * 챌린지 완료 도메인 이벤트
 * 챌린지 세션이 완료되었을 때 발행되는 이벤트
 */
public record ChallengeCompletedEvent(
        Long challengeId,
        Long sessionId,
        Long userId,
        LocalDateTime completedAt
) {

    public static ChallengeCompletedEvent of(Long challengeId, Long sessionId, Long userId) {
        return new ChallengeCompletedEvent(challengeId, sessionId, userId, LocalDateTime.now());
    }
}