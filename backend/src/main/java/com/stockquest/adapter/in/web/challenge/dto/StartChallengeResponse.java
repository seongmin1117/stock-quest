package com.stockquest.adapter.in.web.challenge.dto;

import com.stockquest.application.challenge.port.in.StartChallengeUseCase.StartChallengeResult;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 시작 응답 DTO
 */
@Builder
public record StartChallengeResponse(
    Long sessionId,
    Long challengeId,
    String challengeTitle,
    BigDecimal seedBalance,
    LocalDateTime startedAt,
    String message
) {
    
    public static StartChallengeResponse from(StartChallengeResult result) {
        return StartChallengeResponse.builder()
                .sessionId(result.sessionId())
                .challengeId(result.challengeId())
                .challengeTitle(result.challengeTitle())
                .seedBalance(result.seedBalance())
                .startedAt(result.startedAt())
                .message("챌린지가 성공적으로 시작되었습니다.")
                .build();
    }
}