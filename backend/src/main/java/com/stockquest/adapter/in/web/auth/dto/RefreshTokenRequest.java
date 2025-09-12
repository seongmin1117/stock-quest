package com.stockquest.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 리프레시 토큰 요청 DTO
 */
@Schema(description = "리프레시 토큰 요청")
public record RefreshTokenRequest(
    
    @Schema(description = "리프레시 토큰", required = true)
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    String refreshToken
) {}