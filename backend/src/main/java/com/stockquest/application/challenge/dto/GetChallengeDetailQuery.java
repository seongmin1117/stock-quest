package com.stockquest.application.challenge.dto;

/**
 * 챌린지 상세 조회 쿼리
 */
public record GetChallengeDetailQuery(
    Long challengeId,
    Long userId
) {
}