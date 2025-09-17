package com.stockquest.application.company;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 회사 정보 동기화 결과
 */
@Getter
@Builder
public class CompanySyncResult {
    private final String symbol;
    private final boolean success;
    private final Long updatedMarketCap;
    private final String errorMessage;
    private final LocalDateTime syncedAt;
    private final Long previousMarketCap;
    private final Double changePercentage;

    public static CompanySyncResult success(String symbol, Long updatedMarketCap, Long previousMarketCap) {
        double changePercentage = previousMarketCap != null && previousMarketCap > 0
            ? ((double)(updatedMarketCap - previousMarketCap) / previousMarketCap) * 100
            : 0.0;

        return CompanySyncResult.builder()
                .symbol(symbol)
                .success(true)
                .updatedMarketCap(updatedMarketCap)
                .previousMarketCap(previousMarketCap)
                .changePercentage(changePercentage)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    public static CompanySyncResult failure(String symbol, String errorMessage) {
        return CompanySyncResult.builder()
                .symbol(symbol)
                .success(false)
                .errorMessage(errorMessage)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    public boolean isSuccess() {
        return success;
    }
}