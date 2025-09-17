package com.stockquest.application.company;

import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.port.CompanyRepositoryPort;
import com.stockquest.domain.market.CandleTimeframe;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.market.port.ExternalMarketDataClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanySyncService 테스트")
class CompanySyncServiceTest {

    @Mock
    private CompanyRepositoryPort companyRepository;

    @Mock
    private ExternalMarketDataClient marketDataClient;

    @InjectMocks
    private CompanySyncService companySyncService;

    private Company samsungElectronics;
    private Company skHynix;
    private PriceCandle samsungLatestPrice;
    private PriceCandle skLatestPrice;

    @BeforeEach
    void setUp() {
        // 테스트용 회사 데이터 설정
        samsungElectronics = Company.builder()
                .id(1L)
                .symbol("005930")
                .nameKr("삼성전자")
                .nameEn("Samsung Electronics")
                .sector("반도체")
                .marketCap(360_000_000_000_000L)
                .isActive(true)
                .build();

        skHynix = Company.builder()
                .id(2L)
                .symbol("000660")
                .nameKr("SK하이닉스")
                .nameEn("SK Hynix")
                .sector("반도체")
                .marketCap(70_000_000_000_000L)
                .isActive(true)
                .build();

        // 최신 가격 데이터 설정 (생성자 사용)
        samsungLatestPrice = new PriceCandle(
                "005930",                      // ticker
                LocalDate.now(),               // date
                BigDecimal.valueOf(75000),     // openPrice
                BigDecimal.valueOf(76000),     // highPrice
                BigDecimal.valueOf(74000),     // lowPrice
                BigDecimal.valueOf(75000),     // closePrice
                15_000_000L,                   // volume
                CandleTimeframe.DAILY          // timeframe
        );

        skLatestPrice = new PriceCandle(
                "000660",                      // ticker
                LocalDate.now(),               // date
                BigDecimal.valueOf(120000),    // openPrice
                BigDecimal.valueOf(122000),    // highPrice
                BigDecimal.valueOf(119000),    // lowPrice
                BigDecimal.valueOf(120000),    // closePrice
                5_000_000L,                    // volume
                CandleTimeframe.DAILY          // timeframe
        );
    }

    @Test
    @DisplayName("단일 회사의 시가총액을 최신 주가 기준으로 업데이트한다")
    void syncSingleCompanyMarketCap() {
        // given
        when(companyRepository.findBySymbol("005930")).thenReturn(Optional.of(samsungElectronics));
        when(marketDataClient.fetchLatestPrice("005930")).thenReturn(samsungLatestPrice);

        // 삼성전자 발행주식수를 가정 (약 59.7억주)
        long outstandingShares = 5_969_782_550L;
        long expectedMarketCap = samsungLatestPrice.getClosePrice()
                .multiply(BigDecimal.valueOf(outstandingShares))
                .longValue();

        // when
        CompanySyncResult result = companySyncService.syncCompany("005930");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSymbol()).isEqualTo("005930");
        assertThat(result.getUpdatedMarketCap()).isEqualTo(expectedMarketCap);
        assertThat(result.getSyncedAt()).isNotNull();

        verify(companyRepository).updateMarketCap(eq("005930"), eq(expectedMarketCap));
    }

    @Test
    @DisplayName("모든 활성 회사들의 시가총액을 일괄 업데이트한다")
    void syncAllActiveCompanies() {
        // given
        List<Company> activeCompanies = Arrays.asList(samsungElectronics, skHynix);
        List<String> symbols = Arrays.asList("005930", "000660");
        List<PriceCandle> latestPrices = Arrays.asList(samsungLatestPrice, skLatestPrice);

        when(companyRepository.findAllActive()).thenReturn(activeCompanies);
        when(marketDataClient.fetchLatestPrices(symbols)).thenReturn(latestPrices);

        // when
        CompanySyncBatchResult batchResult = companySyncService.syncAllCompanies();

        // then
        assertThat(batchResult).isNotNull();
        assertThat(batchResult.getTotalCompanies()).isEqualTo(2);
        assertThat(batchResult.getSuccessCount()).isEqualTo(2);
        assertThat(batchResult.getFailureCount()).isEqualTo(0);
        assertThat(batchResult.getSyncResults()).hasSize(2);

        verify(companyRepository, times(2)).updateMarketCap(anyString(), anyLong());
    }

    @Test
    @DisplayName("외부 API 호출 실패 시 적절한 에러 처리를 한다")
    void handleExternalApiFailure() {
        // given
        when(companyRepository.findBySymbol("005930")).thenReturn(Optional.of(samsungElectronics));
        when(marketDataClient.fetchLatestPrice("005930"))
                .thenThrow(new RuntimeException("External API error"));

        // when
        CompanySyncResult result = companySyncService.syncCompany("005930");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("External API error");

        verify(companyRepository, never()).updateMarketCap(anyString(), anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 회사 심볼로 동기화 시도 시 에러를 반환한다")
    void syncNonExistentCompany() {
        // given
        when(companyRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // when
        CompanySyncResult result = companySyncService.syncCompany("INVALID");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Company not found");

        verify(marketDataClient, never()).fetchLatestPrice(anyString());
        verify(companyRepository, never()).updateMarketCap(anyString(), anyLong());
    }

    @Test
    @DisplayName("주가 데이터가 없는 경우 시가총액 업데이트를 건너뛴다")
    void skipUpdateWhenNoPriceData() {
        // given
        when(companyRepository.findBySymbol("005930")).thenReturn(Optional.of(samsungElectronics));
        when(marketDataClient.fetchLatestPrice("005930")).thenReturn(null);

        // when
        CompanySyncResult result = companySyncService.syncCompany("005930");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("No price data available");

        verify(companyRepository, never()).updateMarketCap(anyString(), anyLong());
    }

    @Test
    @DisplayName("동기화 로그를 저장한다")
    void saveSyncLog() {
        // given
        when(companyRepository.findBySymbol("005930")).thenReturn(Optional.of(samsungElectronics));
        when(marketDataClient.fetchLatestPrice("005930")).thenReturn(samsungLatestPrice);

        // when
        CompanySyncResult result = companySyncService.syncCompany("005930");

        // then
        verify(companyRepository).saveSyncLog(any(CompanySyncLog.class));
    }

    @Test
    @DisplayName("일별 동기화 스케줄을 실행한다")
    void runDailySync() {
        // given
        List<Company> activeCompanies = Arrays.asList(samsungElectronics, skHynix);
        when(companyRepository.findAllActive()).thenReturn(activeCompanies);
        when(marketDataClient.fetchLatestPrices(anyList()))
                .thenReturn(Arrays.asList(samsungLatestPrice, skLatestPrice));

        // when
        companySyncService.runScheduledSync();

        // then
        verify(companyRepository, atLeast(2)).updateMarketCap(anyString(), anyLong());
        verify(companyRepository).saveSyncBatchLog(any(CompanySyncBatchLog.class));
    }
}