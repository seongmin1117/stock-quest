package com.stockquest.application.session.port.in;

import com.stockquest.application.session.dto.GetPortfolioQuery;
import com.stockquest.application.session.dto.GetPortfolioResult;

/**
 * 포트폴리오 조회 유스케이스
 */
public interface GetPortfolioUseCase {

    /**
     * 세션의 포트폴리오 조회
     */
    GetPortfolioResult getPortfolio(GetPortfolioQuery query);
}