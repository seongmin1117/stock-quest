package com.stockquest.integration;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;
import com.stockquest.domain.portfolio.PortfolioPosition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * 핵심 도메인 엔티티들의 통합 테스트
 * 새로 작성된 테스트 코드들의 정상 동작을 검증
 */
@DisplayName("핵심 도메인 통합 테스트")
class CoreDomainTestSuite {

    @Nested
    @DisplayName("Challenge 도메인 기본 동작 검증")
    class ChallengeBasicTest {

        @Test
        @DisplayName("Challenge 엔티티 생성 및 기본 기능 검증")
        void challengeEntityBasicOperations() {
            // given & when
            Challenge challenge = Challenge.builder()
                .title("통합 테스트 챌린지")
                .description("테스트용 챌린지")
                .difficulty(ChallengeDifficulty.BEGINNER)
                .seedMoney(new BigDecimal("1000000"))
                .maxParticipants(100)
                .status(ChallengeStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .participantCount(0)
                .build();

            // then
            assertThat(challenge.getTitle()).isEqualTo("통합 테스트 챌린지");
            assertThat(challenge.getDifficulty()).isEqualTo(ChallengeDifficulty.BEGINNER);
            assertThat(challenge.canJoin()).isTrue();
            assertThat(challenge.getRemainingSlots()).isEqualTo(100);
        }

        @Test
        @DisplayName("Challenge 참가자 관리 기능 검증")
        void challengeParticipantManagement() {
            // given
            Challenge challenge = Challenge.builder()
                .title("참가자 관리 테스트")
                .maxParticipants(2)
                .participantCount(0)
                .status(ChallengeStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

            // when & then - 참가자 추가
            challenge.addParticipant();
            assertThat(challenge.getParticipantCount()).isEqualTo(1);
            assertThat(challenge.canJoin()).isTrue();

            challenge.addParticipant();
            assertThat(challenge.getParticipantCount()).isEqualTo(2);
            assertThat(challenge.canJoin()).isFalse();
        }
    }

    @Nested
    @DisplayName("ChallengeSession 도메인 기본 동작 검증")
    class ChallengeSessionBasicTest {

        @Test
        @DisplayName("ChallengeSession 엔티티 생성 및 상태 관리 검증")
        void sessionEntityBasicOperations() {
            // given & when
            ChallengeSession session = ChallengeSession.builder()
                .userId(1L)
                .challengeId(1L)
                .seedMoney(new BigDecimal("1000000"))
                .currentBalance(new BigDecimal("1000000"))
                .totalInvestment(BigDecimal.ZERO)
                .totalReturn(BigDecimal.ZERO)
                .returnRate(BigDecimal.ZERO)
                .status(ChallengeSession.SessionStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

            // then
            assertThat(session.getUserId()).isEqualTo(1L);
            assertThat(session.getChallengeId()).isEqualTo(1L);
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.READY);
            assertThat(session.getSeedMoney()).isEqualTo(new BigDecimal("1000000"));
        }

        @Test
        @DisplayName("ChallengeSession 잔액 관리 기능 검증")
        void sessionBalanceManagement() {
            // given
            ChallengeSession session = ChallengeSession.builder()
                .currentBalance(new BigDecimal("1000000"))
                .status(ChallengeSession.SessionStatus.ACTIVE)
                .build();

            // when & then - 투자금 증가
            session.increaseInvestment(new BigDecimal("500000"));
            assertThat(session.getCurrentBalance()).isEqualTo(new BigDecimal("500000"));

            // when & then - 투자금 감소 (수익 실현)
            session.decreaseInvestment(new BigDecimal("100000"));
            assertThat(session.getCurrentBalance()).isEqualTo(new BigDecimal("600000"));
        }
    }

    @Nested
    @DisplayName("Order 도메인 기본 동작 검증")
    class OrderBasicTest {

        @Test
        @DisplayName("Order 엔티티 생성 및 실행 검증")
        void orderEntityBasicOperations() {
            // given
            Order order = new Order(1L, "A", OrderSide.BUY,
                                  new BigDecimal("100"), OrderType.MARKET, null);

            // then - 초기 상태 확인
            assertThat(order.getSessionId()).isEqualTo(1L);
            assertThat(order.getInstrumentKey()).isEqualTo("A");
            assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
            assertThat(order.getQuantity()).isEqualTo(new BigDecimal("100"));
            assertThat(order.getOrderType()).isEqualTo(OrderType.MARKET);

            // when - 주문 실행
            BigDecimal marketPrice = new BigDecimal("50000");
            BigDecimal slippageRate = new BigDecimal("1.0");
            order.execute(marketPrice, slippageRate);

            // then - 실행 후 상태 확인
            assertThat(order.getExecutedPrice()).isNotNull();
            assertThat(order.getSlippageRate()).isEqualTo(slippageRate);
            assertThat(order.getTotalValue()).isEqualTo(order.getExecutedPrice().multiply(order.getQuantity()));
        }
    }

    @Nested
    @DisplayName("PortfolioPosition 도메인 기본 동작 검증")
    class PortfolioPositionBasicTest {

        @Test
        @DisplayName("PortfolioPosition 엔티티 생성 및 포지션 관리 검증")
        void portfolioPositionBasicOperations() {
            // given
            PortfolioPosition position = new PortfolioPosition(1L, "A");

            // then - 초기 상태 확인
            assertThat(position.getSessionId()).isEqualTo(1L);
            assertThat(position.getInstrumentKey()).isEqualTo("A");
            assertThat(position.hasPosition()).isFalse();
            assertThat(position.getQuantity()).isEqualTo(BigDecimal.ZERO);

            // when - 매수 추가
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000"));

            // then - 매수 후 상태 확인
            assertThat(position.hasPosition()).isTrue();
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("100"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000"));
            assertThat(position.getTotalCost()).isEqualTo(new BigDecimal("5000000"));
        }

        @Test
        @DisplayName("PortfolioPosition 매수/매도 및 손익 계산 검증")
        void portfolioPositionPnLCalculation() {
            // given
            PortfolioPosition position = new PortfolioPosition(1L, "A");
            position.addPosition(new BigDecimal("100"), new BigDecimal("50000"));

            // when - 일부 매도
            BigDecimal realizedPnL = position.reducePosition(new BigDecimal("30"), new BigDecimal("55000"));

            // then - 매도 후 상태 확인
            assertThat(realizedPnL).isEqualTo(new BigDecimal("150000")); // (55000 - 50000) * 30
            assertThat(position.getQuantity()).isEqualTo(new BigDecimal("70"));
            assertThat(position.getAveragePrice()).isEqualTo(new BigDecimal("50000.0000")); // 평균가 유지

            // when & then - 평가손익 계산
            BigDecimal currentPrice = new BigDecimal("52000");
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);
            assertThat(unrealizedPnL).isEqualTo(new BigDecimal("140000")); // (52000 - 50000) * 70
        }
    }

    @Nested
    @DisplayName("도메인 간 통합 시나리오 검증")
    class IntegratedScenarioTest {

        @Test
        @DisplayName("챌린지 참가 → 세션 생성 → 주문 실행 → 포지션 관리 통합 시나리오")
        void fullTradingScenario() {
            // 1. 챌린지 생성 및 참가
            Challenge challenge = Challenge.builder()
                .title("통합 시나리오 챌린지")
                .maxParticipants(10)
                .participantCount(0)
                .seedMoney(new BigDecimal("1000000"))
                .status(ChallengeStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

            challenge.addParticipant();
            assertThat(challenge.getParticipantCount()).isEqualTo(1);

            // 2. 챌린지 세션 생성 및 시작
            ChallengeSession session = ChallengeSession.builder()
                .userId(1L)
                .challengeId(1L)
                .seedMoney(challenge.getSeedMoney())
                .currentBalance(challenge.getSeedMoney())
                .totalInvestment(BigDecimal.ZERO)
                .status(ChallengeSession.SessionStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

            session.start();
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.ACTIVE);

            // 3. 주문 생성 및 실행
            Order buyOrder = new Order(1L, "A", OrderSide.BUY,
                                     new BigDecimal("100"), OrderType.MARKET, null);
            buyOrder.execute(new BigDecimal("50000"), new BigDecimal("1.0"));

            assertThat(buyOrder.getExecutedPrice()).isNotNull();
            BigDecimal totalOrderValue = buyOrder.getTotalValue();

            // 4. 포지션 업데이트
            PortfolioPosition position = new PortfolioPosition(1L, "A");
            position.addPosition(buyOrder.getQuantity(), buyOrder.getExecutedPrice());

            // 5. 세션 잔액 업데이트
            session.increaseInvestment(totalOrderValue);

            // 6. 통합 검증
            assertThat(position.hasPosition()).isTrue();
            assertThat(position.getQuantity()).isEqualTo(buyOrder.getQuantity());
            assertThat(session.getCurrentBalance()).isEqualTo(
                challenge.getSeedMoney().subtract(totalOrderValue)
            );

            // 7. 현재 평가손익 계산
            BigDecimal currentPrice = new BigDecimal("52000");
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);
            assertThat(unrealizedPnL).isGreaterThan(BigDecimal.ZERO); // 수익 상태

            // 8. 세션 종료
            session.complete();
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.COMPLETED);
        }
    }
}