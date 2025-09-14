package com.stockquest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson JSON 파싱 설정
 * 특수 문자 처리 및 기타 JSON 파싱 문제 해결
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .featuresToEnable(
                    // JSON 파싱 시 특수 문자 허용
                    JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
                    // 백슬래시 이스케이프 허용
                    JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                    // 단일 따옴표 허용
                    JsonParser.Feature.ALLOW_SINGLE_QUOTES
                )
                .featuresToDisable(
                    // 알려지지 않은 속성 무시
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    // 빈 문자열을 null로 처리하지 않음
                    DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                    // LocalDateTime을 배열로 직렬화하지 않고 ISO-8601 문자열로 직렬화
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .modules(new JavaTimeModule())
                .build();
    }
}