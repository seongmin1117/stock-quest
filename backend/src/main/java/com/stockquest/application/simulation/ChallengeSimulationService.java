package com.stockquest.application.simulation;

import com.stockquest.application.leaderboard.CalculateLeaderboardService;
import com.stockquest.application.market.MarketDataService;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.market.PriceCandle;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 챌린지 시뮬레이션 엔진
 * 
 * 주요 기능:
 * 1. 활성 세션들의 시뮬레이션 진행 상태 관리
 * 2. Speed Factor에 따른 시간 가속 처리
 * 3. 시뮬레이션 완료 시 자동 세션 종료
 * 4. 시장 데이터 기반 실시간 포트폴리오 평가
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeSimulationService {
    
    private final ChallengeSessionRepository sessionRepository;
    private final ChallengeRepository challengeRepository;
    private final MarketDataService marketDataService;
    private final CalculateLeaderboardService leaderboardService;
    private final SimulationPortfolioService portfolioService;
    
    @Value("${stockquest.simulation.tick-interval:10000}") // 10초마다 실행
    private long tickIntervalMs;
    
    @Value("${stockquest.simulation.max-sessions-per-batch:50}")
    private int maxSessionsPerBatch;
    
    // 세션별 시뮬레이션 상태 관리 (메모리 캐시)
    private final Map<Long, SimulationState> sessionStates = new ConcurrentHashMap<>();
    
    /**
     * 메인 시뮬레이션 스케줄러
     * 매 10초마다 실행되어 활성 세션들의 시뮬레이션 진행
     */
    @Scheduled(fixedDelayString = "${stockquest.simulation.tick-interval:10000}")
    public void processSimulationTick() {
        try {
            log.debug("시뮬레이션 틱 처리 시작");
            
            // 1. 활성 세션 조회
            List<ChallengeSession> activeSessions = sessionRepository
                .findByStatus(SessionStatus.ACTIVE);
            
            if (activeSessions.isEmpty()) {
                log.debug("활성 세션 없음, 시뮬레이션 스킵");
                return;
            }
            
            log.info("시뮬레이션 처리 대상 세션: {}개", activeSessions.size());
            
            // 2. 배치 단위로 세션 처리
            int processed = 0;
            for (ChallengeSession session : activeSessions) {
                if (processed >= maxSessionsPerBatch) {
                    log.info("배치 처리 한도 도달, 다음 틱에서 계속: processed={}", processed);
                    break;
                }
                
                try {
                    processSessionTick(session);
                    processed++;
                } catch (Exception e) {
                    log.error("세션 시뮬레이션 처리 실패: sessionId={}, error={}", 
                             session.getId(), e.getMessage(), e);
                }
            }
            
            log.info("시뮬레이션 틱 처리 완료: processed={}", processed);
            
        } catch (Exception e) {
            log.error("시뮬레이션 틱 처리 중 전체 오류: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 개별 세션의 시뮬레이션 진행 처리
     */
    private void processSessionTick(ChallengeSession session) {
        Long sessionId = session.getId();
        
        // 1. 챌린지 정보 조회
        Challenge challenge = challengeRepository.findById(session.getChallengeId())
            .orElseThrow(() -> new IllegalArgumentException("챌린지 없음: " + session.getChallengeId()));
        
        if (challenge.getStatus() != ChallengeStatus.ACTIVE) {
            log.warn("비활성 챌린지의 세션 발견, 세션 종료: sessionId={}, challengeId={}", 
                    sessionId, challenge.getId());
            endSession(session, "챌린지 비활성화");
            return;
        }
        
        // 2. 시뮬레이션 상태 초기화 또는 업데이트
        SimulationState state = sessionStates.computeIfAbsent(sessionId, 
            k -> initializeSimulationState(session, challenge));
        
        // 3. Speed Factor에 따른 시간 진행 계산
        LocalDate currentSimulationDate = calculateCurrentSimulationDate(state, challenge);
        
        // 4. 시뮬레이션 완료 여부 확인
        if (currentSimulationDate.isAfter(challenge.getPeriodEnd()) || 
            currentSimulationDate.isEqual(challenge.getPeriodEnd())) {
            log.info("시뮬레이션 완료, 세션 종료: sessionId={}, 최종일={}", 
                    sessionId, currentSimulationDate);
            endSession(session, "시뮬레이션 완료");
            sessionStates.remove(sessionId);
            return;
        }
        
        // 5. 시뮬레이션 날짜 업데이트
        state.setCurrentSimulationDate(currentSimulationDate);
        state.setLastProcessedAt(LocalDateTime.now());
        
        // 6. 진행률 로깅 (10% 단위)
        logProgressIfNeeded(sessionId, state, challenge);
        
        log.debug("세션 시뮬레이션 진행: sessionId={}, 현재일={}, 진행률={}%", 
                 sessionId, currentSimulationDate, calculateProgressPercentage(state, challenge));
    }
    
    /**
     * 시뮬레이션 상태 초기화
     */
    private SimulationState initializeSimulationState(ChallengeSession session, Challenge challenge) {
        log.info("시뮬레이션 상태 초기화: sessionId={}, 시작일={}", 
                session.getId(), challenge.getPeriodStart());
        
        return SimulationState.builder()
            .sessionId(session.getId())
            .challengeId(challenge.getId())
            .speedFactor(challenge.getSpeedFactor())
            .periodStart(challenge.getPeriodStart())
            .periodEnd(challenge.getPeriodEnd())
            .currentSimulationDate(challenge.getPeriodStart())
            .simulationStartedAt(LocalDateTime.now())
            .lastProcessedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Speed Factor 기반 현재 시뮬레이션 날짜 계산
     * 
     * 로직:
     * - speedFactor = 1: 실시간 (1일 = 1일)  
     * - speedFactor = 10: 10배속 (1일 = 2.4시간)
     * - speedFactor = 100: 100배속 (1일 = 14.4분)
     */
    private LocalDate calculateCurrentSimulationDate(SimulationState state, Challenge challenge) {
        LocalDateTime now = LocalDateTime.now();
        
        // 시뮬레이션 시작부터 경과한 실제 시간 (밀리초)
        long elapsedRealTimeMs = ChronoUnit.MILLIS.between(state.getSimulationStartedAt(), now);
        
        // Speed Factor 적용된 시뮬레이션 시간 계산
        // 1일 = 86400000ms, speedFactor만큼 가속
        long simulationTimeMs = elapsedRealTimeMs * challenge.getSpeedFactor();
        long simulationDays = simulationTimeMs / 86400000L; // 밀리초를 일수로 변환
        
        LocalDate calculatedDate = state.getPeriodStart().plusDays(simulationDays);
        
        // 시뮬레이션 종료일을 넘지 않도록 제한
        if (calculatedDate.isAfter(challenge.getPeriodEnd())) {
            return challenge.getPeriodEnd();
        }
        
        return calculatedDate;
    }
    
    /**
     * 세션 종료 처리
     */
    private void endSession(ChallengeSession session, String reason) {
        log.info("세션 자동 종료 시작: sessionId={}, reason={}", session.getId(), reason);
        
        try {
            // 1. 챌린지 정보 조회
            Challenge challenge = challengeRepository.findById(session.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("챌린지 없음: " + session.getChallengeId()));
            
            // 2. 최종 포트폴리오 평가 (시뮬레이션 종료일 기준)
            SimulationState state = sessionStates.get(session.getId());
            LocalDate finalDate = state != null ? state.getCurrentSimulationDate() : challenge.getPeriodEnd();
            
            BigDecimal finalPortfolioValue = portfolioService.calculatePortfolioValue(
                session.getId(), challenge, finalDate);
            
            // 3. 최종 수익률 계산
            BigDecimal finalReturnRate = session.calculateReturnPercentage(finalPortfolioValue);
            
            // 4. 세션 종료
            session.end();
            sessionRepository.save(session);
            
            // 5. 랭킹 업데이트 시도 (비동기)
            try {
                // CalculateLeaderboardService의 기존 메소드 사용
                leaderboardService.calculateLeaderboard(new com.stockquest.application.leaderboard.port.in.CalculateLeaderboardUseCase.CalculateLeaderboardCommand(session.getChallengeId()));
                log.info("세션 종료 후 랭킹 업데이트 완료: challengeId={}", session.getChallengeId());
            } catch (Exception e) {
                log.warn("랭킹 업데이트 실패 (세션 종료는 성공): sessionId={}, error={}", 
                        session.getId(), e.getMessage());
            }
            
            // 6. 시뮬레이션 상태 정리
            sessionStates.remove(session.getId());
            
            log.info("세션 자동 종료 완료: sessionId={}, 최종포트폴리오={}, 수익률={}%", 
                    session.getId(), finalPortfolioValue, finalReturnRate);
            
        } catch (Exception e) {
            log.error("세션 종료 처리 실패: sessionId={}, error={}", 
                     session.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 진행률 계산
     */
    private int calculateProgressPercentage(SimulationState state, Challenge challenge) {
        long totalDays = ChronoUnit.DAYS.between(challenge.getPeriodStart(), challenge.getPeriodEnd());
        long elapsedDays = ChronoUnit.DAYS.between(challenge.getPeriodStart(), state.getCurrentSimulationDate());
        
        if (totalDays <= 0) return 100;
        
        return (int) Math.min(100, (elapsedDays * 100) / totalDays);
    }
    
    /**
     * 진행률 로깅 (10% 단위)
     */
    private void logProgressIfNeeded(Long sessionId, SimulationState state, Challenge challenge) {
        int currentProgress = calculateProgressPercentage(state, challenge);
        int lastLoggedProgress = state.getLastLoggedProgress();
        
        // 10% 단위로 진행률 로깅
        if (currentProgress >= lastLoggedProgress + 10) {
            log.info("시뮬레이션 진행: sessionId={}, 진행률={}%, 현재일={}", 
                    sessionId, currentProgress, state.getCurrentSimulationDate());
            state.setLastLoggedProgress((currentProgress / 10) * 10); // 10의 배수로 정규화
        }
    }
    
    /**
     * 특정 세션의 현재 시뮬레이션 상태 조회
     */
    public SimulationState getSimulationState(Long sessionId) {
        return sessionStates.get(sessionId);
    }
    
    /**
     * 모든 활성 시뮬레이션 상태 조회 (관리용)
     */
    public Map<Long, SimulationState> getAllSimulationStates() {
        return new HashMap<>(sessionStates);
    }
    
    /**
     * 시뮬레이션 상태 강제 정리 (메모리 누수 방지)
     * 매 시간마다 실행
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void cleanupStaleStates() {
        log.info("시뮬레이션 상태 정리 시작");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        int removed = 0;
        
        sessionStates.entrySet().removeIf(entry -> {
            SimulationState state = entry.getValue();
            if (state.getLastProcessedAt().isBefore(cutoffTime)) {
                log.debug("오래된 시뮬레이션 상태 제거: sessionId={}, lastProcessed={}", 
                         entry.getKey(), state.getLastProcessedAt());
                return true;
            }
            return false;
        });
        
        if (removed > 0) {
            log.info("시뮬레이션 상태 정리 완료: 제거된 상태={}개", removed);
        }
    }
    
    /**
     * 시뮬레이션 통계 로깅 (매 5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    public void logSimulationStatistics() {
        if (sessionStates.isEmpty()) {
            return;
        }
        
        int activeStates = sessionStates.size();
        
        // 평균 진행률 계산
        double avgProgress = sessionStates.values().stream()
            .mapToInt(state -> {
                Challenge challenge = challengeRepository.findById(state.getChallengeId()).orElse(null);
                return challenge != null ? calculateProgressPercentage(state, challenge) : 0;
            })
            .average()
            .orElse(0.0);
        
        log.info("시뮬레이션 통계: 활성세션={}개, 평균진행률={}%", activeStates, String.format("%.1f", avgProgress));
    }
}