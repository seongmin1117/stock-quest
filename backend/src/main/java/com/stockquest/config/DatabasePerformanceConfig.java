package com.stockquest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Database performance optimization configuration for StockQuest trading operations.
 * 
 * <p>This configuration provides profile-specific HikariCP connection pool optimizations
 * to handle high-frequency portfolio calculations and real-time trading operations.
 * 
 * <p>Performance targets:
 * <ul>
 *   <li>Production: Support 1000+ concurrent portfolio calculations</li>
 *   <li>Portfolio query response time: &lt; 100ms for 95th percentile</li>
 *   <li>Leaderboard calculation: &lt; 2000ms for full refresh</li>
 * </ul>
 * 
 * @author StockQuest Performance Team
 * @since 1.0
 * @see com.stockquest.domain.portfolio.port.PortfolioRepository
 */
@Configuration
public class DatabasePerformanceConfig {
    
    /**
     * Production-optimized database connection pool with aggressive performance tuning.
     * 
     * <p>Configured for high-load trading operations with optimized connection pooling,
     * prepared statement caching, and MySQL-specific performance enhancements.
     * 
     * <p>Key optimizations:
     * <ul>
     *   <li>50 max connections for concurrent portfolio calculations</li>
     *   <li>500 prepared statement cache size for complex queries</li>
     *   <li>Batch statement rewriting for bulk operations</li>
     *   <li>Connection leak detection for reliability</li>
     * </ul>
     * 
     * @return Fully configured HikariDataSource for production environment
     * @throws SQLException if database connection cannot be established
     * @see HikariConfig
     */
    @Bean
    @Profile("prod")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource productionDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Connection pool optimization for high-load trading
        config.setMaximumPoolSize(50); // 동시 거래 처리를 위한 충분한 connection pool
        config.setMinimumIdle(10); // 빠른 응답을 위한 minimum idle connections
        config.setConnectionTimeout(3000); // 3초 connection timeout
        config.setIdleTimeout(300000); // 5분 idle timeout
        config.setMaxLifetime(900000); // 15분 max connection lifetime
        config.setLeakDetectionThreshold(60000); // 1분 leak detection
        
        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500"); // Portfolio queries caching
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // MySQL-specific optimizations for portfolio calculations
        config.addDataSourceProperty("zeroDateTimeBehavior", "convertToNull");
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("requireSSL", "true");
        config.addDataSourceProperty("verifyServerCertificate", "false");
        
        return new HikariDataSource(config);
    }
    
    /**
     * Development-optimized database connection pool for local development.
     * 
     * <p>Provides balanced performance settings suitable for development environments
     * with reduced resource usage while maintaining reasonable performance.
     * 
     * <p>Configuration highlights:
     * <ul>
     *   <li>10 max connections for moderate concurrent testing</li>
     *   <li>100 prepared statement cache for common queries</li>
     *   <li>Extended timeouts for debugging sessions</li>
     *   <li>Development-friendly connection lifecycle</li>
     * </ul>
     * 
     * @return HikariDataSource configured for development environment
     * @throws SQLException if database connection cannot be established
     */
    @Bean
    @Profile({"dev", "local"})
    public DataSource developmentDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Smaller pool size for development
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(600000); // 10분
        config.setMaxLifetime(1200000); // 20분
        
        // Development-friendly settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "100");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }
    
    /**
     * Test-optimized database connection pool for unit and integration testing.
     * 
     * <p>Minimal configuration designed for fast test execution with quick startup
     * times and reduced resource consumption during test runs.
     * 
     * <p>Test-specific optimizations:
     * <ul>
     *   <li>5 max connections for test isolation</li>
     *   <li>Minimal cache sizes for fast startup</li>
     *   <li>Short connection lifetimes for test cleanup</li>
     *   <li>Disabled prepared statement caching for simplicity</li>
     * </ul>
     * 
     * @return Lightweight HikariDataSource for test environment
     * @throws SQLException if test database connection fails
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Minimal pool for testing
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(1000);
        config.setIdleTimeout(60000); // 1분
        config.setMaxLifetime(300000); // 5분
        
        // Fast startup for tests
        config.addDataSourceProperty("cachePrepStmts", "false");
        config.addDataSourceProperty("useServerPrepStmts", "false");
        
        return new HikariDataSource(config);
    }
}