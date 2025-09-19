package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

@Builder
public record AdminDashboardResponse(
    Long totalArticles,
    Long publishedArticles,
    Long draftArticles,
    Long totalCategories,
    Long totalTags,
    Long totalViews,
    Long todayViews,
    ArticleStatsResponse recentStats
) {
}