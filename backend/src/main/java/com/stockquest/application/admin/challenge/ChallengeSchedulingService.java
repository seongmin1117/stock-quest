package com.stockquest.application.admin.challenge;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeSchedule;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.challenge.port.ChallengeScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 챌린지 스케줄링 서비스
 * 자동으로 챌린지를 활성화/비활성화하고 상태를 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeSchedulingService {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeScheduleRepository scheduleRepository;
    
    /**
     * 예약된 챌린지를 활성화
     * 매 5분마다 실행
     */
    @Scheduled(fixedRate = 300000) // 5분 간격
    @Transactional
    public void activateScheduledChallenges() {
        log.debug("Checking for scheduled challenges to activate");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 활성화 대상 스케줄 조회
        List<ChallengeSchedule> schedulesToActivate = scheduleRepository.findSchedulesToActivate(now);
        
        for (ChallengeSchedule schedule : schedulesToActivate) {
            try {
                activateChallenge(schedule);
            } catch (Exception e) {
                log.error("Failed to activate challenge for schedule {}: {}", schedule.getId(), e.getMessage(), e);
            }
        }
        
        if (!schedulesToActivate.isEmpty()) {
            log.info("Processed {} challenge activation schedules", schedulesToActivate.size());
        }
    }
    
    /**
     * 활성 챌린지를 완료로 변경
     * 매 10분마다 실행
     */
    @Scheduled(fixedRate = 600000) // 10분 간격
    @Transactional
    public void completeExpiredChallenges() {
        log.debug("Checking for expired challenges to complete");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 완료 대상 스케줄 조회
        List<ChallengeSchedule> schedulesToComplete = scheduleRepository.findSchedulesToDeactivate(now);
        
        for (ChallengeSchedule schedule : schedulesToComplete) {
            try {
                completeChallenge(schedule);
            } catch (Exception e) {
                log.error("Failed to complete challenge for schedule {}: {}", schedule.getId(), e.getMessage(), e);
            }
        }
        
        if (!schedulesToComplete.isEmpty()) {
            log.info("Processed {} challenge completion schedules", schedulesToComplete.size());
        }
    }
    
    /**
     * 반복 스케줄 처리
     * 매 시간마다 실행
     */
    @Scheduled(fixedRate = 3600000) // 1시간 간격
    @Transactional
    public void processRecurringSchedules() {
        log.debug("Processing recurring challenge schedules");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 반복 스케줄 조회
        List<ChallengeSchedule> recurringSchedules = scheduleRepository.findRecurringSchedules();
        
        for (ChallengeSchedule schedule : recurringSchedules) {
            try {
                processRecurringSchedule(schedule, now);
            } catch (Exception e) {
                log.error("Failed to process recurring schedule {}: {}", schedule.getId(), e.getMessage(), e);
            }
        }
        
        if (!recurringSchedules.isEmpty()) {
            log.info("Processed {} recurring challenge schedules", recurringSchedules.size());
        }
    }
    
    private void activateChallenge(ChallengeSchedule schedule) {
        Challenge challenge = challengeRepository.findById(schedule.getChallengeId())
                .orElse(null);
        
        if (challenge == null) {
            log.warn("Challenge {} not found for schedule {}", schedule.getChallengeId(), schedule.getId());
            return;
        }
        
        if (challenge.getStatus() != ChallengeStatus.SCHEDULED) {
            log.debug("Challenge {} is not in SCHEDULED status, current status: {}", 
                    challenge.getId(), challenge.getStatus());
            return;
        }
        
        // 챌린지를 활성화
        Challenge activatedChallenge = challenge.toBuilder()
                .status(ChallengeStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        challengeRepository.save(activatedChallenge);
        
        log.info("Challenge {} activated successfully", challenge.getId());
    }
    
    private void completeChallenge(ChallengeSchedule schedule) {
        Challenge challenge = challengeRepository.findById(schedule.getChallengeId())
                .orElse(null);
        
        if (challenge == null) {
            log.warn("Challenge {} not found for schedule {}", schedule.getChallengeId(), schedule.getId());
            return;
        }
        
        if (challenge.getStatus() != ChallengeStatus.ACTIVE) {
            log.debug("Challenge {} is not in ACTIVE status, current status: {}", 
                    challenge.getId(), challenge.getStatus());
            return;
        }
        
        // 챌린지를 완료로 변경
        Challenge completedChallenge = challenge.toBuilder()
                .status(ChallengeStatus.COMPLETED)
                .endDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        challengeRepository.save(completedChallenge);
        
        log.info("Challenge {} completed successfully", challenge.getId());
    }
    
    private void processRecurringSchedule(ChallengeSchedule schedule, LocalDateTime now) {
        // 다음 실행 시간 계산
        LocalDateTime nextActivation = calculateNextActivationTime(schedule, now);
        
        if (nextActivation == null) {
            log.debug("No next activation time for recurring schedule {}", schedule.getId());
            return;
        }
        
        // 새로운 챌린지 인스턴스 생성이 필요한지 확인
        Challenge originalChallenge = challengeRepository.findById(schedule.getChallengeId())
                .orElse(null);
        
        if (originalChallenge == null) {
            log.warn("Original challenge {} not found for recurring schedule {}", 
                    schedule.getChallengeId(), schedule.getId());
            return;
        }
        
        // 새로운 챌린지 인스턴스 생성
        Challenge newChallenge = createRecurringChallengeInstance(originalChallenge, nextActivation);
        Challenge savedChallenge = challengeRepository.save(newChallenge);
        
        // 새로운 스케줄 생성
        ChallengeSchedule newSchedule = schedule.toBuilder()
                .id(null) // 새로운 ID 생성
                .challengeId(savedChallenge.getId())
                .activationDate(nextActivation)
                .deactivationDate(calculateDeactivationTime(nextActivation, originalChallenge))
                .createdAt(LocalDateTime.now())
                .build();
        
        scheduleRepository.save(newSchedule);
        
        log.info("Created recurring challenge instance {} with schedule {}", 
                savedChallenge.getId(), newSchedule.getId());
    }
    
    private LocalDateTime calculateNextActivationTime(ChallengeSchedule schedule, LocalDateTime from) {
        if (schedule.getRecurrencePattern() == null) {
            return null;
        }
        
        // CRON 표현식 파싱 및 다음 실행 시간 계산
        // 실제 구현에서는 CronExpression 라이브러리 사용
        // 예: 매주 월요일 오전 9시 = "0 0 9 * * MON"
        
        // 간단한 예시: 매주 반복
        if ("WEEKLY".equals(schedule.getRecurrencePattern())) {
            return from.plusWeeks(1);
        } else if ("DAILY".equals(schedule.getRecurrencePattern())) {
            return from.plusDays(1);
        } else if ("MONTHLY".equals(schedule.getRecurrencePattern())) {
            return from.plusMonths(1);
        }
        
        return null;
    }
    
    private Challenge createRecurringChallengeInstance(Challenge original, LocalDateTime startDate) {
        return original.toBuilder()
                .id(null) // 새로운 ID 생성
                .status(ChallengeStatus.SCHEDULED)
                .currentParticipants(0) // 참여자 수 초기화
                .startDate(startDate)
                .endDate(null) // 종료일은 스케줄에서 계산
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private LocalDateTime calculateDeactivationTime(LocalDateTime activationTime, Challenge challenge) {
        if (challenge.getEstimatedDurationMinutes() != null) {
            return activationTime.plusMinutes(challenge.getEstimatedDurationMinutes());
        }
        
        // 기본값: 활성화 후 7일
        return activationTime.plusDays(7);
    }
    
    /**
     * 수동으로 챌린지 스케줄 생성
     */
    @Transactional
    public ChallengeSchedule createSchedule(Long challengeId, LocalDateTime activationDate, 
                                          LocalDateTime deactivationDate, String recurrencePattern) {
        
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));
        
        ChallengeSchedule schedule = ChallengeSchedule.builder()
                .challengeId(challengeId)
                .scheduleType(recurrencePattern != null ? 
                        ChallengeSchedule.ScheduleType.RECURRING : 
                        ChallengeSchedule.ScheduleType.ONE_TIME)
                .recurrencePattern(recurrencePattern)
                .activationDate(activationDate)
                .deactivationDate(deactivationDate)
                .timezone(ZoneId.systemDefault().getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ChallengeSchedule savedSchedule = scheduleRepository.save(schedule);
        
        // 챌린지 상태를 SCHEDULED로 변경
        Challenge scheduledChallenge = challenge.toBuilder()
                .status(ChallengeStatus.SCHEDULED)
                .updatedAt(LocalDateTime.now())
                .build();
        
        challengeRepository.save(scheduledChallenge);
        
        log.info("Created schedule {} for challenge {}", savedSchedule.getId(), challengeId);
        
        return savedSchedule;
    }
    
    /**
     * 스케줄 취소
     */
    @Transactional
    public void cancelSchedule(Long scheduleId) {
        ChallengeSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));
        
        // 스케줄 비활성화
        ChallengeSchedule cancelledSchedule = schedule.toBuilder()
                .isActive(false)
                .updatedAt(LocalDateTime.now())
                .build();
        
        scheduleRepository.save(cancelledSchedule);
        
        log.info("Cancelled schedule {}", scheduleId);
    }
}