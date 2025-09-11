package com.stockquest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 로깅 설정
 * 요청/응답 로깅 및 구조화된 로그 포맷 설정
 */
@Configuration
public class LoggingConfig {

    /**
     * HTTP 요청 로깅 필터
     * 모든 HTTP 요청과 응답을 로깅
     */
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10240); // 10KB
        filter.setIncludeHeaders(false); // 보안상 헤더는 제외
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        
        return filter;
    }
}