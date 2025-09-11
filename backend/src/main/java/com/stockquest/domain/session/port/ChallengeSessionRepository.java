package com.stockquest.domain.session.port;

import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 세션 저장소 포트 (출력 포트)
 * 
 * 비즈니스 요구사항:
 * - 사용자별 세션 히스토리 관리
 * - 리더보드를 위한 성능 조회
 * - 활성 세션 모니터링
 * - 세션 통계 및 분석
 */
public interface ChallengeSessionRepository {
    
    /**
     * 세션 저장
     */
    ChallengeSession save(ChallengeSession session);
    
    /**
     * ID로 세션 조회
     */
    Optional<ChallengeSession> findById(Long id);
    
    /**
     * 챌린지별 사용자 세션 조회
     */
    Optional<ChallengeSession> findByChallengeIdAndUserId(Long challengeId, Long userId);
    
    /**
     * 챌린지의 모든 세션 조회
     */
    List<ChallengeSession> findByChallengeId(Long challengeId);
    
    /**
     * 상태별 세션 조회
     */
    List<ChallengeSession> findByStatus(SessionStatus status);
    
    /**
     * 사용자의 모든 세션 조회 (최신순)
     */
    List<ChallengeSession> findByUserIdOrderByStartedAtDesc(Long userId);
    
    /**
     * 사용자, 챌린지, 상태별 세션 조회
     */
    Optional<ChallengeSession> findByUserIdAndChallengeIdAndStatus(Long userId, Long challengeId, SessionStatus status);
    
    /**
     * 챌린지별 상태별 세션 조회
     */
    List<ChallengeSession> findByChallengeIdAndStatus(Long challengeId, SessionStatus status);
    
    // === 새로 추가된 비즈니스 로직 메서드 ===
    
    /**
     * 현재 활성 세션 조회 (시작시간 내림차순)
     * 실시간 모니터링 및 관리자 대시보드용
     */
    List<ChallengeSession> findActiveSessionsOrderByStartedAtDesc();
    
    /**
     * 사용자의 특정 상태 세션 조회
     * 사용자 프로필 페이지 및 세션 상태 관리용
     */
    List<ChallengeSession> findByUserIdAndStatus(Long userId, SessionStatus status);
    
    /**
     * 챌린지별 완료된 세션들을 수익률 내림차순으로 조회 (리더보드용)
     * 페이징을 위한 limit과 offset 지원
     */
    List<ChallengeSession> findTopPerformersByChallengeId(Long challengeId, int limit, int offset);
    
    /**
     * 특정 기간 내 완료된 세션 조회
     * 통계 및 분석용
     */
    List<ChallengeSession> findCompletedSessionsBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 사용자의 최근 완료된 세션 조회 (limit 개수)
     * 사용자 대시보드 및 히스토리용
     */
    List<ChallengeSession> findRecentCompletedSessionsByUserId(Long userId, int limit);
    
    /**
     * 챌린지별 참가자 수 조회
     * 챌린지 인기도 측정용
     */
    long countByChallengeId(Long challengeId);
    
    /**
     * 챌린지별 활성 참가자 수 조회
     * 실시간 참가자 현황용
     */
    long countByChallengeIdAndStatus(Long challengeId, SessionStatus status);
    
    /**
     * 세션 삭제 (테스트 데이터 정리용)
     */
    void deleteById(Long id);
    
    /**
     * 오래된 완료 세션 정리 (데이터 관리용)
     * 특정 날짜 이전의 완료된 세션들을 삭제
     */
    void deleteCompletedSessionsBeforeDate(LocalDateTime cutoffDate);
    
    /**
     * 챌린지별 수익률 내림차순으로 조회
     */
    List<ChallengeSession> findByChallengeIdOrderByReturnRateDesc(Long challengeId);
    
    /**
     * 챌린지별 특정 시간 이후 생성된 세션 조회
     */
    List<ChallengeSession> findByChallengeIdAndCreatedAtAfter(Long challengeId, LocalDateTime since);
}