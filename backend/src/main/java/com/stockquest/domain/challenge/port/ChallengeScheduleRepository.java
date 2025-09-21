package com.stockquest.domain.challenge.port;

import com.stockquest.domain.challenge.ChallengeSchedule;
import com.stockquest.domain.common.Page;
import com.stockquest.domain.common.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 스케줄 저장소 포트
 */
public interface ChallengeScheduleRepository {
    
    /**
     * 스케줄 저장
     */
    ChallengeSchedule save(ChallengeSchedule schedule);
    
    /**
     * ID로 스케줄 조회
     */
    Optional<ChallengeSchedule> findById(Long id);
    
    /**
     * 챌린지별 스케줄 목록 조회
     */
    List<ChallengeSchedule> findByChallengeId(Long challengeId);
    
    /**
     * 활성화 대상 스케줄 조회
     */
    List<ChallengeSchedule> findSchedulesToActivate(LocalDateTime currentTime);
    
    /**
     * 비활성화 대상 스케줄 조회
     */
    List<ChallengeSchedule> findSchedulesToDeactivate(LocalDateTime currentTime);
    
    /**
     * 반복 스케줄 조회
     */
    List<ChallengeSchedule> findRecurringSchedules();
    
    /**
     * 활성 스케줄 목록 조회
     */
    List<ChallengeSchedule> findActiveSchedules();
    
    /**
     * 페이지네이션된 스케줄 목록 조회
     */
    Page<ChallengeSchedule> findAll(PageRequest pageRequest);
    
    /**
     * 특정 기간의 스케줄 조회
     */
    List<ChallengeSchedule> findByActivationDateBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 스케줄 삭제
     */
    void delete(ChallengeSchedule schedule);
    
    /**
     * ID로 스케줄 삭제
     */
    void deleteById(Long id);
    
    /**
     * 스케줄 존재 여부 확인
     */
    boolean existsById(Long id);
    
    /**
     * 챌린지의 활성 스케줄 존재 여부 확인
     */
    boolean existsActiveByChallengeId(Long challengeId);
}