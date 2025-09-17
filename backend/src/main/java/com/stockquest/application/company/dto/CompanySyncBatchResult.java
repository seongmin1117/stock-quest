package com.stockquest.application.company;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회사 정보 일괄 동기화 결과
 */
@Getter
@Builder
public class CompanySyncBatchResult {
    private final int totalCompanies;
    private final int successCount;
    private final int failureCount;
    private final List<CompanySyncResult> syncResults;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final long elapsedTimeMillis;

    public static CompanySyncBatchResult create(List<CompanySyncResult> syncResults,
                                                LocalDateTime startedAt,
                                                LocalDateTime completedAt) {
        int successCount = (int) syncResults.stream()
                .filter(CompanySyncResult::isSuccess)
                .count();

        int failureCount = syncResults.size() - successCount;

        long elapsedTimeMillis = java.time.Duration.between(startedAt, completedAt).toMillis();

        return CompanySyncBatchResult.builder()
                .totalCompanies(syncResults.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .syncResults(syncResults)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .elapsedTimeMillis(elapsedTimeMillis)
                .build();
    }

    public double getSuccessRate() {
        return totalCompanies > 0 ? (double) successCount / totalCompanies * 100 : 0;
    }

    public boolean isFullSuccess() {
        return failureCount == 0;
    }
}