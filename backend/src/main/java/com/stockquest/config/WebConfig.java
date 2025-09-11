package com.stockquest.config;

import com.stockquest.adapter.in.web.common.LoggingInterceptor;
// import com.stockquest.adapter.in.web.common.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * 인터셉터 등록 및 기타 웹 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // private final RateLimitInterceptor rateLimitInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 (가장 먼저 실행)
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .order(1);
        
        // Rate Limiting 인터셉터 - 임시 비활성화 (bucket4j 의존성 문제)
        // registry.addInterceptor(rateLimitInterceptor)
        //         .addPathPatterns("/api/**")
        //         .excludePathPatterns(
        //             "/api/public/**",
        //             "/actuator/health",
        //             "/actuator/info"
        //         )
        //         .order(2);
    }
}