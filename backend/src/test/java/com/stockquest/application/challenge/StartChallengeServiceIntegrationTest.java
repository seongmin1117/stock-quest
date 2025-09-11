package com.stockquest.application.challenge;

import com.stockquest.application.challenge.port.in.StartChallengeUseCase.StartChallengeCommand;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * 챌린지 시작 서비스 통합 테스트
 * 세션 충돌 문제 해결 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StartChallengeServiceIntegrationTest {

    @Autowired
    private StartChallengeService startChallengeService;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private ChallengeSessionRepository sessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private Challenge testChallenge1;
    private Challenge testChallenge2;
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .nickname("테스트유저")
            .build();
        testUser = userRepository.save(testUser);
        
        // 테스트 챌린지 1 생성
        testChallenge1 = Challenge.builder()
            .title("테스트 챌린지 1")
            .description("첫 번째 테스트 챌린지")
            .status(ChallengeStatus.ACTIVE)
            .initialBalance(new BigDecimal("1000000"))
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .build();
        testChallenge1 = challengeRepository.save(testChallenge1);
        
        // 테스트 챌린지 2 생성
        testChallenge2 = Challenge.builder()
            .title("테스트 챌린지 2")
            .description("두 번째 테스트 챌린지")
            .status(ChallengeStatus.ACTIVE)
            .initialBalance(new BigDecimal("2000000"))
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .build();
        testChallenge2 = challengeRepository.save(testChallenge2);
    }
    
    @Test
    @DisplayName("새로운 챌린지 세션 시작 성공")
    void startNewChallengeSession_Success() {
        // given
        var command = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        
        // when
        var result = startChallengeService.start(command);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.sessionId()).isNotNull();
        assertThat(result.challengeId()).isEqualTo(testChallenge1.getId());
        assertThat(result.initialBalance()).isEqualTo(testChallenge1.getInitialBalance());
        
        // 데이터베이스에서 세션 확인
        var savedSession = sessionRepository.findById(result.sessionId());
        assertThat(savedSession).isPresent();
        assertThat(savedSession.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(savedSession.get().getStartedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("동일한 챌린지에서 활성 세션이 있을 때 새 세션 시작 실패")
    void startChallengeSession_WithExistingActiveSession_ShouldFail() {
        // given - 먼저 세션 시작
        var firstCommand = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        startChallengeService.start(firstCommand);
        
        // when & then - 같은 챌린지에서 다시 시작 시도
        var secondCommand = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        assertThatThrownBy(() -> startChallengeService.start(secondCommand))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 진행 중인 챌린지 세션이 있습니다");
    }
    
    @Test
    @DisplayName("다른 챌린지에서는 동시에 세션 시작 가능")
    void startMultipleChallengesSessions_DifferentChallenges_Success() {
        // given
        var command1 = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        var command2 = new StartChallengeCommand(testUser.getId(), testChallenge2.getId());
        
        // when
        var result1 = startChallengeService.start(command1);
        var result2 = startChallengeService.start(command2);
        
        // then
        assertThat(result1.challengeId()).isEqualTo(testChallenge1.getId());
        assertThat(result2.challengeId()).isEqualTo(testChallenge2.getId());
        assertThat(result1.sessionId()).isNotEqualTo(result2.sessionId());
        
        // 두 세션 모두 활성 상태 확인
        var session1 = sessionRepository.findById(result1.sessionId());
        var session2 = sessionRepository.findById(result2.sessionId());
        
        assertThat(session1).isPresent();
        assertThat(session2).isPresent();
        assertThat(session1.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session2.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("완료된 세션이 있는 경우 같은 챌린지 재참여 시도시 제한")
    void startChallengeSession_WithCompletedSession_ShouldShowRestriction() {
        // given - 세션 시작 후 완료 처리
        var command = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        var result = startChallengeService.start(command);
        
        // 세션을 완료 상태로 변경
        var session = sessionRepository.findById(result.sessionId()).get();
        session.end();
        sessionRepository.save(session);
        
        // when & then - 같은 챌린지에 다시 참여 시도 (현재는 제한됨)
        var newCommand = new StartChallengeCommand(testUser.getId(), testChallenge1.getId());
        assertThatThrownBy(() -> startChallengeService.start(newCommand))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 참여한 기록이 있습니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자로 세션 시작 시도")
    void startChallengeSession_WithInvalidUser_ShouldFail() {
        // given
        Long invalidUserId = 99999L;
        var command = new StartChallengeCommand(invalidUserId, testChallenge1.getId());
        
        // when & then
        assertThatThrownBy(() -> startChallengeService.start(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 챌린지로 세션 시작 시도")
    void startChallengeSession_WithInvalidChallenge_ShouldFail() {
        // given
        Long invalidChallengeId = 99999L;
        var command = new StartChallengeCommand(testUser.getId(), invalidChallengeId);
        
        // when & then
        assertThatThrownBy(() -> startChallengeService.start(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("챌린지를 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("비활성 챌린지로 세션 시작 시도")
    void startChallengeSession_WithInactiveChallenge_ShouldFail() {
        // given - 비활성 챌린지 생성
        var inactiveChallenge = Challenge.builder()
            .title("비활성 챌린지")
            .description("비활성 상태인 챌린지")
            .status(ChallengeStatus.INACTIVE)
            .initialBalance(new BigDecimal("1000000"))
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .build();
        inactiveChallenge = challengeRepository.save(inactiveChallenge);
        
        var command = new StartChallengeCommand(testUser.getId(), inactiveChallenge.getId());
        
        // when & then
        assertThatThrownBy(() -> startChallengeService.start(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("활성 상태가 아닌 챌린지는 시작할 수 없습니다");
    }
}