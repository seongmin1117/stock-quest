package com.stockquest.config.performance;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JPA/Hibernate 쿼리 최적화 설정
 * N+1 문제 해결 및 배치 처리 최적화
 */
@Configuration
public class QueryOptimizationConfig {
    
    @Value("${spring.jpa.hibernate.batch-size:25}")
    private int batchSize;
    
    @Value("${spring.jpa.hibernate.fetch-size:50}")
    private int fetchSize;
    
    @Value("${spring.jpa.hibernate.order-inserts:true}")
    private boolean orderInserts;
    
    @Value("${spring.jpa.hibernate.order-updates:true}")
    private boolean orderUpdates;
    
    @Value("${spring.jpa.hibernate.batch-versioned-data:true}")
    private boolean batchVersionedData;
    
    /**
     * Hibernate 성능 최적화 설정
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // 배치 처리 최적화
            hibernateProperties.put(AvailableSettings.STATEMENT_BATCH_SIZE, batchSize);
            hibernateProperties.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, fetchSize);
            hibernateProperties.put(AvailableSettings.ORDER_INSERTS, orderInserts);
            hibernateProperties.put(AvailableSettings.ORDER_UPDATES, orderUpdates);
            hibernateProperties.put(AvailableSettings.BATCH_VERSIONED_DATA, batchVersionedData);
            
            // JDBC 배치 처리 활성화
            hibernateProperties.put(AvailableSettings.STATEMENT_BATCH_SIZE, batchSize);
            hibernateProperties.put("hibernate.jdbc.batch_versioned_data", batchVersionedData);
            
            // 쿼리 최적화 - 캐시 비활성화
            hibernateProperties.put(AvailableSettings.USE_QUERY_CACHE, false);
            hibernateProperties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
            
            // 통계 수집 (개발 환경에서만)
            if (isDevEnvironment()) {
                hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, true);
                hibernateProperties.put(AvailableSettings.LOG_SLOW_QUERY, 1000); // 1초 이상 쿼리 로깅
            }
            
            // 커넥션 풀 최적화
            hibernateProperties.put(AvailableSettings.CONNECTION_HANDLING, "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");
            
            // 스키마 검증 비활성화
            hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, "none");
            
            // 네이밍 전략
            hibernateProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, 
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        };
    }
    
    private boolean isDevEnvironment() {
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        return activeProfiles.contains("dev");
    }
}