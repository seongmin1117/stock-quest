package com.stockquest.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.adapter.in.web.session.dto.PlaceOrderRequest;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;
import org.junit.jupiter.api.DisplayName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * API Contract Test
 * OpenAPI 스펙과 실제 구현체 간의 계약 준수를 검증
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API 계약 테스트")
class ApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 API 계약 검증")
    void testSignupApiContract() throws Exception {
        String signupRequest = """
            {
                "email": "contract-test@example.com",
                "password": "password123",
                "nickname": "계약테스트"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("contract-test@example.com")))
                .andExpect(jsonPath("$.nickname", is("계약테스트")))
                .andExpect(jsonPath("$.token", nullValue())); // 회원가입 시에는 토큰 없음
    }

    @Test
    @DisplayName("로그인 API 계약 검증")
    void testLoginApiContract() throws Exception {
        // 먼저 회원가입
        String signupRequest = """
            {
                "email": "login-test@example.com",
                "password": "password123",
                "nickname": "로그인테스트"
            }
            """;
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest));

        // 로그인 테스트
        String loginRequest = """
            {
                "email": "login-test@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("login-test@example.com")))
                .andExpect(jsonPath("$.nickname", is("로그인테스트")))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("주문 API 계약 검증 - PlaceOrderResponse 구조 확인")
    void testPlaceOrderApiContract() throws Exception {
        // Given - 주문 요청 데이터 준비
        PlaceOrderRequest orderRequest = new PlaceOrderRequest(
            "A", // instrumentKey
            OrderSide.BUY,
            new BigDecimal("10"),
            OrderType.MARKET,
            null // limitPrice - MARKET 주문이므로 null
        );

        String requestJson = objectMapper.writeValueAsString(orderRequest);

        // When & Then
        mockMvc.perform(post("/api/sessions/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // PlaceOrderResponse 구조 검증
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.instrumentKey", is("A")))
                .andExpect(jsonPath("$.side", is("BUY")))
                .andExpect(jsonPath("$.quantity", is(10)))
                .andExpect(jsonPath("$.executedPrice", notNullValue()))
                .andExpect(jsonPath("$.slippageRate", notNullValue()))
                .andExpect(jsonPath("$.status", is("EXECUTED")))
                .andExpect(jsonPath("$.executedAt", notNullValue()))
                .andExpect(jsonPath("$.newBalance", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("에러 응답 구조 계약 검증")
    void testErrorResponseContract() throws Exception {
        // 잘못된 회원가입 요청 (이메일 없음)
        String invalidRequest = """
            {
                "password": "password123",
                "nickname": "에러테스트"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // ErrorResponse 구조 검증
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }

    @Test
    @DisplayName("Enum 값 계약 검증")
    void testEnumValuesContract() throws Exception {
        // OrderSide, OrderType, OrderStatus 등의 enum 값이 프론트엔드와 일치하는지 확인
        PlaceOrderRequest buyOrder = new PlaceOrderRequest(
            "A",
            OrderSide.BUY,
            new BigDecimal("5"),
            OrderType.LIMIT,
            new BigDecimal("100.00")
        );

        PlaceOrderRequest sellOrder = new PlaceOrderRequest(
            "B",
            OrderSide.SELL,
            new BigDecimal("3"),
            OrderType.MARKET,
            null
        );

        // BUY 주문 테스트
        String buyRequestJson = objectMapper.writeValueAsString(buyOrder);
        mockMvc.perform(post("/api/sessions/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buyRequestJson)
                .with(mockUser()))
                .andExpect(jsonPath("$.side", is("BUY")))
                .andExpect(jsonPath("$.status", oneOf("PENDING", "EXECUTED", "CANCELLED")));

        // SELL 주문 테스트
        String sellRequestJson = objectMapper.writeValueAsString(sellOrder);
        mockMvc.perform(post("/api/sessions/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sellRequestJson)
                .with(mockUser()))
                .andExpect(jsonPath("$.side", is("SELL")))
                .andExpect(jsonPath("$.status", oneOf("PENDING", "EXECUTED", "CANCELLED")));
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor mockUser() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("1");
    }
}