package com.stockquest.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
// // import org.springframework.boot.actuator.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring configuration for StockQuest database and cache operations.
 * 
 * <p>Provides comprehensive performance instrumentation to validate the optimization
 * targets defined in issue #16:
 * 
 * <ul>
 *   <li>Portfolio calculations: &lt; 500ms response time</li>
 *   <li>Leaderboard calculations: &lt; 2000ms response time</li>
 *   <li>Database queries: 95th percentile &lt; 100ms</li>
 *   <li>Cache hit rates: &gt; 90% for portfolio operations</li>
 * </ul>
 * 
 * <p>Metrics are exposed via Micrometer and can be monitored through:
 * <ul>
 *   <li>Spring Boot Actuator endpoints (/actuator/metrics)</li>
 *   <li>Prometheus integration for production monitoring</li>
 *   <li>Application logs with performance thresholds</li>
 * </ul>
 * 
 * @author StockQuest Performance Team
 * @since 1.0
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.boot.actuator.metrics.cache.CacheMetricsRegistrar
 */
@Configuration
public class PerformanceMonitoringConfig {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final CacheManager cacheManager;

    // Performance thresholds from issue #16
    private static final Duration PORTFOLIO_CALCULATION_THRESHOLD = Duration.ofMillis(500);
    private static final Duration LEADERBOARD_CALCULATION_THRESHOLD = Duration.ofMillis(2000);
    private static final Duration DATABASE_QUERY_THRESHOLD = Duration.ofMillis(100);
    private static final double MIN_CACHE_HIT_RATE = 0.90;

