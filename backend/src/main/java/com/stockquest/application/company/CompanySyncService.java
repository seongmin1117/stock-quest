package com.stockquest.application.company;

import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.port.CompanyRepositoryPort;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.port.ExternalMarketDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 회사 정보 동기화 서비스
 *
 * 외부 마켓 데이터를 가져와서 회사의 시가총액 등을 업데이트합니다.
 * 주기적인 스케줄링과 수동 동기화를 모두 지원합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanySyncService {

    private final CompanyRepositoryPort companyRepository;
    private final ExternalMarketDataClient marketDataClient;

    // 한국 주식시장 주요 종목의 발행주식수 (실제로는 별도 테이블이나 API로 관리해야 함)
    private static final Map<String, Long> OUTSTANDING_SHARES_MAP = Map.of(
        "005930", 5_969_782_550L,  // 삼성전자
        "000660", 728_002_365L,     // SK하이닉스
        "373220", 234_000_000L,     // LG에너지솔루션
        "035720", 445_680_226L,     // 카카오
        "035420", 163_565_594L,     // 네이버
        "005380", 211_668_770L,     // 현대차
        "000270", 402_956_834L      // 기아
    );

    /**
     * 단일 회사의 시가총액을 동기화합니다.
     *
     * @param symbol 회사 심볼
     * @return 동기화 결과
     */
    public CompanySyncResult syncCompany(String symbol) {
        try {
            log.info("Starting sync for company: {}", symbol);

            // 회사 조회
            Optional<Company> companyOpt = companyRepository.findBySymbol(symbol);
            if (companyOpt.isEmpty()) {
                log.error("Company not found for symbol: {}", symbol);
                return CompanySyncResult.failure(symbol, "Company not found: " + symbol);
            }

            Company company = companyOpt.get();

            // 최신 가격 정보 조회
            PriceCandle latestPrice = marketDataClient.fetchLatestPrice(symbol);
            if (latestPrice == null) {
                log.error("No price data available for symbol: {}", symbol);
                return CompanySyncResult.failure(symbol, "No price data available for: " + symbol);
            }

            // 발행주식수 조회 (실제로는 별도 API나 데이터베이스에서 가져와야 함)
            Long outstandingShares = getOutstandingShares(symbol);
            if (outstandingShares == null) {
                log.warn("Outstanding shares not found for symbol: {}, using default", symbol);
                outstandingShares = 1_000_000_000L; // 기본값 10억주
            }

            // 시가총액 계산
            Long previousMarketCap = company.getMarketCap();
            Long updatedMarketCap = latestPrice.getClosePrice()
                    .multiply(BigDecimal.valueOf(outstandingShares))
                    .longValue();

            // 시가총액 업데이트
            companyRepository.updateMarketCap(symbol, updatedMarketCap);

            // 동기화 로그 저장
            CompanySyncResult result = CompanySyncResult.success(symbol, updatedMarketCap, previousMarketCap);
            CompanySyncLog syncLog = CompanySyncLog.fromResult(result, company.getNameKr(), "MANUAL");
            companyRepository.saveSyncLog(syncLog);

            log.info("Successfully synced company: {} - Market cap updated from {} to {}",
                    symbol, previousMarketCap, updatedMarketCap);

            return result;

        } catch (Exception e) {
            log.error("Error syncing company: " + symbol, e);
            return CompanySyncResult.failure(symbol, "Sync failed: " + e.getMessage());
        }
    }

    /**
     * 모든 활성 회사들의 시가총액을 일괄 동기화합니다.
     *
     * @return 일괄 동기화 결과
     */
    public CompanySyncBatchResult syncAllCompanies() {
        LocalDateTime startedAt = LocalDateTime.now();
        log.info("Starting batch sync for all active companies");

        try {
            // 모든 활성 회사 조회
            List<Company> activeCompanies = companyRepository.findAllActive();
            log.info("Found {} active companies to sync", activeCompanies.size());

            if (activeCompanies.isEmpty()) {
                return CompanySyncBatchResult.create(new ArrayList<>(), startedAt, LocalDateTime.now());
            }

            // 심볼 목록 추출
            List<String> symbols = activeCompanies.stream()
                    .map(Company::getSymbol)
                    .collect(Collectors.toList());

            // 최신 가격 일괄 조회
            List<PriceCandle> latestPrices = marketDataClient.fetchLatestPrices(symbols);
            Map<String, PriceCandle> priceMap = latestPrices.stream()
                    .collect(Collectors.toMap(PriceCandle::getSymbol, pc -> pc));

            // 각 회사별로 동기화 수행
            List<CompanySyncResult> syncResults = new ArrayList<>();
            for (Company company : activeCompanies) {
                PriceCandle price = priceMap.get(company.getSymbol());
                CompanySyncResult result;

                if (price != null) {
                    Long outstandingShares = getOutstandingShares(company.getSymbol());
                    if (outstandingShares == null) {
                        outstandingShares = 1_000_000_000L; // 기본값 10억주
                    }

                    Long previousMarketCap = company.getMarketCap();
                    Long updatedMarketCap = price.getClosePrice()
                            .multiply(BigDecimal.valueOf(outstandingShares))
                            .longValue();

                    companyRepository.updateMarketCap(company.getSymbol(), updatedMarketCap);
                    result = CompanySyncResult.success(company.getSymbol(), updatedMarketCap, previousMarketCap);

                    // 개별 동기화 로그 저장
                    CompanySyncLog syncLog = CompanySyncLog.fromResult(result, company.getNameKr(), "BATCH");
                    companyRepository.saveSyncLog(syncLog);
                } else {
                    result = CompanySyncResult.failure(company.getSymbol(), "No price data available");
                }

                syncResults.add(result);
            }

            LocalDateTime completedAt = LocalDateTime.now();
            CompanySyncBatchResult batchResult = CompanySyncBatchResult.create(syncResults, startedAt, completedAt);

            // 배치 로그 저장
            CompanySyncBatchLog batchLog = CompanySyncBatchLog.fromBatchResult(batchResult, "MANUAL", "API");
            companyRepository.saveSyncBatchLog(batchLog);

            log.info("Batch sync completed: {} success, {} failures out of {} companies",
                    batchResult.getSuccessCount(), batchResult.getFailureCount(), batchResult.getTotalCompanies());

            return batchResult;

        } catch (Exception e) {
            log.error("Error during batch sync", e);
            return CompanySyncBatchResult.create(new ArrayList<>(), startedAt, LocalDateTime.now());
        }
    }

    /**
     * 스케줄된 동기화 작업을 실행합니다.
     * 매일 오전 9시와 오후 3시 30분에 실행됩니다.
     */
    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시
    @Scheduled(cron = "0 30 15 * * ?") // 매일 오후 3시 30분
    public void runScheduledSync() {
        log.info("Running scheduled company sync");

        LocalDateTime startedAt = LocalDateTime.now();
        CompanySyncBatchResult result = syncAllCompanies();

        // 스케줄 배치 로그 저장
        CompanySyncBatchLog batchLog = CompanySyncBatchLog.fromBatchResult(result, "DAILY", "SCHEDULER");
        companyRepository.saveSyncBatchLog(batchLog);

        log.info("Scheduled sync completed: success rate {}%", result.getSuccessRate());
    }

    /**
     * 발행주식수를 조회합니다.
     * 실제 구현에서는 별도의 API나 데이터베이스에서 가져와야 합니다.
     *
     * @param symbol 회사 심볼
     * @return 발행주식수
     */
    private Long getOutstandingShares(String symbol) {
        return OUTSTANDING_SHARES_MAP.get(symbol);
    }
}