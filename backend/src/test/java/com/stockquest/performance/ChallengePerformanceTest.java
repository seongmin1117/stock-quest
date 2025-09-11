package com.stockquest.performance;

import com.stockquest.application.challenge.StartChallengeService;
import com.stockquest.application.challenge.GetChallengeListService;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import com.stockquest.testutils.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 챌린지 관련 성능 테스트
 * 대량 사용자 동시 접근 시나리오 테스트
 */
@DisplayName("챌린지 성능 테스트")
class ChallengePerformanceTest extends PerformanceTestBase {
    
    @Autowired
    private StartChallengeService startChallengeService;
    
    @Autowired
    private GetChallengeListService getChallengeListService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private ChallengeSessionRepository sessionRepository;
    
    private List<User> testUsers;
    private Challenge testChallenge;
    
    @BeforeEach
    void setUp() {
        // 대량 테스트 데이터 준비
        TestDataFactory.resetCounters();
        
        // 1000명의 테스트 사용자 생성
        System.out.println("Creating 1000 test users...");
        testUsers = new ArrayList<>();
        
        // 배치로 사용자 생성 (성능 향상)
        for (int batch = 0; batch < 10; batch++) {
            List<User> batchUsers = IntStream.range(batch * 100, (batch + 1) * 100)
                    .mapToObj(i -> TestDataFactory.createUser("perfuser" + i + "@test.com", "Performance User " + i))
                    .toList();
            
            List<User> savedBatchUsers = new ArrayList<>();
            for (User user : batchUsers) {
                savedBatchUsers.add(userRepository.save(user));
            }
            testUsers.addAll(savedBatchUsers);
        }
        
        // 테스트 챌린지 생성
        testChallenge = TestDataFactory.createActiveChallenge();
        testChallenge.setTitle("Performance Test Challenge");
        testChallenge.setMaxParticipants(1000);
        testChallenge.setCurrentParticipants(0);
        testChallenge = challengeRepository.save(testChallenge);
        
        System.out.println("Test data setup completed");
    }
    
    @AfterEach
    void tearDown() {
        tearDownPerformanceTest();
    }
    
    @Test
    @DisplayName("대량 동시 챌린지 시작 성능 테스트")
    void shouldHandleMassiveConcurrentChallengeStarts() {
        // Given
        int concurrentUsers = 500;
        List<User> selectedUsers = testUsers.subList(0, concurrentUsers);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(concurrentUsers);
        
        System.out.println("=== Starting massive concurrent challenge test ===");
        System.out.println("Concurrent users: " + concurrentUsers);
        
        // When: 동시에 챌린지 시작
        long testStartTime = System.currentTimeMillis();
        
        List<CompletableFuture<Void>> futures = selectedUsers.stream()
                .map(user -> CompletableFuture.runAsync(() -> {
                    try {
                        // 모든 스레드가 동시에 시작하도록 대기
                        startLatch.await();
                        
                        long operationStart = System.currentTimeMillis();
                        boolean success = false;
                        
                        try {
                            ChallengeSession session = startChallengeService.startChallenge(
                                user.getId(), testChallenge.getId());
                            success = (session != null && session.getId() != null);
                        } catch (Exception e) {
                            System.err.println("Error for user " + user.getId() + ": " + e.getMessage());
                        }
                        
                        long operationTime = System.currentTimeMillis() - operationStart;
                        metrics.recordOperation(operationTime, success);
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completeLatch.countDown();
                    }
                }, executorService))
                .toList();
        
        // 모든 스레드 동시 시작
        startLatch.countDown();
        
        // 모든 작업 완료 대기 (최대 2분)
        try {
            boolean completed = completeLatch.await(120, TimeUnit.SECONDS);
            assertThat(completed).as("All operations should complete within timeout").isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }
        
        long totalTestTime = System.currentTimeMillis() - testStartTime;
        
        // Then: 성능 결과 분석
        metrics.printSummary("Concurrent Challenge Start", totalTestTime);
        
        // 성능 임계값 검증
        assertPerformanceThresholds(
            metrics,
            2000.0,  // 평균 응답시간 2초 이하
            50.0,    // 처리율 50 ops/sec 이상
            5.0,     // 에러율 5% 이하
            totalTestTime
        );
        
        // 데이터 무결성 검증
        long sessionCount = sessionRepository.countByChallengeId(testChallenge.getId());
        long successfulOperations = metrics.getTotalOperations() - metrics.getErrorCount();
        
        assertThat(sessionCount)
            .as("Created sessions should match successful operations")
            .isEqualTo(successfulOperations);
    }
    
