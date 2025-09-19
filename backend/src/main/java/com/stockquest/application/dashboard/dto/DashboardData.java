package com.stockquest.application.dashboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardData(
    UserStats userStats,
    List<RecentSession> recentSessions
) {
}