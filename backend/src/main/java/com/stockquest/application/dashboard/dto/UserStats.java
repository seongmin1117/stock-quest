package com.stockquest.application.dashboard.dto;

import lombok.Builder;

@Builder
public record UserStats(
    Integer totalSessions,
    Integer activeSessions,
    Integer completedSessions,
    Double averageReturn,
    Double bestReturn,
    Double worstReturn,
    Double totalReturn,
    Double winRate
) {
}