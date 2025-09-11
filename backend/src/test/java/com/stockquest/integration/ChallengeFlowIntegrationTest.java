package com.stockquest.integration;

import com.stockquest.application.challenge.StartChallengeService;
import com.stockquest.application.challenge.GetChallengeDetailService;
import com.stockquest.application.session.CloseChallengeService;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 챌린지 전체 플로우 통합 테스트
 * 실제 DB 환경에서 전체 시나리오 테스트
 */
@DisplayName("챌린지 플로우 통합 테스트")
@Transactional
class ChallengeFlowIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private StartChallengeService startChallengeService;
    
    @Autowired
    private GetChallengeDetailService getChallengeDetailService;
    
    @Autowired
    private CloseChallengeService closeChallengeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private ChallengeSessionRepository sessionRepository;
    
    private User testUser;
    private Challenge testChallenge;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        TestDataFactory.resetCounters();
        
        // 테스트 사용자 생성 및 저장
        testUser = TestDataFactory.createUser("integration@test.com", "Integration Test User");
        testUser = userRepository.save(testUser);
        
        // 테스트 챌린지 생성 및 저장
        testChallenge = TestDataFactory.createActiveChallenge();
        testChallenge.setTitle("Integration Test Challenge");
        testChallenge.setMaxParticipants(10);
        testChallenge.setCurrentParticipants(0);
        testChallenge = challengeRepository.save(testChallenge);
    }
    
    @Test
    @DisplayName("완전한 챌린지 플로우: 시작 -> 진행 -> 종료")
    void shouldCompleteFullChallengeFlow() {
        // Given: 준비된 사용자와 챌린지
        assertThat(testUser.getId()).isNotNull();
        assertThat(testChallenge.getId()).isNotNull();
        
        // When 1: 챌린지 시작
        ChallengeSession session = startChallengeService.startChallenge(testUser.getId(), testChallenge.getId());
        
        // Then 1: 세션이 정상적으로 생성됨
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(testUser.getId());
        assertThat(session.getChallengeId()).isEqualTo(testChallenge.getId());
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getInitialBalance()).isEqualTo(testChallenge.getSeedBalance());
        assertThat(session.getCurrentBalance()).isEqualTo(testChallenge.getSeedBalance());
        assertThat(session.getStartedAt()).isNotNull();
        
        // When 2: 데이터베이스에서 세션 조회
        ChallengeSession savedSession = sessionRepository.findById(session.getId()).orElse(null);
        
        // Then 2: 세션이 올바르게 저장됨
        assertThat(savedSession).isNotNull();
        assertThat(savedSession.getUserId()).isEqualTo(testUser.getId());
        
        // When 3: 챌린지 상세 정보 조회
        Challenge challengeDetail = getChallengeDetailService.getChallengeDetail(testChallenge.getId());
        
        // Then 3: 챌린지 정보가 정확함
        assertThat(challengeDetail).isNotNull();
        assertThat(challengeDetail.getId()).isEqualTo(testChallenge.getId());
        
        // When 4: 세션 잔고 업데이트 (수익 시뮬레이션)
        BigDecimal finalBalance = BigDecimal.valueOf(1200000); // 20% 수익
        savedSession.setCurrentBalance(finalBalance);
        savedSession = sessionRepository.save(savedSession);
        
        // When 5: 챌린지 종료
        ChallengeSession closedSession = closeChallengeService.closeSession(savedSession.getId());
        
        // Then 5: 세션이 정상적으로 종료됨
        assertThat(closedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(closedSession.getFinalBalance()).isEqualTo(finalBalance);
        assertThat(closedSession.getFinalReturnRate()).isNotNull();
        assertThat(closedSession.getCompletedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("동시 참가자 처리: 여러 사용자가 동시에 챌린지 시작")
    void shouldHandleConcurrentParticipants() {
        // Given: 여러 테스트 사용자 생성
        List<User> users = IntStream.range(0, 5)
                .mapToObj(i -> {
                    User user = TestDataFactory.createUser("user" + i + "@test.com", "User " + i);
                    return userRepository.save(user);
                })
                .toList();
        
        // When: 동시에 챌린지 시작
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        List<CompletableFuture<ChallengeSession>> futures = users.stream()
                .map(user -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return startChallengeService.startChallenge(user.getId(), testChallenge.getId());
                    } catch (Exception e) {
                        System.out.println("Error starting challenge for user " + user.getId() + ": " + e.getMessage());
                        return null;
                    }
                }, executor))
                .toList();
        
        // Then: 모든 사용자가 성공적으로 참가
        List<ChallengeSession> sessions = futures.stream()
                .map(CompletableFuture::join)
                .filter(session -> session != null)
                .toList();
        
        assertThat(sessions).hasSize(5);
        
        // 각 세션이 고유하고 올바른 상태인지 확인
        sessions.forEach(session -> {
            assertThat(session.getId()).isNotNull();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(users.stream().anyMatch(u -> u.getId().equals(session.getUserId()))).isTrue();
        });
        
        // 중복 참가가 없는지 확인
        long uniqueUserCount = sessions.stream()
                .map(ChallengeSession::getUserId)
                .distinct()
                .count();
        assertThat(uniqueUserCount).isEqualTo(5);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("데이터 무결성: 트랜잭션 롤백 시나리오")
    void shouldMaintainDataIntegrityOnRollback() {
        // Given: 챌린지 시작
        ChallengeSession session = startChallengeService.startChallenge(testUser.getId(), testChallenge.getId());
        Long sessionId = session.getId();
        
        // When: 트랜잭션 내에서 예외 발생 시뮬레이션
        assertThatThrownBy(() -> {
            // 세션 조회
            ChallengeSession existingSession = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            // 일부 변경 작업
            existingSession.setCurrentBalance(BigDecimal.valueOf(500000));
            sessionRepository.save(existingSession);
            
            // 예외 발생으로 롤백 유도
            throw new RuntimeException("Simulated transaction failure");
            
        }).isInstanceOf(RuntimeException.class);
        
        // Then: 롤백 후 원래 상태 유지 확인
        ChallengeSession rollbackSession = sessionRepository.findById(sessionId).orElse(null);
        assertThat(rollbackSession).isNotNull();
        assertThat(rollbackSession.getCurrentBalance()).isEqualTo(testChallenge.getSeedBalance()); // 원래 값
    }
    
    @Test
    @DisplayName("복잡한 쿼리: 리더보드 조회 성능 테스트")
    void shouldPerformComplexQueries() {
        // Given: 여러 완료된 세션 생성
        List<User> users = IntStream.range(0, 20)
                .mapToObj(i -> {
                    User user = TestDataFactory.createUser("leader" + i + "@test.com", "Leader " + i);
                    return userRepository.save(user);
                })
                .toList();
        
        // 각 사용자에 대한 완료된 세션 생성 (다양한 수익률)
        users.forEach(user -> {
            ChallengeSession session = TestDataFactory.createSession(user.getId(), testChallenge.getId());
            double randomReturn = (Math.random() - 0.5) * 0.6; // -30% ~ +30%
            BigDecimal finalBalance = BigDecimal.valueOf(1000000 * (1 + randomReturn));
            
            session.setStatus(SessionStatus.COMPLETED);
            session.setFinalBalance(finalBalance);
            session.setFinalReturnRate(BigDecimal.valueOf(randomReturn));
            session.setCompletedAt(java.time.LocalDateTime.now());
            
            sessionRepository.save(session);
        });
        
        // When: 복잡한 리더보드 쿼리 실행
        long startTime = System.currentTimeMillis();
        
        List<ChallengeSession> topPerformers = sessionRepository.findTopPerformersByChallengeId(
                testChallenge.getId(), 10, 0);
        
        long queryTime = System.currentTimeMillis() - startTime;
        
        // Then: 쿼리 결과와 성능 검증
        assertThat(topPerformers).hasSize(10);
        assertThat(queryTime).isLessThan(1000); // 1초 이내
        
        // 수익률 순서 확인
        for (int i = 0; i < topPerformers.size() - 1; i++) {
            BigDecimal currentReturn = topPerformers.get(i).getFinalReturnRate();
            BigDecimal nextReturn = topPerformers.get(i + 1).getFinalReturnRate();
            assertThat(currentReturn.compareTo(nextReturn)).isGreaterThanOrEqualTo(0);
        }
    }
    
    @Test
    @DisplayName("예외 상황: 잘못된 데이터 처리")
    void shouldHandleInvalidDataGracefully() {
        // Given: 유효하지 않은 ID로 테스트
        Long invalidUserId = 999999L;
        Long invalidChallengeId = 999999L;
        
        // When & Then: 예외 처리 확인
        assertThatThrownBy(() -> 
            startChallengeService.startChallenge(invalidUserId, testChallenge.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        
        assertThatThrownBy(() -> 
            startChallengeService.startChallenge(testUser.getId(), invalidChallengeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Challenge not found");
    }
}