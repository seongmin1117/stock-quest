package com.stockquest.application.dashboard.dto;

import lombok.Builder;

@Builder
public record RecentSession(
    Integer id,
    Integer challengeId,
    String challengeTitle,
    String status,
    Integer progress,
    Double currentBalance,
    Double returnRate,
    String startedAt,
    String completedAt
) {
}