package com.stockquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StockQuest 메인 애플리케이션
 * 모의 투자 챌린지 학습 플랫폼
 */
@EnableScheduling
@EnableJpaAuditing
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {
    "com.stockquest.adapter.out.persistence.repository",
    "com.stockquest.adapter.out.persistence.company"
})
@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
public class StockQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockQuestApplication.class, args);
    }
}