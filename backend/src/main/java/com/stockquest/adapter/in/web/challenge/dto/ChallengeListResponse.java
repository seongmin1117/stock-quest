package com.stockquest.adapter.in.web.challenge.dto;

import com.stockquest.application.challenge.dto.GetChallengeListResult;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 챌린지 목록 조회 응답 DTO
 */
@Builder
public record ChallengeListResponse(
    List<ChallengeItem> challenges,
    int totalCount,
    int page,
    int size
) {
    
    @Builder
    public record ChallengeItem(
        Long id,
        String title,
        String description,
        ChallengeDifficulty difficulty,
        ChallengeStatus status,
        BigDecimal initialBalance,
        Integer durationDays,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        
        public static ChallengeItem from(Challenge challenge) {
            return ChallengeItem.builder()
                    .id(challenge.getId())
                    .title(challenge.getTitle())
                    .description(challenge.getDescription())
                    .difficulty(challenge.getDifficulty())
                    .status(challenge.getStatus())
                    .initialBalance(challenge.getInitialBalance())
                    .durationDays(challenge.getDurationDays())
                    .startDate(challenge.getStartDate())
                    .endDate(challenge.getEndDate())
                    .build();
        }
    }
    
    public static ChallengeListResponse from(GetChallengeListResult result) {
        List<ChallengeItem> challengeItems = result.challenges()
                .stream()
                .map(ChallengeItem::from)
                .collect(Collectors.toList());
        
        return ChallengeListResponse.builder()
                .challenges(challengeItems)
                .totalCount(result.totalCount())
                .page(result.page())
                .size(result.size())
                .build();
    }
}