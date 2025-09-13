package com.stockquest.adapter.in.web;

import com.stockquest.application.session.SessionManagementService;
import com.stockquest.application.session.SessionManagementService.SessionStatusInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

/**
 * 세션 관리 컨트롤러
 * 관리자 기능 및 디버깅 목적으로 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@Tag(name = "Session Management", description = "세션 관리 API (관리자용)")
public class SessionManagementController {
    
    private final SessionManagementService sessionManagementService;
    
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 세션 히스토리 조회", description = "특정 사용자의 모든 세션 참여 이력을 조회합니다")
    public ResponseEntity<List<SessionStatusInfo>> getUserSessionHistory(
            @Parameter(description = "사용자 ID") @PathVariable @NotNull @Positive Long userId) {
        
        log.info("사용자 세션 히스토리 조회 요청: userId={}", userId);
        var history = sessionManagementService.getUserSessionHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/{sessionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "세션 상태 조회", description = "특정 세션의 상태 정보를 조회합니다")
    public ResponseEntity<SessionStatusInfo> getSessionStatus(
            @Parameter(description = "세션 ID") @PathVariable @NotNull @Positive Long sessionId) {
        
        log.info("세션 상태 조회 요청: sessionId={}", sessionId);
        return sessionManagementService.getSessionStatus(sessionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users/{userId}/challenges/{challengeId}/force-end")
    @Operation(summary = "활성 세션 강제 종료", description = "사용자의 특정 챌린지 활성 세션을 강제로 종료합니다")
    public ResponseEntity<Map<String, Object>> forceEndActiveSession(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "종료 사유") @RequestParam(defaultValue = "관리자 강제 종료") String reason) {
        
        log.info("활성 세션 강제 종료 요청: userId={}, challengeId={}, reason={}", userId, challengeId, reason);
        
        var endedSession = sessionManagementService.forceEndActiveSession(userId, challengeId, reason);
        
        if (endedSession.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "세션이 성공적으로 종료되었습니다",
                "sessionId", endedSession.get().getId(),
                "reason", reason
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "종료할 활성 세션이 없습니다"
            ));
        }
    }
    
    @PostMapping("/users/{userId}/force-end-all")
    @Operation(summary = "사용자의 모든 활성 세션 강제 종료", description = "사용자의 모든 활성 세션을 강제로 종료합니다")
    public ResponseEntity<Map<String, Object>> forceEndAllActiveSessionsByUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "종료 사유") @RequestParam(defaultValue = "관리자 일괄 종료") String reason) {
        
        log.info("사용자 모든 활성 세션 강제 종료 요청: userId={}, reason={}", userId, reason);
        
        int endedCount = sessionManagementService.forceEndAllActiveSessionsByUser(userId, reason);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", String.format("%d개 세션이 종료되었습니다", endedCount),
            "endedCount", endedCount,
            "reason", reason
        ));
    }
    
    @PostMapping("/cleanup/stale-ready")
    @Operation(summary = "오래된 READY 세션 정리", description = "지정된 시간 이상 READY 상태로 유지된 세션들을 정리합니다")
    public ResponseEntity<Map<String, Object>> cleanupStaleReadySessions(
            @Parameter(description = "정리 기준 시간(시간 단위)") @RequestParam(defaultValue = "24") int cutoffHours) {
        
        log.info("오래된 READY 세션 정리 요청: cutoffHours={}", cutoffHours);
        
        int cleanedCount = sessionManagementService.cleanupStaleReadySessions(cutoffHours);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", String.format("%d개 오래된 세션이 정리되었습니다", cleanedCount),
            "cleanedCount", cleanedCount,
            "cutoffHours", cutoffHours
        ));
    }
}