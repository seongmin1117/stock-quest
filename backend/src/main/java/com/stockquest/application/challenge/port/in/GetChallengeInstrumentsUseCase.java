package com.stockquest.application.challenge.port.in;

import com.stockquest.application.challenge.dto.GetChallengeInstrumentsQuery;
import com.stockquest.application.challenge.dto.GetChallengeInstrumentsResult;

/**
 * 챌린지 상품 조회 유즈케이스
 */
public interface GetChallengeInstrumentsUseCase {
    GetChallengeInstrumentsResult getChallengeInstruments(GetChallengeInstrumentsQuery query);
}