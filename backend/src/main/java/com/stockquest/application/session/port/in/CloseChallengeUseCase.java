package com.stockquest.application.session.port.in;

import java.math.BigDecimal;
import java.util.List;

/**
 * 챌린지 종료 유스케이스 (입력 포트)
 */
public interface CloseChallengeUseCase {
    
    /**
     * 챌린지 세션 종료 및 결과 계산
     */
    CloseChallengeResult close(CloseChallengeCommand command);
    
    record CloseChallengeCommand(
        Long sessionId
    ) {}
    
    record CloseChallengeResult(
        Long sessionId,
        BigDecimal finalBalance,
        BigDecimal totalPnL,
        BigDecimal returnPercentage,
        Integer rank,
        List<RevealedInstrument> revealedInstruments
    ) {}
    
    record RevealedInstrument(
        String instrumentKey,
        String actualTicker,
        String actualName
    ) {}
}