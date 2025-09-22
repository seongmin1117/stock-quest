package com.stockquest.application.event;

import com.stockquest.application.leaderboard.port.in.CalculateLeaderboardUseCase;
import com.stockquest.domain.event.ChallengeCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 리더보드 관련 이벤트 핸들러
 * 챌린지 완료 이벤트를 수신하여 리더보드를 자동 계산
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardEventHandler {

    private final CalculateLeaderboardUseCase calculateLeaderboardUseCase;

    /**
     * 챌린지 완료 이벤트 처리
     * 비동기로 리더보드를 계산하여 성능 영향을 최소화
     */
    @Async
    @EventListener
    public void handleChallengeCompleted(ChallengeCompletedEvent event) {
        log.info("챌린지 완료 이벤트 수신: challengeId={}, sessionId={}, userId={}",
                event.challengeId(), event.sessionId(), event.userId());

        try {
            // 리더보드 계산 실행
            var command = new CalculateLeaderboardUseCase.CalculateLeaderboardCommand(event.challengeId());
            var leaderboardEntries = calculateLeaderboardUseCase.calculateLeaderboard(command);

            log.info("이벤트 기반 리더보드 계산 완료: challengeId={}, 엔트리 수={}",
                    event.challengeId(), leaderboardEntries.size());

        } catch (Exception e) {
            log.error("이벤트 기반 리더보드 계산 실패: challengeId={}", event.challengeId(), e);
            // 이벤트 처리 실패가 다른 시스템에 영향을 주지 않도록 예외를 삼킴
        }
    }
}