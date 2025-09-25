package com.stockquest.application.port.out;

import com.stockquest.domain.challenge.MarketPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 시장 기간 데이터 액세스를 위한 포트 인터페이스
 */
public interface MarketPeriodPort {
    
    /**
     * 시장 기간 저장
     */
    MarketPeriod save(MarketPeriod marketPeriod);
    
    /**
     * ID로 시장 기간 조회
     */
    Optional<MarketPeriod> findById(Long id);
    
    /**
     * 활성 상태의 모든 시장 기간 조회
     */
    List<MarketPeriod> findAllActive();
    
    /**
     * 모든 시장 기간 조회
     */
    List<MarketPeriod> findAll();
    
    /**
     * 기간 유형별 조회
     */
    List<MarketPeriod> findByPeriodType(String periodType);
    
    /**
     * 날짜 범위로 시장 기간 조회
     */
    List<MarketPeriod> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 시장 지역별 조회
     */
    List<MarketPeriod> findByMarketRegion(String marketRegion);
    
    /**
     * 난이도별 조회
     */
    List<MarketPeriod> findByDifficultyRating(int minRating, int maxRating);
    
    /**
     * 시장 기간 삭제
     */
    void deleteById(Long id);
    
    /**
     * 이름으로 시장 기간 검색
     */
    List<MarketPeriod> findByNameContaining(String name);
}