package com.stockquest.config;

import com.stockquest.application.admin.challenge.ChallengeTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챌린지 관리 시스템 설정
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ChallengeManagementConfig {
    
    private final ChallengeTemplateService templateService;
    
    /**
     * 애플리케이션 시작 시 기본 데이터 초기화
     */
    @Bean
    @Transactional
    public CommandLineRunner initializeChallengeSystem() {
        return args -> {
            log.info("Initializing challenge management system...");
            
            try {
                // 기본 챌린지 템플릿 생성
                templateService.createDefaultTemplates();
                log.info("Challenge management system initialized successfully");
            } catch (Exception e) {
                log.warn("Some components of challenge management system failed to initialize: {}", e.getMessage());
                // 초기화 실패가 애플리케이션 시작을 막지 않도록 예외를 삼킴
            }
        };
    }
}