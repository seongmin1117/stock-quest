package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ArticleAnalyticsResponse(
    Long articleId,
    String title,
    Long totalViews,
    Long weeklyViews,
    Long monthlyViews,
    List<DailyViewStats> dailyStats,
    LocalDateTime lastViewed
) {
}