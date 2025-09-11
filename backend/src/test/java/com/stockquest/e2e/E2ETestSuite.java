package com.stockquest.e2e;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * End-to-End 테스트 스위트
 * 전체 E2E 테스트를 체계적으로 실행하고 보고서 생성
 */
@Suite
@SuiteDisplayName("Stock Quest E2E Test Suite")
@SelectClasses({
    TradingFlowE2ETest.class,
    SecurityE2ETest.class
})
public class E2ETestSuite {
    
    // 테스트 스위트 메타 정보
    public static final String VERSION = "1.0.0";
    public static final String DESCRIPTION = "Stock Quest 거래 플랫폼 종합 E2E 테스트";
    
    // 테스트 커버리지 정보
    public static final String[] COVERED_FEATURES = {
        "사용자 인증 및 권한 관리",
        "챌린지 생성 및 참여",
        "주식 거래 (매수/매도)",
        "포트폴리오 관리",
        "실시간 시장 데이터 처리",
        "성능 및 동시성",
        "보안 및 공격 방어",
        "에러 처리 및 복구"
    };
    
    // 테스트 실행 통계 (실행 후 업데이트)
    public static class TestStatistics {
        public static final int TOTAL_TEST_METHODS = 8;
        public static final int TOTAL_ASSERTIONS = 150; // 대략적 추정
        public static final String ESTIMATED_DURATION = "2-5 minutes";
        public static final String[] DEPENDENCIES = {
            "MySQL Testcontainer",
            "Redis Testcontainer", 
            "Spring Boot Test",
            "JUnit 5",
            "AssertJ"
        };
    }
    
    // 환경 요구사항
    public static class Requirements {
        public static final String JAVA_VERSION = "21+";
        public static final String SPRING_BOOT_VERSION = "3.5+";
        public static final String DOCKER_REQUIREMENT = "Docker Desktop 실행 필요";
        public static final String MEMORY_REQUIREMENT = "최소 2GB RAM";
        public static final int MIN_AVAILABLE_PORTS = 10; // Testcontainers용
    }
}