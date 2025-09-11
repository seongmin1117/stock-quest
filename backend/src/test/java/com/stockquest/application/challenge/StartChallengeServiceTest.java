package com.stockquest.application.challenge;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import com.stockquest.testutils.TestBase;
import com.stockquest.testutils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * StartChallengeService 단위 테스트
 * 챌린지 시작 로직의 모든 시나리오 테스트
 */
@DisplayName("StartChallengeService 테스트")
class StartChallengeServiceTest extends TestBase {
    
    @Mock
    private ChallengeRepository challengeRepository;
    
    @Mock
    private ChallengeSessionRepository sessionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private StartChallengeService startChallengeService;
    
    @Nested
    @DisplayName("챌린지 시작 성공 시나리오")
    class SuccessfulStart {
        
        @Test
        @DisplayName("유효한 챌린지와 사용자로 시작 성공")
        void shouldStartChallengeSuccessfully() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            user.setId(userId);
            
            Challenge challenge = TestDataFactory.createActiveChallenge();
            challenge.setId(challengeId);
            challenge.setMaxParticipants(100);
            challenge.setCurrentParticipants(50);
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            
            ChallengeSession savedSession = TestDataFactory.createActiveSession(userId, challengeId);
            given(sessionRepository.save(any(ChallengeSession.class))).willReturn(savedSession);
            
            // When
            ChallengeSession result = startChallengeService.startChallenge(userId, challengeId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getChallengeId()).isEqualTo(challengeId);
            assertThat(result.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(result.getInitialBalance()).isEqualTo(challenge.getSeedBalance());
            
            // Verify session creation
            ArgumentCaptor<ChallengeSession> sessionCaptor = ArgumentCaptor.forClass(ChallengeSession.class);
            then(sessionRepository).should().save(sessionCaptor.capture());
            
            ChallengeSession capturedSession = sessionCaptor.getValue();
            assertThat(capturedSession.getUserId()).isEqualTo(userId);
            assertThat(capturedSession.getChallengeId()).isEqualTo(challengeId);
            assertThat(capturedSession.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(capturedSession.getStartedAt()).isNotNull();
        }
        
        @Test
        @DisplayName("첫 번째 참가자로 챌린지 시작")
        void shouldStartAsFirstParticipant() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createActiveChallenge();
            challenge.setCurrentParticipants(0); // 첫 번째 참가자
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            given(sessionRepository.save(any(ChallengeSession.class))).willReturn(new ChallengeSession());
            
            // When
            startChallengeService.startChallenge(userId, challengeId);
            
            // Then
            then(sessionRepository).should().save(any(ChallengeSession.class));
        }
    }
    
    @Nested
    @DisplayName("챌린지 시작 실패 시나리오")
    class FailedStart {
        
        @Test
        @DisplayName("존재하지 않는 사용자로 시작 실패")
        void shouldFailWithNonExistentUser() {
            // Given
            Long userId = 999L;
            Long challengeId = 1L;
            
            given(userRepository.findById(userId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
            
            // Verify no session is created
            then(sessionRepository).should(never()).save(any());
        }
        
        @Test
        @DisplayName("존재하지 않는 챌린지로 시작 실패")
        void shouldFailWithNonExistentChallenge() {
            // Given
            Long userId = 1L;
            Long challengeId = 999L;
            
            User user = TestDataFactory.createUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Challenge not found");
        }
        
        @Test
        @DisplayName("이미 참여 중인 챌린지 시작 실패")
        void shouldFailWithExistingSession() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createActiveChallenge();
            ChallengeSession existingSession = TestDataFactory.createActiveSession(userId, challengeId);
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.of(existingSession));
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Already participating");
        }
        
        @Test
        @DisplayName("비활성 챌린지 시작 실패")
        void shouldFailWithInactiveChallenge() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createChallenge();
            challenge.setStatus(ChallengeStatus.INACTIVE);
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Challenge is not active");
        }
        
        @Test
        @DisplayName("참가자 수 초과로 시작 실패")
        void shouldFailWhenMaxParticipantsExceeded() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createActiveChallenge();
            challenge.setMaxParticipants(100);
            challenge.setCurrentParticipants(100); // 이미 최대치
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Challenge is full");
        }
        
        @Test
        @DisplayName("시작 시간이 지나지 않은 챌린지 시작 실패")
        void shouldFailWithFutureStartTime() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createChallenge();
            challenge.setStartDate(LocalDateTime.now().plusDays(1)); // 미래 시간
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Challenge has not started yet");
        }
        
        @Test
        @DisplayName("종료된 챌린지 시작 실패")
        void shouldFailWithExpiredChallenge() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createChallenge();
            challenge.setEndDate(LocalDateTime.now().minusDays(1)); // 과거 시간
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Challenge has expired");
        }
    }
    
    @Nested
    @DisplayName("경계값 및 예외 상황")
    class EdgeCases {
        
        @Test
        @DisplayName("null userId로 시작 시도")
        void shouldFailWithNullUserId() {
            // Given
            Long userId = null;
            Long challengeId = 1L;
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID and Challenge ID are required");
        }
        
        @Test
        @DisplayName("null challengeId로 시작 시도")
        void shouldFailWithNullChallengeId() {
            // Given
            Long userId = 1L;
            Long challengeId = null;
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID and Challenge ID are required");
        }
        
        @Test
        @DisplayName("데이터베이스 저장 실패 처리")
        void shouldHandleDatabaseSaveFailure() {
            // Given
            Long userId = 1L;
            Long challengeId = 1L;
            
            User user = TestDataFactory.createUser();
            Challenge challenge = TestDataFactory.createActiveChallenge();
            
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
            given(sessionRepository.findByChallengeIdAndUserId(challengeId, userId)).willReturn(Optional.empty());
            given(sessionRepository.save(any(ChallengeSession.class)))
                    .willThrow(new RuntimeException("Database save failed"));
            
            // When & Then
            assertThatThrownBy(() -> startChallengeService.startChallenge(userId, challengeId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database save failed");
        }
    }
}