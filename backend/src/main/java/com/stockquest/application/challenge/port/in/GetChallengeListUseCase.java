package com.stockquest.application.challenge.port.in;

import com.stockquest.application.challenge.dto.GetChallengeListQuery;
import com.stockquest.application.challenge.dto.GetChallengeListResult;

/**
 * 챌린지 목록 조회 입력 포트
 */
public interface GetChallengeListUseCase {
    
    /**
     * 챌린지 목록 조회
     */
    GetChallengeListResult getChallengeList(GetChallengeListQuery query);
}