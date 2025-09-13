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
) {
    
    /**
     * 간단한 에러 응답 생성용 빌더
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * ErrorResponse 빌더 클래스
     */
    public static class Builder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String error;
        private String message;
        private String path;
        private String traceId;
        private Map<String, String> validationErrors;
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        
        public Builder validationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, message, path, traceId, validationErrors);
        }
    }
}