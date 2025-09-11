package com.stockquest.application.leaderboard;

import com.stockquest.application.leaderboard.port.in.CalculateLeaderboardUseCase;
import com.stockquest.domain.leaderboard.LeaderboardEntry;
import com.stockquest.domain.leaderboard.port.LeaderboardRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 리더보드 계산 서비스
 * 챌린지 종료 시 참가자들의 수익률을 계산하여 순위를 매김
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CalculateLeaderboardService implements CalculateLeaderboardUseCase {
    
    private final LeaderboardRepository leaderboardRepository;
    private final ChallengeSessionRepository challengeSessionRepository;
    
    @Override
    @CacheEvict(value = "leaderboard", key = "#command.challengeId() + '*'", allEntries = true)
    public List<LeaderboardEntry> calculateLeaderboard(CalculateLeaderboardCommand command) {
        // 기존 리더보드 삭제 (재계산)
        leaderboardRepository.deleteByChallengeId(command.challengeId());
        
        // 완료된 세션들 조회
        List<ChallengeSession> completedSessions = challengeSessionRepository
                .findByChallengeIdAndStatus(command.challengeId(), SessionStatus.ENDED);
        
        if (completedSessions.isEmpty()) {
            return List.of();
        }
        
        // 수익률 기준 정렬 및 순위 부여
        List<LeaderboardEntry> entries = completedSessions.stream()
                .map(this::createLeaderboardEntry)
                .sorted(Comparator.comparing(LeaderboardEntry::getReturnPercentage).reversed())
                .collect(java.util.stream.Collectors.toList());
        
        // 순위 설정
        AtomicInteger rank = new AtomicInteger(1);
        entries.forEach(entry -> entry.updateRank(rank.getAndIncrement()));
        
        // 저장
        return leaderboardRepository.saveAll(entries);
    }
    
    private LeaderboardEntry createLeaderboardEntry(ChallengeSession session) {
        BigDecimal initialBalance = session.getInitialBalance();
        BigDecimal currentBalance = session.getCurrentBalance();
        
        // 손익 계산 (현재잔고 - 초기잔고)
        BigDecimal pnl = currentBalance.subtract(initialBalance);
        
        // 수익률 계산 ((현재잔고 - 초기잔고) / 초기잔고 * 100)
        BigDecimal returnPercentage = pnl.divide(initialBalance, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return new LeaderboardEntry(
                session.getChallengeId(),
                session.getId(),
                session.getUserId(),
                pnl,
                returnPercentage
        );
    }
}