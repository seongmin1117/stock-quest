package com.stockquest.adapter.in.web.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인증 응답 DTO
 */
@Schema(description = "인증 응답")
public record AuthResponse(
    
    @Schema(description = "JWT 인증 토큰")
    String token,
    
    @Schema(description = "사용자 ID")
    Long userId,
    
    @Schema(description = "사용자 이메일")
    String email,
    
    @Schema(description = "사용자 닉네임")
    String nickname
) {}