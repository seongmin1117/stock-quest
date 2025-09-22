package com.stockquest.config.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.boot.actuator.health.Status;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 데이터베이스 charset 설정 모니터링 컴포넌트
 *
 * 기능:
 * 1. 데이터베이스 charset/collation 설정 검증
 * 2. Spring Boot Actuator Health Indicator 제공
 * 3. 설정 변경 감지 및 경고 로그
 * 4. 운영 환경에서 charset 이슈 사전 방지
 */
@Component
@Slf4j
public class DatabaseCharsetMonitor implements HealthIndicator {

    private static final String EXPECTED_CHARSET = "utf8mb4";
    private static final String EXPECTED_COLLATION = "utf8mb4_unicode_ci";

    private final DataSource dataSource;

    @Autowired
    public DatabaseCharsetMonitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            boolean isHealthy = true;
            StringBuilder issues = new StringBuilder();

            // 1. 데이터베이스 charset 설정 검증
            Map<String, String> charsetConfig = checkCharsetConfiguration();
            details.put("charset_configuration", charsetConfig);

            for (Map.Entry<String, String> entry : charsetConfig.entrySet()) {
                String variable = entry.getKey();
                String value = entry.getValue();

                if (variable.contains("character_set") && !EXPECTED_CHARSET.equals(value)) {
                    isHealthy = false;
                    issues.append(String.format("%s=%s (expected=%s); ", variable, value, EXPECTED_CHARSET));
                    log.warn("⚠️ Charset Issue Detected: {} = {} (expected: {})",
                            variable, value, EXPECTED_CHARSET);
                }
            }

            // 2. Collation 설정 검증
            Map<String, String> collationConfig = checkCollationConfiguration();
            details.put("collation_configuration", collationConfig);

            for (Map.Entry<String, String> entry : collationConfig.entrySet()) {
                String variable = entry.getKey();
                String value = entry.getValue();

                if (variable.contains("collation") && !EXPECTED_COLLATION.equals(value)) {
                    isHealthy = false;
                    issues.append(String.format("%s=%s (expected=%s); ", variable, value, EXPECTED_COLLATION));
                    log.warn("⚠️ Collation Issue Detected: {} = {} (expected: {})",
                            variable, value, EXPECTED_COLLATION);
                }
            }

            // 3. 테이블별 charset 검증
            Map<String, String> tableCharsets = checkTableCharsets();
            details.put("table_charsets", tableCharsets);

            for (Map.Entry<String, String> entry : tableCharsets.entrySet()) {
                String tableName = entry.getKey();
                String charset = entry.getValue();

                if (!EXPECTED_CHARSET.equals(charset)) {
                    isHealthy = false;
                    issues.append(String.format("table_%s=%s; ", tableName, charset));
                    log.warn("⚠️ Table Charset Issue: {} has charset {} (expected: {})",
                            tableName, charset, EXPECTED_CHARSET);
                }
            }

            // 4. 한국어 텍스트 저장/조회 테스트
            boolean koreanTextTest = performKoreanTextTest();
            details.put("korean_text_test", koreanTextTest);

            if (!koreanTextTest) {
                isHealthy = false;
                issues.append("korean_text_test_failed; ");
                log.error("❌ Korean Text Test Failed: Cannot properly store/retrieve Korean text");
            }

            // 건강 상태 결정
            if (isHealthy) {
                log.debug("✅ Database charset monitoring: All checks passed");
                return Health.up()
                        .withDetail("status", "All charset configurations are correct")
                        .withDetail("charset", EXPECTED_CHARSET)
                        .withDetail("collation", EXPECTED_COLLATION)
                        .withDetails(details)
                        .build();
            } else {
                log.error("❌ Database charset monitoring: Issues detected - {}", issues.toString());
                return Health.down()
                        .withDetail("status", "Charset configuration issues detected")
                        .withDetail("issues", issues.toString())
                        .withDetail("expected_charset", EXPECTED_CHARSET)
                        .withDetail("expected_collation", EXPECTED_COLLATION)
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("❌ Database charset monitoring failed", e);
            return Health.down()
                    .withDetail("status", "Monitoring check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * 데이터베이스 charset 설정 확인
     */
    private Map<String, String> checkCharsetConfiguration() throws Exception {
        Map<String, String> config = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SHOW VARIABLES LIKE 'character_set_%'";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String variable = rs.getString("Variable_name");
                    String value = rs.getString("Value");
                    config.put(variable, value);
                }
            }
        }

