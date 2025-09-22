package com.stockquest.application.challenge.dto;

/**
 * 챌린지 목록 조회 쿼리
 */
public record GetChallengeListQuery(
    int page,
    int size,
    Long userId
) {
}