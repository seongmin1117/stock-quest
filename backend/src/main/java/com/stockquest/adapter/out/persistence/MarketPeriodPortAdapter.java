package com.stockquest.adapter.out.persistence;

import com.stockquest.application.port.out.MarketPeriodPort;
import com.stockquest.domain.challenge.MarketPeriod;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * MarketPeriodPort 인터페이스의 어댑터 구현체
 * TODO: 실제 JPA 구현으로 대체 필요
 * 현재는 메모리 기반 임시 구현
 */
@Repository
public class MarketPeriodPortAdapter implements MarketPeriodPort {

    private final ConcurrentHashMap<Long, MarketPeriod> marketPeriods = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public MarketPeriodPortAdapter() {
        // 초기 데이터 로딩 (실제로는 데이터베이스에서 로딩)
        initializeMarketPeriods();
    }

    @Override
    public MarketPeriod save(MarketPeriod marketPeriod) {
        if (marketPeriod.getId() == null) {
            // MarketPeriod는 이제 Lombok Builder를 사용하므로 toBuilder()로 ID 설정
            Long newId = idGenerator.getAndIncrement();
            marketPeriod = marketPeriod.toBuilder().id(newId).build();
        }
        marketPeriods.put(marketPeriod.getId(), marketPeriod);
        return marketPeriod;
    }

    @Override
    public Optional<MarketPeriod> findById(Long id) {
        return Optional.ofNullable(marketPeriods.get(id));
    }

    @Override
    public List<MarketPeriod> findAllActive() {
        return marketPeriods.values().stream()
                .filter(MarketPeriod::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketPeriod> findAll() {
        return new ArrayList<>(marketPeriods.values());
    }

    @Override
    public List<MarketPeriod> findByPeriodType(String periodType) {
        return marketPeriods.values().stream()
                .filter(period -> period.getPeriodType().name().equals(periodType.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketPeriod> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return marketPeriods.values().stream()
                .filter(period -> !period.getStartDate().isAfter(endDate) && !period.getEndDate().isBefore(startDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketPeriod> findByMarketRegion(String marketRegion) {
        return marketPeriods.values().stream()
                .filter(period -> marketRegion.equals(period.getMarketRegion()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketPeriod> findByDifficultyRating(int minRating, int maxRating) {
        return marketPeriods.values().stream()
                .filter(period -> period.getDifficultyRating() >= minRating && period.getDifficultyRating() <= maxRating)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        marketPeriods.remove(id);
    }

    @Override
    public List<MarketPeriod> findByNameContaining(String name) {
        return marketPeriods.values().stream()
                .filter(period -> period.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void initializeMarketPeriods() {
        // V17 마이그레이션에서 생성된 시장 기간 데이터와 동일한 데이터 생성
        save(new MarketPeriod("2008 Financial Crisis", "글로벌 금융위기 시기", 
                MarketPeriod.PeriodType.CRASH, 
                LocalDate.of(2008, 9, 1), LocalDate.of(2009, 3, 31)));
        
        save(new MarketPeriod("COVID-19 Pandemic", "코로나19 팬데믹 시기", 
                MarketPeriod.PeriodType.VOLATILITY, 
                LocalDate.of(2020, 2, 1), LocalDate.of(2020, 12, 31)));
        
        save(new MarketPeriod("Tech Bubble 2000", "닷컴 버블 시기", 
                MarketPeriod.PeriodType.CRASH, 
                LocalDate.of(2000, 3, 1), LocalDate.of(2002, 10, 31)));
        
        save(new MarketPeriod("Bull Market 2009-2020", "대규모 상승장 시기", 
                MarketPeriod.PeriodType.BULL_MARKET, 
                LocalDate.of(2009, 4, 1), LocalDate.of(2020, 1, 31)));
        
        save(new MarketPeriod("Recovery 2021", "코로나 회복기", 
                MarketPeriod.PeriodType.RECOVERY, 
                LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)));
    }
}