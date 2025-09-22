package com.stockquest.config.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 charset 설정 주기적 모니터링 서비스
 *
 * 기능:
 * 1. 애플리케이션 시작 시 초기 charset 검증
 * 2. 주기적(1시간마다) charset 설정 검증
 * 3. 설정 변경 감지 시 경고 로그 출력
 * 4. 운영 환경에서 charset 드리프트 방지
 */
@Component
@Slf4j
@ConditionalOnProperty(
    name = "stockquest.monitoring.charset.enabled",
    havingValue = "true",
    matchIfMissing = true  // 기본값: 활성화
)
public class DatabaseCharsetScheduledMonitor {

    private final DatabaseCharsetMonitor charsetMonitor;
    private boolean isFirstCheck = true;

    @Autowired
    public DatabaseCharsetScheduledMonitor(DatabaseCharsetMonitor charsetMonitor) {
        this.charsetMonitor = charsetMonitor;
    }

    /**
     * 애플리케이션 시작 완료 후 초기 charset 검증
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🔍 Starting initial database charset verification...");
        performCharsetCheck("Application Startup");
        isFirstCheck = false;
    }

    /**
     * 1시간마다 charset 설정 검증
     * cron: 매시 정각 (0분 0초)에 실행
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledCharsetCheck() {
        log.debug("🔍 Performing scheduled database charset check...");
        performCharsetCheck("Scheduled Check");
    }

    /**
     * 매일 오전 9시에 상세 charset 상태 리포트 생성
     * 운영팀이 확인할 수 있도록 상세한 로그 출력
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyCharsetStatusReport() {
        log.info("📊 === Daily Database Charset Status Report ===");
        performCharsetCheck("Daily Report");
        log.info("📊 === End of Daily Charset Report ===");
    }

    /**
     * charset 검증 수행
     */
    private void performCharsetCheck(String checkType) {
        try {
            var health = charsetMonitor.health();
            var status = health.getStatus();

            if ("UP".equals(status.getCode())) {
                if (isFirstCheck) {
                    log.info("✅ [{}] Database charset configuration verified successfully", checkType);
                    log.info("✅ [{}] Expected charset: utf8mb4, collation: utf8mb4_unicode_ci", checkType);
                } else {
                    log.debug("✅ [{}] Database charset configuration: OK", checkType);
                }
            } else {
                // 문제 발견 시 경고 로그
                log.error("❌ [{}] Database charset configuration issues detected!", checkType);
                log.error("❌ [{}] Status: {}", checkType, status.getCode());

                var details = health.getDetails();
                if (details.containsKey("issues")) {
                    log.error("❌ [{}] Issues: {}", checkType, details.get("issues"));
                }

                // 각 설정 카테고리별 상세 정보 로깅
                logConfigurationDetails(checkType, details);

                // 운영팀 알림을 위한 특별 로그 (로그 모니터링 시스템에서 감지 가능)
                log.error("🚨 CHARSET_MONITORING_ALERT: Database charset configuration drift detected - {}", checkType);
            }

        } catch (Exception e) {
            log.error("❌ [{}] Failed to perform charset check", checkType, e);
            log.error("🚨 CHARSET_MONITORING_ERROR: Charset monitoring system failure - {}", checkType);
        }
    }

    /**
     * 설정 세부 정보 로깅
     */
    private void logConfigurationDetails(String checkType, Object details) {
        if (!(details instanceof java.util.Map)) {
            return;
        }

        @SuppressWarnings("unchecked")
        var detailsMap = (java.util.Map<String, Object>) details;

        // Charset 설정 상세 정보
        if (detailsMap.containsKey("charset_configuration")) {
            log.warn("📋 [{}] Charset Configuration Details:", checkType);
            @SuppressWarnings("unchecked")
            var charsetConfig = (java.util.Map<String, String>) detailsMap.get("charset_configuration");
            charsetConfig.forEach((key, value) -> {
                if (key.contains("character_set")) {
                    String status = "utf8mb4".equals(value) ? "✅" : "❌";
                    log.warn("📋 [{}]   {} {} = {}", checkType, status, key, value);
                }
            });
        }

        // Collation 설정 상세 정보
        if (detailsMap.containsKey("collation_configuration")) {
            log.warn("📋 [{}] Collation Configuration Details:", checkType);
            @SuppressWarnings("unchecked")
            var collationConfig = (java.util.Map<String, String>) detailsMap.get("collation_configuration");
            collationConfig.forEach((key, value) -> {
                if (key.contains("collation")) {
                    String status = "utf8mb4_unicode_ci".equals(value) ? "✅" : "❌";
                    log.warn("📋 [{}]   {} {} = {}", checkType, status, key, value);
                }
            });
        }

        // 테이블별 charset 정보
        if (detailsMap.containsKey("table_charsets")) {
            log.warn("📋 [{}] Table Charset Details:", checkType);
            @SuppressWarnings("unchecked")
            var tableCharsets = (java.util.Map<String, String>) detailsMap.get("table_charsets");
            tableCharsets.forEach((table, charset) -> {
                String status = "utf8mb4".equals(charset) ? "✅" : "❌";
                log.warn("📋 [{}]   {} Table '{}' = {}", checkType, status, table, charset);
            });
        }

        // 한국어 텍스트 테스트 결과
        if (detailsMap.containsKey("korean_text_test")) {
            Boolean koreanTextTest = (Boolean) detailsMap.get("korean_text_test");
            String status = Boolean.TRUE.equals(koreanTextTest) ? "✅" : "❌";
            log.warn("📋 [{}]   {} Korean Text Test: {}", checkType, status,
                    Boolean.TRUE.equals(koreanTextTest) ? "PASSED" : "FAILED");
        }
    }

    /**
     * 수동 charset 검증 트리거 (관리자용)
     * JMX나 관리 엔드포인트에서 호출 가능
     */
    public void triggerManualCheck() {
        log.info("🔧 Manual charset check triggered by administrator");
        performCharsetCheck("Manual Check");
    }

    /**
     * 모니터링 상태 정보 반환
     */
    public String getMonitoringStatus() {
        try {
            var health = charsetMonitor.health();
            return String.format("Charset Monitoring Status: %s", health.getStatus().getCode());
        } catch (Exception e) {
            return "Charset Monitoring Status: ERROR - " + e.getMessage();
        }
    }
}