package com.stockquest.application.dashboard;

import com.stockquest.application.dashboard.dto.DashboardData;
import com.stockquest.application.dashboard.dto.RecentSession;
import com.stockquest.application.dashboard.dto.UserStats;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * 사용자 대시보드 데이터 서비스
 * 사용자의 통계 정보와 최근 세션 정보를 제공
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final ChallengeSessionRepository sessionRepository;
    private final ChallengeRepository challengeRepository;

    /**
     * 사용자 대시보드 데이터 조회
     */
    public DashboardData getDashboardData(Long userId) {
        log.info("대시보드 데이터 조회 시작: userId={}", userId);

        // 사용자의 모든 세션 조회
        List<ChallengeSession> allSessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);

        // 사용자 통계 계산
        UserStats userStats = calculateUserStats(allSessions);

        // 최근 세션 조회 (최대 5개)
        List<RecentSession> recentSessions = getRecentSessions(allSessions, 5);

        log.info("대시보드 데이터 조회 완료: userId={}, totalSessions={}, recentSessions={}",
                userId, allSessions.size(), recentSessions.size());

        return DashboardData.builder()
                .userStats(userStats)
                .recentSessions(recentSessions)
                .build();
    }

    /**
     * 사용자 통계 계산
     */
    private UserStats calculateUserStats(List<ChallengeSession> sessions) {
        if (sessions.isEmpty()) {
            return UserStats.builder()
                    .totalSessions(0)
                    .activeSessions(0)
                    .completedSessions(0)
                    .averageReturn(0.0)
                    .bestReturn(0.0)
                    .worstReturn(0.0)
                    .totalReturn(0.0)
                    .winRate(0.0)
                    .build();
        }

        int totalSessions = sessions.size();

        long activeSessions = sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .count();

        long completedSessions = sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .count();

        List<ChallengeSession> finishedSessions = sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED ||
                                 session.getStatus() == SessionStatus.CANCELLED)
                .collect(Collectors.toList());

        double averageReturn = 0.0;
        double bestReturn = 0.0;
        double worstReturn = 0.0;
        double totalReturn = 0.0;
        double winRate = 0.0;

        if (!finishedSessions.isEmpty()) {
            OptionalDouble avgReturn = finishedSessions.stream()
                    .mapToDouble(this::calculateReturnRate)
                    .average();
            averageReturn = avgReturn.orElse(0.0);

            bestReturn = finishedSessions.stream()
                    .mapToDouble(this::calculateReturnRate)
                    .max()
                    .orElse(0.0);

            worstReturn = finishedSessions.stream()
                    .mapToDouble(this::calculateReturnRate)
                    .min()
                    .orElse(0.0);

            totalReturn = finishedSessions.stream()
                    .mapToDouble(this::calculateReturnRate)
                    .sum();

            long winningSessions = finishedSessions.stream()
                    .filter(session -> calculateReturnRate(session) > 0)
                    .count();
            winRate = (double) winningSessions / finishedSessions.size() * 100.0;
        }

        return UserStats.builder()
                .totalSessions(totalSessions)
                .activeSessions((int) activeSessions)
                .completedSessions((int) completedSessions)
                .averageReturn(averageReturn)
                .bestReturn(bestReturn)
                .worstReturn(worstReturn)
                .totalReturn(totalReturn)
                .winRate(winRate)
                .build();
    }

    /**
     * 최근 세션 목록 생성
     */
    private List<RecentSession> getRecentSessions(List<ChallengeSession> sessions, int limit) {
        return sessions.stream()
                .limit(limit)
                .map(this::convertToRecentSession)
                .collect(Collectors.toList());
    }

    /**
     * ChallengeSession을 RecentSession으로 변환
     */
    private RecentSession convertToRecentSession(ChallengeSession session) {
        String challengeTitle = "알 수 없는 챌린지";
        try {
            challengeTitle = challengeRepository.findById(session.getChallengeId())
                    .map(challenge -> challenge.getTitle())
                    .orElse("알 수 없는 챌린지");
        } catch (Exception e) {
            log.warn("챌린지 제목 조회 실패: challengeId={}", session.getChallengeId(), e);
        }

        return RecentSession.builder()
                .id(session.getId().intValue())
                .challengeId(session.getChallengeId().intValue())
                .challengeTitle(challengeTitle)
                .status(convertToFrontendStatus(session.getStatus()))
                .progress(calculateProgress(session))
                .currentBalance(session.getCurrentBalance().doubleValue())
                .returnRate(calculateReturnRate(session))
                .startedAt(session.getStartedAt() != null ? session.getStartedAt().toString() : session.getCreatedAt().toString())
                .completedAt(session.getCompletedAt() != null ? session.getCompletedAt().toString() : null)
                .build();
    }

    /**
     * SessionStatus를 프론트엔드 형식으로 변환
     */
    private String convertToFrontendStatus(SessionStatus status) {
        switch (status) {
            case READY: return "READY";
            case ACTIVE: return "ACTIVE";
            case COMPLETED: return "COMPLETED";
            case CANCELLED: return "CANCELLED";
            case ENDED: return "ENDED";
            default: return status.name();
        }
    }

    /**
     * 진행률 계산 (임시 구현)
     */
    private int calculateProgress(ChallengeSession session) {
        if (session.getStatus() == SessionStatus.COMPLETED ||
            session.getStatus() == SessionStatus.CANCELLED ||
            session.getStatus() == SessionStatus.ENDED) {
            return 100;
        }

        if (session.getStatus() == SessionStatus.ACTIVE) {
            // TODO: 실제 챌린지 진행률 계산 로직 구현
            // 현재는 임의의 값 반환 (65%)
            return 65;
        }

        return 0;
    }

    /**
     * 수익률 계산
     */
    private double calculateReturnRate(ChallengeSession session) {
        BigDecimal initialBalance = session.getInitialBalance();
        BigDecimal currentBalance = session.getCurrentBalance();

        if (initialBalance.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal returnAmount = currentBalance.subtract(initialBalance);
        BigDecimal returnRate = returnAmount.divide(initialBalance, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return returnRate.doubleValue();
    }
}