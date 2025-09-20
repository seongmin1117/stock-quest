package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.PriceCandleJpaEntity;
import com.stockquest.domain.market.CandleTimeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 가격 캔들 JPA 리포지토리
 */
@Repository
public interface PriceCandleJpaRepository extends JpaRepository<PriceCandleJpaEntity, Long> {
    
    /**
     * 특정 티커의 특정 날짜 캔들 조회
     */
    Optional<PriceCandleJpaEntity> findByTickerAndDateAndTimeframe(String ticker, LocalDate date, CandleTimeframe timeframe);
    
    /**
     * 특정 티커의 기간별 캔들 조회
     */
    List<PriceCandleJpaEntity> findByTickerAndDateBetweenAndTimeframeOrderByDateAsc(
        String ticker, LocalDate startDate, LocalDate endDate, CandleTimeframe timeframe);
    
    /**
     * 특정 티커의 최신 캔들 조회
     */
    @Query("SELECT p FROM PriceCandleJpaEntity p WHERE p.ticker = :ticker AND p.timeframe = :timeframe ORDER BY p.date DESC LIMIT 1")
    Optional<PriceCandleJpaEntity> findLatestByTickerAndTimeframe(@Param("ticker") String ticker, @Param("timeframe") CandleTimeframe timeframe);
    
    /**
     * 특정 날짜에 데이터가 있는 모든 티커 조회
     */
    @Query("SELECT DISTINCT p.ticker FROM PriceCandleJpaEntity p WHERE p.date = :date AND p.timeframe = :timeframe")
    List<String> findDistinctTickersByDateAndTimeframe(@Param("date") LocalDate date, @Param("timeframe") CandleTimeframe timeframe);
}