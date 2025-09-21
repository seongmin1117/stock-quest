package com.stockquest.domain.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * PortfolioPosition 도메인 엔티티 테스트
 * 포지션 관리, 평균가 계산, 손익 계산 등 핵심 비즈니스 로직 검증
 */
@DisplayName("PortfolioPosition 도메인 테스트")
class PortfolioPositionTest {

    // 테스트 데이터 팩토리
    private static final Long VALID_SESSION_ID = 1L;
    private static final String VALID_INSTRUMENT_KEY = "A";

    private PortfolioPosition createValidPosition() {
        return new PortfolioPosition(VALID_SESSION_ID, VALID_INSTRUMENT_KEY);
    }

    @Nested
    @DisplayName("포지션 생성 검증")
    class PositionCreationTest {

        @Test
        @DisplayName("유효한 포지션 생성")
        void createValidPosition() {
            // given & when
            PortfolioPosition position = createValidPosition();

            // then
            assertThat(position.getSessionId()).isEqualTo(VALID_SESSION_ID);
            assertThat(position.getInstrumentKey()).isEqualTo(VALID_INSTRUMENT_KEY);
            assertThat(position.getQuantity()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getAveragePrice()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getTotalCost()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.hasPosition()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("유효하지 않은 세션 ID로 포지션 생성 실패")
        void createPositionWithInvalidSessionId(Long invalidSessionId) {
            // given & when & then
            assertThatThrownBy(() -> new PortfolioPosition(invalidSessionId, VALID_INSTRUMENT_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 세션 ID가 필요합니다");
        }

        @Test
        @DisplayName("null 세션 ID로 포지션 생성 실패")
        void createPositionWithNullSessionId() {
            // given & when & then
            assertThatThrownBy(() -> new PortfolioPosition(null, VALID_INSTRUMENT_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 세션 ID가 필요합니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("유효하지 않은 상품 키로 포지션 생성 실패")
        void createPositionWithInvalidInstrumentKey(String invalidKey) {
            // given & when & then
            assertThatThrownBy(() -> new PortfolioPosition(VALID_SESSION_ID, invalidKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 키는 필수입니다");
        }

        @Test
        @DisplayName("null 상품 키로 포지션 생성 실패")
        void createPositionWithNullInstrumentKey() {
            // given & when & then
            assertThatThrownBy(() -> new PortfolioPosition(VALID_SESSION_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 키는 필수입니다");
        }
    }

    @Nested
    @DisplayName("매수 포지션 추가")
    class AddPositionTest {

        @Test
        @DisplayName("첫 매수 주문 처리")
        void addFirstBuyOrder() {
            // given
            PortfolioPosition position = createValidPosition();
            BigDecimal buyQuantity = new BigDecimal("100");
            BigDecimal buyPrice = new BigDecimal("50000");

            // when
            position.addPosition(buyQuantity, buyPrice);

            // then
            assertThat(position.getQuantity()).isEqualTo(buyQuantity);
            assertThat(position.getAveragePrice()).isEqualTo(buyPrice);
            assertThat(position.getTotalCost()).isEqualTo(buyQuantity.multiply(buyPrice));
            assertThat(position.hasPosition()).isTrue();
        }

        @Test
        @DisplayName("추가 매수 주문 처리 - 평균가 재계산")
        void addAdditionalBuyOrder() {
            // given
            PortfolioPosition position = createValidPosition();

            // 첫 번째 매수: 100주 @ 50,000원
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000"));

            // 두 번째 매수: 50주 @ 60,000원
            BigDecimal additionalQuantity = new BigDecimal("50");
            BigDecimal additionalPrice = new BigDecimal("60000");

            // when
            position.addPosition(additionalQuantity, additionalPrice);

            // then
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("150")); // 100 + 50

            // 평균가 계산: (100*50000 + 50*60000) / 150 = 8300000 / 150 = 55333.3333
            BigDecimal expectedAveragePrice = new BigDecimal("53333.3333");
            assertThat(position.getAveragePrice()).isEqualTo(expectedAveragePrice);

            // 총 비용: 100*50000 + 50*60000 = 8,000,000
            assertThat(position.getTotalCost()).isEqualTo(new BigDecimal("8000000"));
        }

        @Test
        @DisplayName("여러 번의 매수 주문 누적 처리")
        void addMultipleBuyOrders() {
            // given
            PortfolioPosition position = createValidPosition();

            // when - 3번의 매수 주문
            position.addPosition(new BigDecimal("50"), new BigDecimal("40000"));   // 50주 @ 40,000원
            position.addPosition(new BigDecimal("30"), new BigDecimal("50000"));   // 30주 @ 50,000원
            position.addPosition(new BigDecimal("20"), new BigDecimal("60000"));   // 20주 @ 60,000원

            // then
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100")); // 50 + 30 + 20

            // 총 비용: 50*40000 + 30*50000 + 20*60000 = 2000000 + 1500000 + 1200000 = 4700000
            assertThat(position.getTotalCost()).isEqualTo(new BigDecimal("4700000"));

            // 평균가: 4700000 / 100 = 47000
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("47000.0000"));
        }

        @ParameterizedTest
        @MethodSource("invalidBuyQuantities")
        @DisplayName("유효하지 않은 매수 수량으로 포지션 추가 실패")
        void addPositionWithInvalidQuantity(BigDecimal invalidQuantity) {
            // given
            PortfolioPosition position = createValidPosition();

            // when & then
            assertThatThrownBy(() -> position.addPosition(invalidQuantity, new BigDecimal("50000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매수 수량은 0보다 커야 합니다");
        }

        private static Stream<Arguments> invalidBuyQuantities() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-100"))
            );
        }

        @ParameterizedTest
        @MethodSource("invalidBuyPrices")
        @DisplayName("유효하지 않은 매수 가격으로 포지션 추가 실패")
        void addPositionWithInvalidPrice(BigDecimal invalidPrice) {
            // given
            PortfolioPosition position = createValidPosition();

            // when & then
            assertThatThrownBy(() -> position.addPosition(new BigDecimal("100"), invalidPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매수 가격은 0보다 커야 합니다");
        }

        private static Stream<Arguments> invalidBuyPrices() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-50000"))
            );
        }
    }

    @Nested
    @DisplayName("매도 포지션 감소")
    class ReducePositionTest {

        private PortfolioPosition createPositionWithHoldings() {
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000")); // 100주 @ 50,000원
            return position;
        }

        @Test
        @DisplayName("일부 매도 주문 처리 - 손익 계산")
        void reducePositionPartially() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal sellQuantity = new BigDecimal("30");
            BigDecimal sellPrice = new BigDecimal("55000");

            // when
            BigDecimal realizedPnL = position.reducePosition(sellQuantity, sellPrice);

            // then
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("70")); // 100 - 30

            // 매도한 부분의 비용: 30 * 50000 = 1,500,000
            // 남은 비용: (100 * 50000) - 1,500,000 = 3,500,000
            assertThat(position.getTotalCost()).isEqualTo(new BigDecimal("3500000"));

            // 평균가는 변하지 않음
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000"));

            // 실현 손익: (55000 - 50000) * 30 = 150,000원 수익
            assertThat(realizedPnL).isEqualTo(new BigDecimal("150000"));
            assertThat(position.hasPosition()).isTrue();
        }

        @Test
        @DisplayName("전체 매도 주문 처리 - 포지션 클리어")
        void reducePositionCompletely() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal sellQuantity = new BigDecimal("100");
            BigDecimal sellPrice = new BigDecimal("45000");

            // when
            BigDecimal realizedPnL = position.reducePosition(sellQuantity, sellPrice);

            // then
            assertThat(position.getQuantity()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getTotalCost()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getAveragePrice()).isEqualTo(BigDecimal.ZERO);

            // 실현 손익: (45000 - 50000) * 100 = -500,000원 손실
            assertThat(realizedPnL).isEqualTo(new BigDecimal("-500000"));
            assertThat(position.hasPosition()).isFalse();
        }

        @Test
        @DisplayName("매도 수량이 보유 수량보다 많은 경우 실패")
        void cannotSellMoreThanHolding() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal excessiveQuantity = new BigDecimal("150"); // 보유량 100주보다 많음

            // when & then
            assertThatThrownBy(() -> position.reducePosition(excessiveQuantity, new BigDecimal("50000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유 수량보다 많이 매도할 수 없습니다");
        }

        @ParameterizedTest
        @MethodSource("invalidSellQuantities")
        @DisplayName("유효하지 않은 매도 수량으로 포지션 감소 실패")
        void reducePositionWithInvalidQuantity(BigDecimal invalidQuantity) {
            // given
            PortfolioPosition position = createPositionWithHoldings();

            // when & then
            assertThatThrownBy(() -> position.reducePosition(invalidQuantity, new BigDecimal("50000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매도 수량은 0보다 커야 합니다");
        }

        private static Stream<Arguments> invalidSellQuantities() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-50"))
            );
        }

        @ParameterizedTest
        @MethodSource("invalidSellPrices")
        @DisplayName("유효하지 않은 매도 가격으로 포지션 감소 실패")
        void reducePositionWithInvalidPrice(BigDecimal invalidPrice) {
            // given
            PortfolioPosition position = createPositionWithHoldings();

            // when & then
            assertThatThrownBy(() -> position.reducePosition(new BigDecimal("50"), invalidPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매도 가격은 0보다 커야 합니다");
        }

        private static Stream<Arguments> invalidSellPrices() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-50000"))
            );
        }

        @ParameterizedTest
        @MethodSource("realizedPnLTestCases")
        @DisplayName("다양한 매도 시나리오에 따른 실현 손익 계산")
        void verifyRealizedPnLCalculation(BigDecimal averagePrice, BigDecimal sellPrice,
                                        BigDecimal sellQuantity, BigDecimal expectedPnL) {
            // given
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), averagePrice);

            // when
            BigDecimal actualPnL = position.reducePosition(sellQuantity, sellPrice);

            // then
            assertThat(actualPnL).isEqualTo(expectedPnL);
        }

        private static Stream<Arguments> realizedPnLTestCases() {
            return Stream.of(
                // 수익 시나리오
                Arguments.of(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("50"), new BigDecimal("500000")),
                Arguments.of(new BigDecimal("30000"), new BigDecimal("35000"), new BigDecimal("20"), new BigDecimal("100000")),

                // 손실 시나리오
                Arguments.of(new BigDecimal("50000"), new BigDecimal("40000"), new BigDecimal("50"), new BigDecimal("-500000")),
                Arguments.of(new BigDecimal("60000"), new BigDecimal("55000"), new BigDecimal("30"), new BigDecimal("-150000")),

                // 무손익 시나리오
                Arguments.of(new BigDecimal("50000"), new BigDecimal("50000"), new BigDecimal("100"), BigDecimal.ZERO)
            );
        }
    }

    @Nested
    @DisplayName("평가손익 계산")
    class UnrealizedPnLTest {

        private PortfolioPosition createPositionWithHoldings() {
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000")); // 100주 @ 50,000원
            return position;
        }

        @Test
        @DisplayName("현재가가 평균가보다 높을 때 평가수익")
        void calculateUnrealizedProfitWhenPriceIncreases() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal currentPrice = new BigDecimal("55000");

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);

            // then
            // (55000 - 50000) * 100 = 500,000원 평가수익
            assertThat(unrealizedPnL).isEqualTo(new BigDecimal("500000"));
        }

        @Test
        @DisplayName("현재가가 평균가보다 낮을 때 평가손실")
        void calculateUnrealizedLossWhenPriceDecreases() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal currentPrice = new BigDecimal("45000");

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);

            // then
            // (45000 - 50000) * 100 = -500,000원 평가손실
            assertThat(unrealizedPnL).isEqualTo(new BigDecimal("-500000"));
        }

        @Test
        @DisplayName("현재가가 평균가와 동일할 때 평가손익 0")
        void calculateUnrealizedPnLWhenPriceEqual() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal currentPrice = new BigDecimal("50000");

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);

