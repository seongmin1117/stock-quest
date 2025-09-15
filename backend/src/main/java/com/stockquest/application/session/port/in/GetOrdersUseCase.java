package com.stockquest.application.session.port.in;

import com.stockquest.application.session.dto.GetOrdersQuery;
import com.stockquest.application.session.dto.GetOrdersResult;

/**
 * 주문 내역 조회 유스케이스
 */
public interface GetOrdersUseCase {

    /**
     * 세션의 주문 내역 조회
     */
    GetOrdersResult getOrders(GetOrdersQuery query);
}