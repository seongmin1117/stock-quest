package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.session.ChallengeSession;

/**
 * 챌린지 상세 조회 결과
 */
public record GetChallengeDetailResult(
    Challenge challenge,
    ChallengeSession userSession
) {
}