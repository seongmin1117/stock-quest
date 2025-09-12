package com.stockquest.integration;

import com.stockquest.application.order.PlaceOrderService;
import com.stockquest.application.order.port.in.PlaceOrderCommand;
import com.stockquest.application.order.port.in.PlaceOrderResult;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderType;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import com.stockquest.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA 주문 처리 통합 테스트
 * 데이터베이스 스키마 수정 후 주문 처리가 정상 작동하는지 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("주문 처리 JPA 통합 테스트")
class OrderPlacementJpaTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeSessionRepository sessionRepository;

    private User testUser;
    private Challenge testChallenge;
    private ChallengeSession testSession;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetCounters();
        
        // 테스트 사용자 생성
        testUser = TestDataFactory.createUser("test@order.com", "Order Test User");
        testUser = userRepository.save(testUser);
        
        // 테스트 챌린지 생성  
        testChallenge = TestDataFactory.createActiveChallenge();
        testChallenge = challengeRepository.save(testChallenge);
        
        // 테스트 세션 생성
        testSession = new ChallengeSession(
            testChallenge.getId(), 
            testUser.getId(), 
            BigDecimal.valueOf(1000000) // 100만원
        );
        testSession.start();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("실제 티커 심볼(AAPL)로 주문 처리 - JPA 저장 성공")
    @Transactional
    void shouldSuccessfullyProcessOrderWithRealTickerSymbol() {
        // Given: AAPL 주식 매수 주문
        PlaceOrderCommand command = new PlaceOrderCommand(
            testSession.getId(),
            "AAPL",  // 4글자 티커 심볼 - 이전에는 CHAR(1) 제약으로 실패
            OrderSide.BUY,
            BigDecimal.valueOf(10), // 10주
            OrderType.MARKET,
            null // 시장가 주문
        );

        // When: 주문 실행
        PlaceOrderResult result = placeOrderService.placeOrder(command);

        // Then: 주문이 성공적으로 처리됨
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        assertThat(result.instrumentKey()).isEqualTo("AAPL");
        assertThat(result.side()).isEqualTo(OrderSide.BUY);
        assertThat(result.quantity()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(result.executedPrice()).isPositive();
        assertThat(result.remainingBalance()).isLessThan(testSession.getCurrentBalance());
    }

    @Test
    @DisplayName("다양한 길이의 티커 심볼들 처리 테스트")
    @Transactional
    void shouldHandleVariousTickerSymbolLengths() {
        // Given: 다양한 길이의 티커 심볼들
        String[] tickerSymbols = {"A", "BT", "IBM", "AAPL", "GOOGL", "BERKSHIREA"};

        for (String ticker : tickerSymbols) {
            // When: 각 티커로 주문 실행
            PlaceOrderCommand command = new PlaceOrderCommand(
                testSession.getId(),
                ticker,
                OrderSide.BUY,
                BigDecimal.valueOf(1), // 1주씩
                OrderType.MARKET,
                null
            );

            PlaceOrderResult result = placeOrderService.placeOrder(command);

            // Then: 모든 티커 길이에 대해 성공적으로 처리됨
            assertThat(result).isNotNull();
            assertThat(result.instrumentKey()).isEqualTo(ticker);
            assertThat(result.orderId()).isNotNull();
        }
    }

    @Test
    @DisplayName("10글자 초과 티커 심볼 처리 - 적절한 예외 발생")
    @Transactional
    void shouldRejectTooLongTickerSymbol() {
        // Given: 10글자 초과하는 티커 심볼 (데이터베이스 제약 초과)
        PlaceOrderCommand command = new PlaceOrderCommand(
            testSession.getId(),
            "TOOLONGTICKERNAMEXYZ", // 20글자 - VARCHAR(10) 제약 초과
            OrderSide.BUY,
            BigDecimal.valueOf(1),
            OrderType.MARKET,
            null
        );

        // When & Then: 데이터베이스 제약 위반으로 예외 발생
        assertThatThrownBy(() -> placeOrderService.placeOrder(command))
            .isInstanceOf(Exception.class);
            // 구체적인 예외 타입은 JPA 구현에 따라 다를 수 있음 
            // (DataAccessException, ConstraintViolationException 등)
    }

    @Test
    @DisplayName("정밀도 테스트 - 높은 정밀도의 가격과 수량 처리")
    @Transactional
    void shouldHandleHighPrecisionPricesAndQuantities() {
        // Given: 높은 정밀도의 수량과 지정가
        PlaceOrderCommand command = new PlaceOrderCommand(
            testSession.getId(),
            "TSLA",
            OrderSide.BUY,
            BigDecimal.valueOf(15.2539), // DECIMAL(15,4) 정밀도
            OrderType.LIMIT,
            BigDecimal.valueOf(250.7834) // DECIMAL(12,4) 정밀도
        );

        // When: 주문 실행
        PlaceOrderResult result = placeOrderService.placeOrder(command);

        // Then: 높은 정밀도 값들이 올바르게 처리됨
        assertThat(result).isNotNull();
        assertThat(result.instrumentKey()).isEqualTo("TSLA");
        assertThat(result.quantity()).isEqualTo(BigDecimal.valueOf(15.2539));
        // executed_price는 시장가 적용으로 limit_price와 다를 수 있음
        assertThat(result.executedPrice()).isPositive();
    }

    @Test
    @DisplayName("동일 세션에서 같은 종목 중복 주문 처리 - 포트폴리오 업데이트")
    @Transactional
    void shouldHandleMultipleOrdersSameInstrument() {
        // Given: 첫 번째 주문
        PlaceOrderCommand firstOrder = new PlaceOrderCommand(
            testSession.getId(),
            "MSFT",
            OrderSide.BUY,
            BigDecimal.valueOf(5),
            OrderType.MARKET,
            null
        );

        PlaceOrderResult firstResult = placeOrderService.placeOrder(firstOrder);
        BigDecimal balanceAfterFirst = firstResult.remainingBalance();

        // When: 같은 종목 추가 주문
        PlaceOrderCommand secondOrder = new PlaceOrderCommand(
            testSession.getId(),
            "MSFT",
            OrderSide.BUY,
            BigDecimal.valueOf(3),
            OrderType.MARKET,
            null
        );

        PlaceOrderResult secondResult = placeOrderService.placeOrder(secondOrder);

        // Then: 두 주문 모두 성공적으로 처리됨
        assertThat(firstResult.instrumentKey()).isEqualTo("MSFT");
        assertThat(secondResult.instrumentKey()).isEqualTo("MSFT");
        assertThat(secondResult.remainingBalance()).isLessThan(balanceAfterFirst);
        
        // 포트폴리오에서 포지션이 적절히 합쳐져야 함 (총 8주)
        // 이는 포트폴리오 서비스를 통해 별도로 확인 가능
    }

    @Test
    @DisplayName("세션 상태 검증 - 비활성 세션에서 주문 거부")
    @Transactional  
    void shouldRejectOrderFromInactiveSession() {
        // Given: 세션 종료
        testSession.end();
        sessionRepository.save(testSession);

        PlaceOrderCommand command = new PlaceOrderCommand(
            testSession.getId(),
            "NVDA",
            OrderSide.BUY,
            BigDecimal.valueOf(5),
            OrderType.MARKET,
            null
        );

        // When & Then: 비활성 세션에서 주문 시 예외 발생
        assertThatThrownBy(() -> placeOrderService.placeOrder(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("활성 상태의 세션에서만 주문할 수 있습니다");
    }
}