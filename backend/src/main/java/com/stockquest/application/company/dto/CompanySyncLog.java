package com.stockquest.application.company;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 회사 정보 동기화 로그
 */
@Getter
@Builder
public class CompanySyncLog {
    private final Long id;
    private final String symbol;
    private final String companyName;
    private final boolean success;
    private final Long previousMarketCap;
    private final Long updatedMarketCap;
    private final Double changePercentage;
    private final String errorMessage;
    private final LocalDateTime syncedAt;
    private final String syncType; // MANUAL, SCHEDULED, API_TRIGGERED

    public static CompanySyncLog fromResult(CompanySyncResult result, String companyName, String syncType) {
        return CompanySyncLog.builder()
                .symbol(result.getSymbol())
                .companyName(companyName)
                .success(result.isSuccess())
                .previousMarketCap(result.getPreviousMarketCap())
                .updatedMarketCap(result.getUpdatedMarketCap())
                .changePercentage(result.getChangePercentage())
                .errorMessage(result.getErrorMessage())
                .syncedAt(result.getSyncedAt())
                .syncType(syncType)
                .build();
    }
}