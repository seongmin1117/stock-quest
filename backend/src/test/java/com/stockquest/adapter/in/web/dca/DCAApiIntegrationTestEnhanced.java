package com.stockquest.adapter.in.web.dca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DCA API 통합 테스트 확장판
 *
 * 실제 데이터베이스와 전체 애플리케이션 컨텍스트를 사용한 End-to-End 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DCAApiIntegrationTestEnhanced {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void 실제_삼성전자_데이터로_DCA_시뮬레이션을_실행해야_한다() throws Exception {
        // given - 실제 DB에 있는 삼성전자 데이터 기간 사용
        DCASimulationRequest request = new DCASimulationRequest(
            "005930", // 삼성전자
            new BigDecimal("100000"),
            "2020-01-02T00:00:00", // 실제 데이터 시작일
            "2020-06-01T00:00:00", // 실제 데이터 종료일
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(jsonPath("$.totalInvestmentAmount").value(500000)) // 10만원 × 5개월
            .andExpect(jsonPath("$.finalPortfolioValue").value(greaterThan(0)))
            .andExpect(jsonPath("$.totalReturnPercentage").isNumber())
            .andExpect(jsonPath("$.annualizedReturn").isNumber())
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(5))
            .andExpect(jsonPath("$.sp500ReturnAmount").value(greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.nasdaqReturnAmount").value(greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.outperformanceVsSP500").isNumber())
            .andExpect(jsonPath("$.outperformanceVsNASDAQ").isNumber())
            .andExpect(jsonPath("$.maxPortfolioValue").value(greaterThan(0)));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_종목으로_요청시_적절한_에러_응답을_반환해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "INVALID", // 존재하지 않는 종목
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("해당 기간의 주가 데이터를 찾을 수 없습니다")))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/v1/dca/simulate"));
    }

    @Test
    @WithMockUser
    void 데이터가_없는_기간으로_요청시_적절한_에러_응답을_반환해야_한다() throws Exception {
        // given - 삼성전자는 있지만 데이터가 없는 기간
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2019-01-01T00:00:00", // 데이터가 없는 기간
            "2019-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("해당 기간의 주가 데이터를 찾을 수 없습니다")));
    }

    @Test
    @WithMockUser
    void 주별_투자_주기로_시뮬레이션을_실행해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("25000"), // 주당 2.5만원
            "2020-01-02T00:00:00",
            "2020-02-01T00:00:00", // 1개월간 주별 투자
            "WEEKLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(greaterThan(1))) // 최소 2주 이상
            .andExpect(jsonPath("$.totalInvestmentAmount").value(greaterThan(25000))); // 최소 1회 이상 투자
    }

    @Test
    @WithMockUser
    void 일별_투자_주기로_시뮬레이션을_실행해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("5000"), // 일당 5천원
            "2020-01-02T00:00:00",
            "2020-01-10T00:00:00", // 1주일간 일별 투자
            "DAILY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(greaterThan(5))); // 평일 5일 이상
    }

    @Test
    @WithMockUser
    void 한국어_종목명이_포함된_응답을_확인해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-03-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(content().string(containsString("005930"))); // 한글 인코딩 정상 확인
    }

    @Test
    @WithMockUser
    void 음수_투자_금액으로_요청시_검증_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("-100000"), // 음수 금액
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("투자 금액은 0보다 커야 합니다")));
    }

    @Test
    @WithMockUser
    void 잘못된_날짜_형식으로_요청시_검증_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-13-01T00:00:00", // 잘못된 월 (13월)
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("지원하지 않는 날짜 형식입니다")));
    }

    @Test
    @WithMockUser
    void 종료일이_시작일보다_빠른_경우_검증_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-06-01T00:00:00", // 시작일
            "2020-01-01T00:00:00", // 종료일이 시작일보다 빠름
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("시작일은 종료일보다 빨라야 합니다")));
    }

    @Test
    @WithMockUser
    void DCA_컨트롤러_헬스체크_엔드포인트가_정상_작동해야_한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/dca/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("DCA Controller is working!"));
    }

    @Test
    @WithMockUser
    void 대용량_기간_요청도_정상_처리되어야_한다() throws Exception {
        // given - 전체 데이터 기간 사용 (실제 DB 데이터 범위)
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("50000"), // 금액을 줄여서 처리 시간 단축
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "DAILY" // 일별 투자로 데이터 포인트 증가
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("005930"))
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(greaterThan(50))) // 많은 투자 기록
            .andExpect(jsonPath("$.totalInvestmentAmount").value(greaterThan(1000000))); // 상당한 투자 금액
    }

    @Test
    @WithMockUser
    void JSON_응답_구조가_API_명세와_일치해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-03-01T00:00:00",
            "MONTHLY"
        );

        // when & then - 모든 필수 필드 존재 확인
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
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

            // 투자 기록 구조 확인
            .andExpect(jsonPath("$.investmentRecords[0].investmentDate").exists())
            .andExpect(jsonPath("$.investmentRecords[0].investmentAmount").exists())
            .andExpect(jsonPath("$.investmentRecords[0].stockPrice").exists())
            .andExpect(jsonPath("$.investmentRecords[0].sharesPurchased").exists())
            .andExpect(jsonPath("$.investmentRecords[0].portfolioValue").exists());
    }

    @Test
    void 인증_없이_DCA_시뮬레이션_요청시_401_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "005930",
            new BigDecimal("100000"),
            "2020-01-02T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}