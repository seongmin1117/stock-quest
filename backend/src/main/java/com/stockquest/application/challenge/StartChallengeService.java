package com.stockquest.application.challenge;

import com.stockquest.application.challenge.port.in.StartChallengeUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 챌린지 시작 서비스 구현체
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StartChallengeService implements StartChallengeUseCase {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository sessionRepository;
    private final UserRepository userRepository;
    
    @Value("${stockquest.challenge.default-seed-balance:10000000}")
    private BigDecimal defaultSeedBalance; // 기본 시드머니 1천만원
    
    @Override
    public StartChallengeResult start(StartChallengeCommand command) {
        // 1. 사용자 존재 확인
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + command.userId()));
        
        // 2. 챌린지 존재 및 활성 상태 확인  
        var challenge = challengeRepository.findById(command.challengeId())
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + command.challengeId()));
        
        if (challenge.getStatus() != com.stockquest.domain.challenge.ChallengeStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태가 아닌 챌린지는 시작할 수 없습니다");
        }
        
        // 3. 기존 세션 확인 및 처리
        var existingActiveSession = sessionRepository.findByUserIdAndChallengeIdAndStatus(
            command.userId(), 
            command.challengeId(), 
            ChallengeSession.SessionStatus.ACTIVE
        );
        
        if (existingActiveSession.isPresent()) {
            throw new IllegalStateException("이미 진행 중인 챌린지 세션이 있습니다. 세션 ID: " + existingActiveSession.get().getId());
        }
        
        // 4. 기존 완료된 세션이 있는지 확인 (선택적 정보 제공)
        var anyExistingSession = sessionRepository.findByChallengeIdAndUserId(command.challengeId(), command.userId());
        if (anyExistingSession.isPresent()) {
            var session = anyExistingSession.get();
            if (session.isCompleted()) {
                // 이미 완료된 세션이 있으면 새로 시작할 수 있음 (데이터베이스 UNIQUE 제약 조건에 의해 실제로는 불가능)
                // 이 경우는 데이터베이스 제약 조건을 변경해야 함
                throw new IllegalStateException(
                    String.format("이 챌린지에 이미 참여한 기록이 있습니다. 상태: %s, 완료일: %s", 
                        session.getStatus().getDescription(), 
                        session.getCompletedAt())
                );
            }
        }
        
        // 5. 새 세션 생성 및 시작
        BigDecimal seedBalance = challenge.getInitialBalance() != null ? 
            challenge.getInitialBalance() : defaultSeedBalance;
            
        var session = ChallengeSession.builder()
            .challengeId(command.challengeId())
            .userId(command.userId())
            .initialBalance(seedBalance)
            .currentBalance(seedBalance)
            .returnRate(BigDecimal.ZERO)
            .status(ChallengeSession.SessionStatus.READY)
            .build();
        
        session.start(); // 상태를 ACTIVE로 변경하고 startedAt 설정
        var savedSession = sessionRepository.save(session);
        
        return new StartChallengeResult(
            savedSession.getId(),
            challenge.getId(),
            challenge.getTitle(),
            seedBalance,
            savedSession.getStartedAt()
        );
    }
}