package com.stockquest.adapter.in.web.admin;

import com.stockquest.config.monitoring.DatabaseCharsetMonitor;
import com.stockquest.config.monitoring.DatabaseCharsetScheduledMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuator.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 데이터베이스 charset 모니터링 관리 컨트롤러
 *
 * 기능:
 * 1. 관리자용 charset 상태 조회 API
 * 2. 수동 charset 검증 트리거 API
 * 3. 모니터링 설정 조회/변경 API
 * 4. charset 이슈 해결 가이드 제공
 */
@RestController
@RequestMapping("/api/admin/charset-monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "데이터베이스 Charset 모니터링", description = "데이터베이스 charset 설정 모니터링 관리 API")
public class CharsetMonitoringController {

    private final DatabaseCharsetMonitor charsetMonitor;
    private final DatabaseCharsetScheduledMonitor scheduledMonitor;

    @GetMapping("/status")
    @Operation(summary = "Charset 모니터링 상태 조회", description = "현재 데이터베이스 charset 설정 상태를 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCharsetStatus() {
        try {
            Health health = charsetMonitor.health();

            return ResponseEntity.ok(Map.of(
                "timestamp", LocalDateTime.now(),
                "charset_status", health.getStatus().getCode(),
                "details", health.getDetails(),
                "monitoring_status", scheduledMonitor.getMonitoringStatus(),
                "expected_charset", "utf8mb4",
                "expected_collation", "utf8mb4_unicode_ci"
            ));

        } catch (Exception e) {
            log.error("Failed to get charset status", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Failed to retrieve charset status",
                        "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/check")
    @Operation(summary = "수동 charset 검증 실행", description = "즉시 데이터베이스 charset 설정을 검증합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerCharsetCheck() {
        try {
            log.info("Manual charset check triggered by admin via API");

            // 수동 검증 실행
            scheduledMonitor.triggerManualCheck();

            // 결과 조회
            Health health = charsetMonitor.health();

            return ResponseEntity.ok(Map.of(
                "timestamp", LocalDateTime.now(),
                "check_result", "completed",
                "charset_status", health.getStatus().getCode(),
                "details", health.getDetails(),
                "message", "Manual charset check completed successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to trigger manual charset check", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "check_result", "failed",
                        "error", "Failed to trigger charset check",
                        "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Charset Health Check", description = "Spring Boot Actuator Health Indicator 결과를 반환합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Health> getCharsetHealth() {
        try {
            Health health = charsetMonitor.health();
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Failed to get charset health", e);
            return ResponseEntity.internalServerError()
                    .body(Health.down()
                        .withDetail("error", e.getMessage())
                        .withDetail("timestamp", LocalDateTime.now())
                        .build());
        }
    }

    @GetMapping("/troubleshooting-guide")
    @Operation(summary = "Charset 문제 해결 가이드", description = "charset 관련 문제 발생 시 해결 방법을 제공합니다")
    public ResponseEntity<Map<String, Object>> getTroubleshootingGuide() {
        return ResponseEntity.ok(Map.of(
            "title", "Database Charset Troubleshooting Guide",
            "common_issues", Map.of(
                "incorrect_charset", Map.of(
                    "symptom", "Database charset is not utf8mb4",
                    "solution", "ALTER DATABASE stockquest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
                    "prevention", "Ensure docker-compose.yml includes --character-set-server=utf8mb4"
                ),
                "incorrect_table_charset", Map.of(
                    "symptom", "Table charset is not utf8mb4",
                    "solution", "ALTER TABLE table_name CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
                    "prevention", "Use migration template with ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                ),
                "korean_text_corruption", Map.of(
                    "symptom", "Korean text displays as question marks or boxes",
                    "solution", "Check connection string includes useUnicode=true&characterEncoding=UTF-8",
                    "prevention", "Always use utf8mb4 charset and test with Korean text"
                ),
                "connection_charset_issues", Map.of(
                    "symptom", "Data corruption during save/load operations",
                    "solution", "Add connection-init-sql: 'SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci'",
                    "prevention", "Include HikariCP connection-init-sql in all environments"
                )
            ),
            "verification_commands", Map.of(
                "check_database_charset", "SHOW VARIABLES LIKE 'character_set_%';",
                "check_database_collation", "SHOW VARIABLES LIKE 'collation_%';",
                "check_table_charset", "SHOW CREATE TABLE table_name;",
                "test_korean_text", "SELECT '한국어 테스트' as test_korean;"
            ),
            "configuration_examples", Map.of(
                "datasource_url", "jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                "hikaricp_init_sql", "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci",
                "docker_mysql_command", "--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci",
                "migration_template", "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            ),
            "monitoring_endpoints", Map.of(
                "health_check", "/api/admin/charset-monitoring/health",
                "detailed_status", "/api/admin/charset-monitoring/status",
                "manual_check", "POST /api/admin/charset-monitoring/check",
                "actuator_health", "/actuator/health/databaseCharsetMonitor"
            )
        ));
    }

    @GetMapping("/configuration-summary")
    @Operation(summary = "현재 charset 설정 요약", description = "현재 시스템의 charset 설정을 요약하여 제공합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfigurationSummary() {
        try {
            Health health = charsetMonitor.health();
            var details = health.getDetails();

            return ResponseEntity.ok(Map.of(
                "summary", Map.of(
                    "overall_status", health.getStatus().getCode(),
                    "charset_compliant", "UP".equals(health.getStatus().getCode()),
                    "monitoring_active", true,
                    "korean_text_support", details.getOrDefault("korean_text_test", false)
                ),
                "current_configuration", Map.of(
                    "database_charset", extractCharsetFromDetails(details, "charset_configuration"),
                    "database_collation", extractCollationFromDetails(details, "collation_configuration"),
                    "table_charsets", details.getOrDefault("table_charsets", Map.of()),
                    "korean_text_test_result", details.getOrDefault("korean_text_test", false)
                ),
                "recommendations", generateRecommendations(health),
                "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("Failed to get configuration summary", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "error", "Failed to retrieve configuration summary",
                        "message", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Health details에서 charset 정보 추출
     */
    private Map<String, String> extractCharsetFromDetails(Object details, String key) {
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            var detailsMap = (Map<String, Object>) details;
            if (detailsMap.containsKey(key)) {
                @SuppressWarnings("unchecked")
                var charsetConfig = (Map<String, String>) detailsMap.get(key);
                return charsetConfig.entrySet().stream()
                        .filter(entry -> entry.getKey().contains("character_set"))
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ));
            }
        }
        return Map.of();
    }

    /**
     * Health details에서 collation 정보 추출
     */
    private Map<String, String> extractCollationFromDetails(Object details, String key) {
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            var detailsMap = (Map<String, Object>) details;
            if (detailsMap.containsKey(key)) {
                @SuppressWarnings("unchecked")
                var collationConfig = (Map<String, String>) detailsMap.get(key);
                return collationConfig.entrySet().stream()
                        .filter(entry -> entry.getKey().contains("collation"))
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ));
            }
        }
        return Map.of();
    }

    /**
     * Health 상태에 따른 권장사항 생성
     */
    private java.util.List<String> generateRecommendations(Health health) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        if (!"UP".equals(health.getStatus().getCode())) {
            recommendations.add("시스템에 charset 관련 문제가 감지되었습니다. 즉시 조치가 필요합니다.");
            recommendations.add("troubleshooting-guide 엔드포인트를 참조하여 문제를 해결하세요.");
            recommendations.add("문제 해결 후 manual check를 실행하여 재검증하세요.");
        } else {
            recommendations.add("모든 charset 설정이 정상입니다.");
            recommendations.add("정기적인 모니터링이 계속 수행되고 있습니다.");
            recommendations.add("새로운 마이그레이션 작성 시 템플릿을 사용하세요.");
        }

        return recommendations;
    }
}