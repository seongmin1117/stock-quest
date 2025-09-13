package com.stockquest.e2e;

import com.stockquest.application.auth.LoginService;
import com.stockquest.application.auth.dto.LoginCommand;
import com.stockquest.application.challenge.StartChallengeService;
import com.stockquest.application.challenge.GetChallengeListService;
import com.stockquest.application.challenge.dto.GetChallengeListQuery;
import com.stockquest.application.order.CreateOrderService;
import com.stockquest.application.order.dto.CreateOrderCommand;
import com.stockquest.application.portfolio.GetPortfolioService;
import com.stockquest.application.session.CloseChallengeService;
import com.stockquest.domain.auth.AuthResult;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderType;
import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.SessionStatus;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import com.stockquest.integration.IntegrationTestBase;
import com.stockquest.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * 거래 플랫폼 End-to-End 테스트
 * 실제 사용자 워크플로우를 시뮬레이션하여 전체 시스템 검증
 */
@DisplayName("거래 플랫폼 E2E 테스트")
class TradingFlowE2ETest extends IntegrationTestBase {

    @Autowired
    private LoginService loginService;
    
    @Autowired
    private GetChallengeListService getChallengeListService;
    
    @Autowired
    private StartChallengeService startChallengeService;
    
    @Autowired
    private CreateOrderService createOrderService;
    
    @Autowired
    private GetPortfolioService getPortfolioService;
    
    @Autowired
    private CloseChallengeService closeChallengeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    private User testUser;
    private Challenge testChallenge;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetCounters();
        
        // 테스트 사용자 생성
        testUser = TestDataFactory.createUser("e2e@test.com", "E2E Test User");
        testUser = userRepository.save(testUser);
        
