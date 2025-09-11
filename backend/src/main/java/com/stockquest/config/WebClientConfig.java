package com.stockquest.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * Yahoo Finance API 연동을 위한 HTTP 클라이언트 구성
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    @Value("${yahoo-finance.timeout:5000}")
    private int timeoutMs;
    
    @Value("${yahoo-finance.connection-timeout:3000}")
    private int connectionTimeoutMs;
    
    @Value("${yahoo-finance.max-memory-size:10485760}") // 10MB
    private int maxMemorySize;
    
    /**
     * Yahoo Finance API 전용 WebClient 구성
     */
    @Bean
    public WebClient webClient() {
        // HTTP 클라이언트 설정
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs)
            .responseTimeout(Duration.ofMillis(timeoutMs))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
            );
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemorySize))
            .filter(logRequest())
            .filter(logResponse())
            .filter(handleErrors())
            .defaultHeader("User-Agent", "StockQuest/1.0")
            .defaultHeader("Accept", "application/json")
            .build();
    }
    
    /**
     * 요청 로깅 필터
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("WebClient 요청: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) -> 
                    log.debug("WebClient 요청 헤더: {}={}", name, values));
            }
            return Mono.just(clientRequest);
        });
    }
    
    /**
     * 응답 로깅 필터
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("WebClient 응답: {} {}", clientResponse.statusCode(), clientResponse.headers());
            }
            return Mono.just(clientResponse);
        });
    }
    
    /**
     * 에러 처리 필터
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.warn("WebClient HTTP 오류: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}