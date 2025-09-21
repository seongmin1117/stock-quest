package com.stockquest.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Order 도메인 엔티티 테스트
 * 주문 실행, 슬리피지 계산, 상태 관리 등 핵심 비즈니스 로직 검증
 */
@DisplayName("Order 도메인 테스트")
class OrderTest {

    // 테스트 데이터 팩토리
    private static final Long VALID_SESSION_ID = 1L;
    private static final String VALID_INSTRUMENT_KEY = "A";
    private static final BigDecimal VALID_QUANTITY = new BigDecimal("100");
    private static final BigDecimal VALID_PRICE = new BigDecimal("50000");

    private Order createValidMarketOrder() {
        return new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                        VALID_QUANTITY, OrderType.MARKET, null);
    }

    private Order createValidLimitOrder() {
        return new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                        VALID_QUANTITY, OrderType.LIMIT, VALID_PRICE);
    }

    @Nested
    @DisplayName("주문 생성 검증")
    class OrderCreationTest {

        @Test
        @DisplayName("유효한 시장가 주문 생성")
        void createValidMarketOrder() {
            // given & when
            Order order = createValidMarketOrder();

            // then
            assertThat(order.getSessionId()).isEqualTo(VALID_SESSION_ID);
            assertThat(order.getInstrumentKey()).isEqualTo(VALID_INSTRUMENT_KEY);
            assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
            assertThat(order.getQuantity()).isEqualTo(VALID_QUANTITY);
            assertThat(order.getOrderType()).isEqualTo(OrderType.MARKET);
            assertThat(order.getLimitPrice()).isNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getOrderedAt()).isNotNull();
            assertThat(order.getSlippageRate()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("유효한 지정가 주문 생성")
        void createValidLimitOrder() {
            // given & when
            Order order = createValidLimitOrder();

            // then
            assertThat(order.getSessionId()).isEqualTo(VALID_SESSION_ID);
            assertThat(order.getInstrumentKey()).isEqualTo(VALID_INSTRUMENT_KEY);
            assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
            assertThat(order.getQuantity()).isEqualTo(VALID_QUANTITY);
            assertThat(order.getOrderType()).isEqualTo(OrderType.LIMIT);
            assertThat(order.getLimitPrice()).isEqualTo(VALID_PRICE);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("유효하지 않은 세션 ID로 주문 생성 실패")
        void createOrderWithInvalidSessionId(Long invalidSessionId) {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(invalidSessionId, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.MARKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 세션 ID가 필요합니다");
        }

        @Test
        @DisplayName("null 세션 ID로 주문 생성 실패")
        void createOrderWithNullSessionId() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(null, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.MARKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 세션 ID가 필요합니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("유효하지 않은 상품 키로 주문 생성 실패")
        void createOrderWithInvalidInstrumentKey(String invalidKey) {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, invalidKey, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.MARKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 키는 필수입니다");
        }

        @Test
        @DisplayName("null 상품 키로 주문 생성 실패")
        void createOrderWithNullInstrumentKey() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, null, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.MARKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 키는 필수입니다");
        }

        @ParameterizedTest
        @MethodSource("invalidQuantities")
        @DisplayName("유효하지 않은 수량으로 주문 생성 실패")
        void createOrderWithInvalidQuantity(BigDecimal invalidQuantity) {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         invalidQuantity, OrderType.MARKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 0보다 커야 합니다");
        }

        private static Stream<Arguments> invalidQuantities() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-100"))
            );
        }

        @Test
        @DisplayName("null 주문 유형으로 주문 생성 실패")
        void createOrderWithNullOrderType() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 유형은 필수입니다");
        }

        @Test
        @DisplayName("지정가 주문에 null 가격으로 생성 실패")
        void createLimitOrderWithNullPrice() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.LIMIT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지정가 주문은 유효한 가격이 필요합니다");
        }

        @Test
        @DisplayName("지정가 주문에 0 이하 가격으로 생성 실패")
        void createLimitOrderWithZeroPrice() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.LIMIT, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지정가 주문은 유효한 가격이 필요합니다");
        }

        @Test
        @DisplayName("시장가 주문에 지정가격 설정하여 생성 실패")
        void createMarketOrderWithLimitPrice() {
            // given & when & then
            assertThatThrownBy(() ->
                new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                         VALID_QUANTITY, OrderType.MARKET, VALID_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시장가 주문에는 지정가를 설정할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("주문 실행 로직")
    class OrderExecutionTest {

        @Test
        @DisplayName("정상적인 매수 주문 실행")
        void executeBuyOrder() {
            // given
            Order order = createValidMarketOrder();
            BigDecimal marketPrice = new BigDecimal("50000");
            BigDecimal slippageRate = new BigDecimal("1.5"); // 1.5%
            LocalDateTime beforeExecution = LocalDateTime.now();

            // when
            order.execute(marketPrice, slippageRate);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);
            assertThat(order.getSlippageRate()).isEqualTo(slippageRate);
            assertThat(order.getExecutedAt()).isAfter(beforeExecution);

            // 매수 시 슬리피지로 인해 더 높은 가격에 체결되어야 함
            BigDecimal expectedPrice = marketPrice.multiply(new BigDecimal("1.015")); // 1 + 1.5/100
            assertThat(order.getExecutedPrice()).isEqualTo(expectedPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        @Test
        @DisplayName("정상적인 매도 주문 실행")
        void executeSellOrder() {
            // given
            Order order = new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.SELL,
                                  VALID_QUANTITY, OrderType.MARKET, null);
            BigDecimal marketPrice = new BigDecimal("50000");
            BigDecimal slippageRate = new BigDecimal("2.0"); // 2.0%

            // when
            order.execute(marketPrice, slippageRate);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);

            // 매도 시 슬리피지로 인해 더 낮은 가격에 체결되어야 함
            BigDecimal expectedPrice = marketPrice.multiply(new BigDecimal("0.98")); // 1 - 2.0/100
            assertThat(order.getExecutedPrice()).isEqualTo(expectedPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        @Test
        @DisplayName("슬리피지 없이 주문 실행")
        void executeOrderWithoutSlippage() {
            // given
            Order order = createValidMarketOrder();
            BigDecimal marketPrice = new BigDecimal("50000");
            BigDecimal slippageRate = BigDecimal.ZERO;

            // when
            order.execute(marketPrice, slippageRate);

            // then
            assertThat(order.getExecutedPrice()).isEqualTo(marketPrice);
            assertThat(order.getSlippageRate()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("null 슬리피지로 주문 실행 (기본값 적용)")
        void executeOrderWithNullSlippage() {
            // given
            Order order = createValidMarketOrder();
            BigDecimal marketPrice = new BigDecimal("50000");

            // when
            order.execute(marketPrice, null);

            // then
            assertThat(order.getExecutedPrice()).isEqualTo(marketPrice);
            assertThat(order.getSlippageRate()).isNull();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"EXECUTED", "CANCELLED"})
        @DisplayName("대기 중이 아닌 주문은 실행할 수 없음")
        void cannotExecuteNonPendingOrder(OrderStatus status) {
            // given
            Order order = createValidMarketOrder();
            if (status == OrderStatus.EXECUTED) {
                order.execute(VALID_PRICE, BigDecimal.ZERO);
            } else if (status == OrderStatus.CANCELLED) {
                order.cancel();
            }

            // when & then
            assertThatThrownBy(() -> order.execute(VALID_PRICE, BigDecimal.ZERO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 주문만 체결할 수 있습니다");
        }

        @ParameterizedTest
        @MethodSource("invalidMarketPrices")
        @DisplayName("유효하지 않은 시장가로 주문 실행 실패")
        void executeOrderWithInvalidMarketPrice(BigDecimal invalidPrice) {
            // given
            Order order = createValidMarketOrder();

            // when & then
            assertThatThrownBy(() -> order.execute(invalidPrice, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 시장가가 필요합니다");
        }

        private static Stream<Arguments> invalidMarketPrices() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-1000"))
            );
        }

        @ParameterizedTest
        @MethodSource("slippageTestCases")
        @DisplayName("슬리피지 계산 정확성 검증")
        void verifySlippageCalculation(OrderSide side, BigDecimal marketPrice,
                                     BigDecimal slippageRate, BigDecimal expectedPrice) {
            // given
            Order order = new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, side,
                                  VALID_QUANTITY, OrderType.MARKET, null);

            // when
            order.execute(marketPrice, slippageRate);

            // then
            assertThat(order.getExecutedPrice()).isEqualTo(expectedPrice);
        }

        private static Stream<Arguments> slippageTestCases() {
            return Stream.of(
                // 매수 주문 - 슬리피지로 인해 더 높은 가격
                Arguments.of(OrderSide.BUY, new BigDecimal("10000"), new BigDecimal("1"), new BigDecimal("10100.00")),
                Arguments.of(OrderSide.BUY, new BigDecimal("50000"), new BigDecimal("2"), new BigDecimal("51000.00")),
                Arguments.of(OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.5"), new BigDecimal("100500.00")),

                // 매도 주문 - 슬리피지로 인해 더 낮은 가격
                Arguments.of(OrderSide.SELL, new BigDecimal("10000"), new BigDecimal("1"), new BigDecimal("9900.00")),
                Arguments.of(OrderSide.SELL, new BigDecimal("50000"), new BigDecimal("2"), new BigDecimal("49000.00")),
                Arguments.of(OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.5"), new BigDecimal("99500.00"))
            );
        }
    }

    @Nested
    @DisplayName("주문 취소 로직")
    class OrderCancellationTest {

        @Test
        @DisplayName("대기 중인 주문 정상 취소")
        void cancelPendingOrder() {
            // given
            Order order = createValidMarketOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"EXECUTED", "CANCELLED"})
        @DisplayName("대기 중이 아닌 주문은 취소할 수 없음")
        void cannotCancelNonPendingOrder(OrderStatus status) {
            // given
            Order order = createValidMarketOrder();
            if (status == OrderStatus.EXECUTED) {
                order.execute(VALID_PRICE, BigDecimal.ZERO);
            } else if (status == OrderStatus.CANCELLED) {
                order.cancel();
            }

            // when & then
            assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 주문만 취소할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("주문 총액 계산")
    class OrderTotalValueTest {

        @Test
        @DisplayName("체결된 주문의 총액 계산")
        void calculateTotalValueForExecutedOrder() {
            // given
            Order order = createValidMarketOrder();
            BigDecimal executedPrice = new BigDecimal("50000");
            order.execute(executedPrice, BigDecimal.ZERO);

            // when
            BigDecimal totalValue = order.getTotalValue();

            // then
            BigDecimal expectedTotal = VALID_QUANTITY.multiply(executedPrice);
            assertThat(totalValue).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("미체결 주문의 총액은 0")
        void calculateTotalValueForPendingOrder() {
            // given
            Order order = createValidMarketOrder();

            // when
            BigDecimal totalValue = order.getTotalValue();

            // then
            assertThat(totalValue).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("취소된 주문의 총액은 0")
        void calculateTotalValueForCancelledOrder() {
            // given
            Order order = createValidMarketOrder();
            order.cancel();

            // when
            BigDecimal totalValue = order.getTotalValue();

            // then
            assertThat(totalValue).isEqualTo(BigDecimal.ZERO);
        }

        @ParameterizedTest
        @MethodSource("totalValueTestCases")
        @DisplayName("다양한 수량과 가격에 대한 총액 계산")
        void calculateTotalValueWithVariousAmounts(BigDecimal quantity, BigDecimal price, BigDecimal expectedTotal) {
            // given
            Order order = new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                                  quantity, OrderType.MARKET, null);
            order.execute(price, BigDecimal.ZERO);

            // when
            BigDecimal totalValue = order.getTotalValue();

            // then
            assertThat(totalValue).isEqualTo(expectedTotal);
        }

        private static Stream<Arguments> totalValueTestCases() {
            return Stream.of(
                Arguments.of(new BigDecimal("1"), new BigDecimal("100000"), new BigDecimal("100000")),
                Arguments.of(new BigDecimal("10"), new BigDecimal("50000"), new BigDecimal("500000")),
                Arguments.of(new BigDecimal("100"), new BigDecimal("1000"), new BigDecimal("100000")),
                Arguments.of(new BigDecimal("0.5"), new BigDecimal("200000"), new BigDecimal("100000.0"))
            );
        }
    }

    @Nested
    @DisplayName("주문 상태 전환 테스트")
    class OrderStateTransitionTest {

        @Test
        @DisplayName("완전한 주문 라이프사이클 테스트")
        void completeOrderLifecycle() {
            // given
            Order order = createValidMarketOrder();

            // 초기 상태 확인
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getExecutedAt()).isNull();
            assertThat(order.getExecutedPrice()).isNull();

            // 주문 실행
            BigDecimal marketPrice = new BigDecimal("50000");
            BigDecimal slippageRate = new BigDecimal("1.0");

            order.execute(marketPrice, slippageRate);

            // 실행 후 상태 확인
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);
            assertThat(order.getExecutedAt()).isNotNull();
            assertThat(order.getExecutedPrice()).isNotNull();
            assertThat(order.getSlippageRate()).isEqualTo(slippageRate);
        }

        @Test
        @DisplayName("주문 취소 라이프사이클 테스트")
        void orderCancellationLifecycle() {
            // given
            Order order = createValidMarketOrder();

            // 초기 상태 확인
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            // 주문 취소
            order.cancel();

            // 취소 후 상태 확인
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getExecutedAt()).isNull();
            assertThat(order.getExecutedPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("주문 엔티티 불변성 테스트")
    class OrderImmutabilityTest {

        @Test
        @DisplayName("Builder 패턴으로 생성된 주문의 불변성")
        void builderCreatedOrderImmutability() {
            // given & when
            Order order = Order.builder()
                .sessionId(VALID_SESSION_ID)
                .instrumentKey(VALID_INSTRUMENT_KEY)
                .side(OrderSide.BUY)
                .quantity(VALID_QUANTITY)
                .orderType(OrderType.MARKET)
                .status(OrderStatus.PENDING)
                .build();

            // then
            assertThat(order.getSessionId()).isEqualTo(VALID_SESSION_ID);
            assertThat(order.getInstrumentKey()).isEqualTo(VALID_INSTRUMENT_KEY);
            assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
            assertThat(order.getQuantity()).isEqualTo(VALID_QUANTITY);
            assertThat(order.getOrderType()).isEqualTo(OrderType.MARKET);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("주문 엔티티 비즈니스 규칙")
    class OrderBusinessRulesTest {

        @Test
        @DisplayName("동일한 세션 내에서 여러 주문 생성 가능")
        void multipleOrdersInSameSession() {
            // given & when
            Order order1 = new Order(VALID_SESSION_ID, "A", OrderSide.BUY,
                                   new BigDecimal("100"), OrderType.MARKET, null);
            Order order2 = new Order(VALID_SESSION_ID, "B", OrderSide.SELL,
                                   new BigDecimal("50"), OrderType.LIMIT, new BigDecimal("60000"));

            // then
            assertThat(order1.getSessionId()).isEqualTo(order2.getSessionId());
            assertThat(order1.getInstrumentKey()).isNotEqualTo(order2.getInstrumentKey());
            assertThat(order1.getSide()).isNotEqualTo(order2.getSide());
        }

        @Test
        @DisplayName("다양한 상품에 대한 주문 생성")
        void ordersForDifferentInstruments() {
            // given
            String[] instruments = {"A", "B", "C", "APPLE", "SAMSUNG"};

            for (String instrument : instruments) {
                // when
                Order order = new Order(VALID_SESSION_ID, instrument, OrderSide.BUY,
                                      VALID_QUANTITY, OrderType.MARKET, null);

                // then
                assertThat(order.getInstrumentKey()).isEqualTo(instrument);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            }
        }

        @Test
        @DisplayName("대량 주문의 정확한 슬리피지 계산")
        void largeOrderSlippageCalculation() {
            // given
            BigDecimal largeQuantity = new BigDecimal("10000");
            Order order = new Order(VALID_SESSION_ID, VALID_INSTRUMENT_KEY, OrderSide.BUY,
                                  largeQuantity, OrderType.MARKET, null);
            BigDecimal marketPrice = new BigDecimal("100000");
            BigDecimal highSlippage = new BigDecimal("3.0"); // 3%

            // when
            order.execute(marketPrice, highSlippage);

            // then
            BigDecimal expectedPrice = marketPrice.multiply(new BigDecimal("1.03"));
            assertThat(order.getExecutedPrice()).isEqualTo(expectedPrice.setScale(2, BigDecimal.ROUND_HALF_UP));

            BigDecimal expectedTotalValue = largeQuantity.multiply(order.getExecutedPrice());
            assertThat(order.getTotalValue()).isEqualTo(expectedTotalValue);
        }
    }
}