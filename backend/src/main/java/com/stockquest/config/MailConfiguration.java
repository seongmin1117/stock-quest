package com.stockquest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 메일 설정
 */
@Configuration
public class MailConfiguration {
    
    /**
     * JavaMailSender Bean 설정
     * 개발 환경에서는 실제 메일 발송 없이 로그만 출력하는 Mock 설정
     */
    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 개발용 설정 - 실제 메일 발송하지 않음
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setUsername("");
        mailSender.setPassword("");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
    
    /**
     * 실제 메일 발송을 위한 설정 (필요시 활성화)
     */
    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
    public JavaMailSender realJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 실제 SMTP 서버 설정 (환경 변수에서 가져와야 함)
        mailSender.setHost("${spring.mail.host:smtp.gmail.com}");
        mailSender.setPort(587);
        mailSender.setUsername("${spring.mail.username:}");
        mailSender.setPassword("${spring.mail.password:}");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}