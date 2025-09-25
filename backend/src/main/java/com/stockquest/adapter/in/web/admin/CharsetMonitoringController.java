package com.stockquest.adapter.in.web.admin;

import com.stockquest.config.monitoring.DatabaseCharsetMonitor;
import com.stockquest.config.monitoring.DatabaseCharsetScheduledMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 데이터베이스 인코딩 모니터링 API 컨트롤러 (임시 간소화 버전)
 *
 * TODO: Spring Boot 3.x actuator health dependencies 수정 후 원래 기능 복원
 */
@RestController
@RequestMapping("/api/admin/charset")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Charset Monitoring", description = "데이터베이스 인코딩 설정 모니터링 API")
public class CharsetMonitoringController {

    private final DatabaseCharsetMonitor charsetMonitor;

    /**
     * 현재 데이터베이스 인코딩 상태 조회 (간소화 버전)
     */
    @GetMapping("/status")
    @Operation(summary = "데이터베이스 인코딩 상태 조회", description = "현재 데이터베이스 인코딩 설정 상태를 조회합니다")
    public ResponseEntity<Map<String, Object>> getCharsetStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("status", "SIMPLIFIED");
        status.put("charset_valid", charsetMonitor.isCharsetValid());
        status.put("message", "Charset monitoring temporarily simplified - actuator dependencies need fixing");

        log.info("Charset status check requested - returning simplified status");
        return ResponseEntity.ok(status);
    }

    /**
     * 데이터베이스 인코딩 건강 상태 조회 (간소화 버전)
     */
    @GetMapping("/health")
    @Operation(summary = "인코딩 건강 상태 조회", description = "데이터베이스 인코딩 설정의 건강 상태를 조회합니다")
    public ResponseEntity<Map<String, Object>> getCharsetHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", charsetMonitor.isCharsetValid() ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        health.put("message", "Health check temporarily simplified - actuator dependencies need fixing");

        return ResponseEntity.ok(health);
    }

    /**
     * 수동 인코딩 검증 실행 (간소화 버전)
     */
    @PostMapping("/validate")
    @Operation(summary = "수동 인코딩 검증", description = "데이터베이스 인코딩 설정을 수동으로 검증합니다")
    public ResponseEntity<Map<String, Object>> validateCharset() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("valid", charsetMonitor.isCharsetValid());
        result.put("message", "Manual validation temporarily simplified - actuator dependencies need fixing");

        log.info("Manual charset validation requested - returning simplified result");
        return ResponseEntity.ok(result);
    }
}