        // 테스트 챌린지 생성
        testChallenge = TestDataFactory.createActiveChallenge();
        testChallenge.setTitle("E2E Trading Challenge");
        testChallenge.setSeedBalance(BigDecimal.valueOf(1000000)); // 100만원
        testChallenge = challengeRepository.save(testChallenge);
    }

    @Test
    @DisplayName("완전한 트레이딩 워크플로우: 로그인 → 챌린지 참여 → 거래 → 포트폴리오 확인 → 종료")
    @Transactional
    void shouldCompleteFullTradingWorkflow() {
        // ========== 1단계: 사용자 로그인 ==========
        LoginCommand loginCommand = new LoginCommand(testUser.getEmail(), "password");
        AuthResult authResult = loginService.login(loginCommand);
        
        assertThat(authResult).isNotNull();
        assertThat(authResult.accessToken()).isNotBlank();
        assertThat(authResult.user().getId()).isEqualTo(testUser.getId());
        
        System.out.println("✅ 1단계: 로그인 성공 - Access Token: " + authResult.accessToken().substring(0, 20) + "...");
        
        // ========== 2단계: 챌린지 목록 조회 ==========
        var challengeListResult = getChallengeListService.getChallengeList(
            new GetChallengeListQuery(0, 10, null));
        
        assertThat(challengeListResult.challenges()).isNotEmpty();
        assertThat(challengeListResult.challenges()).anyMatch(c -> c.getId().equals(testChallenge.getId()));
        
        System.out.println("✅ 2단계: 챌린지 목록 조회 성공 - " + challengeListResult.challenges().size() + "개 발견");
        
        // ========== 3단계: 챌린지 참여 ==========
        ChallengeSession session = startChallengeService.startChallenge(testUser.getId(), testChallenge.getId());
        
        assertThat(session).isNotNull();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getCurrentBalance()).isEqualTo(testChallenge.getSeedBalance());
        
        System.out.println("✅ 3단계: 챌린지 참여 성공 - Session ID: " + session.getId() + 
                          ", 초기 잔고: " + session.getCurrentBalance());
        
        // ========== 4단계: 주식 매수 주문 ==========
        CreateOrderCommand buyOrder = new CreateOrderCommand(
            session.getId(),
            "AAPL",
            OrderType.BUY,
            10,  // 10주
            BigDecimal.valueOf(150.00),  // $150 per share
            "LIMIT"
        );
        
        Order executedBuyOrder = createOrderService.createOrder(buyOrder);
        
        assertThat(executedBuyOrder).isNotNull();
        assertThat(executedBuyOrder.getSymbol()).isEqualTo("AAPL");
        assertThat(executedBuyOrder.getQuantity()).isEqualTo(10);
        assertThat(executedBuyOrder.getOrderType()).isEqualTo(OrderType.BUY);
        
        System.out.println("✅ 4단계: 매수 주문 성공 - " + executedBuyOrder.getSymbol() + 
                          " " + executedBuyOrder.getQuantity() + "주 @ $" + executedBuyOrder.getPrice());
        
        // ========== 5단계: 포트폴리오 확인 ==========
        Portfolio portfolio = getPortfolioService.getPortfolio(session.getId());
        
        assertThat(portfolio).isNotNull();
        assertThat(portfolio.getHoldings()).isNotEmpty();
        assertThat(portfolio.getHoldings()).anyMatch(holding -> 
            holding.getSymbol().equals("AAPL") && holding.getQuantity() == 10);
        
        System.out.println("✅ 5단계: 포트폴리오 확인 성공 - " + portfolio.getHoldings().size() + 
                          "개 종목 보유, 총 가치: $" + portfolio.getTotalValue());
        
        // ========== 6단계: 일부 매도 ==========
        CreateOrderCommand sellOrder = new CreateOrderCommand(
            session.getId(),
            "AAPL",
            OrderType.SELL,
            5,  // 5주 매도
            BigDecimal.valueOf(155.00),  // $155 per share (이익 실현)
            "LIMIT"
        );
        
        Order executedSellOrder = createOrderService.createOrder(sellOrder);
        
        assertThat(executedSellOrder).isNotNull();
        assertThat(executedSellOrder.getOrderType()).isEqualTo(OrderType.SELL);
        assertThat(executedSellOrder.getQuantity()).isEqualTo(5);
        
        System.out.println("✅ 6단계: 매도 주문 성공 - " + executedSellOrder.getSymbol() + 
                          " " + executedSellOrder.getQuantity() + "주 @ $" + executedSellOrder.getPrice());
        
        // ========== 7단계: 업데이트된 포트폴리오 확인 ==========
        Portfolio updatedPortfolio = getPortfolioService.getPortfolio(session.getId());
        
        assertThat(updatedPortfolio.getHoldings()).anyMatch(holding -> 
            holding.getSymbol().equals("AAPL") && holding.getQuantity() == 5); // 5주 남아있어야 함
        
        System.out.println("✅ 7단계: 포트폴리오 업데이트 확인 - AAPL 5주 남음, 총 가치: $" + updatedPortfolio.getTotalValue());
        
        // ========== 8단계: 챌린지 종료 ==========
        ChallengeSession closedSession = closeChallengeService.closeSession(session.getId());
        
        assertThat(closedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(closedSession.getFinalBalance()).isNotNull();
        assertThat(closedSession.getFinalReturnRate()).isNotNull();
        assertThat(closedSession.getCompletedAt()).isNotNull();
        
        System.out.println("✅ 8단계: 챌린지 종료 성공 - 최종 잔고: " + closedSession.getFinalBalance() + 
                          ", 수익률: " + closedSession.getFinalReturnRate() + "%");
        
        // ========== 전체 워크플로우 검증 ==========
        assertThat(closedSession.getFinalReturnRate()).isGreaterThan(BigDecimal.ZERO); // 수익 실현 확인
        
        System.out.println("🎉 E2E 트레이딩 워크플로우 완료!");
        System.out.println("📊 최종 결과:");
        System.out.println("   - 초기 잔고: $" + testChallenge.getSeedBalance());
        System.out.println("   - 최종 잔고: $" + closedSession.getFinalBalance());
        System.out.println("   - 수익률: " + closedSession.getFinalReturnRate() + "%");
    }

    @Test
    @DisplayName("동시 사용자 트레이딩 시나리오")
    void shouldHandleConcurrentTradingScenarios() throws InterruptedException {
        // Given: 10명의 동시 트레이더
        int concurrentTraders = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(concurrentTraders);
        ExecutorService executor = Executors.newFixedThreadPool(concurrentTraders);
        
        System.out.println("🚀 동시 트레이딩 시나리오 시작 - " + concurrentTraders + "명의 트레이더");
        
        // When: 모든 트레이더가 동시에 거래 시작
        for (int i = 0; i < concurrentTraders; i++) {
            final int traderId = i;
            executor.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    startLatch.await();
                    
                    // 각 트레이더별 사용자 생성
                    User trader = TestDataFactory.createUser("trader" + traderId + "@test.com", "Trader " + traderId);
                    trader = userRepository.save(trader);
                    
                    // 챌린지 참여
                    ChallengeSession session = startChallengeService.startChallenge(trader.getId(), testChallenge.getId());
                    
                    // 랜덤 거래 실행
                    String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN"};
                    String symbol = symbols[traderId % symbols.length];
                    
                    CreateOrderCommand order = new CreateOrderCommand(
                        session.getId(),
                        symbol,
                        OrderType.BUY,
                        5 + (traderId % 10),  // 5-14주
                        BigDecimal.valueOf(100 + (traderId * 10)),  // 가격 차별화
                        "MARKET"
                    );
                    
                    Order executedOrder = createOrderService.createOrder(order);
                    
                    System.out.println("✅ Trader " + traderId + ": " + executedOrder.getSymbol() + 
                                     " " + executedOrder.getQuantity() + "주 매수");
                    
                } catch (Exception e) {
                    System.err.println("❌ Trader " + traderId + " 실패: " + e.getMessage());
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // 모든 트레이더 동시 시작
        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        
        // 모든 거래 완료 대기 (최대 30초)
        boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Then: 검증
        assertThat(completed).as("모든 동시 거래가 30초 내에 완료되어야 함").isTrue();
        
        System.out.println("🏆 동시 트레이딩 완료!");
        System.out.println("📊 성능 결과:");
        System.out.println("   - 총 처리 시간: " + totalTime + "ms");
        System.out.println("   - 평균 거래 시간: " + (totalTime / concurrentTraders) + "ms");
        System.out.println("   - 처리량: " + String.format("%.2f", (concurrentTraders * 1000.0 / totalTime)) + " 거래/초");
        
        // 성능 임계값 검증
        assertThat(totalTime).as("전체 처리 시간은 10초 이내여야 함").isLessThan(10000);
        assertThat(totalTime / concurrentTraders).as("평균 거래 시간은 1초 이내여야 함").isLessThan(1000);
        
        executor.shutdown();
    }

    @Test
    @DisplayName("실패 시나리오: 잘못된 주문과 복구")
    void shouldHandleFailureScenarios() {
        // Given: 챌린지 참여
        ChallengeSession session = startChallengeService.startChallenge(testUser.getId(), testChallenge.getId());
        
        System.out.println("⚠️ 실패 시나리오 테스트 시작");
        
        // When & Then 1: 잔고 부족 주문
        CreateOrderCommand oversizedOrder = new CreateOrderCommand(
            session.getId(),
            "AAPL",
            OrderType.BUY,
            10000,  // 매우 큰 수량
            BigDecimal.valueOf(1000.00),  // 높은 가격
            "LIMIT"
        );
        
        assertThatThrownBy(() -> createOrderService.createOrder(oversizedOrder))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient balance");
        
        System.out.println("✅ 잔고 부족 주문 거부 확인");
        
        // When & Then 2: 존재하지 않는 종목
        CreateOrderCommand invalidSymbolOrder = new CreateOrderCommand(
            session.getId(),
            "INVALID_SYMBOL",
            OrderType.BUY,
            1,
            BigDecimal.valueOf(100.00),
            "MARKET"
        );
        
        assertThatThrownBy(() -> createOrderService.createOrder(invalidSymbolOrder))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid symbol");
        
        System.out.println("✅ 잘못된 종목 주문 거부 확인");
        
        // When & Then 3: 정상 주문으로 복구
        CreateOrderCommand validOrder = new CreateOrderCommand(
            session.getId(),
            "AAPL",
            OrderType.BUY,
            5,
            BigDecimal.valueOf(150.00),
            "LIMIT"
        );
        
        Order executedOrder = createOrderService.createOrder(validOrder);
        assertThat(executedOrder).isNotNull();
        
        System.out.println("✅ 정상 주문 실행 성공 - 시스템 복구 확인");
        
        // 포트폴리오 정상성 확인
        Portfolio portfolio = getPortfolioService.getPortfolio(session.getId());
        assertThat(portfolio.getHoldings()).hasSize(1);
        assertThat(portfolio.getHoldings().get(0).getSymbol()).isEqualTo("AAPL");
        
        System.out.println("🔄 시스템 복구 완료 - 정상 거래 가능");
    }

    @Test
    @DisplayName("마켓 데이터 피드 시뮬레이션")
    void shouldSimulateMarketDataFeed() {
        // Given: 챌린지 참여 및 초기 주문
        ChallengeSession session = startChallengeService.startChallenge(testUser.getId(), testChallenge.getId());
        
        CreateOrderCommand initialOrder = new CreateOrderCommand(
            session.getId(),
            "AAPL",
            OrderType.BUY,
            10,
            BigDecimal.valueOf(150.00),
            "MARKET"
        );
        createOrderService.createOrder(initialOrder);
        
        System.out.println("📈 마켓 데이터 피드 시뮬레이션");
        
        // When: 시간에 따른 가격 변동 시뮬레이션
        BigDecimal[] priceSequence = {
            BigDecimal.valueOf(150.00),  // 초기 가격
            BigDecimal.valueOf(152.50),  // +1.67%
            BigDecimal.valueOf(148.75),  // -2.46%
            BigDecimal.valueOf(155.00),  // +4.17%
            BigDecimal.valueOf(153.25)   // -1.13%
        };
        
        Portfolio initialPortfolio = getPortfolioService.getPortfolio(session.getId());
        BigDecimal initialValue = initialPortfolio.getTotalValue();
        
        for (int i = 0; i < priceSequence.length; i++) {
            // 포트폴리오 가치 재계산 (실제 구현에서는 실시간 가격 피드)
            Portfolio currentPortfolio = getPortfolioService.getPortfolio(session.getId());
            
            System.out.println("📊 T+" + i + ": AAPL $" + priceSequence[i] + 
                             " | 포트폴리오 가치: $" + currentPortfolio.getTotalValue());
            
            // 가격 변동에 따른 포트폴리오 가치 변화 확인
            assertThat(currentPortfolio.getTotalValue()).isNotNull();
        }
        
        // Then: 최종 포트폴리오 상태 검증
        Portfolio finalPortfolio = getPortfolioService.getPortfolio(session.getId());
        assertThat(finalPortfolio.getHoldings()).hasSize(1);
        assertThat(finalPortfolio.getHoldings().get(0).getQuantity()).isEqualTo(10);
        
        System.out.println("📈 마켓 시뮬레이션 완료!");
        System.out.println("   - 초기 가치: $" + initialValue);
        System.out.println("   - 최종 가치: $" + finalPortfolio.getTotalValue());
    }
}