    /**
     * Constructs performance monitoring configuration with required dependencies.
     * 
     * @param meterRegistry Micrometer registry for metric collection
     * @param dataSource Database connection pool for query monitoring
     * @param cacheManager Redis cache manager for cache performance tracking
     */
    public PerformanceMonitoringConfig(MeterRegistry meterRegistry, 
                                     DataSource dataSource, 
                                     CacheManager cacheManager) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.cacheManager = cacheManager;
    }

    /**
     * Database query performance timer for portfolio operations.
     * 
     * <p>Tracks query execution times with percentile histograms to monitor
     * the 95th percentile performance target of &lt; 100ms.
     * 
     * @return Timer for portfolio database queries
     */
    @Bean
    public Timer portfolioQueryTimer() {
        return Timer.builder("stockquest.db.portfolio.query.duration")
                .description("Database query execution time for portfolio operations")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(meterRegistry);
    }

    /**
     * Leaderboard calculation performance timer.
     * 
     * <p>Monitors end-to-end leaderboard calculation performance including
     * database queries, cache operations, and ranking algorithms.
     * Target: &lt; 2000ms for full leaderboard refresh.
     * 
     * @return Timer for leaderboard calculations
     */
    @Bean
    public Timer leaderboardCalculationTimer() {
        return Timer.builder("stockquest.leaderboard.calculation.duration")
                .description("End-to-end leaderboard calculation time")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(100))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .register(meterRegistry);
    }

    /**
     * Portfolio calculation performance timer.
     * 
     * <p>Tracks individual portfolio value calculation performance.
     * Target: &lt; 500ms per portfolio calculation.
     * 
     * @return Timer for portfolio calculations
     */
    @Bean
    public Timer portfolioCalculationTimer() {
        return Timer.builder("stockquest.portfolio.calculation.duration")
                .description("Individual portfolio calculation time")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .register(meterRegistry);
    }

    /**
     * Cache performance metrics registration.
     * 
     * <p>Automatically registers cache hit rate and miss rate metrics
     * for all configured cache regions to monitor cache effectiveness.
     * 
     * @return CacheMetricsRegistrar for automatic cache metrics collection
     */
    // @Bean
    // public CacheMetricsRegistrar cacheMetricsRegistrar() {
    //     CacheMetricsRegistrar registrar = new CacheMetricsRegistrar(meterRegistry);
    //     
    //     // Register all cache regions for monitoring
    //     if (cacheManager != null) {
    //         cacheManager.getCacheNames().forEach(cacheName -> {
    //             org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
    //             if (cache != null) {
    //                 registrar.bindCacheToRegistry(cache, "stockquest.cache");
    //             }
    //         });
    //     }
    //     
    //     return registrar;
    // }

    /**
     * Database connection pool monitoring setup.
     * 
     * <p>Monitors HikariCP connection pool metrics including active connections,
     * idle connections, and connection acquisition time.
     * 
     * @param event Application ready event to trigger monitoring setup
     */
    @EventListener
    public void setupConnectionPoolMonitoring(ApplicationReadyEvent event) {
        // HikariCP automatically registers metrics with Micrometer
        // This method ensures the metrics are properly tagged
        meterRegistry.gauge("stockquest.db.connections.active", 
            dataSource, ds -> getActiveConnectionCount(ds));
        meterRegistry.gauge("stockquest.db.connections.idle", 
            dataSource, ds -> getIdleConnectionCount(ds));
    }

    /**
     * Performance validation and alerting.
     * 
     * <p>Performs initial performance validation against the target thresholds
     * and sets up monitoring alerts for production environments.
     * 
     * @param event Application ready event to trigger validation
     */
    @EventListener
    public void validatePerformanceTargets(ApplicationReadyEvent event) {
        // Run basic performance validation
        validateDatabasePerformance();
        validateCacheConfiguration();
        
        // Log performance monitoring setup
        System.out.println("=== StockQuest Performance Monitoring Initialized ===");
        System.out.println("Portfolio calculation target: < " + PORTFOLIO_CALCULATION_THRESHOLD.toMillis() + "ms");
        System.out.println("Leaderboard calculation target: < " + LEADERBOARD_CALCULATION_THRESHOLD.toMillis() + "ms");
        System.out.println("Database query target (95th percentile): < " + DATABASE_QUERY_THRESHOLD.toMillis() + "ms");
        System.out.println("Cache hit rate target: > " + (MIN_CACHE_HIT_RATE * 100) + "%");
        System.out.println("Metrics endpoint: /actuator/metrics");
        System.out.println("======================================================");
    }

    /**
     * Validates database performance against targets through sample queries.
     */
    private void validateDatabasePerformance() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try (Connection connection = dataSource.getConnection()) {
            // Test basic database connectivity and response time
            Instant start = Instant.now();
            
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int tableCount = rs.getInt(1);
                
                Duration queryTime = Duration.between(start, Instant.now());
                
                if (queryTime.compareTo(DATABASE_QUERY_THRESHOLD) > 0) {
                    System.err.println("WARNING: Database query exceeded threshold: " + 
                        queryTime.toMillis() + "ms > " + DATABASE_QUERY_THRESHOLD.toMillis() + "ms");
                } else {
                    System.out.println("Database performance validation passed: " + 
                        queryTime.toMillis() + "ms (Tables: " + tableCount + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Database performance validation failed: " + e.getMessage());
        } finally {
            sample.stop(portfolioQueryTimer());
        }
    }

    /**
     * Validates cache configuration and connectivity.
     */
    private void validateCacheConfiguration() {
        if (cacheManager != null) {
            System.out.println("Cache regions configured: " + cacheManager.getCacheNames());
            
            // Test basic cache functionality
            cacheManager.getCacheNames().forEach(cacheName -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // Test cache put/get
                    cache.put("test-key", "test-value");
                    org.springframework.cache.Cache.ValueWrapper value = cache.get("test-key");
                    
                    if (value != null && "test-value".equals(value.get())) {
                        System.out.println("Cache validation passed for region: " + cacheName);
                    } else {
                        System.err.println("WARNING: Cache validation failed for region: " + cacheName);
                    }
                    
                    // Clean up test data
                    cache.evict("test-key");
                }
            });
        }
    }

    /**
     * Gets active connection count from HikariCP pool.
     * 
     * @param dataSource HikariDataSource instance
     * @return Current active connection count
     */
    private int getActiveConnectionCount(DataSource dataSource) {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                return ((com.zaxxer.hikari.HikariDataSource) dataSource).getHikariPoolMXBean().getActiveConnections();
            }
        } catch (Exception e) {
            // Ignore, return 0 if unable to get metrics
        }
        return 0;
    }

    /**
     * Gets idle connection count from HikariCP pool.
     * 
     * @param dataSource HikariDataSource instance  
     * @return Current idle connection count
     */
    private int getIdleConnectionCount(DataSource dataSource) {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                return ((com.zaxxer.hikari.HikariDataSource) dataSource).getHikariPoolMXBean().getIdleConnections();
            }
        } catch (Exception e) {
            // Ignore, return 0 if unable to get metrics
        }
        return 0;
    }
}