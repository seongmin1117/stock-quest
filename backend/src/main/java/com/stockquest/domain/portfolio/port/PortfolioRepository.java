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
}