package com.stockquest.adapter.in.web.session.dto;

import com.stockquest.application.session.port.in.CloseChallengeUseCase.CloseChallengeResult;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 챌린지 종료 응답 DTO
 */
@Builder
public record CloseChallengeResponse(
    Long sessionId,
    BigDecimal finalBalance,
    BigDecimal totalPnL,
    BigDecimal returnPercentage,
    Integer rank,
    List<RevealedInstrument> revealedInstruments,
    String message
) {
    
    @Builder
    public record RevealedInstrument(
        String instrumentKey,
        String realSymbol,
        String realName
    ) {
        
        public static RevealedInstrument from(com.stockquest.application.session.port.in.CloseChallengeUseCase.RevealedInstrument instrument) {
            return RevealedInstrument.builder()
                    .instrumentKey(instrument.instrumentKey())
                    .realSymbol(instrument.actualTicker())
                    .realName(instrument.actualName())
                    .build();
        }
    }
    
    public static CloseChallengeResponse from(CloseChallengeResult result) {
        List<RevealedInstrument> instruments = result.revealedInstruments()
                .stream()
                .map(RevealedInstrument::from)
                .collect(Collectors.toList());
        
        return CloseChallengeResponse.builder()
                .sessionId(result.sessionId())
                .finalBalance(result.finalBalance())
                .totalPnL(result.totalPnL())
                .returnPercentage(result.returnPercentage())
                .rank(result.rank())
                .revealedInstruments(instruments)
                .message("챌린지가 성공적으로 종료되었습니다.")
                .build();
    }
}