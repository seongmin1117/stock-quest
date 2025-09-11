package com.stockquest.performance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 성능 테스트 기본 클래스
 * 실제 환경과 유사한 조건에서 성능 측정
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance-test")
@Testcontainers
public abstract class PerformanceTestBase {
    
    // 성능 테스트용 컨테이너 (더 많은 리소스 할당)
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("stockquest_perf")
            .withUsername("perfuser")
            .withPassword("perfpass")
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--innodb-buffer-pool-size=256M",
                "--max-connections=1000"
            );
    
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--maxmemory", "128mb", "--maxmemory-policy", "allkeys-lru");
    
    protected ExecutorService executorService;
    protected PerformanceMetrics metrics;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 데이터베이스 설정
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        
        // HikariCP 성능 설정
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "10");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
        
        // Redis 설정
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.data.redis.lettuce.pool.max-active", () -> "20");
        
        // JPA 성능 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "50");
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
        
        // 로깅 최소화 (성능 영향 제거)
        registry.add("logging.level.org.hibernate.SQL", () -> "WARN");
        registry.add("logging.level.com.stockquest", () -> "WARN");
    }
    
    @BeforeAll
    static void setupContainers() {
        System.out.println("=== Performance Test Environment Starting ===");
        System.out.println("MySQL: " + mysqlContainer.getJdbcUrl());
        System.out.println("Redis: " + redisContainer.getHost() + ":" + redisContainer.getMappedPort(6379));
    }
    
    @BeforeEach
    void setUpPerformanceTest() {
        // Thread Pool 초기화 (CPU 코어 수 기반)
        int coreCount = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(Math.max(coreCount * 2, 8));
        
        // 성능 메트릭 초기화
        metrics = new PerformanceMetrics();
        
        System.out.println("Performance test setup - Threads: " + (coreCount * 2));
    }
    
    /**
     * 성능 테스트 실행 후 정리
     */
    protected void tearDownPerformanceTest() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 성능 메트릭 수집 클래스
     */
    protected static class PerformanceMetrics {
        private long totalOperations = 0;
        private long totalTime = 0;
        private long minTime = Long.MAX_VALUE;
        private long maxTime = 0;
        private long errorCount = 0;
        
        public synchronized void recordOperation(long executionTime, boolean success) {
            totalOperations++;
            if (success) {
                totalTime += executionTime;
                minTime = Math.min(minTime, executionTime);
                maxTime = Math.max(maxTime, executionTime);
            } else {
                errorCount++;
            }
        }
        
        public double getAverageTime() {
            long successfulOperations = totalOperations - errorCount;
            return successfulOperations > 0 ? (double) totalTime / successfulOperations : 0;
        }
        
        public double getThroughput(long durationMs) {
            return durationMs > 0 ? (double) totalOperations * 1000 / durationMs : 0;
        }
        
        public double getErrorRate() {
            return totalOperations > 0 ? (double) errorCount / totalOperations * 100 : 0;
        }
        
        public void printSummary(String operationName, long totalDurationMs) {
            System.out.println("=== Performance Test Results: " + operationName + " ===");
            System.out.println("Total Operations: " + totalOperations);
            System.out.println("Successful Operations: " + (totalOperations - errorCount));
            System.out.println("Failed Operations: " + errorCount);
            System.out.println("Error Rate: " + String.format("%.2f%%", getErrorRate()));
            System.out.println("Average Response Time: " + String.format("%.2f ms", getAverageTime()));
            System.out.println("Min Response Time: " + minTime + " ms");
            System.out.println("Max Response Time: " + maxTime + " ms");
            System.out.println("Throughput: " + String.format("%.2f ops/sec", getThroughput(totalDurationMs)));
            System.out.println("Total Duration: " + totalDurationMs + " ms");
            System.out.println("========================================");
        }
        
        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getErrorCount() { return errorCount; }
        public long getMinTime() { return minTime == Long.MAX_VALUE ? 0 : minTime; }
        public long getMaxTime() { return maxTime; }
    }
    
    /**
     * 성능 임계값 검증
     */
    protected void assertPerformanceThresholds(
            PerformanceMetrics metrics, 
            double maxAvgResponseTime, 
            double minThroughput,
            double maxErrorRate,
            long totalDurationMs) {
        
        double avgResponseTime = metrics.getAverageTime();
        double throughput = metrics.getThroughput(totalDurationMs);
        double errorRate = metrics.getErrorRate();
        
        if (avgResponseTime > maxAvgResponseTime) {
            throw new AssertionError(
                String.format("Average response time %.2fms exceeds threshold %.2fms",
                    avgResponseTime, maxAvgResponseTime));
        }
        
        if (throughput < minThroughput) {
            throw new AssertionError(
                String.format("Throughput %.2f ops/sec is below threshold %.2f ops/sec",
                    throughput, minThroughput));
        }
        
        if (errorRate > maxErrorRate) {
            throw new AssertionError(
                String.format("Error rate %.2f%% exceeds threshold %.2f%%",
                    errorRate, maxErrorRate));
        }
    }
}