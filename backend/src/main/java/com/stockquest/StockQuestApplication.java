package com.stockquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StockQuest 메인 애플리케이션
 * 모의 투자 챌린지 학습 플랫폼
 */
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class StockQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockQuestApplication.class, args);
    }
}