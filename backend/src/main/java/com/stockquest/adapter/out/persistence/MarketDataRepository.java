package com.stockquest.adapter.out.persistence;

import com.stockquest.domain.marketdata.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    
    Optional<MarketData> findBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);
    
    List<MarketData> findBySymbolOrderByTimestampDesc(String symbol);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol = :symbol AND md.timestamp >= :startTime AND md.timestamp <= :endTime ORDER BY md.timestamp")
    List<MarketData> findBySymbolAndTimestampBetween(@Param("symbol") String symbol, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT md FROM MarketData md WHERE md.timestamp >= :startTime ORDER BY md.timestamp")
    List<MarketData> findByTimestampAfter(@Param("startTime") LocalDateTime startTime);
}