package com.stockquest.adapter.in.web.leaderboard.dto;

import com.stockquest.domain.leaderboard.LeaderboardEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리더보드 응답 DTO
 */
@Schema(description = "리더보드 엔트리")
public record LeaderboardResponse(
        @Schema(description = "엔트리 ID")
        Long id,
        
        @Schema(description = "챌린지 ID")
        Long challengeId,
        
        @Schema(description = "세션 ID")
        Long sessionId,
        
        @Schema(description = "사용자 ID")
        Long userId,
        
        @Schema(description = "손익 (원)", example = "150000")
        BigDecimal pnl,
        
        @Schema(description = "수익률 (%)", example = "15.25")
        BigDecimal returnPercentage,
        
        @Schema(description = "순위", example = "1")
        Integer rankPosition,
        
        @Schema(description = "계산일시")
        LocalDateTime calculatedAt
) {
    public static LeaderboardResponse from(LeaderboardEntry entry) {
        return new LeaderboardResponse(
                entry.getId(),
                entry.getChallengeId(),
                entry.getSessionId(),
                entry.getUserId(),
                entry.getPnl(),
                entry.getReturnPercentage(),
                entry.getRankPosition(),
                entry.getCalculatedAt()
        );
    }
    
    public static List<LeaderboardResponse> from(List<LeaderboardEntry> entries) {
        return entries.stream()
                .map(LeaderboardResponse::from)
                .toList();
    }
}