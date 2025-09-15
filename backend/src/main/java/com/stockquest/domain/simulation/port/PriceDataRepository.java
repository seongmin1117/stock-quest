package com.stockquest.domain.simulation.port;

import com.stockquest.domain.simulation.PriceData;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주가 데이터 조회를 위한 포트 인터페이스
 * DCA 시뮬레이션에 필요한 주가 데이터를 제공
 */
public interface PriceDataRepository {

    /**
     * 특정 종목의 기간별 주가 데이터 조회
     *
     * @param symbol 종목 코드 (예: AAPL, MSFT)
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간 내 주가 데이터 리스트 (날짜 순 정렬)
     */
    List<PriceData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);
}