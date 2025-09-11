package com.stockquest.adapter.in.web.challenge.dto;

import com.stockquest.application.challenge.dto.GetChallengeDetailResult;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 챌린지 상세 조회 응답 DTO
 */
@Builder
public record ChallengeDetailResponse(
    Long id,
    String title,
    String description,
    ChallengeDifficulty difficulty,
    ChallengeStatus status,
    BigDecimal initialBalance,
    Integer durationDays,
    LocalDateTime startDate,
    LocalDateTime endDate,
    UserSession userSession
) {
    
    @Builder
    public record UserSession(
        Long sessionId,
        SessionStatus status,
        BigDecimal currentBalance,
        BigDecimal returnRate,
        LocalDateTime startedAt,
        LocalDateTime completedAt
    ) {
        
        public static UserSession from(com.stockquest.domain.session.ChallengeSession session) {
            if (session == null) {
                return null;
            }
            
            return UserSession.builder()
                    .sessionId(session.getId())
                    .status(session.getStatus())
                    .currentBalance(session.getCurrentBalance())
                    .returnRate(session.getReturnRate())
                    .startedAt(session.getStartedAt())
                    .completedAt(session.getCompletedAt())
                    .build();
        }
    }
    
    public static ChallengeDetailResponse from(GetChallengeDetailResult result) {
        return ChallengeDetailResponse.builder()
                .id(result.challenge().getId())
                .title(result.challenge().getTitle())
                .description(result.challenge().getDescription())
                .difficulty(result.challenge().getDifficulty())
                .status(result.challenge().getStatus())
                .initialBalance(result.challenge().getInitialBalance())
                .durationDays(result.challenge().getDurationDays())
                .startDate(result.challenge().getStartDate())
                .endDate(result.challenge().getEndDate())
                .userSession(UserSession.from(result.userSession()))
                .build();
    }
}