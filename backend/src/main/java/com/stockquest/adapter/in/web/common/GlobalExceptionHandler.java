package com.stockquest.adapter.in.web.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.stockquest.application.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 애플리케이션의 모든 예외를 통합적으로 처리하고 일관된 응답 형태 제공
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 도메인 예외 처리
     */
    @ExceptionHandler({
        UserNotFoundException.class,
        ChallengeNotFoundException.class,
        SessionNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleEntityNotFound(DomainException ex, HttpServletRequest request) {
        logError(ex, request, "ENTITY_NOT_FOUND");
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler({
        InvalidOrderException.class,
        InsufficientFundsException.class,
        InvalidChallengeStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessLogicError(DomainException ex, HttpServletRequest request) {
        logError(ex, request, "BUSINESS_LOGIC_ERROR");
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Business Logic Error",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException ex, HttpServletRequest request) {
        logError(ex, request, "AUTHENTICATION_ERROR");
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "잘못된 인증 정보입니다.",
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logError(ex, request, "ACCESS_DENIED");
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "접근 권한이 없습니다.",
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * JSR-303 유효성 검사 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logError(ex, request, "VALIDATION_ERROR");
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "입력값 검증에 실패했습니다.",
            request.getRequestURI(),
            MDC.get("traceId"),
            validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        logError(ex, request, "CONSTRAINT_VIOLATION");
        
        Map<String, String> validationErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            validationErrors.put(propertyPath, message);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Constraint Violation",
            "제약 조건 위반입니다.",
            request.getRequestURI(),
            MDC.get("traceId"),
            validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * JSON 파싱 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        System.err.println("=== JSON PARSE ERROR ===");
        System.err.println("Request URI: " + request.getRequestURI());
        System.err.println("Error message: " + ex.getMessage());
        System.err.println("Root cause: " + (ex.getCause() != null ? ex.getCause().getMessage() : "null"));
        ex.printStackTrace();
        logError(ex, request, "JSON_PARSE_ERROR");
        
        String message = "잘못된 JSON 형식입니다.";
        
        // 더 구체적인 에러 메시지 제공
        if (ex.getCause() instanceof MismatchedInputException mismatchedInputException) {
            String fieldName = mismatchedInputException.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(name -> name != null)
                .collect(Collectors.joining("."));
            
            if (!fieldName.isEmpty()) {
                message = String.format("필드 '%s'의 형식이 올바르지 않습니다.", fieldName);
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON",
            message,
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 모든 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {
        logError(ex, request, "INTERNAL_SERVER_ERROR");
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            request.getRequestURI(),
            MDC.get("traceId"),
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 에러 로깅 헬퍼 메서드
     */
    private void logError(Exception ex, HttpServletRequest request, String errorType) {
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        log.error("Error occurred - Type: {}, Path: {}, Method: {}, IP: {}, User-Agent: {}, Message: {}",
            errorType,
            request.getRequestURI(),
            request.getMethod(),
            clientIp,
            userAgent,
            ex.getMessage(),
            ex
        );
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}