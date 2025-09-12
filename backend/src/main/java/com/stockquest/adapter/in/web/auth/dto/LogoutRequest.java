package com.stockquest.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그아웃 요청 DTO
 */
@Schema(description = "로그아웃 요청")
public record LogoutRequest(
    
    @Schema(description = "리프레시 토큰 (제공시 해당 토큰만 폐기, 미제공시 모든 토큰 폐기)", nullable = true)
    String refreshToken,
    
    @Schema(description = "모든 디바이스에서 로그아웃 여부", defaultValue = "false")
    Boolean logoutFromAllDevices
) {
    
    public LogoutRequest {
        if (logoutFromAllDevices == null) {
            logoutFromAllDevices = false;
        }
    }
}