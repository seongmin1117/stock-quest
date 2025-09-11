package com.stockquest.application.session.dto;

/**
 * 세션 상세 조회 쿼리
 */
public record GetSessionDetailQuery(
    Long sessionId,
    Long userId
) {
}