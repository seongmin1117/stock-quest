package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeScheduleJpaEntity;
import com.stockquest.domain.challenge.ChallengeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 챌린지 스케줄 JPA Repository
 */
@Repository
public interface ChallengeScheduleJpaRepository extends JpaRepository<ChallengeScheduleJpaEntity, Long> {
    
    /**
     * 챌린지별 스케줄 목록 조회
     */
    List<ChallengeScheduleJpaEntity> findByChallengeId(Long challengeId);
    
    /**
     * 활성화 대상 스케줄 조회 (활성화 시간이 현재 시간보다 이전이고, 아직 활성화되지 않은 스케줄)
     */
    @Query("SELECT s FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.activationDate <= :currentTime AND " +
           "s.isActive = true AND " +
           "(s.deactivationDate IS NULL OR s.deactivationDate > :currentTime)")
    List<ChallengeScheduleJpaEntity> findSchedulesToActivate(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 비활성화 대상 스케줄 조회 (비활성화 시간이 현재 시간보다 이전인 활성 스케줄)
     */
    @Query("SELECT s FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.deactivationDate <= :currentTime AND " +
           "s.isActive = true")
    List<ChallengeScheduleJpaEntity> findSchedulesToDeactivate(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 반복 스케줄 조회
     */
    @Query("SELECT s FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.scheduleType = :scheduleType AND " +
           "s.isActive = true")
    List<ChallengeScheduleJpaEntity> findByScheduleType(@Param("scheduleType") ChallengeSchedule.ScheduleType scheduleType);
    
    /**
     * 활성 스케줄 목록 조회
     */
    @Query("SELECT s FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.isActive = true ORDER BY s.createdAt DESC")
    List<ChallengeScheduleJpaEntity> findActiveSchedules();
    
    /**
     * 특정 기간의 스케줄 조회
     */
    @Query("SELECT s FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.activationDate BETWEEN :start AND :end AND " +
           "s.isActive = true ORDER BY s.activationDate")
    List<ChallengeScheduleJpaEntity> findByActivationDateBetween(@Param("start") LocalDateTime start, 
                                                                 @Param("end") LocalDateTime end);
    
    /**
     * 챌린지의 활성 스케줄 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM ChallengeScheduleJpaEntity s WHERE " +
           "s.challengeId = :challengeId AND " +
           "s.isActive = true")
    boolean existsActiveByChallengeId(@Param("challengeId") Long challengeId);
}