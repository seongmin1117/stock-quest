package com.stockquest.adapter.in.web.company;

import com.stockquest.application.company.CompanySyncBatchResult;
import com.stockquest.application.company.CompanySyncResult;
import com.stockquest.application.company.CompanySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;

/**
 * Company Sync Web Adapter - 회사 정보 동기화 API
 *
 * 관리자용 회사 정보 동기화 엔드포인트를 제공합니다.
 * 실시간 시장 데이터를 가져와서 회사 정보를 업데이트합니다.
 */
@RestController
@RequestMapping("/api/v1/companies/sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Sync", description = "Company data synchronization APIs (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class CompanySyncWebAdapter {

    private final CompanySyncService companySyncService;

    @Operation(summary = "Sync single company",
              description = "Synchronize market data for a single company (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - Admin only"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PostMapping("/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanySyncResult> syncCompany(
            @Parameter(description = "Stock symbol to sync", example = "005930")
            @PathVariable @NotBlank String symbol) {

        log.info("POST /api/v1/companies/sync/{} - Admin initiated single company sync", symbol);

        CompanySyncResult result = companySyncService.syncCompany(symbol);

        if (result.isSuccess()) {
            log.info("Successfully synced company {}: Market cap updated to {}",
                    symbol, result.getUpdatedMarketCap());
            return ResponseEntity.ok(result);
        } else {
            log.error("Failed to sync company {}: {}", symbol, result.getErrorMessage());
            if (result.getErrorMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result); // Still return 200 with error details
        }
    }

    @Operation(summary = "Sync all companies",
              description = "Synchronize market data for all active companies (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch sync completed"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - Admin only")
    })
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanySyncBatchResult> syncAllCompanies() {

        log.info("POST /api/v1/companies/sync/all - Admin initiated batch company sync");

        CompanySyncBatchResult result = companySyncService.syncAllCompanies();

        log.info("Batch sync completed: {} success, {} failures out of {} companies",
                result.getSuccessCount(), result.getFailureCount(), result.getTotalCompanies());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Trigger scheduled sync manually",
              description = "Manually trigger the scheduled synchronization (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scheduled sync triggered"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - Admin only")
    })
    @PostMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerScheduledSync() {

        log.info("POST /api/v1/companies/sync/scheduled - Admin manually triggered scheduled sync");

        // 스케줄된 동기화를 수동으로 실행
        companySyncService.runScheduledSync();

        return ResponseEntity.ok("Scheduled sync has been triggered successfully");
    }

    /**
     * 동기화 상태 조회 (향후 구현 예정)
     */
    @Operation(summary = "Get sync status",
              description = "Get the current synchronization status and history (Admin only)")
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncStatusResponse> getSyncStatus() {
        log.info("GET /api/v1/companies/sync/status - Admin checking sync status");

        // TODO: 실제 동기화 로그를 데이터베이스에서 조회하여 반환
        SyncStatusResponse response = SyncStatusResponse.builder()
                .lastSyncTime("2024-01-15 09:00:00")
                .nextScheduledSync("2024-01-15 15:30:00")
                .totalCompanies(20)
                .lastSyncSuccessCount(19)
                .lastSyncFailureCount(1)
                .isCurrentlyRunning(false)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 동기화 상태 응답 DTO
     */
    record SyncStatusResponse(
            String lastSyncTime,
            String nextScheduledSync,
            int totalCompanies,
            int lastSyncSuccessCount,
            int lastSyncFailureCount,
            boolean isCurrentlyRunning
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String lastSyncTime;
            private String nextScheduledSync;
            private int totalCompanies;
            private int lastSyncSuccessCount;
            private int lastSyncFailureCount;
            private boolean isCurrentlyRunning;

            public Builder lastSyncTime(String lastSyncTime) {
                this.lastSyncTime = lastSyncTime;
                return this;
            }

            public Builder nextScheduledSync(String nextScheduledSync) {
                this.nextScheduledSync = nextScheduledSync;
                return this;
            }

            public Builder totalCompanies(int totalCompanies) {
                this.totalCompanies = totalCompanies;
                return this;
            }

            public Builder lastSyncSuccessCount(int lastSyncSuccessCount) {
                this.lastSyncSuccessCount = lastSyncSuccessCount;
                return this;
            }

            public Builder lastSyncFailureCount(int lastSyncFailureCount) {
                this.lastSyncFailureCount = lastSyncFailureCount;
                return this;
            }

            public Builder isCurrentlyRunning(boolean isCurrentlyRunning) {
                this.isCurrentlyRunning = isCurrentlyRunning;
                return this;
            }

            public SyncStatusResponse build() {
                return new SyncStatusResponse(
                    lastSyncTime,
                    nextScheduledSync,
                    totalCompanies,
                    lastSyncSuccessCount,
                    lastSyncFailureCount,
                    isCurrentlyRunning
                );
            }
        }
    }
}