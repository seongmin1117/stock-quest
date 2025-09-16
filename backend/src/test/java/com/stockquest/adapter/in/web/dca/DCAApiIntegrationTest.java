package com.stockquest.adapter.in.web.dca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DCA API 완전 통합 테스트
 * 실제 데이터베이스와 전체 애플리케이션 컨텍스트를 사용하여
 * DCA 시뮬레이션 API의 전체 플로우를 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/stockquest",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Transactional
class DCAApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Samsung Electronics 실제 데이터로 DCA 시뮬레이션 API가 정상 작동해야 한다")
    void should_simulate_DCA_successfully_with_real_Samsung_data() throws Exception {
        // given - 실제 데이터베이스에 있는 Samsung Electronics 데이터 사용
        DCASimulationRequest request = new DCASimulationRequest(
            "005930", // Samsung Electronics
            new BigDecimal("100000"), // 월 10만원 투자
            "2020-01-01T00:00:00", // 시작일
            "2020-06-01T00:00:00", // 종료일 (5개월)
            "MONTHLY" // 월별 투자
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol").value("005930"))
                .andExpect(jsonPath("$.totalInvestmentAmount").value(500000)) // 10만원 × 5개월
                .andExpect(jsonPath("$.finalPortfolioValue").exists())
                .andExpect(jsonPath("$.totalReturnPercentage").exists())
                .andExpect(jsonPath("$.annualizedReturnPercentage").exists())
                .andExpect(jsonPath("$.investmentRecords").isArray())
                .andExpect(jsonPath("$.investmentRecords.length()").value(5))
                .andExpect(jsonPath("$.sp500Return").exists())
                .andExpect(jsonPath("$.nasdaqReturn").exists())
                // 수학적 오류가 해결되었는지 확인 - NaN/Infinity가 없어야 함
                .andExpect(jsonPath("$.annualizedReturnPercentage").isNumber())
                .andExpect(jsonPath("$.totalReturnPercentage").isNumber())
                .andExpect(jsonPath("$.maxPortfolioValue").exists())
                .andExpect(jsonPath("$.maxDrawdown").exists());
    }

    @Test
    @DisplayName("짧은 투자 기간(3개월)에서도 연평균 수익률이 올바르게 계산되어야 한다")
    void should_calculate_annualized_return_correctly_for_short_period() throws Exception {
        // given - 3개월 투자 (이전에 NaN 오류가 발생했던 케이스)
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-04-01T00:00:00", // 3개월
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("005930"))
                .andExpect(jsonPath("$.totalInvestmentAmount").value(300000)) // 10만원 × 3개월
                .andExpect(jsonPath("$.investmentRecords.length()").value(3))
                // 핵심: 연평균 수익률이 숫자이고 NaN/Infinity가 아니어야 함
                .andExpect(jsonPath("$.annualizedReturnPercentage").isNumber())
                .andExpect(jsonPath("$.totalReturnPercentage").isNumber());
    }

    @Test
    @DisplayName("주별 투자 주기도 정상적으로 작동해야 한다")
    void should_handle_weekly_investment_frequency() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("25000"), // 주당 2.5만원
            "2020-01-01T00:00:00",
            "2020-02-01T00:00:00", // 1개월 (약 4-5주)
            "WEEKLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("005930"))
                .andExpect(jsonPath("$.investmentRecords").isArray())
                .andExpect(jsonPath("$.annualizedReturnPercentage").isNumber())
                .andExpect(jsonPath("$.totalReturnPercentage").isNumber());
    }

    @Test
    @DisplayName("잘못된 심볼로 요청시 적절한 에러 응답을 반환해야 한다")
    void should_return_error_for_invalid_symbol() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "INVALID_SYMBOL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 기간의 주가 데이터를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("잘못된 날짜 순서로 요청시 적절한 에러 응답을 반환해야 한다")
    void should_return_error_for_invalid_date_order() throws Exception {
        // given - 시작일이 종료일보다 늦음
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-06-01T00:00:00", // 종료일
            "2020-01-01T00:00:00", // 시작일
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("시작일은 종료일보다 빨라야 합니다"));
    }

    @Test
    @DisplayName("음수 투자 금액으로 요청시 적절한 에러 응답을 반환해야 한다")
    void should_return_error_for_negative_investment_amount() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("-100000"), // 음수 투자금
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("투자 금액은 0보다 커야 합니다"));
    }

    @Test
    @DisplayName("잘못된 투자 주기로 요청시 적절한 에러 응답을 반환해야 한다")
    void should_return_error_for_invalid_investment_frequency() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "INVALID_FREQUENCY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("DCA 컨트롤러 헬스 체크 엔드포인트가 정상 작동해야 한다")
    void should_return_ok_for_health_check() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/dca/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("DCA Controller is working"));
    }

    @Test
    @DisplayName("복잡한 시나리오 - 장기간 투자에서 벤치마크 비교가 정상 작동해야 한다")
    void should_handle_complex_long_term_investment_scenario() throws Exception {
        // given - 6개월 장기 투자
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("200000"), // 월 20만원 투자
            "2020-01-01T00:00:00",
            "2020-07-01T00:00:00", // 6개월
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("005930"))
                .andExpect(jsonPath("$.totalInvestmentAmount").value(1200000)) // 20만원 × 6개월
                .andExpect(jsonPath("$.finalPortfolioValue").exists())
                .andExpect(jsonPath("$.investmentRecords.length()").value(6))
                // 벤치마크 비교 데이터 검증
                .andExpect(jsonPath("$.sp500Return").isNumber())
                .andExpect(jsonPath("$.nasdaqReturn").isNumber())
                .andExpect(jsonPath("$.outperformanceVsSP500").isNumber())
                .andExpect(jsonPath("$.outperformanceVsNASDAQ").isNumber())
                // 성과 분석 데이터 검증
                .andExpect(jsonPath("$.maxPortfolioValue").isNumber())
                .andExpect(jsonPath("$.maxDrawdown").isNumber())
                // 모든 수익률이 올바른 숫자 형태인지 확인
                .andExpect(jsonPath("$.totalReturnPercentage").isNumber())
                .andExpect(jsonPath("$.annualizedReturnPercentage").isNumber());
    }
}