        return config;
    }

    /**
     * 데이터베이스 collation 설정 확인
     */
    private Map<String, String> checkCollationConfiguration() throws Exception {
        Map<String, String> config = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SHOW VARIABLES LIKE 'collation_%'";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String variable = rs.getString("Variable_name");
                    String value = rs.getString("Value");
                    config.put(variable, value);
                }
            }
        }

        return config;
    }

    /**
     * 주요 테이블들의 charset 확인
     */
    private Map<String, String> checkTableCharsets() throws Exception {
        Map<String, String> tableCharsets = new HashMap<>();
        String[] importantTables = {"users", "challenge", "company", "challenge_instrument"};

        try (Connection connection = dataSource.getConnection()) {
            for (String tableName : importantTables) {
                try {
                    String sql = "SELECT TABLE_COLLATION FROM information_schema.TABLES " +
                               "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, tableName);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String collation = rs.getString("TABLE_COLLATION");
                                // collation에서 charset 추출 (예: utf8mb4_unicode_ci -> utf8mb4)
                                String charset = collation != null && collation.contains("_")
                                        ? collation.substring(0, collation.indexOf("_"))
                                        : collation;
                                tableCharsets.put(tableName, charset);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to check charset for table {}: {}", tableName, e.getMessage());
                    tableCharsets.put(tableName, "unknown");
                }
            }
        }

        return tableCharsets;
    }

    /**
     * 한국어 텍스트 저장/조회 테스트
     * 실제로 한국어 텍스트가 정상적으로 저장되고 조회되는지 확인
     */
    private boolean performKoreanTextTest() {
        try (Connection connection = dataSource.getConnection()) {
            // 임시 테이블 생성
            String createTableSql = "CREATE TEMPORARY TABLE charset_test_temp (" +
                                   "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                   "korean_text VARCHAR(255) NOT NULL" +
                                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            try (PreparedStatement createStmt = connection.prepareStatement(createTableSql)) {
                createStmt.execute();
            }

            // 한국어 텍스트 삽입
            String koreanText = "한국어 테스트 텍스트 🚀 이모지 포함";
            String insertSql = "INSERT INTO charset_test_temp (korean_text) VALUES (?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, koreanText);
                insertStmt.executeUpdate();
            }

            // 조회하여 검증
            String selectSql = "SELECT korean_text FROM charset_test_temp WHERE id = 1";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                if (rs.next()) {
                    String retrievedText = rs.getString("korean_text");
                    boolean testPassed = koreanText.equals(retrievedText);

                    if (!testPassed) {
                        log.error("Korean text test failed: expected='{}', actual='{}'",
                                koreanText, retrievedText);
                    }

                    return testPassed;
                }
            }

        } catch (Exception e) {
            log.error("Korean text test failed with exception", e);
            return false;
        }

        return false;
    }

    /**
     * 모니터링 정보를 로그로 출력 (주기적 호출용)
     */
    public void logCharsetStatus() {
        try {
            Health health = health();
            Status status = health.getStatus();

            if (Status.UP.equals(status)) {
                log.info("✅ Database charset monitoring: All systems operational");
            } else {
                log.warn("⚠️ Database charset monitoring: Issues detected - {}",
                        health.getDetails().get("issues"));
            }

        } catch (Exception e) {
            log.error("Failed to log charset status", e);
        }
    }
}