package com.stockquest.testutils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 테스트 기본 클래스
 * 공통 테스트 설정 및 유틸리티 제공
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class TestBase {
    
    @BeforeEach
    void setUpBase() {
        // 테스트 데이터 팩토리 카운터 리셋
        TestDataFactory.resetCounters();
        
        // 하위 클래스별 초기화
        setUp();
    }
    
    @AfterEach
    void tearDownBase() {
        // 하위 클래스별 정리
        tearDown();
    }
    
    /**
     * 하위 클래스에서 오버라이드할 초기화 메소드
     */
    protected void setUp() {
        // Override in subclasses if needed
    }
    
    /**
     * 하위 클래스에서 오버라이드할 정리 메소드
     */
    protected void tearDown() {
        // Override in subclasses if needed
    }
}