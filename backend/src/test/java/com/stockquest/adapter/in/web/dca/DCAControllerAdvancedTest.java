package com.stockquest.adapter.in.web.dca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.lessThan;

/**
 * DCA 컨트롤러 고급 테스트 케이스
 *
 * 추가된 테스트 시나리오:
 * 1. 다양한 투자 주기별 테스트 (일별, 주별, 월별)
 * 2. 극단적인 투자 금액 테스트 (최소/최대값)
 * 3. 다양한 기간 시나리오 테스트
 * 4. 한국 주요 종목 코드 테스트
 * 5. 성능 지표 검증
 * 6. 벤치마크 비교 검증
 */
@WebMvcTest(DCAController.class)
class DCAControllerAdvancedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DCASimulationService dcaSimulationService;

    @ParameterizedTest
    @ValueSource(strings = {"DAILY", "WEEKLY", "MONTHLY"})
    @WithMockUser
    void 모든_투자_주기에_대해_시뮬레이션이_정상_동작해야_한다(String frequency) throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930", // 삼성전자
            new BigDecimal("50000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            frequency
        );

        DCASimulationResponse response = createResponseForFrequency(frequency);
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(jsonPath("$.totalInvestmentAmount").isNumber())
            .andExpect(jsonPath("$.finalPortfolioValue").isNumber())
            .andExpect(jsonPath("$.totalReturnPercentage").isNumber())
            .andExpect(jsonPath("$.investmentRecords").isArray());
    }

    @ParameterizedTest
    @ValueSource(strings = {"005930", "000660", "035720", "005380"})
    @WithMockUser
    void 한국_주요_종목에_대해_시뮬레이션이_정상_동작해야_한다(String symbol) throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            symbol,
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createResponseForSymbol(symbol);
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value(symbol));
    }

    @Test
    @WithMockUser
    void 최소_투자_금액으로_시뮬레이션이_정상_동작해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("1000"), // 최소 1000원
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createMinimalInvestmentResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalInvestmentAmount").value(5000)) // 5개월 * 1000원
            .andExpect(jsonPath("$.finalPortfolioValue").isNumber());
    }

    @Test
    @WithMockUser
    void 대용량_투자_금액으로_시뮬레이션이_정상_동작해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("10000000"), // 천만원
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createLargeInvestmentResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalInvestmentAmount").value(50000000))
            .andExpect(jsonPath("$.finalPortfolioValue").isNumber());
    }

    @Test
    @WithMockUser
    void 단기_투자_기간으로_시뮬레이션이_정상_동작해야_한다() throws Exception {
        // given - 1개월 단기 투자
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-02-01T00:00:00",
            "WEEKLY"
        );

        DCASimulationResponse response = createShortTermResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(4)); // 4주간
    }

    @Test
    @WithMockUser
    void 장기_투자_기간으로_시뮬레이션이_정상_동작해야_한다() throws Exception {
        // given - 1년 장기 투자
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2021-01-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createLongTermResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(12)) // 12개월
            .andExpect(jsonPath("$.annualizedReturn").isNumber());
    }

    @Test
    @WithMockUser
    void 수익률이_음수인_경우_시뮬레이션이_정상_동작해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createNegativeReturnResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalReturnPercentage").value(lessThan(0.0)))
            .andExpect(jsonPath("$.finalPortfolioValue").value(lessThan(500000))); // 투자 원금보다 적음
    }

    @Test
    @WithMockUser
    void 벤치마크_비교_지표가_올바르게_계산되어야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createBenchmarkComparisonResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sp500ReturnAmount").isNumber())
            .andExpect(jsonPath("$.nasdaqReturnAmount").isNumber())
            .andExpect(jsonPath("$.outperformanceVsSP500").isNumber())
            .andExpect(jsonPath("$.outperformanceVsNASDAQ").isNumber());
    }

    @Test
    @WithMockUser
    void 응답_JSON_구조가_API_명세와_정확히_일치해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createCompleteResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // 필수 필드 검증
            .andExpect(jsonPath("$.symbol").exists())
            .andExpect(jsonPath("$.totalInvestmentAmount").exists())
            .andExpect(jsonPath("$.finalPortfolioValue").exists())
            .andExpect(jsonPath("$.totalReturnPercentage").exists())
            .andExpect(jsonPath("$.annualizedReturn").exists())
            .andExpect(jsonPath("$.investmentRecords").exists())
            .andExpect(jsonPath("$.sp500ReturnAmount").exists())
            .andExpect(jsonPath("$.nasdaqReturnAmount").exists())
            .andExpect(jsonPath("$.outperformanceVsSP500").exists())
            .andExpect(jsonPath("$.outperformanceVsNASDAQ").exists())
            .andExpect(jsonPath("$.maxPortfolioValue").exists())
            // 투자 기록 구조 검증
            .andExpect(jsonPath("$.investmentRecords[0].investmentDate").exists())
            .andExpect(jsonPath("$.investmentRecords[0].investmentAmount").exists())
            .andExpect(jsonPath("$.investmentRecords[0].stockPrice").exists())
            .andExpect(jsonPath("$.investmentRecords[0].sharesPurchased").exists())
            .andExpect(jsonPath("$.investmentRecords[0].portfolioValue").exists());
    }

    // Helper methods for creating mock responses
    private DCASimulationResponse createResponseForFrequency(String frequency) {
        int recordCount = switch (frequency) {
            case "DAILY" -> 100; // 약 100일간
            case "WEEKLY" -> 20;  // 약 20주간
            case "MONTHLY" -> 5;  // 5개월간
            default -> 5;
        };

        return createMockResponse("005930", new BigDecimal("50000"), recordCount);
    }

    private DCASimulationResponse createResponseForSymbol(String symbol) {
        return createMockResponse(symbol, new BigDecimal("100000"), 5);
    }

    private DCASimulationResponse createMinimalInvestmentResponse() {
        return createMockResponse("005930", new BigDecimal("1000"), 5);
    }

    private DCASimulationResponse createLargeInvestmentResponse() {
        return createMockResponse("005930", new BigDecimal("10000000"), 5);
    }

    private DCASimulationResponse createShortTermResponse() {
        return createMockResponse("005930", new BigDecimal("100000"), 4);
    }

    private DCASimulationResponse createLongTermResponse() {
        return createMockResponse("005930", new BigDecimal("100000"), 12);
    }

    private DCASimulationResponse createNegativeReturnResponse() {
        List<DCASimulationResponse.MonthlyInvestmentRecordDto> records = Arrays.asList(
            new DCASimulationResponse.MonthlyInvestmentRecordDto(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("100000")
            ),
            new DCASimulationResponse.MonthlyInvestmentRecordDto(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("80"), // 주가 하락
                new BigDecimal("1250.00"),
                new BigDecimal("180000") // 포트폴리오 가치 하락
            )
        );

        return new DCASimulationResponse(
            "005930",
            new BigDecimal("500000"), // 총 투자 금액
            new BigDecimal("400000"), // 손실 발생
            new BigDecimal("-20.00"), // 음수 수익률
            new BigDecimal("-24.00"), // 음수 연환산 수익률
            records,
            new BigDecimal("450000"), // S&P 500도 하락
            new BigDecimal("420000"), // NASDAQ도 하락
            new BigDecimal("-11.11"), // S&P 500 대비 상대적 언더퍼폼
            new BigDecimal("-4.76"),  // NASDAQ 대비 상대적 언더퍼폼
            new BigDecimal("500000")  // 최고 포트폴리오 가치는 초기값
        );
    }

    private DCASimulationResponse createBenchmarkComparisonResponse() {
        return createMockResponse("005930", new BigDecimal("100000"), 5);
    }

    private DCASimulationResponse createCompleteResponse() {
        return createMockResponse("005930", new BigDecimal("100000"), 5);
    }

    private DCASimulationResponse createMockResponse(String symbol, BigDecimal monthlyAmount, int recordCount) {
        List<DCASimulationResponse.MonthlyInvestmentRecordDto> records =
            java.util.stream.IntStream.range(0, recordCount)
                .mapToObj(i -> new DCASimulationResponse.MonthlyInvestmentRecordDto(
                    LocalDateTime.of(2020, 1 + i, 1, 0, 0),
                    monthlyAmount,
                    new BigDecimal("100").add(new BigDecimal(i * 5)), // 가격 상승
                    new BigDecimal("1000.00"),
                    monthlyAmount.multiply(new BigDecimal(i + 1))
                ))
                .toList();

        BigDecimal totalInvestment = monthlyAmount.multiply(new BigDecimal(recordCount));
        BigDecimal finalValue = totalInvestment.multiply(new BigDecimal("1.3")); // 30% 수익

        return new DCASimulationResponse(
            symbol,
            totalInvestment,
            finalValue,
            new BigDecimal("30.00"),
            new BigDecimal("36.00"),
            records,
            totalInvestment.multiply(new BigDecimal("1.25")), // S&P 500: 25% 수익
            totalInvestment.multiply(new BigDecimal("1.28")), // NASDAQ: 28% 수익
            new BigDecimal("4.00"), // S&P 500 대비 4% 초과수익
            new BigDecimal("1.43"), // NASDAQ 대비 1.43% 초과수익
            finalValue.multiply(new BigDecimal("1.1")) // 최고값은 10% 더 높았음
        );
    }
}