    @Test
    @DisplayName("챌린지 목록 조회 성능 테스트")
    void shouldHandleHighVolumeListRequests() {
        // Given: 추가 챌린지 생성 (다양한 상태)
        List<Challenge> challenges = IntStream.range(0, 50)
                .mapToObj(i -> {
                    Challenge challenge = TestDataFactory.createChallenge(
                        "Performance Challenge " + i,
                        com.stockquest.domain.challenge.ChallengeDifficulty.values()[i % 3]
                    );
                    return challengeRepository.save(challenge);
                })
                .toList();
        
        int requestCount = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(requestCount);
        
        System.out.println("=== Starting high volume list request test ===");
        System.out.println("Total requests: " + requestCount);
        
        // When: 대량 목록 조회 요청
        long testStartTime = System.currentTimeMillis();
        
        IntStream.range(0, requestCount)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        startLatch.await();
                        
                        long operationStart = System.currentTimeMillis();
                        boolean success = false;
                        
                        try {
                            var challengeList = getChallengeListService.getChallengeList(
                                new com.stockquest.application.challenge.dto.GetChallengeListQuery(0, 20, null)
                            );
                            success = (challengeList != null && challengeList.challenges() != null);
                        } catch (Exception e) {
                            System.err.println("List request error: " + e.getMessage());
                        }
                        
                        long operationTime = System.currentTimeMillis() - operationStart;
                        metrics.recordOperation(operationTime, success);
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completeLatch.countDown();
                    }
                }));
        
        startLatch.countDown();
        
        // 완료 대기
        try {
            boolean completed = completeLatch.await(60, TimeUnit.SECONDS);
            assertThat(completed).as("All list requests should complete").isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }
        
        long totalTestTime = System.currentTimeMillis() - testStartTime;
        
        // Then: 성능 결과 분석
        metrics.printSummary("Challenge List Requests", totalTestTime);
        
        // 캐싱 효과로 매우 빠른 응답 기대
        assertPerformanceThresholds(
            metrics,
            100.0,   // 평균 응답시간 100ms 이하 (캐싱)
            200.0,   // 처리율 200 ops/sec 이상
            1.0,     // 에러율 1% 이하
            totalTestTime
        );
    }
    
    @Test
    @DisplayName("스트레스 테스트: 시스템 한계 탐색")
    void shouldSurviveStressTest() {
        // Given: 점진적 부하 증가
        int[] loadLevels = {100, 250, 500, 750, 1000};
        
        for (int loadLevel : loadLevels) {
            System.out.println("=== Stress Test Level: " + loadLevel + " users ===");
            
            PerformanceMetrics levelMetrics = new PerformanceMetrics();
            List<User> levelUsers = testUsers.subList(0, Math.min(loadLevel, testUsers.size()));
            
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completeLatch = new CountDownLatch(loadLevel);
            
            long levelStartTime = System.currentTimeMillis();
            
            // 해당 레벨의 부하 실행
            levelUsers.forEach(user -> executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    long operationStart = System.currentTimeMillis();
                    boolean success = false;
                    
                    try {
                        // 다양한 작업 수행
                        if (Math.random() < 0.7) {
                            // 70% 챌린지 시작
                            ChallengeSession session = startChallengeService.startChallenge(
                                user.getId(), testChallenge.getId());
                            success = (session != null);
                        } else {
                            // 30% 목록 조회
                            var result = getChallengeListService.getChallengeList(
                                new com.stockquest.application.challenge.dto.GetChallengeListQuery(0, 10, null)
                            );
                            success = (result != null);
                        }
                    } catch (Exception e) {
                        // 스트레스 상황에서는 일부 실패 허용
                    }
                    
                    long operationTime = System.currentTimeMillis() - operationStart;
                    levelMetrics.recordOperation(operationTime, success);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            }));
            
            startLatch.countDown();
            
            try {
                boolean completed = completeLatch.await(180, TimeUnit.SECONDS); // 3분 대기
                if (!completed) {
                    System.err.println("Timeout at load level: " + loadLevel);
                    break; // 시스템 한계 도달
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            long levelTime = System.currentTimeMillis() - levelStartTime;
            levelMetrics.printSummary("Stress Level " + loadLevel, levelTime);
            
            // 에러율이 20% 초과하면 시스템 한계로 판단
            if (levelMetrics.getErrorRate() > 20.0) {
                System.out.println("System limit reached at load level: " + loadLevel);
                break;
            }
            
            // 다음 레벨 전 잠시 휴식
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}