package com.stockquest;

import com.stockquest.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 컨텍스트 로딩 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ContextLoadTest {
    
    @Test
    void contextLoads() {
        // Spring 컨텍스트가 정상적으로 로딩되는지 테스트
    }
}