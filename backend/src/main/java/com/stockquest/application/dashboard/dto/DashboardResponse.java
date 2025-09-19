package com.stockquest.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 대시보드 응답 DTO
 * 사용자 대시보드에 표시될 모든 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private UserStatsDto userStats;
    private List<RecentSessionDto> recentSessions;
    private DashboardSummaryDto summary;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatsDto {
        private Long totalSessions;
        private Long activeSessions;
        private Long completedSessions;
        private BigDecimal averageReturn;
        private BigDecimal bestReturn;
        private BigDecimal worstReturn;
        private BigDecimal totalReturn;
        private BigDecimal winRate;
        private Long currentRank;
        private Long totalUsers;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentSessionDto {
        private Long id;
        private String challengeTitle;
        private String status;
        private BigDecimal progress;
        private BigDecimal currentBalance;
        private BigDecimal returnRate;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long challengeId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummaryDto {
        private Long totalActiveSessions;
        private Long totalCompletedSessions;
        private BigDecimal averageReturnRate;
        private BestSessionDto bestSession;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BestSessionDto {
        private Long id;
        private String challengeTitle;
        private BigDecimal returnRate;
    }
}