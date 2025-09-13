package com.stockquest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스 설정
 * OpenAPI 스펙 파일 등의 정적 파일을 서빙하기 위한 설정
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // OpenAPI 스펙 파일 서빙
        registry.addResourceHandler("/openapi/**")
                .addResourceLocations("classpath:/openapi/")
                .setCachePeriod(3600); // 1시간 캐시
        
        // 기본 정적 리소스
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 24시간 캐시
    }
}