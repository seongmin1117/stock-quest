package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.PriceCandleJpaEntity;
import com.stockquest.adapter.out.persistence.repository.PriceCandleJpaRepository;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.port.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MarketDataRepository 구현체
 * 도메인 포트와 JPA 저장소를 연결하는 어댑터
 */
@Repository
@RequiredArgsConstructor
public class MarketDataRepositoryImpl implements MarketDataRepository {

    private final PriceCandleJpaRepository jpaRepository;

    @Override
    public PriceCandle save(PriceCandle candle) {
        PriceCandleJpaEntity entity = PriceCandleJpaEntity.fromDomain(candle);
        PriceCandleJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<PriceCandle> saveAll(List<PriceCandle> candles) {
        List<PriceCandleJpaEntity> entities = candles.stream()
            .map(PriceCandleJpaEntity::fromDomain)
            .collect(Collectors.toList());
        List<PriceCandleJpaEntity> savedEntities = jpaRepository.saveAll(entities);
        return savedEntities.stream()
            .map(PriceCandleJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<PriceCandle> findByTickerAndDate(String ticker, LocalDate date, CandleTimeframe timeframe) {
        return jpaRepository.findByTickerAndDateAndTimeframe(ticker, date, timeframe)
            .map(PriceCandleJpaEntity::toDomain);
    }

    @Override
    public List<PriceCandle> findByTickerAndDateBetween(String ticker, LocalDate startDate,
                                                       LocalDate endDate, CandleTimeframe timeframe) {
        return jpaRepository.findByTickerAndDateBetweenAndTimeframeOrderByDateAsc(ticker, startDate, endDate, timeframe)
            .stream()
            .map(PriceCandleJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<PriceCandle> findLatestByTicker(String ticker, CandleTimeframe timeframe) {
        return jpaRepository.findLatestByTickerAndTimeframe(ticker, timeframe)
            .map(PriceCandleJpaEntity::toDomain);
    }

    @Override
    public List<String> findAvailableTickersForDate(LocalDate date, CandleTimeframe timeframe) {
        return jpaRepository.findDistinctTickersByDateAndTimeframe(date, timeframe);
    }
}