package com.stockquest.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트 기본 클래스
 * Testcontainers를 이용한 실제 DB/Redis 환경 구성
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Testcontainers
public abstract class IntegrationTestBase {
    
    // MySQL 테스트 컨테이너
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("stockquest_test")
            .withUsername("testuser")
            .withPassword("testpass")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
    
    // Redis 테스트 컨테이너
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    
    /**
     * 동적 프로퍼티 설정
     * 테스트 컨테이너의 동적 포트를 Spring 설정에 주입
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        
        // Redis 설정
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        
        // 테스트 전용 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("logging.level.org.hibernate.SQL", () -> "DEBUG");
    }
    
    @BeforeAll
    static void setupContainers() {
        // 컨테이너 시작 확인
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        
        if (!redisContainer.isRunning()) {
            redisContainer.start();
        }
        
        System.out.println("=== Integration Test Containers Started ===");
        System.out.println("MySQL URL: " + mysqlContainer.getJdbcUrl());
        System.out.println("Redis Host:Port: " + redisContainer.getHost() + ":" + redisContainer.getMappedPort(6379));
    }
}