package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.Challenge;

import java.util.List;

/**
 * 챌린지 목록 조회 결과
 */
public record GetChallengeListResult(
    List<Challenge> challenges,
    int totalCount,
    int page,
    int size
) {
}