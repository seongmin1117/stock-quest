package com.stockquest.application.simulation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 시뮬레이션 모니터링 및 관리 API
 * 개발/관리 목적의 시뮬레이션 상태 조회 및 제어
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/simulation")
@RequiredArgsConstructor
public class SimulationController {
    
    private final ChallengeSimulationService simulationService;
    
    /**
     * 특정 세션의 시뮬레이션 상태 조회
     */
    @GetMapping("/sessions/{sessionId}/state")
    public ResponseEntity<Map<String, Object>> getSessionSimulationState(
            @PathVariable Long sessionId) {
        
        log.info("세션 시뮬레이션 상태 조회: sessionId={}", sessionId);
        
        SimulationState state = simulationService.getSimulationState(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        
        if (state != null) {
            response.put("found", true);
            response.put("state", createStateResponse(state));
        } else {
            response.put("found", false);
            response.put("message", "해당 세션의 시뮬레이션 상태가 없습니다");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 모든 활성 시뮬레이션 상태 조회
     */
    @GetMapping("/states")
    public ResponseEntity<Map<String, Object>> getAllSimulationStates() {
        log.info("모든 시뮬레이션 상태 조회");
        
        Map<Long, SimulationState> allStates = simulationService.getAllSimulationStates();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalSessions", allStates.size());
        response.put("states", allStates.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    entry -> createStateResponse(entry.getValue())
                )));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 시뮬레이션 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSimulationStatistics() {
        log.info("시뮬레이션 통계 조회");
        
        Map<Long, SimulationState> allStates = simulationService.getAllSimulationStates();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", allStates.size());
        
        if (!allStates.isEmpty()) {
            // 평균 진행률
            double avgProgress = allStates.values().stream()
                .mapToDouble(SimulationState::calculateProgress)
                .average()
                .orElse(0.0);
            
            // Speed Factor 분포
            Map<Integer, Long> speedFactorDistribution = allStates.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SimulationState::getSpeedFactor,
                    java.util.stream.Collectors.counting()
                ));
            
            // 평균 실행 시간
            double avgElapsedMinutes = allStates.values().stream()
                .mapToLong(SimulationState::getElapsedRealTimeMinutes)
                .average()
                .orElse(0.0);
            
            stats.put("averageProgress", String.format("%.1f%%", avgProgress));
            stats.put("speedFactorDistribution", speedFactorDistribution);
            stats.put("averageElapsedMinutes", String.format("%.1f", avgElapsedMinutes));
            
            // 가장 진행이 빠른/느린 세션
            SimulationState fastest = allStates.values().stream()
                .max((s1, s2) -> Double.compare(s1.calculateProgress(), s2.calculateProgress()))
                .orElse(null);
            
            SimulationState slowest = allStates.values().stream()
                .min((s1, s2) -> Double.compare(s1.calculateProgress(), s2.calculateProgress()))
                .orElse(null);
            
            if (fastest != null) {
                stats.put("fastestSession", Map.of(
                    "sessionId", fastest.getSessionId(),
                    "progress", String.format("%.1f%%", fastest.calculateProgress())
                ));
            }
            
            if (slowest != null) {
                stats.put("slowestSession", Map.of(
                    "sessionId", slowest.getSessionId(),
                    "progress", String.format("%.1f%%", slowest.calculateProgress())
                ));
            }
        }
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 시뮬레이션 상태를 API 응답용 객체로 변환
     */
    private Map<String, Object> createStateResponse(SimulationState state) {
        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put("challengeId", state.getChallengeId());
        stateMap.put("speedFactor", state.getSpeedFactor());
        stateMap.put("periodStart", state.getPeriodStart());
        stateMap.put("periodEnd", state.getPeriodEnd());
        stateMap.put("currentSimulationDate", state.getCurrentSimulationDate());
        stateMap.put("progress", String.format("%.1f%%", state.calculateProgress()));
        stateMap.put("isCompleted", state.isCompleted());
        stateMap.put("simulationStartedAt", state.getSimulationStartedAt());
        stateMap.put("elapsedRealTimeMinutes", state.getElapsedRealTimeMinutes());
        stateMap.put("estimatedCompletionTime", state.getEstimatedCompletionTime());
        stateMap.put("summary", state.getSummary());
        
        return stateMap;
    }
}