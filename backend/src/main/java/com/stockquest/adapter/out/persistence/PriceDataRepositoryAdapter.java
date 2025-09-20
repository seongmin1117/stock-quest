package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.PriceCandleJpaEntity;
import com.stockquest.adapter.out.persistence.repository.PriceCandleJpaRepository;
import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.simulation.PriceData;
import com.stockquest.domain.simulation.port.PriceDataRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주가 데이터 저장소 어댑터
 * Domain PriceDataRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 * PriceCandle 데이터를 PriceData로 변환하여 DCA 시뮬레이션에 제공
 */
@Component
public class PriceDataRepositoryAdapter implements PriceDataRepository {

    private final PriceCandleJpaRepository priceCandleRepository;

    public PriceDataRepositoryAdapter(PriceCandleJpaRepository priceCandleRepository) {
        this.priceCandleRepository = priceCandleRepository;
    }

    @Override
    public List<PriceData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        // LocalDateTime을 LocalDate로 변환
        var startLocalDate = startDate.toLocalDate();
        var endLocalDate = endDate.toLocalDate();

        // 일봉 데이터를 조회 (DCA 시뮬레이션은 일단위로 진행)
        List<PriceCandleJpaEntity> priceCandles = priceCandleRepository
                .findByTickerAndDateBetweenAndTimeframeOrderByDateAsc(
                        symbol,
                        startLocalDate,
                        endLocalDate,
                        CandleTimeframe.DAILY
                );

        // PriceCandleJpaEntity를 PriceData로 변환
        return priceCandles.stream()
                .map(candle -> new PriceData(
                        candle.getDate().atStartOfDay(), // LocalDate를 LocalDateTime으로 변환
                        candle.getClosePrice()           // 종가를 사용
                ))
                .toList();
    }
}