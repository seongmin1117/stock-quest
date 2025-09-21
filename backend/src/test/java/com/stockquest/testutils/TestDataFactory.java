package com.stockquest.testutils;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.session.ChallengeSession;
// SessionStatus는 ChallengeSession의 중첩 enum으로 사용
import com.stockquest.domain.user.User;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.OrderSide;
import com.stockquest.domain.order.OrderStatus;
import com.stockquest.domain.order.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 테스트 데이터 팩토리
 * 일관성 있고 재사용 가능한 테스트 데이터 생성
 */
public class TestDataFactory {
    
    private static long userIdCounter = 1L;
    private static long challengeIdCounter = 1L;
    private static long sessionIdCounter = 1L;
    private static long orderIdCounter = 1L;
    
    // ========== User Test Data ==========
    
    /**
     * 기본 사용자 생성
     */
    public static User createUser() {
        return createUser("test" + userIdCounter + "@example.com", "Test User " + userIdCounter++);
    }
    
    /**
     * 커스텀 사용자 생성
     */
    public static User createUser(String email, String name) {
        User user = new User();
        user.setId(userIdCounter++);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("$2a$12$encrypted.password.hash");
        user.setCreatedAt(LocalDateTime.now().minusDays(30));
        return user;
    }
    
    /**
     * 여러 사용자 생성
     */
    public static List<User> createUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createUser())
                .toList();
    }
    
    // ========== Challenge Test Data ==========
    
    /**
     * 기본 챌린지 생성
     */
    public static Challenge createChallenge() {
        return createChallenge("Test Challenge " + challengeIdCounter, ChallengeDifficulty.BEGINNER);
    }
    
    /**
     * 커스텀 챌린지 생성
     */
    public static Challenge createChallenge(String title, ChallengeDifficulty difficulty) {
        Challenge challenge = new Challenge();
        challenge.setId(challengeIdCounter++);
        challenge.setTitle(title);
        challenge.setDescription("Test challenge description for " + title);
        challenge.setDifficulty(difficulty);
        challenge.setStatus(ChallengeStatus.ACTIVE);
        challenge.setSeedBalance(BigDecimal.valueOf(1000000)); // 100만원
        challenge.setStartDate(LocalDateTime.now().minusDays(1));
        challenge.setEndDate(LocalDateTime.now().plusDays(30));
        challenge.setMaxParticipants(100);
        challenge.setCurrentParticipants(0);
        challenge.setCreatedAt(LocalDateTime.now().minusDays(2));
        return challenge;
    }
    
    /**
     * 활성 챌린지 생성
     */
    public static Challenge createActiveChallenge() {
        Challenge challenge = createChallenge();
        challenge.setStatus(ChallengeStatus.ACTIVE);
        challenge.setStartDate(LocalDateTime.now().minusHours(1));
        challenge.setEndDate(LocalDateTime.now().plusDays(7));
        return challenge;
    }
    
    /**
     * 완료된 챌린지 생성
     */
    public static Challenge createCompletedChallenge() {
        Challenge challenge = createChallenge();
        challenge.setStatus(ChallengeStatus.COMPLETED);
        challenge.setStartDate(LocalDateTime.now().minusDays(30));
        challenge.setEndDate(LocalDateTime.now().minusDays(1));
        return challenge;
    }
    
    // ========== Session Test Data ==========
    
    /**
     * 기본 세션 생성
     */
    public static ChallengeSession createSession(Long userId, Long challengeId) {
        ChallengeSession session = new ChallengeSession();
        session.setId(sessionIdCounter++);
        session.setUserId(userId);
        session.setChallengeId(challengeId);
        session.setStatus(ChallengeSession.SessionStatus.ACTIVE);
        session.setInitialBalance(BigDecimal.valueOf(1000000));
        session.setCurrentBalance(BigDecimal.valueOf(1000000));
        session.setStartedAt(LocalDateTime.now().minusHours(2));
        session.setCreatedAt(LocalDateTime.now().minusHours(2));
        return session;
    }
    
    /**
     * 활성 세션 생성
     */
    public static ChallengeSession createActiveSession(Long userId, Long challengeId) {
        ChallengeSession session = createSession(userId, challengeId);
        session.setStatus(ChallengeSession.SessionStatus.ACTIVE);
        session.setCurrentBalance(BigDecimal.valueOf(1200000)); // 20% 수익
        return session;
    }
    
    /**
     * 완료된 세션 생성 (수익)
     */
    public static ChallengeSession createCompletedSessionWithProfit(Long userId, Long challengeId) {
        ChallengeSession session = createSession(userId, challengeId);
        session.setStatus(ChallengeSession.SessionStatus.COMPLETED);
        session.setFinalBalance(BigDecimal.valueOf(1300000)); // 30% 수익
        session.setFinalReturnRate(BigDecimal.valueOf(0.30));
        session.setCompletedAt(LocalDateTime.now().minusHours(1));
        return session;
    }
    
    /**
     * 완료된 세션 생성 (손실)
     */
    public static ChallengeSession createCompletedSessionWithLoss(Long userId, Long challengeId) {
        ChallengeSession session = createSession(userId, challengeId);
        session.setStatus(ChallengeSession.SessionStatus.COMPLETED);
        session.setFinalBalance(BigDecimal.valueOf(850000)); // -15% 손실
        session.setFinalReturnRate(BigDecimal.valueOf(-0.15));
        session.setCompletedAt(LocalDateTime.now().minusHours(1));
        return session;
    }
    
    // ========== Order Test Data ==========
    
    /**
     * 매수 주문 생성
     */
    public static Order createBuyOrder(Long sessionId, String ticker) {
        return createOrder(sessionId, ticker, OrderSide.BUY, OrderType.MARKET, 10, BigDecimal.valueOf(150.00));
    }
    
    /**
     * 매도 주문 생성
     */
    public static Order createSellOrder(Long sessionId, String ticker) {
        return createOrder(sessionId, ticker, OrderSide.SELL, OrderType.MARKET, 5, BigDecimal.valueOf(155.00));
    }
    
    /**
     * 커스텀 주문 생성
     */
    public static Order createOrder(Long sessionId, String ticker, OrderSide side, OrderType type, 
                                  Integer quantity, BigDecimal price) {
        Order order = new Order();
        order.setId(orderIdCounter++);
        order.setSessionId(sessionId);
        order.setTicker(ticker);
        order.setSide(side);
        order.setType(type);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setStatus(OrderStatus.FILLED);
        order.setFilledQuantity(quantity);
        order.setFilledPrice(price);
        order.setTotalAmount(price.multiply(BigDecimal.valueOf(quantity)));
        order.setCommission(price.multiply(BigDecimal.valueOf(quantity)).multiply(BigDecimal.valueOf(0.0025)));
        order.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        order.setFilledAt(LocalDateTime.now().minusMinutes(29));
        return order;
    }
    
    /**
     * 대기 중인 주문 생성
     */
    public static Order createPendingOrder(Long sessionId, String ticker) {
        Order order = createOrder(sessionId, ticker, OrderSide.BUY, OrderType.LIMIT, 20, BigDecimal.valueOf(140.00));
        order.setStatus(OrderStatus.PENDING);
        order.setFilledQuantity(0);
        order.setFilledPrice(null);
        order.setFilledAt(null);
        return order;
    }
    
    /**
     * 취소된 주문 생성
     */
    public static Order createCancelledOrder(Long sessionId, String ticker) {
        Order order = createOrder(sessionId, ticker, OrderSide.SELL, OrderType.LIMIT, 15, BigDecimal.valueOf(160.00));
        order.setStatus(OrderStatus.CANCELLED);
        order.setFilledQuantity(0);
        order.setFilledPrice(null);
        order.setFilledAt(null);
        order.setCancelledAt(LocalDateTime.now().minusMinutes(5));
        return order;
    }
    
    // ========== 복합 테스트 시나리오 ==========
    
    /**
     * 완전한 거래 시나리오 생성 (사용자 + 챌린지 + 세션 + 주문)
     */
    public static CompleteScenario createCompleteScenario() {
        User user = createUser();
        Challenge challenge = createActiveChallenge();
        ChallengeSession session = createActiveSession(user.getId(), challenge.getId());
        
        List<Order> orders = List.of(
            createBuyOrder(session.getId(), "AAPL"),
            createBuyOrder(session.getId(), "GOOGL"),
            createSellOrder(session.getId(), "AAPL")
        );
        
        return new CompleteScenario(user, challenge, session, orders);
    }
    
    /**
     * 리더보드 테스트용 시나리오 (여러 사용자 + 성과 데이터)
     */
    public static LeaderboardScenario createLeaderboardScenario(int userCount) {
        Challenge challenge = createActiveChallenge();
        List<User> users = createUsers(userCount);
        
        List<ChallengeSession> sessions = users.stream()
                .map(user -> {
                    // 무작위 성과로 세션 생성
                    double randomReturn = (Math.random() - 0.5) * 0.6; // -30% ~ +30%
                    BigDecimal finalBalance = BigDecimal.valueOf(1000000 * (1 + randomReturn));
                    
                    ChallengeSession session = createCompletedSessionWithProfit(user.getId(), challenge.getId());
                    session.setFinalBalance(finalBalance);
                    session.setFinalReturnRate(BigDecimal.valueOf(randomReturn));
                    return session;
                })
                .toList();
        
        return new LeaderboardScenario(challenge, users, sessions);
    }
    
    // ========== 테스트 시나리오 데이터 클래스 ==========
    
    public static class CompleteScenario {
        private final User user;
        private final Challenge challenge;
        private final ChallengeSession session;
        private final List<Order> orders;
        
        public CompleteScenario(User user, Challenge challenge, ChallengeSession session, List<Order> orders) {
            this.user = user;
            this.challenge = challenge;
            this.session = session;
            this.orders = orders;
        }
        
        // Getters
        public User getUser() { return user; }
        public Challenge getChallenge() { return challenge; }
        public ChallengeSession getSession() { return session; }
        public List<Order> getOrders() { return orders; }
    }
    
    public static class LeaderboardScenario {
        private final Challenge challenge;
        private final List<User> users;
        private final List<ChallengeSession> sessions;
        
        public LeaderboardScenario(Challenge challenge, List<User> users, List<ChallengeSession> sessions) {
            this.challenge = challenge;
            this.users = users;
            this.sessions = sessions;
        }
        
        // Getters
        public Challenge getChallenge() { return challenge; }
        public List<User> getUsers() { return users; }
        public List<ChallengeSession> getSessions() { return sessions; }
    }
    
    // ========== 유틸리티 메소드 ==========
    
    /**
     * ID 카운터 리셋 (테스트 격리를 위해)
     */
    public static void resetCounters() {
        userIdCounter = 1L;
        challengeIdCounter = 1L;
        sessionIdCounter = 1L;
        orderIdCounter = 1L;
    }
}