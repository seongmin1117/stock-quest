package com.stockquest.application.challenge.port.in;

import com.stockquest.application.challenge.dto.GetChallengeDetailQuery;
import com.stockquest.application.challenge.dto.GetChallengeDetailResult;

/**
 * 챌린지 상세 조회 입력 포트
 */
public interface GetChallengeDetailUseCase {
    
    /**
     * 챌린지 상세 조회
     */
    GetChallengeDetailResult getChallengeDetail(GetChallengeDetailQuery query);
}