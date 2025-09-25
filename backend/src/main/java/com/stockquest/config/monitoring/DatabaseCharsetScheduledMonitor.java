package com.stockquest.config.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 charset 설정 주기적 모니터링 서비스 (임시 비활성화)
 *
 * TODO: Fix Spring Boot 3.x actuator health dependencies and re-enable scheduled monitoring
 */
@Component
@ConditionalOnProperty(
    value = "monitoring.charset.enabled",
    havingValue = "true",
    matchIfMissing = false  // Disabled by default due to actuator issues
)
@Slf4j
public class DatabaseCharsetScheduledMonitor {

    private final DatabaseCharsetMonitor charsetMonitor;

    @Autowired
    public DatabaseCharsetScheduledMonitor(DatabaseCharsetMonitor charsetMonitor) {
        this.charsetMonitor = charsetMonitor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateCharsetOnStartup() {
        log.info("Charset monitoring temporarily simplified - actuator dependencies need fixing");
        // Simplified validation
        if (charsetMonitor.isCharsetValid()) {
            log.info("Database charset configuration is valid");
        }
    }

    @Scheduled(fixedDelay = 3600000)  // 1시간마다
    public void validateCharsetPeriodically() {
        // Temporarily disabled - will be re-enabled when actuator issues are fixed
        log.debug("Periodic charset validation skipped - actuator dependencies need fixing");
    }
}