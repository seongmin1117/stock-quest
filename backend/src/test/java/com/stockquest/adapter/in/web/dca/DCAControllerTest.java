package com.stockquest.adapter.in.web.dca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD: RED - DCA 컨트롤러 테스트
 * 웹 레이어에서 DCA 시뮬레이션 요청을 받아 애플리케이션 서비스를 호출하는 기능 검증
 */
@WebMvcTest(DCAController.class)
class DCAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DCASimulationService dcaSimulationService;

    @Test
    @WithMockUser
    void DCA_시뮬레이션_요청을_정상적으로_처리해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "AAPL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        DCASimulationResponse response = createMockResponse();
        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.totalInvestmentAmount").value(500000))
            .andExpect(jsonPath("$.finalPortfolioValue").value(650000))
            .andExpect(jsonPath("$.totalReturnPercentage").value(30.00))
            .andExpect(jsonPath("$.investmentRecords").isArray())
            .andExpect(jsonPath("$.investmentRecords.length()").value(5));

        verify(dcaSimulationService).simulate(argThat(command ->
            command.getSymbol().equals("AAPL") &&
            command.getMonthlyInvestmentAmount().equals(new BigDecimal("100000")) &&
            command.getFrequency().equals("MONTHLY")
        ));
    }

    @Test
    @WithMockUser
    void 잘못된_요청_데이터가_전달되면_400_에러가_발생해야_한다() throws Exception {
        // given - 필수 필드 누락
        String invalidRequest = "{}";

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());

        verify(dcaSimulationService, never()).simulate(any());
    }

    @Test
    @WithMockUser
    void 애플리케이션_서비스에서_예외가_발생하면_500_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "INVALID",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenThrow(new RuntimeException("데이터를 찾을 수 없습니다"));

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void 인증되지_않은_사용자는_401_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "AAPL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "MONTHLY"
        );

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 투자_주기_값이_잘못된_경우_400_에러가_발생해야_한다() throws Exception {
        // given
        DCASimulationRequest request = new DCASimulationRequest(
            "AAPL",
            new BigDecimal("100000"),
            "2020-01-01T00:00:00",
            "2020-06-01T00:00:00",
            "INVALID"
        );

        when(dcaSimulationService.simulate(any(DCASimulationCommand.class)))
            .thenThrow(new IllegalArgumentException("지원하지 않는 투자 주기입니다: INVALID"));

        // when & then
        mockMvc.perform(post("/api/v1/dca/simulate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("지원하지 않는 투자 주기입니다: INVALID"));
    }

    private DCASimulationResponse createMockResponse() {
        DCASimulationResponse.MonthlyInvestmentRecordDto record1 =
            new DCASimulationResponse.MonthlyInvestmentRecordDto(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("100"),
                new BigDecimal("1000.00"),
                new BigDecimal("100000")
            );

        DCASimulationResponse.MonthlyInvestmentRecordDto record2 =
            new DCASimulationResponse.MonthlyInvestmentRecordDto(
                LocalDateTime.of(2020, 2, 1, 0, 0),
                new BigDecimal("100000"),
                new BigDecimal("110"),
                new BigDecimal("909.09"),
                new BigDecimal("210000")
            );

        return new DCASimulationResponse(
            "AAPL",
            new BigDecimal("500000"),
            new BigDecimal("650000"),
            new BigDecimal("30.00"),
            new BigDecimal("12.00"),
            Arrays.asList(record1, record2, record1, record1, record1), // 5개 기록
            new BigDecimal("600000"),
            new BigDecimal("650000"),
            new BigDecimal("10.00"),
            new BigDecimal("0.00"),
            new BigDecimal("700000")
        );
    }
}