package com.stockquest.application.session;

import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 세션 관리 서비스
 * 관리자 기능 및 세션 정리 작업을 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SessionManagementService {
    
    private final ChallengeSessionRepository sessionRepository;
    
    /**
     * 사용자의 특정 챌린지에서 활성 세션 강제 종료
     * 관리자 또는 시스템 정리 용도
     * 
     * @param userId 사용자 ID
     * @param challengeId 챌린지 ID
     * @param reason 종료 사유
     * @return 종료된 세션 정보
     */
    public Optional<ChallengeSession> forceEndActiveSession(Long userId, Long challengeId, String reason) {
        log.info("활성 세션 강제 종료 시작: userId={}, challengeId={}, reason={}", userId, challengeId, reason);
        
        var activeSession = sessionRepository.findByUserIdAndChallengeIdAndStatus(
            userId, challengeId, SessionStatus.ACTIVE);
            
        if (activeSession.isPresent()) {
            var session = activeSession.get();
            session.cancel(); // 강제 종료는 CANCELLED 상태로 처리
            var savedSession = sessionRepository.save(session);
            
            log.info("활성 세션 강제 종료 완료: sessionId={}, reason={}", session.getId(), reason);
            return Optional.of(savedSession);
        }
        
        log.info("강제 종료할 활성 세션이 없음: userId={}, challengeId={}", userId, challengeId);
        return Optional.empty();
    }
    
    /**
     * 사용자의 모든 활성 세션 강제 종료
     * 
     * @param userId 사용자 ID
     * @param reason 종료 사유
     * @return 종료된 세션 수
     */
    public int forceEndAllActiveSessionsByUser(Long userId, String reason) {
        log.info("사용자의 모든 활성 세션 강제 종료 시작: userId={}, reason={}", userId, reason);
        
        var activeSessions = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE);
        int endedCount = 0;
        
        for (ChallengeSession session : activeSessions) {
            session.cancel();
            sessionRepository.save(session);
            endedCount++;
            log.debug("세션 강제 종료: sessionId={}, challengeId={}", session.getId(), session.getChallengeId());
        }
        
        log.info("사용자의 모든 활성 세션 강제 종료 완료: userId={}, 종료된 세션 수={}", userId, endedCount);
        return endedCount;
    }
    
    /**
     * 오래된 READY 상태 세션들을 CANCELLED로 정리
     * 
     * @param cutoffHours READY 상태로 유지할 최대 시간 (시간 단위)
     * @return 정리된 세션 수
     */
    public int cleanupStaleReadySessions(int cutoffHours) {
        log.info("오래된 READY 세션 정리 시작: cutoffHours={}", cutoffHours);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(cutoffHours);
        var readySessions = sessionRepository.findByStatus(SessionStatus.READY);
        
        int cleanedCount = 0;
        for (ChallengeSession session : readySessions) {
            if (session.getCreatedAt() != null && session.getCreatedAt().isBefore(cutoffTime)) {
                session.cancel();
                sessionRepository.save(session);
                cleanedCount++;
                log.debug("오래된 READY 세션 정리: sessionId={}, createdAt={}", session.getId(), session.getCreatedAt());
            }
        }
        
        log.info("오래된 READY 세션 정리 완료: 정리된 세션 수={}", cleanedCount);
        return cleanedCount;
    }
    
    /**
     * 특정 세션의 상태 조회
     * 
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    @Transactional(readOnly = true)
    public Optional<SessionStatusInfo> getSessionStatus(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .map(session -> new SessionStatusInfo(
                session.getId(),
                session.getChallengeId(),
                session.getUserId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getCreatedAt()
            ));
    }
    
    /**
     * 사용자의 세션 참여 이력 조회
     * 
     * @param userId 사용자 ID
     * @return 세션 이력 목록
     */
    @Transactional(readOnly = true)
    public List<SessionStatusInfo> getUserSessionHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
            .stream()
            .map(session -> new SessionStatusInfo(
                session.getId(),
                session.getChallengeId(),
                session.getUserId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getCreatedAt()
            ))
            .toList();
    }
    
    /**
     * 세션 상태 정보 DTO
     */
    public record SessionStatusInfo(
        Long sessionId,
        Long challengeId,
        Long userId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
    ) {}
}