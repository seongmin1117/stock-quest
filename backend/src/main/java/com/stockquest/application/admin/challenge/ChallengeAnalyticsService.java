package com.stockquest.application.admin.challenge;

import com.stockquest.domain.challenge.ChallengeAnalytics;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.session.ChallengeSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * 챌린지 분석 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChallengeAnalyticsService {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository sessionRepository;
    
    /**
     * 챌린지 분석 데이터 계산
     */
    @Transactional
    public ChallengeAnalytics calculateAnalytics(Long challengeId) {
        log.info("Calculating analytics for challenge: {}", challengeId);
        
        // 해당 챌린지의 모든 세션 조회
        List<ChallengeSession> allSessions = sessionRepository.findByChallengeId(challengeId);
        List<ChallengeSession> completedSessions = allSessions.stream()
                .filter(session -> session.getStatus() == ChallengeSession.SessionStatus.COMPLETED)
                .toList();
        
        // 기본 통계 계산
        int totalParticipants = allSessions.size();
        int completedParticipants = completedSessions.size();
        
        // 수익률 통계 계산
        List<BigDecimal> returnRates = completedSessions.stream()
                .map(ChallengeSession::getReturnRate)
                .filter(rate -> rate != null)
                .toList();
        
        BigDecimal averageReturnRate = calculateAverageReturnRate(returnRates);
        BigDecimal medianReturnRate = calculateMedianReturnRate(returnRates);
        BigDecimal bestReturnRate = calculateBestReturnRate(returnRates);
        BigDecimal worstReturnRate = calculateWorstReturnRate(returnRates);
        
        // 완료 시간 통계 계산
        Integer averageCompletionTime = calculateAverageCompletionTime(completedSessions);
        
        // 성공률 계산
        BigDecimal successRate = calculateSuccessRate(totalParticipants, completedParticipants);
        
        // 참여도 점수 계산
        BigDecimal engagementScore = calculateEngagementScore(allSessions);
        
        // 분석 데이터 빌드
        ChallengeAnalytics analytics = ChallengeAnalytics.builder()
                .challengeId(challengeId)
                .totalParticipants(totalParticipants)
                .completedParticipants(completedParticipants)
                .averageReturnRate(averageReturnRate)
                .medianReturnRate(medianReturnRate)
                .bestReturnRate(bestReturnRate)
                .worstReturnRate(worstReturnRate)
                .averageCompletionTimeMinutes(averageCompletionTime)
                .successRatePercentage(successRate)
                .engagementScore(engagementScore)
                .lastCalculatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        log.info("Analytics calculated for challenge {}: {} participants, {}% success rate", 
                challengeId, totalParticipants, successRate);
        
        return analytics;
    }
    
    private BigDecimal calculateAverageReturnRate(List<BigDecimal> returnRates) {
        if (returnRates.isEmpty()) return BigDecimal.ZERO;
        
        return returnRates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returnRates.size()), 6, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateMedianReturnRate(List<BigDecimal> returnRates) {
        if (returnRates.isEmpty()) return BigDecimal.ZERO;
        
        List<BigDecimal> sortedRates = returnRates.stream()
                .sorted()
                .toList();
        
        int size = sortedRates.size();
        if (size % 2 == 1) {
            return sortedRates.get(size / 2);
        } else {
            BigDecimal mid1 = sortedRates.get(size / 2 - 1);
            BigDecimal mid2 = sortedRates.get(size / 2);
            return mid1.add(mid2).divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        }
    }
    
    private BigDecimal calculateBestReturnRate(List<BigDecimal> returnRates) {
        return returnRates.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    private BigDecimal calculateWorstReturnRate(List<BigDecimal> returnRates) {
        return returnRates.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    private Integer calculateAverageCompletionTime(List<ChallengeSession> completedSessions) {
        if (completedSessions.isEmpty()) return null;
        
        OptionalDouble avgTime = completedSessions.stream()
                .filter(session -> session.getStartedAt() != null && session.getCompletedAt() != null)
                .mapToLong(session -> {
                    return java.time.Duration.between(session.getStartedAt(), session.getCompletedAt()).toMinutes();
                })
                .average();
        
        return avgTime.isPresent() ? (int) avgTime.getAsDouble() : null;
    }
    
    private BigDecimal calculateSuccessRate(int total, int completed) {
        if (total == 0) return BigDecimal.ZERO;
        
        return BigDecimal.valueOf(completed)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateEngagementScore(List<ChallengeSession> allSessions) {
        if (allSessions.isEmpty()) return BigDecimal.ZERO;
        
        // 참여도 점수 = (완료율 * 0.6) + (평균 플레이 시간 기준 점수 * 0.4)
        double completionRate = allSessions.stream()
                .filter(session -> session.getStatus() == ChallengeSession.SessionStatus.COMPLETED)
                .count() / (double) allSessions.size();
        
        // 평균 플레이 시간 점수 (15분 이상이면 만점)
        double avgPlayTimeScore = Math.min(1.0, allSessions.stream()
                .filter(session -> session.getStartedAt() != null && 
                        (session.getCompletedAt() != null || session.getStatus() != ChallengeSession.SessionStatus.READY))
                .mapToLong(session -> {
                    LocalDateTime endTime = session.getCompletedAt() != null ? 
                            session.getCompletedAt() : LocalDateTime.now();
                    return java.time.Duration.between(session.getStartedAt(), endTime).toMinutes();
                })
                .average()
                .orElse(0.0) / 15.0); // 15분을 기준으로 정규화
        
        double engagementScore = (completionRate * 0.6) + (avgPlayTimeScore * 0.4);
        
        return BigDecimal.valueOf(engagementScore * 5) // 5점 만점으로 스케일
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 챌린지별 상위 퍼포머 조회
     */
    public List<ChallengeSession> getTopPerformers(Long challengeId, int limit) {
        return sessionRepository.findByChallengeIdOrderByReturnRateDesc(challengeId)
                .stream()
                .filter(session -> session.getStatus() == ChallengeSession.SessionStatus.COMPLETED)
                .filter(session -> session.getReturnRate() != null)
                .limit(limit)
                .toList();
    }
    
    /**
     * 챌린지 참여 트렌드 분석
     */
    public ParticipationTrend getParticipationTrend(Long challengeId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ChallengeSession> recentSessions = sessionRepository.findByChallengeIdAndCreatedAtAfter(challengeId, since);
        
        // 일별 참여자 수 계산
        long dailyParticipants = recentSessions.size() / Math.max(1, days);
        
        // 완료율 트렌드
        double recentCompletionRate = recentSessions.stream()
                .filter(session -> session.getStatus() == ChallengeSession.SessionStatus.COMPLETED)
                .count() / (double) Math.max(1, recentSessions.size());
        
        return ParticipationTrend.builder()
                .challengeId(challengeId)
                .periodDays(days)
                .totalParticipants(recentSessions.size())
                .dailyAverageParticipants(dailyParticipants)
                .completionRate(BigDecimal.valueOf(recentCompletionRate).setScale(4, RoundingMode.HALF_UP))
                .calculatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 참여 트렌드 정보
     */
    @lombok.Builder
    @lombok.Data
    public static class ParticipationTrend {
        private Long challengeId;
        private int periodDays;
        private int totalParticipants;
        private long dailyAverageParticipants;
        private BigDecimal completionRate;
        private LocalDateTime calculatedAt;
    }
}