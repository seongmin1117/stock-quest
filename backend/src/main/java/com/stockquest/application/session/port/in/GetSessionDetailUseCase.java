package com.stockquest.application.session.port.in;

import com.stockquest.application.session.dto.GetSessionDetailQuery;
import com.stockquest.application.session.dto.GetSessionDetailResult;

/**
 * 세션 상세 조회 입력 포트
 */
public interface GetSessionDetailUseCase {
    
    /**
     * 세션 상세 조회
     */
    GetSessionDetailResult getSessionDetail(GetSessionDetailQuery query);
}