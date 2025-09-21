package com.stockquest.domain.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ChallengeSession 도메인 엔티티 핵심 비즈니스 로직 테스트
 */
@DisplayName("ChallengeSession 도메인 테스트")
class ChallengeSessionTest {

    @Nested
    @DisplayName("세션 생성 유효성 검증 테스트")
    class SessionCreationValidationTest {

        @Test
        @DisplayName("유효한 매개변수로 세션 생성 성공")
        void createSessionWithValidParameters() {
            // Given
            Long challengeId = 1L;
            Long userId = 100L;
            BigDecimal initialBalance = BigDecimal.valueOf(1000000);

            // When
            ChallengeSession session = new ChallengeSession(challengeId, userId, initialBalance);

            // Then
            assertThat(session.getChallengeId()).isEqualTo(challengeId);
            assertThat(session.getUserId()).isEqualTo(userId);
            assertThat(session.getInitialBalance()).isEqualByComparingTo(initialBalance);
            assertThat(session.getCurrentBalance()).isEqualByComparingTo(initialBalance);
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.READY);
            assertThat(session.getCreatedAt()).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L})
        @DisplayName("잘못된 챌린지 ID로 세션 생성 실패")
        void createSessionWithInvalidChallengeId(Long invalidChallengeId) {
            // Given
            Long userId = 100L;
            BigDecimal initialBalance = BigDecimal.valueOf(1000000);

            // When & Then
            assertThatThrownBy(() -> new ChallengeSession(invalidChallengeId, userId, initialBalance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 챌린지 ID가 필요합니다");
        }

        @Test
        @DisplayName("null 챌린지 ID로 세션 생성 실패")
        void createSessionWithNullChallengeId() {
            // Given
            Long challengeId = null;
            Long userId = 100L;
            BigDecimal initialBalance = BigDecimal.valueOf(1000000);

            // When & Then
            assertThatThrownBy(() -> new ChallengeSession(challengeId, userId, initialBalance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 챌린지 ID가 필요합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L})
        @DisplayName("잘못된 사용자 ID로 세션 생성 실패")
        void createSessionWithInvalidUserId(Long invalidUserId) {
            // Given
            Long challengeId = 1L;
            BigDecimal initialBalance = BigDecimal.valueOf(1000000);

            // When & Then
            assertThatThrownBy(() -> new ChallengeSession(challengeId, invalidUserId, initialBalance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 사용자 ID가 필요합니다");
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInitialBalances")
        @DisplayName("잘못된 초기 잔고로 세션 생성 실패")
        void createSessionWithInvalidInitialBalance(BigDecimal invalidBalance, String expectedMessage) {
            // Given
            Long challengeId = 1L;
            Long userId = 100L;

            // When & Then
            assertThatThrownBy(() -> new ChallengeSession(challengeId, userId, invalidBalance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> provideInvalidInitialBalances() {
            return Stream.of(
                Arguments.of(null, "시드 머니는 0보다 커야 합니다"),
                Arguments.of(BigDecimal.ZERO, "시드 머니는 0보다 커야 합니다"),
                Arguments.of(BigDecimal.valueOf(-1000), "시드 머니는 0보다 커야 합니다"),
                Arguments.of(BigDecimal.valueOf(100000001), "시드 머니는 1억원을 초과할 수 없습니다")
            );
        }
    }

    @Nested
    @DisplayName("세션 상태 전환 테스트")
    class SessionStateTransitionTest {

        @Test
        @DisplayName("READY 상태에서 세션 시작 성공")
        void startSessionFromReadyState() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.READY);

            // When
            session.start();

            // Then
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.ACTIVE);
            assertThat(session.getStartedAt()).isNotNull();
            assertThat(session.isActive()).isTrue();
            assertThat(session.canStart()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ACTIVE", "COMPLETED", "CANCELLED"})
        @DisplayName("READY가 아닌 상태에서 세션 시작 실패")
        void startSessionFromNonReadyState(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When & Then
            assertThatThrownBy(session::start)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("준비 상태의 세션만 시작할 수 있습니다");
        }

        @Test
        @DisplayName("ACTIVE 상태에서 세션 종료 성공")
        void endSessionFromActiveState() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);

            // When
            session.end();

            // Then
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.COMPLETED);
            assertThat(session.getCompletedAt()).isNotNull();
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.isActive()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"READY", "COMPLETED", "CANCELLED"})
        @DisplayName("ACTIVE가 아닌 상태에서 세션 종료 실패")
        void endSessionFromNonActiveState(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When & Then
            assertThatThrownBy(session::end)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("진행 중인 세션만 종료할 수 있습니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"READY", "ACTIVE"})
        @DisplayName("READY 또는 ACTIVE 상태에서 세션 취소 성공")
        void cancelSessionFromValidState(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When
            session.cancel();

            // Then
            assertThat(session.getStatus()).isEqualTo(ChallengeSession.SessionStatus.CANCELLED);
            assertThat(session.getCompletedAt()).isNotNull();
            assertThat(session.isCompleted()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"COMPLETED", "CANCELLED"})
        @DisplayName("완료된 상태에서 세션 취소 실패")
        void cancelSessionFromCompletedState(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When & Then
            assertThatThrownBy(session::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("진행 중이거나 준비 상태의 세션만 취소할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("잔고 관리 테스트")
    class BalanceManagementTest {

        @Test
        @DisplayName("유효한 잔고로 업데이트 성공")
        void updateBalanceWithValidAmount() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);
            BigDecimal newBalance = BigDecimal.valueOf(1200000);

            // When
            session.updateBalance(newBalance);

            // Then
            assertThat(session.getCurrentBalance()).isEqualByComparingTo(newBalance);
        }

        @Test
        @DisplayName("null 잔고로 업데이트 실패")
        void updateBalanceWithNull() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);

            // When & Then
            assertThatThrownBy(() -> session.updateBalance(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("잔고는 null일 수 없습니다");
        }

        @Test
        @DisplayName("음수 잔고로 업데이트 실패")
        void updateBalanceWithNegativeAmount() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);
            BigDecimal negativeBalance = BigDecimal.valueOf(-1000);

            // When & Then
            assertThatThrownBy(() -> session.updateBalance(negativeBalance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("잔고는 음수일 수 없습니다");
        }

        @Test
        @DisplayName("ACTIVE 상태이고 잔고 충분하면 주문 가능")
        void canPlaceOrderWhenActiveAndSufficientBalance() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);
            session.updateBalance(BigDecimal.valueOf(1000000));
            BigDecimal orderValue = BigDecimal.valueOf(500000);

            // When & Then
            assertThat(session.canPlaceOrder(orderValue)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태이지만 잔고 부족하면 주문 불가")
        void cannotPlaceOrderWhenActiveButInsufficientBalance() {
            // Given
            ChallengeSession session = createSession(ChallengeSession.SessionStatus.ACTIVE);
            session.updateBalance(BigDecimal.valueOf(500000));
            BigDecimal orderValue = BigDecimal.valueOf(1000000);

            // When & Then
            assertThat(session.canPlaceOrder(orderValue)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"READY", "COMPLETED", "CANCELLED"})
        @DisplayName("ACTIVE가 아닌 상태에서는 주문 불가")
        void cannotPlaceOrderWhenNotActive(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);
            session.updateBalance(BigDecimal.valueOf(1000000));
            BigDecimal orderValue = BigDecimal.valueOf(500000);

            // When & Then
            assertThat(session.canPlaceOrder(orderValue)).isFalse();
        }
    }

    @Nested
    @DisplayName("수익률 계산 테스트")
    class ProfitLossCalculationTest {

        @Test
        @DisplayName("총 손익 계산 - 수익 케이스")
        void calculateTotalPnLWithProfit() {
            // Given
            ChallengeSession session = createSessionWithBalance(BigDecimal.valueOf(1000000), BigDecimal.valueOf(800000));
            BigDecimal portfolioValue = BigDecimal.valueOf(1500000);

            // When
            BigDecimal totalPnL = session.calculateTotalPnL(portfolioValue);

            // Then
            // 포트폴리오 1,500,000 + 현재 잔고 800,000 - 초기 잔고 1,000,000 = 1,300,000
            assertThat(totalPnL).isEqualByComparingTo(BigDecimal.valueOf(1300000));
        }

        @Test
        @DisplayName("총 손익 계산 - 손실 케이스")
        void calculateTotalPnLWithLoss() {
            // Given
            ChallengeSession session = createSessionWithBalance(BigDecimal.valueOf(1000000), BigDecimal.valueOf(600000));
            BigDecimal portfolioValue = BigDecimal.valueOf(200000);

            // When
            BigDecimal totalPnL = session.calculateTotalPnL(portfolioValue);

            // Then
            // 포트폴리오 200,000 + 현재 잔고 600,000 - 초기 잔고 1,000,000 = -200,000
            assertThat(totalPnL).isEqualByComparingTo(BigDecimal.valueOf(-200000));
        }

        @Test
        @DisplayName("수익률 계산 - 양의 수익률")
        void calculateReturnPercentageWithPositiveReturn() {
            // Given
            ChallengeSession session = createSessionWithBalance(BigDecimal.valueOf(1000000), BigDecimal.valueOf(500000));
            BigDecimal portfolioValue = BigDecimal.valueOf(800000);

            // When
            BigDecimal returnPercentage = session.calculateReturnPercentage(portfolioValue);

            // Then
            // 총 가치: 800,000 + 500,000 = 1,300,000
            // 수익률: (1,300,000 - 1,000,000) / 1,000,000 * 100 = 30%
            assertThat(returnPercentage).isEqualByComparingTo(BigDecimal.valueOf(30.0000));
            assertThat(session.getReturnRate()).isEqualByComparingTo(BigDecimal.valueOf(30.0000));
        }

        @Test
        @DisplayName("수익률 계산 - 음의 수익률")
        void calculateReturnPercentageWithNegativeReturn() {
            // Given
            ChallengeSession session = createSessionWithBalance(BigDecimal.valueOf(1000000), BigDecimal.valueOf(400000));
            BigDecimal portfolioValue = BigDecimal.valueOf(300000);

            // When
            BigDecimal returnPercentage = session.calculateReturnPercentage(portfolioValue);

            // Then
            // 총 가치: 300,000 + 400,000 = 700,000
            // 수익률: (700,000 - 1,000,000) / 1,000,000 * 100 = -30%
            assertThat(returnPercentage).isEqualByComparingTo(BigDecimal.valueOf(-30.0000));
            assertThat(session.getReturnRate()).isEqualByComparingTo(BigDecimal.valueOf(-30.0000));
        }

        @Test
        @DisplayName("수익률 계산 - 손익 없음")
        void calculateReturnPercentageWithNoChange() {
            // Given
            ChallengeSession session = createSessionWithBalance(BigDecimal.valueOf(1000000), BigDecimal.valueOf(700000));
            BigDecimal portfolioValue = BigDecimal.valueOf(300000);

            // When
            BigDecimal returnPercentage = session.calculateReturnPercentage(portfolioValue);

            // Then
            // 총 가치: 300,000 + 700,000 = 1,000,000
            // 수익률: (1,000,000 - 1,000,000) / 1,000,000 * 100 = 0%
            assertThat(returnPercentage).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusCheckMethodsTest {

        @Test
        @DisplayName("READY 상태에서만 시작 가능")
        void canStartOnlyWhenReady() {
            // Given
            ChallengeSession readySession = createSession(ChallengeSession.SessionStatus.READY);
            ChallengeSession activeSession = createSession(ChallengeSession.SessionStatus.ACTIVE);

            // When & Then
            assertThat(readySession.canStart()).isTrue();
            assertThat(activeSession.canStart()).isFalse();
        }

        @Test
        @DisplayName("ACTIVE 상태에서만 활성")
        void isActiveOnlyWhenActive() {
            // Given
            ChallengeSession activeSession = createSession(ChallengeSession.SessionStatus.ACTIVE);
            ChallengeSession readySession = createSession(ChallengeSession.SessionStatus.READY);

            // When & Then
            assertThat(activeSession.isActive()).isTrue();
            assertThat(readySession.isActive()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"COMPLETED", "CANCELLED", "ENDED"})
        @DisplayName("완료 상태들에서는 완료됨으로 표시")
        void isCompletedWhenInCompletedStates(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When & Then
            assertThat(session.isCompleted()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"READY", "ACTIVE"})
        @DisplayName("진행 중 상태들에서는 완료되지 않음으로 표시")
        void isNotCompletedWhenInProgressStates(String statusName) {
            // Given
            ChallengeSession.SessionStatus status = ChallengeSession.SessionStatus.valueOf(statusName);
            ChallengeSession session = createSession(status);

            // When & Then
            assertThat(session.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("SessionStatus enum의 canStartNewSession 메서드 테스트")
        void sessionStatusCanStartNewSessionMethod() {
            // When & Then
            assertThat(ChallengeSession.SessionStatus.COMPLETED.canStartNewSession()).isTrue();
            assertThat(ChallengeSession.SessionStatus.CANCELLED.canStartNewSession()).isTrue();
            assertThat(ChallengeSession.SessionStatus.ENDED.canStartNewSession()).isTrue();
            assertThat(ChallengeSession.SessionStatus.READY.canStartNewSession()).isFalse();
            assertThat(ChallengeSession.SessionStatus.ACTIVE.canStartNewSession()).isFalse();
        }
    }

    // 테스트 헬퍼 메서드들
    private ChallengeSession createSession(ChallengeSession.SessionStatus status) {
        ChallengeSession session = ChallengeSession.builder()
                .id(1L)
                .challengeId(100L)
                .userId(200L)
                .initialBalance(BigDecimal.valueOf(1000000))
                .currentBalance(BigDecimal.valueOf(1000000))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        // 상태에 따른 추가 설정
        if (status == ChallengeSession.SessionStatus.ACTIVE) {
            session = session.toBuilder()
                    .startedAt(LocalDateTime.now())
                    .build();
        } else if (status == ChallengeSession.SessionStatus.COMPLETED ||
                   status == ChallengeSession.SessionStatus.CANCELLED) {
            session = session.toBuilder()
                    .startedAt(LocalDateTime.now().minusHours(1))
                    .completedAt(LocalDateTime.now())
                    .build();
        }

        return session;
    }

    private ChallengeSession createSessionWithBalance(BigDecimal initialBalance, BigDecimal currentBalance) {
        return ChallengeSession.builder()
                .id(1L)
                .challengeId(100L)
                .userId(200L)
                .initialBalance(initialBalance)
                .currentBalance(currentBalance)
                .status(ChallengeSession.SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
    }
}