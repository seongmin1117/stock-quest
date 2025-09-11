package com.stockquest.domain.portfolio.port;

import com.stockquest.domain.portfolio.PortfolioPosition;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 저장소 포트 (출력 포트)
 */
public interface PortfolioRepository {
    
    /**
     * 포지션 저장
     */
    PortfolioPosition save(PortfolioPosition position);
    
    /**
     * ID로 포지션 조회
     */
    Optional<PortfolioPosition> findById(Long id);
    
    /**
     * 세션별 모든 포지션 조회
     */
    List<PortfolioPosition> findBySessionId(Long sessionId);
    
    /**
     * 세션 및 상품키로 포지션 조회
     */
    Optional<PortfolioPosition> findBySessionIdAndInstrumentKey(Long sessionId, String instrumentKey);
    
    /**
     * 보유량이 0보다 큰 포지션만 조회
     */
    List<PortfolioPosition> findActivePositionsBySessionId(Long sessionId);
    
    /**
     * 사용자의 활성 세션들의 포지션을 batch로 조회 (성능 최적화)
     */
    List<PortfolioPosition> findActivePositionsByUserId(Long userId);
    
    /**
     * 포트폴리오 총 가치 계산을 위한 최적화된 조회
     * 필요한 필드만 projection하여 성능 향상
     */
    List<PortfolioPositionSummary> findPortfolioSummaryBySessionId(Long sessionId);
    
    /**
     * 리더보드 계산을 위한 batch 조회 (전체 사용자)
     */
    List<PortfolioPositionSummary> findAllActivePortfolioSummaries();
    
    /**
     * 특정 기간의 포지션 변화를 효율적으로 조회
     */
    List<PortfolioPosition> findPositionChangesAfterTimestamp(Long sessionId, java.time.Instant timestamp);
    
    /**
     * 포트폴리오 성과 계산을 위한 projection interface
     */
    interface PortfolioPositionSummary {
        Long getSessionId();
        Long getUserId();
        String getInstrumentKey();
        java.math.BigDecimal getCurrentQuantity();
        java.math.BigDecimal getAveragePrice();
        java.math.BigDecimal getCurrentValue();
        java.time.Instant getLastUpdated();
    }
}