            // then
            assertThat(unrealizedPnL).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("보유량이 0일 때 평가손익 0")
        void calculateUnrealizedPnLWithZeroQuantity() {
            // given
            PortfolioPosition position = createValidPosition(); // 빈 포지션
            BigDecimal currentPrice = new BigDecimal("50000");

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);

            // then
            assertThat(unrealizedPnL).isEqualTo(BigDecimal.ZERO);
        }

        @ParameterizedTest
        @MethodSource("invalidCurrentPrices")
        @DisplayName("유효하지 않은 현재가로 평가손익 계산 시 0 반환")
        void calculateUnrealizedPnLWithInvalidPrice(BigDecimal invalidPrice) {
            // given
            PortfolioPosition position = createPositionWithHoldings();

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(invalidPrice);

            // then
            assertThat(unrealizedPnL).isEqualTo(BigDecimal.ZERO);
        }

        private static Stream<Arguments> invalidCurrentPrices() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-1000"))
            );
        }
    }

    @Nested
    @DisplayName("현재 평가금액 계산")
    class CurrentValueTest {

        private PortfolioPosition createPositionWithHoldings() {
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000")); // 100주 @ 50,000원
            return position;
        }

        @Test
        @DisplayName("정상적인 현재 평가금액 계산")
        void calculateCurrentValue() {
            // given
            PortfolioPosition position = createPositionWithHoldings();
            BigDecimal currentPrice = new BigDecimal("55000");

            // when
            BigDecimal currentValue = position.calculateCurrentValue(currentPrice);

            // then
            // 100 * 55000 = 5,500,000
            assertThat(currentValue).isEqualTo(new BigDecimal("5500000"));
        }

        @Test
        @DisplayName("보유량이 0일 때 현재 평가금액 0")
        void calculateCurrentValueWithZeroQuantity() {
            // given
            PortfolioPosition position = createValidPosition(); // 빈 포지션
            BigDecimal currentPrice = new BigDecimal("50000");

            // when
            BigDecimal currentValue = position.calculateCurrentValue(currentPrice);

            // then
            assertThat(currentValue).isEqualTo(BigDecimal.ZERO);
        }

        @ParameterizedTest
        @MethodSource("invalidCurrentPrices")
        @DisplayName("유효하지 않은 현재가로 평가금액 계산 시 0 반환")
        void calculateCurrentValueWithInvalidPrice(BigDecimal invalidPrice) {
            // given
            PortfolioPosition position = createPositionWithHoldings();

            // when
            BigDecimal currentValue = position.calculateCurrentValue(invalidPrice);

            // then
            assertThat(currentValue).isEqualTo(BigDecimal.ZERO);
        }

        private static Stream<Arguments> invalidCurrentPrices() {
            return Stream.of(
                Arguments.of((BigDecimal) null),
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-1000"))
            );
        }
    }

    @Nested
    @DisplayName("복합 거래 시나리오")
    class ComplexTradingScenarioTest {

        @Test
        @DisplayName("완전한 거래 라이프사이클 - 매수/추가매수/부분매도/전체매도")
        void completeTradeLifecycle() {
            // given
            PortfolioPosition position = createValidPosition();

            // 1. 첫 번째 매수: 50주 @ 40,000원
            position.addPosition(new BigDecimal("50"), new BigDecimal("40000"));
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("50"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("40000"));

            // 2. 두 번째 매수: 50주 @ 60,000원 (평균가: 50,000원)
            position.addPosition(new BigDecimal("50"), new BigDecimal("60000"));
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000"));

            // 3. 부분 매도: 30주 @ 55,000원 (실현수익: 150,000원)
            BigDecimal firstSellPnL = position.reducePosition(new BigDecimal("30"), new BigDecimal("55000"));
            assertThat(firstSellPnL).isEqualTo(new BigDecimal("150000"));
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("70"));

            // 4. 평가손익 확인 (현재가 52,000원)
            BigDecimal currentPrice = new BigDecimal("52000");
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);
            assertThat(unrealizedPnL).isEqualTo(new BigDecimal("140000")); // (52000 - 50000) * 70

            // 5. 전체 매도: 70주 @ 48,000원 (실현손실: 140,000원)
            BigDecimal secondSellPnL = position.reducePosition(new BigDecimal("70"), new BigDecimal("48000"));
            assertThat(secondSellPnL).isEqualTo(new BigDecimal("-140000"));
            assertThat(position.hasPosition()).isFalse();
        }

        @Test
        @DisplayName("분할 매수 후 분할 매도 - 평균가 유지 확인")
        void splitBuyAndSellWithAveragePriceConsistency() {
            // given
            PortfolioPosition position = createValidPosition();

            // 3번의 분할 매수
            position.addPosition(new BigDecimal("30"), new BigDecimal("45000"));
            position.addPosition(new BigDecimal("40"), new BigDecimal("50000"));
            position.addPosition(new BigDecimal("30"), new BigDecimal("55000"));

            // 총 100주, 평균가 계산: (30*45000 + 40*50000 + 30*55000) / 100 = 50000
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000"));

            // 첫 번째 부분 매도
            BigDecimal firstPnL = position.reducePosition(new BigDecimal("25"), new BigDecimal("52000"));
            assertThat(firstPnL).isEqualTo(new BigDecimal("50000")); // (52000 - 50000) * 25
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("75"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000")); // 평균가 유지

            // 두 번째 부분 매도
            BigDecimal secondPnL = position.reducePosition(new BigDecimal("25"), new BigDecimal("47000"));
            assertThat(secondPnL).isEqualTo(new BigDecimal("-75000")); // (47000 - 50000) * 25
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("50"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000")); // 평균가 유지
        }

        @Test
        @DisplayName("극한 시나리오 - 소수점 수량과 가격")
        void extremeScenarioWithDecimalValues() {
            // given
            PortfolioPosition position = createValidPosition();

            // 소수점 수량과 가격으로 매수
            position.addPosition(new BigDecimal("12.5"), new BigDecimal("33333.33"));
            position.addPosition(new BigDecimal("7.5"), new BigDecimal("66666.67"));

            // then
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("20.0"));

            // 평균가 계산: (12.5 * 33333.33 + 7.5 * 66666.67) / 20
            // = (416666.625 + 500000.025) / 20 = 916666.65 / 20 = 45833.3325
            BigDecimal expectedAverage = new BigDecimal("45833.3325");
            assertThat(position.getAveragePrice()).isEqualTo(expectedAverage);

            // 부분 매도
            BigDecimal pnl = position.reducePosition(new BigDecimal("5.5"), new BigDecimal("50000"));
            // (50000 - 45833.3325) * 5.5 = 4166.6675 * 5.5 = 22916.67125
            assertThat(pnl).isEqualTo(new BigDecimal("22916.67125"));
        }
    }

    @Nested
    @DisplayName("포지션 상태 확인")
    class PositionStatusTest {

        @Test
        @DisplayName("빈 포지션 상태 확인")
        void checkEmptyPositionStatus() {
            // given
            PortfolioPosition position = createValidPosition();

            // then
            assertThat(position.hasPosition()).isFalse();
            assertThat(position.getQuantity()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("보유 포지션 상태 확인")
        void checkHoldingPositionStatus() {
            // given
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000"));

            // then
            assertThat(position.hasPosition()).isTrue();
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("전체 매도 후 빈 포지션 상태 확인")
        void checkEmptyPositionAfterFullSell() {
            // given
            PortfolioPosition position = createValidPosition();
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000"));
            position.reducePosition(new BigDecimal("100"), new BigDecimal("55000"));

            // then
            assertThat(position.hasPosition()).isFalse();
            assertThat(position.getQuantity()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getAveragePrice()).isEqualTo(BigDecimal.ZERO);
            assertThat(position.getTotalCost()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("Builder로 포지션 생성")
        void createPositionWithBuilder() {
            // given & when
            PortfolioPosition position = PortfolioPosition.builder()
                .sessionId(VALID_SESSION_ID)
                .instrumentKey(VALID_INSTRUMENT_KEY)
                .quantity(new BigDecimal("100"))
                .averagePrice(new BigDecimal("50000"))
                .totalCost(new BigDecimal("5000000"))
                .build();

            // then
            assertThat(position.getSessionId()).isEqualTo(VALID_SESSION_ID);
            assertThat(position.getInstrumentKey()).isEqualTo(VALID_INSTRUMENT_KEY);
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000"));
            assertThat(position.getTotalCost()).isEqualTo(new BigDecimal("5000000"));
            assertThat(position.hasPosition()).isTrue();
        }
    }

    @Nested
    @DisplayName("정밀도 및 반올림 테스트")
    class PrecisionAndRoundingTest {

        @Test
        @DisplayName("평균가 계산의 정밀도 확인")
        void verifyAveragePricePrecision() {
            // given
            PortfolioPosition position = createValidPosition();

            // when - 정확히 나누어떨어지지 않는 경우
            position.addPosition(new BigDecimal("3"), new BigDecimal("10000"));
            position.addPosition(new BigDecimal("7"), new BigDecimal("20000"));

            // then
            // 총 비용: 3*10000 + 7*20000 = 30000 + 140000 = 170000
            // 총 수량: 3 + 7 = 10
            // 평균가: 170000 / 10 = 17000.0000 (소수점 4자리)
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("17000.0000"));
        }

        @Test
        @DisplayName("반올림 처리 확인 (HALF_UP)")
        void verifyHalfUpRounding() {
            // given
            PortfolioPosition position = createValidPosition();

            // when - 소수점 5자리에서 반올림이 필요한 경우
            position.addPosition(new BigDecimal("3"), new BigDecimal("33333.33"));

            // then
            // 총 비용: 3 * 33333.33 = 99999.99
            // 평균가: 99999.99 / 3 = 33333.3300 (HALF_UP 반올림)
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("33333.3300"));
        }
    }
}