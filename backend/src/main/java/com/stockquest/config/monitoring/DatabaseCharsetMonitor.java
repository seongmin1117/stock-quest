package com.stockquest.config.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 charset 설정 모니터링 컴포넌트 (임시 비활성화)
 *
 * TODO: Fix Spring Boot 3.x actuator health indicator dependencies and re-enable monitoring
 * Original implementation moved to DatabaseCharsetMonitor.java.disabled
 */
@Component
@Slf4j
public class DatabaseCharsetMonitor {

    public DatabaseCharsetMonitor() {
        log.info("DatabaseCharsetMonitor temporarily disabled - actuator health dependencies need fixing");
    }

    // Temporary stub methods to maintain compatibility
    public boolean isCharsetValid() {
        // Temporary implementation - always returns true
        return true;
    }
}