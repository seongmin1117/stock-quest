package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeSessionJpaEntity;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 세션 JPA 저장소
 * 도메인 포트의 모든 메서드를 지원하도록 확장
 */
@Repository
public interface ChallengeSessionJpaRepository extends JpaRepository<ChallengeSessionJpaEntity, Long> {
    
    // === 기존 메서드들 ===
    
    List<ChallengeSessionJpaEntity> findByUserId(Long userId);
    
    List<ChallengeSessionJpaEntity> findByChallengeId(Long challengeId);

    Optional<ChallengeSessionJpaEntity> findByChallengeIdAndUserId(Long challengeId, Long userId);

    List<ChallengeSessionJpaEntity> findByStatus(SessionStatus status);
    
    List<ChallengeSessionJpaEntity> findByUserIdAndStatus(Long userId, SessionStatus status);
    
    Optional<ChallengeSessionJpaEntity> findByUserIdAndChallengeIdAndStatus(Long userId, Long challengeId, SessionStatus status);
    
    List<ChallengeSessionJpaEntity> findByChallengeIdAndStatus(Long challengeId, SessionStatus status);
    
    List<ChallengeSessionJpaEntity> findByUserIdOrderByStartedAtDesc(Long userId);
    
    // === 새로 추가된 고급 쿼리 메서드들 ===
    
    /**
     * 활성 세션들을 시작시간 내림차순으로 조회
     */
    @Query("SELECT s FROM ChallengeSessionJpaEntity s WHERE s.status = 'ACTIVE' ORDER BY s.startedAt DESC")
    List<ChallengeSessionJpaEntity> findActiveSessionsOrderByStartedAtDesc();
    
    /**
     * 리더보드용 - 챌린지별 수익률 상위 세션들 조회
     */
    @Query("SELECT s FROM ChallengeSessionJpaEntity s " +
           "WHERE s.challengeId = :challengeId AND s.status = 'ENDED' AND s.returnRate IS NOT NULL " +
           "ORDER BY s.returnRate DESC")
    List<ChallengeSessionJpaEntity> findTopPerformersByChallengeId(@Param("challengeId") Long challengeId, Pageable pageable);
    
    /**
     * 기간별 완료된 세션 조회 (통계용)
     */
    @Query("SELECT s FROM ChallengeSessionJpaEntity s " +
           "WHERE s.status = 'ENDED' AND s.completedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY s.completedAt DESC")
    List<ChallengeSessionJpaEntity> findCompletedSessionsBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 사용자의 최근 완료 세션 조회
     */
    @Query("SELECT s FROM ChallengeSessionJpaEntity s " +
           "WHERE s.userId = :userId AND s.status = 'ENDED' " +
           "ORDER BY s.completedAt DESC")
    List<ChallengeSessionJpaEntity> findRecentCompletedSessionsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 챌린지별 참가자 수
     */
    long countByChallengeId(Long challengeId);
    
    /**
     * 챌린지별 상태별 참가자 수
     */
    long countByChallengeIdAndStatus(Long challengeId, SessionStatus status);
    
    /**
     * 오래된 완료 세션 정리 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM ChallengeSessionJpaEntity s " +
           "WHERE s.status = 'ENDED' AND s.completedAt < :cutoffDate")
    void deleteCompletedSessionsBeforeDate(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 사용자의 특정 챌린지 참여 여부 확인
     */
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
    
    /**
     * 챌린지의 평균 수익률 조회
     */
    @Query("SELECT AVG(s.returnRate) FROM ChallengeSessionJpaEntity s " +
           "WHERE s.challengeId = :challengeId AND s.status = 'ENDED' AND s.returnRate IS NOT NULL")
    Double getAverageReturnRateByChallengeId(@Param("challengeId") Long challengeId);
    
    /**
     * 수익률 내림차순으로 챌린지별 세션 조회
     */
    @Query("SELECT s FROM ChallengeSessionJpaEntity s " +
           "WHERE s.challengeId = :challengeId AND s.returnRate IS NOT NULL " +
           "ORDER BY s.returnRate DESC")
    List<ChallengeSessionJpaEntity> findByChallengeIdOrderByReturnRateDesc(@Param("challengeId") Long challengeId);
    
    /**
     * 특정 시간 이후 생성된 챌린지 세션 조회
     */
    List<ChallengeSessionJpaEntity> findByChallengeIdAndCreatedAtAfter(Long challengeId, LocalDateTime since);
    
    /**
     * 사용자의 평균 수익률 조회
     */
    @Query("SELECT AVG(s.returnRate) FROM ChallengeSessionJpaEntity s " +
           "WHERE s.userId = :userId AND s.status = 'ENDED' AND s.returnRate IS NOT NULL")
    Double findAverageReturnRateByUserId(@Param("userId") Long userId);
}