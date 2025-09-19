package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ArticleStatsResponse(
    List<PopularArticle> topArticles,
    List<CategoryStats> categoryStats,
    LocalDateTime lastUpdated
) {
}

@Builder
record PopularArticle(
    Long id,
    String title,
    String slug,
    Long views,
    String categoryName
) {}

@Builder
record CategoryStats(
    String categoryName,
    Long articleCount,
    Long totalViews
) {}