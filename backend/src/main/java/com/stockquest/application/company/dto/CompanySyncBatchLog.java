package com.stockquest.application.company;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 회사 정보 일괄 동기화 로그
 */
@Getter
@Builder
public class CompanySyncBatchLog {
    private final Long id;
    private final int totalCompanies;
    private final int successCount;
    private final int failureCount;
    private final double successRate;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final long elapsedTimeMillis;
    private final String syncType; // DAILY, WEEKLY, MANUAL
    private final String triggerSource; // SCHEDULER, API, ADMIN

    public static CompanySyncBatchLog fromBatchResult(CompanySyncBatchResult result,
                                                      String syncType,
                                                      String triggerSource) {
        return CompanySyncBatchLog.builder()
                .totalCompanies(result.getTotalCompanies())
                .successCount(result.getSuccessCount())
                .failureCount(result.getFailureCount())
                .successRate(result.getSuccessRate())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .elapsedTimeMillis(result.getElapsedTimeMillis())
                .syncType(syncType)
                .triggerSource(triggerSource)
                .build();
    }
}