package com.stockquest.adapter.in.web.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 통일된 에러 응답 DTO
 * API 에러 응답의 일관된 형태를 제공
 */
@Schema(description = "에러 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    
    @Schema(description = "에러 발생 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime timestamp,
    
    @Schema(description = "HTTP 상태 코드", example = "400")
    int status,
    
    @Schema(description = "에러 타입", example = "Validation Failed")
    String error,
    
    @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
    String message,
    
    @Schema(description = "요청 경로", example = "/api/auth/login")
    String path,
    
    @Schema(description = "추적 ID")
    String traceId,
    
    @Schema(description = "유효성 검사 에러 상세 정보")
    Map<String, String> validationErrors
) {}