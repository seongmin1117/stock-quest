package com.stockquest.adapter.in.web.dca;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DCA 시뮬레이션 에러 응답 DTO
 * API 에러 발생시 클라이언트에게 구조화된 에러 정보를 제공
 */
@Getter
@AllArgsConstructor
public class DCASimulationErrorResponse {

    /**
     * 에러 코드 (예: VALIDATION_ERROR, DATA_NOT_FOUND, INTERNAL_ERROR)
     */
    private final String errorCode;

    /**
     * 사용자에게 표시할 에러 메시지
     */
    private final String message;

    /**
     * 개발자용 상세 에러 정보 (옵션)
     */
    private final String details;

    /**
     * 에러 발생 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 요청된 경로
     */
    private final String path;

    /**
     * 기본 에러 응답 생성자 (간단한 에러용)
     */
    public DCASimulationErrorResponse(String errorCode, String message, String path) {
        this(errorCode, message, null, LocalDateTime.now(), path);
    }

    /**
     * 상세 에러 정보를 포함한 응답 생성자
     */
    public DCASimulationErrorResponse(String errorCode, String message, String details, String path) {
        this(errorCode, message, details, LocalDateTime.now(), path);
    }
}